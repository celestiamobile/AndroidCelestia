/*
 * CelestiaControlView.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.celestia

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.StandardImageButton

enum class CelestiaControlAction {
    ZoomIn, ZoomOut, ShowMenu, ToggleModeToCamera, ToggleModeToObject, Info, Hide, Show
}

interface CelestiaControlButton

class CelestiaToggleButton(val image: Int, val offAction: CelestiaControlAction, val onAction: CelestiaControlAction): CelestiaControlButton
class CelestiaTapButton(val image: Int, val action: CelestiaControlAction): CelestiaControlButton
class CelestiaPressButton(val image: Int, val action: CelestiaControlAction): CelestiaControlButton

class CelestiaControlView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs)  {
    init {
        setUp()
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setUp() {
        orientation = VERTICAL

        val items = listOf(
            CelestiaToggleButton(R.drawable.control_mode_combined, CelestiaControlAction.ToggleModeToObject, CelestiaControlAction.ToggleModeToCamera),
            CelestiaPressButton(R.drawable.control_zoom_in, CelestiaControlAction.ZoomIn),
            CelestiaPressButton(R.drawable.control_zoom_out, CelestiaControlAction.ZoomOut),
            CelestiaTapButton(R.drawable.control_info, CelestiaControlAction.Info),
            CelestiaTapButton(R.drawable.control_action_menu, CelestiaControlAction.ShowMenu),
            CelestiaTapButton(R.drawable.toolbar_exit, CelestiaControlAction.Hide)
        )

        for (index in items.indices) {
            val item = items[index]
            val button = StandardImageButton(context)
            button.setColorFilter(ContextCompat.getColor(context, R.color.colorSecondaryLabel))

            val size = resources.getDimensionPixelSize(R.dimen.control_view_icon_size)
            val params = LayoutParams(size, size)
            if (index != 0) {
                params.topMargin = resources.getDimensionPixelOffset(R.dimen.control_view_icon_spacing)
            }
            button.layoutParams = params
            when (item) {
                is CelestiaTapButton -> {
                    button.setImageResource(item.image)
                    button.setOnClickListener { listener?.didTapAction(item.action) }
                }
                is CelestiaPressButton -> {
                    button.setImageResource(item.image)
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
                is CelestiaToggleButton -> {
                    button.setImageResource(item.image)
                    button.setOnClickListener { btn ->
                        btn.isSelected = !btn.isSelected
                        listener?.didToggleToMode(if (btn.isSelected) item.onAction else item.offAction)
                    }
                }
                else -> {}
            }
            addView(button)
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