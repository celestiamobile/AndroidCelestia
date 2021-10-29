/*
 * SettingsCurrentTimeRecyclerViewAdapter.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
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
import space.celestia.celestia.AppCore
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.createDateFromJulianDay
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList

enum class CurrentTimeAction {
    PickDate, SetToCurrentTime;
}

interface CurrentTimeItem : RecyclerViewItem {
    val action: CurrentTimeAction
    val title: String
}

class DatePickerItem : CurrentTimeItem {
    override val action: CurrentTimeAction
        get() = CurrentTimeAction.PickDate

    override val title: String
        get() = CelestiaString("Select Time", "")
}

class SetToCurrentTimeItem : CurrentTimeItem {
    override val action: CurrentTimeAction
        get() = CurrentTimeAction.SetToCurrentTime

    override val title: String
        get() = CelestiaString("Set to Current Time", "")
}

private fun createSections(): List<CommonSectionV2> {
    val sections = ArrayList<CommonSectionV2>()
    sections.add(CommonSectionV2(listOf(DatePickerItem(), SetToCurrentTimeItem())))
    return sections
}

class SettingsCurrentTimeRecyclerViewAdapter(
    private val listener: SettingsCurrentTimeFragment.Listener?
) : SeparatorHeaderRecyclerViewAdapter(createSections()) {
    private val formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())

    override fun onItemSelected(item: RecyclerViewItem) {
        if (item is CurrentTimeItem) {
            listener?.onCurrentTimeActionRequested(item.action)
        }
    }

    override fun itemViewType(item: RecyclerViewItem): Int {
        if (item is SetToCurrentTimeItem || item is DatePickerItem)
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
        if (item is SetToCurrentTimeItem && holder is CommonTextViewHolder) {
            holder.title.text = item.title
            holder.detail.visibility = View.GONE
            return
        }
        if (item is DatePickerItem && holder is CommonTextViewHolder) {
            val core = AppCore.shared()
            holder.title.text = item.title
            holder.detail.visibility = View.VISIBLE
            holder.detail.text = formatter.format(createDateFromJulianDay(core.simulation.time))
            return
        }
        super.bindVH(holder, item)
    }

    fun reload() {
        updateSectionsWithHeader(createSections())
    }

    private companion object {
        const val SETTING_ITEM = 0
    }
}
