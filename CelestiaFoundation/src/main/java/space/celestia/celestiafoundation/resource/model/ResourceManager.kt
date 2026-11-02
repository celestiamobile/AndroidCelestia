// ResourceManager.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.celestiafoundation.resource.model

import android.icu.text.Collator
import android.icu.text.RuleBasedCollator
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import space.celestia.ziputils.ZipExceptionContext
import space.celestia.ziputils.ZipUtils
import java.io.File
import java.io.IOException
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max

internal fun commitResourceInstallation(stagingDirectory: File, destinationDirectory: File) {
    val backupDirectory = File(destinationDirectory.parentFile, ".${destinationDirectory.name}.${UUID.randomUUID()}.backup")
    val replacing = destinationDirectory.exists()
    if (replacing && !destinationDirectory.renameTo(backupDirectory)) {
        throw IOException("Unable to back up $destinationDirectory")
    }
    if (!stagingDirectory.renameTo(destinationDirectory)) {
        val restored = !replacing || backupDirectory.renameTo(destinationDirectory)
        throw IOException(if (restored) "Unable to install $destinationDirectory" else "Unable to restore $destinationDirectory from $backupDirectory")
    }
    if (replacing && !backupDirectory.deleteRecursively()) {
        Log.w("ResourceManager", "Unable to remove installation backup $backupDirectory")
    }
}

class ResourceManager {
    private var listeners = arrayListOf<Listener>()
    private var tasks = hashMapOf<String, Job>()
    private val activeCalls = ConcurrentHashMap<String, Call>()

    sealed class ErrorContext {
        data object Cancelled: ErrorContext()
        data object Download: ErrorContext()
        data object ZipError: ErrorContext()
        class CreateDirectory(contextPath: String): ErrorContext()
        class OpenFile(contextPath: String): ErrorContext()
        class WriteFile(contextPath: String): ErrorContext()
    }

    private val parentJob = Job()

    var addonDirectory: String? = null
    var scriptDirectory: String? = null

    interface Listener {
        fun onProgressUpdate(identifier: String, progress: Float)
        fun onFileDownloaded(identifier: String)
        fun onFileUnzipped(identifier: String)
        fun onResourceFetchError(identifier: String, errorContext: ErrorContext)
        fun onAddonUninstalled(identifier: String)
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

    suspend fun installedResourceAsync(item: ResourceItem): ResourceItem? {
        return withContext(Dispatchers.IO) {
            installedResource(item)
        }
    }

