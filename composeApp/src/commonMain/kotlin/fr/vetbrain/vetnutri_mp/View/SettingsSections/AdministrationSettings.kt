package fr.vetbrain.vetnutri_mp.View.SettingsSections

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.View.SettingsComponents.DatabaseClearConfirmationDialog
import fr.vetbrain.vetnutri_mp.View.SettingsComponents.FullScreenProgressIndicator
import fr.vetbrain.vetnutri_mp.View.SettingsComponents.SettingsSection
import fr.vetbrain.vetnutri_mp.View.SettingsComponents.WarningSection
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate
import kotlinx.coroutines.launch

/**
 * Section d'administration de la base de données
 * @param viewModel ViewModel des paramètres
 * @param onAnimalListRefresh Callback pour rafraîchir la liste des animaux
 * @param onFoodListRefresh Callback pour rafraîchir la liste des aliments
 * @param modifier Modificateur appliqué au composant
 */
@Composable
fun AdministrationSettings(
        viewModel: SettingsViewModel,
        onAnimalListRefresh: () -> Unit,
        onFoodListRefresh: () -> Unit,
        onBackupClick: () -> Unit = {},
        modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    // États pour les dialogues de confirmation
    var showFoodDeleteDialog by remember { mutableStateOf(false) }
    var showAnimalDeleteDialog by remember { mutableStateOf(false) }
    var showReferenceDeleteDialog by remember { mutableStateOf(false) }
    var showEquationDeleteDialog by remember { mutableStateOf(false) }
    var showBiblioDeleteDialog by remember { mutableStateOf(false) }

    // États pour le traitement
    var isProcessing by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf("") }

    // États pour l'import automatique
    var isAutoImporting by remember { mutableStateOf(false) }
    var autoImportResult by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // Section d'administration
        SettingsSection(
                title = translate(LocalizationKeys.Administration.TITLE),
                subtitle = translate(LocalizationKeys.Administration.SUBTITLE),
                icon = Icons.Default.AdminPanelSettings,
                content = {
                    Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Bouton d'import automatique (en première place)
                        Button(
                                onClick = {
                                    isAutoImporting = true
                                    autoImportResult = null
                                    coroutineScope.launch {
                                        try {
                                            val result = viewModel.relaunchAutomaticImport(forceImport = true)
                                            when (result) {
                                                is SettingsViewModel.ImportResult.Success -> {
                                                    autoImportResult =
                                                            translate(LocalizationKeys.Administration.AUTO_IMPORT_SUCCESS, result.count.toString())
                                                    // Rafraîchir les listes après l'import
                                                    onAnimalListRefresh()
                                                    onFoodListRefresh()
                                                }
                                                is SettingsViewModel.ImportResult.Error -> {
                                                    autoImportResult =
                                                            translate(LocalizationKeys.Administration.AUTO_IMPORT_ERROR, result.message)
                                                }
                                            }
                                        } catch (e: Exception) {
                                            autoImportResult = translate(LocalizationKeys.Administration.UNEXPECTED_ERROR, e.message ?: "")
                                        } finally {
                                            isAutoImporting = false
                                        }
                                    }
                                },
                                enabled = !isAutoImporting,
                                colors =
                                        ButtonDefaults.buttonColors(
                                                backgroundColor = VetNutriColors.Primary,
                                                contentColor = Color.White
                                        ),
                                modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isAutoImporting) {
                                CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                    if (isAutoImporting) translate(LocalizationKeys.Administration.AUTO_IMPORT_RUNNING)
                                    else translate(LocalizationKeys.Administration.AUTO_IMPORT_ACTION)
                            )
                        }

                        // Bouton de gestion des sauvegardes
                        Button(
                                onClick = onBackupClick,
                                colors = ButtonDefaults.buttonColors(
                                        backgroundColor = VetNutriColors.Secondary,
                                        contentColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                    imageVector = Icons.Default.Backup,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(translate(LocalizationKeys.Administration.MANAGE_BACKUPS))
                        }

                        // Affichage du résultat de l'import automatique
                        autoImportResult?.let { result ->
                            Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    backgroundColor =
                                            if (result.startsWith("✅"))
                                                    VetNutriColors.Primary.copy(alpha = 0.1f)
                                            else VetNutriColors.Error.copy(alpha = 0.1f),
                                    elevation = 1.dp
                            ) {
                                Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                            text = result,
                                            style = MaterialTheme.typography.body2,
                                            color =
                                                    if (result.startsWith("✅"))
                                                            VetNutriColors.Primary
                                                    else VetNutriColors.Error,
                                            modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = { autoImportResult = null }) {
                                        Icon(
                                                Icons.Default.Delete,
                                                contentDescription = translate(LocalizationKeys.AnalNut.CLOSE),
                                                tint = Color.Gray
                                        )
                                    }
                                }
                            }
                        }

                        // Avertissement général
                        WarningSection(
                                title = translate(LocalizationKeys.Administration.IRREVERSIBLE_TITLE),
                                message = translate(LocalizationKeys.Administration.IRREVERSIBLE_MSG)
                        )

                        // Boutons d'action
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Supprimer les aliments
                            Button(
                                    onClick = { showFoodDeleteDialog = true },
                                    colors =
                                            ButtonDefaults.buttonColors(
                                                    backgroundColor = VetNutriColors.Error,
                                                    contentColor = Color.White
                                            ),
                                    modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(translate(LocalizationKeys.Administration.CLEAR_FOODS))
                            }

                            // Supprimer les animaux
                            Button(
                                    onClick = { showAnimalDeleteDialog = true },
                                    colors =
                                            ButtonDefaults.buttonColors(
                                                    backgroundColor = VetNutriColors.Error,
                                                    contentColor = Color.White
                                            ),
                                    modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(translate(LocalizationKeys.Administration.CLEAR_ANIMALS))
                            }

                            // Supprimer les références nutritionnelles
                            Button(
                                    onClick = { showReferenceDeleteDialog = true },
                                    colors =
                                            ButtonDefaults.buttonColors(
                                                    backgroundColor = VetNutriColors.Error,
                                                    contentColor = Color.White
                                            ),
                                    modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(translate(LocalizationKeys.Administration.CLEAR_REFS))
                            }

                            // Supprimer les équations
                            Button(
                                    onClick = { showEquationDeleteDialog = true },
                                    colors =
                                            ButtonDefaults.buttonColors(
                                                    backgroundColor = VetNutriColors.Error,
                                                    contentColor = Color.White
                                            ),
                                    modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(translate(LocalizationKeys.Administration.CLEAR_EQUATIONS))
                            }

                            // Supprimer les bibliographies
                            Button(
                                    onClick = { showBiblioDeleteDialog = true },
                                    colors =
                                            ButtonDefaults.buttonColors(
                                                    backgroundColor = VetNutriColors.Error,
                                                    contentColor = Color.White
                                            ),
                                    modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(translate(LocalizationKeys.Administration.CLEAR_BIBLIO))
                            }
                        }
                    }
                }
        )
    }

    // Dialogues de confirmation
    if (showFoodDeleteDialog) {
        DatabaseClearConfirmationDialog(
                entityName = "aliments",
                onConfirm = {
                    showFoodDeleteDialog = false
                    isProcessing = true
                    coroutineScope.launch {
                        try {
                            val count = viewModel.clearAllFoods()
                            resultMessage = "$count aliments ont été supprimés avec succès."
                            onFoodListRefresh()
                        } catch (e: Exception) {
                            resultMessage = "Erreur lors de la suppression : ${e.message}"
                        } finally {
                            isProcessing = false
                        }
                    }
                },
                onDismiss = { showFoodDeleteDialog = false }
        )
    }

    if (showAnimalDeleteDialog) {
        DatabaseClearConfirmationDialog(
                entityName = "animaux",
                onConfirm = {
                    showAnimalDeleteDialog = false
                    isProcessing = true
                    coroutineScope.launch {
                        try {
                            val count = viewModel.clearAllAnimals()
                            resultMessage = "$count animaux ont été supprimés avec succès."
                            onAnimalListRefresh()
                        } catch (e: Exception) {
                            resultMessage = "Erreur lors de la suppression : ${e.message}"
                        } finally {
                            isProcessing = false
                        }
                    }
                },
                onDismiss = { showAnimalDeleteDialog = false }
        )
    }

    if (showReferenceDeleteDialog) {
        DatabaseClearConfirmationDialog(
                entityName = "références nutritionnelles, équations et bibliographies",
                onConfirm = {
                    showReferenceDeleteDialog = false
                    isProcessing = true
                    coroutineScope.launch {
                        try {
                            var refCount = 0
                            var eqCount = 0
                            var bibCount = 0

                            try {
                                refCount = viewModel.clearAllReferences()
                            } catch (e: Exception) {}
                            try {
                                eqCount = viewModel.clearAllEquations()
                            } catch (e: Exception) {}
                            try {
                                bibCount = viewModel.clearAllBiblioRefs()
                            } catch (e: Exception) {}

                            resultMessage =
                                    "$refCount références nutritionnelles, $eqCount équations et $bibCount bibliographies ont été supprimées avec succès."
                        } catch (e: Exception) {
                            resultMessage = "Erreur lors de la suppression : ${e.message}"
                        } finally {
                            isProcessing = false
                        }
                    }
                },
                onDismiss = { showReferenceDeleteDialog = false }
        )
    }

    if (showEquationDeleteDialog) {
        DatabaseClearConfirmationDialog(
                entityName = "équations",
                onConfirm = {
                    showEquationDeleteDialog = false
                    isProcessing = true
                    coroutineScope.launch {
                        try {
                            val count = viewModel.clearAllEquations()
                            resultMessage = "$count équations ont été supprimées avec succès."
                        } catch (e: Exception) {
                            resultMessage = "Erreur lors de la suppression : ${e.message}"
                        } finally {
                            isProcessing = false
                        }
                    }
                },
                onDismiss = { showEquationDeleteDialog = false }
        )
    }

    if (showBiblioDeleteDialog) {
        DatabaseClearConfirmationDialog(
                entityName = "références bibliographiques",
                onConfirm = {
                    showBiblioDeleteDialog = false
                    isProcessing = true
                    coroutineScope.launch {
                        try {
                            val count = viewModel.clearAllBiblioRefs()
                            resultMessage =
                                    "$count références bibliographiques ont été supprimées avec succès."
                        } catch (e: Exception) {
                            resultMessage = "Erreur lors de la suppression : ${e.message}"
                        } finally {
                            isProcessing = false
                        }
                    }
                },
                onDismiss = { showBiblioDeleteDialog = false }
        )
    }

    // Indicateur de progression
    FullScreenProgressIndicator(isVisible = isProcessing)

    // Affichage du résultat
    if (resultMessage.isNotEmpty()) {
        Snackbar(
                modifier = Modifier.padding(16.dp),
                action = { TextButton(onClick = { resultMessage = "" }) { Text("OK") } }
        ) { Text(resultMessage) }
    }
}
