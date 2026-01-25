// GoToScreen.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.travel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import space.celestia.celestia.GoToLocation
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.Header
import space.celestia.mobilecelestia.compose.ObjectNameAutoComplete
import space.celestia.mobilecelestia.compose.OptionSelect
import space.celestia.mobilecelestia.travel.viewmodel.GoToViewModel
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.toDoubleOrNull
import java.text.NumberFormat

@Composable
fun GoToScreen(paddingValues: PaddingValues) {
    var parseNumberFormat by remember {
        val numberFormat = NumberFormat.getNumberInstance()
        numberFormat.isGroupingUsed = false
        mutableStateOf(numberFormat)
    }
    var displayNumberFormat by remember {
        val numberFormat = NumberFormat.getNumberInstance()
        numberFormat.maximumFractionDigits = 2
        numberFormat.isGroupingUsed = false
        mutableStateOf(numberFormat)
    }
    val viewModel: GoToViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    var longitudeString by rememberSaveable {
        mutableStateOf(displayNumberFormat.format(viewModel.initialLongitude))
    }
    var latitudeString by rememberSaveable {
        mutableStateOf(displayNumberFormat.format(viewModel.initialLatitude))
    }
    var distanceString by rememberSaveable {
        mutableStateOf(displayNumberFormat.format(viewModel.initialDistance))
    }
    val textViewModifier = Modifier
        .fillMaxWidth()
        .padding(
            horizontal = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal),
            vertical = dimensionResource(id = R.dimen.common_page_medium_gap_vertical),
        )
    val currentLongitudeValue = longitudeString.toDoubleOrNull(parseNumberFormat)
    val currentLatitudeValue = latitudeString.toDoubleOrNull(parseNumberFormat)
    val currentDistanceValue = distanceString.toDoubleOrNull(parseNumberFormat)
    val isLongitudeValid = currentLongitudeValue != null && currentLongitudeValue >= -180.0 && currentLongitudeValue <= 180.0
    val isLatitudeValid = currentLatitudeValue != null && currentLatitudeValue >= -90.0 && currentLatitudeValue <= 90.0
    val isDistanceValid = currentDistanceValue != null && currentDistanceValue >= 0.0
    var showObjectNotFoundDialog by remember { mutableStateOf(false)  }
    val nestedScrollInterop = rememberNestedScrollInteropConnection()
    Column(modifier = Modifier
        .nestedScroll(nestedScrollInterop)
        .verticalScroll(state = rememberScrollState(), enabled = true)
        .padding(paddingValues)) {
        Header(text = CelestiaString("Object", "In eclipse finder, object to find eclipse with, or in go to"))
        ObjectNameAutoComplete(executor = viewModel.executor, core = viewModel.appCore, name = viewModel.objectName.value, selection = viewModel.selection.value, modifier = textViewModifier, inputUpdated = {
            viewModel.objectName.value = it
        }, selectionUpdated = {
            viewModel.selection.value = it
        })
        Header(text = CelestiaString("Coordinates", "Longitude and latitude (in Go to)"))
        Row(modifier = textViewModifier, horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.list_item_gap_horizontal))) {
            OutlinedTextField(value = latitudeString, label = { Text(text = CelestiaString("Latitude", "Coordinates")) }, onValueChange = {
                latitudeString = it
            }, isError = !isLatitudeValid, modifier = Modifier.weight(1.0f))
            OutlinedTextField(value = longitudeString, label = { Text(text = CelestiaString("Longitude", "Coordinates")) }, onValueChange = {
                longitudeString = it
            }, isError = !isLongitudeValid, modifier = Modifier.weight(1.0f))
        }
        Header(text = CelestiaString("Distance", "Distance to the object (in Go to)"))
        Row(modifier = textViewModifier, horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.list_item_gap_horizontal))) {
            OutlinedTextField(value = distanceString, onValueChange = {
                distanceString = it
            }, isError = !isDistanceValid, modifier = Modifier.weight(1.0f))
            OptionSelect(options = GoToViewModel.distanceUnits.map {
                when(it) {
                    GoToLocation.DistanceUnit.radii -> {
                        CelestiaString("radii", "In Go to, specify the distance based on the object radius")
                    }
                    GoToLocation.DistanceUnit.km -> {
                        CelestiaString("km", "Unit")
                    }
                    GoToLocation.DistanceUnit.au -> {
                        CelestiaString("au", "Astronomical unit")
                    }
                }
            }, selectedIndex = GoToViewModel.distanceUnits.indexOf(viewModel.distanceUnit.value) , selectionChange = {
                viewModel.distanceUnit.value = GoToViewModel.distanceUnits[it]
            })
        }
        FilledTonalButton(
            enabled = isLatitudeValid && isLongitudeValid && isDistanceValid,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = dimensionResource(id = R.dimen.common_page_medium_margin_horizontal),
                    top = dimensionResource(id = R.dimen.common_page_medium_gap_vertical),
                    end = dimensionResource(id = R.dimen.common_page_medium_margin_horizontal),
                    bottom = dimensionResource(id = R.dimen.common_page_medium_margin_vertical)
                ),
            onClick = {
                val longitude = currentLongitudeValue ?: return@FilledTonalButton
                val latitude = currentLatitudeValue ?: return@FilledTonalButton
                val distance = currentDistanceValue ?: return@FilledTonalButton
                if (viewModel.selection.value.isEmpty) {
                    showObjectNotFoundDialog = true
                    return@FilledTonalButton
                }

                val location = GoToLocation(
                    viewModel.selection.value,
                    longitude.toFloat(),
                    latitude.toFloat(),
                    distance,
                    viewModel.distanceUnit.value
                )
                scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                    viewModel.appCore.simulation.goToLocation(location)
                }
            }
        ) {
            Text(text = CelestiaString("Go", "Go to an object"))
        }
    }

    if (showObjectNotFoundDialog) {
        AlertDialog(onDismissRequest = {
            showObjectNotFoundDialog = false
        }, confirmButton = {
            TextButton(onClick = {
                showObjectNotFoundDialog = false
            }) {
                Text(text = CelestiaString("OK", ""))
            }
        }, title = {
            Text(text = CelestiaString("Object not found", ""))
        })
    }
}