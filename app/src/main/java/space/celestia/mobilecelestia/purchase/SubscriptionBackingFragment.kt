// SubscriptionBackingFragment.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.purchase

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.compose.EmptyHint
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.settings.viewmodel.SettingsViewModel
import space.celestia.mobilecelestia.utils.CelestiaString


@Composable
fun SubscriptionBacking(paddingValues: PaddingValues, openSubscriptionManagement: () -> Unit, content: @Composable (PaddingValues) -> Unit) {
    val viewModel: SettingsViewModel = hiltViewModel()
    if (!viewModel.purchaseManager.canUseInAppPurchase()) {
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
            EmptyHint(text = CelestiaString("This feature is not supported.", ""))
        }
    } else if (viewModel.purchaseManager.purchaseToken() == null) {
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
            EmptyHint(text = CelestiaString("This feature is only available to Celestia PLUS users.", ""), actionText = CelestiaString("Get Celestia PLUS", "")) {
                openSubscriptionManagement()
            }
        }
    } else {
        content(paddingValues)
    }
}

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
                    SubscriptionBacking(paddingValues = WindowInsets.systemBars.asPaddingValues(), openSubscriptionManagement = {
                        listener?.requestOpenSubscriptionManagement()
                    }) {
                        MainView(it)
                    }
                }
            }
        }
    }

    @Composable
    abstract fun MainView(paddingValues: PaddingValues)

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