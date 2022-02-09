/*
 * InstalledResourceListFragment.kt
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
import android.view.View
import space.celestia.mobilecelestia.resource.model.ResourceItem
import space.celestia.mobilecelestia.resource.model.ResourceManager
import space.celestia.mobilecelestia.utils.CelestiaString

class InstalledResourceListFragment : AsyncListFragment<ResourceItem>() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null)
            title = CelestiaString("Installed", "")
    }

    override val defaultErrorMessage: String?
        get() = null

    override suspend fun refresh(): List<ResourceItem> {
        try {
            return ResourceManager.shared.installedResourcesAsync()
        } catch (ignored: Throwable) {
            return listOf()
        }
    }

    override fun createViewHolder(listener: Listener<ResourceItem>?): BaseAsyncListAdapter<ResourceItem> {
        return AsyncListAdapter(listener)
    }

    companion object {
        @JvmStatic
        fun newInstance() = InstalledResourceListFragment()
    }
}
