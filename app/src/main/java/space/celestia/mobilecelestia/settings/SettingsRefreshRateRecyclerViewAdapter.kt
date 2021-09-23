/*
 * SettingsRefreshRateRecyclerViewAdapter.kt
 *
 * Copyright (C) 2001-2021, Celestia Development Team
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
import space.celestia.mobilecelestia.common.CommonSectionV2
import space.celestia.mobilecelestia.common.CommonTextViewHolder
import space.celestia.mobilecelestia.common.RecyclerViewItem
import space.celestia.mobilecelestia.common.SeparatorHeaderRecyclerViewAdapter
import space.celestia.mobilecelestia.utils.CelestiaString

class RefreshRateItem(val swapInterval: Int, val rate: Float) : RecyclerViewItem
class ResetRefreshRateItem() : RecyclerViewItem

class SettingsRefreshRateRecyclerViewAdapter(
    private val listener: SettingsRefreshRateFragment.Listener?
) : SeparatorHeaderRecyclerViewAdapter(listOf()) {

    override fun onItemSelected(item: RecyclerViewItem) {
        if (item is RefreshRateItem) {
            listener?.onRefreshRateChanged(item.swapInterval)
        }
        if (item is ResetRefreshRateItem) {
            listener?.onRefreshRateChanged(null)
        }
    }

    override fun itemViewType(item: RecyclerViewItem): Int {
        if (item is RefreshRateItem || item is ResetRefreshRateItem)
            return SETTING_ITEM
        return super.itemViewType(item)
    }

    override fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == SETTING_ITEM) {
            return CommonTextViewHolder(parent)
        }
        return super.createVH(parent, viewType)
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        if (item is RefreshRateItem && holder is CommonTextViewHolder) {
            holder.title.text = item.rate.toString()
            return
        }
        if (item is ResetRefreshRateItem && holder is CommonTextViewHolder) {
            holder.title.text = CelestiaString("Reset to Default", "")
            return
        }
        super.bindVH(holder, item)
    }

    fun update(availableRefreshRates: List<Pair<Int, Float>>) {
        val section = CommonSectionV2(availableRefreshRates.map { RefreshRateItem(it.first, it.second) })
        updateSectionsWithHeader(listOf(section, CommonSectionV2((listOf(ResetRefreshRateItem())))))
    }

    private companion object {
        const val SETTING_ITEM = 0
    }
}
