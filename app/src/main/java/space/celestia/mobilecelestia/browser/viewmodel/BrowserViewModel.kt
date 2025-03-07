package space.celestia.mobilecelestia.browser.viewmodel

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import space.celestia.celestia.AppCore
import space.celestia.celestia.BrowserItem
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.browser.Browser
import space.celestia.mobilecelestia.browser.createDynamicBrowserItems
import space.celestia.mobilecelestia.browser.createStaticBrowserItems
import space.celestia.mobilecelestia.browser.dsoBrowserRoot
import space.celestia.mobilecelestia.browser.solBrowserRoot
import space.celestia.mobilecelestia.browser.starBrowserRoot
import space.celestia.mobilecelestia.common.CelestiaExecutor
import javax.inject.Inject

@Serializable
sealed class BrowserTab {
    @get:DrawableRes
    abstract val iconResource: Int

    @Serializable
    data object SolarSystem: BrowserTab() {
        override val iconResource: Int
            get() = R.drawable.browser_tab_sso
    }

    @Serializable
    data object DSO: BrowserTab() {
        override val iconResource: Int
            get() = R.drawable.browser_tab_dso

    }

    @Serializable
    data object Stars: BrowserTab() {
        override val iconResource: Int
            get() = R.drawable.browser_tab_star
    }
}

data class BrowserTabData(val tab: BrowserTab, val item: BrowserItem, val root: Browser.Root)

@HiltViewModel
class BrowserViewModel @Inject constructor(val appCore: AppCore, val executor: CelestiaExecutor) : ViewModel() {
    lateinit var tabData: List<BrowserTabData>

    suspend fun loadBrowserTabs() {
        val browserTabs = arrayListOf<BrowserTabData>()
        withContext(executor.asCoroutineDispatcher()) {
            val simulation = appCore.simulation
            val universe = simulation.universe
            val observer = simulation.activeObserver
            simulation.createStaticBrowserItems(observer)
            universe.createDynamicBrowserItems(observer)
            val solRoot = simulation.solBrowserRoot()
            if (solRoot != null)
                browserTabs.add(BrowserTabData(BrowserTab.SolarSystem, solRoot, Browser.Root.SolarSystem("${solRoot.hashCode()}")))
            val starRoot = simulation.universe.starBrowserRoot(observer)
            browserTabs.add(BrowserTabData(BrowserTab.Stars, starRoot, Browser.Root.Stars("${starRoot.hashCode()}")))
            val dsoRoot = simulation.universe.dsoBrowserRoot()
            browserTabs.add(BrowserTabData(BrowserTab.DSO, dsoRoot, Browser.Root.DSOs("${dsoRoot.hashCode()}")))
        }
        tabData = browserTabs
    }

    fun reset() {
        tabData = listOf()
    }
}

@HiltViewModel
class SubsystemBrowserViewModel @Inject constructor(val appCore: AppCore) : ViewModel()