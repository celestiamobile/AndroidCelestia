/*
 * ProgressButton.kt
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
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import space.celestia.mobilecelestia.R

class ProgressButton : View {
    private var textPaint: Paint
    private var backgroundPaint: Paint
    private var progressPaint: Paint
    private var text: String = ""
    private var textSize = 0
    private var textColor = 0
    private var backgroundColor = 0
    private var progressColor = 0
    private val cornerRadius = 10f
    private val topX = 0f
    private val topY = 0f
    private var progress = 0f

    constructor(context: Context?) : this(context, null)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        textSize = resources.getDimensionPixelSize(R.dimen.progress_button_text_size_default)
        val attributeArray =
            getContext().theme.obtainStyledAttributes(attrs, R.styleable.ProgressButton, 0, 0)
        try {
            backgroundColor =
                attributeArray.getColor(R.styleable.ProgressButton_backgroundColor, ResourcesCompat.getColor(resources, R.color.colorProgressBackground, null))
            progressColor =
                attributeArray.getColor(R.styleable.ProgressButton_progressColor, ResourcesCompat.getColor(resources, R.color.colorProgressForeground, null))
            textColor =
                attributeArray.getColor(R.styleable.ProgressButton_textColor, Color.WHITE)
            textSize = attributeArray.getDimensionPixelSize(
                R.styleable.ProgressButton_textSize,
                textSize
            )
            text = attributeArray.getString(R.styleable.ProgressButton_text) ?: ""
        } finally {
            attributeArray.recycle()
        }
        backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        backgroundPaint.color = backgroundColor
        backgroundPaint.style = Paint.Style.FILL_AND_STROKE
        progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        progressPaint.color = progressColor
        progressPaint.style = Paint.Style.FILL_AND_STROKE
        textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint.color = textColor
        textPaint.textSize = textSize.toFloat()
        textPaint.style = Paint.Style.FILL_AND_STROKE
    }

    override fun setBackgroundColor(bgColor: Int) {
        backgroundColor = bgColor
        backgroundPaint.color = bgColor
        invalidate()
    }

    fun setProgressColor(progColor: Int) {
        progressColor = progColor
        progressPaint.color = progColor
        invalidate()
    }

    fun setTextColor(textColor: Int) {
        this.textColor = textColor
        textPaint.color = textColor
        invalidate()
    }

    fun setTextSize(size: Int) {
        textSize = size
        textPaint.textSize = size.toFloat()
        invalidate()
    }

    fun setText(text: String) {
        this.text = text
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height: Int
        val width: Int
        val specWidth = MeasureSpec.getSize(widthMeasureSpec)
        val specHeight = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        height = when (heightMode) {
            MeasureSpec.EXACTLY -> specHeight
            MeasureSpec.UNSPECIFIED, MeasureSpec.AT_MOST -> resources.getDimensionPixelSize(R.dimen.progress_button_default_height)
            else -> resources.getDimensionPixelSize(R.dimen.progress_button_default_height)
        }
        width = when (widthMode) {
            MeasureSpec.EXACTLY -> specWidth
            MeasureSpec.UNSPECIFIED, MeasureSpec.AT_MOST -> resources.getDimensionPixelSize(R.dimen.progress_button_default_width)
            else -> resources.getDimensionPixelSize(R.dimen.progress_button_default_width)
        }
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawDeterminateProgress(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    alpha = 0.3f
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    alpha = 1f
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun drawDeterminateProgress(canvas: Canvas) {
        if (progress == 0f) {
            onDrawInit(canvas)
        } else if (progress > 0f && progress < 100f) {
            onDrawProgress(canvas)
        } else {
            onDrawFinished(canvas)
        }
    }

    private fun onDrawInit(canvas: Canvas) {
        val bgRectf = RectF(
            topX, topY,
            canvas.width.toFloat(), canvas.height.toFloat()
        )
        canvas.drawRoundRect(
            bgRectf, cornerRadius, cornerRadius,
            backgroundPaint
        )
        drawText(canvas)
    }

    private fun onDrawProgress(canvas: Canvas) {
        val bgRectf = RectF(
            topX, topY,
            canvas.width.toFloat(), canvas.height.toFloat()
        )
        canvas.drawRoundRect(
            bgRectf, cornerRadius, cornerRadius,
            backgroundPaint
        )
        val progressPoint = canvas.width / 100 * progress
        val progRect = RectF(
            topX, topY, progressPoint,
            canvas.height.toFloat()
        )
        canvas.drawRoundRect(
            progRect, cornerRadius, cornerRadius,
            progressPaint
        )
        drawText(canvas)
    }

    private fun onDrawFinished(canvas: Canvas) {
        val bgRectf = RectF(
            topX, topY,
            canvas.width.toFloat(), canvas.height.toFloat()
        )
        canvas.drawRoundRect(
            bgRectf, cornerRadius, cornerRadius,
            backgroundPaint
        )
        val progRect = RectF(
            topX, topY,
            canvas.width.toFloat(), canvas.height.toFloat()
        )
        canvas.drawRoundRect(
            progRect, cornerRadius, cornerRadius,
            progressPaint
        )
        drawText(canvas)
    }

    private fun drawText(canvas: Canvas) {
        val bounds = Rect()
        textPaint.getTextBounds(text, topX.toInt(), text.length, bounds)
        val xPos = (canvas.width - bounds.width()) / 2
        val textAdjust = (textPaint.descent() + textPaint.ascent()).toInt() / 2
        val yPos = canvas.height / 2 - textAdjust
        canvas.drawText(text, xPos.toFloat(), yPos.toFloat(), textPaint)
    }

    fun setProgress(currentProgress: Float) {
        progress = currentProgress
        invalidate()
    }

    fun reset() {
        progress = 0f
        invalidate()
    }
}