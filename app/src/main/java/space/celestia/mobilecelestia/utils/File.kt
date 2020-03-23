package space.celestia.mobilecelestia.utils

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
}