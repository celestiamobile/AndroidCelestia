package space.celestia.celestiaui.resource.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import space.celestia.celestia.AppCore
import space.celestia.celestiafoundation.resource.model.ResourceItem
import space.celestia.celestiafoundation.resource.model.ResourceManager
import space.celestia.celestiaui.di.Platform
import space.celestia.celestiaui.purchase.PurchaseManager
import space.celestia.celestiaui.resource.model.AddonUpdateManager
import space.celestia.celestiaui.resource.model.ResourceAPIService
import java.util.concurrent.Executor
import javax.inject.Inject

sealed class AddonManagerPage {
    data object Home: AddonManagerPage()
    data object Updates: AddonManagerPage()
    data class Addon(val addon: ResourceItem): AddonManagerPage() {
        var title = mutableStateOf(addon.name)
    }
}

@HiltViewModel
class AddonManagerViewModel @Inject constructor(val appCore: AppCore, val executor: Executor, val resourceManager: ResourceManager, val addonUpdateManager: AddonUpdateManager, val resourceAPI: ResourceAPIService, val purchaseManager: PurchaseManager, val platform: Platform): ViewModel() {
    val backStack = mutableStateListOf<AddonManagerPage>(AddonManagerPage.Home)
}