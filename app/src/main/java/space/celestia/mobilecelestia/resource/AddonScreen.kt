// AddonScreen.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.resource

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import space.celestia.celestia.AppCore
import space.celestia.celestiafoundation.resource.model.ResourceItem
import space.celestia.celestiafoundation.resource.model.ResourceManager
import space.celestia.celestiafoundation.utils.URLHelper
import space.celestia.mobilecelestia.BuildConfig
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.SimpleAlertDialog
import space.celestia.mobilecelestia.info.model.CelestiaAction
import space.celestia.mobilecelestia.resource.viewmodel.AddonViewModel
import space.celestia.mobilecelestia.utils.CelestiaString
import java.io.File
import java.io.Serializable


sealed class AddonState: Serializable {
    data object None: AddonState(), Serializable {
        private fun readResolve(): Any = None
    }
    data object Downloading: AddonState(), Serializable {
        private fun readResolve(): Any = Downloading
    }
    data object Installed: AddonState(), Serializable {
        private fun readResolve(): Any = Installed
    }
}

sealed class AddonAlert {
    data object Cancel: AddonAlert()
    data object Uninstall: AddonAlert()
    data object UninstallFailed: AddonAlert()
    data object ObjectNotFound: AddonAlert()
    data class DownloadFailed(val message: String): AddonAlert()
}

