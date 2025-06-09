/*
 * SettingsCurrentTimeFragment.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import space.celestia.celestia.AppCore
import space.celestia.celestia.Utils
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.CelestiaExecutor
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.compose.TextRow
import space.celestia.mobilecelestia.info.model.CelestiaAction
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.julianDay
import space.celestia.mobilecelestia.utils.showAlert
import space.celestia.mobilecelestia.utils.showDateInput
import space.celestia.mobilecelestia.utils.showTextInput
import space.celestia.mobilecelestia.utils.toDoubleOrNull
import java.lang.ref.WeakReference
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class SettingsCurrentTimeFragment : NavigationFragment.SubFragment() {
    private val formatter by lazy { DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault()) }
    private val displayNumberFormat by lazy { NumberFormat.getNumberInstance() }

    @Inject
    lateinit var appCore: AppCore
    @Inject
    lateinit var executor: CelestiaExecutor

    private var currentTime = mutableStateOf(Date())
    private var currentJulianDay = mutableDoubleStateOf(0.0)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        displayNumberFormat.isGroupingUsed = false
        displayNumberFormat.maximumFractionDigits = 4

        reload()
        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Mdc3Theme {
                    MainScreen()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = CelestiaString("Current Time", "")
    }

    @Composable
    fun MainScreen() {
        val nestedScrollInterop = rememberNestedScrollInteropConnection()
        Column(modifier = Modifier
            .nestedScroll(nestedScrollInterop)
            .verticalScroll(state = rememberScrollState(), enabled = true)
            .systemBarsPadding()) {
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
            TextRow(primaryText = CelestiaString("Select Time", "Select simulation time"), secondaryText = formatter.format(currentTime.value), modifier = Modifier.clickable(onClick = {
                onPickTime()
            }))
            TextRow(primaryText = CelestiaString("Julian Day", "Select time via entering Julian day"), secondaryText = displayNumberFormat.format(currentJulianDay.doubleValue), modifier = Modifier.clickable {
                onPickJulianDay()
            })
            TextRow(primaryText = CelestiaString("Set to Current Time", "Set simulation time to device"), modifier = Modifier.clickable(onClick = {
                onSyncWithCurrentTime()
            }))
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_tall)))
        }
    }

    private fun onPickTime() {
        val activity = this.activity ?: return
        val format = android.text.format.DateFormat.getBestDateTimePattern(
            Locale.getDefault(),
            "yyyyMMddHHmmss"
        )
        val weakSelf = WeakReference(this)
        activity.showDateInput(
            CelestiaString(
                "Please enter the time in \"%s\" format.",
                ""
            ).format(format), format
        ) { date ->
            val self = weakSelf.get() ?: return@showDateInput
            val innerActivity = self.activity ?: return@showDateInput
            if (date == null) {
                innerActivity.showAlert(CelestiaString("Unrecognized time string.", "String not in correct format"))
                return@showDateInput
            }
            self.lifecycleScope.launch {
                withContext(self.executor.asCoroutineDispatcher()) {
                    self.appCore.simulation.time = date.julianDay
                }
                self.reload()
            }
        }
    }

    private fun onPickJulianDay() {
        val activity = this.activity ?: return
        val numberFormat = NumberFormat.getNumberInstance()
        numberFormat.isGroupingUsed = false
        val weakSelf = WeakReference(this)
        activity.showTextInput(title = CelestiaString("Please enter Julian day.", "In time settings, enter Julian day for the simulation")) { julianDayString ->
            val self = weakSelf.get() ?: return@showTextInput
            val innerActivity = self.activity ?: return@showTextInput
            val value = julianDayString.toDoubleOrNull(numberFormat)
            if (value == null) {
                innerActivity.showAlert(CelestiaString("Invalid Julian day string.", "The input of julian day is not valid"))
                return@showTextInput
            }
            self.lifecycleScope.launch {
                withContext(self.executor.asCoroutineDispatcher()) {
                    self.appCore.simulation.time = value
                }
                self.reload()
            }
        }
    }

    private fun onSyncWithCurrentTime() {
        lifecycleScope.launch {
            withContext(executor.asCoroutineDispatcher()) { appCore.charEnter(CelestiaAction.CurrentTime.value) }
            reload()
        }
    }

    private fun reload() {
        val time = appCore.simulation.time
        currentTime.value = Utils.createDateFromJulianDay(time)
        currentJulianDay.doubleValue = time
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SettingsCurrentTimeFragment()
    }
}
