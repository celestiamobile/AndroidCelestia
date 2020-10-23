/*
 * ResourceItemListFragment.kt
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
import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.disposables.CompositeDisposable
import space.celestia.mobilecelestia.core.CelestiaAppCore
import space.celestia.mobilecelestia.resource.model.ResourceAPI
import space.celestia.mobilecelestia.resource.model.ResourceAPIService
import space.celestia.mobilecelestia.resource.model.ResourceCategory
import space.celestia.mobilecelestia.resource.model.ResourceItem
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.commonHandler

class ResourceItemListFragment : AsyncListFragment<ResourceItem>() {
    private var category: ResourceCategory? = null
    private val compositeDisposable = CompositeDisposable()

    override val title: String
        get() = category?.name ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            val savedCategory = savedInstanceState.getSerializable(ARG_CATEGORY)
            if (savedCategory != null)
                category = savedCategory as ResourceCategory
        } else {
            val savedCategory = arguments?.getSerializable(ARG_CATEGORY)
            category = savedCategory as ResourceCategory
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putSerializable(ARG_CATEGORY, category)
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    override fun refresh(success: (List<ResourceItem>) -> Unit, failure: (String) -> Unit) {
        val categoryID = category?.id ?: return
        val lang = CelestiaAppCore.getLocalizedString("LANGUAGE", "celestia")
        val service = ResourceAPI.shared.create(ResourceAPIService::class.java)
        val disposable = service.items(lang, categoryID).commonHandler(object: TypeToken<ArrayList<ResourceItem>>() {}.type, success, {
            failure(CelestiaString("Failed to load add-ons.", ""))
        })
        compositeDisposable.add(disposable)
    }

    companion object {
        private const val ARG_CATEGORY = "category"

        @JvmStatic
        fun newInstance(category: ResourceCategory)  =
            ResourceItemListFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_CATEGORY, category)
                }
            }
    }
}
