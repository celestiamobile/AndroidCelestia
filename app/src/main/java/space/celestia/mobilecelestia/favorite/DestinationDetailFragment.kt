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
import android.widget.TextView
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.EndSubFragment
import space.celestia.mobilecelestia.core.CelestiaDestination
import space.celestia.mobilecelestia.utils.CelestiaString

class DestinationDetailFragment : EndSubFragment() {
    private var item: CelestiaDestination? = null
    private var listener: Listener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            item = it.getSerializable(ARG_ITEM) as? CelestiaDestination
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_destination_detail, container, false)
        val title = view.findViewById<TextView>(R.id.title)
        val content = view.findViewById<TextView>(R.id.description)
        val buttonContainer = view.findViewById<View>(R.id.button_container)
        val button = view.findViewById<TextView>(R.id.button)

        title.text = item?.name ?: ""
        content.text = item?.description ?: ""

        buttonContainer.setOnClickListener { this.onButtonClick() }

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
        fun onGoToDestination(destination: CelestiaDestination)
    }

    companion object {
        private const val ARG_ITEM = "item"
        @JvmStatic
        fun newInstance(destination: CelestiaDestination) =
            DestinationDetailFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ITEM, destination)
                }
            }
    }
}