package space.celestia.mobilecelestia.resource

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import space.celestia.celestia.Selection
import space.celestia.celestiafoundation.resource.model.ResourceItem
import space.celestia.celestiafoundation.resource.model.ResourceManager
import space.celestia.celestiafoundation.utils.URLHelper
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.rememberWebViewState
import space.celestia.mobilecelestia.info.model.CelestiaAction
import space.celestia.mobilecelestia.resource.viewmodel.AddonViewModel
import space.celestia.mobilecelestia.utils.CelestiaString
import java.io.File

enum class AddonState {
    None, Downloading, Installed
}

sealed class AddonAction {
    data class RunScript(val file: File) : AddonAction()
    data class GoTo(val selection: Selection) : AddonAction()
}

fun AddonViewModel.getAddonAction(addon: ResourceItem): AddonAction? {
    if (addon.type == "script") {
        val scriptName = addon.mainScriptName
        if (scriptName != null) {
            val file = File(resourceManager.contextDirectory(addon), scriptName)
            if (file.exists()) {
                return AddonAction.RunScript(file)
            }
        }
    } else {
        val objectName = addon.objectName
        if (objectName != null) {
            val selection = appCore.simulation.findObject(objectName)
            if (!selection.isEmpty) {
                return AddonAction.GoTo(selection)
            }
        }
    }
    return null
}

fun ResourceManager.getAddonState(addon: ResourceItem): AddonState {
    return if (isInstalled(addon)) {
        AddonState.Installed
    } else if (isDownloading(addon.id)) {
        AddonState.Downloading
    } else {
        AddonState.None
    }
}

