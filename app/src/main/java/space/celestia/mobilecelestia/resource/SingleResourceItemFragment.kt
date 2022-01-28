/*
 * SingleResourceItemFragment.kt
 *
 * Copyright (C) 2001-2022, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.resource

import android.os.Bundle
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.resource.model.ResourceItem

class SingleResourceItemFragment : NavigationFragment() {
    private lateinit var item: ResourceItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        item = requireArguments().getSerializable(ARG_ITEM) as ResourceItem
    }

    override fun createInitialFragment(savedInstanceState: Bundle?): SubFragment {
        return ResourceItemFragment.newInstance(item)
    }

    companion object {
        private const val ARG_ITEM = "item"

        @JvmStatic
        fun newInstance(item: ResourceItem) =
            SingleResourceItemFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ITEM, item)
                }
            }
    }
}