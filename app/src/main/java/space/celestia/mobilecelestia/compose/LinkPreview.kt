package space.celestia.mobilecelestia.compose

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import space.celestia.mobilecelestia.R
import space.celestia.ui.linkpreview.LPLinkView
import space.celestia.ui.linkpreview.LPLinkViewData
import space.celestia.ui.linkpreview.LPMetadataProvider
import java.net.URL

private enum class FetchState {
    Successful, Failed, Fetching, None
}

@SuppressLint("InflateParams")
@Composable
fun LinkPreviewInternal(metadata: LPLinkViewData?, modifier: Modifier = Modifier, onClick: (URL) -> Unit) {
    AndroidView(factory = { context ->
        val view = LayoutInflater.from(context).inflate(R.layout.common_link_preview, null, false) as LPLinkView
        view.setOnClickListener {
            val url = metadata?.url ?: return@setOnClickListener
            onClick(url)
        }
        view
    }, update = {
        it.linkData = metadata
    }, modifier = modifier)
}

@Composable
fun LinkPreview(url: URL, modifier: Modifier = Modifier, loadResult: (Boolean) -> Unit, onClick: (URL) -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var metadata by remember {
        mutableStateOf<LPLinkViewData?>(null)
    }
    var state by remember {
        mutableStateOf(FetchState.None)
    }
    val lifeCycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifeCycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                metadata?.image?.recycle()
                metadata = null
            }
        }
        lifeCycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifeCycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(url) {
        metadata = null
        state = FetchState.None
    }

    if (state == FetchState.None) {
        state = FetchState.Fetching
        val fetcher = LPMetadataProvider()
        fetcher.startFetchMetadataForURL(coroutineScope, url) { metaData, _ ->
            if (metaData == null) {
                state = FetchState.Failed
                loadResult(false)
                return@startFetchMetadataForURL
            }
            metadata = LPLinkViewData(metaData.url, metaData.title, null, true)
            loadResult(true)
            val imageURL = metaData.imageURL ?: metaData.iconURL
            val usesIcon = metaData.imageURL == null
            val client = OkHttpClient()
            val image = withContext(Dispatchers.IO) {
                try {
                    val req = Request.Builder().url(imageURL).build()
                    val res = client.newCall(req).execute()
                    val stream = res.body?.byteStream() ?: return@withContext null
                    return@withContext BitmapFactory.decodeStream(stream)
                } catch(ignored: Throwable) {
                    return@withContext null
                }
            }
            if (image != null) {
                metadata = LPLinkViewData(metaData.url, metaData.title, image, usesIcon)
            }
            state = FetchState.Successful
        }
    }

    if (state == FetchState.Successful) {
        LinkPreviewInternal(metadata = metadata, modifier = modifier, onClick = onClick)
    }
}