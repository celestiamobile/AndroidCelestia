// SettingsViewModel.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.settings.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import space.celestia.celestia.AppCore
import space.celestia.mobilecelestia.common.CelestiaExecutor
import space.celestia.celestiafoundation.utils.FilePaths
import space.celestia.mobilecelestia.di.AppSettings
import space.celestia.mobilecelestia.di.CoreSettings
import space.celestia.mobilecelestia.purchase.PurchaseManager
import space.celestia.mobilecelestia.utils.PreferenceManager
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(val appCore: AppCore, val executor: CelestiaExecutor, @param:AppSettings val appSettings: PreferenceManager, @param:CoreSettings val coreSettings: PreferenceManager, val purchaseManager: PurchaseManager, val defaultFilePaths: FilePaths) : ViewModel()