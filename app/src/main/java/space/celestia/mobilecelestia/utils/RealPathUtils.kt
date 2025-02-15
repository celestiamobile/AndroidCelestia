/*
 * RealPathUtils.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import space.celestia.celestiafoundation.utils.FileUtils
import space.celestia.mobilecelestia.BuildConfig
import java.io.File
import java.io.IOException

object RealPathUtils {
    fun getRealPath(
        context: Context,
        fileUri: Uri
    ): String? {
        return getRealPathFromURIAboveAPI19(context, fileUri)
    }

    private fun getRealPathFromURIAboveAPI19(
        context: Context,
        uri: Uri
    ): String? {
        // DocumentProvider
        val isTree = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && DocumentsContract.isTreeUri(uri)
        when {
            DocumentsContract.isDocumentUri(context, uri) || isTree -> {
                // ExternalStorageProvider
                when {
                    isExternalStorageDocument(uri) -> {
                        val docId = if (isTree) DocumentsContract.getTreeDocumentId(uri) else DocumentsContract.getDocumentId(uri)
                        val split = docId.split(":").toTypedArray()
                        val type = split[0]

                        // This is for checking Main Memory
                        return if ("primary".equals(type, ignoreCase = true)) {
                            if (split.size > 1) {
                                getExternalStorageDirectory(context) + "/" + split[1]
                            } else {
                                getExternalStorageDirectory(context) + "/"
                            }
                            // This is for checking SD Card
                        } else {
                            "storage" + "/" + docId.replace(":", "/")
                        }
                    }
                    !isTree && isDownloadsDocument(uri) -> {
                        val fileName = getFilePath(context, uri)
                        if (fileName != null) {
                            return getExternalStorageDirectory(context) + "/Download/" + fileName
                        }
                        val id = DocumentsContract.getDocumentId(uri)

                        if (id != null && id.startsWith("raw:"))
                            return id.substring(4)

                        val contentUriPrefixesToTry = arrayOf(
                            "content://downloads/public_downloads",
                            "content://downloads/my_downloads",
                            "content://downloads/all_downloads"
                        )
                        val idLong: Long
                        try {
                            idLong = java.lang.Long.valueOf(id)
                        } catch (ignored: NumberFormatException) {
                            return (uri.path ?: "").replaceFirst("^/document/raw:", "").replaceFirst("^raw:", "")
                        }
                        for (contentUriPrefix in contentUriPrefixesToTry) {
                            val contentUri = ContentUris.withAppendedId(
                                Uri.parse(contentUriPrefix),
                                idLong
                            )
                            val path = getDataColumn(context, contentUri, null, null)
                            if (path != null)
                                return path
                        }
                        // path could not be retrieved using ContentResolver, therefore copy file to accessible cache using streams
                        val destinationFile = generateFileName(getDocumentCacheDir(context), getFileName(context, uri))
                        if (destinationFile != null) {
                            val path = destinationFile.absolutePath
                            try {
                                FileUtils.copyUri(context, uri, path)
                            } catch (exception: IOException) {
                                return null
                            }
                            return path
                        }
                        return null
                    }
                    !isTree && isMediaDocument(uri) -> {
                        val docId = DocumentsContract.getDocumentId(uri)
                        val split = docId.split(":").toTypedArray()
                        val type = split[0]
                        var contentUri: Uri? = null
                        when (type) {
                            "image" -> {
                                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            }
                            "video" -> {
                                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                            }
                            "audio" -> {
                                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                            }
                        }
                        val selection = "_id=?"
                        val selectionArgs = arrayOf(
                            split[1]
                        )
                        return getDataColumn(context, contentUri, selection, selectionArgs)
                    }
                }
            }
            "content".equals(uri.scheme, ignoreCase = true) -> {
                // Return the remote address
                return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
                    context,
                    uri,
                    null,
                    null
                )
            }
            "file".equals(uri.scheme, ignoreCase = true) -> {
                return uri.path
            }
        }
        return null
    }

    private fun getExternalStorageDirectory(context: Context): String {
        val rootPath = context.getExternalFilesDir(null)?.absolutePath ?: return ""
        val extra = "/Android/data/" + BuildConfig.APPLICATION_ID + File.separator + "files"
        return rootPath.replace(extra, "")
    }

    private fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        if (uri == null) return null

        val column = "_data"
        val projection = arrayOf(
            column
        )
        try {
            val cursor = context.contentResolver.query(
                uri, projection, selection, selectionArgs,
                null
            )
            cursor?.use {
                it.moveToFirst()
                return cursor.getString(cursor.getColumnIndexOrThrow(column))
            }
        } catch (ignored: Exception) {}
        return null
    }


    private fun getFilePath(
        context: Context,
        uri: Uri
    ): String? {
        var cursor: Cursor? = null
        val projection = arrayOf(
            MediaStore.MediaColumns.DISPLAY_NAME
        )
        try {
            cursor = context.contentResolver.query(
                uri, projection, null, null,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index: Int = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    private fun getDocumentCacheDir(context: Context): File {
        val dir = File(context.cacheDir, "documents")
        if (!dir.exists())
            dir.mkdir()
        return dir
    }

    private fun generateFileName(directory: File, name: String?): File? {
        if (name == null)
            return null

        var file = File(directory, name)
        if (file.exists()) {
            var fileName = name
            var extension = ""
            val dotIndex = name.lastIndexOf('.')
            if (dotIndex > 0) {
                fileName = name.substring(0, dotIndex)
                extension = name.substring(dotIndex)
            }
            var index = 0
            while (file.exists()) {
                index++
                val newName = "$fileName($index)$extension"
                file = File(directory, newName)
            }
        }

        try {
            if (!file.createNewFile()) {
                return null
            }
        } catch (e: IOException) {
            return null
        }
        return file
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        val mimeType = context.contentResolver.getType(uri)
        var filename: String? = null
        if (mimeType == null) {
            val uriString = uri.toString()
            val index = uriString.lastIndexOf("/")
            filename = uriString.substring(index + 1)
        } else {
            val returnCursor = context.contentResolver.query(uri, null, null, null, null)
            if (returnCursor != null) {
                val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                returnCursor.moveToFirst()
                filename = returnCursor.getString(nameIndex)
                returnCursor.close()
            }
        }
        return filename
    }
}