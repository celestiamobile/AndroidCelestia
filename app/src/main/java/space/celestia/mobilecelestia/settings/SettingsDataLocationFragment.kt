/*
 * SettingsDataLocationFragment.kt
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import space.celestia.mobilecelestia.MainActivity
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.compose.Footer
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.compose.Separator
import space.celestia.mobilecelestia.compose.TextRow
import space.celestia.mobilecelestia.utils.CelestiaString

enum class DataType {
    Config, DataDirectory;
}

class SettingsDataLocationFragment : NavigationFragment.SubFragment(), SettingsBaseFragment {
    private var listener: Listener? = null

    private var bottomPadding = mutableIntStateOf(0)
    private var customConfigFilePath = mutableStateOf(MainActivity.customConfigFilePath)
    private var customDataDirPath = mutableStateOf(MainActivity.customDataDirPath)

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

        title = CelestiaString("Data Location", "")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement SettingsDataLocationFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun reload() {
        customConfigFilePath.value = MainActivity.customConfigFilePath
        customDataDirPath.value = MainActivity.customDataDirPath
    }

    @Composable
    private fun MainScreen() {
        val internalViewModifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal),
                vertical = dimensionResource(id = R.dimen.common_page_medium_gap_vertical),
            )
        val nestedScrollInterop = rememberNestedScrollInteropConnection()
        LazyColumn(modifier = Modifier.nestedScroll(nestedScrollInterop)) {
            item {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
            }
            item {
                TextRow(primaryText = CelestiaString("Config File", ""), secondaryText = if (customConfigFilePath.value == null) CelestiaString("Default", "") else CelestiaString("Custom", ""), modifier = Modifier.clickable {
                    listener?.onDataLocationRequested(DataType.Config)
                })
                TextRow(primaryText = CelestiaString("Data Directory", ""), secondaryText = if (customDataDirPath.value == null) CelestiaString("Default", "") else CelestiaString("Custom", ""), modifier = Modifier.clickable {
                    listener?.onDataLocationRequested(DataType.DataDirectory)
                })
            }
            item {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
                Separator()
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
            }
            item {
                FilledTonalButton(modifier = internalViewModifier, onClick = {
                    listener?.onDataLocationNeedReset()
                }) {
                    Text(text = CelestiaString("Reset to Default", ""))
                }
                Footer(text = CelestiaString("Configuration will take effect after a restart.", ""))
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_tall)))
                with(LocalDensity.current) {
                    Spacer(modifier = Modifier.height(bottomPadding.intValue.toDp()))
                }
            }
        }
    }

    interface Listener {
        fun onDataLocationNeedReset()
        fun onDataLocationRequested(dataType: DataType)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SettingsDataLocationFragment()
    }
}
