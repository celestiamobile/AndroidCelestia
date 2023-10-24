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

import android.content.Context
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import dagger.hilt.android.AndroidEntryPoint
import space.celestia.celestia.AppCore
import space.celestia.celestia.Utils
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.compose.TextRow
import space.celestia.mobilecelestia.utils.CelestiaString
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class SettingsCurrentTimeFragment : NavigationFragment.SubFragment(), SettingsBaseFragment {
    private val formatter by lazy { DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault()) }

    private var listener: Listener? = null

    @Inject
    lateinit var appCore: AppCore

    private var currentTime = mutableStateOf(Date())
    private var currentJulianDay = mutableStateOf(0.0)

    private lateinit var displayNumberFormat: NumberFormat

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        displayNumberFormat = NumberFormat.getNumberInstance()
        displayNumberFormat.isGroupingUsed = false
        displayNumberFormat.maximumFractionDigits = 4

        val time = appCore.simulation.time
        currentTime.value = Utils.createDateFromJulianDay(time)
        currentJulianDay.value = time
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement SettingsCurrentTimeFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    @Composable
    fun MainScreen() {
        val nestedScrollInterop = rememberNestedScrollInteropConnection()
        Column(modifier = Modifier
            .nestedScroll(nestedScrollInterop)
            .verticalScroll(state = rememberScrollState(), enabled = true)
            .systemBarsPadding()) {
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
            TextRow(primaryText = CelestiaString("Select Time", ""), secondaryText = formatter.format(currentTime.value), modifier = Modifier.clickable(onClick = {
                listener?.onPickTime()
            }))
            TextRow(primaryText = CelestiaString("Julian Day", ""), secondaryText = displayNumberFormat.format(currentJulianDay.value), modifier = Modifier.clickable {
                listener?.onPickJulianDay()
            })
            TextRow(primaryText = CelestiaString("Set to Current Time", ""), modifier = Modifier.clickable(onClick = {
                listener?.onSyncWithCurrentTime()
            }))
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_tall)))
        }
    }

    override fun reload() {
        val time = appCore.simulation.time
        currentTime.value = Utils.createDateFromJulianDay(time)
        currentJulianDay.value = time
    }

    interface Listener {
        fun onPickTime()
        fun onPickJulianDay()
        fun onSyncWithCurrentTime()
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SettingsCurrentTimeFragment()
    }
}
