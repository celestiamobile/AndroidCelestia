/*
 * SettingsSliderRecyclerViewAdapter.kt
 *
 * Copyright (C) 2001-2020, the Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.settings

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import kotlinx.android.synthetic.main.common_text_list_with_slider_item.view.*
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.CommonSectionV2

import space.celestia.mobilecelestia.common.RecyclerViewItem
import space.celestia.mobilecelestia.common.SeparatorHeaderRecyclerViewAdapter
import space.celestia.mobilecelestia.core.CelestiaAppCore

class SettingsSliderRecyclerViewAdapter(
    private val item: SettingsSliderItem,
    private val listener: SettingsSliderFragment.Listener?
) : SeparatorHeaderRecyclerViewAdapter(listOf(CommonSectionV2(listOf(item)))) {
    override fun itemViewType(item: RecyclerViewItem): Int {
        if (item is SettingsSliderItem) {
            return SETTING_ITEM
        }
        return super.itemViewType(item)
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        if (holder is SliderViewHolder && item is SettingsSliderItem) {
            holder.title.text = item.name
            holder.seekBar.max = 100
            holder.seekBar.progress = (100 * (CelestiaAppCore.shared().getDoubleValueForField(item.key) - item.minValue) / (item.maxValue - item.minValue)).toInt()
            holder.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {}

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    if (seekBar == null) return;
                    listener?.onSliderSettingItemChange(item.key, seekBar.progress.toDouble() / 100 * (item.maxValue - item.minValue) + item.minValue)
                }
            })
            return
        }
        super.bindVH(holder, item)
    }

    override fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == SETTING_ITEM) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.common_text_list_with_slider_item, parent,false)
            return SliderViewHolder(view)
        }
        return super.createVH(parent, viewType)
    }

    inner class SliderViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.title
        val seekBar: SeekBar = view.slider
    }

    private companion object {
        const val SETTING_ITEM = 0
    }
}
