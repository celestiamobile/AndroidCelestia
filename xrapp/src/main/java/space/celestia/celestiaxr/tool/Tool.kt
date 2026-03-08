package space.celestia.celestiaxr.tool

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import space.celestia.celestia.Selection
import space.celestia.celestiafoundation.resource.model.ResourceItem
import space.celestia.celestiaui.utils.CelestiaString

sealed interface Tool {
    val title: String

    data object Info : Tool { override val title get() = CelestiaString("Get Info", "Action for getting info about current selected object") }

    sealed class Page : Tool, Parcelable {
        @Parcelize
        data object StarBrowser : Page() { @IgnoredOnParcel override val title get() = CelestiaString("Star Browser", "") }
        @Parcelize
        data object Search : Page() { @IgnoredOnParcel override val title get() = CelestiaString("Search", "") }
        @Parcelize
        data object GoTo : Page() { @IgnoredOnParcel override val title get() = CelestiaString("Go to Object", "") }
        @Parcelize
        data object EclipseFinder : Page() { @IgnoredOnParcel override val title get() = CelestiaString("Eclipse Finder", "") }
        @Parcelize
        data object CameraControl : Page() { @IgnoredOnParcel override val title get() = CelestiaString("Camera Control", "Observer control") }
        @Parcelize
        data object CurrentTime : Page() { @IgnoredOnParcel override val title get() = CelestiaString("Current Time", "") }
        @Parcelize
        data object Favorites : Page() { @IgnoredOnParcel override val title get() = CelestiaString("Favorites", "Favorites (currently bookmarks and scripts)") }
        @Parcelize
        data object Settings : Page() { @IgnoredOnParcel override val title get() = CelestiaString("Settings", "") }
        @Parcelize
        data object InstalledAddons : Page() { @IgnoredOnParcel override val title get() = CelestiaString("Installed Add-ons", "Open a page for managing installed add-ons") }
        @Parcelize
        data object GetAddons : Page() { @IgnoredOnParcel override val title get() = CelestiaString("Get Add-ons", "Open webpage for downloading add-ons") }
        @Parcelize
        data object Help : Page() { @IgnoredOnParcel override val title get() = CelestiaString("Help", "") }

        @Parcelize
        data class Subsystem(val selection: Selection) : Page() { @IgnoredOnParcel override val title = "" }
        @Parcelize
        data class GetCategoryAddon(val isLeaf: Boolean, val categoryId: String) : Page() { @IgnoredOnParcel override val title = "" }

        @Parcelize
        data class ObjectInfo(val selection: Selection) : Page() { @IgnoredOnParcel override val title = "" }

        @Parcelize
        data class Addon(val item: ResourceItem) : Page() { @IgnoredOnParcel override val title = "" }

        @Parcelize
        data class Article(val id: String) : Page() { @IgnoredOnParcel override val title = "" }
    }

    companion object {
        val all: List<Tool> = listOf(
            Info, Page.StarBrowser, Page.Search, Page.GoTo, Page.EclipseFinder,
            Page.CameraControl, Page.CurrentTime, Page.Favorites, Page.Settings,
            Page.InstalledAddons, Page.GetAddons, Page.Help
        )
    }
}
