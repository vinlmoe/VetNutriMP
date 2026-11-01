package fr.vetbrain.vetnutri_mp.View

import androidx.compose.ui.focus.FocusManager

import fr.vetbrain.vetnutri_mp.Utils.rethrowIfCancellation
import fr.vetbrain.vetnutri_mp.Utils.UserErrors
import fr.vetbrain.vetnutri_mp.Utils.orUserError
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.graphics.Color
import fr.vetbrain.vetnutri_mp.Components.IconWithTooltip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.CenteredMessage
import fr.vetbrain.vetnutri_mp.Components.RationItem
import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.Data.ValeurNutritionnelle
import fr.vetbrain.vetnutri_mp.Data.convertirPreferencesVersLabelsNutriments
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Animal
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.AnalNut
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.AnimalDetail
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Consultation
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Evolutive
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.General
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Ration as RationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Repository.RecipeRepository
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.View.AnalNut.AnalyseNutritionnelleCard
import fr.vetbrain.vetnutri_mp.View.AnalNut.MultiNutrientAdjustmentView
import fr.vetbrain.vetnutri_mp.View.AnalNut.NutrientDetailDialog
import fr.vetbrain.vetnutri_mp.View.AnalNut.SectionAlimentsRation
import fr.vetbrain.vetnutri_mp.View.AnalNut.SectionBilanEnergetique
import fr.vetbrain.vetnutri_mp.View.AnalNut.SectionCoefficients
import fr.vetbrain.vetnutri_mp.View.AnalNut.SectionValeursMetaboliques
import fr.vetbrain.vetnutri_mp.View.AnalNut.AnalyseQuantitativeRationSection
import fr.vetbrain.vetnutri_mp.View.Components.RecipeDialog
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.MutableState

