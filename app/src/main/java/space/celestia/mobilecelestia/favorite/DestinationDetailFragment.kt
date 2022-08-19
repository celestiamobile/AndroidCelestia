/*
 * DestinationDetailFragment.kt
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.celestia.Destination
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.getSerializableValue

class DestinationDetailFragment : NavigationFragment.SubFragment() {
    private var item: Destination? = null
    private var listener: Listener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            item = it.getSerializableValue(ARG_ITEM, Destination::class.java)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_destination_detail, container, false)
        val title = view.findViewById<TextView>(R.id.title)
        val content = view.findViewById<TextView>(R.id.description)
        val button = view.findViewById<Button>(R.id.action_button)

        title.text = item?.name ?: ""
        content.text = item?.description ?: ""

        button.setOnClickListener { this.onButtonClick() }

        button.text = CelestiaString("Go", "")

        return view
    }

    private fun onButtonClick() {
        val destination = item ?: return
        listener?.onGoToDestination(destination)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement DestinationDetailFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface Listener {
        fun onGoToDestination(destination: Destination)
    }

    companion object {
        private const val ARG_ITEM = "item"
        @JvmStatic
        fun newInstance(destination: Destination) =
            DestinationDetailFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ITEM, destination)
                }
            }
    }
}