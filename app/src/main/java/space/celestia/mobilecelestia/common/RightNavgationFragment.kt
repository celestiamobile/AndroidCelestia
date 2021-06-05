/*
 * RightNavgationFragment.kt
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

abstract class RightNavgationFragment: NavigationFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.setPadding(0, currentSafeInsets.top, currentSafeInsets.right, 0)
    }

    override fun onInsetChanged(view: View, newInset: EdgeInsets) {
        super.onInsetChanged(view, newInset)

        toolbar.setPadding(0, newInset.top, newInset.right, 0)
    }
}

abstract class RightSubFragment: NavigationFragment.SubFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setPadding(0, 0, currentSafeInsets.right, currentSafeInsets.bottom)
    }

    override fun onInsetChanged(view: View, newInset: EdgeInsets) {
        super.onInsetChanged(view, newInset)

        view.setPadding(0, 0, newInset.right, newInset.bottom)
    }
}