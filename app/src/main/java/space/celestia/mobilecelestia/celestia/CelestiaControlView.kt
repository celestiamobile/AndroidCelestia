/*
 * CelestiaControlView.kt
 *
 * Copyright (C) 2025-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.celestia

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.celestia.viewmodel.CelestiaViewModel

enum class CelestiaControlAction {
    ZoomIn, ZoomOut, ShowMenu, ToggleModeToCamera, ToggleModeToObject, Info, Search, Hide, Show, Go
}

sealed class CelestiaControlButton {
    data class Toggle(
        val offImage: Int,
        val onImage: Int,
        val offAction: CelestiaControlAction,
        val onAction: CelestiaControlAction,
        val contentDescription: String,
        val currentState: Boolean
    ) : CelestiaControlButton()

    data class Tap(
        val image: Int,
        val action: CelestiaControlAction,
        val contentDescription: String
    ) : CelestiaControlButton()

    data class Press(
        val image: Int,
        val action: CelestiaControlAction,
        val contentDescription: String
    ) : CelestiaControlButton()
}

@Composable
private fun ControlButton(imageId: Int, contentDescription: String?, onActionDown: () -> Unit = {}, onActionUp: () -> Unit = {}, onAction: () -> Unit = {}) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    Box(modifier = Modifier.size(dimensionResource(R.dimen.control_view_icon_size))
        .pointerInput(Unit) {
            detectTapGestures(onTap = {
                onAction()
            }, onPress = { offset ->
                onActionDown()
                val press = PressInteraction.Press(offset)
                interactionSource.emit(press)
                tryAwaitRelease()
                onActionUp()
                interactionSource.emit(PressInteraction.Release(press))
            })
        }, contentAlignment = Alignment.Center
    ) {
        Icon(painterResource(imageId), contentDescription = contentDescription, tint = colorResource(com.google.android.material.R.color.material_on_background_emphasis_medium), modifier = Modifier.alpha(if (isPressed) 0.38f else 1.0f).size(dimensionResource(R.dimen.bottom_control_view_item_dimension)))
    }
}

@Composable
private fun ContinuousButton(imageId: Int, contentDescription: String?, onActionDown: () -> Unit, onActionUp: () -> Unit) {
    ControlButton(imageId, contentDescription, onActionDown = onActionDown, onActionUp = onActionUp)
}

@Composable
private fun InstantButton(imageId: Int, contentDescription: String?, onAction: () -> Unit) {
    ControlButton(imageId, contentDescription, onAction = onAction)
}

@Composable
fun CelestiaControlView(
    didTapAction: (CelestiaControlAction) -> Unit,
    didStartPressingAction: (CelestiaControlAction) -> Unit,
    didEndPressingAction:(CelestiaControlAction) -> Unit,
    didToggleToMode:(CelestiaControlAction) -> Unit
) {
    val viewModel: CelestiaViewModel = hiltViewModel()
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.control_view_icon_spacing)),
        modifier = Modifier.background(
            color = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(size = dimensionResource(R.dimen.bottom_control_container_corner_radius))
        ).padding(dimensionResource(R.dimen.control_view_margin))
    ) {
        items(viewModel.controlButtons) {
            when (it) {
                is CelestiaControlButton.Press -> {
                    ContinuousButton(
                        imageId = it.image,
                        contentDescription = it.contentDescription,
                        onActionUp = {
                            didEndPressingAction(it.action)
                        },
                        onActionDown = {
                            didStartPressingAction(it.action)
                        })
                }

                is CelestiaControlButton.Tap -> {
                    InstantButton(imageId = it.image, contentDescription = it.contentDescription) {
                        didTapAction(it.action)
                    }
                }

                is CelestiaControlButton.Toggle -> {
                    var isSelected by remember { mutableStateOf(it.currentState) }
                    InstantButton(
                        imageId = if (isSelected) it.onImage else it.offImage,
                        contentDescription = it.contentDescription
                    ) {
                        isSelected = !isSelected
                        didToggleToMode(if (isSelected) it.onAction else it.offAction)
                    }
                }
            }
        }
    }
}