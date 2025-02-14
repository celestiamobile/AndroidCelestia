/*
 * TimeSettingsScreen.kt
 *
 * Copyright (C) 2025-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import space.celestia.celestia.Utils
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.DateInputDialog
import space.celestia.mobilecelestia.compose.TextInputDialog
import space.celestia.mobilecelestia.compose.TextRow
import space.celestia.mobilecelestia.info.model.CelestiaAction
import space.celestia.mobilecelestia.settings.viewmodel.SettingsViewModel
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.julianDay
import space.celestia.mobilecelestia.utils.toDoubleOrNull
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TimeSettingsScreen(paddingValues: PaddingValues, modifier: Modifier = Modifier) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    val formatter = remember { DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault()) }
    val displayNumberFormat = remember {
        val numberFormat = NumberFormat.getNumberInstance()
        numberFormat.isGroupingUsed = false
        numberFormat.maximumFractionDigits = 4
        numberFormat
    }
    var showJulianDayDialog by remember { mutableStateOf(false)  }
    var showTimeDialog by remember { mutableStateOf(false)  }
    var currentJulianDay by remember { mutableDoubleStateOf(viewModel.appCore.simulation.time) }
    var currentTime by remember { mutableStateOf(Utils.createDateFromJulianDay(currentJulianDay)) }
    Column(modifier = modifier
        .verticalScroll(state = rememberScrollState(), enabled = true)
        .padding(paddingValues)) {
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
        TextRow(primaryText = CelestiaString("Select Time", "Select simulation time"), secondaryText = formatter.format(currentTime), modifier = Modifier.clickable(onClick = {
            showTimeDialog = true
        }))
        TextRow(primaryText = CelestiaString("Julian Day", "Select time via entering Julian day"), secondaryText = displayNumberFormat.format(currentJulianDay), modifier = Modifier.clickable {
            showJulianDayDialog = true
        })
        TextRow(primaryText = CelestiaString("Set to Current Time", "Set simulation time to device"), modifier = Modifier.clickable(onClick = {
            scope.launch {
                withContext(viewModel.executor.asCoroutineDispatcher()) {
                    viewModel.appCore.charEnter(CelestiaAction.CurrentTime.value)
                }
                val time = viewModel.appCore.simulation.time
                currentTime = Utils.createDateFromJulianDay(time)
                currentJulianDay = time
            }
        }))
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_tall)))
    }

    var errorText: String? by remember {
        mutableStateOf(null)
    }

    if (showJulianDayDialog) {
        TextInputDialog(onDismissRequest = {
            showJulianDayDialog = false
        }, title = CelestiaString("Please enter Julian day.", "In time settings, enter Julian day for the simulation"),
        ) {
            showJulianDayDialog = false
            val numberFormat = NumberFormat.getNumberInstance()
            numberFormat.isGroupingUsed = false
            val value = it?.toDoubleOrNull(numberFormat)
            if (value != null) {
                scope.launch {
                    withContext(viewModel.executor.asCoroutineDispatcher()) {
                        viewModel.appCore.simulation.time = value
                    }
                    currentTime = Utils.createDateFromJulianDay(value)
                    currentJulianDay = value
                }
            } else {
                errorText = CelestiaString("Invalid Julian day string.", "The input of julian day is not valid")
            }
        }
    }

    if (showTimeDialog) {
        DateInputDialog(onDismissRequest = {
            showTimeDialog = false
        }, errorHandler = {
            showTimeDialog = false
            errorText = it
        }) {
            showTimeDialog = false
            scope.launch {
                val julianDay = it.julianDay
                withContext(viewModel.executor.asCoroutineDispatcher()) {
                    viewModel.appCore.simulation.time = julianDay
                }
                currentTime = it
                currentJulianDay = julianDay
            }
        }
    }

    errorText?.let {
        AlertDialog(onDismissRequest = {
            errorText = null
        }, confirmButton = {
            TextButton(onClick = {
                errorText = null
            }) {
                Text(text = CelestiaString("OK", ""))
            }
        }, title = {
            Text(text = it)
        })
    }
}