// SearchScreen.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.celestiaui.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.withResumed
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import space.celestia.celestia.Completion
import space.celestia.celestia.Selection
import space.celestia.celestiaui.R
import space.celestia.celestiaui.compose.EmptyHint
import space.celestia.celestiaui.compose.Header
import space.celestia.celestiaui.compose.SheetHeaderColor
import space.celestia.celestiaui.compose.SimpleAlertDialog
import space.celestia.celestiaui.compose.TextRow
import space.celestia.celestiaui.compose.groupedItems
import space.celestia.celestiaui.compose.groupedListContentPadding
import space.celestia.celestiaui.compose.contentWindowInsetsIgnoringVisibility
import space.celestia.celestiaui.info.InfoScreen
import space.celestia.celestiaui.search.viewmodel.RecentSearch
import space.celestia.celestiaui.search.viewmodel.SearchViewModel
import space.celestia.celestiaui.utils.CelestiaString

sealed class SearchAlert {
    data object ObjectNotFound: SearchAlert()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(prepareForInput: suspend () -> Boolean, linkClicked: (String) -> Unit, openSubsystem: (Selection) -> Unit, openRelatedAddons: (String) -> Unit, openSubscriptionManagement: () -> Unit) {
    val viewModel: SearchViewModel = hiltViewModel()
    val textFieldState = rememberTextFieldState()
    val searchBarState = rememberSearchBarState()
    val isSearchExpanded = searchBarState.currentValue == SearchBarValue.Expanded ||
        searchBarState.targetValue == SearchBarValue.Expanded
    val pageContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
    SheetHeaderColor(pageContainerColor)
    val searchBarColors = SearchBarDefaults.colors()
    val searchWindowInsets = SearchBarDefaults.windowInsets.only(WindowInsetsSides.Horizontal)
    val searchKey = textFieldState.text.toString()
    var searchResults by rememberSaveable {
        mutableStateOf(listOf<Completion>())
    }
    var isSearching by remember {
        mutableStateOf(false)
    }
    var isLoadingPage by remember {
        mutableStateOf(false)
    }
    var alert by remember { mutableStateOf<SearchAlert?>(null) }
    val scope = rememberCoroutineScope()
    var selectionJob by remember { mutableStateOf<Job?>(null) }
    var selectionQuery by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var initialFocusPending by rememberSaveable { mutableStateOf(true) }
    var inputFocusGeneration by remember { mutableIntStateOf(0) }

    suspend fun prepareSearchInput(): Boolean {
        lifecycle.withResumed {}
        return prepareForInput() && lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
    }

    fun collapseSearch() {
        initialFocusPending = false
        // Invalidate focus work that is still waiting for sheet expansion.
        inputFocusGeneration++
        focusManager.clearFocus()
        scope.launch { searchBarState.animateToCollapsed() }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        initialFocusPending = false
        inputFocusGeneration++
    }

    fun showResult(selection: Selection? = null, path: String? = null) {
        selectionJob?.cancel()
        selectionQuery = textFieldState.text.toString()
        collapseSearch()
        isLoadingPage = true
        selectionJob = scope.launch {
            val resolved = selection ?: withContext(viewModel.executor.asCoroutineDispatcher()) {
                viewModel.appCore.simulation.findObject(requireNotNull(path))
            }
            if (resolved.isEmpty) {
                alert = SearchAlert.ObjectNotFound
            } else {
                viewModel.recordSelection(resolved)
            }
            isLoadingPage = false
        }
    }

    LaunchedEffect(textFieldState) {
        snapshotFlow { textFieldState.text.toString() }.collectLatest { query ->
            if (query != selectionQuery) {
                selectionJob?.cancel()
                isLoadingPage = false
            }
            isSearching = true
            searchResults = if (query.isEmpty()) {
                emptyList()
            } else {
                withContext(viewModel.executor.asCoroutineDispatcher()) {
                    viewModel.appCore.simulation.completionForText(query, 100)
                }
            }
            isSearching = false
        }
    }

    val inputField: @Composable () -> Unit = {
        SearchBarDefaults.InputField(
            textFieldState = textFieldState,
            searchBarState = searchBarState,
            onSearch = { query ->
                if (query.isNotEmpty() && searchResults.isEmpty()) {
                    showResult(path = query)
                } else {
                    collapseSearch()
                }
            },
            placeholder = {
                Text(text = CelestiaString("Search", ""))
            },
            leadingIcon = {
                Icon(painter = painterResource(id = R.drawable.search_24px), contentDescription = null)
            },
            modifier = Modifier.focusRequester(focusRequester)
        )
    }

    LaunchedEffect(searchBarState) {
        var previousTarget: SearchBarValue? = null
        snapshotFlow { searchBarState.targetValue }.collectLatest { target ->
            val requestInitialFocus = initialFocusPending
            initialFocusPending = false
            val requestedExpansion = previousTarget == SearchBarValue.Collapsed &&
                target == SearchBarValue.Expanded
            previousTarget = target
            if (requestInitialFocus || requestedExpansion) {
                val generation = inputFocusGeneration
                if (prepareSearchInput() &&
                    generation == inputFocusGeneration &&
                    searchBarState.targetValue == target
                ) {
                    focusRequester.requestFocus()
                    if (target == SearchBarValue.Expanded) {
                        // Finish expansion after the collapsed input's coroutine is disposed.
                        searchBarState.animateToExpanded()
                    }
                }
            }
        }
    }

