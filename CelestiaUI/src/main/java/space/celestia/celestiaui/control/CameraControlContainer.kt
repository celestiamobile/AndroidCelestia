// CameraControlContainer.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.celestiaui.control

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import space.celestia.celestiaui.R
import space.celestia.celestiaui.control.viewmodel.CameraControlViewModel
import space.celestia.celestiaui.control.viewmodel.Page
import space.celestia.celestiaui.utils.CelestiaString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraControlContainer(observerModeLearnMoreClicked: (String, Boolean) -> Unit) {
    val viewModel: CameraControlViewModel = hiltViewModel()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val backStack = viewModel.backStack
    if (backStack.isEmpty()) return

    Scaffold(
        topBar = {
            TopAppBar(title = {
                when (backStack.lastOrNull()) {
                    Page.CameraControl -> {
                        Text(CelestiaString("Camera Control", "Observer control"))
                    }
                    Page.ObserverMode -> {
                        Text(CelestiaString("Flight Mode", ""))
                    }
                    else -> {}
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
                    is Page.CameraControl -> {
                        NavEntry(route) {
                            CameraControl(paddingValues) {
                                viewModel.backStack.add(Page.ObserverMode)
                            }
                        }
                    }
                    is Page.ObserverMode -> {
                        NavEntry(route) {
                            ObserverModeScreen(paddingValues) { link, localizable ->
                                observerModeLearnMoreClicked(link, localizable)
                            }
                        }
                    }
                }
            }
        )
    }
}
