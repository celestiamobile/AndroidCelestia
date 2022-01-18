package space.celestia.mobilecelestia.common

import android.os.Build
import android.view.RoundedCorner
import androidx.annotation.RequiresApi
import androidx.core.view.WindowInsetsCompat

class RoundedCorners(val topLeft: Int, val topRight: Int, val bottomLeft: Int, val bottomRight: Int) {
    @RequiresApi(Build.VERSION_CODES.S)
    constructor(insets: WindowInsetsCompat?) :this(
        insets?.toWindowInsets()?.getRoundedCorner(RoundedCorner.POSITION_TOP_LEFT)?.radius ?: 0,
        insets?.toWindowInsets()?.getRoundedCorner(RoundedCorner.POSITION_TOP_RIGHT)?.radius ?: 0,
        insets?.toWindowInsets()?.getRoundedCorner(RoundedCorner.POSITION_BOTTOM_LEFT)?.radius ?: 0,
        insets?.toWindowInsets()?.getRoundedCorner(RoundedCorner.POSITION_BOTTOM_RIGHT)?.radius ?: 0
    )
}