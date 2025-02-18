package space.celestia.mobilecelestia.browser.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import space.celestia.celestia.AppCore
import space.celestia.celestia.BrowserItem
import space.celestia.mobilecelestia.common.CelestiaExecutor
import javax.inject.Inject

@HiltViewModel
class BrowserNavigationViewModel @Inject constructor(val appCore: AppCore, val executor: CelestiaExecutor) : ViewModel() {
    lateinit var rootPath: String
    lateinit var currentPath: String
    val browserMap = hashMapOf<String, BrowserItem>()
}