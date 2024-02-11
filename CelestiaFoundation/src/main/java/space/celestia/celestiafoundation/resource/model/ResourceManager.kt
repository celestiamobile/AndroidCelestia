/*
 * ResourceManager.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.celestiafoundation.resource.model

import android.icu.text.Collator
import android.icu.text.RuleBasedCollator
import android.os.Build
import android.util.Log
import com.google.gson.GsonBuilder
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import space.celestia.ziputils.ZipUtils
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.lang.ref.WeakReference
import kotlin.coroutines.coroutineContext
import kotlin.math.max

fun <P, R> CoroutineScope.executeAsyncTask(
    doInBackground: suspend (suspend (P) -> Unit) -> R,
    onPostExecute: (R) -> Unit,
    onProgressUpdate: (P) -> Unit
) = launch {
    val result = doInBackground {
        withContext(Dispatchers.Main) { onProgressUpdate(it) }
    }
    withContext(Dispatchers.Main) { onPostExecute(result) }
}

class ResourceManager {
    private var listeners = arrayListOf<Listener>()
    private var tasks = hashMapOf<String, Job>()

    private val parentJob = Job()

    var addonDirectory: String? = null
    var scriptDirectory: String? = null

    enum class State {
        Downloading, Downloaded
    }

    class Progress(val state: State, val finished: Long, val total: Long)

    interface Listener {
        fun onProgressUpdate(identifier: String, progress: Float)
        fun onFileDownloaded(identifier: String)
        fun onFileUnzipped(identifier: String)
        fun onResourceFetchError(identifier: String)
    }

    fun addListener(listener: Listener) {
        if (!listeners.contains(listener))
            listeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    fun isDownloading(identifier: String): Boolean {
        return tasks[identifier] != null
    }

    fun isInstalled(item: ResourceItem): Boolean {
        return contextDirectory(item).exists()
    }

    suspend fun installedResourcesAsync(): List<ResourceItem> {
        return withContext(Dispatchers.IO) {
            installedResources()
        }
    }

    private fun installedResources(): List<ResourceItem> {
        val items = arrayListOf<ResourceItem>()
        val scriptDirPath = scriptDirectory
        if (scriptDirPath != null) {
            val parentDir = File(scriptDirPath)
            if (parentDir.exists() && parentDir.isDirectory) {
                for (folder in parentDir.listFiles() ?: arrayOf()) {
                    if (!folder.isDirectory) {
                        continue
                    }
                    val jsonDescriptionFile = File(folder, "description.json")
                    if (!jsonDescriptionFile.exists() || jsonDescriptionFile.isDirectory)
                        continue
                    try {
                        val reader = FileReader(jsonDescriptionFile)
                        reader.use {
                            val gson = GsonBuilder().create()
                            val item = gson.fromJson(it, ResourceItem::class.java)
                            if (item.id == folder.name && item.type == "script") {
                                items.add(item)
                            }
                        }
                    } catch (ignored: Throwable) {}
                }
            }
        }

        val addonDirPath = addonDirectory
        if (addonDirPath != null) {
            val parentDir = File(addonDirPath)
            if (parentDir.exists() && parentDir.isDirectory) {
                for (folder in parentDir.listFiles() ?: arrayOf()) {
                    if (!folder.isDirectory) {
                        continue
                    }
                    val jsonDescriptionFile = File(folder, "description.json")
                    if (!jsonDescriptionFile.exists() || jsonDescriptionFile.isDirectory)
                        continue
                    try {
                        val reader = FileReader(jsonDescriptionFile)
                        reader.use {
                            val gson = GsonBuilder().create()
                            val item = gson.fromJson(it, ResourceItem::class.java)
                            if (item.id == folder.name && item.type != "script") {
                                items.add(item)
                            }
                        }
                    } catch (ignored: Throwable) {}
                }
            }
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val collator = Collator.getInstance()
            if (collator is RuleBasedCollator) {
                collator.numericCollation = true
            }
            items.sortedWith(compareBy(collator) {
                it.name
            })
        } else {
            items.sortedWith(compareBy { it.name })
        }
    }

    fun contextDirectory(item: ResourceItem): File {
        if (item.type == "script")
            return File(scriptDirectory, item.id)
        return File(addonDirectory, item.id)
    }

    fun uninstall(item: ResourceItem): Boolean {
        return contextDirectory(item).deleteRecursively()
    }

    fun cancel(identifier: String) {
        tasks.remove(identifier)?.cancel()
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    fun download(item: ResourceItem, destination: File) {
        val unzipDestination = contextDirectory(item)
        val reference = WeakReference(this)
        val task = CoroutineScope(parentJob + Dispatchers.IO).executeAsyncTask(doInBackground = { publishProgress: suspend (progress: Progress) -> Unit ->
            val client = OkHttpClient()
            val call = client.newCall(Request.Builder().url(item.item).get().build())
            try {
                val response = call.execute()
                val body = response.body
                if (!response.isSuccessful || body == null) {
                    return@executeAsyncTask false
                }

                val totalLength = body.contentLength()
                var writtenLength = 0L
                body.byteStream().use { input ->
                    destination.outputStream().use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var bytes = input.read(buffer)
                        while (bytes >= 0) {
                            output.write(buffer, 0, bytes)
                            writtenLength += bytes
                            publishProgress(Progress(State.Downloading, writtenLength, totalLength))

                            // Check if user has canceled
                            if (!coroutineContext.isActive)
                                return@executeAsyncTask false
                            bytes = input.read(buffer)
                        }
                    }
                }
                publishProgress(Progress(State.Downloaded, 0, 0))
                if (!unzipDestination.exists() && !unzipDestination.mkdir()) {
                    return@executeAsyncTask false
                }
                if (!ZipUtils.unzip(destination.path, unzipDestination.path)) {
                    return@executeAsyncTask false
                }
                // We also save a `description.json` just in case for future use
                try {
                    val writer = FileWriter(File(unzipDestination, "description.json"))
                    writer.use {
                        val gson = GsonBuilder().create()
                        gson.toJson(item, it)
                    }
                } catch (ignored: Throwable) {}
            } catch (e: Throwable) {
                e.printStackTrace()
                return@executeAsyncTask false
            }
            return@executeAsyncTask true
        }, onPostExecute = { result: Boolean ->
            if (result)
                reference.get()?.onUnzipFinished(item.id)
            else
                reference.get()?.onResourceFetchingFailed(item.id)
        }, onProgressUpdate = { progress: Progress ->
            if (progress.state == State.Downloading)
                reference.get()?.onProgressUpdate(item.id, progress.finished, progress.total)
            else
                reference.get()?.onFileDownloaded(item.id)
        })
        tasks[item.id] = task
    }

    private fun callListenerProgressCallback(identifier: String, progress: Float) {
        for (listener in listeners)
            listener.onProgressUpdate(identifier, progress)
    }

    private fun callListenerErrorCallback(identifier: String) {
        for (listener in listeners)
            listener.onResourceFetchError(identifier)
    }

    private fun callListenerDownloadSuccessCallback(identifier: String) {
        for (listener in listeners)
            listener.onFileDownloaded(identifier)
    }

    private fun callListenerUnzipSuccessCallback(identifier: String) {
        for (listener in listeners)
            listener.onFileUnzipped(identifier)
    }

    private fun onFileDownloaded(identifier: String) {
        Log.d(TAG, "File download success")
        callListenerDownloadSuccessCallback(identifier)
    }

    private fun onProgressUpdate(identifier: String, bytesWritten: Long, totalBytes: Long) {
        Log.d(TAG, "Progress update")
        callListenerProgressCallback(identifier, bytesWritten.toFloat() / max(totalBytes, 1).toFloat())
    }

    private fun onResourceFetchingFailed(identifier: String) {
        Log.d(TAG, "Resource fetch failed")
        tasks.remove(identifier)
        callListenerErrorCallback(identifier)
    }

    private fun onUnzipFinished(identifier: String) {
        Log.d(TAG, "Unzip finished")
        tasks.remove(identifier)
        callListenerUnzipSuccessCallback(identifier)
    }

    companion object {
        private const val TAG = "ResourceManager"
    }
}