package space.celestia.mobilecelestia.celestia

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.Lifecycle

internal fun Context.supportsPictureInPicture(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
        packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
}

internal fun shouldRenderForLifecycle(state: Lifecycle.State, keepRenderingWhilePaused: Boolean): Boolean {
    return state.isAtLeast(Lifecycle.State.RESUMED) ||
        (state.isAtLeast(Lifecycle.State.STARTED) && keepRenderingWhilePaused)
}

internal fun renderSurfaceSize(viewSize: IntSize, retainedSize: IntSize?, scaleFactor: Float): IntSize {
    return retainedSize ?: IntSize((viewSize.width * scaleFactor).toInt(), (viewSize.height * scaleFactor).toInt())
}
