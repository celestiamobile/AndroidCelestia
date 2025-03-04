/*
 * BrowserFragment.kt
 *
 * Copyright (C) 2025-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.browser

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import space.celestia.celestia.Selection
import space.celestia.mobilecelestia.browser.viewmodel.BrowserNavigationViewModel
import space.celestia.mobilecelestia.browser.viewmodel.BrowserViewModel
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.info.model.InfoActionItem
import java.net.URL

@AndroidEntryPoint
class BrowserFragment : Fragment() {
    private var listener: Listener? = null

    interface Listener {
        fun onBrowserAddonCategoryRequested(categoryInfo: BrowserPredefinedItem.CategoryInfo)
        fun onInfoActionSelected(action: InfoActionItem, item: Selection)
        fun onInfoLinkMetaDataClicked(url: URL)
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
                    MainScreen()
                }
            }
        }
    }

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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainScreen() {
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

        var title by remember { mutableStateOf("") }
        var canPop by remember { mutableStateOf(false) }
        var loaded by remember { mutableStateOf(false) }

        val viewModelStoreOwner = requireNotNull(LocalViewModelStoreOwner.current)
        val navigationModel: BrowserNavigationViewModel = hiltViewModel()
        val viewModel: BrowserViewModel = hiltViewModel()

        LaunchedEffect(Unit) {
            viewModel.loadBrowserTabs()
            loaded = true
        }

        if (!loaded) {
            Box(modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }

        val navController = rememberNavController()
        var selectedTabIndex by remember { mutableIntStateOf(0) }
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
        }, bottomBar = {
            NavigationBar {
                viewModel.tabData.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        icon = {
                            Icon(painter = painterResource(id = tab.tab.iconResource), contentDescription = "")
                        },
                        label = {
                            Text(tab.item.alternativeName ?: tab.item.name)
                        },
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedTabIndex = index
                            val path = "${tab.item.hashCode()}"
                            val previousRoot = navigationModel.rootPath
                            navigationModel.rootPath = path
                            navigationModel.currentPath = path
                            navigationModel.browserMap[path] = tab.item
                            navController.navigate(Browser(path)) {
                                popUpTo(Browser(previousRoot)) {
                                    inclusive = true
                                    // TODO: State restoration does not work
                                    // saveState = true
                                }
                                launchSingleTop = true
                                // restoreState = true
                            }
                        }
                    )
                }
            }
        }) { paddingValues ->
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides viewModelStoreOwner
            ) {
                BrowserNavigationScreen(
                    rootItem = viewModel.tabData[0].item,
                    navController = navController,
                    addonCategoryRequested = {
                        listener?.onBrowserAddonCategoryRequested(it)
                    },
                    linkHandler = {
                        listener?.onInfoLinkMetaDataClicked(it)
                    },
                    actionHandler = { action, selection ->
                        listener?.onInfoActionSelected(action, selection)
                    },
                    topBarStateChange = { newTitle, newCanPop ->
                        title = newTitle
                        canPop = newCanPop
                    },
                    paddingValues = paddingValues,
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                )
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): BrowserFragment {
            return BrowserFragment()
        }
    }
}