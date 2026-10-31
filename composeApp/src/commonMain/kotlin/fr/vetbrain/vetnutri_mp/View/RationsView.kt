package fr.vetbrain.vetnutri_mp.View

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

// Suppression du calcul local de DE : on utilisera EquationEvaluator avec nutriments
// complémentaires

/**
 * Rations d'un animal.
 * - S'appuie sur `AnimalDetailViewModel` pour les consultations/rations/ref. nutritionnelles.
 * - Calcule BE/K/énergie additionnelle et affiche les rations + analyses associées.
 * - Gère export/partage et feedback via `showSnackbar`.
 */
@Composable
fun RationsView(
        viewModel: AnimalDetailViewModel,
        showSnackbar: (String) -> Unit,
        modifier: Modifier = Modifier,
        equationRepository: EquationRepository,
        recipeRepository: RecipeRepository,
        isExamMode: Boolean = false
) {
        val animal by viewModel.animal.collectAsState()
        val selectedConsultation by viewModel.selectedConsultation.collectAsState()
        // Troupeau : consultation vue depuis un animal du type analysé (poids, référentiel, K du
        // type), utilisée pour les besoins et l'analyse ; identique à selectedConsultation sinon
        val consultationAnalysee by viewModel.consultationAnalysee.collectAsState()
        val typeTroupeauAnalyse by viewModel.typeTroupeauAnalyse.collectAsState()
        val analyseTroupeau by viewModel.analyseTroupeau.collectAsState()
        val apportsTroupeauEntier by viewModel.apportsTroupeauEntier.collectAsState()
        // Troupeau analysé dans son ensemble : pas de besoins d'un individu (poids, référentiel,
        // K), mais l'analyse des apports au troupeau entier
        val troupeauEntier = animal?.estTroupeau == true && typeTroupeauAnalyse == null
        var showCopyRationDialog by remember { mutableStateOf(false) }
        if (showCopyRationDialog && !isExamMode) {
                CopyRationDialog(
                        consultations = animal?.consultations.orEmpty().filter {
                                it.uuid != selectedConsultation?.uuid && it.idAnim == animal?.uuid
                        },
                        onDismiss = { showCopyRationDialog = false },
                        onCopy = { consultation, ration ->
                                if (viewModel.copyRationFromConsultation(consultation.uuid, ration.uuid)) {
                                        showCopyRationDialog = false
                                        showSnackbar("Ration reprise dans la consultation courante")
                                } else {
                                        showSnackbar("Impossible de reprendre cette ration")
                                }
                        }
                )
        }
        val availableReferences by viewModel.availableReferences.collectAsState()
        val selectedRation by viewModel.selectedRation.collectAsState()
        val rationAnalysisScope by viewModel.rationAnalysisScope.collectAsState()

        // Mode d'analyse groupée : la ration analysée n'est plus une ration de la consultation mais
        // la moyenne de toutes les rations actuelles (ou de toutes les proposées), pondérée par le
        // coefficient de chaque ration. Sa composition est donc en lecture seule.
        // Troupeau : la ration d'un animal d'un type est elle aussi virtuelle (lecture seule)
        val analyseGroupee = rationAnalysisScope.estGroupe || typeTroupeauAnalyse != null
        val rationsActuelles =
                remember(selectedConsultation) {
                        RationAggregator.rationsDuGroupe(
                                selectedConsultation,
                                RationAnalysisScope.GROUPE_ACTUELLES
                        )
                }
        val rationsProposees =
                remember(selectedConsultation) {
                        RationAggregator.rationsDuGroupe(
                                selectedConsultation,
                                RationAnalysisScope.GROUPE_PROPOSEES
                        )
                }

        // Rations principales, chacune suivie des étapes de son plan évolutif (triées par poids)
        val rationsAffichees =
                remember(selectedConsultation) {
                        val consultation = selectedConsultation
                        consultation?.let { c ->
                                PlanEvolutif.rationsPrincipales(c).sortedForDisplay().flatMap { parent ->
                                        listOf(parent) +
                                                PlanEvolutif.planTrie(c, parent).filter {
                                                        it.uuid != parent.uuid
                                                }
                                }
                        } ?: emptyList()
                }
        // Plan évolutif : dialogues d'étape (création / édition) et de synthèse
        var parentNouvelleEtape by remember { mutableStateOf<Ration?>(null) }
        var etapeAEditer by remember { mutableStateOf<Ration?>(null) }
        var parentSynthese by remember { mutableStateOf<Ration?>(null) }
        val bilansEtapes by viewModel.bilansEtapes.collectAsState()
        // Étape du plan évolutif actuellement sélectionnée (null hors plan)
        val etapeSelectionnee =
                remember(selectedConsultation, selectedRation?.uuid) {
                        val consultation = selectedConsultation
                        consultation?.rations
                                ?.firstOrNull { it.uuid == selectedRation?.uuid }
                                ?.takeIf { VariablesEtape.estEtape(consultation, it) }
                }

        // Résolution centralisée des références maladies sélectionnées + logs
        val referencesMaladiesResolues =
                remember(selectedConsultation?.referencesMaladies, availableReferences) {
                        val ids = selectedConsultation?.referencesMaladies ?: emptyList()

                        val resolved =
                                ids.mapNotNull { id ->
                                        availableReferences.firstOrNull { it.uuid == id }
                                }
                        if (resolved.isEmpty()) {
                                if (ids.isNotEmpty()) {} else {}
                        } else {}
                        resolved
                }

        // Récupération des valeurs métaboliques calculées
        val poidsMetabolique by viewModel.poidsMetabolique.collectAsState()
        // Poids utilisé pour les besoins (poids de l'étape pour un plan évolutif)
        val poidsEffectif by viewModel.poidsEffectif.collectAsState()
        val besoinEnergetiqueStandard by viewModel.besoinEnergetiqueStandard.collectAsState()
        // Le BE final devient la seule valeur de BE utilisée dans la vue
        val referenceUtilisee by viewModel.referenceUtilisee.collectAsState()
        // Variables propres aux étapes : celles des équations d'énergie (et complémentaires)
        val variablesEtapeRequises =
                remember(referenceUtilisee, referencesMaladiesResolues) {
                        fr.vetbrain.vetnutri_mp.Utils.VariablesEnergie.variablesEnergieRequises(
                                referenceUtilisee,
                                referencesMaladiesResolues
                        )
                }

        // L'énergie apportée sera calculée plus bas après chargement des préférences
        var energieApportee by remember { mutableStateOf(0.0) }

        // Calcul du K calculé (produit de tous les coefficients K + coefficient d'ajustement)
        val kCalcule =
                remember(consultationAnalysee) {
                        RationAnalysisCalculations.calculerCoefficientGlobal(consultationAnalysee)
                }

        // Préférences partagées (filtrage des nutriments, mode d'affichage), chargées par le ViewModel
        val preferencesApplication by viewModel.preferences.collectAsState()

        // Énergie additionnelle issue des références maladies (calculée dans le ViewModel)
        val energieAdditionnelle by viewModel.energieAdditionnelle.collectAsState()

        // Calcul du BE après K et du BE total final (K déjà inclus dans besoinEnergetiqueTotal du VM)
        val beApresK = remember(besoinEnergetiqueStandard, kCalcule) {
                RationAnalysisCalculations.calculerBesoinApresK(
                        besoinEnergetiqueStandard,
                        kCalcule
                )
        }
        val besoinEnergetiqueTotal = remember(beApresK, energieAdditionnelle) {
                RationAnalysisCalculations.calculerBesoinTotal(
                        beApresK,
                        energieAdditionnelle
                )
        }

        // Délégue le calcul d'énergie additionnelle au ViewModel quand les dépendances changent
        LaunchedEffect(
                consultationAnalysee,
                referencesMaladiesResolues,
                referenceUtilisee,
                selectedRation,
                // Plan évolutif : relancer une fois le BEE de l'étape recalculé
                besoinEnergetiqueStandard,
                poidsMetabolique
        ) {
                val consultation = consultationAnalysee
                val ration = selectedRation
                if (consultation != null && ration != null) {
                        viewModel.updateEnergieAdditionnelle(
                                referencesMaladies = referencesMaladiesResolues,
                                consultation = consultation,
                                ration = ration,
                                referenceEv = referenceUtilisee
                        )
                }
        }

        // Calcul du pourcentage de couverture avec le BE total final
        val pourcentageCouverture =
                remember(energieApportee, besoinEnergetiqueTotal) {
                        RationAnalysisCalculations.calculerPourcentageCouverture(
                                energieApportee,
                                besoinEnergetiqueTotal
                        )
                }

        // Calcul du K Observé avec le besoin énergétique de référence
        val kObserve =
                remember(energieApportee, besoinEnergetiqueStandard) {
                        RationAnalysisCalculations.calculerCoefficientObserve(
                                energieApportee,
                                besoinEnergetiqueStandard
                        )
                }

        // Calcul de l'énergie totale apportée par la ration sélectionnée (avec nutriments
        // complémentaires)
        val energieKey = listOf(selectedRation, referenceUtilisee, preferencesApplication, animal)
        LaunchedEffect(energieKey) {
                val rationActuelle = selectedRation
                val reference = referenceUtilisee
                val prefsApp = preferencesApplication
                val animalActuel = animal

                energieApportee =
                        if (rationActuelle != null &&
                                        reference != null &&
                                        prefsApp != null &&
                                        animalActuel != null
                        ) {
                                val prefsEspece =
                                        prefsApp.getPreferencesEspece(animalActuel.getEspece())
                                // Utiliser getDensiteEnergetiqueMoyenne() qui utilise les nouvelles
                                // fonctions d'énergie
                                // et multiplier par la quantité totale pour obtenir l'énergie
                                // totale
                                val densiteEnergetique =
                                        rationActuelle.getDensiteEnergetiqueMoyenne(
                                                referenceEv = reference,
                                                equationRepository = equationRepository
                                        )
                                densiteEnergetique * rationActuelle.getQuantiteTotale()
                        } else 0.0
        }

        // États pour les dialogues et navigation
        var showRationEditDialog by remember { mutableStateOf(false) }
        var showAddAlimentView by remember { mutableStateOf(false) }
        var rationToEdit by remember { mutableStateOf<Ration?>(null) }
        var rationForAddAliment by remember { mutableStateOf<Ration?>(null) }
        var editingAlimentId by remember { mutableStateOf<String?>(null) }

        // Scope pour les coroutines locales dans le composable
        val coroutineScope = rememberCoroutineScope()
        val focusManager = LocalFocusManager.current

        // États pour les dialogues de section agrandie
        var showMetabolicValuesDialog by remember { mutableStateOf(false) }
        var showCoefficientsDialog by remember { mutableStateOf(false) }

        // États pour le dialog détaillé de nutriment
        var showNutrimentDetailDialog by remember { mutableStateOf(false) }
        var selectedNutrimentData by remember {
                mutableStateOf<Triple<String, ValeurNutritionnelle, Ration>?>(null)
        }

        // Sélection locale (non sauvegardée) du type d'expression des besoins
        val typeExpressionBesoinFromPrefs by viewModel.typeExpressionBesoin.collectAsState()
        val typeExpressionBesoinOverride by viewModel.typeExpressionBesoinOverride.collectAsState()
        var showTypeExpressionMenu by remember { mutableStateOf(false) }
        val typeExpressionOptions = remember { TypeExpressionBesoin.getValidUnits() }

        val effectiveTypeExpressionBesoin =
                typeExpressionBesoinOverride
                        ?: typeExpressionBesoinFromPrefs
                        ?: TypeExpressionBesoin.DEFAULT
        var showRationNutrientInspector by remember { mutableStateOf(false) }

        val nutrimentsSelectionnesPreferences =
                remember(animal, preferencesApplication) {
                        val animalActuel = animal
                        val prefsApp = preferencesApplication
                        if (animalActuel != null && prefsApp != null) {
                                val preferencesEspece =
                                        prefsApp.getPreferencesEspece(animalActuel.getEspece())
                                val nutrimentsLabels =
                                        convertirPreferencesVersLabelsNutriments(preferencesEspece)
                                if (nutrimentsLabels.isNotEmpty()) {
                                        nutrimentsLabels
                                } else {
                                        listOf(
                                                "PROTEINE",
                                                "LIPIDE",
                                                "ENA",
                                                "CELLULOSE",
                                                "CENDRE",
                                                "CAL",
                                                "PHOS"
                                        )
                                }
                        } else {
                                listOf(
                                        "PROTEINE",
                                        "LIPIDE",
                                        "ENA",
                                        "CELLULOSE",
                                        "CENDRE",
                                        "CAL",
                                        "PHOS",
                                        "FE",
                                        "ZN",
                                        "CU",
                                        "MN",
                                        "I",
                                        "SE",
                                        "NA",
                                        "K",
                                        "MG",
                                        "CHL",
                                        "VITA",
                                        "VITD",
                                        "VITE",
                                        "VITB1",
                                        "VITB2",
                                        "VITB3",
                                        "VITB5",
                                        "VITB6",
                                        "VITB8",
                                        "VITB9",
                                        "VITB12",
                                        "O3",
                                        "O6",
                                        "AG205",
                                        "AG226",
                                        "EPADHA",
                                        "AG60",
                                        "AG80",
                                        "AG100",
                                        "LYSINE",
                                        "METHIONINE",
                                        "TRYPTOPHANE",
                                        "CAP",
                                        "O6O3",
                                        "KNA",
                                        "ZNCU",
                                        "TAURINE",
                                        "CARNITINE"
                                )
                        }
                }

        fun formatNutrientNameForDisplay(nutrientCode: String): String {
                val nutrient = NutrientResolver.resolveStoredLabel(nutrientCode)
                return if (nutrient is CustomNutrient) nutrient.nameToString() else nutrientCode
        }

        val typeExpressionSelector: @Composable RowScope.() -> Unit = {
                Box {
                        Row(
                                modifier = Modifier
                                        .heightIn(min = AppSizes.iconSizeXSmall)
                                        .clickable(role = androidx.compose.ui.semantics.Role.Button) {
                                                showTypeExpressionMenu = true
                                        }
                                        .padding(horizontal = AppSizes.paddingXSmall),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingXSmall)
                        ) {
                                Text(
                                        text =
                                                effectiveTypeExpressionBesoin.unitReqEnum
                                                        .translateEnum(),
                                        style = MaterialTheme.typography.caption,
                                        color = VetNutriColors.Primary
                                )
                                Icon(
                                        imageVector = Icons.Filled.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = VetNutriColors.Primary,
                                        modifier = Modifier.size(AppSizes.iconSizeXSmall)
                                )
                        }
                        DropdownMenu(
                                expanded = showTypeExpressionMenu,
                                onDismissRequest = { showTypeExpressionMenu = false }
                        ) {
                                typeExpressionOptions.forEach { type ->
                                        DropdownMenuItem(
                                                onClick = {
                                                        viewModel.setTypeExpressionBesoinOverride(
                                                                type
                                                        )
                                                        showTypeExpressionMenu = false
                                                }
                                        ) {
                                                Row(
                                                        verticalAlignment =
                                                                Alignment.CenterVertically,
                                                        horizontalArrangement =
                                                                Arrangement.spacedBy(
                                                                        AppSizes.paddingXSmall
                                                                )
                                                ) {
                                                        Text(
                                                                text =
                                                                        type.unitReqEnum
                                                                                .translateEnum(),
                                                                style =
                                                                        MaterialTheme.typography.body2
                                                        )
                                                        if (type == effectiveTypeExpressionBesoin) {
                                                                Icon(
                                                                        imageVector =
                                                                                Icons.Filled.Check,
                                                                        contentDescription = null,
                                                                        tint =
                                                                                VetNutriColors
                                                                                        .Primary,
                                                                        modifier =
                                                                                Modifier.size(16.dp)
                                                                )
                                                        }
                                                }
                                        }
                                }
                        }
                }

                if (selectedRation != null && referenceUtilisee != null) {
                        IconWithTooltip(
                                imageVector = Icons.Filled.Share,
                                contentDescription = translate("ration.exportAnalysisPdf"),
                                tooltip = translate("ration.exportAnalysisPdf"),
                                tint = VetNutriColors.Primary,
                                modifier = Modifier.size(AppSizes.iconSizeXSmall),
                                onClick = {
                                        val rationAExporter = selectedRation
                                        val animalActuel = animal
                                        if (rationAExporter != null && animalActuel != null) {
                                                val exportData =
                                                        fr.vetbrain.vetnutri_mp.Export.ExportData(
                                                                animal = animalActuel,
                                                                ration = rationAExporter,
                                                                reference = referenceUtilisee,
                                                                title =
                                                                        translate(AnimalDetail.RATION_ANALYSIS_TITLE) +
                                                                                rationAExporter.name.let { if (it.isNotBlank()) " - $it" else "" },
                                                                typeExpressionBesoin =
                                                                        effectiveTypeExpressionBesoin,
                                                                // Poids réel, ou poids de l'étape pour un plan évolutif
                                                                poidsAnimal =
                                                                        if (fr.vetbrain.vetnutri_mp.Data.VariablesEtape.estEtape(
                                                                                        selectedConsultation,
                                                                                        rationAExporter
                                                                                )
                                                                        ) poidsEffectif
                                                                        else consultationAnalysee?.weight,
                                                                poidsMetabolique =
                                                                        poidsMetabolique,
                                                                besoinEnergetiqueEntretien =
                                                                        besoinEnergetiqueStandard,
                                                                besoinEnergetiqueStandard =
                                                                        besoinEnergetiqueStandard,
                                                                besoinEnergetiqueTotal =
                                                                        besoinEnergetiqueTotal,
                                                                energieApportee = energieApportee,
                                                                energieAdditionnelle =
                                                                        energieAdditionnelle,
                                                                kCalcule = kCalcule,
                                                                kObserve = kObserve,
                                                                pourcentageCouverture =
                                                                        pourcentageCouverture,
                                                                equationRepository =
                                                                        equationRepository,
                                                                referencesMaladies =
                                                                        referencesMaladiesResolues
                                                        )
                                                coroutineScope.launch(
                                                        fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
                                                                .IO
                                                ) {
                                                        fr.vetbrain.vetnutri_mp.Export.PdfExporter
                                                                .exportDocument(
                                                                        fr.vetbrain.vetnutri_mp
                                                                                .Export
                                                                                .DocumentType
                                                                                .RATION_ANALYSIS,
                                                                        exportData,
                                                                        "analyse_ration_${animalActuel.nom}_${rationAExporter.name}.pdf"
                                                                )
                                                }
                                        }
                                }
                        )
                }
        }

        // État pour le dialog d'ajustement multi-nutriments
        var showMultiNutrientAdjustmentDialog by remember { mutableStateOf(false) }
        var showRecipeDialog by remember { mutableStateOf(false) }
        var showSaveRecipeDialog by remember { mutableStateOf(false) }
        var newRecipeName by remember { mutableStateOf("") }

        // État pour le dialog de confirmation de suppression de ration
        var showDeleteRationDialog by remember { mutableStateOf(false) }
        var rationToDelete by remember { mutableStateOf<Ration?>(null) }

        // Dialog de gestion des recettes
        if (!isExamMode && showRecipeDialog) {
                RecipeDialog(
                        repository = recipeRepository,
                        foodRepository = viewModel.foodRepository,
                        onApply = { recette ->
                                viewModel.applyRecipeToRation(recette)
                                showRecipeDialog = false
                        },
                        onClose = { showRecipeDialog = false }
                )
        }

        if (!isExamMode && showSaveRecipeDialog) {
                AlertDialog(
                        onDismissRequest = { showSaveRecipeDialog = false },
                        title = { Text(translate(RationKeys.CREATE_RECIPE)) },
                        text = {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                                value = newRecipeName,
                                                onValueChange = { newRecipeName = it },
                                                label = { Text(translate(RationKeys.RECIPE_NAME)) },
                                                modifier = Modifier.fillMaxWidth()
                                        )
                                        val especeLabel = selectedRation?.espece ?: ""
                                        if (especeLabel.isNotEmpty()) {
                                                Text(
                                                        "${translate(Animal.SPECIES)}: $especeLabel",
                                                        style = MaterialTheme.typography.body2
                                                )
                                        }
                                }
                        },
                        confirmButton = {
                                Button(
                                        onClick = {
                                                val ration = selectedRation
                                                if (ration != null && newRecipeName.isNotBlank()) {
                                                        coroutineScope.launch {
                                                                try {
                                                                        val recette =
                                                                                recipeRepository
                                                                                        .createRecipe(
                                                                                                name =
                                                                                                        newRecipeName,
                                                                                                espece =
                                                                                                        ration.espece,
                                                                                                description =
                                                                                                        null
                                                                                        )
                                                                        recipeRepository
                                                                                .replaceAliments(
                                                                                        recette.uuid,
                                                                                        ration.alimentMutableList
                                                                                )
                                                                        showSnackbar(
                                                                                "${translate(RationKeys.RECIPE_CREATED)} $newRecipeName"
                                                                        )
                                                                } catch (err: Exception) {
                                                                        err.rethrowIfCancellation()
                                                                        showSnackbar(
                                                                                "${translate(General.ERROR)}: ${err.message ?: translate(RationKeys.RECIPE_CREATE_ERROR_FALLBACK)}"
                                                                        )
                                                                } finally {
                                                                        showSaveRecipeDialog = false
                                                                }
                                                        }
                                                }
                                        }
                                ) { Text(translate(General.CREATE)) }
                        },
                        dismissButton = {
                                OutlinedButton(onClick = { showSaveRecipeDialog = false }) {
                                        Text(translate(General.CANCEL))
                                }
                        }
                )
        }

        // Afficher la vue d'ajout d'aliment si nécessaire
        if (showAddAlimentView && rationForAddAliment != null) {
                AddAlimentView(
                        viewModel = viewModel,
                        ration = rationForAddAliment!!,
                        onNavigateBack = {
                                showAddAlimentView = false
                                rationForAddAliment = null
                        },
                        equationRepository = equationRepository,
                        onAddAliment = { aliment, quantite ->
                                // Ajout asynchrone pour garantir la version complète de l'aliment
                                coroutineScope.launch {
                                        val alimentComplet =
                                                viewModel.getAlimentComplet(aliment.uuid)
                                        if (alimentComplet == null) {
                                                showSnackbar(
                                                        translate(RationKeys.FOOD_NOT_FOUND_ERROR)
                                                )
                                                return@launch
                                        }
                                        // La sélection a pu changer pendant le chargement de l'aliment
                                        val consultation =
                                                selectedConsultation.orUserError(
                                                        "aucune consultation n'est sélectionnée"
                                                ) ?: return@launch
                                        val ration =
                                                rationForAddAliment.orUserError(
                                                        "la ration n'est plus disponible"
                                                ) ?: return@launch
                                        val newAlimentRation =
                                                AlimentRation(
                                                        refAlimUnif = alimentComplet.uuid,
                                                        quantite = quantite,
                                                        aliment = alimentComplet,
                                                        refRation = ration.uuid
                                                )
                                        val updatedAliments =
                                                ration.alimentMutableList.toMutableList()
                                        updatedAliments.add(newAlimentRation)
                                        val updatedRation =
                                                ration.copy(alimentMutableList = updatedAliments)

                                        val updatedRations = consultation.rations.toMutableList()
                                        val rationIndex =
                                                updatedRations.indexOfFirst { it.uuid == ration.uuid }
                                        if (rationIndex < 0) {
                                                UserErrors.report(
                                                        "Action impossible : la ration « ${ration.name} » n'appartient plus à la consultation sélectionnée."
                                                )
                                                return@launch
                                        }
                                        updatedRations[rationIndex] = updatedRation
                                        viewModel.updateConsultation(
                                                consultation.copy(rations = updatedRations)
                                        )
                                        viewModel.selectRation(updatedRation)
                                        // Référence locale pour les ajouts suivants
                                        rationForAddAliment = updatedRation

                                        showSnackbar(
                                                "${translate(RationKeys.ADD_FOOD_TO_RATION)} '${alimentComplet.nom}' (${quantite}g)"
                                        )
                                        // Ne pas fermer la vue pour permettre l'ajout multiple
                                        // d'aliments
                                }
                        },
                        modifier = modifier
                )
        } else {
                val scrollState = rememberScrollState()

                Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
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
                                                                                            contentDescription = "Reprendre une ration",
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

                        // Contenu principal - grille 2x2 de cartes (colonnes fixes 50/50).
                        // Sans consultation, seules des cartes vides seraient affichées : le
                        // message de sélection suffit (et la grille serait illisible sur téléphone)
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
                                                                                    contentDescription = "Reprendre une ration",
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

                        // Afficher le dialogue d'édition de ration si nécessaire
                        // Plan évolutif : nouvelle étape sous une ration
                        parentNouvelleEtape?.let { parent ->
                                selectedConsultation?.let { consultation ->
                                        EtapeEditDialog(
                                                titre = translate(Evolutive.NEW_STEP_TITLE_FORMAT, parent.name),
                                                consultation = consultation,
                                                etape = null,
                                                variablesRequises = variablesEtapeRequises,
                                                onDismiss = { parentNouvelleEtape = null },
                                                onSave = { poids, variables, libelle ->
                                                        parentNouvelleEtape = null
                                                        viewModel.ajouterEtape(parent, poids, variables, libelle)
                                                }
                                        )
                                }
                        }
                        // Plan évolutif : édition du poids et des variables d'une étape
                        etapeAEditer?.let { etape ->
                                selectedConsultation?.let { consultation ->
                                        EtapeEditDialog(
                                                titre = translate(Evolutive.EDIT_STEP_TITLE),
                                                consultation = consultation,
                                                etape = etape,
                                                variablesRequises = variablesEtapeRequises,
                                                onDismiss = { etapeAEditer = null },
                                                onSave = { poids, variables, libelle ->
                                                        etapeAEditer = null
                                                        viewModel.mettreAJourEtape(etape, poids, variables, libelle = libelle)
                                                }
                                        )
                                }
                        }
                        // Plan évolutif : synthèse ingrédients × étapes
                        parentSynthese?.let { parent ->
                                selectedConsultation?.let { consultation ->
                                        SynthesePlanDialog(
                                                consultation = consultation,
                                                parent = consultation.rations.firstOrNull { it.uuid == parent.uuid } ?: parent,
                                                bilans = bilansEtapes,
                                                onQuantite = { etape, ref, quantite ->
                                                        viewModel.mettreAJourQuantiteEtape(etape, ref, quantite)
                                                },
                                                onPropager = {
                                                        val source = consultation.rations.firstOrNull { it.uuid == parent.uuid } ?: parent
                                                        val ajouts = viewModel.propagerAlimentsEtape(source)
                                                        showSnackbar(translate(Evolutive.PROPAGATE_DONE, ajouts.toString()))
                                                },
                                                onDismiss = { parentSynthese = null }
                                        )
                                }
                        }

                        if (showRationEditDialog) {
                                RationEditDialog(
                                        ration = rationToEdit,
                                        onDismiss = {
                                                showRationEditDialog = false
                                                rationToEdit = null
                                        },
                                        onSave = { updatedRation ->
                                                if (rationToEdit == null) {
                                                        // Création d'une nouvelle ration
                                                        // Assurer que la ration est liée à
                                                        // la
                                                        // consultation
                                                        val newRation =
                                                                updatedRation.copy(
                                                                        idConsult =
                                                                                selectedConsultation
                                                                                        ?.uuid
                                                                                        ?: ""
                                                                )

                                                        selectedConsultation?.let { consultation ->
                                                                // Créer une copie de la
                                                                // liste des
                                                                // rations et y ajouter la
                                                                // nouvelle
                                                                // ration
                                                                val updatedRations =
                                                                        consultation.rations
                                                                                .toMutableList()
                                                                updatedRations.add(newRation)

                                                                // Mettre à jour la
                                                                // consultation
                                                                // avec la nouvelle liste de
                                                                // rations
                                                                val updatedConsultation =
                                                                        consultation.copy(
                                                                                rations =
                                                                                        updatedRations
                                                                        )

                                                                // Sauvegarder la
                                                                // consultation mise
                                                                // à jour
                                                                viewModel.updateConsultation(
                                                                        updatedConsultation
                                                                )

                                                                // Sélectionner la nouvelle
                                                                // ration
                                                                viewModel.selectRation(newRation)

                                                                showSnackbar(
                                                                        translate(
                                                                                RationKeys.CREATED,
                                                                                newRation.name
                                                                        )
                                                                )
                                                        }
                                                } else {
                                                        // Mise à jour d'une ration
                                                        // existante
                                                        viewModel.updateRation(updatedRation)
                                                        showSnackbar(
                                                                translate(
                                                                        RationKeys.UPDATED,
                                                                        updatedRation.name
                                                                )
                                                        )
                                                }

                                                showRationEditDialog = false
                                                rationToEdit = null
                                        }
                                )
                        }

                        // Dialogues d'agrandissement des sections
                        if (showMetabolicValuesDialog) {
                                MetabolicValuesDialog(
                                        selectedConsultation = consultationAnalysee,
                                        poidsCalcul = poidsEffectif,
                                        poidsMetabolique = poidsMetabolique,
                                        besoinEnergetiqueStandard = besoinEnergetiqueStandard,
                                        besoinEnergetiqueTotal = besoinEnergetiqueTotal,
                                        kObserve = kObserve,
                                        kCalcule = kCalcule,
                                        referenceUtilisee = referenceUtilisee,
                                        onDismiss = { showMetabolicValuesDialog = false }
                                )
                        }

                        if (showCoefficientsDialog) {
                                CoefficientsDialog(
                                        selectedConsultation = consultationAnalysee,
                                        viewModel = viewModel,
                                        onDismiss = { showCoefficientsDialog = false }
                                )
                        }
                }

                // Dialog détaillé de nutriment
                if (showNutrimentDetailDialog && selectedNutrimentData != null) {
                        val (nom, valeurNutritionnelle, ration) = selectedNutrimentData!!
                        NutrientDetailDialog(
                                nom = nom,
                                valeurNutritionnelle = valeurNutritionnelle,
                                ration = ration,
                                poidsMetabolique = poidsMetabolique,
                                referenceUtilisee = referenceUtilisee,
                                besoinEnergetiqueEntretien = besoinEnergetiqueStandard,
                                besoinEnergetiqueCible = besoinEnergetiqueTotal,
                                poidsAnimal = poidsEffectif,
                                espece = animal?.getEspece() ?: Espece.CHIEN,
                                preferences = preferencesApplication,
                                equationRepository = equationRepository,
                                referencesMaladies = referencesMaladiesResolues,
                                onDismiss = {
                                        showNutrimentDetailDialog = false
                                        selectedNutrimentData = null
                                }
                        )
                }

                // Dialog d'ajustement multi-nutriments
                if (showMultiNutrientAdjustmentDialog &&
                                selectedRation != null &&
                                referenceUtilisee != null &&
                                besoinEnergetiqueTotal != null &&
                                // Lu plus bas avec !! : sans ce test, plantage tant que le BEE
                                // n'est pas calculé
                                besoinEnergetiqueStandard != null
                ) {
                        val ration = selectedRation!!
                        val reference = referenceUtilisee!!
                        val besoinEnergetique = besoinEnergetiqueTotal!!

                        MultiNutrientAdjustmentView(
                                ration = ration,
                                referenceUtilisee = reference,
                                besoinEnergetiqueTotal = besoinEnergetique,
                                besoinEnergetiqueStandard = besoinEnergetiqueStandard!!,
                                poidsAnimal = poidsEffectif,
                                poidsMetabolique = poidsMetabolique,
                                equationRepository = equationRepository,
                                onConfirm = { result ->
                                        if (result.success) {
                                                // Appliquer les ajustements à la ration
                                                result.adjustedAliments?.let { adjustedAliments ->
                                                        coroutineScope.launch {
                                                                viewModel.updateRationAliments(
                                                                        ration,
                                                                        adjustedAliments
                                                                )
                                                                showSnackbar(result.message)
                                                        }
                                                }
                                        } else {
                                                showSnackbar(
                                                        "${translate(General.ERROR)}: ${result.message}"
                                                )
                                        }
                                        showMultiNutrientAdjustmentDialog = false
                                },
                                onDismiss = { showMultiNutrientAdjustmentDialog = false }
                        )
                }

                // Dialog de confirmation de suppression de ration
                if (showDeleteRationDialog) {
                        val rationCible = rationToDelete
                        if (rationCible != null) {
                                AlertDialog(
                                        onDismissRequest = {
                                                showDeleteRationDialog = false
                                                rationToDelete = null
                                        },
                                        title = {
                                                Text(
                                                        translate(RationKeys.DELETE_CONFIRM_TITLE),
                                                        style = MaterialTheme.typography.h6,
                                                        color = VetNutriColors.Error
                                                )
                                        },
                                        text = {
                                                Text(
                                                        translate(
                                                                RationKeys.DELETE_CONFIRM_MESSAGE,
                                                                rationCible.name
                                                        ),
                                                        style = MaterialTheme.typography.body1
                                                )
                                        },
                                        confirmButton = {
                                                Button(
                                                        onClick = {
                                                                viewModel.removeRationFromConsultation(
                                                                        rationCible
                                                                )
                                                                showSnackbar(
                                                                        translate(
                                                                                RationKeys.DELETED,
                                                                                rationCible.name
                                                                        )
                                                                )
                                                                showDeleteRationDialog = false
                                                                rationToDelete = null
                                                        },
                                                        colors =
                                                                ButtonDefaults.buttonColors(
                                                                        backgroundColor =
                                                                                VetNutriColors.Error,
                                                                        contentColor =
                                                                                VetNutriColors.OnError
                                                                )
                                                ) { Text(translate(General.DELETE)) }
                                        },
                                        dismissButton = {
                                                TextButton(
                                                        onClick = {
                                                                showDeleteRationDialog = false
                                                                rationToDelete = null
                                                        }
                                                ) { Text(translate(General.CANCEL)) }
                                        }
                                )
                        }
                }
        }
}
