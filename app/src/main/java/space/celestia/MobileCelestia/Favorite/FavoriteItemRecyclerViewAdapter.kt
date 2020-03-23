package space.celestia.MobileCelestia.Favorite

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import space.celestia.MobileCelestia.Common.CommonSectionV2
import space.celestia.MobileCelestia.Common.CommonTextViewHolder

import space.celestia.MobileCelestia.Favorite.FavoriteItemFragment.Listener

import space.celestia.MobileCelestia.Common.RecyclerViewItem
import space.celestia.MobileCelestia.Common.SeparatorHeaderRecyclerViewAdapter
import space.celestia.MobileCelestia.Core.CelestiaScript
import java.io.Serializable
import java.lang.RuntimeException

interface FavoriteBaseItem : RecyclerViewItem, Serializable {
    val children: List<FavoriteBaseItem>
    val isLeaf: Boolean
    val title: String
}

interface MutableFavoriteBaseItem : FavoriteBaseItem {
    fun insert(newItem: FavoriteBaseItem, index: Int)

    fun append(newItem: FavoriteBaseItem) {
        insert(newItem, children.size)
    }
}

enum class FavoriteType {
    Script
}

class FavoriteRoot : FavoriteBaseItem {
    override val children: List<FavoriteBaseItem>
        get() = listOf(
            FavoriteTypeItem(FavoriteType.Script),
            FavoriteBookmarkItem(currentBookmarkRoot)
        )
    override val title: String
        get() = "Favorites"
    override val isLeaf: Boolean
        get() = false
}

class FavoriteTypeItem(val type: FavoriteType) : FavoriteBaseItem {
    override val children: List<FavoriteBaseItem>
        get() {
            when (type) {
                FavoriteType.Script -> {
                    return currentScripts.map { FavoriteScriptItem(it) }
                }
            }
        }
    override val title: String
        get() = type.toString()
    override val isLeaf: Boolean
        get() = false
}

class FavoriteScriptItem(val script: CelestiaScript) : FavoriteBaseItem {
    override val children: List<FavoriteBaseItem>
        get() = listOf()
    override val title: String
        get() = script.title
    override val isLeaf: Boolean
        get() = true
}

class FavoriteBookmarkItem(val bookmark: BookmarkNode) : MutableFavoriteBaseItem {
    override val children: List<FavoriteBaseItem>
        get() = if (bookmark.isLeaf) listOf() else bookmark.children!!.map { FavoriteBookmarkItem(it) }
    override val title: String
        get() = bookmark.name
    override val isLeaf: Boolean
        get() = bookmark.isLeaf

    override fun insert(newItem: FavoriteBaseItem, index: Int) {
        if (!(newItem is FavoriteBookmarkItem))
            throw RuntimeException("$newItem does not match type FavoriteBookmarkItem")
        bookmark.children!!.add(index, newItem.bookmark)
    }
}
public fun updateCurrentScripts(scripts: List<CelestiaScript>) {
    currentScripts = scripts
}


public fun getCurrentBookmarks(): List<BookmarkNode> {
    return currentBookmarkRoot.children ?: return listOf()
}

public fun updateCurrentBookmarks(nodes: List<BookmarkNode>) {
    currentBookmarkRoot.children = ArrayList(nodes)
}

private var currentScripts: List<CelestiaScript> = listOf()
private var currentBookmarkRoot: BookmarkNode = BookmarkNode("Bookmarks", "", arrayListOf())

class FavoriteItemRecyclerViewAdapter(
    private val item: FavoriteBaseItem,
    private val listener: Listener?
) : SeparatorHeaderRecyclerViewAdapter(listOf(CommonSectionV2(item.children))) {

    override fun onItemSelected(item: RecyclerViewItem) {
        (item as? FavoriteBaseItem)?.let {
            listener?.onFavoriteItemSelected(it)
        }
    }

    override fun itemViewType(item: RecyclerViewItem): Int {
        if (item is FavoriteTypeItem) {
            return FAVORITE_TYPE
        }
        if (item is FavoriteScriptItem) {
            return FAVORITE_SCRIPT
        }
        if (item is FavoriteBookmarkItem) {
            return FAVORITE_BOOKMARK
        }
        return super.itemViewType(item)
    }

    override fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == FAVORITE_TYPE || viewType == FAVORITE_SCRIPT || viewType == FAVORITE_BOOKMARK) {
            return CommonTextViewHolder(parent)
        }
        return super.createVH(parent, viewType)
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        if (holder is CommonTextViewHolder && item is FavoriteBaseItem) {
            holder.title.text = item.title
            holder.accessory.visibility = if (item.isLeaf) View.GONE else View.VISIBLE
            return
        }
        super.bindVH(holder, item)
    }

    fun reload() {
        updateSectionsWithHeader(listOf(CommonSectionV2(item.children)))
    }

    private companion object {
        const val FAVORITE_TYPE = 0
        const val FAVORITE_SCRIPT = 1
        const val FAVORITE_BOOKMARK = 2
    }
}
