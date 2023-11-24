/*
 * FavoriteItemFragment.kt
 *
 * Copyright (C) 2023-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.favorite

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import space.celestia.celestiafoundation.utils.getSerializableValue
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.compose.ContextMenuContainer
import space.celestia.mobilecelestia.compose.DragDropState
import space.celestia.mobilecelestia.compose.DraggableItem
import space.celestia.mobilecelestia.compose.EmptyHint
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.compose.dragContainerForDragHandle
import space.celestia.mobilecelestia.compose.rememberDragDropState
import space.celestia.mobilecelestia.utils.CelestiaString

class FavoriteItemFragment : NavigationFragment.SubFragment() {
    private var listener: Listener? = null

    var favoriteItem: FavoriteBaseItem? = null
    private var childItems = mutableStateListOf<FavoriteBaseItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            favoriteItem = it.getSerializableValue(ARG_ITEM, FavoriteBaseItem::class.java)
            reload()
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

        title = favoriteItem?.title ?: ""
        var items: List<NavigationFragment.BarButtonItem> = listOf()
        if (favoriteItem is MutableFavoriteBaseItem) {
            items = listOf(
                NavigationFragment.BarButtonItem(MENU_ITEM_ADD, CelestiaString("Add", ""), R.drawable.ic_add)
            )
        }
        rightNavigationBarItems = items
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement FavoriteItemFragment.Listener")
        }
    }

    override fun menuItemClicked(groupId: Int, id: Int): Boolean {
        when (id) {
            MENU_ITEM_ADD -> {
                listener?.addFavoriteItem(favoriteItem as MutableFavoriteBaseItem)
            } else -> {}
        }
        return true
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun MainScreen() {
        if (childItems.isEmpty()) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .systemBarsPadding(), contentAlignment = Alignment.Center) {
                EmptyHint(text = favoriteItem?.emptyHint ?: "")
            }
        } else {
            val systemPadding = WindowInsets.systemBars.asPaddingValues()
            val direction = LocalLayoutDirection.current
            val contentPadding = PaddingValues(
                start = systemPadding.calculateStartPadding(direction),
                top = dimensionResource(id = R.dimen.list_spacing_short) + systemPadding.calculateTopPadding(),
                end = systemPadding.calculateEndPadding(direction),
                bottom = dimensionResource(id = R.dimen.list_spacing_tall) + systemPadding.calculateBottomPadding(),
            )

            val listState = rememberLazyListState()
            val dragDropState = rememberDragDropState(listState) { fromIndex, toIndex ->
                if (favoriteItem is MutableFavoriteBaseItem && fromIndex >= 0 && fromIndex < childItems.size && toIndex >= 0 && toIndex < childItems.size) {
                    listener?.moveFavoriteItem(fromIndex, toIndex)
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.nestedScroll(rememberNestedScrollInteropConnection()),
                contentPadding = contentPadding
            ) {
                itemsIndexed(childItems, key = { _, item -> item }) { index, item ->
                    if (favoriteItem is MutableFavoriteBaseItem) {
                        DraggableItem(dragDropState = dragDropState, index = index) {
                            Item(item = item, index = index, dragDropState = dragDropState, isDraggable = true)
                        }
                    } else {
                        Item(item = item, index = index, dragDropState = dragDropState, isDraggable = false)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun Item(item: FavoriteBaseItem, index: Int, dragDropState: DragDropState, isDraggable: Boolean) {
        var showMenu by remember { mutableStateOf(false) }
        var rowModifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal),
            )
        if (isDraggable) {
            rowModifier = Modifier
                .combinedClickable(onLongClick = {
                    if (!item.supportedItemActions.isEmpty())
                        showMenu = true
                }, onClick = {
                    listener?.onFavoriteItemSelected(item)
                })
                .then(rowModifier)
        } else {
            rowModifier = Modifier
                .clickable {
                    listener?.onFavoriteItemSelected(item)
                }
                .then(rowModifier)
        }
        ContextMenuContainer(expanded = !item.supportedItemActions.isEmpty() && showMenu, onDismissRequest = { showMenu = false }, menu = {
            if (item !is MutableFavoriteBaseItem) return@ContextMenuContainer
            for (supportedAction in item.supportedItemActions) {
                DropdownMenuItem(text = { Text(text = supportedAction.title) }, onClick = {
                    showMenu = false
                    when (supportedAction) {
                        FavoriteItemAction.Delete -> {
                            listener?.deleteFavoriteItem(index)
                        }
                        FavoriteItemAction.Rename -> {
                            listener?.renameFavoriteItem(item)
                        }
                        FavoriteItemAction.Share -> {
                            listener?.shareFavoriteItem(item)
                        }
                    }
                })
            }
        }) {
            Row(modifier = rowModifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.list_item_gap_horizontal))) {
                Text(item.title, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyLarge, modifier = Modifier
                    .weight(1.0f)
                    .padding(vertical = dimensionResource(id = R.dimen.list_item_medium_margin_vertical),))
                if (isDraggable) {
                    Icon(imageVector = Icons.Default.Menu, contentDescription = CelestiaString("Drag Handle", ""), tint = colorResource(id = com.google.android.material.R.color.material_on_background_disabled), modifier = Modifier
                        .dragContainerForDragHandle(dragDropState = dragDropState, key = item)
                        .padding(
                            dimensionResource(id = R.dimen.list_item_action_icon_padding)
                        ))
                }
                if (!item.isLeaf || item.hasFullPageRepresentation) {
                    Image(painter = painterResource(id = R.drawable.accessory_full_disclosure), contentDescription = "", colorFilter = ColorFilter.tint(colorResource(id = com.google.android.material.R.color.material_on_background_disabled)))
                }
            }
        }
    }

    fun reload() {
        childItems.clear()
        childItems.addAll(favoriteItem?.children ?: listOf())
    }

    interface Listener {
        fun onFavoriteItemSelected(item: FavoriteBaseItem)
        fun deleteFavoriteItem(index: Int)
        fun renameFavoriteItem(item: MutableFavoriteBaseItem)
        fun addFavoriteItem(item: MutableFavoriteBaseItem)
        fun shareFavoriteItem(item: MutableFavoriteBaseItem)
        fun moveFavoriteItem(fromIndex: Int, toIndex: Int)
    }

    companion object {
        const val ARG_ITEM = "item"
        const val MENU_ITEM_ADD = 0

        @JvmStatic
        fun newInstance(item: FavoriteBaseItem) =
            FavoriteItemFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ITEM, item)
                }
            }
    }
}
