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

import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import space.celestia.celestia.Renderer
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.*
import space.celestia.mobilecelestia.utils.CelestiaString

class RefreshRateItem(val frameRateOption: Int, val frameRate: Int, val checked: Boolean) : RecyclerViewItem
class ResetRefreshRateItem(val frameRate: Int, val checked: Boolean) : RecyclerViewItem

class SettingsRefreshRateRecyclerViewAdapter(
    private val listener: SettingsRefreshRateFragment.Listener?
) : SeparatorHeaderRecyclerViewAdapter(listOf()) {

    override fun onItemSelected(item: RecyclerViewItem) {
        if (item is RefreshRateItem) {
            listener?.onRefreshRateChanged(item.frameRateOption)
        }
        if (item is ResetRefreshRateItem) {
            listener?.onRefreshRateChanged(Renderer.FRAME_MAX)
        }
    }

    override fun itemViewType(item: RecyclerViewItem): Int {
        if (item is RefreshRateItem || item is ResetRefreshRateItem)
            return SETTING_ITEM
        return super.itemViewType(item)
    }

    override fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == SETTING_ITEM) {
            val holder =  CommonTextViewHolder(parent)
            holder.accessory.setImageResource(R.drawable.ic_check)
            ImageViewCompat.setImageTintList(holder.accessory, ColorStateList.valueOf(parent.context.getSecondaryColor()))
            return holder
        }
        return super.createVH(parent, viewType)
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        if (item is RefreshRateItem && holder is CommonTextViewHolder) {
            holder.title.text = CelestiaString("%d FPS", "").format(item.frameRate)
            holder.accessory.visibility = if (item.checked) View.VISIBLE else View.GONE
            return
        }
        if (item is ResetRefreshRateItem && holder is CommonTextViewHolder) {
            holder.title.text = CelestiaString("Maximum (%d FPS)", "").format(item.frameRate)
            holder.accessory.visibility = if (item.checked) View.VISIBLE else View.GONE
            return
        }
        super.bindVH(holder, item)
    }

    fun update(availableRefreshRates: List<Pair<Int, Int>>?, maxRefreshRate: Int?, selectedRateOption: Int) {
        val items: ArrayList<RecyclerViewItem> = ArrayList(availableRefreshRates?.map { RefreshRateItem(it.first, it.second, selectedRateOption == it.first) } ?: listOf())
        if (maxRefreshRate != null)
            items.add(0, ResetRefreshRateItem(maxRefreshRate, selectedRateOption == Renderer.FRAME_MAX))
        updateSectionsWithHeader(listOf(CommonSectionV2(items)))
    }

    private companion object {
        const val SETTING_ITEM = 0
    }
}
