/*
 * SettingsDataLocationRecyclerViewAdapter.kt
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
import space.celestia.mobilecelestia.common.CommonSectionV2
import space.celestia.mobilecelestia.common.CommonTextViewHolder
import space.celestia.mobilecelestia.common.RecyclerViewItem
import space.celestia.mobilecelestia.common.SeparatorHeaderRecyclerViewAdapter
import space.celestia.mobilecelestia.core.CelestiaAppCore
import space.celestia.mobilecelestia.utils.CelestiaString

enum class DataType {
    Config, DataDirectory;
}

interface DataLocationItem : RecyclerViewItem {
    val title: String
    val subtitle: String?
}

interface DataLocationDisplayItem : DataLocationItem {
    val custom: Boolean
    val type: DataType

    override val title: String
        get() = if (type == DataType.Config) CelestiaString("Config File", "") else CelestiaString("Data Directory", "")

    override val subtitle: String?
        get() = if (custom) CelestiaString("Custom", "") else CelestiaString("Default", "")
}

class SpecificDataLocationItem(override val custom: Boolean, override val type: DataType) : DataLocationDisplayItem

class ResetDataLocationItem : DataLocationItem {
    override val title: String
        get() = CelestiaString("Reset to Default", "")
    override val subtitle: String?
        get() = null
}

class SettingsDataLocationRecyclerViewAdapter(
    private val listener: SettingsDataLocationFragment.Listener?
) : SeparatorHeaderRecyclerViewAdapter(listOf()) {

    override fun onItemSelected(item: RecyclerViewItem) {
        if (item is ResetDataLocationItem) {
            listener?.onDataLocationNeedReset()
        } else if (item is DataLocationDisplayItem) {
            listener?.onDataLocationRequested(item.type)
        }
    }

    override fun itemViewType(item: RecyclerViewItem): Int {
        if (item is DataLocationItem)
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
        if (item is DataLocationItem && holder is CommonTextViewHolder) {
            holder.title.text = item.title
            holder.detail.visibility = View.VISIBLE
            holder.detail.text = item.subtitle
            return
        }
        super.bindVH(holder, item)
    }

    fun update(customConfig: Boolean, customDataDir: Boolean) {
        val sections = ArrayList<CommonSectionV2>()
        sections.add(CommonSectionV2(listOf(SpecificDataLocationItem(customConfig, DataType.Config),
            SpecificDataLocationItem(customDataDir, DataType.DataDirectory)
        ), "", CelestiaString("Configuration will take effect after a restart.", "")))
        sections.add(CommonSectionV2(listOf(ResetDataLocationItem()), null))
        updateSectionsWithHeader(sections)
    }

    private companion object {
        const val SETTING_ITEM = 0
    }
}