    private fun installedResource(item: ResourceItem): ResourceItem? {
        val contextDir = contextDirectory(item)
        if (!contextDir.exists())
            return null
        val jsonDescriptionFile = File(contextDir, "description.json")
        if (!jsonDescriptionFile.exists() || jsonDescriptionFile.isDirectory)
            return null
        return try {
            json.decodeFromString<ResourceItem>(jsonDescriptionFile.readText())
        } catch (_: Throwable) {
            null
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
                        val item = json.decodeFromString<ResourceItem>(jsonDescriptionFile.readText())
                        if (item.id == folder.name && item.type == "script") {
                            items.add(item)
                        }
                    } catch (_: Throwable) {}
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
                        val item = json.decodeFromString<ResourceItem>(jsonDescriptionFile.readText())
                        if (item.id == folder.name && item.type != "script") {
                            items.add(item)
                        }
                    } catch (_: Throwable) {}
                }
            }
        }
        val collator = Collator.getInstance()
        if (collator is RuleBasedCollator) {
            collator.numericCollation = true
        }
        return items.sortedWith(compareBy(collator) {
            it.name
        })
    }

    fun contextDirectory(item: ResourceItem): File {
        if (item.type == "script")
            return File(scriptDirectory, item.id)
        return File(addonDirectory, item.id)
    }

    fun uninstall(item: ResourceItem): Boolean {
        val result = uninstallInternally(item)
        if (result) {
            for (listener in listeners) {
                listener.onAddonUninstalled(item.id)
            }
        }
        return result
    }

    private fun uninstallInternally(item: ResourceItem): Boolean {
        return contextDirectory(item).deleteRecursively()
    }

    fun reinstall(item: ResourceItem, destination: File) {
        download(item, destination)
    }

    fun cancel(identifier: String) {
        tasks[identifier]?.cancel()
        activeCalls[identifier]?.cancel()
    }

    private sealed class Result {
        data object Success: Result()
        class Failure(val errorContext: ErrorContext): Result()
    }

    fun download(item: ResourceItem, destination: File) {
        if (isDownloading(item.id)) return
        val task = CoroutineScope(parentJob + Dispatchers.IO).launch(start = CoroutineStart.LAZY) {
            val result = try {
                downloadAndInstall(item, destination)
            } catch (_: CancellationException) {
                Result.Failure(ErrorContext.Cancelled)
            }
            withContext(NonCancellable + Dispatchers.Main) {
                when (result) {
                    is Result.Success -> onUnzipFinished(item.id)
                    is Result.Failure -> onResourceFetchingFailed(item.id, result.errorContext)
                }
            }
        }
        tasks[item.id] = task
        task.invokeOnCompletion { cause ->
            if (cause is CancellationException) {
                CoroutineScope(Dispatchers.Main).launch {
                    if (tasks[item.id] === task) {
                        onResourceFetchingFailed(item.id, ErrorContext.Cancelled)
                    }
                }
            }
        }
        task.start()
    }

    private suspend fun downloadAndInstall(item: ResourceItem, destination: File): Result {
        val unzipDestination = contextDirectory(item)
        val stagingDirectory = File(unzipDestination.parentFile, ".${item.id}.${UUID.randomUUID()}.staging")
        var errorContext: ErrorContext = ErrorContext.Download
        try {
            val client = OkHttpClient()
            val call = client.newCall(Request.Builder().url(item.item).get().build())
            activeCalls[item.id] = call
            currentCoroutineContext().ensureActive()
            call.execute().use { response ->
                val body = response.body
                if (!response.isSuccessful) {
                    return Result.Failure(ErrorContext.Download)
                }

                val totalLength = body.contentLength()
                var writtenLength = 0L
                body.byteStream().use { input ->
                    destination.outputStream().use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var bytes = input.read(buffer)
                        while (bytes >= 0) {
                            currentCoroutineContext().ensureActive()
                            output.write(buffer, 0, bytes)
                            writtenLength += bytes
                            withContext(Dispatchers.Main) {
                                onProgressUpdate(item.id, writtenLength, totalLength)
                            }
                            bytes = input.read(buffer)
                        }
                    }
                }
            }
            withContext(Dispatchers.Main) { onFileDownloaded(item.id) }
            currentCoroutineContext().ensureActive()

            errorContext = ErrorContext.CreateDirectory(stagingDirectory.path)
            if (!stagingDirectory.mkdirs()) {
                return Result.Failure(errorContext)
            }

            errorContext = ErrorContext.ZipError
            val zipError = ZipUtils.unzip(destination.path, stagingDirectory.path)
            if (zipError != null) {
                return when (zipError.code) {
                    ZipExceptionContext.ZIP_ERROR -> Result.Failure(errorContext = ErrorContext.ZipError)
                    ZipExceptionContext.CREATE_DIRECTORY_ERROR -> Result.Failure(errorContext = ErrorContext.CreateDirectory(zipError.contextPath ?: ""))
                    ZipExceptionContext.OPEN_FILE_ERROR -> Result.Failure(errorContext = ErrorContext.OpenFile(zipError.contextPath ?: ""))
                    ZipExceptionContext.WRITE_FILE_ERROR -> Result.Failure(errorContext = ErrorContext.WriteFile(zipError.contextPath ?: ""))
                    else -> Result.Failure(errorContext = ErrorContext.ZipError)
                }
            }
            errorContext = ErrorContext.WriteFile(unzipDestination.path)
            File(stagingDirectory, "description.json").writeText(json.encodeToString(item))
            currentCoroutineContext().ensureActive()
            commitResourceInstallation(stagingDirectory, unzipDestination)
            return Result.Success
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            currentCoroutineContext().ensureActive()
            Log.e(TAG, "Unable to install ${item.id}", e)
            return Result.Failure(errorContext)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Unable to install ${item.id}", e)
            return Result.Failure(errorContext)
        } catch (e: SecurityException) {
            Log.e(TAG, "Unable to install ${item.id}", e)
            return Result.Failure(errorContext)
        } finally {
            activeCalls.remove(item.id)
            if (stagingDirectory.exists() && !stagingDirectory.deleteRecursively()) {
                Log.w(TAG, "Unable to remove installation staging directory $stagingDirectory")
            }
            if (destination.exists() && !destination.delete()) {
                Log.w(TAG, "Unable to remove downloaded archive $destination")
            }
        }
    }

    private fun callListenerProgressCallback(identifier: String, progress: Float) {
        for (listener in listeners)
            listener.onProgressUpdate(identifier, progress)
    }

    private fun callListenerErrorCallback(identifier: String, errorContext: ErrorContext) {
        for (listener in listeners)
            listener.onResourceFetchError(identifier, errorContext)
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

    private fun onResourceFetchingFailed(identifier: String, errorContext: ErrorContext) {
        Log.d(TAG, "Resource fetch failed")
        tasks.remove(identifier)
        callListenerErrorCallback(identifier, errorContext)
    }

    private fun onUnzipFinished(identifier: String) {
        Log.d(TAG, "Unzip finished")
        tasks.remove(identifier)
        callListenerUnzipSuccessCallback(identifier)
    }

    companion object {
        private const val TAG = "ResourceManager"
        private val json = Json { ignoreUnknownKeys = true }
    }
}