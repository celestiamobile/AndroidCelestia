// EventFinderInputScreen.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.celestiaui.eventfinder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.lifecycle.compose.dropUnlessResumed
import space.celestia.celestia.AppCore
import space.celestia.celestia.EclipseFinder
import space.celestia.celestiaui.R
import space.celestia.celestiaui.compose.CheckboxRow
import space.celestia.celestiaui.compose.DateInputDialog
import space.celestia.celestiaui.compose.GroupedListItem
import space.celestia.celestiaui.compose.groupedListContentPadding
import space.celestia.celestiaui.compose.OptionInputDialog
import space.celestia.celestiaui.compose.SimpleAlertDialog
import space.celestia.celestiaui.compose.TextInputDialog
import space.celestia.celestiaui.compose.TextRow
import space.celestia.celestiaui.utils.CelestiaString
import java.text.DateFormat
import java.util.Date
import java.util.Locale

private sealed class EventFinderInputAlert {
    data object BadTimeString: EventFinderInputAlert()
    data object ObjectSelection: EventFinderInputAlert()
    data class TimeInput(val start: Boolean): EventFinderInputAlert()
    data class ObjectInput(val placeholder: String): EventFinderInputAlert()
}

@Composable
fun EventFinderInputScreen(paddingValues: PaddingValues, handler: (objectName: String, startDate: Date, endDate: Date, kind: Int) -> Unit) {
    val defaultSearchingInterval: Long = 365L * 24 * 60 * 60 * 1000
    val defaultReferenceTime = remember { Date() }
    var startTime by rememberSaveable { mutableStateOf(Date(defaultReferenceTime.time - defaultSearchingInterval)) }
    var endTime by rememberSaveable { mutableStateOf(Date(defaultReferenceTime.time + defaultSearchingInterval)) }
    var objectName by rememberSaveable { mutableStateOf(AppCore.getLocalizedStringDomain("Earth", "celestia-data")) }
    var objectPath by rememberSaveable { mutableStateOf("Sol/Earth") }
    var findSolar by rememberSaveable { mutableStateOf(true) }
    var findLunar by rememberSaveable { mutableStateOf(true) }
    val formatter by remember { mutableStateOf(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())) }

    var alert by remember { mutableStateOf<EventFinderInputAlert?>(null) }
    val defaultObjects by remember {
        mutableStateOf(listOf(
            Pair(AppCore.getLocalizedStringDomain("Earth", "celestia-data"), "Sol/Earth"),
            Pair(AppCore.getLocalizedStringDomain("Jupiter", "celestia-data"), "Sol/Jupiter"),
        ))
    }

    val contentPadding = groupedListContentPadding(paddingValues)
    LazyColumn(
        contentPadding = contentPadding,
        modifier = Modifier.nestedScroll(rememberNestedScrollInteropConnection())
    ) {
        item {
            GroupedListItem(index = 0, count = 2) {
                TextRow(primaryText = CelestiaString("Start Time", "In eclipse finder, range of time to find eclipse in"), secondaryText = formatter.format(startTime), modifier = Modifier.clickable(onClick = dropUnlessResumed {
                    alert = EventFinderInputAlert.TimeInput(true)
                }))
            }
            GroupedListItem(index = 1, count = 2) {
                TextRow(primaryText = CelestiaString("End Time", "In eclipse finder, range of time to find eclipse in"), secondaryText = formatter.format(endTime), modifier = Modifier.clickable(onClick = dropUnlessResumed {
                    alert = EventFinderInputAlert.TimeInput(false)
                }))
            }
        }

        item {
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_group_spacing)))
        }

        item {
            GroupedListItem(index = 0, count = 3) {
                TextRow(primaryText = CelestiaString("Object", "In eclipse finder, object to find eclipse with, or in go to"), secondaryText = objectName, modifier = Modifier.clickable(onClick = dropUnlessResumed {
                    alert = EventFinderInputAlert.ObjectSelection
                }))
            }
            GroupedListItem(index = 1, count = 3) {
                CheckboxRow(primaryText = CelestiaString("Solar", "Solar eclipses."), checked = findSolar, onCheckedChange = { findSolar = it })
            }
            GroupedListItem(index = 2, count = 3) {
                CheckboxRow(primaryText = CelestiaString("Lunar", "Lunar eclipses."), checked = findLunar, onCheckedChange = { findLunar = it })
            }
        }

        item {
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
            FilledTonalButton(enabled = findSolar || findLunar, modifier = Modifier.fillMaxWidth().padding(
                horizontal = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal),
                vertical = dimensionResource(id = R.dimen.common_page_medium_gap_vertical),
            ), onClick = dropUnlessResumed {
                var kind = 0
                if (findSolar) kind = kind or EclipseFinder.ECLIPSE_KIND_SOLAR
                if (findLunar) kind = kind or EclipseFinder.ECLIPSE_KIND_LUNAR
                handler(objectPath, startTime, endTime, kind)
            }) {
                Text(text = CelestiaString("Find", "Find (eclipses)"))
            }
        }
    }

    alert?.let { content ->
        when (content) {
            is EventFinderInputAlert.BadTimeString -> {
                SimpleAlertDialog(onDismissRequest = {
                    alert = null
                }, onConfirm = {
                    alert = null
                }, title = CelestiaString("Unrecognized time string.", "String not in correct format"))
            }
            is EventFinderInputAlert.ObjectSelection -> {
                OptionInputDialog(
                    onDismissRequest = {
                        alert = null
                    },
                    title = CelestiaString("Please choose an object.", "In eclipse finder, choose an object to find eclipse wth"),
                    items = defaultObjects.map { it.first } + listOf(CelestiaString("Other", "Other location labels; Android/iOS, Other objects to choose from in Eclipse Finder"))
                ) { index ->
                    alert = null
                    if (index < defaultObjects.size) {
                        objectName = defaultObjects[index].first
                        objectPath = defaultObjects[index].second
                        return@OptionInputDialog
                    }

                    alert = EventFinderInputAlert.ObjectInput(objectName)
                }
            }
            is EventFinderInputAlert.TimeInput -> {
                DateInputDialog(onDismissRequest = {
                    alert = null
                }, errorHandler = {
                    alert = EventFinderInputAlert.BadTimeString
                }) {
                    alert = null
                    if (content.start) {
                        startTime = it
                    } else {
                        endTime = it
                    }
                }
            }
            is EventFinderInputAlert.ObjectInput -> {
                TextInputDialog(
                    onDismissRequest = {
                        alert = null
                    },
                    title = CelestiaString("Please enter an object name.", "In Go to; Android/iOS, Enter the name of an object in Eclipse Finder"),
                    placeholder = content.placeholder
                ) { name ->
                    alert = null
                    if (name != null) {
                        objectName = name
                        objectPath = name
                    }
                }
            }
        }
    }
}