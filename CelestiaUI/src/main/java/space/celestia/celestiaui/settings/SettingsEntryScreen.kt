// SettingsEntryScreen.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.celestiaui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.lifecycle.compose.dropUnlessResumed
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import space.celestia.celestiaui.R
import space.celestia.celestiaui.compose.CheckboxRow
import space.celestia.celestiaui.compose.Footer
import space.celestia.celestiaui.compose.FooterLink
import space.celestia.celestiaui.compose.GroupedListItem
import space.celestia.celestiaui.compose.groupedListContentPadding
import space.celestia.celestiaui.compose.Header
import space.celestia.celestiaui.compose.OptionInputDialog
import space.celestia.celestiaui.compose.RadioButtonRow
import space.celestia.celestiaui.compose.SliderRow
import space.celestia.celestiaui.compose.SwitchRow
import space.celestia.celestiaui.compose.TextRow
import space.celestia.celestiaui.settings.viewmodel.Footer
import space.celestia.celestiaui.settings.viewmodel.SettingsActionItem
import space.celestia.celestiaui.settings.viewmodel.SettingsCommonItem
import space.celestia.celestiaui.settings.viewmodel.SettingsItem
import space.celestia.celestiaui.settings.viewmodel.SettingsIntegerSliderItem
import space.celestia.celestiaui.settings.viewmodel.SettingsPreferenceIntegerSliderItem
import space.celestia.celestiaui.settings.viewmodel.SettingsPreferenceSelectionItem
import space.celestia.celestiaui.settings.viewmodel.SettingsPreferenceSliderItem
import space.celestia.celestiaui.settings.viewmodel.SettingsPreferenceSwitchItem
import space.celestia.celestiaui.settings.viewmodel.SettingsSelectionSingleItem
import space.celestia.celestiaui.settings.viewmodel.SettingsSliderItem
import space.celestia.celestiaui.settings.viewmodel.SettingsSwitchItem
import space.celestia.celestiaui.settings.viewmodel.SettingsUnknownTextItem
import space.celestia.celestiaui.settings.viewmodel.SettingsViewModel
import space.celestia.celestiaui.settings.viewmodel.isVisible
import space.celestia.celestiaui.settings.viewmodel.resolvedItem
import space.celestia.celestiaui.settings.viewmodel.settingUnmarkAllID
import space.celestia.celestiaui.utils.PreferenceManager
import java.text.NumberFormat

