// CelestiaFragment.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.celestia

import android.animation.ObjectAnimator
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.animation.doOnEnd
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import space.celestia.celestia.AppCore
import space.celestia.celestia.Renderer
import space.celestia.celestia.Selection
import space.celestia.celestiafoundation.utils.showToast
import space.celestia.celestiaui.di.AppSettings
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.EdgeInsets
import space.celestia.mobilecelestia.common.RoundedCorners
import space.celestia.mobilecelestia.common.SHEET_MAX_FULL_WIDTH_DP
import space.celestia.celestiaui.purchase.PurchaseManager
import space.celestia.celestiaui.settings.ToolbarAction
import space.celestia.celestiaui.settings.toolbarItems
import space.celestia.celestiaui.utils.AlertResult
import space.celestia.celestiaui.utils.CelestiaString
import space.celestia.celestiaui.utils.PreferenceManager
import space.celestia.celestiaui.utils.showAlert
import space.celestia.celestiaui.utils.showAlertAsync
import space.celestia.mobilecelestia.celestia.viewmodel.RendererSettings
import java.lang.ref.WeakReference
import java.util.Timer
import java.util.concurrent.Executor
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer

@AndroidEntryPoint
class CelestiaFragment: Fragment(), CelestiaControlView.Listener, AppCore.FatalErrorHandler, AppCore.SystemAccessHandler {

    @Inject
    lateinit var appCore: AppCore
    @Inject
    lateinit var renderer: Renderer
    @Inject
    lateinit var executor: Executor
    @AppSettings
    @Inject
    lateinit var appSettings: PreferenceManager
    @Inject
    lateinit var purchaseManager: PurchaseManager
    @Inject
    lateinit var rendererSettings: RendererSettings

    // MARK: Interaction
    private lateinit var interactionView: FrameLayout
    private var viewInteraction: CelestiaInteraction? = null

    // Parameters for child fragment
    private var pathToLoad: String? = null
    private var cfgToLoad: String? = null
    private var addonDirsToLoad: List<String> = listOf()
    private lateinit var languageOverride: String

    // MARK: Celestia
    private var savedInsets = EdgeInsets()
    // Used by the compose renderer path (featureFlags.composeSurfaceV2) to drive RendererScreen.
    private var safeAreaInsetsState by mutableStateOf(EdgeInsets())
    private val frameRateOptionEvents = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    private val reapplyContentScaleEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private var isControlViewVisible = true
    private var hideAnimator: ObjectAnimator? = null
    private var showAnimator: ObjectAnimator? = null

    private var zoomTimer: Timer? = null

    private var loadSuccess = false
    private var interactionMode = CelestiaInteraction.InteractionMode.Object
    private lateinit var controlView: CelestiaControlView
    private lateinit var controlViewContainer: FrameLayout

    interface Listener {
        fun celestiaFragmentDidRequestActionMenu()
        fun celestiaFragmentDidRequestGoTo()
        fun celestiaFragmentDidRequestObjectInfo()
        fun celestiaFragmentDidRequestSearch()
        fun celestiaFragmentDidRequestObjectInfo(selection: Selection)
        fun celestiaFragmentLoadingFromFallback()
        fun celestiaFragmentCanAcceptKeyEvents(): Boolean
        fun celestiaFragmentInteractionViewReady(view: View)
    }

