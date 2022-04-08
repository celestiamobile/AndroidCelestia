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

class GoToInputFragment : NavigationFragment.SubFragment() {
    class GoToData(var objectName: String, var longitude: Float, var latitude: Float, var distance: Double, var distanceUnit: GoToLocation.DistanceUnit) :
        Serializable

    private var listener: Listener? = null

    private var adapter: GoToInputRecyclerViewAdapter? = null

    private lateinit var goToData: GoToInputFragment.GoToData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            goToData = savedInstanceState.getSerializable(ARG_DATA) as GoToData
        } else {
            arguments?.let {
                goToData = it.getSerializable(ARG_DATA) as GoToInputFragment.GoToData
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
        adapter = GoToInputRecyclerViewAdapter(longitude = goToData.longitude, latitude = goToData.latitude, distance = goToData.distance, unit = goToData.distanceUnit, objectName = goToData.objectName, chooseFloatValueCallback = {
            type, value ->
            val ac = activity ?: return@GoToInputRecyclerViewAdapter
            ac.showTextInput("", String.format("%.2f", value)) { newValue ->
                val floatValue = newValue.toFloatOrNull() ?: return@showTextInput
                when (type) {
                    GoToFloatValueType.Longitude -> {
                        goToData.longitude = floatValue
                        adapter?.longitude = floatValue
                    }
                    GoToFloatValueType.Latitude -> {
                        goToData.latitude = floatValue
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
                        goToData.distance = doubleValue
                        adapter?.distance = doubleValue
                    }
                }
                adapter?.reload()
                adapter?.notifyDataSetChanged()
            }
        }, chooseObjectCallback = { _ ->
            listener?.onEditGoToObject(goToData)
        }, chooseUnitCallback = { current ->
            val ac = activity ?: return@GoToInputRecyclerViewAdapter
            ac.showOptions("", distanceUnits.map { value -> CelestiaString(value.name, "") }.toTypedArray(),) { newIndex ->
                val unit = distanceUnits[newIndex]
                adapter?.unit = unit
                goToData.distanceUnit = unit
                adapter?.reload()
                adapter?.notifyDataSetChanged()
            }

        }, proceedCallback = {
            adapter?.let {
                listener?.onGoToObject(goToData)
            }
        })

        adapter?.objectName = goToData.objectName
        adapter?.longitude = goToData.longitude
        adapter?.latitude = goToData.latitude
        adapter?.distance = goToData.distance
        adapter?.unit = goToData.distanceUnit
        adapter?.reload()

        recView.adapter = adapter
        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(ARG_DATA, goToData)

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

        private const val LONGITUDE_TAG = "longitude"
        private const val LATITUDE_TAG = "latitude"
        private const val OBJECT_TAG = "object"
        private const val DISTANCE_TAG = "distance"
        private const val UNIT_TAG = "unit"

        @JvmStatic
        fun newInstance(goToData: GoToData) =
            GoToInputFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_DATA, goToData)
                }
            }
    }
}
