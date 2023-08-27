package space.celestia.mobilecelestia.compose

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import space.celestia.celestia.AppCore
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.CelestiaExecutor
import java.lang.ref.WeakReference

@SuppressLint("InflateParams")
@Composable
fun ObjectNameAutoComplete(executor: CelestiaExecutor, core: AppCore, name: String, modifier: Modifier = Modifier, path: String, inputUpdated: (String) -> Unit, objectPathUpdated: (String) -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var savedCompletions by rememberSaveable {
        mutableStateOf(listOf<String>())
    }
    var savedName by rememberSaveable {
        mutableStateOf(name)
    }
    var savedPath by rememberSaveable {
        mutableStateOf(path)
    }

    class StringArrayAdapter(context: Context, var content: List<String> = listOf()): ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line) {
        fun updateContent(content: List<String>) {
            this.content = content
        }

        override fun getCount(): Int {
            return content.size
        }

        override fun getItem(position: Int): String {
            return content[position]
        }
    }

    AndroidView(factory = { context ->
        val view = LayoutInflater.from(context).inflate(R.layout.common_auto_complete_text_view, null, false)
        val autoCompleteTextView = view.findViewById<MaterialAutoCompleteTextView>(R.id.text_view)
        autoCompleteTextView.setText(savedName)
        val adapter = StringArrayAdapter(context, content = savedCompletions)
        autoCompleteTextView.setAdapter(adapter)
        val weakView = WeakReference(autoCompleteTextView)
        val autoCompleteAction: (String) -> Unit = { text ->
            savedName = text
            inputUpdated(savedName)
            objectPathUpdated(savedName)
            coroutineScope.launch {
                val strongView = weakView.get() ?: return@launch
                val completions = if (text.isEmpty()) listOf<String>() else withContext(executor.asCoroutineDispatcher()) {
                    core.simulation.completionForText(text, 10)
                }
                if (text != savedName) return@launch
                if (savedCompletions != completions) {
                    savedCompletions = completions
                    adapter.updateContent(completions)
                    if (savedCompletions.isEmpty())
                        adapter.notifyDataSetInvalidated()
                    else
                        adapter.notifyDataSetChanged()
                }
                if (savedCompletions.isNotEmpty())
                    strongView.showDropDown()
            }
        }
        autoCompleteTextView.setOnFocusChangeListener { _, hasFocus ->
            val strongView = weakView.get() ?: return@setOnFocusChangeListener
            if (hasFocus) {
                autoCompleteAction(strongView.text.toString())
            }
        }
        autoCompleteTextView.addTextChangedListener {
            val strongView = weakView.get() ?: return@addTextChangedListener
            if (strongView.isPerformingCompletion) return@addTextChangedListener
            val text = it?.toString() ?: ""
            autoCompleteAction(text)
        }
        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            if (position >= 0 && position < adapter.count) {
                val result = adapter.getItem(position)
                val lastSeparator = savedName.lastIndexOf('/')
                val objectPath: String = if (lastSeparator != -1) {
                    savedName.substring(startIndex = 0, endIndex = lastSeparator + 1) + result
                } else {
                    result
                }
                savedPath = objectPath
                objectPathUpdated(objectPath)
            }
        }
        return@AndroidView view
    }, modifier = modifier)
}