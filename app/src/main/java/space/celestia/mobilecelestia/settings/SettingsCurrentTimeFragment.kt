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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.dimensionResource
import com.google.accompanist.themeadapter.material3.Mdc3Theme
import dagger.hilt.android.AndroidEntryPoint
import space.celestia.celestia.AppCore
import space.celestia.celestia.Utils
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.compose.TextRow
import space.celestia.mobilecelestia.utils.CelestiaString
import java.text.DateFormat
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        currentTime.value = Utils.createDateFromJulianDay(appCore.simulation.time)
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
        Column(modifier = Modifier
            .verticalScroll(state = rememberScrollState(), enabled = true)
            .systemBarsPadding()) {
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
            TextRow(primaryText = CelestiaString("Select Time", ""), secondaryText = formatter.format(currentTime.value), accessoryResource = R.drawable.accessory_full_disclosure, modifier = Modifier.clickable(onClick = {
                listener?.onPickTime()
            }))
            TextRow(primaryText = CelestiaString("Set to Current Time", ""), accessoryResource = R.drawable.accessory_full_disclosure, modifier = Modifier.clickable(onClick = {
                listener?.onSyncWithCurrentTime()
            }))
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_tall)))
        }
    }

    override fun reload() {
        currentTime.value = Utils.createDateFromJulianDay(appCore.simulation.time)
    }

    interface Listener {
        fun onPickTime()
        fun onSyncWithCurrentTime()
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SettingsCurrentTimeFragment()
    }
}
