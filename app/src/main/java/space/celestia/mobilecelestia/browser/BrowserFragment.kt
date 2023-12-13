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
import android.widget.LinearLayout
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.progressindicator.CircularProgressIndicator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import space.celestia.celestia.AppCore
import space.celestia.celestia.BrowserItem
import space.celestia.celestia.Selection
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.CelestiaExecutor
import space.celestia.mobilecelestia.common.Poppable
import space.celestia.mobilecelestia.common.replace
import space.celestia.mobilecelestia.info.InfoFragment
import space.celestia.celestiafoundation.utils.getSerializableValue
import java.io.Serializable
import javax.inject.Inject

interface BrowserRootFragment {
    fun pushItem(browserItem: BrowserItem)
    fun showInfo(selection: Selection)
}

@AndroidEntryPoint
class BrowserFragment : Fragment(), Poppable, BrowserRootFragment, NavigationBarView.OnItemSelectedListener {
    private var currentPath = ""
    private var selectedItemIndex = 0
    private lateinit var loadingIndicator: CircularProgressIndicator
    private lateinit var browserContainer: LinearLayout
    private lateinit var navigation: BottomNavigationView
    private var tabs = listOf<Tab>()

    @Inject
    lateinit var appCore: AppCore
    @Inject
    lateinit var executor: CelestiaExecutor

    class Tab(private val type: Type): Serializable {
        enum class Type: Serializable {
            SolarSystem, Star, DSO
        }

        val iconResource: Int
        get() {
            return when (type) {
                Type.SolarSystem -> {
                    R.drawable.browser_tab_sso
                }
                Type.Star -> {
                    R.drawable.browser_tab_star
                }
                Type.DSO -> {
                    R.drawable.browser_tab_dso
                }
            }
        }

        fun getBrowserItem(appCore: AppCore): BrowserItem {
            val universe = appCore.simulation.universe
            return when (type) {
                Type.SolarSystem -> {
                    universe.solBrowserRoot()!!
                }
                Type.Star -> {
                    universe.starBrowserRoot()
                }
                Type.DSO -> {
                    universe.dsoBrowserRoot()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            currentPath = savedInstanceState.getString(ARG_PATH_TAG, "")
            selectedItemIndex = savedInstanceState.getInt(ARG_ITEM_TAG, 0)
            @Suppress("UNCHECKED_CAST")
            tabs = savedInstanceState.getSerializableValue(ARG_TABS, ArrayList::class.java) as List<Tab>
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(ARG_PATH_TAG, currentPath)
        outState.putInt(ARG_ITEM_TAG, selectedItemIndex)
        outState.putSerializable(ARG_TABS, ArrayList(tabs))

        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_browser, container, false)
        loadingIndicator = view.findViewById(R.id.loading_indicator)
        browserContainer = view.findViewById(R.id.browser_container)
        navigation = view.findViewById(R.id.navigation)

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.navigation_container)) { _, insets ->
            // Consume bottom insets because we have a bottom bar now
            // TODO: the suggested replacement for the deprecated methods does not work
            val builder = WindowInsetsCompat.Builder(insets).setSystemWindowInsets(Insets.of(insets.systemWindowInsetLeft , insets.systemWindowInsetTop, insets.systemWindowInsetRight, 0))
            return@setOnApplyWindowInsetsListener builder.build()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (tabs.isEmpty()) {
            loadRootBrowserItems()
        } else {
            rootItemsLoaded()
            if (savedInstanceState == null) {
                showTab(selectedItemIndex)
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val newSelectedItemIndex = item.itemId
        if (newSelectedItemIndex != selectedItemIndex) {
            showTab(newSelectedItemIndex)
        }
        return true
    }

    private fun rootItemsLoaded() {
        browserContainer.visibility = View.VISIBLE
        loadingIndicator.visibility = View.GONE
        for (i in 0 until tabs.count()) {
            val tab = tabs[i]
            val item = tab.getBrowserItem(appCore)
            navigation.menu.add(Menu.NONE, i, Menu.NONE, item.alternativeName ?: item.name).setIcon(tab.iconResource)
        }
        navigation.selectedItemId = selectedItemIndex
        navigation.setOnItemSelectedListener(this)
    }

    private fun showTab(index: Int) {
        selectedItemIndex = index
        replaceItem(tabs[selectedItemIndex].getBrowserItem(appCore))
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

    private fun loadRootBrowserItems() = lifecycleScope.launch {
        browserContainer.visibility = View.GONE
        loadingIndicator.visibility = View.VISIBLE
        val browserTabs = arrayListOf(Tab(Tab.Type.Star), Tab(Tab.Type.DSO))
        withContext(executor.asCoroutineDispatcher()) {
            val universe = appCore.simulation.universe
            universe.createStaticBrowserItems()
            universe.createDynamicBrowserItems()
            val solRoot = universe.solBrowserRoot()
            if (solRoot != null) {
                browserTabs.add(0, Tab(Tab.Type.SolarSystem))
            }
        }
        tabs = browserTabs
        rootItemsLoaded()
        showTab(selectedItemIndex)
    }

    companion object {
        private const val ARG_PATH_TAG = "path"
        private const val ARG_ITEM_TAG = "item"
        private const val ARG_TABS = "tabs"

        @JvmStatic
        fun newInstance(): BrowserFragment {
            return BrowserFragment()
        }

        // Global lookup map for browser item. This allows restoration
        // as BrowserItem cannot be saved and restored
        val browserMap = HashMap<String, BrowserItem>()
    }
}