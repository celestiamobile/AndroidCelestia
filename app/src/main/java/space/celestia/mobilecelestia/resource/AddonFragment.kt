package space.celestia.mobilecelestia.resource

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import dagger.hilt.android.lifecycle.HiltViewModel
import space.celestia.celestiafoundation.resource.model.ResourceItem
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.compose.Mdc3Theme
import java.io.File
import java.lang.ref.WeakReference

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
                    IconButton(onClick = {
                        backStack.removeLastOrNull()
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

class AddonFragment: Fragment() {
    private lateinit var item: ResourceItem

    interface Listener {
        fun webRequestRunScript(script: File)
        fun webRequestShareAddon(name: String, id: String)
    }

    private var listener: Listener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement AddonFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        item = if (savedInstanceState != null) {
            BundleCompat.getSerializable(savedInstanceState, ARG_ITEM, ResourceItem::class.java)!!
        } else {
            BundleCompat.getSerializable(requireArguments(), ARG_ITEM, ResourceItem::class.java)!!
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(ARG_ITEM, item)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Mdc3Theme {
                    SingleAddonScreen(item = item, requestRunScript = { script ->
                        listener?.webRequestRunScript(script)
                    }, requestShareAddon = { name, id ->
                        listener?.webRequestShareAddon(name, id)
                    })
                }
            }
        }
    }

    companion object {
        private const val ARG_ITEM = "item"

        @JvmStatic
        fun newInstance(item: ResourceItem) =
            AddonFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ITEM, item)
                }
            }
    }
}