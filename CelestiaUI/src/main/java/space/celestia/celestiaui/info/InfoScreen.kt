// InfoScreen.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.celestiaui.info

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import space.celestia.celestia.Body
import space.celestia.celestia.DSO
import space.celestia.celestia.Selection
import space.celestia.celestia.Star
import space.celestia.celestia.Universe
import space.celestia.celestiaui.R
import space.celestia.celestiaui.compose.LinkPreview
import space.celestia.celestiaui.compose.OptionInputDialog
import space.celestia.celestiaui.compose.SimpleAlertDialog
import space.celestia.celestiaui.compose.SwitchRow
import space.celestia.celestiaui.info.model.AlternateSurfacesItem
import space.celestia.celestiaui.info.model.InfoActionItem
import space.celestia.celestiaui.info.model.InfoNormalActionItem
import space.celestia.celestiaui.info.model.InfoSelectActionItem
import space.celestia.celestiaui.info.model.InfoWebActionItem
import space.celestia.celestiaui.info.model.MarkItem
import space.celestia.celestiaui.info.model.RelatedAddonItem
import space.celestia.celestiaui.info.model.SubsystemActionItem
import space.celestia.celestiaui.info.model.perform
import space.celestia.celestiaui.info.viewmodel.InfoViewModel
import space.celestia.celestiaui.utils.CelestiaString
import space.celestia.celestiaui.utils.getOverviewForSelection
import java.net.URL


private sealed class InfoAlert {
    data class AlternativeSurfaceSelection(val surfaces: List<String>): InfoAlert()
    data object MarkerSelection: InfoAlert()
    data object RequireCelestiaPlus: InfoAlert()
}

private data class InfoContextObject(val actions: List<InfoActionItem>, val name: String, val overview: String, val webInfoURL: URL?)
@Composable
fun InfoScreen(selection: Selection, showTitle: Boolean, linkClicked: (String) -> Unit, openSubsystem: () -> Unit, openRelatedAddons: (String) -> Unit, openSubscriptionManagement: () -> Unit, paddingValues: PaddingValues, modifier: Modifier = Modifier) {
    val viewModel: InfoViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    var alert by remember { mutableStateOf<InfoAlert?>(null) }
    var infoContext by remember { mutableStateOf<InfoContextObject?>(null) }
    var cockpitStatus by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(selection) {
        val results = withContext(viewModel.executor.asCoroutineDispatcher()) {
            val actions = arrayListOf<InfoActionItem>()
            actions.addAll(InfoActionItem.infoActions)

            if (viewModel.purchaseManager.canUseInAppPurchase()) {
                val objectPath = when (val item = selection.`object`) {
                    is Body -> {
                        item.getPath(viewModel.appCore.simulation.universe.starCatalog)
                    }
                    is Star -> {
                        viewModel.appCore.simulation.universe.starCatalog.getStarName(item, false)
                    }
                    is DSO -> {
                        viewModel.appCore.simulation.universe.dsoCatalog.getDSOName(item, false)
                    }
                    else -> {
                        null
                    }
                }
                if (!objectPath.isNullOrEmpty() && !objectPath.startsWith(" ")) {
                    actions.add(RelatedAddonItem(objectPath))
                }
            }

            val alternativeSurfaceNames = selection.body?.alternateSurfaceNames
            val url = selection.webInfoURL
            var connectionURL: URL? = null
            if (!url.isNullOrEmpty()) {
                actions.add(InfoWebActionItem(url))
                try {
                    connectionURL = URL(url)
                } catch (ignored: Throwable) {}
            }
            if (!alternativeSurfaceNames.isNullOrEmpty())
                actions.add(AlternateSurfacesItem(alternativeSurfaceNames))
            actions.add(SubsystemActionItem())
            actions.add(MarkItem())
            val context = InfoContextObject(
                actions = actions,
                name = if (showTitle) viewModel.appCore.simulation.universe.getNameForSelection(selection) else "",
                overview = viewModel.appCore.getOverviewForSelection(selection),
                webInfoURL = connectionURL
            )
            val cockpitStatus = if (selection.body?.canBeUsedAsCockpit() == true) {
                viewModel.appCore.simulation.activeObserver.cockpit == selection
            } else {
               null
            }
            return@withContext Pair(context, cockpitStatus)
        }
        infoContext = results.first
        cockpitStatus = results.second
    }

    val context = infoContext
    if (context == null) {
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
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
        item(span = { GridItemSpan(2) }) {
            Column {
                if (showTitle) {
                    SelectionContainer {
                        Text(text = context.name, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.headlineSmall, modifier = rowModifier.padding(bottom = dimensionResource(
                            id = R.dimen.common_page_medium_gap_vertical
                        )))
                    }
                }
                SelectionContainer {
                    Text(text = context.overview, color = colorResource(id = com.google.android.material.R.color.material_on_background_emphasis_medium), style = MaterialTheme.typography.bodyLarge, modifier = rowModifier.padding(bottom = dimensionResource(
                        id = R.dimen.common_page_medium_gap_vertical
                    )))
                }
                cockpitStatus?.let { enabled ->
                    SwitchRow(primaryText = CelestiaString("Use as Cockpit","Option to use a spacecraft as cockpit"), checked = enabled, onCheckedChange = {
                        cockpitStatus = it
                        scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                            viewModel.appCore.simulation.activeObserver.cockpit = if (it) selection else Selection()
                        }
                    }, horizontalPadding = 0.dp, canTapWholeRow = false)
                }
                context.webInfoURL?.let { url ->
                    LinkPreview(url = url, modifier = rowModifier.padding(bottom = dimensionResource(id = R.dimen.common_page_medium_gap_vertical)), onClick = { finalURL ->
                        linkClicked(finalURL.toString())
                    })
                }
            }
        }

        val count = context.actions.size
        items(context.actions.mapIndexed { index, infoActionItem -> Pair(index, infoActionItem) }) { item ->
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
                            val surfaces = ArrayList<String>()
                            surfaces.add(CelestiaString("Default", ""))
                            surfaces.addAll(action.names)
                            alert = InfoAlert.AlternativeSurfaceSelection(surfaces)
                        }
                        is InfoNormalActionItem -> {
                            scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                                viewModel.appCore.simulation.selection = selection
                                viewModel.appCore.perform(action.item)
                            }
                        }
                        is InfoSelectActionItem -> {
                            scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                                viewModel.appCore.simulation.selection = selection
                            }
                        }
                        is InfoWebActionItem -> {
                            linkClicked(action.url)
                        }
                        is MarkItem -> {
                            alert = InfoAlert.MarkerSelection
                        }
                        is SubsystemActionItem -> {
                            openSubsystem()
                        }
                        is RelatedAddonItem -> {
                            if (viewModel.purchaseManager.purchaseToken() == null) {
                                alert = InfoAlert.RequireCelestiaPlus
                            } else {
                                openRelatedAddons(action.objectPath)
                            }
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
                val markers = InfoFragment.getAvailableMarkers()
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
            is InfoAlert.RequireCelestiaPlus -> {
                SimpleAlertDialog(onDismissRequest = {
                    alert = null
                }, onConfirm = {
                    alert = null
                    openSubscriptionManagement()
                }, confirmButtonText = CelestiaString("Get Celestia PLUS", "Action button to get Celestia PLUS"), title = CelestiaString("Related Add-ons", "Alert title for add-ons that are related to an object"), text = CelestiaString("To access related add-ons, you need to have an active Celestia PLUS subscription.", "Alert message for requiring Celestia PLUS to access add-ons that are related to an object"))
            }
        }
    }
}