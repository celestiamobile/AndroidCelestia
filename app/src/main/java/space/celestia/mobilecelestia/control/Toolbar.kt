/*
 * Toolbar.kt
 *
 * Copyright (C) 2025-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.control

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.ContextMenuContainer
import space.celestia.mobilecelestia.info.model.CelestiaAction
import space.celestia.mobilecelestia.info.model.CelestiaContinuosAction
import space.celestia.mobilecelestia.utils.CelestiaString

@Composable
private fun BottomActionButton(imageId: Int, contentDescription: String?, onActionDown: () -> Unit = {}, onActionUp: () -> Unit = {}, onAction: () -> Unit = {}) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    Box(modifier = Modifier.size(dimensionResource(R.dimen.bottom_control_view_dimension))
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
        Icon(painterResource(imageId), contentDescription = contentDescription, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.alpha(if (isPressed) 0.38f else 1.0f).size(dimensionResource(R.dimen.bottom_control_view_item_dimension)))
    }
}

@Composable
private fun ContinuousButton(imageId: Int, contentDescription: String?, onActionDown: () -> Unit, onActionUp: () -> Unit) {
    BottomActionButton(imageId, contentDescription, onActionDown = onActionDown, onActionUp = onActionUp)
}

@Composable
private fun InstantButton(imageId: Int, contentDescription: String?, onAction: () -> Unit) {
    BottomActionButton(imageId, contentDescription, onAction = onAction)
}

@Composable
private fun GroupButton(imageId: Int, contentDescription: String?, actions: List<GroupActionItem>, onAction: (CelestiaContinuosAction) -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    ContextMenuContainer(expanded = showMenu, onDismissRequest = { showMenu = false }, menu = {
        for (action in actions) {
            DropdownMenuItem(text = {
                Text(text = action.title)
            }, onClick = {
                showMenu = false
                onAction(action.action)
            })
        }
    }, content = {
        BottomActionButton(imageId, contentDescription, onAction = {
            showMenu = true
        })
    })
}

@Composable
fun Toolbar(
    actions: List<BottomControlAction>,
    onInstantActionSelected: (CelestiaAction) -> Unit,
    onContinuousActionDown: (CelestiaContinuosAction) -> Unit,
    onContinuousActionUp: (CelestiaContinuosAction) -> Unit,
    onCustomAction: (CustomActionType) -> Unit,
    onBottomControlHide: () -> Unit
) {
    LazyRow(
        modifier = Modifier
            .wrapContentSize(align = Alignment.CenterStart)
            .background(color = MaterialTheme.colorScheme.background, shape = RoundedCornerShape(size = dimensionResource(R.dimen.bottom_control_container_corner_radius)))
            .padding(horizontal = dimensionResource(R.dimen.bottom_control_view_margin_horizontal), vertical = dimensionResource(R.dimen.bottom_control_view_margin_vertical)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(actions)  { action ->
            when (action) {
                is ContinuousAction -> {
                    ContinuousButton(action.imageID!!, action.contentDescription, onActionDown = {
                        onContinuousActionDown(action.action)
                    }, onActionUp = {
                        onContinuousActionUp(action.action)
                    })
                }
                is CustomAction -> {
                    InstantButton(action.imageID, action.contentDescription) {
                        onCustomAction(action.type)
                    }
                }
                is GroupAction -> {
                    GroupButton(action.imageID, action.contentDescription, actions = action.actions) { value ->
                        onContinuousActionDown(value)
                        onContinuousActionUp(value)
                    }
                }
                is InstantAction -> {
                    InstantButton(action.imageID!!, action.contentDescription) {
                        onInstantActionSelected(action.action)
                    }
                }
            }
        }

        item {
            InstantButton(R.drawable.bottom_control_hide, CelestiaString("Close", "")) {
                onBottomControlHide()
            }
        }
    }
}