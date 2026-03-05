package space.celestia.celestiaui.resource

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
import androidx.compose.runtime.LaunchedEffect
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

sealed class SingleAddonPage {
    data class Addon(val addon: ResourceItem): SingleAddonPage() {
        var title = mutableStateOf(addon.name)
    }
}

class SingleAddonViewModel(): ViewModel() {
    val backStack = mutableStateListOf<SingleAddonPage>()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleAddonScreen(item: ResourceItem, requestRunScript: (File) -> Unit, requestShareAddon: (String, String) -> Unit) {
    val viewModel: SingleAddonViewModel = hiltViewModel()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val backStack = viewModel.backStack
    LaunchedEffect(Unit) {
        if (backStack.isEmpty())
            backStack.add(SingleAddonPage.Addon(item))
    }

    if (backStack.isEmpty()) return

    Scaffold(
        topBar = {
            TopAppBar(title = {
                val lastEntry = backStack.lastOrNull() ?: return@TopAppBar
                when (lastEntry) {
                    is SingleAddonPage.Addon -> {
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
                }
            }, actions = {
                val lastEntry = backStack.lastOrNull() ?: return@TopAppBar
                if (lastEntry is SingleAddonPage.Addon) {
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
                    is SingleAddonPage.Addon -> NavEntry(route) {
                        AddonScreen(item = route.addon, paddingValues = paddingValues, addonInfoUpdated = { info ->
                            route.title.value = info.name
                        }, requestRunScript = requestRunScript)
                    }
                }
            }
        )
    }
}

