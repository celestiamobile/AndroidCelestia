package space.celestia.mobilecelestia.utils

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
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

fun <T: Parcelable> Bundle.getParcelableValue(key: String, clazz: Class<T>): T? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        return getParcelable(key, clazz)
    } else {
        @Suppress("DEPRECATION")
        return getParcelable(key)
    }
}

fun <T: Parcelable> Bundle.getParcelableArrayListValue(key: String, clazz: Class<T>): ArrayList<T>? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        return getParcelableArrayList(key, clazz)
    } else {
        @Suppress("DEPRECATION")
        return getParcelableArrayList(key)
    }
}