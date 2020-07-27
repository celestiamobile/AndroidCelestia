/*
 * SettingsLanguageFragment.kt
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
import space.celestia.mobilecelestia.MainActivity
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.utils.CelestiaString

class SettingsLanguageFragment : SettingsBaseFragment() {
    private var listener: Listener? = null
    private var dataSource: DataSource? = null

    private val listAdapter by lazy { SettingsLanguageRecyclerViewAdapter(listener, dataSource) }

    override val title: String
        get() = CelestiaString("Data Location", "")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            inflater.inflate(R.layout.fragment_general_grouped_list, container, false)

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
            throw RuntimeException("$context must implement SettingsLanguageFragment.Listener and SettingsLanguageFragment.DataSource")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        dataSource = null
    }

    override fun reload() {
        listAdapter.reload()
        listAdapter.notifyDataSetChanged()
    }

    interface Listener {
        fun onSetOverrideLanguage(language: String?)
    }

    interface DataSource {
        fun availableLanguages(): List<String>
        fun currentOverrideLanguage(): String?
        fun currentLanguage(): String
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SettingsLanguageFragment()
    }
}
