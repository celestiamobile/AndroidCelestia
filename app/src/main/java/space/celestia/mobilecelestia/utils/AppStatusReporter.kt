// AppStatusReporter.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.utils

import space.celestia.celestia.AppCore
import space.celestia.celestia.AppCore.ProgressWatcher

class AppStatusReporter : ProgressWatcher {
    private val listeners = arrayListOf<Listener>()
    private var _status = ""
    private var _state = State.NONE
    private val lock = Any()

    enum class State(val value: Int) {
        EXTERNAL_LOADING_FAILURE(-2), LOADING_FAILURE(-1), NONE(0), EXTERNAL_LOADING(1), LOADING(2), LOADING_SUCCESS(
            3
        ),
        FINISHED(4);

    }

    val state: State
        get() = synchronized(lock) { return _state }

    val status: String
        get() = synchronized(lock) { return _status }

    interface Listener {
        fun celestiaLoadingProgress(status: String)
        fun celestiaLoadingStateChanged(newState: State)
    }

    fun register(listener: Listener) {
        synchronized(lock) { if (!listeners.contains(listener)) listeners.add(listener) }
    }

    fun unregister(listener: Listener) {
        synchronized(lock) { listeners.remove(listener) }
    }

    override fun onCelestiaProgress(progress: String) {
        updateStatus(String.format(CelestiaString("Loading: %s", "Celestia initialization, loading file"), progress))
    }

    fun updateState(state: State) {
        var currentListeners: ArrayList<Listener>
        synchronized(lock) {
            _state = state
            currentListeners = ArrayList(listeners.size)
            currentListeners.addAll(listeners)
        }
        for (listener in currentListeners) {
            listener.celestiaLoadingStateChanged(state)
        }
    }

    fun updateStatus(status: String) {
        var currentListeners: ArrayList<Listener>
        synchronized(lock) {
            _status = status
            currentListeners = ArrayList(listeners.size)
            currentListeners.addAll(listeners)
        }
        for (listener in currentListeners) {
            listener.celestiaLoadingProgress(status)
        }
    }
}