// BrowserFragment.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.browser

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import space.celestia.celestia.Selection
import space.celestia.celestiaui.browser.Browser
import space.celestia.celestiaui.browser.viewmodel.BrowserPredefinedItem
import space.celestia.celestiaui.compose.Mdc3Theme

@AndroidEntryPoint
class BrowserFragment : Fragment() {
    interface Listener {
        fun browserAddonCategoryRequested(addonCategory: BrowserPredefinedItem.CategoryInfo)
        fun browserLinkClicked(link: String)
        fun browserRequestSubsystem(selection: Selection)
        fun browserRequestOpenSubscriptionManagement()
        fun browserRequestOpenRelatedAddons(objectPath: String)
    }

    private var listener: Listener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement BrowserFragment.Listener")
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
                    Browser(linkClicked = {
                        listener?.browserLinkClicked(it)
                    }, openSubsystem = { selection ->
                        listener?.browserRequestSubsystem(selection)
                    }, addonCategoryRequested = {
                        listener?.browserAddonCategoryRequested(it)
                    }, openRelatedAddons = {
                        listener?.browserRequestOpenRelatedAddons(it)
                    }, openSubscriptionManagement = {
                        listener?.browserRequestOpenSubscriptionManagement()
                    })
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): BrowserFragment = BrowserFragment()
    }
}
