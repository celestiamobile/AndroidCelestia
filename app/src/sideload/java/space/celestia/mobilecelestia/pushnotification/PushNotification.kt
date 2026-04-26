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

import space.celestia.celestiaui.pushnotification.PushNotificationRegistrar
import space.celestia.celestiaui.utils.PreferenceManager
import space.celestia.mobilecelestia.MainActivity

const val pushNotificationsAvailable = false

@Suppress("UNUSED_PARAMETER")
fun MainActivity.setUpPushNotifications(
    appSettings: PreferenceManager,
    appSettingsNoBackup: PreferenceManager,
    registrar: PushNotificationRegistrar
) {
    // Push notifications are only available in the play flavor.
}
