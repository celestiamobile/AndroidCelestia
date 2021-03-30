/*
 * Fragment.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.common

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import space.celestia.mobilecelestia.R

fun Fragment.push(fragment: Fragment, containerID: Int) = run {
    val ltr = resources.configuration.layoutDirection != View.LAYOUT_DIRECTION_RTL

    val ani1 = if (ltr) R.anim.enter_from_right else R.anim.enter_from_left
    val ani2 = if (ltr) R.anim.exit_to_left else R.anim.exit_to_right
    val ani3 = if (ltr) R.anim.enter_from_left else R.anim.enter_from_right
    val ani4 = if (ltr) R.anim.exit_to_right else R.anim.exit_to_left

    childFragmentManager.beginTransaction()
        .addToBackStack(childFragmentManager.backStackEntryCount.toString())
        .show(fragment)
        .setCustomAnimations(ani1, ani2, ani3, ani4)
        .add(containerID, fragment)
        .commitAllowingStateLoss()
}

fun Fragment.pop() = run {
    childFragmentManager.popBackStack((childFragmentManager.backStackEntryCount - 1).toString(), FragmentManager.POP_BACK_STACK_INCLUSIVE)
}

fun Fragment.replace(fragment: Fragment, containerID: Int) {
    val current = childFragmentManager.findFragmentById(containerID)
    var trans = childFragmentManager.beginTransaction()
    if (current != null) {
        trans = trans.hide(current).remove(current)
    }
    trans.add(containerID, fragment)
    trans.commitAllowingStateLoss()
}

interface Poppable {
    fun canPop(): Boolean
    fun popLast()
}