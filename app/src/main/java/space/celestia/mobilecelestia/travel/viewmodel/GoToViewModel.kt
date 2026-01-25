package space.celestia.mobilecelestia.travel.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import space.celestia.celestia.AppCore
import space.celestia.celestia.GoToLocation
import space.celestia.celestia.Selection
import space.celestia.mobilecelestia.common.CelestiaExecutor
import java.io.Serializable
import javax.inject.Inject

sealed class Page {
    data object GoTo : Page()
}

@HiltViewModel
class GoToViewModel @Inject constructor(val appCore: AppCore, val executor: CelestiaExecutor) : ViewModel() {
    val backStack = mutableStateListOf<Page>(Page.GoTo)

    val selection = mutableStateOf(Selection())
    val objectName = mutableStateOf("")
    val initialLongitude = 0.0f
    val initialLatitude = 0.0f
    val initialDistance = 8.0
    val distanceUnit = mutableStateOf(GoToLocation.DistanceUnit.radii)

    companion object {
        val distanceUnits = listOf(
            GoToLocation.DistanceUnit.radii,
            GoToLocation.DistanceUnit.km,
            GoToLocation.DistanceUnit.au
        )
    }
}