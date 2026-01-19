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
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.Display
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.runtime.snapshotFlow
import androidx.core.animation.doOnEnd
import androidx.core.view.MenuCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import space.celestia.celestia.AppCore
import space.celestia.celestia.Body
import space.celestia.celestia.BrowserItem
import space.celestia.celestia.Selection
import space.celestia.celestia.Universe
import space.celestia.celestia.Utils
import space.celestia.celestiafoundation.utils.showToast
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.CelestiaExecutor
import space.celestia.mobilecelestia.common.EdgeInsets
import space.celestia.mobilecelestia.common.RoundedCorners
import space.celestia.mobilecelestia.common.SheetLayout
import space.celestia.mobilecelestia.di.AppSettings
import space.celestia.mobilecelestia.info.model.CelestiaAction
import space.celestia.mobilecelestia.purchase.PurchaseManager
import space.celestia.mobilecelestia.purchase.ToolbarSettingFragment
import space.celestia.mobilecelestia.purchase.toolbarItems
import space.celestia.mobilecelestia.utils.AlertResult
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.PreferenceManager
import space.celestia.mobilecelestia.utils.showAlert
import space.celestia.mobilecelestia.utils.showAlertAsync
import java.lang.ref.WeakReference
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer

@AndroidEntryPoint
class CelestiaFragment: Fragment(), CelestiaControlView.Listener, CelestiaRendererFragment.Listener, AppCore.ContextMenuHandler, AppCore.FatalErrorHandler, AppCore.SystemAccessHandler, SensorEventListener {

    @Inject
    lateinit var appCore: AppCore
    @Inject
    lateinit var executor: CelestiaExecutor
    @AppSettings
    @Inject
    lateinit var appSettings: PreferenceManager
    @Inject
    lateinit var purchaseManager: PurchaseManager
    @Inject
    lateinit var sessionSettings: SessionSettings
    @Inject
    lateinit var rendererSettings: RendererSettings

    private lateinit var sensorManager: SensorManager
    private var gyroscope: Sensor? = null
    private var isGyroscopeActive = false
    private var lastRotationQuaternion: FloatArray? = null

    // MARK: Interaction
    private var rendererContainer: FrameLayout? = null
    private var viewInteraction: CelestiaInteraction? = null

    // Parameters for child fragment
    private var pathToLoad: String? = null
    private var cfgToLoad: String? = null
    private var addonDirsToLoad: List<String> = listOf()
    private lateinit var languageOverride: String

    // MARK: Celestia
    private var pendingTarget: Selection? = null
    private var browserItems: ArrayList<BrowserItem> = arrayListOf()
    private var savedInsets = EdgeInsets()

    private var isControlViewVisible = true
    private var hideAnimator: ObjectAnimator? = null
    private var showAnimator: ObjectAnimator? = null

    private var zoomTimer: Timer? = null

    private var loadSuccess = false
    private var interactionMode = CelestiaInteraction.InteractionMode.Object
    private lateinit var controlView: CelestiaControlView

    private var isContextMenuEnabled = true

    interface Listener {
        fun celestiaFragmentDidRequestActionMenu()
        fun celestiaFragmentDidRequestGoTo()
        fun celestiaFragmentDidRequestObjectInfo()
        fun celestiaFragmentDidRequestSearch()
        fun celestiaFragmentDidRequestObjectInfo(selection: Selection)
        fun celestiaFragmentLoadingFromFallback()
        fun celestiaFragmentCanAcceptKeyEvents(): Boolean
    }

    private var listener: Listener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize SensorManager and get the gyroscope sensor
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        arguments?.let {
            pathToLoad = it.getString(ARG_DATA_DIR)
            cfgToLoad = it.getString(ARG_CFG_FILE)
            addonDirsToLoad = it.getStringArrayList(ARG_ADDON_DIR) ?: listOf()
            languageOverride = it.getString(ARG_LANG_OVERRIDE, "en")
        }

        isContextMenuEnabled = appSettings[PreferenceManager.PredefinedKey.ContextMenu] != "false"

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
        controlView.listener = this

        val controlViewContainer = view.findViewById<FrameLayout>(R.id.active_control_view_container)
        controlViewContainer.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        // Create and add the renderer fragment
        if (savedInstanceState == null) {
            val data = pathToLoad
            val cfg = cfgToLoad
            if (data != null && cfg != null) {
                val rendererFragment = CelestiaRendererFragment.newInstance(
                    data, cfg, addonDirsToLoad, languageOverride
                )
                childFragmentManager.beginTransaction()
                    .replace(R.id.celestia_renderer_container, rendererFragment, TAG_RENDERER_FRAGMENT)
                    .commit()
            }
        }

        // Store reference to renderer container for touch handling
        rendererContainer = view.findViewById(R.id.celestia_renderer_container)

