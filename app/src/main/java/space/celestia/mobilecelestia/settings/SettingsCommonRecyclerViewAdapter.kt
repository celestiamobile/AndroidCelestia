/*
 * SettingsCommonRecyclerViewAdapter.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
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
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.common_text_list_with_slider_item.view.*
import kotlinx.android.synthetic.main.common_text_list_with_slider_item.view.title
import kotlinx.android.synthetic.main.common_text_list_with_switch_item.view.*
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.CommonSectionV2
import space.celestia.mobilecelestia.common.CommonTextViewHolder
import space.celestia.mobilecelestia.common.RecyclerViewItem
import space.celestia.mobilecelestia.common.SeparatorHeaderRecyclerViewAdapter

class SettingsCommonRecyclerViewAdapter(
    private val item: SettingsCommonItem,
    private val listener: SettingsCommonFragment.Listener?,
    private val dataSource: SettingsCommonFragment.DataSource?
) : SeparatorHeaderRecyclerViewAdapter(listOf()) {

    init {
        reload()
    }

    override fun itemViewType(item: RecyclerViewItem): Int {
        if (item is SettingsSliderItem)
            return ITEM_SLIDER

        if (item is SettingsActionItem)
            return ITEM_ACTION

        if (item is SettingsPreferenceSwitchItem)
            return ITEM_PREF_SWITCH

        if (item is SettingsUnknownTextItem)
            return ITEM_UNKNOWN_TEXT

        if (item is SettingsSwitchItem)
            return if (item.representation == SettingsSwitchItem.Representation.Switch) ITEM_SWITCH else ITEM_CHECKMARK

        return super.itemViewType(item)
    }

    override fun onItemSelected(item: RecyclerViewItem) {
        if (item is SettingsActionItem)
            listener?.onCommonSettingActionItemSelected(item.action)
        else if (item is SettingsUnknownTextItem)
            listener?.onCommonSettingUnknownAction(item.id)
        else if (item is SettingsSwitchItem && item.representation == SettingsSwitchItem.Representation.Checkmark) {
            val on = dataSource?.commonSettingSwitchState(item.key) ?: false
            listener?.onCommonSettingSwitchStateChanged(item.key, !on, item.volatile)
        }
    }

    override fun bindVH(holder: RecyclerView.ViewHolder, item: RecyclerViewItem) {
        if (holder is SliderViewHolder && item is SettingsSliderItem) {
            val num = dataSource?.commonSettingSliderValue(item.key) ?: 0.0
            val value = (num - item.minValue) / (item.maxValue - item.minValue)
            holder.configure(item.name, value) { newValue ->
                val transformed = newValue * (item.maxValue - item.minValue) + item.minValue
                listener?.onCommonSettingSliderItemChange(item.key, transformed)
            }
            return
        }
        if (holder is CommonTextViewHolder) {
            if (item is SettingsActionItem) {
                holder.configure(item.name)
            } else if (item is SettingsUnknownTextItem) {
                holder.configure(item.name)
            } else if (item is SettingsSwitchItem) {
                val on = dataSource?.commonSettingSwitchState(item.key) ?: false
                holder.title.text = item.name
                holder.accessory.visibility = if (on) View.VISIBLE else View.INVISIBLE
            }
            return
        }
        if (holder is SwitchViewHolder) {
            if (item is SettingsPreferenceSwitchItem) {
                holder.configure(item.name, dataSource?.commonSettingPreferenceSwitchState(item.key) ?: false) { checked ->
                    listener?.onCommonSettingPreferenceSwitchStateChanged(item.key, checked)
                }
            } else if (item is SettingsSwitchItem) {
                val on = dataSource?.commonSettingSwitchState(item.key) ?: false
                holder.configure(item.name, on) { newValue ->
                    listener?.onCommonSettingSwitchStateChanged(item.key, newValue, item.volatile)
                }
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
        if (viewType == ITEM_ACTION || viewType == ITEM_UNKNOWN_TEXT) {
            return CommonTextViewHolder(parent)
        }
        if (viewType == ITEM_PREF_SWITCH || viewType == ITEM_SWITCH) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.common_text_list_with_switch_item, parent,false)
            return SwitchViewHolder(view)
        }
        if (viewType == ITEM_CHECKMARK) {
            val holder = CommonTextViewHolder(parent)
            holder.accessory.setImageResource(R.drawable.ic_check)
            ImageViewCompat.setImageTintList(holder.accessory, ColorStateList.valueOf(ResourcesCompat.getColor(parent.resources, R.color.colorThemeLabel, null)))
            return holder
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

    inner class SwitchViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.title
        val switch: Switch = view.accessory

        fun configure(text:String, isChecked: Boolean, stateChangeCallback: (Boolean) -> Unit) {
            title.text = text
            switch.isChecked = isChecked
            switch.setOnCheckedChangeListener { _, checked ->
                stateChangeCallback(checked)
            }
        }
    }

    fun reload() {
        val results = ArrayList<CommonSectionV2>()
        for (section in item.sections) {
            val sectionResults = ArrayList<RecyclerViewItem>()
            if (section.rows.size == 1 && section.rows[0] is SettingsDynamicListItem) {
                val item = section.rows[0] as SettingsDynamicListItem
                results.add(CommonSectionV2(item.createItems(), section.header, section.footer))
            } else {
                for (row in section.rows) {
                    if (row is SettingsDynamicListItem) {
                        throw RuntimeException("SettingsDynamicListItem should not be embedded in a multi-row section")
                    }
                    sectionResults.add(row)
                }
                results.add(CommonSectionV2(sectionResults, section.header, section.footer))
            }
        }
        updateSections(results)
    }

    private companion object {
        const val ITEM_SLIDER           = 0
        const val ITEM_ACTION           = 1
        const val ITEM_PREF_SWITCH      = 2
        const val ITEM_SWITCH           = 3
        const val ITEM_CHECKMARK        = 4
        const val ITEM_UNKNOWN_TEXT     = 5
    }
}
