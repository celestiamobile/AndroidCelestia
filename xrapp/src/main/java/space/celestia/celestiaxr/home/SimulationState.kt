package space.celestia.celestiaxr.home

import space.celestia.celestia.AppState

data class SimulationState(
    val appState: AppState? = null,
    val selectedObjectName: String = "",
    val referenceObjectName: String = "",
    val targetObjectName: String = "",
    val messageText: String = ""
)
