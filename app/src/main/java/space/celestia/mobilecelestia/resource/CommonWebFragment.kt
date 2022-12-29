package space.celestia.mobilecelestia.resource

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.google.android.material.progressindicator.CircularProgressIndicator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import space.celestia.celestia.AppCore
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.common.replace
import space.celestia.mobilecelestia.resource.model.ResourceAPI
import space.celestia.mobilecelestia.resource.model.ResourceAPIService
import space.celestia.mobilecelestia.resource.model.ResourceItem
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.commonHandler
import space.celestia.mobilecelestia.utils.getParcelableValue
import space.celestia.mobilecelestia.utils.getSerializableValue
import java.io.File
import java.lang.ref.WeakReference
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
open class CommonWebFragment: NavigationFragment.SubFragment(), CelestiaJavascriptInterface.MessageHandler {
    private lateinit var uri: Uri
    private lateinit var matchingQueryKeys: List<String>
    private var filterURL = true
    private var contextDirectory: File? = null

    private var listener: Listener? = null
    private var webView: WebView? = null
    private var webViewState: Bundle? = null
    private var initialLoadFinished = false
    private var showFallbackContainer = false

    @Inject
    lateinit var resourceAPI: ResourceAPIService

    interface Listener {
        fun onExternalWebLinkClicked(url: String)
        fun onRunScript(type: String, content: String, name: String?, location: String?, contextDirectory: File?)
        fun onShareURL(title: String, url: String)
        fun onReceivedACK(id: String)
        fun onRunDemo()
    }

    open fun createFallbackFragment(): Fragment? {
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        uri = requireArguments().getParcelableValue(ARG_URI, Uri::class.java)!!
        matchingQueryKeys = requireArguments().getStringArrayList(ARG_MATCHING_QUERY_KEYS) ?: listOf()
        contextDirectory = requireArguments().getSerializableValue(ARG_CONTEXT_DIRECTORY, File::class.java)
        filterURL = requireArguments().getBoolean(ARG_FILTER_URL, true)
        if (savedInstanceState != null) {
            initialLoadFinished = savedInstanceState.getBoolean(ARG_INITIAL_LOAD_FINISHED, false)
            showFallbackContainer = savedInstanceState.getBoolean(ARG_SHOW_FALLBACK, false)
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
            val view = inflater.inflate(R.layout.layout_empty_hint, container, false)
            val hint = view.findViewById<TextView>(R.id.hint)
            hint.text = CelestiaString("WebView is not available.", "")
            view
        }
    }

    private fun configureWebView(view: View, savedInstanceState: Bundle?) {
        val loadingIndicator = view.findViewById<CircularProgressIndicator>(R.id.loading_indicator)
        val fallbackContainer = view.findViewById<FrameLayout>(R.id.fallback)

        val webView = view.findViewById<WebView>(R.id.webview)
        webView.setBackgroundColor(Color.TRANSPARENT)
        webView.isHorizontalScrollBarEnabled = false

        val webSettings = webView.settings
        @SuppressLint("SetJavaScriptEnabled")
        webSettings.javaScriptEnabled = true

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
            @TargetApi(Build.VERSION_CODES.N)
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return shouldOverrideUrl(request?.url.toString())
            }

