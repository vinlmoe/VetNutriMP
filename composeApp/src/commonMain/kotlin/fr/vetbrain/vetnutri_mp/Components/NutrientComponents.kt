package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Enumer.AAEnum
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMin
import fr.vetbrain.vetnutri_mp.Enumer.NutrientOther
import fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors

/**
 * Composant pour afficher une section de valeurs nutritionnelles avec un fond coloré.
 *
 * @param titre Titre de la section
 * @param nutriments Liste des nutriments à afficher
 * @param valeursNutriments Map des valeurs actuelles pour chaque nutriment (état mutable)
 * @param erreursNutriments Map des erreurs de validation pour chaque nutriment (état mutable)
 * @param couleurArrierePlan Couleur d'arrière-plan de la carte
 * @param modifier Modificateur Compose optionnel
 */
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
                                                        is NutrientLipid ->
                                                                (nutriment as NutrientLipid)
                                                                        .nameToString()
                                                        is NutrientMacro ->
                                                                (nutriment as NutrientMacro)
                                                                        .nameToString()
                                                        is NutrientMain ->
                                                                (nutriment as NutrientMain)
                                                                        .nameToString()
                                                        is NutrientMin ->
                                                                (nutriment as NutrientMin)
                                                                        .nameToString()
                                                        is NutrientOther ->
                                                                (nutriment as NutrientOther)
                                                                        .nameToString()
                                                        is NutrientVitam ->
                                                                (nutriment as NutrientVitam)
                                                                        .displayName
                                                        is AAEnum -> (nutriment as AAEnum).nom
                                                        else -> nutriment.toString()
                                                }

                                        Text(text = nomAffiche, modifier = Modifier.weight(1f))

                                        OutlinedTextField(
                                                value = valeur,
                                                onValueChange = { nouvelleValeur ->
                                                        valeursNutriments[nutriment] =
                                                                nouvelleValeur
                                                        // Validation en temps réel
                                                        if (nouvelleValeur.isNotBlank()) {
                                                                val valeurDouble =
                                                                        nouvelleValeur
                                                                                .replace(",", ".")
                                                                                .toDoubleOrNull()
                                                                erreursNutriments[nutriment] =
                                                                        valeurDouble == null ||
                                                                                valeurDouble < 0
                                                        } else {
                                                                erreursNutriments.remove(nutriment)
                                                        }
                                                },
                                                modifier = Modifier.width(120.dp),
                                                keyboardOptions =
                                                        KeyboardOptions(
                                                                keyboardType = KeyboardType.Decimal
                                                        ),
                                                colors =
                                                        TextFieldDefaults.outlinedTextFieldColors(
                                                                focusedBorderColor =
                                                                        if (aErreur) Color.Red
                                                                        else VetNutriColors.Primary,
                                                                unfocusedBorderColor =
                                                                        if (aErreur) Color.Red
                                                                        else Color.Gray,
                                                                backgroundColor =
                                                                        Color.White.copy(
                                                                                alpha = 0.8f
                                                                        ),
                                                                errorBorderColor = Color.Red
                                                        ),
                                                isError = aErreur,
                                                trailingIcon = {
                                                        Text(
                                                                text = nutriment.unite,
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .caption
                                                        )
                                                }
                                        )
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
        valeursTotales: Map<Nutrient, Double>,
        diviseur: Double,
        typeDiviseur: String,
        couleurFond: Color = MaterialTheme.colors.surface,
        onModeDivisionChange: () -> Unit,
        modifier: Modifier = Modifier
) {
        Card(
                modifier = modifier.fillMaxWidth(),
                elevation = AppSizes.elevationMedium,
                backgroundColor = couleurFond
        ) {
                Column(
                        modifier = Modifier.padding(AppSizes.paddingMedium),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
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
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .height(AppSizes.cardMinHeight.times(1.5f)),
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
                                        verticalArrangement =
                                                Arrangement.spacedBy(AppSizes.paddingXSmall)
                                ) {
                                        // En-tête du tableau
                                        Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                                Text(
                                                        text = "Nutriment",
                                                        style =
                                                                MaterialTheme.typography.subtitle2
                                                                        .copy(
                                                                                fontWeight =
                                                                                        FontWeight
                                                                                                .Bold
                                                                        ),
                                                        modifier = Modifier.weight(2f)
                                                )
                                                Text(
                                                        text = "Valeur",
                                                        style =
                                                                MaterialTheme.typography.subtitle2
                                                                        .copy(
                                                                                fontWeight =
                                                                                        FontWeight
                                                                                                .Bold
                                                                        ),
                                                        modifier = Modifier.weight(1f)
                                                )
                                                Text(
                                                        text = "Unité",
                                                        style =
                                                                MaterialTheme.typography.subtitle2
                                                                        .copy(
                                                                                fontWeight =
                                                                                        FontWeight
                                                                                                .Bold
                                                                        ),
                                                        modifier = Modifier.weight(1f)
                                                )
                                        }

                                        Divider()

                                        // Liste des nutriments
                                        nutriments.forEach { nutriment ->
                                                val valeurBrute = valeursTotales[nutriment] ?: 0.0
                                                val valeurRapportee =
                                                        if (diviseur > 0) valeurBrute / diviseur
                                                        else 0.0

                                                Row(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .padding(
                                                                                vertical =
                                                                                        AppSizes.paddingXXSmall
                                                                        ),
                                                        horizontalArrangement =
                                                                Arrangement.SpaceBetween,
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        // Obtenir le nom à afficher selon le type
                                                        // de nutriment
                                                        val nomAffiche =
                                                                when (nutriment) {
                                                                        is NutrientLipid ->
                                                                                (nutriment as
                                                                                                NutrientLipid)
                                                                                        .nameToString()
                                                                        is NutrientMacro ->
                                                                                (nutriment as
                                                                                                NutrientMacro)
                                                                                        .nameToString()
                                                                        is NutrientMain ->
                                                                                (nutriment as
                                                                                                NutrientMain)
                                                                                        .nameToString()
                                                                        is NutrientMin ->
                                                                                (nutriment as
                                                                                                NutrientMin)
                                                                                        .nameToString()
                                                                        is NutrientOther ->
                                                                                (nutriment as
                                                                                                NutrientOther)
                                                                                        .nameToString()
                                                                        is NutrientVitam ->
                                                                                (nutriment as
                                                                                                NutrientVitam)
                                                                                        .displayName
                                                                        is AAEnum ->
                                                                                (nutriment as
                                                                                                AAEnum)
                                                                                        .nom
                                                                        else -> nutriment.toString()
                                                                }

                                                        Text(
                                                                text = nomAffiche,
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .body2,
                                                                modifier = Modifier.weight(2f)
                                                        )
                                                        Text(
                                                                text =
                                                                        valeurRapportee
                                                                                .toString()
                                                                                .take(4),
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .body2,
                                                                modifier = Modifier.weight(1f)
                                                        )
                                                        Text(
                                                                text =
                                                                        "${nutriment.unite}/${typeDiviseur.substringAfter("poids ")}",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .body2,
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
        valeursTotales: Map<Nutrient, Double>,
        diviseur: Double,
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
                                        contentPadding =
                                                PaddingValues(horizontal = 8.dp, vertical = 4.dp)
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
                        Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                        ) {
                                if (nutriments.isEmpty() || diviseur <= 0) {
                                        Text(
                                                text =
                                                        if (diviseur <= 0) "Diviseur nul"
                                                        else "Aucun nutriment",
                                                style = MaterialTheme.typography.caption,
                                                color = Color.Gray,
                                                modifier = Modifier.padding(vertical = 24.dp)
                                        )
                                } else {
                                        Column(
                                                modifier =
                                                        Modifier.fillMaxWidth()
                                                                .height(150.dp)
                                                                .verticalScroll(
                                                                        rememberScrollState()
                                                                ),
                                                verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                                // Afficher manuellement chaque élément
                                                nutriments.forEach { nutriment ->
                                                        val valeurBrute =
                                                                valeursTotales[nutriment] ?: 0.0
                                                        val valeurRapportee =
                                                                if (diviseur > 0)
                                                                        valeurBrute / diviseur
                                                                else 0.0

                                                        // Obtenir le nom à afficher selon le type
                                                        // de nutriment
                                                        val nomAffiche =
                                                                when (nutriment) {
                                                                        is NutrientLipid ->
                                                                                (nutriment as
                                                                                                NutrientLipid)
                                                                                        .nameToString()
                                                                        is NutrientMacro ->
                                                                                (nutriment as
                                                                                                NutrientMacro)
                                                                                        .nameToString()
                                                                        is NutrientMain ->
                                                                                (nutriment as
                                                                                                NutrientMain)
                                                                                        .nameToString()
                                                                        is NutrientMin ->
                                                                                (nutriment as
                                                                                                NutrientMin)
                                                                                        .nameToString()
                                                                        is NutrientOther ->
                                                                                (nutriment as
                                                                                                NutrientOther)
                                                                                        .nameToString()
                                                                        is NutrientVitam ->
                                                                                (nutriment as
                                                                                                NutrientVitam)
                                                                                        .displayName
                                                                        is AAEnum ->
                                                                                (nutriment as
                                                                                                AAEnum)
                                                                                        .nom
                                                                        else -> nutriment.toString()
                                                                }

                                                        Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement =
                                                                        Arrangement.SpaceBetween
                                                        ) {
                                                                Text(
                                                                        text = nomAffiche,
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .caption,
                                                                        modifier =
                                                                                Modifier.weight(
                                                                                        1.5f
                                                                                )
                                                                )
                                                                Text(
                                                                        text =
                                                                                valeurRapportee
                                                                                        .toString()
                                                                                        .take(4),
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .caption,
                                                                        modifier =
                                                                                Modifier.weight(
                                                                                        0.8f
                                                                                )
                                                                )
                                                                Text(
                                                                        text =
                                                                                "${nutriment.unite}/${typeDiviseur.substringAfter("poids ")}",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .caption,
                                                                        modifier =
                                                                                Modifier.weight(
                                                                                        0.7f
                                                                                )
                                                                )
                                                        }
                                                }
                                        }
                                }
                        }
                }
        }
}
