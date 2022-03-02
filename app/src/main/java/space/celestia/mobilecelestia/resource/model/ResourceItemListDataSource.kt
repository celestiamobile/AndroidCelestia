package space.celestia.mobilecelestia.resource.model

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.gson.reflect.TypeToken
import space.celestia.mobilecelestia.utils.commonHandler
import kotlin.math.max
import kotlin.math.min

class ResourceItemListDataSource(
    private val api: ResourceAPIService,
    private val lang: String,
    private val category: String
): PagingSource<Int, ResourceItem>() {
    override fun getRefreshKey(state: PagingState<Int, ResourceItem>): Int? {
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ResourceItem> {
        val position = params.key ?: 0
        return try {
            val data: List<ResourceItem> = api.items(lang, category = category, pageStart = position, pageSize = params.loadSize).commonHandler(object: TypeToken<ArrayList<ResourceItem>>() {}.type, ResourceAPI.gson)
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