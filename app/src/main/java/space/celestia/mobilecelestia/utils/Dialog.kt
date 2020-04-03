package space.celestia.mobilecelestia.utils

import android.app.Activity
import android.app.AlertDialog
import android.text.InputType
import android.view.LayoutInflater
import android.widget.EditText
import space.celestia.mobilecelestia.R

fun Activity.showTextInput(title: String, placeholder: String?, handler: (String) -> Unit) {
    val builder = AlertDialog.Builder(this)
    builder.setTitle(title)
    val customView = LayoutInflater.from(this).inflate(R.layout.dialog_text_input, findViewById(android.R.id.content), false)

    val editText = customView.findViewById<EditText>(R.id.input)
    editText.inputType = InputType.TYPE_CLASS_TEXT
    if (placeholder != null)
        editText.setText(placeholder)
    builder.setView(customView)

    builder.setPositiveButton("OK") { _, _ ->
        handler(editText.text.toString())
    }

    builder.setNegativeButton("Cancel") { dialog, _ ->
        dialog.cancel()
    }
    builder.show()
}

fun Activity.showAlert(title: String, handler: (() -> Unit)? = null) {
    val builder = AlertDialog.Builder(this)
    builder.setTitle(title)
    builder.setPositiveButton("OK") { _, _ ->
        if (handler != null)
            handler()
    }
    if (handler != null) {
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }
    }
    builder.show()
}

fun Activity.showError(error: Throwable) {
    var message = error.message
    if (message == null)
        message = "Unknown error"
    showAlert(message)
}