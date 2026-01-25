package space.celestia.mobilecelestia.control

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import space.celestia.celestia.Observer
import space.celestia.celestia.Selection
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.FooterLink
import space.celestia.mobilecelestia.compose.Header
import space.celestia.mobilecelestia.compose.ObjectNameAutoComplete
import space.celestia.mobilecelestia.compose.OptionSelect
import space.celestia.mobilecelestia.control.viewmodel.CameraControlViewModel
import space.celestia.mobilecelestia.utils.CelestiaString

@Composable
fun ObserverModeScreen(paddingValues: PaddingValues, observerModeLearnMoreClicked: (String, Boolean) -> Unit) {
    val viewModel: CameraControlViewModel = hiltViewModel()
    val internalViewModifier = Modifier
        .fillMaxWidth()
        .padding(
            horizontal = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal),
            vertical = dimensionResource(id = R.dimen.common_page_medium_gap_vertical),
        )
    var referenceObjectName by rememberSaveable {
        mutableStateOf("")
    }
    var referenceObject by rememberSaveable {
        mutableStateOf(Selection())
    }
    var targetObjectName by rememberSaveable {
        mutableStateOf("")
    }
    var selectedCoordinateIndex by rememberSaveable {
        mutableIntStateOf(0)
    }
    var targetObject by rememberSaveable {
        mutableStateOf(Selection())
    }
    val scope = rememberCoroutineScope()
    val selectedCoordinateSystem = CameraControlViewModel.coordinateSystems[selectedCoordinateIndex].first
    val nestedScrollInterop = rememberNestedScrollInteropConnection()
    val direction = LocalLayoutDirection.current
    val contentPadding = PaddingValues(
        start = paddingValues.calculateStartPadding(direction),
        top = dimensionResource(id = R.dimen.list_spacing_short) + paddingValues.calculateTopPadding(),
        end = paddingValues.calculateEndPadding(direction),
        bottom = dimensionResource(id = R.dimen.list_spacing_tall) + paddingValues.calculateBottomPadding(),
    )
    Column(modifier = Modifier
        .nestedScroll(nestedScrollInterop)
        .verticalScroll(state = rememberScrollState(), enabled = true)
        .padding(contentPadding)) {
        Header(text = CelestiaString("Coordinate System", "Used in Flight Mode"))
        OptionSelect(options =  CameraControlViewModel.coordinateSystems.map { it.second }, selectedIndex = selectedCoordinateIndex, selectionChange = {
            selectedCoordinateIndex = it
        }, modifier = internalViewModifier)

        if (selectedCoordinateSystem != Observer.COORDINATE_SYSTEM_UNIVERSAL) {
            Header(text = CelestiaString("Reference Object", "Used in Flight Mode"))
            ObjectNameAutoComplete(executor = viewModel.executor, core = viewModel.appCore, name = referenceObjectName, selection = referenceObject, inputUpdated = {
                referenceObjectName = it
            }, selectionUpdated = {
                referenceObject = it
            }, modifier = internalViewModifier)
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
        }

        if (selectedCoordinateSystem == Observer.COORDINATE_SYSTEM_PHASE_LOCK) {
            Header(text = CelestiaString("Target Object", "Used in Flight Mode"))
            ObjectNameAutoComplete(executor = viewModel.executor, core = viewModel.appCore, name = targetObjectName, selection = targetObject, inputUpdated = {
                targetObjectName = it
            }, selectionUpdated = {
                targetObject = it
            }, modifier = internalViewModifier)
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
        }

        val infoText = CelestiaString("Flight mode decides how you move around in Celestia. Learn more…", "")
        val infoLinkText = CelestiaString("Learn more…", "Text for the link in Flight mode decides how you move around in Celestia. Learn more…")
        FooterLink(text = infoText, linkText = infoLinkText, link = "https://celestia.mobi/help/flight-mode", action = { link ->
            observerModeLearnMoreClicked(link, true)
        })

        FilledTonalButton(modifier = internalViewModifier, onClick = {
            scope.launch(viewModel.executor.asCoroutineDispatcher()) {
                viewModel.appCore.simulation.activeObserver.setFrame(selectedCoordinateSystem, referenceObject, targetObject)
            }
        }) {
            Text(text = CelestiaString("OK", ""))
        }
    }
}