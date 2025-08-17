// BottomControlFragment.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.control

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.info.model.CelestiaAction
import space.celestia.mobilecelestia.info.model.CelestiaContinuosAction

class BottomControlFragment : Fragment() {

    private var listener: Listener? = null
    private var items: List<BottomControlAction> = listOf()
    private var overflowItems: List<OverflowItem> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            @Suppress("UNCHECKED_CAST")
            items = BundleCompat.getSerializable(it, ARG_ACTIONS, ArrayList::class.java) as List<BottomControlAction>
            @Suppress("UNCHECKED_CAST")
            overflowItems = BundleCompat.getSerializable(it, ARG_OVERFLOW_ACTIONS, ArrayList::class.java) as List<OverflowItem>
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
            adapter = BottomControlRecyclerViewAdapter(items, overflowItems, listener)
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
        fun onInstantActionSelected(item: CelestiaAction)
        fun onContinuousActionDown(item: CelestiaContinuosAction)
        fun onContinuousActionUp(item: CelestiaContinuosAction)
        fun onCustomAction(type: CustomActionType)
        fun onBottomControlHide()
    }

    companion object {
        const val ARG_ACTIONS = "action"
        const val ARG_OVERFLOW_ACTIONS = "overflow_actions"

        @JvmStatic
        fun newInstance(items: List<BottomControlAction>, overflowItems: List<OverflowItem> = listOf()) =
            BottomControlFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ACTIONS, ArrayList<BottomControlAction>(items))
                    putSerializable(ARG_OVERFLOW_ACTIONS, ArrayList<OverflowItem>(overflowItems))
                }
            }
    }
}
