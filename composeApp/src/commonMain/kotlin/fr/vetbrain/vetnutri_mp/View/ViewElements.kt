package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
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

/**
 * Carte d'analyse nutritionnelle pour afficher les apports nutritionnels d'une ration
 *
 * @param nutriments Liste des nutriments à afficher
 * @param valeursTotales Map des valeurs totales pour chaque nutriment
 * @param diviseur Valeur utilisée comme diviseur (poids de l'animal ou de la ration)
 * @param typeDiviseur Type de diviseur utilisé ("poids animal" ou "poids ration")
 * @param couleurFond Couleur de fond de la carte
 * @param onModeDivisionChange Callback appelé lors du changement de mode de division
 * @param modifier Modificateur optionnel pour personnaliser l'apparence
 */
@Composable
fun AnalyseNutritionnelleCard(
        nutriments: List<Nutrient>,
        valeursTotales: Map<Nutrient, Float>,
        diviseur: Float,
        typeDiviseur: String,
        couleurFond: Color = MaterialTheme.colors.surface,
        onModeDivisionChange: () -> Unit,
        modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth(), elevation = 4.dp, backgroundColor = couleurFond) {
        Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // En-tête avec titre et switch pour changer le mode de division
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        text = "Analyse nutritionnelle",
                        style = MaterialTheme.typography.h6,
                        color = VetNutriColors.Primary
                )

                TextButton(onClick = onModeDivisionChange) {
                    Text(
                            text = "Par $typeDiviseur",
                            style = MaterialTheme.typography.caption,
                            color = VetNutriColors.Primary
                    )
                }
            }

            Divider()

            // Afficher le message si aucun nutriment ou si diviseur nul
            if (nutriments.isEmpty() || diviseur <= 0) {
                Box(
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        contentAlignment = Alignment.Center
                ) {
                    Text(
                            text =
                                    if (diviseur <= 0)
                                            "Impossible de calculer les apports (diviseur nul)"
                                    else "Aucun nutriment à afficher",
                            style = MaterialTheme.typography.body2,
                            color = Color.Gray
                    )
                }
            } else {
                // Tableau des nutriments avec leurs valeurs
                Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // En-tête du tableau
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                                text = "Nutriment",
                                style =
                                        MaterialTheme.typography.subtitle2.copy(
                                                fontWeight = FontWeight.Bold
                                        ),
                                modifier = Modifier.weight(2f)
                        )
                        Text(
                                text = "Valeur",
                                style =
                                        MaterialTheme.typography.subtitle2.copy(
                                                fontWeight = FontWeight.Bold
                                        ),
                                modifier = Modifier.weight(1f)
                        )
                        Text(
                                text = "Unité",
                                style =
                                        MaterialTheme.typography.subtitle2.copy(
                                                fontWeight = FontWeight.Bold
                                        ),
                                modifier = Modifier.weight(1f)
                        )
                    }

                    Divider()

                    // Liste des nutriments
                    nutriments.forEach { nutriment ->
                        val valeurBrute = valeursTotales[nutriment] ?: 0f
                        val valeurRapportee = if (diviseur > 0) valeurBrute / diviseur else 0f

                        Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Obtenir le nom à afficher selon le type de nutriment
                            val nomAffiche =
                                    when (nutriment) {
                                        is NutrientLipid ->
                                                (nutriment as NutrientLipid).nameToString()
                                        is NutrientMacro ->
                                                (nutriment as NutrientMacro).nameToString()
                                        is NutrientMain ->
                                                (nutriment as NutrientMain).nameToString()
                                        is NutrientMin -> (nutriment as NutrientMin).nameToString()
                                        is NutrientOther ->
                                                (nutriment as NutrientOther).nameToString()
                                        is NutrientVitam -> (nutriment as NutrientVitam).displayName
                                        is AAEnum -> (nutriment as AAEnum).nom
                                        else -> nutriment.toString()
                                    }

                            Text(
                                    text = nomAffiche,
                                    style = MaterialTheme.typography.body2,
                                    modifier = Modifier.weight(2f)
                            )
                            Text(
                                    text = String.format("%.2f", valeurRapportee),
                                    style = MaterialTheme.typography.body2,
                                    modifier = Modifier.weight(1f)
                            )
                            Text(
                                    text =
                                            "${nutriment.unite}/${typeDiviseur.substringAfter("poids ")}",
                                    style = MaterialTheme.typography.body2,
                                    modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Version plus compacte de l'analyse nutritionnelle pour les espaces plus restreints
 *
 * @param nutriments Liste des nutriments à afficher
 * @param valeursTotales Map des valeurs totales pour chaque nutriment
 * @param diviseur Valeur utilisée comme diviseur (poids de l'animal ou de la ration)
 * @param typeDiviseur Type de diviseur utilisé ("poids animal" ou "poids ration")
 * @param couleurFond Couleur de fond de la carte
 * @param onModeDivisionChange Callback appelé lors du changement de mode de division
 * @param modifier Modificateur optionnel pour personnaliser l'apparence
 */
@Composable
fun AnalyseNutritionnelleCompacte(
        nutriments: List<Nutrient>,
        valeursTotales: Map<Nutrient, Float>,
        diviseur: Float,
        typeDiviseur: String,
        couleurFond: Color = MaterialTheme.colors.surface,
        onModeDivisionChange: () -> Unit,
        modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth(), elevation = 4.dp, backgroundColor = couleurFond) {
        Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // En-tête avec titre et bouton pour changer le mode de division
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        text = "Analyse nutritionnelle",
                        style = MaterialTheme.typography.subtitle1,
                        color = VetNutriColors.Primary
                )

                TextButton(
                        onClick = onModeDivisionChange,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                            text = "Par $typeDiviseur",
                            style = MaterialTheme.typography.caption,
                            color = VetNutriColors.Primary
                    )
                }
            }

            Divider()

            // Corps de la carte
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                if (nutriments.isEmpty() || diviseur <= 0) {
                    Text(
                            text = if (diviseur <= 0) "Diviseur nul" else "Aucun nutriment",
                            style = MaterialTheme.typography.caption,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 24.dp)
                    )
                } else {
                    Column(
                            modifier =
                                    Modifier.fillMaxWidth()
                                            .height(150.dp)
                                            .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // Afficher manuellement chaque élément
                        nutriments.forEach { nutriment ->
                            val valeurBrute = valeursTotales[nutriment] ?: 0f
                            val valeurRapportee = if (diviseur > 0) valeurBrute / diviseur else 0f

                            // Obtenir le nom à afficher selon le type de nutriment
                            val nomAffiche =
                                    when (nutriment) {
                                        is NutrientLipid ->
                                                (nutriment as NutrientLipid).nameToString()
                                        is NutrientMacro ->
                                                (nutriment as NutrientMacro).nameToString()
                                        is NutrientMain ->
                                                (nutriment as NutrientMain).nameToString()
                                        is NutrientMin -> (nutriment as NutrientMin).nameToString()
                                        is NutrientOther ->
                                                (nutriment as NutrientOther).nameToString()
                                        is NutrientVitam -> (nutriment as NutrientVitam).displayName
                                        is AAEnum -> (nutriment as AAEnum).nom
                                        else -> nutriment.toString()
                                    }

                            Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                        text = nomAffiche,
                                        style = MaterialTheme.typography.caption,
                                        modifier = Modifier.weight(1.5f)
                                )
                                Text(
                                        text = String.format("%.2f", valeurRapportee),
                                        style = MaterialTheme.typography.caption,
                                        modifier = Modifier.weight(0.8f)
                                )
                                Text(
                                        text =
                                                "${nutriment.unite}/${typeDiviseur.substringAfter("poids ")}",
                                        style = MaterialTheme.typography.caption,
                                        modifier = Modifier.weight(0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Calcule les valeurs nutritionnelles totales d'une ration
 *
 * @param alimentsRation Liste des aliments de la ration
 * @param nutriments Liste des nutriments à considérer
 * @return Map associant chaque nutriment à sa valeur totale dans la ration
 */
fun calculerValeursNutritionnelles(
        alimentsRation: List<AlimentRation>,
        nutriments: List<Nutrient>
): Map<Nutrient, Float> {
    val resultat = mutableMapOf<Nutrient, Float>()

    // Initialiser tous les nutriments à 0
    nutriments.forEach { nutriment -> resultat[nutriment] = 0f }

    // Pour chaque aliment, ajouter sa contribution pour chaque nutriment
    alimentsRation.forEach { alimentRation ->
        // Si l'aliment a des informations nutritionnelles
        try {
            // Utilisation sécurisée pour gérer le cas où ces propriétés pourraient ne pas exister
            val aliment = alimentRation.aliment ?: return@forEach

            // Accès direct aux valeurs nutritionnelles dans valMap
            nutriments.forEach { nutriment ->
                val valeurNutritive = aliment.valMap[nutriment]?.value
                if (valeurNutritive != null) {
                    // La valeur est en g/kg ou unités/kg, donc pour obtenir la valeur réelle:
                    // valeur * quantité(g) / 1000
                    val quantiteEnKg = alimentRation.quantity / 1000f
                    val contributionNutriment = valeurNutritive * quantiteEnKg
                    val valeurCourante = resultat[nutriment] ?: 0f
                    resultat[nutriment] = valeurCourante + contributionNutriment
                }
            }

            // Si la valMap est vide ou si certains nutriments n'y sont pas,
            // essayer d'accéder via nutritionnalData pour compatibilité
            if (aliment.valMap.isEmpty() || nutriments.any { it !in aliment.valMap.keys }) {
                // Accès sécurisé aux propriétés nutritionnelles via réflexion
                val nutritionnelData =
                        try {
                            val field = aliment.javaClass.getDeclaredField("nutritionnalData")
                            field.isAccessible = true
                            field.get(aliment)
                        } catch (e: Exception) {
                            null
                        }

                if (nutritionnelData == null) return@forEach

                // Accéder à nutriContent
                val nutriContent =
                        try {
                            val field = nutritionnelData.javaClass.getDeclaredField("nutriContent")
                            field.isAccessible = true
                            field.get(nutritionnelData)
                        } catch (e: Exception) {
                            null
                        }

                if (nutriContent == null) return@forEach

                // Pour chaque nutriment demandé
                nutriments.forEach { nutriment ->
                    // Vérifier si la valeur existe déjà dans le résultat
                    if (resultat[nutriment] != 0f) return@forEach

                    // Récupérer la valeur nutritionnelle pour ce nutriment dans cet aliment
                    val valeurNutriment =
                            try {
                                when (nutriment) {
                                    is NutrientMain -> {
                                        val mainField =
                                                nutriContent.javaClass.getDeclaredField("main")
                                        mainField.isAccessible = true
                                        val main = mainField.get(nutriContent) as? Map<*, *>
                                        main?.get(nutriment) as? Float
                                    }
                                    is NutrientMacro -> {
                                        val macrosField =
                                                nutriContent.javaClass.getDeclaredField("macros")
                                        macrosField.isAccessible = true
                                        val macros = macrosField.get(nutriContent) as? Map<*, *>
                                        macros?.get(nutriment) as? Float
                                    }
                                    is NutrientLipid -> {
                                        val lipidsField =
                                                nutriContent.javaClass.getDeclaredField("lipids")
                                        lipidsField.isAccessible = true
                                        val lipids = lipidsField.get(nutriContent) as? Map<*, *>
                                        lipids?.get(nutriment) as? Float
                                    }
                                    is NutrientVitam -> {
                                        val vitaminsField =
                                                nutriContent.javaClass.getDeclaredField("vitamins")
                                        vitaminsField.isAccessible = true
                                        val vitamins = vitaminsField.get(nutriContent) as? Map<*, *>
                                        vitamins?.get(nutriment) as? Float
                                    }
                                    is NutrientMin -> {
                                        val mineralsField =
                                                nutriContent.javaClass.getDeclaredField("minerals")
                                        mineralsField.isAccessible = true
                                        val minerals = mineralsField.get(nutriContent) as? Map<*, *>
                                        minerals?.get(nutriment) as? Float
                                    }
                                    is AAEnum -> {
                                        val aaField = nutriContent.javaClass.getDeclaredField("aa")
                                        aaField.isAccessible = true
                                        val aa = aaField.get(nutriContent) as? Map<*, *>
                                        aa?.get(nutriment) as? Float
                                    }
                                    else -> null
                                }
                            } catch (e: Exception) {
                                null
                            }

                    // Si la valeur existe, l'ajouter au total en prenant en compte la quantité de
                    // l'aliment
                    valeurNutriment?.let { valeur ->
                        // La valeur est en g/kg ou unités/kg, donc pour obtenir la valeur réelle:
                        // valeur * quantité(g) / 1000
                        val quantiteEnKg = alimentRation.quantity / 1000f
                        // Calcul sécurisé de la contribution
                        val contributionNutriment = valeur * quantiteEnKg
                        // Mise à jour sécurisée du résultat
                        val valeurCourante = resultat[nutriment] ?: 0f
                        resultat[nutriment] = valeurCourante + contributionNutriment
                    }
                }
            }
        } catch (e: Exception) {
            // Ignorer les erreurs et continuer avec les autres aliments
            println("Erreur lors du calcul des valeurs nutritionnelles: ${e.message}")
        }
    }

    return resultat
}
