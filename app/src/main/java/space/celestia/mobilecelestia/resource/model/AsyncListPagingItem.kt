package space.celestia.mobilecelestia.resource.model

sealed class AsyncListPagingItem {
    data class Data(val data: AsyncListItem): AsyncListPagingItem()
    data class Separator(val dividerInsetStartResource: Int): AsyncListPagingItem()
    object Header : AsyncListPagingItem()
    object Footer : AsyncListPagingItem()
}
