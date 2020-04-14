/*
 * PreferenceManager.kt
 *
 * Copyright (C) 2001-2020, the Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context, private val name: String) {
    private val sp: SharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)
    private var et: SharedPreferences.Editor? = null

    interface Key {
        val valueString: String
    }

    enum class PredefinedKey : Key {
        DataVersion,
        OnboardMessage;

        override val valueString: String
            get() = toString()
    }

    class CustomKey(val key: String) : Key {
        override val valueString: String
            get() = key
    }

    operator fun get(key: Key): String? {
        return sp.getString(key.valueString, null)
    }

    @SuppressLint("CommitPrefEdits")
    fun startEditing() {
        if (et != null)
            throw RuntimeException("Editing context already exists when calling start.")
        et = sp.edit()
    }

    fun stopEditing() {
        if (et == null)
            throw RuntimeException("No current editing context when calling stop.")
        et!!.apply()
    }

    @SuppressLint("CommitPrefEdits")
    operator fun set(key: Key, value: String?) {
        val needEditor = et == null

        val editor = if (needEditor) sp.edit() else et
        editor?.putString(key.valueString, value)

        if (needEditor) { editor?.apply() }
    }
}