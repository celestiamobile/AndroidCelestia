// SettingsViewModel.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.celestiaui.settings.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import space.celestia.celestia.AppCore
import space.celestia.celestiafoundation.utils.FilePaths
import space.celestia.celestiaui.di.AppSettings
import space.celestia.celestiaui.di.AppSettingsNoBackup
import space.celestia.celestiaui.di.ApplicationId
import space.celestia.celestiaui.di.CoreSettings
import space.celestia.celestiaui.purchase.PurchaseManager
import space.celestia.celestiaui.utils.PreferenceManager
import java.util.concurrent.Executor
import javax.inject.Inject

sealed class Page {
    data object Home: Page()
    data class Common(val item: SettingsCommonItem): Page()
    data object CurrentTime: Page()
    data object RefreshRate: Page()
    data object RenderInfo: Page()
    data object DataLocation: Page()
    data object About: Page()
    data object Language: Page()
    data object Toolbar: Page()
    @RequiresApi(Build.VERSION_CODES.Q)
    data object Font: Page()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(val appCore: AppCore, val executor: Executor, @param:AppSettings val appSettings: PreferenceManager, @param:AppSettingsNoBackup val appSettingsNoBackup: PreferenceManager, @param:CoreSettings val coreSettings: PreferenceManager, val purchaseManager: PurchaseManager, val defaultFilePaths: FilePaths, @param:ApplicationId val applicationId: String, val settingsEntryProvider: SettingsEntryProvider) : ViewModel() {
    val backStack = mutableStateListOf<Page>(Page.Home)
}