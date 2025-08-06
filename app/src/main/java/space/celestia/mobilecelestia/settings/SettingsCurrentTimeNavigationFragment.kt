// SettingsCurrentTimeNavigationFragment.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.settings

import android.os.Bundle
import space.celestia.mobilecelestia.common.NavigationFragment

class SettingsCurrentTimeNavigationFragment: NavigationFragment() {
    override fun createInitialFragment(savedInstanceState: Bundle?): SubFragment {
        return SettingsCurrentTimeFragment.newInstance()
    }

    companion object {
        fun newInstance() = SettingsCurrentTimeNavigationFragment()
    }
}