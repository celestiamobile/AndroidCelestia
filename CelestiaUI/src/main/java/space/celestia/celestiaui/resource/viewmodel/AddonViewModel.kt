package space.celestia.celestiaui.resource.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import space.celestia.celestia.AppCore
import space.celestia.celestiafoundation.resource.model.ResourceManager
import space.celestia.celestiaui.di.Flavor
import space.celestia.celestiaui.resource.model.ResourceAPIService
import java.util.concurrent.Executor
import javax.inject.Inject

@HiltViewModel
class AddonViewModel @Inject constructor(val appCore: AppCore, val executor: Executor, val resourceManager: ResourceManager, val resourceAPI: ResourceAPIService, @param:Flavor val flavor: String): ViewModel()