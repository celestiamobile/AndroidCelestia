package space.celestia.celestiaui.eventfinder.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import space.celestia.celestia.AppCore
import space.celestia.celestia.EclipseFinder
import java.util.concurrent.Executor
import javax.inject.Inject

sealed class Page {
    data object Home : Page()
    class Results(val results: List<EclipseFinder.Eclipse>) : Page()
}

@HiltViewModel
class EventFinderViewModel @Inject constructor(val appCore: AppCore, val executor: Executor) : ViewModel() {
    val backStack = mutableStateListOf<Page>(Page.Home)
}