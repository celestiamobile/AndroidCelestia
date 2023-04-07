package space.celestia.mobilecelestia.utils

import android.os.Build
import android.os.Bundle
import java.io.Serializable

fun <T: Serializable> Bundle.getSerializableValue(key: String, clazz: Class<T>): T? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        return getSerializable(key, clazz)
    } else {
        @Suppress("DEPRECATION")
        val value = getSerializable(key) ?: return null
        @Suppress("UNCHECKED_CAST")
        return value as? T
    }
}