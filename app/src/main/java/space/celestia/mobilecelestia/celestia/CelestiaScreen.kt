package space.celestia.mobilecelestia.celestia

import android.os.Build
import android.view.RoundedCorner
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import space.celestia.celestia.AppCore
import space.celestia.celestiaui.compose.SimpleAlertDialog
import space.celestia.celestiaui.utils.AppStatusReporter
import space.celestia.celestiaui.utils.CelestiaString
import space.celestia.celestiaui.utils.PreferenceManager
import space.celestia.mobilecelestia.celestia.viewmodel.RendererViewModel
import space.celestia.mobilecelestia.common.EdgeInsets
import space.celestia.mobilecelestia.common.RoundedCorners
import space.celestia.mobilecelestia.common.SHEET_MAX_FULL_WIDTH_DP
import kotlin.math.max

sealed class CelestiaAlert {
    data object ErrorLoadingData: CelestiaAlert()
    data class FatalError(val message: String): CelestiaAlert()
}

private val InteractionModeSaver = Saver<CelestiaInteraction.InteractionMode, String>(
    save = { it.name },
    restore = { savedString ->
        try {
            CelestiaInteraction.InteractionMode.valueOf(savedString)
        } catch (_: IllegalArgumentException) {
            // Fallback to a default if the saved value is invalid/deprecated
            CelestiaInteraction.InteractionMode.Object
        }
    }
)

