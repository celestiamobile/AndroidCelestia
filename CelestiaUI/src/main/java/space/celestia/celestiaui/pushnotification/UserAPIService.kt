// UserAPIService.kt
//
// Copyright (C) 2026-present, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.celestiaui.pushnotification

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST

enum class PushNotificationContentType(val rawValue: String) {
    WEEKLY_ADDON("weekly-addon"),
    LATEST_NEWS("latest-news"),
    FEATURED_ADDON("featured-addon");

    override fun toString(): String = rawValue
}

@Keep
data class RegisterRequest(
    @SerializedName("token") val token: String,
    @SerializedName("tokenType") val tokenType: String,
    @SerializedName("lang") val lang: String,
    @SerializedName("timezone") val timezone: String,
    @SerializedName("contentTypes") val contentTypes: List<String>,
    @SerializedName("api") val api: Int,
    @SerializedName("platform") val platform: String,
    @SerializedName("distribution") val distribution: String?,
    @SerializedName("lastShownNewsID") val lastShownNewsID: String?
)

interface UserAPIService {
    @POST("register")
    suspend fun register(@Body body: RegisterRequest)
}
