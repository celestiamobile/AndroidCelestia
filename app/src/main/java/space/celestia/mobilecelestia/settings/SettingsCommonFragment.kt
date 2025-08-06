// SettingsCommonFragment.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBars
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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.core.os.BundleCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import space.celestia.celestia.AppCore
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.CelestiaExecutor
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.compose.CheckboxRow
import space.celestia.mobilecelestia.compose.Footer
import space.celestia.mobilecelestia.compose.Header
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.compose.RadioButtonRow
import space.celestia.mobilecelestia.compose.Separator
import space.celestia.mobilecelestia.compose.SliderRow
import space.celestia.mobilecelestia.compose.SwitchRow
import space.celestia.mobilecelestia.compose.TextRow
import space.celestia.mobilecelestia.di.AppSettings
import space.celestia.mobilecelestia.di.CoreSettings
import space.celestia.mobilecelestia.utils.PreferenceManager
import space.celestia.mobilecelestia.utils.showOptions
import javax.inject.Inject

@AndroidEntryPoint
class SettingsCommonFragment : NavigationFragment.SubFragment() {
    private var item: SettingsCommonItem? = null

    @Inject
    lateinit var appCore: AppCore
    @Inject
    lateinit var executor: CelestiaExecutor

    @AppSettings
    @Inject
    lateinit var appSettings: PreferenceManager
    @CoreSettings
    @Inject
    lateinit var coreSettings: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            item = BundleCompat.getSerializable(it, ARG_ITEM, SettingsCommonItem::class.java)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Mdc3Theme {
                    MainScreen()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = item?.name ?: ""
    }

    @Composable
    private fun MainScreen() {
        val settingItem = item ?: return
        LazyColumn(modifier = Modifier
            .nestedScroll(rememberNestedScrollInteropConnection()), contentPadding = WindowInsets.systemBars.asPaddingValues()) {
            for (index in settingItem.sections.indices) {
                val section = settingItem.sections[index]
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
                    if (!footer.isNullOrEmpty()) {
                        Footer(text = footer)
                    }
                    if (index == settingItem.sections.size - 1) {
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_tall)))
                    } else {
                         val nextHeader = settingItem.sections[index + 1].header
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
    private fun SettingEntry(item: SettingsItem) {
        val scope = rememberCoroutineScope()
        when (item) {
            is SettingsSwitchItem -> {
                var on by remember {
                    mutableStateOf(appCore.getBooleanValueForPield(item.key))
                }
                when (item.representation) {
                    SettingsSwitchItem.Representation.Switch -> {
                        SwitchRow(primaryText = item.name, secondaryText = item.subtitle, checked = on, onCheckedChange = { newValue ->
                            on = newValue
                            if (!item.volatile)
                                coreSettings[PreferenceManager.CustomKey(item.key)] = if (newValue) "1" else "0"
                            scope.launch(executor.asCoroutineDispatcher()) {
                                appCore.setBooleanValueForField(item.key, newValue)
                            }
                        })
                    }
                    SettingsSwitchItem.Representation.Checkmark -> {
                        CheckboxRow(primaryText = item.name, secondaryText = item.subtitle, checked = on, onCheckedChange = { newValue ->
                            on = newValue
                            if (!item.volatile)
                                coreSettings[PreferenceManager.CustomKey(item.key)] = if (newValue) "1" else "0"
                            scope.launch(executor.asCoroutineDispatcher()) {
                                appCore.setBooleanValueForField(item.key, newValue)
                            }
                        })
                    }
                }
            }

            is SettingsSelectionSingleItem -> {
                var selected by remember {
                    val value = appCore.getIntValueForField(item.key)
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
                        coreSettings[PreferenceManager.CustomKey(item.key)] = option.first.toString()
                        scope.launch(executor.asCoroutineDispatcher()) {
                            appCore.setIntValueForField(item.key, option.first)
                        }
                    }
                }
            }

            is SettingsPreferenceSwitchItem -> {
                var on by remember {
                    mutableStateOf(when (appSettings[item.key]) { "true" -> true "false" -> false else -> item.defaultOn })
                }
                SwitchRow(primaryText = item.name, secondaryText = item.subtitle, checked = on, onCheckedChange = { newValue ->
                    on = newValue
                    appSettings[item.key] = if (newValue) "true" else "false"
                })
            }

            is SettingsPreferenceSelectionItem -> {
                var selected by remember {
                    mutableIntStateOf(appSettings[item.key]?.toIntOrNull() ?: item.defaultSelection)
                }
                TextRow(
                    primaryText = item.name,
                    secondaryText = item.options.firstOrNull { it.first == selected }?.second,
                    modifier = Modifier.clickable {
                        val activity = this.activity ?: return@clickable
                        activity.showOptions("", options = item.options.map { it.second }.toTypedArray()) { index ->
                            val value = item.options[index].first
                            selected = value
                            appSettings[item.key] = value.toString()
                        }
                    }
                )
            }

            is SettingsActionItem -> {
                TextRow(primaryText = item.name, modifier = Modifier.clickable {
                    scope.launch(executor.asCoroutineDispatcher()) {
                        appCore.charEnter(item.action)
                    }
                })
            }

            is SettingsUnknownTextItem -> {
                TextRow(primaryText = item.name, modifier = Modifier.clickable {
                    when (item.id) {
                        settingUnmarkAllID -> {
                            scope.launch(executor.asCoroutineDispatcher()) {
                                appCore.simulation.universe.unmarkAll()
                            }
                        }
                    }
                })
            }

            is SettingsSliderItem -> {
                var value by remember {
                    mutableFloatStateOf(appCore.getDoubleValueForField(item.key).toFloat())
                }
                SliderRow(primaryText = item.name, value = value, valueRange = item.minValue.toFloat()..item.maxValue.toFloat(), onValueChange = { newValue ->
                    value = newValue
                    coreSettings[PreferenceManager.CustomKey(item.key)] = newValue.toString()
                    scope.launch(executor.asCoroutineDispatcher()) {
                        appCore.setDoubleValueForField(item.key, newValue.toDouble())
                    }
                })
            }

            is SettingsPreferenceSliderItem -> {
                var value by remember {
                    mutableFloatStateOf(appSettings[item.key]?.toFloat() ?: item.defaultValue.toFloat())
                }
                SliderRow(primaryText = item.name, secondaryText = item.subtitle, value = value, valueRange = item.minValue.toFloat()..item.maxValue.toFloat(), onValueChange = { newValue ->
                    value = newValue
                    appSettings[item.key] = newValue.toString()
                })
            }
        }
    }

    companion object {
        private const val ARG_ITEM = "item"

        @JvmStatic
        fun newInstance(item: SettingsCommonItem) =
            SettingsCommonFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ITEM, item)
                }
            }
    }
}
