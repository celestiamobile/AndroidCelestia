// SearchViewModel.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.celestiaui.search.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import space.celestia.celestia.AppCore
import space.celestia.celestia.Selection
import space.celestia.celestiaui.di.AppSettingsNoBackup
import space.celestia.celestiaui.utils.PreferenceManager
import java.util.concurrent.Executor
import javax.inject.Inject

data class RecentSearch(val path: String, val name: String)

@HiltViewModel
class SearchViewModel @Inject constructor(
    val appCore: AppCore,
    val executor: Executor,
    @param:AppSettingsNoBackup private val preferences: PreferenceManager,
) : ViewModel() {
    var recentSearches by mutableStateOf(emptyList<RecentSearch>())
        private set
    private var historyGeneration = 0
    private var recentSearchJob: Job? = null

    init {
        refreshRecentSearches()
    }

    private fun refreshRecentSearches() {
        recentSearchJob?.cancel()
        val paths = readRecentSearches()
        recentSearchJob = viewModelScope.launch {
            recentSearches = withContext(executor.asCoroutineDispatcher()) {
                paths.mapNotNull { path ->
                    val selection = appCore.simulation.findObject(path)
                    if (selection.isEmpty) null else RecentSearch(
                        path = path,
                        name = appCore.simulation.universe.getNameForSelection(selection),
                    )
                }
            }
        }
    }

    private fun readRecentSearches(): List<String> {
        val stored = preferences[PreferenceManager.PredefinedKey.RecentSearches] ?: return emptyList()
        return try {
            Json.decodeFromString<List<String>>(stored).filter { it.isNotEmpty() }.distinct().take(10)
        } catch (error: SerializationException) {
            Log.e("SearchViewModel", "Failed to read recent searches", error)
            emptyList()
        }
    }

    suspend fun recordSelection(selection: Selection) {
        if (selection.isEmpty) return
        val generation = historyGeneration
        val path = withContext(executor.asCoroutineDispatcher()) {
            appCore.simulation.universe.getPathForSelection(selection)
        }
        if (generation != historyGeneration) return
        check(path.isNotEmpty()) { "Selected object has no search path" }
        val updated = (listOf(path) + readRecentSearches().filter { it != path }).take(10)
        preferences[PreferenceManager.PredefinedKey.RecentSearches] = Json.encodeToString(updated)
        refreshRecentSearches()
    }

    fun clearHistory() {
        historyGeneration++
        recentSearchJob?.cancel()
        preferences[PreferenceManager.PredefinedKey.RecentSearches] = null
        recentSearches = emptyList()
    }
}