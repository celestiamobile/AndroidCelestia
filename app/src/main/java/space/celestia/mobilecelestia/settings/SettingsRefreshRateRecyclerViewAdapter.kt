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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import space.celestia.celestia.Renderer
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.CommonSectionV2
import space.celestia.mobilecelestia.common.RadioButtonViewHolder
import space.celestia.mobilecelestia.common.RecyclerViewItem
import space.celestia.mobilecelestia.common.SeparatorHeaderRecyclerViewAdapter
import space.celestia.mobilecelestia.utils.CelestiaString
import java.lang.ref.WeakReference

class RefreshRateSelectionItem(val availableRefreshRates: List<Pair<Int, Int>>?, val maxRefreshRate: Int?, val selectedRateOption: Int) : RecyclerViewItem {
    override val clickable: Boolean
        get() = false
}

class SettingsRefreshRateRecyclerViewAdapter(
    private val listener: SettingsRefreshRateFragment.Listener?
) : SeparatorHeaderRecyclerViewAdapter(listOf()) {
    override fun itemViewType(item: RecyclerViewItem): Int {
        if (item is RefreshRateSelectionItem)
            return SELECTION_ITEM
        return super.itemViewType(item)
    }

    override fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == SELECTION_ITEM) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.common_text_list_with_radio_button_item, parent,false)
            return RadioButtonViewHolder(view)
        }
        return super.createVH(parent, viewType)
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        if (item is RefreshRateSelectionItem && holder is RadioButtonViewHolder) {
            val refreshRates = item.availableRefreshRates ?: listOf()
            val options = ArrayList(refreshRates.map { CelestiaString("%d FPS", "").format(it.second) })
            val checkedIndex: Int
            if (item.maxRefreshRate != null) {
                options.add(0, CelestiaString("Maximum (%d FPS)", "").format(item.maxRefreshRate))
                if (item.selectedRateOption == Renderer.FRAME_MAX) {
                    checkedIndex = 0
                } else {
                    val potentialIndex = refreshRates.indexOfFirst { it.first == item.selectedRateOption }
                    checkedIndex = if (potentialIndex == -1) -1 else potentialIndex + 1
                }
            } else {
                checkedIndex = refreshRates.indexOfFirst { it.first == item.selectedRateOption }
            }
            val weakSelf = WeakReference(this)
            holder.configure(text = "", showTitle = false, options = options, checkedIndex = checkedIndex) { newIndex ->
                val self = weakSelf.get() ?: return@configure
                if (item.maxRefreshRate != null) {
                    if (newIndex == 0) {
                        self.listener?.onRefreshRateChanged(Renderer.FRAME_MAX)
                    } else {
                        self.listener?.onRefreshRateChanged(refreshRates[newIndex - 1].first)
                    }
                } else {
                    self.listener?.onRefreshRateChanged(refreshRates[newIndex].first)
                }
            }
            return
        }
        super.bindVH(holder, item)
    }

    fun update(availableRefreshRates: List<Pair<Int, Int>>?, maxRefreshRate: Int?, selectedRateOption: Int) {
        updateSectionsWithHeader(listOf(CommonSectionV2(listOf(RefreshRateSelectionItem(availableRefreshRates, maxRefreshRate, selectedRateOption)))))
    }

    private companion object {
        const val SELECTION_ITEM = 0
    }
}
