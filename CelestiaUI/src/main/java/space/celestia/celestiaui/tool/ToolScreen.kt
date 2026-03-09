package space.celestia.celestiaui.tool

import android.view.Display
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import space.celestia.celestia.AppCore
import space.celestia.celestiaui.browser.Browser
import space.celestia.celestiaui.browser.SubsystemBrowser
import space.celestia.celestiaui.control.CameraControlContainer
import space.celestia.celestiaui.eventfinder.EventFinder
import space.celestia.celestiaui.favorite.FavoriteBookmarkItem
import space.celestia.celestiaui.favorite.FavoriteContainer
import space.celestia.celestiaui.favorite.FavoriteScriptItem
import space.celestia.celestiaui.favorite.MutableFavoriteBaseItem
import space.celestia.celestiaui.help.NewHelpScreen
import space.celestia.celestiaui.info.InfoScreen
import space.celestia.celestiaui.resource.AddonDownload
import space.celestia.celestiaui.resource.AddonManagerScreen
import space.celestia.celestiaui.resource.SingleAddonScreen
import space.celestia.celestiaui.resource.WebPage
import space.celestia.celestiaui.resource.WebScreen
import space.celestia.celestiaui.search.SearchScreen
import space.celestia.celestiaui.settings.Settings
import space.celestia.celestiaui.settings.TimeSettingsContainer
import space.celestia.celestiaui.travel.GoToContainer
import space.celestia.celestiaui.utils.URLHelper
import space.celestia.celestiaui.tool.viewmodel.ToolPage
import space.celestia.celestiaui.tool.viewmodel.ToolViewModel
import java.io.File

