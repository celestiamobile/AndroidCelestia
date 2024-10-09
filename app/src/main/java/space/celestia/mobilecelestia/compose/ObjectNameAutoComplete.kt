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
import space.celestia.celestia.Completion
import space.celestia.celestia.Selection
import space.celestia.mobilecelestia.R
import space.celestia.mobilecelestia.common.CelestiaExecutor
import java.lang.ref.WeakReference

@SuppressLint("InflateParams")
@Composable
fun ObjectNameAutoComplete(executor: CelestiaExecutor, core: AppCore, name: String, modifier: Modifier = Modifier, selection: Selection, inputUpdated: (String) -> Unit, selectionUpdated: (Selection) -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var savedCompletions by rememberSaveable {
        mutableStateOf(listOf<Completion>())
    }
    var savedName by rememberSaveable {
        mutableStateOf(name)
    }
    var savedObject by rememberSaveable {
        mutableStateOf(selection)
    }

    class StringArrayAdapter(context: Context, var content: List<Completion> = listOf()): ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line) {
        fun updateContent(content: List<Completion>) {
            this.content = content
        }

        override fun getCount(): Int {
            return content.size
        }

        override fun getItem(position: Int): String {
            return content[position].name
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
            coroutineScope.launch {
                val strongView = weakView.get() ?: return@launch
                val results = if (text.isEmpty()) Pair(listOf(), Selection()) else withContext(executor.asCoroutineDispatcher()) {
                    val completions = core.simulation.completionForText(text, 10)
                    val fullMatch = core.simulation.findObject(text)
                    return@withContext Pair(completions, fullMatch)
                }
                if (text != savedName) return@launch
                val completions = results.first
                val fullMatch = results.second
                selectionUpdated(fullMatch)
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
                val selected = savedCompletions[position].selection
                savedObject = selected
                selectionUpdated(selected)
            }
        }
        return@AndroidView view
    }, modifier = modifier)
}