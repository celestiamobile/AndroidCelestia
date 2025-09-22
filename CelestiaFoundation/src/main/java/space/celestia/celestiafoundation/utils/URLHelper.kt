package space.celestia.celestiafoundation.utils

import android.net.Uri
import androidx.core.net.toUri

class URLHelper {
    companion object {
        fun buildInAppGuideURI(id: String, language: String, shareable: Boolean? = null, additionalQueryParameters: Map<String, String>? = null): Uri {
            val baseURL = "https://celestia.mobi/resources/guide"
            var builder = baseURL.toUri()
                .buildUpon()
                .appendQueryParameter("guide", id)
                .appendQueryParameter("lang", language)
                .appendQueryParameter("platform", "android")
                .appendQueryParameter("theme", "dark")
                .appendQueryParameter("transparentBackground", "true")
            if (shareable != null) {
                builder = builder.appendQueryParameter("share", if (shareable) "true" else "false")
            }
            if (additionalQueryParameters != null) {
                for ((key, value) in additionalQueryParameters) {
                    builder = builder.appendQueryParameter(key, value)
                }
            }
            return builder.build()
        }

        fun buildInAppGuideShortURI(path: String, language: String, shareable: Boolean? = null): Uri {
            val baseURL = "https://celestia.mobi"
            var builder = baseURL.toUri()
                .buildUpon()
                .path(path)
                .appendQueryParameter("lang", language)
                .appendQueryParameter("platform", "android")
                .appendQueryParameter("theme", "dark")
                .appendQueryParameter("transparentBackground", "true")
            if (shareable != null) {
                builder = builder.appendQueryParameter("share", if (shareable) "true" else "false")
            }
            return builder.build()
        }

        fun buildInAppAddonURI(id: String, language: String): Uri {
            val baseURL = "https://celestia.mobi/resources/item"
            return baseURL.toUri()
                .buildUpon()
                .appendQueryParameter("item", id)
                .appendQueryParameter("lang", language)
                .appendQueryParameter("platform", "android")
                .appendQueryParameter("theme", "dark")
                .appendQueryParameter("transparentBackground", "true")
                .appendQueryParameter("titleVisibility", "collapsed")
                .build()
        }
    }
}