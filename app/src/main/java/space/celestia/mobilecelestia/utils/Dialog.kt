/*
 * Dialog.kt
 *
 * Copyright (C) 2001-2020, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.utils

import android.app.Activity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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
    if (isFinishing || isDestroyed)
        return

    val builder = MaterialAlertDialogBuilder(this)
    builder.setTitle(title)
    if (message != null)
        builder.setMessage(message)
    builder.setPositiveButton(CelestiaString("OK", "")) { _, _ ->
        if (handler != null)
            handler()
    }
    if (handler != null || cancelHandler != null) {
        builder.setNegativeButton(CelestiaString("Cancel", "")) { dialog, _ ->
            dialog.cancel()
        }
    }
    if (cancelHandler != null) {
        builder.setOnCancelListener {
            cancelHandler()
        }
    }
    builder.show()
}

fun Activity.showError(error: Throwable) {
    var message = error.message
    if (message == null)
        message = CelestiaString("Unknown error", "")
    showAlert(message)
}