@Composable
fun AddonScreen(
    addon: ResourceItem,
    language: String,
    shareURLHandler: (String, String) -> Unit,
    receivedACKHandler: (String) -> Unit,
    openSubscriptionPageHandler: () -> Unit,
    openExternalWebLink: (String) -> Unit,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
) {
    val viewModel: AddonViewModel = hiltViewModel()
    var addonState: AddonState by remember { mutableStateOf(AddonState.None) }
    var downloadProgress by remember { mutableFloatStateOf(0.0f) }
    var errorText: String? by remember { mutableStateOf(null) }
    var showUninstallAlert by remember { mutableStateOf(false) }
    var showCancelAlert by remember { mutableStateOf(false) }
    val localContext = LocalContext.current
    val scope = rememberCoroutineScope()

    fun updateAddonState() {
        addonState = viewModel.resourceManager.getAddonState(addon)
        if (addonState != AddonState.Downloading)
            downloadProgress = 0.0f
        if (addonState == AddonState.Installed)
            showCancelAlert = false
    }

    LaunchedEffect(Unit) {
        updateAddonState()
    }

    DisposableEffect(viewModel.resourceManager) {
        val listener = object: ResourceManager.Listener {
            override fun onProgressUpdate(identifier: String, progress: Float) {
                if (identifier == addon.id) {
                    downloadProgress = progress
                    updateAddonState()
                    }
            }

            override fun onFileDownloaded(identifier: String) {
                if (identifier == addon.id)
                    updateAddonState()
            }

            override fun onFileUnzipped(identifier: String) {
                if (identifier == addon.id)
                    updateAddonState()
            }

            override fun onResourceFetchError(
                identifier: String,
                errorContext: ResourceManager.ErrorContext
            ) {
                if (identifier != addon.id) return

                updateAddonState()
                val message = when (errorContext) {
                    ResourceManager.ErrorContext.Cancelled -> null
                    ResourceManager.ErrorContext.ZipError -> CelestiaString("Error unzipping add-on", "")
                    ResourceManager.ErrorContext.Download -> CelestiaString("Error downloading add-on", "")
                    is ResourceManager.ErrorContext.CreateDirectory -> CelestiaString("Error creating directory for add-on", "")
                    is ResourceManager.ErrorContext.OpenFile -> CelestiaString("Error opening file for saving add-on", "")
                    is ResourceManager.ErrorContext.WriteFile -> CelestiaString("Error writing data file for add-on", "")
                }
                errorText = message
            }

        }
        viewModel.resourceManager.addListener(listener)
        onDispose {
            viewModel.resourceManager.removeListener(listener)
        }
    }

    Column(modifier = modifier
        .fillMaxSize()
        .padding(paddingValues),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        if (addonState == AddonState.Downloading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), progress = { downloadProgress })
        }

        val webViewState = rememberWebViewState(url = URLHelper.buildInAppAddonURI(addon.id, language).toString(), filterURL = true, matchingQueryKeys = listOf("item"))

        SingleWebScreen(
            webViewState = webViewState,
            contextDirectory = viewModel.resourceManager.contextDirectory(addon),
            paddingValues = PaddingValues(),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f),
            shareURLHandler = shareURLHandler,
            receivedACKHandler = receivedACKHandler,
            openSubscriptionPageHandler = openSubscriptionPageHandler,
            openExternalWebLink = openExternalWebLink
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = dimensionResource(R.dimen.common_page_medium_margin_horizontal),
                    end = dimensionResource(R.dimen.common_page_medium_margin_horizontal),
                    top = dimensionResource(R.dimen.common_page_medium_gap_vertical),
                    bottom = dimensionResource(R.dimen.common_page_medium_margin_vertical),
                )
        ) {
            if (addonState == AddonState.Installed) {
                val action = viewModel.getAddonAction(addon)
                if (action != null) {
                    FilledTonalButton(onClick = {
                        when (action) {
                            is AddonAction.GoTo -> scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                                viewModel.appCore.simulation.selection = action.selection
                                viewModel.appCore.charEnter(CelestiaAction.GoTo.value)
                            }
                            is AddonAction.RunScript -> scope.launch(viewModel.executor.asCoroutineDispatcher()) { viewModel.appCore.runScript(action.file.absolutePath) }
                        }
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            when (action) {
                                is AddonAction.GoTo -> CelestiaString("Go", "Go to an object")
                                is AddonAction.RunScript -> CelestiaString("Run", "Run a script")
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.common_page_button_gap_vertical)))
                }
            }

            FilledTonalButton(onClick = {
                when (addonState) {
                    AddonState.None -> {
                        // Start downloading
                        viewModel.resourceManager.download(addon, File(localContext.cacheDir, addon.id))
                        addonState = AddonState.Downloading
                    }
                    AddonState.Downloading -> {
                        showCancelAlert = true
                    }
                    AddonState.Installed -> {
                        showUninstallAlert = true
                    }
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text(when (addonState) {
                    AddonState.None -> CelestiaString("Install", "Install an add-on")
                    AddonState.Downloading -> CelestiaString("Cancel", "")
                    AddonState.Installed -> CelestiaString("Uninstall", "Uninstall an add-on")
                })
            }
        }
    }

    if (showUninstallAlert) {
        AlertDialog(onDismissRequest = {
            showUninstallAlert = false
        }, confirmButton = {
            TextButton(onClick = {
                showUninstallAlert = false
                var success = false
                try {
                    success = viewModel.resourceManager.uninstall(addon)
                } catch (_: Throwable) {}
                if (!success) {
                    errorText = CelestiaString("Unable to uninstall add-on.", "")
                }
                updateAddonState()
            }) {
                Text(text = CelestiaString("OK", ""))
            }
        }, dismissButton = {
            TextButton(onClick = {
                showUninstallAlert = false
            }) {
                Text(text = CelestiaString("Cancel", ""))
            }
        }, title = {
            Text(text = CelestiaString("Do you want to uninstall this add-on?", ""))
        })
    }

    if (showCancelAlert) {
        AlertDialog(onDismissRequest = {
            showCancelAlert = false
        }, confirmButton = {
            TextButton(onClick = {
                showCancelAlert = false
                viewModel.resourceManager.cancel(addon.id)
                updateAddonState()
            }) {
                Text(text = CelestiaString("OK", ""))
            }
        }, dismissButton = {
            TextButton(onClick = {
                showCancelAlert = false
            }) {
                Text(text = CelestiaString("Cancel", ""))
            }
        }, title = {
            Text(text = CelestiaString("Do you want to cancel this task?", "Prompt to ask to cancel downloading an add-on"))
        })
    }

    errorText?.let {
        AlertDialog(onDismissRequest = {
            errorText = null
        }, confirmButton = {
            TextButton(onClick = {
                errorText = null
            }) {
                Text(text = CelestiaString("OK", ""))
            }
        }, title = {
            Text(text = it)
        })
    }
}