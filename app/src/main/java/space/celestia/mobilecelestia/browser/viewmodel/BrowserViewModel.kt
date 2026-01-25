package space.celestia.mobilecelestia.browser.viewmodel

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import space.celestia.celestia.AppCore
import space.celestia.celestia.BrowserItem
import space.celestia.celestia.Selection
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.browser.viewmodel.createDynamicBrowserItems
import space.celestia.mobilecelestia.browser.viewmodel.createStaticBrowserItems
import space.celestia.mobilecelestia.browser.viewmodel.dsoBrowserRoot
import space.celestia.mobilecelestia.browser.viewmodel.solBrowserRoot
import space.celestia.mobilecelestia.browser.viewmodel.starBrowserRoot
import space.celestia.mobilecelestia.common.CelestiaExecutor
import java.io.Serializable
import javax.inject.Inject

sealed class Page {
    data class Item(val item: BrowserItem) : Page()
    data class Info(val info: Selection) : Page()
}

@HiltViewModel
class BrowserViewModel @Inject constructor(val appCore: AppCore, val executor: CelestiaExecutor) : ViewModel() {
    val backStacks = mutableStateListOf(mutableStateListOf<Page>())
    var tabs = mutableStateListOf<Tab>()
    var selectedTabIndex = mutableIntStateOf(0)

    class Tab(private val type: Type, val rootItem: BrowserItem) {
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
            val simulation = appCore.simulation
            val universe = simulation.universe
            return when (type) {
                Type.SolarSystem -> {
                    simulation.solBrowserRoot()!!
                }
                Type.Star -> {
                    universe.starBrowserRoot(simulation.activeObserver)
                }
                Type.DSO -> {
                    universe.dsoBrowserRoot()
                }
            }
        }
    }

    suspend fun loadRootBrowserItems() {
        val browserTabs = arrayListOf<Tab>()
        withContext(executor.asCoroutineDispatcher()) {
            val simulation = appCore.simulation
            val universe = simulation.universe
            val observer = simulation.activeObserver
            simulation.createStaticBrowserItems(observer)
            universe.createDynamicBrowserItems(observer)
            val solRoot = simulation.solBrowserRoot()
            if (solRoot != null)
                browserTabs.add(Tab(Tab.Type.SolarSystem, simulation.solBrowserRoot()!!))
            browserTabs.add(Tab(Tab.Type.Star, universe.starBrowserRoot(simulation.activeObserver)))
            browserTabs.add(Tab(Tab.Type.DSO, universe.dsoBrowserRoot()))
        }
        backStacks.clear()
        backStacks.addAll(browserTabs.map { listOf(Page.Item(it.rootItem)).toMutableStateList() })
        tabs.clear()
        tabs.addAll(browserTabs)
    }
}