// Flavor.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.sentry.android.core.SentryAndroid
import kotlinx.coroutines.tasks.await

fun CelestiaApplication.setUpFlavor() {
    SentryAndroid.init(this) { options ->
        options.dsn = "SENTRY-DSN"
        options.isDebug = BuildConfig.DEBUG
        options.isAttachScreenshot = false
        options.isAttachViewHierarchy = false
        options.addBundleId("BUNDLE_UUID")
        options.addBundleId("FLAVOR_BUNDLE_UUID")
        options.addBundleId("CELESTIA_BUNDLE_UUID")
        options.addBundleId("CELESTIA_FOUNDATION_BUNDLE_UUID")
        options.addBundleId("LINK_PREVIEW_BUNDLE_UUID")
        options.addBundleId("ZIP_UTILS_BUNDLE_UUID")
        options.proguardUuid = "PROGUARD_UUID"
    }
}

suspend fun MainActivity.registerForPushNotification(): String? {
    return try {
        return FirebaseMessaging.getInstance().token.await()
    } catch (ignored: Throwable) {
        null
    }
}

class CelestiaMessagingService: FirebaseMessagingService() {
    override fun onNewToken(token: String) {}

    override fun onMessageReceived(message: RemoteMessage) {
        val addonId = message.data["celestia.addon-id"] ?: return

        val title = message.notification?.title
        val body = message.notification?.body

        showNotification(title, body, addonId)
    }

    private fun showNotification(title: String?, body: String?, addonId: String) {
        val channelId = "default_channel_id"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Create the Channel (Required for Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Notifications", NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Create the PendingIntent
        val intent = Intent(Intent.ACTION_VIEW, Uri.Builder().scheme("celaddon").authority("item").appendQueryParameter("item", addonId).build())
        val requestCode = System.currentTimeMillis().toInt()
        val flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            flags
        )

        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(0, builder.build())
    }
}