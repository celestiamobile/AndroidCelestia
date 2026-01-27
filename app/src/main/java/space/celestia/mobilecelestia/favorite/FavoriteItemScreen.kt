// FavoriteItemScreen.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.favorite

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.ContextMenuContainer
import space.celestia.mobilecelestia.compose.DragDropState
import space.celestia.mobilecelestia.compose.DraggableItem
import space.celestia.mobilecelestia.compose.EmptyHint
import space.celestia.mobilecelestia.compose.TextInputDialog
import space.celestia.mobilecelestia.compose.dragContainerForDragHandle
import space.celestia.mobilecelestia.compose.rememberDragDropState
import space.celestia.mobilecelestia.favorite.viewmodel.FavoriteViewModel
import space.celestia.mobilecelestia.favorite.viewmodel.Page
import space.celestia.mobilecelestia.utils.CelestiaString

sealed class FavoriteItemAlert {
    data class Rename(val item: MutableFavoriteBaseItem): FavoriteItemAlert()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FavoriteItemScreen(parent: FavoriteBaseItem, paddingValues: PaddingValues, shareRequested: (MutableFavoriteBaseItem) -> Unit, openBookmarkRequested: (FavoriteBookmarkItem) -> Unit, openScriptRequested: (FavoriteScriptItem) -> Unit) {
    val viewModel: FavoriteViewModel = hiltViewModel()
    var alert by remember { mutableStateOf<FavoriteItemAlert?>(null)  }
    if (parent.children.isEmpty()) {
        Box(modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(), contentAlignment = Alignment.Center) {
            EmptyHint(text = parent.emptyHint ?: "")
        }
    } else {
        val direction = LocalLayoutDirection.current
        val contentPadding = PaddingValues(
            start = paddingValues.calculateStartPadding(direction),
            top = dimensionResource(id = R.dimen.list_spacing_short) + paddingValues.calculateTopPadding(),
            end = paddingValues.calculateEndPadding(direction),
            bottom = dimensionResource(id = R.dimen.list_spacing_tall) + paddingValues.calculateBottomPadding(),
        )

        val listState = rememberLazyListState()
        val dragDropState = rememberDragDropState(listState) { fromIndex, toIndex ->
            if (parent is MutableFavoriteBaseItem && fromIndex >= 0 && fromIndex < parent.children.size && toIndex >= 0 && toIndex < parent.children.size) {
                parent.move(fromIndex, toIndex)
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.nestedScroll(rememberNestedScrollInteropConnection()),
            contentPadding = contentPadding
        ) {
            itemsIndexed(parent.children, key = { _, item -> item }) { index, item ->
                val selectionHandler: () -> Unit = {
                    if (item.isLeaf) {
                        when (item) {
                            is FavoriteScriptItem -> {
                                openScriptRequested(item)
                            }
                            is FavoriteBookmarkItem -> {
                                openBookmarkRequested(item)
                            }
                            is FavoriteDestinationItem -> {
                                viewModel.backStack.add(Page.Destination(item.destination))
                            }
                            else -> {}
                        }
                    } else {
                        viewModel.backStack.add(Page.Item(item))
                    }
                }

                val shareHandler: () -> Unit = if (item is MutableFavoriteBaseItem) {{
                    shareRequested(item)
                }} else {{}}

                if (parent is MutableFavoriteBaseItem) {
                    val deleteHandler: () -> Unit = {
                        parent.remove(index)
                    }

                    val renameHandler: () -> Unit = if (item is MutableFavoriteBaseItem) {{
                        alert = FavoriteItemAlert.Rename(item)
                    }} else {{}}
                    DraggableItem(dragDropState = dragDropState, index = index, modifier = Modifier.animateItem()) {
                        Item(item = item, index = index, dragDropState = dragDropState, isDraggable = true, selected = selectionHandler, deleteRequested = deleteHandler, renameRequested = renameHandler, shareRequested = shareHandler)
                    }
                } else {
                    Item(item = item, index = index, dragDropState = dragDropState, isDraggable = false, selected = selectionHandler, deleteRequested = {}, renameRequested = {}, shareRequested = shareHandler)
                }
            }
        }
    }

    alert?.let { content ->
        when (content) {
            is FavoriteItemAlert.Rename -> {
                TextInputDialog(onDismissRequest = {
                    alert = null
                }, title = CelestiaString("Rename", "Rename a favorite item (currently bookmark)"), placeholder = content.item.title) {
                    alert = null
                    if (it != null) {
                        content.item.rename(it)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Item(item: FavoriteBaseItem, index: Int, dragDropState: DragDropState, isDraggable: Boolean, selected: () -> Unit, renameRequested: () -> Unit, deleteRequested: () -> Unit, shareRequested: () -> Unit, ) {
    var showMenu by remember { mutableStateOf(false) }
    var rowModifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.background)
        .defaultMinSize(minHeight = dimensionResource(R.dimen.list_item_one_line_min_height))
        .padding(
            horizontal = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal)
        )
    if (isDraggable) {
        rowModifier = Modifier
            .combinedClickable(onLongClick = {
                if (item.supportedItemActions.isNotEmpty())
                    showMenu = true
            }, onClick = {
                selected()
            })
            .then(rowModifier)
    } else {
        rowModifier = Modifier
            .clickable {
                selected()
            }
            .then(rowModifier)
    }
    BuildSwipeToDismissBox(item = item, index = index, renameRequested = renameRequested, deleteRequested = deleteRequested) {
        BuildContextMenuContainer(item = item, index = index, showMenu = showMenu, dismissMenu = { showMenu = false }, renameRequested = renameRequested, deleteRequested = deleteRequested, shareRequested = shareRequested) {
            Row(
                modifier = rowModifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.list_item_gap_horizontal)),
            ) {
                Text(
                    item.title,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .weight(1.0f)
                        .padding(vertical = dimensionResource(id = R.dimen.list_item_medium_margin_vertical))
                )
                if (isDraggable) {
                    Icon(
                        painter = painterResource(id = R.drawable.reorder_24px),
                        contentDescription = CelestiaString(
                            "Drag Handle",
                            "Accessibility description for the drag handle for reorder"
                        ),
                        tint = colorResource(id = com.google.android.material.R.color.material_on_background_disabled),
                        modifier = Modifier
                            .dragContainerForDragHandle(
                                dragDropState = dragDropState,
                                key = item
                            )
                            .padding(
                                dimensionResource(id = R.dimen.list_item_action_icon_padding)
                            )
                    )
                }
                if (!item.isLeaf || item.hasFullPageRepresentation) {
                    Image(
                        painter = painterResource(id = R.drawable.accessory_full_disclosure),
                        contentDescription = "",
                        colorFilter = ColorFilter.tint(colorResource(id = com.google.android.material.R.color.material_on_background_disabled))
                    )
                }
            }
        }
    }
}

@Composable
private fun BuildSwipeToDismissBox(item: FavoriteBaseItem, index: Int, renameRequested: () -> Unit, deleteRequested: () -> Unit, content: @Composable RowScope.() -> Unit) {
    if (item is MutableFavoriteBaseItem && (item.supportedItemActions.contains(FavoriteItemAction.Delete) || item.supportedItemActions.contains(FavoriteItemAction.Rename))) {
        val swipeState = rememberSwipeToDismissBoxState()
        val color: Color?
        val iconColor: Color?
        val icon: Painter?
        val alignment: Alignment?
        if (item.supportedItemActions.contains(FavoriteItemAction.Delete) && swipeState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
            color = MaterialTheme.colorScheme.errorContainer
            iconColor = MaterialTheme.colorScheme.onErrorContainer
            icon = painterResource(id = R.drawable.delete_24px)
            alignment = Alignment.CenterEnd
        } else if (item.supportedItemActions.contains(FavoriteItemAction.Rename) && swipeState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
            color = MaterialTheme.colorScheme.secondaryContainer
            iconColor = MaterialTheme.colorScheme.onSecondaryContainer
            icon = painterResource(id = R.drawable.edit_24px)
            alignment = Alignment.CenterStart
        } else {
            color = null
            iconColor = null
            icon = null
            alignment = null
        }
        SwipeToDismissBox(
            state = swipeState,
            backgroundContent = {
                if (color != null && icon != null && iconColor != null && alignment != null) {
                    Box(
                        contentAlignment = alignment,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color)
                    ) {
                        Icon(
                            modifier = Modifier.minimumInteractiveComponentSize(),
                            painter = icon, contentDescription = null,
                            tint = iconColor
                        )
                    }
                }
            },
            content = content
        )
        when (swipeState.currentValue) {
            SwipeToDismissBoxValue.StartToEnd -> {
                LaunchedEffect(swipeState) {
                    renameRequested()
                    swipeState.snapTo(SwipeToDismissBoxValue.Settled)
                }
            }
            SwipeToDismissBoxValue.EndToStart -> {
                deleteRequested()
            }
            SwipeToDismissBoxValue.Settled -> {}
        }
    } else {
        Row(content = content)
    }
}

@Composable
private fun BuildContextMenuContainer(item: FavoriteBaseItem, index: Int, showMenu: Boolean, dismissMenu: () -> Unit, renameRequested: () -> Unit, deleteRequested: () -> Unit, shareRequested: () -> Unit, content: @Composable BoxScope.() -> Unit) {
    ContextMenuContainer(expanded = item.supportedItemActions.isNotEmpty() && showMenu, onDismissRequest = dismissMenu, menu = {
        if (item !is MutableFavoriteBaseItem) return@ContextMenuContainer
        for (supportedAction in item.supportedItemActions) {
            DropdownMenuItem(text = { Text(text = supportedAction.title) }, onClick = {
                dismissMenu()
                when (supportedAction) {
                    FavoriteItemAction.Delete -> {
                        deleteRequested()
                    }
                    FavoriteItemAction.Rename -> {
                        renameRequested()
                    }
                    FavoriteItemAction.Share -> {
                        shareRequested()
                    }
                }
            })
        }
    }, content = content)
}