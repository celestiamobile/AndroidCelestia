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

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.core.CelestiaAppCore
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.showAlert
import space.celestia.mobilecelestia.utils.showDateInput
import space.celestia.mobilecelestia.utils.showSingleSelection
import java.util.*

class EventFinderInputFragment : Fragment() {
    private var listener: Listener? = null

    private var adapter: EventFinderInputRecyclerViewAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_event_finder_input, container, false)

        val recView = view.findViewById<RecyclerView>(R.id.list)
        recView.layoutManager = LinearLayoutManager(context)
        adapter = EventFinderInputRecyclerViewAdapter({ isStartTime ->
            val ac = context as? Activity ?: return@EventFinderInputRecyclerViewAdapter
            val format = "yyyy/MM/dd HH:mm:ss"
            ac.showDateInput(CelestiaString("Please enter the time in \"$format\" format.", ""), format) { date ->
                if (date == null) {
                    ac.showAlert(CelestiaString("Unrecognized time string.", ""))
                    return@showDateInput
                }
                if (isStartTime) adapter?.startDate = date else adapter?.endDate = date
                adapter?.reload()
                adapter?.notifyDataSetChanged()
            }
        }, { current ->
            val ac = context as? Activity ?: return@EventFinderInputRecyclerViewAdapter
            val objects = listOf("Earth", "Jupiter")
            val currentIndex = Math.max(0, objects.indexOf(current))
            ac.showSingleSelection(CelestiaString("Please choose an object.", ""), objects.map { CelestiaAppCore.getLocalizedString(it, "celestia") }, currentIndex) { index ->
                adapter?.objectName = objects[index]
                adapter?.reload()
                adapter?.notifyDataSetChanged()
            }
        }, {
            val startDate = adapter?.startDate ?: return@EventFinderInputRecyclerViewAdapter
            val endDate = adapter?.endDate ?: return@EventFinderInputRecyclerViewAdapter
            val objectName = adapter?.objectName ?: return@EventFinderInputRecyclerViewAdapter
            listener?.onSearchForEvent(objectName, startDate, endDate)
        })
        recView.adapter = adapter
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement EventFinderInputFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface Listener {
        fun onSearchForEvent(objectName: String, startDate: Date, endDate: Date)
    }

    companion object {
        private const val TAG = "EventFinderInput"

        @JvmStatic
        fun newInstance() =
            EventFinderInputFragment()
    }
}
