package space.celestia.mobilecelestia.resource.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import space.celestia.celestia.AppCore
import space.celestia.celestiafoundation.resource.model.ResourceItem
import space.celestia.celestiafoundation.resource.model.ResourceManager
import space.celestia.mobilecelestia.common.CelestiaExecutor
import space.celestia.mobilecelestia.purchase.PurchaseManager
import space.celestia.mobilecelestia.resource.model.AddonUpdateManager
import space.celestia.mobilecelestia.resource.model.ResourceAPIService
import javax.inject.Inject

sealed class AddonManagerPage {
    data object Home: AddonManagerPage()
    data object Updates: AddonManagerPage()
    data class Addon(val addon: ResourceItem): AddonManagerPage() {
        var title = mutableStateOf(addon.name)
    }
}

@HiltViewModel
class AddonManagerViewModel @Inject constructor(val appCore: AppCore, val executor: CelestiaExecutor, val resourceManager: ResourceManager, val addonUpdateManager: AddonUpdateManager, val resourceAPI: ResourceAPIService, val purchaseManager: PurchaseManager): ViewModel() {
    val backStack = mutableStateListOf<AddonManagerPage>(AddonManagerPage.Home)
}