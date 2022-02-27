package space.celestia.mobilecelestia.resource.model

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.gson.reflect.TypeToken
import space.celestia.mobilecelestia.utils.commonHandler
import kotlin.math.min

class GuideListDataSource(
    private val api: ResourceAPIService,
    private val lang: String,
    private val type: String
): PagingSource<Int, GuideItem>() {
    override fun getRefreshKey(state: PagingState<Int, GuideItem>): Int? {
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GuideItem> {
        val position = params.key ?: 0
        return try {
            val data: List<GuideItem> = api.guides(lang = lang, type = type, pageStart = position, pageSize = params.loadSize).commonHandler(object: TypeToken<ArrayList<GuideItem>>() {}.type, ResourceAPI.gson)
            LoadResult.Page(
                data = data,
                prevKey = if (position == 0) null else min(0, position - params.loadSize),
                nextKey = if (data.isEmpty()) null else position + data.size
            )
        } catch (exception: Throwable) {
            LoadResult.Error(exception)
        }
    }
}