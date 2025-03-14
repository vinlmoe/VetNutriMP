package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.ViewModel.ImportViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.rememberViewModel

@Composable
fun ImportView(viewModel: ImportViewModel = rememberViewModel(), onNavigateBack: () -> Unit) {
    var showImportDialog by remember { mutableStateOf(false) }
    var showClearAndImportDialog by remember { mutableStateOf(false) }
    val isImporting by viewModel.isImporting.collectAsState()
    val importResult by viewModel.importResult.collectAsState()

    // Effet pour gérer l'importation lorsque showImportDialog devient true
    LaunchedEffect(showImportDialog) {
        if (showImportDialog) {
            viewModel.importAnimalsFromFileUI()
            showImportDialog = false
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
                                Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
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

            // Afficher le résultat de l'importation s'il y en a un
            importResult?.let { result ->
                when (result) {
                    is ImportViewModel.ImportResult.Success -> {
                        Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.1f)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                        "Importation réussie : ${result.animalCount} animaux importés",
                                        style = MaterialTheme.typography.subtitle1,
                                        color = MaterialTheme.colors.primary
                                )
                                if (result.foodCount > 0) {
                                    Text(
                                            "${result.foodCount} aliments importés",
                                            style = MaterialTheme.typography.body2
                                    )
                                }
                            }
                        }
                    }
                    is ImportViewModel.ImportResult.Error -> {
                        Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                backgroundColor = MaterialTheme.colors.error.copy(alpha = 0.1f)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                        "Erreur lors de l'importation",
                                        style = MaterialTheme.typography.subtitle1,
                                        color = MaterialTheme.colors.error
                                )
                                Text(result.message, style = MaterialTheme.typography.body2)
                            }
                        }
                    }
                }
            }

            Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                // Bouton d'importation standard
                Button(
                        onClick = { showImportDialog = true },
                        enabled = !isImporting,
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
                    Text("Importer fichier")
                }

                // Bouton pour vider la base et importer
                Button(
                        onClick = { showClearAndImportDialog = true },
                        enabled = !isImporting,
                        colors =
                                ButtonDefaults.buttonColors(
                                        backgroundColor = MaterialTheme.colors.error
                                )
                ) { Text("Vider DB et importer") }
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
