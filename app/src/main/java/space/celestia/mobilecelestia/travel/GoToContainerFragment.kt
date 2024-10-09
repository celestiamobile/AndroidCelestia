/*
 * GoToContainerFragment.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.travel

import android.os.Bundle
import androidx.core.os.BundleCompat
import space.celestia.celestia.Selection
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.celestiafoundation.utils.getSerializableValue

class GoToContainerFragment : NavigationFragment() {
    private val goToData: GoToInputFragment.GoToData
        get() = requireNotNull(_goToData)
    private var _goToData: GoToInputFragment.GoToData? = null

    private val selection: Selection
        get() = requireNotNull(_selection)
    private var _selection: Selection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            _selection = BundleCompat.getParcelable(it, ARG_OBJECT, Selection::class.java)
            _goToData = it.getSerializableValue(ARG_DATA, GoToInputFragment.GoToData::class.java)
        }
    }

    override fun createInitialFragment(savedInstanceState: Bundle?): SubFragment {
        return GoToInputFragment.newInstance(goToData, selection)
    }

    companion object {
        private const val ARG_DATA = "data"
        private const val ARG_OBJECT = "object"

        @JvmStatic
        fun newInstance(goToData: GoToInputFragment.GoToData, selection: Selection) =
            GoToContainerFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_OBJECT, selection)
                    putSerializable(ARG_DATA, goToData)
                }
            }
    }
}
