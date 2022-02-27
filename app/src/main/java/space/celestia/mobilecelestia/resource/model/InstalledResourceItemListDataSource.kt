package space.celestia.mobilecelestia.resource.model

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlin.math.max
import kotlin.math.min

class InstalledResourceItemListDataSource(
    private val resourceManager: ResourceManager,
): PagingSource<Int, ResourceItem>() {
    private var allInstalledItems: List<ResourceItem> = arrayListOf()

    override fun getRefreshKey(state: PagingState<Int, ResourceItem>): Int? {
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ResourceItem> {
        val position = params.key ?: 0
        return try {
            if (allInstalledItems.isEmpty()) {
                allInstalledItems = resourceManager.installedResourcesAsync()
            }

            val allItems = allInstalledItems
            if (allItems.isEmpty() || position < 0 || position >= allItems.size || params.loadSize <= 0) {
                return LoadResult.Page(listOf(), null, null)
            }

            val data = allItems.subList(position, min(allItems.size, position + params.loadSize))
            LoadResult.Page(
                data = data,
                prevKey = if (position == 0) null else max(0, position - params.loadSize),
                nextKey = if (data.isEmpty()) null else position + data.size
            )
        } catch (exception: Throwable) {
            LoadResult.Error(exception)
        }
    }
}