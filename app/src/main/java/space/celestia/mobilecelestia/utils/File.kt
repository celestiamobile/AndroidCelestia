/*
 * File.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.utils

import android.content.Context
import android.net.Uri
import java.io.BufferedReader
import java.io.File
import java.io.IOException

object FileUtils {
    @Throws(IOException::class)
    fun readFileToText(path: String): String {
        val bufferedReader: BufferedReader = File(path).bufferedReader()
        return bufferedReader.use { it.readText() }
    }

    @Throws(IOException::class)
    fun writeTextToFile(text: String, path: String) {
        val file = File(path)
        writeTextToFile(text, file)
    }

    @Throws(IOException::class)
    fun writeTextToFile(text: String, file: File) {
        if (!file.exists())
            file.createNewFile()
        file.bufferedWriter().use { it.write(text) }
    }

    @Throws(IOException::class)
    fun copyUri(context: Context, uri: Uri, path: String): Boolean {
        val input = context.contentResolver.openInputStream(uri) ?: return false
        val file = File(path)
        if (!file.exists())
            file.createNewFile()
        file.outputStream().use { input.copyTo(it) }
        return true
    }
}

fun File.deleteRecursively(): Boolean {
    if (isDirectory) {
        for (file in listFiles() ?: arrayOf<File>()) {
            if (!file.deleteRecursively()) {
                return false
            }
        }
    }
    return delete()
}