/*
 * BrowserCommonRecyclerViewAdapter.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.browser

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.browser.BrowserCommonFragment.Listener
import space.celestia.mobilecelestia.common.CommonSectionV2
import space.celestia.mobilecelestia.common.CommonTextViewHolder
import space.celestia.mobilecelestia.common.RecyclerViewItem
import space.celestia.mobilecelestia.common.SeparatorHeaderRecyclerViewAdapter
import space.celestia.celestia.BrowserItem

fun BrowserItem.createSection(): List<CommonSectionV2> {
    val list = ArrayList<CommonSectionV2>()

    if (`object` != null) {
        val section = CommonSectionV2(listOf( BrowserUIItem(this, true) ))
        list.add(section)
    }
    list.add(CommonSectionV2(children.map { BrowserUIItem(it, it.children.size == 0) }))
    return list
}

class BrowserCommonRecyclerViewAdapter(
    item: BrowserItem,
    private val listener: Listener?
) : SeparatorHeaderRecyclerViewAdapter(item.createSection()) {

    override fun onItemSelected(item: RecyclerViewItem) {
        if (item is BrowserUIItem) {
            listener?.onBrowserItemSelected(item)
        }
    }

    override fun itemViewType(item: RecyclerViewItem): Int {
        if (item is BrowserUIItem) {
            return BROWSER_ITEM
        }
        return super.itemViewType(item)
    }

    override fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == BROWSER_ITEM) {
            return CommonTextViewHolder(parent)
        }
        return super.createVH(parent, viewType)
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        if (holder is CommonTextViewHolder && item is BrowserUIItem) {
            holder.title.text = item.item.name
            if (item.isLeaf) {
                holder.accessory.visibility = View.GONE
            } else {
                holder.accessory.visibility = View.VISIBLE
            }
            return
        }
        super.bindVH(holder, item)
    }

    companion object {
        const val BROWSER_ITEM  = 0
    }
}
