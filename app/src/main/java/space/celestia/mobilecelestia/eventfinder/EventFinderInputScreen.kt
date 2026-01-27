// EventFinderInputScreen.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.eventfinder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import space.celestia.celestia.AppCore
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.DateInputDialog
import space.celestia.mobilecelestia.compose.OptionInputDialog
import space.celestia.mobilecelestia.compose.Separator
import space.celestia.mobilecelestia.compose.SimpleAlertDialog
import space.celestia.mobilecelestia.compose.TextInputDialog
import space.celestia.mobilecelestia.compose.TextRow
import space.celestia.mobilecelestia.utils.CelestiaString
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
fun EventFinderInputScreen(paddingValues: PaddingValues, handler: (objectName: String, startDate: Date, endDate: Date) -> Unit) {
    val defaultSearchingInterval: Long = 365L * 24 * 60 * 60 * 1000
    var startTime by rememberSaveable { mutableStateOf(Date(Date().time - defaultSearchingInterval)) }
    var endTime by rememberSaveable { mutableStateOf(Date()) }
    var objectName by rememberSaveable { mutableStateOf(AppCore.getLocalizedStringDomain("Earth", "celestia-data")) }
    var objectPath by rememberSaveable { mutableStateOf("Sol/Earth") }
    val formatter by remember { mutableStateOf(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())) }

    var alert by remember { mutableStateOf<EventFinderInputAlert?>(null) }
    val defaultObjects by remember {
        mutableStateOf(listOf(
            Pair(AppCore.getLocalizedStringDomain("Earth", "celestia-data"), "Sol/Earth"),
            Pair(AppCore.getLocalizedStringDomain("Jupiter", "celestia-data"), "Sol/Jupiter"),
        ))
    }

    val direction = LocalLayoutDirection.current
    val contentPadding = PaddingValues(
        start = paddingValues.calculateStartPadding(direction),
        top = dimensionResource(id = R.dimen.list_spacing_short) + paddingValues.calculateTopPadding(),
        end = paddingValues.calculateEndPadding(direction),
        bottom = dimensionResource(id = R.dimen.list_spacing_tall) + paddingValues.calculateBottomPadding(),
    )
    LazyColumn(
        contentPadding = contentPadding,
        modifier = Modifier.nestedScroll(rememberNestedScrollInteropConnection())
    ) {
        item {
            TextRow(primaryText = CelestiaString("Start Time", "In eclipse finder, range of time to find eclipse in"), secondaryText = formatter.format(startTime), modifier = Modifier.clickable {
                alert = EventFinderInputAlert.TimeInput(true)
            })
            TextRow(primaryText = CelestiaString("End Time", "In eclipse finder, range of time to find eclipse in"), secondaryText = formatter.format(endTime), modifier = Modifier.clickable {
                alert = EventFinderInputAlert.TimeInput(false)
            })
        }

        item {
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
            Separator()
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
        }

        item {
            TextRow(primaryText = CelestiaString("Object", "In eclipse finder, object to find eclipse with, or in go to"), secondaryText = objectName, modifier = Modifier.clickable {
                alert = EventFinderInputAlert.ObjectSelection
            })
        }

        item {
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
            FilledTonalButton(modifier = Modifier.fillMaxWidth().padding(
                horizontal = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal),
                vertical = dimensionResource(id = R.dimen.common_page_medium_gap_vertical),
            ), onClick = {
                handler(objectPath, startTime, endTime)
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