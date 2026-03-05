// SubsystemBrowserFragment.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.celestiaui.browser

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import space.celestia.celestia.BrowserItem
import space.celestia.celestia.Selection
import space.celestia.celestiaui.R
import space.celestia.celestiaui.browser.viewmodel.BrowserPredefinedItem
import space.celestia.celestiaui.browser.viewmodel.SubsystemPage
import space.celestia.celestiaui.browser.viewmodel.SubsystemViewModel
import space.celestia.celestiaui.compose.SimpleAlertDialog
import space.celestia.celestiaui.info.InfoScreen
import space.celestia.celestiaui.utils.CelestiaString

sealed class SubsystemBrowserAlert {
    data object ObjectNotFound: SubsystemBrowserAlert()
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubsystemBrowser(selection: Selection, linkClicked: (String) -> Unit, openSubsystem: (Selection) -> Unit, addonCategoryRequested: (BrowserPredefinedItem.CategoryInfo) -> Unit, openRelatedAddons: (String) -> Unit, openSubscriptionManagement: () -> Unit) {
    val viewModel: SubsystemViewModel = hiltViewModel()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    var alert by remember { mutableStateOf<SubsystemBrowserAlert?>(null)  }
    val backStack = viewModel.backStack
    LaunchedEffect(Unit) {
        if (backStack.isEmpty())
            backStack.add(SubsystemPage.Item(BrowserItem(viewModel.appCore.simulation.universe.getNameForSelection(selection), null, requireNotNull(selection.`object`), viewModel.appCore.simulation.universe)))
    }

    if (backStack.isEmpty()) return

    Scaffold(
        topBar = {
            TopAppBar(title = {
                val current = backStack.lastOrNull() ?: return@TopAppBar
                when (current) {
                    is SubsystemPage.Item -> {
                        Text(current.item.alternativeName ?: current.item.name)
                    }
                    is SubsystemPage.Info -> {
                        Text(viewModel.appCore.simulation.universe.getNameForSelection(current.info))
                    }
                }
            }, navigationIcon = {
                if (backStack.count() > 1) {
                    IconButton(onClick = dropUnlessResumed {
                        if (backStack.count() > 1) {
                            backStack.removeLastOrNull()
                        }
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_action_arrow_back),
                            contentDescription = null
                        )
                    }
                }
            }, scrollBehavior = scrollBehavior, windowInsets = WindowInsets())
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Bottom),
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        NavDisplay(
            backStack = backStack,
            transitionSpec = {
                slideInHorizontally(initialOffsetX = { it }) togetherWith
                        slideOutHorizontally(targetOffsetX = { -it })
            },
            popTransitionSpec = {
                slideInHorizontally(initialOffsetX = { -it }) togetherWith
                        slideOutHorizontally(targetOffsetX = { it })
            },
            predictivePopTransitionSpec = {
                slideInHorizontally(initialOffsetX = { -it }) togetherWith
                        slideOutHorizontally(targetOffsetX = { it })
            },
            entryProvider = { route ->
                when (route) {
                    is SubsystemPage.Item -> {
                        NavEntry(route) {
                            BrowserEntry(item = route.item, paddingValues = paddingValues, itemSelected = { item, isLeaf ->
                                if (isLeaf) {
                                    val obj = item.`object`
                                    if (obj != null) {
                                        backStack.add(SubsystemPage.Info(Selection(obj)))
                                    } else {
                                        alert = SubsystemBrowserAlert.ObjectNotFound
                                    }
                                } else {
                                    backStack.add(SubsystemPage.Item(item))
                                }
                            }, addonCategoryRequested = {
                                addonCategoryRequested(it)
                            })
                        }
                    }
                    is SubsystemPage.Info -> NavEntry(route) {
                        InfoScreen(selection = route.info, showTitle = false, linkClicked = {
                            linkClicked(it)
                        }, openSubsystem = {
                            openSubsystem(route.info)
                        }, openRelatedAddons = openRelatedAddons, openSubscriptionManagement = openSubscriptionManagement, paddingValues = paddingValues)
                    }
                }
            }
        )
    }

    alert?.let { content ->
        when (content) {
            is SubsystemBrowserAlert.ObjectNotFound -> {
                SimpleAlertDialog(onDismissRequest = {
                    alert = null
                }, onConfirm = {
                    alert = null
                }, title = CelestiaString("Object not found", ""))
            }
        }
    }
}

