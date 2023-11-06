/*
 * RenderInfoScreen.kt
 *
 * Copyright (C) 2023-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.settings.viewmodel.SettingsViewModel

@Composable
fun RenderInfoScreen(paddingValues: PaddingValues, modifier: Modifier = Modifier) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val scroll = rememberScrollState(0)
    var renderInfo: String? by remember {
        mutableStateOf(null)
    }
    LaunchedEffect(true) {
        renderInfo = withContext(viewModel.executor.asCoroutineDispatcher()) {
            viewModel.appCore.renderInfo
        }
    }

    val info = renderInfo
    if (info != null) {
        SelectionContainer(modifier = modifier
            .verticalScroll(scroll)
            .fillMaxWidth()
            .padding(paddingValues)
            .padding(
                vertical = dimensionResource(id = R.dimen.common_page_medium_margin_vertical),
                horizontal = dimensionResource(id = R.dimen.common_page_medium_margin_horizontal),
            )
        ) {
            Text(text = info, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyMedium)
        }
    } else {
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}