/** Onglet Rations : grille des cartes (rations de la consultation, aliments de la ration, analyse nutritionnelle) */
@Composable
internal fun ColumnScope.GrilleRations(
        dialogues: RationsDialogState,
        analyseGroupee: Boolean,
        analyseTroupeau: AnalyseTroupeau?,
        animal: AnimalEv?,
        apportsTroupeauEntier: List<LigneApportTroupeau>,
        besoinEnergetiqueStandard: Double?,
        besoinEnergetiqueTotal: Double?,
        effectiveTypeExpressionBesoin: TypeExpressionBesoin,
        energieApportee: Double,
        equationRepository: EquationRepository,
        focusManager: FocusManager,
        isExamMode: Boolean,
        modifier: Modifier,
        nutrimentsSelectionnesPreferences: List<String>,
        poidsEffectif: Double?,
        poidsMetabolique: Double?,
        preferencesApplication: PreferencesApplication?,
        rationAnalysisScope: RationAnalysisScope,
        rationsActuelles: List<Ration>,
        rationsAffichees: List<Ration>,
        rationsProposees: List<Ration>,
        referenceUtilisee: ReferenceEv?,
        referencesMaladiesResolues: List<ReferenceEv>,
        selectedConsultation: ConsultationEv?,
        selectedRation: Ration?,
        showSnackbar: (String) -> Unit,
        troupeauEntier: Boolean,
        typeExpressionSelector: @Composable RowScope.() -> Unit,
        viewModel: AnimalDetailViewModel
) {
        var etapeAEditer by dialogues.etapeAEditer
        var newRecipeName by dialogues.newRecipeName
        var parentNouvelleEtape by dialogues.parentNouvelleEtape
        var parentSynthese by dialogues.parentSynthese
        var rationForAddAliment by dialogues.rationForAddAliment
        var rationToDelete by dialogues.rationToDelete
        var rationToEdit by dialogues.rationToEdit
        var selectedNutrimentData by dialogues.selectedNutrimentData
        var showAddAlimentView by dialogues.showAddAlimentView
        var showCopyRationDialog by dialogues.showCopyRationDialog
        var showDeleteRationDialog by dialogues.showDeleteRationDialog
        var showMultiNutrientAdjustmentDialog by dialogues.showMultiNutrientAdjustmentDialog
        var showNutrimentDetailDialog by dialogues.showNutrimentDetailDialog
        var showRationEditDialog by dialogues.showRationEditDialog
        var showRationNutrientInspector by dialogues.showRationNutrientInspector
        var showRecipeDialog by dialogues.showRecipeDialog
        var showSaveRecipeDialog by dialogues.showSaveRecipeDialog
        if (selectedConsultation != null) Box(
                modifier =
                        Modifier.weight(1f)
                                .padding(
                                        horizontal = AppSizes.paddingMedium,
                                        vertical = AppSizes.paddingSmall
                                )
        ) {
                Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement =
                                Arrangement.spacedBy(AppSizes.paddingMedium)
                ) {
                        // Colonne gauche (listes) - 50% de l'espace
                        Column(
                                modifier = Modifier.weight(0.5f),
                                verticalArrangement =
                                        Arrangement.spacedBy(AppSizes.paddingMedium)
                        ) {
                                // Segment 2: Liste des rations de la
                                // consultation
                                Card(
                                        modifier =
                                                Modifier.weight(1f).fillMaxWidth(),
                                        elevation = AppSizes.elevationMedium,
                                        backgroundColor =
                                                MaterialTheme.colors.surface
                                ) {
                                        Column(
                                                modifier =
                                                        Modifier.fillMaxSize()
                                                                .padding(
                                                                        AppSizes.paddingMedium
                                                                ),
                                                verticalArrangement =
                                                        Arrangement.spacedBy(
                                                                AppSizes.paddingSmall
                                                        )
                                        ) {
                                                // En-tête avec titre et
                                                // bouton
                                                // d'ajout
                                                Row(
                                                        modifier =
                                                                Modifier.fillMaxWidth(),
                                                        horizontalArrangement =
                                                                Arrangement
                                                                        .SpaceBetween,
                                                        verticalAlignment =
                                                                Alignment
                                                                        .CenterVertically
                                                ) {
                                                        // Titre "Rations de la
                                                        // consultation" (dans la
                                                        // Card en-tête)
                                                        Text(
                                                                text =
                                                                        translate(RationKeys.CONSULTATION_RATIONS),
                                                                modifier = Modifier.weight(1f),
                                                                style =
                                                                        MaterialTheme
                                                                                .typography
                                                                                .subtitle2, // taille réduite
                                                                color =
                                                                        VetNutriColors
                                                                                .Primary
                                                        )

                                                        // Bouton pour
                                                        // ajouter une
                                                        // nouvelle ration
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingXSmall)
                                                        ) {
                                                            if (!isExamMode) {
                                                                IconWithTooltip(
                                                                    imageVector = Icons.Filled.ContentCopy,
                                                                    contentDescription = translate("copieRation.reprendre"),
                                                                    tooltip = "Reprendre une ration d’une autre consultation",
                                                                    onClick = { showCopyRationDialog = true },
                                                                    enabled = selectedConsultation != null,
                                                                    tint = VetNutriColors.Primary,
                                                                    modifier = Modifier.size(AppSizes.iconSizeXSmall)
                                                                )
                                                            }
                                                            IconWithTooltip(
                                                                imageVector = Icons.Filled.Add,
                                                                contentDescription = translate(AnalNut.ADD_RATION),
                                                                tooltip = translate(AnalNut.ADD_RATION),
                                                                onClick = {
                                                                    rationToEdit = null
                                                                    showRationEditDialog = true
                                                                },
                                                                tint = VetNutriColors.Primary,
                                                                modifier = Modifier.size(AppSizes.iconSizeXSmall)
                                                            )
                                                        }
                                                }

                                                Divider()

                                                if (rationsAffichees.isEmpty()
                                                ) {
                                                        CenteredMessage(
                                                                message =
                                                                        translate(RationKeys.NO_RATION_AVAILABLE),
                                                                modifier =
                                                                        Modifier.weight(
                                                                                1f
                                                                        )
                                                        )
                                                } else {
                                                        LazyColumn(
                                                                modifier =
                                                                        Modifier.weight(
                                                                                1f
                                                                        ),
                                                                verticalArrangement =
                                                                        Arrangement
                                                                                .spacedBy(
                                                                                        8.dp
                                                                                )
                                                        ) {
                                                                item(key = "groupes-rations") {
                                                                        GroupesRationsItems(
                                                                                rationsActuelles = rationsActuelles,
                                                                                rationsProposees = rationsProposees,
                                                                                scopeCourant = rationAnalysisScope,
                                                                                onSelectGroupe = { scope ->
                                                                                        focusManager.clearFocus(force = true)
                                                                                        viewModel.setRationAnalysisScope(scope)
                                                                                }
                                                                        )
                                                                }
                                                                items(
                                                                        rationsAffichees,
                                                                        key = { ration -> ration.uuid }
                                                                ) { ration ->
                                                                        RationItem(
                                                                                // Plan évolutif : étapes en retrait sous leur ration, actions sur la ration
                                                                                estEtape = ration.refRationParente != null,
                                                                                modifier =
                                                                                        if (ration.refRationParente != null) Modifier.padding(start = 24.dp)
                                                                                        else Modifier,
                                                                                onAjouterEtape =
                                                                                        if (ration.refRationParente == null) { { parentNouvelleEtape = ration } }
                                                                                        else null,
                                                                                onSynthesePlan =
                                                                                        if (selectedConsultation?.let { PlanEvolutif.aUnPlan(it, ration) } == true)
                                                                                                { { parentSynthese = ration } }
                                                                                        else null,
                                                                                ration =
                                                                                        ration,
                                                                                isSelected =
                                                                                        ration.uuid ==
                                                                                                selectedRation
                                                                                                        ?.uuid,
                                                                                onClick = {
                                                                                        focusManager.clearFocus(
                                                                                                force = true
                                                                                        )
                                                                                        viewModel
                                                                                                .selectRation(
                                                                                                        ration
                                                                                                )
                                                                                },
                                                                                onEdit = {
                                                                                        // Étape : édition du poids et des variables (nom fixe)
                                                                                        if (ration.refRationParente != null) {
                                                                                                etapeAEditer = ration
                                                                                        } else {
                                                                                                rationToEdit = ration
                                                                                                showRationEditDialog = true
                                                                                        }
                                                                                },
                                                                                onDuplicate = {
                                                                                        viewModel
                                                                                                .duplicateRation(
                                                                                                        ration
                                                                                                )
                                                                                        showSnackbar(
                                                                                                "${translate(RationKeys.DUPLICATED)} '${ration.name}'"
                                                                                        )
                                                                                },
                                                                                onDelete = {
                                                                                        rationToDelete =
                                                                                                ration
                                                                                        showDeleteRationDialog =
                                                                                                true
                                                                                }
                                                                        )
                                                                }
                                                        }
                                                }
                                        }
                                }

                                // Segment 3: Liste des aliments de la ration
                                // sélectionnée
                                if (selectedRation != null) {
                                        SectionAlimentsRation(
                                                selectedRation = selectedRation,
                                                referenceUtilisee = referenceUtilisee,
                                                besoinEnergetiqueTotal =
                                                        besoinEnergetiqueTotal,
                                                besoinEnergetiqueStandard =
                                                        besoinEnergetiqueStandard,
                                                viewModel = viewModel,
                                                equationRepository = equationRepository,
                                                onAddAliment = {
                                                        rationForAddAliment =
                                                                selectedRation
                                                        showAddAlimentView = true
                                                },
                                                onMultiNutrientAdjustment = {
                                                        showMultiNutrientAdjustmentDialog =
                                                                true
                                                },
                                                onOpenRecipeDialog = {
                                                        showRecipeDialog = true
                                                },
                                                onSaveRecipe = {
                                                        selectedRation?.let { r ->
                                                                newRecipeName = r.name
                                                                showSaveRecipeDialog = true
                                                        }
                                                },
                                                isExamMode = isExamMode,
                                                showSnackbar = showSnackbar,
                                                isReadOnly = analyseGroupee,
                                                modifier =
                                                        Modifier.weight(1f).fillMaxWidth()
                                        )
                                } else {
                                        Card(
                                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                                elevation = AppSizes.elevationMedium
                                        ) {
                                                Box(
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentAlignment = Alignment.Center
                                                ) {
                                                        Text(
                                                                translate(RationKeys.SELECT_RATION_HINT),
                                                                style = MaterialTheme.typography.body1,
                                                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                                        )
                                                }
                                        }
                                }
                        }

                        // Colonne droite (analyses) - 50% de l'espace
                        Column(
                                modifier = Modifier.weight(0.5f),
                                verticalArrangement =
                                        Arrangement.spacedBy(AppSizes.paddingMedium)
                        ) {

                                // Analyse nutritionnelle de la ration
                                // sélectionnée
                                if (selectedRation != null) {
                                        if (showRationNutrientInspector) {
                                                AnalyseQuantitativeRationSection(
                                                        ration = selectedRation!!,
                                                        referenceUtilisee = referenceUtilisee,
                                                        equationRepository = equationRepository,
                                                        nutrimentsSelectionnes =
                                                                nutrimentsSelectionnesPreferences,
                                                        energieTotaleKcal = energieApportee,
                                                        isLargeView = true,
                                                        onContextBadgeClick = {
                                                                showRationNutrientInspector =
                                                                        false
                                                        },
                                                        modifier = Modifier.fillMaxSize()
                                                )
                                        } else if (troupeauEntier) {
                                                AnalyseApportsTroupeau(
                                                        analyse = analyseTroupeau,
                                                        lignes = apportsTroupeauEntier,
                                                        modifier = Modifier.fillMaxSize(),
                                                        defilementVertical = true
                                                )
                                        } else if (referenceUtilisee != null) {
                                                AnalyseNutritionnelleCard(
                                                        ration = selectedRation!!,
                                                        poidsMetabolique = poidsMetabolique,
                                                        referenceUtilisee =
                                                                referenceUtilisee,
                                                        besoinEnergetiqueEntretien =
                                                                besoinEnergetiqueStandard,
                                                        besoinEnergetiqueCible =
                                                                besoinEnergetiqueTotal,
                                                        poidsAnimal = poidsEffectif,
                                                        modifier = Modifier.fillMaxWidth(),
                                                        nutrimentsSelectionnes =
                                                                nutrimentsSelectionnesPreferences,
                                                        onNutrimentClick = {
                                                                nom,
                                                                valeurNutritionnelle ->
                                                                selectedNutrimentData =
                                                                        selectedRation
                                                                                .orUserError("aucune ration n'est sélectionnée")
                                                                                ?.let { ration -> Triple(nom, valeurNutritionnelle, ration) }
                                                                showNutrimentDetailDialog =
                                                                        true
                                                        },
                                                        animal = animal,
                                                        preferences =
                                                                preferencesApplication,
                                                        equationRepository =
                                                                equationRepository,
                                                        typeExpressionBesoin =
                                                                effectiveTypeExpressionBesoin,
                                                        headerActions =
                                                                typeExpressionSelector,
                                                        titleOverride = "",
                                                        contextBadgeLabel =
                                                                translate(
                                                                        AnalNut
                                                                                .BADGE_INTAKES
                                                                ),
                                                        contextBadgeColor =
                                                                Color(0xFF3558A8),
                                                        contextBadgeOnClick = {
                                                                showRationNutrientInspector =
                                                                        true
                                                        },
                                                        contextBadgeTooltip =
                                                                translate(
                                                                        AnalNut
                                                                                .TOOLTIP_SWITCH_TO_COMPOSITION
                                                                ),
                                                        showDisplayModeText = false,
                                                        isLargeView = true,
                                                        referencesMaladies =
                                                                referencesMaladiesResolues
                                                )
                                        } else {
                                                Card(
                                                        modifier = Modifier.fillMaxSize(),
                                                        elevation = AppSizes.elevationMedium
                                                ) {
                                                        Box(
                                                                modifier =
                                                                        Modifier.fillMaxSize(),
                                                                contentAlignment =
                                                                        Alignment.Center
                                                        ) {
                                                                Text(
                                                                        translate(AnalNut.SELECT_REFERENCE_FOR_ANALYSIS),
                                                                        style = MaterialTheme.typography.body1,
                                                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                                                )
                                                        }
                                                }
                                        }
                                } else {
                                        Card(
                                                modifier = Modifier.fillMaxSize(),
                                                elevation = AppSizes.elevationMedium
                                        ) {
                                                Box(
                                                        modifier =
                                                                Modifier.fillMaxSize(),
                                                        contentAlignment =
                                                                Alignment.Center
                                                ) {
                                                        Text(
                                                                translate(AnalNut.SELECT_RATION_FOR_ANALYSIS)
                                                        )
                                                }
                                        }
                                }
                        }
                }
        }
}

