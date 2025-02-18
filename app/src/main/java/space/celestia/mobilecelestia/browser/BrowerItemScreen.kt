/*
 * BrowserItemScreen.kt
 *
 * Copyright (C) 2025-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.browser

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.hilt.navigation.compose.hiltViewModel
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.browser.viewmodel.BrowserNavigationViewModel
import space.celestia.mobilecelestia.compose.Header
import space.celestia.mobilecelestia.compose.TeachingCard
import space.celestia.mobilecelestia.compose.TextRow
import space.celestia.mobilecelestia.utils.CelestiaString

@Composable
fun BrowserItemScreen(paddingValues: PaddingValues, path: String, itemSelected: (BrowserUIItem) -> Unit, addonCategoryRequested: (BrowserPredefinedItem.CategoryInfo) -> Unit, modifier: Modifier = Modifier) {
    val viewModel: BrowserNavigationViewModel = hiltViewModel()
    val item = requireNotNull(viewModel.browserMap[path])

    val direction = LocalLayoutDirection.current
    val contentPadding = PaddingValues(
        start = paddingValues.calculateStartPadding(direction),
        top = dimensionResource(id = R.dimen.list_spacing_short) + paddingValues.calculateTopPadding(),
        end = paddingValues.calculateEndPadding(direction),
        bottom = dimensionResource(id = R.dimen.list_spacing_tall) + paddingValues.calculateBottomPadding(),
    )
    LazyColumn(
        contentPadding = contentPadding,
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        var hasMainObject = false
        var hasItems = false
        if (item.`object` != null) {
            item {
                TextRow(primaryText = item.name, modifier = Modifier.clickable {
                    itemSelected(BrowserUIItem(item, true))
                })
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

            items(item.children) { item ->
                TextRow(primaryText = item.name, accessoryResource = if (item.children.isNotEmpty() || item.`object` == null) R.drawable.accessory_full_disclosure else 0, modifier = Modifier.clickable {
                    itemSelected(BrowserUIItem(item, item.`object` != null && item.children.isEmpty()))
                })
            }

            hasItems = true
        }

        if (item is BrowserPredefinedItem) {
            val categoryInfo = item.categoryInfo
            if (categoryInfo != null) {
                item {
                    if (hasItems) {
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
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