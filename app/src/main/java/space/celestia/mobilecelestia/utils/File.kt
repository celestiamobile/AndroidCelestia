package space.celestia.mobilecelestia.utils

import android.content.Context
import android.net.Uri
import java.io.BufferedReader
import java.io.File
import java.io.IOException

object FileUtils {
    @Throws(IOException::class)
    public fun readFileToText(path: String): String {
        val bufferedReader: BufferedReader = File(path).bufferedReader()
        return bufferedReader.use { it.readText() }
    }

    @Throws(IOException::class)
    public fun writeTextToFile(text: String, path: String) {
        val file = File(path)
        if (!file.exists())
            file.createNewFile()

        file.bufferedWriter().use { it.write(text) }
    }

    public fun copyUri(context: Context, uri: Uri, path: String): Boolean {
        val input = context.contentResolver.openInputStream(uri) ?: return false
        val file = File(path)
        if (!file.exists())
            file.createNewFile()
        file.outputStream().use { input.copyTo(it) }
        return true
    }
}