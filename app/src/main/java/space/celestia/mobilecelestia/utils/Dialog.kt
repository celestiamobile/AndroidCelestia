// Dialog.kt
//
// Copyright (C) 2025, Celestia Development Team
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

package space.celestia.mobilecelestia.utils

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun Activity.showOptions(title: String, options: Array<String>, handler: (Int) -> Unit) {
    if (isFinishing || isDestroyed)
        return

    val builder = MaterialAlertDialogBuilder(this)
    builder.setTitle(title)
    builder.setItems(options) { _, index ->
        handler(index)
    }
    builder.show()
}

fun Activity.showAlert(title: String, message: String? = null, handler: (() -> Unit)? = null, cancelHandler: (() -> Unit)? = null) {
    if (isFinishing || isDestroyed) {
        if (cancelHandler != null)
            cancelHandler()
        return
    }

    var handlerCalled = false
    (this as? ComponentActivity)?.lifecycle?.addObserver(object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            owner.lifecycle.removeObserver(this)
            if (!handlerCalled) {
                handlerCalled = true
                if (cancelHandler != null)
                    cancelHandler()
            }
        }
    })

    val builder = MaterialAlertDialogBuilder(this)
    builder.setTitle(title)
    if (message != null)
        builder.setMessage(message)
    builder.setPositiveButton(CelestiaString("OK", "")) { _, _ ->
        handlerCalled = true
        if (handler != null)
            handler()
    }
    if (handler != null || cancelHandler != null) {
        builder.setNegativeButton(CelestiaString("Cancel", "")) { dialog, _ ->
            dialog.cancel()
        }
    }
    builder.setOnCancelListener {
        handlerCalled = true
        if (cancelHandler != null)
            cancelHandler()
    }
    builder.show()
}

enum class AlertResult {
    OK,
    Cancel
}

suspend fun Activity.showAlertAsync(title: String, message: String? = null, showCancel: Boolean = false): AlertResult = suspendCoroutine { cont ->
    showAlert(
        title = title,
        message = message,
        handler = {
            cont.resume(AlertResult.OK)
        },
        cancelHandler = if (showCancel) {
            {
                cont.resume(AlertResult.Cancel)
            }
        } else null
    )
}

fun Activity.showError(error: Throwable) {
    var message = error.message
    if (message == null)
        message = CelestiaString("Unknown error", "")
    showAlert(message)
}