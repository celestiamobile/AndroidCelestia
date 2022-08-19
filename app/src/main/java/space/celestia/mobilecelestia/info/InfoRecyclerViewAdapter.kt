/*
 * InfoRecyclerViewAdapter.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.info

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import space.celestia.celestia.Selection
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.info.InfoFragment.Listener
import space.celestia.mobilecelestia.info.model.InfoActionItem
import space.celestia.mobilecelestia.info.model.InfoItem

class InfoRecyclerViewAdapter(
    private val values: List<InfoItem>,
    private val selection: Selection,
    private val listener: Listener?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val onClickListener: View.OnClickListener = View.OnClickListener { v ->
        val tag = v.tag
        if (tag is InfoActionItem) {
            listener?.onInfoActionSelected(tag, selection)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == ACTION_ITEM) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_info_action_item, parent, false)
            return ActionViewHolder(view)
        }
        throw RuntimeException("Unkwown view type $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = values[position]
        if (item is InfoActionItem && holder is ActionViewHolder) {
            holder.button.text = item.title
            holder.button.tag = item
            holder.button.setOnClickListener(onClickListener)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = values[position]
        if (item is InfoActionItem)
            return ACTION_ITEM
        return super.getItemViewType(position)
    }

    override fun getItemCount(): Int = values.size

    inner class ActionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val button: TextView
            get() = itemView.findViewById(R.id.action_button)
    }

    companion object {
        const val ACTION_ITEM         = 0
    }
}
