package space.celestia.celestiaxr.loading

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.launch
import space.celestia.celestiaui.utils.AppStatusReporter
import space.celestia.celestiaxr.viewmodel.MainViewModel

@Composable
fun LoadingScreen(paddingValues: PaddingValues) {
    val viewModel: MainViewModel = hiltViewModel()
    val lifeCycleOwner = LocalLifecycleOwner.current

    var statusText by rememberSaveable { mutableStateOf(viewModel.appStatusReporter.status) }
    val scope = rememberCoroutineScope()

    DisposableEffect(lifeCycleOwner) {
        val observer = object: AppStatusReporter.Listener {
            override fun celestiaLoadingProgress(status: String) {
                scope.launch {
                    statusText = status
                }
            }

            override fun celestiaLoadingStateChanged(newState: AppStatusReporter.State) {}
        }
        viewModel.appStatusReporter.register(observer)
        onDispose {
            viewModel.appStatusReporter.unregister(observer)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(color = MaterialTheme.colorScheme.background).padding(paddingValues).padding(horizontal = dimensionResource(space.celestia.celestiaui.R.dimen.common_page_medium_margin_horizontal)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Image(painter = painterResource(space.celestia.celestiaui.R.drawable.loading_icon), contentDescription = null, modifier = Modifier.size(dimensionResource(space.celestia.celestiaui.R.dimen.app_icon_dimension)))
            Text(text = statusText, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyLarge)
        }
    }
}