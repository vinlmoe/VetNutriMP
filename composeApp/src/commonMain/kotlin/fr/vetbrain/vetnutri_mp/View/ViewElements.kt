package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import fr.vetbrain.vetnutri_mp.Data.Labelable
import fr.vetbrain.vetnutri_mp.Enumer.AAEnum
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMin
import fr.vetbrain.vetnutri_mp.Enumer.NutrientOther
import fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.General
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterialApi::class)
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
        mutableStateOf(TextFieldValue(init?.label?.translate() ?: General.VALIDATE.translate()))
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
                    label = { Text(General.CALCULATE.translate()) },
                    placeholder = { Text(General.CALCULATE.translate()) },
                    modifier = Modifier.clickable { expanded = !expanded }
            )
            Icon(
                    Icons.Filled.ArrowDropDown,
                    General.VALIDATE.translate(),
                    Modifier.align(Alignment.CenterEnd).clickable { expanded = !expanded }
            )
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
                DropdownMenuItem(
                        content = { Text(item.label?.translate() ?: General.VALIDATE.translate()) },
                        onClick = {
                            selectedText =
                                    TextFieldValue(
                                            item.label?.translate() ?: General.VALIDATE.translate()
                                    )
                            expanded = false
                            onItemSelected(item.label ?: "null")
                            selectedObject = item
                        }
                )
            }
        }
    }
}

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
                onDismissRequest = { expanded = false },
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

@Composable
fun Badge(
        text: String,
        subText: String? = null,
        id: Any? = null,
        backgroundColor: Color,
        modifier: Modifier = Modifier
) {
    Surface(
            color = backgroundColor.copy(alpha = 0.2f),
            shape = MaterialTheme.shapes.small,
            modifier = modifier.padding(vertical = AppSizes.paddingXXSmall)
    ) {
        Column(
                modifier =
                        Modifier.padding(
                                horizontal = AppSizes.paddingSmall,
                                vertical = AppSizes.paddingXSmall
                        ),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                    text = text,
                    style = MaterialTheme.typography.body1.copy(fontSize = AppSizes.fontSizeBody1)
            )
            if (subText != null || id != null) {
                Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingXSmall),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    if (id != null) {
                        Text(
                                text = "ID: $id",
                                style =
                                        MaterialTheme.typography.caption.copy(
                                                fontSize = AppSizes.fontSizeCaption
                                        ),
                                color = Color.Gray
                        )
                    }
                    if (subText != null) {
                        Text(
                                text = "($subText)",
                                style =
                                        MaterialTheme.typography.caption.copy(
                                                fontSize = AppSizes.fontSizeCaption
                                        ),
                                color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

/** Composant pour afficher une section de valeurs nutritionnelles avec un fond coloré. */
@Composable
fun NutrientSection(
        titre: String,
        nutriments: List<Nutrient>,
        valeursNutriments: SnapshotStateMap<Nutrient, String>,
        erreursNutriments: SnapshotStateMap<Nutrient, Boolean>,
        couleurArrierePlan: Color,
        modifier: Modifier = Modifier
) {
    Card(
            modifier = modifier.fillMaxWidth(),
            elevation = 4.dp,
            backgroundColor = couleurArrierePlan
    ) {
        Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                    text = titre,
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.onSurface
            )

            Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f))

            nutriments.forEach { nutriment ->
                val valeur = valeursNutriments[nutriment] ?: ""
                val aErreur = erreursNutriments[nutriment] == true

                Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    // Obtenir le nom à afficher selon le type de nutriment
                    val nomAffiche =
                            when (nutriment) {
                                is NutrientLipid -> (nutriment as NutrientLipid).nameToString()
                                is NutrientMacro -> (nutriment as NutrientMacro).nameToString()
                                is NutrientMain -> (nutriment as NutrientMain).nameToString()
                                is NutrientMin -> (nutriment as NutrientMin).nameToString()
                                is NutrientOther -> (nutriment as NutrientOther).nameToString()
                                is NutrientVitam -> (nutriment as NutrientVitam).displayName
                                is AAEnum -> (nutriment as AAEnum).nom
                                else -> nutriment.toString()
                            }

                    Text(text = nomAffiche, modifier = Modifier.weight(1f))

                    OutlinedTextField(
                            value = valeur,
                            onValueChange = { nouvelleValeur ->
                                valeursNutriments[nutriment] = nouvelleValeur
                                // Validation en temps réel
                                if (nouvelleValeur.isNotBlank()) {
                                    val valeurFloat =
                                            nouvelleValeur.replace(",", ".").toFloatOrNull()
                                    erreursNutriments[nutriment] =
                                            valeurFloat == null || valeurFloat < 0
                                } else {
                                    erreursNutriments.remove(nutriment)
                                }
                            },
                            modifier = Modifier.width(120.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors =
                                    TextFieldDefaults.outlinedTextFieldColors(
                                            focusedBorderColor =
                                                    if (aErreur) Color.Red
                                                    else VetNutriColors.Primary,
                                            unfocusedBorderColor =
                                                    if (aErreur) Color.Red else Color.Gray,
                                            backgroundColor = Color.White.copy(alpha = 0.8f),
                                            errorBorderColor = Color.Red
                                    ),
                            isError = aErreur,
                            trailingIcon = {
                                Text(
                                        text = nutriment.unite,
                                        style = MaterialTheme.typography.caption
                                )
                            }
                    )
                }
            }
        }
    }
}

