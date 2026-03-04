package space.celestia.celestiaxr.tool

import space.celestia.celestia.Selection
import space.celestia.celestiafoundation.resource.model.ResourceItem
import space.celestia.celestiaui.tool.viewmodel.ToolPage
import space.celestia.celestiaui.utils.CelestiaString

sealed interface Tool {
    val title: String

    data object Info : Tool { override val title get() = CelestiaString("Get Info", "Action for getting info about current selected object") }

    sealed interface Page : Tool {
        val page: ToolPage
        data object StarBrowser : Page { override val title get() = CelestiaString("Star Browser", ""); override val page: ToolPage get() = ToolPage.Browser }
        data object Search : Page { override val title get() = CelestiaString("Search", ""); override val page: ToolPage get() = ToolPage.Search }
        data object GoTo : Page { override val title get() = CelestiaString("Go to Object", ""); override val page: ToolPage get() = ToolPage.GoTo }
        data object EclipseFinder : Page { override val title get() = CelestiaString("Eclipse Finder", ""); override val page: ToolPage get() = ToolPage.EventFinder }
        data object CameraControl : Page { override val title get() = CelestiaString("Camera Control", "Observer control"); override val page: ToolPage get() = ToolPage.CameraControl }
        data object CurrentTime : Page { override val title get() = CelestiaString("Current Time", ""); override val page: ToolPage get() = ToolPage.TimeSettings }
        data object Favorites : Page { override val title get() = CelestiaString("Favorites", "Favorites (currently bookmarks and scripts)"); override val page: ToolPage get() = ToolPage.Favorites }
        data object Settings : Page { override val title get() = CelestiaString("Settings", ""); override val page: ToolPage get() = ToolPage.Settings }
        data object InstalledAddons : Page { override val title get() = CelestiaString("Installed Add-ons", "Open a page for managing installed add-ons"); override val page: ToolPage get() = ToolPage.InstalledAddons }
        data object GetAddons : Page { override val title get() = CelestiaString("Get Add-ons", "Open webpage for downloading add-ons"); override val page: ToolPage get() = ToolPage.AddonDownload(null, null) }
        data object Help : Page { override val title get() = CelestiaString("Help", ""); override val page: ToolPage get() = ToolPage.Help }
        data class ObjectInfo(val selection: Selection) : Page { override val title = ""; override val page: ToolPage get() = ToolPage.Info(selection) }
        data class Addon(val item: ResourceItem) : Page { override val title = ""; override val page: ToolPage get() = ToolPage.Addon(item) }
        data class Article(val id: String) : Page { override val title = ""; override val page: ToolPage get() = ToolPage.Article(id) }
    }

    companion object {
        val all: List<Tool> = listOf(
            Info, Page.StarBrowser, Page.Search, Page.GoTo, Page.EclipseFinder,
            Page.CameraControl, Page.CurrentTime, Page.Favorites, Page.Settings,
            Page.InstalledAddons, Page.GetAddons, Page.Help
        )
    }
}
