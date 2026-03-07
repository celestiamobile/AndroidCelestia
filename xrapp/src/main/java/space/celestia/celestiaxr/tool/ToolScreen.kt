package space.celestia.celestiaxr.tool

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import space.celestia.celestia.AppCore
import space.celestia.celestia.Selection
import space.celestia.celestiafoundation.resource.model.ResourceItem
import space.celestia.celestiaui.browser.Browser
import space.celestia.celestiaui.browser.SubsystemBrowser
import space.celestia.celestiaui.compose.EmptyHint
import space.celestia.celestiaui.control.CameraControlContainer
import space.celestia.celestiaui.eventfinder.EventFinder
import space.celestia.celestiaui.favorite.FavoriteContainer
import space.celestia.celestiaui.favorite.MutableFavoriteBaseItem
import space.celestia.celestiaui.help.NewHelpScreen
import space.celestia.celestiaui.info.InfoScreen
import space.celestia.celestiaui.resource.AddonDownload
import space.celestia.celestiaui.resource.AddonManagerScreen
import space.celestia.celestiaui.resource.AddonScreen
import space.celestia.celestiaui.resource.SingleAddonScreen
import space.celestia.celestiaui.resource.WebPage
import space.celestia.celestiaui.search.SearchScreen
import space.celestia.celestiaui.settings.Settings
import space.celestia.celestiaui.settings.TimeSettingsContainer
import space.celestia.celestiaui.travel.GoToContainer
import space.celestia.celestiaui.utils.CelestiaString
import space.celestia.celestiaui.utils.URLHelper
import space.celestia.celestiaxr.tool.viewmodel.ToolViewModel
import java.io.File

private sealed class AddonFetchResult {
    data class Success(val addon: ResourceItem) : AddonFetchResult()
    object Failure : AddonFetchResult()
}

