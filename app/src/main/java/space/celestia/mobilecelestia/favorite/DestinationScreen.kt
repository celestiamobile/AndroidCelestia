/*
 * DestinationScreen.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.favorite

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import space.celestia.celestia.Destination
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.favorite.viewmodel.DestinationViewModel
import space.celestia.mobilecelestia.favorite.viewmodel.FavoriteViewModel
import space.celestia.mobilecelestia.utils.CelestiaString

@Composable
fun DestinationScreen(destination: Destination, paddingValues: PaddingValues, modifier: Modifier = Modifier) {
    val viewModel: DestinationViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    val scroll = rememberScrollState(0)
    val direction = LocalLayoutDirection.current
    val contentPadding = PaddingValues(
        start = paddingValues.calculateStartPadding(direction) + dimensionResource(id = R.dimen.common_page_medium_margin_horizontal),
        top = paddingValues.calculateTopPadding(),
        end = paddingValues.calculateEndPadding(direction) + dimensionResource(id = R.dimen.common_page_medium_margin_horizontal),
        bottom = paddingValues.calculateBottomPadding(),
    )
    Column(modifier = modifier
        .fillMaxSize()
        .padding(contentPadding),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(modifier = Modifier
            .weight(1.0f)
            .fillMaxWidth()) {
            SelectionContainer(
                modifier = Modifier
                    .verticalScroll(scroll)
                    .padding(
                        top = dimensionResource(id = R.dimen.common_page_medium_margin_vertical),
                    )
            ) {
                Text(
                    text = destination.description ?: "",
                    color = colorResource(id = com.google.android.material.R.color.material_on_background_emphasis_medium),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        FilledTonalButton(modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = dimensionResource(id = R.dimen.common_page_medium_gap_vertical),
                bottom = dimensionResource(id = R.dimen.common_page_medium_margin_vertical),
            ), onClick = {
            scope.launch(viewModel.executor.asCoroutineDispatcher()) { viewModel.appCore.simulation.goTo(destination) }
        }) {
            Text(text = CelestiaString("Go", "Go to an object"))
        }
    }
}