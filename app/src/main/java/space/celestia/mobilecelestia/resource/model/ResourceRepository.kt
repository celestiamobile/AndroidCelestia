package space.celestia.mobilecelestia.resource.model

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.liveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResourceRepository @Inject constructor(val resourceAPI: ResourceAPIService, val resourceManager: ResourceManager) {
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