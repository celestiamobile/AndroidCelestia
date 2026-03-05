package space.celestia.celestiaxr.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import space.celestia.celestia.AppCore
import space.celestia.celestia.XRRenderer
import space.celestia.celestiaui.utils.AppStatusReporter
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val appStatusReporter: AppStatusReporter,
    val appCore: AppCore,
    val xrRenderer: XRRenderer
) : ViewModel()