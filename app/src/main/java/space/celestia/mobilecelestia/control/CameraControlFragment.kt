/*
 * CameraControlFragment.kt
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.RightSubFragment
import space.celestia.mobilecelestia.utils.CelestiaString

enum class CameraControlAction(val value: Int) {
    Pitch0(32), Pitch1(26), Yaw0(28), Yaw1(30), Roll0(31), Roll1(33), Reverse(-1);
}

class CameraControlFragment : RightSubFragment() {
    private var listener: Listener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_general_grouped_list, container, false)

        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = CameraControlItemRecyclerViewAdapter(listener)
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null)
            title = CelestiaString("Camera Control", "")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement CameraControlFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface Listener {
        fun onCameraActionClicked(action: CameraControlAction)
        fun onCameraActionStepperTouchDown(action: CameraControlAction)
        fun onCameraActionStepperTouchUp(action: CameraControlAction)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            CameraControlFragment()
    }
}
