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
import space.celestia.mobilecelestia.common.NavigationFragment

class BrowserNavigationFragment : NavigationFragment() {
    private var path = ""

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