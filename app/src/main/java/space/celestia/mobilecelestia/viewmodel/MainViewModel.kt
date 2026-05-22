package space.celestia.mobilecelestia.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import space.celestia.celestiaui.tool.viewmodel.ToolPage

class MainViewModel: ViewModel() {
    val backStack = mutableStateListOf<ToolPage>()
    var bottomSheetVisible = mutableStateOf(false)
}