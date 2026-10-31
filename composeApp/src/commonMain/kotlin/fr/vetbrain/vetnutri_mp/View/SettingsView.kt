package fr.vetbrain.vetnutri_mp.View

import fr.vetbrain.vetnutri_mp.Utils.rethrowIfCancellation
import fr.vetbrain.vetnutri_mp.Utils.AppLog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Repository.ExportImportRepository
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Components.IconButtonWithTooltip
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.Equation
import fr.vetbrain.vetnutri_mp.Data.FoodSearchFilters
import fr.vetbrain.vetnutri_mp.Data.Recette
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Export.HtmlSection
import fr.vetbrain.vetnutri_mp.Export.SectionCategory
import fr.vetbrain.vetnutri_mp.View.SettingsComponents.SettingsTabs
import fr.vetbrain.vetnutri_mp.View.SettingsSections.AdministrationSettings
import fr.vetbrain.vetnutri_mp.View.SettingsSections.InterfaceSettings
import fr.vetbrain.vetnutri_mp.View.SettingsSections.RecipeEditView
import fr.vetbrain.vetnutri_mp.ViewModel.ImportViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.RecipeEditViewModel
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel
import fr.vetbrain.vetnutri_mp.Service.ExcelFoodService
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Settings
import fr.vetbrain.vetnutri_mp.Localization.translate

// Import délégué via SettingsViewModel.importApiFromFileUI()

internal data class FilterOption<T>(val label: String, val value: T?)

/**
 * Dialogue simple pour les paramètres d'affichage
 * @param viewModel Le ViewModel des paramètres
 * @param onDismiss Callback appelé lorsque l'utilisateur ferme le dialogue
 */
@Composable
fun SettingsDialog(
        viewModel: SettingsViewModel,
        onDismiss: () -> Unit,
        onBackupClick: () -> Unit = {}
) {
        val uiScale by viewModel.uiScale.collectAsState()

        AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(translate(Settings.DISPLAY_SETTINGS), style = MaterialTheme.typography.h6) },
                text = {
                        Column(
                                modifier = Modifier.padding(AppSizes.paddingMedium),
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                        ) {
                                Text(
                                        translate(Settings.UI_SCALE),
                                        style = MaterialTheme.typography.subtitle1
                                )

                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Button(
                                                onClick = { viewModel.decrementUiScale() },
                                                enabled = uiScale > 0.5f,
                                                modifier = Modifier.size(AppSizes.buttonHeight)
                                        ) { Text("-") }

                                        Text(
                                                "${(uiScale * 100).roundToInt()}%",
                                                style = MaterialTheme.typography.body1
                                        )

                                        Button(
                                                onClick = { viewModel.incrementUiScale() },
                                                enabled = uiScale < 2f,
                                                modifier = Modifier.size(AppSizes.buttonHeight)
                                        ) { Text("+") }
                                }
                        }
                },
                confirmButton = {
                        Button(
                                onClick = onDismiss,
                                colors =
                                        ButtonDefaults.buttonColors(
                                                backgroundColor = VetNutriColors.Primary,
                                                contentColor = Color.White
                                        )
                        ) { Text(translate(Settings.CLOSE)) }
                },
                backgroundColor = MaterialTheme.colors.surface
        )
}