    private var listener: Listener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            pathToLoad = it.getString(ARG_DATA_DIR)
            cfgToLoad = it.getString(ARG_CFG_FILE)
            addonDirsToLoad = it.getStringArrayList(ARG_ADDON_DIR) ?: listOf()
            languageOverride = it.getString(ARG_LANG_OVERRIDE, "en")
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(ARG_INTERACTION_MODE)) {
            interactionMode = CelestiaInteraction.InteractionMode.fromButton(savedInstanceState.getInt(ARG_INTERACTION_MODE))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(ARG_INTERACTION_MODE, interactionMode.button)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_celestia, container, false)
        controlView = view.findViewById(R.id.control_view)
        controlViewContainer = view.findViewById(R.id.active_control_view_container)
        controlView.listener = this

        controlViewContainer.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        // Create and add the renderer fragment / compose host
        val data = pathToLoad
        val cfg = cfgToLoad
        if (data != null && cfg != null) {
            val container = view.findViewById<FrameLayout>(R.id.celestia_renderer_container)
            val composeView = ComposeView(requireActivity()).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    RendererScreen(
                        pathToLoad = data,
                        cfgToLoad = cfg,
                        addonDirsToLoad = addonDirsToLoad,
                        languageOverride = languageOverride,
                        safeAreaInsets = safeAreaInsetsState,
                        frameRateOptionEvents = frameRateOptionEvents,
                        reapplyContentScaleEvents = reapplyContentScaleEvents,
                        celestiaRendererReady = { celestiaRendererReady() },
                        celestiaRendererLoadingFromFallback = { celestiaRendererLoadingFromFallback() },
                    )
                }
            }
            container.addView(composeView)
        }

        interactionView = view.findViewById(R.id.interaction_view)

        val weakSelf = WeakReference(this)
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val self = weakSelf.get() ?: return@setOnApplyWindowInsetsListener insets
            val hasRegularHorizontalSpace =  self.resources.configuration.screenWidthDp > SHEET_MAX_FULL_WIDTH_DP
            val safeInsets = EdgeInsets(
                insets,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) RoundedCorners(insets) else RoundedCorners(0, 0, 0, 0),
                hasRegularHorizontalSpace
            )
            handleInsetsChanged(safeInsets)
            return@setOnApplyWindowInsetsListener insets
        }
        return view
    }

    override fun onDestroyView() {
        appCore.setFatalErrorHandler(null)
        appCore.setSystemAccessHandler(null)
        super.onDestroyView()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement CelestiaFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()

        listener = null
        zoomTimer?.cancel()
        zoomTimer = null
    }

    fun updateFrameRateOption(newFrameRateOption: Int) {
        frameRateOptionEvents.tryEmit(newFrameRateOption)
    }

    fun reapplyContentScale() {
        reapplyContentScaleEvents.tryEmit(Unit)
    }

    private fun handleInsetsChanged(newInsets: EdgeInsets) {
        savedInsets = newInsets
        safeAreaInsetsState = newInsets
    }

    // Callback from CelestiaRendererFragment when rendering is ready
    private fun celestiaRendererLoadingFromFallback() {
        listener?.celestiaFragmentLoadingFromFallback()
    }

    private fun celestiaRendererReady() {
        setUpInteractions()
    }

    private fun setUpInteractions() {
        val container = interactionView
        controlViewContainer.isVisible = true

        // Set up control buttons
        val buttonMap = hashMapOf(
            ToolbarAction.Mode to CelestiaControlButton.Toggle(space.celestia.celestiaui.R.drawable.control_mode_combined, CelestiaControlAction.ToggleModeToObject, CelestiaControlAction.ToggleModeToCamera, contentDescription = CelestiaString("Toggle Interaction Mode", "Touch interaction mode"), interactionMode == CelestiaInteraction.InteractionMode.Camera),
            ToolbarAction.ZoomIn to CelestiaControlButton.Press(space.celestia.celestiaui.R.drawable.control_zoom_in, CelestiaControlAction.ZoomIn, CelestiaString("Zoom In", "")),
            ToolbarAction.ZoomOut to CelestiaControlButton.Press(space.celestia.celestiaui.R.drawable.control_zoom_out, CelestiaControlAction.ZoomOut, CelestiaString("Zoom Out", "")),
            ToolbarAction.Info to CelestiaControlButton.Tap(space.celestia.celestiaui.R.drawable.control_info, CelestiaControlAction.Info, CelestiaString("Get Info", "Action for getting info about current selected object")),
            ToolbarAction.Search to CelestiaControlButton.Tap(space.celestia.celestiaui.R.drawable.control_search, CelestiaControlAction.Search, CelestiaString("Search", "")),
            ToolbarAction.Menu to CelestiaControlButton.Tap(space.celestia.celestiaui.R.drawable.control_action_menu, CelestiaControlAction.ShowMenu, CelestiaString("Menu", "Menu button")),
            ToolbarAction.Hide to CelestiaControlButton.Tap(space.celestia.celestiaui.R.drawable.control_close, CelestiaControlAction.Hide, CelestiaString("Hide", "Action to hide the tool overlay")),
            ToolbarAction.Go to CelestiaControlButton.Tap(space.celestia.celestiaui.R.drawable.control_go, CelestiaControlAction.Go, CelestiaString("Go", "Go to an object"))
        )
        val hasCelestiaPlus = purchaseManager.canUseInAppPurchase() && purchaseManager.purchaseToken() != null
        val actions = ArrayList(if (hasCelestiaPlus) appSettings.toolbarItems ?: ToolbarAction.defaultItems else ToolbarAction.defaultItems)
        if (!actions.contains(ToolbarAction.Menu))
            actions.add(ToolbarAction.Menu)
        controlView.buttons = actions.mapNotNull { buttonMap[it] }

        val weakSelf = WeakReference(this)
        val interaction = CelestiaInteraction(requireActivity(), appCore, renderer, executor, interactionMode, appSettings, rendererSettings, canAcceptKeyEvents = {
            val self = weakSelf.get() ?: return@CelestiaInteraction false
            return@CelestiaInteraction self.listener?.celestiaFragmentCanAcceptKeyEvents() ?: false
        }, showMenu = {
            val self = weakSelf.get() ?: return@CelestiaInteraction
            self.listener?.celestiaFragmentDidRequestActionMenu()
        })
        viewInteraction = interaction
        interaction.isReady = true

        // All interaction listeners on the container - no need for GL view reference
        @Suppress("ClickableViewAccessibility")
        container.setOnTouchListener { v, event ->
            showControlViewIfNeeded()
            interaction.onTouch(v, event)
        }

        // Make container focusable for keyboard/gamepad input
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            container.defaultFocusHighlightEnabled = false
            container.isFocusable = true
            container.isFocusableInTouchMode = true
            container.requestFocus()
        }
        container.setOnKeyListener(interaction)
        container.setOnGenericMotionListener(interaction)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pointerCaptureListener = interaction.pointerCaptureListener as? View.OnCapturedPointerListener
            if (pointerCaptureListener != null) {
                container.setOnCapturedPointerListener(pointerCaptureListener)
            }
        }
        container.setOnHoverListener(interaction)

        listener?.celestiaFragmentInteractionViewReady(container)

        lifecycleScope.launch(executor.asCoroutineDispatcher()) {
            appCore.setFatalErrorHandler(this@CelestiaFragment)
            appCore.setSystemAccessHandler(this@CelestiaFragment)
        }

        loadSuccess = true

        Log.d(TAG, "Ready to display")

        handleInsetsChanged(savedInsets)
    }

    // Actions
    override fun didTapAction(action: CelestiaControlAction) {
        if (!isControlViewVisible) return

        when (action) {
            CelestiaControlAction.ShowMenu -> {
                listener?.celestiaFragmentDidRequestActionMenu()
            }
            CelestiaControlAction.Info -> {
                listener?.celestiaFragmentDidRequestObjectInfo()
            }
            CelestiaControlAction.Search -> {
                listener?.celestiaFragmentDidRequestSearch()
            }
            CelestiaControlAction.Hide -> {
                hideControlViewIfNeeded()
            }
            CelestiaControlAction.Show -> {
                showControlViewIfNeeded()
            }
            CelestiaControlAction.Go -> {
                listener?.celestiaFragmentDidRequestGoTo()
            }
            else -> {}
        }
    }

    private fun hideControlView() {
        val controlView = controlViewContainer
        if (hideAnimator != null) return
        showAnimator?.let {
            it.cancel()
            showAnimator = null
        }

        val animator = ObjectAnimator.ofFloat(controlView, View.ALPHA, 1f, 0f)
        animator.duration = 200
        animator.doOnEnd {
            hideAnimator = null
            isControlViewVisible = false
            controlView.isVisible = false
        }
        animator.start()
        hideAnimator = animator
    }

    private fun showControlView() {
        val controlView = controlViewContainer
        if (showAnimator != null) return
        hideAnimator?.let {
            it.cancel()
            hideAnimator = null
        }

        controlView.isVisible = true
        val animator = ObjectAnimator.ofFloat(controlView, View.ALPHA, 0f, 1f)
        animator.duration = 200
        animator.doOnEnd {
            showAnimator = null
            isControlViewVisible = true
        }
        animator.start()
        showAnimator = animator
    }

    private fun showControlViewIfNeeded() {
        if (!isControlViewVisible) {
            showControlView()
        }
    }

    private fun hideControlViewIfNeeded() {
        if (isControlViewVisible) {
            hideControlView()
        }
    }

    override fun didToggleToMode(action: CelestiaControlAction) {
        if (!isControlViewVisible) return

        when (action) {
            CelestiaControlAction.ToggleModeToCamera -> {
                interactionMode = CelestiaInteraction.InteractionMode.Camera
                viewInteraction?.setInteractionMode(CelestiaInteraction.InteractionMode.Camera)
                requireActivity().showToast(CelestiaString("Switched to camera mode", "Move/zoom camera FOV"), Toast.LENGTH_SHORT)
            }
            CelestiaControlAction.ToggleModeToObject -> {
                interactionMode = CelestiaInteraction.InteractionMode.Object
                viewInteraction?.setInteractionMode(CelestiaInteraction.InteractionMode.Object)
                requireActivity().showToast(CelestiaString("Switched to object mode", "Move/zoom on an object"), Toast.LENGTH_SHORT)
            }
            else -> {}
        }
    }

    override fun didStartPressingAction(action: CelestiaControlAction) {
        if (!isControlViewVisible) return
        val interaction = viewInteraction ?: return

        when (action) {
            CelestiaControlAction.ZoomIn -> { interaction.zoomMode = CelestiaInteraction.ZoomMode.In; interaction.callZoom() }
            CelestiaControlAction.ZoomOut -> { interaction.zoomMode = CelestiaInteraction.ZoomMode.Out; interaction.callZoom() }
            else -> {}
        }

        zoomTimer?.cancel()
        val weakSelf = WeakReference(this)
        zoomTimer = fixedRateTimer("zoom", false, 0, 100) {
            val self = weakSelf.get() ?: return@fixedRateTimer
            self.viewInteraction?.callZoom()
        }
    }

    override fun didEndPressingAction(action: CelestiaControlAction) {
        if (!isControlViewVisible) return

        zoomTimer?.cancel()
        zoomTimer = null
        viewInteraction?.zoomMode = null
    }

    override fun fatalError(message: String) {
        lifecycleScope.launch {
            val activity = this@CelestiaFragment.activity ?: return@launch
            activity.showAlert(title = CelestiaString("Fatal Error", "Error for fatal error alert title"), message = message)
        }
    }

    override fun requestSystemAccess(): Int {
        val weakSelf = WeakReference(this)
        return runBlocking(context = Dispatchers.Main) {
            val activity =
                weakSelf.get()?.activity ?: return@runBlocking AppCore.SYSTEM_ACCESS_UNKNOWN
            val result = activity.showAlertAsync(
                title = CelestiaString(
                    "Script System Access",
                    "Alert title for scripts requesting system access"
                ),
                message = CelestiaString(
                    "This script requests permission to read/write files and execute external programs. Allowing this can be dangerous.\nDo you trust the script and want to allow this?",
                    "Alert message for scripts requesting system access"
                ),
                showCancel = true
            )
            return@runBlocking when (result) {
                AlertResult.OK -> AppCore.SYSTEM_ACCESS_GRANTED
                AlertResult.Cancel -> AppCore.SYSTEM_ACCESS_DENIED
            }
        }
    }

    companion object {
        private const val ARG_DATA_DIR = "data"
        private const val ARG_CFG_FILE = "cfg"
        private const val ARG_ADDON_DIR = "addon"
        private const val ARG_INTERACTION_MODE = "interaction-mode"
        private const val ARG_LANG_OVERRIDE = "lang"
        private const val TAG = "CelestiaFragment"

        fun newInstance(data: String, cfg: String, addons: List<String>, languageOverride: String) =
            CelestiaFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_DATA_DIR, data)
                    putString(ARG_CFG_FILE, cfg)
                    putString(ARG_LANG_OVERRIDE, languageOverride)
                    putStringArrayList(ARG_ADDON_DIR, ArrayList(addons))
                }
            }
    }
}

fun EdgeInsets.scaleBy(factor: Float): EdgeInsets {
    return EdgeInsets(
        (left * factor).toInt(),
        (top * factor).toInt(),
        (right * factor).toInt(),
        (bottom * factor).toInt()
    )
}

fun AppCore.setSafeAreaInsets(insets: EdgeInsets) {
    setSafeAreaInsets(insets.left, insets.top, insets.right, insets.bottom)
}

data class RenderChanges(val scaling: Boolean = false, val safeArea: Boolean = false)

fun AppCore.updateContentScale(rendererSettings: RendererSettings, changes: RenderChanges) {
    if (changes.scaling) {
        screenDPI = (96 * rendererSettings.density * rendererSettings.scaleFactor).toInt()
        setPickTolerance(rendererSettings.pickSensitivity * rendererSettings.density * rendererSettings.scaleFactor)
        textScaleFactor = rendererSettings.fontScale
    }

    if (changes.scaling || changes.safeArea) {
        setSafeAreaInsets(rendererSettings.safeAreaInsets.scaleBy(rendererSettings.scaleFactor))
    }
}