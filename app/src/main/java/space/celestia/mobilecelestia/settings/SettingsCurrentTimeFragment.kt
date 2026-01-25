// SettingsCurrentTimeFragment.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import space.celestia.celestia.Utils
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.compose.TextRow
import space.celestia.mobilecelestia.info.model.CelestiaAction
import space.celestia.mobilecelestia.settings.viewmodel.SettingsViewModel
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.julianDay
import space.celestia.mobilecelestia.utils.showAlert
import space.celestia.mobilecelestia.utils.showDateInput
import space.celestia.mobilecelestia.utils.showTextInput
import space.celestia.mobilecelestia.utils.toDoubleOrNull
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TimeSettings(paddingValues: PaddingValues) {
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

    val localActivity = LocalActivity.current
    val scope = rememberCoroutineScope()
    Column(modifier = Modifier
        .nestedScroll(nestedScrollInterop)
        .verticalScroll(state = rememberScrollState(), enabled = true)
        .padding(paddingValues)) {
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
        TextRow(primaryText = CelestiaString("Select Time", "Select simulation time"), secondaryText = dateTimeDisplayFormatter.format(currentTime), modifier = Modifier.clickable(onClick = {
            val activity = localActivity ?: return@clickable
            val format = android.text.format.DateFormat.getBestDateTimePattern(
                Locale.getDefault(),
                "yyyyMMddHHmmss"
            )
            activity.showDateInput(
                CelestiaString("Please enter the time in \"%s\" format.", "").format(format), format
            ) { date ->
                if (date == null) {
                    activity.showAlert(CelestiaString("Unrecognized time string.", "String not in correct format"))
                    return@showDateInput
                }
                scope.launch {
                    withContext(viewModel.executor.asCoroutineDispatcher()) {
                        viewModel.appCore.simulation.time = date.julianDay
                    }
                    currentTime = date
                    currentJulianDay = date.julianDay
                }
            }
        }))
        TextRow(primaryText = CelestiaString("Julian Day", "Select time via entering Julian day"), secondaryText = displayNumberFormat.format(currentJulianDay), modifier = Modifier.clickable {
            val activity = localActivity ?: return@clickable
            val numberFormat = NumberFormat.getNumberInstance()
            numberFormat.isGroupingUsed = false
            activity.showTextInput(title = CelestiaString("Please enter Julian day.", "In time settings, enter Julian day for the simulation")) { julianDayString ->
                val value = julianDayString.toDoubleOrNull(numberFormat)
                if (value == null) {
                    activity.showAlert(CelestiaString("Invalid Julian day string.", "The input of julian day is not valid"))
                    return@showTextInput
                }
                scope.launch {
                    withContext(viewModel.executor.asCoroutineDispatcher()) {
                        viewModel.appCore.simulation.time = value
                    }
                    currentTime = Utils.createDateFromJulianDay(value)
                    currentJulianDay = value
                }
            }
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
}

@AndroidEntryPoint
class SettingsCurrentTimeFragment : NavigationFragment.SubFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Mdc3Theme {
                    TimeSettings(paddingValues = WindowInsets.systemBars.asPaddingValues())
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = CelestiaString("Current Time", "")
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SettingsCurrentTimeFragment()
    }
}
