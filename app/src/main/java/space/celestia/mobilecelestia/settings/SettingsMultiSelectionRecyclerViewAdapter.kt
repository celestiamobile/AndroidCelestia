/*
 * SettingsMultiSelectionRecyclerViewAdapter.kt
 *
 * Copyright (C) 2001-2020, the Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.settings

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.common_text_list_with_switch_item.view.*
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.CommonSectionV2
import space.celestia.mobilecelestia.common.CommonTextViewHolder
import space.celestia.mobilecelestia.common.RecyclerViewItem
import space.celestia.mobilecelestia.common.SeparatorHeaderRecyclerViewAdapter
import space.celestia.mobilecelestia.core.CelestiaAppCore

fun SettingsMultiSelectionItem.createSections(): List<CommonSectionV2> {
    val sections = ArrayList<CommonSectionV2>()
    val core = CelestiaAppCore.shared()
    var on = true
    if (masterKey != null) {
        sections.add(CommonSectionV2(listOf(MasterSwitch(this))))
        on = core.getBooleanValueForPield(masterKey)
    }
    if (on)
        sections.add(CommonSectionV2(selections))
    return sections
}

class MasterSwitch(item: SettingsMultiSelectionItem) : RecyclerViewItem {
    val name = item.name
    val key = item.masterKey!!

    override val clickable: Boolean
        get() = false
}

class SettingsMultiSelectionRecyclerViewAdapter(
    val item: SettingsMultiSelectionItem,
    private val listener: SettingsMultiSelectionFragment.Listener?
) : SeparatorHeaderRecyclerViewAdapter(item.createSections()) {

    override fun onItemSelected(item: RecyclerViewItem) {
        if (item is SettingsMultiSelectionItem.Selection) {
            val core = CelestiaAppCore.shared()
            listener?.onMultiSelectionSettingItemChange(item.key, !core.getBooleanValueForPield(item.key))
        }
    }

    override fun itemViewType(item: RecyclerViewItem): Int {
        if (item is SettingsMultiSelectionItem.Selection)
            return SETTING_ITEM
        if (item is MasterSwitch)
            return SETTING_MASTER
        return super.itemViewType(item)
    }

    override fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == SETTING_ITEM) {
            val holder = CommonTextViewHolder(parent)
            holder.accessory.setImageResource(R.drawable.ic_check)
            ImageViewCompat.setImageTintList(holder.accessory, ColorStateList.valueOf(ResourcesCompat.getColor(parent.resources, R.color.colorThemeLabel, null)))
            return holder
        }
        if (viewType == SETTING_MASTER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.common_text_list_with_switch_item, parent,false)
            return MasterViewHolder(view)
        }
        return super.createVH(parent, viewType)
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        val core = CelestiaAppCore.shared()
        if (item is SettingsMultiSelectionItem.Selection && holder is CommonTextViewHolder) {
            holder.title.text = item.name
            holder.accessory.visibility = if (core.getBooleanValueForPield(item.key)) View.VISIBLE else View.INVISIBLE
            return
        }
        if (item is MasterSwitch && holder is MasterViewHolder) {
            holder.title.text = item.name
            holder.switch.isChecked = core.getBooleanValueForPield(item.key)
            holder.switch.setOnCheckedChangeListener { _, b ->
                listener?.onMultiSelectionSettingItemChange(item.key, b)
            }
            return
        }
        super.bindVH(holder, item)
    }

    fun reload() {
        updateSectionsWithHeader(item.createSections())
    }

    inner class MasterViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.title
        val switch: Switch = view.accessory
    }

    private companion object {
        const val SETTING_ITEM = 0
        const val SETTING_MASTER = 1
    }
}
