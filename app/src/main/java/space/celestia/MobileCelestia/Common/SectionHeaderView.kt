package space.celestia.MobileCelestia.Common

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import space.celestia.MobileCelestia.R

class SectionHeaderView(context: Context): FrameLayout(context) {
    val textView: TextView = TextView(context)

    init {
        val density = resources.displayMetrics.density

        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, (headerHeight * density).toInt())
        val padding = (headerHorizontalPadding * density).toInt()
        setPadding(padding, 0, padding, 0)

        val view = View(context)
        view.setBackgroundColor(resources.getColor(R.color.colorSeparator))

        textView.setTextColor(resources.getColor(R.color.colorSecondaryLabel))
        textView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL)
        addView(textView)
    }

    private companion object {
        const val headerHeight: Float = 36F
        const val headerHorizontalPadding: Float = 16F
    }
}