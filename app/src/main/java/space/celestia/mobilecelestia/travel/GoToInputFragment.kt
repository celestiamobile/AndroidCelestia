/*
 * GoToInputFragment.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.travel

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.celestia.GoToLocation
import space.celestia.mobilecelestia.utils.*
import java.io.Serializable
import java.lang.ref.WeakReference

class GoToInputFragment : NavigationFragment.SubFragment() {
    class GoToData(var objectName: String, var longitude: Float, var latitude: Float, var distance: Double, var distanceUnit: GoToLocation.DistanceUnit) :
        Serializable

    private var listener: Listener? = null

    private val adapter by lazy { createAdapter() }

    private val goToData: GoToData
        get() = requireNotNull(_goToData)
    private var _goToData: GoToData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = CelestiaString("Go to Object", "")

        if (_goToData == null) {
            if (savedInstanceState != null) {
                _goToData = savedInstanceState.getSerializable(ARG_DATA) as GoToData
            } else {
                arguments?.let {
                    _goToData = it.getSerializable(ARG_DATA) as GoToData
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_general_grouped_list, container, false)

        val recView = view.findViewById<RecyclerView>(R.id.list)
        recView.layoutManager = LinearLayoutManager(context)

        adapter.objectName = goToData.objectName
        adapter.longitude = goToData.longitude
        adapter.latitude = goToData.latitude
        adapter.distance = goToData.distance
        adapter.unit = goToData.distanceUnit
        adapter.reload()

        recView.adapter = adapter
        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(ARG_DATA, goToData)

        super.onSaveInstanceState(outState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement GoToInputFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    fun createAdapter(): GoToInputRecyclerViewAdapter {
        val weakSelf = WeakReference(this)
        return GoToInputRecyclerViewAdapter(longitude = goToData.longitude, latitude = goToData.latitude, distance = goToData.distance, unit = goToData.distanceUnit, objectName = goToData.objectName, chooseFloatValueCallback = {
                type, value ->
            val self = weakSelf.get() ?: return@GoToInputRecyclerViewAdapter
            val ac = self.activity ?: return@GoToInputRecyclerViewAdapter
            ac.showTextInput("", String.format("%.2f", value)) { newValue ->
                val floatValue = newValue.toFloatOrNull() ?: return@showTextInput
                when (type) {
                    GoToFloatValueType.Longitude -> {
                        self.goToData.longitude = floatValue
                        self.adapter.longitude = floatValue
                    }
                    GoToFloatValueType.Latitude -> {
                        self.goToData.latitude = floatValue
                        self.adapter.latitude = floatValue
                    }
                }
                self.adapter.reload()
                self.adapter.notifyDataSetChanged()
            }
        }, chooseDoubleValueCallback = {
                type, value ->
            val self = weakSelf.get() ?: return@GoToInputRecyclerViewAdapter
            val ac = self.activity ?: return@GoToInputRecyclerViewAdapter
            ac.showTextInput("", String.format("%.2f", value)) { newValue ->
                val doubleValue = newValue.toDoubleOrNull() ?: return@showTextInput
                when (type) {
                    GoToDoubleValueType.Distance -> {
                        self.goToData.distance = doubleValue
                        self.adapter.distance = doubleValue
                    }
                }
                self.adapter.reload()
                self.adapter.notifyDataSetChanged()
            }
        }, chooseObjectCallback = {
            val self = weakSelf.get() ?: return@GoToInputRecyclerViewAdapter
            self.listener?.onEditGoToObject(self.goToData)
        }, chooseUnitCallback = {
            val self = weakSelf.get() ?: return@GoToInputRecyclerViewAdapter
            val ac = self.activity ?: return@GoToInputRecyclerViewAdapter
            ac.showOptions("", distanceUnits.map { value -> CelestiaString(value.name, "") }.toTypedArray(),) { newIndex ->
                val unit = distanceUnits[newIndex]
                self.adapter.unit = unit
                self.goToData.distanceUnit = unit
                self.adapter.reload()
                self.adapter.notifyDataSetChanged()
            }
        }, proceedCallback = {
            val self = weakSelf.get() ?: return@GoToInputRecyclerViewAdapter
            self.listener?.onGoToObject(goToData)
        })
    }

    fun updateObjectName(name: String) {
        goToData.objectName = name
        adapter.objectName = name
        adapter.reload()
        adapter.notifyDataSetChanged()
    }

    interface Listener {
        fun onGoToObject(goToData: GoToData)
        fun onEditGoToObject(goToData: GoToData)
    }

    companion object {
        private const val TAG = "GoToInput"
        private const val ARG_DATA = "data"

        private val distanceUnits = listOf(
            GoToLocation.DistanceUnit.radii,
            GoToLocation.DistanceUnit.km,
            GoToLocation.DistanceUnit.au
        )

        @JvmStatic
        fun newInstance(goToData: GoToData) =
            GoToInputFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_DATA, goToData)
                }
            }
    }
}
