// SettingsEntryScreen.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.settings

import androidx.activity.compose.LocalActivity
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.CheckboxRow
import space.celestia.mobilecelestia.compose.Footer
import space.celestia.mobilecelestia.compose.FooterLink
import space.celestia.mobilecelestia.compose.Header
import space.celestia.mobilecelestia.compose.RadioButtonRow
import space.celestia.mobilecelestia.compose.Separator
import space.celestia.mobilecelestia.compose.SliderRow
import space.celestia.mobilecelestia.compose.SwitchRow
import space.celestia.mobilecelestia.compose.TextRow
import space.celestia.mobilecelestia.settings.viewmodel.SettingsViewModel
import space.celestia.mobilecelestia.utils.PreferenceManager
import space.celestia.mobilecelestia.utils.showOptions

@Composable
fun SettingsEntryScreen(item: SettingsCommonItem, paddingValues: PaddingValues, linkClicked: (String, Boolean) -> Unit) {
    LazyColumn(modifier = Modifier
        .nestedScroll(rememberNestedScrollInteropConnection()), contentPadding = paddingValues) {
        for (index in item.sections.indices) {
            val section = item.sections[index]
            item {
                val header = section.header
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
                val footer = section.footer
                when (footer) {
                    is Footer.Text -> {
                        Footer(text = footer.text)
                    }
                    is Footer.TextWithLink -> {
                        FooterLink(text = footer.text, link = footer.link, linkText = footer.linkText, action = {
                            linkClicked(it, footer.localizable)
                        })
                    }
                    else -> {}
                }
                if (index == item.sections.size - 1) {
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_tall)))
                } else {
                    val nextHeader = item.sections[index + 1].header
                    if (nextHeader.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
                        if (footer == null) {
                            Separator()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingEntry(item: SettingsItem) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    val localActivity = LocalActivity.current
    when (item) {
        is SettingsSwitchItem -> {
            var on by remember {
                mutableStateOf(viewModel.appCore.getBooleanValueForPield(item.key))
            }
            when (item.representation) {
                SettingsSwitchItem.Representation.Switch -> {
                    SwitchRow(primaryText = item.name, secondaryText = item.subtitle, checked = on, onCheckedChange = { newValue ->
                        on = newValue
                        if (!item.volatile)
                            viewModel.coreSettings[PreferenceManager.CustomKey(item.key)] = if (newValue) "1" else "0"
                        scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                            viewModel.appCore.setBooleanValueForField(item.key, newValue)
                        }
                    })
                }
                SettingsSwitchItem.Representation.Checkmark -> {
                    CheckboxRow(primaryText = item.name, secondaryText = item.subtitle, checked = on, onCheckedChange = { newValue ->
                        on = newValue
                        if (!item.volatile)
                            viewModel.coreSettings[PreferenceManager.CustomKey(item.key)] = if (newValue) "1" else "0"
                        scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                            viewModel.appCore.setBooleanValueForField(item.key, newValue)
                        }
                    })
                }
            }
        }

        is SettingsSelectionSingleItem -> {
            var selected by remember {
                val value = viewModel.appCore.getIntValueForField(item.key)
                mutableIntStateOf(
                    if (item.options.any { it.first == value }) {
                        value
                    } else {
                        item.defaultSelection
                    }
                )
            }
            if (item.showTitle) {
                TextRow(primaryText = item.name, secondaryText = item.subtitle)
            }
            for (option in item.options) {
                RadioButtonRow(primaryText = option.second, selected = option.first == selected) {
                    selected = option.first
                    viewModel.coreSettings[PreferenceManager.CustomKey(item.key)] = option.first.toString()
                    scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                        viewModel.appCore.setIntValueForField(item.key, option.first)
                    }
                }
            }
        }

        is SettingsPreferenceSwitchItem -> {
            var on by remember {
                mutableStateOf(when (viewModel.appSettings[item.key]) { "true" -> true "false" -> false else -> item.defaultOn })
            }
            SwitchRow(primaryText = item.name, secondaryText = item.subtitle, checked = on, onCheckedChange = { newValue ->
                on = newValue
                viewModel.appSettings[item.key] = if (newValue) "true" else "false"
            })
        }

        is SettingsPreferenceSelectionItem -> {
            var selected by remember {
                mutableIntStateOf(viewModel.appSettings[item.key]?.toIntOrNull() ?: item.defaultSelection)
            }
            TextRow(
                primaryText = item.name,
                secondaryText = item.options.firstOrNull { it.first == selected }?.second,
                modifier = Modifier.clickable {
                    val activity = localActivity ?: return@clickable
                    activity.showOptions("", options = item.options.map { it.second }.toTypedArray()) { index ->
                        val value = item.options[index].first
                        selected = value
                        viewModel.appSettings[item.key] = value.toString()
                    }
                }
            )
        }

        is SettingsActionItem -> {
            TextRow(primaryText = item.name, modifier = Modifier.clickable {
                scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                    viewModel.appCore.charEnter(item.action)
                }
            })
        }

        is SettingsUnknownTextItem -> {
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

        is SettingsSliderItem -> {
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

        is SettingsPreferenceSliderItem -> {
            var value by remember {
                mutableFloatStateOf(viewModel.appSettings[item.key]?.toFloat() ?: item.defaultValue.toFloat())
            }
            SliderRow(primaryText = item.name, secondaryText = item.subtitle, value = value, valueRange = item.minValue.toFloat()..item.maxValue.toFloat(), onValueChange = { newValue ->
                value = newValue
                viewModel.appSettings[item.key] = newValue.toString()
            })
        }
    }
}