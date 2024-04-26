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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import dagger.hilt.android.AndroidEntryPoint
import space.celestia.celestia.AppCore
import space.celestia.celestia.GoToLocation
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.CelestiaExecutor
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.compose.Header
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.compose.ObjectNameAutoComplete
import space.celestia.mobilecelestia.compose.OptionSelect
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.celestiafoundation.utils.getSerializableValue
import space.celestia.mobilecelestia.utils.toDoubleOrNull
import java.io.Serializable
import java.text.NumberFormat
import javax.inject.Inject

@AndroidEntryPoint
class GoToInputFragment : NavigationFragment.SubFragment() {
    class GoToData(var objectName: String, var objectPath: String, var longitude: Float, var latitude: Float, var distance: Double, var distanceUnit: GoToLocation.DistanceUnit) :
        Serializable

    private var listener: Listener? = null
    private val goToData: GoToData
        get() = requireNotNull(_goToData)
    private var _goToData: GoToData? = null

    private lateinit var displayNumberFormat: NumberFormat
    private lateinit var parseNumberFormat: NumberFormat

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
        displayNumberFormat = NumberFormat.getNumberInstance()
        displayNumberFormat.maximumFractionDigits = 2
        displayNumberFormat.isGroupingUsed = false
        parseNumberFormat = NumberFormat.getNumberInstance()
        parseNumberFormat.isGroupingUsed = false

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
        var objectPath by remember {
            mutableStateOf(initialData.objectPath)
        }
        var longitudeString by rememberSaveable {
            mutableStateOf(displayNumberFormat.format(initialData.longitude))
        }
        var latitudeString by rememberSaveable {
            mutableStateOf(displayNumberFormat.format(initialData.latitude))
        }
        var distanceString by rememberSaveable {
            mutableStateOf(displayNumberFormat.format(initialData.distance))
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
        val currentLongitudeValue = longitudeString.toDoubleOrNull(parseNumberFormat)
        val currentLatitudeValue = latitudeString.toDoubleOrNull(parseNumberFormat)
        val currentDistanceValue = distanceString.toDoubleOrNull(parseNumberFormat)
        val isLongitudeValid = currentLongitudeValue != null && currentLongitudeValue >= -180.0 && currentLongitudeValue <= 180.0
        val isLatitudeValid = currentLatitudeValue != null && currentLatitudeValue >= -90.0 && currentLatitudeValue <= 90.0
        val isDistanceValid = currentDistanceValue != null && currentDistanceValue >= 0.0
        val nestedScrollInterop = rememberNestedScrollInteropConnection()
        Column(modifier = Modifier
            .nestedScroll(nestedScrollInterop)
            .verticalScroll(state = rememberScrollState(), enabled = true)
            .systemBarsPadding()) {
            Header(text = CelestiaString("Object", "In eclipse finder, object to find eclipse with, or in go to"))
            ObjectNameAutoComplete(executor = executor, core = appCore, name = objectName, path = objectPath, modifier = textViewModifier, inputUpdated = {
                objectName = it
            }, objectPathUpdated = {
                objectPath = it
            })
            Header(text = CelestiaString("Coordinates", "Longitude and latitude (in Go to)"))
            Row(modifier = textViewModifier, horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.list_item_gap_horizontal))) {
                OutlinedTextField(value = latitudeString, label = { Text(text = CelestiaString("Latitude", "Coordinates")) }, onValueChange = {
                    latitudeString = it
                }, isError = !isLatitudeValid, modifier = Modifier.weight(1.0f))
                OutlinedTextField(value = longitudeString, label = { Text(text = CelestiaString("Longitude", "Coordinates")) }, onValueChange = {
                    longitudeString = it
                }, isError = !isLongitudeValid, modifier = Modifier.weight(1.0f))
            }
            Header(text = CelestiaString("Distance", "Distance to the object (in Go to)"))
            Row(modifier = textViewModifier, horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.list_item_gap_horizontal))) {
                OutlinedTextField(value = distanceString, onValueChange = {
                    distanceString = it
                }, isError = !isDistanceValid, modifier = Modifier.weight(1.0f))
                OptionSelect(options = distanceUnits.map {
                    when(it) {
                        GoToLocation.DistanceUnit.radii -> {
                            CelestiaString("radii", "In Go to, specify the distance based on the object radius")
                        }
                        GoToLocation.DistanceUnit.km -> {
                            CelestiaString("km", "Unit")
                        }
                        GoToLocation.DistanceUnit.au -> {
                            CelestiaString("au", "Astronomical unit")
                        }
                    }
                }, selectedIndex = distanceUnits.indexOf(distanceUnit) , selectionChange = {
                    distanceUnit = distanceUnits[it]
                })
            }
            FilledTonalButton(
                enabled = isLatitudeValid && isLongitudeValid && isDistanceValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = dimensionResource(id = R.dimen.common_page_medium_margin_horizontal),
                        top = dimensionResource(id = R.dimen.common_page_medium_gap_vertical),
                        end = dimensionResource(id = R.dimen.common_page_medium_margin_horizontal),
                        bottom = dimensionResource(id = R.dimen.common_page_medium_margin_vertical)
                    ),
                onClick = {
                    val longitude = currentLongitudeValue ?: return@FilledTonalButton
                    val latitude = currentLatitudeValue ?: return@FilledTonalButton
                    val distance = currentDistanceValue ?: return@FilledTonalButton
                    listener?.onGoToObject(GoToData(objectName, objectPath, longitude = longitude.toFloat(), latitude = latitude.toFloat(), distance = distance, distanceUnit = distanceUnit))
                }
            ) {
                Text(text = CelestiaString("Go", "Go to an object"))
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
