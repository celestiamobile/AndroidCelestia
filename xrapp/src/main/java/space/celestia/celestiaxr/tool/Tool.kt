package space.celestia.celestiaxr.tool

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import space.celestia.celestia.Selection

sealed interface Tool {
    val title: String

    sealed class Page : Tool, Parcelable {
        @Parcelize
        data object Info : Page() { @IgnoredOnParcel override val title = "Info" }
        @Parcelize
        data object StarBrowser : Page() { @IgnoredOnParcel override val title = "Star Browser" }
        @Parcelize
        data object Search : Page() { @IgnoredOnParcel override val title = "Search" }
        @Parcelize
        data object GoTo : Page() { @IgnoredOnParcel override val title = "Go to Object" }
        @Parcelize
        data object EclipseFinder : Page() { @IgnoredOnParcel override val title = "Eclipse Finder" }
        @Parcelize
        data object CameraControl : Page() { @IgnoredOnParcel override val title = "Camera Control" }
        @Parcelize
        data object CurrentTime : Page() { @IgnoredOnParcel override val title = "Current Time" }
        @Parcelize
        data object Favorites : Page() { @IgnoredOnParcel override val title = "Favorites" }
        @Parcelize
        data object Settings : Page() { @IgnoredOnParcel override val title = "Settings" }
        @Parcelize
        data object InstalledAddons : Page() { @IgnoredOnParcel override val title = "Installed Add-ons" }
        @Parcelize
        data object GetAddons : Page() { @IgnoredOnParcel override val title = "Get Add-ons" }
        @Parcelize
        data object Help : Page() { @IgnoredOnParcel override val title = "Help" }

        @Parcelize
        data class Subsystem(val selection: Selection) : Page() { @IgnoredOnParcel override val title = "" }
        @Parcelize
        data class GetCategoryAddon(val isLeaf: Boolean, val categoryId: String) : Page() { @IgnoredOnParcel override val title = "" }

        @Parcelize
        data class ObjectInfo(val selection: Selection) : Page() { @IgnoredOnParcel override val title = "" }

        @Parcelize
        data class Addon(val id: String) : Page() { @IgnoredOnParcel override val title = "" }

        @Parcelize
        data class Article(val id: String) : Page() { @IgnoredOnParcel override val title = "" }
    }

    data object Pause : Tool { override val title = "Pause" }

    companion object {
        val all: List<Tool> = listOf(
            Page.Info, Page.StarBrowser, Page.Search, Page.GoTo, Page.EclipseFinder,
            Page.CameraControl, Page.CurrentTime, Page.Favorites, Page.Settings,
            Page.InstalledAddons, Page.GetAddons, Page.Help, Pause
        )
    }
}
