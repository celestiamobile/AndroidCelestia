/*
 * SettingsItemRecyclerViewAdapter.kt
 *
 * Copyright (C) 2001-2020, the Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.settings

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.common.CommonTextViewHolder
import space.celestia.mobilecelestia.common.RecyclerViewItem
import space.celestia.mobilecelestia.common.SeparatorHeaderRecyclerViewAdapter
import space.celestia.mobilecelestia.settings.SettingsItemFragment.Listener

class SettingsItemRecyclerViewAdapter(
    private val listener: Listener?
) : SeparatorHeaderRecyclerViewAdapter(mainSettingSections) {

    override fun onItemSelected(item: RecyclerViewItem) {
        if (item is SettingsItem)
            listener?.onMainSettingItemSelected(item)
    }

    override fun itemViewType(item: RecyclerViewItem): Int {
        if (item is SettingsItem)
            return SETTING_ITEM
        return super.itemViewType(item)
    }

    override fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == SETTING_ITEM)
            return CommonTextViewHolder(parent)
        return super.createVH(parent, viewType)
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        if (item is SettingsItem && holder is CommonTextViewHolder) {
            holder.title.text = item.name
            holder.accessory.visibility = View.VISIBLE
            return
        }
        super.bindVH(holder, item)
    }

    private companion object {
        const val SETTING_ITEM = 0
    }
}
