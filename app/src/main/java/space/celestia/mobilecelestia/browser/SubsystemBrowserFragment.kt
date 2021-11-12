/*
 * SubsystemBrowserFragment.kt
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
import space.celestia.mobilecelestia.common.EndNavigationFragment
import space.celestia.celestia.BrowserItem
import space.celestia.celestia.Selection
import space.celestia.mobilecelestia.info.InfoFragment

class SubsystemBrowserFragment : EndNavigationFragment(), BrowserRootFragment {
    private var currentPath = ""
    private var rootPath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            rootPath = savedInstanceState.getString(ARG_ROOT_PATH, SUBSYSTEM_DEFAULT_PREFIX)
            currentPath = savedInstanceState.getString(ARG_CURR_PATH, rootPath)
        } else {
            arguments?.let {
                val path = it.getString(ARG_ROOT_PATH, SUBSYSTEM_DEFAULT_PREFIX)
                rootPath = path
                currentPath = path
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(ARG_CURR_PATH, currentPath)
        outState.putString(ARG_ROOT_PATH, rootPath)

        super.onSaveInstanceState(outState)
    }

    override fun createInitialFragment(savedInstanceState: Bundle?): SubFragment {
        return BrowserCommonFragment.newInstance(currentPath, rootPath)
    }

    override fun pushItem(browserItem: BrowserItem) {
        currentPath = "$currentPath/${browserItem.name}"
        browserMap[rootPath]!![currentPath] = browserItem
        pushFragment(BrowserCommonFragment.newInstance(currentPath, rootPath))
    }

    override fun showInfo(selection: Selection) {
        pushFragment(InfoFragment.newInstance(selection, true))
    }

    companion object {
        private const val ARG_ROOT_PATH = "root"
        private const val ARG_CURR_PATH = "current"
        private const val SUBSYSTEM_DEFAULT_PREFIX = "subsystem"

        @JvmStatic
        fun newInstance(browserItem: BrowserItem) =
            SubsystemBrowserFragment().apply {
                val root = "$SUBSYSTEM_DEFAULT_PREFIX/${browserItem.name}"
                browserMap[root] = hashMapOf(root to browserItem)
                arguments = Bundle().apply {
                    putString(ARG_ROOT_PATH, root)
                }
            }

        val browserMap = HashMap<String, HashMap<String, BrowserItem>>()
    }
}
