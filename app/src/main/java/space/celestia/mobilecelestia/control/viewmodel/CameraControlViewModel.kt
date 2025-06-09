package space.celestia.mobilecelestia.control.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import space.celestia.mobilecelestia.celestia.SessionSettings
import javax.inject.Inject

@HiltViewModel
class CameraControlViewModel @Inject constructor(val sessionSettings: SessionSettings) : ViewModel()