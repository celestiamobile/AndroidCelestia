/*
 * HelpScreen.kt
 *
 * Copyright (C) 2025-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.help

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.utils.CelestiaString

enum class HelpAction {
    RunDemo;
}

@Composable
fun HelpScreen(helpURLSelected: (String) -> Unit, helpActionSelected: (HelpAction) -> Unit, paddingValues: PaddingValues, modifier: Modifier = Modifier) {
    val staticHelpDescriptionItems: List<DescriptionItem> by lazy { listOf(
        DescriptionItem(
            CelestiaString("Tap the mode button on the sidebar to switch between object mode and camera mode.", ""), R.drawable.tutorial_switch_mode),
        DescriptionItem(
            CelestiaString("In object mode, drag to rotate around an object.\n\nPinch to zoom in/out on an object.", ""), R.drawable.tutorial_mode_object),
        DescriptionItem(
            CelestiaString("In camera mode, drag to move field of view.\n\nPinch to zoom in/out field of view.", ""), R.drawable.tutorial_mode_camera)
    ) }
    val staticHelpURLItems: List<URLItem> by lazy { listOf(
        URLItem(CelestiaString("Mouse/Keyboard Controls", "Guide to control Celestia with a mouse/keyboard"), "celguide://guide?guide=BE1B5023-46B6-1F10-F15F-3B3F02F30300"),
        URLItem(CelestiaString("Use Add-ons and Scripts", "URL for Use Add-ons and Scripts wiki"), "celguide://guide?guide=D1A96BFA-00BB-0089-F361-10DD886C8A4F"),
        URLItem(CelestiaString("Scripts and URLs", "URL for Scripts and URLs wiki"), "celguide://guide?guide=A0AB3F01-E616-3C49-0934-0583D803E9D0")
    ) }
    val staticHelpActionItems: List<ActionItem> by lazy {
        listOf(
            ActionItem(CelestiaString("Run Demo", ""), HelpAction.RunDemo),
        )
    }
    val buttonModifier = Modifier.fillMaxWidth().padding(
        vertical = dimensionResource(id = R.dimen.list_text_gap_vertical),
        horizontal = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal)
    )
    LazyColumn(
        contentPadding = paddingValues,
        modifier = modifier
    ) {
        item {
            Text(text = CelestiaString("Welcome to Celestia", "Welcome message"), style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(
                start = dimensionResource(id = R.dimen.common_page_medium_margin_horizontal),
                end = dimensionResource(id = R.dimen.common_page_medium_margin_horizontal),
                top = dimensionResource(id = R.dimen.common_page_medium_margin_vertical),
                bottom = dimensionResource(id = R.dimen.common_page_medium_gap_vertical),
            ))
        }

        items(staticHelpDescriptionItems) {
            Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.tutorial_list_item_gap_horizontal)), modifier = Modifier.fillMaxWidth().padding(horizontal = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal), vertical = dimensionResource(id = R.dimen.list_item_medium_margin_vertical))) {
                Icon(painter = painterResource(id = it.imageResourceID), contentDescription = null, modifier = Modifier.size(dimensionResource(id = R.dimen.tutorial_list_icon_dimension)), tint = MaterialTheme.colorScheme.onBackground)
                Text(text = it.description, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyLarge)
            }
        }

        items(staticHelpURLItems) {
            FilledTonalButton(modifier = buttonModifier, onClick = {
                helpURLSelected(it.url)
            }) {
                Text(text = it.title)
            }
        }

        items(staticHelpActionItems) {
            FilledTonalButton(modifier = buttonModifier, onClick = {
                helpActionSelected(it.action)
            }) {
                Text(text = it.title)
            }
        }
    }
}