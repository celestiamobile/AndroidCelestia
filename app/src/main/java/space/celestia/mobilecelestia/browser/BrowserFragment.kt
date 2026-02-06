// BrowserFragment.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.browser

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.ui.Alignment
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
import dagger.hilt.android.AndroidEntryPoint
import space.celestia.celestia.Selection
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.browser.viewmodel.BrowserPredefinedItem
import space.celestia.mobilecelestia.browser.viewmodel.BrowserViewModel
import space.celestia.mobilecelestia.browser.viewmodel.Page
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.compose.SimpleAlertDialog
import space.celestia.mobilecelestia.info.InfoScreen
import space.celestia.mobilecelestia.info.model.InfoActionItem
import space.celestia.mobilecelestia.utils.CelestiaString
import java.net.URL

sealed class BrowserAlert {
    data object ObjectNotFound: BrowserAlert()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Browser(linkClicked: (String) -> Unit, openSubsystem: (Selection) -> Unit, addonCategoryRequested: (BrowserPredefinedItem.CategoryInfo) -> Unit) {
    val viewModel: BrowserViewModel = hiltViewModel()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    var alert by remember { mutableStateOf<BrowserAlert?>(null)  }
    LaunchedEffect(Unit) {
        if (viewModel.tabs.isEmpty()) {
            viewModel.loadRootBrowserItems()
        }
    }

    if (viewModel.tabs.isEmpty() || viewModel.selectedTabIndex.intValue >= viewModel.tabs.size || viewModel.selectedTabIndex.intValue >= viewModel.backStacks.size) {
        Box(modifier = Modifier.fillMaxSize().systemBarsPadding(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val backStack = viewModel.backStacks[viewModel.selectedTabIndex.intValue]
        if (backStack.isEmpty()) return
        Scaffold(
            topBar = {
                TopAppBar(title = {
                    val current = backStack.lastOrNull() ?: return@TopAppBar
                    when (current) {
                        is Page.Item -> {
                            Text(current.item.alternativeName ?: current.item.name)
                        }
                        is Page.Info -> {
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
            bottomBar = {
                NavigationBar(
                    windowInsets = NavigationBarDefaults.windowInsets.only(
                        WindowInsetsSides.Bottom
                    )
                ) {
                    viewModel.tabs.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            selected = index == viewModel.selectedTabIndex.intValue,
                            onClick = {
                                viewModel.selectedTabIndex.intValue = index
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(tab.iconResource),
                                    contentDescription = tab.rootItem.alternativeName ?: tab.rootItem.name
                                )
                            },
                            label = {
                                Text(tab.rootItem.alternativeName ?: tab.rootItem.name)
                            }
                        )
                    }
                }
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
                                BrowserEntry(item = route.item, paddingValues = paddingValues, itemSelected = { item, isLeaf ->
                                    if (isLeaf) {
                                        val obj = item.`object`
                                        if (obj != null) {
                                            backStack.add(Page.Info(Selection(obj)))
                                        } else {
                                            alert = BrowserAlert.ObjectNotFound
                                        }
                                    } else {
                                        backStack.add(Page.Item(item))
                                    }
                                }, addonCategoryRequested = {
                                    addonCategoryRequested(it)
                                })
                            }
                        }
                        is Page.Info -> NavEntry(route) {
                            InfoScreen(selection = route.info, showTitle = false, linkClicked = {
                                linkClicked(it)
                            }, openSubsystem = {
                                openSubsystem(route.info)
                            }, paddingValues = paddingValues)
                        }
                    }
                }
            )
        }
    }

    alert?.let { content ->
        when (content) {
            is BrowserAlert.ObjectNotFound -> {
                SimpleAlertDialog(onDismissRequest = {
                    alert = null
                }, onConfirm = {
                    alert = null
                }, title = CelestiaString("Object not found", ""))
            }
        }
    }
}

@AndroidEntryPoint
class BrowserFragment : Fragment() {
    interface Listener {
        fun browserAddonCategoryRequested(addonCategory: BrowserPredefinedItem.CategoryInfo)
        fun browserLinkClicked(link: String)
        fun browserRequestSubsystem(selection: Selection)
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
                    Browser(linkClicked = {
                        listener?.browserLinkClicked(it)
                    }, openSubsystem = { selection ->
                        listener?.browserRequestSubsystem(selection)
                    }, addonCategoryRequested = {
                        listener?.browserAddonCategoryRequested(it)
                    })
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): BrowserFragment = BrowserFragment()
    }
}