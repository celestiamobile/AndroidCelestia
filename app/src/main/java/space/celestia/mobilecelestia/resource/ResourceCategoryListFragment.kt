/*
 * ResourceCategoryListFragment.kt
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
import com.google.gson.reflect.TypeToken
import space.celestia.celestia.AppCore
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.resource.model.ResourceAPI
import space.celestia.mobilecelestia.resource.model.ResourceAPIService
import space.celestia.mobilecelestia.resource.model.ResourceCategory
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.commonHandler

class ResourceCategoryListFragment : AsyncListFragment<ResourceCategory>() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            title = CelestiaString("Categories", "")
            rightNavigationBarItems = listOf(
                NavigationFragment.BarButtonItem(
                    MENU_ITEM_MANAGE_INSTALLED,
                    CelestiaString("Installed", "")
                )
            )
        }
    }

    override val defaultErrorMessage: String
        get() = CelestiaString("Failed to load add-ons.", "")

    override suspend fun refresh(): List<ResourceCategory> {
        val lang = AppCore.getLocalizedString("LANGUAGE", "celestia")
        val service = ResourceAPI.shared.create(ResourceAPIService::class.java)
        return service.categories(lang).commonHandler<List<ResourceCategory>>(object: TypeToken<ArrayList<ResourceCategory>>() {}.type, ResourceAPI.gson)
    }

    override fun menuItemClicked(groupId: Int, id: Int): Boolean {
        when (id) {
            MENU_ITEM_MANAGE_INSTALLED -> {
                val fragment = InstalledResourceListFragment.newInstance()
                (parentFragment as? NavigationFragment)?.pushFragment(fragment)
            } else -> {}
        }
        return true
    }

    companion object {
        @JvmStatic
        fun newInstance() = ResourceCategoryListFragment()

        const val MENU_ITEM_MANAGE_INSTALLED = 0
    }
}
