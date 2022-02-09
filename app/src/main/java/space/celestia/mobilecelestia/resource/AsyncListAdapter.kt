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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.*
import space.celestia.mobilecelestia.utils.GlideUrlCustomCacheKey
import java.io.Serializable

interface AsyncListItem: Serializable {
    val name: String
    val imageURL: GlideUrlCustomCacheKey?
}

class AsyncListAdapterItem<T: AsyncListItem>(val item: T): RecyclerViewItem

open class BaseAsyncListAdapter<T: AsyncListItem>(
    showSeparators: Boolean,
    private val showHeader: Boolean,
    private val listener: AsyncListFragment.Listener<T>?
) : SeparatorHeaderRecyclerViewAdapter(
    showSeparators = showSeparators
) {
    open fun updateItems(items: List<T>) {
        updateSectionsWithHeader(listOf(CommonSectionV2( items.map { AsyncListAdapterItem(it) }, if (showHeader) "" else null)))
    }

    override fun onItemSelected(item: RecyclerViewItem) {
        if (item is AsyncListAdapterItem<*>) {
            @Suppress("UNCHECKED_CAST")
            listener?.onAsyncListItemSelected(item.item as T)
        }
    }
}

class AsyncListAdapter<T: AsyncListItem>(
    listener: AsyncListFragment.Listener<T>?
) : BaseAsyncListAdapter<T>(false, false, listener) {
    override fun itemViewType(item: RecyclerViewItem): Int {
        if (item is AsyncListAdapterItem<*>)
            return ITEM
        return super.itemViewType(item)
    }

    override fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == ITEM)
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.common_resource_item, parent, false))
        return super.createVH(parent, viewType)
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        if (item is AsyncListAdapterItem<*> && holder is AsyncListAdapter<*>.ViewHolder) {
            holder.title.text = item.item.name
            val imageURL = item.item.imageURL
            if (imageURL != null) {
                Glide.with(holder.image).load(imageURL).placeholder(R.drawable.resource_item_placeholder).into(holder.image)
            } else {
                Glide.with(holder.image).clear(holder.image)
                holder.image.setImageResource(R.drawable.resource_item_placeholder)
            }
            return
        }
        super.bindVH(holder, item)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView
            get() = itemView.findViewById(R.id.resource_title)
        val image: ImageView
            get() = itemView.findViewById(R.id.resource_image)
    }

    private companion object {
        const val ITEM = 0
    }
}

class PlainAsyncListAdapter<T: AsyncListItem>(
    listener: AsyncListFragment.Listener<T>?
) : BaseAsyncListAdapter<T>(true, true, listener) {
    override fun itemViewType(item: RecyclerViewItem): Int {
        if (item is AsyncListAdapterItem<*>)
            return ITEM
        return super.itemViewType(item)
    }

    override fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == ITEM) {
            val holder = CommonTextViewHolder(parent)
            holder.accessory.visibility = View.VISIBLE
            return holder
        }
        return super.createVH(parent, viewType)
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        if (item is AsyncListAdapterItem<*> && holder is CommonTextViewHolder) {
            holder.title.text = item.item.name
            return
        }
        super.bindVH(holder, item)
    }

    private companion object {
        const val ITEM = 0
    }
}
