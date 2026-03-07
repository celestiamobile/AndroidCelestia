package space.celestia.celestiaxr.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import space.celestia.celestiaui.utils.CelestiaString
import space.celestia.celestiaxr.loading.LoadingScreen
import space.celestia.celestiaxr.viewmodel.MainViewModel

@Composable
fun HomeScreen(paddingValues: PaddingValues) {
    val viewModel: MainViewModel = hiltViewModel()
    var state by remember { mutableStateOf(viewModel.appStatusReporter.state) }
    var alertMessage by remember { mutableStateOf<String?>(null) }
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
        val alertJob = scope.launch {
            viewModel.alertMessages.collect { message ->
                alertMessage = message
            }
        }
        onDispose {
            viewModel.appStatusReporter.unregister(observer)
            alertJob.cancel()
        }
    }

    alertMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { alertMessage = null },
            confirmButton = {
                TextButton(onClick = { alertMessage = null }) {
                    Text(CelestiaString("OK", ""))
                }
            },
            text = { Text(message) }
        )
    }

    when (state) {
        AppStatusReporter.State.FINISHED -> {
            RunningScreen()
        }
        else -> {
            LoadingScreen(paddingValues)
        }
    }
}
