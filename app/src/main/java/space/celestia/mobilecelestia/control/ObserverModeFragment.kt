package space.celestia.mobilecelestia.control

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import space.celestia.celestia.AppCore
import space.celestia.celestia.Observer
import space.celestia.celestia.Selection
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.CelestiaExecutor
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.compose.FooterLink
import space.celestia.mobilecelestia.compose.Header
import space.celestia.mobilecelestia.compose.Mdc3Theme
import space.celestia.mobilecelestia.compose.ObjectNameAutoComplete
import space.celestia.mobilecelestia.compose.OptionSelect
import space.celestia.mobilecelestia.utils.CelestiaString
import javax.inject.Inject

@AndroidEntryPoint
class ObserverModeFragment: NavigationFragment.SubFragment() {
    @Inject
    lateinit var appCore: AppCore

    @Inject
    lateinit var executor: CelestiaExecutor

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
    private fun MainScreen() {
        val internalViewModifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(id = R.dimen.list_item_medium_margin_horizontal),
                vertical = dimensionResource(id = R.dimen.common_page_medium_gap_vertical),
            )
        var referenceObjectName by remember {
            mutableStateOf("")
        }
        var referenceObjectPath by remember {
            mutableStateOf("")
        }
        var targetObjectName by remember {
            mutableStateOf("")
        }
        var selectedCoordinateIndex by remember {
            mutableStateOf(0)
        }
        var targetObjectPath by remember {
            mutableStateOf("")
        }
        val selectedCoordinateSystem = coordinateSystems[selectedCoordinateIndex].first
        val nestedScrollInterop = rememberNestedScrollInteropConnection()
        Column(modifier = Modifier
            .nestedScroll(nestedScrollInterop)
            .verticalScroll(state = rememberScrollState(), enabled = true)
            .systemBarsPadding()) {
            Header(text = CelestiaString("Coordinate System", ""))
            OptionSelect(options = coordinateSystems.map { it.second }, selectedIndex = selectedCoordinateIndex, selectionChange = {
                selectedCoordinateIndex = it
            }, modifier = internalViewModifier)

            if (selectedCoordinateSystem != Observer.COORDINATE_SYSTEM_UNIVERSAL) {
                Header(text = CelestiaString("Reference Object", ""))
                ObjectNameAutoComplete(executor = executor, core = appCore, name = referenceObjectName, path = referenceObjectPath, inputUpdated = {
                    referenceObjectName = it
                }, objectPathUpdated = {
                    referenceObjectPath = it
                }, modifier = internalViewModifier)
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
            }

            if (selectedCoordinateSystem == Observer.COORDINATE_SYSTEM_PHASE_LOCK) {
                Header(text = CelestiaString("Target Object", ""))
                ObjectNameAutoComplete(executor = executor, core = appCore, name = targetObjectName, path = targetObjectPath, inputUpdated = {
                    targetObjectName = it
                }, objectPathUpdated = {
                    targetObjectPath = it
                }, modifier = internalViewModifier)
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_short)))
            }

            val infoText = CelestiaString("Flight mode decides how you move around in Celestia. Learn more…", "")
            val infoLinkText = CelestiaString("Learn more…", "")
            FooterLink(text = infoText, linkText = infoLinkText, link = "https://celestia.mobi/help/flight-mode?lang=${AppCore.getLanguage()}", action = { link ->
                listener?.onObserverModeLearnMoreClicked(link)
            })

            FilledTonalButton(modifier = internalViewModifier, onClick = {
                applyObserverMode(referenceObjectPath = referenceObjectPath, targetObjectPath = targetObjectPath, coordinateSystem = selectedCoordinateSystem)
            }) {
                Text(text = CelestiaString("OK", ""))
            }
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.list_spacing_tall)))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = CelestiaString("Flight Mode", "")
    }

    private fun applyObserverMode(referenceObjectPath: String, targetObjectPath: String, coordinateSystem: Int) = lifecycleScope.launch(executor.asCoroutineDispatcher()) {
        val ref = if (referenceObjectPath.isEmpty()) Selection() else appCore.simulation.findObject(referenceObjectPath)
        val target = if (targetObjectPath.isEmpty()) Selection() else appCore.simulation.findObject(targetObjectPath)
        appCore.simulation.activeObserver.setFrame(coordinateSystem, ref, target)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement ObserverModeFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface Listener {
        fun onObserverModeLearnMoreClicked(link: String)
    }

    companion object {
        private val coordinateSystems = listOf(
            Pair(Observer.COORDINATE_SYSTEM_UNIVERSAL, CelestiaString("Free Flight", "")),
            Pair(Observer.COORDINATE_SYSTEM_ECLIPTICAL, CelestiaString("Follow", "")),
            Pair(Observer.COORDINATE_SYSTEM_BODY_FIXED, CelestiaString("Sync Orbit", "")),
            Pair(Observer.COORDINATE_SYSTEM_PHASE_LOCK, CelestiaString("Phase Lock", "")),
            Pair(Observer.COORDINATE_SYSTEM_CHASE, CelestiaString("Chase", "")),
        )

        fun newInstance(): ObserverModeFragment {
            return ObserverModeFragment()
        }
    }
}