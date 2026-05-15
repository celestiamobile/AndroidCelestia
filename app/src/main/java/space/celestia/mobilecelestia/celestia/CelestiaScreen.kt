package space.celestia.mobilecelestia.celestia

import android.animation.ObjectAnimator
import android.os.Build
import android.view.RoundedCorner
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingtoolbar.FloatingToolbarLayout
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import space.celestia.celestia.AppCore
import space.celestia.celestiaui.compose.SimpleAlertDialog
import space.celestia.celestiaui.settings.ToolbarAction
import space.celestia.celestiaui.settings.toolbarItems
import space.celestia.celestiaui.utils.AppStatusReporter
import space.celestia.celestiaui.utils.CelestiaString
import space.celestia.mobilecelestia.celestia.viewmodel.RendererViewModel
import space.celestia.mobilecelestia.common.EdgeInsets
import space.celestia.mobilecelestia.common.RoundedCorners
import space.celestia.mobilecelestia.common.SHEET_MAX_FULL_WIDTH_DP
import java.util.Timer
import kotlin.concurrent.fixedRateTimer
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
fun CelestiaScreen(pathToLoad: String, cfgToLoad: String, addonDirsToLoad: List<String>, languageOverride: String, frameRateOptionEvents: Flow<Int>, reapplyContentScaleEvents: Flow<Unit>, canAcceptKeyEvents: () -> Boolean, showMenu: () -> Unit, showInfo: () -> Unit, showSearch: () -> Unit, goTo: () -> Unit, onInteractionModeChanged: (CelestiaInteraction.InteractionMode) -> Unit, onInteractionViewReady: (View) -> Unit) {
    val lifeCycleOwner = LocalLifecycleOwner.current
    val viewModel: RendererViewModel = hiltViewModel()
    var currentState by rememberSaveable { mutableStateOf(viewModel.appStatusReporter.state) }
    if (currentState == AppStatusReporter.State.LOADING_FAILURE || currentState == AppStatusReporter.State.EXTERNAL_LOADING_FAILURE) {
        return
    }

    var viewInteraction: CelestiaInteraction? by remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope()

    val controlViewState = remember {
        object {
            var isVisible = false
            var hideAnimator: ObjectAnimator? = null
            var showAnimator: ObjectAnimator? = null
            var floatingToolbar: FloatingToolbarLayout? = null
            var zoomTimer: Timer? = null
        }
    }

    fun showControlView() {
        val toolbar = controlViewState.floatingToolbar ?: return
        if (controlViewState.showAnimator != null) return
        controlViewState.hideAnimator?.let { it.cancel(); controlViewState.hideAnimator = null }
        toolbar.isVisible = true
        val animator = ObjectAnimator.ofFloat(toolbar, View.ALPHA, 0f, 1f)
        animator.duration = 200
        animator.doOnEnd {
            controlViewState.showAnimator = null
            controlViewState.isVisible = true
        }
        animator.start()
        controlViewState.showAnimator = animator
    }

    fun hideControlView() {
        val toolbar = controlViewState.floatingToolbar ?: return
        if (controlViewState.hideAnimator != null) return
        controlViewState.showAnimator?.let { it.cancel(); controlViewState.showAnimator = null }
        val animator = ObjectAnimator.ofFloat(toolbar, View.ALPHA, 1f, 0f)
        animator.duration = 200
        animator.doOnEnd {
            controlViewState.hideAnimator = null
            controlViewState.isVisible = false
            toolbar.isVisible = false
        }
        animator.start()
        controlViewState.hideAnimator = animator
    }

    fun showControlViewIfNeeded() { if (!controlViewState.isVisible) showControlView() }
    fun hideControlViewIfNeeded() { if (controlViewState.isVisible) hideControlView() }
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

    var showInteractionOverlay by remember { mutableStateOf(false) }
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
                    showControlViewIfNeeded()
                    interaction.onTouch(v, event)
                }

                // Make container focusable for keyboard/gamepad input
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    view.defaultFocusHighlightEnabled = false
                    view.isFocusable = true
                    view.isFocusableInTouchMode = true
                    view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                        override fun onViewAttachedToWindow(v: View) {
                            v.requestFocus()
                            v.removeOnAttachStateChangeListener(this)
                        }
                        override fun onViewDetachedFromWindow(v: View) {}
                    })
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
                onInteractionViewReady(view)
                return@AndroidView view
            }, modifier = Modifier.fillMaxSize(), update = {})

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = with(density) { safeAreaInsets.top.toDp() },
                        end = with(density) { (if (layoutDirection == LayoutDirection.Rtl) safeAreaInsets.left else safeAreaInsets.right).toDp() } + 8.dp,
                        bottom = with(density) { safeAreaInsets.bottom.toDp() }
                    ),
                contentAlignment = Alignment.CenterEnd
            ) {
                AndroidView(factory = { ctx ->
                    val floatingToolbar = FloatingToolbarLayout(ctx)
                    val controlView = CelestiaControlView(ctx)
                    floatingToolbar.addView(controlView, FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))

                    val hasCelestiaPlus = viewModel.purchaseManager.canUseInAppPurchase() && viewModel.purchaseManager.purchaseToken() != null
                    val actions = ArrayList(if (hasCelestiaPlus) viewModel.appSettings.toolbarItems ?: ToolbarAction.defaultItems else ToolbarAction.defaultItems)
                    if (!actions.contains(ToolbarAction.Menu)) actions.add(ToolbarAction.Menu)
                    val buttonMap = hashMapOf(
                        ToolbarAction.Mode to CelestiaControlButton.Toggle(space.celestia.celestiaui.R.drawable.control_mode_combined, CelestiaControlAction.ToggleModeToObject, CelestiaControlAction.ToggleModeToCamera, contentDescription = CelestiaString("Toggle Interaction Mode", "Touch interaction mode"), currentState = interactionMode == CelestiaInteraction.InteractionMode.Camera),
                        ToolbarAction.ZoomIn to CelestiaControlButton.Press(space.celestia.celestiaui.R.drawable.control_zoom_in, CelestiaControlAction.ZoomIn, CelestiaString("Zoom In", "")),
                        ToolbarAction.ZoomOut to CelestiaControlButton.Press(space.celestia.celestiaui.R.drawable.control_zoom_out, CelestiaControlAction.ZoomOut, CelestiaString("Zoom Out", "")),
                        ToolbarAction.Info to CelestiaControlButton.Tap(space.celestia.celestiaui.R.drawable.control_info, CelestiaControlAction.Info, CelestiaString("Get Info", "Action for getting info about current selected object")),
                        ToolbarAction.Search to CelestiaControlButton.Tap(space.celestia.celestiaui.R.drawable.control_search, CelestiaControlAction.Search, CelestiaString("Search", "")),
                        ToolbarAction.Menu to CelestiaControlButton.Tap(space.celestia.celestiaui.R.drawable.control_action_menu, CelestiaControlAction.ShowMenu, CelestiaString("Menu", "Menu button")),
                        ToolbarAction.Hide to CelestiaControlButton.Tap(space.celestia.celestiaui.R.drawable.control_close, CelestiaControlAction.Hide, CelestiaString("Hide", "Action to hide the tool overlay")),
                        ToolbarAction.Go to CelestiaControlButton.Tap(space.celestia.celestiaui.R.drawable.control_go, CelestiaControlAction.Go, CelestiaString("Go", "Go to an object"))
                    )
                    controlView.buttons = actions.mapNotNull { buttonMap[it] }

                    controlView.listener = object : CelestiaControlView.Listener {
                        override fun didTapAction(action: CelestiaControlAction) {
                            if (!controlViewState.isVisible) return
                            when (action) {
                                CelestiaControlAction.ShowMenu -> showMenu()
                                CelestiaControlAction.Info -> showInfo()
                                CelestiaControlAction.Search -> showSearch()
                                CelestiaControlAction.Hide -> hideControlViewIfNeeded()
                                CelestiaControlAction.Show -> showControlViewIfNeeded()
                                CelestiaControlAction.Go -> goTo()
                                else -> {}
                            }
                        }
                        override fun didToggleToMode(action: CelestiaControlAction) {
                            if (!controlViewState.isVisible) return
                            when (action) {
                                CelestiaControlAction.ToggleModeToCamera -> {
                                    interactionMode = CelestiaInteraction.InteractionMode.Camera
                                    viewInteraction?.setInteractionMode(CelestiaInteraction.InteractionMode.Camera)
                                    onInteractionModeChanged(CelestiaInteraction.InteractionMode.Camera)
                                }
                                CelestiaControlAction.ToggleModeToObject -> {
                                    interactionMode = CelestiaInteraction.InteractionMode.Object
                                    viewInteraction?.setInteractionMode(CelestiaInteraction.InteractionMode.Object)
                                    onInteractionModeChanged(CelestiaInteraction.InteractionMode.Object)
                                }
                                else -> {}
                            }
                        }
                        override fun didStartPressingAction(action: CelestiaControlAction) {
                            if (!controlViewState.isVisible) return
                            val interaction = viewInteraction ?: return
                            when (action) {
                                CelestiaControlAction.ZoomIn -> { interaction.zoomMode = CelestiaInteraction.ZoomMode.In; interaction.callZoom() }
                                CelestiaControlAction.ZoomOut -> { interaction.zoomMode = CelestiaInteraction.ZoomMode.Out; interaction.callZoom() }
                                else -> {}
                            }
                            controlViewState.zoomTimer?.cancel()
                            controlViewState.zoomTimer = fixedRateTimer("zoom", false, 0, 100) {
                                viewInteraction?.callZoom()
                            }
                        }
                        override fun didEndPressingAction(action: CelestiaControlAction) {
                            if (!controlViewState.isVisible) return
                            controlViewState.zoomTimer?.cancel()
                            controlViewState.zoomTimer = null
                            viewInteraction?.zoomMode = null
                        }
                    }

                    controlViewState.floatingToolbar = floatingToolbar
                    controlViewState.isVisible = true
                    floatingToolbar
                })
            }

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

            DisposableEffect(lifeCycleOwner) {
                onDispose {
                    controlViewState.zoomTimer?.cancel()
                    controlViewState.zoomTimer = null
                    viewInteraction?.zoomMode = null
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
            title = CelestiaString("Script System Access", "Alert title for scripts requesting system access"),
            text = CelestiaString("This script requests permission to read/write files and execute external programs. Allowing this can be dangerous.\nDo you trust the script and want to allow this?", "Alert message for scripts requesting system access"),
            showCancel = true
        )
    }

    alert?.let { content ->
        when (content) {
            CelestiaAlert.ErrorLoadingData -> {
                SimpleAlertDialog(onDismissRequest = {
                    alert = null
                }, onConfirm = {
                    alert = null
                }, title = CelestiaString("Error Loading Data", ""), text = CelestiaString("Error loading data, fallback to original configuration.", ""))
            }
            is CelestiaAlert.FatalError -> {
                SimpleAlertDialog(onDismissRequest = {
                    alert = null
                }, onConfirm = {
                    alert = null
                }, title = CelestiaString("Fatal Error", "Error for fatal error alert title"), text = content.message)
            }
        }
    }
}
