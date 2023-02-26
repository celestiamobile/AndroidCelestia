package space.celestia.mobilecelestia.control

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import space.celestia.mobilecelestia.R

@SuppressLint("InflateParams")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionSelect(options: List<String>, selectedIndex: Int, modifier: Modifier = Modifier, selectionChange: (Int) -> Unit) {
    AndroidView(factory = { context ->
        val view = LayoutInflater.from(context).inflate(R.layout.common_options_select, null, false)
        val autoCompleteTextView = view.findViewById<MaterialAutoCompleteTextView>(R.id.text_view)
        val adapter = object: ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line) {
            override fun getCount(): Int {
                return options.size
            }

            override fun getItem(position: Int): String {
                return options[position]
            }
        }
        autoCompleteTextView.setAdapter(adapter)
        autoCompleteTextView.setText(options[selectedIndex])
        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            selectionChange(position)
        }
        return@AndroidView view
    }, modifier = modifier)
}