// CameraControl.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.celestiaui.control

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
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.dropUnlessResumed
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import space.celestia.celestiaui.R
import space.celestia.celestiaui.compose.CheckboxRow
import space.celestia.celestiaui.compose.Footer
import space.celestia.celestiaui.compose.GroupedListItem
import space.celestia.celestiaui.compose.groupedListContentPadding
import space.celestia.celestiaui.compose.Stepper
import space.celestia.celestiaui.compose.TextRow
import space.celestia.celestiaui.control.viewmodel.CameraControlViewModel
import space.celestia.celestiaui.utils.CelestiaString

enum class CameraControlAction(val value: Int) {
    Pitch0(32), Pitch1(26), Yaw0(28), Yaw1(30), Roll0(31), Roll1(33), Reverse(-1), ZoomIn(5), ZoomOut(6);
}

@Composable
fun CameraControl(paddingValues: PaddingValues, cameraControlObserverModeClicked: () -> Unit) {
    val viewModel: CameraControlViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    val internalViewModifier = Modifier
        .fillMaxWidth()
        .padding(
            horizontal = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal),
            vertical = dimensionResource(id = R.dimen.common_page_medium_gap_vertical),
        )
    val nestedScrollInterop = rememberNestedScrollInteropConnection()

    val contentPadding = groupedListContentPadding(paddingValues)
    Column(modifier = Modifier
        .nestedScroll(nestedScrollInterop)
        .verticalScroll(state = rememberScrollState(), enabled = true)
        .padding(contentPadding)) {
        GroupedListItem(index = 0, count = 3) {
            StepperRow(name = CelestiaString("Pitch", "Camera control"), minusAction = CameraControlAction.Pitch0, plusAction = CameraControlAction.Pitch1, modifier = internalViewModifier)
        }
        GroupedListItem(index = 1, count = 3) {
            StepperRow(name = CelestiaString("Yaw", "Camera control"), minusAction = CameraControlAction.Yaw0, plusAction = CameraControlAction.Yaw1, modifier = internalViewModifier)
        }
        GroupedListItem(index = 2, count = 3) {
            StepperRow(name = CelestiaString("Roll", "Camera control"), minusAction = CameraControlAction.Roll0, plusAction = CameraControlAction.Roll1, modifier = internalViewModifier)
        }
        Footer(text = CelestiaString("Long press on stepper to change orientation.", ""))
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_group_spacing)))
        GroupedListItem {
            StepperRow(name = CelestiaString("Zoom (Distance)", "Zoom in/out in Camera Control, this changes the relative distance to the object"), minusAction = CameraControlAction.ZoomOut, plusAction = CameraControlAction.ZoomIn, modifier = internalViewModifier)
        }
        Footer(text = CelestiaString("Long press on stepper to zoom in/out.", ""))
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_group_spacing)))
        if (viewModel.sessionSettings.isGyroscopeSupported) {
            GroupedListItem {
                CheckboxRow(primaryText = CelestiaString("Enable Gyroscope Control", "Enable gyroscope control for camera rotation"), checked = viewModel.sessionSettings.isGyroscopeEnabled, onCheckedChange = {
                    viewModel.sessionSettings.isGyroscopeEnabled = it
                })
            }
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_group_spacing)))
        }
        GroupedListItem {
            TextRow(primaryText = CelestiaString("Flight Mode", ""), accessoryResource = R.drawable.accessory_full_disclosure, modifier = Modifier.clickable(onClick = dropUnlessResumed {
                cameraControlObserverModeClicked()
            }))
        }
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
        FilledTonalButton(modifier = internalViewModifier, onClick = dropUnlessResumed {
            scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                viewModel.appCore.simulation.reverseObserverOrientation()
            }
        }) {
            Text(text = CelestiaString("Reverse Direction", "Reverse camera direction, reverse travel direction"))
        }
    }
}

@Composable
private fun StepperRow(name: String, minusAction: CameraControlAction, plusAction: CameraControlAction, modifier: Modifier = Modifier) {
    val viewModel: CameraControlViewModel = hiltViewModel()
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_item_gap_horizontal)), verticalAlignment = Alignment.CenterVertically) {
        Text(text = name, modifier = Modifier.weight(1f))
        Stepper(
            minusLabel = CelestiaString("Decrease %s", "Accessibility label for a camera control").format(name),
            plusLabel = CelestiaString("Increase %s", "Accessibility label for a camera control").format(name),
            touchDown = { minus ->
                viewModel.executor.execute {
                    viewModel.appCore.keyDown((if (minus) minusAction else plusAction).value)
                }
            },
            touchUp = { minus ->
                viewModel.executor.execute {
                    viewModel.appCore.keyUp((if (minus) minusAction else plusAction).value)
                }
            },
            onClick = { minus ->
                val key = (if (minus) minusAction else plusAction).value
                viewModel.executor.execute {
                    viewModel.appCore.keyDown(key)
                    // A nested task runs after the renderer has ticked with the key held.
                    viewModel.executor.execute {
                        viewModel.appCore.keyUp(key)
                    }
                }
            },
        )
    }
}