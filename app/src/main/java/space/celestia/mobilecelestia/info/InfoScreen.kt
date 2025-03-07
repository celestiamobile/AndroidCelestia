/*
 * InfoScreen.kt
 *
 * Copyright (C) 2023-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.info

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import space.celestia.celestia.Selection
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.LinkPreview
import space.celestia.mobilecelestia.info.model.AlternateSurfacesItem
import space.celestia.mobilecelestia.info.model.InfoActionItem
import space.celestia.mobilecelestia.info.model.InfoWebActionItem
import space.celestia.mobilecelestia.info.model.MarkItem
import space.celestia.mobilecelestia.info.model.SubsystemActionItem
import space.celestia.mobilecelestia.settings.viewmodel.InfoViewModel
import space.celestia.mobilecelestia.utils.getOverviewForSelection
import java.net.MalformedURLException
import java.net.URL

@Composable
fun InfoScreen(selection: Selection, showTitle: Boolean, linkHandler: (URL) -> Unit, actionHandler: (InfoActionItem, Selection) -> Unit, paddingValues: PaddingValues, modifier: Modifier = Modifier) {
    val viewModel: InfoViewModel = hiltViewModel()
    var isWebInfoLoaded by remember { mutableStateOf(false) }
    var objectName by remember { mutableStateOf("") }
    var overview by remember { mutableStateOf("") }

    LaunchedEffect(selection) {
        objectName = if (showTitle) viewModel.appCore.simulation.universe.getNameForSelection(selection) else ""
        overview = viewModel.appCore.getOverviewForSelection(selection)
        isWebInfoLoaded = false
    }

    val rowModifier = Modifier.fillMaxWidth()

    val direction = LocalLayoutDirection.current
    val contentPadding = PaddingValues(
        start = dimensionResource(id = R.dimen.common_page_medium_margin_horizontal) + paddingValues.calculateStartPadding(direction),
        top = dimensionResource(id = R.dimen.common_page_medium_margin_vertical) + paddingValues.calculateTopPadding(),
        end = dimensionResource(id = R.dimen.common_page_medium_margin_horizontal) + paddingValues.calculateEndPadding(direction),
        bottom = dimensionResource(id = R.dimen.common_page_medium_margin_vertical) + paddingValues.calculateBottomPadding(),
    )

    LazyVerticalGrid(modifier = modifier, contentPadding = contentPadding, columns = GridCells.Fixed(2), content = {
        val hasAltSurface = (selection.body?.alternateSurfaceNames?.size ?: 0) > 0
        val hasWebInfo = !selection.webInfoURL.isNullOrEmpty()

        item(span = { GridItemSpan(2) }) {
            Column {
                if (showTitle) {
                    SelectionContainer {
                        Text(text = objectName, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.headlineSmall, modifier = rowModifier.padding(bottom = dimensionResource(
                            id = R.dimen.common_page_medium_gap_vertical
                        )))
                    }
                }
                SelectionContainer {
                    Text(text = overview, color = colorResource(id = com.google.android.material.R.color.material_on_background_emphasis_medium), style = MaterialTheme.typography.bodyLarge, modifier = rowModifier.padding(bottom = dimensionResource(
                        id = R.dimen.common_page_medium_gap_vertical
                    )))
                }
                val urlString = selection.webInfoURL
                var url: URL? = null
                try {
                    url = URL(urlString)
                } catch (ignored: MalformedURLException) {}
                if (hasWebInfo && url != null) {
                    LinkPreview(url = url, modifier = rowModifier.padding(bottom = dimensionResource(id = R.dimen.common_page_medium_gap_vertical)), loadResult = { loadResult ->
                        isWebInfoLoaded = loadResult
                    }, onClick = { finalURL ->
                        linkHandler(finalURL)
                    })
                }
            }
        }

        val actions = ArrayList(InfoActionItem.infoActions)
        if (hasWebInfo)
            actions.add(InfoWebActionItem())
        if (hasAltSurface)
            actions.add(AlternateSurfacesItem())
        actions.add(SubsystemActionItem())
        actions.add(MarkItem())

        val count = actions.size
        items(actions.mapIndexed { index, infoActionItem -> Pair(index, infoActionItem) }) { item ->
            val horizontalButtonSpacing = R.dimen.common_page_medium_gap_horizontal
            val verticalButtonSpacing = R.dimen.common_page_button_gap_vertical
            val index = item.first
            FilledTonalButton(
                modifier = Modifier.padding(
                    start = if (index % 2 == 0) 0.dp else dimensionResource(horizontalButtonSpacing) / 2,
                    top = if (index / 2 == 0) 0.dp else dimensionResource(verticalButtonSpacing) / 2,
                    end =  if (index % 2 == 1) 0.dp else dimensionResource(horizontalButtonSpacing) / 2,
                    bottom = if (index / 2 == (count - 1) / 2) 0.dp else dimensionResource(verticalButtonSpacing) / 2,
                ), onClick = {
                    actionHandler(item.second, selection)
                }) {
                Text(text = item.second.title)
            }
        }
    })
}