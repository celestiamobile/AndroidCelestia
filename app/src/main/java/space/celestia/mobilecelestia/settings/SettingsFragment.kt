// SettingsFragment.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.settings

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.Display
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.fragment.app.Fragment
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import dagger.hilt.android.AndroidEntryPoint
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.purchase.FontSettingsScreen
import space.celestia.mobilecelestia.purchase.SubscriptionBacking
import space.celestia.mobilecelestia.purchase.ToolbarSettingsScreen
import space.celestia.mobilecelestia.settings.viewmodel.Page
import space.celestia.mobilecelestia.settings.viewmodel.SettingsViewModel
import space.celestia.mobilecelestia.utils.CelestiaString


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(linkClicked: (String, Boolean) -> Unit, providePreferredDisplay: () -> Display?, refreshRateChanged: (Int) -> Unit, openSubscriptionManagement: () -> Unit) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val backStack = viewModel.backStack

    Scaffold(
        topBar = {
            TopAppBar(title = {
                val current = backStack.lastOrNull() ?: return@TopAppBar
                when (current) {
                    is Page.Home -> {
                        Text(CelestiaString("Settings", ""))
                    }
                    is Page.Common -> {
                        Text(current.item.name)
                    }
                    is Page.CurrentTime -> {
                        Text(CelestiaString("Current Time", ""))
                    }
                    is Page.RenderInfo -> {
                        Text(CelestiaString("Render Info", "Information about renderer"))
                    }
                    is Page.RefreshRate -> {
                        Text(CelestiaString("Frame Rate", "Frame rate of simulation"))
                    }
                    is Page.About -> {
                        Text(CelestiaString("About", "About Celestia"))
                    }
                    is Page.DataLocation -> {
                        Text(CelestiaString("Data Location", "Title for celestia.cfg, data location setting"))
                    }
                    is Page.Language -> {
                        Text(CelestiaString("Language", "Display language setting"))
                    }
                    is Page.Font -> {
                        Text(CelestiaString("Font", ""))
                    }
                    is Page.Toolbar -> {
                        Text(CelestiaString("Toolbar", "Toolbar customization entry in Settings"))
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
                        SettingsHomeScreen(paddingValues = paddingValues) { item ->
                            when (item) {
                                is SettingsCommonItem -> {
                                    viewModel.backStack.add(Page.Common(item))
                                }
                                is SettingsCurrentTimeItem -> {
                                    viewModel.backStack.add(Page.CurrentTime)
                                }
                                is SettingsRenderInfoItem -> {
                                    viewModel.backStack.add(Page.RenderInfo)
                                }
                                is SettingsRefreshRateItem -> {
                                    viewModel.backStack.add(Page.RefreshRate)
                                }
                                is SettingsAboutItem -> {
                                    viewModel.backStack.add(Page.About)
                                }
                                is SettingsDataLocationItem -> {
                                    viewModel.backStack.add(Page.DataLocation)
                                }
                                is SettingsLanguageItem -> {
                                    viewModel.backStack.add(Page.Language)
                                }
                                is SettingsFontItem -> {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                                        viewModel.backStack.add(Page.Font)
                                }
                                is SettingsToolbarItem -> {
                                    viewModel.backStack.add(Page.Toolbar)
                                }
                                else -> {
                                    throw RuntimeException("SettingsFragment cannot handle item $item")
                                }
                            }
                        }
                    }
                    is Page.Common -> NavEntry(route) {
                        SettingsEntryScreen(item = route.item, paddingValues = paddingValues, linkClicked = linkClicked)
                    }
                    is Page.CurrentTime -> NavEntry(route) {
                        TimeSettingsScreen(paddingValues)
                    }
                    is Page.RenderInfo -> NavEntry(route) {
                        RenderInfoScreen(paddingValues)
                    }
                    is Page.RefreshRate -> NavEntry(route) {
                        RefreshRateSettingsScreen(paddingValues = paddingValues, providePreferredDisplay = providePreferredDisplay, refreshRateChanged = refreshRateChanged)
                    }
                    is Page.About -> NavEntry(route) {
                        AboutScreen(paddingValues, linkClicked = linkClicked)
                    }
                    is Page.DataLocation -> NavEntry(route) {
                        DataLocationSettingsScreen(paddingValues)
                    }
                    is Page.Language -> NavEntry(route) {
                        LanguageSettingsScreen(paddingValues)
                    }
                    is Page.Font -> NavEntry(route) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            SubscriptionBacking(paddingValues = paddingValues, openSubscriptionManagement = openSubscriptionManagement) {
                                FontSettingsScreen(it)
                            }
                        }
                    }
                    is Page.Toolbar -> NavEntry(route) {
                        SubscriptionBacking(paddingValues = paddingValues, openSubscriptionManagement = openSubscriptionManagement) {
                            ToolbarSettingsScreen(it)
                        }
                    }
                }
            }
        )
    }
}

@AndroidEntryPoint
class SettingsFragment: Fragment() {
    interface Listener {
        fun settingsLinkClicked(link: String, localizable: Boolean)
        fun settingsProvidePreferredDisplay(): Display?
        fun settingsRefreshRateChanged(frameRateOption: Int)
        fun settingsOpenSubscriptionManagement()
    }

    private var listener: Listener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement BrowserFragment.Listener")
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
                    Settings(
                        linkClicked = { link, localizable ->
                            listener?.settingsLinkClicked(link, localizable)
                        },
                        providePreferredDisplay = {
                            listener?.settingsProvidePreferredDisplay()
                        },
                        refreshRateChanged = {
                            listener?.settingsRefreshRateChanged(it)
                        },
                        openSubscriptionManagement = {
                            listener?.settingsOpenSubscriptionManagement()
                        }
                    )
                }
            }
        }
    }

    companion object {
        fun newInstance() = SettingsFragment()
    }
}
