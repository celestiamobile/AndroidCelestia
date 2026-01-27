/*
 * OptionInputDialog.kt
 *
 * Copyright (C) 2025-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionInputDialog(onDismissRequest: () -> Unit, title: String? = null, items: List<String>, selectionHandler: (Int) -> Unit) {
    // val DialogPadding = PaddingValues(all = 24.dp)
    // val TitlePadding = PaddingValues(bottom = 16.dp)
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties()
    ) {
        Surface(
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation,
        ) {
            Column(modifier = Modifier.padding(PaddingValues(vertical = 24.dp))) {
                title?.let {
                    Box(
                        Modifier.padding(PaddingValues(bottom = 16.dp, start = 24.dp, end = 24.dp))
                            .align(Alignment.Start)
                    ) {
                        Text(text = title, fontStyle = MaterialTheme.typography.headlineSmall.fontStyle, fontSize = MaterialTheme.typography.headlineSmall.fontSize, fontWeight = MaterialTheme.typography.headlineSmall.fontWeight, fontFamily = MaterialTheme.typography.headlineSmall.fontFamily, color = AlertDialogDefaults.titleContentColor)
                    }
                }
                LazyColumn(content = {
                    itemsIndexed(items) { index, item ->
                        TextRow(primaryText = null, secondaryText = item, horizontalPadding = 24.dp, modifier = Modifier.clickable {
                            selectionHandler(index)
                        })
                    }
                })
            }
        }
    }
}