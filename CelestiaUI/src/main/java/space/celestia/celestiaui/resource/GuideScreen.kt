package space.celestia.celestiaui.resource

import android.net.Uri
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.dropUnlessResumed
import space.celestia.celestiaui.R
import space.celestia.celestiaui.compose.SheetTopAppBar
import space.celestia.celestiaui.compose.contentWindowInsetsIgnoringVisibility
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideScreen(
    uri: Uri,
    initialTitle: String = "",
    fallbackContent: (@Composable (Modifier, PaddingValues) -> Unit)? = null,
    runScript: ((String, String, String?, String?, File?) -> Unit),
    shareURL: ((String, String) -> Unit),
    receivedACK: ((String) -> Unit),
    runDemo: (() -> Unit),
    openSubscriptionPage: ((String?) -> Unit),
    externalLinkClicked: ((String) -> Unit),
) {
    var documentTitle by remember(uri) { mutableStateOf(initialTitle) }
    var pageInfo by remember(uri) { mutableStateOf<CelestiaJavascriptInterface.SendACKContext?>(null) }
    var canGoBack by remember(uri) { mutableStateOf(false) }
    var goBackRequest by remember(uri) { mutableStateOf<(() -> Unit)?>(null) }
    val title = pageInfo?.title ?: documentTitle
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    Scaffold(
        topBar = {
            SheetTopAppBar(
                title = {
                    Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                subtitle = {
                    pageInfo?.subtitle?.takeIf { it.isNotEmpty() }?.let {
                        Text(it, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                },
                navigationIcon = {
                    if (canGoBack) {
                        IconButton(onClick = dropUnlessResumed { goBackRequest?.invoke() }) {
                            Icon(painterResource(R.drawable.ic_action_arrow_back), contentDescription = null)
                        }
                    }
                },
                actions = {
                    val shareLink = pageInfo?.shareURL
                    if (shareLink != null) {
                        IconButton(onClick = {
                            shareURL(documentTitle.ifEmpty { title }, shareLink)
                        }) {
                            Icon(painterResource(R.drawable.ic_share), contentDescription = null)
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                windowInsets = WindowInsets()
            )
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsetsIgnoringVisibility.only(WindowInsetsSides.Bottom),
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        WebPage(
            uri = uri,
            filterURL = true,
            matchingQueryKeys = listOf("guide"),
            paddingValues = paddingValues,
            fallbackContent = if (fallbackContent == null) null else { modifier, padding ->
                LaunchedEffect(uri) {
                    documentTitle = initialTitle
                    pageInfo = null
                    canGoBack = false
                }
                fallbackContent(modifier, padding)
            },
            titleChanged = { documentTitle = it },
            pageInfoChanged = { pageInfo = it },
            canGoBackChanged = { canGoBack = it },
            goBackRequest = { goBackRequest = it },
            runScript = runScript,
            shareURL = shareURL,
            receivedACK = receivedACK,
            runDemo = runDemo,
            openSubscriptionPage = openSubscriptionPage,
            externalLinkClicked = externalLinkClicked,
        )
    }
}
