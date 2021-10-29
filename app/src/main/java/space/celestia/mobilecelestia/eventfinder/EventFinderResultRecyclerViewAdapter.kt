/*
 * EventFinderResultRecyclerViewAdapter.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.eventfinder

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.common.CommonSectionV2
import space.celestia.mobilecelestia.common.CommonTextViewHolder
import space.celestia.mobilecelestia.common.RecyclerViewItem
import space.celestia.mobilecelestia.common.SeparatorHeaderRecyclerViewAdapter
import space.celestia.celestia.EclipseFinder
import space.celestia.mobilecelestia.utils.createDateFromJulianDay
import java.text.DateFormat
import java.util.*

class EventFinderEclipseItem(val eclipse: EclipseFinder.Eclipse): RecyclerViewItem

class EventFinderResultRecyclerViewAdapter(
    private val onEclipseChosen: (EclipseFinder.Eclipse) -> Unit,
    eclipses: List<EclipseFinder.Eclipse>
) : SeparatorHeaderRecyclerViewAdapter(listOf(
    CommonSectionV2(eclipses.map { EventFinderEclipseItem(it) }, null)
)) {
    private val formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())

    override fun onItemSelected(item: RecyclerViewItem) {
        if (item is EventFinderEclipseItem) {
            onEclipseChosen(item.eclipse)
        }
    }

    override fun itemViewType(item: RecyclerViewItem): Int {
        if (item is EventFinderEclipseItem)
            return ECLIPSE_ITEM
        return super.itemViewType(item)
    }

    override fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == ECLIPSE_ITEM)
            return CommonTextViewHolder(parent)
        return super.createVH(parent, viewType)
    }

    @SuppressLint("SetTextI18n")
    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        if (item is EventFinderEclipseItem && holder is CommonTextViewHolder) {
            holder.title.text = "${item.eclipse.occulter.name} -> ${item.eclipse.receiver.name}"
            holder.detail.visibility = View.VISIBLE
            holder.detail.text = formatter.format(createDateFromJulianDay(item.eclipse.startTimeJulian))
            return
        }
        super.bindVH(holder, item)
    }

    private companion object {
        const val ECLIPSE_ITEM = 0
    }
}
