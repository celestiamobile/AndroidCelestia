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
import space.celestia.mobilecelestia.resource.model.ResourceCategory
import space.celestia.mobilecelestia.resource.model.ResourceItem

class ResourceFragment : NavigationFragment(), Toolbar.OnMenuItemClickListener {
    private lateinit var language: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        language = requireArguments().getString(ARG_LANG, "en")
    }

    override fun createInitialFragment(savedInstanceState: Bundle?): SubFragment {
        return ResourceCategoryListPagingFragment.newInstance(language)
    }

    fun pushItem(item: ResourceCategory) {
        val frag = ResourceItemListPagingFragment.newInstance(item, language)
        pushFragment(frag)
    }

    fun pushItem(item: ResourceItem) {
        val frag = ResourceItemFragment.newInstance(item, language)
        pushFragment(frag)
    }

    companion object {
        private const val ARG_LANG = "lang"

        fun newInstance(language: String) = ResourceFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_LANG, language)
            }
        }
    }
}
