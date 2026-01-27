package fr.vetbrain.vetnutri_mp.View

// Import délégué via SettingsViewModel.importApiFromFileUI()
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Repository.ExportImportRepository
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Components.IconButtonWithTooltip
import fr.vetbrain.vetnutri_mp.Components.DropdownField
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.Equation
import fr.vetbrain.vetnutri_mp.Data.FoodSearchFilters
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.View.SettingsComponents.SettingsTabs
import fr.vetbrain.vetnutri_mp.View.SettingsSections.AdministrationSettings
import fr.vetbrain.vetnutri_mp.View.SettingsSections.InterfaceSettings
import fr.vetbrain.vetnutri_mp.View.SettingsSections.RecipeEditView
import fr.vetbrain.vetnutri_mp.ViewModel.ImportViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.RecipeEditViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel
import fr.vetbrain.vetnutri_mp.Services.ExcelFoodService
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Settings
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.General
import fr.vetbrain.vetnutri_mp.Localization.translate

private data class FilterOption<T>(val label: String, val value: T?)

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
        modifier: Modifier = Modifier,
        onSpeciesClick: (fr.vetbrain.vetnutri_mp.Enumer.Espece) -> Unit = {},
        onBackupClick: () -> Unit = {}
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
        val nutritionalRequirementMessage by remember {
                derivedStateOf { importViewModel.nutritionalRequirementImportResultMessage }
        }

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
                        // En cas d'erreur, garder 0
                        conseilsCount = 0
                }
        }

        Column(modifier = modifier.fillMaxSize()) {
                // En-tête avec bouton retour

                // Navigation par onglets
                SettingsTabs(selectedTab = selectedTab, onTabSelected = { selectedTab = it })

                // Contenu de l'onglet sélectionné
                Box(modifier = Modifier.fillMaxSize().padding(AppSizes.paddingMedium)) {
                        when (selectedTab) {
                                0 -> { // Interface
                                        InterfaceSettings(
                                                viewModel = viewModel,
                                                modifier = Modifier.fillMaxWidth()
                                        )
                                }
                                1 -> { // Préférences
                                        PreferencesSection(
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
                                                                contentDescription = "Conseils",
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
                                                var selectedAnimalIds by remember {
                                                        mutableStateOf(setOf<String>())
                                                }
                                                var selectedFoodIds by remember {
                                                        mutableStateOf(setOf<String>())
                                                }
                                                var selectedEquationIds by remember {
                                                        mutableStateOf(setOf<String>())
                                                }
                                                var selectedReferenceIds by remember {
                                                        mutableStateOf(setOf<String>())
                                                }

                                                var showAnimalSelectionDialog by remember {
                                                        mutableStateOf(false)
                                                }
                                                var showFoodSelectionDialog by remember {
                                                        mutableStateOf(false)
                                                }
                                                var showEquationSelectionDialog by remember {
                                                        mutableStateOf(false)
                                                }
                                                var showReferenceSelectionDialog by remember {
                                                        mutableStateOf(false)
                                                }

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

                                                var selectionLoadError by remember {
                                                        mutableStateOf<String?>(null)
                                                }

                                                var foodSelectionFilters by remember {
                                                        mutableStateOf(FoodSearchFilters())
                                                }
                                                var animalFilterEspece by remember {
                                                        mutableStateOf(
                                                                FilterOption<Espece>(
                                                                        label = "Toutes",
                                                                        value = null
                                                                )
                                                        )
                                                }
                                                var equationFilterEspece by remember {
                                                        mutableStateOf(
                                                                FilterOption<Espece>(
                                                                        label = "Toutes",
                                                                        value = null
                                                                )
                                                        )
                                                }
                                                var equationFilterKind by remember {
                                                        mutableStateOf(
                                                                FilterOption<EquationKind>(
                                                                        label = "Tous",
                                                                        value = null
                                                                )
                                                        )
                                                }
                                                var referenceFilterEspece by remember {
                                                        mutableStateOf(
                                                                FilterOption<Espece>(
                                                                        label = "Toutes",
                                                                        value = null
                                                                )
                                                        )
                                                }

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
                                                                                        selectionLoadError =
                                                                                                "Erreur chargement equations: ${e.message}"
                                                                                } finally {
                                                                                        isLoadingEquations = false
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                ) { Text("Selectionner equations (${selectedEquationIds.size})") }

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
                                                                                        selectionLoadError =
                                                                                                "Erreur chargement references: ${e.message}"
                                                                                } finally {
                                                                                        isLoadingReferences = false
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                ) { Text("Selectionner references (${selectedReferenceIds.size})") }

                                                // Cases à cocher d’inclusion

                                                if (showAnimalSelectionDialog) {
                                                        Dialog(
                                                                onDismissRequest = {
                                                                        showAnimalSelectionDialog = false
                                                                },
                                                                properties =
                                                                        DialogProperties(
                                                                                usePlatformDefaultWidth =
                                                                                        false
                                                                        )
                                                        ) {
                                                                Box(
                                                                        modifier =
                                                                                Modifier.fillMaxSize()
                                                                                        .background(
                                                                                                MaterialTheme
                                                                                                        .colors
                                                                                                        .background
                                                                                        )
                                                                ) {
                                                                        when {
                                                                                isLoadingAnimals -> {
                                                                                        Column(
                                                                                                modifier =
                                                                                                        Modifier.fillMaxSize(),
                                                                                                verticalArrangement =
                                                                                                        Arrangement.Center,
                                                                                                horizontalAlignment =
                                                                                                        Alignment.CenterHorizontally
                                                                                        ) {
                                                                                                CircularProgressIndicator()
                                                                                                Spacer(
                                                                                                        modifier =
                                                                                                                Modifier.height(
                                                                                                                        12.dp
                                                                                                                )
                                                                                                )
                                                                                                Text(
                                                                                                        "Chargement des animaux..."
                                                                                                )
                                                                                        }
                                                                                }
                                                                                selectionLoadError != null -> {
                                                                                        Column(
                                                                                                modifier =
                                                                                                        Modifier.fillMaxSize()
                                                                                                                .padding(
                                                                                                                        24.dp
                                                                                                                ),
                                                                                                verticalArrangement =
                                                                                                        Arrangement.Center,
                                                                                                horizontalAlignment =
                                                                                                        Alignment.CenterHorizontally
                                                                                        ) {
                                                                                                Text(
                                                                                                        selectionLoadError
                                                                                                                ?: "Erreur inconnue",
                                                                                                        color =
                                                                                                                MaterialTheme
                                                                                                                        .colors
                                                                                                                        .error,
                                                                                                        style =
                                                                                                                MaterialTheme
                                                                                                                        .typography
                                                                                                                        .body1
                                                                                                )
                                                                                                Spacer(
                                                                                                        modifier =
                                                                                                                Modifier.height(
                                                                                                                        16.dp
                                                                                                                )
                                                                                                )
                                                                                                Button(
                                                                                                        onClick = {
                                                                                                                showAnimalSelectionDialog =
                                                                                                                        false
                                                                                                                selectionLoadError =
                                                                                                                        null
                                                                                                        }
                                                                                                ) { Text("Fermer") }
                                                                                        }
                                                                                }
                                                                                availableAnimals.isEmpty() -> {
                                                                                        Column(
                                                                                                modifier =
                                                                                                        Modifier.fillMaxSize()
                                                                                                                .padding(
                                                                                                                        24.dp
                                                                                                                ),
                                                                                                verticalArrangement =
                                                                                                        Arrangement.Center,
                                                                                                horizontalAlignment =
                                                                                                        Alignment.CenterHorizontally
                                                                                        ) {
                                                                                                Text(
                                                                                                        "Aucun animal disponible."
                                                                                                )
                                                                                                Spacer(
                                                                                                        modifier =
                                                                                                                Modifier.height(
                                                                                                                        16.dp
                                                                                                                )
                                                                                                )
                                                                                                Button(
                                                                                                        onClick = {
                                                                                                                showAnimalSelectionDialog =
                                                                                                                        false
                                                                                                        }
                                                                                                ) { Text("Fermer") }
                                                                                        }
                                                                                }
                                                                                else -> {
                                                                                        val animalById =
                                                                                                remember(
                                                                                                        availableAnimals
                                                                                                ) {
                                                                                                        availableAnimals
                                                                                                                .associateBy {
                                                                                                                        it.uuid
                                                                                                                }
                                                                                                }
                                                                                        val animalFilterOptions =
                                                                                                remember {
                                                                                                        listOf<FilterOption<Espece>>(
                                                                                                                FilterOption<Espece>(
                                                                                                                        label =
                                                                                                                                "Toutes",
                                                                                                                        value = null
                                                                                                                )
                                                                                                        ) +
                                                                                                                Espece
                                                                                                                        .values()
                                                                                                                        .map {
                                                                                                                                FilterOption(
                                                                                                                                        label =
                                                                                                                                                it.label,
                                                                                                                                        value =
                                                                                                                                                it
                                                                                                                                )
                                                                                                                        }
                                                                                                }
                                                                                        SelectionDialog(
                                                                                                title =
                                                                                                        "Selection des animaux",
                                                                                                items =
                                                                                                        availableAnimals.map {
                                                                                                                SelectionItem(
                                                                                                                        id =
                                                                                                                                it.uuid,
                                                                                                                        title =
                                                                                                                                it.nom
                                                                                                                                        .ifBlank {
                                                                                                                                                "Sans nom"
                                                                                                                                        },
                                                                                                                        subtitle =
                                                                                                                                it.ownerName
                                                                                                                                        .ifBlank {
                                                                                                                                                ""
                                                                                                                                        }
                                                                                                                )
                                                                                                        },
                                                                                                initialSelectedIds =
                                                                                                        selectedAnimalIds,
                                                                                                onConfirm = {
                                                                                                        ids ->
                                                                                                        selectedAnimalIds =
                                                                                                                ids
                                                                                                        showAnimalSelectionDialog =
                                                                                                                false
                                                                                                },
                                                                                                onDismiss = {
                                                                                                        showAnimalSelectionDialog =
                                                                                                                false
                                                                                                },
                                                                                                confirmLabel =
                                                                                                        "Valider la selection",
                                                                                                emptyLabel =
                                                                                                        "Aucun animal disponible.",
                                                                                                filtersContent = {
                                                                                                        DropdownField(
                                                                                                                label =
                                                                                                                        "Espece",
                                                                                                                selectedValue =
                                                                                                                        animalFilterEspece,
                                                                                                                options =
                                                                                                                        animalFilterOptions,
                                                                                                                onValueChange = {
                                                                                                                        animalFilterEspece =
                                                                                                                                it
                                                                                                                },
                                                                                                                valueToString = {
                                                                                                                        it.label
                                                                                                                },
                                                                                                                modifier =
                                                                                                                        Modifier.fillMaxWidth()
                                                                                                        )
                                                                                                },
                                                                                                filterPredicate = { item ->
                                                                                                        val animal =
                                                                                                                animalById[
                                                                                                                        item.id
                                                                                                                ]
                                                                                                        val selected =
                                                                                                                animalFilterEspece.value
                                                                                                        selected == null ||
                                                                                                                animal?.getEspece() ==
                                                                                                                        selected
                                                                                                }
                                                                                        )
                                                                                }
                                                                        }

                                                                        IconButton(
                                                                                onClick = {
                                                                                        showAnimalSelectionDialog =
                                                                                                false
                                                                                },
                                                                                modifier =
                                                                                        Modifier.align(
                                                                                                Alignment.TopEnd
                                                                                        )
                                                                                                .padding(
                                                                                                        16.dp
                                                                                                )
                                                                        ) {
                                                                                Icon(
                                                                                        imageVector =
                                                                                                Icons.Default.Close,
                                                                                        contentDescription =
                                                                                                "Fermer"
                                                                                )
                                                                        }
                                                                }
                                                        }
                                                }

                                                if (showFoodSelectionDialog) {
                                                        Dialog(
                                                                onDismissRequest = {
                                                                        showFoodSelectionDialog = false
                                                                },
                                                                properties =
                                                                        DialogProperties(
                                                                                usePlatformDefaultWidth =
                                                                                        false
                                                                        )
                                                        ) {
                                                                Box(
                                                                        modifier =
                                                                                Modifier.fillMaxSize()
                                                                                        .background(
                                                                                                MaterialTheme
                                                                                                        .colors
                                                                                                        .background
                                                                                        )
                                                                ) {
                                                                        when {
                                                                                isLoadingFoods -> {
                                                                                        Column(
                                                                                                modifier =
                                                                                                        Modifier.fillMaxSize(),
                                                                                                verticalArrangement =
                                                                                                        Arrangement.Center,
                                                                                                horizontalAlignment =
                                                                                                        Alignment.CenterHorizontally
                                                                                        ) {
                                                                                                CircularProgressIndicator()
                                                                                                Spacer(
                                                                                                        modifier =
                                                                                                                Modifier.height(
                                                                                                                        12.dp
                                                                                                                )
                                                                                                )
                                                                                                Text(
                                                                                                        "Chargement des aliments..."
                                                                                                )
                                                                                        }
                                                                                }
                                                                                selectionLoadError != null -> {
                                                                                        Column(
                                                                                                modifier =
                                                                                                        Modifier.fillMaxSize()
                                                                                                                .padding(
                                                                                                                        24.dp
                                                                                                                ),
                                                                                                verticalArrangement =
                                                                                                        Arrangement.Center,
                                                                                                horizontalAlignment =
                                                                                                        Alignment.CenterHorizontally
                                                                                        ) {
                                                                                                Text(
                                                                                                        selectionLoadError
                                                                                                                ?: "Erreur inconnue",
                                                                                                        color =
                                                                                                                MaterialTheme
                                                                                                                        .colors
                                                                                                                        .error,
                                                                                                        style =
                                                                                                                MaterialTheme
                                                                                                                        .typography
                                                                                                                        .body1
                                                                                                )
                                                                                                Spacer(
                                                                                                        modifier =
                                                                                                                Modifier.height(
                                                                                                                        16.dp
                                                                                                                )
                                                                                                )
                                                                                                Button(
                                                                                                        onClick = {
                                                                                                                showFoodSelectionDialog =
                                                                                                                        false
                                                                                                                selectionLoadError =
                                                                                                                        null
                                                                                                        }
                                                                                                ) { Text("Fermer") }
                                                                                        }
                                                                                }
                                                                                availableFoods.isEmpty() -> {
                                                                                        Column(
                                                                                                modifier =
                                                                                                        Modifier.fillMaxSize()
                                                                                                                .padding(
                                                                                                                        24.dp
                                                                                                                ),
                                                                                                verticalArrangement =
                                                                                                        Arrangement.Center,
                                                                                                horizontalAlignment =
                                                                                                        Alignment.CenterHorizontally
                                                                                        ) {
                                                                                                Text(
                                                                                                        "Aucun aliment disponible."
                                                                                                )
                                                                                                Spacer(
                                                                                                        modifier =
                                                                                                                Modifier.height(
                                                                                                                        16.dp
                                                                                                                )
                                                                                                )
                                                                                                Button(
                                                                                                        onClick = {
                                                                                                                showFoodSelectionDialog =
                                                                                                                        false
                                                                                                        }
                                                                                                ) { Text("Fermer") }
                                                                                        }
                                                                                }
                                                                                else -> {
                                                                                        AnalyseSelectionAlimentsView(
                                                                                                aliments =
                                                                                                        availableFoods,
                                                                                                onClose = {
                                                                                                        showFoodSelectionDialog =
                                                                                                                false
                                                                                                },
                                                                                                onPrimaryAction = {
                                                                                                        aliments ->
                                                                                                        selectedFoodIds =
                                                                                                                aliments.map {
                                                                                                                        it.uuid
                                                                                                                }
                                                                                                                        .toSet()
                                                                                                        showFoodSelectionDialog =
                                                                                                                false
                                                                                                },
                                                                                                primaryActionLabel =
                                                                                                        "Valider la selection",
                                                                                                alimentsInitialementSelectionnes =
                                                                                                        availableFoods.filter {
                                                                                                                selectedFoodIds.contains(
                                                                                                                        it.uuid
                                                                                                                )
                                                                                                        },
                                                                                                onSelectionChanged = {
                                                                                                        selectedFoodIds =
                                                                                                                it.map { al ->
                                                                                                                        al.uuid
                                                                                                                }
                                                                                                                        .toSet()
                                                                                                },
                                                                                                filtersInitial =
                                                                                                        foodSelectionFilters,
                                                                                                onFiltersChange = {
                                                                                                        foodSelectionFilters =
                                                                                                                it
                                                                                                },
                                                                                                modifier =
                                                                                                        Modifier.fillMaxSize()
                                                                                                                .padding(
                                                                                                                        16.dp
                                                                                                                )
                                                                                        )
                                                                                }
                                                                        }

                                                                        IconButton(
                                                                                onClick = {
                                                                                        showFoodSelectionDialog =
                                                                                                false
                                                                                },
                                                                                modifier =
                                                                                        Modifier.align(
                                                                                                Alignment.TopEnd
                                                                                        )
                                                                                                .padding(
                                                                                                        16.dp
                                                                                                )
                                                                        ) {
                                                                                Icon(
                                                                                        imageVector =
                                                                                                Icons.Default.Close,
                                                                                        contentDescription =
                                                                                                "Fermer"
                                                                                )
                                                                        }
                                                                }
                                                        }
                                                }

                                                if (showEquationSelectionDialog) {
                                                        Dialog(
                                                                onDismissRequest = {
                                                                        showEquationSelectionDialog = false
                                                                },
                                                                properties =
                                                                        DialogProperties(
                                                                                usePlatformDefaultWidth =
                                                                                        false
                                                                        )
                                                        ) {
                                                                Box(
                                                                        modifier =
                                                                                Modifier.fillMaxSize()
                                                                                        .background(
                                                                                                MaterialTheme
                                                                                                        .colors
                                                                                                        .background
                                                                                        )
                                                                ) {
                                                                        when {
                                                                                isLoadingEquations -> {
                                                                                        Column(
                                                                                                modifier =
                                                                                                        Modifier.fillMaxSize(),
                                                                                                verticalArrangement =
                                                                                                        Arrangement.Center,
                                                                                                horizontalAlignment =
                                                                                                        Alignment.CenterHorizontally
                                                                                        ) {
                                                                                                CircularProgressIndicator()
                                                                                                Spacer(
                                                                                                        modifier =
                                                                                                                Modifier.height(
                                                                                                                        12.dp
                                                                                                                )
                                                                                                )
                                                                                                Text(
                                                                                                        "Chargement des equations..."
                                                                                                )
                                                                                        }
                                                                                }
                                                                                selectionLoadError != null -> {
                                                                                        Column(
                                                                                                modifier =
                                                                                                        Modifier.fillMaxSize()
                                                                                                                .padding(
                                                                                                                        24.dp
                                                                                                                ),
                                                                                                verticalArrangement =
                                                                                                        Arrangement.Center,
                                                                                                horizontalAlignment =
                                                                                                        Alignment.CenterHorizontally
                                                                                        ) {
                                                                                                Text(
                                                                                                        selectionLoadError
                                                                                                                ?: "Erreur inconnue",
                                                                                                        color =
                                                                                                                MaterialTheme
                                                                                                                        .colors
                                                                                                                        .error,
                                                                                                        style =
                                                                                                                MaterialTheme
                                                                                                                        .typography
                                                                                                                        .body1
                                                                                                )
                                                                                                Spacer(
                                                                                                        modifier =
                                                                                                                Modifier.height(
                                                                                                                        16.dp
                                                                                                                )
                                                                                                )
                                                                                                Button(
                                                                                                        onClick = {
                                                                                                                showEquationSelectionDialog =
                                                                                                                        false
                                                                                                                selectionLoadError =
                                                                                                                        null
                                                                                                        }
                                                                                                ) { Text("Fermer") }
                                                                                        }
                                                                                }
                                                                                availableEquations.isEmpty() -> {
                                                                                        Column(
                                                                                                modifier =
                                                                                                        Modifier.fillMaxSize()
                                                                                                                .padding(
                                                                                                                        24.dp
                                                                                                                ),
                                                                                                verticalArrangement =
                                                                                                        Arrangement.Center,
                                                                                                horizontalAlignment =
                                                                                                        Alignment.CenterHorizontally
                                                                                        ) {
                                                                                                Text(
                                                                                                        "Aucune equation disponible."
                                                                                                )
                                                                                                Spacer(
                                                                                                        modifier =
                                                                                                                Modifier.height(
                                                                                                                        16.dp
                                                                                                                )
                                                                                                )
                                                                                                Button(
                                                                                                        onClick = {
                                                                                                                showEquationSelectionDialog =
                                                                                                                        false
                                                                                                        }
                                                                                                ) { Text("Fermer") }
                                                                                        }
                                                                                }
                                                                                else -> {
                                                                                        val equationById =
                                                                                                remember(
                                                                                                        availableEquations
                                                                                                ) {
                                                                                                        availableEquations
                                                                                                                .associateBy {
                                                                                                                        it.uuid
                                                                                                                }
                                                                                                }
                                                                                        val equationSpecieOptions =
                                                                                                remember {
                                                                                                        listOf<FilterOption<Espece>>(
                                                                                                                FilterOption<Espece>(
                                                                                                                        label =
                                                                                                                                "Toutes",
                                                                                                                        value = null
                                                                                                                )
                                                                                                        ) +
                                                                                                                Espece
                                                                                                                        .values()
                                                                                                                        .map {
                                                                                                                                FilterOption(
                                                                                                                                        label =
                                                                                                                                                it.label,
                                                                                                                                        value =
                                                                                                                                                it
                                                                                                                                )
                                                                                                                        }
                                                                                                }
                                                                                        val equationKindOptions =
                                                                                                remember {
                                                                                                        listOf<FilterOption<EquationKind>>(
                                                                                                                FilterOption<EquationKind>(
                                                                                                                        label =
                                                                                                                                "Tous",
                                                                                                                        value = null
                                                                                                                )
                                                                                                        ) +
                                                                                                                EquationKind
                                                                                                                        .values()
                                                                                                                        .map {
                                                                                                                                FilterOption(
                                                                                                                                        label =
                                                                                                                                                it.name,
                                                                                                                                        value =
                                                                                                                                                it
                                                                                                                                )
                                                                                                                        }
                                                                                                }
                                                                                        SelectionDialog(
                                                                                                title =
                                                                                                        "Selection des equations",
                                                                                                items =
                                                                                                        availableEquations.map {
                                                                                                                val specie =
                                                                                                                        it.specie?.name
                                                                                                                                ?: "ALL"
                                                                                                                val kind =
                                                                                                                        it.kind.name
                                                                                                                SelectionItem(
                                                                                                                        id =
                                                                                                                                it.uuid,
                                                                                                                        title =
                                                                                                                                it.name.ifBlank {
                                                                                                                                        it.description.ifBlank {
                                                                                                                                                "Equation"
                                                                                                                                        }
                                                                                                                                },
                                                                                                                        subtitle =
                                                                                                                                "$kind - $specie"
                                                                                                                )
                                                                                                        },
                                                                                                initialSelectedIds =
                                                                                                        selectedEquationIds,
                                                                                                onConfirm = {
                                                                                                        ids ->
                                                                                                        selectedEquationIds =
                                                                                                                ids
                                                                                                        showEquationSelectionDialog =
                                                                                                                false
                                                                                                },
                                                                                                onDismiss = {
                                                                                                        showEquationSelectionDialog =
                                                                                                                false
                                                                                                },
                                                                                                confirmLabel =
                                                                                                        "Valider la selection",
                                                                                                emptyLabel =
                                                                                                        "Aucune equation disponible.",
                                                                                                filtersContent = {
                                                                                                        Column(
                                                                                                                verticalArrangement =
                                                                                                                        Arrangement.spacedBy(
                                                                                                                                8.dp
                                                                                                                        )
                                                                                                        ) {
                                                                                                                DropdownField(
                                                                                                                        label =
                                                                                                                                "Espece",
                                                                                                                        selectedValue =
                                                                                                                                equationFilterEspece,
                                                                                                                        options =
                                                                                                                                equationSpecieOptions,
                                                                                                                        onValueChange = {
                                                                                                                                equationFilterEspece =
                                                                                                                                        it
                                                                                                                        },
                                                                                                                        valueToString = {
                                                                                                                                it.label
                                                                                                                        },
                                                                                                                        modifier =
                                                                                                                                Modifier.fillMaxWidth()
                                                                                                                )
                                                                                                                DropdownField(
                                                                                                                        label =
                                                                                                                                "Type d'equation",
                                                                                                                        selectedValue =
                                                                                                                                equationFilterKind,
                                                                                                                        options =
                                                                                                                                equationKindOptions,
                                                                                                                        onValueChange = {
                                                                                                                                equationFilterKind =
                                                                                                                                        it
                                                                                                                        },
                                                                                                                        valueToString = {
                                                                                                                                it.label
                                                                                                                        },
                                                                                                                        modifier =
                                                                                                                                Modifier.fillMaxWidth()
                                                                                                                )
                                                                                                        }
                                                                                                },
                                                                                                filterPredicate = { item ->
                                                                                                        val equation =
                                                                                                                equationById[
                                                                                                                        item.id
                                                                                                                ]
                                                                                                        val selectedSpecie =
                                                                                                                equationFilterEspece.value
                                                                                                        val selectedKind =
                                                                                                                equationFilterKind.value
                                                                                                        val matchesSpecie =
                                                                                                                selectedSpecie ==
                                                                                                                        null ||
                                                                                                                        equation?.specie ==
                                                                                                                        selectedSpecie
                                                                                                        val matchesKind =
                                                                                                                selectedKind ==
                                                                                                                        null ||
                                                                                                                        equation?.kind ==
                                                                                                                        selectedKind
                                                                                                        matchesSpecie &&
                                                                                                                matchesKind
                                                                                                }
                                                                                        )
                                                                                }
                                                                        }

                                                                        IconButton(
                                                                                onClick = {
                                                                                        showEquationSelectionDialog =
                                                                                                false
                                                                                },
                                                                                modifier =
                                                                                        Modifier.align(
                                                                                                Alignment.TopEnd
                                                                                        )
                                                                                                .padding(
                                                                                                        16.dp
                                                                                                )
                                                                        ) {
                                                                                Icon(
                                                                                        imageVector =
                                                                                                Icons.Default.Close,
                                                                                        contentDescription =
                                                                                                "Fermer"
                                                                                )
                                                                        }
                                                                }
                                                        }
                                                }

                                                if (showReferenceSelectionDialog) {
                                                        Dialog(
                                                                onDismissRequest = {
                                                                        showReferenceSelectionDialog = false
                                                                },
                                                                properties =
                                                                        DialogProperties(
                                                                                usePlatformDefaultWidth =
                                                                                        false
                                                                        )
                                                        ) {
                                                                Box(
                                                                        modifier =
                                                                                Modifier.fillMaxSize()
                                                                                        .background(
                                                                                                MaterialTheme
                                                                                                        .colors
                                                                                                        .background
                                                                                        )
                                                                ) {
                                                                        when {
                                                                                isLoadingReferences -> {
                                                                                        Column(
                                                                                                modifier =
                                                                                                        Modifier.fillMaxSize(),
                                                                                                verticalArrangement =
                                                                                                        Arrangement.Center,
                                                                                                horizontalAlignment =
                                                                                                        Alignment.CenterHorizontally
                                                                                        ) {
                                                                                                CircularProgressIndicator()
                                                                                                Spacer(
                                                                                                        modifier =
                                                                                                                Modifier.height(
                                                                                                                        12.dp
                                                                                                                )
                                                                                                )
                                                                                                Text(
                                                                                                        "Chargement des references..."
                                                                                                )
                                                                                        }
                                                                                }
                                                                                selectionLoadError != null -> {
                                                                                        Column(
                                                                                                modifier =
                                                                                                        Modifier.fillMaxSize()
                                                                                                                .padding(
                                                                                                                        24.dp
                                                                                                                ),
                                                                                                verticalArrangement =
                                                                                                        Arrangement.Center,
                                                                                                horizontalAlignment =
                                                                                                        Alignment.CenterHorizontally
                                                                                        ) {
                                                                                                Text(
                                                                                                        selectionLoadError
                                                                                                                ?: "Erreur inconnue",
                                                                                                        color =
                                                                                                                MaterialTheme
                                                                                                                        .colors
                                                                                                                        .error,
                                                                                                        style =
                                                                                                                MaterialTheme
                                                                                                                        .typography
                                                                                                                        .body1
                                                                                                )
                                                                                                Spacer(
                                                                                                        modifier =
                                                                                                                Modifier.height(
                                                                                                                        16.dp
                                                                                                                )
                                                                                                )
                                                                                                Button(
                                                                                                        onClick = {
                                                                                                                showReferenceSelectionDialog =
                                                                                                                        false
                                                                                                                selectionLoadError =
                                                                                                                        null
                                                                                                        }
                                                                                                ) { Text("Fermer") }
                                                                                        }
                                                                                }
                                                                                availableReferences.isEmpty() -> {
                                                                                        Column(
                                                                                                modifier =
                                                                                                        Modifier.fillMaxSize()
                                                                                                                .padding(
                                                                                                                        24.dp
                                                                                                                ),
                                                                                                verticalArrangement =
                                                                                                        Arrangement.Center,
                                                                                                horizontalAlignment =
                                                                                                        Alignment.CenterHorizontally
                                                                                        ) {
                                                                                                Text(
                                                                                                        "Aucune reference disponible."
                                                                                                )
                                                                                                Spacer(
                                                                                                        modifier =
                                                                                                                Modifier.height(
                                                                                                                        16.dp
                                                                                                                )
                                                                                                )
                                                                                                Button(
                                                                                                        onClick = {
                                                                                                                showReferenceSelectionDialog =
                                                                                                                        false
                                                                                                        }
                                                                                                ) { Text("Fermer") }
                                                                                        }
                                                                                }
                                                                                else -> {
                                                                                        val referenceById =
                                                                                                remember(
                                                                                                        availableReferences
                                                                                                ) {
                                                                                                        availableReferences
                                                                                                                .associateBy {
                                                                                                                        it.uuid
                                                                                                                }
                                                                                                }
                                                                                        val referenceSpecieOptions =
                                                                                                remember {
                                                                                                        listOf<FilterOption<Espece>>(
                                                                                                                FilterOption<Espece>(
                                                                                                                        label =
                                                                                                                                "Toutes",
                                                                                                                        value = null
                                                                                                                )
                                                                                                        ) +
                                                                                                                Espece
                                                                                                                        .values()
                                                                                                                        .map {
                                                                                                                                FilterOption(
                                                                                                                                        label =
                                                                                                                                                it.label,
                                                                                                                                        value =
                                                                                                                                                it
                                                                                                                                )
                                                                                                                        }
                                                                                                }
                                                                                        SelectionDialog(
                                                                                                title =
                                                                                                        "Selection des references",
                                                                                                items =
                                                                                                        availableReferences.map {
                                                                                                                SelectionItem(
                                                                                                                        id =
                                                                                                                                it.uuid,
                                                                                                                        title =
                                                                                                                                it.nom.ifBlank {
                                                                                                                                        "Reference"
                                                                                                                                },
                                                                                                                        subtitle =
                                                                                                                                it.espece.label
                                                                                                                )
                                                                                                        },
                                                                                                initialSelectedIds =
                                                                                                        selectedReferenceIds,
                                                                                                onConfirm = {
                                                                                                        ids ->
                                                                                                        selectedReferenceIds =
                                                                                                                ids
                                                                                                        showReferenceSelectionDialog =
                                                                                                                false
                                                                                                },
                                                                                                onDismiss = {
                                                                                                        showReferenceSelectionDialog =
                                                                                                                false
                                                                                                },
                                                                                                confirmLabel =
                                                                                                        "Valider la selection",
                                                                                                emptyLabel =
                                                                                                        "Aucune reference disponible.",
                                                                                                filtersContent = {
                                                                                                        DropdownField(
                                                                                                                label =
                                                                                                                        "Espece",
                                                                                                                selectedValue =
                                                                                                                        referenceFilterEspece,
                                                                                                                options =
                                                                                                                        referenceSpecieOptions,
                                                                                                                onValueChange = {
                                                                                                                        referenceFilterEspece =
                                                                                                                                it
                                                                                                                },
                                                                                                                valueToString = {
                                                                                                                        it.label
                                                                                                                },
                                                                                                                modifier =
                                                                                                                        Modifier.fillMaxWidth()
                                                                                                        )
                                                                                                },
                                                                                                filterPredicate = { item ->
                                                                                                        val reference =
                                                                                                                referenceById[
                                                                                                                        item.id
                                                                                                                ]
                                                                                                        val selected =
                                                                                                                referenceFilterEspece.value
                                                                                                        selected == null ||
                                                                                                                reference?.espece ==
                                                                                                                        selected
                                                                                                }
                                                                                        )
                                                                                }
                                                                        }

                                                                        IconButton(
                                                                                onClick = {
                                                                                        showReferenceSelectionDialog =
                                                                                                false
                                                                                },
                                                                                modifier =
                                                                                        Modifier.align(
                                                                                                Alignment.TopEnd
                                                                                        )
                                                                                                .padding(
                                                                                                        16.dp
                                                                                                )
                                                                        ) {
                                                                                Icon(
                                                                                        imageVector =
                                                                                                Icons.Default.Close,
                                                                                        contentDescription =
                                                                                                "Fermer"
                                                                                )
                                                                        }
                                                                }
                                                        }
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
                                                                                contentDescription = "Fermer",
                                                                                tooltip = "Fermer",
                                                                                tint = Color.Gray
                                                                        )
                                                                }
                                                        }
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
                                                                                                        equationIds =
                                                                                                                selectedEquationIds
                                                                                                )
                                                                                val hasNoSelection =
                                                                                        selectedAnimalIds.isEmpty() &&
                                                                                                selectedFoodIds.isEmpty() &&
                                                                                                selectedReferenceIds.isEmpty() &&
                                                                                                selectedEquationIds.isEmpty()
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
                                                                                                        includeEquations =
                                                                                                                selectedEquationIds.isNotEmpty(),
                                                                                                        includeLinkedFromAnimals =
                                                                                                                true
                                                                                                )
                                                                                        }
                                                                                val json =
                                                                                        withContext(
                                                                                                AppDispatchers.IO
                                                                                        ) {
                                                                                                exportRepo
                                                                                                        .exportWithSelection(
                                                                                                                effectiveOptions
                                                                                                        )
                                                                                        }
                                                                                val ok =
                                                                                        withContext(
                                                                                                AppDispatchers.IO
                                                                                        ) {
                                                                                                fr.vetbrain
                                                                                                        .vetnutri_mp
                                                                                                        .exportJsonToFile(
                                                                                                                content =
                                                                                                                        json,
                                                                                                                defaultFileName =
                                                                                                                        "vetnutri_export.json"
                                                                                                        )
                                                                                        }
                                                                                // Export
                                                                                // terminé,
                                                                                // résultat
                                                                                // : $ok
                                                                        } catch (e: Exception) {
                                                                                // Erreur
                                                                                // d'export
                                                                                // gérée
                                                                        }
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

                                                // Import depuis jsonbin.io
                                                var showJsonBinImportDialog by remember {
                                                        mutableStateOf(false)
                                                }
                                                var jsonBinUrlOrId by remember {
                                                        mutableStateOf("")
                                                }
                                                
                                                Button(
                                                        onClick = {
                                                                showJsonBinImportDialog = true
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
                                                                translate(Settings.IMPORT_JSONBIN),
                                                                color = Color.White
                                                        )
                                                }
                                                
                                                // Dialogue pour saisir l'URL ou l'ID jsonbin.io
                                                if (showJsonBinImportDialog) {
                                                        AlertDialog(
                                                                onDismissRequest = {
                                                                        showJsonBinImportDialog = false
                                                                        jsonBinUrlOrId = ""
                                                                },
                                                                title = {
                                                                        Text(translate(Settings.JSONBIN_TITLE))
                                                                },
                                                                text = {
                                                                        Column(
                                                                                verticalArrangement =
                                                                                        Arrangement
                                                                                                .spacedBy(
                                                                                                        8.dp
                                                                                                )
                                                                        ) {
                                                                                Text(
                                                                                        translate(Settings.JSONBIN_MESSAGE),
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .body2
                                                                                )
                                                                                OutlinedTextField(
                                                                                        value =
                                                                                                jsonBinUrlOrId,
                                                                                        onValueChange = {
                                                                                                jsonBinUrlOrId =
                                                                                                        it
                                                                                        },
                                                                                        label = {
                                                                                                 Text(
                                                                                                         translate(Settings.JSONBIN_LABEL)
                                                                                                 )
                                                                                        },
                                                                                        placeholder = {
                                                                                                 Text(
                                                                                                         translate(Settings.JSONBIN_PLACEHOLDER)
                                                                                                 )
                                                                                        },
                                                                                        modifier =
                                                                                                Modifier.fillMaxWidth(),
                                                                                        singleLine =
                                                                                                true
                                                                                )
                                                                        }
                                                                },
                                                                confirmButton = {
                                                                        Button(
                                                                                onClick = {
                                                                                        if (jsonBinUrlOrId.isNotBlank()) {
                                                                                                coroutineScope.launch {
                                                                                                        val result =
                                                                                                                viewModel.importFromJsonBin(
                                                                                                                        jsonBinUrlOrId.trim()
                                                                                                                )
                                                                                                        viewModel.setImportResult(
                                                                                                                result
                                                                                                        )
                                                                                                        showJsonBinImportDialog =
                                                                                                                false
                                                                                                        jsonBinUrlOrId =
                                                                                                                ""
                                                                                                }
                                                                                        }
                                                                                },
                                                                                enabled =
                                                                                        jsonBinUrlOrId.isNotBlank(),
                                                                                colors =
                                                                                        ButtonDefaults.buttonColors(
                                                                                                backgroundColor =
                                                                                                        VetNutriColors
                                                                                                                .Primary
                                                                                        )
                                                                        ) {
                                                                                Text(
                                                                                        translate(General.IMPORT),
                                                                                        color =
                                                                                                Color.White
                                                                                )
                                                                        }
                                                                },
                                                                dismissButton = {
                                                                        TextButton(
                                                                                onClick = {
                                                                                        showJsonBinImportDialog =
                                                                                                false
                                                                                        jsonBinUrlOrId =
                                                                                                ""
                                                                                }
                                                                        ) {
                                                                                Text(translate(General.CANCEL))
                                                                        }
                                                                }
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
                                                                                "Résultat de l'import API"
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
                                                                                                "Aucun résultat."
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
                                                                        ) { Text("OK") }
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
                                                                "Importer des aliments",
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
                                                                                "Options d'import des aliments"
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
                                                                                                "Fusionner les nutriments (ne pas supprimer ceux absents du fichier)"
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
                                                                                                "N'importer que si la date de dernière mise à jour est plus récente"
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
                                                                                        ) {
                                                                                                // Les erreurs sont gérées par le ViewModel
                                                                                        }
                                                                                }
                                                                        ) { Text("Continuer") }
                                                                },
                                                                dismissButton = {
                                                                        Button(
                                                                                onClick = {
                                                                                        showFoodImportOptionsDialog =
                                                                                                false
                                                                                }
                                                                        ) { Text("Annuler") }
                                                                }
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
                                                                } catch (e: Exception) {
                                                                        // Les erreurs sont
                                                                        // gérées par le
                                                                        // ViewModel
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
                                                                "Importer des références nutritionnelles (.vbnr.json)",
                                                                color = Color.White
                                                        )
                                                }
                                        }
                                }
                                3 -> { // Excel Import/Export
                                        val excelFoodService = remember {
                                                viewModel.foodRepository?.let { foodRepo ->
                                                        ExcelFoodService(foodRepo)
                                                }
                                        }
                                        ExcelImportExportSection(
                                                modifier = Modifier.fillMaxWidth(),
                                                excelFoodService = excelFoodService,
                                                foodRepository = viewModel.foodRepository
                                        )
                                }
                                4 -> { // Recettes
                                        RecipeEditView(
                                                viewModel =
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
                                                        ),
                                                modifier = Modifier.fillMaxWidth()
                                        )
                                }
                                5 -> { // Administration
                                        AdministrationSettings(
                                                viewModel = viewModel,
                                                onAnimalListRefresh = onAnimalListRefresh,
                                                onFoodListRefresh = onFoodListRefresh,
                                                onBackupClick = { onBackupClick() },
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
                                        "Résultat de l'importation",
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
                                ) { Text("OK") }
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

/** Composant pour la section des préférences */
@Composable
private fun PreferencesSection(
        modifier: Modifier = Modifier,
        onSpeciesClick: (fr.vetbrain.vetnutri_mp.Enumer.Espece) -> Unit = {}
) {
        // Créer l'instance PreferencesStorage pour Desktop
        val preferencesStorage = remember {
                try {
                        // Utiliser la fonction helper createPreferencesStorage
                        fr.vetbrain.vetnutri_mp.Utils.createPreferencesStorage()
                } catch (e: Exception) {
                        null
                }
        }

        if (preferencesStorage != null) {
                // Créer le repository des préférences
                val preferencesRepository = remember {
                        fr.vetbrain.vetnutri_mp.Repository.PreferencesRepository(preferencesStorage)
                }

                // Utiliser le vrai système de persistance
                PreferencesContentWithPersistence(
                        preferencesRepository = preferencesRepository,
                        modifier = modifier,
                        onSpeciesClick = onSpeciesClick
                )
        } else {
                // Fallback temporaire si PreferencesStorage n'est pas disponible
                PreferencesContentSimplified(modifier = modifier)
        }
}

/** Contenu des préférences avec persistance réelle */
@Composable
private fun PreferencesContentWithPersistence(
        preferencesRepository: fr.vetbrain.vetnutri_mp.Repository.PreferencesRepository,
        modifier: Modifier = Modifier,
        onSpeciesClick: (fr.vetbrain.vetnutri_mp.Enumer.Espece) -> Unit = {}
) {
        // État pour les préférences chargées
        var preferencesLoaded by remember { mutableStateOf(false) }
        var currentPreferences by remember {
                mutableStateOf<fr.vetbrain.vetnutri_mp.Data.PreferencesApplication?>(null)
        }
        var isLoading by remember { mutableStateOf(false) }

        // Charger les préférences au démarrage
        LaunchedEffect(Unit) {
                try {
                        isLoading = true
                        preferencesRepository.loadPreferences()
                        currentPreferences = preferencesRepository.preferences
                        preferencesLoaded = true
                } catch (e: Exception) {
                        // Utiliser des préférences par défaut
                        currentPreferences = fr.vetbrain.vetnutri_mp.Data.PreferencesApplication()
                        preferencesLoaded = true
                } finally {
                        isLoading = false
                }
        }

        if (isLoading) {
                // Indicateur de chargement
                Box(
                        modifier = modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = VetNutriColors.Primary)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Chargement des préférences...")
                        }
                }
        } else if (preferencesLoaded && currentPreferences != null) {
                Column(
                        modifier = modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                        var nomUtilisateur by remember {
                                mutableStateOf(currentPreferences!!.nomUtilisateur)
                        }
                        var numeroOrdre by remember {
                                mutableStateOf(currentPreferences!!.numeroOrdre)
                        }
                        var adressePostale by remember {
                                mutableStateOf(currentPreferences!!.adressePostale)
                        }
                        var codePostal by remember { mutableStateOf(currentPreferences!!.codePostal) }
                        var ville by remember { mutableStateOf(currentPreferences!!.ville) }
                        var telephone by remember { mutableStateOf(currentPreferences!!.telephone) }
                        var email by remember { mutableStateOf(currentPreferences!!.email) }
                        var isSavingUser by remember { mutableStateOf(false) }
                        Text(
                                text = "Informations utilisateur",
                                style = MaterialTheme.typography.h6,
                                color = VetNutriColors.Primary
                        )
                        Card(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = Color.White,
                                elevation = 2.dp
                        ) {
                                Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                        OutlinedTextField(
                                                value = nomUtilisateur,
                                                onValueChange = { nomUtilisateur = it },
                                                label = { Text("Nom de l'utilisateur") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                        )
                                        OutlinedTextField(
                                                value = numeroOrdre,
                                                onValueChange = { numeroOrdre = it },
                                                label = { Text("Numéro d'ordre") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                        )
                                        OutlinedTextField(
                                                value = adressePostale,
                                                onValueChange = { adressePostale = it },
                                                label = { Text("Adresse postale") },
                                                singleLine = false,
                                                modifier = Modifier.fillMaxWidth()
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                OutlinedTextField(
                                                        value = codePostal,
                                                        onValueChange = { codePostal = it },
                                                        label = { Text("Code postal") },
                                                        singleLine = true,
                                                        modifier = Modifier.weight(1f)
                                                )
                                                OutlinedTextField(
                                                        value = ville,
                                                        onValueChange = { ville = it },
                                                        label = { Text("Ville") },
                                                        singleLine = true,
                                                        modifier = Modifier.weight(2f)
                                                )
                                        }
                                        OutlinedTextField(
                                                value = telephone,
                                                onValueChange = { telephone = it },
                                                label = { Text("Téléphone") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                        )
                                        OutlinedTextField(
                                                value = email,
                                                onValueChange = { email = it },
                                                label = { Text("Email") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                Button(
                                                        onClick = {
                                                                kotlinx.coroutines.GlobalScope
                                                                        .launch {
                                                                                try {
                                                                                        isSavingUser =
                                                                                                true
                                                                                        val updated =
                                                                                                currentPreferences!!
                                                                                                        .copy(
                                                                                                                nomUtilisateur =
                                                                                                                        nomUtilisateur,
                                                                                                                numeroOrdre =
                                                                                                                        numeroOrdre,
                                                                                                                adressePostale =
                                                                                                                        adressePostale,
                                                                                                                codePostal =
                                                                                                                        codePostal,
                                                                                                                ville = ville,
                                                                                                                telephone =
                                                                                                                        telephone,
                                                                                                                email =
                                                                                                                        email
                                                                                                        )
                                                                                        preferencesRepository
                                                                                                .savePreferences(
                                                                                                        updated
                                                                                                )
                                                                                        currentPreferences =
                                                                                                updated
                                                                                } catch (
                                                                                        e:
                                                                                                Exception) {} finally {
                                                                                        isSavingUser =
                                                                                                false
                                                                                }
                                                                        }
                                                        },
                                                        enabled = !isSavingUser,
                                                        colors =
                                                                ButtonDefaults.buttonColors(
                                                                        backgroundColor =
                                                                                VetNutriColors
                                                                                        .Primary
                                                                )
                                                ) { Text("Enregistrer", color = Color.White) }
                                                if (isSavingUser) {
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        CircularProgressIndicator(
                                                                modifier = Modifier.size(18.dp),
                                                                color = VetNutriColors.Primary,
                                                                strokeWidth = 2.dp
                                                        )
                                                }
                                        }
                                }
                        }
                        Text(
                                text = "Expression des besoins par espèce",
                                style = MaterialTheme.typography.h6,
                                color = VetNutriColors.Primary
                        )

                        Text(
                                text =
                                        "Définissez pour chaque espèce comment exprimer les besoins nutritionnels (sauvegarde automatique)",
                                style = MaterialTheme.typography.body2,
                                color = Color.Gray
                        )

                        // Afficher les préférences pour chaque espèce (sauf CH qui est "ALL")
                        LazyColumn(
                                modifier = Modifier.height(300.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                                items(
                                        fr.vetbrain.vetnutri_mp.Enumer.Espece.valuesExcept(
                                                fr.vetbrain.vetnutri_mp.Enumer.Espece.CH
                                        )
                                ) { espece ->
                                        SpeciesPreferenceCardWithPersistence(
                                                species = espece,
                                                preferencesRepository = preferencesRepository,
                                                currentPreferences = currentPreferences!!,
                                                onPreferencesChanged = { newPreferences ->
                                                        currentPreferences = newPreferences
                                                },
                                                onSpeciesClick = onSpeciesClick
                                        )
                                }
                        }

                        // Informations sur la persistance
                        Card(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = VetNutriColors.Primary.copy(alpha = 0.1f),
                                elevation = 1.dp
                        ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = "Persistance active",
                                                        tint = VetNutriColors.Primary
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                        text = "Persistance active",
                                                        style = MaterialTheme.typography.subtitle2,
                                                        fontWeight = FontWeight.Bold,
                                                        color = VetNutriColors.Primary
                                                )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                                text =
                                                        "Vos préférences sont automatiquement sauvegardées et seront restaurées au prochain démarrage.",
                                                style = MaterialTheme.typography.body2,
                                                color = Color.Gray
                                        )
                                }
                        }
                }
        }
}

