package fr.vetbrain.vetnutri_mp.View.SettingsSections

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.View.SettingsComponents.SettingsSection
import fr.vetbrain.vetnutri_mp.View.SettingsComponents.WarningSection
import fr.vetbrain.vetnutri_mp.View.SettingsComponents.DatabaseClearConfirmationDialog
import fr.vetbrain.vetnutri_mp.View.SettingsComponents.FullScreenProgressIndicator
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel
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

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // Section d'administration
        SettingsSection(
            title = "Administration de la base de données",
            subtitle = "Actions d'administration et de maintenance de la base de données",
            icon = Icons.Default.AdminPanelSettings,
            content = {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Avertissement général
                    WarningSection(
                        title = "⚠️ Actions irréversibles",
                        message = "Ces actions sont irréversibles et supprimeront définitivement les données. Utilisez avec précaution."
                    )

                    // Boutons d'action
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Supprimer les aliments
                        Button(
                            onClick = { showFoodDeleteDialog = true },
                            colors = ButtonDefaults.buttonColors(
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
                            Text("Vider la base de données des aliments")
                        }

                        // Supprimer les animaux
                        Button(
                            onClick = { showAnimalDeleteDialog = true },
                            colors = ButtonDefaults.buttonColors(
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
                            Text("Vider la base de données des animaux")
                        }

                        // Supprimer les références nutritionnelles
                        Button(
                            onClick = { showReferenceDeleteDialog = true },
                            colors = ButtonDefaults.buttonColors(
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
                            Text("Vider la base de données des références nutritionnelles")
                        }

                        // Supprimer les équations
                        Button(
                            onClick = { showEquationDeleteDialog = true },
                            colors = ButtonDefaults.buttonColors(
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
                            Text("Vider la base de données des équations")
                        }

                        // Supprimer les bibliographies
                        Button(
                            onClick = { showBiblioDeleteDialog = true },
                            colors = ButtonDefaults.buttonColors(
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
                            Text("Vider la base de données des bibliographies")
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
