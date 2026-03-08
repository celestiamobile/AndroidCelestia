// MenuScreen.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.menu

import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import space.celestia.celestiaui.compose.Separator
import space.celestia.celestiaui.utils.CelestiaString
import space.celestia.mobilecelestia.R
import java.io.Serializable

enum class ToolbarAction : Serializable {
    Setting, Share, Search, Time, Script, Camera, Browse, Help, Favorite, Home, Event, Exit, Addons, Download, Paperplane, Speedometer, NewsArchive, Feedback, CelestiaPlus;

    val title: String
        get() {
            return when (this) {
                Time -> CelestiaString("Time Control", "")
                Script -> CelestiaString("Script Control", "")
                Camera -> CelestiaString("Camera Control", "Observer control")
                Browse -> CelestiaString("Star Browser", "")
                Favorite -> CelestiaString("Favorites", "Favorites (currently bookmarks and scripts)")
                Setting -> CelestiaString("Settings", "")
                Home -> CelestiaString("Home (Sol)", "Home object, sun.")
                Event -> CelestiaString("Eclipse Finder", "")
                Addons -> CelestiaString("Installed Add-ons", "Open a page for managing installed add-ons")
                Download -> CelestiaString("Get Add-ons", "Open webpage for downloading add-ons")
                Paperplane -> CelestiaString("Go to Object", "")
                Speedometer -> CelestiaString("Speed Control", "Speed control")
                NewsArchive -> CelestiaString("News Archive", "Archive for updates and featured content")
                Feedback -> CelestiaString("Send Feedback", "")
                CelestiaPlus -> CelestiaString("Celestia PLUS", "Name for the subscription service")
                Share -> CelestiaString("Share", "")
                Search -> CelestiaString("Search", "")
                Help -> CelestiaString("Help", "")
                Exit -> CelestiaString("Exit", "")
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


@Composable
fun MenuScreen(actions: List<List<ToolbarAction>>, actionSelected: (ToolbarAction) -> Unit) {
    val allSections = ArrayList(actions)
    allSections.addAll(ToolbarAction.persistentAction)
    Scaffold(contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Bottom + WindowInsetsSides.End)) { paddingValues ->
        Surface(
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            LazyColumn(
                contentPadding = paddingValues,
                modifier = Modifier.nestedScroll(rememberNestedScrollInteropConnection())
            ) {
                for (sectionIndex in allSections.indices) {
                    val section = allSections[sectionIndex]
                    items(section) {
                        NavigationDrawerItem(
                            label = {
                                Text(
                                    text = it.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(vertical = dimensionResource(id = space.celestia.celestiaui.R.dimen.list_item_medium_margin_vertical))
                                )
                            },
                            selected = false,
                            icon = {
                                Icon(
                                    painter = painterResource(id = it.imageResource),
                                    contentDescription = "",
                                    modifier = Modifier.size(dimensionResource(id = R.dimen.toolbar_list_icon_dimension))
                                )
                            },
                            onClick = {
                                actionSelected(it)
                            },
                            modifier = Modifier.fillMaxWidth()
                                .padding(horizontal = dimensionResource(id = space.celestia.celestiaui.R.dimen.list_item_small_margin_horizontal))
                        )
                    }
                    if (sectionIndex != allSections.size - 1) {
                        item {
                            Separator(
                                modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.toolbar_separator_padding_vertical)),
                                separatorStart = dimensionResource(id = R.dimen.toolbar_separator_inset_start)
                            )
                        }
                    }
                }
            }
        }
    }
}