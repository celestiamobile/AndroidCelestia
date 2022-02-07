package space.celestia.mobilecelestia.resource

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import java.lang.ref.WeakReference

class CommonWebFragment: NavigationFragment.SubFragment(), CelestiaJavascriptInterface.MessageHandler {
    private lateinit var uri: Uri
    private lateinit var webView: WebView

    private var listener: Listener? = null

    interface Listener {
        fun onExternalWebLinkClicked(url: String)
        fun onRunScript(type: String, content: String)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        uri = requireArguments().getParcelable<Uri>(ARG_URI)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_common_web, container, false)
        webView = view.findViewById(R.id.webview)
        webView.setBackgroundColor(Color.TRANSPARENT)
        webView.isHorizontalScrollBarEnabled = false

        val webSettings = webView.settings
        @SuppressLint("SetJavaScriptEnabled")
        webSettings.javaScriptEnabled = true

        val weakSelf = WeakReference(this)

        val defaultUri = uri
        webView.addJavascriptInterface(CelestiaJavascriptInterface(this), "AndroidCelestia")
        webView.webViewClient = object: WebViewClient() {
            @TargetApi(Build.VERSION_CODES.N)
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return shouldOverrideUrl(request?.url.toString())
            }

            @SuppressWarnings("deprecation")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return shouldOverrideUrl(url)
            }

            fun shouldOverrideUrl(url: String?): Boolean {
                if (url == null) return true
                val uri = Uri.parse(url)
                if (uri.host == defaultUri.host && uri.path == defaultUri.path) {
                    return false
                }
                weakSelf.get()?.listener?.onExternalWebLinkClicked(url)
                return true
            }
        }

        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            WebSettingsCompat.setForceDark(webSettings, WebSettingsCompat.FORCE_DARK_ON)
        }

        webView.loadUrl(uri.toString())
        return view
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement CommonWebFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun runScript(type: String, content: String) {
        listener?.onRunScript(type, content)
    }

    companion object {
        private val ARG_URI = "uri"

        fun newInstance(uri: Uri) = CommonWebFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_URI, uri)
            }
        }
    }
}