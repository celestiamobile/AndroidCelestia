/*
 * SettingsDataLocationFragment.kt
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
import space.celestia.mobilecelestia.MainActivity
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.utils.CelestiaString

class SettingsDataLocationFragment : SettingsBaseFragment() {
    private var listener: Listener? = null

    private val listAdapter by lazy { SettingsDataLocationRecyclerViewAdapter(listener) }

    override val title: String
        get() = CelestiaString("Data Location", "")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            inflater.inflate(R.layout.fragment_settings_current_time_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                listAdapter.update(customConfig, customDataDir)
                adapter = listAdapter
            }
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement SettingsDataLocationFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun reload() {
        listAdapter.update(customConfig, customDataDir)
        listAdapter.notifyDataSetChanged()
    }

    interface Listener {
        fun onDataLocationNeedReset()
        fun onDataLocationRequested(dataType: DataType)
    }

    companion object {
        val customConfig: Boolean
            get() = MainActivity.customConfigFilePath != null
        val customDataDir: Boolean
            get() = MainActivity.customDataDirPath != null

        @JvmStatic
        fun newInstance() =
            SettingsDataLocationFragment()
    }
}
