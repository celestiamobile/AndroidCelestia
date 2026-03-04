package space.celestia.celestiaxr.tool.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import space.celestia.celestia.AppCore
import space.celestia.celestiaui.di.Platform
import space.celestia.celestiaui.purchase.PurchaseManager
import space.celestia.celestiaui.resource.model.ResourceAPIService
import space.celestia.celestiaui.tool.viewmodel.ToolPage
import space.celestia.celestiaxr.tool.Tool
import space.celestia.celestiaxr.tool.ToolActivity
import java.util.concurrent.Executor
import javax.inject.Inject


@HiltViewModel
class ToolViewModel @Inject constructor(
    val purchaseManager: PurchaseManager,
    val platform: Platform,
    val appCore: AppCore,
    val executor: Executor,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val backStack = mutableStateListOf(requireNotNull(savedStateHandle[ToolActivity.EXTRA_TOOL] as? ToolPage))
}