/*
 * SeparatorView.kt
 *
 * Copyright (C) 2001-2020, the Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.common

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import space.celestia.mobilecelestia.R

@SuppressLint("ViewConstructor")
class SeparatorView(context: Context, height: Int, left: Int, backgroundColor: Int): FrameLayout(context) {
    init {
        val density = resources.displayMetrics.density

        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, (height * density).toInt())
        setPadding((left * density).toInt(), 0, 0, 0)

        setBackgroundResource(backgroundColor)

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