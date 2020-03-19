package space.celestia.MobileCelestia.Common

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import space.celestia.MobileCelestia.R

class SeparatorView(context: Context, height: Int, left: Int): FrameLayout(context) {
    init {
        val density = resources.displayMetrics.density

        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, (height * density).toInt())
        setPadding((left * density).toInt(), 0, 0, 0)

        val view = View(context)
        view.setBackgroundColor(resources.getColor(R.color.colorSeparator))

        val height = (separatorHeight * density).toInt()
        view.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, height, Gravity.CENTER)

        addView(view)
    }

    private companion object {
        const val separatorHeight: Float = 1F
    }
}