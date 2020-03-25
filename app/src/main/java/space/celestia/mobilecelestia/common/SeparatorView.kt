package space.celestia.mobilecelestia.common

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import space.celestia.mobilecelestia.R

@SuppressLint("ViewConstructor")
class SeparatorView(context: Context, height: Int, left: Int): FrameLayout(context) {
    init {
        val density = resources.displayMetrics.density

        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, (height * density).toInt())
        setPadding((left * density).toInt(), 0, 0, 0)

        val view = View(context)
        view.setBackgroundResource(R.color.colorSeparator)

        val sepHeight = (separatorHeight * density).toInt()
        view.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, sepHeight, Gravity.CENTER)

        addView(view)
    }

    private companion object {
        const val separatorHeight: Float = 1F
    }
}