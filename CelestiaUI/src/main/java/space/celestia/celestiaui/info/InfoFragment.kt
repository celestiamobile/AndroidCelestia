// InfoFragment.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.celestiaui.info

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import space.celestia.celestia.Selection
import space.celestia.celestiaui.compose.Mdc3Theme
import space.celestia.celestiaui.utils.CelestiaString

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
                    Scaffold(contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Bottom)) { paddingValues ->
                        InfoScreen(selection = selection, showTitle = true, linkClicked = {
                            listener?.infoLinkClicked(it)
                        }, openSubsystem = {
                            listener?.infoRequestOpenSubsystem(selection)
                        }, openRelatedAddons = {
                            listener?.infoRequestOpenRelatedAddons(it)
                        }, openSubscriptionManagement = {
                            listener?.infoRequestOpenSubscriptionManagement()
                        }, paddingValues = paddingValues)
                    }
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
        fun infoRequestOpenSubscriptionManagement()
        fun infoRequestOpenSubsystem(selection: Selection)
        fun infoRequestOpenRelatedAddons(objectPath: String)
        fun infoLinkClicked(link: String)
    }

    companion object {
        const val ARG_OBJECT = "object"

        fun getAvailableMarkers(): List<String> {
            return listOf(
                CelestiaString("Diamond", "Marker"),
                CelestiaString("Triangle", "Marker"),
                CelestiaString("Square", "Marker"),
                CelestiaString("Filled Square", "Marker"),
                CelestiaString("Plus", "Marker"),
                CelestiaString("X", "Marker"),
                CelestiaString("Left Arrow", "Marker"),
                CelestiaString("Right Arrow", "Marker"),
                CelestiaString("Up Arrow", "Marker"),
                CelestiaString("Down Arrow", "Marker"),
                CelestiaString("Circle", "Marker"),
                CelestiaString("Disk", "Marker"),
                CelestiaString("Crosshair", "Marker"),
                CelestiaString("Unmark", "Unmark an object"),
            )
        }

        @JvmStatic
        fun newInstance(selection: Selection) =
            InfoFragment().apply {
                arguments = Bundle().apply {
                    this.putParcelable(ARG_OBJECT, selection)
                }
            }
    }
}
