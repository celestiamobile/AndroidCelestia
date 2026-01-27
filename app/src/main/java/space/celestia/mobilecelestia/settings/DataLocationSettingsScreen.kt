// DataLocationSettingsScreen.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.settings

import android.content.Intent
import android.provider.DocumentsContract
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.Footer
import space.celestia.mobilecelestia.compose.OptionInputDialog
import space.celestia.mobilecelestia.compose.Separator
import space.celestia.mobilecelestia.compose.TextRow
import space.celestia.mobilecelestia.settings.viewmodel.SettingsViewModel
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.PreferenceManager
import space.celestia.mobilecelestia.utils.RealPathUtils
import java.io.File
import kotlin.system.exitProcess

private sealed class AlertContent {
    data class WrongPathProvided(val expectedParent: String) : AlertContent()
    data object UnsupportedAction : AlertContent()
    data class MigrateSelection(val dataDir: File, val mediaDir: File) : AlertContent()
    data class MigrateConfirmation(val dataDir: File, val mediaDir: File, val useMediaDirForAddons: Boolean) : AlertContent()
}

@Composable
fun DataLocationSettingsScreen(paddingValues: PaddingValues) {
    val viewModel: SettingsViewModel = hiltViewModel()
    var customConfigFilePath by remember { mutableStateOf(viewModel.appSettings[PreferenceManager.PredefinedKey.ConfigFilePath]) }
    var customDataDirPath by remember { mutableStateOf(viewModel.appSettings[PreferenceManager.PredefinedKey.DataDirPath]) }
    val internalViewModifier = Modifier
        .fillMaxWidth()
        .padding(
            horizontal = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal),
            vertical = dimensionResource(id = R.dimen.common_page_medium_gap_vertical),
        )
    val localActivity = LocalActivity.current
    var alert by remember { mutableStateOf<AlertContent?>(null) }
    val fileChooserLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val activity = localActivity ?: return@rememberLauncherForActivityResult
        val uri = it.data?.data ?: return@rememberLauncherForActivityResult
        val path = RealPathUtils.getRealPath(activity, uri)
        if (path == null) {
            alert = AlertContent.WrongPathProvided((activity.externalMediaDirs?.firstOrNull() ?: activity.getExternalFilesDir(null))?.absolutePath ?: "")
        } else {
            viewModel.appSettings[PreferenceManager.PredefinedKey.ConfigFilePath] = path
            customConfigFilePath = path
        }
    }
    val directoryChooserLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val activity = localActivity ?: return@rememberLauncherForActivityResult
        val uri = it.data?.data ?: return@rememberLauncherForActivityResult
        val path = RealPathUtils.getRealPath(activity, DocumentsContract.buildDocumentUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri)))
        if (path == null) {
            alert = AlertContent.WrongPathProvided((activity.externalMediaDirs?.firstOrNull() ?: activity.getExternalFilesDir(null))?.absolutePath ?: "")
        } else {
            viewModel.appSettings[PreferenceManager.PredefinedKey.DataDirPath] = path
            customDataDirPath = path
        }
    }
    val nestedScrollInterop = rememberNestedScrollInteropConnection()
    LazyColumn(modifier = Modifier.nestedScroll(nestedScrollInterop), contentPadding = paddingValues) {
        item {
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
        }
        item {
            TextRow(primaryText = CelestiaString("Config File", "celestia.cfg"), secondaryText = if (customConfigFilePath == null) CelestiaString("Default", "") else CelestiaString("Custom", ""), modifier = Modifier.clickable {
                val packageManager = localActivity?.packageManager ?: return@clickable
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.type = "*/*"
                intent.putExtra("android.content.extra.SHOW_ADVANCED", true)
                if (intent.resolveActivity(packageManager) != null)
                    fileChooserLauncher.launch(intent)
                else
                    alert = AlertContent.UnsupportedAction
            })
            TextRow(primaryText = CelestiaString("Data Directory", "Directory to load data from"), secondaryText = if (customDataDirPath == null) CelestiaString("Default", "") else CelestiaString("Custom", ""), modifier = Modifier.clickable {
                val packageManager = localActivity?.packageManager ?: return@clickable
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                intent.putExtra("android.content.extra.SHOW_ADVANCED", true)
                if (intent.resolveActivity(packageManager) != null)
                    directoryChooserLauncher.launch(intent)
                else
                    alert = AlertContent.UnsupportedAction
            })
        }

        item {
            TextRow(primaryText = CelestiaString("Migrate Add-on Data", "Action to migrate data where the add-on is downloaded"), modifier = Modifier.clickable {
                val activity = localActivity ?: return@clickable
                val dataDir = activity.getExternalFilesDir(null)
                val mediaDir = activity.externalMediaDirs.firstOrNull()
                if (dataDir == null || mediaDir == null) {
                    alert = AlertContent.UnsupportedAction
                    return@clickable
                }
                alert = AlertContent.MigrateSelection(dataDir = dataDir, mediaDir = mediaDir)
            })
        }

        item {
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
            Separator()
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
        }
        item {
            FilledTonalButton(modifier = internalViewModifier, onClick = {
                viewModel.appSettings[PreferenceManager.PredefinedKey.ConfigFilePath] = null
                viewModel.appSettings[PreferenceManager.PredefinedKey.DataDirPath] = null
                customConfigFilePath = null
                customDataDirPath = null
            }) {
                Text(text = CelestiaString("Reset to Default", "Reset celestia.cfg, data directory location"))
            }
            Footer(text = CelestiaString("Configuration will take effect after a restart.", "Change requires a restart"))
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_tall)))
        }
    }

    alert?.let { content ->
        when (content) {
            is AlertContent.WrongPathProvided -> {
                AlertDialog(onDismissRequest = {
                    alert = null
                }, confirmButton = {
                    TextButton(onClick = {
                        alert = null
                    }) {
                        Text(text = CelestiaString("OK", ""))
                    }
                }, title = {
                    Text(CelestiaString("Unable to resolve path", "Custom config/data directory path have to be under a specific path"))
                }, text = {
                    Text(CelestiaString("Please ensure that you have selected a path under %s.", "Custom config/data directory path have to be under a specific path").format(localActivity?.externalMediaDirs?.firstOrNull() ?: localActivity?.getExternalFilesDir(null)?.absolutePath ?: ""))
                })
            }
            is AlertContent.UnsupportedAction -> {
                AlertDialog(onDismissRequest = {
                    alert = null
                }, confirmButton = {
                    TextButton(onClick = {
                        alert = null
                    }) {
                        Text(text = CelestiaString("OK", ""))
                    }
                }, title = {
                    Text(CelestiaString("Unsupported action.", ""))
                })
            }
            is AlertContent.MigrateSelection -> {
                OptionInputDialog(
                    onDismissRequest = {
                        alert = null
                    },
                    title = CelestiaString("Migrate Add-on Data", "Action to migrate data where the add-on is downloaded"),
                    items = listOf(
                        String.format(CelestiaString("Migrate to %s", "Option to migrate add-on data to a directory. %s is the target directory"), "Android/data"),
                        String.format(CelestiaString("Migrate to %s", "Option to migrate add-on data to a directory. %s is the target directory"), "Android/media")
                    )
                ) { index ->
                    alert = AlertContent.MigrateConfirmation(content.dataDir, content.mediaDir, index == 1)
                }
            }
            is AlertContent.MigrateConfirmation -> {
                AlertDialog(onDismissRequest = {
                    alert = null
                }, confirmButton = {
                    TextButton(onClick = {
                        alert = null
                        viewModel.appSettings.startEditing()
                        if (!content.useMediaDirForAddons) {
                            viewModel.appSettings[PreferenceManager.PredefinedKey.MigrationSourceDirectory] = content.mediaDir.absolutePath
                            viewModel.appSettings[PreferenceManager.PredefinedKey.MigrationTargetDirectory] = content.dataDir.absolutePath
                            viewModel.appSettings[PreferenceManager.PredefinedKey.UseMediaDirForAddons] = "false"
                        } else {
                            viewModel.appSettings[PreferenceManager.PredefinedKey.MigrationSourceDirectory] = content.dataDir.absolutePath
                            viewModel.appSettings[PreferenceManager.PredefinedKey.MigrationTargetDirectory] = content.mediaDir.absolutePath
                            viewModel.appSettings[PreferenceManager.PredefinedKey.UseMediaDirForAddons] = "true"
                        }
                        viewModel.appSettings.stopEditing(writeImmediatelly = true)
                        localActivity?.finishAndRemoveTask()
                        exitProcess(0)
                    }) {
                        Text(text = CelestiaString("OK", ""))
                    }
                }, dismissButton = {
                    TextButton(onClick = {
                        alert = null
                    }) {
                        Text(text = CelestiaString("Cancel", ""))
                    }
                }, title = {
                    Text(CelestiaString("Migrate Add-on Data", "Action to migrate data where the add-on is downloaded"))
                }, text = {
                    Text(CelestiaString("Celestia will exit and perform migration the next time it is opened. Target directory will be replaced with content in the current directory. The current directory will be cleared after the content is copied. Future add-ons will be downloaded to the target directory. This operation cannot be undone, so please ensure you make a backup before you proceed.", ""))
                })
            }
        }
    }
}