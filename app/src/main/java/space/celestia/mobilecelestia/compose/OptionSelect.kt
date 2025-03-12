package space.celestia.mobilecelestia.compose

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionSelect(options: List<String>, selectedIndex: Int, modifier: Modifier = Modifier, selectionChange: (Int) -> Unit, wrapWidth: Boolean = false) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = modifier) {
        SimpleTextField(value = options[selectedIndex], readOnly = true, onValueChange = {}, trailingIcon = {
            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
        }, textStyle = MaterialTheme.typography.bodyLarge, modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).then(if (wrapWidth) Modifier.width(IntrinsicSize.Min) else Modifier.fillMaxWidth()))
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.exposedDropdownSize(matchTextFieldWidth = true)) {
            for (option in options.withIndex()) {
                DropdownMenuItem({
                    Text(text = option.value)
                }, onClick = {
                    selectionChange(option.index)
                    expanded = false
                }, contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SimpleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    trailingIcon: @Composable (() -> Unit)? = null,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
) {
    val interactionSource = remember { MutableInteractionSource() }

    val textColor =
        textStyle.color.takeOrElse {
            val focused = interactionSource.collectIsFocusedAsState().value
            if (focused) colors.focusedTextColor else colors.unfocusedTextColor
        }
    val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        interactionSource = interactionSource,
        enabled = true,
        readOnly = readOnly,
        textStyle = mergedTextStyle,
        singleLine = false,
        modifier = modifier
    ) {
        OutlinedTextFieldDefaults.DecorationBox(
            value = value,
            visualTransformation = VisualTransformation.None,
            innerTextField = it,
            singleLine = false,
            enabled = true,
            interactionSource = interactionSource,
            contentPadding = OutlinedTextFieldDefaults.contentPadding(),
            trailingIcon = trailingIcon,
            colors = colors
        )
    }
}