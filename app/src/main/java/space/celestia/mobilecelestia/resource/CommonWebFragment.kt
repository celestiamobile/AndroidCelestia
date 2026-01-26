package space.celestia.mobilecelestia.resource

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import androidx.fragment.compose.AndroidFragment
import androidx.lifecycle.lifecycleScope
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import space.celestia.celestia.AppCore
import space.celestia.celestiafoundation.resource.model.ResourceItem
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.EmptyHint
import space.celestia.mobilecelestia.resource.CommonWebFragment.Companion.ARG_CONTEXT_DIRECTORY
import space.celestia.mobilecelestia.resource.CommonWebFragment.Companion.ARG_FILTER_URL
import space.celestia.mobilecelestia.resource.CommonWebFragment.Companion.ARG_MATCHING_QUERY_KEYS
import space.celestia.mobilecelestia.resource.CommonWebFragment.Companion.ARG_URI
import space.celestia.mobilecelestia.resource.model.ResourceAPIService
import space.celestia.mobilecelestia.utils.CelestiaString
import java.io.File
import java.io.Serializable
import java.lang.ref.WeakReference
import javax.inject.Inject

sealed class WebError: Serializable {
    data object NotAvailable: WebError(), Serializable {
        private fun readResolve(): Any = NotAvailable
    }
    data object Loading: WebError(), Serializable {
        private fun readResolve(): Any = Loading
    }
}

