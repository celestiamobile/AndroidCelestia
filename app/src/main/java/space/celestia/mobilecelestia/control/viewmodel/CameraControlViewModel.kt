package space.celestia.mobilecelestia.control.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import space.celestia.celestia.AppCore
import space.celestia.mobilecelestia.common.CelestiaExecutor
import javax.inject.Inject

@HiltViewModel
class CameraControlViewModel @Inject constructor(val appCore: AppCore, val executor: CelestiaExecutor) : ViewModel()