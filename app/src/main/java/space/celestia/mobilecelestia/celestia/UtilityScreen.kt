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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import space.celestia.celestia.Selection
import space.celestia.mobilecelestia.browser.BrowserPredefinedItem
import space.celestia.mobilecelestia.browser.BrowserScreen
import space.celestia.mobilecelestia.browser.SubsystemBrowserScreen
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
    saveFavorites: () -> Unit
) {
    val viewModel: UtilityViewModel = hiltViewModel()
    when (val current = viewModel.current) {
        Utility.AddonManagement -> {
            AddonManagementScreen(onOpenAddonDownload = onOpenAddonDownload, onShareAddon = onShareAddon, onExternalWebLinkClicked = onExternalWebLinkClicked, onShareURL = onShareURL, onOpenSubscriptionPage = onOpenSubscriptionPage, onReceivedACK = onReceivedACK)
        }
        Utility.CurrentTime -> {
            CurrentTimeScreen()
        }
        Utility.Settings -> {
            SettingsScreen(onRefreshRateChanged = onRefreshRateChanged, onAboutURLSelected = onAboutURLSelected, requestOpenSubscriptionManagement = requestOpenSubscriptionManagement)
        }
        is Utility.Addon -> {
            AddonNavigationScreen(item = current.item, onShareAddon = onShareAddon, onExternalWebLinkClicked = onExternalWebLinkClicked, onShareURL = onShareURL, onOpenSubscriptionPage = onOpenSubscriptionPage, onReceivedACK = onReceivedACK)
        }
        Utility.CameraControl -> {
            CameraControlMainScreen(onObserverModeLearnMoreClicked = onObserverModeLearnMoreClicked)
        }
        Utility.Help -> {
            NewHelpScreen(onShareAddon = onShareAddon, onExternalWebLinkClicked = onExternalWebLinkClicked, onShareURL = onShareURL, onOpenSubscriptionPage = onOpenSubscriptionPage, onReceivedACK = onReceivedACK)
        }
        Utility.EventFinder -> {
            EventFinderScreen()
        }
        Utility.Search -> {
            SearchScreen(onInfoLinkMetaDataClicked = onInfoLinkMetaDataClicked, onInfoActionSelected = onInfoActionSelected)
        }
        Utility.Browser -> {
            BrowserScreen(onBrowserAddonCategoryRequested = onBrowserAddonCategoryRequested, onInfoActionSelected = onInfoActionSelected, onInfoLinkMetaDataClicked = onInfoLinkMetaDataClicked)
        }
        Utility.Favorites -> {
            FavoriteScreen(shareItem = shareItem, saveFavorites = saveFavorites)
        }
        Utility.InAppPurchase -> {
            SubscriptionManagerScreen()
        }
        is Utility.GoTo -> {
            GoToMainScreen(goToData = current.goToData, selection = current.selection, onGoToObject = onGoToObject)
        }
        is Utility.Info -> {
            val selection = current.selection
            InfoScreen(selection = selection, showTitle = true, onInfoLinkMetaDataClicked = onInfoLinkMetaDataClicked, onInfoActionSelected = onInfoActionSelected, paddingValues = WindowInsets.systemBars.asPaddingValues())
        }
        is Utility.SubsystemBrowser -> {
            SubsystemBrowserScreen(item = current.selection, onBrowserAddonCategoryRequested = onBrowserAddonCategoryRequested, onInfoActionSelected = onInfoActionSelected, onInfoLinkMetaDataClicked = onInfoLinkMetaDataClicked)
        }
        is Utility.Web -> {
            CommonWebScreen(
                uri = current.uri,
                matchingQueryKeys = current.matchingQueryKeys,
                filterURL = current.filterURL,
                onExternalWebLinkClicked = onExternalWebLinkClicked,
                onShareURL = onShareURL,
                onOpenSubscriptionPage = onOpenSubscriptionPage,
                onReceivedACK = onReceivedACK
            )
        }
        is Utility.WebNavigation -> {
            CommonWebNavigationScreen(
                uri = current.uri,
                onShareAddon = onShareAddon,
                onExternalWebLinkClicked = onExternalWebLinkClicked,
                onShareURL = onShareURL,
                onOpenSubscriptionPage = onOpenSubscriptionPage,
                onReceivedACK = onReceivedACK
            )
        }
        null -> { Box(modifier = Modifier.fillMaxSize()) }
    }
}