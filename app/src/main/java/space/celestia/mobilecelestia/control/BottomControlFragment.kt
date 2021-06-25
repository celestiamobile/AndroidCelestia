/*
 * BottomControlFragment.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
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
import space.celestia.mobilecelestia.info.model.CelestiaAction

fun CelestiaAction.imageID(): Int? {
    return when (this) {
        CelestiaAction.Faster -> {
            R.drawable.time_faster
        }
        CelestiaAction.Slower -> {
            R.drawable.time_slower
        }
        CelestiaAction.PlayPause -> {
            R.drawable.time_playpause
        }
        CelestiaAction.CancelScript -> {
            R.drawable.time_stop
        }
        CelestiaAction.Reverse -> {
            R.drawable.time_reverse
        }
        else -> {
            null
        }
    }
}

class CelestiaActionItem(val action: CelestiaAction, val image: Int)

class BottomControlFragment : Fragment() {

    private var listener: Listener? = null
    private var items: List<CelestiaAction> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            @Suppress("UNCHECKED_CAST")
            items = it.getSerializable(ARG_ACTIONS) as? List<CelestiaAction> ?: listOf()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bottom_control_list, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.list)
        // Set the adapter
        with(recyclerView) {
            val manager = LinearLayoutManager(context)
            manager.orientation = LinearLayoutManager.HORIZONTAL
            layoutManager = manager
            adapter = BottomControlRecyclerViewAdapter(
                items.map {
                    CelestiaActionItem(
                        it,
                        it.imageID() ?: 0
                    )
                }, listener
            )
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement BottomControlFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface Listener {
        fun onActionSelected(item: CelestiaAction)
        fun onBottomControlHide()
    }

    companion object {
        const val ARG_ACTIONS = "action"

        @JvmStatic
        fun newInstance(items: List<CelestiaAction>) =
            BottomControlFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ACTIONS, ArrayList<CelestiaAction>(items))
                }
            }
    }
}
