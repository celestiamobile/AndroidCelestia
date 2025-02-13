/*
 * CameraControlContainerFragment.kt
 *
 * Copyright (C) 2023-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.control

import android.content.Context
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
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.utils.CelestiaString

@Serializable
object CameraControl

@Serializable
object ObserverMode

class CameraControlContainerFragment : Fragment() {
    private var listener: Listener? = null

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
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        val navController = rememberNavController()
        var title by remember {
            mutableStateOf("")
        }
        var canPop by remember { mutableStateOf(false) }
        DisposableEffect(navController) {
            val listener = NavController.OnDestinationChangedListener { controller, _, _ ->
                canPop = controller.previousBackStackEntry != null
                title = if (controller.currentBackStackEntry?.destination?.hasRoute<ObserverMode>() == true) CelestiaString("Flight Mode", "") else CelestiaString("Camera Control", "Observer control")
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
            NavHost(navController = navController, startDestination = CameraControl) {
                composable<CameraControl> {
                    CameraControlScreen(paddingValues = paddingValues, observerModeTapped = {
                        navController.navigate(ObserverMode)
                    }, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection))
                }
                composable<ObserverMode> {
                    ObserverModeScreen(paddingValues = paddingValues, openLink = { link ->
                        listener?.onObserverModeLearnMoreClicked(link)
                    }, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection))
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement CameraControlContainerFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface Listener {
        fun onObserverModeLearnMoreClicked(link: String)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            CameraControlContainerFragment()
    }
}
