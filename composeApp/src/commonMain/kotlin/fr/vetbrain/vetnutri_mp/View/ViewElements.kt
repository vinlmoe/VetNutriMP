package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.vetbrain.vetnutri_mp.Data.Namable
import fr.vetbrain.vetnutri_mp.Data.Nutrient
import org.jetbrains.compose.ui.tooling.preview.Preview


@Composable
fun  TextFieldNut(
    value : Nutrient?,
    label:String
)
{


        TextField (
        value = value?.toStringer()?:"",
        onValueChange = { },
        readOnly = true,
        label={Text(label)},
        modifier = Modifier

    )


}

@Preview
@Composable
fun ComboBox(
    items: List<Nutrient>,
    init: Nutrient?,
    modifier: Modifier = Modifier,
    label: String = "",
    onItemSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedObject by remember { mutableStateOf(init) }
    var selectedText by remember { mutableStateOf(TextFieldValue(init?.label?:"")) }

    Column(modifier = modifier) {
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier

                .clip(RoundedCornerShape(4.dp))
                .border(BorderStroke(1.dp, Color.LightGray), RoundedCornerShape(4.dp))
                .clickable { expanded = !expanded },
        ) {
            TextField (
                value = selectedText,
                onValueChange = { },
                readOnly = true,
                label={Text(label)},
                modifier = Modifier

                    .clickable { expanded = !expanded }



            )
            Icon(
                Icons.Filled.ArrowDropDown, "contentDescription",
                Modifier.align(Alignment.CenterEnd)
                    .clickable { expanded = !expanded }
            )
        }


        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(

                    content = { Text(item.label?:"") },
                    onClick = {
                        selectedText = TextFieldValue(item.label?:"")
                        expanded = false
                        onItemSelected(item.label?:"")
                        selectedObject=item
                    }
                )
            }
        }

    }
}