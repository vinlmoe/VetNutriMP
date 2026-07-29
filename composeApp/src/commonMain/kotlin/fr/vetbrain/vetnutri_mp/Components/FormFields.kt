package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import fr.vetbrain.vetnutri_mp.Data.Labelable
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.General
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.isIosPlatform
import org.jetbrains.compose.ui.tooling.preview.Preview

/** Composant d'affichage d'un champ de texte non modifiable pour les valeurs nutritionnelles */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TextFieldNut(value: Labelable?, label: String) {
        TextField(
            value = value?.translateEnum() ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(label.translate()) },
                modifier = Modifier
        )
}

/** Composant de liste déroulante personnalisée pour sélectionner parmi des éléments Labelable */
@Preview
@Composable
fun ComboBox(
        items: List<Labelable>,
        init: Labelable?,
        modifier: Modifier = Modifier,
        label: String = "",
        onItemSelected: (String) -> Unit,
        itemLabelProvider: (Labelable) -> String = { it.translateEnum() }
) {
        var expanded by remember { mutableStateOf(false) }
        var selectedObject by remember { mutableStateOf(init) }
        var selectedText by remember {
                mutableStateOf(
                        TextFieldValue(
                                init?.let { itemLabelProvider(it) } ?: General.VALIDATE.translate()
                        )
                )
        }

        Column(modifier = modifier) {
                Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier =
                                Modifier.clip(RoundedCornerShape(AppSizes.cornerRadius))
                                        .border(
                                                BorderStroke(AppSizes.borderWidth, Color.LightGray),
                                                RoundedCornerShape(AppSizes.cornerRadius)
                                        )
                                        .clickable { expanded = !expanded }
                ) {
                        OutlinedTextField(
                                value = selectedText,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(label.translate()) },
                                placeholder = { Text(label.translate()) },
                                modifier = Modifier.clickable { expanded = !expanded }
                        )
                        Icon(
                                Icons.Filled.ArrowDropDown,
                                General.VALIDATE.translate(),
                                Modifier.align(Alignment.CenterEnd).clickable {
                                        expanded = !expanded
                                }
                        )
                }

                DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                                if (!isIosPlatform) {
                                        expanded = false
                                }
                        }
                ) {
                        items.forEach { item ->
                                DropdownMenuItem(
                                        content = { Text(itemLabelProvider(item)) },
                                        onClick = {
                                                selectedText =
                                                        TextFieldValue(itemLabelProvider(item))
                                                expanded = false
                                                onItemSelected(item.label ?: "null")
                                                selectedObject = item
                                        }
                                )
                        }
                }
        }
}

/** Composant générique de liste déroulante pour tout type de données */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T> GenericDropdown(
        selectedItem: T?,
        onItemSelected: (T?) -> Unit,
        items: List<T?>,
        getDisplayText: (T?) -> String,
        placeholder: String,
        modifier: Modifier = Modifier
) {
        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = modifier
        ) {
                OutlinedTextField(
                        value = selectedItem?.let { getDisplayText(it) } ?: placeholder,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                                Icon(
                                        if (expanded) Icons.Default.KeyboardArrowUp
                                        else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        modifier = Modifier.padding(AppSizes.paddingXSmall)
                                )
                        },
                        colors =
                                TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = VetNutriColors.Primary,
                                        unfocusedBorderColor = Color.Gray
                                ),
                        modifier = Modifier.fillMaxWidth()
                )

                DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                                if (!isIosPlatform) {
                                        expanded = false
                                }
                        },
                        modifier = Modifier.exposedDropdownSize()
                ) {
                        items.forEach { item ->
                                DropdownMenuItem(
                                        onClick = {
                                                onItemSelected(item)
                                                expanded = false
                                        }
                                ) {
                                        Text(
                                                text = getDisplayText(item),
                                                style =
                                                        MaterialTheme.typography.body1.copy(
                                                                fontSize = AppSizes.fontSizeBody1
                                                        )
                                        )
                                }
                        }
                }
        }
}
