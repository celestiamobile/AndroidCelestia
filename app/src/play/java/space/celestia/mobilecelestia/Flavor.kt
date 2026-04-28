// Flavor.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia

import androidx.lifecycle.lifecycleScope
import io.sentry.Attachment
import io.sentry.EventProcessor
import io.sentry.Hint
import io.sentry.SentryEvent
import io.sentry.android.core.SentryAndroid
import kotlinx.coroutines.launch
import space.celestia.celestiaui.utils.PreferenceManager
import space.celestia.mobilecelestia.pushnotification.ensurePushNotificationChannel
import java.io.File

private var reportParentFolder: File? = null
private const val installedAddonListFileName = "installed-addons.txt"
private const val featureFlagsFileName = "feature-flags.txt"

private fun proguardSeedPROGUARD_METHOD_SEED() { }

fun CelestiaApplication.setUpFlavor() {
    // Create the push notification channel as early as possible so background
    // FCM-displayed notifications (which Android won't route through our service)
    // land in our channel — at IMPORTANCE_HIGH — instead of FCM's fallback.
    ensurePushNotificationChannel(this)

    SentryAndroid.init(this) { options ->
        options.dsn = "SENTRY-DSN"
        options.isDebug = BuildConfig.DEBUG
        options.isAttachScreenshot = false
        options.isAttachViewHierarchy = false
        options.addBundleId("BUNDLE_UUID")
        options.addBundleId("FLAVOR_BUNDLE_UUID")
        options.addBundleId("CELESTIA_BUNDLE_UUID")
        options.addBundleId("CELESTIA_FLAVOR_BUNDLE_UUID")
        options.addBundleId("CELESTIA_FOUNDATION_BUNDLE_UUID")
        options.addBundleId("CELESTIA_UI_BUNDLE_UUID")
        options.addBundleId("CELESTIA_UI_FLAVOR_BUNDLE_UUID")
        options.addBundleId("LINK_PREVIEW_BUNDLE_UUID")
        options.addBundleId("ZIP_UTILS_BUNDLE_UUID")
        options.proguardUuid = "PROGUARD_UUID"
        options.maxAttachmentSize = 5 * 1024 * 1024 // 5 MiB

        var reportFolder: File?
        try {
            val sentryFolder = File(noBackupFilesDir, "sentry")
            reportFolder = if (sentryFolder.exists()) {
                if (!sentryFolder.isDirectory) {
                    null
                } else {
                    sentryFolder
                }
            } else if (!sentryFolder.mkdir()) {
                null
            } else {
                sentryFolder
            }
        } catch (ignored: Throwable) {
            reportFolder = null
        }

        if (reportFolder != null) {
            options.eventProcessors.add(object : EventProcessor {
                override fun process(event: SentryEvent, hint: Hint): SentryEvent {
                    if (event.isCrashed) {
                        for (fileNames in listOf(installedAddonListFileName, featureFlagsFileName)) {
                            val file = File(reportFolder, fileNames)
                            if (file.exists()) {
                                hint.addAttachment(Attachment(file.absolutePath))
                            }
                        }
                    }
                    return event
                }
            })
        }
        reportParentFolder = reportFolder
    }
}

fun MainActivity.initialSetUpComplete() = lifecycleScope.launch {
    val reportFolder = reportParentFolder ?: return@launch
    val installedAddonsText = resourceManager.installedResourcesAsync().joinToString(separator = "\n") { "${it.name}/${it.id}" }
    writeTextToFileWithName(installedAddonsText, reportFolder, installedAddonListFileName)
    writeTextToFileWithName(appSettingsNoBackup[PreferenceManager.PredefinedKey.FeatureFlags] ?: "", reportFolder, featureFlagsFileName)
}