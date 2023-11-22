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
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.celestiafoundation.utils.getSerializableValue

class GoToContainerFragment : NavigationFragment() {
    private val goToData: GoToInputFragment.GoToData
        get() = requireNotNull(_goToData)
    private var _goToData: GoToInputFragment.GoToData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (_goToData == null) {
            if (savedInstanceState != null) {
                _goToData = savedInstanceState.getSerializableValue(ARG_DATA, GoToInputFragment.GoToData::class.java)
            } else {
                arguments?.let {
                    _goToData = it.getSerializableValue(ARG_DATA, GoToInputFragment.GoToData::class.java)
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(ARG_DATA, goToData)
        super.onSaveInstanceState(outState)
    }

    override fun createInitialFragment(savedInstanceState: Bundle?): SubFragment {
        return GoToInputFragment.newInstance(goToData)
    }

    companion object {
        private const val ARG_DATA = "data"

        @JvmStatic
        fun newInstance(goToData: GoToInputFragment.GoToData) =
            GoToContainerFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_DATA, goToData)
                }
            }
    }
}
