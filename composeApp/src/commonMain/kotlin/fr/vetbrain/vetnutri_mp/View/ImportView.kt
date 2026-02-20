package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.ViewModel.ImportViewModel

/**
 * Vue d'import (animaux + références nutritionnelles).
 * - Déclenche les imports plateforme via `ImportViewModel`.
 * - Affiche les messages de résultat et confirmations (vider DB, import réf. .vbnr.json).
 */
@Composable
fun ImportView(viewModel: ImportViewModel, onNavigateBack: () -> Unit) {
    var showImportDialog by remember { mutableStateOf(false) }
    var showClearAndImportDialog by remember { mutableStateOf(false) }
    var showNutritionalRequirementImportDialog by remember { mutableStateOf(false) }

    val isImporting by viewModel.isImporting.collectAsState()
    val isImportingNutritionalRequirements by
            viewModel.isImportingNutritionalRequirements.collectAsState()
    val nutritionalRequirementImportResult by
            viewModel.nutritionalRequirementImportResultMessage.collectAsState()

    // Effet pour gérer l'importation lorsque showImportDialog devient true
    LaunchedEffect(showImportDialog) {
        if (showImportDialog) {
            viewModel.importAnimalsFromFileUI()
            showImportDialog = false
        }
    }

    // Effet pour gérer l'importation des références nutritionnelles
    LaunchedEffect(showNutritionalRequirementImportDialog) {
        if (showNutritionalRequirementImportDialog) {
            viewModel.importNutritionalRequirementsFromFileUI()
            showNutritionalRequirementImportDialog = false
        }
    }

    // Effet pour réinitialiser le résultat d'importation lorsque la vue est affichée
    LaunchedEffect(Unit) { viewModel.resetImportResult() }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Importer des données") },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Retour")
                            }
                        }
                )
            }
    ) { paddingValues ->
        Column(
                modifier = Modifier.padding(paddingValues).fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Afficher le résultat de l'importation des références nutritionnelles
            nutritionalRequirementImportResult?.let { result ->
                Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        backgroundColor =
                                if (result.startsWith("✅"))
                                        MaterialTheme.colors.primary.copy(alpha = 0.1f)
                                else MaterialTheme.colors.error.copy(alpha = 0.1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                                if (result.startsWith("✅")) "Références nutritionnelles"
                                else "Erreur références nutritionnelles",
                                style = MaterialTheme.typography.subtitle1,
                                color =
                                        if (result.startsWith("✅")) MaterialTheme.colors.primary
                                        else MaterialTheme.colors.error
                        )
                        Text(result, style = MaterialTheme.typography.body2)
                    }
                }
            }

            // Section Importation d'animaux
            Text(
                    "Importation d'animaux",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 24.dp)
            ) {
                // Bouton d'importation standard
                Button(
                        onClick = { showImportDialog = true },
                        enabled = !isImporting && !isImportingNutritionalRequirements,
                        colors =
                                ButtonDefaults.buttonColors(
                                        backgroundColor = MaterialTheme.colors.primary
                                )
                ) {
                    if (isImporting) {
                        CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colors.onPrimary,
                                strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Importer animaux")
                }

                // Bouton pour vider la base et importer
                Button(
                        onClick = { showClearAndImportDialog = true },
                        enabled = !isImporting && !isImportingNutritionalRequirements,
                        colors =
                                ButtonDefaults.buttonColors(
                                        backgroundColor = MaterialTheme.colors.error
                                )
                ) { Text("Vider DB et importer") }
            }

            // Section Importation des références nutritionnelles
            Text(
                    "Importation des références nutritionnelles",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(bottom = 8.dp)
            )

            Button(
                    onClick = { showNutritionalRequirementImportDialog = true },
                    enabled = !isImporting && !isImportingNutritionalRequirements,
                    colors =
                            ButtonDefaults.buttonColors(
                                    backgroundColor = MaterialTheme.colors.secondary
                            ),
                    modifier = Modifier.padding(bottom = 16.dp)
            ) {
                if (isImportingNutritionalRequirements) {
                    CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colors.onSecondary,
                            strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Importer références (.vbnr.json)")
            }

            // Informations sur les formats de fichiers
            Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colors.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                            "Formats de fichiers supportés",
                            style = MaterialTheme.typography.subtitle1,
                            modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text("• Animaux: .json (format standard)")
                    Text("• Références nutritionnelles: .vbnr.json")
                }
            }
        }

        // Dialog de confirmation pour vider la base et importer
        if (showClearAndImportDialog) {
            AlertDialog(
                    onDismissRequest = { showClearAndImportDialog = false },
                    title = { Text("Attention") },
                    text = {
                        Text(
                                "Cette action va supprimer tous les aliments existants dans la base de données avant d'importer le fichier. Êtes-vous sûr de vouloir continuer?"
                        )
                    },
                    confirmButton = {
                        Button(
                                onClick = {
                                    showClearAndImportDialog = false
                                    // Activer le flag pour supprimer les aliments avant
                                    // l'importation
                                    viewModel.shouldClearFoodsBeforeImport = true
                                    // Déclencher l'importation
                                    showImportDialog = true
                                },
                                colors =
                                        ButtonDefaults.buttonColors(
                                                backgroundColor = MaterialTheme.colors.error
                                        )
                        ) { Text("Vider et importer") }
                    },
                    dismissButton = {
                        Button(onClick = { showClearAndImportDialog = false }) { Text("Annuler") }
                    }
            )
        }
    }
}
