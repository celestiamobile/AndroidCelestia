/*
 * SettingsCommonFragment.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.utils.PreferenceManager
import space.celestia.mobilecelestia.utils.getSerializableValue

class SettingsCommonFragment : NavigationFragment.SubFragment(), SettingsBaseFragment {
    private var item: SettingsCommonItem? = null
    private var listener: Listener? = null
    private var dataSource: DataSource? = null
    private var adapter: SettingsCommonRecyclerViewAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            item = it.getSerializableValue(ARG_ITEM, SettingsCommonItem::class.java)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_general_grouped_list, container, false)
        (view as? RecyclerView)?.let {
            it.layoutManager = LinearLayoutManager(context)
            adapter = SettingsCommonRecyclerViewAdapter(item!!, listener, dataSource)
            it.adapter = adapter
            it.clipToPadding = false
            it.fitsSystemWindows = true
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = item?.name ?: ""
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener && context is DataSource) {
            listener = context
            dataSource = context
        } else {
            throw RuntimeException("$context must implement SettingsCommonFragment.Listener and SettingsCommonFragment.DataSource")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        dataSource = null
        adapter = null
    }

    override fun reload() {
        super.reload()

        adapter?.reload()
        adapter?.notifyDataSetChanged()
    }

    interface Listener {
        fun onCommonSettingSliderItemChange(field: String, value: Double)
        fun onCommonSettingActionItemSelected(action: Int)
        fun onCommonSettingPreferenceSwitchStateChanged(key: PreferenceManager.PredefinedKey, value: Boolean)
        fun onCommonSettingPreferenceSliderStateChanged(key: PreferenceManager.PredefinedKey, value: Double)
        fun onCommonSettingSwitchStateChanged(field: String, value: Boolean, volatile: Boolean)
        fun onCommonSettingUnknownAction(id: String)
        fun onCommonSettingSelectionChanged(field: String, selected: Int)
        fun onCommonSettingSelectionRequested(key: PreferenceManager.PredefinedKey, options: List<Pair<Int, String>>)
        fun onCommonSettingSelectionRequested(key: String, options: List<Pair<Int, String>>)
    }

    interface DataSource {
        fun commonSettingPreferenceSwitchState(key: PreferenceManager.PredefinedKey): Boolean?
        fun commonSettingPreferenceSelectionState(key: PreferenceManager.PredefinedKey): Int?
        fun commonSettingPreferenceSliderState(key: PreferenceManager.PredefinedKey): Double?
        fun commonSettingSwitchState(field: String): Boolean
        fun commonSettingSliderValue(field: String): Double
        fun commonSettingSelectionValue(field: String): Int
    }

    companion object {
        private const val ARG_ITEM = "item"

        @JvmStatic
        fun newInstance(item: SettingsCommonItem) =
            SettingsCommonFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ITEM, item)
                }
            }
    }
}
