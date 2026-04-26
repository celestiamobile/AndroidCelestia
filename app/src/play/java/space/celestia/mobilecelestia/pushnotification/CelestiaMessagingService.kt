/*
 * CelestiaMessagingService.kt
 *
 * Copyright (C) 2025-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.pushnotification

import com.google.firebase.messaging.FirebaseMessagingService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import space.celestia.celestiaui.di.AppSettings
import space.celestia.celestiaui.pushnotification.PushNotificationRegistrar
import space.celestia.celestiaui.utils.PreferenceManager
import javax.inject.Inject

@AndroidEntryPoint
class CelestiaMessagingService : FirebaseMessagingService() {
    @AppSettings
    @Inject
    lateinit var appSettings: PreferenceManager

    @Inject
    lateinit var registrar: PushNotificationRegistrar

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        appSettings[PreferenceManager.PredefinedKey.FCMToken] = token
        if (appSettings[PreferenceManager.PredefinedKey.PushNotificationsAsked] != "true") return
        scope.launch { registrar.register() }
    }

    // onMessageReceived: when the FCM payload contains a `notification` field (which our
    // server sends), the system displays the notification automatically while the app is
    // backgrounded. No custom handling needed for foreground; can be added later.
}
