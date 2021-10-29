/*
 * FavoriteItemRecyclerViewAdapter.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.favorite

import android.os.Build
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.*
import space.celestia.celestia.Destination
import space.celestia.celestia.Script
import space.celestia.mobilecelestia.favorite.FavoriteItemFragment.Listener
import space.celestia.mobilecelestia.utils.CelestiaString
import java.io.Serializable

enum class FavoriteItemAction {
    Delete, Rename, Share
}

interface FavoriteBaseItem : RecyclerViewItem, Serializable {
    val children: List<FavoriteBaseItem>
    val isLeaf: Boolean
    val title: String

    val supportedItemActions: List<FavoriteItemAction>
        get() = listOf()
}

interface MutableFavoriteBaseItem : FavoriteBaseItem {
    fun insert(newItem: FavoriteBaseItem, index: Int)

    fun append(newItem: FavoriteBaseItem) {
        insert(newItem, children.size)
    }

    fun remove(index: Int)
    fun rename(newName: String)

    fun swap(index1: Int, index2: Int)
}

enum class FavoriteType {
    Script,
    Destination
}

class FavoriteRoot : FavoriteBaseItem {
    override val children: List<FavoriteBaseItem>
        get() = listOf(
            FavoriteTypeItem(FavoriteType.Script),
            FavoriteBookmarkRootItem(currentBookmarkRoot),
            FavoriteTypeItem(FavoriteType.Destination)
        )
    override val title: String
        get() = CelestiaString("Favorites", "")
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
                FavoriteType.Destination -> {
                    return currentDestinations.map { FavoriteDestinationItem(it) }
                }
            }
        }
    override val title: String
    get() = when (type) {
        FavoriteType.Script -> {
            CelestiaString("Scripts", "")
        }
        FavoriteType.Destination -> {
            CelestiaString("Destinations", "")
        }
    }
    override val isLeaf: Boolean
        get() = false
}

class FavoriteScriptItem(val script: Script) : FavoriteBaseItem {
    override val children: List<FavoriteBaseItem>
        get() = listOf()
    override val title: String
        get() = script.title
    override val isLeaf: Boolean
        get() = true
}

class FavoriteDestinationItem(val destination: Destination): FavoriteBaseItem {
    override val children: List<FavoriteBaseItem>
        get() = listOf()
    override val title: String
        get() = destination.name
    override val isLeaf: Boolean
        get() = true
}

open class FavoriteBookmarkItem(val bookmark: BookmarkNode) : MutableFavoriteBaseItem {
    override val children: List<FavoriteBaseItem>
        get() = if (bookmark.isLeaf) listOf() else bookmark.children!!.map { FavoriteBookmarkItem(it) }
    override val title: String
        get() = bookmark.name
    override val isLeaf: Boolean
        get() = bookmark.isLeaf
    override val supportedItemActions: List<FavoriteItemAction>
        get() = listOf(FavoriteItemAction.Delete, FavoriteItemAction.Rename, FavoriteItemAction.Share)

    override fun insert(newItem: FavoriteBaseItem, index: Int) {
        if (newItem !is FavoriteBookmarkItem)
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

class FavoriteBookmarkRootItem(bookmark: BookmarkNode) : FavoriteBookmarkItem(bookmark) {
    // Root item does not support any customization
    override val supportedItemActions: List<FavoriteItemAction>
        get() = listOf()
}

fun updateCurrentScripts(scripts: List<Script>) {
    currentScripts = scripts
}

fun getCurrentBookmarks(): List<BookmarkNode> {
    return currentBookmarkRoot.children ?: return listOf()
}

fun updateCurrentBookmarks(nodes: List<BookmarkNode>) {
    currentBookmarkRoot.children = ArrayList(nodes)
}

fun updateCurrentDestinations(destinations: List<Destination>) {
    currentDestinations = destinations
}

private var currentScripts: List<Script> = listOf()
private var currentBookmarkRoot: BookmarkNode = BookmarkNode(CelestiaString("Bookmarks", "") , "", arrayListOf())
private var currentDestinations: List<Destination> = listOf()

class FavoriteItemRecyclerViewAdapter private constructor(
    private val item: FavoriteBaseItem,
    private var children: List<FavoriteBaseItem>,
    private val listener: Listener?,
    private val helper: ItemTouchHelper
) : SeparatorHeaderRecyclerViewAdapter(listOf(CommonSectionV2(children))) {

    constructor(item: FavoriteBaseItem, listener: Listener?, helper: ItemTouchHelper) : this(item, item.children, listener, helper)

    val editable: Boolean
        get() = item is MutableFavoriteBaseItem

    override fun onItemSelected(item: RecyclerViewItem) {
        (item as? FavoriteBaseItem)?.let {
            listener?.onFavoriteItemSelected(it)
        }
    }

    override fun itemViewType(item: RecyclerViewItem): Int {

        if (item is FavoriteBaseItem) {
            return if (editable) FAVORITE_EDITABLE else FAVORITE_CONST
        }
        return super.itemViewType(item)
    }

    override fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == FAVORITE_CONST) {
            return CommonTextViewHolder(parent)
        }
        if (viewType == FAVORITE_EDITABLE) {
            return CommonReorderableTextViewHolder(parent)
        }
        return super.createVH(parent, viewType)
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        if (holder is BaseTextItemHolder && item is FavoriteBaseItem) {
            holder.title.text = item.title
            holder.accessory.visibility = if (item.isLeaf) View.GONE else View.VISIBLE
            if (holder is CommonReorderableTextViewHolder) {
                // Can reorder
                holder.dragView.setOnLongClickListener {
                    helper.startDrag(holder)
                    return@setOnLongClickListener true
                }
            }
            if (item is MutableFavoriteBaseItem && item.supportedItemActions.isNotEmpty()) {
                val actions = item.supportedItemActions
                holder.itemView.setOnLongClickListener {
                    val popup = PopupMenu(it.context, it)
                    setupPopupMenu(popup, actions) { menuItem ->
                        when (menuItem) {
                            FavoriteItemAction.Delete -> {
                                listener?.deleteFavoriteItem(children.indexOf(item))
                            }
                            FavoriteItemAction.Rename -> {
                                listener?.renameFavoriteItem(item)
                            }
                            FavoriteItemAction.Share -> {
                                if (item.isLeaf) {
                                    listener?.shareFavoriteItem(item)
                                }
                            }
                        }
                    }
                    return@setOnLongClickListener true
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.itemView.setOnContextClickListener {
                        val popup = PopupMenu(it.context, it)
                        setupPopupMenu(popup, actions) { menuItem ->
                            when (menuItem) {
                                FavoriteItemAction.Delete -> {
                                    listener?.deleteFavoriteItem(children.indexOf(item))
                                }
                                FavoriteItemAction.Rename -> {
                                    listener?.renameFavoriteItem(item)
                                }
                                FavoriteItemAction.Share -> {
                                    if (item.isLeaf) {
                                        listener?.shareFavoriteItem(item)
                                    }
                                }
                            }
                        }
                        return@setOnContextClickListener true
                    }
                }
            } else {
                holder.itemView.setOnLongClickListener(null)
            }
            return
        }
        super.bindVH(holder, item)
    }

    private fun setupPopupMenu(menu: PopupMenu, actions: List<FavoriteItemAction>, handler: (FavoriteItemAction) -> Unit) {
        for (i in actions.indices) {
            val action = actions[i]
            menu.menu.add(Menu.NONE, i, Menu.NONE, CelestiaString(action.toString(), ""))
        }
        menu.setOnMenuItemClickListener { menuItem ->
            handler(actions[menuItem.itemId])
            return@setOnMenuItemClickListener true
        }
        menu.show()
    }

    override fun swapItem(item1: RecyclerViewItem, item2: RecyclerViewItem): Boolean {
        val index1 = children.indexOf(item1)
        val index2 = children.indexOf(item2)
        if (index1 < 0 || index2 < 0 || item !is MutableFavoriteBaseItem)
            return false

        item.swap(index1, index2)
        return true
    }

    fun reload() {
        children = item.children
        updateSectionsWithHeader(listOf(CommonSectionV2(children)))
    }

    inner class CommonReorderableTextViewHolder(parent: ViewGroup):
        RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.common_reorderable_text_list_item, parent, false)), BaseTextItemHolder {
        override val title: TextView
            get() = itemView.findViewById(R.id.title)
        override val accessory: ImageView
            get() = itemView.findViewById(R.id.accessory)
        val dragView: ImageView
            get() = itemView.findViewById(R.id.drag_accessory)
    }

    private companion object {
        const val FAVORITE_CONST    = 0
        const val FAVORITE_EDITABLE = 1
    }
}

class FavoriteItemItemTouchCallback :
    ItemTouchHelper.Callback() {
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val adapter = recyclerView.adapter as FavoriteItemRecyclerViewAdapter
        val dragFlags = if (adapter.editable && viewHolder is BaseTextItemHolder) ItemTouchHelper.UP or ItemTouchHelper.DOWN else 0
        return makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        if (viewHolder is BaseTextItemHolder && target is BaseTextItemHolder) {
            val adapter = recyclerView.adapter as FavoriteItemRecyclerViewAdapter
            val fromPos = viewHolder.bindingAdapterPosition
            val toPos = target.bindingAdapterPosition
            return adapter.swapItem(fromPos, toPos)
        }
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

    override fun isItemViewSwipeEnabled(): Boolean { return false }
    override fun isLongPressDragEnabled(): Boolean { return false }
}