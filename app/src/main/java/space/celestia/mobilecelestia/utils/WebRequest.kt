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

package space.celestia.mobilecelestia.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Function
import io.reactivex.rxjava3.schedulers.Schedulers
import java.lang.reflect.Type

class BaseResult(val status: Int, val info: Info) {
    class Info(val detail: String?, val reason: String?)
}

class ResultException internal constructor(val code: Int) : java.lang.Exception()

class ResultMap<T>: Function<BaseResult, T?> {
    val cls: Class<T>?
    val tt: Type?
    val gson: Gson

    constructor(cls: Class<T>, gson: Gson) {
        this.cls = cls
        this.tt = null
        this.gson = gson
    }

    constructor(tt: Type, gson: Gson) {
        this.cls = null
        this.tt = tt
        this.gson = gson
    }

    @Throws(Exception::class)
    override fun apply(baseResult: BaseResult): T? {
        if (baseResult.status != 0) throw ResultException(baseResult.status)
        val detail = baseResult.info.detail ?: throw ResultException(baseResult.status)
        @Suppress("UNCHECKED_CAST")
        if (cls == String::class.java) return cls.cast(detail) as T
        if (tt != null) return gson.fromJson(detail, tt)
        return gson.fromJson(detail, cls)
    }
}

fun <T> Observable<BaseResult>.commonHandler(tt: Type, gson: Gson = GsonBuilder().create(), success: (T) -> Unit, failure: (() -> Unit)? = null): Disposable {
    fun callFailure() {
        if (failure != null)
            failure()
    }

    return this.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .map(ResultMap<T>(tt, gson))
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

fun <T> Observable<BaseResult>.commonHandler(cls: Class<T>, gson: Gson = GsonBuilder().create(), success: (T) -> Unit, failure: (() -> Unit)? = null): Disposable {
    fun callFailure() {
        if (failure != null)
            failure()
    }

    return this.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .map(ResultMap(cls, gson))
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