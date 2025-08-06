// EventFinderResultFragment.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.eventfinder

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import space.celestia.celestia.EclipseFinder
import space.celestia.celestia.Utils
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.compose.EmptyHint
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.compose.TextRow
import space.celestia.mobilecelestia.utils.CelestiaString
import java.text.DateFormat
import java.util.Locale

class EventFinderResultFragment : NavigationFragment.SubFragment() {
    private var listener: Listener? = null
    private val formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())

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
                    MainScreen()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = CelestiaString("Eclipse Finder", "")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement EventFinderResultFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    @Composable
    private fun MainScreen() {
        if (eclipses.isEmpty()) {
            Box(modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(), contentAlignment = Alignment.Center) {
                EmptyHint(text = CelestiaString("No eclipse is found for the given object in the time range", ""))
            }
        } else {
            val systemPadding = WindowInsets.systemBars.asPaddingValues()
            val direction = LocalLayoutDirection.current
            val contentPadding = PaddingValues(
                start = systemPadding.calculateStartPadding(direction),
                top = dimensionResource(id = R.dimen.list_spacing_short) + systemPadding.calculateTopPadding(),
                end = systemPadding.calculateEndPadding(direction),
                bottom = dimensionResource(id = R.dimen.list_spacing_tall) + systemPadding.calculateBottomPadding(),
            )
            LazyColumn(
                contentPadding = contentPadding,
                modifier = Modifier.nestedScroll(rememberNestedScrollInteropConnection())
            ) {
                items(eclipses) { eclipse ->
                    TextRow(primaryText = "${eclipse.occulter.name} -> ${eclipse.receiver.name}", secondaryText = formatter.format(Utils.createDateFromJulianDay(eclipse.startTimeJulian)), modifier = Modifier.clickable {
                        listener?.onEclipseChosen(eclipse)
                    })
                }
            }
        }
    }

    interface Listener {
        fun onEclipseChosen(eclipse: EclipseFinder.Eclipse)
    }

    companion object {
        private const val TAG = "EventFinderResult"

        var eclipses: List<EclipseFinder.Eclipse> = listOf()

        @JvmStatic
        fun newInstance() =
            EventFinderResultFragment()
    }
}
