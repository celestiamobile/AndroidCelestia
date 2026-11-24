package space.celestia.mobilecelestia.celestia

import android.os.Build
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import space.celestia.celestia.AppCore
import space.celestia.celestiaui.compose.SimpleAlertDialog
import space.celestia.celestiaui.settings.ToolbarAction
import space.celestia.celestiaui.settings.toolbarItems
import space.celestia.celestiaui.utils.CelestiaString
import space.celestia.celestiaui.utils.PreferenceManager
import space.celestia.mobilecelestia.celestia.viewmodel.RendererViewModel
import space.celestia.mobilecelestia.common.EdgeInsets

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
@OptIn(ExperimentalMaterial3Api::class)
fun CelestiaScreen(controlViewVisible: Boolean, onControlViewVisibilityChanged: (Boolean) -> Unit, safeAreaInsets: EdgeInsets, rendererSafeAreaInsets: EdgeInsets, pathToLoad: String, cfgToLoad: String, addonDirsToLoad: List<String>, languageOverride: String, frameRateOptionEvents: Flow<Int>, reapplyContentScaleEvents: Flow<Unit>, canAcceptKeyEvents: () -> Boolean, showMenu: () -> Unit, showInfo: () -> Unit, showSearch: () -> Unit, goTo: () -> Unit, onInteractionModeChanged: (CelestiaInteraction.InteractionMode) -> Unit, onInteractionViewReady: (View) -> Unit) {
    val lifeCycleOwner = LocalLifecycleOwner.current
    val viewModel: RendererViewModel = hiltViewModel()

    var viewInteraction: CelestiaInteraction? by remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope()

    var zoomJob by remember { mutableStateOf<Job?>(null) }

    val isControlViewVisible by rememberUpdatedState(controlViewVisible)
    val setControlViewVisible by rememberUpdatedState(onControlViewVisibilityChanged)
    val restoreHintState = rememberTooltipState(isPersistent = true)
    var showRestoreHint by rememberSaveable { mutableStateOf(false) }

    fun hideControlViewIfNeeded() {
        setControlViewVisible(false)
        if (viewModel.appSettings[PreferenceManager.PredefinedKey.ControlViewRestoreHintAcknowledged] != "true") {
            showRestoreHint = true
        }
    }

    LaunchedEffect(isControlViewVisible, showRestoreHint) {
        if (!isControlViewVisible && showRestoreHint) {
            try {
                restoreHintState.show()
            } finally {
                showRestoreHint = false
            }
        } else {
            restoreHintState.dismiss()
        }
    }

    var showInteractionOverlay by remember { mutableStateOf(false) }
    var interactionMode by rememberSaveable(stateSaver = InteractionModeSaver) { mutableStateOf(CelestiaInteraction.InteractionMode.Object) }
    var alert by remember { mutableStateOf<CelestiaAlert?>(null) }
    var systemAccessDeferred by remember { mutableStateOf<CompletableDeferred<Int>?>(null) }

    fun stopZoom(mode: CelestiaInteraction.ZoomMode? = null) {
        if (mode != null && viewInteraction?.zoomMode != mode) return
        zoomJob?.cancel()
        zoomJob = null
        viewInteraction?.zoomMode = null
    }

    fun startZoom(mode: CelestiaInteraction.ZoomMode) {
        if (!isControlViewVisible) return
        val interaction = viewInteraction ?: return
        stopZoom()
        interaction.zoomMode = mode
        interaction.callZoom()
        zoomJob = scope.launch {
            while (isActive) {
                delay(100)
                interaction.callZoom()
            }
        }
    }

    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    Box(modifier = Modifier.fillMaxSize()) {
        RendererScreen(
            pathToLoad = pathToLoad,
            cfgToLoad = cfgToLoad,
            addonDirsToLoad = addonDirsToLoad,
            languageOverride = languageOverride,
            safeAreaInsets = rendererSafeAreaInsets,
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

            AnimatedVisibility(
                visible = isControlViewVisible,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(200)),
                modifier = Modifier.fillMaxSize()
            ) {
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
                    val hasCelestiaPlus = viewModel.purchaseManager.canUseInAppPurchase() && viewModel.purchaseManager.purchaseToken() != null
                    val actions = (if (hasCelestiaPlus) viewModel.appSettings.toolbarItems ?: ToolbarAction.defaultItems else ToolbarAction.defaultItems).toMutableList()
                    if (!actions.contains(ToolbarAction.Menu)) actions.add(ToolbarAction.Menu)
                    CelestiaControlToolbar(
                        actions = actions,
                        interactionMode = interactionMode,
                        onInteractionModeChanged = { mode ->
                            interactionMode = mode
                            viewInteraction?.setInteractionMode(mode)
                            onInteractionModeChanged(mode)
                        },
                        onZoomStarted = ::startZoom,
                        onZoomEnded = ::stopZoom,
                        onAction = { action ->
                            when (action) {
                                ToolbarAction.Menu -> showMenu()
                                ToolbarAction.Info -> showInfo()
                                ToolbarAction.Search -> showSearch()
                                ToolbarAction.Hide -> hideControlViewIfNeeded()
                                ToolbarAction.Go -> goTo()
                                else -> {}
                            }
                        },
                        enabled = isControlViewVisible
                    )
                }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = with(density) { safeAreaInsets.top.toDp() } + 16.dp,
                        start = with(density) { (if (layoutDirection == LayoutDirection.Rtl) safeAreaInsets.right else safeAreaInsets.left).toDp() } + 16.dp,
                        end = with(density) { (if (layoutDirection == LayoutDirection.Rtl) safeAreaInsets.left else safeAreaInsets.right).toDp() } + 16.dp,
                        bottom = with(density) { safeAreaInsets.bottom.toDp() } + 16.dp
                    ),
                contentAlignment = Alignment.BottomCenter
            ) {
                val maximumHintHeight = maxHeight
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                    tooltip = {
                        // Reserve the standard action row and its bottom padding.
                        val minimumActionHeight = maxOf(
                            48.dp,
                            with(density) { MaterialTheme.typography.labelLarge.lineHeight.toDp() } + 16.dp
                        ) + 8.dp
                        var actionHeight by remember(minimumActionHeight) { mutableStateOf(minimumActionHeight) }
                        RichTooltip(
                            modifier = Modifier.heightIn(max = maximumHintHeight),
                            title = { Text(CelestiaString("Show the toolbar", "Title for the hidden toolbar hint")) },
                            action = {
                                TextButton(
                                    modifier = Modifier.onSizeChanged {
                                        actionHeight = maxOf(minimumActionHeight, with(density) { it.height.toDp() } + 8.dp)
                                    },
                                    onClick = {
                                        viewModel.appSettings[PreferenceManager.PredefinedKey.ControlViewRestoreHintAcknowledged] = "true"
                                        restoreHintState.dismiss()
                                    }
                                ) {
                                    Text(CelestiaString("Got it", "Dismiss the hidden toolbar hint"))
                                }
                            }
                        ) {
                            BoxWithConstraints {
                                Text(
                                    CelestiaString("The toolbar is hidden. Press your device's Back button to show the toolbar again. If you use gesture navigation, swipe inward from either side of the screen instead.", "Hint shown the first time the toolbar is hidden"),
                                    modifier = Modifier
                                        .heightIn(max = (maxHeight - actionHeight).coerceAtLeast(0.dp))
                                        .verticalScroll(rememberScrollState())
                                )
                            }
                        }
                    },
                    state = restoreHintState,
                    // Require acknowledgement instead of dismissing on outside touches or Back.
                    onDismissRequest = {},
                    focusable = true,
                    enableUserInput = false,
                    hasAction = true
                ) {
                    Box(Modifier.size(1.dp))
                }
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

            DisposableEffect(lifeCycleOwner) {
                var pendingDeferred: CompletableDeferred<Int>? = null
                val handler = AppCore.SystemAccessHandler {
                    val deferred = CompletableDeferred<Int>()
                    pendingDeferred = deferred
                    lifeCycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                        systemAccessDeferred = deferred
                    }
                    val result = runBlocking { deferred.await() }
                    pendingDeferred = null
                    result
                }
                lifeCycleOwner.lifecycleScope.launch(viewModel.executor.asCoroutineDispatcher()) {
                    viewModel.appCore.setSystemAccessHandler(handler)
                }
                onDispose {
                    pendingDeferred?.complete(AppCore.SYSTEM_ACCESS_DENIED)
                    lifeCycleOwner.lifecycleScope.launch(viewModel.executor.asCoroutineDispatcher()) {
                        viewModel.appCore.setSystemAccessHandler(null)
                    }
                }
            }

            DisposableEffect(isControlViewVisible, viewInteraction) {
                val interaction = viewInteraction
                onDispose {
                    zoomJob?.cancel()
                    zoomJob = null
                    interaction?.zoomMode = null
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
