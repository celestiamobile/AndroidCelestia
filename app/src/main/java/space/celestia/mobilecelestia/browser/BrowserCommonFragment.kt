/*
 * BrowserCommonFragment.kt
 *
 * Copyright (C) 2023-present, Celestia Development Team
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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import space.celestia.celestia.BrowserItem
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.compose.Header
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.compose.TextRow
import space.celestia.mobilecelestia.utils.CelestiaString

class BrowserCommonFragment : NavigationFragment.SubFragment() {
    private var listener: Listener? = null
    private var browserItem: BrowserItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            val path = it.getString(ARG_PATH)!!
            val rootPath = it.getString(ARG_ROOT)
            browserItem = if (rootPath != null) SubsystemBrowserFragment.browserMap[rootPath]!![path] else BrowserFragment.browserMap[path]
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = browserItem?.alternativeName ?: browserItem?.name ?: ""
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement BrowserCommonFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    @Composable
    private fun MainScreen() {
        val item = browserItem ?: return

        val systemPadding = WindowInsets.systemBars.asPaddingValues()
        val direction = LocalLayoutDirection.current
        val contentPadding = PaddingValues(
            start = systemPadding.calculateStartPadding(direction),
            top = dimensionResource(id = R.dimen.list_spacing_short) + systemPadding.calculateTopPadding(),
            end = systemPadding.calculateEndPadding(direction),
            bottom = dimensionResource(id = R.dimen.list_spacing_tall) + systemPadding.calculateBottomPadding(),
        )

        LazyColumn(
            contentPadding = contentPadding,
            modifier = Modifier
                .nestedScroll(rememberNestedScrollInteropConnection())
                .background(color = MaterialTheme.colorScheme.background)
        ) {
            var hasMainObject = false
            if (item.`object` != null) {
                item {
                    TextRow(primaryText = item.name, modifier = Modifier.clickable {
                        listener?.onBrowserItemSelected(BrowserUIItem(item, true))
                    })
                }
                hasMainObject = true
            }

            if (!item.children.isEmpty()) {
                if (hasMainObject) {
                    item {
                        Header(text = CelestiaString("Subsystem", ""))
                    }
                }

                items(item.children) { item ->
                    TextRow(primaryText = item.name, accessoryResource = if (!item.children.isEmpty()) R.drawable.accessory_full_disclosure else 0, modifier = Modifier.clickable {
                        listener?.onBrowserItemSelected(BrowserUIItem(item, item.children.isEmpty()))
                    })
                }
            }
        }
    }

    interface Listener {
        fun onBrowserItemSelected(item: BrowserUIItem)
    }

    companion object {
        private const val ARG_PATH = "path"
        private const val ARG_ROOT = "root"

        @JvmStatic
        fun newInstance(path: String, rootPath: String? = null) =
            BrowserCommonFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PATH, path)
                    putString(ARG_ROOT, rootPath)
                }
            }
    }
}
