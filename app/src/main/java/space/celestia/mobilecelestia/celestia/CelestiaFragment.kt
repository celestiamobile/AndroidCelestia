/*
 * CelestiaFragment.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.celestia

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.LayoutDirection
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.core.view.MenuCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import space.celestia.celestia.*
import space.celestia.mobilecelestia.MainActivity
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.CelestiaExecutor
import space.celestia.mobilecelestia.common.EdgeInsets
import space.celestia.mobilecelestia.di.AppSettings
import space.celestia.mobilecelestia.info.model.CelestiaAction
import space.celestia.mobilecelestia.utils.*
import java.lang.ref.WeakReference
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer

@AndroidEntryPoint
class CelestiaFragment: Fragment(), SurfaceHolder.Callback, CelestiaControlView.Listener, AppStatusReporter.Listener, AppCore.ContextMenuHandler, AppCore.FatalErrorHandler {
    private var activity: Activity? = null

    @Inject
    lateinit var appCore: AppCore
    @Inject
    lateinit var appStatusReporter: AppStatusReporter
    @Inject
    lateinit var renderer: Renderer
    @Inject
    lateinit var executor: CelestiaExecutor
    @AppSettings
    @Inject
    lateinit var appSettings: PreferenceManager

    // MARK: GL View
    private lateinit var glView: CelestiaView
    private lateinit var viewInteraction: CelestiaInteraction

    private var currentControlViewID = R.id.active_control_view_container

    // Parameters
    private var pathToLoad: String? = null
    private var cfgToLoad: String? = null
    private var addonDirsToLoad: List<String> = listOf()
    private var enableMultisample = false
    private var enableFullResolution = false
    private var frameRateOption = Renderer.FRAME_60FPS
    private lateinit var languageOverride: String

    // MARK: Celestia
    private var pendingTarget: Selection? = null
    private var browserItems: ArrayList<BrowserItem> = arrayListOf()
    private var density: Float = 1f
    private var previousDensity: Float = 0f
    private var savedInsets = EdgeInsets()
    private var hasSetRenderer: Boolean = false

    private var isControlViewVisible = true
    private var hideAnimator: ObjectAnimator? = null
    private var showAnimator: ObjectAnimator? = null

    private val scaleFactor: Float
        get() = if (enableFullResolution) 1.0f else (1.0f / density)

    private var loadSuccess = false
    private var haveSurface = false
    private var interactionMode = CelestiaInteraction.InteractionMode.Object
    private lateinit var controlView: CelestiaControlView

    private var isContextMenuEnabled = true
    private var sensitivity = 10.0f

    interface Listener {
        fun celestiaFragmentDidRequestActionMenu()
        fun celestiaFragmentDidRequestObjectInfo()
        fun celestiaFragmentDidRequestSearch()
        fun celestiaFragmentDidRequestObjectInfo(selection: Selection)
        fun provideFallbackConfigFilePath(): String
        fun provideFallbackDataDirectoryPath(): String
        fun celestiaFragmentLoadingFromFallback()
        fun celestiaFragmentCanAcceptKeyEvents(): Boolean
    }

    private var listener: Listener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        density = resources.displayMetrics.density

        arguments?.let {
            pathToLoad = it.getString(ARG_DATA_DIR)
            cfgToLoad = it.getString(ARG_CFG_FILE)
            addonDirsToLoad = it.getStringArrayList(ARG_ADDON_DIR) ?: listOf()
            enableMultisample = it.getBoolean(ARG_MULTI_SAMPLE)
            enableFullResolution = it.getBoolean(ARG_FULL_RESOLUTION)
            languageOverride = it.getString(ARG_LANG_OVERRIDE, "en")
            frameRateOption = it.getInt(ARG_FRAME_RATE_OPTION)
        }

        isContextMenuEnabled = appSettings[PreferenceManager.PredefinedKey.ContextMenu] != "false"
        val pickSensitivity = appSettings[PreferenceManager.PredefinedKey.PickSensitivity]?.toDoubleOrNull()
        if (pickSensitivity != null) {
            sensitivity = pickSensitivity.toFloat()
        }

        if (savedInstanceState != null) {
            previousDensity = savedInstanceState.getFloat(KEY_PREVIOUS_DENSITY, 0f)
            frameRateOption = savedInstanceState.getInt(ARG_FRAME_RATE_OPTION, Renderer.FRAME_60FPS)
            hasSetRenderer = savedInstanceState.getBoolean(ARG_HAS_SET_RENDERER, false)
            if (savedInstanceState.containsKey(ARG_INTERACTION_MODE)) {
                interactionMode = CelestiaInteraction.InteractionMode.fromButton(savedInstanceState.getInt(ARG_INTERACTION_MODE))
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putFloat(KEY_PREVIOUS_DENSITY, density)
        outState.putInt(ARG_FRAME_RATE_OPTION, frameRateOption)
        outState.putInt(ARG_INTERACTION_MODE, interactionMode.button)
        outState.putBoolean(ARG_HAS_SET_RENDERER, hasSetRenderer)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        appStatusReporter.register(this)

        val view = inflater.inflate(R.layout.fragment_celestia, container, false)
        controlView = view.findViewById(R.id.control_view)
        controlView.listener = this

        if (!hasSetRenderer) {
            appCore.setRenderer(renderer)
            renderer.setEngineStartedListener {
                loadCelestia()
            }
            hasSetRenderer = true
        }

        setUpGLView(view.findViewById(R.id.celestia_gl_view))
        return view
    }

    override fun onDestroyView() {
        appStatusReporter.unregister(this)
        appCore.setContextMenuHandler(null)
        appCore.setFatalErrorHandler(null)

        super.onDestroyView()
    }

    override fun onDestroy() {
        pendingTarget = null

        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()

        renderer.pause()
    }

    override fun onResume() {
        super.onResume()

        renderer.resume()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement CelestiaFragment.Listener")
        }
        activity = context as? Activity
    }

    override fun onDetach() {
        super.onDetach()

        listener = null
        activity = null
    }

    override fun celestiaLoadingStateChanged(newState: AppStatusReporter.State) {
        if (newState == AppStatusReporter.State.LOADING_SUCCESS)
            loadingFinished()
    }

    override fun celestiaLoadingProgress(status: String) {}

    fun updateFrameRateOption(newFrameRateOption: Int) {
        frameRateOption = newFrameRateOption
        lifecycleScope.launch(executor.asCoroutineDispatcher()) {
            renderer.setFrameRateOption(newFrameRateOption)
        }
    }

    fun handleInsetsChanged(newInsets: EdgeInsets) {
        savedInsets = newInsets
        val thisView = view ?: return
        if (!loadSuccess) { return }

        val insets = savedInsets.scaleBy(scaleFactor)
        lifecycleScope.launch(executor.asCoroutineDispatcher()) {
            appCore.setSafeAreaInsets(insets)
        }

        val ltr = resources.configuration.layoutDirection != View.LAYOUT_DIRECTION_RTL
        val safeInsetEnd = if (ltr) newInsets.right else newInsets.left

        val controlView = thisView.findViewById<FrameLayout>(currentControlViewID) ?: return
        val params = controlView.layoutParams as? ConstraintLayout.LayoutParams
        if (params != null) {
            params.marginEnd = resources.getDimensionPixelOffset(R.dimen.control_view_container_margin_end) + safeInsetEnd
            controlView.layoutParams = params
        }
    }

    private fun setUpGLView(container: FrameLayout) {
        val activity = this.activity ?: return
        val view = CelestiaView(activity, scaleFactor)

        registerForContextMenu(view)

        val weakSelf = WeakReference(this)
        val interaction = CelestiaInteraction(activity, appCore, executor, interactionMode, appSettings, canAcceptKeyEvents = {
            val self = weakSelf.get() ?: return@CelestiaInteraction false
            return@CelestiaInteraction self.listener?.celestiaFragmentCanAcceptKeyEvents() ?: false
        }, showMenu = {
            val self = weakSelf.get() ?: return@CelestiaInteraction
            self.listener?.celestiaFragmentDidRequestActionMenu()
        })
        glView = view
        viewInteraction = interaction

        interaction.scaleFactor = scaleFactor
        interaction.density = density
        renderer.startConditionally(activity, enableMultisample)
        container.addView(view, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        view.holder?.addCallback(this)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun loadCelestia(): Boolean {
        val data = pathToLoad
        val cfg = cfgToLoad
        val addonDirs = addonDirsToLoad.toTypedArray()

        if (data == null || cfg == null) {
            appStatusReporter.updateState(AppStatusReporter.State.LOADING_FAILURE)
            return false
        }

        appStatusReporter.updateState(AppStatusReporter.State.LOADING)

        AppCore.initGL()
        AppCore.chdir(data)

        // Set up locale
        AppCore.setLocaleDirectoryPath("$data/locale", languageOverride)

        // Reading config, data
        if (!appCore.startSimulation(cfg, addonDirs, appStatusReporter)) {
            val lis = listener
            if (lis != null) {
                // Read from fallback
                val fallbackConfigPath = lis.provideFallbackConfigFilePath()
                val fallbackDataPath = lis.provideFallbackDataDirectoryPath()
                if (fallbackConfigPath != cfg || fallbackDataPath != data) {
                    lis.celestiaFragmentLoadingFromFallback()
                    AppCore.chdir(fallbackDataPath)
                    AppCore.setLocaleDirectoryPath("$fallbackDataPath/locale", languageOverride)
                    if (!appCore.startSimulation(fallbackConfigPath, addonDirs, appStatusReporter)) {
                        appStatusReporter.updateState(AppStatusReporter.State.LOADING_FAILURE)
                        return false
                    }
                } else {
                    appStatusReporter.updateState(AppStatusReporter.State.LOADING_FAILURE)
                    return false
                }
            } else {
                appStatusReporter.updateState(AppStatusReporter.State.LOADING_FAILURE)
                return false
            }
        }

        // Prepare renderer
        if (!appCore.startRenderer()) {
            appStatusReporter.updateState(AppStatusReporter.State.LOADING_FAILURE)
            return false
        }

        updateContentScale()

        // Display
        appCore.tick()
        appCore.start()

        appStatusReporter.updateState(AppStatusReporter.State.LOADING_SUCCESS)

        return true
    }

    private fun updateContentScale() {
        if (density == previousDensity) return

        renderer.makeContextCurrent()

        appCore.setDPI((96 * density * scaleFactor).toInt())
        appCore.setPickTolerance(sensitivity * density * scaleFactor)

        appCore.setSafeAreaInsets(savedInsets.scaleBy(scaleFactor))

        appCore.clearFonts()

        // Use installed font
        val locale = AppCore.getLanguage()
        val preferredInstalledFont = MainActivity.availableInstalledFonts[locale] ?: MainActivity.defaultInstalledFont
        if (preferredInstalledFont != null) {
            val font = preferredInstalledFont.first
            val boldFont = preferredInstalledFont.second
            appCore.setFont(font.first, font.second, 9)
            appCore.setTitleFont(boldFont.first, boldFont.second, 15)
            appCore.setRendererFont(font.first, font.second, 9, AppCore.RENDER_FONT_STYLE_NORMAL)
            appCore.setRendererFont(boldFont.first, boldFont.second, 15, AppCore.RENDER_FONT_STYLE_LARGE)
        }
        previousDensity = density
    }

    private fun loadingFinished() = lifecycleScope.launch {
        if (!haveSurface) return@launch
        val isRTL = resources.configuration.layoutDirection == LayoutDirection.RTL
        withContext(executor.asCoroutineDispatcher()) {
            updateContentScale()
            appCore.layoutDirection = if (isRTL) AppCore.LAYOUT_DIRECTION_RTL else AppCore.LAYOUT_DIRECTION_LTR
        }
        setUpInteractions()
    }

    private fun setUpInteractions() {
        // Set up control buttons
        val items = listOf(
            CelestiaToggleButton(R.drawable.control_mode_combined, CelestiaControlAction.ToggleModeToObject, CelestiaControlAction.ToggleModeToCamera, contentDescription = CelestiaString("Toggle Interaction Mode", ""), interactionMode == CelestiaInteraction.InteractionMode.Camera),
            CelestiaTapButton(R.drawable.control_info, CelestiaControlAction.Info, CelestiaString("Get Info", "")),
            CelestiaTapButton(R.drawable.control_search, CelestiaControlAction.Search, CelestiaString("Search", "")),
            CelestiaTapButton(R.drawable.control_action_menu, CelestiaControlAction.ShowMenu, CelestiaString("Menu", "")),
            CelestiaTapButton(R.drawable.toolbar_exit, CelestiaControlAction.Hide, CelestiaString("Hide", ""))
        )
        controlView.buttons = items

        glView.isReady = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isContextMenuEnabled) {
            glView.isContextClickable = true
        }
        viewInteraction.isReady = true
        @Suppress("ClickableViewAccessibility")
        glView.setOnTouchListener { v, event ->
            showControlViewIfNeeded()
            viewInteraction.onTouch(v, event)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            glView.defaultFocusHighlightEnabled = false
            glView.isFocusable = true
            glView.isFocusableInTouchMode = true
            glView.requestFocus()
        }
        glView.setOnKeyListener(viewInteraction)
        glView.setOnGenericMotionListener(viewInteraction)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pointerCaptureListener = viewInteraction.pointerCaptureListener as? View.OnCapturedPointerListener
            if (pointerCaptureListener != null) {
                glView.setOnCapturedPointerListener(pointerCaptureListener)
            }
        }
        glView.setOnHoverListener(viewInteraction)
        lifecycleScope.launch(executor.asCoroutineDispatcher()) {
            if (isContextMenuEnabled)
                appCore.setContextMenuHandler(this@CelestiaFragment)
            appCore.setFatalErrorHandler(this@CelestiaFragment)
        }
        if (isContextMenuEnabled)
            registerForContextMenu(glView)
        loadSuccess = true

        Log.d(TAG, "Ready to display")

        handleInsetsChanged(savedInsets)
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
                menu.add(GROUP_BROWSER_ITEM_GO, browserItems.size, Menu.NONE, CelestiaString("Go", ""))
                browserItems.add(browserItem)
            }

            browserItem.children.withIndex().forEach {
                val subMenu = menu.addSubMenu(GROUP_BROWSER_ITEM, 0, Menu.NONE, browserItem.childNameAtIndex(it.index))
                createSubMenu(subMenu, it.value)
            }
            MenuCompat.setGroupDividerEnabled(menu, true)
        }

        menu.setHeaderTitle(appCore.simulation.universe.getNameForSelection(selection))
        menu.add(GROUP_GET_INFO, 0, Menu.NONE, CelestiaString("Get Info", ""))

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
            if (alternateSurfaces.size > 0) {
                val subMenu = menu.addSubMenu(GROUP_ALT_SURFACE_TOP, 0, Menu.NONE, CelestiaString("Alternate Surfaces", ""))
                subMenu.add(GROUP_ALT_SURFACE, 0, Menu.NONE, CelestiaString("Default", ""))
                alternateSurfaces.withIndex().forEach {
                    subMenu.add(GROUP_ALT_SURFACE, it.index + 1, Menu.NONE, it.value)
                }
            }
        }
        val markMenu = menu.addSubMenu(GROUP_MARK_TOP, 0, Menu.NONE, CelestiaString("Mark", ""))
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
                if (item.itemId == availableMarkers.size) {
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

    override fun surfaceCreated(holder: SurfaceHolder) {
        renderer.setSurface(holder.surface)
        renderer.setFrameRateOption(frameRateOption)
        haveSurface = true
        if (appStatusReporter.state.value >= AppStatusReporter.State.LOADING_SUCCESS.value) {
            loadingFinished()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.d(TAG, "Resize to $width x $height")
        renderer.setSurfaceSize(width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        renderer.setSurface(null)
    }

    // Actions
    override fun didTapAction(action: CelestiaControlAction) {
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
        when (action) {
            CelestiaControlAction.ToggleModeToCamera -> {
                interactionMode = CelestiaInteraction.InteractionMode.Camera
                viewInteraction.setInteractionMode(CelestiaInteraction.InteractionMode.Camera)
                activity?.showToast(CelestiaString("Switched to camera mode", ""), Toast.LENGTH_SHORT)
            }
            CelestiaControlAction.ToggleModeToObject -> {
                interactionMode = CelestiaInteraction.InteractionMode.Object
                viewInteraction.setInteractionMode(CelestiaInteraction.InteractionMode.Object)
                activity?.showToast(CelestiaString("Switched to object mode", ""), Toast.LENGTH_SHORT)
            }
            else -> {}
        }
    }

    override fun didStartPressingAction(action: CelestiaControlAction) {}

    override fun didEndPressingAction(action: CelestiaControlAction) {}

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
            glView.showContextMenu(x / scaleFactor, y / scaleFactor)
        }
    }

    override fun fatalError(message: String) {
        lifecycleScope.launch {
            val activity = this@CelestiaFragment.activity ?: return@launch
            activity.showAlert(message)
        }
    }

    companion object {
        private const val ARG_DATA_DIR = "data"
        private const val ARG_CFG_FILE = "cfg"
        private const val ARG_ADDON_DIR = "addon"
        private const val ARG_MULTI_SAMPLE = "multisample"
        private const val ARG_FULL_RESOLUTION = "fullresolution"
        private const val ARG_FRAME_RATE_OPTION = "framerateoption"
        private const val ARG_INTERACTION_MODE = "interaction-mode"
        private const val ARG_HAS_SET_RENDERER = "has-set-renderer"
        private const val ARG_LANG_OVERRIDE = "lang"
        private const val GROUP_ACTION = 0
        private const val GROUP_ALT_SURFACE_TOP = 1
        private const val GROUP_ALT_SURFACE = 2
        private const val GROUP_MARK_TOP = 3
        private const val GROUP_MARK = 4
        private const val GROUP_BROWSER_ITEM_GO = 6
        private const val GROUP_BROWSER_ITEM = 7
        private const val GROUP_GET_INFO = 8
        private const val KEY_PREVIOUS_DENSITY = "density"

        val availableMarkers: List<String>
            get() = listOf(
                "Diamond", "Triangle", "Square", "Filled Square",
                "Plus", "X", "Left Arrow", "Right Arrow",
                "Up Arrow", "Down Arrow", "Circle", "Disk",
                "Crosshair", "Unmark"
            ).map { CelestiaString(it, "") }

        private const val TAG = "CelestiaFragment"

        fun newInstance(data: String, cfg: String, addons: List<String>, enableMultisample: Boolean, enableFullResolution: Boolean, frameRateOption: Int, languageOverride: String) =
            CelestiaFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_DATA_DIR, data)
                    putString(ARG_CFG_FILE, cfg)
                    putBoolean(ARG_MULTI_SAMPLE, enableMultisample)
                    putBoolean(ARG_FULL_RESOLUTION, enableFullResolution)
                    putString(ARG_LANG_OVERRIDE, languageOverride)
                    putInt(ARG_FRAME_RATE_OPTION, frameRateOption)
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