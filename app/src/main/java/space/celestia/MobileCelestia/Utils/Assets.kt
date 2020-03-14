package space.celestia.MobileCelestia.Utils

import android.content.Context
import android.content.res.AssetManager
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object AssetUtils {
    @Throws(IOException::class)
    public fun copyFileOrDir(context: Context, path: String, base: String) {
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
        val output = File(fullPath).outputStream().use {
            input.copyTo(it)
        }
        input.close()
    }
}