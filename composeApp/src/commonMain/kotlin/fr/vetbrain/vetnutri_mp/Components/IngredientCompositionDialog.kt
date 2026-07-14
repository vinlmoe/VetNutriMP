package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Data.determinerCategorieNutriment
import fr.vetbrain.vetnutri_mp.Data.obtenirTitreCategorie
import fr.vetbrain.vetnutri_mp.Data.ordreNutrimentParType
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.GraphFormattingUtils

/** Ordre d'affichage des catégories de nutriments, identique à FoodEditViewModel.loadNutrients(). */
private val ORDRE_CATEGORIES = listOf("BASE", "ENERGY", "MACRO", "LIPID", "MIN", "VITAM", "AMA", "ANA", "OTHER")

/**
 * Dialogue en lecture seule affichant la composition nutritionnelle complète d'un aliment
 * (pour 100 g ou pour 1000 kcal) ainsi que sa bibliographie associée.
 */
@Composable
fun IngredientCompositionDialog(
        aliment: AlimentEv,
        referenceEv: ReferenceEv?,
        onDismiss: () -> Unit
) {
        var afficherPour1000Kcal by remember { mutableStateOf(false) }

        val energiePour100g =
                remember(aliment, referenceEv) { aliment.getNutrient(NutrientMain.ENERGIE, referenceEv) }
        val energieDisponible = energiePour100g != null && energiePour100g > 0.0

        val nutrimentsParCategorie =
                remember(aliment, referenceEv) {
                        aliment.valMap.keys
                                .mapNotNull { nutrient ->
                                        val valeur = aliment.getNutrient(nutrient, referenceEv)
                                        if (valeur != null) nutrient to valeur else null
                                }
                                .groupBy { (nutrient, _) -> determinerCategorieNutriment(nutrient.label, nutrient) }
                                .mapValues { (_, paires) ->
                                        paires.sortedBy { (nutrient, _) -> ordreNutrimentParType(nutrient) }
                                }
                                .toList()
                                .sortedBy { (categorie, _) ->
                                        ORDRE_CATEGORIES.indexOf(categorie).let { if (it < 0) ORDRE_CATEGORIES.size else it }
                                }
                }

        AlertDialog(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f),
                onDismissRequest = onDismiss,
                title = {
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Text(
                                        text = aliment.nom ?: translate("alimentItem.noNameLabel"),
                                        style = MaterialTheme.typography.h6,
                                        fontWeight = FontWeight.Bold,
                                        color = VetNutriColors.Primary,
                                        modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                                        Icon(
                                                imageVector = Icons.Filled.Close,
                                                contentDescription = translate(LocalizationKeys.General.CLOSE),
                                                tint = VetNutriColors.Primary,
                                                modifier = Modifier.size(24.dp)
                                        )
                                }
                        }
                },
                text = {
                        Column(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                                // Bascule pour 100 g / pour 1000 kcal
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                                ) {
                                        Button(
                                                onClick = { afficherPour1000Kcal = false },
                                                modifier = Modifier.weight(1f),
                                                colors =
                                                        ButtonDefaults.buttonColors(
                                                                backgroundColor =
                                                                        if (!afficherPour1000Kcal) VetNutriColors.Primary
                                                                        else MaterialTheme.colors.surface
                                                        )
                                        ) {
                                                Text(
                                                        translate("compositionDialog.per100g"),
                                                        color =
                                                                if (!afficherPour1000Kcal) VetNutriColors.OnPrimary
                                                                else MaterialTheme.colors.onSurface
                                                )
                                        }
                                        Button(
                                                onClick = { afficherPour1000Kcal = true },
                                                enabled = energieDisponible,
                                                modifier = Modifier.weight(1f),
                                                colors =
                                                        ButtonDefaults.buttonColors(
                                                                backgroundColor =
                                                                        if (afficherPour1000Kcal) VetNutriColors.Primary
                                                                        else MaterialTheme.colors.surface
                                                        )
                                        ) {
                                                Text(
                                                        translate("compositionDialog.per1000kcal"),
                                                        color =
                                                                if (afficherPour1000Kcal) VetNutriColors.OnPrimary
                                                                else MaterialTheme.colors.onSurface
                                                )
                                        }
                                }

                                if (afficherPour1000Kcal && !energieDisponible) {
                                        Text(
                                                text = translate("compositionDialog.energyUnavailable"),
                                                style = MaterialTheme.typography.caption,
                                                color = VetNutriColors.Error,
                                                modifier = Modifier.padding(top = AppSizes.paddingXSmall)
                                        )
                                }

                                Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                                Divider()

                                LazyColumn(
                                        modifier = Modifier.fillMaxWidth().weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingXSmall)
                                ) {
                                        nutrimentsParCategorie.forEach { (categorie, nutriments) ->
                                                item {
                                                        Text(
                                                                text = obtenirTitreCategorie(categorie),
                                                                style = MaterialTheme.typography.subtitle1,
                                                                fontWeight = FontWeight.Bold,
                                                                color = VetNutriColors.Primary,
                                                                modifier = Modifier.padding(top = AppSizes.paddingSmall)
                                                        )
                                                }
                                                items(nutriments) { (nutrient, valeurPour100g) ->
                                                        NutrimentCompositionRow(
                                                                nutrient = nutrient,
                                                                valeurPour100g = valeurPour100g,
                                                                afficherPour1000Kcal = afficherPour1000Kcal,
                                                                energiePour100g = energiePour100g
                                                        )
                                                }
                                        }

                                        item {
                                                Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                                                Text(
                                                        text = translate("compositionDialog.bibliographyTitle"),
                                                        style = MaterialTheme.typography.subtitle1,
                                                        fontWeight = FontWeight.Bold,
                                                        color = VetNutriColors.Primary
                                                )
                                                Divider()
                                        }

                                        if (aliment.biblioRefs.isEmpty()) {
                                                item {
                                                        Text(
                                                                text = translate("compositionDialog.noBibliography"),
                                                                style = MaterialTheme.typography.body2,
                                                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                                                                modifier = Modifier.padding(vertical = AppSizes.paddingSmall)
                                                        )
                                                }
                                        } else {
                                                items(aliment.biblioRefs) { ref -> BiblioRefReadOnlyCard(ref) }
                                        }
                                }
                        }
                },
                confirmButton = {},
                dismissButton = {}
        )
}

