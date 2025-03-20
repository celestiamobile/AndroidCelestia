/*
 * BrowserScreen.kt
 *
 * Copyright (C) 2025-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.browser

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import space.celestia.celestia.Selection
import space.celestia.mobilecelestia.browser.viewmodel.BrowserNavigationViewModel
import space.celestia.mobilecelestia.browser.viewmodel.BrowserViewModel
import space.celestia.mobilecelestia.info.model.InfoActionItem
import space.celestia.mobilecelestia.utils.CelestiaString
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(onBrowserAddonCategoryRequested: (categoryInfo: BrowserPredefinedItem.CategoryInfo) -> Unit, onInfoActionSelected: (InfoActionItem, Selection) -> Unit, onInfoLinkMetaDataClicked: (URL) -> Unit) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var title by remember { mutableStateOf("") }
    var canPop by remember { mutableStateOf(false) }
    var loaded by remember { mutableStateOf(false) }

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
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
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
    }, bottomBar = {
        NavigationBar {
            viewModel.tabData.forEachIndexed { index, tab ->
                NavigationBarItem(
                    icon = {
                        Icon(painter = painterResource(id = tab.tab.iconResource), contentDescription = tab.tab.contentDescription)
                    },
                    label = {
                        Text(tab.item.alternativeName ?: tab.item.name)
                    },
                    selected = selectedTabIndex == index,
                    onClick = {
                        selectedTabIndex = index
                        val previousRoot = navigationModel.root
                        navigationModel.root = tab.root
                        navigationModel.currentPath = navigationModel.currentPathMap[tab.root] ?: tab.root.path
                        navigationModel.browserMap[tab.root.path] = tab.item
                        navController.navigate(tab.root) {
                            popUpTo(previousRoot) {
                                inclusive = true
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
        BrowserNavigationScreen(
            root = viewModel.tabData[0].root,
            rootItem = viewModel.tabData[0].item,
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