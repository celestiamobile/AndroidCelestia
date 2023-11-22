/*
 * InstalledAddonListFragment.kt
 *
 * Copyright (C) 2023-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.resource

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import dagger.hilt.android.AndroidEntryPoint
import space.celestia.celestiafoundation.resource.model.ResourceItem
import space.celestia.celestiafoundation.resource.model.ResourceManager
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.compose.TextRow
import space.celestia.mobilecelestia.utils.CelestiaString
import javax.inject.Inject

@AndroidEntryPoint
class InstalledAddonListFragment: NavigationFragment.SubFragment() {
    @Inject
    lateinit var resourceManager: ResourceManager

    private var installedAddons = mutableStateListOf<ResourceItem>()
    private var needRefresh = mutableStateOf(true)

    private var listener: Listener? = null

    interface Listener {
        fun onInstalledAddonSelected(addon: ResourceItem)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        needRefresh.value = true
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

        title = CelestiaString("Installed", "")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement InstalledAddonListFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    @Composable
    private fun MainScreen() {
        if (needRefresh.value) {
            LaunchedEffect(true) {
                val addons = resourceManager.installedResourcesAsync()
                installedAddons.clear()
                installedAddons.addAll(addons)
                needRefresh.value = false
            }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
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
                items(installedAddons) {
                    TextRow(primaryText = it.name, accessoryResource = R.drawable.accessory_full_disclosure, modifier = Modifier.clickable {
                        listener?.onInstalledAddonSelected(it)
                    })
                }
            }
        }
    }

    companion object {
        fun newInstance() = InstalledAddonListFragment()
    }
}