/*
 * WebRequest.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.celestiafoundation.utils

import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.lang.reflect.Type

@Keep
class BaseResult(val status: Int, val info: Info) {
    @Keep
    class Info(val detail: String?, val reason: String?)
}

class ResultException internal constructor(val code: Int, val reason: String?) : java.lang.Exception()
class MissingBodyException : java.lang.Exception()

fun <T> BaseResult.commonHandler(t: Type, gson: Gson = GsonBuilder().create()): T {
    if (status != 0) throw ResultException(status, info.reason)
    val detail = info.detail ?: throw MissingBodyException()
    return gson.fromJson(detail, t)
}

fun <T> BaseResult.commonHandler(cls: Class<T>, gson: Gson = GsonBuilder().create()): T {
    if (status != 0) throw ResultException(status, info.reason)
    val detail = info.detail ?: throw MissingBodyException()
    @Suppress("UNCHECKED_CAST")
    if (cls == String::class.java) return cls.cast(detail) as T
    return gson.fromJson(detail, cls)
}