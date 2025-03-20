/*
 * LoadingView.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.loading

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.utils.CelestiaString

@Composable
fun LoadingView(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.common_page_medium_margin_horizontal))) {
            Image(
                painter = painterResource(id = R.drawable.loading_icon),
                contentDescription = null,
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.app_icon_dimension))
            )
            if (text.isNotEmpty()) {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.loading_gap_vertical)))
                Text(
                    text,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}