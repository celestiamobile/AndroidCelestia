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

package space.celestia.mobilecelestia.goto

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.core.CelestiaAppCore
import space.celestia.mobilecelestia.core.CelestiaGoToLocation
import space.celestia.mobilecelestia.utils.*
import java.util.*

class GoToInputFragment : NavigationFragment.SubFragment() {
    private var listener: Listener? = null

    private var adapter: GoToInputRecyclerViewAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_general_grouped_list, container, false)

        val recView = view.findViewById<RecyclerView>(R.id.list)
        recView.layoutManager = LinearLayoutManager(context)
        adapter = GoToInputRecyclerViewAdapter(longitude = 0.0f, latitude = 0.0f, distance = 8.0, unit = CelestiaGoToLocation.DistanceUnit.radii, objectName = CelestiaAppCore.getLocalizedString("Earth", "celestia"), chooseFloatValueCallback = {
            type, value ->
            val ac = activity ?: return@GoToInputRecyclerViewAdapter
            ac.showTextInput("", String.format("%.2f", value)) { newValue ->
                val floatValue = newValue.toFloatOrNull() ?: return@showTextInput
                when (type) {
                    GoToFloatValueType.Longitude -> {
                        adapter?.longitude = floatValue
                    }
                    GoToFloatValueType.Latitude -> {
                        adapter?.latitude = floatValue
                    }
                }
                adapter?.reload()
                adapter?.notifyDataSetChanged()
            }
        }, chooseDoubleValueCallback = {
            type, value ->
            val ac = activity ?: return@GoToInputRecyclerViewAdapter
            ac.showTextInput("", String.format("%.2f", value)) { newValue ->
                val doubleValue = newValue.toDoubleOrNull() ?: return@showTextInput
                when (type) {
                    GoToDoubleValueType.Distance -> {
                        adapter?.distance = doubleValue
                    }
                }
                adapter?.reload()
                adapter?.notifyDataSetChanged()
            }
        }, chooseObjectCallback = { current ->
            val ac = activity ?: return@GoToInputRecyclerViewAdapter
            ac.showTextInput(CelestiaString("Please enter an object name.", ""), current) { objectName ->
                adapter?.objectName = objectName
                adapter?.reload()
                adapter?.notifyDataSetChanged()
            }
        }, chooseUnitCallback = { current ->
            val ac = activity ?: return@GoToInputRecyclerViewAdapter
            val index = distanceUnits.indexOf(current)
            ac.showSingleSelection("", distanceUnits.map { value -> CelestiaString(value.name, "") }, index, { newIndex ->
                adapter?.unit = distanceUnits[newIndex]
                adapter?.reload()
                adapter?.notifyDataSetChanged()
            })

        }, proceedCallback = {
            adapter?.let {
                listener?.onGoToObject(it.objectName, it.longitude, it.latitude, it.distance, it.unit)
            }
        })

        if (savedInstanceState != null) {
            val objectName = savedInstanceState.getString(OBJECT_TAG, CelestiaAppCore.getLocalizedString("Earth", "celestia"))
            val longitude = savedInstanceState.getFloat(LONGITUDE_TAG, 0.0f)
            val latitude = savedInstanceState.getFloat(LATITUDE_TAG, 0.0f)
            val distance = savedInstanceState.getDouble(DISTANCE_TAG, 8.0)
            val unit = savedInstanceState.getSerializable(UNIT_TAG) as? CelestiaGoToLocation.DistanceUnit ?: CelestiaGoToLocation.DistanceUnit.radii

            adapter?.objectName = objectName
            adapter?.longitude = longitude
            adapter?.latitude = latitude
            adapter?.distance = distance
            adapter?.unit = unit
            adapter?.reload()
        }

        recView.adapter = adapter
        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        adapter?.let {
            outState.putFloat(LONGITUDE_TAG, it.longitude)
            outState.putFloat(LATITUDE_TAG, it.latitude)
            outState.putDouble(DISTANCE_TAG, it.distance)
            outState.putString(OBJECT_TAG, it.objectName)
            outState.putSerializable(UNIT_TAG, it.unit)
        }

        super.onSaveInstanceState(outState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null)
            title = CelestiaString("Go to Object", "")
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
        adapter = null
    }

    interface Listener {
        fun onGoToObject(objectName: String, longitude: Float, latitude: Float, distance: Double, distanceUnit: CelestiaGoToLocation.DistanceUnit)
    }

    companion object {
        private const val TAG = "GoToInput"

        private val distanceUnits = listOf(
            CelestiaGoToLocation.DistanceUnit.radii,
            CelestiaGoToLocation.DistanceUnit.km,
            CelestiaGoToLocation.DistanceUnit.au
        )

        private const val LONGITUDE_TAG = "longitude"
        private const val LATITUDE_TAG = "latitude"
        private const val OBJECT_TAG = "object"
        private const val DISTANCE_TAG = "distance"
        private const val UNIT_TAG = "unit"

        @JvmStatic
        fun newInstance() =
            GoToInputFragment()
    }
}
