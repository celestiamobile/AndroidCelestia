/*
 * CameraControlFragment.kt
 *
 * Copyright (C) 2001-2020, the Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.control

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.core.CelestiaEclipseFinder

class EventFinderResultFragment : Fragment() {
    private var listener: Listener? = null

    private var adapter: EventFinderResultRecyclerViewAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_general_grouped_list, container, false)

        val recView = view.findViewById<RecyclerView>(R.id.list)
        recView.layoutManager = LinearLayoutManager(context)
        adapter = EventFinderResultRecyclerViewAdapter({ eclipse ->
            listener?.onEclipseChosen(eclipse)
        }, eclipses)
        recView.adapter = adapter
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement EventFinderResultFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface Listener {
        fun onEclipseChosen(eclipse: CelestiaEclipseFinder.Eclipse)
    }

    companion object {
        private const val TAG = "EventFinderResult"

        var eclipses: List<CelestiaEclipseFinder.Eclipse> = listOf()

        @JvmStatic
        fun newInstance() =
            EventFinderResultFragment()
    }
}