/** Card de préférences avec persistance pour une espèce */
@Composable
private fun SpeciesPreferenceCardWithPersistence(
        species: fr.vetbrain.vetnutri_mp.Enumer.Espece,
        preferencesRepository: fr.vetbrain.vetnutri_mp.Repository.PreferencesRepository,
        currentPreferences: fr.vetbrain.vetnutri_mp.Data.PreferencesApplication,
        onPreferencesChanged: (fr.vetbrain.vetnutri_mp.Data.PreferencesApplication) -> Unit,
        onSpeciesClick: (fr.vetbrain.vetnutri_mp.Enumer.Espece) -> Unit
) {
        var expanded by remember { mutableStateOf(false) }
        var isSaving by remember { mutableStateOf(false) }

        val speciesPreferences = currentPreferences.getPreferencesEspece(species)
        val currentExpressionType = speciesPreferences.getTypeExpressionBesoinEnum()

        Card(
                modifier = Modifier.fillMaxWidth().clickable { onSpeciesClick(species) },
                backgroundColor = Color.White,
                elevation = 2.dp
        ) {
                Column(modifier = Modifier.padding(16.dp)) {
                        // En-tête avec nom de l'espèce
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Column {
                                        Text(
                                                text = species.label,
                                                style = MaterialTheme.typography.subtitle1,
                                                fontWeight = FontWeight.Bold
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                        text =
                                                                "Expression: ${currentExpressionType.displayName}",
                                                        style = MaterialTheme.typography.body2,
                                                        color = VetNutriColors.Primary
                                                )
                                                if (isSaving) {
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        CircularProgressIndicator(
                                                                modifier = Modifier.size(16.dp),
                                                                color = VetNutriColors.Primary,
                                                                strokeWidth = 2.dp
                                                        )
                                                }
                                        }
                                }

                                IconButtonWithTooltip(
                                        onClick = { expanded = !expanded },
                                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = if (expanded) "Réduire" else "Développer",
                                        tooltip = if (expanded) "Réduire" else "Développer"
                                )
                        }

                        // Contenu développable
                        if (expanded) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Divider()
                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                        text = "Type d'expression des besoins:",
                                        style = MaterialTheme.typography.subtitle2,
                                        fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Options de type d'expression
                                fr.vetbrain.vetnutri_mp.Enumer.TypeExpressionBesoin.values()
                                        .forEach { type ->
                                                Row(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .clickable(
                                                                                enabled = !isSaving
                                                                        ) {
                                                                                // Sauvegarder avec
                                                                                // persistance
                                                                                kotlinx.coroutines
                                                                                        .GlobalScope
                                                                                        .launch {
                                                                                                try {
                                                                                                        isSaving =
                                                                                                                true

                                                                                                        // Mettre à jour les préférences
                                                                                                        val updatedSpeciesPrefs =
                                                                                                                speciesPreferences
                                                                                                                        .copy(
                                                                                                                                typeExpressionBesoinId =
                                                                                                                                        type.id
                                                                                                                        )

                                                                                                        val updatedPrefs =
                                                                                                                currentPreferences
                                                                                                                        .updatePreferencesEspece(
                                                                                                                                updatedSpeciesPrefs
                                                                                                                        )

                                                                                                        // Sauvegarder dans PreferencesStorage
                                                                                                        preferencesRepository
                                                                                                                .savePreferences(
                                                                                                                        updatedPrefs
                                                                                                                )

                                                                                                        // Mettre à jour l'UI
                                                                                                        onPreferencesChanged(
                                                                                                                updatedPrefs
                                                                                                        )
                                                                                                } catch (
                                                                                                        e:
                                                                                                                Exception) {} finally {
                                                                                                        isSaving =
                                                                                                                false
                                                                                                }
                                                                                        }
                                                                        }
                                                                        .padding(vertical = 4.dp),
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        RadioButton(
                                                                selected =
                                                                        currentExpressionType ==
                                                                                type,
                                                                onClick = null, // Géré par le
                                                                // clickable du Row
                                                                enabled = !isSaving
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Column(modifier = Modifier.weight(1f)) {
                                                                Text(
                                                                        text = type.displayName,
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .body1,
                                                                        color =
                                                                                if (isSaving)
                                                                                        Color.Gray
                                                                                else Color.Black
                                                                )
                                                                Text(
                                                                        text =
                                                                                type.unitReqEnum
                                                                                        .label,
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .body2,
                                                                        color = Color.Gray
                                                                )
                                                        }
                                                }
                                        }
                        }
                }
        }
}

