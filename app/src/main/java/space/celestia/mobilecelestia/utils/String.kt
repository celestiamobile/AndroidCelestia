package space.celestia.mobilecelestia.utils

import space.celestia.mobilecelestia.core.CelestiaAppCore

@Suppress("FunctionName")
fun CelestiaString(key: String, @Suppress("UNUSED_PARAMETER") comment: String): String {
    return CelestiaAppCore.getLocalizedString(key)
}