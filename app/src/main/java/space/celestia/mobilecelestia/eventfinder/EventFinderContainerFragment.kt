// EventFinderContainerFragment.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.eventfinder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.LocalActivity
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.fragment.app.Fragment
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import space.celestia.celestia.Body
import space.celestia.celestia.EclipseFinder
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.eventfinder.viewmodel.EventFinderViewModel
import space.celestia.mobilecelestia.eventfinder.viewmodel.Page
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.julianDay
import space.celestia.mobilecelestia.utils.showAlert
import space.celestia.mobilecelestia.utils.showLoading

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventFinder() {
    val scope = rememberCoroutineScope()
    val viewModel: EventFinderViewModel = hiltViewModel()
    val activity = LocalActivity.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val backStack = viewModel.backStack

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(CelestiaString("Eclipse Finder", ""))
            }, navigationIcon = {
                if (backStack.count() > 1) {
                    IconButton(onClick = {
                        backStack.removeLastOrNull()
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
            onBack = { backStack.removeLastOrNull() },
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
                    is Page.Home -> NavEntry(route) {
                        EventFinderInput(paddingValues) { objectName, startDate, endDate ->
                            scope.launch {
                                val body = withContext(viewModel.executor.asCoroutineDispatcher()) {
                                    viewModel.appCore.simulation.findObject(objectName).`object` as? Body
                                }
                                if (body == null) {
                                    activity?.showAlert(CelestiaString("Object not found", ""))
                                    return@launch
                                }
                                val finder = EclipseFinder(body)
                                val alert = activity?.showLoading(CelestiaString("Calculating…", "Calculating for eclipses")) {
                                    finder.abort()
                                } ?: return@launch
                                val results = withContext(Dispatchers.IO) {
                                    finder.search(
                                        startDate.julianDay,
                                        endDate.julianDay,
                                        EclipseFinder.ECLIPSE_KIND_LUNAR or EclipseFinder.ECLIPSE_KIND_SOLAR
                                    )
                                }
                                finder.close()
                                if (alert.isShowing) alert.dismiss()
                                backStack.add(Page.Results(results))
                            }
                        }
                    }
                    is Page.Results -> NavEntry(route) {
                        EventFinderResults(results = route.results, paddingValues = paddingValues) { eclipse ->
                            scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                                viewModel.appCore.simulation.goToEclipse(eclipse)
                            }
                        }
                    }
                }
            }
        )
    }
}

class EventFinderContainerFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Mdc3Theme {
                    EventFinder()
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            EventFinderContainerFragment()
    }
}
