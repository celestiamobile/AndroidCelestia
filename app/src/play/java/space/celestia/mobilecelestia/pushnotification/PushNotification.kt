/*
 * PushNotification.kt
 *
 * Copyright (C) 2025-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.pushnotification

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import space.celestia.celestiaui.pushnotification.PushNotificationRegistrar
import space.celestia.celestiaui.utils.AlertResult
import space.celestia.celestiaui.utils.CelestiaString
import space.celestia.celestiaui.utils.PreferenceManager
import space.celestia.celestiaui.utils.showAlertAsync
import space.celestia.mobilecelestia.MainActivity

@Suppress("UNUSED_PARAMETER")
fun MainActivity.setUpPushNotifications(
    appSettings: PreferenceManager,
    appSettingsNoBackup: PreferenceManager,
    registrar: PushNotificationRegistrar
) {
    val flow = PushNotificationFlow(this, appSettingsNoBackup, registrar)
    lifecycleScope.launch { flow.runFirstRunOrReregister() }
}

private class PushNotificationFlow(
    private val activity: MainActivity,
    private val appSettingsNoBackup: PreferenceManager,
    private val registrar: PushNotificationRegistrar
) {
    private val permissionChannel = Channel<Boolean>(Channel.RENDEZVOUS)
    // Register without a LifecycleOwner — this overload has no STARTED-state
    // constraint, so we can call it safely after the activity is started.
    private val permissionLauncher = activity.activityResultRegistry.register(
        "push-notification-permission",
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionChannel.trySend(granted)
    }

    suspend fun runFirstRunOrReregister() {
        if (hasNotificationPermission()) {
            // Permission is already granted — no need to ask. Just keep the
            // server in sync (empty contentTypes if all types disabled, server
            // clears the row).
            registrar.register()
            return
        }
        val asked = appSettingsNoBackup[PreferenceManager.PredefinedKey.PushNotificationsAsked] == "true"
        if (!asked) {
            runFirstRun()
        }
        // Permission missing and we've already asked — don't reprompt.
    }

    private fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    private suspend fun runFirstRun() {
        val title = CelestiaString("Stay Updated", "Push notification opt-in dialog title")
        val message = CelestiaString(
            "Receive notifications about featured add-ons and the latest news. You can change which kinds you receive in Settings.",
            "Push notification opt-in dialog message"
        )
        val result = activity.showAlertAsync(title, message, showCancel = true)
        appSettingsNoBackup[PreferenceManager.PredefinedKey.PushNotificationsAsked] = "true"

        if (result == AlertResult.OK) {
            ensureNotificationsPermission()
        }
        registrar.register()
    }

    private suspend fun ensureNotificationsPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED) return true
        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        return permissionChannel.receive()
    }
}
