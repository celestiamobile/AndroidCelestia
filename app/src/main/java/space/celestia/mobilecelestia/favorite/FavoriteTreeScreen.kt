/*
 * FavoriteTreeScreen.kt
 *
 * Copyright (C) 2025-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
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
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.ContextMenuContainer
import space.celestia.mobilecelestia.compose.DragDropState
import space.celestia.mobilecelestia.compose.DraggableItem
import space.celestia.mobilecelestia.compose.EmptyHint
import space.celestia.mobilecelestia.compose.TextInputDialog
import space.celestia.mobilecelestia.compose.dragContainerForDragHandle
import space.celestia.mobilecelestia.compose.rememberDragDropState
import space.celestia.mobilecelestia.favorite.viewmodel.Favorite
import space.celestia.mobilecelestia.favorite.viewmodel.FavoriteAction
import space.celestia.mobilecelestia.favorite.viewmodel.FavoriteTree
import space.celestia.mobilecelestia.favorite.viewmodel.FavoriteViewModel
import space.celestia.mobilecelestia.utils.CelestiaString

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FavoriteTreeScreen(tree: FavoriteTree<*>, itemSelected: (Favorite) -> Unit, shareItem: (Favorite.Shareable.Object) -> Unit, paddingValues: PaddingValues, modifier: Modifier = Modifier) {
    val children = remember { tree.children.toMutableStateList() }
    val viewModel: FavoriteViewModel = hiltViewModel()

    viewModel.needsRefresh.observe(LocalLifecycleOwner.current) {
        if (it) {
            children.clear()
            children.addAll(tree.children)
            viewModel.setNeedsRefresh(false)
        }
    }

    if (children.isEmpty()) {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues), contentAlignment = Alignment.Center) {
            EmptyHint(text = tree.emptyHint ?: "")
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
            if (tree is FavoriteTree.Editable && fromIndex >= 0 && fromIndex < children.size && toIndex >= 0 && toIndex < children.size) {
                tree.move(fromIndex, toIndex)
                children.add(toIndex, children.removeAt(fromIndex))
            }
        }

        LazyColumn(
            state = listState,
            modifier = modifier,
            contentPadding = contentPadding
        ) {
            itemsIndexed(children, key = { _, item -> item }) { index, item ->
                if (tree is FavoriteTree.Editable) {
                    DraggableItem(dragDropState = dragDropState, index = index, modifier = Modifier.animateItem()) {
                        Item(item = item, tree = tree, index = index, dragDropState = dragDropState, isDraggable = true, remove = {
                            children.removeAt(index)
                        }, itemSelected = { itemSelected(item) }, shareItem = shareItem)
                    }
                } else {
                    Item(item = item, tree = tree, index = index, dragDropState = dragDropState, isDraggable = false, remove = {
                        children.removeAt(index)
                    }, itemSelected = { itemSelected(item) }, shareItem = shareItem)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Item(item: Favorite, tree: FavoriteTree<*>, index: Int, dragDropState: DragDropState, isDraggable: Boolean, remove: () -> Unit, itemSelected: () -> Unit, shareItem: (Favorite.Shareable.Object) -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf(item.title) }
    var showNewNameDialog by remember { mutableStateOf(false) }
    var rowModifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.background)
        .padding(
            horizontal = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal),
        )
    if (isDraggable) {
        rowModifier = Modifier
            .combinedClickable(onLongClick = {
                if (item.supportedActions(tree).isNotEmpty())
                    showMenu = true
            }, onClick = {
                itemSelected()
            })
            .then(rowModifier)
    } else {
        rowModifier = Modifier
            .clickable {
                itemSelected()
            }
            .then(rowModifier)
    }

    BuildSwipeToDismissBox(item = item, tree = tree, index = index, rename = {
        showNewNameDialog = true
    }, remove = remove) {
        BuildContextMenuContainer(item = item, tree = tree, index = index, showMenu = showMenu, dismissMenu = { showMenu = false }, rename = {
            showNewNameDialog = true
        }, remove = remove, shareItem = shareItem) {
            Row(
                modifier = rowModifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.list_item_gap_horizontal)),
            ) {
                Text(
                    title,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .weight(1.0f)
                        .padding(vertical = dimensionResource(id = R.dimen.list_item_medium_margin_vertical))
                )
                if (isDraggable) {
                    Icon(
                        imageVector = Icons.Default.Menu,
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
                if (item.tree != null || item.representation != null) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = colorResource(id = com.google.android.material.R.color.material_on_background_disabled)
                    )
                }
            }
        }
    }

    if (showNewNameDialog && item is Favorite.Renamable) {
        TextInputDialog(
            onDismissRequest = {
                showNewNameDialog = false
            },
            title = CelestiaString("Rename", "Rename a favorite item (currently bookmark)"),
            placeholder = title
        ) {
            showNewNameDialog = false
            if (it != null) {
                title = it
                item.rename(it)
            }
        }
    }
}

@Composable
private fun BuildSwipeToDismissBox(item: Favorite, tree: FavoriteTree<*>, index: Int, rename: () -> Unit, remove: () -> Unit, content: @Composable RowScope.() -> Unit) {
    if (tree is FavoriteTree.Editable || item is Favorite.Renamable) {
        val swipeState = rememberSwipeToDismissBoxState()
        val color: Color?
        val iconColor: Color?
        val icon: ImageVector?
        val alignment: Alignment?
        val contentDescription: String?
        if (tree is FavoriteTree.Editable && swipeState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
            color = MaterialTheme.colorScheme.errorContainer
            iconColor = MaterialTheme.colorScheme.onErrorContainer
            icon = Icons.Outlined.Delete
            alignment = Alignment.CenterEnd
            contentDescription = CelestiaString("Delete", "")
        } else if (item is Favorite.Renamable && swipeState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
            color = MaterialTheme.colorScheme.secondaryContainer
            iconColor = MaterialTheme.colorScheme.onSecondaryContainer
            icon = Icons.Outlined.Edit
            alignment = Alignment.CenterStart
            contentDescription = CelestiaString("Rename", "Rename a favorite item (currently bookmark)")
        } else {
            color = null
            iconColor = null
            icon = null
            alignment = null
            contentDescription = null
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
                            imageVector = icon, contentDescription = contentDescription,
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
                    rename()
                    swipeState.snapTo(SwipeToDismissBoxValue.Settled)
                }
            }
            SwipeToDismissBoxValue.EndToStart -> {
                if (tree is FavoriteTree.Editable) {
                    tree.remove(index)
                    remove()
                }
            }
            SwipeToDismissBoxValue.Settled -> {}
        }
    } else {
        Row(content = content)
    }
}

@Composable
private fun BuildContextMenuContainer(item: Favorite, tree: FavoriteTree<*>, index: Int, showMenu: Boolean, dismissMenu: () -> Unit, rename: () -> Unit, remove: () -> Unit, shareItem: (Favorite.Shareable.Object) -> Unit, content: @Composable BoxScope.() -> Unit) {
    ContextMenuContainer(expanded = item.supportedActions(tree).isNotEmpty() && showMenu, onDismissRequest = dismissMenu, menu = {
        for (supportedAction in item.supportedActions(tree)) {
            DropdownMenuItem(text = { Text(text = supportedAction.title) }, onClick = {
                dismissMenu()
                when (supportedAction) {
                    FavoriteAction.Delete -> {
                        if (tree is FavoriteTree.Editable) {
                            tree.remove(index)
                            remove()
                        }
                    }
                    FavoriteAction.Rename -> {
                        rename()
                    }
                    FavoriteAction.Share -> {
                        if (item is Favorite.Shareable) {
                            val shareableContent = item.shareableContent
                            if (shareableContent != null) {
                                shareItem(shareableContent)
                            }
                        }
                    }
                }
            })
        }
    }, content = content)
}