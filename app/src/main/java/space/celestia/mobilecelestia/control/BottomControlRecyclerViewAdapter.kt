/*
 * BottomControlRecyclerViewAdapter.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.control

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.StandardImageButton
import space.celestia.mobilecelestia.control.BottomControlFragment.Listener

class BottomControlRecyclerViewAdapter(
    private val values: List<CelestiaActionItem>,
    private val listener: Listener?
) : RecyclerView.Adapter<BottomControlRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as CelestiaActionItem
            listener?.onActionSelected(item.action)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_bottom_control_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == values.size) {
            holder.imageButton.setImageResource(R.drawable.bottom_control_hide)
            holder.imageButton.setOnClickListener {
                listener?.onBottomControlHide()
            }
            return
        }

        val item = values[position]

        holder.imageButton.setImageResource(item.image)

        with(holder.imageButton) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = values.size + 1

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageButton: StandardImageButton
            get() = itemView.findViewById(R.id.button)
    }
}
