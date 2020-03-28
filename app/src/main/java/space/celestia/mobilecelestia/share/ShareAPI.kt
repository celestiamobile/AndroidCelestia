package space.celestia.mobilecelestia.share

import android.annotation.SuppressLint
import com.google.gson.Gson
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
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

class ResultMap<T>(private val cls: Class<T>?) : Function<BaseResult, T?> {
    @Throws(Exception::class)
    override fun apply(baseResult: BaseResult): T? {
        if (baseResult.status != 0) throw ResultException(baseResult.status)
        val detail = baseResult.info.detail ?: throw ResultException(baseResult.status)
        @Suppress("UNCHECKED_CAST")
        if (cls == String::class.java) return cls.cast(detail) as T
        return Gson().fromJson(detail, cls)
    }
}

object ShareAPI {
    val shared: Retrofit = Retrofit.Builder()
        .baseUrl("https://astroweather.cn/celestia/")
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

interface ShareAPIService {
    @FormUrlEncoded
    @POST("create")
    fun create(
        @Field("title") title: String,
        @Field("url") url: String,
        @Field("version") version: String
    ): Observable<BaseResult>
}

@SuppressLint("CheckResult")
fun <T> Observable<BaseResult>.commonHandler(cls: Class<T>?, success: (T) -> Unit, failure: (() -> Unit)? = null) {
    fun callFailure() {
        if (failure != null)
            failure()
    }

    this.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .map(ResultMap(cls))
        .subscribe({
            if (it == null) {
                callFailure()
                return@subscribe
            }
            success(it)
        }, {
            callFailure()
        })
}