package space.celestia.mobilecelestia.resource.model

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GuideListViewModel @Inject constructor(
    repository: ResourceRepository,
    savedStateHandle: SavedStateHandle
) : AsyncListPagingViewModel() {
    val type: String = savedStateHandle.get(ARG_TYPE)!!
    val title: String = savedStateHandle.get(ARG_TITLE)!!
    val language: String = savedStateHandle.get(ResourceItemListViewModel.ARG_LANG)!!

    override val itemsWithoutSeparators =
        repository.getGuides(type = type, language = language).map { it.map { it as AsyncListItem } }

    override val stylized: Boolean
        get() = false

    companion object {
        const val ARG_TYPE = "type"
        const val ARG_TITLE = "title"
        const val ARG_LANG = "lang"
    }
}