/**
 * Écran des paramètres.
 * - Regroupe les onglets Interface, Préférences et Import/Export.
 * - S'appuie sur `SettingsViewModel` pour l'état UI et `ImportViewModel` pour les imports.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SettingsView(
        viewModel: SettingsViewModel,
        importViewModel: ImportViewModel,
        onImportAnimals: () -> Unit,
        onBack: () -> Unit,
        onAnimalListRefresh: () -> Unit,
        onFoodListRefresh: () -> Unit,
        onShowCrossAnalysis: () -> Unit,
        modifier: Modifier = Modifier,
        onSpeciesClick: (fr.vetbrain.vetnutri_mp.Enumer.Espece) -> Unit = {},
        onBackupClick: () -> Unit = {},
        onLegacyMigrationClick: () -> Unit = {},
        onBulkBiblioClick: () -> Unit = {},
        isExamMode: Boolean = false
) {

        // État pour le dialogue d'alerte d'importation des références nutritionnelles
        var showImportDialog by remember { mutableStateOf(false) }
        var importDialogMessage by remember { mutableStateOf("") }

        val coroutineScope = rememberCoroutineScope()
        val uiScale by viewModel.uiScale.collectAsState()

        // État pour l'onglet actuel
        var selectedTab by remember { mutableStateOf(0) }

        // État pour le nombre de conseils
        var conseilsCount by remember { mutableStateOf(0) }

        // Observer le message d'importation des références nutritionnelles
        val nutritionalRequirementMessage by
                importViewModel.nutritionalRequirementImportResultMessage.collectAsState()

        // Afficher le dialogue d'alerte quand l'importation est terminée
        LaunchedEffect(nutritionalRequirementMessage) {
                nutritionalRequirementMessage?.let { message ->
                        // Afficher le dialogue si le message n'est pas vide et ne contient pas
                        // l'indicateur de progression
                        if (message.isNotEmpty() &&
                                        !message.contains("🔄") &&
                                        !message.contains("Sélection du fichier") &&
                                        (message.startsWith("✅") || message.startsWith("❌"))
                        ) {
                                importDialogMessage = message
                                showImportDialog = true
                        }
                }
        }

        // Charger le nombre de conseils au démarrage
        LaunchedEffect(Unit) {
                try {
                        val count =
                                viewModel.conseilRepository?.getConseilsCount()?.getOrThrow() ?: 0
                        conseilsCount = count
                } catch (e: Exception) {
                        e.rethrowIfCancellation()
                        // En cas d'erreur, garder 0
                        conseilsCount = 0
                }
        }

        Column(modifier = modifier.fillMaxSize()) {
                // En-tête avec bouton retour

                // Navigation par onglets
                SettingsTabs(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        isExamMode = isExamMode
                )

                // Contenu de l'onglet sélectionné
                Box(modifier = Modifier.fillMaxSize().padding(AppSizes.paddingMedium)) {
                        when (selectedTab) {
                                0 -> { // Interface
                                        Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalArrangement =
                                                        Arrangement.spacedBy(AppSizes.paddingSmall)
                                        ) {
                                                Button(
                                                        onClick = onShowCrossAnalysis,
                                                        colors =
                                                                ButtonDefaults.buttonColors(
                                                                        backgroundColor =
                                                                                VetNutriColors
                                                                                        .Primary,
                                                                        contentColor =
                                                                                VetNutriColors
                                                                                        .OnPrimary
                                                                ),
                                                        modifier = Modifier.fillMaxWidth()
                                                ) {
                                                        Text(
                                                                translate(
                                                                        LocalizationKeys
                                                                                .AnimalList
                                                                                .CROSS_ANALYSIS
                                                                )
                                                        )
                                                }
                                                InterfaceSettings(
                                                        viewModel = viewModel,
                                                        modifier = Modifier.fillMaxWidth()
                                                )
                                        }
                                }
                                1 -> { // Préférences
                                        PreferencesSection(
                                                preferencesRepository = viewModel.preferencesRepository,
                                                modifier = Modifier.fillMaxWidth(),
                                                onSpeciesClick = onSpeciesClick
                                        )
                                }
                                2 -> { // Importation
                                        // Affichage du nombre de conseils
                                        Card(
                                                modifier =
                                                        Modifier.fillMaxWidth()
                                                                .padding(bottom = 8.dp),
                                                backgroundColor =
                                                        VetNutriColors.Primary.copy(alpha = 0.1f)
                                        ) {
                                                Row(
                                                        modifier = Modifier.padding(12.dp),
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        Icon(
                                                                imageVector = Icons.Default.Info,
                                                                contentDescription = translate("database.stat.conseils"),
                                                                tint = VetNutriColors.Primary
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                                text =
                                                                        "${translate(Settings.CONSEILS_COUNT)}$conseilsCount",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .body2,
                                                                color = VetNutriColors.Primary,
                                                                fontWeight = FontWeight.Medium
                                                        )
                                                }
                                        }
                                        Column(
                                                modifier =
                                                        Modifier.fillMaxWidth()
                                                                .verticalScroll(
                                                                        rememberScrollState()
                                                                ),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                                // Sélection export avancée
                                                val selectedAnimalIdsState = remember {
                                                        mutableStateOf(setOf<String>())
                                                }
                                                var selectedAnimalIds by selectedAnimalIdsState
                                                val selectedFoodIdsState = remember {
                                                        mutableStateOf(setOf<String>())
                                                }
                                                var selectedFoodIds by selectedFoodIdsState
                                                val selectedEquationIdsState = remember {
                                                        mutableStateOf(setOf<String>())
                                                }
                                                var selectedEquationIds by selectedEquationIdsState
                                                val selectedReferenceIdsState = remember {
                                                        mutableStateOf(setOf<String>())
                                                }
                                                var selectedReferenceIds by selectedReferenceIdsState
                                                val selectedRecipeIdsState = remember {
                                                        mutableStateOf(setOf<String>())
                                                }
                                                var selectedRecipeIds by selectedRecipeIdsState
                                                val selectedConseilIdsState = remember {
                                                        mutableStateOf(setOf<String>())
                                                }
                                                var selectedConseilIds by selectedConseilIdsState

                                                val showAnimalSelectionDialogState = remember {
                                                        mutableStateOf(false)
                                                }
                                                var showAnimalSelectionDialog by showAnimalSelectionDialogState
                                                val showFoodSelectionDialogState = remember {
                                                        mutableStateOf(false)
                                                }
                                                var showFoodSelectionDialog by showFoodSelectionDialogState
                                                val showEquationSelectionDialogState = remember {
                                                        mutableStateOf(false)
                                                }
                                                var showEquationSelectionDialog by showEquationSelectionDialogState
                                                val showReferenceSelectionDialogState = remember {
                                                        mutableStateOf(false)
                                                }
                                                var showReferenceSelectionDialog by showReferenceSelectionDialogState
                                                val showRecipeSelectionDialogState = remember {
                                                        mutableStateOf(false)
                                                }
                                                var showRecipeSelectionDialog by showRecipeSelectionDialogState
                                                val showConseilSelectionDialogState = remember {
                                                        mutableStateOf(false)
                                                }
                                                var showConseilSelectionDialog by showConseilSelectionDialogState

                                                var availableAnimals by remember {
                                                        mutableStateOf<List<AnimalEv>>(emptyList())
                                                }
                                                var availableFoods by remember {
                                                        mutableStateOf<List<AlimentEv>>(emptyList())
                                                }
                                                var availableEquations by remember {
                                                        mutableStateOf<List<Equation>>(emptyList())
                                                }
                                                var availableReferences by remember {
                                                        mutableStateOf<List<ReferenceEv>>(emptyList())
                                                }
                                                var availableRecipes by remember {
                                                        mutableStateOf<List<Recette>>(emptyList())
                                                }
                                                var availableConseils by remember {
                                                        mutableStateOf<List<HtmlSection>>(emptyList())
                                                }

                                                var isLoadingAnimals by remember {
                                                        mutableStateOf(false)
                                                }
                                                var isLoadingFoods by remember {
                                                        mutableStateOf(false)
                                                }
                                                var isLoadingEquations by remember {
                                                        mutableStateOf(false)
                                                }
                                                var isLoadingReferences by remember {
                                                        mutableStateOf(false)
                                                }
                                                var isLoadingRecipes by remember {
                                                        mutableStateOf(false)
                                                }
                                                var isLoadingConseils by remember {
                                                        mutableStateOf(false)
                                                }

                                                val selectionLoadErrorState = remember {
                                                        mutableStateOf<String?>(null)
                                                }
                                                var selectionLoadError by selectionLoadErrorState

                                                val foodSelectionFiltersState = remember {
                                                        mutableStateOf(FoodSearchFilters())
                                                }
                                                var foodSelectionFilters by foodSelectionFiltersState
                                                val animalFilterEspeceState = remember {
                                                        mutableStateOf(
                                                                FilterOption<Espece>(
                                                                        label = translate("crossConsultation.speciesAll"),
                                                                        value = null
                                                                )
                                                        )
                                                }
                                                var animalFilterEspece by animalFilterEspeceState
                                                val equationFilterEspeceState = remember {
                                                        mutableStateOf(
                                                                FilterOption<Espece>(
                                                                        label = translate("crossConsultation.speciesAll"),
                                                                        value = null
                                                                )
                                                        )
                                                }
                                                var equationFilterEspece by equationFilterEspeceState
                                                val equationFilterKindState = remember {
                                                        mutableStateOf(
                                                                FilterOption<EquationKind>(
                                                                        label = translate("crossConsultation.keywordsAll"),
                                                                        value = null
                                                                )
                                                        )
                                                }
                                                var equationFilterKind by equationFilterKindState
                                                val referenceFilterEspeceState = remember {
                                                        mutableStateOf(
                                                                FilterOption<Espece>(
                                                                        label = translate("crossConsultation.speciesAll"),
                                                                        value = null
                                                                )
                                                        )
                                                }
                                                var referenceFilterEspece by referenceFilterEspeceState
                                                val conseilFilterCategoryState = remember {
                                                        mutableStateOf(
                                                                FilterOption<SectionCategory>(
                                                                        label = translate("crossConsultation.speciesAll"),
                                                                        value = null
                                                                )
                                                        )
                                                }
                                                var conseilFilterCategory by conseilFilterCategoryState

                                                var showAdvancedSelection by remember {
                                                        mutableStateOf(false)
                                                }

                                                Card(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        backgroundColor = MaterialTheme.colors.surface
                                                ) {
                                                        Column(modifier = Modifier.padding(12.dp)) {
                                                                Text(
                                                                        translate("auto.view.settingsview.export_api"),
                                                                        style = MaterialTheme.typography.subtitle1,
                                                                        fontWeight = FontWeight.Medium
                                                                )
                                                                Spacer(modifier = Modifier.height(6.dp))
                                                                Text(
                                                                        "Sélection avancée: animaux ${selectedAnimalIds.size}, aliments ${selectedFoodIds.size}, recettes ${selectedRecipeIds.size}, équations ${selectedEquationIds.size}, références ${selectedReferenceIds.size}, conseils ${selectedConseilIds.size}",
                                                                        style = MaterialTheme.typography.body2,
                                                                        color = Color.Gray
                                                                )
                                                                Spacer(modifier = Modifier.height(8.dp))
                                                                OutlinedButton(
                                                                        onClick = {
                                                                                showAdvancedSelection =
                                                                                        !showAdvancedSelection
                                                                        }
                                                                ) {
                                                                        Text(
                                                                                if (showAdvancedSelection)
                                                                                        "Masquer la sélection avancée"
                                                                                else "Afficher la sélection avancée"
                                                                        )
                                                                }
                                                        }
                                                }

                                                if (showAdvancedSelection) {
                                                // Boutons: sélectionner les éléments à exporter
                                                OutlinedButton(
                                                        onClick = {
                                                                showAnimalSelectionDialog = true
                                                                if (availableAnimals.isEmpty() &&
                                                                                !isLoadingAnimals
                                                                ) {
                                                                        coroutineScope.launch {
                                                                                isLoadingAnimals = true
                                                                                selectionLoadError = null
                                                                                try {
                                                                                        availableAnimals =
                                                                                                viewModel
                                                                                                        .animalRepository
                                                                                                        .getAllAnimals()
                                                                                } catch (e: Exception) {
                                                                                        e.rethrowIfCancellation()
                                                                                        selectionLoadError =
                                                                                                "Erreur chargement animaux: ${e.message}"
                                                                                } finally {
                                                                                        isLoadingAnimals = false
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                ) {
                                                        Text(
                                                                "${translate(Settings.SELECT_ANIMALS)} (${selectedAnimalIds.size})"
                                                        )
                                                }

                                                OutlinedButton(
                                                        onClick = {
                                                                showFoodSelectionDialog = true
                                                                if (availableFoods.isEmpty() &&
                                                                                !isLoadingFoods
                                                                ) {
                                                                        coroutineScope.launch {
                                                                                isLoadingFoods = true
                                                                                selectionLoadError = null
                                                                                try {
                                                                                        availableFoods =
                                                                                                viewModel
                                                                                                        .foodRepository
                                                                                                        .getAllFoods()
                                                                                } catch (e: Exception) {
                                                                                        e.rethrowIfCancellation()
                                                                                        selectionLoadError =
                                                                                                "Erreur chargement aliments: ${e.message}"
                                                                                } finally {
                                                                                        isLoadingFoods = false
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                ) {
                                                        Text(
                                                                "${translate(Settings.SELECT_FOODS)} (${selectedFoodIds.size})"
                                                        )
                                                }

                                                OutlinedButton(
                                                        onClick = {
                                                                showRecipeSelectionDialog = true
                                                                if (availableRecipes.isEmpty() &&
                                                                                !isLoadingRecipes
                                                                ) {
                                                                        coroutineScope.launch {
                                                                                isLoadingRecipes = true
                                                                                selectionLoadError = null
                                                                                try {
                                                                                        availableRecipes =
                                                                                                viewModel
                                                                                                        .recipeRepository
                                                                                                        ?.getAllRecipesAsRecette()
                                                                                                        ?: emptyList()
                                                                                } catch (e: Exception) {
                                                                                        e.rethrowIfCancellation()
                                                                                        selectionLoadError =
                                                                                                "Erreur chargement recettes: ${e.message}"
                                                                                } finally {
                                                                                        isLoadingRecipes = false
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                ) { Text(translate("auto.view.settingsview.selectionner_recettes_arg", (selectedRecipeIds.size).toString())) }

                                                OutlinedButton(
                                                        onClick = {
                                                                showEquationSelectionDialog = true
                                                                if (availableEquations.isEmpty() &&
                                                                                !isLoadingEquations
                                                                ) {
                                                                        coroutineScope.launch {
                                                                                isLoadingEquations = true
                                                                                selectionLoadError = null
                                                                                try {
                                                                                        availableEquations =
                                                                                                viewModel
                                                                                                        .equationRepository
                                                                                                        ?.getAllEquations()
                                                                                                        ?: emptyList()
                                                                                } catch (e: Exception) {
                                                                                        e.rethrowIfCancellation()
                                                                                        selectionLoadError =
                                                                                                "Erreur chargement equations: ${e.message}"
                                                                                } finally {
                                                                                        isLoadingEquations = false
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                ) { Text(translate("auto.view.settingsview.selectionner_equations_arg", (selectedEquationIds.size).toString())) }

                                                OutlinedButton(
                                                        onClick = {
                                                                showReferenceSelectionDialog = true
                                                                if (availableReferences.isEmpty() &&
                                                                                !isLoadingReferences
                                                                ) {
                                                                        coroutineScope.launch {
                                                                                isLoadingReferences = true
                                                                                selectionLoadError = null
                                                                                try {
                                                                                        availableReferences =
                                                                                                viewModel
                                                                                                        .referenceEvRepository
                                                                                                        ?.getAllReferenceEv()
                                                                                                        ?: emptyList()
                                                                                } catch (e: Exception) {
                                                                                        e.rethrowIfCancellation()
                                                                                        selectionLoadError =
                                                                                                "Erreur chargement references: ${e.message}"
                                                                                } finally {
                                                                                        isLoadingReferences = false
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                ) { Text(translate("auto.view.settingsview.selectionner_references_arg", (selectedReferenceIds.size).toString())) }

                                                OutlinedButton(
                                                        onClick = {
                                                                showConseilSelectionDialog = true
                                                                if (availableConseils.isEmpty() &&
                                                                                !isLoadingConseils
                                                                ) {
                                                                        coroutineScope.launch {
                                                                                isLoadingConseils = true
                                                                                selectionLoadError = null
                                                                                try {
                                                                                        availableConseils =
                                                                                                viewModel
                                                                                                        .conseilRepository
                                                                                                        ?.getConseilsActifs()
                                                                                                        ?.getOrThrow()
                                                                                                        ?: emptyList()
                                                                                } catch (e: Exception) {
                                                                                        e.rethrowIfCancellation()
                                                                                        selectionLoadError =
                                                                                                "Erreur chargement conseils: ${e.message}"
                                                                                } finally {
                                                                                        isLoadingConseils = false
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                ) { Text(translate("auto.view.settingsview.selectionner_conseils_arg", (selectedConseilIds.size).toString())) }

                                                // Cases à cocher d’inclusion

                                                SelectionAnimauxExport(
                                                        availableAnimals = availableAnimals,
                                                        isLoadingAnimals = isLoadingAnimals,
                                                        selection = selectedAnimalIdsState,
                                                        filtreEspece = animalFilterEspeceState,
                                                        erreur = selectionLoadErrorState,
                                                        visible = showAnimalSelectionDialogState
                                                )

                                                SelectionAlimentsExport(
                                                        availableFoods = availableFoods,
                                                        isLoadingFoods = isLoadingFoods,
                                                        selection = selectedFoodIdsState,
                                                        filtres = foodSelectionFiltersState,
                                                        erreur = selectionLoadErrorState,
                                                        visible = showFoodSelectionDialogState
                                                )

                                                SelectionEquationsExport(
                                                        availableEquations = availableEquations,
                                                        isLoadingEquations = isLoadingEquations,
                                                        selection = selectedEquationIdsState,
                                                        filtreEspece = equationFilterEspeceState,
                                                        filtreType = equationFilterKindState,
                                                        erreur = selectionLoadErrorState,
                                                        visible = showEquationSelectionDialogState
                                                )

                                                SelectionReferencesExport(
                                                        availableReferences = availableReferences,
                                                        isLoadingReferences = isLoadingReferences,
                                                        selection = selectedReferenceIdsState,
                                                        filtreEspece = referenceFilterEspeceState,
                                                        erreur = selectionLoadErrorState,
                                                        visible = showReferenceSelectionDialogState
                                                )

                                                SelectionConseilsExport(
                                                        availableConseils = availableConseils,
                                                        isLoadingConseils = isLoadingConseils,
                                                        selection = selectedConseilIdsState,
                                                        filtreCategorie = conseilFilterCategoryState,
                                                        erreur = selectionLoadErrorState,
                                                        visible = showConseilSelectionDialogState
                                                )

                                                SelectionRecettesExport(
                                                        availableRecipes = availableRecipes,
                                                        isLoadingRecipes = isLoadingRecipes,
                                                        selection = selectedRecipeIdsState,
                                                        erreur = selectionLoadErrorState,
                                                        visible = showRecipeSelectionDialogState
                                                )
                                                }
                                                // Affichage du message de résultat
                                                // d'importation des références
                                                // nutritionnelles
                                                nutritionalRequirementMessage?.let { message ->
                                                        Card(
                                                                modifier =
                                                                        Modifier.fillMaxWidth()
                                                                                .padding(
                                                                                        bottom =
                                                                                                8.dp
                                                                                ),
                                                                backgroundColor =
                                                                        if (message.startsWith("✅"))
                                                                                VetNutriColors
                                                                                        .Primary
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.1f
                                                                                        )
                                                                        else if (message.startsWith(
                                                                                        "❌"
                                                                                )
                                                                        )
                                                                                VetNutriColors.Error
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.1f
                                                                                        )
                                                                        else
                                                                                VetNutriColors
                                                                                        .Secondary
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.1f
                                                                                        )
                                                        ) {
                                                                Row(
                                                                        modifier =
                                                                                Modifier.fillMaxWidth()
                                                                                        .padding(
                                                                                                12.dp
                                                                                        ),
                                                                        horizontalArrangement =
                                                                                Arrangement
                                                                                        .SpaceBetween,
                                                                        verticalAlignment =
                                                                                Alignment
                                                                                        .CenterVertically
                                                                ) {
                                                                        Text(
                                                                                message,
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .body2,
                                                                                color =
                                                                                        if (message.startsWith(
                                                                                                        "✅"
                                                                                                )
                                                                                        )
                                                                                                VetNutriColors
                                                                                                        .Primary
                                                                                        else if (message.startsWith(
                                                                                                        "❌"
                                                                                                )
                                                                                        )
                                                                                                VetNutriColors
                                                                                                        .Error
                                                                                        else
                                                                                                Color.DarkGray,
                                                                                modifier =
                                                                                        Modifier.weight(
                                                                                                1f
                                                                                        )
                                                                        )
                                                                        IconButtonWithTooltip(
                                                                                onClick = {
                                                                                        importViewModel
                                                                                                .resetImportResult()
                                                                                },
                                                                                imageVector = Icons.Default.Close,
                                                                                contentDescription = translate("settings.close"),
                                                                                tooltip = translate("settings.close"),
                                                                                tint = Color.Gray
                                                                        )
                                                                }
                                                        }
                                                }

                                                // Export (nouveau format API)
                                                Button(
                                                        onClick = {
                                                                coroutineScope.launch {
                                                                        try {
                                                                                val exportRepo =
                                                                                        ExportImportRepository(
                                                                                                animalRepository =
                                                                                                        viewModel
                                                                                                                .animalRepository,
                                                                                                foodRepository =
                                                                                                        viewModel
                                                                                                                .foodRepository,
                                                                                                equationRepository =
                                                                                                        viewModel
                                                                                                                .equationRepository,
                                                                                                referenceRepository =
                                                                                                        viewModel
                                                                                                                .referenceEvRepository,
                                                                                                biblioRepository =
                                                                                                        viewModel
                                                                                                                .biblioRefRepository,
                                                                                                consultationRepository =
                                                                                                        viewModel
                                                                                                                .consultationRepository,
                                                                                                recipeRepository =
                                                                                                        viewModel
                                                                                                                .recipeRepository,
                                                                                                conseilRepository =
                                                                                                        viewModel
                                                                                                                .conseilRepository
                                                                                        )
                                                                                val exportOptions =
                                                                                        ExportImportRepository
                                                                                                .ExportSelectionOptions(
                                                                                                        includeAnimals =
                                                                                                                false,
                                                                                                        includeFoods =
                                                                                                                false,
                                                                                                        includeRations =
                                                                                                                false,
                                                                                                        includeRecipes =
                                                                                                                false,
                                                                                                        includeEquations =
                                                                                                                false,
                                                                                                        includeConseils =
                                                                                                                false,
                                                                                                        includeLinkedFromAnimals =
                                                                                                                true,
                                                                                                        animalIds =
                                                                                                                selectedAnimalIds,
                                                                                                        foodIds =
                                                                                                                selectedFoodIds,
                                                                                                        referenceIds =
                                                                                                                selectedReferenceIds,
                                                                                                        recipeIds =
                                                                                                                selectedRecipeIds,
                                                                                                        equationIds =
                                                                                                                selectedEquationIds,
                                                                                                        conseilIds =
                                                                                                                selectedConseilIds
                                                                                                )
                                                                                val hasNoSelection =
                                                                                        selectedAnimalIds.isEmpty() &&
                                                                                                selectedFoodIds.isEmpty() &&
                                                                                                selectedRecipeIds.isEmpty() &&
                                                                                                selectedReferenceIds.isEmpty() &&
                                                                                                selectedEquationIds.isEmpty() &&
                                                                                                selectedConseilIds.isEmpty()
                                                                                val effectiveOptions =
                                                                                        if (hasNoSelection) {
                                                                                                exportOptions.copy(
                                                                                                        includeAnimals =
                                                                                                                true,
                                                                                                        includeFoods =
                                                                                                                true,
                                                                                                        includeRations =
                                                                                                                true,
                                                                                                        includeRecipes =
                                                                                                                true,
                                                                                                        includeEquations =
                                                                                                                true,
                                                                                                        includeConseils =
                                                                                                                true,
                                                                                                        includeLinkedFromAnimals =
                                                                                                                true
                                                                                                )
                                                                                        } else {
                                                                                                exportOptions.copy(
                                                                                                        includeAnimals =
                                                                                                                selectedAnimalIds.isNotEmpty(),
                                                                                                        includeFoods =
                                                                                                                selectedFoodIds.isNotEmpty(),
                                                                                                        includeRecipes =
                                                                                                                selectedRecipeIds.isNotEmpty(),
                                                                                                        includeEquations =
                                                                                                                selectedEquationIds.isNotEmpty(),
                                                                                                        includeConseils =
                                                                                                                selectedConseilIds.isNotEmpty(),
                                                                                                        includeLinkedFromAnimals =
                                                                                                                true
                                                                                                )
                                                                                        }
                                                                                val ok =
                                                                                        withContext(
                                                                                                AppDispatchers.IO
                                                                                        ) {
                                                                                                val envelope =
                                                                                                        exportRepo
                                                                                                                .exportWithSelectionEnvelope(
                                                                                                                        effectiveOptions
                                                                                                                )
                                                                                                fr.vetbrain
                                                                                                        .vetnutri_mp
                                                                                                        .exportApiEnvelopeToFile(
                                                                                                                envelope =
                                                                                                                        envelope,
                                                                                                                defaultFileName =
                                                                                                                        "vetnutri_export.json"
                                                                                                        )
                                                                                        }
                                                                                // Export
                                                                                // terminé,
                                                                                // résultat
                                                                                // : $ok
                                                                        } catch (e: Exception) { e.rethrowIfCancellation(); AppLog.d("SettingsView", "Exception ignorée : ${e.message}") }
                                                                }
                                                        },
                                                        colors =
                                                                ButtonDefaults.buttonColors(
                                                                        backgroundColor =
                                                                                VetNutriColors
                                                                                        .Secondary
                                                                ),
                                                        modifier = Modifier.fillMaxWidth()
                                                ) {
                                                        Text(
                                                                translate(Settings.EXPORT_API),
                                                                color = Color.White
                                                        )
                                                }

                                                // Import (nouveau format API) – aligné sur
                                                // import animaux
                                                Button(
                                                        onClick = {
                                                                viewModel.importApiFromFileUI()
                                                        },
                                                        colors =
                                                                ButtonDefaults.buttonColors(
                                                                        backgroundColor =
                                                                                VetNutriColors
                                                                                        .Secondary
                                                                ),
                                                        modifier = Modifier.fillMaxWidth()
                                                ) {
                                                        Text(
                                                                translate(Settings.IMPORT_API),
                                                                color = Color.White
                                                        )
                                                }

                                                // Dialog de résultat pour l'import API
                                                val apiImportResult =
                                                        viewModel.importResult.collectAsState()
                                                                .value
                                                val apiImporting =
                                                        viewModel.isApiImporting.collectAsState()
                                                                .value
                                                val apiProgress =
                                                        viewModel.apiImportProgress.collectAsState()
                                                                .value
                                                val apiLogs =
                                                        viewModel.apiImportLogs.collectAsState()
                                                                .value
                                                if (apiImporting) {
                                                        AlertDialog(
                                                                onDismissRequest = {},
                                                                title = {
                                                                         Text(translate(Settings.IMPORT_RUNNING))
                                                                },
                                                                text = {
                                                                        Column(
                                                                                verticalArrangement =
                                                                                        Arrangement
                                                                                                .spacedBy(
                                                                                                        8.dp
                                                                                                )
                                                                        ) {
                                                                                LinearProgressIndicator(
                                                                                        progress =
                                                                                                apiProgress
                                                                                                        .toFloat()
                                                                                )
                                                                                Box(
                                                                                        modifier =
                                                                                                Modifier.fillMaxWidth()
                                                                                                        .height(
                                                                                                                120.dp
                                                                                                        )
                                                                                                        .background(
                                                                                                                Color(
                                                                                                                        0xFFF5F5F5
                                                                                                                )
                                                                                                        )
                                                                                ) {
                                                                                        // Affichage
                                                                                        // simple
                                                                                        // des logs
                                                                                        // (limités)
                                                                                        Column(
                                                                                                modifier =
                                                                                                        Modifier.padding(
                                                                                                                8.dp
                                                                                                        )
                                                                                        ) {
                                                                                                apiLogs.takeLast(
                                                                                                                10
                                                                                                        )
                                                                                                        .forEach {
                                                                                                                line
                                                                                                                ->
                                                                                                                Text(
                                                                                                                        line
                                                                                                                )
                                                                                                        }
                                                                                        }
                                                                                }
                                                                        }
                                                                },
                                                                confirmButton = {}
                                                        )
                                                }
                                                var showApiImportDialog by remember {
                                                        mutableStateOf(false)
                                                }
                                                LaunchedEffect(apiImportResult) {
                                                        if (apiImportResult != null)
                                                                showApiImportDialog = true
                                                }
                                                if (showApiImportDialog) {
                                                        AlertDialog(
                                                                onDismissRequest = {
                                                                        showApiImportDialog = false
                                                                        viewModel
                                                                                .resetImportResult()
                                                                },
                                                                title = {
                                                                        Text(
                                                                                translate("auto.view.settingsview.resultat_de_l_import_api")
                                                                        )
                                                                },
                                                                text = {
                                                                        when (val r =
                                                                                        apiImportResult
                                                                        ) {
                                                                                is SettingsViewModel.ImportResult.Success -> {
                                                                                        Column {
                                                                                                Text(
                                                                                                        "Total pris en compte: ${r.count}"
                                                                                                )
                                                                                                Text(
                                                                                                        "Importés: ${r.importedCount}"
                                                                                                )
                                                                                                if (r.updatedCount >
                                                                                                                0
                                                                                                )
                                                                                                        Text(
                                                                                                                "Mises à jour: ${r.updatedCount}"
                                                                                                        )
                                                                                                if (r.deletedCount >
                                                                                                                0
                                                                                                )
                                                                                                        Text(
                                                                                                                "Supprimés: ${r.deletedCount}"
                                                                                                        )
                                                                                                if (r.errorCount >
                                                                                                                0
                                                                                                )
                                                                                                        Text(
                                                                                                                "Erreurs: ${r.errorCount}",
                                                                                                                color =
                                                                                                                        MaterialTheme
                                                                                                                                .colors
                                                                                                                                .error
                                                                                                        )
                                                                                                if (r.conseils >
                                                                                                                0
                                                                                                )
                                                                                                        Text(
                                                                                                                "Conseils: ${r.conseils}"
                                                                                                        )
                                                                                        }
                                                                                }
                                                                                is SettingsViewModel.ImportResult.Error -> {
                                                                                        Text(
                                                                                                "Erreur: ${r.message}",
                                                                                                color =
                                                                                                        MaterialTheme
                                                                                                                .colors
                                                                                                                .error
                                                                                        )
                                                                                }
                                                                                null ->
                                                                                        Text(
                                                                                                translate("auto.view.settingsview.aucun_resultat")
                                                                                        )
                                                                        }
                                                                },
                                                                confirmButton = {
                                                                        Button(
                                                                                onClick = {
                                                                                        showApiImportDialog =
                                                                                                false
                                                                                        viewModel
                                                                                                .resetImportResult()
                                                                                        // rafraîchit la liste des animaux si l'import a concerné des animaux
                                                                                        onAnimalListRefresh()
                                                                                }
                                                                        ) { Text(translate("general.ok")) }
                                                                }
                                                        )
                                                }

                                                var showFoodImportOptionsDialog by remember {
                                                        mutableStateOf(false)
                                                }
                                                var mergeNutrients by remember {
                                                        mutableStateOf(viewModel.importMergeNutrients)
                                                }
                                                var importOnlyIfNewer by remember {
                                                        mutableStateOf(viewModel.importOnlyIfNewer)
                                                }
                                                LaunchedEffect(showFoodImportOptionsDialog) {
                                                        if (showFoodImportOptionsDialog) {
                                                                mergeNutrients =
                                                                        viewModel.importMergeNutrients
                                                                importOnlyIfNewer =
                                                                        viewModel.importOnlyIfNewer
                                                        }
                                                }

                                                // Bouton pour importer des aliments
                                                Button(
                                                        onClick = {
                                                                showFoodImportOptionsDialog = true
                                                        },
                                                        colors =
                                                                ButtonDefaults.buttonColors(
                                                                        backgroundColor =
                                                                                VetNutriColors
                                                                                        .Primary
                                                                ),
                                                        modifier = Modifier.fillMaxWidth()
                                                ) {
                                                        Text(
                                                                translate("auto.view.settingsview.importer_des_aliments"),
                                                                color = Color.White
                                                        )
                                                }

                                                if (showFoodImportOptionsDialog) {
                                                        AlertDialog(
                                                                onDismissRequest = {
                                                                        showFoodImportOptionsDialog =
                                                                                false
                                                                },
                                                                title = {
                                                                        Text(
                                                                                translate("auto.view.settingsview.options_d_import_des_aliments")
                                                                        )
                                                                },
                                                                text = {
                                                                        Column {
                                                                                Row(
                                                                                        verticalAlignment =
                                                                                                Alignment
                                                                                                        .CenterVertically
                                                                                ) {
                                                                                        Checkbox(
                                                                                                checked =
                                                                                                        mergeNutrients,
                                                                                                onCheckedChange = {
                                                                                                        mergeNutrients =
                                                                                                                it
                                                                                                }
                                                                                        )
                                                                                        Text(
                                                                                                translate("auto.view.excelimportexportcomponents.fusionner_les_nutriments_ne_pas_supprimer_ceux_a")
                                                                                        )
                                                                                }
                                                                                Spacer(
                                                                                        modifier =
                                                                                                Modifier
                                                                                                        .height(
                                                                                                                8.dp
                                                                                                        )
                                                                                )
                                                                                Row(
                                                                                        verticalAlignment =
                                                                                                Alignment
                                                                                                        .CenterVertically
                                                                                ) {
                                                                                        Checkbox(
                                                                                                checked =
                                                                                                        importOnlyIfNewer,
                                                                                                onCheckedChange = {
                                                                                                        importOnlyIfNewer =
                                                                                                                it
                                                                                                }
                                                                                        )
                                                                                        Text(
                                                                                                translate("auto.view.excelimportexportcomponents.n_importer_que_si_la_date_de_derniere_mise_a_jou")
                                                                                        )
                                                                                }
                                                                        }
                                                                },
                                                                confirmButton = {
                                                                        Button(
                                                                                onClick = {
                                                                                        showFoodImportOptionsDialog =
                                                                                                false
                                                                                        viewModel.importMergeNutrients =
                                                                                                mergeNutrients
                                                                                        viewModel.importOnlyIfNewer =
                                                                                                importOnlyIfNewer
                                                                                        try {
                                                                                                viewModel
                                                                                                        .importFoodsFromFileUI()
                                                                                        } catch (
                                                                                                e: Exception
                                                                                        ) { e.rethrowIfCancellation(); AppLog.d("SettingsView", "Exception ignorée : ${e.message}") }
                                                                                }
                                                                        ) { Text(translate("startup.continue")) }
                                                                },
                                                                dismissButton = {
                                                                        Button(
                                                                                onClick = {
                                                                                        showFoodImportOptionsDialog =
                                                                                                false
                                                                                }
                                                                        ) { Text(translate("general.cancel")) }
                                                                }
                                                        )
                                                }

                                                Button(
                                                        onClick = onImportAnimals,
                                                        colors =
                                                                ButtonDefaults.buttonColors(
                                                                        backgroundColor =
                                                                                VetNutriColors
                                                                                        .Primary
                                                                ),
                                                        modifier = Modifier.fillMaxWidth()
                                                ) {
                                                        Text(
                                                                translate(Settings.IMPORT_ANIMALS),
                                                                color = Color.White
                                                        )
                                                }

                                                // Bouton pour importer des références
                                                // nutritionnelles
                                                Button(
                                                        onClick = {
                                                                try {
                                                                        // Utilisons la
                                                                        // méthode du
                                                                        // ImportViewModel
                                                                        // pour
                                                                        // importer les
                                                                        // références
                                                                        // nutritionnelles
                                                                        importViewModel
                                                                                .importNutritionalRequirementsFromFileUI()
                                                                } catch (e: Exception) { e.rethrowIfCancellation(); AppLog.d("SettingsView", "Exception ignorée : ${e.message}") }
                                                        },
                                                        colors =
                                                                ButtonDefaults.buttonColors(
                                                                        backgroundColor =
                                                                                VetNutriColors
                                                                                        .Secondary
                                                                ),
                                                        modifier = Modifier.fillMaxWidth()
                                                ) {
                                                        Text(
                                                                translate("auto.view.settingsview.importer_des_references_nutritionnelles_vbnr_jso"),
                                                                color = Color.White
                                                        )
                                                }
                                        }
                                }
                                3 -> { // Excel Import/Export
                                        val excelFoodService = remember {
                                                viewModel.foodRepository?.let { foodRepo ->
                                                        viewModel.biblioRefRepository?.let { biblioRepo ->
                                                                ExcelFoodService(foodRepo, biblioRepo)
                                                        }
                                                }
                                        }
                                        ExcelImportExportSection(
                                                modifier = Modifier.fillMaxWidth(),
                                                excelFoodService = excelFoodService,
                                                foodRepository = viewModel.foodRepository
                                        )
                                }
                                4 -> { // Recettes
                                        if (isExamMode) {
                                                Box(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        contentAlignment = Alignment.Center
                                                ) {
                                                        Text(
                                                                translate("auto.view.settingsview.recettes_indisponibles_en_mode_examen")
                                                        )
                                                }
                                        } else {
                                                val recipeEditViewModel = remember(
                                                        viewModel.recipeRepository,
                                                        viewModel.foodRepository
                                                ) {
                                                        RecipeEditViewModel(
                                                                recipeRepository =
                                                                        viewModel.recipeRepository
                                                                                ?: throw IllegalStateException(
                                                                                        "RecipeRepository not available"
                                                                                ),
                                                                foodRepository =
                                                                        viewModel.foodRepository
                                                                                ?: throw IllegalStateException(
                                                                                        "FoodRepository not available"
                                                                                )
                                                        )
                                                }
                                                DisposableEffect(recipeEditViewModel) {
                                                        onDispose { recipeEditViewModel.clear() }
                                                }
                                                RecipeEditView(
                                                        viewModel = recipeEditViewModel,
                                                        modifier = Modifier.fillMaxWidth()
                                                )
                                        }
                                }
                                5 -> { // Administration
                                        AdministrationSettings(
                                                viewModel = viewModel,
                                                onAnimalListRefresh = onAnimalListRefresh,
                                                onFoodListRefresh = onFoodListRefresh,
                                                onBackupClick = { onBackupClick() },
                                                onLegacyMigrationClick = { onLegacyMigrationClick() },
                                                onBulkBiblioClick = { onBulkBiblioClick() },
                                                modifier = Modifier.fillMaxWidth()
                                        )
                                }
                        }
                }
        }

        // Dialogue d'alerte pour l'importation des références nutritionnelles
        if (showImportDialog) {
                AlertDialog(
                        onDismissRequest = {
                                showImportDialog = false
                                importViewModel.resetImportResult()
                        },
                        title = {
                                Text(
                                        translate("dialog.resultImportAnimals.title"),
                                        style = MaterialTheme.typography.h6,
                                        color =
                                                if (importDialogMessage.startsWith("✅"))
                                                        VetNutriColors.Primary
                                                else VetNutriColors.Error
                                )
                        },
                        text = {
                                Text(importDialogMessage, style = MaterialTheme.typography.body2)
                        },
                        confirmButton = {
                                Button(
                                        onClick = {
                                                showImportDialog = false
                                                importViewModel.resetImportResult()
                                        },
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor = VetNutriColors.Primary,
                                                        contentColor = Color.White
                                                )
                                ) { Text(translate("general.ok")) }
                        },
                        backgroundColor = MaterialTheme.colors.surface
                )
        }
}

/** Sections disponibles dans les paramètres */
enum class SettingsSection(val title: String) {
        INTERFACE("Interface"),
        PREFERENCES("Préférences"),
        IMPORTATION("Importation"),
        EXCEL("Import/Export Excel"),
        RECIPES("Recettes"),
        ADMINISTRATION("Administration")
}
