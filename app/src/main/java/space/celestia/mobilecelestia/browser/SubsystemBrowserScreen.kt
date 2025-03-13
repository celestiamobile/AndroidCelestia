/*
 * SubsystemBrowserScreen.kt
 *
 * Copyright (C) 2025-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.browser

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import space.celestia.celestia.BrowserItem
import space.celestia.celestia.Selection
import space.celestia.mobilecelestia.browser.viewmodel.SubsystemBrowserViewModel
import space.celestia.mobilecelestia.info.model.InfoActionItem
import space.celestia.mobilecelestia.utils.CelestiaString
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubsystemBrowserScreen(item: Selection, onBrowserAddonCategoryRequested: (categoryInfo: BrowserPredefinedItem.CategoryInfo) -> Unit, onInfoActionSelected: (InfoActionItem, Selection) -> Unit, onInfoLinkMetaDataClicked: (URL) -> Unit) {
    val viewModel: SubsystemBrowserViewModel = hiltViewModel()
    val browserItem by remember { mutableStateOf(BrowserItem(viewModel.appCore.simulation.universe.getNameForSelection(item), null, requireNotNull(item.`object`), viewModel.appCore.simulation.universe)) }
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
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = CelestiaString("Go Back", "Button to go back to the previous page"))
                }
            }
        }, scrollBehavior = scrollBehavior)
    }) { paddingValues ->
        BrowserNavigationScreen(
            root = Browser.Item("${item.hashCode()}"),
            rootItem = browserItem,
            navController = navController,
            addonCategoryRequested = {
                onBrowserAddonCategoryRequested(it)
            },
            linkHandler = {
                onInfoLinkMetaDataClicked(it)
            },
            actionHandler = { action, selection ->
                onInfoActionSelected(action, selection)
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