/**
 * Dialogues ouverts et éléments sélectionnés de l'onglet Rations. Un seul objet partagé par
 * [RationsView] et ses sous-composables, au lieu de dix-sept paramètres MutableState.
 */
@Stable
internal class RationsDialogState {
    val showCoefficientsDialog = mutableStateOf(false)
    val showMetabolicValuesDialog = mutableStateOf(false)
    val showCopyRationDialog = mutableStateOf(false)
    val parentNouvelleEtape = mutableStateOf<Ration?>(null)
    val etapeAEditer = mutableStateOf<Ration?>(null)
    val parentSynthese = mutableStateOf<Ration?>(null)
    val showRationEditDialog = mutableStateOf(false)
    val showAddAlimentView = mutableStateOf(false)
    val rationToEdit = mutableStateOf<Ration?>(null)
    val rationForAddAliment = mutableStateOf<Ration?>(null)
    val showNutrimentDetailDialog = mutableStateOf(false)
    val selectedNutrimentData = mutableStateOf<Triple<String, ValeurNutritionnelle, Ration>?>(null)
    val showRationNutrientInspector = mutableStateOf(false)
    val showMultiNutrientAdjustmentDialog = mutableStateOf(false)
    val showRecipeDialog = mutableStateOf(false)
    val showSaveRecipeDialog = mutableStateOf(false)
    val newRecipeName = mutableStateOf("")
    val showDeleteRationDialog = mutableStateOf(false)
    val rationToDelete = mutableStateOf<Ration?>(null)
}