@Composable
fun SettingsEntryScreen(item: SettingsCommonItem, paddingValues: PaddingValues, linkClicked: (String, Boolean) -> Unit) {
    val viewModel: SettingsViewModel = hiltViewModel()
    LazyColumn(modifier = Modifier
        .nestedScroll(rememberNestedScrollInteropConnection()), contentPadding = groupedListContentPadding(paddingValues)) {
        var visibleSectionCount = 0
        for (index in item.sections.indices) {
            val section = item.sections[index]
            val visibleRows = section.rows.withIndex()
                .filter { it.value.isVisible(viewModel) }
                .map { it.index to it.value.resolvedItem() }
            val rowOffsets = visibleRows.runningFold(0) { count, (_, row) ->
                count + if (row is SettingsSelectionSingleItem) {
                    row.options.size + if (row.showTitle) 1 else 0
                } else {
                    1
                }
            }
            if (rowOffsets.last() == 0) continue
            val isFirst = visibleSectionCount == 0
            visibleSectionCount++
            item(key = "section-$index-header") {
                val header = section.header
                if (header.isNullOrEmpty()) {
                    if (!isFirst) {
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.list_group_spacing)))
                    }
                } else {
                    Header(text = header, isFirst = isFirst)
                }
            }
            itemsIndexed(visibleRows, key = { _, row -> "section-$index-row-${row.first}" }) { rowIndex, row ->
                SettingEntry(item = row.second, viewModel = viewModel, index = rowOffsets[rowIndex], count = rowOffsets.last())
            }
            item(key = "section-$index-footer") {
                val footer = section.footer
                when (footer) {
                    is Footer.Text -> {
                        if (footer.text.isNotEmpty()) {
                            Footer(text = footer.text)
                        }
                    }
                    is Footer.TextWithLink -> {
                        if (footer.text.isNotEmpty()) {
                            FooterLink(text = footer.text, link = footer.link, linkText = footer.linkText, action = {
                                linkClicked(it, footer.localizable)
                            }, modifier = Modifier.fillMaxWidth().padding(
                                start = dimensionResource(id = R.dimen.section_footer_margin_horizontal),
                                top = dimensionResource(id = R.dimen.section_footer_margin_top),
                                end = dimensionResource(id = R.dimen.section_footer_margin_horizontal),
                                bottom = dimensionResource(id = R.dimen.section_footer_margin_bottom)
                            ))
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun SettingEntry(item: SettingsItem, viewModel: SettingsViewModel, index: Int, count: Int) {
    if (item is SettingsSelectionSingleItem) {
        SettingEntryContent(item, viewModel, index, count)
    } else {
        GroupedListItem(index, count) {
            SettingEntryContent(item, viewModel, index, count)
        }
    }
}

@Composable
private fun SettingEntryContent(item: SettingsItem, viewModel: SettingsViewModel, index: Int, count: Int) {
    val scope = rememberCoroutineScope()
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
                        viewModel.refreshSettingVisibility()
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
                        viewModel.refreshSettingVisibility()
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
                GroupedListItem(index, count) {
                    TextRow(primaryText = item.name, secondaryText = item.subtitle)
                }
            }
            val optionOffset = if (item.showTitle) 1 else 0
            for ((optionIndex, option) in item.options.withIndex()) {
                GroupedListItem(index + optionOffset + optionIndex, count) {
                    RadioButtonRow(primaryText = option.second, selected = option.first == selected) {
                        selected = option.first
                        viewModel.coreSettings[PreferenceManager.CustomKey(item.key)] = option.first.toString()
                        viewModel.refreshSettingVisibility()
                        scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                            viewModel.appCore.setIntValueForField(item.key, option.first)
                        }
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
                viewModel.refreshSettingVisibility()
            })
        }

        is SettingsPreferenceSelectionItem -> {
            var selected by remember {
                mutableIntStateOf(viewModel.appSettings[item.key]?.toIntOrNull() ?: item.defaultSelection)
            }
            var showOptions by remember {  mutableStateOf(false) }
            TextRow(
                primaryText = item.name,
                subtitle = item.subtitle,
                secondaryText = item.options.firstOrNull { it.first == selected }?.second,
                modifier = Modifier.clickable(onClick = dropUnlessResumed {
                    showOptions = true
                })
            )

            if (showOptions) {
                OptionInputDialog(onDismissRequest = {
                    showOptions = false
                }, title = null, items = item.options.map { it.second }) { index ->
                    showOptions = false
                    val value = item.options[index].first
                    selected = value
                    viewModel.appSettings[item.key] = value.toString()
                    viewModel.refreshSettingVisibility()
                }
            }
        }

        is SettingsActionItem -> {
            TextRow(primaryText = item.name, modifier = Modifier.clickable(onClick = dropUnlessResumed {
                scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                    viewModel.appCore.charEnter(item.action)
                }
            }))
        }

        is SettingsUnknownTextItem -> {
            TextRow(primaryText = item.name, modifier = Modifier.clickable(onClick = dropUnlessResumed {
                when (item.id) {
                    settingUnmarkAllID -> {
                        scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                            viewModel.appCore.simulation.universe.unmarkAll()
                        }
                    }
                }
            }))
        }

        is SettingsIntegerSliderItem -> {
            var value by remember {
                mutableFloatStateOf(viewModel.appCore.getIntValueForField(item.key).toFloat())
            }
            SliderRow(
                primaryText = item.name,
                secondaryText = item.subtitle,
                value = value,
                valueRange = item.minValue.toFloat()..item.maxValue.toFloat(),
                steps = item.maxValue - item.minValue - 1,
                valueText = value.toInt().toString(),
                onValueChange = { newValue ->
                    val actual = kotlin.math.round(newValue).toInt()
                    value = actual.toFloat()
                    viewModel.coreSettings[PreferenceManager.CustomKey(item.key)] = actual.toString()
                    scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                        viewModel.appCore.setIntValueForField(item.key, actual)
                    }
                }
            )
        }

        is SettingsSliderItem -> {
            val isLog = item.isLogarithmic
            val minValue = item.minValue
            val maxValue = item.maxValue
            val logMin = if (isLog) kotlin.math.ln(minValue) else 0.0
            val logMax = if (isLog) kotlin.math.ln(maxValue) else 0.0
            fun actualToSlider(actual: Double): Float {
                return if (isLog) {
                    val clamped = actual.coerceIn(minValue, maxValue)
                    ((kotlin.math.ln(clamped) - logMin) / (logMax - logMin)).toFloat()
                } else {
                    actual.toFloat()
                }
            }
            fun sliderToActual(slider: Float): Double {
                return if (isLog) {
                    kotlin.math.exp(logMin + slider.toDouble() * (logMax - logMin))
                } else {
                    slider.toDouble()
                }
            }
            val valueRange = if (isLog) 0f..1f else item.minValue.toFloat()..item.maxValue.toFloat()
            val valueFormatter = remember {
                NumberFormat.getNumberInstance().apply {
                    maximumFractionDigits = 2
                }
            }
            var value by remember {
                mutableFloatStateOf(actualToSlider(viewModel.appCore.getDoubleValueForField(item.key)))
            }
            SliderRow(
                primaryText = item.name,
                secondaryText = item.subtitle,
                value = value,
                valueRange = valueRange,
                valueText = valueFormatter.format(sliderToActual(value)),
                onValueChange = { newValue ->
                    value = newValue
                    val actual = sliderToActual(newValue)
                    viewModel.coreSettings[PreferenceManager.CustomKey(item.key)] = actual.toString()
                    scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                        viewModel.appCore.setDoubleValueForField(item.key, actual)
                    }
                }
            )
        }

        is SettingsPreferenceIntegerSliderItem -> {
            val storedValue = viewModel.appSettings[item.key]?.toIntOrNull() ?: item.defaultSelection
            val defaultIndex = item.values.indexOf(item.defaultSelection).coerceAtLeast(0)
            val valueFormatter = remember {
                NumberFormat.getIntegerInstance()
            }
            var selectedIndex by remember {
                mutableIntStateOf(item.values.indexOf(storedValue).takeIf { it >= 0 } ?: defaultIndex)
            }
            SliderRow(
                primaryText = item.name,
                secondaryText = item.subtitle,
                value = selectedIndex.toFloat(),
                valueRange = 0f..item.values.lastIndex.toFloat(),
                steps = item.values.size - 2,
                valueText = valueFormatter.format(item.values[selectedIndex]),
                onValueChange = { newValue ->
                    selectedIndex = kotlin.math.round(newValue).toInt().coerceIn(item.values.indices)
                    viewModel.appSettings[item.key] = item.values[selectedIndex].toString()
                }
            )
        }

        is SettingsPreferenceSliderItem -> {
            val valueFormatter = remember {
                NumberFormat.getNumberInstance().apply {
                    maximumFractionDigits = 2
                }
            }
            var value by remember {
                mutableFloatStateOf(viewModel.appSettings[item.key]?.toFloat() ?: item.defaultValue.toFloat())
            }
            SliderRow(
                primaryText = item.name,
                secondaryText = item.subtitle,
                value = value,
                valueRange = item.minValue.toFloat()..item.maxValue.toFloat(),
                valueText = valueFormatter.format(value.toDouble()),
                onValueChange = { newValue ->
                    value = newValue
                    viewModel.appSettings[item.key] = newValue.toString()
                }
            )
        }
    }
}