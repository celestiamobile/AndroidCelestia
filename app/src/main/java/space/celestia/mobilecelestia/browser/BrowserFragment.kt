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
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.Poppable
import space.celestia.mobilecelestia.common.replace
import space.celestia.mobilecelestia.core.CelestiaAppCore
import space.celestia.mobilecelestia.core.CelestiaBrowserItem
import space.celestia.mobilecelestia.info.InfoFragment
import space.celestia.mobilecelestia.info.model.InfoDescriptionItem

interface BrowserRootFragment {
    fun pushItem(browserItem: CelestiaBrowserItem)
    fun showInfo(info: InfoDescriptionItem)
}

class BrowserFragment : Fragment(), Poppable, BrowserRootFragment, BottomNavigationView.OnNavigationItemSelectedListener {
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
        nav.setOnNavigationItemSelectedListener(this)
        if (savedInstanceState == null)
            replaceItem(browserItemMenu[0].item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        replaceItem(browserItemMenu[item.itemId].item)
        return true
    }

    private fun replaceItem(browserItem: CelestiaBrowserItem) {
        currentPath = browserItem.name
        browserMap[currentPath] = browserItem
        replace(BrowserNavigationFragment.newInstance(currentPath), R.id.navigation_container)
    }

    override fun pushItem(browserItem: CelestiaBrowserItem) {
        val navigationFragment = childFragmentManager.findFragmentById(R.id.navigation_container) as? BrowserNavigationFragment ?: return
        currentPath = "$currentPath/${browserItem.name}"
        browserMap[currentPath] = browserItem
        navigationFragment.pushItem(currentPath)
    }

    override fun canPop(): Boolean {
        val navigationFragment = childFragmentManager.findFragmentById(R.id.navigation_container) as? BrowserNavigationFragment ?: return false
        return navigationFragment.canPop()
    }

    override fun popLast() {
        val navigationFragment = childFragmentManager.findFragmentById(R.id.navigation_container) as? BrowserNavigationFragment ?: return
        navigationFragment.popLast()
    }

    companion object {
        private val browserItemMenu by lazy {
            val sim = CelestiaAppCore.shared().simulation
            listOf(
                BrowserItemMenu(sim.universe.solBrowserRoot(), R.drawable.browser_tab_sso),
                BrowserItemMenu(sim.starBrowserRoot(), R.drawable.browser_tab_star),
                BrowserItemMenu(sim.universe.dsoBrowserRoot(), R.drawable.browser_tab_dso)
            )
        }

        @JvmStatic
        fun newInstance() =
            BrowserFragment()

        val browserMap = HashMap<String, CelestiaBrowserItem>()
    }
}