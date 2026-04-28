/*
 * CelestiaMessagingService.kt
 *
 * Copyright (C) 2026-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.pushnotification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import space.celestia.celestiaui.di.AppSettingsNoBackup
import space.celestia.celestiaui.pushnotification.PushNotificationRegistrar
import space.celestia.celestiaui.utils.PreferenceManager
import space.celestia.mobilecelestia.MainActivity
import space.celestia.mobilecelestia.R
import javax.inject.Inject

private const val NOTIFICATION_CHANNEL_ID = "celestia_default"

@AndroidEntryPoint
class CelestiaMessagingService : FirebaseMessagingService() {
    @AppSettingsNoBackup
    @Inject
    lateinit var appSettingsNoBackup: PreferenceManager

    @Inject
    lateinit var registrar: PushNotificationRegistrar

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        appSettingsNoBackup[PreferenceManager.PredefinedKey.FCMToken] = token
        // The registrar gates on notification permission, so on Android 13+ this
        // is a no-op until the user has granted permission.
        scope.launch { registrar.register() }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        // While the app is backgrounded, FCM displays the notification itself
        // (with image attachment). When the app is foregrounded, FCM only calls
        // this method, so we post a picture-less notification ourselves so
        // the user still sees title + body and can tap through.
        val notification = message.notification ?: return
        val title = notification.title
        val body = notification.body
        if (title.isNullOrEmpty() && body.isNullOrEmpty()) return

        ensurePushNotificationChannel(this)

        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            for ((key, value) in message.data) {
                putExtra(key, value)
            }
        }
        val requestCode = message.messageId?.hashCode() ?: System.currentTimeMillis().toInt()
        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationExtras = Bundle().apply {
            message.data["article-id"]?.let { putString("article-id", it) }
            message.data["addon-id"]?.let { putString("addon-id", it) }
        }

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(getColor(R.color.notification_icon_color))
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addExtras(notificationExtras)

        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(requestCode, builder.build())
    }

}

internal fun ensurePushNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val nm = context.getSystemService<NotificationManager>() ?: return
    if (nm.getNotificationChannel(NOTIFICATION_CHANNEL_ID) != null) return
    val channel = NotificationChannel(
        NOTIFICATION_CHANNEL_ID,
        context.getString(R.string.app_name),
        NotificationManager.IMPORTANCE_HIGH
    )
    nm.createNotificationChannel(channel)
}
