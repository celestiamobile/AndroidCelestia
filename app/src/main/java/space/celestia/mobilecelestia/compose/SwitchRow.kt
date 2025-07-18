/*
 * SwitchRow.kt
 *
 * Copyright (C) 2024-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import space.celestia.mobilecelestia.R

@Composable
fun SwitchRow(primaryText: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier, primaryTextColor: Color? = null, secondaryText: String? = null, horizontalPadding: Dp? = null, canTapWholeRow: Boolean = true) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = (if (canTapWholeRow) modifier.toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange) else modifier).padding(
        horizontal = horizontalPadding ?: dimensionResource(id = R.dimen.list_item_medium_margin_horizontal),
    )) {
        Column(modifier = Modifier.weight(1.0f).padding(
            vertical = dimensionResource(id = R.dimen.list_item_small_margin_vertical)
        ), horizontalAlignment = Alignment.Start) {
            Text(text = primaryText, color = primaryTextColor ?: MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyLarge)
            if (secondaryText != null) {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_item_gap_vertical)))
                Text(
                    text = secondaryText,
                    color = colorResource(id = com.google.android.material.R.color.material_on_background_emphasis_medium),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.list_item_gap_horizontal)))
        Switch(checked = checked, onCheckedChange = if (canTapWholeRow) null else onCheckedChange, modifier = if (canTapWholeRow) Modifier.minimumInteractiveComponentSize() else Modifier)
    }
}