        val weakSelf = WeakReference(this)
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val self = weakSelf.get() ?: return@setOnApplyWindowInsetsListener insets
            val hasRegularHorizontalSpace =  self.resources.configuration.screenWidthDp > SheetLayout.sheetMaxFullWidthDp
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
        appCore.setContextMenuHandler(null)
        appCore.setFatalErrorHandler(null)
        appCore.setSystemAccessHandler(null)
        super.onDestroyView()
    }

    override fun onDestroy() {
        pendingTarget = null

        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()

        if (isGyroscopeActive) {
            sensorManager.unregisterListener(this, gyroscope)
            lastRotationQuaternion = null
            isGyroscopeActive = false // Mark as inactive
        }
    }

    override fun onResume() {
        super.onResume()

        if (sessionSettings.isGyroscopeEnabled) {
            updateGyroscope(true)
        }
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
        val rendererFragment = childFragmentManager.findFragmentByTag(TAG_RENDERER_FRAGMENT) as? CelestiaRendererFragment
        rendererFragment?.updateFrameRateOption(newFrameRateOption)
    }

    private fun handleInsetsChanged(newInsets: EdgeInsets) {
        savedInsets = newInsets
        val rendererFragment = childFragmentManager.findFragmentByTag(TAG_RENDERER_FRAGMENT) as? CelestiaRendererFragment
        rendererFragment?.handleInsetsChanged(newInsets)
    }

    // Callback from CelestiaRendererFragment when rendering is ready
    override fun celestiaRendererLoadingFromFallback() {
        listener?.celestiaFragmentLoadingFromFallback()
    }

    override fun celestiaRendererReady() {
        setUpInteractions()
    }

    private fun setUpInteractions() {
        val container = rendererContainer ?: return

        // Set up control buttons
        val buttonMap = hashMapOf(
            ToolbarSettingFragment.ToolbarAction.Mode to CelestiaControlButton.Toggle(R.drawable.control_mode_combined, CelestiaControlAction.ToggleModeToObject, CelestiaControlAction.ToggleModeToCamera, contentDescription = CelestiaString("Toggle Interaction Mode", "Touch interaction mode"), interactionMode == CelestiaInteraction.InteractionMode.Camera),
            ToolbarSettingFragment.ToolbarAction.ZoomIn to CelestiaControlButton.Press(R.drawable.control_zoom_in, CelestiaControlAction.ZoomIn, CelestiaString("Zoom In", "")),
            ToolbarSettingFragment.ToolbarAction.ZoomOut to CelestiaControlButton.Press(R.drawable.control_zoom_out, CelestiaControlAction.ZoomOut, CelestiaString("Zoom Out", "")),
            ToolbarSettingFragment.ToolbarAction.Info to CelestiaControlButton.Tap(R.drawable.control_info, CelestiaControlAction.Info, CelestiaString("Get Info", "Action for getting info about current selected object")),
            ToolbarSettingFragment.ToolbarAction.Search to CelestiaControlButton.Tap(R.drawable.control_search, CelestiaControlAction.Search, CelestiaString("Search", "")),
            ToolbarSettingFragment.ToolbarAction.Menu to CelestiaControlButton.Tap(R.drawable.control_action_menu, CelestiaControlAction.ShowMenu, CelestiaString("Menu", "Menu button")),
            ToolbarSettingFragment.ToolbarAction.Hide to CelestiaControlButton.Tap(R.drawable.control_close, CelestiaControlAction.Hide, CelestiaString("Hide", "Action to hide the tool overlay")),
            ToolbarSettingFragment.ToolbarAction.Go to CelestiaControlButton.Tap(R.drawable.control_go, CelestiaControlAction.Go, CelestiaString("Go", "Go to an object"))
        )
        val hasCelestiaPlus = purchaseManager.canUseInAppPurchase() && purchaseManager.purchaseToken() != null
        val actions = ArrayList(if (hasCelestiaPlus) appSettings.toolbarItems ?: ToolbarSettingFragment.ToolbarAction.defaultItems else ToolbarSettingFragment.ToolbarAction.defaultItems)
        if (!actions.contains(ToolbarSettingFragment.ToolbarAction.Menu))
            actions.add(ToolbarSettingFragment.ToolbarAction.Menu)
        controlView.buttons = actions.mapNotNull { buttonMap[it] }

        val weakSelf = WeakReference(this)
        val interaction = CelestiaInteraction(requireActivity(), appCore, executor, interactionMode, appSettings, rendererSettings, canAcceptKeyEvents = {
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

        // Register context menu on container
        if (isContextMenuEnabled) {
            registerForContextMenu(container)
            container.isContextClickable = true
        }

        lifecycleScope.launch(executor.asCoroutineDispatcher()) {
            if (isContextMenuEnabled)
                appCore.setContextMenuHandler(this@CelestiaFragment)
            appCore.setFatalErrorHandler(this@CelestiaFragment)
            appCore.setSystemAccessHandler(this@CelestiaFragment)
        }

        snapshotFlow { sessionSettings.isGyroscopeEnabled }
            .onEach { isEnabled ->
                updateGyroscope(isEnabled)
            }
            .launchIn(lifecycleScope)

        loadSuccess = true

        Log.d(TAG, "Ready to display")

        handleInsetsChanged(savedInsets)
    }

    private fun updateGyroscope(isEnabled: Boolean) {
        if (isEnabled) {
            if (!isGyroscopeActive) {
                // Register the listener only if it's not already active
                sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME)
                isGyroscopeActive = true
                Log.d(TAG, "Gyroscope enabled and listener registered.")
            }
        } else {
            if (isGyroscopeActive) {
                // Unregister the listener only if it's active
                sensorManager.unregisterListener(this, gyroscope)
                lastRotationQuaternion = null
                isGyroscopeActive = false
                Log.d(TAG, "Gyroscope disabled and listener unregistered.")
            }
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        val selection = pendingTarget ?: return

        browserItems.clear()

        fun createSubMenu(menu: Menu, browserItem: BrowserItem) {
            val obj = browserItem.`object`
            if (obj != null) {
                menu.add(GROUP_BROWSER_ITEM_GO, browserItems.size, Menu.NONE, CelestiaString("Go", "Go to an object"))
                browserItems.add(browserItem)
            }

            browserItem.children.withIndex().forEach {
                val subMenu = menu.addSubMenu(GROUP_BROWSER_ITEM, 0, Menu.NONE, browserItem.childNameAtIndex(it.index))
                createSubMenu(subMenu, it.value)
            }
            MenuCompat.setGroupDividerEnabled(menu, true)
        }

        menu.setHeaderTitle(appCore.simulation.universe.getNameForSelection(selection))
        menu.add(GROUP_GET_INFO, 0, Menu.NONE, CelestiaString("Get Info", "Action for getting info about current selected object"))

        CelestiaAction.allActions.withIndex().forEach {
            menu.add(GROUP_ACTION, it.index, Menu.NONE, it.value.title)
        }
        val obj = selection.`object`

        if (obj != null) {
            val browserItem = BrowserItem(
                appCore.simulation.universe.getNameForSelection(selection),
                null,
                obj,
                appCore.simulation.universe
            )
            for (child in browserItem.children) {
                val subMenu = menu.addSubMenu(GROUP_BROWSER_ITEM, 0, Menu.NONE, child.name)
                createSubMenu(subMenu, child)
            }
        }

        if (obj is Body) {
            val alternateSurfaces = obj.alternateSurfaceNames
            if (alternateSurfaces.isNotEmpty()) {
                val subMenu = menu.addSubMenu(GROUP_ALT_SURFACE_TOP, 0, Menu.NONE, CelestiaString("Alternate Surfaces", "Alternative textures to display"))
                subMenu.add(GROUP_ALT_SURFACE, 0, Menu.NONE, CelestiaString("Default", ""))
                alternateSurfaces.withIndex().forEach {
                    subMenu.add(GROUP_ALT_SURFACE, it.index + 1, Menu.NONE, it.value)
                }
            }
        }
        val markMenu = menu.addSubMenu(GROUP_MARK_TOP, 0, Menu.NONE, CelestiaString("Mark", "Mark an object"))
        val availableMarkers = getAvailableMarkers()
        availableMarkers.withIndex().forEach {
            markMenu.add(GROUP_MARK, it.index, Menu.NONE, it.value)
        }
        MenuCompat.setGroupDividerEnabled(menu, true)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val selection = pendingTarget ?: return true
        if (selection.isEmpty) return true
        val actions = CelestiaAction.allActions
        if (item.groupId == GROUP_ACTION) {
            if (item.itemId >= 0 && item.itemId < actions.size) {
                lifecycleScope.launch(executor.asCoroutineDispatcher()) {
                    appCore.simulation.selection = selection
                    appCore.charEnter(actions[item.itemId].value)
                }
            }
        } else if (item.groupId == GROUP_ALT_SURFACE) {
            val body = selection.body
            if (body != null) {
                lifecycleScope.launch(executor.asCoroutineDispatcher()) {
                    val alternateSurfaces = body.alternateSurfaceNames
                    if (item.itemId == 0) {
                        appCore.simulation.activeObserver.displayedSurface = ""
                    } else if (item.itemId >= 0 && item.itemId < alternateSurfaces.size) {
                        appCore.simulation.activeObserver.displayedSurface = alternateSurfaces[item.itemId - 1]
                    }
                }
            }
        } else if (item.groupId == GROUP_MARK) {
            lifecycleScope.launch(executor.asCoroutineDispatcher()) {
                if (item.itemId >= Universe.MARKER_COUNT) {
                    appCore.simulation.universe.unmark(selection)
                } else {
                    appCore.simulation.universe.mark(selection, item.itemId)
                    appCore.showMarkers = true
                }
            }
        } else if (item.groupId == GROUP_BROWSER_ITEM_GO) {
            if (item.itemId >= 0 && item.itemId < browserItems.size) {
                val ent = browserItems[item.itemId].`object`
                if (ent != null) {
                    lifecycleScope.launch(executor.asCoroutineDispatcher()) {
                        val newSelection = Selection(ent)
                        appCore.simulation.selection = newSelection
                        appCore.charEnter(CelestiaAction.GoTo.value)
                    }
                }
            }
        } else if (item.groupId == GROUP_GET_INFO) {
            listener?.celestiaFragmentDidRequestObjectInfo(selection)
        }
        return true
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
        val controlView = view?.findViewById<FrameLayout>(R.id.active_control_view_container) ?: return
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
        val controlView = view?.findViewById<FrameLayout>(R.id.active_control_view_container) ?: return
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

    override fun requestContextMenu(x: Float, y: Float, selection: Selection) {
        if (!isContextMenuEnabled)
            return

        // Avoid showing context menu before Android 8, since it is fullscreen
        if (selection.isEmpty || Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return
        }

        pendingTarget = selection

        // Show context menu on main thread
        lifecycleScope.launch {
            // Context menu needs to be shown on the renderer container
            rendererContainer?.showContextMenu(x / rendererSettings.scaleFactor, y / rendererSettings.scaleFactor)
        }
    }

    override fun fatalError(message: String) {
        lifecycleScope.launch {
            val activity = this@CelestiaFragment.activity ?: return@launch
            activity.showAlert(message)
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

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ROTATION_VECTOR) return
        if (!isGyroscopeActive) return // Extra check, though should be handled by registration logic

        val rawQuat = FloatArray(4)
        SensorManager.getQuaternionFromVector(rawQuat, event.values)

        val display: Display?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display = requireActivity().display
        } else {
            @Suppress("DEPRECATION")
            display = requireActivity().windowManager?.defaultDisplay
        }
        val screenRotation = display?.rotation ?: Surface.ROTATION_0
        val angleZ: Float = when (screenRotation) {
            Surface.ROTATION_0 -> 0f
            Surface.ROTATION_90 -> (Math.PI / 2.0f).toFloat()
            Surface.ROTATION_180 -> Math.PI.toFloat()
            Surface.ROTATION_270 -> (-Math.PI / 2.0).toFloat()
            else -> 0f
        }

        val currentQuat = Utils.transformQuaternion(floatArrayOf(
            rawQuat[1], // x
            rawQuat[2], // y
            rawQuat[3], // z
            -rawQuat[0]  // w
        ), angleZ)

        val fromQuat = lastRotationQuaternion
        lastRotationQuaternion = currentQuat.copyOf() // Defensive copy

        if (fromQuat != null) {
            lifecycleScope.launch(executor.asCoroutineDispatcher()) {
                appCore.simulation.activeObserver.applyQuaternion(currentQuat, fromQuat)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "Gyroscope accuracy changed to: $accuracy")
    }

    companion object {
        private const val ARG_DATA_DIR = "data"
        private const val ARG_CFG_FILE = "cfg"
        private const val ARG_ADDON_DIR = "addon"
        private const val ARG_INTERACTION_MODE = "interaction-mode"
        private const val ARG_LANG_OVERRIDE = "lang"
        private const val GROUP_ACTION = 0
        private const val GROUP_ALT_SURFACE_TOP = 1
        private const val GROUP_ALT_SURFACE = 2
        private const val GROUP_MARK_TOP = 3
        private const val GROUP_MARK = 4
        private const val GROUP_BROWSER_ITEM_GO = 6
        private const val GROUP_BROWSER_ITEM = 7
        private const val GROUP_GET_INFO = 8
        private const val TAG_RENDERER_FRAGMENT = "renderer_fragment"

        fun getAvailableMarkers(): List<String> {
            return listOf(
                CelestiaString("Diamond", "Marker"),
                CelestiaString("Triangle", "Marker"),
                CelestiaString("Square", "Marker"),
                CelestiaString("Filled Square", "Marker"),
                CelestiaString("Plus", "Marker"),
                CelestiaString("X", "Marker"),
                CelestiaString("Left Arrow", "Marker"),
                CelestiaString("Right Arrow", "Marker"),
                CelestiaString("Up Arrow", "Marker"),
                CelestiaString("Down Arrow", "Marker"),
                CelestiaString("Circle", "Marker"),
                CelestiaString("Disk", "Marker"),
                CelestiaString("Crosshair", "Marker"),
                CelestiaString("Unmark", "Unmark an object"),
            )
        }

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