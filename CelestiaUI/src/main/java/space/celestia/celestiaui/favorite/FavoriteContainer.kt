// FavoriteContainer.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.celestiaui.favorite

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import space.celestia.celestiaui.R
import space.celestia.celestiaui.compose.SimpleAlertDialog
import space.celestia.celestiaui.favorite.viewmodel.FavoriteViewModel
import space.celestia.celestiaui.favorite.viewmodel.Page
import space.celestia.celestiaui.utils.CelestiaString
import space.celestia.celestiaui.utils.currentBookmark

sealed class FavoriteAlert {
    data object UnableToAdd: FavoriteAlert()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteContainer(shareRequested: (MutableFavoriteBaseItem) -> Unit, openBookmarkRequested: (FavoriteBookmarkItem) -> Unit, openScriptRequested: (FavoriteScriptItem) -> Unit) {
    val viewModel: FavoriteViewModel = hiltViewModel()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val scope = rememberCoroutineScope()
    var alert by remember { mutableStateOf<FavoriteAlert?>(null)  }
    val backStack = viewModel.backStack
    if (backStack.isEmpty()) return

    Scaffold(
        topBar = {
            TopAppBar(title = {
                backStack.lastOrNull()?.let {
                    when (it) {
                        is Page.Item -> {
                            Text(it.item.title)
                        }
                        is Page.Destination -> {
                            Text(it.destination.name)
                        }
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
            }, actions = {
                val current = backStack.lastOrNull() ?: return@TopAppBar
                if (current !is Page.Item || current.item !is MutableFavoriteBaseItem) return@TopAppBar
                IconButton(onClick = {
                    when (current.item) {
                        is FavoriteBookmarkItem -> {
                            scope.launch {
                                val bookmark = withContext(viewModel.executor.asCoroutineDispatcher()) { viewModel.appCore.currentBookmark }
                                if (bookmark == null) {
                                    alert = FavoriteAlert.UnableToAdd
                                    return@launch
                                }
                                current.item.append(FavoriteBookmarkItem(bookmark))
                            }
                        }
                    }
                }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = CelestiaString("Add", "Add a new item (bookmark)")
                    )
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
                    is Page.Item -> {
                        NavEntry(route) {
                            FavoriteItemScreen(route.item, paddingValues, shareRequested = shareRequested, openBookmarkRequested = openBookmarkRequested, openScriptRequested = openScriptRequested)
                        }
                    }
                    is Page.Destination -> {
                        NavEntry(route) {
                            DestinationDetailScreen(item = route.destination, paddingValues = paddingValues)
                        }
                    }
                }
            }
        )
    }

    alert?.let { content ->
        when (content) {
            is FavoriteAlert.UnableToAdd -> {
                SimpleAlertDialog(onDismissRequest = {
                    alert = null
                }, onConfirm = {
                    alert = null
                }, title = CelestiaString("Cannot add object", "Failed to add a favorite item (currently a bookmark)"))
            }
        }
    }
}
