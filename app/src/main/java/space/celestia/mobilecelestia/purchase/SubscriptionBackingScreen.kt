// SubscriptionBackingScreen.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.purchase

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import space.celestia.mobilecelestia.compose.EmptyHint
import space.celestia.mobilecelestia.settings.viewmodel.SettingsViewModel
import space.celestia.mobilecelestia.utils.CelestiaString

@Composable
fun SubscriptionBackingScreen(paddingValues: PaddingValues, openSubscriptionManagement: () -> Unit, content: @Composable (PaddingValues) -> Unit) {
    val viewModel: SettingsViewModel = hiltViewModel()
    if (!viewModel.purchaseManager.canUseInAppPurchase()) {
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
            EmptyHint(text = CelestiaString("This feature is not supported.", ""))
        }
    } else if (viewModel.purchaseManager.purchaseToken() == null) {
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
            EmptyHint(text = CelestiaString("This feature is only available to Celestia PLUS users.", ""), actionText = CelestiaString("Get Celestia PLUS", "")) {
                openSubscriptionManagement()
            }
        }
    } else {
        content(paddingValues)
    }
}