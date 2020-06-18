/*
 * Fragment.kt
 *
 * Copyright (C) 2001-2020, the Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.common

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import space.celestia.mobilecelestia.R

fun Fragment.push(fragment: Fragment, containerID: Int) = run {
    childFragmentManager.beginTransaction()
        .addToBackStack(childFragmentManager.backStackEntryCount.toString())
        .show(fragment)
        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
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

open class TitledFragment : Fragment() {
    open val title: String
        get() = ""
}

interface Poppable {
    fun canPop(): Boolean
    fun popLast()
}

abstract class PoppableFragment: Fragment(), Poppable {
    abstract override fun canPop(): Boolean
    abstract override fun popLast()
}