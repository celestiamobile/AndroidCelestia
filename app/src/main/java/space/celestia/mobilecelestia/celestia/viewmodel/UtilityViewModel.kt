/*
 * UtilityViewModel.kt
 *
 * Copyright (C) 2025-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.celestia.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import space.celestia.celestia.Selection
import space.celestia.celestiafoundation.resource.model.ResourceItem
import space.celestia.mobilecelestia.travel.GoToData
import javax.inject.Inject

sealed class Utility {
    data object Settings : Utility()
    data object AddonManagement : Utility()
    data object CurrentTime : Utility()
    data object CameraControl : Utility()
    data object Help : Utility()
    data object EventFinder : Utility()
    data object Search : Utility()
    data object Browser : Utility()
    data object Favorites : Utility()
    data object InAppPurchase : Utility()
    data class Web(val uri: Uri, val matchingQueryKeys: List<String>? = null, val filterURL: Boolean = false) : Utility()
    data class WebNavigation(val uri: Uri) : Utility()
    data class GoTo(val goToData: GoToData, val selection: Selection) : Utility()
    data class Addon(val item: ResourceItem) : Utility()
    data class Info(val selection: Selection) : Utility()
    data class SubsystemBrowser(val selection: Selection) : Utility()
}

@HiltViewModel
class UtilityViewModel @Inject constructor() : ViewModel() {
    var current: Utility? by mutableStateOf(null)
}