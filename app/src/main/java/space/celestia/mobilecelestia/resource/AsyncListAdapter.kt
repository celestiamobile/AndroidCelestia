/*
 * AsyncListAdapter.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.resource

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.common.*
import java.io.Serializable

interface AsyncListTextItem: Serializable {
    val name: String
}

class AsyncListAdapterItem<T: AsyncListTextItem>(val item: T): RecyclerViewItem

class AsyncListAdapter<T: AsyncListTextItem>(
    private val listener: AsyncListFragment.Listener<T>?
) : SeparatorHeaderRecyclerViewAdapter() {
    override fun itemViewType(item: RecyclerViewItem): Int {
        if (item is AsyncListAdapterItem<*>)
            return ITEM
        return super.itemViewType(item)
    }

    override fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == ITEM)
            return CommonTextViewHolder(parent)
        return super.createVH(parent, viewType)
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        if (item is AsyncListAdapterItem<*> && holder is CommonTextViewHolder) {
            holder.title.text = item.item.name
            holder.accessory.visibility = View.VISIBLE
            return
        }
        super.bindVH(holder, item)
    }

    override fun onItemSelected(item: RecyclerViewItem) {
        if (item is AsyncListAdapterItem<*>) {
            @Suppress("UNCHECKED_CAST")
            listener?.onAsyncListItemSelected(item.item as T)
        }
    }

    fun updateItems(items: List<T>) {
        updateSectionsWithHeader(listOf(CommonSectionV2(items.map { AsyncListAdapterItem(it) })))
    }

    private companion object {
        const val ITEM = 0
    }
}
