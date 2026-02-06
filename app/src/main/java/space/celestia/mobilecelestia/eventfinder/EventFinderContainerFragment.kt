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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.fragment.app.Fragment
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.dropUnlessResumed
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
import space.celestia.mobilecelestia.compose.SimpleAlertDialog
import space.celestia.mobilecelestia.eventfinder.viewmodel.EventFinderViewModel
import space.celestia.mobilecelestia.eventfinder.viewmodel.Page
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.julianDay

private sealed class EventFinderAlert {
    data object ObjectNotFound : EventFinderAlert()
    data class Calculating(val finder: EclipseFinder): EventFinderAlert()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventFinder() {
    val scope = rememberCoroutineScope()
    val viewModel: EventFinderViewModel = hiltViewModel()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val backStack = viewModel.backStack
    var alert by remember { mutableStateOf<EventFinderAlert?>(null) }
    if (backStack.isEmpty()) return

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(CelestiaString("Eclipse Finder", ""))
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
                    is Page.Home -> NavEntry(route) {
                        EventFinderInputScreen(paddingValues) { objectName, startDate, endDate ->
                            scope.launch {
                                val body = withContext(viewModel.executor.asCoroutineDispatcher()) {
                                    viewModel.appCore.simulation.findObject(objectName).`object` as? Body
                                }
                                if (body == null) {
                                    alert = EventFinderAlert.ObjectNotFound
                                    return@launch
                                }
                                val finder = EclipseFinder(body)
                                alert = EventFinderAlert.Calculating(finder)
                                val results = withContext(Dispatchers.IO) {
                                    finder.search(
                                        startDate.julianDay,
                                        endDate.julianDay,
                                        EclipseFinder.ECLIPSE_KIND_LUNAR or EclipseFinder.ECLIPSE_KIND_SOLAR
                                    )
                                }
                                finder.close()
                                alert?.let { content ->
                                    if (content is EventFinderAlert.Calculating && content.finder == finder) {
                                        alert = null
                                        backStack.add(Page.Results(results))
                                    }
                                }
                            }
                        }
                    }
                    is Page.Results -> NavEntry(route) {
                        EventFinderResultsScreen(results = route.results, paddingValues = paddingValues) { eclipse ->
                            scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                                viewModel.appCore.simulation.goToEclipse(eclipse)
                            }
                        }
                    }
                }
            }
        )
    }

    alert?.let { content ->
        when (content) {
            is EventFinderAlert.ObjectNotFound -> {
                SimpleAlertDialog(onDismissRequest = {
                    alert = null
                }, onConfirm = {
                    alert = null
                }, title = CelestiaString("Object not found", ""))
            }
            is EventFinderAlert.Calculating -> {
                SimpleAlertDialog(onDismissRequest = {
                    alert = null
                }, onConfirm = {
                    alert = null
                    content.finder.abort()
                }, title = CelestiaString("Calculating…", "Calculating for eclipses"), confirmButtonText = CelestiaString("Cancel", ""), dismissOnBackPressOrClickOutside = false)
            }
        }
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
