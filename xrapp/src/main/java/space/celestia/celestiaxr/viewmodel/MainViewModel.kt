package space.celestia.celestiaxr.viewmodel

import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import space.celestia.celestia.AppCore
import space.celestia.celestia.XRRenderer
import space.celestia.celestiaui.utils.AppStatusReporter
import space.celestia.celestiaxr.di.AlertMessage
import java.util.concurrent.Executor
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val appStatusReporter: AppStatusReporter,
    val appCore: AppCore,
    val xrRenderer: XRRenderer,
    val executor: Executor,
    @param: AlertMessage val alertMessage: MutableState<String?>
) : ViewModel()
