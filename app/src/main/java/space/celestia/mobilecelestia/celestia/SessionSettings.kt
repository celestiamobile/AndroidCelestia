package space.celestia.mobilecelestia.celestia

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class SessionSettings {
    var isGyroscopeEnabled: Boolean by mutableStateOf(false)
}