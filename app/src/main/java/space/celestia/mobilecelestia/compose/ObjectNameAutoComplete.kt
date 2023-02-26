package space.celestia.mobilecelestia.control

import android.annotation.SuppressLint
import android.view.LayoutInflater
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import space.celestia.celestia.AppCore
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.travel.GoToSuggestionAdapter

@SuppressLint("InflateParams")
@Composable
fun ObjectNameAutoComplete(appCore: AppCore, name: String, modifier: Modifier = Modifier, inputUpdated: (String) -> Unit) {
    AndroidView(factory = { context ->
        val view = LayoutInflater.from(context).inflate(R.layout.common_auto_complete_text_view, null, false)
        val autoCompleteTextView = view.findViewById<MaterialAutoCompleteTextView>(R.id.text_view)
        autoCompleteTextView.threshold = 1
        autoCompleteTextView.setAdapter(GoToSuggestionAdapter(context, android.R.layout.simple_dropdown_item_1line, appCore))
        autoCompleteTextView.setText(name)
        autoCompleteTextView.addTextChangedListener {
            inputUpdated(it?.toString() ?: "")
        }
        return@AndroidView view
    }, modifier = modifier)
}