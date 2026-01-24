// PreferenceManager.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context, name: String) {
    private val sp: SharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)
    private var et: SharedPreferences.Editor? = null

    interface Key {
        val valueString: String
    }

    enum class PredefinedKey : Key {
        DataVersion,
        OnboardMessage,
        DataDirPath,
        ConfigFilePath,
        FullDPI,
        MSAA,
        FrameRateOption,
        Language,
        PrivacyPolicyAccepted,
        LastNewsID,
        ControllerRemapA,
        ControllerRemapB,
        ControllerRemapX,
        ControllerRemapY,
        ControllerRemapLT,
        ControllerRemapLB,
        ControllerRemapRT,
        ControllerRemapRB,
        ControllerRemapDpadLeft,
        ControllerRemapDpadUp,
        ControllerRemapDpadRight,
        ControllerRemapDpadDown,
        ControllerInvertX,
        ControllerInvertY,
        ControllerEnableLeftThumbstick,
        ControllerEnableRightThumbstick,
        ContextMenu,
        PickSensitivity,
        NormalFontPath,
        NormalFontIndex,
        BoldFontPath,
        BoldFontIndex,
        ToolbarItems,
        MigrationSourceDirectory,
        MigrationTargetDirectory,
        UseMediaDirForAddons,
        DetectVirtualDisplay
        ;

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

    fun stopEditing(writeImmediatelly: Boolean = false) {
        val editor = et ?: throw RuntimeException("No current editing context when calling stop.")
        if (writeImmediatelly) {
            editor.commit()
        } else {
            editor.apply()
        }
    }

    @SuppressLint("CommitPrefEdits")
    operator fun set(key: Key, value: String?) {
        val needEditor = et == null

        val editor = if (needEditor) sp.edit() else et
        editor?.putString(key.valueString, value)

        if (needEditor) { editor?.apply() }
    }
}