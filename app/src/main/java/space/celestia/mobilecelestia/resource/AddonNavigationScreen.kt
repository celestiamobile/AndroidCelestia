/*
 * AddonNavigationScreen.kt
 *
 * Copyright (C) 2025-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.resource

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import space.celestia.celestia.AppCore
import space.celestia.celestiafoundation.resource.model.ResourceItem
import space.celestia.mobilecelestia.utils.CelestiaString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddonNavigationScreen(item: ResourceItem, onShareAddon:(String, String) -> Unit, onExternalWebLinkClicked:(String) -> Unit, onShareURL:(String, String) -> Unit, onOpenSubscriptionPage:() -> Unit, onReceivedACK:(String) -> Unit) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(topBar = {
        TopAppBar(title = {
            Text(text = item.name)
        }, actions = {
            IconButton(onClick = {
                onShareAddon(item.name, item.id)
            }) {
                Icon(imageVector = Icons.Filled.Share, contentDescription = CelestiaString("Share", ""))
            }
        }, scrollBehavior = scrollBehavior)
    }) { paddingValues ->
        AddonScreen(
            addon = item,
            shareURLHandler = { title, url ->
                onShareURL(title, url)
            },
            receivedACKHandler = {
                onReceivedACK(it)
            },
            openSubscriptionPageHandler = {
                onOpenSubscriptionPage()
            },
            openExternalWebLink = {
                onExternalWebLinkClicked(it)
            },
            paddingValues = paddingValues,
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        )
    }
}
