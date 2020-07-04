/*
 * SectionFooterView.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.common

import android.content.Context
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import space.celestia.mobilecelestia.R

class SectionFooterView(context: Context): FrameLayout(context) {
    val textView: TextView = TextView(context)

    init {
        val density = resources.displayMetrics.density

        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        val paddingH = (footerHorizontalPadding * density).toInt()
        val paddingV = (footerVerticalPadding * density).toInt()
        setPadding(paddingH, paddingV, paddingH, paddingV)

        textView.setTextColor(ResourcesCompat.getColor(resources, R.color.colorSecondaryLabel, null))
        textView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        addView(textView)
    }

    private companion object {
        const val footerHorizontalPadding: Float = 16F
        const val footerVerticalPadding: Float = 8F
    }
}