package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.Section
import fr.vetbrain.vetnutri_mp.Components.TopBar
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/**
 * Dialogue simple pour les paramètres d'affichage
 * @param viewModel Le ViewModel des paramètres
 * @param onDismiss Callback appelé lorsque l'utilisateur ferme le dialogue
 */
@Composable
fun SettingsDialog(viewModel: SettingsViewModel, onDismiss: () -> Unit) {
        val uiScale by viewModel.uiScale.collectAsState()

        AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Paramètres d'affichage", style = MaterialTheme.typography.h6) },
                text = {
                        Column(
                                modifier = Modifier.padding(AppSizes.paddingMedium),
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                        ) {
                                Text(
                                        "Taille de l'interface",
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
                        ) { Text("Fermer") }
                },
                backgroundColor = MaterialTheme.colors.surface
        )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SettingsView(viewModel: SettingsViewModel, onImportAnimals: () -> Unit, onBack: () -> Unit) {
        // État pour le dialogue de confirmation de suppression
        var isDialogVisible by remember { mutableStateOf(false) }
        var isProcessing by remember { mutableStateOf(false) }
        var resultMessage by remember { mutableStateOf("") }
        val coroutineScope = rememberCoroutineScope()
        val uiScale by viewModel.uiScale.collectAsState()

        Column(
                modifier = Modifier.fillMaxSize().padding(AppSizes.paddingMedium),
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
        ) {
                TopBar(title = "Paramètres", onBackClick = onBack, onSettingsClick = {})

                // Section pour l'échelle de l'interface
                Section(title = "Échelle de l'interface") {
                        Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
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

                // Section pour l'importation des données
                Section(title = "Importation des données") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                        onClick = onImportAnimals,
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor = VetNutriColors.Primary
                                                ),
                                        modifier = Modifier.fillMaxWidth()
                                ) { Text("Importer des animaux", color = Color.White) }

                                // Bouton pour importer des aliments avec lambda pour éviter
                                // l'ambiguïté
                                Button(
                                        onClick = {
                                                try {
                                                        // Utilisons la méthode du ViewModel qui
                                                        // encapsule l'appel à importFoodsFromFile
                                                        viewModel.importFoodsFromFileUI()
                                                } catch (e: Exception) {
                                                        resultMessage =
                                                                "Erreur lors de l'importation : ${e.message}"
                                                }
                                        },
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor = VetNutriColors.Primary
                                                ),
                                        modifier = Modifier.fillMaxWidth()
                                ) { Text("Importer des aliments", color = Color.White) }
                        }
                }

                // Section pour l'administration de la base de données
                Section(title = "Administration de la base de données") {
                        Button(
                                onClick = { isDialogVisible = true },
                                colors =
                                        ButtonDefaults.buttonColors(
                                                backgroundColor = VetNutriColors.Error,
                                                contentColor = Color.White
                                        ),
                                modifier = Modifier.fillMaxWidth()
                        ) { Text("Vider la base de données des aliments") }
                }

                // Dialogue de confirmation pour vider la base de données
                if (isDialogVisible) {
                        AlertDialog(
                                onDismissRequest = { isDialogVisible = false },
                                title = { Text("Confirmation") },
                                text = {
                                        Text(
                                                "Êtes-vous sûr de vouloir supprimer TOUS les aliments de la base de données ? Cette action est irréversible."
                                        )
                                },
                                confirmButton = {
                                        Button(
                                                onClick = {
                                                        isDialogVisible = false
                                                        isProcessing = true
                                                        coroutineScope.launch {
                                                                try {
                                                                        val count =
                                                                                viewModel
                                                                                        .clearAllFoods()
                                                                        resultMessage =
                                                                                "$count aliments ont été supprimés avec succès."
                                                                } catch (e: Exception) {
                                                                        resultMessage =
                                                                                "Erreur lors de la suppression : ${e.message}"
                                                                } finally {
                                                                        isProcessing = false
                                                                }
                                                        }
                                                },
                                                colors =
                                                        ButtonDefaults.buttonColors(
                                                                backgroundColor =
                                                                        VetNutriColors.Error,
                                                                contentColor = Color.White
                                                        )
                                        ) { Text("Oui, vider la base") }
                                },
                                dismissButton = {
                                        Button(onClick = { isDialogVisible = false }) {
                                                Text("Annuler")
                                        }
                                }
                        )
                }

                // Affichage du résultat
                if (resultMessage.isNotEmpty()) {
                        Snackbar(
                                modifier = Modifier.padding(16.dp),
                                action = {
                                        TextButton(onClick = { resultMessage = "" }) { Text("OK") }
                                }
                        ) { Text(resultMessage) }
                }

                // Indicateur de progression pendant le traitement
                if (isProcessing) {
                        Box(
                                modifier =
                                        Modifier.fillMaxSize()
                                                .background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator(color = VetNutriColors.Primary) }
                }
        }
}
