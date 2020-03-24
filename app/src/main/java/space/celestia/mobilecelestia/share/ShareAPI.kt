package space.celestia.mobilecelestia.share

import com.google.gson.Gson
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import io.reactivex.Observable
import io.reactivex.functions.Function
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

class BaseResult(val status: Int, val info: Info) {
    class Info(val detail: String?, val reason: String?)
}

class URLCreationResponse(val publicURL: String)

class ResultException internal constructor(val code: Int) : java.lang.Exception()

class ResultMap<T>(val cls: Class<T>?) : Function<BaseResult, T?> {
    @Throws(Exception::class)
    override fun apply(baseResult: BaseResult): T? {
        if (baseResult.status != 0) throw ResultException(baseResult.status)
        val detail = baseResult.info.detail ?: throw ResultException(baseResult.status)
        if (cls == String::class.java) return cls.cast(detail) as T
        return Gson().fromJson(detail, cls)
    }
}

object ShareAPI {
    val shared: Retrofit = Retrofit.Builder()
        .baseUrl("https://astroweather.cn/celestia/create/")
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

interface ShareAPIService {
    @FormUrlEncoded
    @POST("create")
    fun create(@Field("title") title: String, @Field("url") url: String, @Field("version") version: String): Observable<BaseResult>
}