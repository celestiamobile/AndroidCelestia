/*
 * SimpleTextFragment.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.RightSubFragment

class SimpleTextFragment : RightSubFragment() {

    private var textTitle: String? = null
    private var textDetail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            textTitle = it.getString(ARG_TITLE, null)
            textDetail = it.getString(ARG_DETAIL, null)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_simple_text, container, false)
        view.findViewById<TextView>(R.id.text).text = textDetail
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null)
            title = textTitle ?: ""
    }

    companion object {

        const val ARG_TITLE = "title"
        const val ARG_DETAIL = "detail"

        @JvmStatic
        fun newInstance(title: String, detail: String) =
            SimpleTextFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_DETAIL, detail)
                }
            }
    }
}
