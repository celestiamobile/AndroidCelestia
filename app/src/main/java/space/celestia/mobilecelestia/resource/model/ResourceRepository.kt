package space.celestia.mobilecelestia.resource.model

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.liveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResourceRepository @Inject constructor(val resourceAPI: ResourceAPIService, val celestiaLanguage: String, val resourceManager: ResourceManager) {
    fun getCategories() =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { ResourceCategoryListDataSource(resourceAPI, celestiaLanguage) }
        ).liveData

    fun getItems(category: String) =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { ResourceItemListDataSource(resourceAPI, celestiaLanguage, category) }
        ).liveData

    fun getGuides(type: String) =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { GuideListDataSource(resourceAPI, celestiaLanguage, type) }
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