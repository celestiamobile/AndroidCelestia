// LPMetadataProvider.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.ui.linkpreview

import kotlinx.coroutines.*
import org.jsoup.Jsoup
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.CoroutineContext

class LPMetadataProvider {
    class TaskCancellationException: Exception()
    class TooManyRedirectsException: Exception()

    var timeout = DEFAULT_TIMEOUT

    private var currentTask: Job? = null

    fun startFetchMetadataForURL(scope: CoroutineScope, url: URL, completionHandler: suspend CoroutineScope.(LPLinkMetadata?, Throwable?) -> Unit) {
        currentTask = scope.launch {
            try {
                val metaData = getMetaDataAsync(url)
                completionHandler(metaData, null)
            } catch (e: Throwable) {
                completionHandler(null, e)
            }
        }
    }

    private suspend fun getMetaDataAsync(url: URL): LPLinkMetadata {
        return withContext(Dispatchers.IO) {
            getMetaData(url, coroutineContext)
        }
    }

    private fun getMetaData(url: URL, coroutineContext: CoroutineContext): LPLinkMetadata {
        var redirectCount = 0
        var finalInputSteam: InputStream? = null
        var finalURL = url
        while (redirectCount < MAX_REDIRECT_COUNT) {
            // Check if it has been canceled
            if (!coroutineContext.isActive)
                throw TaskCancellationException()

            val connection = finalURL.openConnection() as HttpURLConnection
            connection.connectTimeout = timeout
            connection.instanceFollowRedirects = false
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", DEFAULT_UA)
            connection.connect()
            finalInputSteam = connection.inputStream
            if (connection.responseCode == HttpURLConnection.HTTP_MOVED_PERM || connection.responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                val redirectURL = connection.getHeaderField("Location")
                finalURL = URL(redirectURL)
                redirectCount += 1
            } else {
                break
            }
        }

        // Check if exceeding the max redirect count
        if (redirectCount >= MAX_REDIRECT_COUNT || finalInputSteam == null)
            throw TooManyRedirectsException()

        val byteArrayOutputStream = ByteArrayOutputStream()
        finalInputSteam.use {
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var bytes = it.read(buffer)
            while (bytes >= 0) {
                byteArrayOutputStream.write(buffer, 0, bytes)
                // Check if it has been canceled
                if (!coroutineContext.isActive)
                    throw TaskCancellationException()
                bytes = it.read(buffer)
            }
        }
        val document = Jsoup.parse(byteArrayOutputStream.toString("UTF-8"))
        val metaTags = document.getElementsByTag("meta")
        val links = document.getElementsByTag("link")

        var title = document.title()
        var imageURL: URL? = null
        var favIconURL = URL(finalURL.protocol, finalURL.host, finalURL.port, "/favicon.ico")
        for (metaTag in metaTags) {
            val name = metaTag.attr("name")
            val property = metaTag.attr("property")
            if (property == "og:image") {
                val newImageURL = metaTag.absUrl("content")
                if (newImageURL != "")
                    imageURL = URL(newImageURL)
            } else if (name == "og:title") {
                val newTitle = metaTag.attr("content")
                if (newTitle != "")
                    title = newTitle
            }
        }
        for (link in links) {
            val rel = link.attr("rel")
            val href = link.attr("abs:href")
            if (rel == "shortcut icon" && href != "") {
                favIconURL = URL(href)
            }
        }
        return LPLinkMetadata(finalURL, url, title, imageURL, favIconURL)
    }

    fun cancel() {
        currentTask?.cancel()
    }

    private companion object {
        const val DEFAULT_TIMEOUT = 30000
        const val MAX_REDIRECT_COUNT = 40
        const val DEFAULT_UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36"
    }
}