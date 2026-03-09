package space.celestia.mobilecelestia.tool.viewmodel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import space.celestia.celestia.Selection
import space.celestia.celestiafoundation.resource.model.ResourceItem

sealed class ToolPage: Parcelable {
    @Parcelize
    data object Settings: ToolPage()
    @Parcelize
    data object Browser: ToolPage()
    @Parcelize
    data class SubsystemBrowser(val selection: Selection): ToolPage()
    @Parcelize
    data class Info(val selection: Selection): ToolPage()
    @Parcelize
    data class AddonDownload(val isLeaf: Boolean?, val categoryId: String?): ToolPage()
    @Parcelize
    data object Search: ToolPage()
    @Parcelize
    data class Addon(val item: ResourceItem): ToolPage()
    @Parcelize
    data class Article(val id: String): ToolPage()
    @Parcelize
    data object TimeSettings: ToolPage()
    @Parcelize
    data object CameraControl: ToolPage()
    @Parcelize
    data object Help: ToolPage()
    @Parcelize
    data object Favorites: ToolPage()
    @Parcelize
    data object EventFinder: ToolPage()
    @Parcelize
    data object GoTo: ToolPage()
    @Parcelize
    data object InstalledAddons: ToolPage()
    @Parcelize
    data class RelatedAddons(val objectPath: String): ToolPage()
    @Parcelize
    data class SubscriptionManager(val preferredPlayOfferId: String?): ToolPage()
}