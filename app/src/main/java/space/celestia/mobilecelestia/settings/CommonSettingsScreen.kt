/*
 * CommonSettingsScreen.kt
 *
 * Copyright (C) 2025-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.CheckboxRow
import space.celestia.mobilecelestia.compose.Footer
import space.celestia.mobilecelestia.compose.Header
import space.celestia.mobilecelestia.compose.RadioButtonRow
import space.celestia.mobilecelestia.compose.SelectionInputDialog
import space.celestia.mobilecelestia.compose.Separator
import space.celestia.mobilecelestia.compose.SliderRow
import space.celestia.mobilecelestia.compose.SwitchRow
import space.celestia.mobilecelestia.compose.TextRow
import space.celestia.mobilecelestia.settings.viewmodel.SettingsViewModel
import space.celestia.mobilecelestia.utils.PreferenceManager

@Composable
fun CommonSettingsScreen(paddingValues: PaddingValues, item: Settings.Common.Data, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier, contentPadding = paddingValues) {
        for (index in item.sections.indices) {
            val section = item.sections[index]
            item {
                val header = section.header?.displayName
                if (header.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
                } else {
                    Header(text = header)
                }
            }
            items(section.rows) { item ->
                SettingEntry(item = item)
            }
            item {
                val footer = section.footer?.displayName
                if (!footer.isNullOrEmpty()) {
                    Footer(text = footer)
                }
                if (index == item.sections.size - 1) {
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_tall)))
                } else {
                    val nextHeader = item.sections[index + 1].header?.displayName
                    if (nextHeader.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
                        if (footer.isNullOrEmpty()) {
                            Separator()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingEntry(item: SettingsEntry) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    when (item) {
        is SettingsEntry.SwitchItem -> {
            var on by remember {
                mutableStateOf(viewModel.appCore.getBooleanValueForPield(item.key.valueString))
            }
            when (item.representation) {
                SettingsEntry.SwitchItem.Representation.Switch -> {
                    SwitchRow(primaryText = item.name, secondaryText = item.key.subtitle, checked = on, onCheckedChange = { newValue ->
                        on = newValue
                        if (!item.volatile)
                            viewModel.coreSettings[PreferenceManager.CustomKey(item.key.valueString)] = if (newValue) "1" else "0"
                        scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                            viewModel.appCore.setBooleanValueForField(item.key.valueString, newValue)
                        }
                    })
                }
                SettingsEntry.SwitchItem.Representation.Checkmark -> {
                    CheckboxRow(primaryText = item.name, secondaryText = item.key.subtitle, checked = on, onCheckedChange = { newValue ->
                        on = newValue
                        if (!item.volatile)
                            viewModel.coreSettings[PreferenceManager.CustomKey(item.key.valueString)] = if (newValue) "1" else "0"
                        scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                            viewModel.appCore.setBooleanValueForField(item.key.valueString, newValue)
                        }
                    })
                }
            }
        }

        is SettingsEntry.SelectionSingleItem -> {
            var selected by remember {
                val value = viewModel.appCore.getIntValueForField(item.key.valueString)
                mutableIntStateOf(
                    if (item.options.any { it.index == value }) {
                        value
                    } else {
                        item.defaultSelection
                    }
                )
            }
            if (item.showTitle) {
                TextRow(primaryText = item.name, secondaryText = item.key.subtitle)
            }
            for (option in item.options) {
                RadioButtonRow(primaryText = option.name.displayName, selected = option.index == selected) {
                    selected = option.index
                    viewModel.coreSettings[PreferenceManager.CustomKey(item.key.valueString)] = option.index.toString()
                    scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                        viewModel.appCore.setIntValueForField(item.key.valueString, option.index)
                    }
                }
            }
        }

        is SettingsEntry.PreferenceSwitchItem -> {
            var on by remember {
                mutableStateOf(when (viewModel.appSettings[item.key]) { "true" -> true "false" -> false else -> item.defaultOn })
            }
            SwitchRow(primaryText = item.name, secondaryText = item.key.subtitle, checked = on, onCheckedChange = { newValue ->
                on = newValue
                viewModel.appSettings[item.key] = if (newValue) "true" else "false"
            })
        }

        is SettingsEntry.PreferenceSelectionItem -> {
            var selected by remember {
                mutableIntStateOf(viewModel.appSettings[item.key]?.toIntOrNull() ?: item.defaultSelection)
            }
            var showSelectionDialog by remember {
                mutableStateOf(false)
            }
            TextRow(
                primaryText = item.name,
                secondaryText = item.options.firstOrNull { it.index == selected }?.name?.displayName,
                modifier = Modifier.clickable {
                    showSelectionDialog = true
                }
            )
            if (showSelectionDialog) {
                SelectionInputDialog(
                    onDismissRequest = { showSelectionDialog = false },
                    selectedIndex = item.options.indexOfFirst { it.index == selected },
                    items = item.options.map { it.name.displayName },
                    selectionChangeHandler = {
                        showSelectionDialog = false
                        if (it >= 0 && it < item.options.size) {
                            val value = item.options[it].index
                            selected = value
                            viewModel.appSettings[item.key] = value.toString()
                        }
                    }
                )
            }
        }

        is SettingsEntry.ActionItem -> {
            TextRow(primaryText = item.name, modifier = Modifier.clickable {
                scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                    viewModel.appCore.charEnter(item.action)
                }
            })
        }

        is SettingsEntry.UnknownTextItem -> {
            TextRow(primaryText = item.name, modifier = Modifier.clickable {
                when (item.id) {
                    settingUnmarkAllID -> {
                        scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                            viewModel.appCore.simulation.universe.unmarkAll()
                        }
                    }
                }
            })
        }

        is SettingsEntry.SliderItem -> {
            var value by remember {
                mutableFloatStateOf(viewModel.appCore.getDoubleValueForField(item.key).toFloat())
            }
            SliderRow(primaryText = item.name, value = value, valueRange = item.minValue.toFloat()..item.maxValue.toFloat(), onValueChange = { newValue ->
                value = newValue
                viewModel.coreSettings[PreferenceManager.CustomKey(item.key)] = newValue.toString()
                scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                    viewModel.appCore.setDoubleValueForField(item.key, newValue.toDouble())
                }
            })
        }

        is SettingsEntry.PreferenceSliderItem -> {
            var value by remember {
                mutableFloatStateOf(viewModel.appSettings[item.key]?.toFloat() ?: item.defaultValue.toFloat())
            }
            SliderRow(primaryText = item.name, secondaryText = item.key.subtitle, value = value, valueRange = item.minValue.toFloat()..item.maxValue.toFloat(), onValueChange = { newValue ->
                value = newValue
                viewModel.appSettings[item.key] = newValue.toString()
            })
        }
    }
}