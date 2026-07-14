package space.celestia.celestiaui.resource

import android.annotation.SuppressLint
import android.graphics.Color
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import space.celestia.celestia.AppCore
import space.celestia.celestiafoundation.resource.model.ResourceItem
import space.celestia.celestiaui.compose.EmptyHint
import space.celestia.celestiaui.resource.viewmodel.WebPageViewModel
import space.celestia.celestiaui.utils.CelestiaString
import java.io.File

sealed class WebError: Parcelable {
    @Parcelize
    data object NotAvailable: WebError()
    @Parcelize
    data object Loading: WebError()
}

@Composable
fun WebPage(
    uri: Uri,
    modifier: Modifier = Modifier,
    matchingQueryKeys: List<String> = listOf(),
    contextDirectory: File? = null,
    filterURL: Boolean = false,
    fallbackContent: (@Composable (Modifier, PaddingValues) -> Unit)? = null,
    paddingValues: PaddingValues,
    titleChanged: ((String) -> Unit)? = null,
    canGoBackChanged: ((Boolean) -> Unit)? = null,
    goBackRequest: ((() -> Unit) -> Unit)? = null,
    openAddon: ((ResourceItem) -> Unit)? = null,
    runScript: ((type: String, content: String, name: String?, location: String?, contextDirectory: File?) -> Unit),
    shareURL: ((title: String, url: String) -> Unit),
    receivedACK: ((id: String) -> Unit),
    runDemo: (() -> Unit),
    openSubscriptionPage: ((preferredPlayOfferId: String?) -> Unit),
    externalLinkClicked: ((url: String) -> Unit),
) {
    val viewModel: WebPageViewModel = hiltViewModel()
    var error by rememberSaveable { mutableStateOf<WebError?>(null) }
    var initialLoadingFinished by rememberSaveable { mutableStateOf(false) }
    when (error) {
        is WebError.NotAvailable -> {
            Box(modifier = modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                EmptyHint(text = CelestiaString("WebView is not available.", "WebView component is missing or disabled"))
            }
        }
        is WebError.Loading -> {
            if (fallbackContent != null) {
                fallbackContent(modifier, paddingValues)
            } else {
                Box(modifier = modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    EmptyHint(text = CelestiaString("Failed to load page.", "WebView failed to load content"))
                }
            }
        }
        else -> {
            Box(modifier = modifier.fillMaxSize()) {
                ComposeWebView(
                    uri = uri,
                    matchingQueryKeys = matchingQueryKeys,
                    contextDirectory = contextDirectory,
                    filterURL = filterURL,
                    paddingValues = paddingValues,
                    titleChanged = titleChanged,
                    canGoBackChanged = canGoBackChanged,
                    goBackRequest = goBackRequest,
                    openAddon = openAddon,
                    runScript = runScript,
                    shareURL = shareURL,
                    receivedACK = receivedACK,
                    runDemo = runDemo,
                    openSubscriptionPage = openSubscriptionPage,
                    externalLinkClicked = externalLinkClicked,
                    viewModel = viewModel,
                    onError = { error = it },
                    onInitialLoadingFinished = { initialLoadingFinished = true }
                )
                if (!initialLoadingFinished) {
                    Box(modifier = modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun ComposeWebView(
    uri: Uri,
    matchingQueryKeys: List<String>,
    contextDirectory: File?,
    filterURL: Boolean,
    paddingValues: PaddingValues,
    titleChanged: ((String) -> Unit)?,
    canGoBackChanged: ((Boolean) -> Unit)?,
    goBackRequest: ((() -> Unit) -> Unit)?,
    openAddon: ((ResourceItem) -> Unit)?,
    runScript: ((String, String, String?, String?, File?) -> Unit),
    shareURL: ((String, String) -> Unit),
    receivedACK: ((String) -> Unit),
    runDemo: (() -> Unit),
    openSubscriptionPage: ((String?) -> Unit),
    externalLinkClicked: ((String) -> Unit),
    viewModel: WebPageViewModel,
    onError: (WebError) -> Unit,
    onInitialLoadingFinished: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val canGoBackState = remember { mutableStateOf(false) }
    val webViewRef = remember { mutableStateOf<WebView?>(null) }
    var savedWebViewState by rememberSaveable { mutableStateOf<Bundle?>(null) }
    val currentTitleChanged = rememberUpdatedState(titleChanged)
    val currentCanGoBackChanged = rememberUpdatedState(canGoBackChanged)
    val currentOpenAddon = rememberUpdatedState(openAddon)
    val currentRunScript = rememberUpdatedState(runScript)
    val currentShareURL = rememberUpdatedState(shareURL)
    val currentReceivedACK = rememberUpdatedState(receivedACK)
    val currentRunDemo = rememberUpdatedState(runDemo)
    val currentOpenSubscriptionPage = rememberUpdatedState(openSubscriptionPage)
    val currentExternalLinkClicked = rememberUpdatedState(externalLinkClicked)

    BackHandler(enabled = canGoBackState.value) {
        webViewRef.value?.goBack()
    }

    SideEffect {
        val ref = webViewRef.value
        goBackRequest?.invoke { ref?.goBack() }
    }

    val messageHandler = remember {
        object : CelestiaJavascriptInterface.MessageHandler {
            override fun runScript(type: String, content: String, scriptName: String?, scriptLocation: String?) {
                currentRunScript.value.invoke(type, content, scriptName, scriptLocation, contextDirectory)
            }
            override fun shareURL(title: String, url: String) {
                currentShareURL.value.invoke(title, url)
            }
            override fun receivedACK(id: String) {
                currentReceivedACK.value.invoke(id)
            }
            override fun openAddonNext(id: String) {
                val lang = AppCore.getLanguage()
                coroutineScope.launch {
                    try {
                        val result = viewModel.resourceAPI.item(lang, id)
                        currentOpenAddon.value?.invoke(result)
                    } catch (_: Throwable) {}
                }
            }
            override fun runDemo() {
                currentRunDemo.value.invoke()
            }
            override fun openSubscriptionPage(preferredPlayOfferId: String?) {
                currentOpenSubscriptionPage.value.invoke(preferredPlayOfferId)
            }
        }
    }

    AndroidView(
        factory = { context ->
            val webView = try {
                WebView(context)
            } catch (_: Throwable) {
                onError(WebError.NotAvailable)
                return@AndroidView FrameLayout(context)
            }
            webView.apply {
                setBackgroundColor(Color.TRANSPARENT)
                isHorizontalScrollBarEnabled = false
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                        WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, false)
                    }
                }
                // Zero out insets to avoid double-insets with Compose padding
                ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets -> insets }

                addJavascriptInterface(CelestiaJavascriptInterface(messageHandler), "AndroidCelestia")

                val shouldFilterURL = filterURL
                val queryKeys = matchingQueryKeys
                val defaultUri = uri

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?) =
                        shouldOverrideUrl(request?.url.toString())

                    @Deprecated("Deprecated in Java")
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?) =
                        shouldOverrideUrl(url)

                    fun isURLAllowed(testUri: Uri): Boolean {
                        if (!shouldFilterURL) return true
                        if (testUri.host != defaultUri.host || testUri.path != defaultUri.path) return false
                        for (key in queryKeys) {
                            if (testUri.getQueryParameter(key) != defaultUri.getQueryParameter(key)) return false
                        }
                        return true
                    }

                    fun shouldOverrideUrl(url: String?): Boolean {
                        if (url == null) return true
                        val navUri = url.toUri()
                        if (isURLAllowed(navUri)) return false
                        currentExternalLinkClicked.value.invoke(url)
                        return true
                    }

                    override fun onPageCommitVisible(view: WebView?, url: String?) {
                        val wv = view ?: return
                        onInitialLoadingFinished()
                        canGoBackState.value = wv.canGoBack()
                        currentCanGoBackChanged.value?.invoke(wv.canGoBack())
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        currentTitleChanged.value?.invoke(view?.title ?: "")
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                        @Suppress("DEPRECATION")
                        super.onReceivedError(view, errorCode, description, failingUrl)
                        onError(WebError.Loading)
                    }

                    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, err: WebResourceError?) {
                        super.onReceivedError(view, request, err)
                        if (request?.isForMainFrame == true) onError(WebError.Loading)
                    }

                    override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
                        super.onReceivedHttpError(view, request, errorResponse)
                        if (request?.isForMainFrame == true) onError(WebError.Loading)
                    }

                    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, err: SslError?) {
                        super.onReceivedSslError(view, handler, err)
                        onError(WebError.Loading)
                    }
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onReceivedTitle(view: WebView?, title: String?) {
                        super.onReceivedTitle(view, title)
                        currentTitleChanged.value?.invoke(title ?: "")
                    }
                }

                webViewRef.value = this
                val saved = savedWebViewState
                if (saved != null) {
                    restoreState(saved)
                    canGoBackState.value = canGoBack()
                    currentCanGoBackChanged.value?.invoke(canGoBack())
                    currentTitleChanged.value?.invoke(title ?: "")
                } else {
                    loadUrl(uri.toString())
                }
            }
        },
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        onRelease = {
            webViewRef.value?.let { wv ->
                savedWebViewState = Bundle().also { wv.saveState(it) }
                wv.destroy()
            }
            webViewRef.value = null
        }
    )
}