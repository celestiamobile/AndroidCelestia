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
    private var enableMultisample = false
    private var enableFullResolution = false
    private var frameRateOption = Renderer.FRAME_60FPS
    private lateinit var languageOverride: String

    private var density: Float = 1f
    private var fontScale: Float = 1f
    private var previousDensity: Float = 0f
    private var previousFontScale: Float = 0f
    private var savedInsets = EdgeInsets()
    private var hasSetRenderer: Boolean = false

    private val scaleFactor: Float
        get() = if (enableFullResolution) 1.0f else (1.0f / density)

    private var loadSuccess = false
    private var haveSurface = false

    private var sensitivity = 10.0f

    interface Listener {
        fun celestiaRendererLoadingFromFallback()
        fun celestiaRendererReady(scaleFactor: Float, density: Float)
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
            enableMultisample = it.getBoolean(ARG_MULTI_SAMPLE)
            enableFullResolution = it.getBoolean(ARG_FULL_RESOLUTION)
            languageOverride = it.getString(ARG_LANG_OVERRIDE, "en")
            frameRateOption = it.getInt(ARG_FRAME_RATE_OPTION)
        }

        val pickSensitivity = appSettings[PreferenceManager.PredefinedKey.PickSensitivity]?.toDoubleOrNull()
        if (pickSensitivity != null) {
            sensitivity = pickSensitivity.toFloat()
        }

        if (savedInstanceState != null) {
            previousDensity = savedInstanceState.getFloat(KEY_PREVIOUS_DENSITY, 0f)
            previousFontScale = savedInstanceState.getFloat(KEY_PREVIOUS_FONT_SCALE, 0f)
            frameRateOption = savedInstanceState.getInt(ARG_FRAME_RATE_OPTION, Renderer.FRAME_60FPS)
            hasSetRenderer = savedInstanceState.getBoolean(ARG_HAS_SET_RENDERER, false)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putFloat(KEY_PREVIOUS_FONT_SCALE, fontScale)
        outState.putFloat(KEY_PREVIOUS_DENSITY, density)
        outState.putInt(ARG_FRAME_RATE_OPTION, frameRateOption)
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
        frameRateOption = newFrameRateOption
        lifecycleScope.launch(executor.asCoroutineDispatcher()) {
            renderer.setFrameRateOption(newFrameRateOption)
        }
    }

    fun handleInsetsChanged(newInsets: EdgeInsets) {
        savedInsets = newInsets
        if (!loadSuccess) { return }

        val insets = savedInsets.scaleBy(scaleFactor)
        lifecycleScope.launch(executor.asCoroutineDispatcher()) {
            appCore.setSafeAreaInsets(insets)
        }
    }

    private fun setUpGLView(container: FrameLayout) {
        val view = CelestiaView(requireActivity(), scaleFactor)

        glView = view

        renderer.startConditionally(requireActivity(), enableMultisample)
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

        updateContentScale()

        // Display
        appCore.tick()
        appCore.start()

        appStatusReporter.updateState(AppStatusReporter.State.LOADING_SUCCESS)

        return true
    }

    fun updateContentScale() {
        if (density == previousDensity && fontScale == previousFontScale) return

        renderer.makeContextCurrent()

        appCore.setDPI((96 * density * scaleFactor).toInt())
        appCore.setPickTolerance(sensitivity * density * scaleFactor)

        appCore.setSafeAreaInsets(savedInsets.scaleBy(scaleFactor))

        // Use installed font
        val locale = AppCore.getLanguage()
        val hasCelestiaPlus = purchaseManager.canUseInAppPurchase() && purchaseManager.purchaseToken() != null
        var normalFont = if (hasCelestiaPlus) appSettings.normalFont else null
        var boldFont = if (hasCelestiaPlus) appSettings.boldFont else null
        val preferredInstalledFont = MainActivity.availableInstalledFonts[locale] ?: MainActivity.defaultInstalledFont
        if (preferredInstalledFont != null) {
            normalFont = normalFont ?: preferredInstalledFont.first
            boldFont = boldFont ?: preferredInstalledFont.second
        }
        if (normalFont != null) {
            appCore.setFont(normalFont.path, normalFont.ttcIndex, (9 * fontScale).toInt())
            appCore.setRendererFont(normalFont.path, normalFont.ttcIndex, (9 * fontScale).toInt(), AppCore.RENDER_FONT_STYLE_NORMAL)
        }
        if (boldFont != null) {
            appCore.setTitleFont(boldFont.path, boldFont.ttcIndex, (15 * fontScale).toInt())
            appCore.setRendererFont(boldFont.path, boldFont.ttcIndex, (15 * fontScale).toInt(), AppCore.RENDER_FONT_STYLE_LARGE)
        }
        previousDensity = density
        previousFontScale = fontScale
    }

    private fun loadingFinished() = lifecycleScope.launch {
        if (!haveSurface) return@launch
        val isRTL = resources.configuration.layoutDirection == LayoutDirection.RTL
        withContext(executor.asCoroutineDispatcher()) {
            updateContentScale()
            appCore.layoutDirection = if (isRTL) AppCore.LAYOUT_DIRECTION_RTL else AppCore.LAYOUT_DIRECTION_LTR
        }
        loadSuccess = true
        handleInsetsChanged(savedInsets)
        
        // Notify parent that rendering is ready (no need to pass GL view)
        listener?.celestiaRendererReady(scaleFactor, density)

        Log.d(TAG, "Ready to display")
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

    companion object {
        private const val ARG_DATA_DIR = "data"
        private const val ARG_CFG_FILE = "cfg"
        private const val ARG_ADDON_DIR = "addon"
        private const val ARG_MULTI_SAMPLE = "multisample"
        private const val ARG_FULL_RESOLUTION = "fullresolution"
        private const val ARG_FRAME_RATE_OPTION = "framerateoption"
        private const val ARG_HAS_SET_RENDERER = "has-set-renderer"
        private const val ARG_LANG_OVERRIDE = "lang"
        private const val KEY_PREVIOUS_DENSITY = "density"
        private const val KEY_PREVIOUS_FONT_SCALE = "fontscale"

        private const val TAG = "CelestiaRendererFragment"

        fun newInstance(data: String, cfg: String, addons: List<String>, enableMultisample: Boolean, enableFullResolution: Boolean, frameRateOption: Int, languageOverride: String) =
            CelestiaRendererFragment().apply {
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
