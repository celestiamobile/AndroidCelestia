/*
 * ProgressView.kt
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
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import space.celestia.mobilecelestia.R

class ProgressView : FrameLayout {
    private var backgroundColor = 0
    private var progressColor = 0
    private var rippleColor = 0
    private var cornerRadius = 10
    private var progress = 0f

    private var rippleDrawable: RippleDrawable? = null
    private var progressLayerId: Int = 0

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val attributeArray =
            getContext().theme.obtainStyledAttributes(attrs, R.styleable.ProgressView, 0, 0)
        try {
            backgroundColor =
                attributeArray.getColor(R.styleable.ProgressView_backgroundColor, ResourcesCompat.getColor(resources, R.color.colorProgressBackground, null))
            progressColor =
                attributeArray.getColor(R.styleable.ProgressView_progressColor, ResourcesCompat.getColor(resources, R.color.colorProgressForeground, null))
            rippleColor =
                attributeArray.getColor(R.styleable.ProgressView_rippleColor, ResourcesCompat.getColor(resources, R.color.colorProgressForeground, null))
            cornerRadius =
                attributeArray.getDimensionPixelSize(R.styleable.ProgressView_cornerRadius, (resources.displayMetrics.density * 4).toInt())
        } finally {
            attributeArray.recycle()
        }
    }

    override fun setBackgroundColor(bgColor: Int) {
        backgroundColor = bgColor

        updateDrawable()
    }

    fun setProgressColor(progColor: Int) {
        progressColor = progColor

        updateDrawable()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        updateDrawable()
    }

    fun updateDrawable() {
        val isRTL = resources.configuration.layoutDirection == LAYOUT_DIRECTION_RTL

        val backgroundDrawable = GradientDrawable()
        backgroundDrawable.shape = GradientDrawable.RECTANGLE
        backgroundDrawable.cornerRadius = cornerRadius.toFloat()
        backgroundDrawable.color = ColorStateList(arrayOf(intArrayOf()), intArrayOf(backgroundColor))
        val progressDrawable = GradientDrawable()
        progressDrawable.shape = GradientDrawable.RECTANGLE
        progressDrawable.cornerRadius = cornerRadius.toFloat()
        progressDrawable.color = ColorStateList(arrayOf(intArrayOf()), intArrayOf(progressColor))
        val layerDrawable = LayerDrawable(arrayOf(backgroundDrawable, progressDrawable))

        layerDrawable.setLayerInset(0, 0, 0, 0, 0)
        layerDrawable.setLayerInset(1, if (isRTL) (width - progress * width / 100).toInt() else 0, 0, if (isRTL) 0 else (width - progress * width / 100).toInt(), 0)
        layerDrawable.setBounds(0, 0, width, height)

        if (rippleDrawable == null) {
            val ripple = RippleDrawable(ColorStateList(arrayOf(intArrayOf()), intArrayOf(rippleColor)), layerDrawable, null)
            rippleDrawable = ripple
            for (i in 0 until ripple.numberOfLayers) {
                if (ripple.getDrawable(i) == layerDrawable) {
                    progressLayerId = ripple.getId(i)
                    break
                }
            }
            background = rippleDrawable
        } else {
            rippleDrawable?.setDrawableByLayerId(progressLayerId, layerDrawable)
        }
        invalidate()
    }

    fun setProgress(currentProgress: Float) {
        progress = currentProgress
        updateDrawable()
    }

    fun reset() {
        progress = 0f
        updateDrawable()
    }
}