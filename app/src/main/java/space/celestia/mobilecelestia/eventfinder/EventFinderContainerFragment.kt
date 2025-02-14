/*
 * EventFinderContainerFragment.kt
 *
 * Copyright (C) 2025-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.eventfinder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.eventfinder.viewmodel.EventFinderViewModel
import space.celestia.mobilecelestia.utils.CelestiaString

@Serializable
data object EventFinderInput
@Serializable
data object EventFinderResult

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
                    MainScreen()
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainScreen() {
        val viewModelStoreOwner = requireNotNull(LocalViewModelStoreOwner.current)
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        val navController = rememberNavController()
        val title by remember {
            mutableStateOf(CelestiaString("Eclipse Finder", ""))
        }
        var canPop by remember { mutableStateOf(false) }
        DisposableEffect(navController) {
            val listener = NavController.OnDestinationChangedListener { controller, _, _ ->
                canPop = controller.previousBackStackEntry != null
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
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "")
                    }
                }
            }, scrollBehavior = scrollBehavior)
        }) { paddingValues ->
            NavHost(navController = navController, startDestination = EventFinderInput) {
                composable<EventFinderInput> {
                    CompositionLocalProvider(
                        LocalViewModelStoreOwner provides viewModelStoreOwner
                    ) {
                        EventFinderInputScreen(paddingValues = paddingValues, showResults = {
                            navController.navigate(EventFinderResult)
                        }, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection))
                    }
                }
                composable<EventFinderResult> {
                    CompositionLocalProvider(
                        LocalViewModelStoreOwner provides viewModelStoreOwner
                    ) {
                        EventFinderResultScreen(
                            paddingValues = paddingValues,
                            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                        )
                    }
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
