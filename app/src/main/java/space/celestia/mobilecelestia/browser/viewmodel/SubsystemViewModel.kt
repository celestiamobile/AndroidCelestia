package space.celestia.mobilecelestia.browser.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import space.celestia.celestia.AppCore
import space.celestia.celestia.BrowserItem
import space.celestia.celestia.Selection
import space.celestia.mobilecelestia.common.CelestiaExecutor
import javax.inject.Inject

sealed class SubsystemPage {
    data class Item(val item: BrowserItem) : SubsystemPage()
    data class Info(val info: Selection) : SubsystemPage()
}

@HiltViewModel
class SubsystemViewModel @Inject constructor(val appCore: AppCore, val executor: CelestiaExecutor) : ViewModel() {
    val backStack = mutableStateListOf<SubsystemPage>()
}