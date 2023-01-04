/*
 * StepperView.kt
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
import android.util.AttributeSet
import android.util.LayoutDirection
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.LinearLayout
import space.celestia.mobilecelestia.R
import java.lang.ref.WeakReference

class StepperView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): LinearLayout(context, attrs, defStyleAttr) {
    private val startView by lazy { findViewById<View>(R.id.stepper_start) }
    private val endView by lazy { findViewById<View>(R.id.stepper_end) }

    var listener: Listener? = null

    init {
        View.inflate(context, R.layout.stepper_view, this)

        val weakSelf = WeakReference(this)
        @SuppressLint("ClickableViewAccessibility")
        val subViewListener = OnTouchListener { view, event ->
            val self = weakSelf.get() ?: return@OnTouchListener view.onTouchEvent(event)
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    self.listener?.stepperTouchDown(self, view == self.startView)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    self.listener?.stepperTouchUp(self, view == self.startView)
                }
            }
            view.onTouchEvent(event)
        }

        val isRTL = resources.configuration.layoutDirection == LayoutDirection.RTL
        startView.setBackgroundResource(if (isRTL) R.drawable.stepper_right else R.drawable.stepper_left)
        endView.setBackgroundResource(if (isRTL) R.drawable.stepper_left else R.drawable.stepper_right)
        startView.setOnTouchListener(subViewListener)
        endView.setOnTouchListener(subViewListener)
    }

    interface Listener {
        fun stepperTouchDown(view: StepperView, left: Boolean)
        fun stepperTouchUp(view: StepperView, left: Boolean)
    }
}