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

package space.celestia.mobilecelestia.goto

import android.os.Bundle
import space.celestia.mobilecelestia.common.EndNavigationFragment

class GoToContainerFragment : EndNavigationFragment() {
    private lateinit var goToData: GoToInputFragment.GoToData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            goToData = savedInstanceState.getSerializable(ARG_DATA) as GoToInputFragment.GoToData
        } else {
            arguments?.let {
                goToData = it.getSerializable(ARG_DATA) as GoToInputFragment.GoToData
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
