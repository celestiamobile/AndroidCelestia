// Flavor.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia

import io.sentry.android.core.SentryAndroid

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