/*
 * InfoFragment.kt
 *
 * Copyright (C) 2023-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.info

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import space.celestia.celestia.Selection
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.info.model.InfoActionItem
import java.net.URL

@AndroidEntryPoint
class InfoFragment : Fragment() {
    private var listener: Listener? = null
    private lateinit var selection: Selection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            selection = BundleCompat.getParcelable(it, ARG_OBJECT, Selection::class.java)!!
        }
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
                    InfoScreen(selection = selection, showTitle = true, linkHandler = {
                        listener?.onInfoLinkMetaDataClicked(it)
                    }, actionHandler = { item, selection ->
                        listener?.onInfoActionSelected(item, selection)
                    }, paddingValues = WindowInsets.systemBars.asPaddingValues())
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement InfoFragment.nListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface Listener {
        fun onInfoActionSelected(action: InfoActionItem, item: Selection)
        fun onInfoLinkMetaDataClicked(url: URL)
    }

    companion object {
        const val ARG_OBJECT = "object"

        @JvmStatic
        fun newInstance(selection: Selection) =
            InfoFragment().apply {
                arguments = Bundle().apply {
                    this.putParcelable(ARG_OBJECT, selection)
                }
            }
    }
}
