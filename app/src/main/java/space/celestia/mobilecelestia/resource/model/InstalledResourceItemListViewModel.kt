package space.celestia.mobilecelestia.resource.model

import androidx.lifecycle.map
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class InstalledResourceItemListViewModel @Inject constructor(
    private val repository: ResourceRepository) : AsyncListPagingViewModel() {
    override fun getItemsWithoutSeparators() =
        repository.getInstalledItems().map { it.map { it as AsyncListItem } }

    override val cacheEnabled: Boolean
        get() = false
}