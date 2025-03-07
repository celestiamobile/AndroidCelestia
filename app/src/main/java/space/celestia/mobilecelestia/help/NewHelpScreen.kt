/*
 * NewHelpScreen.kt
 *
 * Copyright (C) 2025-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.help

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import space.celestia.celestia.AppCore
import space.celestia.celestiafoundation.utils.URLHelper
import space.celestia.mobilecelestia.compose.rememberWebViewState
import space.celestia.mobilecelestia.resource.SingleWebScreen
import space.celestia.mobilecelestia.resource.viewmodel.WebViewModel

@Composable
fun NewHelpScreen(onShareAddon:(String, String) -> Unit, onExternalWebLinkClicked:(String) -> Unit, onShareURL:(String, String) -> Unit, onOpenSubscriptionPage:() -> Unit, onReceivedACK:(String) -> Unit) {
    val scope = rememberCoroutineScope()
    val viewModel: WebViewModel = hiltViewModel()
    val webViewState = rememberWebViewState(url = URLHelper.buildInAppGuideShortURI("/help/welcome", AppCore.getLanguage(), false).toString(), matchingQueryKeys = listOf("guide"), filterURL = true)
    SingleWebScreen(webViewState = webViewState, fallback = {
        HelpScreen(helpActionSelected = {
            when (it) {
                HelpAction.RunDemo -> {
                    scope.launch(viewModel.executor.asCoroutineDispatcher()) { viewModel.appCore.runDemo() }
                }
            }
        }, helpURLSelected = {
            onExternalWebLinkClicked(it)
        }, paddingValues = WindowInsets.systemBars.asPaddingValues())
    }, openSubscriptionPageHandler = {
        onOpenSubscriptionPage()
    }, openExternalWebLink = {
        onExternalWebLinkClicked(it)
    }, shareURLHandler = { title, url ->
        onShareURL(title, url)
    }, receivedACKHandler = {
        onReceivedACK(it)
    }, paddingValues = WindowInsets.systemBars.asPaddingValues(), modifier = Modifier.fillMaxSize())
}