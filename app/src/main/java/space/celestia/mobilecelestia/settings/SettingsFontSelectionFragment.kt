/*
 * SettingsFontSelectionFragment.kt
 *
 * Copyright (C) 2001-2020, the Celestia Development Team
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
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.FontHelper

class SettingsFontSelectionFragment : SettingsBaseFragment() {

    private var listener: Listener? = null
    private var dataSource: DataSource? = null

    private val listAdapter by lazy { SettingsFontSelectionRecyclerViewAdapter(listener, dataSource) }

    override val title: String
        get() = CelestiaString("Font", "")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            inflater.inflate(R.layout.fragment_settings_font_selection_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = listAdapter
            }
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener && context is DataSource) {
            listener = context
            dataSource = context
        } else {
            throw RuntimeException("$context must implement SettingsFontSelectionFragment.Listener and SettingsFontSelectionFragment.DataSource")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun reload() {
        listAdapter.reload()
        listAdapter.notifyDataSetChanged()
    }

    interface Listener {
        fun onFontReset()
        fun onCustomFontProvided(font: FontHelper.FontCompat)
    }

    interface DataSource {
        val currentFont: FontHelper.FontCompat?
    }

    companion object {

        const val ARG_ITEM = "item"

        @JvmStatic
        fun newInstance() = SettingsFontSelectionFragment()
    }
}
