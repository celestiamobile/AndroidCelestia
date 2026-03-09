package space.celestia.mobilecelestia.tool.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import space.celestia.celestiaui.di.Platform
import space.celestia.celestiaui.purchase.PurchaseManager
import javax.inject.Inject

@HiltViewModel
class ToolViewModel @Inject constructor(val platform: Platform, val purchaseManager: PurchaseManager): ViewModel()