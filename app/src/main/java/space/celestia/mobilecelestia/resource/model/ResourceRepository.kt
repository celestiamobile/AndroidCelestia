package space.celestia.mobilecelestia.resource.model

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.liveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResourceRepository @Inject constructor(val resourceAPI: ResourceAPIService, val resourceManager: ResourceManager) {
    fun getCategories(language: String) =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 40,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { ResourceCategoryListDataSource(resourceAPI, language) }
        ).liveData

    fun getItems(category: String, language: String) =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 40,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { ResourceItemListDataSource(resourceAPI, language, category) }
        ).liveData

    fun getGuides(type: String, language: String) =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 40,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { GuideListDataSource(resourceAPI, language, type) }
        ).liveData

    fun getInstalledItems() =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 40,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { InstalledResourceItemListDataSource(resourceManager) }
        ).liveData
}