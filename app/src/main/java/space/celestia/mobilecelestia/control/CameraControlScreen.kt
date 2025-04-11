/*
 * CameraControlScreen.kt
 *
 * Copyright (C) 2025-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.control

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.Footer
import space.celestia.mobilecelestia.compose.Stepper
import space.celestia.mobilecelestia.compose.TextRow
import space.celestia.mobilecelestia.control.viewmodel.CameraControlViewModel
import space.celestia.mobilecelestia.utils.CelestiaString

enum class CameraControlAction(val value: Int) {
    Pitch0(32), Pitch1(26), Yaw0(28), Yaw1(30), Roll0(31), Roll1(33), Reverse(-1), ZoomIn(5), ZoomOut(6);
}

@Composable
private fun StepperRow(name: String, minusAction: CameraControlAction, plusAction: CameraControlAction, touchDown: (CameraControlAction) -> Unit, touchUp: (CameraControlAction) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(text = name)
        Stepper(touchDown = { minus ->
            touchDown(if (minus) minusAction else plusAction)
        }, touchUp = { minus ->
            touchUp(if (minus) minusAction else plusAction)
        })
    }
}

@Composable
fun CameraControlScreen(paddingValues: PaddingValues, observerModeTapped: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: CameraControlViewModel = hiltViewModel()
    val internalViewModifier = Modifier
        .fillMaxWidth()
        .padding(
            horizontal = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal),
            vertical = dimensionResource(id = R.dimen.common_page_medium_gap_vertical),
        )
    val scope = rememberCoroutineScope()
    Column(modifier = modifier
        .verticalScroll(state = rememberScrollState(), enabled = true)
        .padding(paddingValues)) {
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
        StepperRow(name = CelestiaString("Pitch", "Camera control"), minusAction = CameraControlAction.Pitch0, plusAction = CameraControlAction.Pitch1, touchDown = {
            scope.launch(viewModel.executor.asCoroutineDispatcher()) { viewModel.appCore.keyDown(it.value) }
        }, touchUp = {
            scope.launch(viewModel.executor.asCoroutineDispatcher()) { viewModel.appCore.keyUp(it.value) }
        }, modifier = internalViewModifier)
        StepperRow(name = CelestiaString("Yaw", "Camera control"), minusAction = CameraControlAction.Yaw0, plusAction = CameraControlAction.Yaw1, touchDown = {
            scope.launch(viewModel.executor.asCoroutineDispatcher()) { viewModel.appCore.keyDown(it.value) }
        }, touchUp = {
            scope.launch(viewModel.executor.asCoroutineDispatcher()) { viewModel.appCore.keyUp(it.value) }
        }, modifier = internalViewModifier)
        StepperRow(name = CelestiaString("Roll", "Camera control"), minusAction = CameraControlAction.Roll0, plusAction = CameraControlAction.Roll1, touchDown = {
            scope.launch(viewModel.executor.asCoroutineDispatcher()) { viewModel.appCore.keyDown(it.value) }
        }, touchUp = {
            scope.launch(viewModel.executor.asCoroutineDispatcher()) { viewModel.appCore.keyUp(it.value) }
        }, modifier = internalViewModifier)
        Footer(text = CelestiaString("Long press on stepper to change orientation.", ""))
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
        StepperRow(name = CelestiaString("Zoom (Distance)", "Zoom in/out in Camera Control, this changes the relative distance to the object"), minusAction = CameraControlAction.ZoomOut, plusAction = CameraControlAction.ZoomIn, touchDown = {
            scope.launch(viewModel.executor.asCoroutineDispatcher()) { viewModel.appCore.keyDown(it.value) }
        }, touchUp = {
            scope.launch(viewModel.executor.asCoroutineDispatcher()) { viewModel.appCore.keyUp(it.value) }
        }, modifier = internalViewModifier)
        Footer(text = CelestiaString("Long press on stepper to zoom in/out.", ""))
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
        TextRow(primaryText = CelestiaString("Flight Mode", ""), accessoryIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight, modifier = Modifier.clickable(onClick = {
            observerModeTapped()
        }))
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
        FilledTonalButton(modifier = internalViewModifier, onClick = {
            scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                viewModel.appCore.simulation.reverseObserverOrientation()
            }
        }) {
            Text(text = CelestiaString("Reverse Direction", "Reverse camera direction, reverse travel direction"))
        }
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_tall)))
    }
}