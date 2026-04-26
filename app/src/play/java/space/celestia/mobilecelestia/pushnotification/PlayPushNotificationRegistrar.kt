/*
 * PlayPushNotificationRegistrar.kt
 *
 * Copyright (C) 2026-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.pushnotification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.tasks.await
import space.celestia.celestia.AppCore
import space.celestia.celestiaui.di.AppSettings
import space.celestia.celestiaui.di.AppSettingsNoBackup
import space.celestia.celestiaui.di.Platform
import space.celestia.celestiaui.pushnotification.PushNotificationContentType
import space.celestia.celestiaui.pushnotification.PushNotificationRegistrar
import space.celestia.celestiaui.pushnotification.RegisterRequest
import space.celestia.celestiaui.pushnotification.UserAPIService
import space.celestia.celestiaui.utils.PreferenceManager
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayPushNotificationRegistrar @Inject constructor(
    @param: ApplicationContext private val context: Context,
    @param: AppSettings private val appSettings: PreferenceManager,
    @param: AppSettingsNoBackup private val appSettingsNoBackup: PreferenceManager,
    private val userAPI: UserAPIService,
    private val platform: Platform
) : PushNotificationRegistrar {
    override suspend fun register() {
        if (!hasNotificationPermission()) return
        val token = try {
            FirebaseMessaging.getInstance().token.await()
        } catch (_: Throwable) {
            return
        }
        appSettingsNoBackup[PreferenceManager.PredefinedKey.FCMToken] = token
        try {
            userAPI.register(buildRegisterRequest(token, appSettings, platform))
        } catch (_: Throwable) {}
    }

    private fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }
}

@Module
@InstallIn(SingletonComponent::class)
interface PushNotificationRegistrarModule {
    @Binds
    fun bindPushNotificationRegistrar(impl: PlayPushNotificationRegistrar): PushNotificationRegistrar
}

internal const val PUSH_API_VERSION = 1

internal fun PreferenceManager.isPushTypeEnabled(key: PreferenceManager.PredefinedKey): Boolean {
    // Defaults to true so newly-added types are enabled-by-default for opted-in users.
    return this[key] != "false"
}

internal fun PreferenceManager.enabledPushContentTypes(): List<String> {
    val list = mutableListOf<String>()
    if (isPushTypeEnabled(PreferenceManager.PredefinedKey.PushWeeklyAddon))
        list.add(PushNotificationContentType.WEEKLY_ADDON.rawValue)
    if (isPushTypeEnabled(PreferenceManager.PredefinedKey.PushLatestNews))
        list.add(PushNotificationContentType.LATEST_NEWS.rawValue)
    if (isPushTypeEnabled(PreferenceManager.PredefinedKey.PushFeaturedAddon))
        list.add(PushNotificationContentType.FEATURED_ADDON.rawValue)
    return list
}

private fun buildRegisterRequest(token: String, appSettings: PreferenceManager, platform: Platform): RegisterRequest = RegisterRequest(
    token = token,
    tokenType = "fcm",
    lang = AppCore.getLanguage(),
    timezone = TimeZone.getDefault().id,
    contentTypes = appSettings.enabledPushContentTypes(),
    api = PUSH_API_VERSION,
    platform = platform.name,
    distribution = platform.flavor,
    lastShownNewsID = appSettings[PreferenceManager.PredefinedKey.LastNewsID]
)
