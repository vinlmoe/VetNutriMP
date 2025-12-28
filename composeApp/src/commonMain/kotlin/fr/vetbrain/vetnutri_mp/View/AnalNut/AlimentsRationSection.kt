package fr.vetbrain.vetnutri_mp.View.AnalNut

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import fr.vetbrain.vetnutri_mp.Components.AlimentItem
import fr.vetbrain.vetnutri_mp.Components.CenteredMessage
import fr.vetbrain.vetnutri_mp.Components.IconWithTooltip
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import kotlinx.coroutines.launch

/**
 * Section réutilisable pour l'affichage et la gestion des aliments d'une ration.
 *
 * @param selectedRation La ration sélectionnée (ou null)
 * @param referenceUtilisee La référence nutritionnelle utilisée (ou null)
 * @param besoinEnergetiqueTotal Le besoin énergétique total de l'animal
 * @param viewModel Le ViewModel pour les opérations sur les données
 * @param onAddAliment Callback pour ajouter un aliment
 * @param onMultiNutrientAdjustment Callback pour l'ajustement multi-nutriments
 * @param onOpenRecipeDialog Callback pour ouvrir le gestionnaire de recettes
 * @param onSaveRecipe Callback pour sauvegarder la ration actuelle comme recette
 * @param showSnackbar Callback pour afficher des messages
 * @param modifier Modificateur optionnel pour personnaliser l'apparence
 */
