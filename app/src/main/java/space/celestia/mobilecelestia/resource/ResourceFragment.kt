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

import android.content.Context
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.resource.model.ResourceCategory
import space.celestia.mobilecelestia.resource.model.ResourceItem

class ResourceFragment : NavigationFragment(), Toolbar.OnMenuItemClickListener {
    private var listener: Listener? = null

    override fun createInitialFragment(savedInstanceState: Bundle?): SubFragment {
        return ResourceCategoryListPagingFragment.newInstance()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement ResourceFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    fun pushItem(item: ResourceCategory) {
        val frag = ResourceItemListPagingFragment.newInstance(item)
        pushFragment(frag)
    }

    fun pushItem(item: ResourceItem) {
        val frag = ResourceItemFragment.newInstance(item)
        pushFragment(frag)
    }

    interface Listener {
    }

    companion object {
        fun newInstance() = ResourceFragment()
    }
}
