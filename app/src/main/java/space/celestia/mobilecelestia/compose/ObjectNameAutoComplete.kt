package space.celestia.mobilecelestia.compose

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import space.celestia.celestia.AppCore
import space.celestia.celestia.Completion
import space.celestia.celestia.Selection
import space.celestia.mobilecelestia.common.CelestiaExecutor

@OptIn(ExperimentalMaterial3Api::class)
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

    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = modifier) {
        SimpleTextField(value = savedName, readOnly = false, onValueChange = { text ->
            savedName = text
            inputUpdated(savedName)
            coroutineScope.launch {
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
                }
                if (!expanded)
                    expanded = true
            }
        }, trailingIcon = {
            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
        }, textStyle = MaterialTheme.typography.bodyLarge, modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth())
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.exposedDropdownSize(matchTextFieldWidth = true)) {
            for (completion in savedCompletions) {
                DropdownMenuItem({
                    Text(text = completion.name)
                }, onClick = {
                    selectionUpdated(completion.selection)
                    savedName = completion.name
                    inputUpdated(savedName)
                    savedObject = completion.selection
                    selectionUpdated(savedObject)
                    expanded = false
                }, contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding)
            }
        }
    }
}