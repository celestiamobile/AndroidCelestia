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
import java.util.UUID

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

        EnablePushNotifications,
        EnablePushNotificationsAddonCreation,
        EnablePushNotificationsAddonModification,
        PushNotificationToken,
        HasAskedForNotificationPermission
        ;

        override val valueString: String
            get() = toString()
    }

    class CustomKey(val key: String) : Key {
        override val valueString: String
            get() = key
    }

    open class ChangeListener(val key: Key) {
        internal val id = UUID.randomUUID()
        open fun preferenceChanged(newValue: String?) {}
    }

    private val changeListeners = hashMapOf<UUID, SharedPreferences.OnSharedPreferenceChangeListener>()

    operator fun get(key: Key): String? {
        return sp.getString(key.valueString, null)
    }

    fun registerOnPreferenceChangeListener(changeListener: ChangeListener) {
        if (changeListeners.get(changeListener.id) != null)
            return
        val actualListener: SharedPreferences.OnSharedPreferenceChangeListener = { sp, key ->
            if (changeListener.key.valueString == key) {
                changeListener.preferenceChanged(sp.getString(key, null))
            }
        }
        sp.registerOnSharedPreferenceChangeListener(actualListener)
        changeListeners[changeListener.id] = actualListener
    }

    fun unregisterOnPreferenceChangeListener(changeListener: ChangeListener) {
        val actualListener = changeListeners[changeListener.id] ?: return
        sp.unregisterOnSharedPreferenceChangeListener(actualListener)
        changeListeners.remove(changeListener.id)
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