@Composable
fun SectionAlimentsRation(
        selectedRation: Ration?,
        referenceUtilisee: ReferenceEv?,
        besoinEnergetiqueTotal: Double?,
        besoinEnergetiqueStandard: Double?,
        viewModel: AnimalDetailViewModel,
        equationRepository: fr.vetbrain.vetnutri_mp.Repository.EquationRepository?,
        onAddAliment: () -> Unit,
        onMultiNutrientAdjustment: () -> Unit,
        onOpenRecipeDialog: () -> Unit,
        onSaveRecipe: () -> Unit,
        showSnackbar: (String) -> Unit,
        isCompact: Boolean = false,
        modifier: Modifier = Modifier
) {
        var editingAlimentId by remember { mutableStateOf<String?>(null) }
        val coroutineScope = rememberCoroutineScope()

        Card(
                modifier = modifier.fillMaxWidth(),
                elevation = AppSizes.elevationMedium,
                backgroundColor = MaterialTheme.colors.surface
        ) {
                Column(
                        modifier = Modifier.fillMaxWidth().padding(AppSizes.paddingMedium),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                ) {
                        // En-tête avec titre et boutons d'action
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Text(
                                        text = translate(LocalizationKeys.AnalNut.RATION_ALIMENTS_TITLE),
                                        style = MaterialTheme.typography.subtitle2,
                                        color = VetNutriColors.Primary
                                )
                                Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement =
                                                Arrangement.spacedBy(AppSizes.paddingXSmall)
                                ) {
                                        // Sauvegarder la ration comme recette
                                        IconWithTooltip(
                                                imageVector = Icons.Filled.Save,
                                                contentDescription = translate(LocalizationKeys.AnalNut.SAVE_AS_RECIPE),
                                                tint =
                                                        if (selectedRation?.alimentMutableList
                                                                        ?.isNotEmpty() == true
                                                        )
                                                                VetNutriColors.Primary
                                                        else
                                                                VetNutriColors.Primary.copy(
                                                                        alpha = 0.5f
                                                                ),
                                                modifier = Modifier.size(AppSizes.iconSizeXSmall),
                                                tooltip = translate(LocalizationKeys.AnalNut.SAVE_AS_RECIPE),
                                                enabled =
                                                        selectedRation
                                                                ?.alimentMutableList
                                                                ?.isNotEmpty() ==
                                                                true,
                                                onClick = onSaveRecipe
                                        )

                                        // Bouton pour l'ajustement multi-nutriments
                                        IconWithTooltip(
                                                imageVector = Icons.Filled.Tune,
                                                contentDescription = translate(LocalizationKeys.AnalNut.MULTI_NUTRIENT_ADJUSTMENT),
                                                tint =
                                                        if (selectedRation?.alimentMutableList
                                                                        ?.isNotEmpty() == true
                                                        )
                                                                VetNutriColors.Primary
                                                        else
                                                                VetNutriColors.Primary.copy(
                                                                        alpha = 0.5f
                                                                ),
                                                modifier = Modifier.size(AppSizes.iconSizeXSmall),
                                                tooltip = translate(LocalizationKeys.AnalNut.MULTI_NUTRIENT_ADJUSTMENT),
                                                enabled =
                                                        selectedRation
                                                                ?.alimentMutableList
                                                                ?.isNotEmpty() ==
                                                                true,
                                                onClick = onMultiNutrientAdjustment
                                        )

                                        // Bouton pour ajustement rapide multi-nutriments
                                        IconWithTooltip(
                                                imageVector = Icons.Filled.Balance,
                                                contentDescription =
                                                        translate(LocalizationKeys.AnalNut.QUICK_MULTI_NUTRIENT_ADJUSTMENT),
                                                tint =
                                                        if (selectedRation != null &&
                                                                        referenceUtilisee != null &&
                                                                        (selectedRation
                                                                                .alimentMutableList
                                                                                .isNotEmpty())
                                                        )
                                                                VetNutriColors.Primary
                                                        else
                                                                VetNutriColors.Primary.copy(
                                                                        alpha = 0.5f
                                                                ),
                                                modifier = Modifier.size(AppSizes.iconSizeXSmall),
                                                tooltip = translate(LocalizationKeys.AnalNut.QUICK_MULTI_NUTRIENT_ADJUSTMENT),
                                                enabled =
                                                        selectedRation !=
                                                                null &&
                                                                referenceUtilisee !=
                                                                        null &&
                                                                (selectedRation
                                                                        .alimentMutableList
                                                                        .isNotEmpty()),
                                                onClick = {
                                                                                if (selectedRation !=
                                                                                                null &&
                                                                                                referenceUtilisee !=
                                                                                                        null
                                                                                ) {
                                                                                        coroutineScope
                                                                                                .launch {
                                                                                                        try {
                                                                                                                // Créer les données d'ajustement par défaut
                                                                                                                val adjustmentData =
                                                                                                                        selectedRation
                                                                                                                                .alimentMutableList
                                                                                                                                .map {
                                                                                                                                        alimentRation
                                                                                                                                        ->
                                                                                                                                        val suggestion =
                                                                                                                                                suggestDefaultTargetNutrient(
                                                                                                                                                        alimentRation,
                                                                                                                                                        referenceUtilisee
                                                                                                                                                )
                                                                                                                                        AlimentAdjustmentData(
                                                                                                                                                alimentRation =
                                                                                                                                                        alimentRation,
                                                                                                                                                selectedNutrient =
                                                                                                                                                        suggestion,
                                                                                                                                                isLocked =
                                                                                                                                                        false,
                                                                                                                                                isEnergyAdjustable =
                                                                                                                                                        true
                                                                                                                                        )
                                                                                                                                }

                                                                                                                // Calculer l'ajustement avec les valeurs par défaut
                                                                                                                val result =
                                                                                                                        calculerAjustement(
                                                                                                                                ration =
                                                                                                                                        selectedRation,
                                                                                                                                adjustmentData =
                                                                                                                                        adjustmentData,
                                                                                                                                referenceUtilisee =
                                                                                                                                        referenceUtilisee,
                                                                                                                                besoinEnergetiqueTotal =
                                                                                                                                        besoinEnergetiqueTotal
                                                                                                                                                ?: 0.0,
                                                                                                                                besoinEnergetiqueStandard =
                                                                                                                                        besoinEnergetiqueStandard
                                                                                                                                                ?: 0.0,
                                                                                                                                poidsAnimal =
                                                                                                                                        null, // Valeur par défaut
                                                                                                                                poidsMetabolique =
                                                                                                                                        null, // Valeur par défaut
                                                                                                                                equationRepository =
                                                                                                                                        equationRepository
                                                                                                                        )

                                                                                                                if (result.success
                                                                                                                ) {
                                                                                                                        result.adjustedAliments
                                                                                                                                ?.let {
                                                                                                                                        adjustedAliments
                                                                                                                                        ->
                                                                                                                                        viewModel
                                                                                                                                                .updateRationAliments(
                                                                                                                                                        selectedRation,
                                                                                                                                                        adjustedAliments
                                                                                                                                                )
                                                                                                                                        showSnackbar(
                                                                                                                                                translate(LocalizationKeys.AnalNut.QUICK_ADJUST_SUCCESS, result.message)
                                                                                                                                        )
                                                                                                                                }
                                                                                                                } else {
                                                                                                                        showSnackbar(
                                                                                                                                translate(LocalizationKeys.AnalNut.ADJUST_ERROR, result.message)
                                                                                                                        )
                                                                                                                }
                                                                                                        } catch (
                                                                                                                e:
                                                                                                                        Exception) {
                                                                                                                showSnackbar(
                                                                                                                        translate(LocalizationKeys.AnalNut.ADJUST_ERROR, e.message ?: "Unknown error")
                                                                                                                )
                                                                                                        }
                                                                                                }
                                                                                }
                                                                        }
                                        )

                                        // Ouvrir le gestionnaire de recettes
                                        IconWithTooltip(
                                                imageVector = Icons.Filled.MenuBook,
                                                contentDescription = translate(LocalizationKeys.AnalNut.OPEN_RECIPES),
                                                tint = VetNutriColors.Primary,
                                                modifier = Modifier.size(AppSizes.iconSizeXSmall),
                                                tooltip = translate(LocalizationKeys.AnalNut.OPEN_RECIPES),
                                                onClick = onOpenRecipeDialog
                                        )

                                        // Bouton pour ajouter un aliment
                                        IconWithTooltip(
                                                imageVector = Icons.Filled.Add,
                                                contentDescription = translate(LocalizationKeys.AnalNut.ADD_ALIMENT),
                                                tint = VetNutriColors.Primary,
                                                modifier = Modifier.size(AppSizes.iconSizeXSmall),
                                                tooltip = translate(LocalizationKeys.AnalNut.ADD_ALIMENT),
                                                onClick = onAddAliment
                                        )
                                }
                        }

                        Divider()

                        // Liste des aliments
                        if (selectedRation == null) {
                                CenteredMessage(
                                        message = translate(LocalizationKeys.AnalNut.NO_RATION_SELECTED),
                                        modifier = Modifier.fillMaxWidth()
                                )
                        } else if (selectedRation.alimentMutableList.isEmpty()) {
                                CenteredMessage(
                                        message = translate(LocalizationKeys.AnalNut.NO_ALIMENT_IN_RATION),
                                        modifier = Modifier.fillMaxWidth()
                                )
                        } else {
                                if (isCompact) {
                                        // Vue compacte : Column simple
                                        Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalArrangement =
                                                        Arrangement.spacedBy(AppSizes.paddingSmall)
                                        ) {
                                                selectedRation.alimentMutableList.forEach { aliment ->
                                                        key("${aliment.uuid}-${aliment.quantite}") {
                                                                AlimentItem(
                                                                        aliment = aliment,
                                                                        isEditing =
                                                                                editingAlimentId ==
                                                                                        aliment.uuid,
                                                                        onStartEditing = {
                                                                                if (editingAlimentId !=
                                                                                                null &&
                                                                                                editingAlimentId !=
                                                                                                        aliment.uuid
                                                                                ) {
                                                                                        editingAlimentId =
                                                                                                null
                                                                                }
                                                                                editingAlimentId =
                                                                                        aliment.uuid
                                                                        },
                                                                        onQuantityChange = { newQuantity ->
                                                                                viewModel
                                                                                        .updateAlimentQuantity(
                                                                                                aliment.uuid,
                                                                                                newQuantity
                                                                                        )
                                                                        },
                                                                        onFinishEditing = {
                                                                                editingAlimentId = null
                                                                        },
                                                                        onDelete = {
                                                                                viewModel
                                                                                        .removeAlimentFromRation(
                                                                                                aliment.uuid
                                                                                        )
                                                                        }
                                                                )
                                                        }
                                                }
                                        }
                                } else {
                                        // Vue normale : LazyColumn avec scroll
                                        LazyColumn(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement =
                                                        Arrangement.spacedBy(AppSizes.paddingSmall)
                                        ) {
                                                items(
                                                        selectedRation.alimentMutableList,
                                                        key = { "${it.uuid}-${it.quantite}" }
                                                ) { aliment ->
                                                        AlimentItem(
                                                                aliment = aliment,
                                                                isEditing =
                                                                        editingAlimentId ==
                                                                                aliment.uuid,
                                                                onStartEditing = {
                                                                        if (editingAlimentId !=
                                                                                        null &&
                                                                                        editingAlimentId !=
                                                                                                aliment.uuid
                                                                        ) {
                                                                                editingAlimentId =
                                                                                        null
                                                                        }
                                                                        editingAlimentId =
                                                                                aliment.uuid
                                                                },
                                                                onQuantityChange = { newQuantity ->
                                                                        viewModel
                                                                                .updateAlimentQuantity(
                                                                                        aliment.uuid,
                                                                                        newQuantity
                                                                                )
                                                                },
                                                                onFinishEditing = {
                                                                        editingAlimentId = null
                                                                },
                                                                onDelete = {
                                                                        viewModel
                                                                                .removeAlimentFromRation(
                                                                                        aliment.uuid
                                                                                )
                                                                }
                                                        )
                                                }
                                        }
                                }
                        }
                }
        }
}

