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
    celestiaLanguage: String,
    savedStateHandle: SavedStateHandle) : AsyncListPagingViewModel() {
    val type: String = savedStateHandle.get(ARG_TYPE)!!
    val title: String = savedStateHandle.get(ARG_TITLE)!!

    override val itemsWithoutSeparators =
        repository.getGuides(type = type, lang = celestiaLanguage).map { it.map { it as AsyncListItem } }

    override val stylized: Boolean
        get() = false

    companion object {
        const val ARG_TYPE = "type"
        const val ARG_TITLE = "title"
    }
}