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
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

enum class PushNotificationContentType(val rawValue: String) {
    WEEKLY_ADDON("weekly-addon"),
    LATEST_NEWS("latest-news"),
    FEATURED_ADDON("featured-addon");

    override fun toString(): String = rawValue
}

@Keep
@Serializable
data class RegisterRequest(
    val token: String,
    val tokenType: String,
    val lang: String,
    val timezone: String,
    val contentTypes: List<String>,
    val api: Int,
    val platform: String,
    val distribution: String? = null,
    val lastShownNewsID: String? = null
)

interface UserAPIService {
    @POST("register")
    suspend fun register(@Body body: RegisterRequest)
}
