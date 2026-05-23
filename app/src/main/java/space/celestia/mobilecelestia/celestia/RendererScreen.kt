package space.celestia.mobilecelestia.celestia

import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.AndroidExternalSurface
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import space.celestia.celestia.AppCore
import space.celestia.celestiaui.settings.viewmodel.boldFont
import space.celestia.celestiaui.settings.viewmodel.normalFont
import space.celestia.celestiaui.utils.AppStatusReporter
import space.celestia.mobilecelestia.MainActivity
import space.celestia.mobilecelestia.celestia.viewmodel.RendererViewModel
import space.celestia.mobilecelestia.common.EdgeInsets
import java.util.Locale

@Composable
fun RendererScreen(pathToLoad: String, cfgToLoad: String, addonDirsToLoad: List<String>, languageOverride: String, safeAreaInsets: EdgeInsets, frameRateOptionEvents: Flow<Int>, reapplyContentScaleEvents: Flow<Unit>, celestiaRendererReady: () -> Unit, celestiaRendererLoadingFromFallback: () -> Unit) {
    val viewModel: RendererViewModel = hiltViewModel()
    val density = LocalDensity.current.density
    val fontScale = LocalConfiguration.current.fontScale
    val layoutDirection = LocalLayoutDirection.current
    val scaleFactor = if (viewModel.rendererSettings.enableFullResolution) 1.0f else (1.0f / density)
    var surfaceSize by remember { mutableStateOf(IntSize.Zero) }
    var haveSurface by remember { mutableStateOf(false) }
    var loadSuccess by remember { mutableStateOf(false) }

    // Effects keyed on Unit (frameRate/reapply collectors) capture their closure once;
    // route through rememberUpdatedState so they always read the latest values.
    val currentSafeAreaInsets by rememberUpdatedState(safeAreaInsets)
    val currentDensity by rememberUpdatedState(density)
    val currentFontScale by rememberUpdatedState(fontScale)

    fun renderChanges(): RenderChanges {
        return if (viewModel.renderer.hasPresentationSurface()) RenderChanges() else RenderChanges(scaling = currentDensity != viewModel.rendererSettings.density || currentFontScale != viewModel.rendererSettings.fontScale, safeArea = currentSafeAreaInsets != viewModel.rendererSettings.safeAreaInsets)
    }

    fun applyRenderChanges(changes: RenderChanges): RenderChanges {
        if (changes.scaling) {
            viewModel.rendererSettings.density = currentDensity
            viewModel.rendererSettings.fontScale = currentFontScale
        }

        if (changes.safeArea) {
            viewModel.rendererSettings.safeAreaInsets = currentSafeAreaInsets
        }
        return changes
    }

    fun updateContentScale(changes: RenderChanges) {
        if (!changes.scaling && !changes.safeArea) return

        viewModel.renderer.makeContextCurrent()
        viewModel.appCore.updateContentScale(viewModel.rendererSettings, changes)
    }

    fun loadCelestia(samples: Int): Boolean {
        val addonDirs = addonDirsToLoad.toTypedArray()

        viewModel.appStatusReporter.updateState(AppStatusReporter.State.LOADING)

        AppCore.initGL()
        AppCore.chdir(pathToLoad)

        val countryCode = Locale.getDefault().country

        // Set up locale
        AppCore.setLocaleDirectoryPath("$pathToLoad/locale", languageOverride, countryCode)

        // Reading config, data
        if (!viewModel.appCore.startSimulation(cfgToLoad, addonDirs, viewModel.appStatusReporter)) {
            // Read from fallback
            val fallbackConfigPath = viewModel.defaultFilePaths.configFilePath
            val fallbackDataPath = viewModel.defaultFilePaths.dataDirectoryPath
            if (fallbackConfigPath != cfgToLoad || fallbackDataPath != pathToLoad) {
                celestiaRendererLoadingFromFallback()
                AppCore.chdir(fallbackDataPath)
                AppCore.setLocaleDirectoryPath("$fallbackDataPath/locale", languageOverride, countryCode)
                if (!viewModel.appCore.startSimulation(fallbackConfigPath, addonDirs, viewModel.appStatusReporter)) {
                    viewModel.appStatusReporter.updateState(AppStatusReporter.State.LOADING_FAILURE)
                    return false
                }
            } else {
                viewModel.appStatusReporter.updateState(AppStatusReporter.State.LOADING_FAILURE)
                return false
            }
        }

        // Prepare renderer
        if (!viewModel.appCore.startRenderer(viewModel.rendererSettings.enableSRGBRendering)) {
            viewModel.appStatusReporter.updateState(AppStatusReporter.State.LOADING_FAILURE)
            return false
        }

        // Use installed font
        val locale = AppCore.getLanguage()
        val hasCelestiaPlus =
            viewModel.purchaseManager.canUseInAppPurchase() && viewModel.purchaseManager.purchaseToken() != null
        val normalFont = if (hasCelestiaPlus) viewModel.appSettingsNoBackup.normalFont else null
        val boldFont = if (hasCelestiaPlus) viewModel.appSettingsNoBackup.boldFont else null
        if (normalFont != null) {
            viewModel.appCore.setFont(normalFont.path, normalFont.ttcIndex, 9)
            viewModel.appCore.setRendererFont(
                normalFont.path,
                normalFont.ttcIndex,
                9,
                AppCore.RENDER_FONT_STYLE_NORMAL
            )
        }
        if (boldFont != null) {
            viewModel.appCore.setTitleFont(
                boldFont.path,
                boldFont.ttcIndex,
                15
            )
            viewModel.appCore.setRendererFont(
                boldFont.path,
                boldFont.ttcIndex,
                15,
                AppCore.RENDER_FONT_STYLE_LARGE
            )
        }

        updateContentScale(changes = applyRenderChanges(renderChanges()))

        // Display
        viewModel.appCore.tick()
        viewModel.appCore.start()

        viewModel.appStatusReporter.updateState(AppStatusReporter.State.LOADING_SUCCESS)

        return true
    }

    suspend fun loadingFinished() {
        if (!haveSurface) return
        val isRTL = layoutDirection == LayoutDirection.Rtl
        val changes = applyRenderChanges(renderChanges())
        withContext(viewModel.executor.asCoroutineDispatcher()) {
            updateContentScale(changes)
            viewModel.appCore.layoutDirection = if (isRTL) AppCore.LAYOUT_DIRECTION_RTL else AppCore.LAYOUT_DIRECTION_LTR
        }
        loadSuccess = true

        // Notify parent that rendering is ready (no need to pass GL view)
        celestiaRendererReady()

        Log.d("RendererScreen", "Ready to display")
    }

    LaunchedEffect(safeAreaInsets, loadSuccess) {
        if (!loadSuccess) return@LaunchedEffect
        val changes = applyRenderChanges(renderChanges())
        withContext(viewModel.executor.asCoroutineDispatcher()) {
            updateContentScale(changes)
        }
    }

    LaunchedEffect(Unit) {
        frameRateOptionEvents.collect { option ->
            viewModel.rendererSettings.frameRateOption = option
            withContext(viewModel.executor.asCoroutineDispatcher()) {
                viewModel.renderer.setFrameRateOption(option)
            }
        }
    }

    LaunchedEffect(Unit) {
        reapplyContentScaleEvents.collect {
            val changes = applyRenderChanges(renderChanges())
            withContext(viewModel.executor.asCoroutineDispatcher()) {
                updateContentScale(changes)
            }
        }
    }

    val scope = rememberCoroutineScope()
    val activity = requireNotNull(LocalActivity.current)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val listener = object: AppStatusReporter.Listener {
            override fun celestiaLoadingProgress(status: String) {}

            override fun celestiaLoadingStateChanged(newState: AppStatusReporter.State) {
                if (newState == AppStatusReporter.State.LOADING_SUCCESS) {
                    scope.launch {
                        loadingFinished()
                    }
                }
            }
        }
        viewModel.appStatusReporter.register(listener)
        viewModel.renderer.setEngineStartedListener { samples ->
            loadCelestia(samples)
        }
        viewModel.renderer.startConditionally(activity, viewModel.rendererSettings.enableMultisample)

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.renderer.resume()
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.renderer.pause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            viewModel.appStatusReporter.unregister(listener)
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                surfaceSize = IntSize(
                    (size.width.toFloat() * scaleFactor).toInt(),
                    (size.height.toFloat() * scaleFactor).toInt()
                )
            }
    ) {
        if (surfaceSize.width > 0 && surfaceSize.height > 0) {
            AndroidExternalSurface(
                modifier = Modifier.fillMaxSize(),
                surfaceSize = surfaceSize
            ) {
                onSurface { surface, width, height ->
                    viewModel.renderer.setSurface(surface)
                    viewModel.renderer.setFrameRateOption(viewModel.rendererSettings.frameRateOption)
                    viewModel.renderer.setSurfaceSize(width, height)
                    haveSurface = true
                    if (viewModel.appStatusReporter.state.value >= AppStatusReporter.State.LOADING_SUCCESS.value) {
                        scope.launch { loadingFinished() }
                    }

                    surface.onChanged { width, height ->
                        Log.d("RendererScreen", "Resize to $width x $height")
                        viewModel.renderer.setSurfaceSize(width, height)
                    }

                    surface.onDestroyed {
                        haveSurface = false
                        viewModel.renderer.setSurface(null)
                    }
                }
            }
        }
    }
}