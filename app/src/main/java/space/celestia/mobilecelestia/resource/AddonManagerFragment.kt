// ResourceFragment.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.resource

import android.content.Context
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.fragment.app.Fragment
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.compose.SimpleAlertDialog
import space.celestia.mobilecelestia.purchase.SubscriptionBackingScreen
import space.celestia.mobilecelestia.resource.viewmodel.AddonManagerPage
import space.celestia.mobilecelestia.resource.viewmodel.AddonManagerViewModel
import space.celestia.mobilecelestia.utils.CelestiaString
import java.io.File

sealed class AddonManagerAlert {
    data object AddonUpdatesHelp: AddonManagerAlert()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddonManagerScreen(requestRunScript: (File) -> Unit, requestShareAddon: (String, String) -> Unit, openSubscriptionManagement: () -> Unit, requestOpenAddonDownload: () -> Unit) {
    val viewModel: AddonManagerViewModel = hiltViewModel()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val backStack = viewModel.backStack

    if (backStack.isEmpty()) return

    var alert by remember { mutableStateOf<AddonManagerAlert?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                val lastEntry = backStack.lastOrNull() ?: return@TopAppBar
                when (lastEntry) {
                    is AddonManagerPage.Home -> {
                        Text(CelestiaString("Installed", "Title for the list of installed add-ons"))
                    }
                    is AddonManagerPage.Updates -> {
                        Text(CelestiaString("Updates", "View the list of add-ons that have pending updates."))
                    }
                    is AddonManagerPage.Addon -> {
                        Text(lastEntry.title.value)
                    }
                }
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
            }, actions = {
                val lastEntry = backStack.lastOrNull() ?: return@TopAppBar
                when (lastEntry) {
                    is AddonManagerPage.Home -> {
                        if (viewModel.purchaseManager.canUseInAppPurchase()) {
                            TextButton({
                                backStack.add(AddonManagerPage.Updates)
                            }) {
                                Text(CelestiaString("Updates", "View the list of add-ons that have pending updates."))
                            }
                        }
                    }
                    is AddonManagerPage.Updates -> {
                        if (viewModel.purchaseManager.canUseInAppPurchase() && viewModel.purchaseManager.purchaseToken() != null) {
                            IconButton({
                                alert = AddonManagerAlert.AddonUpdatesHelp
                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.help_24px),
                                    contentDescription = null
                                )
                            }
                        }
                    }
                    is AddonManagerPage.Addon -> {
                        IconButton({
                            requestShareAddon(lastEntry.title.value, lastEntry.addon.id)
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_share),
                                contentDescription = null
                            )
                        }
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
                    is AddonManagerPage.Addon -> NavEntry(route) {
                        AddonScreen(item = route.addon, paddingValues = paddingValues, addonInfoUpdated = { info ->
                            route.title.value = info.name
                        }, requestRunScript = requestRunScript)
                    }
                    is AddonManagerPage.Home -> NavEntry(route) {
                        InstalledAddonListScreen(paddingValues = paddingValues, requestOpenAddonDownload = requestOpenAddonDownload, requestOpenInstalledAddon = {
                            backStack.add(AddonManagerPage.Addon(it))
                        })
                    }
                    is AddonManagerPage.Updates -> NavEntry(route) {
                        SubscriptionBackingScreen(paddingValues, openSubscriptionManagement = openSubscriptionManagement) {
                            AddonUpdatesScreen(paddingValues, requestOpenAddon = {
                                backStack.add(AddonManagerPage.Addon(it))
                            })
                        }
                    }
                }
            }
        )
    }

    alert?.let { content ->
        when (content) {
            is AddonManagerAlert.AddonUpdatesHelp -> {
                SimpleAlertDialog(
                    onDismissRequest = {
                        alert = null
                    }, onConfirm = {
                        alert = null
                    },
                    title = CelestiaString("Add-on Updates", ""),
                    text = CelestiaString("Add-on updates are only supported for add-ons installed on version 1.9.3 or above.", "Hint for requirement for updating add-ons.")
                )
            }
        }
    }
}

class AddonManagerFragment : Fragment() {
    interface Listener {
        fun webRequestRunScript(script: File)
        fun webRequestShareAddon(name: String, id: String)
        fun webRequestOpenSubscriptionManagement()
        fun webRequestOpenAddonDownload()
    }

    private var listener: Listener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement AddonFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }


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
                    AddonManagerScreen(requestRunScript = { script ->
                        listener?.webRequestRunScript(script)
                    }, requestShareAddon = { name, id ->
                        listener?.webRequestShareAddon(name, id)
                    }, requestOpenAddonDownload = {
                        listener?.webRequestOpenAddonDownload()
                    }, openSubscriptionManagement = {
                        listener?.webRequestOpenSubscriptionManagement()
                    })
                }
            }
        }
    }


    companion object {
        fun newInstance() = AddonManagerFragment()
    }
}
