// PageTopAppBar.kt
//
// Copyright (C) 2026, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.celestiaui.compose

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberPageTopAppBarScrollBehavior(
    backStack: List<*>,
    scopeKey: Int = 0,
): TopAppBarScrollBehavior {
    val states = rememberSaveable(saver = PageTopAppBarStates.Saver) { PageTopAppBarStates() }
    return TopAppBarDefaults.pinnedScrollBehavior(states.stateFor(backStack, scopeKey))
}

internal class PageTopAppBarStates {
    private data class Slot(val scope: Int, val index: Int)
    private data class Entry(val page: Any?, val state: TopAppBarState)

    private val states = mutableMapOf<Slot, Entry>()
    private val restoredStates = mutableMapOf<Slot, TopAppBarState>()

    fun stateFor(backStack: List<*>, scopeKey: Int = 0): TopAppBarState {
        require(backStack.isNotEmpty())
        states.keys.removeAll { slot ->
            slot.scope == scopeKey &&
                (slot.index >= backStack.size || states.getValue(slot).page != backStack[slot.index])
        }
        restoredStates.keys.removeAll { it.scope == scopeKey && it.index >= backStack.size }
        val slot = Slot(scopeKey, backStack.lastIndex)
        return states.getOrPut(slot) {
            Entry(
                backStack.last(),
                restoredStates.remove(slot) ?: TopAppBarState(-Float.MAX_VALUE, 0f, 0f)
            )
        }.state
    }

    fun save(): List<Any> {
        val allStates = restoredStates + states.mapValues { it.value.state }
        return allStates.flatMap { (slot, state) ->
            listOf(slot.scope, slot.index, state.heightOffsetLimit, state.heightOffset, state.contentOffset)
        }
    }

    companion object {
        val Saver = listSaver<PageTopAppBarStates, Any>(
            save = { it.save() },
            restore = { restore(it) }
        )

        fun restore(saved: List<Any>): PageTopAppBarStates {
            require(saved.size % 5 == 0)
            return PageTopAppBarStates().apply {
                // Routes need not be Bundle-saveable; reconnect saved slots to the restored stack.
                for (entry in saved.chunked(5)) {
                    restoredStates[Slot(entry[0] as Int, entry[1] as Int)] = TopAppBarState(
                        entry[2] as Float,
                        entry[3] as Float,
                        entry[4] as Float
                    )
                }
            }
        }
    }
}
