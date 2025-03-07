/*
 * CommonWebScreen.kt
 *
 * Copyright (C) 2025-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.resource

import android.net.Uri
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
import space.celestia.mobilecelestia.compose.rememberWebViewState
import space.celestia.mobilecelestia.help.HelpAction
import space.celestia.mobilecelestia.help.HelpScreen
import space.celestia.mobilecelestia.resource.viewmodel.WebViewModel

@Composable
fun CommonWebScreen(uri: Uri, matchingQueryKeys: List<String>?, filterURL: Boolean, onExternalWebLinkClicked:(String) -> Unit, onShareURL:(String, String) -> Unit, onOpenSubscriptionPage:() -> Unit, onReceivedACK:(String) -> Unit) {
    val scope = rememberCoroutineScope()
    val viewModel: WebViewModel = hiltViewModel()
    val webViewState = rememberWebViewState(url = uri.toString(), matchingQueryKeys = matchingQueryKeys, filterURL = filterURL)
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