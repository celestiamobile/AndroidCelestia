/*
 * FontSettingsScreen.kt
 *
 * Copyright (C) 2025-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.purchase

import android.graphics.fonts.SystemFonts
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import space.celestia.celestia.Font
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.Footer
import space.celestia.mobilecelestia.compose.Header
import space.celestia.mobilecelestia.compose.RadioButtonRow
import space.celestia.mobilecelestia.settings.CustomFont
import space.celestia.mobilecelestia.settings.boldFont
import space.celestia.mobilecelestia.settings.normalFont
import space.celestia.mobilecelestia.settings.viewmodel.SettingsViewModel
import space.celestia.mobilecelestia.utils.CelestiaString

private class SystemFont(val path: String, val name: String, val ttcIndex: Int)

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun FontSettingsScreen(paddingValues: PaddingValues, modifier: Modifier = Modifier) {
    val viewModel: SettingsViewModel = hiltViewModel()
    var systemFonts by remember {
        mutableStateOf(listOf<SystemFont>())
    }
    var fontsLoaded by remember {
        mutableStateOf(false)
    }
    var selectedTabIndex by remember {
        mutableIntStateOf(0)
    }
    var normalFont by remember {
        mutableStateOf(viewModel.appSettings.normalFont)
    }
    var boldFont by remember {
        mutableStateOf(viewModel.appSettings.boldFont)
    }
    val currentFont = if (selectedTabIndex == 0) normalFont else boldFont
    if (fontsLoaded) {
        LazyColumn(modifier = modifier, contentPadding = paddingValues) {
            item {
                // TODO: Replace with SegmentedButton
                TabRow(selectedTabIndex = selectedTabIndex) {
                    val tabTitleModifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.tab_title_text_padding_vertical))
                    Tab(selected = selectedTabIndex == 0, onClick = {
                        selectedTabIndex = 0
                    }) {
                        Text(text = CelestiaString("Normal", "Normal font style"), modifier = tabTitleModifier)
                    }
                    Tab(selected = selectedTabIndex == 1, onClick = {
                        selectedTabIndex = 1
                    }) {
                        Text(text = CelestiaString("Bold", "Bold font style"), modifier = tabTitleModifier)
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
                RadioButtonRow(primaryText = CelestiaString("Default", ""), selected = currentFont == null, onClick = {
                    if (selectedTabIndex == 0) {
                        viewModel.appSettings.normalFont = null
                        normalFont = null
                    } else {
                        viewModel.appSettings.boldFont = null
                        boldFont = null
                    }
                })
            }
            if (systemFonts.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
                    Header(text = CelestiaString("System Fonts", ""))
                }
                items(items = systemFonts) {
                    RadioButtonRow(
                        primaryText = it.name,
                        selected = it.path == currentFont?.path && it.ttcIndex == currentFont.ttcIndex,
                        onClick = {
                            val font = CustomFont(it.path, it.ttcIndex)
                            if (selectedTabIndex == 0) {
                                viewModel.appSettings.normalFont = font
                                normalFont = font
                            } else {
                                viewModel.appSettings.boldFont = font
                                boldFont = font
                            }
                        })
                }
            }
            item {
                Footer(text = CelestiaString("Configuration will take effect after a restart.", "Change requires a restart"))
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_tall)))
            }
        }
    } else {
        LaunchedEffect(true) {
            val fonts = withContext(Dispatchers.IO) {
                val availableFonts = SystemFonts.getAvailableFonts()
                val fontPaths = mutableSetOf<String>()
                for (font in availableFonts) {
                    val file = font.file
                    if (file != null && file.isFile) {
                        fontPaths.add(file.path)
                    }
                }
                val fontCollections = arrayListOf<Font>()
                for (fontPath in fontPaths) {
                    val fontCollection = Font(fontPath)
                    if (fontCollection.fontNames.isNotEmpty()) {
                        fontCollections.add(fontCollection)
                    }
                }
                fontCollections.sortWith { p0, p1 -> p0.fontNames[0].compareTo(p1.fontNames[0]) }
                val results = arrayListOf<SystemFont>()
                for (fontCollection in fontCollections) {
                    for (fontIndex in fontCollection.fontNames.indices) {
                        results.add(SystemFont(fontCollection.path, fontCollection.fontNames[fontIndex], fontIndex))
                    }
                }
                results
            }
            systemFonts = fonts
            fontsLoaded = true
        }
        Box(modifier = modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}