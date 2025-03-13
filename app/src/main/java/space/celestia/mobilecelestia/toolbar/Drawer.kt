/*
 * Drawer.kt
 *
 * Copyright (C) 2025-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.toolbar

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
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import space.celestia.mobilecelestia.R
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
fun Drawer(additionalActions: List<List<ToolbarAction>>, onToolbarActionSelected: (ToolbarAction) -> Unit) {
    val allSections = ArrayList(additionalActions)
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
                        onToolbarActionSelected(it)
                    }
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal))) {
                    Icon(painter = painterResource(id = it.imageResource), contentDescription = null, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(dimensionResource(id = R.dimen.toolbar_list_icon_dimension)))
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