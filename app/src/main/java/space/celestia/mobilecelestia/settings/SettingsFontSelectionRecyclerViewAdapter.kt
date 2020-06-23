/*
 * SettingsFontSelectionRecyclerViewAdapter.kt
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
import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.MainActivity
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.CommonSectionV2
import space.celestia.mobilecelestia.common.CommonTextViewHolder
import space.celestia.mobilecelestia.common.RecyclerViewItem
import space.celestia.mobilecelestia.common.SeparatorHeaderRecyclerViewAdapter
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.FontHelper

interface FontRecyclerViewItem: RecyclerViewItem {
    val title: String
    val detail: String?
}

private fun FontHelper.FontCompat.displayName(): String {
    return name ?: "${file.nameWithoutExtension} $collectionIndex"
}

class CurrentFontItem(val font: FontHelper.FontCompat?): FontRecyclerViewItem {
    override val title: String
        get() = CelestiaString("Current Font", "")

    override val detail: String? = if (font == null) CelestiaString("Unknown", "") else font.displayName()
}

class ResetFontItem: FontRecyclerViewItem {
    override val title: String
        get() = CelestiaString("Reset to Default", "")

    override val detail: String?
        get() = null
}

class CustomFontItem(val font: FontHelper.FontCompat): FontRecyclerViewItem {
    override val title: String
        get() = font.displayName()
    override val detail: String?
        get() = null
}

private fun createFontSections(dataSource: SettingsFontSelectionFragment.DataSource?): List<CommonSectionV2> {
    val sections = ArrayList<CommonSectionV2>()

    sections.add(CommonSectionV2(listOf(CurrentFontItem(dataSource?.currentFont))))

    val fontArray = MainActivity.availableSystemFonts.map { CustomFontItem(it) }
    if (fontArray.size > 0)
        sections.add(CommonSectionV2(fontArray))

    sections.add(CommonSectionV2(listOf(ResetFontItem()), "", CelestiaString("Configuration will take effect after a restart.", "")))

    return sections
}

class SettingsFontSelectionRecyclerViewAdapter(
    private val listener: SettingsFontSelectionFragment.Listener?,
    private val dataSource: SettingsFontSelectionFragment.DataSource?
) : SeparatorHeaderRecyclerViewAdapter(createFontSections(dataSource)) {

    override fun onItemSelected(item: RecyclerViewItem) {
        if (item is CustomFontItem) {
            listener?.onCustomFontProvided(item.font)
        }
        if (item is ResetFontItem) {
            listener?.onFontReset()
        }
    }

    override fun itemViewType(item: RecyclerViewItem): Int {
        if (item is CustomFontItem)
            return CUSTOM_FONT
        if (item is CurrentFontItem || item is ResetFontItem)
            return OTHER
        return super.itemViewType(item)
    }

    override fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == CUSTOM_FONT) {
            val holder = CommonTextViewHolder(parent)
            holder.accessory.setImageResource(R.drawable.ic_check)
            ImageViewCompat.setImageTintList(holder.accessory, ColorStateList.valueOf(
                ResourcesCompat.getColor(parent.resources, R.color.colorThemeLabel, null)))
            return holder
        }
        if (viewType == OTHER) {
            val holder = CommonTextViewHolder(parent)
            return holder
        }
        return super.createVH(parent, viewType)
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        if (holder is CommonTextViewHolder && item is FontRecyclerViewItem) {
            holder.title.text = item.title
            holder.detail.text = item.detail
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && item is CustomFontItem) {
                holder.accessory.visibility = if (item.font == dataSource?.currentFont) View.VISIBLE else View.GONE
            }
            return
        }
        super.bindVH(holder, item)
    }

    fun reload() {
        updateSectionsWithHeader(createFontSections(dataSource))
    }

    private companion object {
        const val OTHER = 0
        const val CUSTOM_FONT = 1
    }
}