@Composable
fun AddonScreen(item: ResourceItem, addonInfoUpdated: (ResourceItem) -> Unit, requestRunScript: (File) -> Unit, paddingValues: PaddingValues) {
    val viewModel: AddonViewModel = hiltViewModel()
    var info by rememberSaveable { mutableStateOf(item) }
    var installedAddonChecksum by rememberSaveable { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    var alert by remember { mutableStateOf<AddonAlert?>(null) }

    fun getAddonState(): AddonState {
        if (viewModel.resourceManager.isInstalled(info))
            return AddonState.Installed
        if (viewModel.resourceManager.isDownloading(info.id))
            return AddonState.Downloading
        return AddonState.None
    }

    LaunchedEffect(Unit) {
        try {
            val item = viewModel.resourceAPI.item(AppCore.getLanguage(), info.id)
            info = item
            addonInfoUpdated(item)
        } catch (ignored: Throwable) {
        }
        val installedItem = viewModel.resourceManager.installedResourceAsync(info)
        installedAddonChecksum = installedItem?.checksum
    }

    var state by rememberSaveable { mutableStateOf(getAddonState()) }
    var downloadProgress by remember { mutableFloatStateOf(0.0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = downloadProgress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
    )

    fun updateAddonState() {
        val newState = getAddonState()
        if (newState !is AddonState.Downloading)
            downloadProgress = 0.0f
        if (newState !is AddonState.Installed)
            installedAddonChecksum = null
        state = newState
    }

    val scope = rememberCoroutineScope()

    val lifeCycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifeCycleOwner) {
        val observer = object : ResourceManager.Listener {
            override fun onProgressUpdate(identifier: String, progress: Float) {
                if (identifier == info.id) {
                    downloadProgress = progress
                    updateAddonState()
                }
            }

            override fun onFileDownloaded(identifier: String) {
                if (identifier == info.id)
                    updateAddonState()
            }

            override fun onFileUnzipped(identifier: String) {
                if (identifier == info.id)
                    updateAddonState()
            }

            override fun onResourceFetchError(
                identifier: String,
                errorContext: ResourceManager.ErrorContext
            ) {
                if (identifier == info.id) {
                    val message = when (errorContext) {
                        ResourceManager.ErrorContext.ZipError -> CelestiaString(
                            "Error unzipping add-on",
                            ""
                        )

                        ResourceManager.ErrorContext.Download -> CelestiaString(
                            "Error downloading add-on",
                            ""
                        )

                        is ResourceManager.ErrorContext.CreateDirectory -> CelestiaString(
                            "Error creating directory for add-on",
                            ""
                        )

                        is ResourceManager.ErrorContext.OpenFile -> CelestiaString(
                            "Error opening file for saving add-on",
                            ""
                        )

                        is ResourceManager.ErrorContext.WriteFile -> CelestiaString(
                            "Error writing data file for add-on",
                            ""
                        )

                        else -> null
                    } ?: return
                    alert = AddonAlert.DownloadFailed(message)
                    updateAddonState()
                }
            }

            override fun onAddonUninstalled(identifier: String) {
                if (identifier == info.id)
                    updateAddonState()
            }
        }
        viewModel.resourceManager.addListener(observer)
        onDispose {
            viewModel.resourceManager.removeListener(observer)
        }
    }

    Column(
        modifier = Modifier.padding(paddingValues)
    ) {
        if (state is AddonState.Downloading) {
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth()
            )
        }
        WebPage(
            uri = URLHelper.buildInAppAddonURI(
                info.id,
                AppCore.getLanguage(),
                flavor = BuildConfig.FLAVOR
            ),
            contextDirectory = viewModel.resourceManager.contextDirectory(info),
            filterURL = true,
            matchingQueryKeys = listOf("item"),
            modifier = Modifier.weight(1.0f),
            paddingValues = PaddingValues.Zero
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.common_page_button_gap_vertical)),
            modifier = Modifier.fillMaxWidth().padding(
                start = dimensionResource(R.dimen.common_page_medium_margin_horizontal),
                end = dimensionResource(R.dimen.common_page_medium_margin_horizontal),
                top = dimensionResource(R.dimen.common_page_medium_gap_vertical),
                bottom = dimensionResource(R.dimen.common_page_medium_margin_vertical)
            )
        ) {
            if (state is AddonState.Installed) {
                val scriptName = info.mainScriptName
                val objectName = info.objectName
                val addonType = info.type
                if (objectName != null && addonType != "script") {
                    val selection = viewModel.appCore.simulation.findObject(objectName)
                    if (!selection.isEmpty) {
                        FilledTonalButton(modifier = Modifier.fillMaxWidth(), onClick = {
                            scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                                viewModel.appCore.simulation.selection = selection
                                viewModel.appCore.charEnter(CelestiaAction.GoTo.value)
                            }
                        }) {
                            Text(CelestiaString("Go", "Go to an object"))
                        }
                    }
                }

                if (scriptName != null && info.type == "script") {
                    val scriptFile =
                        File(viewModel.resourceManager.contextDirectory(info), scriptName)
                    if (scriptFile.exists()) {
                        FilledTonalButton(modifier = Modifier.fillMaxWidth(), onClick = {
                            requestRunScript(scriptFile)
                        }) {
                            Text(CelestiaString("Run", "Run a script"))
                        }
                    }
                }

                if (installedAddonChecksum != null && info.checksum != installedAddonChecksum) {
                    FilledTonalButton(modifier = Modifier.fillMaxWidth(), onClick = {
                        viewModel.resourceManager.reinstall(info, File(context.cacheDir, info.id))
                        updateAddonState()
                    }) {
                        Text(CelestiaString("Update", ""))
                    }
                }
            }
            FilledTonalButton(modifier = Modifier.fillMaxWidth(), onClick = {
                when (state) {
                    is AddonState.None -> {
                        viewModel.resourceManager.download(info, File(context.cacheDir, info.id))
                        updateAddonState()
                    }

                    is AddonState.Downloading -> {
                        alert = AddonAlert.Cancel
                    }

                    is AddonState.Installed -> {
                        alert = AddonAlert.Uninstall
                    }
                }
            }) {
                when (state) {
                    is AddonState.None -> {
                        Text(CelestiaString("Install", "Install an add-on"))
                    }

                    is AddonState.Downloading -> {
                        Text(CelestiaString("Cancel", ""))
                    }

                    is AddonState.Installed -> {
                        Text(CelestiaString("Uninstall", "Uninstall an add-on"))
                    }
                }
            }
        }
    }

    alert?.let { content ->
        when (content) {
            is AddonAlert.DownloadFailed -> {
                SimpleAlertDialog(
                    onDismissRequest = { alert = null },
                    onConfirm = { alert = null },
                    title = CelestiaString("Failed to download or install this add-on.", ""),
                    text = content.message
                )
            }

            is AddonAlert.ObjectNotFound -> {
                SimpleAlertDialog(onDismissRequest = {
                    alert = null
                }, onConfirm = {
                    alert = null
                }, title = CelestiaString("Object not found", ""))
            }

            is AddonAlert.Cancel -> {
                SimpleAlertDialog(
                    onDismissRequest = {
                        alert = null
                    },
                    onConfirm = {
                        alert = null
                        viewModel.resourceManager.cancel(info.id)
                        updateAddonState()
                    },
                    title = CelestiaString("Do you want to cancel this task?",  "Prompt to ask to cancel downloading an add-on"),
                    showCancel = true
                )
            }

            is AddonAlert.Uninstall -> {
                SimpleAlertDialog(
                    onDismissRequest = {
                        alert = null
                    },
                    onConfirm = {
                        alert = null
                        var success = false
                        try {
                            success = viewModel.resourceManager.uninstall(item)
                        } catch (_: Exception) {
                        }
                        if (success) {
                            updateAddonState()
                        } else {
                            alert = AddonAlert.UninstallFailed
                        }
                    },
                    title = CelestiaString("Do you want to uninstall this add-on?", ""),
                    showCancel = true
                )
            }

            is AddonAlert.UninstallFailed -> {
                SimpleAlertDialog(onDismissRequest = {
                    alert = null
                }, onConfirm = {
                    alert = null
                }, title = CelestiaString("Object not found", ""))
            }
        }
    }
}