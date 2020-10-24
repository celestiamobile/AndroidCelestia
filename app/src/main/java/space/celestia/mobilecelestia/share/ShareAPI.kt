/*
 * ShareAPI.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.share

import io.reactivex.rxjava3.core.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import space.celestia.mobilecelestia.utils.BaseResult

class URLCreationResponse(val publicURL: String)
class URLResolultionResponse(val resolvedURL: String)

object ShareAPI {
    val shared: Retrofit = Retrofit.Builder()
        .baseUrl("https://celestia.mobi/api/")
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
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

    @GET("resolve")
    fun resolve(
        @Query("path") path: String,
        @Query("id") id: String
    ): Observable<BaseResult>
}