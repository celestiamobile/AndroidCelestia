// EventFinderResultsScreen.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.celestiaui.eventfinder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.lifecycle.compose.dropUnlessResumed
import space.celestia.celestia.EclipseFinder
import space.celestia.celestia.Simulation
import space.celestia.celestia.Utils
import space.celestia.celestiaui.R
import space.celestia.celestiaui.compose.ContextMenuContainer
import space.celestia.celestiaui.compose.EmptyHint
import space.celestia.celestiaui.compose.TextRow
import space.celestia.celestiaui.compose.groupedItems
import space.celestia.celestiaui.compose.groupedListContentPadding
import space.celestia.celestiaui.utils.CelestiaString
import java.text.DateFormat
import java.util.Locale

@Composable
fun EventFinderResultsScreen(results: List<EclipseFinder.Eclipse>, paddingValues: PaddingValues, handler: (eclipse: EclipseFinder.Eclipse, action: Int) -> Unit) {
    val formatter by remember { mutableStateOf(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())) }
    if (results.isEmpty()) {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues), contentAlignment = Alignment.Center) {
            EmptyHint(text = CelestiaString("No eclipse is found for the given object in the time range", ""))
        }
    } else {
        val contentPadding = groupedListContentPadding(paddingValues)
        LazyColumn(
            contentPadding = contentPadding,
            modifier = Modifier.nestedScroll(rememberNestedScrollInteropConnection())
        ) {
            groupedItems(results) { eclipse ->
                var expanded by remember(eclipse) { mutableStateOf(false) }
                ContextMenuContainer(expanded = expanded, onDismissRequest = { expanded = false }, menu = {
                    val actions = listOf(
                        CelestiaString("Set time to mid-eclipse", "") to Simulation.ECLIPSE_ACTION_SET_TIME,
                        CelestiaString("Near %s", "").format(eclipse.receiver.name) to Simulation.ECLIPSE_ACTION_NEAR_ECLIPSED_BODY,
                        CelestiaString("From surface of %s", "").format(eclipse.receiver.name) to Simulation.ECLIPSE_ACTION_FROM_ECLIPSED_BODY_SURFACE,
                        CelestiaString("From surface of %s", "").format(eclipse.occulter.name) to Simulation.ECLIPSE_ACTION_FROM_OCCULTER_SURFACE,
                        CelestiaString("Behind %s", "").format(eclipse.occulter.name) to Simulation.ECLIPSE_ACTION_BEHIND_OCCULTER,
                    )
                    actions.forEach { (title, action) ->
                        DropdownMenuItem(text = { Text(title) }, onClick = {
                            expanded = false
                            handler(eclipse, action)
                        })
                    }
                }) {
                    TextRow(primaryText = listOf(
                        CelestiaString("Occulter: %s", "Eclipse finder result").format(eclipse.occulter.name),
                        CelestiaString("Eclipsed body: %s", "Eclipse finder result").format(eclipse.receiver.name),
                    ).joinToString("\n"), secondaryText = formatter.format(Utils.createDateFromJulianDay(eclipse.startTimeJulian)), modifier = Modifier.clickable(onClick = dropUnlessResumed {
                        expanded = true
                    }))
                }
            }
        }
    }
}