    BackHandler(enabled = isSearchExpanded) {
        collapseSearch()
    }

    Scaffold(containerColor = pageContainerColor, topBar = {
        // AppBarWithSearch disables the IME for its collapsed input; expand within the sheet instead.
        if (isSearchExpanded) {
            Surface(
                color = pageContainerColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(searchWindowInsets)
            ) {
                inputField()
            }
        } else {
            AppBarWithSearch(
                state = searchBarState,
                inputField = inputField,
                colors = SearchBarDefaults.appBarWithSearchColors(
                    searchBarColors = searchBarColors,
                    appBarContainerColor = pageContainerColor,
                    scrolledAppBarContainerColor = pageContainerColor
                ),
                windowInsets = searchWindowInsets,
                contentPadding = PaddingValues(
                    horizontal = dimensionResource(R.dimen.search_bar_padding_horizontal),
                    vertical = dimensionResource(R.dimen.search_bar_padding_vertical)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }, contentWindowInsets = ScaffoldDefaults.contentWindowInsetsIgnoringVisibility.only(WindowInsetsSides.Bottom)) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Retain the info page without measuring or placing it while results are visible.
            Box(modifier = Modifier.layout { measurable, constraints ->
                if (isSearchExpanded) {
                    layout(0, 0) {}
                } else {
                    val placeable = measurable.measure(constraints)
                    layout(placeable.width, placeable.height) {
                        placeable.placeRelative(0, 0)
                    }
                }
            }) {
                SearchContent(selection = viewModel.currentSelection, isLoadingPage = isLoadingPage, linkClicked = linkClicked, openSubsystem = openSubsystem, openRelatedAddons = openRelatedAddons, openSubscriptionManagement = openSubscriptionManagement, paddingValues = paddingValues)
            }
            if (isSearchExpanded) {
                Column(modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues)
                    .imePadding()) {
                    HorizontalDivider(color = searchBarColors.dividerColor)
                    SearchResult(
                        key = searchKey,
                        results = searchResults.toList(),
                        isSearching = isSearching,
                        recentSearches = viewModel.recentSearches,
                        recentSelected = {
                            textFieldState.edit { replace(0, length, "") }
                            searchResults = emptyList()
                            showResult(path = it.path)
                        }
                    ) {
                        showResult(selection = it)
                    }
                }
            }
        }
    }

    alert?.let { content ->
        when (content) {
            is SearchAlert.ObjectNotFound -> {
                SimpleAlertDialog(onDismissRequest = {
                    alert = null
                }, onConfirm = {
                    alert = null
                }, title = CelestiaString("Object not found", ""))
            }
        }
    }
}

@Composable
private fun SearchContent(selection: Selection?, isLoadingPage: Boolean, linkClicked: (String) -> Unit, openSubsystem: (Selection) -> Unit, openRelatedAddons: (String) -> Unit, openSubscriptionManagement: () -> Unit, paddingValues: PaddingValues) {
    if (isLoadingPage) {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(color = MaterialTheme.colorScheme.surfaceContainerLow), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (selection != null) {
        InfoScreen(selection = selection, showTitle = true, linkClicked = linkClicked, openSubsystem = { openSubsystem(selection) }, openRelatedAddons = openRelatedAddons, openSubscriptionManagement = openSubscriptionManagement, paddingValues = paddingValues)
    } else {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(color = MaterialTheme.colorScheme.surfaceContainerLow), contentAlignment = Alignment.Center) {
            EmptyHint(text = CelestiaString("Find stars, DSOs, and nearby objects", ""))
        }
    }
}

@Composable
private fun SearchResult(key: String, results: List<Completion>, isSearching: Boolean, recentSearches: List<RecentSearch>, recentSelected: (RecentSearch) -> Unit, selectionHandler: (Selection) -> Unit) {
    if (key.isEmpty() && recentSearches.isNotEmpty()) {
        LazyColumn(
            contentPadding = groupedListContentPadding(),
            modifier = Modifier
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            item {
                Header(
                    text = CelestiaString("Recent Searches", ""),
                    modifier = Modifier.fillMaxWidth(),
                    isFirst = true,
                )
            }
            groupedItems(recentSearches, key = { it.path }) { recent ->
                TextRow(primaryText = recent.name, modifier = Modifier.clickable(onClick = dropUnlessResumed {
                    recentSelected(recent)
                }))
            }
        }
    } else if (results.isEmpty() && isSearching) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surfaceContainerLow), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (results.isEmpty()) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surfaceContainerLow), contentAlignment = Alignment.Center) {
            EmptyHint(text = if (key.isEmpty()) CelestiaString("Find stars, DSOs, and nearby objects", "") else CelestiaString("No result found", ""))
        }
    } else {
        LazyColumn(
            contentPadding = groupedListContentPadding(),
            modifier = Modifier
                .fillMaxHeight()
                .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            groupedItems(results) {
                TextRow(primaryText = it.name, modifier = Modifier.clickable(onClick = dropUnlessResumed {
                    selectionHandler(it.selection)
                }))
            }
        }
    }
}