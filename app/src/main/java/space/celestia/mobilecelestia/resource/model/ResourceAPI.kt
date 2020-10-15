/*
 * ResourceAPI.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.resource.model

import io.reactivex.rxjava3.core.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import space.celestia.mobilecelestia.utils.BaseResult

object ResourceAPI {
    val shared: Retrofit = Retrofit.Builder()
        .baseUrl("https://celestia.mobi/api/resource/")
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

interface ResourceAPIService {
    @GET("categories")
    fun categories(
        @Query("lang") lang: String
    ): Observable<BaseResult>

    @GET("items")
    fun items(
        @Query("lang") lang: String,
        @Query("category") category: String
    ): Observable<BaseResult>

    @GET("item")
    fun item(
        @Query("lang") lang: String,
        @Query("item") item: String
    ): Observable<BaseResult>
}