/** Composant de carte générique pour les sélections multiples. */
@Composable
fun <T> MultiSelectionCard(
        titre: String,
        elementsDisponibles: List<T>,
        elementsSelectionnes: MutableList<T>,
        onSelectionChange: (List<T>) -> Unit,
        getLabel: (T) -> String,
        getIdentifiant: (T) -> String,
        couleurArrierePlan: Color,
        modifier: Modifier = Modifier
) {
    var afficherDialogue by remember { mutableStateOf(false) }
    val elementsTriés by
            remember(elementsDisponibles) {
                mutableStateOf(elementsDisponibles.sortedBy { getLabel(it) })
            }

    Card(modifier = modifier.fillMaxWidth(), elevation = 4.dp) {
        Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = titre, style = MaterialTheme.typography.h6)

            OutlinedButton(
                    onClick = { afficherDialogue = true },
                    modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                            if (elementsSelectionnes.isEmpty()) "Sélectionner..."
                            else "${elementsSelectionnes.size} élément(s) sélectionné(s)"
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }

            if (elementsSelectionnes.isNotEmpty()) {
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    elementsSelectionnes.take(5).forEach { element ->
                        Surface(
                                color = couleurArrierePlan.copy(alpha = 0.2f),
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Column(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = getLabel(element))
                                Text(
                                        text = "(${getIdentifiant(element)})",
                                        style = MaterialTheme.typography.caption,
                                        color = Color.Gray
                                )
                            }
                        }
                    }
                    if (elementsSelectionnes.size > 5) {
                        Text("+ ${elementsSelectionnes.size - 5} autres")
                    }
                }
            }
        }
    }

    if (afficherDialogue) {
        Dialog(onDismissRequest = { afficherDialogue = false }) {
            Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colors.surface,
                    elevation = 8.dp,
                    modifier = Modifier.fillMaxWidth(0.9f).heightIn(max = 450.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                            text = titre,
                            style = MaterialTheme.typography.h6,
                            modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        Column(
                                modifier =
                                        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                        ) {
                            elementsTriés.forEach { element ->
                                key(getIdentifiant(element)) {
                                    Row(
                                            modifier =
                                                    Modifier.fillMaxWidth()
                                                            .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                                checked = elementsSelectionnes.contains(element),
                                                onCheckedChange = { checked ->
                                                    val nouvelleListe =
                                                            elementsSelectionnes.toMutableList()
                                                    if (checked) {
                                                        if (!nouvelleListe.contains(element)) {
                                                            nouvelleListe.add(element)
                                                        }
                                                    } else {
                                                        nouvelleListe.remove(element)
                                                    }
                                                    onSelectionChange(nouvelleListe)
                                                }
                                        )
                                        Column(modifier = Modifier.padding(start = 8.dp)) {
                                            Text(text = getLabel(element))
                                            Text(
                                                    text = "(${getIdentifiant(element)})",
                                                    style = MaterialTheme.typography.caption,
                                                    color = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Button(
                            onClick = { afficherDialogue = false },
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                    ) { Text("Fermer") }
                }
            }
        }
    }
}
