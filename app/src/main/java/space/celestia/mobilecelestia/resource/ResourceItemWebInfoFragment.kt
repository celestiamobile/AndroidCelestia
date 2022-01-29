/*
 * ResourceItemWebInfoFragment.kt
 *
 * Copyright (C) 2001-2022, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

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
import androidx.fragment.app.Fragment
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import space.celestia.celestia.AppCore
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.resource.model.ResourceItem

class ResourceItemWebInfoFragment: Fragment() {
    private lateinit var item: ResourceItem
    private lateinit var webView: WebView

    private var listener: Listener? = null

    interface Listener {
        fun onExternalWebLinkClicked(url: String)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        item = requireArguments().getSerializable(ARG_ITEM) as ResourceItem
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_resource_item_web_info, container, false)
        webView = view.findViewById(R.id.webview)
        webView.setBackgroundColor(Color.TRANSPARENT)
        webView.isHorizontalScrollBarEnabled = false

        val webSettings = webView.settings
        @SuppressLint("SetJavaScriptEnabled")
        webSettings.javaScriptEnabled = true

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
                if (Uri.parse(url).host == "celestia.mobi") {
                    return false
                }
                listener?.onExternalWebLinkClicked(url)
                return true
            }
        }

        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            WebSettingsCompat.setForceDark(webSettings, WebSettingsCompat.FORCE_DARK_ON)
        }

        val baseURL = "https://celestia.mobi/resources/item"
        val lang = AppCore.getLocalizedString("LANGUAGE", "celestia")
        val uri = Uri.parse(baseURL)
            .buildUpon()
            .appendQueryParameter("item", item.id)
            .appendQueryParameter("lang", lang)
            .appendQueryParameter("environment", "app")
            .appendQueryParameter("theme", "dark")
            .build()

        webView.loadUrl(uri.toString())
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement ResourceItemWebInfoFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    companion object {
        private const val ARG_ITEM = "item"
        @JvmStatic
        fun newInstance(item: ResourceItem) =
            ResourceItemWebInfoFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ITEM, item)
                }
            }
    }
}