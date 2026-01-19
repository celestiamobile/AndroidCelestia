// CelestiaRendererFragment.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.celestia

import android.content.Context
import android.os.Bundle
import android.util.LayoutDirection
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import space.celestia.celestia.AppCore
import space.celestia.celestia.Renderer
import space.celestia.celestiafoundation.utils.FilePaths
import space.celestia.mobilecelestia.MainActivity
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.CelestiaExecutor
import space.celestia.mobilecelestia.common.EdgeInsets
import space.celestia.mobilecelestia.di.AppSettings
import space.celestia.mobilecelestia.purchase.PurchaseManager
import space.celestia.mobilecelestia.settings.boldFont
import space.celestia.mobilecelestia.settings.normalFont
import space.celestia.mobilecelestia.utils.AppStatusReporter
import space.celestia.mobilecelestia.utils.PreferenceManager
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class CelestiaRendererFragment : Fragment(), SurfaceHolder.Callback, AppStatusReporter.Listener {

    @Inject
    lateinit var appCore: AppCore
    @Inject
    lateinit var appStatusReporter: AppStatusReporter
    @Inject
    lateinit var renderer: Renderer
    @Inject
    lateinit var rendererSettings: RendererSettings
    @Inject
    lateinit var executor: CelestiaExecutor
    @AppSettings
    @Inject
    lateinit var appSettings: PreferenceManager
    @Inject
    lateinit var purchaseManager: PurchaseManager
    @Inject
    lateinit var defaultFilePaths: FilePaths

    // MARK: GL View
    private lateinit var glView: CelestiaView

    // Parameters
    private var pathToLoad: String? = null
    private var cfgToLoad: String? = null
    private var addonDirsToLoad: List<String> = listOf()
    private lateinit var languageOverride: String

    private var density: Float = 1f
    private var fontScale: Float = 1f
    private var savedInsets = EdgeInsets()
    private var hasSetRenderer: Boolean = false
    private var loadSuccess = false
    private var haveSurface = false

    private val renderChanges: RenderChanges
        get() = if (renderer.hasPresentationSurface()) RenderChanges() else RenderChanges(scaling = density != rendererSettings.density || fontScale != rendererSettings.fontScale, safeArea = savedInsets != rendererSettings.safeAreaInsets)

    interface Listener {
        fun celestiaRendererLoadingFromFallback()
        fun celestiaRendererReady()
    }

    private var listener: Listener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        density = resources.displayMetrics.density
        fontScale = resources.configuration.fontScale

        arguments?.let {
            pathToLoad = it.getString(ARG_DATA_DIR)
            cfgToLoad = it.getString(ARG_CFG_FILE)
            addonDirsToLoad = it.getStringArrayList(ARG_ADDON_DIR) ?: listOf()
            languageOverride = it.getString(ARG_LANG_OVERRIDE, "en")
        }

        if (savedInstanceState != null) {
            hasSetRenderer = savedInstanceState.getBoolean(ARG_HAS_SET_RENDERER, false)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(ARG_HAS_SET_RENDERER, hasSetRenderer)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        appStatusReporter.register(this)

        val view = inflater.inflate(R.layout.fragment_celestia_renderer, container, false)

        if (!hasSetRenderer) {
            appCore.setRenderer(renderer)
            renderer.setEngineStartedListener { samples ->
                loadCelestia(samples)
            }
            hasSetRenderer = true
        }

        setUpGLView(view.findViewById(R.id.celestia_gl_view))

        return view
    }

    override fun onDestroyView() {
        appStatusReporter.unregister(this)
        super.onDestroyView()
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
        } else if (parentFragment is Listener) {
            listener = parentFragment as Listener
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun celestiaLoadingStateChanged(newState: AppStatusReporter.State) {
        if (newState == AppStatusReporter.State.LOADING_SUCCESS)
            loadingFinished()
    }

    override fun celestiaLoadingProgress(status: String) {}

    fun updateFrameRateOption(newFrameRateOption: Int) {
        rendererSettings.frameRateOption = newFrameRateOption
        lifecycleScope.launch(executor.asCoroutineDispatcher()) {
            renderer.setFrameRateOption(newFrameRateOption)
        }
    }

    fun handleInsetsChanged(newInsets: EdgeInsets) {
        savedInsets = newInsets
        if (!loadSuccess || renderer.hasPresentationSurface()) { return }

        val changes = applyRenderChanges(renderChanges)
        lifecycleScope.launch(executor.asCoroutineDispatcher()) {
            updateContentScale(changes)
        }
    }

    fun reapplyContentScale() {
        val changes = applyRenderChanges(renderChanges)
        lifecycleScope.launch(executor.asCoroutineDispatcher()) {
            updateContentScale(changes)
        }
    }

    private fun setUpGLView(container: FrameLayout) {
        // Cannot use rendererSettings.scaleFactor here because it is before any updateContentScale call
        val view = CelestiaView(requireActivity(), if (rendererSettings.enableFullResolution) 1.0f else (1.0f / density))

        glView = view

        renderer.startConditionally(requireActivity(), rendererSettings.enableMultisample)
        container.addView(view, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        view.holder?.addCallback(this)
    }

    private fun loadCelestia(samples: Int): Boolean {
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

        val countryCode = Locale.getDefault().country

        // Set up locale
        AppCore.setLocaleDirectoryPath("$data/locale", languageOverride, countryCode)

        // Reading config, data
        if (!appCore.startSimulation(cfg, addonDirs, appStatusReporter)) {
            val lis = listener
            if (lis != null) {
                // Read from fallback
                val fallbackConfigPath = defaultFilePaths.configFilePath
                val fallbackDataPath = defaultFilePaths.dataDirectoryPath
                if (fallbackConfigPath != cfg || fallbackDataPath != data) {
                    lis.celestiaRendererLoadingFromFallback()
                    AppCore.chdir(fallbackDataPath)
                    AppCore.setLocaleDirectoryPath("$fallbackDataPath/locale", languageOverride, countryCode)
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

        updateContentScale(changes = applyRenderChanges(renderChanges))

        // Display
        appCore.tick()
        appCore.start()

        appStatusReporter.updateState(AppStatusReporter.State.LOADING_SUCCESS)

        return true
    }

    private fun applyRenderChanges(changes: RenderChanges): RenderChanges {
        if (changes.scaling) {
            rendererSettings.density = density
            rendererSettings.fontScale = fontScale
        }

        if (changes.safeArea) {
            rendererSettings.safeAreaInsets = savedInsets
        }
        return changes
    }

    private fun updateContentScale(changes: RenderChanges) {
        if (!changes.scaling && !changes.safeArea) return

        renderer.makeContextCurrent()
        appCore.updateContentScale(rendererSettings, changes, purchaseManager, appSettings)
    }

    private fun loadingFinished() = lifecycleScope.launch {
        if (!haveSurface) return@launch
        val isRTL = resources.configuration.layoutDirection == LayoutDirection.RTL
        val changes = applyRenderChanges(renderChanges)
        withContext(executor.asCoroutineDispatcher()) {
            updateContentScale(changes)
            appCore.layoutDirection = if (isRTL) AppCore.LAYOUT_DIRECTION_RTL else AppCore.LAYOUT_DIRECTION_LTR
        }
        loadSuccess = true

        // Notify parent that rendering is ready (no need to pass GL view)
        listener?.celestiaRendererReady()

        Log.d(TAG, "Ready to display")
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        renderer.setSurface(holder.surface)
        renderer.setFrameRateOption(rendererSettings.frameRateOption)
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

    companion object {
        private const val ARG_DATA_DIR = "data"
        private const val ARG_CFG_FILE = "cfg"
        private const val ARG_ADDON_DIR = "addon"
        private const val ARG_HAS_SET_RENDERER = "has-set-renderer"
        private const val ARG_LANG_OVERRIDE = "lang"

        private const val TAG = "CelestiaRendererFragment"

        fun newInstance(data: String, cfg: String, addons: List<String>, languageOverride: String) =
            CelestiaRendererFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_DATA_DIR, data)
                    putString(ARG_CFG_FILE, cfg)
                    putString(ARG_LANG_OVERRIDE, languageOverride)
                    putStringArrayList(ARG_ADDON_DIR, ArrayList(addons))
                }
            }
    }
}

data class RenderChanges(val scaling: Boolean = false, val safeArea: Boolean = false)

fun AppCore.updateContentScale(rendererSettings: RendererSettings, changes: RenderChanges, purchaseManager: PurchaseManager, appSettings: PreferenceManager) {
    if (changes.scaling) {
        setDPI((96 * rendererSettings.density * rendererSettings.scaleFactor).toInt())
        setPickTolerance(rendererSettings.pickSensitivity * rendererSettings.density * rendererSettings.scaleFactor)

        // Use installed font
        val locale = AppCore.getLanguage()
        val hasCelestiaPlus =
            purchaseManager.canUseInAppPurchase() && purchaseManager.purchaseToken() != null
        var normalFont = if (hasCelestiaPlus) appSettings.normalFont else null
        var boldFont = if (hasCelestiaPlus) appSettings.boldFont else null
        val preferredInstalledFont =
            MainActivity.availableInstalledFonts[locale] ?: MainActivity.defaultInstalledFont
        if (preferredInstalledFont != null) {
            normalFont = normalFont ?: preferredInstalledFont.first
            boldFont = boldFont ?: preferredInstalledFont.second
        }
        if (normalFont != null) {
            setFont(normalFont.path, normalFont.ttcIndex, (9 * rendererSettings.fontScale).toInt())
            setRendererFont(
                normalFont.path,
                normalFont.ttcIndex,
                (9 * rendererSettings.fontScale).toInt(),
                AppCore.RENDER_FONT_STYLE_NORMAL
            )
        }
        if (boldFont != null) {
            setTitleFont(
                boldFont.path,
                boldFont.ttcIndex,
                (15 * rendererSettings.fontScale).toInt()
            )
            setRendererFont(
                boldFont.path,
                boldFont.ttcIndex,
                (15 * rendererSettings.fontScale).toInt(),
                AppCore.RENDER_FONT_STYLE_LARGE
            )
        }
    }

    if (changes.scaling || changes.safeArea) {
        setSafeAreaInsets(rendererSettings.safeAreaInsets.scaleBy(rendererSettings.scaleFactor))
    }
}