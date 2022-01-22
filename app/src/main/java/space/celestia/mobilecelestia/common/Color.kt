package space.celestia.mobilecelestia.common

import android.content.Context
import android.os.Build
import android.util.TypedValue
import androidx.core.content.res.ResourcesCompat
import space.celestia.mobilecelestia.R

fun Context.getSecondaryColor(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
        val value = TypedValue()
        theme.resolveAttribute(android.R.attr.colorSecondary, value, true)
        value.data
    } else {
        ResourcesCompat.getColor(resources, R.color.colorPrimaryLight, null)
    }
}