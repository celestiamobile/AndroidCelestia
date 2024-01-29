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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.fragment.app.Fragment
import space.celestia.celestiafoundation.utils.getSerializableValue
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.compose.Separator
import space.celestia.mobilecelestia.utils.CelestiaString
import java.io.Serializable

enum class ToolbarAction : Serializable {
    Setting, Share, Search, Time, Script, Camera, Browse, Help, Favorite, Home, Event, Exit, Addons, Download, Paperplane, Speedometer, NewsArchive, Feedback, CelestiaPlus;

    val title: String
        get() {
            return when (this) {
                Time -> CelestiaString("Time Control", "")
                Script -> CelestiaString("Script Control", "")
                Camera -> CelestiaString("Camera Control", "")
                Browse -> CelestiaString("Star Browser", "")
                Favorite -> CelestiaString("Favorites", "")
                Setting -> CelestiaString("Settings", "")
                Home -> CelestiaString("Home (Sol)", "")
                Event -> CelestiaString("Eclipse Finder", "")
                Addons -> CelestiaString("Installed Add-ons", "")
                Download -> CelestiaString("Get Add-ons", "")
                Paperplane -> CelestiaString("Go to Object", "")
                Speedometer -> CelestiaString("Speed Control", "")
                NewsArchive -> CelestiaString("News Archive", "")
                Feedback -> CelestiaString("Send Feedback", "")
                CelestiaPlus -> CelestiaString("Celestia PLUS", "")
                else -> this.toString()
            }
        }

    val imageResource: Int
        get() {
            return when(this) {
                Setting -> R.drawable.toolbar_setting
                Share -> R.drawable.toolbar_share
                Search -> R.drawable.toolbar_search
                Time -> R.drawable.toolbar_time
                Script -> R.drawable.toolbar_script
                Camera -> R.drawable.toolbar_camera
                Browse -> R.drawable.toolbar_browse
                Help -> R.drawable.toolbar_help
                Favorite -> R.drawable.toolbar_favorite
                Home -> R.drawable.toolbar_home
                Event -> R.drawable.toolbar_event
                Exit -> R.drawable.toolbar_exit
                Addons -> R.drawable.toolbar_addons
                Download -> R.drawable.toolbar_download
                Paperplane -> R.drawable.toolbar_paperplane
                Speedometer -> R.drawable.toolbar_speedometer
                NewsArchive -> R.drawable.toolbar_newsarchive
                Feedback -> R.drawable.toolbar_feedback
                CelestiaPlus -> R.drawable.toolbar_celestiaplus
            }
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
    ): View {
        val view =  ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Mdc3Theme {
                    MainScreen()
                }
            }
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

    @Composable
    private fun MainScreen() {
        val allSections = ArrayList(existingActions)
        allSections.addAll(ToolbarAction.persistentAction)

        LazyColumn(
            contentPadding = WindowInsets.systemBars.asPaddingValues(),
            modifier = Modifier
                .nestedScroll(rememberNestedScrollInteropConnection())
                .background(color = MaterialTheme.colorScheme.background)
        ) {
            for (sectionIndex in allSections.indices) {
                val section = allSections[sectionIndex]
                items(section) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.list_item_gap_horizontal)), modifier = Modifier
                        .clickable {
                            listener?.onToolbarActionSelected(it)
                        }
                        .fillMaxWidth()
                        .padding(horizontal = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal))) {
                        Icon(painter = painterResource(id = it.imageResource), contentDescription = "", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(dimensionResource(id = R.dimen.toolbar_list_icon_dimension)))
                        Text(text = it.title, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.list_item_medium_margin_vertical)))
                    }
                }
                if (sectionIndex != allSections.size - 1) {
                    item {
                        Separator(modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.toolbar_separator_padding_vertical)), separatorStart = dimensionResource(id = R.dimen.toolbar_separator_inset_start))
                    }
                }
            }
        }
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
