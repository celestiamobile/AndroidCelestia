package space.celestia.celestiaui.utils

import android.net.Uri
import androidx.core.net.toUri
import space.celestia.celestiaui.di.Platform
import space.celestia.celestiaui.purchase.PurchaseManager

class URLHelper {
    companion object {
        fun buildInAppGuideURI(id: String, language: String, platform: Platform, shareable: Boolean? = null, purchaseManager: PurchaseManager): Uri {
            val baseURL = "https://celestia.mobi/resources/guide"
            var builder = baseURL.toUri()
                .buildUpon()
                .appendQueryParameter("guide", id)
                .appendQueryParameter("lang", language)
                .appendQueryParameter("platform", platform.name)
                .appendQueryParameter("supportsSafeArea", "true")
                .appendQueryParameter("theme", "dark")
                .appendQueryParameter("transparentBackground", "true")
                .appendQueryParameter("api", "2")
            if (platform.flavor != null)
                builder = builder.appendQueryParameter("distribution", platform.flavor)
            if (shareable != null)
                builder = builder.appendQueryParameter("share", if (shareable) "true" else "false")
            if (purchaseManager.canUseInAppPurchase())
                builder = builder.appendQueryParameter("purchaseTokenAndroid", purchaseManager.purchaseToken() ?: "")
            return builder.build()
        }

        fun buildInAppGuideShortURI(path: String, language: String, platform: Platform, shareable: Boolean? = null): Uri {
            val baseURL = "https://celestia.mobi"
            var builder = baseURL.toUri()
                .buildUpon()
                .path(path)
                .appendQueryParameter("lang", language)
                .appendQueryParameter("platform", platform.name)
                .appendQueryParameter("supportsSafeArea", "true")
                .appendQueryParameter("theme", "dark")
                .appendQueryParameter("transparentBackground", "true")
                .appendQueryParameter("api", "2")
            if (platform.flavor != null)
                builder = builder.appendQueryParameter("distribution", platform.flavor)
            if (shareable != null)
                builder = builder.appendQueryParameter("share", if (shareable) "true" else "false")
            return builder.build()
        }

        fun buildInAppAddonURI(id: String, language: String, platform: Platform): Uri {
            val baseURL = "https://celestia.mobi/resources/item"
            var builder = baseURL.toUri()
                .buildUpon()
                .appendQueryParameter("item", id)
                .appendQueryParameter("lang", language)
                .appendQueryParameter("platform", platform.name)
                .appendQueryParameter("supportsSafeArea", "true")
                .appendQueryParameter("theme", "dark")
                .appendQueryParameter("transparentBackground", "true")
                .appendQueryParameter("titleVisibility", "collapsed")
                .appendQueryParameter("api", "2")
            if (platform.flavor != null)
                builder = builder.appendQueryParameter("distribution", platform.flavor)
            return builder.build()
        }

        fun buildInAppRelatedAddonsURI(objectPath: String, language: String, platform: Platform, purchaseManager: PurchaseManager): Uri {
            val baseURL = "https://celestia.mobi/resources/itemsByObjectPath"
            var builder = baseURL.toUri()
                .buildUpon()
                .appendQueryParameter("objectPath", objectPath)
                .appendQueryParameter("lang", language)
                .appendQueryParameter("platform", platform.name)
                .appendQueryParameter("theme", "dark")
                .appendQueryParameter("transparentBackground", "true")
                .appendQueryParameter("api", "2")
            if (platform.flavor != null)
                builder = builder.appendQueryParameter("distribution", platform.flavor)
            if (purchaseManager.canUseInAppPurchase())
                builder = builder.appendQueryParameter("purchaseTokenAndroid", purchaseManager.purchaseToken() ?: "")
            return builder.build()
        }
    }
}