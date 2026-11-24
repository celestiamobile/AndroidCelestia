// SettingsHomeScreen.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.celestiaui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.dropUnlessResumed
import space.celestia.celestiaui.R
import space.celestia.celestiaui.compose.Footer
import space.celestia.celestiaui.compose.Header
import space.celestia.celestiaui.compose.TextRow
import space.celestia.celestiaui.compose.groupedItems
import space.celestia.celestiaui.compose.groupedListContentPadding
import space.celestia.celestiaui.settings.viewmodel.SettingsItem
import space.celestia.celestiaui.settings.viewmodel.SettingsViewModel

@Composable
fun SettingsHomeScreen(paddingValues: PaddingValues, itemSelected: (SettingsItem) -> Unit) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val sections = viewModel.settingsEntryProvider.settings(viewModel.purchaseManager).filter { it.items.isNotEmpty() }
    LazyColumn(modifier = Modifier
        .nestedScroll(rememberNestedScrollInteropConnection()), contentPadding = groupedListContentPadding(paddingValues)) {
        for (index in sections.indices) {
            val section = sections[index]
            item {
                val header = section.header
                if (header.isNullOrEmpty()) {
                    if (index > 0) {
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.list_group_spacing)))
                    }
                } else {
                    Header(text = header, isFirst = index == 0)
                }
            }
            groupedItems(section.items) { item ->
                TextRow(primaryText = item.name, modifier = Modifier.clickable(onClick = dropUnlessResumed {
                    itemSelected(item)
                }), accessoryResource = R.drawable.accessory_full_disclosure)
            }
            item {
                val footer = section.footer
                if (!footer.isNullOrEmpty()) {
                    Footer(text = footer)
                }
            }
        }
    }
}