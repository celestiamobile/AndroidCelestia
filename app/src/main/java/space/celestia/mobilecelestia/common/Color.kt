package space.celestia.mobilecelestia.common

import android.content.Context
import android.util.TypedValue
import space.celestia.mobilecelestia.R

fun Context.getPrimaryColor(): Int {
    val value = TypedValue()
    theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, value, true)
    return value.data
}

fun Context.getSecondaryColor(): Int {
    val value = TypedValue()
    theme.resolveAttribute(com.google.android.material.R.attr.colorSecondary, value, true)
    return value.data
}