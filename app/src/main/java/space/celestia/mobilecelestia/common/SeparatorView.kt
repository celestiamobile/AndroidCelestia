/*
 * SeparatorView.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.common

import android.annotation.SuppressLint
import android.content.Context
import android.util.LayoutDirection
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import space.celestia.mobilecelestia.R
import kotlin.math.max

@SuppressLint("ViewConstructor")
class SeparatorView(context: Context, height: Float, private var inset: Float, backgroundColor: Int): FrameLayout(context) {
    init {
        val density = resources.displayMetrics.density

        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, (height * density).toInt())
        if (resources.configuration.layoutDirection == LayoutDirection.RTL) {
            setPadding(0, 0, (inset * density).toInt(), 0)
        } else {
            setPadding((inset * density).toInt(), 0, 0, 0)
        }

        setBackgroundResource(backgroundColor)

        val view = View(context)
        view.setBackgroundResource(R.color.colorSeparator)

        val sepHeight = max((separatorHeight * density).toInt(), 1)
        view.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, sepHeight, Gravity.CENTER)

        addView(view)
    }

    var separatorInset: Float
        get() = inset
        set(value) {
            inset = value
            val density = resources.displayMetrics.density
            if (resources.configuration.layoutDirection == LayoutDirection.RTL) {
                setPadding(0, 0, (value * density).toInt(), 0)
            } else {
                setPadding((value * density).toInt(), 0, 0, 0)
            }
        }

    private companion object {
        const val separatorHeight: Float = 0.5F
    }
}