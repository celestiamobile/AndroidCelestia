// InfoScreen.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import space.celestia.celestia.Selection
import space.celestia.celestia.Universe
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.celestia.CelestiaFragment
import space.celestia.mobilecelestia.compose.LinkPreview
import space.celestia.mobilecelestia.compose.OptionInputDialog
import space.celestia.mobilecelestia.compose.SwitchRow
import space.celestia.mobilecelestia.info.model.AlternateSurfacesItem
import space.celestia.mobilecelestia.info.model.InfoActionItem
import space.celestia.mobilecelestia.info.model.InfoNormalActionItem
import space.celestia.mobilecelestia.info.model.InfoSelectActionItem
import space.celestia.mobilecelestia.info.model.InfoWebActionItem
import space.celestia.mobilecelestia.info.model.MarkItem
import space.celestia.mobilecelestia.info.model.SubsystemActionItem
import space.celestia.mobilecelestia.info.viewmodel.InfoViewModel
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.getOverviewForSelection
import java.net.MalformedURLException
import java.net.URL


sealed class InfoAlert {
    data class AlternativeSurfaceSelection(val surfaces: List<String>): InfoAlert()
    data object MarkerSelection: InfoAlert()
}

@Composable
fun InfoScreen(selection: Selection, showTitle: Boolean, linkClicked: (String) -> Unit, openSubsystem: () -> Unit, paddingValues: PaddingValues, modifier: Modifier = Modifier) {
    val viewModel: InfoViewModel = hiltViewModel()
    var objectName by remember { mutableStateOf("") }
    var overview by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var alert by remember { mutableStateOf<InfoAlert?>(null) }

    LaunchedEffect(selection) {
        objectName = if (showTitle) viewModel.appCore.simulation.universe.getNameForSelection(selection) else ""
        overview = viewModel.appCore.getOverviewForSelection(selection)
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
                if (selection.body?.canBeUsedAsCockpit() == true) {
                    var isCockpit by remember { mutableStateOf(viewModel.appCore.simulation.activeObserver.cockpit == selection) }
                    SwitchRow(primaryText = CelestiaString("Use as Cockpit","Option to use a spacecraft as cockpit"), checked = isCockpit, onCheckedChange = {
                        isCockpit = it
                        scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                            viewModel.appCore.simulation.activeObserver.cockpit = if (it) selection else Selection()
                        }
                    }, horizontalPadding = 0.dp, canTapWholeRow = false)
                }
                if (hasWebInfo && url != null) {
                    LinkPreview(url = url, modifier = rowModifier.padding(bottom = dimensionResource(id = R.dimen.common_page_medium_gap_vertical)), onClick = { finalURL ->
                        linkClicked(finalURL.toString())
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
                    when (val action = item.second) {
                        is AlternateSurfacesItem -> {
                            val alternateSurfaces = selection.body?.alternateSurfaceNames ?: return@FilledTonalButton
                            val surfaces = ArrayList<String>()
                            surfaces.add(CelestiaString("Default", ""))
                            surfaces.addAll(alternateSurfaces)
                            alert = InfoAlert.AlternativeSurfaceSelection(surfaces)
                        }
                        is InfoNormalActionItem -> {
                            scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                                viewModel.appCore.simulation.selection = selection
                                viewModel.appCore.charEnter(action.item.value)
                            }
                        }
                        is InfoSelectActionItem -> {
                            scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                                viewModel.appCore.simulation.selection = selection
                            }
                        }
                        is InfoWebActionItem -> {
                            selection.webInfoURL?.let {
                                linkClicked(it)
                            }
                        }
                        is MarkItem -> {
                            alert = InfoAlert.MarkerSelection
                        }
                        is SubsystemActionItem -> {
                            openSubsystem()
                        }
                    }
                }) {
                Text(text = item.second.title)
            }
        }
    })

    alert?.let { content ->
        when (content) {
            is InfoAlert.AlternativeSurfaceSelection -> {
                OptionInputDialog(onDismissRequest = {
                    alert = null
                }, title = CelestiaString("Alternate Surfaces", "Alternative textures to display"), items = content.surfaces) { index ->
                    alert = null
                    scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                        if (index == 0)
                            viewModel.appCore.simulation.activeObserver.displayedSurface = ""
                        else
                            viewModel.appCore.simulation.activeObserver.displayedSurface = content.surfaces[index]
                    }
                }
            }
            is InfoAlert.MarkerSelection -> {
                val markers = CelestiaFragment.getAvailableMarkers()
                OptionInputDialog(onDismissRequest = {
                    alert = null
                }, title = CelestiaString("Mark", "Mark an object"), items = markers) { index ->
                    alert = null
                    scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                        if (index >= Universe.MARKER_COUNT) {
                            viewModel.appCore.simulation.universe.unmark(selection)
                        } else {
                            viewModel.appCore.simulation.universe.mark(selection, index)
                            viewModel.appCore.showMarkers = true
                        }
                    }
                }
            }
        }
    }
}