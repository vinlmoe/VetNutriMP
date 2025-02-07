package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Data.Labelable
import fr.vetbrain.vetnutri_mp.Localization.translate
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun TextFieldNut(value: Labelable?, label: String) {
    TextField(
            value = value?.label ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label.translate()) },
            modifier = Modifier
    )
}

@Preview
@Composable
fun ComboBox(
        items: List<Labelable>,
        init: Labelable?,
        modifier: Modifier = Modifier,
        label: String = "",
        onItemSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedObject by remember { mutableStateOf(init) }
    var selectedText by remember {
        mutableStateOf(TextFieldValue(init?.label ?: "common.value".translate()))
    }

    Column(modifier = modifier) {
        Box(
                contentAlignment = Alignment.CenterStart,
                modifier =
                        Modifier.clip(RoundedCornerShape(4.dp))
                                .border(
                                        BorderStroke(1.dp, Color.LightGray),
                                        RoundedCornerShape(4.dp)
                                )
                                .clickable { expanded = !expanded }
        ) {
            OutlinedTextField(
                    value = selectedText,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(label.translate()) },
                    modifier = Modifier.clickable { expanded = !expanded }
            )
            Icon(
                    Icons.Filled.ArrowDropDown,
                    "common.description".translate(),
                    Modifier.align(Alignment.CenterEnd).clickable { expanded = !expanded }
            )
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
                DropdownMenuItem(
                        content = { Text(item.label?.translate() ?: "common.name".translate()) },
                        onClick = {
                            selectedText = TextFieldValue(item.label?.translate() ?: "common.name".translate())
                            expanded = false
                            onItemSelected(item.label?.translate() ?: "common.name".translate())
                            selectedObject = item
                        }
                )
            }
        }
    }
}
