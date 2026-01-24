package space.celestia.mobilecelestia.resource

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import space.celestia.celestia.AppCore
import space.celestia.celestiafoundation.resource.model.ResourceItem
import space.celestia.celestiafoundation.resource.model.ResourceManager
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.NavigationFragment
import space.celestia.mobilecelestia.compose.EmptyHint
import space.celestia.mobilecelestia.compose.TextRow
import space.celestia.mobilecelestia.purchase.PurchaseManager
import space.celestia.mobilecelestia.purchase.SubscriptionBackingFragment
import space.celestia.mobilecelestia.resource.model.AddonUpdateManager
import space.celestia.mobilecelestia.utils.CelestiaString
import space.celestia.mobilecelestia.utils.showAlert
import java.text.DateFormat
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class AddonUpdateListFragment : SubscriptionBackingFragment(), ResourceManager.Listener {
    @Inject
    lateinit var resourceManager: ResourceManager

    @Inject
    lateinit var addonUpdateManager: AddonUpdateManager

    @Inject
    lateinit var purchaseManager: PurchaseManager

    private var listener: Listener? = null

    interface Listener {
        fun onInstalledAddonSelected(addon: ResourceItem)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        resourceManager.addListener(this)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = CelestiaString("Updates", "View the list of add-ons that have pending updates.")
        rightNavigationBarItems = listOf(
            NavigationFragment.BarButtonItem(MENU_ITEM_HELP, null, R.drawable.help_24px)
        )

        lifecycleScope.launch {
            refreshAddonList(checkReason = AddonUpdateManager.CheckReason.ViewAppear)
        }
    }

    override fun menuItemClicked(groupId: Int, id: Int): Boolean {
        when (id) {
            MENU_ITEM_HELP -> {
                activity?.showAlert(title = CelestiaString("Add-on Updates", ""), message = CelestiaString("Add-on updates are only supported for add-ons installed on version 1.9.3 or above.", "Hint for requirement for updating add-ons."))
            }
        }
        return true
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Listener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement AddonUpdateListFragment.Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onDestroyView() {
        resourceManager.removeListener(this)
        super.onDestroyView()
    }

    override fun onAddonUninstalled(identifier: String) {
        lifecycleScope.launch {
            refreshAddonList(checkReason = AddonUpdateManager.CheckReason.Change)
        }
    }

    override fun onFileUnzipped(identifier: String) {
        lifecycleScope.launch {
            refreshAddonList(checkReason = AddonUpdateManager.CheckReason.Change)
        }
    }

    override fun onFileDownloaded(identifier: String) {}

    override fun onResourceFetchError(
        identifier: String,
        errorContext: ResourceManager.ErrorContext
    ) {}

    override fun onProgressUpdate(identifier: String, progress: Float) {}

    private suspend fun refreshAddonList(checkReason: AddonUpdateManager.CheckReason) {
        val purchaseToken = purchaseManager.purchaseToken() ?: return
        val success = addonUpdateManager.refresh(
            reason = checkReason,
            purchaseToken = purchaseToken,
            language = AppCore.getLanguage()
        )
        if (!success) {
            activity?.showAlert(
                title = CelestiaString("Error checking updates", "Encountered error while checking updates."),
                message = CelestiaString("Please ensure you have a valid Celestia PLUS subscription or check again later.", "Encountered error while checking updates, possible recovery instruction.")
            )
        }
    }

    @Composable
    override fun MainView() {
        val state = rememberPullToRefreshState()
        val scope = rememberCoroutineScope()

        val formatter by remember { mutableStateOf(DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())) }

        PullToRefreshBox(
            isRefreshing = addonUpdateManager.isCheckingUpdates.value,
            onRefresh = {
                scope.launch {
                    refreshAddonList(checkReason = AddonUpdateManager.CheckReason.Refresh)
                }
            },
            state = state,
            indicator = {
                Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = addonUpdateManager.isCheckingUpdates.value,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    state = state
                )
            },
        ) {
            if (addonUpdateManager.pendingUpdates.isEmpty()) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .systemBarsPadding(), contentAlignment = Alignment.Center) {
                    EmptyHint(text = CelestiaString("No Update Available", "Hint that there is no update for installed add-ons."))
                }
            } else {
                val systemPadding = WindowInsets.systemBars.asPaddingValues()
                val direction = LocalLayoutDirection.current
                val contentPadding = PaddingValues(
                    start = systemPadding.calculateStartPadding(direction),
                    top = dimensionResource(id = R.dimen.list_spacing_short) + systemPadding.calculateTopPadding(),
                    end = systemPadding.calculateEndPadding(direction),
                    bottom = dimensionResource(id = R.dimen.list_spacing_tall) + systemPadding.calculateBottomPadding(),
                )
                LazyColumn(
                    contentPadding = contentPadding,
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(rememberNestedScrollInteropConnection())
                        .background(color = MaterialTheme.colorScheme.background)
                ) {
                    items(addonUpdateManager.pendingUpdates) {
                        TextRow(
                            modifier = Modifier.clickable(onClick = {
                                listener?.onInstalledAddonSelected(it.addon)
                            }),
                            primaryText = it.addon.name,
                            secondaryText = formatter.format(it.update.modificationDate),
                            accessoryResource = R.drawable.accessory_full_disclosure
                        )
                    }
                }
            }
        }
    }

    companion object {
        const val MENU_ITEM_HELP = 0

        @JvmStatic
        fun newInstance() = AddonUpdateListFragment()
    }
}