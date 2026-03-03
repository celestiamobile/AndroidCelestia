package space.celestia.celestiaui.control.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import space.celestia.celestia.AppCore
import space.celestia.celestia.Observer
import space.celestia.celestiaui.utils.CelestiaString
import java.util.concurrent.Executor
import javax.inject.Inject

sealed class Page {
    data object CameraControl : Page()
    data object ObserverMode : Page()
}

@HiltViewModel
class CameraControlViewModel @Inject constructor(val sessionSettings: SessionSettings, val appCore: AppCore, val executor: Executor) : ViewModel() {
    val backStack = mutableStateListOf<Page>(Page.CameraControl)

    companion object {
        val coordinateSystems = listOf(
            Pair(Observer.COORDINATE_SYSTEM_UNIVERSAL, CelestiaString("Free Flight", "Flight mode, coordinate system")),
            Pair(Observer.COORDINATE_SYSTEM_ECLIPTICAL, CelestiaString("Follow", "")),
            Pair(Observer.COORDINATE_SYSTEM_BODY_FIXED, CelestiaString("Sync Orbit", "")),
            Pair(Observer.COORDINATE_SYSTEM_PHASE_LOCK, CelestiaString("Phase Lock", "Flight mode, coordinate system")),
            Pair(Observer.COORDINATE_SYSTEM_CHASE, CelestiaString("Chase", "")),
        )
    }
}