package space.celestia.mobilecelestia.resource.model

import androidx.lifecycle.map
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class InstalledResourceItemListViewModel @Inject constructor(
    repository: ResourceRepository) : AsyncListPagingViewModel() {
    override val itemsWithoutSeparators =
        repository.getInstalledItems().map { it.map { it as AsyncListItem } }
}