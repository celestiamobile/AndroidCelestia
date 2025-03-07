/*
 * ResourceFragment.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.resource

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.celestiafoundation.resource.model.ResourceItem
import java.util.*

class ResourceFragment : NavigationFragment(), Toolbar.OnMenuItemClickListener {
    override fun createInitialFragment(savedInstanceState: Bundle?): SubFragment {
        return InstalledAddonListFragment.newInstance()
    }

    fun pushItem(item: ResourceItem) {
        // Installed item, update time is unknown so set to epoch time here
        val frag = ResourceItemFragment.newInstance(item, Date(0))
        pushFragment(frag)
    }

    companion object {
        fun newInstance() = ResourceFragment()
    }
}
