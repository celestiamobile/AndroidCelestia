package space.celestia.mobilecelestia.favorite.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import space.celestia.celestia.AppCore
import space.celestia.celestia.Destination
import space.celestia.celestia.Script
import space.celestia.celestiafoundation.favorite.BookmarkNode
import space.celestia.mobilecelestia.common.CelestiaExecutor
import space.celestia.mobilecelestia.utils.CelestiaString
import java.io.Serializable
import javax.inject.Inject

sealed class FavoriteTree<T : Favorite> {
    abstract val children: List<T>
    abstract val emptyHint: String?
    abstract val title: String

    data object Root : FavoriteTree<Favorite.Root>() {
        override val children: List<Favorite.Root>
            get() = listOf(Favorite.Root.Script, Favorite.Root.Bookmark, Favorite.Root.Destination)

        override val emptyHint: String?
            get() = null

        override val title: String
            get() = CelestiaString("Favorites", "Favorites (currently bookmarks and scripts)")
    }

    sealed class Editable<T : Favorite> : FavoriteTree<T>() {
        abstract fun move(fromIndex: Int, toIndex: Int)
        abstract fun remove(index: Int)
        abstract fun add(item: T)
    }

    sealed class Bookmark : Editable<Favorite.Bookmark>() {
        abstract val bookmark: BookmarkNode
        override val children: List<Favorite.Bookmark>
            get() = bookmark.children?.map { Favorite.Bookmark(it) } ?: listOf()
        override val emptyHint: String
            get() = CelestiaString("Create a new bookmark with \"+\" button", "")

        override fun remove(index: Int) {
            bookmark.children!!.removeAt(index)
        }

        override fun move(fromIndex: Int, toIndex: Int) {
            bookmark.children!!.add(toIndex, bookmark.children!!.removeAt(fromIndex))
        }

        override fun add(item: Favorite.Bookmark) {
            bookmark.children!!.add(item.bookmark)
        }

        data object Root : Bookmark() {
            override val bookmark: BookmarkNode
                get() = currentBookmarkRoot

            override val title: String
                get() = CelestiaString("Bookmarks", "URL bookmarks")
        }
        data class Folder(override val bookmark: BookmarkNode) : Bookmark() {
            override val title: String
                get() = bookmark.name
        }
    }
    data object Destination : FavoriteTree<Favorite.Destination>() {
        override val children: List<Favorite.Destination>
            get() = currentDestinations.map { Favorite.Destination(it) }

        override val title: String
            get() = CelestiaString("Destinations", "A list of destinations in guide")

        override val emptyHint: String?
            get() = null
    }
    data object Script : FavoriteTree<Favorite.Script>() {
        override val children: List<Favorite.Script>
            get() = currentScripts.map { Favorite.Script(it) }

        override val title: String
            get() = CelestiaString("Scripts", "")

        override val emptyHint: String?
            get() = null
    }
}

enum class FavoriteAction {
    Delete, Rename, Share;

    val title: String
        get() {
            return when (this) {
                Delete -> CelestiaString("Delete", "")
                Rename -> CelestiaString("Rename", "Rename a favorite item (currently bookmark)")
                Share -> CelestiaString("Share", "")
            }
        }
}

sealed class Favorite {
    abstract val tree: FavoriteTree<*>?
    abstract val title: String
    fun supportedActions(tree: FavoriteTree<*>): List<FavoriteAction> {
        val actions = arrayListOf<FavoriteAction>()
        if (tree is FavoriteTree.Editable)
            actions.add(FavoriteAction.Delete)
        if (this is Renamable)
            actions.add(FavoriteAction.Rename)
        if (this is Shareable && this.shareableContent != null)
            actions.add(FavoriteAction.Share)
        return actions
    }

    interface Renamable {
        fun rename(name: String)
    }
    interface Shareable {
        val shareableContent: Object?

        data class Object(val title: String, val url: String)
    }

    abstract val representation: FavoriteRepresentation?

    sealed class Root : Favorite() {
        override val representation: FavoriteRepresentation?
            get() = null
        data object Bookmark: Root(), Serializable {
            private fun readResolve(): Any = Bookmark
            override val tree: FavoriteTree<*>
                get() = FavoriteTree.Bookmark.Root
            override val title: String
                get() = CelestiaString("Bookmarks", "URL bookmarks")
        }
        data object Destination: Root(), Serializable {
            private fun readResolve(): Any = Destination
            override val tree: FavoriteTree<*>
                get() = FavoriteTree.Destination
            override val title: String
                get() = CelestiaString("Destinations", "A list of destinations in guide")
        }
        data object Script: Root(), Serializable {
            private fun readResolve(): Any = Script
            override val tree: FavoriteTree<*>
                get() = FavoriteTree.Script
            override val title: String
                get() = CelestiaString("Scripts", "")
        }
    }

    data class Destination(val destination: space.celestia.celestia.Destination) : Favorite(), Serializable  {
        override val tree: FavoriteTree<*>?
            get() = null
        override val title: String
            get() = destination.name
        override val representation: FavoriteRepresentation
            get() = FavoriteRepresentation.Destination(destination)
    }
    data class Script(val script: space.celestia.celestia.Script) : Favorite(), Serializable {
        override val tree: FavoriteTree<*>?
            get() = null
        override val title: String
            get() = script.title
        override val representation: FavoriteRepresentation?
            get() = null
    }
    data class Bookmark(val bookmark: BookmarkNode) : Favorite(), Serializable, Renamable, Shareable {
        override val tree: FavoriteTree<*>?
            get() {
                if (bookmark.isLeaf)
                    return null
                return FavoriteTree.Bookmark.Folder(bookmark)
            }
        override val title: String
            get() = bookmark.name
        override val shareableContent: Shareable.Object?
            get() {
                if (!bookmark.isLeaf)
                    return null
                return Shareable.Object(bookmark.name, bookmark.url)
            }
        override val representation: FavoriteRepresentation?
            get() = null

        override fun rename(name: String) {
            bookmark.name = name
        }
    }
}

sealed class FavoriteRepresentation {
    data class Destination(val destination: space.celestia.celestia.Destination) : FavoriteRepresentation()
}

@HiltViewModel
class FavoriteViewModel @Inject constructor(val appCore: AppCore, val executor: CelestiaExecutor) : ViewModel() {
    val treeMap = hashMapOf<Int, FavoriteTree<*>>()
    val destinationMap = hashMapOf<Int, Destination>()

    private var _needsRefresh = MutableLiveData(false)
    val needsRefresh: LiveData<Boolean>
        get() = _needsRefresh

    fun setNeedsRefresh(needsRefresh: Boolean) {
        _needsRefresh.value = needsRefresh
    }
}

private var currentScripts: List<Script> = listOf()
private var currentBookmarkRoot: BookmarkNode = BookmarkNode(CelestiaString("Bookmarks", "URL bookmarks") , "", arrayListOf())
private var currentDestinations: List<Destination> = listOf()


fun updateCurrentScripts(scripts: List<Script>) {
    currentScripts = scripts
}

fun getCurrentBookmarks(): List<BookmarkNode> {
    return currentBookmarkRoot.children ?: listOf()
}

fun updateCurrentBookmarks(nodes: List<BookmarkNode>) {
    currentBookmarkRoot.children = ArrayList(nodes)
}

fun updateCurrentDestinations(destinations: List<Destination>) {
    currentDestinations = destinations
}