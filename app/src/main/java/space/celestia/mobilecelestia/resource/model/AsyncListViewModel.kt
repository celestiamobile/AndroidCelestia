package space.celestia.mobilecelestia.resource.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import space.celestia.mobilecelestia.R

abstract class AsyncListPagingViewModel: ViewModel() {
    abstract fun getItemsWithoutSeparators(): LiveData<PagingData<AsyncListItem>>
    abstract val cacheEnabled: Boolean

    private var savedItems: LiveData<PagingData<AsyncListPagingItem>>? = null

    fun getItems(): LiveData<PagingData<AsyncListPagingItem>> {
        val savedITS = savedItems
        if (cacheEnabled && savedITS != null) {
            return savedITS
        }

        val newITS = getItemsWithoutSeparators().map {
            it.map { AsyncListPagingItem.Data(it) as AsyncListPagingItem }
        }.map {
            var transformed = it.insertSeparators { before, after ->
                if (before == null && after == null)
                    return@insertSeparators null
                if (before == null || after == null)
                    return@insertSeparators AsyncListPagingItem.Separator(R.dimen.full_separator_inset_start)
                return@insertSeparators AsyncListPagingItem.Separator(R.dimen.partial_separator_inset_start)
            }
            transformed = transformed.insertHeaderItem(item = AsyncListPagingItem.Header)
                .insertFooterItem(item = AsyncListPagingItem.Footer)
            return@map transformed
        }.cachedIn(viewModelScope)
        if (cacheEnabled) {
            savedItems = newITS
        }
        return newITS
    }
}