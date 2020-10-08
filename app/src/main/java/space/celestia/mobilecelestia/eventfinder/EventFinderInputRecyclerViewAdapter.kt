/*
 * EventFinderInputRecyclerViewAdapter.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.eventfinder

import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.CommonSectionV2
import space.celestia.mobilecelestia.common.CommonTextViewHolder
import space.celestia.mobilecelestia.common.RecyclerViewItem
import space.celestia.mobilecelestia.common.SeparatorHeaderRecyclerViewAdapter
import space.celestia.mobilecelestia.core.CelestiaAppCore
import space.celestia.mobilecelestia.utils.CelestiaString
import java.text.DateFormat
import java.util.*

class EventFinderDateItem(val title: String, val date: Date, val isStartTime: Boolean): RecyclerViewItem
class EventFinderObjectItem(val objectName: String): RecyclerViewItem
class EventFinderProceedButton: RecyclerViewItem

class EventFinderInputRecyclerViewAdapter(
    private val chooseTimeCallback: (Boolean) -> Unit,
    private val chooseObjectCallback: (String) -> Unit,
    private val proceedCallback: () -> Unit,
    var objectName: String = "Earth",
    var startDate: Date = Date(Date().time - DEFAULT_SEARCHING_INTERVAL),
    var endDate: Date = Date()
) : SeparatorHeaderRecyclerViewAdapter(createSections(objectName, startDate, endDate)) {
    private val formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())

    override fun onItemSelected(item: RecyclerViewItem) {
        when (item) {
            is EventFinderDateItem -> {
                chooseTimeCallback(item.isStartTime)
            }
            is EventFinderProceedButton -> {
                proceedCallback()
            }
            is EventFinderObjectItem -> {
                chooseObjectCallback(objectName)
            }
        }
    }

    override fun itemViewType(item: RecyclerViewItem): Int {
        if (item is EventFinderDateItem)
            return DATE_ITEM
        if (item is EventFinderObjectItem)
            return OBJECT_ITEM
        if (item is EventFinderProceedButton)
            return PROCEED_BUTTON
        return super.itemViewType(item)
    }

    override fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == DATE_ITEM || viewType == OBJECT_ITEM)
            return CommonTextViewHolder(parent)
        if (viewType == PROCEED_BUTTON) {
            val holder = CommonTextViewHolder(parent)
            holder.title.setTextColor(ResourcesCompat.getColor(parent.resources, R.color.colorThemeLabel, null))
            return holder
        }
        return super.createVH(parent, viewType)
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        if (item is EventFinderDateItem && holder is CommonTextViewHolder) {
            holder.title.text = item.title
            holder.detail.visibility = View.VISIBLE
            holder.detail.text = formatter.format(item.date)
            return
        }
        if (item is EventFinderObjectItem && holder is CommonTextViewHolder) {
            holder.title.text = CelestiaString("Object", "")
            holder.detail.visibility = View.VISIBLE
            holder.detail.text = CelestiaAppCore.getLocalizedString(item.objectName, "celestia")
            return
        }
        if (item is EventFinderProceedButton && holder is CommonTextViewHolder) {
            holder.title.text = CelestiaString("Find", "")
            return
        }
        super.bindVH(holder, item)
    }

    fun reload() {
        updateSectionsWithHeader(createSections(objectName, startDate, endDate))
    }

    private companion object {
        const val DATE_ITEM      = 0
        const val OBJECT_ITEM    = 1
        const val PROCEED_BUTTON = 2

        const val DEFAULT_SEARCHING_INTERVAL: Long = 365L * 24 * 60 * 60 * 1000

        fun createSections(objectName: String, startDate: Date, endDate: Date): List<CommonSectionV2> {
            return listOf(
                CommonSectionV2(listOf(
                    EventFinderDateItem(CelestiaString("Start Time", ""), startDate, true),
                    EventFinderDateItem(CelestiaString("End Time", ""), endDate, false)
                )),
                CommonSectionV2((listOf(EventFinderObjectItem(objectName)))),
                CommonSectionV2((listOf(EventFinderProceedButton())))
            )
        }
    }
}
