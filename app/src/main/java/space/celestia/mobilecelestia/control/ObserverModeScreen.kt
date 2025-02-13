/*
 * ObserverModeScreen.kt
 *
 * Copyright (C) 2025-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.control

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import space.celestia.celestia.AppCore
import space.celestia.celestia.Observer
import space.celestia.celestia.Selection
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.FooterLink
import space.celestia.mobilecelestia.compose.Header
import space.celestia.mobilecelestia.compose.ObjectNameAutoComplete
import space.celestia.mobilecelestia.compose.OptionSelect
import space.celestia.mobilecelestia.control.viewmodel.CameraControlViewModel
import space.celestia.mobilecelestia.utils.CelestiaString

@Composable
fun ObserverModeScreen(paddingValues: PaddingValues, openLink: (String) -> Unit, modifier: Modifier = Modifier) {
    val viewModel: CameraControlViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    val internalViewModifier = Modifier
        .fillMaxWidth()
        .padding(
            horizontal = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal),
            vertical = dimensionResource(id = R.dimen.common_page_medium_gap_vertical),
        )
    val coordinateSystems = listOf(
        Pair(Observer.COORDINATE_SYSTEM_UNIVERSAL, CelestiaString("Free Flight", "")),
        Pair(Observer.COORDINATE_SYSTEM_ECLIPTICAL, CelestiaString("Follow", "")),
        Pair(Observer.COORDINATE_SYSTEM_BODY_FIXED, CelestiaString("Sync Orbit", "")),
        Pair(Observer.COORDINATE_SYSTEM_PHASE_LOCK, CelestiaString("Phase Lock", "")),
        Pair(Observer.COORDINATE_SYSTEM_CHASE, CelestiaString("Chase", "")),
    )
    var referenceObjectName by remember {
        mutableStateOf("")
    }
    var referenceObject by remember {
        mutableStateOf(Selection())
    }
    var targetObjectName by remember {
        mutableStateOf("")
    }
    var selectedCoordinateIndex by remember {
        mutableIntStateOf(0)
    }
    var targetObject by remember {
        mutableStateOf(Selection())
    }
    val selectedCoordinateSystem = coordinateSystems[selectedCoordinateIndex].first
    val nestedScrollInterop = rememberNestedScrollInteropConnection()
    Column(modifier = modifier
        .nestedScroll(nestedScrollInterop)
        .verticalScroll(state = rememberScrollState(), enabled = true)
        .padding(paddingValues)) {
        Header(text = CelestiaString("Coordinate System", "Used in Flight Mode"))
        OptionSelect(options = coordinateSystems.map { it.second }, selectedIndex = selectedCoordinateIndex, selectionChange = {
            selectedCoordinateIndex = it
        }, modifier = internalViewModifier)

        if (selectedCoordinateSystem != Observer.COORDINATE_SYSTEM_UNIVERSAL) {
            Header(text = CelestiaString("Reference Object", "Used in Flight Mode"))
            ObjectNameAutoComplete(executor = viewModel.executor, core = viewModel.appCore, name = referenceObjectName, selection = referenceObject, inputUpdated = {
                referenceObjectName = it
            }, selectionUpdated = {
                referenceObject = it
            }, modifier = internalViewModifier)
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
        }

        if (selectedCoordinateSystem == Observer.COORDINATE_SYSTEM_PHASE_LOCK) {
            Header(text = CelestiaString("Target Object", "Used in Flight Mode"))
            ObjectNameAutoComplete(executor = viewModel.executor, core = viewModel.appCore, name = targetObjectName, selection = targetObject, inputUpdated = {
                targetObjectName = it
            }, selectionUpdated = {
                targetObject = it
            }, modifier = internalViewModifier)
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
        }

        val infoText = CelestiaString("Flight mode decides how you move around in Celestia. Learn more…", "")
        val infoLinkText = CelestiaString("Learn more…", "Text for the link in Flight mode decides how you move around in Celestia. Learn more…")
        FooterLink(text = infoText, linkText = infoLinkText, link = "https://celestia.mobi/help/flight-mode?lang=${AppCore.getLanguage()}", action = { link ->
            openLink(link)
        })

        FilledTonalButton(modifier = internalViewModifier, onClick = {
            scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                applyObserverMode(
                    referenceObject = referenceObject,
                    targetObject = targetObject,
                    coordinateSystem = selectedCoordinateSystem,
                    appCore = viewModel.appCore
                )
            }
        }) {
            Text(text = CelestiaString("OK", ""))
        }
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_tall)))
    }
}

private fun applyObserverMode(referenceObject: Selection, targetObject: Selection, coordinateSystem: Int, appCore: AppCore) {
    appCore.simulation.activeObserver.setFrame(coordinateSystem, referenceObject, targetObject)
}