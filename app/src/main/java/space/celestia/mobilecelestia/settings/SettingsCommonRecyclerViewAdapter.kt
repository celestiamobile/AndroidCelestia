/*
 * SettingsCommonRecyclerViewAdapter.kt
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
import android.widget.Switch
import android.widget.TextView
import kotlinx.android.synthetic.main.common_text_list_with_slider_item.view.*
import kotlinx.android.synthetic.main.common_text_list_with_slider_item.view.title
import kotlinx.android.synthetic.main.common_text_list_with_switch_item.view.*
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.CommonSectionV2
import space.celestia.mobilecelestia.common.CommonTextViewHolder

import space.celestia.mobilecelestia.common.RecyclerViewItem
import space.celestia.mobilecelestia.common.SeparatorHeaderRecyclerViewAdapter
import space.celestia.mobilecelestia.core.CelestiaAppCore

fun SettingsCommonItem.createSections(): List<CommonSectionV2> {
    val results = ArrayList<CommonSectionV2>()
    for (section in sections) {
        val sectionResults = ArrayList<RecyclerViewItem>()
        for (row in section.rows) {
            sectionResults.add(row)
        }
        results.add(CommonSectionV2(sectionResults, section.header, section.footer))
    }
    return results
}

class SettingsCommonRecyclerViewAdapter(
    private val item: SettingsCommonItem,
    private val listener: SettingsCommonFragment.Listener?
) : SeparatorHeaderRecyclerViewAdapter(item.createSections()) {
    override fun itemViewType(item: RecyclerViewItem): Int {
        if (item is SettingsSliderItem)
            return ITEM_SLIDER

        if (item is SettingsActionItem)
            return ITEM_ACTION

        if (item is SettingsPreferenceSwitchItem)
            return ITEM_PREF_SWITCH

        return super.itemViewType(item)
    }

    override fun onItemSelected(item: RecyclerViewItem) {
        if (item is SettingsActionItem)
            listener?.onCommonSettingActionItemSelected(item.action)
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        if (holder is SliderViewHolder && item is SettingsSliderItem) {
            val value = (CelestiaAppCore.shared().getDoubleValueForField(item.key) - item.minValue) / (item.maxValue - item.minValue)
            holder.configure(item.name, value) { newValue ->
                val transformed = newValue * (item.maxValue - item.minValue) + item.minValue
                listener?.onCommonSettingSliderItemChange(item.key, transformed)
            }
            return
        }
        if (holder is CommonTextViewHolder) {
            if (item is SettingsActionItem) {
                holder.configure(item.name)
            }
            return
        }
        if (holder is PreferenceSwitchViewHolder && item is SettingsPreferenceSwitchItem) {
            holder.configure(item.name, listener?.commonSettingPreferenceSwitchState(item.key) ?: false) { checked ->
                listener?.onCommonSettingPreferenceSwitchStateChanged(item.key, checked)
            }
            return
        }
        super.bindVH(holder, item)
    }

    override fun createVH(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == ITEM_SLIDER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.common_text_list_with_slider_item, parent,false)
            return SliderViewHolder(view)
        }
        if (viewType == ITEM_ACTION) {
            return CommonTextViewHolder(parent)
        }
        if (viewType == ITEM_PREF_SWITCH) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.common_text_list_with_switch_item, parent,false)
            return PreferenceSwitchViewHolder(view)
        }
        return super.createVH(parent, viewType)
    }

    inner class SliderViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.title
        private val seekBar: SeekBar = view.slider

        fun configure(text: String, progress: Double, progressCallback: (Double) -> Unit) {
            title.text = text
            seekBar.max = 100
            seekBar.progress = (progress * 100).toInt()

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {}

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    if (seekBar == null) return;
                    progressCallback(seekBar.progress.toDouble() / 100)
                }
            })
        }
    }

    inner class PreferenceSwitchViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.title
        val switch: Switch = view.accessory

        fun configure(text:String, isChecked: Boolean, stateChangeCallback: (Boolean) -> Unit) {
            title.text = text
            switch.isChecked = isChecked
            switch.setOnCheckedChangeListener { _, isChecked ->
                stateChangeCallback(isChecked)
            }
        }
    }

    private companion object {
        const val ITEM_SLIDER           = 0
        const val ITEM_ACTION           = 1
        const val ITEM_PREF_SWITCH      = 2
    }
}
