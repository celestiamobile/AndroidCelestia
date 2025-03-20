/*
 * AddonManagementScreen.kt
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import space.celestia.celestia.AppCore
import space.celestia.celestiafoundation.resource.model.ResourceItem
import space.celestia.celestiafoundation.utils.commonHandler
import space.celestia.mobilecelestia.resource.model.ResourceAPI
import space.celestia.mobilecelestia.resource.viewmodel.AddonViewModel
import space.celestia.mobilecelestia.utils.CelestiaString

@Serializable
data object AddonList

@Serializable
data class Addon(val id: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddonManagementScreen(onOpenAddonDownload: () -> Unit, onShareAddon:(String, String) -> Unit, onExternalWebLinkClicked:(String) -> Unit, onShareURL:(String, String) -> Unit, onOpenSubscriptionPage:() -> Unit, onReceivedACK:(String) -> Unit) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var title by remember { mutableStateOf("") }
    var canPop by remember { mutableStateOf(false) }
    var canShare by remember { mutableStateOf(false) }

    val addonListState = remember { AddonListState() }
    val navController = rememberNavController()

    val viewModel: AddonViewModel = hiltViewModel()
    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { controller, _, _ ->
            canPop = controller.previousBackStackEntry != null
            val currentBackStackEntry = controller.currentBackStackEntry
            if (currentBackStackEntry == null) {
                title = ""
                canShare = false
            } else if (currentBackStackEntry.destination.hasRoute<AddonList>()) {
                title = CelestiaString("Installed", "Title for the list of installed add-ons")
                addonListState.needsRefresh = true
                canShare = false
            } else if (currentBackStackEntry.destination.hasRoute<Addon>()) {
                title = requireNotNull(viewModel.addonMap[currentBackStackEntry.toRoute<Addon>().id]).name
                canShare = true
            }
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    Scaffold(topBar = {
        TopAppBar(title = {
            Text(text = title)
        }, navigationIcon = {
            if (canPop) {
                IconButton(onClick = {
                    navController.navigateUp()
                }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = CelestiaString("Go Back", "Button to go back to the previous page"))
                }
            }
        }, actions = {
            if (canShare) {
                IconButton(onClick = {
                    val currentBackStackEntry = navController.currentBackStackEntry
                    if (currentBackStackEntry != null && currentBackStackEntry.destination.hasRoute<Addon>()) {
                        val addon = requireNotNull(viewModel.addonMap[currentBackStackEntry.toRoute<Addon>().id])
                        onShareAddon(addon.name, addon.id)
                    }
                }) {
                    Icon(imageVector = Icons.Filled.Share, contentDescription = CelestiaString("Share", ""))
                }
            }
        }, scrollBehavior = scrollBehavior)
    }) { paddingValues ->
        NavHost(navController = navController, startDestination = AddonList) {
            composable<AddonList> {
                AddonListScreen(state = addonListState, openAddonDownload = {
                    onOpenAddonDownload()
                }, openInstalledAddon = {
                    viewModel.addonMap[it.id] = it
                    navController.navigate(Addon(it.id))
                }, paddingValues = paddingValues, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection))
            }
            composable<Addon> {
                var addon by remember { mutableStateOf(requireNotNull(viewModel.addonMap[it.toRoute<Addon>().id])) }
                LaunchedEffect(Unit) {
                    try {
                        val result = viewModel.resourceAPI.item(AppCore.getLanguage(), addon.id).commonHandler(
                            ResourceItem::class.java, ResourceAPI.gson)
                        viewModel.addonMap[addon.id] = result
                        addon = result
                    } catch (ignored: Throwable) {}
                }
                AddonScreen(
                    addon = addon,
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
    }
}