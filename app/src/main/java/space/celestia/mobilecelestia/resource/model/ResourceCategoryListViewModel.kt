package space.celestia.mobilecelestia.resource.model

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.map
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ResourceCategoryListViewModel @Inject constructor(
    repository: ResourceRepository,
    savedStateHandle: SavedStateHandle
): AsyncListPagingViewModel() {
    val language: String = savedStateHandle.get(ResourceItemListViewModel.ARG_LANG)!!
    override val itemsWithoutSeparators =
        repository.getCategories(language = language).map { it.map { it as AsyncListItem } }

    override val stylized: Boolean
        get() = true

    companion object {
        const val ARG_LANG = "lang"
    }
}