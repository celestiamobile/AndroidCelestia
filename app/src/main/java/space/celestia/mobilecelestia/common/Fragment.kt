// Fragment.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.common

import androidx.fragment.app.Fragment

fun Fragment.replace(fragment: Fragment, containerID: Int, useAsPrimaryNavigation: Boolean): Int? {
    if (!isAdded) return null

    val current = childFragmentManager.findFragmentById(containerID)
    var trans = childFragmentManager.beginTransaction()
    if (current != null) {
        trans = trans.hide(current).remove(current)
    }
    trans = trans.add(containerID, fragment)
    if (useAsPrimaryNavigation)
        trans = trans.setPrimaryNavigationFragment(fragment)
    return trans.commitAllowingStateLoss()
}