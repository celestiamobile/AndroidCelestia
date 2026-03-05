package space.celestia.celestiaui.resource

import android.net.Uri
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import space.celestia.celestiafoundation.resource.model.ResourceItem
import space.celestia.celestiaui.R
import java.io.File

sealed class Page {
    class Home: Page() {
        var canGoBack = mutableStateOf(false)
        var title = mutableStateOf("")
        var goBackRequest: (() -> Unit)? = null
    }
    data class Addon(val addon: ResourceItem): Page() {
        var title = mutableStateOf(addon.name)
    }
}

class WebViewModel(): ViewModel() {
    val backStack = mutableStateListOf<Page>(Page.Home())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebScreen(uri: Uri, requestRunScript: (File) -> Unit, requestShareAddon: (String, String) -> Unit) {
    val viewModel: WebViewModel = hiltViewModel()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val backStack = viewModel.backStack
    if (backStack.isEmpty()) return

    Scaffold(
        topBar = {
            TopAppBar(title = {
                val lastEntry = backStack.lastOrNull() ?: return@TopAppBar
                when (lastEntry) {
                    is Page.Home -> {
                        Text(lastEntry.title.value)
                    }
                    is Page.Addon -> {
                        Text(lastEntry.title.value)
                    }
                }
            }, navigationIcon = {
                if (backStack.count() > 1) {
                    IconButton(onClick = dropUnlessResumed {
                        if (backStack.count() > 1) {
                            backStack.removeLastOrNull()
                        }
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_action_arrow_back),
                            contentDescription = null
                        )
                    }
                } else {
                    val lastEntry = backStack.lastOrNull() ?: return@TopAppBar
                    if (lastEntry is Page.Home && lastEntry.canGoBack.value) {
                        IconButton(onClick = {
                            lastEntry.goBackRequest?.invoke()
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_action_arrow_back),
                                contentDescription = null
                            )
                        }
                    }
                }
            }, actions = {
                val lastEntry = backStack.lastOrNull() ?: return@TopAppBar
                if (lastEntry is Page.Addon) {
                    IconButton({
                        requestShareAddon(lastEntry.title.value, lastEntry.addon.id)
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_share),
                            contentDescription = null
                        )
                    }
                }
            }, scrollBehavior = scrollBehavior, windowInsets = WindowInsets())
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Bottom),
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        NavDisplay(
            backStack = backStack,
            transitionSpec = {
                slideInHorizontally(initialOffsetX = { it }) togetherWith
                        slideOutHorizontally(targetOffsetX = { -it })
            },
            popTransitionSpec = {
                slideInHorizontally(initialOffsetX = { -it }) togetherWith
                        slideOutHorizontally(targetOffsetX = { it })
            },
            predictivePopTransitionSpec = {
                slideInHorizontally(initialOffsetX = { -it }) togetherWith
                        slideOutHorizontally(targetOffsetX = { it })
            },
            entryProvider = { route ->
                when (route) {
                    is Page.Home -> NavEntry(route) {
                        WebPage(uri = uri, paddingValues = paddingValues, titleChanged = {
                            route.title.value = it
                        }, canGoBackChanged = {
                            route.canGoBack.value = it
                        }, goBackRequest = { request ->
                            route.goBackRequest = request
                        }, openAddon = { addon ->
                            backStack.add(Page.Addon(addon))
                        })
                    }
                    is Page.Addon -> NavEntry(route) {
                        AddonScreen(item = route.addon, paddingValues = paddingValues, addonInfoUpdated = { info ->
                            route.title.value = info.name
                        }, requestRunScript = requestRunScript)
                    }
                }
            }
        )
    }
}

