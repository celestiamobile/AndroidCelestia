// EventFinderInputScreen.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.eventfinder

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import space.celestia.mobilecelestia.compose.OptionInputDialog
import space.celestia.mobilecelestia.compose.Separator
import space.celestia.mobilecelestia.compose.TextRow
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.showDateInput
import space.celestia.mobilecelestia.utils.showTextInput
import java.text.DateFormat
import java.util.Date
import java.util.Locale

private sealed class EventFinderInputAlert {
    data object BadTimeString: EventFinderInputAlert()
    data object ObjectSelection: EventFinderInputAlert()
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
    val activity = LocalActivity.current
    LazyColumn(
        contentPadding = contentPadding,
        modifier = Modifier.nestedScroll(rememberNestedScrollInteropConnection())
    ) {
        item {
            TextRow(primaryText = CelestiaString("Start Time", "In eclipse finder, range of time to find eclipse in"), secondaryText = formatter.format(startTime), modifier = Modifier.clickable {
                val ac = activity ?: return@clickable
                val format = android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "yyyyMMddHHmmss")
                ac.showDateInput(
                    CelestiaString("Please enter the time in \"%s\" format.", "").format(format), format
                ) { date ->
                    if (date == null) {
                        alert = EventFinderInputAlert.BadTimeString
                        return@showDateInput
                    }
                    startTime = date
                }
            })
            TextRow(primaryText = CelestiaString("End Time", "In eclipse finder, range of time to find eclipse in"), secondaryText = formatter.format(endTime), modifier = Modifier.clickable {
                val ac = activity ?: return@clickable
                val format = android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "yyyyMMddHHmmss")
                ac.showDateInput(
                    CelestiaString("Please enter the time in \"%s\" format.", "").format(format), format
                ) { date ->
                    if (date == null) {
                        alert = EventFinderInputAlert.BadTimeString
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
                AlertDialog(onDismissRequest = {
                    alert = null
                }, confirmButton = {
                    TextButton(onClick = {
                        alert = null
                    }) {
                        Text(text = CelestiaString("OK", ""))
                    }
                }, title = {
                    Text(CelestiaString("Unrecognized time string.", "String not in correct format"))
                })
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

                    val ac = activity ?: return@OptionInputDialog
                    // User choose other, show text input for the object name
                    ac.showTextInput(
                        CelestiaString("Please enter an object name.", "In Go to; Android/iOS, Enter the name of an object in Eclipse Finder"),
                        objectName
                    ) { name ->
                        objectName = name
                        objectPath = name
                    }
                }
            }
        }
    }
}