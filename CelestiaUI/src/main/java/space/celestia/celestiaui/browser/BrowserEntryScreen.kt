// BrowserEntryScreen.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.celestiaui.browser

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.lifecycle.compose.dropUnlessResumed
import space.celestia.celestia.BrowserItem
import space.celestia.celestiaui.R
import space.celestia.celestiaui.browser.viewmodel.BrowserPredefinedItem
import space.celestia.celestiaui.compose.Header
import space.celestia.celestiaui.compose.TeachingCard
import space.celestia.celestiaui.compose.TextRow
import space.celestia.celestiaui.compose.groupedItem
import space.celestia.celestiaui.compose.groupedItems
import space.celestia.celestiaui.compose.groupedListContentPadding
import space.celestia.celestiaui.utils.CelestiaString


@Composable
fun BrowserEntry(item: BrowserItem, paddingValues: PaddingValues, itemSelected: (BrowserItem, Boolean) -> Unit, addonCategoryRequested: (BrowserPredefinedItem.CategoryInfo) -> Unit) {
    val contentPadding = groupedListContentPadding(paddingValues)

    LazyColumn(
        contentPadding = contentPadding,
        modifier = Modifier
            .nestedScroll(rememberNestedScrollInteropConnection())
            .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        var hasMainObject = false
        var hasItems = false
        if (item.`object` != null) {
            groupedItem {
                TextRow(primaryText = item.name, modifier = Modifier.clickable(onClick = dropUnlessResumed {
                    itemSelected(item, true)
                }))
            }
            hasMainObject = true
            hasItems = true
        }

        if (item.children.isNotEmpty()) {
            if (hasMainObject) {
                item {
                    Header(text = CelestiaString("Subsystem", "Subsystem of an object (e.g. planetarium system)"))
                }
            }

            groupedItems(item.children) { item ->
                TextRow(primaryText = item.name, accessoryResource = if (item.children.isNotEmpty() || item.`object` == null) R.drawable.accessory_full_disclosure else 0, modifier = Modifier.clickable(onClick = dropUnlessResumed {
                    itemSelected(item, item.`object` != null && item.children.isEmpty())
                }))
            }

            hasItems = true
        }

        if (item is BrowserPredefinedItem) {
            val categoryInfo = item.categoryInfo
            if (categoryInfo != null) {
                item {
                    if (hasItems) {
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_group_spacing)))
                    }
                    TeachingCard(title = CelestiaString("Enhance Celestia with online add-ons", ""), actionButtonTitle = CelestiaString("Get Add-ons", "Open webpage for downloading add-ons"), action = {
                        addonCategoryRequested(categoryInfo)
                    }, modifier = Modifier.fillMaxWidth().padding(
                        horizontal = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal),
                        vertical = dimensionResource(id = R.dimen.list_item_medium_margin_vertical)
                    ))
                }
            }
        }
    }
}