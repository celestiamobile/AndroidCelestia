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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.*
import space.celestia.mobilecelestia.core.CelestiaBrowserItem

class SubsystemBrowserFragment : BrowserRootFragment(), Cleanable {
    private val toolbar get() = view!!.findViewById<Toolbar>(R.id.toolbar)
    private var currentPath = ""
    private var rootPath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            val path = it.getString(ARG_ROOT_PATH, SUBSYSTEM_DEFAULT_PREFIX)
            rootPath = path
            currentPath = path
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (savedInstanceState != null) {
            rootPath = savedInstanceState.getString(ARG_ROOT_PATH, SUBSYSTEM_DEFAULT_PREFIX)
            currentPath = savedInstanceState.getString(ARG_CURR_PATH, rootPath)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(ARG_CURR_PATH, currentPath)
        outState.putString(ARG_ROOT_PATH, rootPath)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_general_container_with_toolbar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        toolbar.setNavigationOnClickListener {
            popLast()
        }
        toolbar.navigationIcon = null
        val browserItem = browserMap[rootPath]!![currentPath]!!
        toolbar.title = browserItem.alternativeName ?: browserItem.name
        replace(BrowserCommonFragment.newInstance(currentPath, rootPath), R.id.fragment_container)
    }

    override fun pushItem(browserItem: CelestiaBrowserItem) {
        toolbar.title = browserItem.name
        toolbar.setNavigationIcon(R.drawable.ic_action_arrow_back)
        currentPath = "$currentPath/${browserItem.name}"
        browserMap[rootPath]!![currentPath] = browserItem
        val frag = BrowserCommonFragment.newInstance(currentPath, rootPath)
        push(frag, R.id.fragment_container)
    }

    override fun canPop(): Boolean {
        return childFragmentManager.backStackEntryCount > 0
    }

    override fun popLast() {
        pop()
        val index = childFragmentManager.backStackEntryCount - 1
        if (index == 0) {
            // no more return
            toolbar.navigationIcon = null
        }
        toolbar.title = (childFragmentManager.fragments[index] as TitledFragment).title
    }

    override fun cleanUp() {
        browserMap.remove(currentPath)
    }

    companion object {
        private const val ARG_ROOT_PATH = "root"
        private const val ARG_CURR_PATH = "current"
        private const val SUBSYSTEM_DEFAULT_PREFIX = "subsystem"

        @JvmStatic
        fun newInstance(browserItem: CelestiaBrowserItem) =
            SubsystemBrowserFragment().apply {
                val root = "$SUBSYSTEM_DEFAULT_PREFIX/${browserItem.name}"
                browserMap[root] = hashMapOf(root to browserItem)
                arguments = Bundle().apply {
                    putString(ARG_ROOT_PATH, root)
                }
            }

        val browserMap = HashMap<String, HashMap<String, CelestiaBrowserItem>>()
    }
}
