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

import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.disposables.CompositeDisposable
import space.celestia.mobilecelestia.core.CelestiaAppCore
import space.celestia.mobilecelestia.resource.model.ResourceAPI
import space.celestia.mobilecelestia.resource.model.ResourceAPIService
import space.celestia.mobilecelestia.resource.model.ResourceCategory
import space.celestia.mobilecelestia.utils.commonHandler

class ResourceCategoryListFragment : AsyncListFragment<ResourceCategory>() {
    private val compositeDisposable = CompositeDisposable()

    // TODO: Localization
    override val title: String
        get() = "Categories"

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    override fun refresh(success: (List<ResourceCategory>) -> Unit, failure: (String) -> Unit) {
        val lang = CelestiaAppCore.getLocalizedString("LANGUAGE", "celestia")
        val service = ResourceAPI.shared.create(ResourceAPIService::class.java)
        val disposable = service.categories(lang).commonHandler(object :
            TypeToken<ArrayList<ResourceCategory>>() {}.type, success,
            {
                // TODO: Localization
                failure("Failed to get plugin categories.")
            })
        compositeDisposable.add(disposable)
    }

    companion object {
        @JvmStatic
        fun newInstance() = ResourceCategoryListFragment()
    }
}
