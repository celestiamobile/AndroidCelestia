package space.celestia.celestiaui.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import space.celestia.celestia.AppCore
import space.celestia.celestia.Completion
import space.celestia.celestia.Selection
import java.util.concurrent.Executor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectNameAutoComplete(executor: Executor, core: AppCore, name: String, modifier: Modifier = Modifier, selection: Selection, inputUpdated: (String) -> Unit, selectionUpdated: (Selection) -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var text by rememberSaveable { mutableStateOf(name) }
    var completions by remember { mutableStateOf(listOf<Completion>()) }
    var expanded by remember { mutableStateOf(false) }

    fun fetchCompletions(currentText: String) {
        inputUpdated(currentText)
        coroutineScope.launch {
            val (newCompletions, fullMatch) = if (currentText.isEmpty()) {
                Pair(listOf(), Selection())
            } else withContext(executor.asCoroutineDispatcher()) {
                Pair(
                    core.simulation.completionForText(currentText, 10),
                    core.simulation.findObject(currentText)
                )
            }
            if (currentText != text) return@launch
            completions = newCompletions
            selectionUpdated(fullMatch)
            expanded = newCompletions.isNotEmpty()
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (!it) expanded = false },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { newText ->
                text = newText
                fetchCompletions(newText)
            },
            singleLine = true,
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                .fillMaxWidth()
                .onFocusChanged { if (it.isFocused) fetchCompletions(text) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            completions.forEach { completion ->
                DropdownMenuItem(
                    text = { Text(completion.name) },
                    onClick = {
                        text = completion.name
                        inputUpdated(completion.name)
                        selectionUpdated(completion.selection)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}
