/*
 * SearchResultFragment.kt
 *
 * Copyright (C) 2001-2021, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.replace
import space.celestia.mobilecelestia.info.InfoFragment
import space.celestia.mobilecelestia.info.model.InfoDescriptionItem

class SearchResultFragment : Fragment()  {
    private val toolbar by lazy { requireView().findViewById<Toolbar>(R.id.toolbar) }

    private var descriptionItem: InfoDescriptionItem? = null
    private var listener: Listener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            descriptionItem = it.getSerializable(InfoFragment.ARG_DESCRIPTION_ITEM) as? InfoDescriptionItem
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_general_container_with_toolbar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        toolbar.navigationIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_action_arrow_back, null)
        toolbar.setNavigationOnClickListener {
            listener?.onSearchBackButtonPressed()
        }

        val item = descriptionItem
        if (savedInstanceState == null && item != null) {
            replace(InfoFragment.newInstance(item, true), R.id.fragment_container)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement SearchResultFragment.nListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface Listener {
        fun onSearchBackButtonPressed()
    }

    companion object {
        const val SEARCH_RESULT_LIMIT = 20
        const val ARG_DESCRIPTION_ITEM = "description-item"

        @JvmStatic
        fun newInstance(info: InfoDescriptionItem) = SearchResultFragment().apply {
            arguments = Bundle().apply {
                this.putSerializable(InfoFragment.ARG_DESCRIPTION_ITEM, info)
            }
        }
    }
}
