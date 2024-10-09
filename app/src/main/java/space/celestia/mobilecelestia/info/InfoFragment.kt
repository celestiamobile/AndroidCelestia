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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.BundleCompat
import dagger.hilt.android.AndroidEntryPoint
import space.celestia.celestia.AppCore
import space.celestia.celestia.Selection
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.info.model.InfoActionItem
import java.net.URL
import javax.inject.Inject

@AndroidEntryPoint
class InfoFragment : NavigationFragment.SubFragment() {
    private var listener: Listener? = null
    private lateinit var selection: Selection
    private var embeddedInNavigation = false

    @Inject
    lateinit var appCore: AppCore

    private lateinit var objectName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            selection = BundleCompat.getParcelable(it, ARG_OBJECT, Selection::class.java)!!
            embeddedInNavigation = it.getBoolean(ARG_EMBEDDED_IN_NAVIGATION, false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        objectName = appCore.simulation.universe.getNameForSelection(selection)
        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Mdc3Theme {
                    InfoScreen(selection = selection, title = objectName, showTitle = !embeddedInNavigation, linkHandler = {
                        listener?.onInfoLinkMetaDataClicked(it)
                    }, actionHandler = { item ->
                        listener?.onInfoActionSelected(item, selection)
                    })
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (embeddedInNavigation)
            title = objectName
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
        const val ARG_EMBEDDED_IN_NAVIGATION = "embedded-in-navigation"

        @JvmStatic
        fun newInstance(selection: Selection, embeddedInNavigation: Boolean = false) =
            InfoFragment().apply {
                arguments = Bundle().apply {
                    this.putParcelable(ARG_OBJECT, selection)
                    this.putBoolean(ARG_EMBEDDED_IN_NAVIGATION, embeddedInNavigation)
                }
            }
    }
}
