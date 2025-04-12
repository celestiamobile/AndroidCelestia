/*
 * DataLocationSettingsScreen.kt
 *
 * Copyright (C) 2025-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.settings

import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.hilt.navigation.compose.hiltViewModel
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.Footer
import space.celestia.mobilecelestia.compose.OptionInputDialog
import space.celestia.mobilecelestia.compose.Separator
import space.celestia.mobilecelestia.compose.TextRow
import space.celestia.mobilecelestia.settings.viewmodel.SettingsViewModel
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.PreferenceManager
import space.celestia.mobilecelestia.utils.RealPathUtils
import kotlin.system.exitProcess

@Composable
fun DataLocationSettingsScreen(paddingValues: PaddingValues, modifier: Modifier = Modifier) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val activity = LocalActivity.current

    var customConfigFilePath by remember { mutableStateOf(viewModel.appSettings[PreferenceManager.PredefinedKey.ConfigFilePath]) }
    var customDataDirPath by remember { mutableStateOf(viewModel.appSettings[PreferenceManager.PredefinedKey.DataDirPath]) }

    var errorTitle: String? by remember {
        mutableStateOf(null)
    }
    var errorMessage: String? by remember {
        mutableStateOf(null)
    }

    val context = LocalContext.current
    var showMigrationDialog by remember { mutableStateOf(false)  }
    var migrationOption: Int? by remember { mutableStateOf(null)  }
    val fileChooserLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult(), onResult = {
        val uri = it.data?.data ?: return@rememberLauncherForActivityResult
        val path = RealPathUtils.getRealPath(context, uri)
        if (path != null) {
            viewModel.appSettings[PreferenceManager.PredefinedKey.ConfigFilePath] = path
            customConfigFilePath = path
        } else {
            val expectedParent = context.externalMediaDirs.firstOrNull() ?: context.getExternalFilesDir(null)
            errorTitle = CelestiaString("Unable to resolve path", "Custom config/data directory path have to be under a specific path")
            errorMessage = CelestiaString("Please ensure that you have selected a path under %s.", "Custom config/data directory path have to be under a specific path").format(expectedParent?.absolutePath ?: "")
        }
    })
    val directoryChooserLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult(), onResult = {
        val uri = it.data?.data ?: return@rememberLauncherForActivityResult
        val path = RealPathUtils.getRealPath(context, uri)
        if (path != null) {
            viewModel.appSettings[PreferenceManager.PredefinedKey.DataDirPath] = path
            customDataDirPath = path
        } else {
            val expectedParent = context.externalMediaDirs.firstOrNull() ?: context.getExternalFilesDir(null)
            errorTitle = CelestiaString("Unable to resolve path", "")
            errorMessage = CelestiaString("Please ensure that you have selected a path under %s.", "").format(expectedParent?.absolutePath ?: "")
        }
    })

    val internalViewModifier = Modifier
        .fillMaxWidth()
        .padding(
            horizontal = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal),
            vertical = dimensionResource(id = R.dimen.common_page_medium_gap_vertical),
        )
    LazyColumn(modifier = modifier, contentPadding = paddingValues) {
        item {
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
        }
        item {
            TextRow(primaryText = CelestiaString("Config File", "celestia.cfg"), secondaryText = if (customConfigFilePath == null) CelestiaString("Default", "") else CelestiaString("Custom", ""), modifier = Modifier.clickable {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.type = "*/*"
                intent.putExtra("android.content.extra.SHOW_ADVANCED", true)
                if (intent.resolveActivity(context.packageManager) != null) {
                    fileChooserLauncher.launch(intent)
                } else {
                    errorTitle = CelestiaString("Unsupported action.", "")
                    errorMessage = null
                }
            })
            TextRow(primaryText = CelestiaString("Data Directory", "Directory to load data from"), secondaryText = if (customDataDirPath == null) CelestiaString("Default", "") else CelestiaString("Custom", ""), modifier = Modifier.clickable {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                intent.putExtra("android.content.extra.SHOW_ADVANCED", true)
                if (intent.resolveActivity(context.packageManager) != null) {
                    directoryChooserLauncher.launch(intent)
                } else {
                    errorTitle = CelestiaString("Unsupported action.", "")
                    errorMessage = null
                }
            })
        }
        item {
            TextRow(primaryText = CelestiaString("Migrate Add-on Data", "Action to migrate data where the add-on is downloaded"), modifier = Modifier.clickable {
                showMigrationDialog = true
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

    if (showMigrationDialog) {
        OptionInputDialog(
            onDismissRequest = { showMigrationDialog = false },
            title = CelestiaString("Migrate Add-on Data", "Action to migrate data where the add-on is downloaded"),
            items = listOf(
                String.format(CelestiaString("Migrate to %s", "Option to migrate add-on data to a directory. %s is the target directory"), "Android/data"),
                String.format(CelestiaString("Migrate to %s", "Option to migrate add-on data to a directory. %s is the target directory"), "Android/media")
            )
        ) { index ->
            showMigrationDialog = false
            migrationOption = index
        }
    }

    if (migrationOption != null && activity != null) {
        val useDataFolder = migrationOption == 0
        AlertDialog(onDismissRequest = {
            migrationOption = null
        }, confirmButton = {
            TextButton(onClick = {
                migrationOption = null

                val dataDir = activity.getExternalFilesDir(null)
                val mediaDir = activity.externalMediaDirs.firstOrNull()
                if (dataDir == null || mediaDir == null) {
                    errorTitle = CelestiaString("Cannot change add-on directory", "Alert content when user cannot change add-on directory")
                    return@TextButton
                }

                viewModel.appSettings.startEditing()
                if (useDataFolder) {
                    viewModel.appSettings[PreferenceManager.PredefinedKey.MigrationSourceDirectory] = mediaDir.absolutePath
                    viewModel.appSettings[PreferenceManager.PredefinedKey.MigrationTargetDirectory] = dataDir.absolutePath
                    viewModel.appSettings[PreferenceManager.PredefinedKey.UseMediaDirForAddons] = "false"
                } else {
                    viewModel.appSettings[PreferenceManager.PredefinedKey.MigrationSourceDirectory] = dataDir.absolutePath
                    viewModel.appSettings[PreferenceManager.PredefinedKey.MigrationTargetDirectory] = mediaDir.absolutePath
                    viewModel.appSettings[PreferenceManager.PredefinedKey.UseMediaDirForAddons] = "true"
                }
                viewModel.appSettings.stopEditing(writeImmediatelly = true)
                activity.finishAndRemoveTask()
                exitProcess(0)
            }) {
                Text(text = CelestiaString("OK", ""))
            }
        }, title = {
            Text(text = CelestiaString("Migrate Add-on Data", "Action to migrate data where the add-on is downloaded"))
        }, text = {
            Text(text = CelestiaString("Celestia will exit and perform migration the next time it is opened. Target directory will be replaced with content in the current directory. The current directory will be cleared after the content is copied. Future add-ons will be downloaded to the target directory. This operation cannot be undone, so please ensure you make a backup before you proceed.", ""))
        })
    }

    errorTitle?.let {
        val message = errorMessage
        AlertDialog(onDismissRequest = {
            errorTitle = null
            errorMessage = null
        }, confirmButton = {
            TextButton(onClick = {
                errorTitle = null
                errorMessage = null
            }) {
                Text(text = CelestiaString("OK", ""))
            }
        }, title = {
            Text(text = it)
        }, text = if (message != null) { {
            Text(text = message)
        } } else null)
    }
}
