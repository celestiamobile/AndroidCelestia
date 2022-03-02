package space.celestia.mobilecelestia.resource.model

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.liveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResourceRepository @Inject constructor(val resourceAPI: ResourceAPIService, val resourceManager: ResourceManager) {
    fun getCategories(lang: String) =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { ResourceCategoryListDataSource(resourceAPI, lang) }
        ).liveData

    fun getItems(category: String, lang: String) =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { ResourceItemListDataSource(resourceAPI, lang, category) }
        ).liveData

    fun getGuides(type: String, lang: String) =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { GuideListDataSource(resourceAPI, lang, type) }
        ).liveData

    fun getInstalledItems() =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { InstalledResourceItemListDataSource(resourceManager) }
        ).liveData
}