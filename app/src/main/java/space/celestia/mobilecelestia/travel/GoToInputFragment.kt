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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.dimensionResource
import com.google.accompanist.themeadapter.material3.Mdc3Theme
import dagger.hilt.android.AndroidEntryPoint
import space.celestia.celestia.AppCore
import space.celestia.celestia.GoToLocation
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.CelestiaExecutor
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.control.Header
import space.celestia.mobilecelestia.control.ObjectNameAutoComplete
import space.celestia.mobilecelestia.control.OptionSelect
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.getSerializableValue
import java.io.Serializable
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

    private lateinit var numberFormat: NumberFormat

    @Inject
    lateinit var executor: CelestiaExecutor

    @Inject
    lateinit var appCore: AppCore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (_goToData == null) {
            arguments?.let {
                _goToData = it.getSerializableValue(ARG_DATA, GoToData::class.java)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())
        numberFormat.maximumFractionDigits = 2

        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Mdc3Theme {
                    MainScreen(goToData)
                }
            }
        }
    }

    @Composable
    fun MainScreen(initialData: GoToData) {
        var objectName by remember {
            mutableStateOf(initialData.objectName)
        }
        var longitudeString by rememberSaveable {
            mutableStateOf(numberFormat.format(initialData.longitude))
        }
        var latitudeString by rememberSaveable {
            mutableStateOf(numberFormat.format(initialData.latitude))
        }
        var distanceString by rememberSaveable {
            mutableStateOf(numberFormat.format(initialData.distance))
        }
        var distanceUnit by rememberSaveable {
            mutableStateOf(initialData.distanceUnit)
        }
        val textViewModifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal),
                vertical = dimensionResource(id = R.dimen.common_page_medium_gap_vertical),
            )
        Column(modifier = Modifier
            .verticalScroll(state = rememberScrollState(), enabled = true)
            .systemBarsPadding()) {
            Header(text = CelestiaString("Object", ""))
            ObjectNameAutoComplete(executor = executor, core = appCore, name = objectName, modifier = textViewModifier) {
                objectName = it
            }
            Header(text = CelestiaString("Coordinates", ""))
            Row(modifier = textViewModifier, horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.list_item_gap_horizontal))) {
                val currentLongitudeValue = convertToDoubleOrNull(longitudeString)
                val currentLatitudeValue = convertToDoubleOrNull(latitudeString)
                OutlinedTextField(value = latitudeString, label = { Text(text = CelestiaString("Latitude", "")) }, onValueChange = {
                    latitudeString = it
                }, isError = currentLatitudeValue == null || currentLatitudeValue < -90.0 || currentLatitudeValue > 90.0, modifier = Modifier.weight(1.0f))
                OutlinedTextField(value = longitudeString, label = { Text(text = CelestiaString("Longitude", "")) }, onValueChange = {
                    longitudeString = it
                }, isError = currentLongitudeValue == null || currentLongitudeValue < -180.0 || currentLongitudeValue > 180.0, modifier = Modifier.weight(1.0f))
            }
            Header(text = CelestiaString("Distance", ""))
            Row(modifier = textViewModifier, horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.list_item_gap_horizontal))) {
                OutlinedTextField(value = distanceString, onValueChange = {
                    distanceString = it
                }, isError = convertToDoubleOrNull(distanceString) == null, modifier = Modifier.weight(1.0f))
                OptionSelect(options = distanceUnits.map { CelestiaString(it.name, "") }, selectedIndex = distanceUnits.indexOf(distanceUnit) , selectionChange = {
                    distanceUnit = distanceUnits[it]
                })
            }
            FilledTonalButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = dimensionResource(id = R.dimen.common_page_medium_margin_horizontal),
                        top = dimensionResource(id = R.dimen.common_page_medium_gap_vertical),
                        end = dimensionResource(id = R.dimen.common_page_medium_margin_horizontal),
                        bottom = dimensionResource(id = R.dimen.common_page_medium_margin_vertical)
                    ),
                onClick = {
                    val longitude = convertToDoubleOrNull(longitudeString) ?: return@FilledTonalButton
                    val latitude = convertToDoubleOrNull(latitudeString) ?: return@FilledTonalButton
                    val distance = convertToDoubleOrNull(distanceString) ?: return@FilledTonalButton
                    listener?.onGoToObject(GoToData(objectName, longitude = longitude.toFloat(), latitude = latitude.toFloat(), distance = distance, distanceUnit = distanceUnit))
                }
            ) {
                Text(text = CelestiaString("Go", ""))
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
