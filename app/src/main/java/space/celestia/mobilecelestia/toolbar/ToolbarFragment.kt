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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.toolbar.model.ToolbarActionItem
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.celestiafoundation.utils.getSerializableValue
import java.io.Serializable
import java.util.*

enum class ToolbarAction : Serializable {
    Setting, Share, Search, Time, Script, Camera, Browse, Help, Favorite, Home, Event, Exit, Addons, Download, Paperplane, Speedometer, NewsArchive, Feedback, CelestiaPlus;

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
                Feedback -> "Send Feedback"
                CelestiaPlus -> "Celestia PLUS"
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
                listOf(Feedback, Help),
                listOf(Exit)
            )
    }
}

class ToolbarFragment: Fragment() {
    private var existingActions: List<List<ToolbarAction>> = ArrayList()
    private var listener: Listener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            @Suppress("UNCHECKED_CAST")
            val value = it.getSerializableValue(ARG_ACTION_WRAPPER, ArrayList::class.java) as? List<List<ToolbarAction>>
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

        val listView = view.findViewById<RecyclerView>(R.id.list)
        with(listView) {
            layoutManager = LinearLayoutManager(context)
            adapter = ToolbarRecyclerViewAdapter(model, listener)
        }

        // Container framelayout already has applied the horizontal insets, stop passing the horizontal insets
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.list_container)) { _, insets ->
            // TODO: the suggested replacement for the deprecated methods does not work
            val builder = WindowInsetsCompat.Builder(insets).setSystemWindowInsets(Insets.of(0 , insets.systemWindowInsetTop, 0, insets.systemWindowInsetBottom))
            return@setOnApplyWindowInsetsListener builder.build()
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement ToolbarFragment.Listener")
        }
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
