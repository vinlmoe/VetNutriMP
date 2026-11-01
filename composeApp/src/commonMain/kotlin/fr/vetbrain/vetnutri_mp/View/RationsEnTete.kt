package fr.vetbrain.vetnutri_mp.View

import fr.vetbrain.vetnutri_mp.Enumer.CustomNutrient
import fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver
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

/** Onglet Rations : en-tête de la consultation (valeurs métaboliques, coefficients, bilan énergétique, panneau troupeau) */
@Composable
internal fun ColumnScope.EnTeteRations(
        dialogues: RationsDialogState,
        analyseGroupee: Boolean,
        analyseTroupeau: AnalyseTroupeau?,
        animal: AnimalEv?,
        apportsTroupeauEntier: List<LigneApportTroupeau>,
        besoinEnergetiqueStandard: Double?,
        besoinEnergetiqueTotal: Double?,
        consultationAnalysee: ConsultationEv?,
        effectiveTypeExpressionBesoin: TypeExpressionBesoin,
        energieAdditionnelle: Double,
        energieApportee: Double,
        equationRepository: EquationRepository,
        etapeSelectionnee: Ration?,
        focusManager: FocusManager,
        isExamMode: Boolean,
        kCalcule: Double,
        kObserve: Double,
        modifier: Modifier,
        nutrimentsSelectionnesPreferences: List<String>,
        poidsMetabolique: Double?,
        pourcentageCouverture: Double,
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
        typeTroupeauAnalyse: String?,
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
        var showCoefficientsDialog by dialogues.showCoefficientsDialog
        var showMetabolicValuesDialog by dialogues.showMetabolicValuesDialog
        if (selectedConsultation == null) {
                Box(
                        modifier =
                                Modifier.fillMaxSize()
                                        .padding(AppSizes.paddingLarge),
                        contentAlignment = Alignment.Center
                ) {
                        Text(
                                translate(Consultation.SELECT_HINT),
                                textAlign = TextAlign.Center
                        )
                }
        } else {
                // En-tête avec nom de la consultation
                Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = AppSizes.elevationSmall
                ) {
                        Column(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .padding(AppSizes.paddingMedium),
                                verticalArrangement =
                                        Arrangement.spacedBy(AppSizes.paddingXSmall)
                        ) {
                                Text(
                                        text =
                                                "${translate(Consultation.OF_DATE)} " +
                                                        (selectedConsultation?.date
                                                                ?: ""),
                                        style =
                                                MaterialTheme.typography
                                                        .subtitle1, // taille
                                        // réduite
                                        color = VetNutriColors.Primary
                                )
                                if (!selectedConsultation?.objectConsult
                                                .isNullOrBlank()
                                ) {
                                        Text(
                                                text =
                                                        selectedConsultation
                                                                ?.objectConsult
                                                                ?: "",
                                                style =
                                                        MaterialTheme.typography
                                                                .body2,
                                                color =
                                                        MaterialTheme.colors
                                                                .onSurface.copy(
                                                                alpha = 0.7f
                                                        )
                                        )
                                }
                        }
                }
                Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                // Troupeau : paramètres de chaque type, répartition de la ration
                // du groupe et choix du type analysé
                if (animal?.estTroupeau == true) {
                        PanneauTroupeauConsultation(
                                viewModel = viewModel,
                                showSnackbar = showSnackbar,
                                modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                }
                // Section responsive dans une Card
                BoxWithConstraints(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .padding(AppSizes.paddingMedium)
                ) {
                        val isCompact = maxWidth < 600.dp // seuil abaissé à 600
                        if (isCompact) {
                                // Vue compacte : une seule colonne, scroll global
                                Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement =
                                                Arrangement.spacedBy(
                                                        AppSizes.paddingSmall
                                                )
                                ) {
                                        if (!troupeauEntier) {
                                        // Section 1: Valeurs métaboliques
                                        SectionValeursMetaboliques(
                                                selectedConsultation =
                                                        consultationAnalysee,
                                                poidsMetabolique = poidsMetabolique,
                                                besoinEnergetiqueStandard =
                                                        besoinEnergetiqueStandard,
                                                besoinEnergetiqueTotal =
                                                        besoinEnergetiqueTotal,
                                                kObserve = kObserve,
                                                kCalcule = kCalcule,
                                               energieAdditionnelle = energieAdditionnelle,
                                                referenceUtilisee = referenceUtilisee,
                                                onUpdateWeights = if (typeTroupeauAnalyse != null) null else { currentWeight: Double, idealWeight: Double ->
                                                        selectedConsultation?.let { consultation ->
                                                                viewModel.updateConsultationWeights(
                                                                        consultation.uuid,
                                                                        currentWeight,
                                                                        idealWeight
                                                                )
                                                        }
                                                },
                                                onExpand = {
                                                        showMetabolicValuesDialog =
                                                                true
                                                },
                                                // Étape de plan évolutif : poids de l'étape affiché et éditable
                                                estEtape = etapeSelectionnee != null,
                                                poidsEtape = etapeSelectionnee?.poids,
                                                onUpdatePoidsEtape = { poids, poidsReel ->
                                                        etapeSelectionnee?.let { etape ->
                                                                viewModel.mettreAJourEtape(etape, poids, etape.suppVarp, poidsReel)
                                                        }
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                        )
                                        Divider()

                                        // Section 2: Coefficients
                                        SectionCoefficients(
                                                selectedConsultation =
                                                        consultationAnalysee,
                                                showCoefficientsDialog = {
                                                        showCoefficientsDialog =
                                                                true
                                                },
                                                viewModel = viewModel,
                                                modifier = Modifier.fillMaxWidth()
                                        )
                                        Divider()

                                        // Section 3: Bilan énergétique
                                        SectionBilanEnergetique(
                                                energieApportee = energieApportee,
                                                pourcentageCouverture =
                                                        pourcentageCouverture,
                                                kObserve = kObserve,
                                                kCalcule = kCalcule,

                                                beFinal = besoinEnergetiqueTotal,
                                                modifier = Modifier.fillMaxWidth()
                                        )
                                        Divider()
                                        }

                                        // Section 4: Liste des rations
                                        Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                elevation =
                                                        AppSizes.elevationMedium,
                                                backgroundColor =
                                                        MaterialTheme.colors.surface
                                        ) {
                                                Column(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .padding(
                                                                                AppSizes.paddingMedium
                                                                        ),
                                                        verticalArrangement =
                                                                Arrangement
                                                                        .spacedBy(
                                                                                AppSizes.paddingSmall
                                                                        )
                                                ) {
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
                                                                Text(
                                                                        text =
                                                                                translate(RationKeys.CONSULTATION_RATIONS),
                                                                modifier = Modifier.weight(1f),
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .subtitle2,
                                                                        color =
                                                                                VetNutriColors
                                                                                        .Primary
                                                                )
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
                                                                                Modifier.fillMaxWidth()
                                                                )
                                                        } else {
                                                                Column(
                                                                        modifier =
                                                                                Modifier.fillMaxWidth(),
                                                                        verticalArrangement =
                                                                                Arrangement
                                                                                        .spacedBy(
                                                                                                8.dp
                                                                                        )
                                                                ) {
                                                                        GroupesRationsItems(
                                                                                rationsActuelles = rationsActuelles,
                                                                                rationsProposees = rationsProposees,
                                                                                scopeCourant = rationAnalysisScope,
                                                                                onSelectGroupe = { scope ->
                                                                                        focusManager.clearFocus(force = true)
                                                                                        viewModel.setRationAnalysisScope(scope)
                                                                                }
                                                                        )
                                                                        rationsAffichees
                                                                                ?.forEach {
                                                                                        ration
                                                                                        ->
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
                                        Divider()

                                        // Section 5: Liste des aliments de la
                                        // ration sélectionnée
                                        if (selectedRation != null) {
                                                SectionAlimentsRation(
                                                        selectedRation = selectedRation,
                                                        referenceUtilisee =
                                                                referenceUtilisee,
                                                        besoinEnergetiqueTotal =
                                                                besoinEnergetiqueTotal,
                                                        besoinEnergetiqueStandard =
                                                                besoinEnergetiqueStandard,
                                                        viewModel = viewModel,
                                                        equationRepository = equationRepository,
                                                        onAddAliment = {
                                                                rationForAddAliment =
                                                                        selectedRation
                                                                showAddAlimentView =
                                                                        true
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
                                                                        newRecipeName =
                                                                                r.name
                                                                        showSaveRecipeDialog =
                                                                                true
                                                                }
                                                        },
                                                        isExamMode = isExamMode,
                                                        showSnackbar = showSnackbar,
                                                        isCompact = isCompact,
                                                        isReadOnly = analyseGroupee,
                                                        modifier = Modifier.fillMaxWidth()
                                                )
                                        } else {
                                                Card(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        elevation = AppSizes.elevationMedium
                                                ) {
                                                        Box(
                                                                modifier = Modifier.fillMaxWidth().padding(AppSizes.paddingMedium),
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

                                        if (selectedRation != null && showRationNutrientInspector) {
                                                Divider()
                                                AnalyseQuantitativeRationSection(
                                                        ration = selectedRation!!,
                                                        referenceUtilisee = referenceUtilisee,
                                                        equationRepository = equationRepository,
                                                        nutrimentsSelectionnes =
                                                                nutrimentsSelectionnesPreferences,
                                                        energieTotaleKcal = energieApportee,
                                                        isLargeView = !isCompact,
                                                        onContextBadgeClick = {
                                                                showRationNutrientInspector =
                                                                        false
                                                        },
                                                        modifier = Modifier.fillMaxWidth()
                                                )
                                        }

                                        // Section 6: Analyse nutritionnelle (si une
                                        // ration est sélectionnée)
                                        if (selectedRation != null && !showRationNutrientInspector) {
                                                Divider()
                                                if (troupeauEntier) {
                                                        AnalyseApportsTroupeau(
                                                                analyse = analyseTroupeau,
                                                                lignes = apportsTroupeauEntier,
                                                                modifier = Modifier.fillMaxWidth()
                                                        )
                                                } else if (referenceUtilisee != null) {
                                                        // Utiliser la version existante de
                                                        // AnalyseNutritionnelleCard
                                                        AnalyseNutritionnelleCard(
                                                                ration = selectedRation!!,
                                                                poidsMetabolique =
                                                                        poidsMetabolique,
                                                                referenceUtilisee =
                                                                        referenceUtilisee,
                                                                besoinEnergetiqueEntretien =
                                                                        besoinEnergetiqueStandard,
                                                                besoinEnergetiqueCible =
                                                                        besoinEnergetiqueTotal,
                                                                poidsAnimal =
                                                                        consultationAnalysee
                                                                                ?.weight
                                                                                ?.toDouble(),
                                                                modifier =
                                                                        Modifier.fillMaxWidth(),
                                                                nutrimentsSelectionnes =
                                                                        nutrimentsSelectionnesPreferences,
                                                                onNutrimentClick = {
                                                                        nom,
                                                                        valeurNutritionnelle
                                                                        ->
                                                                        val displayName =
                                                                                formatNutrientNameForDisplay(
                                                                                        nom
                                                                                )
                                                                        selectedNutrimentData =
                                                                        selectedRation
                                                                                .orUserError("aucune ration n'est sélectionnée")
                                                                                ?.let { ration -> Triple(displayName, valeurNutritionnelle, ration) }
                                                                        showNutrimentDetailDialog =
                                                                                true
                                                                },
                                                                animal = animal,
                                                                preferences =
                                                                        preferencesApplication,
                                                                equationRepository =
                                                                        equationRepository,
                                                        // Utiliser les préférences
                                                        // pré-chargées du ViewModel
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
                                                        isLargeView = !isCompact,
                                                        referencesMaladies =
                                                                referencesMaladiesResolues
                                                )
                                                } else {
                                                        Card(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                elevation = AppSizes.elevationMedium
                                                        ) {
                                                                Box(
                                                                        modifier = Modifier.fillMaxWidth().padding(AppSizes.paddingMedium),
                                                                        contentAlignment = Alignment.Center
                                                                ) {
                                                                        Text(
                                                                                translate(AnalNut.SELECT_REFERENCE_FOR_ANALYSIS),
                                                                                style = MaterialTheme.typography.body1,
                                                                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                                                        )
                                                                }
                                                        }
                                                }
                                        }
                                }
                        } else {
                                if (selectedConsultation == null) {
                                        Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                        ) { 
                                                Text(translate(Consultation.SELECT_HINT))
                                        }
                                } else if (!troupeauEntier) {
                                        Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement =
                                                        Arrangement.spacedBy(
                                                                AppSizes.paddingMedium
                                                        )
                                        ) {
                                                SectionValeursMetaboliques(
                                                        selectedConsultation =
                                                                consultationAnalysee,
                                                        poidsMetabolique = poidsMetabolique,
                                                        besoinEnergetiqueStandard =
                                                                besoinEnergetiqueStandard,
                                                        besoinEnergetiqueTotal =
                                                                besoinEnergetiqueTotal,
                                                        kObserve = kObserve,
                                                        kCalcule = kCalcule,
                                                         energieAdditionnelle = energieAdditionnelle,
                                                        referenceUtilisee = referenceUtilisee,
                                                        onUpdateWeights = if (typeTroupeauAnalyse != null) null else { currentWeight: Double, idealWeight: Double ->
                                                                selectedConsultation?.let { consultation ->
                                                                        viewModel.updateConsultationWeights(
                                                                                consultation.uuid,
                                                                                currentWeight,
                                                                                idealWeight
                                                                        )
                                                                }
                                                        },
                                                        onExpand = {
                                                                showMetabolicValuesDialog =
                                                                        true
                                                        },
                                                        // Étape de plan évolutif : poids de l'étape affiché et éditable
                                                        estEtape = etapeSelectionnee != null,
                                                        poidsEtape = etapeSelectionnee?.poids,
                                                        onUpdatePoidsEtape = { poids, poidsReel ->
                                                                etapeSelectionnee?.let { etape ->
                                                                        viewModel.mettreAJourEtape(etape, poids, etape.suppVarp, poidsReel)
                                                                }
                                                        },
                                                        modifier = Modifier.weight(1f)
                                                )
                                                Divider(
                                                        modifier =
                                                                Modifier.width(1.dp)
                                                                        .fillMaxHeight()
                                                )
                                                SectionCoefficients(
                                                        selectedConsultation =
                                                                consultationAnalysee,
                                                        showCoefficientsDialog = {
                                                                showCoefficientsDialog =
                                                                        true
                                                        },
                                                        viewModel = viewModel,
                                                        modifier = Modifier.weight(1f)
                                                )
                                                Divider(
                                                        modifier =
                                                                Modifier.width(1.dp)
                                                                        .fillMaxHeight()
                                                )
                                                SectionBilanEnergetique(
                                                        energieApportee = energieApportee,
                                                        pourcentageCouverture =
                                                                pourcentageCouverture,
                                                        kObserve = kObserve,
                                                        kCalcule = kCalcule,

                                                        beFinal = besoinEnergetiqueTotal,
                                                        modifier = Modifier.weight(1f)
                                                )
                                        }
                                }
                        }
                }
                // ... le reste du contenu de la vue ...
        }
}

/** Libellé affiché d'un nutriment stocké (nom du nutriment personnalisé, sinon le code). */
private fun formatNutrientNameForDisplay(nutrientCode: String): String {
        val nutrient = NutrientResolver.resolveStoredLabel(nutrientCode)
        return if (nutrient is CustomNutrient) nutrient.nameToString() else nutrientCode
}
