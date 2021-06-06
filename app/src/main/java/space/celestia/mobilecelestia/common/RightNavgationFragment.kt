/*
 * EndNavgationFragment.kt
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
import android.util.LayoutDirection
import android.view.View
import androidx.appcompat.widget.Toolbar

abstract class EndNavgationFragment: NavigationFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyPadding(toolbar, currentSafeInsets)
    }

    override fun onInsetChanged(view: View, newInsets: EdgeInsets) {
        super.onInsetChanged(view, newInsets)

        applyPadding(toolbar, newInsets)
    }

    private fun applyPadding(toolbar: Toolbar, insets: EdgeInsets) {
        val isRTL = resources.configuration.layoutDirection == LayoutDirection.RTL
        if (isRTL)
            toolbar.setPadding(insets.left, insets.top, 0, 0)
        else
            toolbar.setPadding(0, insets.top, insets.right, 0)
    }
}

abstract class EndSubFragment: NavigationFragment.SubFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyPadding(view, currentSafeInsets)
    }

    override fun onInsetChanged(view: View, newInsets: EdgeInsets) {
        super.onInsetChanged(view, newInsets)

        applyPadding(view, newInsets)
    }

    private fun applyPadding(view: View, insets: EdgeInsets) {
        val isRTL = resources.configuration.layoutDirection == LayoutDirection.RTL
        if (isRTL)
            view.setPadding(insets.left, 0, 0, insets.bottom)
        else
            view.setPadding(0, 0, insets.right, insets.bottom)
    }
}