@Composable
fun WebPage(uri: Uri, modifier: Modifier = Modifier, matchingQueryKeys: List<String> = listOf(), contextDirectory: File? = null, filterURL: Boolean = false, fallbackContent: (@Composable (Modifier, PaddingValues) -> Unit)? = null, paddingValues: PaddingValues, titleChanged: ((String) -> Unit)? = null, canGoBackChanged: ((Boolean) -> Unit)? = null, goBackRequest: ((() -> Unit) -> Unit)? = null, openAddon: ((ResourceItem) -> Unit)? = null) {
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
                AndroidFragment<CommonWebFragment>(
                    modifier = modifier.fillMaxSize().padding(paddingValues),
                    arguments = Bundle().apply {
                        putParcelable(ARG_URI, uri)
                        putSerializable(ARG_CONTEXT_DIRECTORY, contextDirectory)
                        putStringArrayList(ARG_MATCHING_QUERY_KEYS, ArrayList(matchingQueryKeys))
                        putBoolean(ARG_FILTER_URL, filterURL)
                    }
                ) { fragment ->
                    fragment.titleChanged = {
                        titleChanged?.invoke(it)
                    }
                    fragment.canGoBackChanged = {
                        canGoBackChanged?.invoke(it)
                    }
                    fragment.openAddon = {
                        openAddon?.invoke(it)
                    }
                    fragment.loadingFailed = {
                        error = it
                    }
                    fragment.initialLoadingFinished = {
                        initialLoadingFinished = true
                    }
                    goBackRequest?.invoke {
                        fragment.goBack()
                    }
                }
                if (!initialLoadingFinished) {
                    Box(modifier = modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@AndroidEntryPoint
class CommonWebFragment: Fragment(), CelestiaJavascriptInterface.MessageHandler {
    private lateinit var uri: Uri
    private lateinit var matchingQueryKeys: List<String>
    private var filterURL = true
    private var contextDirectory: File? = null

    private var listener: Listener? = null
    private var webView: WebView? = null
    private var webViewState: Bundle? = null
    private var initialLoadFinished = false

    @Inject
    lateinit var resourceAPI: ResourceAPIService

    private var onBackPressedCallback: OnBackPressedCallback? = null

    var titleChanged: ((String) -> Unit)? = null
    var canGoBackChanged: ((Boolean) -> Unit)? = null
    var initialLoadingFinished: (() -> Unit)? = null
    var loadingFailed: ((WebError) -> Unit)? = null
    var openAddon: ((ResourceItem) -> Unit)? = null

    interface Listener {
        fun onExternalWebLinkClicked(url: String)
        fun onRunScript(type: String, content: String, name: String?, location: String?, contextDirectory: File?)
        fun onShareURL(title: String, url: String)
        fun onReceivedACK(id: String)
        fun onRunDemo()
        fun onOpenSubscriptionPage(preferredPlayOfferId: String?)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        uri = BundleCompat.getParcelable(requireArguments(), ARG_URI, Uri::class.java)!!
        matchingQueryKeys = requireArguments().getStringArrayList(ARG_MATCHING_QUERY_KEYS) ?: listOf()
        contextDirectory = BundleCompat.getSerializable(requireArguments(), ARG_CONTEXT_DIRECTORY, File::class.java)
        filterURL = requireArguments().getBoolean(ARG_FILTER_URL, true)
        if (savedInstanceState != null) {
            initialLoadFinished = savedInstanceState.getBoolean(ARG_INITIAL_LOAD_FINISHED, false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return try {
            val view = inflater.inflate(R.layout.fragment_common_web, container, false)
            configureWebView(view, savedInstanceState)
            view
        } catch (ignored: Throwable) {
            loadingFailed?.invoke(WebError.NotAvailable)
            FrameLayout(requireContext())
        }
    }

    private fun configureWebView(view: View, savedInstanceState: Bundle?) {
        val webView = view.findViewById<WebView>(R.id.webview)
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

        val weakSelf = WeakReference(this)
        val defaultUri = uri
        val shouldFilterURL = filterURL
        val queryKeys = matchingQueryKeys
        webView.addJavascriptInterface(CelestiaJavascriptInterface(this), "AndroidCelestia")
        webView.webViewClient = object: WebViewClient() {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return shouldOverrideUrl(request?.url.toString())
            }

            @Deprecated("Deprecated in Java", ReplaceWith("shouldOverrideUrl(url)"))
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return shouldOverrideUrl(url)
            }

            fun isURLAllowed(uri: Uri): Boolean {
                if (!shouldFilterURL) {
                    return true
                }
                if (uri.host != defaultUri.host || uri.path != defaultUri.path) {
                    return false
                }
                for (key in queryKeys) {
                    if (uri.getQueryParameter(key) != defaultUri.getQueryParameter(key)) {
                        return false
                    }
                }
                return true
            }

            fun shouldOverrideUrl(url: String?): Boolean {
                if (url == null) return true
                val uri = url.toUri()
                if (isURLAllowed(uri))
                    return false
                weakSelf.get()?.listener?.onExternalWebLinkClicked(url)
                return true
            }

            override fun onPageCommitVisible(view: WebView?, url: String?) {
                val wv = view ?: return
                val self = weakSelf.get() ?: return
                self.initialLoadFinished = true
                self.initialLoadingFinished?.invoke()
                self.canGoBackChanged?.invoke(wv.canGoBack())
                self.onBackPressedCallback?.isEnabled = !self.isHidden && self.canGoBack()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                val self = weakSelf.get() ?: return
                self.titleChanged?.invoke(view?.title ?: "")
            }

            @Deprecated("Deprecated in Java")
            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                @Suppress("DEPRECATION")
                super.onReceivedError(view, errorCode, description, failingUrl)
                onError()
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request != null && request.isForMainFrame)
                    onError()
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                super.onReceivedHttpError(view, request, errorResponse)
                if (request != null && request.isForMainFrame)
                    onError()
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                super.onReceivedSslError(view, handler, error)
                onError()
            }

            fun onError() {
                loadingFailed?.invoke(WebError.Loading)
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                val self = weakSelf.get() ?: return
                self.titleChanged?.invoke(title ?: "")
            }
        }
        this.webView = webView

        val savedState = webViewState ?: savedInstanceState
        if (savedState != null) {
            webView.restoreState(savedState)
        } else {
            webView.loadUrl(uri.toString())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val savedState = webViewState ?: savedInstanceState
        val wv = webView
        canGoBackChanged?.invoke(savedState != null && wv != null && wv.canGoBack())

        if (onBackPressedCallback == null) {
            val weakSelf = WeakReference(this)
            val backPressedCallback = object: OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val self = weakSelf.get() ?: return
                    if (self.canGoBack())
                        self.goBack()
                }
            }
            requireActivity().onBackPressedDispatcher.addCallback(backPressedCallback)
            onBackPressedCallback = backPressedCallback
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

        onBackPressedCallback?.isEnabled = !hidden && canGoBack()
    }

    override fun onStop() {
        super.onStop()

        onBackPressedCallback?.isEnabled = false
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement CommonWebFragment.Listener")
        }
    }

    override fun onDestroyView() {
        val webView = this.webView
        if (webView != null) {
            val bundle = Bundle().apply {
                webView.saveState(this)
            }
            webViewState = bundle
        }
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()

        onBackPressedCallback?.remove()
        onBackPressedCallback = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        webView?.saveState(outState)
        outState.putBoolean(ARG_INITIAL_LOAD_FINISHED, initialLoadFinished)
        super.onSaveInstanceState(outState)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun runScript(type: String, content: String, scriptName: String?, scriptLocation: String?) {
        listener?.onRunScript(type, content, scriptName, scriptLocation, contextDirectory)
    }

    override fun shareURL(title: String, url: String) {
        listener?.onShareURL(title, url)
    }

    override fun receivedACK(id: String) {
        listener?.onReceivedACK(id)
    }

    override fun openAddonNext(id: String) {
        val lang = AppCore.getLanguage()
        lifecycleScope.launch {
            try {
                val result = resourceAPI.item(lang, id)
                openAddon?.invoke(result)
            } catch (ignored: Throwable) {}
        }
    }

    override fun runDemo() {
        listener?.onRunDemo()
    }

    override fun openSubscriptionPage(preferredPlayOfferId: String?) {
        listener?.onOpenSubscriptionPage(preferredPlayOfferId)
    }

    fun canGoBack(): Boolean {
        return webView?.canGoBack() ?: false
    }

    fun goBack() {
        webView?.goBack()
    }

    companion object {
        const val ARG_URI = "uri"
        const val ARG_MATCHING_QUERY_KEYS = "query_keys"
        const val ARG_CONTEXT_DIRECTORY = "context_directory"
        const val ARG_FILTER_URL = "filter_url"
        private const val ARG_INITIAL_LOAD_FINISHED = "initial_load_finished"
    }
}