/** Contenu simplifié des préférences qui fonctionne sans PreferencesStorage */
@Composable
private fun PreferencesContentSimplified(modifier: Modifier = Modifier) {
        // État local pour simuler les préférences (sera remplacé par le vrai système)
        val speciesExpressionTypes = remember {
                mutableStateMapOf<
                        fr.vetbrain.vetnutri_mp.Enumer.Espece,
                        fr.vetbrain.vetnutri_mp.Enumer.TypeExpressionBesoin>()
        }
        var nomUtilisateur by remember { mutableStateOf("") }
        var numeroOrdre by remember { mutableStateOf("") }
        var adressePostale by remember { mutableStateOf("") }
        var telephone by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }

        Column(
                modifier = modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
                Text(
                        text = "Informations utilisateur",
                        style = MaterialTheme.typography.h6,
                        color = VetNutriColors.Primary
                )
                Card(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color.White,
                        elevation = 2.dp
                ) {
                        Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                                OutlinedTextField(
                                        value = nomUtilisateur,
                                        onValueChange = { nomUtilisateur = it },
                                        label = { Text("Nom de l'utilisateur") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                        value = numeroOrdre,
                                        onValueChange = { numeroOrdre = it },
                                        label = { Text("Numéro d'ordre") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                        value = adressePostale,
                                        onValueChange = { adressePostale = it },
                                        label = { Text("Adresse postale") },
                                        singleLine = false,
                                        modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                        value = telephone,
                                        onValueChange = { telephone = it },
                                        label = { Text("Téléphone") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                        value = email,
                                        onValueChange = { email = it },
                                        label = { Text("Email") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                        text =
                                                "Ces informations ne sont pas encore persistées dans ce mode.",
                                        style = MaterialTheme.typography.caption,
                                        color = Color.Gray
                                )
                        }
                }
                Text(
                        text = "Expression des besoins par espèce",
                        style = MaterialTheme.typography.h6,
                        color = VetNutriColors.Primary
                )

                Text(
                        text =
                                "Définissez pour chaque espèce comment exprimer les besoins nutritionnels",
                        style = MaterialTheme.typography.body2,
                        color = Color.Gray
                )

                // Afficher les préférences pour chaque espèce (sauf CH qui est "ALL")
                fr.vetbrain.vetnutri_mp.Enumer.Espece.valuesExcept(
                                fr.vetbrain.vetnutri_mp.Enumer.Espece.CH
                        )
                        .forEach { espece ->
                                SpeciesPreferenceCardSimplified(
                                        species = espece,
                                        currentExpressionType = speciesExpressionTypes[espece]
                                                        ?: fr.vetbrain.vetnutri_mp.Enumer
                                                                .TypeExpressionBesoin.DEFAULT,
                                        onExpressionTypeChanged = { newType ->
                                                speciesExpressionTypes[espece] = newType
                                                // TODO: Sauvegarder dans PreferencesStorage
                                        }
                                )
                        }

                // Note d'information
                Card(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = VetNutriColors.Primary.copy(alpha = 0.1f),
                        elevation = 1.dp
                ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = "Information",
                                                tint = VetNutriColors.Primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                                text = "Persistance des données",
                                                style = MaterialTheme.typography.subtitle2,
                                                fontWeight = FontWeight.Bold,
                                                color = VetNutriColors.Primary
                                        )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                        text =
                                                "Les préférences sont actuellement stockées temporairement. Le système de persistance multiplateforme sera intégré dans une prochaine version.",
                                        style = MaterialTheme.typography.body2,
                                        color = Color.Gray
                                )
                        }
                }
        }
}

