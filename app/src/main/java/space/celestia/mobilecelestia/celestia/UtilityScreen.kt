/*
 * UtilityScreen.kt
 *
 * Copyright (C) 2025-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.celestia

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.runningFold
import space.celestia.celestia.Selection
import space.celestia.mobilecelestia.browser.BrowserPredefinedItem
import space.celestia.mobilecelestia.browser.BrowserScreen
import space.celestia.mobilecelestia.browser.SubsystemBrowserScreen
import space.celestia.mobilecelestia.celestia.viewmodel.Context
import space.celestia.mobilecelestia.celestia.viewmodel.Utility
import space.celestia.mobilecelestia.celestia.viewmodel.UtilityViewModel
import space.celestia.mobilecelestia.control.CameraControlMainScreen
import space.celestia.mobilecelestia.eventfinder.EventFinderScreen
import space.celestia.mobilecelestia.favorite.FavoriteScreen
import space.celestia.mobilecelestia.favorite.viewmodel.Favorite
import space.celestia.mobilecelestia.help.NewHelpScreen
import space.celestia.mobilecelestia.info.InfoScreen
import space.celestia.mobilecelestia.info.model.InfoActionItem
import space.celestia.mobilecelestia.purchase.SubscriptionManagerScreen
import space.celestia.mobilecelestia.resource.AddonManagementScreen
import space.celestia.mobilecelestia.resource.AddonNavigationScreen
import space.celestia.mobilecelestia.resource.CommonWebNavigationScreen
import space.celestia.mobilecelestia.resource.CommonWebScreen
import space.celestia.mobilecelestia.search.SearchScreen
import space.celestia.mobilecelestia.settings.CurrentTimeScreen
import space.celestia.mobilecelestia.settings.SettingsScreen
import space.celestia.mobilecelestia.travel.GoToData
import space.celestia.mobilecelestia.travel.GoToMainScreen
import java.net.URL

data class History<T>(val previous: T?, val current: T)
fun <T> Flow<T>.runningHistory(): Flow<History<T>?> =
    runningFold(
        initial = null as (History<T>?),
        operation = { accumulator, new -> History(accumulator?.current, new) }
    )

@Composable
fun UtilityScreen(
    onRefreshRateChanged: (Int) -> Unit,
    onAboutURLSelected: (String, Boolean) -> Unit,
    requestOpenSubscriptionManagement: () -> Unit,
    onOpenAddonDownload: () -> Unit,
    onShareAddon:(String, String) -> Unit,
    onExternalWebLinkClicked:(String) -> Unit,
    onShareURL:(String, String) -> Unit,
    onOpenSubscriptionPage:() -> Unit,
    onReceivedACK:(String) -> Unit,
    onObserverModeLearnMoreClicked: (String) -> Unit,
    onInfoLinkMetaDataClicked: (URL) -> Unit,
    onInfoActionSelected: (InfoActionItem, Selection) -> Unit,
    onGoToObject:(GoToData, Selection) -> Unit,
    onBrowserAddonCategoryRequested: (categoryInfo: BrowserPredefinedItem.CategoryInfo) -> Unit,
    shareItem: (Favorite.Shareable.Object) -> Unit,
    saveFavorites: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: UtilityViewModel = hiltViewModel()

    val navController = rememberNavController()
    LaunchedEffect(Unit) {
        snapshotFlow { viewModel.current }
            .runningHistory()
            .collect { history ->
                if (history?.previous == null) return@collect
                navController.navigate(history.current) {
                    popUpTo(history.previous) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
    }

    NavHost(navController = navController, startDestination = Utility.Empty, modifier = modifier) {
        composable<Utility.Empty> {}
        composable<Utility.AddonManagement> {
            AddonManagementScreen(onOpenAddonDownload = onOpenAddonDownload, onShareAddon = onShareAddon, onExternalWebLinkClicked = onExternalWebLinkClicked, onShareURL = onShareURL, onOpenSubscriptionPage = onOpenSubscriptionPage, onReceivedACK = onReceivedACK)
        }
        composable<Utility.CurrentTime> {
            CurrentTimeScreen()
        }
        composable<Utility.Settings> {
            SettingsScreen(onRefreshRateChanged = onRefreshRateChanged, onAboutURLSelected = onAboutURLSelected, requestOpenSubscriptionManagement = requestOpenSubscriptionManagement)
        }
        composable<Utility.Addon> {
            val context = viewModel.context
            if (context is Context.Addon) {
                AddonNavigationScreen(
                    item = context.item,
                    onShareAddon = onShareAddon,
                    onExternalWebLinkClicked = onExternalWebLinkClicked,
                    onShareURL = onShareURL,
                    onOpenSubscriptionPage = onOpenSubscriptionPage,
                    onReceivedACK = onReceivedACK
                )
            }
        }
        composable<Utility.CameraControl> {
            CameraControlMainScreen(onObserverModeLearnMoreClicked = onObserverModeLearnMoreClicked)
        }
        composable<Utility.Help> {
            NewHelpScreen(onExternalWebLinkClicked = onExternalWebLinkClicked, onShareURL = onShareURL, onOpenSubscriptionPage = onOpenSubscriptionPage, onReceivedACK = onReceivedACK)
        }
        composable<Utility.EventFinder> {
            EventFinderScreen()
        }
        composable<Utility.Search> {
            SearchScreen(onInfoLinkMetaDataClicked = onInfoLinkMetaDataClicked, onInfoActionSelected = onInfoActionSelected)
        }
        composable<Utility.Browser> {
            BrowserScreen(onBrowserAddonCategoryRequested = onBrowserAddonCategoryRequested, onInfoActionSelected = onInfoActionSelected, onInfoLinkMetaDataClicked = onInfoLinkMetaDataClicked)
        }
        composable<Utility.Favorites> {
            FavoriteScreen(shareItem = shareItem, saveFavorites = saveFavorites)
        }
        composable<Utility.InAppPurchase> {
            SubscriptionManagerScreen()
        }
        composable<Utility.GoTo> {
            val context = viewModel.context
            if (context is Context.GoTo) {
                GoToMainScreen(
                    goToData = context.goToData,
                    selection = context.selection,
                    onGoToObject = onGoToObject
                )
            }
        }
        composable<Utility.Info> {
            val context = viewModel.context
            if (context is Context.Info) {
                InfoScreen(
                    selection = context.selection,
                    showTitle = true,
                    onInfoLinkMetaDataClicked = onInfoLinkMetaDataClicked,
                    onInfoActionSelected = onInfoActionSelected,
                    paddingValues = WindowInsets.systemBars.asPaddingValues()
                )
            }
        }
        composable<Utility.SubsystemBrowser> {
            val context = viewModel.context
            if (context is Context.SubsystemBrowser) {
                SubsystemBrowserScreen(
                    item = context.selection,
                    onBrowserAddonCategoryRequested = onBrowserAddonCategoryRequested,
                    onInfoActionSelected = onInfoActionSelected,
                    onInfoLinkMetaDataClicked = onInfoLinkMetaDataClicked
                )
            }
        }
        composable<Utility.Web> {
            val context = viewModel.context
            if (context is Context.Web) {
                CommonWebScreen(
                    uri = context.uri,
                    matchingQueryKeys = context.matchingQueryKeys,
                    filterURL = context.filterURL,
                    onExternalWebLinkClicked = onExternalWebLinkClicked,
                    onShareURL = onShareURL,
                    onOpenSubscriptionPage = onOpenSubscriptionPage,
                    onReceivedACK = onReceivedACK
                )
            }
        }
        composable<Utility.WebNavigation> {
            val context = viewModel.context
            if (context is Context.WebNavigation) {
                CommonWebNavigationScreen(
                    uri = context.uri,
                    onShareAddon = onShareAddon,
                    onExternalWebLinkClicked = onExternalWebLinkClicked,
                    onShareURL = onShareURL,
                    onOpenSubscriptionPage = onOpenSubscriptionPage,
                    onReceivedACK = onReceivedACK
                )
            }
        }
    }
}