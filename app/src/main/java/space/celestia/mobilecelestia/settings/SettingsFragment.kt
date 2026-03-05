// SettingsFragment.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.settings

import android.content.Context
import android.os.Bundle
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import space.celestia.celestiaui.compose.Mdc3Theme
import space.celestia.celestiaui.settings.Settings

@AndroidEntryPoint
class SettingsFragment: Fragment() {
    interface Listener {
        fun settingsLinkClicked(link: String, localizable: Boolean)
        fun settingsProvidePreferredDisplay(): Display?
        fun settingsRefreshRateChanged(frameRateOption: Int)
        fun settingsOpenSubscriptionManagement()
    }

    private var listener: Listener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement SettingsFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Mdc3Theme {
                    Settings(
                        linkClicked = { link, localizable ->
                            listener?.settingsLinkClicked(link, localizable)
                        },
                        providePreferredDisplay = {
                            listener?.settingsProvidePreferredDisplay()
                        },
                        refreshRateChanged = {
                            listener?.settingsRefreshRateChanged(it)
                        },
                        openSubscriptionManagement = {
                            listener?.settingsOpenSubscriptionManagement()
                        }
                    )
                }
            }
        }
    }

    companion object {
        fun newInstance() = SettingsFragment()
    }
}
