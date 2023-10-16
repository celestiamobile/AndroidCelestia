/*
 * SettingsRefreshRateFragment.kt
 *
 * Copyright (C) 2001-2021, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.settings

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.dimensionResource
import androidx.core.hardware.display.DisplayManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import space.celestia.celestia.Renderer
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.compose.RadioButtonRow
import space.celestia.mobilecelestia.utils.CelestiaString
import java.text.NumberFormat

class SettingsRefreshRateFragment : NavigationFragment.SubFragment() {
    private var listener: Listener? = null
    private var dataSource: DataSource? = null
    private var bottomPadding = mutableIntStateOf(0)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Mdc3Theme {
                    MainScreen()
                }
            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, windowInsets ->
            bottomPadding.intValue = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            WindowInsetsCompat.CONSUMED
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = CelestiaString("Frame Rate", "")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement SettingsRefreshRateFragment.Listener")
        }
        if (context is DataSource) {
            dataSource = context
        } else {
            throw RuntimeException("$context must implement SettingsRefreshRateFragment.DataSource")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        dataSource = null
    }

    @Composable
    private fun MainScreen() {
        var currentRefreshRateOption by remember {
            mutableIntStateOf(dataSource?.currentRefreshRateOption() ?: Renderer.FRAME_60FPS)
        }
        val numberFormat by remember {
            mutableStateOf(NumberFormat.getNumberInstance().apply { isGroupingUsed = true })
        }

        val (options, max) = availableRefreshRates()?: return
        LazyColumn {
            item {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
                RadioButtonRow(primaryText = CelestiaString("Maximum (%s FPS)", "").format(numberFormat.format(max)), selected = currentRefreshRateOption == Renderer.FRAME_MAX) {
                    listener?.onRefreshRateChanged(Renderer.FRAME_MAX)
                    currentRefreshRateOption = Renderer.FRAME_MAX
                }
            }

            items(options) {
                RadioButtonRow(primaryText = CelestiaString("%s FPS", "").format(numberFormat.format(it.second)), selected = currentRefreshRateOption == it.first) {
                    listener?.onRefreshRateChanged(it.first)
                    currentRefreshRateOption = it.first
                }
            }

            item {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_tall)))
                with(LocalDensity.current) {
                    Spacer(modifier = Modifier.height(bottomPadding.intValue.toDp()))
                }
            }
        }
    }

    private fun availableRefreshRates(): Pair<List<Pair<Int, Int>>, Int>? {
        val activity = this.activity ?: return null
        val displayManager = DisplayManagerCompat.getInstance(activity)
        val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY) ?: return null
        @Suppress("DEPRECATION")
        val supportedRefreshRates = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) display.supportedModes.map { it.refreshRate } else display.supportedRefreshRates.toList()
        val maxRefreshRate = supportedRefreshRates.maxOrNull()?.toInt() ?: return null
        return Pair(listOf(
            Pair(Renderer.FRAME_60FPS, 60),
            Pair(Renderer.FRAME_30FPS, 30),
            Pair(Renderer.FRAME_20FPS, 20),
        ).filter { it.second <= maxRefreshRate }, maxRefreshRate)
    }

    interface Listener {
        fun onRefreshRateChanged(frameRateOption: Int)
    }

    interface DataSource {
        fun currentRefreshRateOption(): Int
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SettingsRefreshRateFragment()
    }
}
