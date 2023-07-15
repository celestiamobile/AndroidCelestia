/*
 * SettingsCurrentTimeNavigationFragment.kt
 *
 * Copyright (C) 2023-present Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.settings

import android.os.Bundle
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment

class SettingsCurrentTimeNavigationFragment: NavigationFragment(), SettingsBaseFragment {
    override fun createInitialFragment(savedInstanceState: Bundle?): SubFragment {
        return SettingsCurrentTimeFragment.newInstance()
    }

    override fun reload() {
        val frag = childFragmentManager.findFragmentById(R.id.fragment_container)
        if (frag is SettingsBaseFragment)
            frag.reload()
    }

    companion object {
        fun newInstance() = SettingsCurrentTimeNavigationFragment()
    }
}