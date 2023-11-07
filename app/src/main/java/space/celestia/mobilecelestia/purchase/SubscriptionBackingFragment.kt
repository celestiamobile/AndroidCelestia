/*
 * SubscriptionBackingFragment.kt
 *
 * Copyright (C) 2023-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.purchase

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.hilt.navigation.compose.hiltViewModel
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.compose.EmptyHint
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.settings.viewmodel.SettingsViewModel
import space.celestia.mobilecelestia.utils.CelestiaString

abstract class SubscriptionBackingFragment : NavigationFragment.SubFragment() {
    private var listener: Listener? = null

    interface  Listener {
        fun requestOpenSubscriptionManagement()
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

    @Composable
    private fun MainScreen() {
        val viewModel: SettingsViewModel = hiltViewModel()
        if (!viewModel.purchaseManager.canUseInAppPurchase()) {
            Box(modifier = Modifier.fillMaxSize().systemBarsPadding(), contentAlignment = Alignment.Center) {
                EmptyHint(text = CelestiaString("This feature is not supported.", ""))
            }
        } else if (viewModel.purchaseManager.purchaseToken() == null) {
            Box(modifier = Modifier.fillMaxSize().systemBarsPadding(), contentAlignment = Alignment.Center) {
                EmptyHint(text = CelestiaString("This feature is only available to Celestia PLUS users.", ""), actionText = CelestiaString("Get Celestia PLUS", "")) {
                    listener?.requestOpenSubscriptionManagement()
                }
            }
        } else {
            MainView()
        }
    }

    @Composable
    abstract fun MainView()

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement SubscriptionBackingFragment.Listener")
        }
    }
}