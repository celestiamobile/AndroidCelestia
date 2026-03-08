package space.celestia.celestiaui.control.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class SessionSettings(val isGyroscopeSupported: Boolean) {
    var isGyroscopeEnabled: Boolean by mutableStateOf(false)
}