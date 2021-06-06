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
import space.celestia.mobilecelestia.common.*
import space.celestia.mobilecelestia.core.CelestiaSelection
import space.celestia.mobilecelestia.info.InfoFragment
import space.celestia.mobilecelestia.info.model.InfoDescriptionItem

class SearchContainerFragment : EndNavgationFragment() {
    override fun createInitialFragment(savedInstanceState: Bundle?): SubFragment {
        return SearchFragment.newInstance()
    }

    fun pushSearchResult(selection: CelestiaSelection) {
        pushFragment(InfoFragment.newInstance(selection))
    }

    companion object {
        @JvmStatic
        fun newInstance() = SearchContainerFragment()
    }
}
