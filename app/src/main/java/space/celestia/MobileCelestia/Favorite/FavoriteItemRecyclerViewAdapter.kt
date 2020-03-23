package space.celestia.MobileCelestia.Favorite

import android.view.Menu
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.ItemTouchHelper
import space.celestia.MobileCelestia.Common.CommonSectionV2
import space.celestia.MobileCelestia.Common.CommonTextViewHolder

import space.celestia.MobileCelestia.Favorite.FavoriteItemFragment.Listener

import space.celestia.MobileCelestia.Common.RecyclerViewItem
import space.celestia.MobileCelestia.Common.SeparatorHeaderRecyclerViewAdapter
import space.celestia.MobileCelestia.Core.CelestiaScript
import java.io.Serializable
import java.lang.RuntimeException

enum class FavoriteItemAction {
    Delete, Rename
}

interface FavoriteBaseItem : RecyclerViewItem, Serializable {
    val children: List<FavoriteBaseItem>
    val isLeaf: Boolean
    val title: String
}

interface MutableFavoriteBaseItem : FavoriteBaseItem {
    val supportedItemActions: List<FavoriteItemAction>

    fun insert(newItem: FavoriteBaseItem, index: Int)

    fun append(newItem: FavoriteBaseItem) {
        insert(newItem, children.size)
    }

    fun remove(index: Int)
    fun rename(newName: String)

    fun swap(index1: Int, index2: Int)
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
    override val supportedItemActions: List<FavoriteItemAction>
        get() = listOf(FavoriteItemAction.Delete, FavoriteItemAction.Rename)

    override fun insert(newItem: FavoriteBaseItem, index: Int) {
        if (!(newItem is FavoriteBookmarkItem))
            throw RuntimeException("$newItem does not match type FavoriteBookmarkItem")
        bookmark.children!!.add(index, newItem.bookmark)
    }

    override fun remove(index: Int) {
        bookmark.children!!.removeAt(index)
    }

    override fun rename(newName: String) {
        bookmark.name = newName
    }

    override fun swap(index1: Int, index2: Int) {
        val i1 = bookmark.children!![index1]
        val i2 = bookmark.children!![index2]
        bookmark.children!![index2] = i1
        bookmark.children!![index1] = i2
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

class FavoriteItemRecyclerViewAdapter private constructor(
    private val item: FavoriteBaseItem,
    private var children: List<FavoriteBaseItem>,
    private val listener: Listener?
) : SeparatorHeaderRecyclerViewAdapter(listOf(CommonSectionV2(children))) {

    constructor(item: FavoriteBaseItem, listener: Listener?) : this(item, item.children, listener)

    val editable: Boolean
        get() = item is MutableFavoriteBaseItem

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
            if (item is MutableFavoriteBaseItem && item.supportedItemActions.size > 0) {
                val actions = item.supportedItemActions
                holder.itemView.setOnLongClickListener {
                    val popup = PopupMenu(it.context, it)
                    for (i in 0 until actions.size) {
                        val action = actions[i]
                        popup.menu.add(Menu.NONE, i, Menu.NONE, action.toString())
                    }
                    popup.setOnMenuItemClickListener {
                        val action = actions[it.itemId]
                        when (action) {
                            FavoriteItemAction.Delete -> {
                                listener?.deleteFavoriteItem(children.indexOf(item))
                            }
                            FavoriteItemAction.Rename -> {
                                listener?.renameFavoriteItem(item)
                            }
                        }
                        return@setOnMenuItemClickListener true
                    }
                    popup.show()
                    return@setOnLongClickListener true
                }
            } else {
                holder.itemView.setOnLongClickListener(null)
            }
            return
        }
        super.bindVH(holder, item)
    }

    override fun swapItem(item1: RecyclerViewItem, item2: RecyclerViewItem): Boolean {
        val index1 = children.indexOf(item1)
        val index2 = children.indexOf(item2)
        if (index1 < 0 || index2 < 0 || !(item is MutableFavoriteBaseItem))
            return false

        item.swap(index1, index2)
        return true
    }

    fun reload() {
        children = item.children
        updateSectionsWithHeader(listOf(CommonSectionV2(children)))
    }

    private companion object {
        const val FAVORITE_TYPE = 0
        const val FAVORITE_SCRIPT = 1
        const val FAVORITE_BOOKMARK = 2
    }
}

class FavoriteItemItemTouchCallback(val adapter: FavoriteItemRecyclerViewAdapter):
    ItemTouchHelper.Callback() {
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = if (adapter.editable && viewHolder is CommonTextViewHolder) ItemTouchHelper.UP or ItemTouchHelper.DOWN else 0
        return makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        if (viewHolder is CommonTextViewHolder && target is CommonTextViewHolder) {
            val fromPos = viewHolder.adapterPosition
            val toPos = target.adapterPosition
            return adapter.swapItem(fromPos, toPos)
        }
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

    override fun isItemViewSwipeEnabled(): Boolean { return false }
}