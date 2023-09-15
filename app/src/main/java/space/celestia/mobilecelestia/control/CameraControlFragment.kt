/*
 * CameraControlFragment.kt
 *
 * Copyright (C) 2001-2023, Celestia Development Team
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.dimensionResource
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.compose.Footer
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.compose.Stepper
import space.celestia.mobilecelestia.compose.TextRow
import space.celestia.mobilecelestia.utils.CelestiaString

enum class CameraControlAction(val value: Int) {
    Pitch0(32), Pitch1(26), Yaw0(28), Yaw1(30), Roll0(31), Roll1(33), Reverse(-1), ZoomIn(5), ZoomOut(6);
}

class CameraControlFragment : NavigationFragment.SubFragment() {
    private var listener: Listener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Mdc3Theme {
                    MainScreen()
                }
            }
        }
    }

    @Composable
    private fun StepperRow(name: String, minusAction: CameraControlAction, plusAction: CameraControlAction, modifier: Modifier = Modifier) {
        Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = name)
            Stepper(touchDown = { minus ->
                listener?.onCameraActionStepperTouchDown(if (minus) minusAction else plusAction)
            }, touchUp = { minus ->
                listener?.onCameraActionStepperTouchUp(if (minus) minusAction else plusAction)
            })
        }
    }

    @Composable
    private fun MainScreen() {
        val internalViewModifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal),
                vertical = dimensionResource(id = R.dimen.common_page_medium_gap_vertical),
            )
        Column(modifier = Modifier
            .verticalScroll(state = rememberScrollState(), enabled = true)
            .systemBarsPadding()) {
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
            StepperRow(name = CelestiaString("Pitch", ""), minusAction = CameraControlAction.Pitch0, plusAction = CameraControlAction.Pitch1, modifier = internalViewModifier)
            StepperRow(name = CelestiaString("Yaw", ""), minusAction = CameraControlAction.Yaw0, plusAction = CameraControlAction.Yaw1, modifier = internalViewModifier)
            StepperRow(name = CelestiaString("Roll", ""), minusAction = CameraControlAction.Roll0, plusAction = CameraControlAction.Roll1, modifier = internalViewModifier)
            Footer(text = CelestiaString("Long press on stepper to change orientation.", ""))
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
            StepperRow(name = CelestiaString("Zoom (Distance)", ""), minusAction = CameraControlAction.ZoomOut, plusAction = CameraControlAction.ZoomIn, modifier = internalViewModifier)
            Footer(text = CelestiaString("Long press on stepper to zoom in/out.", ""))
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
            TextRow(primaryText = CelestiaString("Flight Mode", ""), accessoryResource = R.drawable.accessory_full_disclosure, modifier = Modifier.clickable(onClick = {
                listener?.onCameraControlObserverModeClicked()
            }))
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
            FilledTonalButton(modifier = internalViewModifier, onClick = {
                listener?.onCameraActionClicked(CameraControlAction.Reverse)
            }) {
                Text(text = CelestiaString("Reverse Direction", ""))
            }
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_tall)))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        fun onCameraControlObserverModeClicked()
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            CameraControlFragment()
    }
}
