package space.celestia.mobilecelestia.resource.model

sealed class AsyncListPagingItem {
    data class Data(val data: AsyncListItem): AsyncListPagingItem()
    data class Separator(val inset: Float): AsyncListPagingItem()
    object Header : AsyncListPagingItem()
    object Footer : AsyncListPagingItem()
}
