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

import android.view.*
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.StandardImageButton
import space.celestia.mobilecelestia.control.BottomControlFragment.Listener
import space.celestia.mobilecelestia.utils.CelestiaString

class BottomControlRecyclerViewAdapter(
    private val values: List<BottomControlAction>,
    private val listener: Listener?
) : RecyclerView.Adapter<BottomControlRecyclerViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_bottom_control_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        if (position == values.size)
            return HIDE_ACTION
        val item = values[position]
        if (item is InstantAction)
            return INSTANT_ACTION
        if (item is ContinuousAction)
            return CONTINUOUS_ACTION
        if (item is GroupAction)
            return GROUP_ACTION
        return super.getItemViewType(position)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == values.size) {
            holder.imageButton.setImageResource(R.drawable.bottom_control_hide)
            holder.imageButton.contentDescription = CelestiaString("Close", "")
            holder.imageButton.setOnClickListener {
                listener?.onBottomControlHide()
            }
            return
        }

        val item = values[position]
        holder.imageButton.setImageResource(item.imageID ?: 0)
        holder.imageButton.contentDescription = item.contentDescription
        if (item is InstantAction) {
            holder.imageButton.setOnClickListener {
                listener?.onInstantActionSelected(item.action)
            }
        } else if (item is ContinuousAction) {
            holder.imageButton.setOnTouchListener { view, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        listener?.onContinuousActionDown(item.action)
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        listener?.onContinuousActionUp(item.action)
                    }
                }
                return@setOnTouchListener view.onTouchEvent(event)
            }
        } else if (item is GroupAction) {
            holder.imageButton.setOnClickListener {
                val popup = PopupMenu(it.context, it)
                for (i in item.actions.indices) {
                    val action = item.actions[i]
                    popup.menu.add(Menu.NONE, i, Menu.NONE, action.title)
                }
                popup.setOnMenuItemClickListener { menuItem ->
                    listener?.onContinuousActionDown(item.actions[menuItem.itemId].action)
                    listener?.onContinuousActionUp(item.actions[menuItem.itemId].action)
                    return@setOnMenuItemClickListener true
                }
                popup.show()
            }
        }
    }

    override fun getItemCount(): Int = values.size + 1

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageButton: StandardImageButton
            get() = itemView.findViewById(R.id.button)
    }

    private companion object {
        const val INSTANT_ACTION = 0
        const val CONTINUOUS_ACTION = 1
        const val GROUP_ACTION = 2
        const val HIDE_ACTION = 3
    }
}
