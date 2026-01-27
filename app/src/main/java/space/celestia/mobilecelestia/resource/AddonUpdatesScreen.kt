package space.celestia.mobilecelestia.resource

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.launch
import space.celestia.celestia.AppCore
import space.celestia.celestiafoundation.resource.model.ResourceItem
import space.celestia.celestiafoundation.resource.model.ResourceManager
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.EmptyHint
import space.celestia.mobilecelestia.compose.SimpleAlertDialog
import space.celestia.mobilecelestia.compose.TextRow
import space.celestia.mobilecelestia.resource.model.AddonUpdateManager
import space.celestia.mobilecelestia.resource.viewmodel.AddonManagerViewModel
import space.celestia.mobilecelestia.utils.CelestiaString
import java.text.DateFormat
import java.util.Locale

sealed class AddonUpdatesAlert {
    data object ErrorCheckingUpdate: AddonUpdatesAlert()
}
@Composable
fun AddonUpdatesScreen(paddingValues: PaddingValues, requestOpenAddon: (ResourceItem) -> Unit) {
    val viewModel: AddonManagerViewModel = hiltViewModel()
    val state = rememberPullToRefreshState()
    val scope = rememberCoroutineScope()

    val formatter by remember { mutableStateOf(DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())) }
    var alert by remember { mutableStateOf<AddonUpdatesAlert?>(null) }

    suspend fun refreshAddonList(checkReason: AddonUpdateManager.CheckReason) {
        val purchaseToken = viewModel.purchaseManager.purchaseToken() ?: return
        val success = viewModel.addonUpdateManager.refresh(
            reason = checkReason,
            purchaseToken = purchaseToken,
            language = AppCore.getLanguage()
        )
        if (!success) {
            alert = AddonUpdatesAlert.ErrorCheckingUpdate
        }
    }

    val lifeCycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifeCycleOwner) {
        val observer = object: ResourceManager.Listener {
            override fun onProgressUpdate(identifier: String, progress: Float) {}
            override fun onFileDownloaded(identifier: String) {}
            override fun onResourceFetchError(identifier: String, errorContext: ResourceManager.ErrorContext) {}

            override fun onFileUnzipped(identifier: String) {
                scope.launch {
                    refreshAddonList(AddonUpdateManager.CheckReason.Change)
                }
            }

            override fun onAddonUninstalled(identifier: String) {
                scope.launch {
                    refreshAddonList(AddonUpdateManager.CheckReason.Change)
                }
            }
        }
        viewModel.resourceManager.addListener(observer)
        onDispose {
            viewModel.resourceManager.removeListener(observer)
        }
    }

    LaunchedEffect(Unit) {
        refreshAddonList(AddonUpdateManager.CheckReason.ViewAppear)
    }

    PullToRefreshBox(
        isRefreshing = viewModel.addonUpdateManager.isCheckingUpdates.value,
        onRefresh = {
            scope.launch {
                refreshAddonList(checkReason = AddonUpdateManager.CheckReason.Refresh)
            }
        },
        state = state,
        indicator = {
            Indicator(
                modifier = Modifier.align(Alignment.TopCenter).padding(paddingValues),
                isRefreshing = viewModel.addonUpdateManager.isCheckingUpdates.value,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                state = state
            )
        }
    ) {
        if (viewModel.addonUpdateManager.pendingUpdates.isEmpty()) {
            Box(modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues), contentAlignment = Alignment.Center) {
                EmptyHint(text = CelestiaString("No Update Available", "Hint that there is no update for installed add-ons."))
            }
        } else {
            val direction = LocalLayoutDirection.current
            val contentPadding = PaddingValues(
                start = paddingValues.calculateStartPadding(direction),
                top = dimensionResource(id = R.dimen.list_spacing_short) + paddingValues.calculateTopPadding(),
                end = paddingValues.calculateEndPadding(direction),
                bottom = dimensionResource(id = R.dimen.list_spacing_tall) + paddingValues.calculateBottomPadding(),
            )
            LazyColumn(
                contentPadding = contentPadding,
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(rememberNestedScrollInteropConnection())
                    .background(color = MaterialTheme.colorScheme.background)
            ) {
                items(viewModel.addonUpdateManager.pendingUpdates) {
                    TextRow(
                        modifier = Modifier.clickable(onClick = {
                            requestOpenAddon(it.addon)
                        }),
                        primaryText = it.addon.name,
                        secondaryText = formatter.format(it.update.modificationDate),
                        accessoryResource = R.drawable.accessory_full_disclosure
                    )
                }
            }
        }
    }

    alert?.let { content ->
        when (content) {
            is AddonUpdatesAlert.ErrorCheckingUpdate -> {
                SimpleAlertDialog(
                    onDismissRequest = {
                        alert = null
                    }, onConfirm = {
                        alert = null
                    },
                    title = CelestiaString("Error checking updates", "Encountered error while checking updates."),
                    text = CelestiaString("Please ensure you have a valid Celestia PLUS subscription or check again later.", "Encountered error while checking updates, possible recovery instruction.")
                )
            }
        }
    }
}