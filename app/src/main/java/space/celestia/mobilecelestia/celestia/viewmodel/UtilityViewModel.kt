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
import space.celestia.mobilecelestia.control.BottomControlAction
import space.celestia.mobilecelestia.toolbar.ToolbarAction
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
    data class Web(val context: Context.Web) : Utility()
    data class WebNavigation(val context: Context.WebNavigation)  : Utility()
    data class GoTo(val context: Context.GoTo)  : Utility()
    data class Addon(val context: Context.Addon)  : Utility()
    data class Info(val context: Context.Info)  : Utility()
    data class SubsystemBrowser(val context: Context.SubsystemBrowser)  : Utility()
    data object Empty : Utility()
}

sealed class Context {
    data class Web(val uri: Uri, val matchingQueryKeys: List<String>? = null, val filterURL: Boolean = false) : Context()
    data class WebNavigation(val uri: Uri) : Context()
    data class GoTo(val goToData: GoToData, val selection: Selection) : Context()
    data class Addon(val item: ResourceItem) : Context()
    data class Info(val selection: Selection) : Context()
    data class SubsystemBrowser(val selection: Selection) : Context()
}

@HiltViewModel
class UtilityViewModel @Inject constructor() : ViewModel() {
    var current: Utility by mutableStateOf(Utility.Empty)
    var additionalDrawerActions: List<List<ToolbarAction>>? by mutableStateOf(null)
    var toolbarActions: List<BottomControlAction>? by mutableStateOf(null)
    var loadingText: String? by mutableStateOf("")
}