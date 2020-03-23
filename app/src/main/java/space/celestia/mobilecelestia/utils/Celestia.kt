package space.celestia.mobilecelestia.utils

import space.celestia.mobilecelestia.core.CelestiaAppCore
import space.celestia.mobilecelestia.favorite.BookmarkNode

val CelestiaAppCore.currentBookmark: BookmarkNode?
    get() {
        val sel = simulation.selection
        if (sel.isEmpty) return null
        val name: String
        if (sel.star != null) {
            name = simulation.universe.starCatalog.getStarName(sel.star)
        } else if (sel.dso != null) {
            name = simulation.universe.dsoCatalog.getDSOName(sel.dso)
        } else if (sel.body != null) {
            name = sel.body!!.name
        } else if (sel.location != null) {
            name = sel.location!!.name
        } else {
            name = "Unknown"
        }
        return BookmarkNode(name, currentURL, null)
    }