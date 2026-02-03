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
import java.io.File

private var reportParentFolder: File? = null
private const val installedAddonListFileName = "installed-addons.txt"

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
        options.maxAttachmentSize = 5 * 1024 * 1024; // 5 MiB

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
                        for (fileNames in listOf(installedAddonListFileName)) {
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
}