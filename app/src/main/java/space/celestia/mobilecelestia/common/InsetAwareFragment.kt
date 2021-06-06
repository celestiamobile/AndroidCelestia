/*
 * InsetAwareFragment.kt
 *
 * Copyright (C) 2001-2021, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.common

import android.os.Bundle
import android.view.View
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment

open class InsetAwareFragment: Fragment() {
    class EdgeInsets(val left: Int, val top: Int, val right: Int, val bottom: Int) {
        constructor(insets: WindowInsetsCompat?) : this(
            insets?.displayCutout?.safeInsetLeft ?: 0,
            insets?.displayCutout?.safeInsetTop ?: 0,
            insets?.displayCutout?.safeInsetRight ?: 0,
            insets?.displayCutout?.safeInsetBottom ?: 0)
    }

    val currentSafeInsets: EdgeInsets
    get() {
        val rootView = view
        if (rootView == null)
            return EdgeInsets(0, 0, 0, 0)
        val insets = ViewCompat.getRootWindowInsets(rootView)
        return EdgeInsets(insets)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view, { _, insets ->
            onInsetChanged(view, EdgeInsets(insets))
            return@setOnApplyWindowInsetsListener insets ?: WindowInsetsCompat(insets)
        })
    }

    open fun onInsetChanged(view: View, newInsets: EdgeInsets) {}
}