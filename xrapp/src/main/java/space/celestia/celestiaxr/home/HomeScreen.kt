package space.celestia.celestiaxr.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.launch
import space.celestia.celestiaui.utils.AppStatusReporter
import space.celestia.celestiaxr.loading.LoadingScreen
import space.celestia.celestiaxr.viewmodel.MainViewModel

@Composable
fun HomeScreen(paddingValues: PaddingValues) {
    val viewModel: MainViewModel = hiltViewModel()
    var state by remember { mutableStateOf(viewModel.appStatusReporter.state) }
    val lifeCycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    DisposableEffect(lifeCycleOwner) {
        val observer = object: AppStatusReporter.Listener {
            override fun celestiaLoadingProgress(status: String) {}

            override fun celestiaLoadingStateChanged(newState: AppStatusReporter.State) {
                scope.launch {
                    state = newState
                }
            }
        }
        viewModel.appStatusReporter.register(observer)
        onDispose {
            viewModel.appStatusReporter.unregister(observer)
        }
    }

    when (state) {
        AppStatusReporter.State.FINISHED -> {
            RunningScreen(viewModel.appCore, viewModel.xrRenderer)
        }
        else -> {
            LoadingScreen(paddingValues)
        }
    }
}