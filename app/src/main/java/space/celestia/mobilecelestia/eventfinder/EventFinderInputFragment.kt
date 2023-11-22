/*
 * EventFinderInputFragment.kt
 *
 * Copyright (C) 2023-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.eventfinder

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import space.celestia.celestia.AppCore
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.compose.Separator
import space.celestia.mobilecelestia.compose.TextRow
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.showAlert
import space.celestia.mobilecelestia.utils.showDateInput
import space.celestia.mobilecelestia.utils.showOptions
import space.celestia.mobilecelestia.utils.showTextInput
import java.text.DateFormat
import java.util.Date
import java.util.Locale

class EventFinderInputFragment : NavigationFragment.SubFragment() {
    private var listener: Listener? = null

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
    @Composable
    private fun MainScreen() {
        var startTime by rememberSaveable { mutableStateOf(Date(Date().time - DEFAULT_SEARCHING_INTERVAL)) }
        var endTime by rememberSaveable { mutableStateOf(Date()) }
        var objectName by rememberSaveable { mutableStateOf(AppCore.getLocalizedString("Earth", "celestia-data")) }
        var objectPath by rememberSaveable { mutableStateOf("Sol/Earth") }
        val formatter by remember { mutableStateOf(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())) }

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
            item {
                TextRow(primaryText = CelestiaString("Start Time", ""), secondaryText = formatter.format(startTime), modifier = Modifier.clickable {
                    val ac = activity ?: return@clickable
                    val format = android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "yyyyMMddHHmmss")
                    ac.showDateInput(
                        CelestiaString(
                            "Please enter the time in \"%s\" format.",
                            ""
                        ).format(format), format
                    ) { date ->
                        if (date == null) {
                            ac.showAlert(CelestiaString("Unrecognized time string.", ""))
                            return@showDateInput
                        }
                        startTime = date
                    }
                })
                TextRow(primaryText = CelestiaString("End Time", ""), secondaryText = formatter.format(endTime), modifier = Modifier.clickable {
                    val ac = activity ?: return@clickable
                    val format = android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "yyyyMMddHHmmss")
                    ac.showDateInput(
                        CelestiaString(
                            "Please enter the time in \"%s\" format.",
                            ""
                        ).format(format), format
                    ) { date ->
                        if (date == null) {
                            ac.showAlert(CelestiaString("Unrecognized time string.", ""))
                            return@showDateInput
                        }
                        endTime = date
                    }
                })
            }

            item {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
                Separator()
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
            }

            item {
                TextRow(primaryText = CelestiaString("Object", ""), secondaryText = objectName, modifier = Modifier.clickable {
                    val ac = activity ?: return@clickable
                    val objects = listOf(
                        Pair(AppCore.getLocalizedString("Earth", "celestia-data"), "Sol/Earth"),
                        Pair(AppCore.getLocalizedString("Jupiter", "celestia-data"), "Sol/Jupiter"),
                    )
                    val other = CelestiaString("Other", "")
                    ac.showOptions(
                        CelestiaString("Please choose an object.", ""),
                        objects.map { it.first }.toTypedArray() + other
                    ) { index ->
                        if (index >= objects.size) {
                            // User choose other, show text input for the object name
                            ac.showTextInput(
                                CelestiaString("Please enter an object name.", ""),
                                objectName
                            ) { name ->
                                objectName = name
                                objectPath = name
                            }
                            return@showOptions
                        }
                        objectName = objects[index].first
                        objectPath = objects[index].second
                    }
                })
            }

            item {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
                FilledTonalButton(modifier = Modifier.fillMaxWidth().padding(
                    horizontal = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal),
                    vertical = dimensionResource(id = R.dimen.common_page_medium_gap_vertical),
                ), onClick = {
                    listener?.onSearchForEvent(objectPath, startTime, endTime)
                }) {
                    Text(text = CelestiaString("Find", ""))
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
            throw RuntimeException("$context must implement EventFinderInputFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface Listener {
        fun onSearchForEvent(objectName: String, startDate: Date, endDate: Date)
    }

    companion object {
        private const val DEFAULT_SEARCHING_INTERVAL: Long = 365L * 24 * 60 * 60 * 1000

        @JvmStatic
        fun newInstance() =
            EventFinderInputFragment()
    }
}
