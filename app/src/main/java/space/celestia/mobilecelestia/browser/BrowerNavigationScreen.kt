/*
 * BrowserNavigationScreen.kt
 *
 * Copyright (C) 2025-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.browser

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import space.celestia.celestia.BrowserItem
import space.celestia.celestia.Selection
import space.celestia.mobilecelestia.browser.viewmodel.BrowserNavigationViewModel
import space.celestia.mobilecelestia.info.InfoScreen
import space.celestia.mobilecelestia.info.model.InfoActionItem
import space.celestia.mobilecelestia.utils.CelestiaString
import java.net.URL

@Serializable
sealed class Browser {
    abstract val path: String

    @Serializable
    data class Item(override val path: String) : Browser()

    @Serializable
    sealed class Root() : Browser() {
        @Serializable
        data class SolarSystem(override val path: String) : Root()
        @Serializable
        data class Stars(override val path: String) : Root()
        @Serializable
        data class DSOs(override val path: String) : Root()
    }
}

@Serializable
data class BrowserItemInfo(val objectType: Int, val objectPointer: Long)

@Composable
fun BrowserNavigationScreen(root: Browser, rootItem: BrowserItem, navController: NavHostController, addonCategoryRequested: (BrowserPredefinedItem.CategoryInfo) -> Unit, linkHandler: (URL) -> Unit, actionHandler: (InfoActionItem, Selection) -> Unit, topBarStateChange: (String, Boolean) -> Unit, paddingValues: PaddingValues, modifier: Modifier = Modifier) {
    val viewModelStoreOwner = requireNotNull(LocalViewModelStoreOwner.current)

    val viewModel: BrowserNavigationViewModel = hiltViewModel()
    if (viewModel.browserMap.isEmpty()) {
        val path = root.path
        viewModel.root = root
        viewModel.currentPath = path
        viewModel.browserMap[path] = rootItem
        viewModel.currentPathMap[root] = path
    }

    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { controller, _, _ ->
            val canPop = controller.previousBackStackEntry != null
            val title: String
            val currentBackStackEntry = controller.currentBackStackEntry
            if (currentBackStackEntry == null) {
                title = ""
            } else if (currentBackStackEntry.destination.hasRoute<Browser.Item>()) {
                val browserItem = viewModel.browserMap[currentBackStackEntry.toRoute<Browser.Item>().path]
                title = browserItem?.alternativeName ?: browserItem?.name ?: ""
            } else if (currentBackStackEntry.destination.hasRoute<Browser.Root.SolarSystem>()) {
                val browserItem = viewModel.browserMap[currentBackStackEntry.toRoute<Browser.Root.SolarSystem>().path]
                title = browserItem?.alternativeName ?: browserItem?.name ?: ""
            } else if (currentBackStackEntry.destination.hasRoute<Browser.Root.Stars>()) {
                val browserItem = viewModel.browserMap[currentBackStackEntry.toRoute<Browser.Root.Stars>().path]
                title = browserItem?.alternativeName ?: browserItem?.name ?: ""
            } else if (currentBackStackEntry.destination.hasRoute<Browser.Root.DSOs>()) {
                val browserItem = viewModel.browserMap[currentBackStackEntry.toRoute<Browser.Root.DSOs>().path]
                title = browserItem?.alternativeName ?: browserItem?.name ?: ""
            } else if (currentBackStackEntry.destination.hasRoute<BrowserItemInfo>()) {
                val item = currentBackStackEntry.toRoute<BrowserItemInfo>()
                title = viewModel.appCore.simulation.universe.getNameForSelection(Selection(item.objectPointer, item.objectType))
            } else {
                title = ""
            }
            topBarStateChange(title, canPop)
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    var errorText: String? by remember {
        mutableStateOf(null)
    }

    NavHost(navController = navController, startDestination = root) {
        @Composable
        fun ListScreen(path: String) {
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides viewModelStoreOwner
            ) {
                BrowserItemScreen(
                    paddingValues = paddingValues,
                    path = path,
                    modifier = modifier,
                    itemSelected = { item ->
                        if (!item.isLeaf) {
                            viewModel.currentPath = "${viewModel.currentPath}/${item.item.hashCode()}"
                            viewModel.currentPathMap[viewModel.root] = viewModel.currentPath
                            viewModel.browserMap[viewModel.currentPath] = item.item
                            navController.navigate(Browser.Item(viewModel.currentPath))
                        } else {
                            val obj = item.item.`object`
                            if (obj != null) {
                                val selection = Selection(obj)
                                navController.navigate(BrowserItemInfo(selection.type, selection.objectPointer))
                            } else {
                                errorText = CelestiaString("Object not found", "")
                            }
                        }
                    },
                    addonCategoryRequested = addonCategoryRequested
                )
            }
        }

        composable<Browser.Item> {
            ListScreen(path = it.toRoute<Browser.Item>().path)
        }

        composable<Browser.Root.SolarSystem> {
            ListScreen(path = it.toRoute<Browser.Root.SolarSystem>().path)
        }

        composable<Browser.Root.Stars> {
            ListScreen(path = it.toRoute<Browser.Root.Stars>().path)
        }

        composable<Browser.Root.DSOs> {
            ListScreen(path = it.toRoute<Browser.Root.DSOs>().path)
        }

        composable<BrowserItemInfo> {
            val item = it.toRoute<BrowserItemInfo>()
            val selection = Selection(item.objectPointer, item.objectType)
            InfoScreen(
                selection = selection,
                showTitle = false,
                linkHandler = linkHandler,
                actionHandler = actionHandler,
                paddingValues = paddingValues,
                modifier = modifier
            )
        }
    }

    errorText?.let {
        AlertDialog(onDismissRequest = {
            errorText = null
        }, confirmButton = {
            TextButton(onClick = {
                errorText = null
            }) {
                Text(text = CelestiaString("OK", ""))
            }
        }, title = {
            Text(text = it)
        })
    }
}