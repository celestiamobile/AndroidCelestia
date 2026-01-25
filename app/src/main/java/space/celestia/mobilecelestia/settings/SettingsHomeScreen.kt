// SettingsHomeScreen.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.Footer
import space.celestia.mobilecelestia.compose.Header
import space.celestia.mobilecelestia.compose.Separator
import space.celestia.mobilecelestia.compose.TextRow
import space.celestia.mobilecelestia.settings.viewmodel.SettingsViewModel

@Composable
fun SettingsHomeScreen(paddingValues: PaddingValues, itemSelected: (SettingsItem) -> Unit) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val sections = if (viewModel.purchaseManager.canUseInAppPurchase()) mainSettingSectionsBeforePlus + celestiaPlusSettingSection + mainSettingSectionsAfterPlus else mainSettingSectionsBeforePlus + mainSettingSectionsAfterPlus
    LazyColumn(modifier = Modifier
        .nestedScroll(rememberNestedScrollInteropConnection()), contentPadding = paddingValues) {
        for (index in sections.indices) {
            val section = sections[index]
            item {
                val header = section.header
                if (header.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
                } else {
                    Header(text = header)
                }
            }
            items(section.items) { item ->
                TextRow(primaryText = item.name, modifier = Modifier.clickable {
                    itemSelected(item)
                }, accessoryResource = R.drawable.accessory_full_disclosure)
            }
            item {
                val footer = section.footer
                if (!footer.isNullOrEmpty()) {
                    Footer(text = footer)
                }
                if (index == sections.size - 1) {
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_tall)))
                } else {
                    val nextHeader = sections[index + 1].header
                    if (nextHeader.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
                        if (footer.isNullOrEmpty()) {
                            Separator()
                        }
                    }
                }
            }
        }
    }
}