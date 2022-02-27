package space.celestia.mobilecelestia.resource.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import androidx.paging.*

abstract class AsyncListPagingViewModel: ViewModel() {
    abstract val itemsWithoutSeparators: LiveData<PagingData<AsyncListItem>>
    abstract val stylized: Boolean

    private val showSeparator: Boolean
        get() = !stylized

    val items by lazy { itemsWithoutSeparators.map {
        it.map { AsyncListPagingItem.Data(it) as AsyncListPagingItem }
    }.map {
        var transformed = it.insertSeparators { before, after ->
            if (showSeparator) {
                if (before == null && after == null)
                    return@insertSeparators null
                if (before == null || after == null)
                    return@insertSeparators AsyncListPagingItem.Separator(0.0f)
                return@insertSeparators AsyncListPagingItem.Separator(16.0f)
            }
            return@insertSeparators null
        }
        if (showSeparator) {
            transformed = transformed.insertHeaderItem(item = AsyncListPagingItem.Header)
                .insertFooterItem(item = AsyncListPagingItem.Footer)
        }
        return@map transformed
    }.cachedIn(viewModelScope) }
}