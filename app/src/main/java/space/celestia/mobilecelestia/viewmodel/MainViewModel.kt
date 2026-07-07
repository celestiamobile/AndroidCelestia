package space.celestia.mobilecelestia.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import space.celestia.celestiaui.tool.viewmodel.ToolPage
import space.celestia.mobilecelestia.control.BottomControlAction
import space.celestia.mobilecelestia.control.OverflowItem

class MainViewModel: ViewModel() {
    val backStack = mutableStateListOf<ToolPage>()
    var bottomSheetVisible = mutableStateOf(false)
    var loadingVisible = mutableStateOf(true)
    var toolbarVisible = mutableStateOf(false)
    var toolbarActions = mutableStateOf<List<BottomControlAction>>(listOf())
    var toolbarOverflowActions = mutableStateOf<List<OverflowItem>>(listOf())
}