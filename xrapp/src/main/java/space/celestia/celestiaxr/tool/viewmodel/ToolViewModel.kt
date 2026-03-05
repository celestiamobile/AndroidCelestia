package space.celestia.celestiaxr.tool.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import space.celestia.celestia.AppCore
import space.celestia.celestiaui.di.Flavor
import space.celestia.celestiaui.purchase.PurchaseManager
import space.celestia.celestiaui.resource.model.ResourceAPIService
import java.util.concurrent.Executor
import javax.inject.Inject


@HiltViewModel
class ToolViewModel @Inject constructor(
    val purchaseManager: PurchaseManager,
    @param:Flavor val flavor: String,
    val appCore: AppCore,
    val executor: Executor,
    val resourceAPI: ResourceAPIService
) : ViewModel()