package space.celestia.mobilecelestia.utils

import space.celestia.mobilecelestia.core.CelestiaAppCore

fun CelestiaString(key: String, comment: String): String {
    return CelestiaAppCore.getLocalizedString(key)
}