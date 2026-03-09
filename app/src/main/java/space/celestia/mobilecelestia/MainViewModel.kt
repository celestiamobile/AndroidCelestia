package space.celestia.mobilecelestia

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import space.celestia.mobilecelestia.tool.viewmodel.ToolPage

class MainViewModel: ViewModel() {
    val backStack = mutableStateListOf<ToolPage>()
}