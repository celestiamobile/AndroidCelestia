/*
 * GoToMainScreen.kt
 *
 * Copyright (C) 2025-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.travel

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import space.celestia.celestia.Selection
import space.celestia.mobilecelestia.utils.CelestiaString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoToMainScreen(goToData: GoToData, selection: Selection, onGoToObject:(GoToData, Selection) -> Unit) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(topBar = {
        TopAppBar(title = {
            Text(text = CelestiaString("Go to Object", ""))
        }, scrollBehavior = scrollBehavior)
    }) { paddingValues ->
        GoToScreen(initialData = goToData, selection = selection, paddingValues = paddingValues, handler = { data, selection ->
            onGoToObject(data, selection)
        }, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection))
    }
}