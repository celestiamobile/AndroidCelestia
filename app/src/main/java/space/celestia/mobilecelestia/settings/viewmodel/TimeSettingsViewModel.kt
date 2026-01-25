package space.celestia.mobilecelestia.settings.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

sealed class TimeSettingsPage {
    data object Home: TimeSettingsPage()
}

class TimeSettingsViewModel: ViewModel() {
    val backStack = mutableStateListOf<TimeSettingsPage>(TimeSettingsPage.Home)
}