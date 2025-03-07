/*
 * CommonWebNavigationScreen.kt
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import space.celestia.mobilecelestia.compose.rememberSaveableWebViewState
import space.celestia.mobilecelestia.compose.rememberWebViewNavigator

@Composable
fun CommonWebNavigationScreen(uri: Uri, onShareAddon:(String, String) -> Unit, onExternalWebLinkClicked:(String) -> Unit, onShareURL:(String, String) -> Unit, onOpenSubscriptionPage:() -> Unit, onReceivedACK:(String) -> Unit) {
    val navigator = rememberWebViewNavigator()
    val webViewState = rememberSaveableWebViewState()
    LaunchedEffect(navigator) {
        val bundle = webViewState.viewState
        if (bundle == null) {
            // This is the first time load, so load the home page.
            navigator.loadUrl(uri.toString())
        }
    }

    WebNavigationScreen(
        webViewState = webViewState,
        navigator = navigator,
        openSubscriptionPageHandler = {
            onOpenSubscriptionPage()
        },
        openExternalWebLink = {
            onExternalWebLinkClicked(it)
        },
        shareURLHandler = { title, url ->
            onShareURL(title, url)
        },
        receivedACKHandler = {
            onReceivedACK(it)
        },
        shareAddon = { title, id ->
            onShareAddon(title, id)
        }
    )
}