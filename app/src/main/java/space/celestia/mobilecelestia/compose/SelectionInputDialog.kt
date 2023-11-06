/*
 * SelectionInputDialog.kt
 *
 * Copyright (C) 2025-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.compose

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import space.celestia.mobilecelestia.utils.CelestiaString

@Composable
fun SelectionInputDialog(onDismissRequest: () -> Unit, title: String? = null, selectedIndex: Int, items: List<String>, selectionChangeHandler: (Int) -> Unit, confirmHandler: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = if (title != null) { { Text(text = title) } } else null,
        text = {
            LazyColumn(content = {
                itemsIndexed(items) { index, item ->
                    RadioButtonRow(primaryText = item, selected = index == selectedIndex, hideHorizontalPadding = true) {
                        selectionChangeHandler(index)
                    }
                }
            })
        },
        confirmButton = {
            TextButton(onClick = {
                confirmHandler()
            }) {
                Text(text = CelestiaString("OK", ""))
            }
        }
    )
}