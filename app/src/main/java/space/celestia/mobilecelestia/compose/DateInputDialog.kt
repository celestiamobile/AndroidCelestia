/*
 * DateInputDialog.kt
 *
 * Copyright (C) 2025-present, Celestia Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package space.celestia.mobilecelestia.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import space.celestia.mobilecelestia.utils.CelestiaString
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DateInputDialog(onDismissRequest: () -> Unit, confirmHandler: () -> Unit, title: String, text: String?, formatter: SimpleDateFormat, dateChange: (Date?, String) -> Unit) {
    TextInputDialog(
        onDismissRequest = onDismissRequest,
        confirmHandler = confirmHandler,
        title = title,
        text = text,
        placeholder = formatter.format(Date()),
        textChange = {
            try {
                val date = formatter.parse(it)
                dateChange(date, it)
            } catch (ignored: Throwable) {
                dateChange(null, it)
            }
        }
    )
}

@Composable
fun DateInputDialog(onDismissRequest: () -> Unit, errorHandler: (String) -> Unit, successHandler: (Date) -> Unit) {
    var date: Date? by remember {
        mutableStateOf(null)
    }
    var dateString: String? by remember {
        mutableStateOf(null)
    }
    val dialogDateFormat = remember { android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "yyyyMMddHHmmss") }
    val dialogDateFormatter = remember { SimpleDateFormat(dialogDateFormat, Locale.US) }
    DateInputDialog(
        onDismissRequest = {
            dateString = null
            date = null
            onDismissRequest()
        },
        confirmHandler = {
            val dateInput = date
            if (dateInput != null) {
                successHandler(dateInput)
            } else {
                errorHandler(CelestiaString("Unrecognized time string.", "String not in correct format"))
            }
            date = null
            dateString = null
        },
        title = CelestiaString("Please enter the time in \"%s\" format.", "").format(
            dialogDateFormat
        ),
        text = dateString,
        formatter = dialogDateFormatter,
        dateChange = { newDate, newDateString ->
            dateString = newDateString
            date = newDate
        }
    )
}
