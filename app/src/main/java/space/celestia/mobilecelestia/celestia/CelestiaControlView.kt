// CelestiaControlView.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.celestia

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.StandardImageButton

enum class CelestiaControlAction {
    ZoomIn, ZoomOut, ShowMenu, ToggleModeToCamera, ToggleModeToObject, Info, Search, Hide, Show, Go
}

sealed class CelestiaControlButton {
    data class Toggle(
        val image: Int,
        val offAction: CelestiaControlAction,
        val onAction: CelestiaControlAction,
        val contentDescription: String,
        val currentState: Boolean
    ) : CelestiaControlButton()

    data class Tap(
        val image: Int,
        val action: CelestiaControlAction,
        val contentDescription: String
    ) : CelestiaControlButton()

    data class Press(
        val image: Int,
        val action: CelestiaControlAction,
        val contentDescription: String
    ) : CelestiaControlButton()
}

class CelestiaControlView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs)  {
    var buttons: List<CelestiaControlButton> = listOf()
    set(value) {
        field = value
        setUp()
    }

    init {
        setUp()
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setUp() {
        removeAllViews()

        orientation = VERTICAL

        for (index in buttons.indices) {
            val item = buttons[index]
            val view = LayoutInflater.from(context).inflate(R.layout.toolbar_item, this, false)
            val button = view.findViewById<StandardImageButton>(R.id.button)
            when (item) {
                is CelestiaControlButton.Tap -> {
                    button.setImageResource(item.image)
                    button.contentDescription = item.contentDescription
                    button.setOnClickListener { listener?.didTapAction(item.action) }
                }
                is CelestiaControlButton.Press -> {
                    button.setImageResource(item.image)
                    button.contentDescription = item.contentDescription
                    button.setOnTouchListener { view, event ->
                        when (event.actionMasked) {
                            MotionEvent.ACTION_DOWN -> {
                                listener?.didStartPressingAction(item.action)
                            }
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                listener?.didEndPressingAction(item.action)
                            }
                        }
                        view.onTouchEvent(event)
                    }
                }
                is CelestiaControlButton.Toggle -> {
                    button.isSelected = item.currentState
                    button.contentDescription = item.contentDescription
                    button.setImageResource(item.image)
                    button.setOnClickListener { btn ->
                        btn.isSelected = !btn.isSelected
                        listener?.didToggleToMode(if (btn.isSelected) item.onAction else item.offAction)
                    }
                }
            }
            addView(view)
        }
    }

    var listener: Listener? = null

    interface Listener {
        fun didTapAction(action: CelestiaControlAction)
        fun didStartPressingAction(action: CelestiaControlAction)
        fun didEndPressingAction(action: CelestiaControlAction)
        fun didToggleToMode(action: CelestiaControlAction)
    }
}