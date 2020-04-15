/*
 * HelpFragment.kt
 *
 * Copyright (C) 2001-2020, the Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.help

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.utils.CelestiaString

enum class HelpAction {
    RunDemo;
}

private val staticHelpDescriptionItems: List<DescriptionItem> by lazy { listOf(
    DescriptionItem(
        CelestiaString("Tap to select an object.", ""), R.drawable.tutorial_gesture_tap),
    DescriptionItem(
        CelestiaString("Drag with one finger to move/rotate around an object.", ""), R.drawable.tutorial_gesture_one_finger_pan),
    DescriptionItem(
        CelestiaString("Pinch to zoom in/out on an object.", ""), R.drawable.tutorial_gesture_pinch)
) }
private val staticHelpActionItems: List<ActionItem> by lazy { listOf( ActionItem(CelestiaString("Run Demo", ""), HelpAction.RunDemo) ) }

class HelpFragment : Fragment() {

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
            adapter = HelpRecyclerViewAdapter(listOf(staticHelpDescriptionItems, staticHelpActionItems), listener)
        }
        return view
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

    interface Listener {
        fun onHelpActionSelected(action: HelpAction)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            HelpFragment()
    }
}
