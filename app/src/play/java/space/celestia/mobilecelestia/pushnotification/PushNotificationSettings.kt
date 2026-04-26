/*
 * PushNotificationSettings.kt
 *
 * Copyright (C) 2026-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.pushnotification

import space.celestia.celestiaui.common.CommonSectionV2
import space.celestia.celestiaui.settings.viewmodel.SettingsItem
import space.celestia.celestiaui.settings.viewmodel.SettingsPushNotificationsItem
import space.celestia.celestiaui.utils.CelestiaString

fun pushNotificationSettingsSection(): CommonSectionV2<SettingsItem>? {
    return CommonSectionV2(
        listOf(SettingsPushNotificationsItem()),
        CelestiaString("Notifications", "Push notification settings section header")
    )
}