            @Deprecated("Deprecated in Java")
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
                val uri = Uri.parse(url)
                if (isURLAllowed(uri))
                    return false
                weakSelf.get()?.listener?.onExternalWebLinkClicked(url)
                return true
            }

            override fun onPageCommitVisible(view: WebView?, url: String?) {
                val wv = view ?: return
                val self = weakSelf.get() ?: return
                weakSelf.get()?.initialLoadFinished = true
                loadingIndicator.isVisible = false
                self.leftNavigationBarItem  = if (wv.canGoBack()) NavigationFragment.BarButtonItem(MENU_ITEM_BACK_BUTTON, null, R.drawable.ic_action_arrow_back) else null
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                val self = weakSelf.get() ?: return
                if ((self.parentFragment as? NavigationFragment)?.top == self) {
                    self.title = view?.title ?: ""
                }
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
                onError(webView)
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                onError(webView)
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                super.onReceivedHttpError(view, request, errorResponse)
                onError(webView)
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                super.onReceivedSslError(view, handler, error)
                onError(webView)
            }

            fun onError(webView: WebView?) {
                if (fallbackContainer.isVisible) return
                val fragment = createFallbackFragment()
                if (fragment != null) {
                    replace(fragment, R.id.fallback)
                    fallbackContainer.isVisible = true
                    webView?.isVisible = false
                    loadingIndicator.isVisible = false
                    weakSelf.get()?.showFallbackContainer = true
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                val self = weakSelf.get() ?: return
                if ((self.parentFragment as? NavigationFragment)?.top == self) {
                    self.title = title ?: ""
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.bottom_safe_area)) { container, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            container.updatePadding(bottom = insets.bottom)
            WindowInsetsCompat.CONSUMED
        }

        this.webView = webView

        val savedState = webViewState ?: savedInstanceState
        if (savedState != null) {
            webView.restoreState(savedState)
            if (showFallbackContainer) {
                fallbackContainer.isVisible = true
                webView.isVisible = false
                loadingIndicator.isVisible = false
            } else if (initialLoadFinished) {
                loadingIndicator.isVisible = false
            }
        } else {
            webView.loadUrl(uri.toString())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val savedState = webViewState ?: savedInstanceState
        val wv = webView
        if (savedState != null && wv != null) {
            leftNavigationBarItem = if (wv.canGoBack()) NavigationFragment.BarButtonItem(MENU_ITEM_BACK_BUTTON, null, R.drawable.ic_action_arrow_back) else null
        } else {
            leftNavigationBarItem = null
        }
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

    override fun onSaveInstanceState(outState: Bundle) {
        webView?.saveState(outState)
        outState.putBoolean(ARG_INITIAL_LOAD_FINISHED, initialLoadFinished)
        outState.putBoolean(ARG_SHOW_FALLBACK, showFallbackContainer)
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
                val result = resourceAPI.item(lang, id).commonHandler(ResourceItem::class.java, ResourceAPI.gson)
                val frag = parentFragment
                if (frag is NavigationFragment) {
                    frag.pushFragment(ResourceItemFragment.newInstance(result, lang, Date()))
                }
            } catch (ignored: Throwable) {}
        }
    }

    override fun runDemo() {
        listener?.onRunDemo()
    }

    override fun menuItemClicked(groupId: Int, id: Int): Boolean {
        when (id) {
            MENU_ITEM_BACK_BUTTON -> {
                goBack()
            }
        }
        return true
    }

    fun canGoBack(): Boolean {
        return webView?.canGoBack() ?: false
    }

    fun goBack() {
        webView?.goBack()
    }

    companion object {
        private const val ARG_URI = "uri"
        private const val ARG_MATCHING_QUERY_KEYS = "query_keys"
        private const val ARG_CONTEXT_DIRECTORY = "context_directory"
        private const val ARG_FILTER_URL = "filter_url"
        private const val ARG_INITIAL_LOAD_FINISHED = "initial_load_finished"
        private const val ARG_SHOW_FALLBACK = "show_fallback_container"
        private const val MENU_ITEM_BACK_BUTTON = 12425

        fun newInstance(uri: Uri, matchingQueryKeys: List<String>, contextDirectory: File? = null) = CommonWebFragment.create({ CommonWebFragment() }, uri, matchingQueryKeys, contextDirectory, true)
        fun newInstance(uri: Uri) = CommonWebFragment.create({ CommonWebFragment() }, uri, listOf(), null, false)

        fun <T: Fragment> create(fragmentCreater: () -> T, uri: Uri, matchingQueryKeys: List<String>, contextDirectory: File?, filterURL: Boolean): T {
            val fragment = fragmentCreater().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_URI, uri)
                    putSerializable(ARG_CONTEXT_DIRECTORY, contextDirectory)
                    putStringArrayList(ARG_MATCHING_QUERY_KEYS, ArrayList(matchingQueryKeys))
                    putBoolean(ARG_FILTER_URL, filterURL)
                }
            }
            return fragment
        }
    }
}