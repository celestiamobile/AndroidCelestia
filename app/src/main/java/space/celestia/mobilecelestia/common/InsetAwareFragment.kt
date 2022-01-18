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

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment

open class InsetAwareFragment: Fragment() {
    val currentSafeInsets: EdgeInsets
        get() {
            val rootView = view
            if (rootView == null)
                return EdgeInsets(0, 0, 0, 0)
            val insets = ViewCompat.getRootWindowInsets(rootView)
            return EdgeInsets(insets)
        }

    val currentRoundedCorners: RoundedCorners
        get() {
            val rootView = view
            if (rootView == null)
                return RoundedCorners(0, 0, 0, 0)
            val insets = ViewCompat.getRootWindowInsets(rootView)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                return RoundedCorners(insets)
            }
            return RoundedCorners(0, 0, 0, 0)
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val roundedCorners = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                RoundedCorners(insets)
            } else {
                RoundedCorners(0, 0, 0, 0)
            }
            onInsetChanged(view, EdgeInsets(insets), roundedCorners)
            return@setOnApplyWindowInsetsListener insets ?: WindowInsetsCompat(insets)
        }
    }

    open fun onInsetChanged(view: View, newInsets: EdgeInsets, roundedCorners: RoundedCorners) {}
}