package space.celestia.celestiaui.favorite.viewmodel

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import space.celestia.celestia.AppCore
import space.celestia.celestia.Script
import space.celestia.celestiafoundation.favorite.BookmarkNode
import space.celestia.celestiafoundation.utils.FileUtils
import space.celestia.celestiaui.favorite.getCurrentBookmarks
import space.celestia.celestiaui.favorite.updateCurrentBookmarks
import space.celestia.celestiaui.favorite.updateCurrentDestinations
import space.celestia.celestiaui.favorite.updateCurrentScripts

class FavoriteManager(private val favoriteFilePath: String, private val appCore: AppCore) {
    var extraScriptPaths: List<String> = listOf()

    fun read() {
        var favorites = arrayListOf<BookmarkNode>()
        try {
            val str = FileUtils.readFileToText(favoriteFilePath)
            val decoded = json.decodeFromString<ArrayList<BookmarkNode>>(str)
            favorites = decoded
        } catch (ignored: Throwable) { }
        updateCurrentBookmarks(favorites)
        val scripts = Script.getScriptsInDirectory("scripts", true)
        for (extraScriptPath in extraScriptPaths) {
            scripts.addAll(Script.getScriptsInDirectory(extraScriptPath, true))
        }
        updateCurrentScripts(scripts)
        updateCurrentDestinations(appCore.destinations)
    }

    fun save() {
        val favorites = getCurrentBookmarks()
        try {
            val str = json.encodeToString<List<BookmarkNode>>(favorites)
            FileUtils.writeTextToFile(str, favoriteFilePath)
        } catch (_: Throwable) { }
    }

    private companion object {
        val json = Json { ignoreUnknownKeys = true }
    }
}
