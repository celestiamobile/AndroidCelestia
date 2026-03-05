package space.celestia.celestiaui.resource

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import space.celestia.celestia.AppCore
import space.celestia.celestiaui.resource.viewmodel.AddonManagerViewModel
import java.io.File

@Composable
fun AddonDownload(isLeaf: Boolean?, categoryId: String?, requestRunScript: (File) -> Unit, requestShareAddon: (String, String) -> Unit) {
    if (categoryId != null && isLeaf != null) {
        AddonDownloadCategory(isLeaf = isLeaf, categoryId = categoryId, requestRunScript = requestRunScript, requestShareAddon = requestShareAddon)
    } else {
        AddonDownloadMain(requestRunScript = requestRunScript, requestShareAddon = requestShareAddon)
    }
}

@Composable
private fun AddonDownloadMain(requestRunScript: (File) -> Unit, requestShareAddon: (String, String) -> Unit) {
    val viewModel: AddonManagerViewModel = hiltViewModel()
    fun getUri(): Uri {
        val baseURL = "https://celestia.mobi/resources/categories"
        var builder = baseURL.toUri()
            .buildUpon()
            .appendQueryParameter("lang", AppCore.getLanguage())
            .appendQueryParameter("platform", "android")
            .appendQueryParameter("supportsSafeArea", "true")
            .appendQueryParameter("distribution", viewModel.flavor)
            .appendQueryParameter("theme", "dark")
            .appendQueryParameter("transparentBackground", "true")
            .appendQueryParameter("api", "2")
        if (viewModel.purchaseManager.canUseInAppPurchase())
            builder = builder.appendQueryParameter("purchaseTokenAndroid", viewModel.purchaseManager.purchaseToken() ?: "")
        return builder.build()
    }

    val uri by remember { mutableStateOf(getUri()) }
    WebScreen(uri, requestRunScript = requestRunScript, requestShareAddon = requestShareAddon)
}

@Composable
private fun AddonDownloadCategory(isLeaf: Boolean, categoryId: String, requestRunScript: (File) -> Unit, requestShareAddon: (String, String) -> Unit) {
    val viewModel: AddonManagerViewModel = hiltViewModel()
    fun getUri(): Uri {
        val baseURL = if (isLeaf) "https://celestia.mobi/resources/category" else "https://celestia.mobi/resources/categories"
        var builder = baseURL.toUri()
            .buildUpon()
            .appendQueryParameter("lang", AppCore.getLanguage())
            .appendQueryParameter("platform", "android")
            .appendQueryParameter("supportsSafeArea", "true")
            .appendQueryParameter("distribution", viewModel.flavor)
            .appendQueryParameter("theme", "dark")
            .appendQueryParameter("transparentBackground", "true")
            .appendQueryParameter("api", "2")
        builder = if (isLeaf)
            builder.appendQueryParameter("category", categoryId)
        else
            builder.appendQueryParameter("parent", categoryId)
        if (viewModel.purchaseManager.canUseInAppPurchase())
            builder = builder.appendQueryParameter("purchaseTokenAndroid", viewModel.purchaseManager.purchaseToken() ?: "")
        return builder.build()
    }

    val uri by remember { mutableStateOf(getUri()) }
    WebScreen(uri, requestRunScript = requestRunScript, requestShareAddon = requestShareAddon)
}