@Composable
fun CelestiaScreen(pathToLoad: String, cfgToLoad: String, addonDirsToLoad: List<String>, languageOverride: String, frameRateOptionEvents: Flow<Int>, reapplyContentScaleEvents: Flow<Unit>, canAcceptKeyEvents: () -> Boolean, showMenu: () -> Unit) {
    val lifeCycleOwner = LocalLifecycleOwner.current
    val viewModel: RendererViewModel = hiltViewModel()
    var currentState by rememberSaveable { mutableStateOf(viewModel.appStatusReporter.state) }
    if (currentState == AppStatusReporter.State.LOADING_FAILURE || currentState == AppStatusReporter.State.EXTERNAL_LOADING_FAILURE) {
        // TODO: test failed state
        return
    }

    var viewInteraction: CelestiaInteraction? by remember { mutableStateOf(null) }
    val isContextMenuEnabled by remember { mutableStateOf(viewModel.appSettings[PreferenceManager.PredefinedKey.ContextMenu] != "false") }
    val scope = rememberCoroutineScope()
    DisposableEffect(lifeCycleOwner) {
        val observer = object: AppStatusReporter.Listener {
            override fun celestiaLoadingProgress(status: String) {}

            override fun celestiaLoadingStateChanged(newState: AppStatusReporter.State) {
                scope.launch {
                    currentState = newState
                }
            }
        }
        viewModel.appStatusReporter.register(observer)
        onDispose {
            viewModel.appStatusReporter.unregister(observer)
        }
    }

    var showInteractionOverlay by remember { mutableStateOf(false) } // TODO: should this be saveable?
    var interactionMode by rememberSaveable(stateSaver = InteractionModeSaver) { mutableStateOf(CelestiaInteraction.InteractionMode.Object) }
    var alert by remember { mutableStateOf<CelestiaAlert?>(null) }
    var systemAccessDeferred by remember { mutableStateOf<CompletableDeferred<Int>?>(null) }

    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val context = LocalContext.current

    val hasRegularHorizontalSpace = with(density) { LocalWindowInfo.current.containerSize.width.toDp() } > SHEET_MAX_FULL_WIDTH_DP.dp

    val systemBars = WindowInsets.systemBars
    val displayCutout = WindowInsets.displayCutout

    val left = max(systemBars.getLeft(density, layoutDirection), displayCutout.getLeft(density, layoutDirection))
    val top = max(systemBars.getTop(density), displayCutout.getTop(density))
    val right = max(systemBars.getRight(density, layoutDirection), displayCutout.getRight(density, layoutDirection))
    val bottom = max(systemBars.getBottom(density), displayCutout.getBottom(density))

    val roundedCorners = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val windowInsets = context.getSystemService(WindowManager::class.java).currentWindowMetrics.windowInsets
        RoundedCorners(
            windowInsets.getRoundedCorner(RoundedCorner.POSITION_TOP_LEFT)?.radius ?: 0,
            windowInsets.getRoundedCorner(RoundedCorner.POSITION_TOP_RIGHT)?.radius ?: 0,
            windowInsets.getRoundedCorner(RoundedCorner.POSITION_BOTTOM_LEFT)?.radius ?: 0,
            windowInsets.getRoundedCorner(RoundedCorner.POSITION_BOTTOM_RIGHT)?.radius ?: 0
        )
    } else {
        RoundedCorners(0, 0, 0, 0)
    }

    val safeAreaInsets = EdgeInsets(
        left = if (!hasRegularHorizontalSpace) left else max(left, max(roundedCorners.topLeft, roundedCorners.bottomLeft)),
        top = if (hasRegularHorizontalSpace) top else max(top, max(roundedCorners.topLeft, roundedCorners.topRight)),
        right = if (!hasRegularHorizontalSpace) right else max(right, max(roundedCorners.topRight, roundedCorners.bottomRight)),
        bottom = if (hasRegularHorizontalSpace) bottom else max(bottom, max(roundedCorners.bottomLeft, roundedCorners.bottomRight))
    )

    Box(modifier = Modifier.fillMaxSize()) {
        RendererScreen(
            pathToLoad = pathToLoad,
            cfgToLoad = cfgToLoad,
            addonDirsToLoad = addonDirsToLoad,
            languageOverride = languageOverride,
            safeAreaInsets = safeAreaInsets,
            frameRateOptionEvents = frameRateOptionEvents,
            reapplyContentScaleEvents = reapplyContentScaleEvents,
            celestiaRendererReady = {
                showInteractionOverlay = true
            },
            celestiaRendererLoadingFromFallback = {
                scope.launch {
                    alert = CelestiaAlert.ErrorLoadingData
                }
            }
        )

        if (showInteractionOverlay) {
            AndroidView(factory = { context ->
                val view = View(context)
                val interaction = CelestiaInteraction(
                    context = context,
                    appCore = viewModel.appCore,
                    renderer = viewModel.renderer,
                    executor = viewModel.executor,
                    interactionMode = interactionMode,
                    appSettings = viewModel.appSettings,
                    rendererSettings = viewModel.rendererSettings,
                    canAcceptKeyEvents = canAcceptKeyEvents,
                    showMenu = showMenu
                )

                // All interaction listeners on the container - no need for GL view reference
                @Suppress("ClickableViewAccessibility")
                view.setOnTouchListener { v, event ->
                    // showControlViewIfNeeded() // TODO: fix
                    interaction.onTouch(v, event)
                }

                // Make container focusable for keyboard/gamepad input
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    view.defaultFocusHighlightEnabled = false
                    view.isFocusable = true
                    view.isFocusableInTouchMode = true
                    view.requestFocus() // TODO: fix
                }
                view.setOnKeyListener(interaction)
                view.setOnGenericMotionListener(interaction)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val pointerCaptureListener = interaction.pointerCaptureListener as? View.OnCapturedPointerListener
                    if (pointerCaptureListener != null) {
                        view.setOnCapturedPointerListener(pointerCaptureListener)
                    }
                }
                view.setOnHoverListener(interaction)

                interaction.isReady = true
                viewInteraction = interaction
                return@AndroidView view
            }, modifier = Modifier.fillMaxSize(), update = {

            })

            // TODO: Test
            DisposableEffect(lifeCycleOwner) {
                val fatalErrorHandler = AppCore.FatalErrorHandler { message ->
                    scope.launch {
                        alert = CelestiaAlert.FatalError(message)
                    }
                }
                lifeCycleOwner.lifecycleScope.launch(viewModel.executor.asCoroutineDispatcher()) {
                    viewModel.appCore.setFatalErrorHandler(fatalErrorHandler)
                }
                onDispose {
                    lifeCycleOwner.lifecycleScope.launch(viewModel.executor.asCoroutineDispatcher()) {
                        viewModel.appCore.setFatalErrorHandler(null)
                    }
                }
            }

            // TODO: Test
            DisposableEffect(lifeCycleOwner) {
                val handler = AppCore.SystemAccessHandler {
                    val deferred = CompletableDeferred<Int>()
                    lifeCycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                        systemAccessDeferred = deferred
                    }
                    runBlocking { deferred.await() }
                }
                lifeCycleOwner.lifecycleScope.launch(viewModel.executor.asCoroutineDispatcher()) {
                    viewModel.appCore.setSystemAccessHandler(handler)
                }
                onDispose {
                    lifeCycleOwner.lifecycleScope.launch(viewModel.executor.asCoroutineDispatcher()) {
                        viewModel.appCore.setSystemAccessHandler(null)
                    }
                }
            }
        }
    }

    systemAccessDeferred?.let { deferred ->
        SimpleAlertDialog(
            onDismissRequest = {
                systemAccessDeferred = null
                deferred.complete(AppCore.SYSTEM_ACCESS_DENIED)
            },
            onConfirm = {
                systemAccessDeferred = null
                deferred.complete(AppCore.SYSTEM_ACCESS_GRANTED)
            },
            title = CelestiaString("Script System Access", "Alert title for scripts requesting system access")
        )
    }

    alert?.let { content ->
        when (content) {
            // TODO: Have a short title
            CelestiaAlert.ErrorLoadingData -> {
                SimpleAlertDialog(onDismissRequest = {
                    alert = null
                }, onConfirm = {
                    alert = null
                }, title = CelestiaString("Error loading data, fallback to original configuration.", ""))
            }
            // TODO: Have a short title
            is CelestiaAlert.FatalError -> {
                SimpleAlertDialog(onDismissRequest = {
                    alert = null
                }, onConfirm = {
                    alert = null
                }, title = content.message)
            }
        }
    }
}
