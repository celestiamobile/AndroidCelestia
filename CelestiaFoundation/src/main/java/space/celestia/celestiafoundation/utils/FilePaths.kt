package space.celestia.celestiafoundation.utils

import android.content.Context

class FilePaths(context: Context) {
    val parentDirectoryPath: String = context.noBackupFilesDir.absolutePath
    val dataDirectoryPath: String = "$parentDirectoryPath/$CELESTIA_DATA_FOLDER_NAME"
    val configFilePath: String = "$dataDirectoryPath/$CELESTIA_CFG_NAME"
    val legacyFontsDirectoryPath: String = "$parentDirectoryPath/$CELESTIA_LEGACY_FONTS_FOLDER_NAME"

    companion object {
        const val CELESTIA_DATA_FOLDER_NAME = "CelestiaResources"
        const val CELESTIA_CFG_NAME = "celestia.cfg"
        const val CELESTIA_LEGACY_FONTS_FOLDER_NAME = "fonts"
    }
}