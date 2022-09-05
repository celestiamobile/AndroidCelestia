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
import android.widget.*
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import space.celestia.celestia.AppCore
import space.celestia.celestia.GoToLocation
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.getSerializableValue
import java.io.Serializable
import java.lang.ref.WeakReference
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class GoToInputFragment : NavigationFragment.SubFragment() {
    class GoToData(var objectName: String, var longitude: Float, var latitude: Float, var distance: Double, var distanceUnit: GoToLocation.DistanceUnit) :
        Serializable

    private var listener: Listener? = null
    private val goToData: GoToData
        get() = requireNotNull(_goToData)
    private var _goToData: GoToData? = null

    private lateinit var distanceUnitEditText: AutoCompleteTextView
    private lateinit var distanceEditText: EditText
    private lateinit var longitudeEditText: EditText
    private lateinit var latitudeEditText: EditText
    private lateinit var objectNameEditText: AutoCompleteTextView

    private lateinit var numberFormat: NumberFormat

    @Inject
    lateinit var appCore: AppCore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (_goToData == null) {
            if (savedInstanceState != null) {
                _goToData = savedInstanceState.getSerializableValue(ARG_DATA, GoToData::class.java)
            } else {
                arguments?.let {
                    _goToData = it.getSerializableValue(ARG_DATA, GoToData::class.java)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_go_to, container, false)

        numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())
        numberFormat.maximumFractionDigits = 2

        val objectHeaderContainer = view.findViewById<View>(R.id.object_name_section_header)
        objectHeaderContainer.findViewById<TextView>(R.id.text).text = CelestiaString("Object", "")

        objectNameEditText = view.findViewById(R.id.object_name_text_view)
        val adapter = GoToSuggestionAdapter(requireActivity(), android.R.layout.simple_dropdown_item_1line, appCore)
        objectNameEditText.threshold = 1
        objectNameEditText.setAdapter(adapter)

        val coordinateHeaderContainer = view.findViewById<View>(R.id.coordinate_section_header)
        coordinateHeaderContainer.findViewById<TextView>(R.id.text).text = CelestiaString("Coordinates", "")
        longitudeEditText = view.findViewById(R.id.longitude_text_view)
        view.findViewById<TextInputLayout>(R.id.longitude_text_container).hint = CelestiaString("Longitude", "")

        latitudeEditText = view.findViewById(R.id.latitude_text_view)
        view.findViewById<TextInputLayout>(R.id.latitude_text_container).hint = CelestiaString("Latitude", "")

        val distanceHeaderContainer = view.findViewById<View>(R.id.distance_section_header)
        distanceHeaderContainer.findViewById<TextView>(R.id.text).text = CelestiaString("Distance", "")

        distanceEditText = view.findViewById(R.id.distance_text_view)

        distanceUnitEditText = view.findViewById(R.id.unit_text_view)
        val unitAdapter = object: ArrayAdapter<String>(requireActivity(), android.R.layout.simple_dropdown_item_1line) {
            override fun getCount(): Int {
                return distanceUnits.size
            }

            override fun getItem(position: Int): String {
                return  CelestiaString(distanceUnits[position].name, "")
            }
        }
        distanceUnitEditText.setAdapter(unitAdapter)

        val goToButton = view.findViewById<Button>(R.id.go_to_button)
        goToButton.text = CelestiaString("Go", "")

        val weakSelf = WeakReference(this)
        goToButton.setOnClickListener {
            val self = weakSelf.get() ?: return@setOnClickListener
            self.listener?.onGoToObject(self.goToData)
        }

        distanceEditText.addTextChangedListener { newText ->
            val text = newText?.toString() ?: return@addTextChangedListener
            val self = weakSelf.get() ?: return@addTextChangedListener
            val result = convertToDoubleOrNull(text) ?: return@addTextChangedListener
            self.goToData.distance = result
        }

        longitudeEditText.addTextChangedListener { newText ->
            val text = newText?.toString() ?: return@addTextChangedListener
            val self = weakSelf.get() ?: return@addTextChangedListener
            val result = convertToDoubleOrNull(text) ?: return@addTextChangedListener
            self.goToData.longitude = result.toFloat()
        }

        latitudeEditText.addTextChangedListener { newText ->
            val text = newText?.toString() ?: return@addTextChangedListener
            val self = weakSelf.get() ?: return@addTextChangedListener
            val result = convertToDoubleOrNull(text) ?: return@addTextChangedListener
            self.goToData.latitude = result.toFloat()
        }

        objectNameEditText.addTextChangedListener { newText ->
            val self = weakSelf.get() ?: return@addTextChangedListener
            val result = newText?.toString() ?: return@addTextChangedListener
            self.goToData.objectName = result
        }

        distanceUnitEditText.setOnItemClickListener { _, _, position, _ ->
            val self = weakSelf.get() ?: return@setOnItemClickListener
            self.goToData.distanceUnit = distanceUnits[position]
        }
        reload()
        return view
    }

    private fun reload() {
        objectNameEditText.setText(goToData.objectName)
        distanceUnitEditText.setText(CelestiaString(goToData.distanceUnit.name, ""))
        longitudeEditText.setText(numberFormat.format(goToData.longitude))
        latitudeEditText.setText(numberFormat.format(goToData.latitude))
        distanceEditText.setText(numberFormat.format(goToData.distance))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = CelestiaString("Go to Object", "")
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

    private fun convertToDoubleOrNull(string: String): Double? {
        try {
            val value = numberFormat.parse(string)?.toDouble()
            if (value != null)
                return value
        } catch(ignored: Throwable) {}
        // Try again with default decimal separator
        return string.toDoubleOrNull()
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    fun updateObjectName(name: String) {
        goToData.objectName = name
    }

    interface Listener {
        fun onGoToObject(goToData: GoToData)
    }

    companion object {
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
