/*
 * FavoriteScreen.kt
 *
 * Copyright (C) 2025-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.favorite

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import space.celestia.mobilecelestia.favorite.viewmodel.Favorite
import space.celestia.mobilecelestia.favorite.viewmodel.FavoriteRepresentation
import space.celestia.mobilecelestia.favorite.viewmodel.FavoriteTree
import space.celestia.mobilecelestia.favorite.viewmodel.FavoriteViewModel
import space.celestia.mobilecelestia.utils.currentBookmark

@Serializable
class FavoriteTreeView(val id: Int)

@Serializable
class DestinationView(val id: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(shareItem: (Favorite.Shareable.Object) -> Unit, saveFavorites: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var title by remember { mutableStateOf("") }
    var canPop by remember { mutableStateOf(false) }
    var canAddNewItem by remember { mutableStateOf(false) }

    val viewModelStoreOwner = requireNotNull(LocalViewModelStoreOwner.current)

    val viewModel: FavoriteViewModel = hiltViewModel()
    if (viewModel.treeMap.isEmpty())
        viewModel.treeMap[FavoriteTree.Root.hashCode()] = FavoriteTree.Root

    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { controller, _, _ ->
            canPop = controller.previousBackStackEntry != null
            val currentBackStackEntry = controller.currentBackStackEntry
            if (currentBackStackEntry != null) {
                if (currentBackStackEntry.destination.hasRoute<FavoriteTreeView>()) {
                    val tree = requireNotNull(viewModel.treeMap[currentBackStackEntry.toRoute<FavoriteTreeView>().id])
                    title = tree.title
                    canAddNewItem = tree is FavoriteTree.Editable
                } else if (currentBackStackEntry.destination.hasRoute<DestinationView>()) {
                    title = requireNotNull(viewModel.destinationMap[currentBackStackEntry.toRoute<DestinationView>().id]).name
                    canAddNewItem = false
                } else {
                    title = ""
                    canAddNewItem = false
                }
            } else {
                title = ""
                canAddNewItem = false
            }
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            saveFavorites()
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    Scaffold(topBar = {
        TopAppBar(title = {
            Text(text = title)
        }, navigationIcon = {
            if (canPop) {
                IconButton(onClick = {
                    navController.navigateUp()
                }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "")
                }
            }
        }, actions = {
            if (canAddNewItem) {
                IconButton(onClick = {
                    val currentBackStackEntry = navController.currentBackStackEntry
                    if (currentBackStackEntry != null && currentBackStackEntry.destination.hasRoute<FavoriteTreeView>()) {
                        val tree = requireNotNull(viewModel.treeMap[currentBackStackEntry.toRoute<FavoriteTreeView>().id])
                        if (tree is FavoriteTree.Editable) {
                            when (tree) {
                                is FavoriteTree.Bookmark -> {
                                    val bookmark = viewModel.appCore.currentBookmark
                                    if (bookmark != null) {
                                        tree.add(Favorite.Bookmark(bookmark))
                                        viewModel.setNeedsRefresh(true)
                                    }
                                }
                            }
                        }
                    }
                }) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = ""
                    )
                }
            }
        }, scrollBehavior = scrollBehavior)
    }) { paddingValues ->
        NavHost(navController = navController, startDestination = FavoriteTreeView(FavoriteTree.Root.hashCode())) {
            composable<FavoriteTreeView> {
                CompositionLocalProvider(
                    LocalViewModelStoreOwner provides viewModelStoreOwner
                ) {
                    FavoriteTreeScreen(
                        tree = requireNotNull(viewModel.treeMap[it.toRoute<FavoriteTreeView>().id]),
                        paddingValues = paddingValues,
                        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                        itemSelected = { handleItemSelected(it, navController, viewModel, scope) },
                        shareItem = shareItem
                    )
                }
            }

            composable<DestinationView> {
                DestinationScreen(destination = requireNotNull(viewModel.destinationMap[it.toRoute<DestinationView>().id]), paddingValues = paddingValues, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection))
            }
        }
    }
}

private fun handleItemSelected(item: Favorite, navController: NavHostController, viewModel: FavoriteViewModel, scope: CoroutineScope) {
    val tree = item.tree
    if (tree != null) {
        viewModel.treeMap[tree.hashCode()] = tree
        navController.navigate(FavoriteTreeView(tree.hashCode()))
    } else {
        val representation = item.representation
        if (representation != null) {
            when (representation) {
                is FavoriteRepresentation.Destination -> {
                    viewModel.destinationMap[representation.destination.hashCode()] = representation.destination
                    navController.navigate(DestinationView(representation.destination.hashCode()))
                }
            }
        } else {
            when (item) {
                is Favorite.Bookmark -> {
                    if (item.bookmark.isLeaf)
                        scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                            viewModel.appCore.goToURL(item.bookmark.url)
                        }
                }
                is Favorite.Script -> {
                    scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                        viewModel.appCore.runScript(item.script.filename)
                    }
                }
                else -> {}
            }
        }
    }
}