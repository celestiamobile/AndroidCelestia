/*
 * EventFinderResultScreen.kt
 *
 * Copyright (C) 2025-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.eventfinder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import space.celestia.celestia.Utils
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.EmptyHint
import space.celestia.mobilecelestia.compose.TextRow
import space.celestia.mobilecelestia.eventfinder.viewmodel.EventFinderViewModel
import space.celestia.mobilecelestia.utils.CelestiaString
import java.text.DateFormat
import java.util.Locale

@Composable
fun EventFinderResultScreen(paddingValues: PaddingValues, modifier: Modifier = Modifier) {
    val viewModel: EventFinderViewModel = hiltViewModel()
    val formatter by remember { mutableStateOf(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())) }
    val scope = rememberCoroutineScope()
    if (viewModel.eclipseResults.isEmpty()) {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues), contentAlignment = Alignment.Center) {
            EmptyHint(text = CelestiaString("No eclipse is found for the given object in the time range", ""))
        }
    } else {
        val direction = LocalLayoutDirection.current
        val contentPadding = PaddingValues(
            start = paddingValues.calculateStartPadding(direction),
            top = dimensionResource(id = R.dimen.list_spacing_short) + paddingValues.calculateTopPadding(),
            end = paddingValues.calculateEndPadding(direction),
            bottom = dimensionResource(id = R.dimen.list_spacing_tall) + paddingValues.calculateBottomPadding(),
        )
        LazyColumn(
            contentPadding = contentPadding,
            modifier = modifier.nestedScroll(rememberNestedScrollInteropConnection())
        ) {
            items(viewModel.eclipseResults) { eclipse ->
                TextRow(primaryText = "${eclipse.occulter.name} -> ${eclipse.receiver.name}", secondaryText = formatter.format(
                    Utils.createDateFromJulianDay(eclipse.startTimeJulian)), modifier = Modifier.clickable {
                    scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                        viewModel.appCore.simulation.goToEclipse(eclipse)
                    }
                })
            }
        }
    }
}