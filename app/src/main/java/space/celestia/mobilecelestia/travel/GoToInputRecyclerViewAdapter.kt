/*
 * GoToInputRecyclerViewAdapter.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.travel

import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import space.celestia.celestia.GoToLocation
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.common.*


enum class GoToFloatValueType {
    Longitude, Latitude
}

enum class GoToDoubleValueType {
    Distance
}

class GoToDoubleValueItem(val title: String, val value: Double, val type: GoToDoubleValueType): RecyclerViewItem
class GoToFloatValueItem(val title: String, val value: Float, val type: GoToFloatValueType): RecyclerViewItem
class GoToObjectItem(val objectName: String): RecyclerViewItem
class GoToUnitItem(val unit: GoToLocation.DistanceUnit): RecyclerViewItem
class GoToProceedItem: RecyclerViewItem

class GoToInputRecyclerViewAdapter(
    private val chooseFloatValueCallback: (GoToFloatValueType, Float) -> Unit,
    private val chooseDoubleValueCallback: (GoToDoubleValueType, Double) -> Unit,
    private val chooseUnitCallback: (GoToLocation.DistanceUnit) -> Unit,
    private val chooseObjectCallback: (String) -> Unit,
    private val proceedCallback: () -> Unit,
    var objectName: String = "Earth",
    var longitude: Float,
    var latitude: Float,
    var distance: Double,
    var unit: GoToLocation.DistanceUnit
) : SeparatorHeaderRecyclerViewAdapter(createSections(objectName, longitude, latitude, distance, unit)) {
    override fun onItemSelected(item: RecyclerViewItem) {
        when (item) {
            is GoToFloatValueItem -> {
                chooseFloatValueCallback(item.type, item.value)
            }
            is GoToDoubleValueItem -> {
                chooseDoubleValueCallback(item.type, item.value)
            }
            is GoToUnitItem -> {
                chooseUnitCallback(item.unit)
            }
            is GoToProceedItem -> {
                proceedCallback()
            }
            is GoToObjectItem -> {
                chooseObjectCallback(objectName)
            }
        }
    }

    override fun itemViewType(item: RecyclerViewItem): Int {
        if (item is GoToDoubleValueItem)
            return DOUBLE_VALUE_ITEM
        if (item is GoToFloatValueItem)
            return FLOAT_VALUE_ITEM
        if (item is GoToObjectItem)
            return OBJECT_ITEM
        if (item is GoToUnitItem)
            return UNIT_ITEM
        if (item is GoToProceedItem)
            return PROCEED_BUTTON
        return super.itemViewType(item)
    }

    override fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == DOUBLE_VALUE_ITEM || viewType == FLOAT_VALUE_ITEM || viewType == UNIT_ITEM || viewType == OBJECT_ITEM)
            return CommonTextViewHolder(parent)
        if (viewType == PROCEED_BUTTON) {
            val holder = CommonTextViewHolder(parent)
            holder.title.setTextColor(parent.context.getPrimaryColor())
            return holder
        }
        return super.createVH(parent, viewType)
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        if (item is GoToDoubleValueItem && holder is CommonTextViewHolder) {
            holder.title.text = item.title
            holder.detail.visibility = View.VISIBLE
            holder.detail.text = String.format("%.2f", item.value)
            return
        }
        if (item is GoToFloatValueItem && holder is CommonTextViewHolder) {
            holder.title.text = item.title
            holder.detail.visibility = View.VISIBLE
            holder.detail.text = String.format("%.2f", item.value)
            return
        }
        if (item is GoToObjectItem && holder is CommonTextViewHolder) {
            holder.title.text = CelestiaString("Object", "")
            holder.detail.visibility = View.VISIBLE
            holder.detail.text = item.objectName
            return
        }
        if (item is GoToUnitItem && holder is CommonTextViewHolder) {
            holder.title.text = ""
            holder.title.visibility = View.GONE
            holder.detail.visibility = View.VISIBLE
            holder.detail.updateLayoutParams<ViewGroup.MarginLayoutParams> { topMargin = 0 }
            holder.detail.text = CelestiaString(item.unit.name, "")
            return
        }
        if (item is GoToProceedItem && holder is CommonTextViewHolder) {
            holder.title.text = CelestiaString("Go", "")
            holder.title.visibility = View.VISIBLE
            return
        }
        super.bindVH(holder, item)
    }

    fun reload() {
        updateSectionsWithHeader(createSections(objectName, longitude, latitude, distance, unit))
    }

    private companion object {
        const val DOUBLE_VALUE_ITEM    = 0
        const val FLOAT_VALUE_ITEM     = 1
        const val OBJECT_ITEM          = 2
        const val UNIT_ITEM            = 3
        const val PROCEED_BUTTON       = 4

        fun createSections(objectName: String, longitude: Float, latitude: Float, distance: Double, unit: GoToLocation.DistanceUnit): List<CommonSectionV2> {
            return listOf(
                CommonSectionV2((listOf(GoToObjectItem(objectName)))),
                CommonSectionV2(listOf(
                    GoToFloatValueItem(CelestiaString("Longitude", ""), longitude, GoToFloatValueType.Longitude),
                    GoToFloatValueItem(CelestiaString("Latitude", ""), latitude, GoToFloatValueType.Latitude)
                )),
                CommonSectionV2(listOf(
                    GoToDoubleValueItem(CelestiaString("Distance", ""), distance, GoToDoubleValueType.Distance),
                    GoToUnitItem(unit)
                )),
                CommonSectionV2((listOf(GoToProceedItem())))
            )
        }
    }
}
