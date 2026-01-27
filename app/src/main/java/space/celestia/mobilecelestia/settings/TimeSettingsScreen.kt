// TimeSettingsScreen.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import space.celestia.celestia.Utils
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.DateInputDialog
import space.celestia.mobilecelestia.compose.SimpleAlertDialog
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


private sealed class TimeSettingsAlert {
    data object BadTimeString: TimeSettingsAlert()
    data object BadJulianDay: TimeSettingsAlert()
    data object TimeInput: TimeSettingsAlert()
    data object JulianDayInput: TimeSettingsAlert()
}

@Composable
fun TimeSettingsScreen(paddingValues: PaddingValues) {
    val nestedScrollInterop = rememberNestedScrollInteropConnection()
    val displayNumberFormat by remember {
        val numberFormat = NumberFormat.getNumberInstance()
        numberFormat.isGroupingUsed = false
        numberFormat.maximumFractionDigits = 4
        mutableStateOf(
            numberFormat
        )
    }
    val dateTimeDisplayFormatter by remember { mutableStateOf(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())) }

    val viewModel: SettingsViewModel = hiltViewModel()
    var currentJulianDay by remember { mutableDoubleStateOf(viewModel.appCore.simulation.time) }
    var currentTime by remember { mutableStateOf(Utils.createDateFromJulianDay(currentJulianDay)) }
    var alert by remember { mutableStateOf<TimeSettingsAlert?>(null) }

    val scope = rememberCoroutineScope()
    Column(modifier = Modifier
        .nestedScroll(nestedScrollInterop)
        .verticalScroll(state = rememberScrollState(), enabled = true)
        .padding(paddingValues)) {
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
        TextRow(primaryText = CelestiaString("Select Time", "Select simulation time"), secondaryText = dateTimeDisplayFormatter.format(currentTime), modifier = Modifier.clickable(onClick = {
            alert = TimeSettingsAlert.TimeInput
        }))
        TextRow(primaryText = CelestiaString("Julian Day", "Select time via entering Julian day"), secondaryText = displayNumberFormat.format(currentJulianDay), modifier = Modifier.clickable {
            alert = TimeSettingsAlert.JulianDayInput
        })
        TextRow(primaryText = CelestiaString("Set to Current Time", "Set simulation time to device"), modifier = Modifier.clickable(onClick = {
            scope.launch {
                val julianDay = withContext(viewModel.executor.asCoroutineDispatcher()) {
                    viewModel.appCore.charEnter(CelestiaAction.CurrentTime.value)
                    return@withContext viewModel.appCore.simulation.time
                }
                currentTime = Utils.createDateFromJulianDay(julianDay)
                currentJulianDay = julianDay
            }
        }))
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_tall)))
    }

    alert?.let { content ->
        when (content) {
            is TimeSettingsAlert.BadTimeString -> {
                SimpleAlertDialog(onDismissRequest = {
                    alert = null
                }, onConfirm = {
                    alert = null
                }, title = CelestiaString("Unrecognized time string.", "String not in correct format"))
            }
            is TimeSettingsAlert.BadJulianDay -> {
                SimpleAlertDialog(onDismissRequest = {
                    alert = null
                }, onConfirm = {
                    alert = null
                }, title = CelestiaString("Invalid Julian day string.", "The input of julian day is not valid"))
            }
            is TimeSettingsAlert.TimeInput -> {
                DateInputDialog(onDismissRequest = {
                    alert = null
                }, errorHandler = {
                    alert = TimeSettingsAlert.BadTimeString
                }) {
                    alert = null
                    currentTime = it
                    currentJulianDay = it.julianDay
                }
            }
            is TimeSettingsAlert.JulianDayInput -> {
                TextInputDialog(
                    onDismissRequest = {
                        alert = null
                    },
                    title = CelestiaString("Please enter Julian day.", "In time settings, enter Julian day for the simulation")
                ) {
                    alert = null
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
                       alert = TimeSettingsAlert.BadJulianDay
                    }
                }
            }
        }
    }
}