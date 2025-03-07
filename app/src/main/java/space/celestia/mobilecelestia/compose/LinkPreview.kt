package space.celestia.mobilecelestia.compose

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import space.celestia.mobilecelestia.R
import space.celestia.ui.linkpreview.LPLinkView
import space.celestia.ui.linkpreview.LPLinkViewData
import space.celestia.ui.linkpreview.LPMetadataProvider
import java.net.URL

private sealed class FetchState {
    data class Successful(val metadata: LPLinkViewData) : FetchState()
    data object Failed : FetchState()
    data object Fetching : FetchState()
    data object None : FetchState()
    data object NotInitialized : FetchState()
}

@SuppressLint("InflateParams")
@Composable
private fun LinkPreviewInternal(metadata: LPLinkViewData, modifier: Modifier = Modifier, onClick: (URL) -> Unit) {
    AndroidView(factory = { context ->
        val view = LayoutInflater.from(context).inflate(R.layout.common_link_preview, null, false) as LPLinkView
        view.setOnClickListener {
            val url = metadata.url
            onClick(url)
        }
        view
    }, update = {
        it.linkData = metadata
    }, modifier = modifier)
}

@Composable
fun LinkPreview(url: URL, modifier: Modifier = Modifier, onClick: (URL) -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var state: FetchState by remember {
        mutableStateOf(FetchState.NotInitialized)
    }

    fun updateState(newState: FetchState) {
        val currentState = state
        if (currentState is FetchState.Successful)
            currentState.metadata.image?.recycle()
        state = newState
    }

    val lifeCycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifeCycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY)
                updateState(FetchState.NotInitialized)
        }
        lifeCycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifeCycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(url) {
        updateState(FetchState.None)
    }

    if (state == FetchState.None) {
        updateState(FetchState.Fetching)
        val fetcher = LPMetadataProvider()
        fetcher.startFetchMetadataForURL(coroutineScope, url) { metaData, _ ->
            if (metaData == null) {
                updateState(FetchState.Failed)
                return@startFetchMetadataForURL
            }
            var metadata = LPLinkViewData(metaData.url, metaData.title, null, true)
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
            updateState(FetchState.Successful(metadata))
        }
    }

    val currentState = state
    if (currentState is FetchState.Successful) {
        LinkPreviewInternal(metadata = currentState.metadata, modifier = modifier, onClick = onClick)
    }
}