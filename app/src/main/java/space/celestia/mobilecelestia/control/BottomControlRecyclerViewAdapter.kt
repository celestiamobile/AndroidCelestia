// BottomControlRecyclerViewAdapter.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.control

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.Menu
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.StandardImageButton
import space.celestia.mobilecelestia.control.BottomControlFragment.Listener
import space.celestia.mobilecelestia.utils.CelestiaString

class BottomControlRecyclerViewAdapter(
    private val items: List<BottomControlAction>,
    private val overflowItems: List<OverflowItem>,
    private val listener: Listener?
) : RecyclerView.Adapter<BottomControlRecyclerViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_bottom_control_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        if (position == items.size)
            return OVERFLOW_ACTION
        return when (items[position]) {
            is ContinuousAction -> CONTINUOUS_ACTION
            is CustomAction -> CUSTOM_ACTION
            is InstantAction -> INSTANT_ACTION
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == items.size) {
            holder.imageButton.setImageResource(R.drawable.bottom_toolbar_overflow)
            holder.imageButton.contentDescription = CelestiaString("More actions", "Button to show more actions to in the bottom toolbar")
            @SuppressLint("ClickableViewAccessibility")
            holder.imageButton.setOnTouchListener(null)
            holder.imageButton.setOnClickListener {
                val popup = PopupMenu(it.context, it)
                for (i in overflowItems.indices) {
                    val action = overflowItems[i]
                    popup.menu.add(Menu.NONE, i, Menu.NONE, action.title)
                }
                popup.menu.add(Menu.NONE, overflowItems.size, Menu.NONE, CelestiaString("Close", ""))
                popup.setOnMenuItemClickListener { menuItem ->
                    if (menuItem.itemId >= overflowItems.size) {
                        listener?.onBottomControlHide()
                    } else {
                        val action = overflowItems[menuItem.itemId].action
                        when (action) {
                            is InstantAction -> {
                                listener?.onInstantActionSelected(action.action)
                            }
                            is ContinuousAction -> {
                                listener?.onContinuousActionDown(action.action)
                                listener?.onContinuousActionUp(action.action)
                            }
                            is CustomAction -> {
                                listener?.onCustomAction(action.type)
                            }
                        }
                    }
                    return@setOnMenuItemClickListener true
                }
                popup.show()
            }
            return
        }

        val item = items[position]
        holder.imageButton.setImageResource(item.imageID ?: 0)
        holder.imageButton.contentDescription = item.contentDescription
        holder.imageButton.setOnClickListener(null)
        @SuppressLint("ClickableViewAccessibility")
        holder.imageButton.setOnTouchListener(null)
        when (item) {
            is InstantAction -> {
                holder.imageButton.setOnClickListener {
                    listener?.onInstantActionSelected(item.action)
                }
            }

            is ContinuousAction -> {
                @SuppressLint("ClickableViewAccessibility")
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
            }

            is CustomAction -> {
                holder.imageButton.setOnClickListener {
                    listener?.onCustomAction(item.type)
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size + 1

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageButton: StandardImageButton
            get() = itemView.findViewById(R.id.button)
    }

    private companion object {
        const val INSTANT_ACTION = 0
        const val CONTINUOUS_ACTION = 1
        const val OVERFLOW_ACTION = 3
        const val CUSTOM_ACTION = 4
    }
}
