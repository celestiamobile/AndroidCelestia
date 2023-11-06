/*
 * TextInputDialog.kt
 *
 * Copyright (C) 2025-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.compose

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import space.celestia.mobilecelestia.utils.CelestiaString

@Composable
fun TextInputDialog(onDismissRequest: () -> Unit, confirmHandler: () -> Unit, title: String, text: String?, placeholder: String? = null, textChange: (String) -> Unit) {
    AlertDialog(onDismissRequest = onDismissRequest, confirmButton = {
        TextButton(onClick = confirmHandler) {
            Text(text = CelestiaString("OK", ""))
        }
    }, title = {
        Text(text = title)
    }, text = {
        TextField(value = text ?: "", placeholder = if (placeholder != null) { { Text(text = placeholder) } } else null, onValueChange = textChange)
    })
}

@Composable
fun TextInputDialog(onDismissRequest: () -> Unit, title: String, confirmHandler: (String?) -> Unit) {
    var string: String? by remember {
        mutableStateOf(null)
    }
    TextInputDialog(
        onDismissRequest = onDismissRequest,
        confirmHandler = {
            confirmHandler(string)
            string = null
        },
        title = title,
        text = string,
        textChange = {
            string = it
        }
    )

}