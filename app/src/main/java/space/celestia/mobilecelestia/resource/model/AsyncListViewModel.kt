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
            val transformed = it.insertFooterItem(item = AsyncListPagingItem.Footer)
            return@map transformed
        }.cachedIn(viewModelScope)
        if (cacheEnabled) {
            savedItems = newITS
        }
        return newITS
    }
}