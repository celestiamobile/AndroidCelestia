/*
 * ContextMenuContainer.kt
 *
 * Copyright (C) 2023-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.unit.IntOffset

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ContextMenuContainer(expanded: Boolean, onDismissRequest: () -> Unit, menu: @Composable ColumnScope.() -> Unit, content: @Composable BoxScope.() -> Unit) {
    var offset by remember { mutableStateOf(Offset.Zero) }
    Box(modifier = Modifier.pointerInteropFilter { event ->
        offset = Offset(event.x, event.y)
        false
    }) {
        content()
        Box(modifier = Modifier.absoluteOffset { IntOffset(x = offset.x.toInt(), y = offset.y.toInt()) }) {
            DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest) {
                menu()
            }
        }
    }
}