/*
 * Assets.kt
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
import android.content.res.AssetManager
import java.io.File
import java.io.IOException

object AssetUtils {
    @Throws(IOException::class)
    fun copyFileOrDir(context: Context, path: String, base: String) {
        val assetManager: AssetManager = context.assets
        val assets = assetManager.list(path) as Array<String>
        if (assets.isEmpty()) {
            copyFile(context, path, base)
        } else {
            val fullPath = "$base/$path"
            val dir = File(fullPath)
            if (!dir.exists())
                dir.mkdir()
            for (asset in assets) {
                copyFileOrDir(context, "$path/$asset", base)
            }
        }
    }

    @Throws(IOException::class)
    private fun copyFile(context: Context, filename: String, base: String) {
        val assetManager: AssetManager = context.assets
        val input = assetManager.open(filename)
        val fullPath = "$base/$filename"
        File(fullPath).outputStream().use {
            input.copyTo(it)
        }
        input.close()
    }

    @Throws(IOException::class)
    fun readFileToText(context: Context, path: String): String {
        val assetManager: AssetManager = context.assets
        val input = assetManager.open(path)
        return input.bufferedReader().use { it.readText() }
    }
}