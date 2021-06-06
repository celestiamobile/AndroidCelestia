/*
 * HelpFragment.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.help

import android.content.Context
import android.os.Bundle
import android.util.LayoutDirection
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.InsetAwareFragment
import space.celestia.mobilecelestia.utils.CelestiaString

enum class HelpAction {
    RunDemo,
    ShowDestinations;
}

private val staticHelpDescriptionItems: List<DescriptionItem> by lazy { listOf(
    DescriptionItem(
        CelestiaString("Tap the mode button on the sidebar to switch between object mode and camera mode.", ""), R.drawable.tutorial_switch_mode),
    DescriptionItem(
        CelestiaString("In object mode, drag to rotate around an object.\n\nPinch to zoom in/out on an object.", ""), R.drawable.tutorial_mode_object),
    DescriptionItem(
        CelestiaString("In camera mode, drag to move field of view.\n\nPinch to zoom in/out field of view.", ""), R.drawable.tutorial_mode_camera)
) }
private val staticHelpURLItems: List<URLItem> by lazy { listOf(
    URLItem(CelestiaString("Mouse/Keyboard Controls", ""), "https://github.com/levinli303/Celestia/wiki/Controls"),
    URLItem(CelestiaString("Use Add-ons and Scripts", ""), "https://github.com/levinli303/Celestia/wiki/Use-Addons-and-Scripts"),
    URLItem(CelestiaString("Scripts and URLs", ""), "https://github.com/levinli303/Celestia/wiki/Scripts-and-URLs")
) }
private val staticHelpActionItems: List<ActionItem> by lazy {
    listOf(
        ActionItem(CelestiaString("Run Demo", ""), HelpAction.RunDemo),
        ActionItem(CelestiaString("Show Destinations", ""), HelpAction.ShowDestinations)
    )
}

class HelpFragment : InsetAwareFragment() {

    private var listener: Listener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_help, container, false)

        view.findViewById<TextView>(R.id.welcome_message).text = CelestiaString("Welcome to Celestia", "")

        // Set the adapter
        with(view.findViewById<RecyclerView>(R.id.list)) {
            layoutManager = LinearLayoutManager(context)
            adapter = HelpRecyclerViewAdapter(listOf(staticHelpDescriptionItems, staticHelpURLItems, staticHelpActionItems), listener)
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyPadding(view, currentSafeInsets)
    }

    override fun onInsetChanged(view: View, newInsets: EdgeInsets) {
        super.onInsetChanged(view, newInsets)

        applyPadding(view, newInsets)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement HelpFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun applyPadding(view: View, insets: EdgeInsets) {
        val isRTL = resources.configuration.layoutDirection == LayoutDirection.RTL
        if (isRTL)
            view.setPadding(insets.left, insets.top, 0, insets.bottom)
        else
            view.setPadding(0, insets.top, insets.right, insets.bottom)
    }

    interface Listener {
        fun onHelpActionSelected(action: HelpAction)
        fun onHelpURLSelected(url: String)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            HelpFragment()
    }
}
