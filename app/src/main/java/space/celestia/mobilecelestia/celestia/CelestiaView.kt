// CelestiaView.kt
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
import android.graphics.PointF
import android.view.SurfaceView

@SuppressLint("ViewConstructor")
class CelestiaView(context: Context, private val scaleFactor: Float) : SurfaceView(context) {
    var isReady = false

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        holder.setFixedSize((width * scaleFactor).toInt(), (height * scaleFactor).toInt())
    }
}

fun PointF.scaleBy(factor: Float): PointF {
    return PointF(x * factor, y * factor)
}