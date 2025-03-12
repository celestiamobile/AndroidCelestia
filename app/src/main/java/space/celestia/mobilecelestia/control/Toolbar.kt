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
import android.view.LayoutInflater
import android.view.Menu
import android.view.MotionEvent
import android.widget.PopupMenu
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.viewinterop.AndroidView
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.StandardImageButton
import space.celestia.mobilecelestia.info.model.CelestiaAction
import space.celestia.mobilecelestia.info.model.CelestiaContinuosAction
import space.celestia.mobilecelestia.utils.CelestiaString

@SuppressLint("ClickableViewAccessibility", "InflateParams")
@Composable
private fun ContinuousButton(imageID: Int?, contentDescription: String?, onActionDown: () -> Unit, onActionUp: () -> Unit) {
    AndroidView(factory = {
        val view = LayoutInflater.from(it).inflate(R.layout.fragment_bottom_control_item, null, false)
        val button = view.findViewById<StandardImageButton>(R.id.button)
        button.setOnTouchListener { sender, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    onActionDown()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    onActionUp()
                }
            }
            return@setOnTouchListener sender.onTouchEvent(event)
        }
        view
    }, update = {
        val button = it.findViewById<StandardImageButton>(R.id.button)
        button.setImageResource(imageID ?: 0)
        button.contentDescription = contentDescription
    }, modifier = Modifier.size(dimensionResource(R.dimen.bottom_control_view_dimension)))
}

@SuppressLint("InflateParams")
@Composable
private fun InstantButton(imageID: Int?, contentDescription: String?, onAction: () -> Unit) {
    AndroidView(factory = {
        val view = LayoutInflater.from(it).inflate(R.layout.fragment_bottom_control_item, null, false)
        val button = view.findViewById<StandardImageButton>(R.id.button)
        button.setOnClickListener {
            onAction()
        }
        view
    }, update = {
        val button = it.findViewById<StandardImageButton>(R.id.button)
        button.setImageResource(imageID ?: 0)
        button.contentDescription = contentDescription
    }, modifier = Modifier.size(dimensionResource(R.dimen.bottom_control_view_dimension)))
}

@SuppressLint("InflateParams")
@Composable
private fun GroupButton(imageID: Int?, contentDescription: String?, actions: List<GroupActionItem>, onAction: (CelestiaContinuosAction) -> Unit) {
    AndroidView(factory = { context ->
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_bottom_control_item, null, false)
        val button = view.findViewById<StandardImageButton>(R.id.button)
        button.setOnClickListener {
            val popup = PopupMenu(context, it)
            for (i in actions.indices) {
                val action = actions[i]
                popup.menu.add(Menu.NONE, i, Menu.NONE, action.title)
            }
            popup.setOnMenuItemClickListener { menuItem ->
                onAction(actions[menuItem.itemId].action)
                return@setOnMenuItemClickListener true
            }
            popup.show()
        }
        view
    }, update = {
        val button = it.findViewById<StandardImageButton>(R.id.button)
        button.setImageResource(imageID ?: 0)
        button.contentDescription = contentDescription
    }, modifier = Modifier.size(dimensionResource(R.dimen.bottom_control_view_dimension)))
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
                    ContinuousButton(action.imageID, action.contentDescription, onActionDown = {
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
                    InstantButton(action.imageID, action.contentDescription) {
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