@Composable
fun ToolScreen(page: Tool.Page, linkClicked: (String, Boolean) -> Unit, requestRunScript: (File) -> Unit, requestShareAddon: (String, String) -> Unit, requestRunFavoriteScript: (String) -> Unit, requestOpenCelestiaURL: (String) -> Unit, requestShareFavorite: (MutableFavoriteBaseItem) -> Unit, saveFavorites: () -> Unit) {
    val viewModel: ToolViewModel = hiltViewModel()
    val context = LocalContext.current
    when (page) {
        is Tool.Page.Info -> {
            var selection by remember { mutableStateOf<Selection?>(null) }
            LaunchedEffect(Unit) {
                selection = withContext(viewModel.executor.asCoroutineDispatcher()) {
                    viewModel.appCore.simulation.selection
                }
            }
            Scaffold { paddingValues ->
                val currentSelection = selection
                if (currentSelection == null) {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    InfoScreen(selection = currentSelection, showTitle = true, linkClicked = {
                        linkClicked(it, false)
                    }, openSubsystem = {
                        val intent = Intent(context, ToolActivity::class.java)
                        intent.putExtra(ToolActivity.EXTRA_TOOL, Tool.Page.Subsystem(currentSelection))
                        context.startActivity(intent)
                    }, openRelatedAddons = {}, openSubscriptionManagement = {}, paddingValues = paddingValues)
                }
            }
        }
        is Tool.Page.StarBrowser -> {
            Browser(linkClicked = {
                linkClicked(it, false)
            }, openSubscriptionManagement = {}, openSubsystem = {
                val intent = Intent(context, ToolActivity::class.java)
                intent.putExtra(ToolActivity.EXTRA_TOOL, Tool.Page.Subsystem(it))
                context.startActivity(intent)
            }, addonCategoryRequested = {
                val intent = Intent(context, ToolActivity::class.java)
                intent.putExtra(ToolActivity.EXTRA_TOOL, Tool.Page.GetCategoryAddon(it.isLeaf, it.id))
                context.startActivity(intent)
            }, openRelatedAddons = {})
        }
        is Tool.Page.Search -> {
            SearchScreen(
                linkClicked = {
                    linkClicked(it, false)
                },
                openSubsystem = {
                    val intent = Intent(context, ToolActivity::class.java)
                    intent.putExtra(ToolActivity.EXTRA_TOOL, Tool.Page.Subsystem(it))
                    context.startActivity(intent)
                },
                openRelatedAddons = {},
                openSubscriptionManagement = {}
            )
        }
        is Tool.Page.GoTo -> {
            GoToContainer()
        }
        is Tool.Page.EclipseFinder -> {
            EventFinder()
        }
        is Tool.Page.CameraControl -> {
            CameraControlContainer(observerModeLearnMoreClicked = { link, localized ->
                linkClicked(link, localized)
            })
        }
        is Tool.Page.CurrentTime -> {
            TimeSettingsContainer()
        }
        is Tool.Page.Favorites -> {
            FavoriteContainer(shareRequested = {
                requestShareFavorite(it)
            }, openBookmarkRequested = { favorite ->
                requestOpenCelestiaURL(favorite.bookmark.url)
            }, openScriptRequested = { favorite ->
                requestRunFavoriteScript(favorite.script.filename)
            }, saveFavorites = saveFavorites)
        }
        is Tool.Page.Settings -> {
            Settings(
                linkClicked = { link, localized ->
                    linkClicked(link, localized)
                },
                providePreferredDisplay = { null },
                refreshRateChanged = {},
                openSubscriptionManagement = {}
            )
        }
        is Tool.Page.InstalledAddons -> {
            AddonManagerScreen(requestRunScript = { script ->
                requestRunScript(script)
            }, requestShareAddon = { name, id ->
                requestShareAddon(name, id)
            }, openSubscriptionManagement = {}, requestOpenAddonDownload = {
                val intent = Intent(context, ToolActivity::class.java)
                intent.putExtra(ToolActivity.EXTRA_TOOL, Tool.Page.GetAddons)
                context.startActivity(intent)
            })
        }
        is Tool.Page.GetAddons -> {
            AddonDownload(isLeaf = null, categoryId = null, requestRunScript = { script ->
                requestRunScript(script)
            }, requestShareAddon = { name, id ->
                requestShareAddon(name, id)
            })
        }
        is Tool.Page.Help -> {
            NewHelpScreen(linkClicked = {
                linkClicked(it, false)
            })
        }
        is Tool.Page.Subsystem -> {
            SubsystemBrowser(
                selection = page.selection,
                linkClicked = {
                    linkClicked(it, false)
                },
                openSubsystem = {
                    val intent = Intent(context, ToolActivity::class.java)
                    intent.putExtra(ToolActivity.EXTRA_TOOL, Tool.Page.Subsystem(it))
                    context.startActivity(intent)
                },
                addonCategoryRequested = {
                    val intent = Intent(context, ToolActivity::class.java)
                    intent.putExtra(ToolActivity.EXTRA_TOOL, Tool.Page.GetCategoryAddon(it.isLeaf, it.id))
                    context.startActivity(intent)
                },
                openRelatedAddons = {},
                openSubscriptionManagement = {}
            )
        }
        is Tool.Page.GetCategoryAddon -> {
            AddonDownload(isLeaf = page.isLeaf, categoryId = page.categoryId, requestRunScript = { script ->
                requestRunScript(script)
            }, requestShareAddon = { name, id ->
                requestShareAddon(name, id)
            })
        }
        is Tool.Page.ObjectInfo -> {
            Scaffold { paddingValues ->
                InfoScreen(selection = page.selection, showTitle = true, linkClicked = {
                    linkClicked(it, false)
                }, openSubsystem = {
                    val intent = Intent(context, ToolActivity::class.java)
                    intent.putExtra(ToolActivity.EXTRA_TOOL, Tool.Page.Subsystem(page.selection))
                    context.startActivity(intent)
                }, openRelatedAddons = {}, openSubscriptionManagement = {}, paddingValues = paddingValues)
            }
        }
        is Tool.Page.Addon -> {
            var addonFetchResult by remember { mutableStateOf<AddonFetchResult?>(null) }
            LaunchedEffect(Unit) {
                try {
                    val item = viewModel.resourceAPI.item(lang = AppCore.getLanguage(), item = page.id)
                    addonFetchResult = AddonFetchResult.Success(item)
                } catch (_: Throwable) {
                    addonFetchResult = AddonFetchResult.Failure
                }
            }
            Scaffold { paddingValues ->
                when (val currentResult = addonFetchResult) {
                    is AddonFetchResult.Success -> {
                        SingleAddonScreen(
                            item = currentResult.addon,
                            requestRunScript = { script ->
                                requestRunScript(script)
                            },
                            requestShareAddon = { name, id ->
                                requestShareAddon(name, id)
                            }
                        )
                    }
                    is AddonFetchResult.Failure -> {
                        Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                            EmptyHint(text = CelestiaString("Failed to load add-on", ""))
                        }
                    }
                    null -> {
                        Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
        is Tool.Page.Article -> {
            val uri by remember { mutableStateOf(URLHelper.buildInAppGuideURI(
                id = page.id, language = AppCore.getLanguage(),
                flavor = viewModel.flavor,
                purchaseManager = viewModel.purchaseManager
            )) }
            Scaffold { paddingValues ->
                WebPage(
                    uri = uri,
                    filterURL = true,
                    matchingQueryKeys = listOf("guide"),
                    paddingValues = paddingValues
                )
            }
        }
    }
}
