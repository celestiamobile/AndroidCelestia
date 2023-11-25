/*
 * SearchFragment.kt
 *
 * Copyright (C) 2023-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import space.celestia.celestia.AppCore
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.CelestiaExecutor
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.compose.EmptyHint
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.compose.TextRow
import space.celestia.mobilecelestia.utils.CelestiaString
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : NavigationFragment.SubFragment() {
    private var listener: Listener? = null

    @Inject
    lateinit var appCore: AppCore

    @Inject
    lateinit var executor: CelestiaExecutor


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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement SearchFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainScreen() {
        var searchKey by rememberSaveable {
            mutableStateOf("")
        }
        var searchResults by rememberSaveable {
            mutableStateOf(listOf<String>())
        }
        var isSearchActive by rememberSaveable {
            mutableStateOf(false)
        }
        var isSeaching by remember {
            mutableStateOf(false)
        }
        val scope = rememberCoroutineScope()
        Scaffold(topBar = {
            SearchBar(query = searchKey, onQueryChange = {
                searchKey = it
                isSeaching = true
                scope.launch {
                    val result = if (it.isEmpty()) listOf<String>() else withContext(executor.asCoroutineDispatcher()) { appCore.simulation.completionForText(it, SEARCH_RESULT_LIMIT) }
                    if (searchKey == it) {
                        searchResults = result
                        isSeaching = false
                    }
                }
            }, onSearch = {
                if (searchKey.isNotEmpty() && searchResults.isEmpty()) {
                    listener?.onSearchItemSubmit(searchKey)
                }
            }, active = isSearchActive, onActiveChange = {
                isSearchActive = it
            }, leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null)
            }, placeholder = {
                Text(text = CelestiaString("Search", ""))
            }, modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (!isSearchActive) dimensionResource(
                        id = R.dimen.search_bar_padding_horizontal
                    ) else 0.dp,
                    vertical = if (!isSearchActive) dimensionResource(
                        id = R.dimen.search_bar_padding_vertical
                    ) else 0.dp
                )) {
                SearchResult(key = searchKey, results = searchResults.toList(), isSeaching = isSeaching)
            }
        }) {
            Empty(paddingValues = it)
        }
    }

    @Composable
    private fun Empty(paddingValues: PaddingValues) {}

    @Composable
    private fun SearchResult(key: String, results: List<String>, isSeaching: Boolean) {
        if (results.isEmpty() && isSeaching) {
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
                    TextRow(primaryText = it, modifier = Modifier.clickable {
                        val lastSeparator = key.lastIndexOf('/')
                        val name = if (lastSeparator != -1) {
                            key.substring(startIndex = 0, endIndex = lastSeparator + 1) + it
                        } else {
                            it
                        }
                        listener?.onSearchItemSelected(name)
                    })
                }
            }
        }
    }

    interface Listener {
        fun onSearchItemSelected(text: String)
        fun onSearchItemSubmit(text: String)
    }

    companion object {
        private const val SEARCH_RESULT_LIMIT = 100

        @JvmStatic
        fun newInstance() = SearchFragment()
    }
}
