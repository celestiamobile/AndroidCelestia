package space.celestia.mobilecelestia.resource

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.webkit.CookieManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import space.celestia.celestia.AppCore
import space.celestia.celestiafoundation.resource.model.ResourceItem
import space.celestia.celestiafoundation.utils.commonHandler
import space.celestia.mobilecelestia.compose.EmptyHint
import space.celestia.mobilecelestia.compose.WebView
import space.celestia.mobilecelestia.compose.WebViewNavigator
import space.celestia.mobilecelestia.compose.WebViewState
import space.celestia.mobilecelestia.compose.rememberWebViewNavigator
import space.celestia.mobilecelestia.resource.model.ResourceAPI
import space.celestia.mobilecelestia.resource.viewmodel.AddonViewModel
import space.celestia.mobilecelestia.resource.viewmodel.WebViewModel
import space.celestia.mobilecelestia.utils.CelestiaString
import java.io.File
import java.util.UUID

enum class WebViewLoadState {
    Loading, Loaded, Error;
}

@Composable
fun SingleWebScreen(
    webViewState: WebViewState,
    modifier: Modifier = Modifier,
    contextDirectory: File? = null,
    shareURLHandler: (String, String) -> Unit,
    receivedACKHandler: (String) -> Unit,
    openSubscriptionPageHandler: () -> Unit,
    openExternalWebLink: (String) -> Unit,
    fallback: (@Composable () -> Unit)? = null,
    paddingValues: PaddingValues
) {
    WebScreen(
        webViewState = webViewState,
        contextDirectory = contextDirectory,
        openSubscriptionPageHandler = openSubscriptionPageHandler,
        openExternalWebLink = openExternalWebLink,
        shareURLHandler = shareURLHandler,
        receivedACKHandler = receivedACKHandler,
        openAddonNextHandler = {},
        fallback = fallback,
        paddingValues = paddingValues,
        modifier = modifier
    )
}

@Serializable
data object WebRoot

