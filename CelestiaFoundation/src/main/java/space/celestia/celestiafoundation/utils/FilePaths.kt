package space.celestia.celestiafoundation.utils

import android.content.Context

class FilePaths(context: Context) {
    val parentDirectoryPath: String
    val dataDirectoryPath: String
    val configFilePath: String
    val fontDirectoryPath: String

    init {
        parentDirectoryPath = context.noBackupFilesDir.absolutePath
        dataDirectoryPath = "$parentDirectoryPath/$CELESTIA_DATA_FOLDER_NAME"
        configFilePath = "$dataDirectoryPath/$CELESTIA_CFG_NAME"
        fontDirectoryPath = "$parentDirectoryPath/$CELESTIA_FONT_FOLDER_NAME"
    }

    companion object {
        const val CELESTIA_DATA_FOLDER_NAME = "CelestiaResources"
        const val CELESTIA_CFG_NAME = "celestia.cfg"
        const val CELESTIA_FONT_FOLDER_NAME = "fonts"
    }
}