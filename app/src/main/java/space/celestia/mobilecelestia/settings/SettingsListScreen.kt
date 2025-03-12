/*
 * SettingsListScreen.kt
 *
 * Copyright (C) 2023-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.Header
import space.celestia.mobilecelestia.compose.TextRow

@Composable
fun SettingsListScreen(paddingValues: PaddingValues, sections: List<SettingsSection>, itemHandler: (Settings) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier, contentPadding = paddingValues) {
        for (index in sections.indices) {
            val section = sections[index]
            item {
                val header = section.title.displayName
                if (header.isEmpty()) {
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
                } else {
                    Header(text = header)
                }
            }
            items(section.items) { item ->
                TextRow(primaryText = item.name, modifier = Modifier.clickable {
                    itemHandler(item)
                })
            }
            item {
                if (index == sections.size - 1) {
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_tall)))
                } else {
                    val nextHeader = sections[index + 1].title.displayName
                    if (nextHeader.isEmpty()) {
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
                    }
                }
            }
        }
    }
}