/*
 * ToolbarRecyclerViewAdapter.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.toolbar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.CommonSection
import space.celestia.mobilecelestia.common.RecyclerViewItem
import space.celestia.mobilecelestia.common.SeparatorRecyclerViewAdapter
import space.celestia.mobilecelestia.toolbar.ToolbarFragment.Listener
import space.celestia.mobilecelestia.toolbar.model.ToolbarActionItem
import space.celestia.mobilecelestia.toolbar.model.ToolbarListItem

class ToolbarRecyclerViewAdapter(
    values: List<List<ToolbarListItem>>,
    private val listener: Listener?
) : SeparatorRecyclerViewAdapter(separatorHeight = 6f, separatorLeft = 32f, sections = values.map { CommonSection(it,
    showSectionSeparator = true,
    showRowSeparator = false
) }, fullSection = false, showFirstAndLastSeparator = false) {

    override fun onItemSelected(item: RecyclerViewItem) {
        if (item is ToolbarActionItem) {
            listener?.onToolbarActionSelected(item.action)
        }
    }

    override fun itemViewType(item: RecyclerViewItem): Int {
        return TOOLBAR_ACTION
    }

    override fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_toolbar_action_item, parent, false)
        return ActionViewHolder(view)
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        if (item is ToolbarActionItem && holder is ActionViewHolder) {
            holder.contentView.text = item.title
            holder.imageView.setImageResource(item.image)
        }
    }

    inner class ActionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val contentView: TextView
            get() = itemView.findViewById(R.id.content)
        val imageView: ImageView
            get() = itemView.findViewById(R.id.image)
    }

    companion object {
        const val TOOLBAR_ACTION = 0
    }
}
