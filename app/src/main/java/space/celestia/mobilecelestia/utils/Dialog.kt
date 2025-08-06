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
import android.os.Build
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import space.celestia.mobilecelestia.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun Activity.showTextInput(title: String, placeholder: String? = null, handler: (String) -> Unit) {
    if (isFinishing || isDestroyed)
        return

    val builder = MaterialAlertDialogBuilder(this)
    builder.setTitle(title)
    val customView = LayoutInflater.from(this).inflate(R.layout.dialog_text_input, findViewById(android.R.id.content), false)

    val editText = customView.findViewById<EditText>(R.id.input)
    editText.inputType = InputType.TYPE_CLASS_TEXT
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        editText.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
    }
    if (placeholder != null)
        editText.setText(placeholder)
    builder.setView(customView)

    builder.setPositiveButton(CelestiaString("OK", "")) { _, _ ->
        handler(editText.text.toString())
    }

    builder.setNegativeButton(CelestiaString("Cancel", "")) { dialog, _ ->
        dialog.cancel()
    }
    builder.show()
}

fun Activity.showDateInput(title: String, format: String, handler: (Date?) -> Unit) {
    if (isFinishing || isDestroyed)
        return

    val formatter = SimpleDateFormat(format, Locale.US)
    val builder = MaterialAlertDialogBuilder(this)
    builder.setTitle(title)
    val customView = LayoutInflater.from(this).inflate(R.layout.dialog_text_input, findViewById(android.R.id.content), false)

    val editText = customView.findViewById<EditText>(R.id.input)
    editText.hint = formatter.format(Date())
    editText.inputType = InputType.TYPE_CLASS_TEXT
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        editText.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
    }
    builder.setView(customView)

    builder.setPositiveButton(CelestiaString("OK", "")) { _, _ ->
        try {
            val date = formatter.parse(editText.text.toString())
            handler(date)
        } catch (_: Exception) {
            handler(null)
        }
    }

    builder.setNegativeButton(CelestiaString("Cancel", "")) { dialog, _ ->
        dialog.cancel()
    }
    builder.show()
}

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

fun Activity.showLoading(title: String, cancelHandler: (() -> Unit)? = null): AlertDialog? {
    if (isFinishing || isDestroyed)
        return null

    val builder = MaterialAlertDialogBuilder(this)
    builder.setTitle(title)
    if (cancelHandler != null) {
        builder.setCancelable(false)
        builder.setNegativeButton(CelestiaString("Cancel", "")) { dialog, _ ->
            dialog.cancel()
        }
        builder.setOnCancelListener {
            cancelHandler()
        }
    } else {
        builder.setCancelable(false)
    }
    return builder.show()
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