/*
 * Flavor.kt
 *
 * Copyright (C) 2024-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia

import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import space.celestia.mobilecelestia.utils.CrashHandler
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun MainActivity.setUpFlavor() {
    if (!AppCenter.isConfigured()) {
        AppCenter.start(
            application, "APPCENTER-APP-ID",
            Analytics::class.java, Crashes::class.java
        )

        Crashes.getMinidumpDirectory().thenAccept { path ->
            if (path != null) {
                CrashHandler.setupNativeCrashesListener(path)
            }
        }
    }
}

suspend fun MainActivity.getLastCrashReportId(): String? = suspendCoroutine { cont ->
    Crashes.getLastSessionCrashReport().thenAccept { errorReport ->
        cont.resume(errorReport?.id)
    }
}