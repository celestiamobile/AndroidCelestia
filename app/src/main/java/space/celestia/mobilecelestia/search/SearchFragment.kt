/*
 * SearchFragment.kt
 *
 * Copyright (C) 2023-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.search

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
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.info.model.InfoActionItem
import java.net.URL

@AndroidEntryPoint
class SearchFragment : Fragment() {
    private var listener: Listener? = null

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
                    SearchScreen(objectNotFoundHandler = {
                        listener?.onObjectNotFound()
                    }, actionHandler = { action, selection ->
                        listener?.onInfoActionSelected(action, selection)
                    }, linkHandler = {
                        listener?.onInfoLinkMetaDataClicked(it)
                    })
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement SearchFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface Listener {
        fun onObjectNotFound()
        fun onInfoActionSelected(action: InfoActionItem, item: Selection)
        fun onInfoLinkMetaDataClicked(url: URL)
    }

    companion object {
        @JvmStatic
        fun newInstance() = SearchFragment()
    }
}