@Composable
fun ToolScreen(
    backStack: List<ToolPage>,
    linkClicked: (String, Boolean) -> Unit,
    providePreferredDisplay: () -> Display?,
    refreshRateChanged: (Int) -> Unit,
    requestRunScript: (File) -> Unit,
    requestShareAddon: (String, String) -> Unit,
    shareRequested: (MutableFavoriteBaseItem) -> Unit,
    openBookmarkRequested: (FavoriteBookmarkItem) -> Unit,
    openScriptRequested: (FavoriteScriptItem) -> Unit,
    saveFavorites: () -> Unit
) {
    if (backStack.isEmpty()) return

    val viewModel: ToolViewModel = hiltViewModel()
    NavDisplay(backStack, entryProvider = { entry ->
        when (entry) {
            is ToolPage.Settings -> NavEntry(entry) {
                Settings(
                    linkClicked = linkClicked,
                    providePreferredDisplay = providePreferredDisplay,
                    refreshRateChanged = refreshRateChanged,
                    openSubscriptionManagement = {
                        if (backStack is MutableList && viewModel.purchaseManager.canUseInAppPurchase()) {
                            backStack.add(ToolPage.SubscriptionManager(null))
                        }
                    }
                )
            }
            is ToolPage.Browser -> NavEntry(entry) {
                Browser(
                    linkClicked = {
                        linkClicked(it, false)
                    },
                    openSubscriptionManagement = {
                        if (backStack is MutableList && viewModel.purchaseManager.canUseInAppPurchase()) {
                            backStack.add(ToolPage.SubscriptionManager(null))
                        }
                    },
                    openSubsystem = {
                        if (backStack is MutableList) {
                            backStack.add(ToolPage.SubsystemBrowser(it))
                        }
                    },
                    addonCategoryRequested = {
                        if (backStack is MutableList) {
                            backStack.add(ToolPage.AddonDownload(isLeaf = it.isLeaf, categoryId = it.id))
                        }
                    },
                    openRelatedAddons = {
                        if (backStack is MutableList) {
                            backStack.add(ToolPage.RelatedAddons(it))
                        }
                    }
                )
            }
            is ToolPage.SubsystemBrowser -> NavEntry(entry) {
                SubsystemBrowser(
                    selection = entry.selection,
                    linkClicked = {
                        linkClicked(it, false)
                    },
                    openSubscriptionManagement = {
                        if (backStack is MutableList && viewModel.purchaseManager.canUseInAppPurchase()) {
                            backStack.add(ToolPage.SubscriptionManager(null))
                        }
                    },
                    openSubsystem = {
                        if (backStack is MutableList) {
                            backStack.add(ToolPage.SubsystemBrowser(it))
                        }
                    },
                    addonCategoryRequested = {
                        if (backStack is MutableList) {
                            backStack.add(ToolPage.AddonDownload(isLeaf = it.isLeaf, categoryId = it.id))
                        }
                    },
                    openRelatedAddons = {
                        if (backStack is MutableList) {
                            backStack.add(ToolPage.RelatedAddons(it))
                        }
                    }
                )
            }
            is ToolPage.Info -> NavEntry(entry) {
                Scaffold(contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Bottom)) { paddingValues ->
                    InfoScreen(
                        selection = entry.selection,
                        linkClicked = {
                            linkClicked(it, false)
                        },
                        openSubscriptionManagement = {
                            if (backStack is MutableList && viewModel.purchaseManager.canUseInAppPurchase()) {
                                backStack.add(ToolPage.SubscriptionManager(null))
                            }
                        },
                        openSubsystem = {
                            if (backStack is MutableList) {
                                backStack.add(ToolPage.SubsystemBrowser(entry.selection))
                            }
                        },
                        openRelatedAddons = {
                            if (backStack is MutableList) {
                                backStack.add(ToolPage.RelatedAddons(it))
                            }
                        },
                        showTitle = true,
                        paddingValues = paddingValues
                    )
                }
            }
            is ToolPage.AddonDownload -> NavEntry(entry) {
                AddonDownload(
                    isLeaf = entry.isLeaf, categoryId = entry.categoryId,
                    requestRunScript = requestRunScript,
                    requestShareAddon = requestShareAddon
                )
            }
            is ToolPage.Search -> NavEntry(entry) {
                SearchScreen(
                    linkClicked = {
                        linkClicked(it, false)
                    },
                    openSubsystem = {
                        if (backStack is MutableList) {
                            backStack.add(ToolPage.SubsystemBrowser(it))
                        }
                    },
                    openRelatedAddons = {
                        if (backStack is MutableList) {
                            backStack.add(ToolPage.RelatedAddons(it))
                        }
                    },
                    openSubscriptionManagement = {
                        if (backStack is MutableList && viewModel.purchaseManager.canUseInAppPurchase()) {
                            backStack.add(ToolPage.SubscriptionManager(null))
                        }
                    }
                )
            }
            is ToolPage.Addon -> NavEntry(entry) {
                SingleAddonScreen(
                    item = entry.item,
                    requestRunScript = requestRunScript,
                    requestShareAddon = requestShareAddon,
                )
            }
            is ToolPage.Article -> NavEntry(entry) {
                val uri by remember { mutableStateOf(URLHelper.buildInAppGuideURI(id = entry.id, language = AppCore.getLanguage(), platform = viewModel.platform, purchaseManager = viewModel.purchaseManager)) }
                Scaffold(contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Bottom)) { paddingValues ->
                    WebPage(
                        uri = uri,
                        filterURL = true,
                        matchingQueryKeys = listOf("guide"),
                        paddingValues = paddingValues
                    )
                }
            }
            is ToolPage.TimeSettings -> NavEntry(entry) {
                TimeSettingsContainer()
            }
            is ToolPage.CameraControl -> NavEntry(entry) {
                CameraControlContainer(observerModeLearnMoreClicked = { link, localizable ->
                    linkClicked(link, localizable)
                })
            }
            is ToolPage.Help -> NavEntry(entry) {
                NewHelpScreen(linkClicked = {
                    linkClicked(it, false)
                })
            }
            is ToolPage.Favorites -> NavEntry(entry) {
                FavoriteContainer(
                    shareRequested = shareRequested,
                    saveFavorites = saveFavorites,
                    openBookmarkRequested = openBookmarkRequested,
                    openScriptRequested = openScriptRequested
                )
            }
            is ToolPage.EventFinder -> NavEntry(entry) {
                EventFinder()
            }
            is ToolPage.GoTo -> NavEntry(entry) {
                GoToContainer()
            }
            is ToolPage.InstalledAddons -> NavEntry(entry) {
                AddonManagerScreen(
                    requestRunScript = requestRunScript,
                    requestShareAddon = requestShareAddon,
                    openSubscriptionManagement = {
                        if (backStack is MutableList && viewModel.purchaseManager.canUseInAppPurchase()) {
                            backStack.add(ToolPage.SubscriptionManager(null))
                        }
                    },
                    requestOpenAddonDownload = {
                        if (backStack is MutableList) {
                            backStack.add(ToolPage.AddonDownload(null, null))
                        }
                    }
                )
            }
            is ToolPage.RelatedAddons -> NavEntry(entry) {
                val uri by remember { mutableStateOf(URLHelper.buildInAppRelatedAddonsURI(objectPath = entry.objectPath, language = AppCore.getLanguage(), platform = viewModel.platform, purchaseManager = viewModel.purchaseManager)) }
                WebScreen(uri, requestRunScript = requestRunScript, requestShareAddon = requestShareAddon)
            }
            is ToolPage.SubscriptionManager -> NavEntry(entry) {
                viewModel.purchaseManager.ManagerScreen(entry.preferredPlayOfferId)
            }
        }
    }, transitionSpec = {
        slideInHorizontally(initialOffsetX = { it }) togetherWith slideOutHorizontally(targetOffsetX = { -it })
    }, popTransitionSpec = {
        slideInHorizontally(initialOffsetX = { -it }) togetherWith slideOutHorizontally(targetOffsetX = { it })
    }, predictivePopTransitionSpec = {
        slideInHorizontally(initialOffsetX = { -it }) togetherWith slideOutHorizontally(targetOffsetX = { it })
    }, entryDecorators = listOf(
        // https://developer.android.com/guide/navigation/navigation-3/save-state#scoping-viewmodels
        // Add the default decorators for managing scenes and saving state
        rememberSaveableStateHolderNavEntryDecorator(),
        // Then add the view model store decorator
        rememberViewModelStoreNavEntryDecorator()
    ))
}