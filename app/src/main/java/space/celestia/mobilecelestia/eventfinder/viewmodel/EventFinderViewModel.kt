package space.celestia.mobilecelestia.eventfinder.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import space.celestia.celestia.AppCore
import space.celestia.celestia.EclipseFinder
import space.celestia.celestia.EclipseFinder.Eclipse
import space.celestia.mobilecelestia.common.CelestiaExecutor
import javax.inject.Inject

@HiltViewModel
class EventFinderViewModel @Inject constructor(val appCore: AppCore, val executor: CelestiaExecutor) : ViewModel() {
    var currentEclipseFinder: EclipseFinder? = null
    var eclipseResults: List<Eclipse> = listOf()

    fun reset() {
        currentEclipseFinder = null
        eclipseResults = listOf()
    }
}