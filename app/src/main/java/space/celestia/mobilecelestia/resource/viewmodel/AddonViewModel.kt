package space.celestia.mobilecelestia.resource.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import space.celestia.celestia.AppCore
import space.celestia.celestiafoundation.resource.model.ResourceItem
import space.celestia.celestiafoundation.resource.model.ResourceManager
import space.celestia.mobilecelestia.common.CelestiaExecutor
import space.celestia.mobilecelestia.resource.model.ResourceAPIService
import javax.inject.Inject

@HiltViewModel
class AddonViewModel @Inject constructor(val appCore: AppCore, val executor: CelestiaExecutor, val resourceManager: ResourceManager, val resourceAPI: ResourceAPIService) : ViewModel() {
    val addonMap = hashMapOf<String, ResourceItem>()
}