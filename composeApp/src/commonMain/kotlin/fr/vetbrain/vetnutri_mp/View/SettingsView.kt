package fr.vetbrain.vetnutri_mp.View

// Import délégué via SettingsViewModel.importApiFromFileUI()
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Components.IconButtonWithTooltip
import fr.vetbrain.vetnutri_mp.View.SettingsComponents.SettingsTabs
import fr.vetbrain.vetnutri_mp.View.SettingsSections.AdministrationSettings
import fr.vetbrain.vetnutri_mp.View.SettingsSections.ExcelSettingsSection
import fr.vetbrain.vetnutri_mp.View.SettingsSections.ImportSettingsSection
import fr.vetbrain.vetnutri_mp.View.SettingsSections.PreferencesSection
import fr.vetbrain.vetnutri_mp.View.SettingsSections.InterfaceSettings
import fr.vetbrain.vetnutri_mp.View.SettingsSections.RecipeEditView
import fr.vetbrain.vetnutri_mp.ViewModel.ImportViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.PreferencesViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.RecipeEditViewModel
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Settings
import fr.vetbrain.vetnutri_mp.Localization.translate

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
        preferencesViewModel: PreferencesViewModel,
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
                                viewModel.getConseilsCount()
                        conseilsCount = count
                } catch (e: Exception) {
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
                                                preferencesViewModel = preferencesViewModel,
                                                modifier = Modifier.fillMaxWidth(),
                                                onSpeciesClick = onSpeciesClick
                                        )
                                }
                                2 -> { // Importation
                                        ImportSettingsSection(
                                                viewModel = viewModel,
                                                importViewModel = importViewModel,
                                                coroutineScope = coroutineScope,
                                                conseilsCount = conseilsCount,
                                                nutritionalRequirementMessage = nutritionalRequirementMessage,
                                                onImportAnimals = onImportAnimals,
                                                onAnimalListRefresh = onAnimalListRefresh
                                        )
                                }
                                3 -> { // Excel Import/Export
                                        ExcelSettingsSection(viewModel = viewModel)
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
                                                val recipeEditViewModel = remember {
                                                        viewModel.createRecipeEditViewModel()
                                                                ?: throw IllegalStateException("RecipeRepository not available")
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

