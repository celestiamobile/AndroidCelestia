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
import io.reactivex.rxjava3.disposables.CompositeDisposable
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.core.CelestiaAppCore
import space.celestia.mobilecelestia.resource.model.ResourceAPI
import space.celestia.mobilecelestia.resource.model.ResourceAPIService
import space.celestia.mobilecelestia.resource.model.ResourceCategory
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.commonHandler

class ResourceCategoryListFragment : AsyncListFragment<ResourceCategory>() {
    private val compositeDisposable = CompositeDisposable()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title = CelestiaString("Categories", "")
        menuItems = listOf(NavigationFragment.MenuItem(MENU_ITEM_MANAGE_INSTALLED, CelestiaString("Installed", "")))
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    override fun refresh(success: (List<ResourceCategory>) -> Unit, failure: (String) -> Unit) {
        val lang = CelestiaAppCore.getLocalizedString("LANGUAGE", "celestia")
        val service = ResourceAPI.shared.create(ResourceAPIService::class.java)
        val disposable = service.categories(lang).commonHandler(object :
            TypeToken<ArrayList<ResourceCategory>>() {}.type, success = success, failure =
            {
                failure(CelestiaString("Failed to load add-on categories.", ""))
            })
        compositeDisposable.add(disposable)
    }

    companion object {
        @JvmStatic
        fun newInstance() = ResourceCategoryListFragment()

        const val MENU_ITEM_MANAGE_INSTALLED = 0
    }
}
