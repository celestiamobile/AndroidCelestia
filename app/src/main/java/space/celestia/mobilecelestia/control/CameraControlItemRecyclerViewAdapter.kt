/*
 * CameraControlItemRecyclerViewAdapter.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.control

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.*
import space.celestia.mobilecelestia.utils.CelestiaString

class CameraControlStepperItem(val title: String, val left: CameraControlAction, val right: CameraControlAction): RecyclerViewItem {
    override val clickable: Boolean
        get() = false

    companion object {
        val staticItems: List<CameraControlStepperItem> by lazy { listOf(
            CameraControlStepperItem(CelestiaString("Pitch", ""), CameraControlAction.Pitch0, CameraControlAction.Pitch1),
            CameraControlStepperItem(CelestiaString("Yaw", ""), CameraControlAction.Yaw0, CameraControlAction.Yaw1),
            CameraControlStepperItem(CelestiaString("Roll", ""), CameraControlAction.Roll0, CameraControlAction.Roll1)
        ) }
    }
}

class CameraControlReverseDirectionItem : RecyclerViewItem {
    val title: String
        get() = CelestiaString("Reverse Direction", "")
}

private val controlSections by lazy {
    listOf(
        CommonSectionV2(CameraControlStepperItem.staticItems, footer =  CelestiaString("Long press on stepper to change orientation.", "")),
        CommonSectionV2(listOf(CameraControlReverseDirectionItem()), null)
    )
}

class CameraControlItemRecyclerViewAdapter(
    private val listener: CameraControlFragment.Listener?
) : SeparatorHeaderRecyclerViewAdapter(controlSections), StepperView.Listener {

    override fun stepperTouchUp(view: StepperView, left: Boolean) {
        (view.tag as? CameraControlStepperItem)?.let {
            listener?.onCameraActionStepperTouchUp(if (left) it.left else it.right)
        }
    }

    override fun stepperTouchDown(view: StepperView, left: Boolean) {
        (view.tag as? CameraControlStepperItem)?.let {
            listener?.onCameraActionStepperTouchDown(if (left) it.left else it.right)
        }
    }

    override fun onItemSelected(item: RecyclerViewItem) {
        if (item is CameraControlReverseDirectionItem) {
            listener?.onCameraActionClicked(CameraControlAction.Reverse)
        }
    }

    override fun itemViewType(item: RecyclerViewItem): Int {
        if (item is CameraControlStepperItem) {
            return STEPPER
        }
        if (item is CameraControlReverseDirectionItem) {
            return TEXT
        }
        return super.itemViewType(item)
    }

    override fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == STEPPER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.common_text_list_with_stepper_item, parent, false)
            return StepperViewHolder(view)
        }
        if (viewType == TEXT) {
            return CommonTextViewHolder(parent)
        }
        return super.createVH(parent, viewType)
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        if (item is CameraControlStepperItem && holder is StepperViewHolder) {
            holder.title.text = item.title
            holder.stepper.tag = item
            holder.stepper.listener = this
            return
        }
        if (item is CameraControlReverseDirectionItem && holder is CommonTextViewHolder) {
            holder.title.text = item.title
            return
        }
        super.bindVH(holder, item)
    }

    inner class StepperViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView
            get() = itemView.findViewById(R.id.title)
        val stepper: StepperView
            get() = itemView.findViewById(R.id.stepper)
    }

    companion object {
        const val STEPPER = 0
        const val TEXT = 1
    }
}
