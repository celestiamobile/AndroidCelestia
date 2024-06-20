/*
 * TeachingCard.kt
 *
 * Copyright (C) 2024-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import space.celestia.mobilecelestia.R

@Composable
fun TeachingCard(title: String, actionButtonTitle: String, action: () -> Unit, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.common_page_medium_gap_vertical)), horizontalAlignment = Alignment.Start, modifier = Modifier.padding(
            dimensionResource(id = R.dimen.card_content_padding)
        )) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            FilledTonalButton(onClick = {
                action()
            }) {
                Text(text = actionButtonTitle)
            }
        }
    }
}