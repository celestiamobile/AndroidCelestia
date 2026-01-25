// RenderInfoScreen.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.settings

import android.os.Build
import android.view.Display
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import space.celestia.celestia.Renderer
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.RadioButtonRow
import space.celestia.mobilecelestia.settings.viewmodel.SettingsViewModel
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.PreferenceManager
import java.text.NumberFormat

@Composable
fun RefreshRateSettingsScreen(paddingValues: PaddingValues, providePreferredDisplay: () -> Display?, refreshRateChanged: (Int) -> Unit) {
    fun availableRefreshRates(): Pair<List<Pair<Int, Int>>, Int>? {
        val display = providePreferredDisplay() ?: return null
        @Suppress("DEPRECATION")
        val supportedRefreshRates = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) display.supportedModes.map { it.refreshRate } else display.supportedRefreshRates.toList()
        val maxRefreshRate = supportedRefreshRates.maxOrNull()?.toInt() ?: return null
        return Pair(listOf(
            Pair(Renderer.FRAME_60FPS, 60),
            Pair(Renderer.FRAME_30FPS, 30),
            Pair(Renderer.FRAME_20FPS, 20),
        ).filter { it.second <= maxRefreshRate }, maxRefreshRate)
    }

    val viewModel: SettingsViewModel = hiltViewModel()
    var currentRefreshRateOption by remember {
        mutableIntStateOf(viewModel.appSettings[PreferenceManager.PredefinedKey.FrameRateOption]?.toIntOrNull() ?: Renderer.FRAME_60FPS)
    }
    val numberFormat by remember {
        mutableStateOf(NumberFormat.getNumberInstance().apply { isGroupingUsed = true })
    }

    val (options, max) = availableRefreshRates()?: return
    val nestedScrollInterop = rememberNestedScrollInteropConnection()
    LazyColumn(modifier = Modifier.nestedScroll(nestedScrollInterop), contentPadding = paddingValues) {
        item {
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
            RadioButtonRow(primaryText = CelestiaString("Maximum (%s FPS)", "").format(numberFormat.format(max)), selected = currentRefreshRateOption == Renderer.FRAME_MAX) {
                viewModel.appSettings[PreferenceManager.PredefinedKey.FrameRateOption] = Renderer.FRAME_MAX.toString()
                currentRefreshRateOption = Renderer.FRAME_MAX
                refreshRateChanged(Renderer.FRAME_MAX)
            }
        }

        items(options) {
            RadioButtonRow(primaryText = CelestiaString("%s FPS", "").format(numberFormat.format(it.second)), selected = currentRefreshRateOption == it.first) {
                viewModel.appSettings[PreferenceManager.PredefinedKey.FrameRateOption] = it.first.toString()
                currentRefreshRateOption = it.first
                refreshRateChanged(it.first)
            }
        }

        item {
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_tall)))
        }
    }
}