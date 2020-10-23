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

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import space.celestia.mobilecelestia.resource.model.ResourceItem
import space.celestia.mobilecelestia.resource.model.ResourceManager
import space.celestia.mobilecelestia.utils.CelestiaString

class InstalledResourceListFragment : AsyncListFragment<ResourceItem>() {
    private val compositeDisposable = CompositeDisposable()

    override val title: String
        get() = CelestiaString("Installed", "")

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    override fun refresh(success: (List<ResourceItem>) -> Unit, failure: (String) -> Unit) {
        val disposable = Observable.create<List<ResourceItem>> {
            it.onNext(ResourceManager.shared.installedResources())
            it.onComplete()
        }
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe { items ->
                success(items)
            }
        compositeDisposable.add(disposable)
    }

    companion object {
        @JvmStatic
        fun newInstance() = InstalledResourceListFragment()
    }
}
