/*
 * SubsystemBrowserFragment.kt
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import space.celestia.celestia.BrowserItem
import space.celestia.celestia.Selection
import space.celestia.mobilecelestia.browser.viewmodel.SubsystemBrowserViewModel
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.info.model.InfoActionItem
import java.net.URL

@AndroidEntryPoint
class SubsystemBrowserFragment : Fragment() {
    private var listener: Listener? = null
    private lateinit var root: Selection

    interface Listener {
        fun onBrowserAddonCategoryRequested(categoryInfo: BrowserPredefinedItem.CategoryInfo)
        fun onInfoActionSelected(action: InfoActionItem, item: Selection)
        fun onInfoLinkMetaDataClicked(url: URL)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            root = requireNotNull(BundleCompat.getParcelable(it, ARG_ROOT, Selection::class.java))
        }
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
            throw RuntimeException("$context must implement SubsystemBrowserFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainScreen() {
        val viewModel: SubsystemBrowserViewModel = hiltViewModel()
        val item by remember { mutableStateOf(BrowserItem(viewModel.appCore.simulation.universe.getNameForSelection(root), null, requireNotNull(root.`object`), viewModel.appCore.simulation.universe)) }
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

        var title by remember { mutableStateOf("") }
        var canPop by remember { mutableStateOf(false) }

        val navController = rememberNavController()

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
            BrowserNavigationScreen(
                root = Browser.Item("${item.hashCode()}"),
                rootItem = item,
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

    companion object {
        private const val ARG_ROOT = "root"

        @JvmStatic
        fun newInstance(selection: Selection) =
            SubsystemBrowserFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_ROOT, selection)
                }
            }
    }
}
