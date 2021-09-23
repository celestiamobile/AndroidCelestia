/*
 * SettingsRefreshRateFragment.kt
 *
 * Copyright (C) 2001-2021, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.settings

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Bundle
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.hardware.display.DisplayManagerCompat
import androidx.core.view.DisplayCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.MainActivity
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.utils.CelestiaString

class SettingsRefreshRateFragment : SettingsBaseFragment() {
    private var listener: Listener? = null

    private val listAdapter by lazy { SettingsRefreshRateRecyclerViewAdapter(listener) }

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
                listAdapter.update(availableRefreshRates())
                adapter = listAdapter
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null)
            title = CelestiaString("Refresh Rate", "")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement SettingsRefreshRateFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun availableRefreshRates(): List<Pair<Int, Float>> {
        val activity = this.activity ?: return listOf()
        val displayManager = DisplayManagerCompat.getInstance(activity)
        val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY) ?: return listOf()
        val supportedRefreshRates = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) display.supportedModes.map { it.refreshRate } else display.supportedRefreshRates.toList()
        val maxRefreshRate = supportedRefreshRates.maxOrNull() ?: return listOf()
        return listOf(
            Pair(1, maxRefreshRate),
            Pair(2, maxRefreshRate / 2),
            Pair(3, maxRefreshRate / 3),
            Pair(4, maxRefreshRate / 4),
            Pair(5, maxRefreshRate / 5)
        )
    }

    override fun reload() {
        listAdapter.update(availableRefreshRates())
        listAdapter.notifyDataSetChanged()
    }

    interface Listener {
        fun onRefreshRateChanged(newSwapInterval: Int?)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SettingsRefreshRateFragment()
    }
}
