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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.serialization.Serializable
import space.celestia.celestia.Selection
import space.celestia.celestiafoundation.resource.model.ResourceItem
import space.celestia.mobilecelestia.control.BottomControlAction
import space.celestia.mobilecelestia.toolbar.ToolbarAction
import space.celestia.mobilecelestia.travel.GoToData
import java.util.UUID
import javax.inject.Inject
import kotlin.uuid.Uuid

@Serializable
sealed class Utility {
    @Serializable
    data object Settings : Utility()
    @Serializable
    data object AddonManagement : Utility()
    @Serializable
    data object CurrentTime : Utility()
    @Serializable
    data object CameraControl : Utility()
    @Serializable
    data object Help : Utility()
    @Serializable
    data object EventFinder : Utility()
    @Serializable
    data object Search : Utility()
    @Serializable
    data object Browser : Utility()
    @Serializable
    data object Favorites : Utility()
    @Serializable
    data object InAppPurchase : Utility()
    @Serializable
    data class Web(val id: String = "${UUID.randomUUID()}") : Utility()
    @Serializable
    data class WebNavigation(val id: String = "${UUID.randomUUID()}")  : Utility()
    @Serializable
    data class GoTo(val id: String = "${UUID.randomUUID()}")  : Utility()
    @Serializable
    data class Addon(val id: String = "${UUID.randomUUID()}")  : Utility()
    @Serializable
    data class Info(val id: String = "${UUID.randomUUID()}")  : Utility()
    @Serializable
    data class SubsystemBrowser(val id: String = "${UUID.randomUUID()}")  : Utility()
    @Serializable
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
    var canShowDrawerAndToolbar: Boolean by mutableStateOf(false)
    var additionalDrawerActions: List<List<ToolbarAction>> by mutableStateOf(listOf())
    var toolbarActions: List<BottomControlAction>? by mutableStateOf(null)
    var context: Context? = null
}