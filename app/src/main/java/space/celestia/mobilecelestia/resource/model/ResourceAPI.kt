// ResourceAPI.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.resource.model

import com.google.gson.*
import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import space.celestia.celestiafoundation.resource.model.AddonUpdate
import space.celestia.celestiafoundation.resource.model.GuideItem
import space.celestia.celestiafoundation.resource.model.ResourceItem
import java.lang.reflect.Type
import java.util.*

object ResourceAPI {
    class DateAdapter : JsonDeserializer<Date> {
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): Date {
            try {
                val seconds = json?.asDouble ?: return Date()
                return Date((seconds * 1000.0).toLong())
            } catch(e: Throwable) {
                throw JsonParseException(e)
            }
        }
    }

    val gson: Gson by lazy { GsonBuilder().registerTypeAdapter(Date::class.java, DateAdapter()).create() }
}

data class UpdateRequest(
    val lang: String,
    val items: List<String>,
    @SerializedName("purchaseTokenAndroid") val purchaseToken: String
)

interface ResourceAPIService {
    @GET("latest")
    suspend fun latest(
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
}
