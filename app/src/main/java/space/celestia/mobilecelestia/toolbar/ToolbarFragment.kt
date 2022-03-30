/*
 * ToolbarFragment.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.toolbar

import android.content.Context
import android.os.Bundle
import android.util.LayoutDirection
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.EdgeInsets
import space.celestia.mobilecelestia.common.InsetAwareFragment
import space.celestia.mobilecelestia.common.RoundedCorners
import space.celestia.mobilecelestia.toolbar.model.ToolbarActionItem
import space.celestia.mobilecelestia.utils.CelestiaString
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

enum class ToolbarAction : Serializable {
    Setting, Share, Search, Time, Script, Camera, Browse, Help, Favorite, Home, Event, Exit, Addons, Download, Paperplane, Speedometer, NewsArchive;

    val title: String
        get() {
            val orig = when (this) {
                Time -> "Time Control"
                Script -> "Script Control"
                Camera -> "Camera Control"
                Browse -> "Star Browser"
                Favorite -> "Favorites"
                Setting -> "Settings"
                Home -> "Home (Sol)"
                Event -> "Eclipse Finder"
                Addons -> "Installed Add-ons"
                Download -> "Get Add-ons"
                Paperplane -> "Go to Object"
                Speedometer -> "Speed Control"
                NewsArchive -> "News Archive"
                else -> this.toString()
            }
            return CelestiaString(orig, "")
        }

    val imageName: String
        get() {
            return "toolbar_" + toString().lowercase(Locale.US)
        }

    companion object {
        val persistentAction: List<List<ToolbarAction>>
            get() = listOf(
                listOf(Setting),
                listOf(Share, Search, Home, Paperplane),
                listOf(Camera, Time, Script, Speedometer),
                listOf(Browse, Favorite, Event),
                listOf(Addons, Download, NewsArchive),
                listOf(Help),
                listOf(Exit)
            )
    }
}

class ToolbarFragment : InsetAwareFragment() {

    private var existingActions: List<List<ToolbarAction>> = ArrayList()
    private var listener: Listener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            @Suppress("UNCHECKED_CAST")
            val value = it.getSerializable(ARG_ACTION_WRAPPER) as? List<List<ToolbarAction>>
            if (value != null) {
                existingActions = value
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_toolbar_list, container, false)

        val allItems = ArrayList(existingActions)
        allItems.addAll(ToolbarAction.persistentAction)
        val model = allItems.map { it.map { inner -> ToolbarActionItem(inner, resources.getIdentifier(inner.imageName, "drawable", requireActivity().packageName)) } }

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = ToolbarRecyclerViewAdapter(model, listener)
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyPadding(view, currentSafeInsets)
    }

    override fun onInsetChanged(view: View, newInsets: EdgeInsets, roundedCorners: RoundedCorners) {
        super.onInsetChanged(view, newInsets, roundedCorners)

        applyPadding(view, newInsets)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement ToolbarFragment.Listener")
        }
    }

    private fun applyPadding(view: View, insets: EdgeInsets) {
        val isRTL = resources.configuration.layoutDirection == LayoutDirection.RTL
        if (isRTL)
            view.setPadding(insets.left, insets.top, 0, insets.bottom)
        else
            view.setPadding(0, insets.top, insets.right, insets.bottom)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface Listener {
        fun onToolbarActionSelected(action: ToolbarAction)
    }

    companion object {

        const val ARG_ACTION_WRAPPER = "action-wrapper"

        @JvmStatic
        fun newInstance(actions: List<List<ToolbarAction>>) =
            ToolbarFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ACTION_WRAPPER, ArrayList<ArrayList<ToolbarAction>>(actions.map { ArrayList(it) }))
                }
            }
    }
}
