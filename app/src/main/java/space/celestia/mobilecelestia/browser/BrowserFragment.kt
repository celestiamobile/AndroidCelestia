/*
 * BrowserFragment.kt
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
import android.view.*
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.*
import space.celestia.mobilecelestia.core.CelestiaAppCore
import space.celestia.mobilecelestia.core.CelestiaBrowserItem


abstract class BrowserRootFragment: PoppableFragment() {
    abstract fun pushItem(browserItem: CelestiaBrowserItem)
}

class BrowserFragment : BrowserRootFragment(), BottomNavigationView.OnNavigationItemSelectedListener {
    private val browserItemMenu by lazy {
        val sim = CelestiaAppCore.shared().simulation
        listOf(
            BrowserItemMenu(sim.universe.solBrowserRoot(), R.drawable.browser_tab_sso),
            BrowserItemMenu(sim.starBrowserRoot(), R.drawable.browser_tab_star),
            BrowserItemMenu(sim.universe.dsoBrowserRoot(), R.drawable.browser_tab_dso)
        )
    }

    private val toolbar get() = view!!.findViewById<Toolbar>(R.id.toolbar)
    private var currentPath = ""

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (savedInstanceState != null) {
            val p = savedInstanceState.getString("path")
            if (p != null) {
                currentPath = p
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString("path", currentPath)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_browser, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val nav = view.findViewById<BottomNavigationView>(R.id.navigation)
        for (i in 0 until browserItemMenu.count()) {
            val item = browserItemMenu[i]
            nav.menu.add(Menu.NONE, i, Menu.NONE, item.item.alternativeName ?: item.item.name).setIcon(item.icon)
        }
        toolbar.setNavigationOnClickListener {
            popLast()
        }
        nav.setOnNavigationItemSelectedListener(this)
        replaceItem(browserItemMenu[0].item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        replaceItem(browserItemMenu[item.itemId].item)
        return true
    }

    private fun replaceItem(browserItem: CelestiaBrowserItem) {
        toolbar.navigationIcon = null
        toolbar.title = browserItem.alternativeName ?: browserItem.name
        currentPath = browserItem.name
        browserMap[currentPath] = browserItem
        replace(BrowserCommonFragment.newInstance(currentPath), R.id.fragment_container)
    }

    override fun pushItem(browserItem: CelestiaBrowserItem) {
        toolbar.title = browserItem.name
        toolbar.setNavigationIcon(R.drawable.ic_action_arrow_back)
        currentPath = "$currentPath/${browserItem.name}"
        browserMap[currentPath] = browserItem
        val frag = BrowserCommonFragment.newInstance(currentPath)
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

    companion object {
        @JvmStatic
        fun newInstance() =
            BrowserFragment()

        val browserMap = HashMap<String, CelestiaBrowserItem>()
    }
}