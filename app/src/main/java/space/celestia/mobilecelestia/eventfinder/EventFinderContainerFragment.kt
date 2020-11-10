/*
 * EventFinderContainerFragment.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.eventfinder

import android.os.Bundle
import space.celestia.mobilecelestia.common.NavigationFragment

class EventFinderContainerFragment : NavigationFragment() {
    override fun createInitialFragment(savedInstanceState: Bundle?): SubFragment {
        return EventFinderInputFragment.newInstance()
    }

    fun showResult() {
        pushFragment(EventFinderResultFragment.newInstance())
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            EventFinderContainerFragment()
    }
}
