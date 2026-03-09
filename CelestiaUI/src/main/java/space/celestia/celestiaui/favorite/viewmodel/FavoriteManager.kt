package space.celestia.celestiaui.favorite.viewmodel

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
            val myType = object : TypeToken<List<BookmarkNode>>() {}.type
            val str = FileUtils.readFileToText(favoriteFilePath)
            val decoded = Gson().fromJson<ArrayList<BookmarkNode>>(str, myType)
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
            val myType = object : TypeToken<List<BookmarkNode>>() {}.type
            val str = Gson().toJson(favorites, myType)
            FileUtils.writeTextToFile(str, favoriteFilePath)
        } catch (_: Throwable) { }
    }
}