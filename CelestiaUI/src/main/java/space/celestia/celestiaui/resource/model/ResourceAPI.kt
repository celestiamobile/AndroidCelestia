// ResourceAPI.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.celestiaui.resource.model

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import space.celestia.celestiafoundation.resource.model.AddonUpdate
import space.celestia.celestiafoundation.resource.model.GuideItem
import space.celestia.celestiafoundation.resource.model.ResourceItem

@Keep
@Serializable
data class UpdateRequest(
    val lang: String,
    val items: List<String>,
    @SerialName("purchaseTokenAndroid") val purchaseToken: String,
    val productType: String,
)

interface ResourceAPIService {
    @GET("latest")
    suspend fun latest(
        @Query("platform") platform: String,
        @Query("distribution") distribution: String?,
        @Query("type") type: String,
        @Query("lang") lang: String
    ): GuideItem

    @GET("item")
    suspend fun item(
        @Query("lang") lang: String,
        @Query("item") item: String
    ): ResourceItem

    @POST("updates")
    suspend fun updates(
        @Body body: UpdateRequest
    ): Map<String, AddonUpdate>

    @GET("features")
    suspend fun features(
        @Query("platform") platform: String,
        @Query("distribution") distribution: String?,
        @Query("lang") lang: String,
        @Query("version") version: String
    ): Map<String, Double>
}
