/*
 * SearchContainerFragment.kt
 *
 * Copyright (C) 2001-2021, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.Poppable
import space.celestia.mobilecelestia.common.pop
import space.celestia.mobilecelestia.common.push
import space.celestia.mobilecelestia.common.replace
import space.celestia.mobilecelestia.core.CelestiaSelection
import space.celestia.mobilecelestia.info.model.InfoDescriptionItem

class SearchContainerFragment : Fragment(), Poppable {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_general_container, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (savedInstanceState != null)
            return

        replace(SearchFragment.newInstance(), R.id.fragment_container)
    }

    fun pushSearchResult(selection: CelestiaSelection) {
        push(SearchResultFragment.newInstance(selection), R.id.fragment_container)
    }

    fun backToSearch() {
        popLast()
    }

    override fun canPop(): Boolean {
        return childFragmentManager.backStackEntryCount > 0
    }

    override fun popLast() {
        pop()
    }

    companion object {
        @JvmStatic
        fun newInstance() = SearchContainerFragment()
    }
}