@Serializable
data class WebAddon(val id: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebNavigationScreen(
    webViewState: WebViewState,
    navigator: WebViewNavigator,
    shareURLHandler: (String, String) -> Unit,
    receivedACKHandler: (String) -> Unit,
    openSubscriptionPageHandler: () -> Unit,
    openExternalWebLink: (String) -> Unit,
    shareAddon: (String, String) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val addonViewModel: AddonViewModel = hiltViewModel()
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    var showAddon by remember { mutableStateOf(false) }
    var addonTitle by remember { mutableStateOf("") }
    var canShare by remember { mutableStateOf(false) }
    var canPop by remember { mutableStateOf(false) }

    val viewModel: AddonViewModel = hiltViewModel()

    LaunchedEffect(navigator) {
        snapshotFlow { navigator.canGoBack }
            .distinctUntilChanged()
            .collect {
                if (!showAddon)
                    canPop = it
            }
    }

    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { controller, _, _ ->
            val currentBackStackEntry = controller.currentBackStackEntry
            if (currentBackStackEntry == null) {
                showAddon = false
                canShare = false
                canPop = false
            } else if (currentBackStackEntry.destination.hasRoute<WebRoot>()) {
                showAddon = false
                canShare = false
                canPop = navigator.canGoBack
            } else if (currentBackStackEntry.destination.hasRoute<WebAddon>()) {
                addonTitle = requireNotNull(viewModel.addonMap[currentBackStackEntry.toRoute<WebAddon>().id]).name
                showAddon = true
                canShare = true
                canPop = true
            }
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    Scaffold(topBar = {
        TopAppBar(title = {
            Text(if (showAddon) addonTitle else webViewState.pageTitle ?: "")
        }, navigationIcon = {
            if (canPop) {
                IconButton(onClick = {
                    if (showAddon) {
                        navController.navigateUp()
                    } else {
                        navigator.navigateBack()
                    }
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = ""
                    )
                }
            }
        }, actions = {
            if (canShare) {
                IconButton(onClick = {
                    val currentBackStackEntry = navController.currentBackStackEntry
                    if (currentBackStackEntry != null && currentBackStackEntry.destination.hasRoute<WebAddon>()) {
                        val addon = requireNotNull(viewModel.addonMap[currentBackStackEntry.toRoute<WebAddon>().id])
                        shareAddon(addon.name, addon.id)
                    }
                }) {
                    Icon(imageVector = Icons.Filled.Share, contentDescription = "")
                }
            }
        }, scrollBehavior = scrollBehavior)
    }) { paddingValues ->
        NavHost(navController = navController, startDestination = WebRoot) {
            composable<WebRoot> {
                WebScreen(
                    webViewState = webViewState,
                    navigator = navigator,
                    openSubscriptionPageHandler = openSubscriptionPageHandler,
                    openExternalWebLink = openExternalWebLink,
                    shareURLHandler = shareURLHandler,
                    receivedACKHandler = receivedACKHandler,
                    openAddonNextHandler = {
                        scope.launch {
                            try {
                                val result = addonViewModel.resourceAPI.item(AppCore.getLanguage(), it).commonHandler(
                                    ResourceItem::class.java, ResourceAPI.gson)
                                addonViewModel.addonMap[it] = result
                                navController.navigate(WebAddon(result.id))
                            } catch (ignored: Throwable) {}
                        }
                    },
                    paddingValues = paddingValues,
                    modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection)
                )
            }
            composable<WebAddon> {
                val addon = requireNotNull(addonViewModel.addonMap[it.toRoute<WebAddon>().id])
                AddonScreen(
                    addon = addon,
                    shareURLHandler = shareURLHandler,
                    receivedACKHandler = receivedACKHandler,
                    openSubscriptionPageHandler = openSubscriptionPageHandler,
                    openExternalWebLink = openExternalWebLink,
                    paddingValues = paddingValues,
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                )
            }
        }
    }
}

@Composable
private fun WebScreen(
    webViewState: WebViewState,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
    navigator: WebViewNavigator = rememberWebViewNavigator(),
    contextDirectory: File? = null,
    shareURLHandler: (String, String) -> Unit,
    receivedACKHandler: (String) -> Unit,
    openAddonNextHandler: (String) -> Unit,
    openSubscriptionPageHandler: () -> Unit,
    openExternalWebLink: (String) -> Unit,
    fallback: (@Composable () -> Unit)? = null,
) {
    // https://stackoverflow.com/questions/43917214/android-how-to-check-if-webview-is-available
    val hasWebView = runCatching { CookieManager.getInstance() }.isSuccess
    if (!hasWebView) {
        Box(modifier = modifier
            .padding(paddingValues), contentAlignment = Alignment.Center) {
            EmptyHint(text = CelestiaString("WebView is not available.", "WebView component is missing or disabled"))
        }
        return
    }

    val viewModel: WebViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    val localContext = LocalContext.current

    var loadState: WebViewLoadState by rememberSaveable { mutableStateOf(WebViewLoadState.Loading) }

    Box(modifier = modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
        if (loadState == WebViewLoadState.Loading) {
            CircularProgressIndicator()
        } else if (loadState == WebViewLoadState.Error && fallback != null) {
            fallback.invoke()
        }

        WebView(
            state = webViewState,
            modifier = Modifier.fillMaxSize().alpha(if (loadState == WebViewLoadState.Loaded || (loadState == WebViewLoadState.Error && fallback == null)) 1f else 0f),
            navigator = navigator,
            onPageCommitVisible = {
                loadState = WebViewLoadState.Loaded
            },
            onError = {
                loadState = WebViewLoadState.Error
            },
            onURLBlocked =  openExternalWebLink,
            onCreated = { webView ->
                webView.setBackgroundColor(Color.TRANSPARENT)
                webView.isHorizontalScrollBarEnabled = false

                val webSettings = webView.settings
                @SuppressLint("SetJavaScriptEnabled")
                webSettings.javaScriptEnabled = true
                webSettings.domStorageEnabled = true

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                        WebSettingsCompat.setAlgorithmicDarkeningAllowed(webSettings, false)
                    }
                }
                val handler = CelestiaJavascriptInterface(object: CelestiaJavascriptInterface.MessageHandler {
                    override fun runScript(
                        type: String,
                        content: String,
                        scriptName: String?,
                        scriptLocation: String?
                    ) {
                        scope.launch {
                            runScript(context = localContext, type = type, content = content, name = scriptName, location = scriptLocation, contextDirectory = contextDirectory, viewModel = viewModel)
                        }
                    }

                    override fun shareURL(title: String, url: String) {
                        shareURLHandler(title, url)
                    }

                    override fun receivedACK(id: String) {
                        receivedACKHandler(id)
                    }

                    override fun openAddonNext(id: String) {
                        openAddonNextHandler(id)
                    }

                    override fun runDemo() {
                        scope.launch(viewModel.executor.asCoroutineDispatcher()) { viewModel.appCore.runDemo()  }
                    }

                    override fun openSubscriptionPage() {
                        openSubscriptionPageHandler()
                    }
                })
                webView.addJavascriptInterface(handler, "AndroidCelestia")
            }
        )
    }
}

private suspend fun runScript(context: Context, type: String, content: String, name: String?, location: String?, contextDirectory: File?, viewModel: WebViewModel) {
    if (!listOf("cel", "celx").contains(type)) return
    val supportedScriptLocations = listOf("temp", "context")
    if (location != null && !supportedScriptLocations.contains(location)) return
    if (location == "context" && contextDirectory == null) return

    val scriptFile: File
    val scriptFileName = "${name ?: UUID.randomUUID()}.${type}"
    scriptFile = if (location == "context") {
        File(contextDirectory, scriptFileName)
    } else {
        File(context.cacheDir, scriptFileName)
    }

    val success = withContext(Dispatchers.IO) {
        try {
            scriptFile.writeText(content)
            true
        } catch (ignored: Throwable) {
            false
        }
    }
    if (success) {
        withContext(viewModel.executor.asCoroutineDispatcher()) {
            viewModel.appCore.runScript(scriptFile.absolutePath)
        }
    }
}
