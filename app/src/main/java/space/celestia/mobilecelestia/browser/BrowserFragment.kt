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
import com.google.android.material.navigation.NavigationBarView
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.Poppable
import space.celestia.mobilecelestia.common.replace
import space.celestia.celestia.AppCore
import space.celestia.celestia.BrowserItem
import space.celestia.celestia.Selection
import space.celestia.mobilecelestia.info.InfoFragment

interface BrowserRootFragment {
    fun pushItem(browserItem: BrowserItem)
    fun showInfo(selection: Selection)
}

class BrowserFragment : Fragment(), Poppable, BrowserRootFragment, NavigationBarView.OnItemSelectedListener {
    private var currentPath = ""
    private var selectedItemIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            currentPath = savedInstanceState.getString(ARG_PATH_TAG, "")
            selectedItemIndex = savedInstanceState.getInt(ARG_ITEM_TAG, 0)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(ARG_PATH_TAG, currentPath)
        outState.putInt(ARG_ITEM_TAG, selectedItemIndex)

        super.onSaveInstanceState(outState)
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
        nav.selectedItemId = selectedItemIndex
        nav.setOnItemSelectedListener(this)

        if (savedInstanceState == null)
            showTab(selectedItemIndex)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val newSelectedItemIndex = item.itemId
        if (newSelectedItemIndex != selectedItemIndex) {
            showTab(newSelectedItemIndex)
        }
        return true
    }

    private fun showTab(index: Int) {
        selectedItemIndex = index
        replaceItem(browserItemMenu[selectedItemIndex].item)
    }

    private fun replaceItem(browserItem: BrowserItem) {
        currentPath = browserItem.name
        browserMap[currentPath] = browserItem
        replace(BrowserNavigationFragment.newInstance(currentPath), R.id.navigation_container)
    }

    override fun pushItem(browserItem: BrowserItem) {
        val navigationFragment = childFragmentManager.findFragmentById(R.id.navigation_container) as? BrowserNavigationFragment ?: return
        currentPath = "$currentPath/${browserItem.name}"
        browserMap[currentPath] = browserItem
        navigationFragment.pushItem(currentPath)
    }

    override fun showInfo(selection: Selection) {
        val navigationFragment = childFragmentManager.findFragmentById(R.id.navigation_container) as? BrowserNavigationFragment ?: return
        navigationFragment.pushFragment(InfoFragment.newInstance(selection, true))
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
        private const val ARG_PATH_TAG = "path"
        private const val ARG_ITEM_TAG = "item"

        private val browserItemMenu by lazy {
            val sim = AppCore.shared().simulation
            var list = arrayListOf(
                BrowserUIItemMenu(sim.starBrowserRoot(), R.drawable.browser_tab_star),
                BrowserUIItemMenu(sim.universe.dsoBrowserRoot(), R.drawable.browser_tab_dso)
            )
            val solRoot = sim.universe.solBrowserRoot()
            if (solRoot != null) {
                list.add(0, BrowserUIItemMenu(solRoot, R.drawable.browser_tab_sso))
            }
            list
        }

        @JvmStatic
        fun newInstance() =
            BrowserFragment()

        val browserMap = HashMap<String, BrowserItem>()
    }
}