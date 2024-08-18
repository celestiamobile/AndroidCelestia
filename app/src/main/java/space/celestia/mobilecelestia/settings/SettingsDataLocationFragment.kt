/*
 * SettingsDataLocationFragment.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.settings

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import dagger.hilt.android.AndroidEntryPoint
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.compose.Footer
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.compose.Separator
import space.celestia.mobilecelestia.compose.TextRow
import space.celestia.mobilecelestia.di.AppSettings
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.PreferenceManager
import space.celestia.mobilecelestia.utils.RealPathUtils
import space.celestia.mobilecelestia.utils.showAlert
import space.celestia.mobilecelestia.utils.showOptions
import java.lang.ref.WeakReference
import javax.inject.Inject
import kotlin.system.exitProcess

@AndroidEntryPoint
class SettingsDataLocationFragment : NavigationFragment.SubFragment() {
    private var customConfigFilePath = mutableStateOf<String?>(null)
    private var customDataDirPath = mutableStateOf<String?>(null)

    @AppSettings
    @Inject
    lateinit var appSettings: PreferenceManager

    private lateinit var fileChooserLauncher: ActivityResultLauncher<Intent>
    private lateinit var directoryChooserLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        customConfigFilePath.value = appSettings[PreferenceManager.PredefinedKey.ConfigFilePath]
        customDataDirPath.value = appSettings[PreferenceManager.PredefinedKey.DataDirPath]

        val weakSelf = WeakReference(this)
        fileChooserLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val self = weakSelf.get() ?: return@registerForActivityResult
            val activity = self.activity ?: return@registerForActivityResult
            val uri = it.data?.data ?: return@registerForActivityResult
            val path = RealPathUtils.getRealPath(activity, uri)
            if (path == null) {
                self.showWrongPathProvided()
            } else {
                self.appSettings[PreferenceManager.PredefinedKey.ConfigFilePath] = path
                self.customConfigFilePath.value = path
            }
        }

        directoryChooserLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val self = weakSelf.get() ?: return@registerForActivityResult
            val activity = self.activity ?: return@registerForActivityResult
            val uri = it.data?.data ?: return@registerForActivityResult
            val path = RealPathUtils.getRealPath(activity, DocumentsContract.buildDocumentUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri)))
            if (path == null) {
                self.showWrongPathProvided()
            } else {
                self.appSettings[PreferenceManager.PredefinedKey.DataDirPath] = path
                self.customDataDirPath.value = path
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Mdc3Theme {
                    MainScreen()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = CelestiaString("Data Location", "Title for celestia.cfg, data location setting")
    }

    private fun showWrongPathProvided() {
        val activity = this.activity ?: return
        val expectedParent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) activity.externalMediaDirs.firstOrNull() else activity.getExternalFilesDir(null)
        activity.showAlert(CelestiaString("Unable to resolve path", "Custom config/data directory path have to be under a specific path"), CelestiaString("Please ensure that you have selected a path under %s.", "Custom config/data directory path have to be under a specific path").format(expectedParent?.absolutePath ?: ""))
    }

    private fun launchDataDirectoryPicker() {
        val packageManager = activity?.packageManager ?: return
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.putExtra("android.content.extra.SHOW_ADVANCED", true)
        if (intent.resolveActivity(packageManager) != null)
            directoryChooserLauncher.launch(intent)
        else
            showUnsupportedAction()
    }

    private fun launchConfigFilePicker() {
        val packageManager = activity?.packageManager ?: return
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "*/*"
        intent.putExtra("android.content.extra.SHOW_ADVANCED", true)
        if (intent.resolveActivity(packageManager) != null)
            fileChooserLauncher.launch(intent)
        else
            showUnsupportedAction()
    }

    private fun showUnsupportedAction() {
        val activity = this.activity ?: return
        activity.showAlert(CelestiaString("Unsupported action.", ""))
    }

    private fun showMigration() {
        val activity = this.activity ?: return
        val dataDir = activity.getExternalFilesDir(null)
        val mediaDir = activity.externalMediaDirs.firstOrNull()
        if (dataDir == null || mediaDir == null){
            activity.showAlert(CelestiaString("Cannot change add-on directory", "Alert content when user cannot change add-on directory"))
            return
        }
        activity.showOptions(CelestiaString("Migrate Add-on Data", "Action to migrate data where the add-on is downloaded"), options = arrayOf(
            String.format(CelestiaString("Migrate to %s", "Option to migrate add-on data to a directory. %s is the target directory"), "Android/data"),
            String.format(CelestiaString("Migrate to %s", "Option to migrate add-on data to a directory. %s is the target directory"), "Android/media")
        )) { index ->
            activity.showAlert(CelestiaString("Migrate Add-on Data", "Action to migrate data where the add-on is downloaded"), message = CelestiaString("Celestia will exit and perform migration the next time it is opened. Target directory will be replaced with content in the current directory. The current directory will be cleared after the content is copied. Future add-ons will be downloaded to the target directory. This operation cannot be undone, so please ensure you make a backup before you proceed.", ""), handler = {
                appSettings.startEditing()
                if (index == 0) {
                    appSettings[PreferenceManager.PredefinedKey.MigrationSourceDirectory] = mediaDir.absolutePath
                    appSettings[PreferenceManager.PredefinedKey.MigrationTargetDirectory] = dataDir.absolutePath
                    appSettings[PreferenceManager.PredefinedKey.UseMediaDirForAddons] = "false"
                } else {
                    appSettings[PreferenceManager.PredefinedKey.MigrationSourceDirectory] = dataDir.absolutePath
                    appSettings[PreferenceManager.PredefinedKey.MigrationTargetDirectory] = mediaDir.absolutePath
                    appSettings[PreferenceManager.PredefinedKey.UseMediaDirForAddons] = "true"
                }
                appSettings.stopEditing(writeImmediatelly = true)
                activity.finishAndRemoveTask()
                exitProcess(0)
            })
        }
    }

    @Composable
    private fun MainScreen() {
        val internalViewModifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal),
                vertical = dimensionResource(id = R.dimen.common_page_medium_gap_vertical),
            )
        val nestedScrollInterop = rememberNestedScrollInteropConnection()
        LazyColumn(modifier = Modifier.nestedScroll(nestedScrollInterop), contentPadding = WindowInsets.systemBars.asPaddingValues()) {
            item {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
            }
            item {
                TextRow(primaryText = CelestiaString("Config File", "celestia.cfg"), secondaryText = if (customConfigFilePath.value == null) CelestiaString("Default", "") else CelestiaString("Custom", ""), modifier = Modifier.clickable {
                    launchConfigFilePicker()
                })
                TextRow(primaryText = CelestiaString("Data Directory", "Directory to load data from"), secondaryText = if (customDataDirPath.value == null) CelestiaString("Default", "") else CelestiaString("Custom", ""), modifier = Modifier.clickable {
                    launchDataDirectoryPicker()
                })
            }

            item {
                TextRow(primaryText = CelestiaString("Migrate Add-on Data", "Action to migrate data where the add-on is downloaded"), modifier = Modifier.clickable {
                    showMigration()
                })
            }

            item {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
                Separator()
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
            }
            item {
                FilledTonalButton(modifier = internalViewModifier, onClick = {
                    appSettings[PreferenceManager.PredefinedKey.ConfigFilePath] = null
                    appSettings[PreferenceManager.PredefinedKey.DataDirPath] = null
                    customConfigFilePath.value = null
                    customDataDirPath.value = null
                }) {
                    Text(text = CelestiaString("Reset to Default", "Reset celestia.cfg, data directory location"))
                }
                Footer(text = CelestiaString("Configuration will take effect after a restart.", "Change requires a restart"))
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_tall)))
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SettingsDataLocationFragment()
    }
}
