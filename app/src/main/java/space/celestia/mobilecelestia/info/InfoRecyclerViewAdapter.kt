/*
 * InfoRecyclerViewAdapter.kt
 *
 * Copyright (C) 2001-2020, the Celestia Development Team
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
import kotlinx.android.synthetic.main.fragment_info_action_item.view.*
import kotlinx.android.synthetic.main.fragment_info_description_item.view.*
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.info.InfoFragment.Listener
import space.celestia.mobilecelestia.info.model.InfoActionItem
import space.celestia.mobilecelestia.info.model.InfoDescriptionItem
import space.celestia.mobilecelestia.info.model.InfoItem

class InfoRecyclerViewAdapter(
    private val values: List<InfoItem>,
    private val listener: Listener?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val onClickListener: View.OnClickListener = View.OnClickListener { v ->
        val tag = v.tag
        if (tag is InfoActionItem) {
            listener?.onInfoActionSelected(tag)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == ACTION_ITEM) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_info_action_item, parent, false)
            return ActionViewHolder(view)
        }
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_info_description_item, parent, false)
        return DescriptionViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = values[position]
        if (item is InfoActionItem && holder is ActionViewHolder) {
            holder.button.text = item.title

            with(holder.view) {
                tag = item
                setOnClickListener(onClickListener)
            }
        } else if (item is InfoDescriptionItem && holder is DescriptionViewHolder) {
            holder.contentView.text = item.overview
            holder.titleView.text = item.name
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = values[position]
        if (item is InfoActionItem) {
            return ACTION_ITEM
        }
        if (item is InfoDescriptionItem) {
            return DESCRIPTION_ITEM
        }
        return super.getItemViewType(position)
    }

    override fun getItemCount(): Int = values.size

    inner class ActionViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val button: TextView = view.button
    }

    inner class DescriptionViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val contentView: TextView = view.content
        val titleView: TextView = view.title
    }

    companion object {
        const val ACTION_ITEM         = 0
        const val DESCRIPTION_ITEM    = 1
    }
}
