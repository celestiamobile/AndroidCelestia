// InstalledAddonListScreen.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.resource

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.launch
import space.celestia.celestiafoundation.resource.model.ResourceItem
import space.celestia.celestiafoundation.resource.model.ResourceManager
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.EmptyHint
import space.celestia.mobilecelestia.compose.TextRow
import space.celestia.mobilecelestia.resource.model.AddonUpdateManager
import space.celestia.mobilecelestia.resource.viewmodel.AddonManagerViewModel
import space.celestia.mobilecelestia.utils.CelestiaString

@Composable
fun InstalledAddonListScreen(paddingValues: PaddingValues, requestOpenAddonDownload: () -> Unit, requestOpenInstalledAddon: (ResourceItem) -> Unit) {
    val viewModel: AddonManagerViewModel = hiltViewModel()
    val installedAddons = remember { mutableStateListOf<ResourceItem>() }
    var isLoading by remember { mutableStateOf(false) }

    suspend fun reloadAddonList() {
        isLoading = true
        val addons = viewModel.resourceManager.installedResourcesAsync()
        installedAddons.clear()
        installedAddons.addAll(addons)
        isLoading = false
    }

    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        reloadAddonList()
    }

    val lifeCycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifeCycleOwner) {
        val observer = object: ResourceManager.Listener {
            override fun onProgressUpdate(identifier: String, progress: Float) {}
            override fun onFileDownloaded(identifier: String) {}
            override fun onResourceFetchError(identifier: String, errorContext: ResourceManager.ErrorContext) {}

            override fun onFileUnzipped(identifier: String) {
                scope.launch {
                    reloadAddonList()
                }
            }

            override fun onAddonUninstalled(identifier: String) {
                scope.launch {
                    reloadAddonList()
                }
            }
        }
        viewModel.resourceManager.addListener(observer)
        onDispose {
            viewModel.resourceManager.removeListener(observer)
        }
    }

    if (installedAddons.isEmpty()) {
        if (isLoading) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues), contentAlignment = Alignment.Center) {
                EmptyHint(text = CelestiaString("Enhance Celestia with online add-ons", ""), actionText = CelestiaString("Get Add-ons", "Open webpage for downloading add-ons"), actionHandler = {
                    requestOpenAddonDownload()
                })
            }
        }
    } else {
        val direction = LocalLayoutDirection.current
        val contentPadding = PaddingValues(
            start = paddingValues.calculateStartPadding(direction),
            top = dimensionResource(id = R.dimen.list_spacing_short) + paddingValues.calculateTopPadding(),
            end = paddingValues.calculateEndPadding(direction),
            bottom = dimensionResource(id = R.dimen.list_spacing_tall) + paddingValues.calculateBottomPadding(),
        )
        LazyColumn(
            contentPadding = contentPadding,
            modifier = Modifier
                .nestedScroll(rememberNestedScrollInteropConnection())
                .background(color = MaterialTheme.colorScheme.background)
        ) {
            items(installedAddons) {
                TextRow(primaryText = it.name, accessoryResource = R.drawable.accessory_full_disclosure, modifier = Modifier.clickable {
                    requestOpenInstalledAddon(it)
                })
            }
        }
    }
}