/**
 * Calcule la densité énergétique d'un aliment selon les formules de la référence
 * @param alimentRation L'aliment ration pour lequel calculer la densité énergétique
 * @param reference La référence contenant les équations DE commerciale et brute
 * @return La densité énergétique en kcal/100g
 */
private fun calculerDensiteEnergetique(
        alimentRation: fr.vetbrain.vetnutri_mp.Data.AlimentRation,
        reference: ReferenceEv
): Double {
        try {
                val aliment = alimentRation.aliment ?: return 0.0

                // Déterminer si l'aliment est commercial (complet/complémentaire) ou brut
                val estCommercial =
                        aliment.indicat.any { indication ->
                                indication.name == "COMP" || indication.name == "COMPL"
                        }

                // Choisir l'équation appropriée
                val equation =
                        if (estCommercial) {
                                reference.equationDEcom
                        } else {
                                reference.equationDEraw
                        }

                if (equation == null || equation.equationScript.isEmpty()) {
                        return 0.0
                }

                // Créer les variables pour l'évaluation
                val variables = mutableMapOf<String, Double>()

                // Ajouter les nutriments principaux nécessaires aux formules
                aliment.valMap.forEach { (nutrient, quantity) ->
                        when (nutrient.label.uppercase()) {
                                "PROTEINE", "PB" -> {
                                        variables["PB"] = quantity.value.toDouble()
                                        variables["PROT"] =
                                                quantity.value
                                                        .toDouble() // Alias pour compatibilité
                                        variables["PROTEINE"] = quantity.value.toDouble()
                                }
                                "LIPIDE" -> {
                                        variables["LIPIDE"] = quantity.value.toDouble()
                                        variables["LIP"] =
                                                quantity.value
                                                        .toDouble() // Alias pour compatibilité
                                }
                                "ENA" -> variables["ENA"] = quantity.value.toDouble()
                                "CENDRE" -> variables["CENDRE"] = quantity.value.toDouble()
                        }
                }

                // Évaluer l'équation
                val resultat =
                        fr.vetbrain.vetnutri_mp.Utils.ExpressionMathematique.evaluer(
                                equation.equationScript,
                                variables
                        )

                return resultat ?: 0.0
        } catch (e: Exception) {
                return 0.0
        }
}
