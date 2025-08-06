// StandardImageButton.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.common

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

class StandardImageButton: androidx.appcompat.widget.AppCompatImageButton {

    constructor(context: Context, attrSet: AttributeSet) : super(context, attrSet) {
        background = null
    }

    constructor(context: Context) : super(context) {
        background = null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    alpha = 0.38f
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    alpha = 1f
                }
            }
        }
        return super.onTouchEvent(event)
    }
}