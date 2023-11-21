/*
 * ToolbarSettingFragment.kt
 *
 * Copyright (C) 2023-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.purchase

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.DraggableItem
import space.celestia.mobilecelestia.compose.Footer
import space.celestia.mobilecelestia.compose.dragContainerForDragHandle
import space.celestia.mobilecelestia.compose.rememberDragDropState
import space.celestia.mobilecelestia.settings.viewmodel.SettingsViewModel
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.PreferenceManager

class ToolbarSettingFragment: SubscriptionBackingFragment() {
    enum class ToolbarAction(val id: String) {
        Mode("mode"),
        Info("info"),
        Search("search"),
        Menu("menu"),
        Hide("hide"),
        ZoomIn("zoom_in"),
        ZoomOut("zoom_out");

        val title: String
            get() {
                return when (this) {
                    Mode -> CelestiaString("Toggle Interaction Mode", "")
                    Info -> CelestiaString("Get Info", "")
                    Search -> CelestiaString("Search", "")
                    Menu -> CelestiaString("Menu", "")
                    Hide -> CelestiaString("Hide", "")
                    ZoomIn -> CelestiaString("Zoom In", "")
                    ZoomOut -> CelestiaString("Zoom Out", "")
                }
            }

        val imageResource: Int
            get() {
                return when (this) {
                    Mode -> R.drawable.tutorial_switch_mode
                    Info -> R.drawable.control_info
                    Search -> R.drawable.control_search
                    Menu -> R.drawable.control_action_menu
                    Hide -> R.drawable.toolbar_exit
                    ZoomIn -> R.drawable.control_zoom_in
                    ZoomOut -> R.drawable.control_zoom_out
                }
            }

        val deletable: Boolean
            get() = this != Menu

        companion object {
            val defaultItems: List<ToolbarAction>
                get() = listOf(
                    Mode,
                    Info,
                    Search,
                    Menu,
                    Hide
                )

            val availableItems: List<ToolbarAction>
                get() = listOf(
                    Mode,
                    Info,
                    Search,
                    Menu,
                    Hide,
                    ZoomIn,
                    ZoomOut
                )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = CelestiaString("Toolbar", "")
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun MainView() {
        val viewModel: SettingsViewModel = hiltViewModel()

        var list by remember {
            val items = ArrayList(viewModel.appSettings.toolbarItems ?: ToolbarAction.defaultItems)
            if (!items.contains(ToolbarAction.Menu))
                items.add(ToolbarAction.Menu)
            mutableStateOf(items.toList())
        }

        val otherItems by remember {
            derivedStateOf { ToolbarAction.availableItems.filterNot { list.contains(it) } }
        }

        val listState = rememberLazyListState()
        val dragDropState = rememberDragDropState(listState) { fromIndex, toIndex ->
            if (fromIndex >= 0 && fromIndex < list.size && toIndex >= 0 && toIndex < list.size) {
                list = list.toMutableList().apply {
                    add(toIndex, removeAt(fromIndex))
                }
                viewModel.appSettings.toolbarItems = list
            }
        }

        @Composable
        fun Item(item: ToolbarAction, isDraggable: Boolean, isDragging: Boolean) {
            var showMenu by remember { mutableStateOf(false) }
            var rowModifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal),
                    vertical = dimensionResource(id = R.dimen.list_item_medium_margin_vertical),
                )
            if (isDraggable && item.deletable) {
                rowModifier = Modifier
                    .clickable {
                        showMenu = true
                    }
                    .then(rowModifier)
            }
            Row(modifier = rowModifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.list_item_gap_horizontal))) {
                Image(painter = painterResource(id = item.imageResource), contentDescription = "Action Icon", colorFilter = ColorFilter.tint(
                    MaterialTheme.colorScheme.onBackground
                ), modifier = Modifier.size(dimensionResource(id = R.dimen.list_item_icon_size)))
                Text(item.title, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1.0f))
                if (isDraggable) {
                    Image(painter = painterResource(id = R.drawable.ic_dehaze), contentDescription = CelestiaString("Drag Handle", ""), colorFilter = ColorFilter.tint(
                        colorResource(id = com.google.android.material.R.color.material_on_background_disabled)
                    ), modifier = Modifier.dragContainerForDragHandle(dragDropState = dragDropState, key = item))
                } else {
                    Image(painter = painterResource(id = R.drawable.ic_add), contentDescription = CelestiaString("Add Button", ""), colorFilter = ColorFilter.tint(
                        colorResource(id = com.google.android.material.R.color.material_on_background_disabled)
                    ), modifier = Modifier.clickable {
                        list = list.toMutableList().apply {
                            add(item)
                        }
                        viewModel.appSettings.toolbarItems = list
                    })
                }
            }

            DropdownMenu(expanded = isDraggable && showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(text = {
                    Text(text = CelestiaString("Delete", ""))
                }, onClick = {
                    val index = list.indexOf(item)
                    if (index >= 0 && index < list.size) {
                        list = list.toMutableList().apply {
                            removeAt(index)
                        }
                        viewModel.appSettings.toolbarItems = list
                    }
                })
            }
        }

        val systemPadding = WindowInsets.systemBars.asPaddingValues()
        val direction = LocalLayoutDirection.current
        val contentPadding = PaddingValues(
            start = systemPadding.calculateStartPadding(direction),
            top = dimensionResource(id = R.dimen.common_page_medium_margin_vertical) + systemPadding.calculateTopPadding(),
            end = systemPadding.calculateEndPadding(direction),
            bottom = dimensionResource(id = R.dimen.list_spacing_tall) + systemPadding.calculateBottomPadding(),
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.nestedScroll(rememberNestedScrollInteropConnection()),
            contentPadding = contentPadding
        ) {
            itemsIndexed(list, key = { _, item -> item }) { index, item ->
                DraggableItem(dragDropState, index) { isDragging ->
                    Item(item = item, isDraggable = true, isDragging = isDragging)
                }
            }

            itemsIndexed(otherItems, key = { _, item -> item }) { index, item ->
                Item(item = item, isDraggable = false, isDragging = false)
            }

            item {
                FilledTonalButton(modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal),
                        vertical = dimensionResource(id = R.dimen.common_page_medium_gap_vertical),
                    ), onClick = {
                    list = ToolbarAction.defaultItems
                    viewModel.appSettings.toolbarItems = null
                }) {
                    Text(text = CelestiaString("Reset to Default", ""))
                }
                Footer(text = CelestiaString("Configuration will take effect after a restart.", ""))
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = ToolbarSettingFragment()
    }
}

var PreferenceManager.toolbarItems: List<ToolbarSettingFragment.ToolbarAction>?
    get() {
        val savedValue = get(PreferenceManager.PredefinedKey.ToolbarItems) ?: return null
        val items = arrayListOf<ToolbarSettingFragment.ToolbarAction>()
        for (item in savedValue.split(",")) {
            val match = ToolbarSettingFragment.ToolbarAction.availableItems.firstOrNull { it.id == item }
                ?: return null
            if (!items.contains(match))
                items.add(match)
        }
        return items
    }
    set(value) {
        if (value == null) {
            set(PreferenceManager.PredefinedKey.ToolbarItems, null)
        } else {
            set(PreferenceManager.PredefinedKey.ToolbarItems, value.map { it.id }.joinToString(separator = ","))
        }
    }