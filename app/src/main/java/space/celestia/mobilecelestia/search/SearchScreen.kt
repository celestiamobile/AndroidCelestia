/*
 * SearchScreen.kt
 *
 * Copyright (C) 2023-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */


package space.celestia.mobilecelestia.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import space.celestia.celestia.Completion
import space.celestia.celestia.Selection
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.EmptyHint
import space.celestia.mobilecelestia.compose.TextRow
import space.celestia.mobilecelestia.info.InfoScreen
import space.celestia.mobilecelestia.info.model.InfoActionItem
import space.celestia.mobilecelestia.settings.viewmodel.SearchViewModel
import space.celestia.mobilecelestia.utils.CelestiaString
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(objectNotFoundHandler: () -> Unit, linkHandler: (URL) -> Unit, actionHandler: (InfoActionItem, Selection) -> Unit) {
    val viewModel: SearchViewModel = hiltViewModel()
    var searchKey by rememberSaveable {
        mutableStateOf("")
    }
    var searchResults by rememberSaveable {
        mutableStateOf(listOf<Completion>())
    }
    var isSearchActive by rememberSaveable {
        mutableStateOf(false)
    }
    var isSearching by remember {
        mutableStateOf(false)
    }
    var currentSelection: Selection? by remember {
        mutableStateOf(null)
    }
    var isLoadingPage by remember {
        mutableStateOf(false)
    }
    val scope = rememberCoroutineScope()
    Scaffold(topBar = {
        SearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query = searchKey,
                    onQueryChange = {
                        searchKey = it
                        isSearching = true
                        scope.launch {
                            val result = if (it.isEmpty()) listOf<Completion>() else withContext(viewModel.executor.asCoroutineDispatcher()) { viewModel.appCore.simulation.completionForText(it, 100) }
                            if (searchKey == it) {
                                searchResults = result
                                isSearching = false
                            }
                        }
                    },
                    onSearch = {
                        isSearchActive = false
                        if (searchKey.isNotEmpty() && searchResults.isEmpty()) {
                            isLoadingPage = true
                            scope.launch {
                                val selection = withContext(viewModel.executor.asCoroutineDispatcher()) { viewModel.appCore.simulation.findObject(searchKey) }
                                isLoadingPage = false
                                if (selection.isEmpty)
                                    objectNotFoundHandler()
                                else
                                    currentSelection = selection
                            }
                        }
                    },
                    expanded = isSearchActive,
                    onExpandedChange = {
                        isSearchActive = it
                    },
                    placeholder = {
                        Text(text = CelestiaString("Search", ""))
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                    },
                )
            },
            expanded = isSearchActive,
            onExpandedChange = {
                isSearchActive = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (!isSearchActive) dimensionResource(
                        id = R.dimen.search_bar_padding_horizontal
                    ) else 0.dp,
                    vertical = if (!isSearchActive) dimensionResource(
                        id = R.dimen.search_bar_padding_vertical
                    ) else 0.dp
                ),
            content = {
                SearchResult(
                    key = searchKey,
                    results = searchResults.toList(),
                    isSearching = isSearching
                ) {
                    isSearchActive = false
                    isLoadingPage = true
                    scope.launch {
                        isLoadingPage = false
                        if (it.isEmpty)
                            objectNotFoundHandler()
                        else
                            currentSelection = it
                    }
                }
            },
        )
    }) { paddingValues ->
        SearchContent(selection = currentSelection, isLoadingPage = isLoadingPage, linkHandler = linkHandler, actionHandler = actionHandler, paddingValues = paddingValues)
    }
}

@Composable
private fun SearchContent(selection: Selection?, isLoadingPage: Boolean, linkHandler: (URL) -> Unit, actionHandler: (InfoActionItem, Selection) -> Unit, paddingValues: PaddingValues) {
    if (isLoadingPage) {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(color = MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (selection != null) {
        InfoScreen(selection = selection, showTitle = true, linkHandler = linkHandler, actionHandler = actionHandler, paddingValues = paddingValues)
    }
}

@Composable
private fun SearchResult(key: String, results: List<Completion>, isSearching: Boolean, selectionHandler: (Selection) -> Unit) {
    if (results.isEmpty() && isSearching) {
        Box(modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .background(color = MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (results.isEmpty()) {
        Box(modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .background(color = MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            EmptyHint(text = if (key.isEmpty()) CelestiaString("Find stars, DSOs, and nearby objects", "") else CelestiaString("No result found", ""))
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .background(color = MaterialTheme.colorScheme.background)
        ) {
            items(results) {
                TextRow(primaryText = it.name, modifier = Modifier.clickable {
                    selectionHandler(it.selection)
                })
            }
        }
    }
}