package space.celestia.mobilecelestia.resource.model

import androidx.lifecycle.map
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ResourceCategoryListViewModel @Inject constructor(
    repository: ResourceRepository,
    celestiaLanguage: String
): AsyncListPagingViewModel() {
    override val itemsWithoutSeparators =
        repository.getCategories(celestiaLanguage).map { it.map { it as AsyncListItem } }

    override val stylized: Boolean
        get() = true
}