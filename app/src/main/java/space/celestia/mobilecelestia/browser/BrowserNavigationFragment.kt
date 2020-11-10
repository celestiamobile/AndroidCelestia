/*
 * BrowserNavigationFragment.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.browser

import android.os.Bundle
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.common.pop
import space.celestia.mobilecelestia.core.CelestiaAppCore

class BrowserNavigationFragment : NavigationFragment() {
    private val browserItemMenu by lazy {
        val sim = CelestiaAppCore.shared().simulation
        listOf(
            BrowserItemMenu(sim.universe.solBrowserRoot(), R.drawable.browser_tab_sso),
            BrowserItemMenu(sim.starBrowserRoot(), R.drawable.browser_tab_star),
            BrowserItemMenu(sim.universe.dsoBrowserRoot(), R.drawable.browser_tab_dso)
        )
    }

    private var path: String = ""
    private var root: BrowserItemMenu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            path = it.getString(ARG_PATH, "")
        }
    }

    override fun createInitialFragment(savedInstanceState: Bundle?): SubFragment {
        return BrowserCommonFragment.newInstance(path)
    }

    fun pushItem(path: String) {
        val frag = BrowserCommonFragment.newInstance(path)
        pushFragment(frag)
    }

    companion object {
        private const val ARG_PATH = "path"

        @JvmStatic
        fun newInstance(path: String) =
            BrowserNavigationFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PATH, path)
                }
            }
    }
}