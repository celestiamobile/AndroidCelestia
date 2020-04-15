/*
 * SteppeView.kt
 *
 * Copyright (C) 2001-2020, the Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.common

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.LinearLayout
import space.celestia.mobilecelestia.R

class StepperView(context: Context, attrs: AttributeSet): LinearLayout(context, attrs) {
    private val leftView by lazy { findViewById<View>(R.id.stepper_left) }
    private val rightView by lazy { findViewById<View>(R.id.stepper_right) }

    var listener: Listener? = null

    private val subViewListener: OnTouchListener = OnTouchListener { view, event ->
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                listener?.stepperTouchDown(this, view == leftView)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                listener?.stepperTouchUp(this, view == leftView)
            }
        }
        view.onTouchEvent(event)
    }

    init {
        View.inflate(context, R.layout.stepper_view, this)

        leftView.setOnTouchListener(subViewListener)
        rightView.setOnTouchListener(subViewListener)
    }

    interface Listener {
        fun stepperTouchDown(view: StepperView, left: Boolean)
        fun stepperTouchUp(view: StepperView, left: Boolean)
    }
}