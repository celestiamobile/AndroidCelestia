/*
 * EventFinderInputScreen.kt
 *
 * Copyright (C) 2025-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import space.celestia.celestia.AppCore
import space.celestia.celestia.Body
import space.celestia.celestia.EclipseFinder
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.DateInputDialog
import space.celestia.mobilecelestia.compose.OptionInputDialog
import space.celestia.mobilecelestia.compose.Separator
import space.celestia.mobilecelestia.compose.TextInputDialog
import space.celestia.mobilecelestia.compose.TextRow
import space.celestia.mobilecelestia.eventfinder.viewmodel.EventFinderViewModel
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.julianDay
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EventFinderInputScreen(paddingValues: PaddingValues, showResults: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: EventFinderViewModel = hiltViewModel()
    var startTime by rememberSaveable { mutableStateOf(Date(Date().time - 365L * 24 * 60 * 60 * 1000)) }
    var endTime by rememberSaveable { mutableStateOf(Date()) }
    var objectName by rememberSaveable { mutableStateOf(AppCore.getLocalizedStringDomain("Earth", "celestia-data")) }
    var objectPath by rememberSaveable { mutableStateOf("Sol/Earth") }
    val formatter by remember { mutableStateOf(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault())) }

    var showStartTimeDialog by remember { mutableStateOf(false)  }
    var showEndTimeDialog by remember { mutableStateOf(false)  }
    var showObjectSelectionDialog by remember { mutableStateOf(false)  }
    var showObjectInputDialog by remember { mutableStateOf(false)  }
    var showLoadingDialog by remember { mutableStateOf(false)  }

    val scope = rememberCoroutineScope()
    var errorText: String? by remember {
        mutableStateOf(null)
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
        modifier = modifier
    ) {
        item {
            TextRow(primaryText = CelestiaString("Start Time", "In eclipse finder, range of time to find eclipse in"), secondaryText = formatter.format(startTime), modifier = Modifier.clickable {
                showStartTimeDialog = true
            })
            TextRow(primaryText = CelestiaString("End Time", "In eclipse finder, range of time to find eclipse in"), secondaryText = formatter.format(endTime), modifier = Modifier.clickable {
                showEndTimeDialog = true
            })
        }

        item {
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
            Separator()
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
        }

        item {
            TextRow(primaryText = CelestiaString("Object", "In eclipse finder, object to find eclipse with, or in go to"), secondaryText = objectName, modifier = Modifier.clickable {
                showObjectSelectionDialog = true
            })
        }

        item {
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
            FilledTonalButton(modifier = Modifier.fillMaxWidth().padding(
                horizontal = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal),
                vertical = dimensionResource(id = R.dimen.common_page_medium_gap_vertical),
            ), onClick = {
                scope.launch {
                    val body = withContext(viewModel.executor.asCoroutineDispatcher()) {
                        viewModel.appCore.simulation.findObject(objectPath).`object` as? Body
                    }
                    if (body == null) {
                        errorText = CelestiaString("Object not found", "")
                        return@launch
                    }

                    val finder = EclipseFinder(body)
                    viewModel.currentEclipseFinder = finder
                    showLoadingDialog = true
                    val results = withContext(Dispatchers.IO) {
                        finder.search(
                            startTime.julianDay,
                            endTime.julianDay,
                            EclipseFinder.ECLIPSE_KIND_LUNAR or EclipseFinder.ECLIPSE_KIND_SOLAR
                        )
                    }
                    viewModel.eclipseResults = results
                    finder.close()
                    showLoadingDialog = false
                    showResults()
                }
            }) {
                Text(text = CelestiaString("Find", "Find (eclipses)"))
            }
        }
    }

    if (showStartTimeDialog) {
        DateInputDialog(onDismissRequest = {
            showStartTimeDialog = false
        }, errorHandler = {
            showStartTimeDialog = false
            errorText = it
        }) {
            showStartTimeDialog = false
            startTime = it
        }
    }

    if (showEndTimeDialog) {
        DateInputDialog(onDismissRequest = {
            showEndTimeDialog = false
        }, errorHandler = {
            showEndTimeDialog = false
            errorText = it
        }) {
            showEndTimeDialog = false
            endTime = it
        }
    }

    if (showObjectSelectionDialog) {
        val objects = listOf(
            Pair(AppCore.getLocalizedStringDomain("Earth", "celestia-data"), "Sol/Earth"),
            Pair(AppCore.getLocalizedStringDomain("Jupiter", "celestia-data"), "Sol/Jupiter"),
        )
        val other = CelestiaString("Other", "Other location labels; Android/iOS, Other objects to choose from in Eclipse Finder")
        OptionInputDialog(
            onDismissRequest = {
                showObjectSelectionDialog = false
            },
            title = CelestiaString("Please choose an object.", "In eclipse finder, choose an object to find eclipse wth"),
            items = objects.map { it.first } + other
        ) { index ->
            showObjectSelectionDialog = false
            if (index >= objects.size) {
                // User choose other, show text input for the object name
                showObjectInputDialog = true
                return@OptionInputDialog
            }
            objectName = objects[index].first
            objectPath = objects[index].second
        }
    }

    if (showObjectInputDialog) {
        TextInputDialog(onDismissRequest = {
            showObjectInputDialog = false
        }, title = CelestiaString("Please enter an object name.", "In Go to; Android/iOS, Enter the name of an object in Eclipse Finder")) {
            showObjectInputDialog = false
            if (it != null) {
                objectName = it
                objectPath = it
            }
        }
    }

    if (showLoadingDialog) {
        AlertDialog(onDismissRequest = {
            showLoadingDialog = false
        }, confirmButton = {
            TextButton(onClick = {
                showLoadingDialog = false
                viewModel.currentEclipseFinder?.abort()
            }) {
                Text(text = CelestiaString("Cancel", ""))
            }
        }, title = {
            Text(text = CelestiaString("Calculating…", "Calculating for eclipses"))
        }, properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false))
    }

    errorText?.let {
        AlertDialog(onDismissRequest = {
            errorText = null
        }, confirmButton = {
            TextButton(onClick = {
                errorText = null
            }) {
                Text(text = CelestiaString("OK", ""))
            }
        }, title = {
            Text(text = it)
        })
    }
}