/** Card de préférences simplifiée pour une espèce */
@Composable
private fun SpeciesPreferenceCardSimplified(
        species: fr.vetbrain.vetnutri_mp.Enumer.Espece,
        currentExpressionType: fr.vetbrain.vetnutri_mp.Enumer.TypeExpressionBesoin,
        onExpressionTypeChanged: (fr.vetbrain.vetnutri_mp.Enumer.TypeExpressionBesoin) -> Unit
) {
        var expanded by remember { mutableStateOf(false) }

        Card(
                modifier =
                        Modifier.fillMaxWidth().clickable {
                                onExpressionTypeChanged(currentExpressionType)
                        },
                backgroundColor = Color.White,
                elevation = 2.dp
        ) {
                Column(modifier = Modifier.padding(16.dp)) {
                        // En-tête avec nom de l'espèce
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Column {
                                        Text(
                                                text = species.label,
                                                style = MaterialTheme.typography.subtitle1,
                                                fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                                text =
                                                        "Expression: ${currentExpressionType.displayName}",
                                                style = MaterialTheme.typography.body2,
                                                color = VetNutriColors.Primary
                                        )
                                }

                                IconButtonWithTooltip(
                                        onClick = { expanded = !expanded },
                                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = if (expanded) "Réduire" else "Développer",
                                        tooltip = if (expanded) "Réduire" else "Développer"
                                )
                        }

                        // Contenu développable
                        if (expanded) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Divider()
                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                        text = "Type d'expression des besoins:",
                                        style = MaterialTheme.typography.subtitle2,
                                        fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Options de type d'expression
                                fr.vetbrain.vetnutri_mp.Enumer.TypeExpressionBesoin.values()
                                        .forEach { type ->
                                                Row(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .clickable {
                                                                                onExpressionTypeChanged(
                                                                                        type
                                                                                )
                                                                        }
                                                                        .padding(vertical = 4.dp),
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        RadioButton(
                                                                selected =
                                                                        currentExpressionType ==
                                                                                type,
                                                                onClick = {
                                                                        onExpressionTypeChanged(
                                                                                type
                                                                        )
                                                                }
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Column {
                                                                Text(
                                                                        text = type.displayName,
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .body1
                                                                )
                                                                Text(
                                                                        text =
                                                                                type.unitReqEnum
                                                                                        .label,
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .body2,
                                                                        color = Color.Gray
                                                                )
                                                        }
                                                }
                                        }
                        }
                }
        }
}