@Composable
private fun NutrimentCompositionRow(
        nutrient: Nutrient,
        valeurPour100g: Double,
        afficherPour1000Kcal: Boolean,
        energiePour100g: Double?
) {
        val (valeur, unite) =
                if (afficherPour1000Kcal && energiePour100g != null && energiePour100g > 0.0) {
                        Pair((valeurPour100g / energiePour100g) * 1000.0, "${nutrient.unite}/1000kcal")
                } else {
                        Pair(valeurPour100g, nutrient.unite)
                }

        Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = AppSizes.paddingXXSmall),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
                Text(text = nutrient.translateEnum(), style = MaterialTheme.typography.body2)
                Text(
                        text = "${GraphFormattingUtils.formatSmartDecimal(valeur)} $unite",
                        style = MaterialTheme.typography.body2,
                        fontWeight = FontWeight.Medium
                )
        }
}

@Composable
private fun BiblioRefReadOnlyCard(ref: fr.vetbrain.vetnutri_mp.Data.BiblioRef) {
        Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = AppSizes.paddingXXSmall),
                elevation = AppSizes.elevationSmall
        ) {
                Column(modifier = Modifier.fillMaxWidth().padding(AppSizes.paddingSmall)) {
                        Text(
                                text = "${ref.firstAuthor} (${ref.year})",
                                style = MaterialTheme.typography.subtitle2
                        )
                        if (ref.completeRef.isNotBlank()) {
                                Text(
                                        text = ref.completeRef,
                                        style = MaterialTheme.typography.caption,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                )
                        }
                }
        }
}
