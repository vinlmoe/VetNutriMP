package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Service.BackupService.BackupMetadata
import fr.vetbrain.vetnutri_mp.ViewModel.BackupRestoreViewModel

/**
 * Vue pour la gestion des sauvegardes et de la restauration
 */
@Composable
fun BackupRestoreView(
    viewModel: BackupRestoreViewModel,
    onBack: () -> Unit
) {
    val backups = viewModel.backups.collectAsState().value
    val isLoading = viewModel.isLoading.collectAsState().value
    val error = viewModel.error.collectAsState().value
    val isRestoring = viewModel.isRestoring.collectAsState().value
    val restoreProgress = viewModel.restoreProgress.collectAsState().value
    val showRestoreResultDialog = viewModel.showRestoreResultDialog.collectAsState().value
    
    var showDeleteDialog by remember { mutableStateOf<BackupMetadata?>(null) }
    var showRestoreDialog by remember { mutableStateOf<BackupMetadata?>(null) }
    
    // Effacer l'erreur au montage
    LaunchedEffect(Unit) {
        viewModel.clearError()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // En-tête
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Retour")
                }
                Text("Sauvegardes et Restauration", style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)
            }
            
            Button(
                onClick = { viewModel.createManualBackup() },
                enabled = !isLoading && !isRestoring
            ) {
                Icon(Icons.Default.Backup, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nouvelle sauvegarde")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Message d'erreur
        error?.let { errorMessage ->
            Card(modifier = Modifier.fillMaxWidth(), backgroundColor = MaterialTheme.colors.error.copy(alpha = 0.15f)) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colors.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = errorMessage, color = MaterialTheme.colors.error)
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { viewModel.clearError() }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Fermer",
                            tint = MaterialTheme.colors.error
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Progrès de restauration
        if (isRestoring) {
            Card(modifier = Modifier.fillMaxWidth(), backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.12f)) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Restauration en cours...", style = MaterialTheme.typography.subtitle1, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(progress = restoreProgress.toFloat(), modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(restoreLog, style = MaterialTheme.typography.body2)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Liste des sauvegardes
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (backups.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Aucune sauvegarde disponible", style = MaterialTheme.typography.subtitle1, color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Créez votre première sauvegarde en cliquant sur le bouton ci-dessus", style = MaterialTheme.typography.body2, color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f), textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(backups) { backup ->
                    BackupItem(
                        backup = backup,
                        onRestore = { showRestoreDialog = backup },
                        onDelete = { showDeleteDialog = backup },
                        viewModel = viewModel
                    )
                }
            }
        }
    }
    
    // Dialog de confirmation de suppression
    showDeleteDialog?.let { backup ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Supprimer la sauvegarde") },
            text = { 
                Text("Êtes-vous sûr de vouloir supprimer la sauvegarde du ${viewModel.formatDate(backup.createdAt)} ?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteBackup(backup)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Annuler")
                }
            }
        )
    }
    
    // Dialog de confirmation de restauration
    showRestoreDialog?.let { backup ->
        AlertDialog(
            onDismissRequest = { showRestoreDialog = null },
            title = { Text("Restaurer la sauvegarde") },
            text = { 
                Column {
                    Text("Êtes-vous sûr de vouloir restaurer la sauvegarde du ${viewModel.formatDate(backup.createdAt)} ?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Cette action remplacera toutes les données actuelles par celles de la sauvegarde.", style = MaterialTheme.typography.body2, color = MaterialTheme.colors.error)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.restoreBackup(backup)
                        showRestoreDialog = null
                    }
                ) {
                    Text("Restaurer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = null }) {
                    Text("Annuler")
                }
            }
        )
    }
    
    // Dialog de bilan de restauration
    showRestoreResultDialog?.let { importCounts ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissRestoreResultDialog() },
            title = { 
                Text(
                    "Restauration terminée",
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.primary
                )
            },
            text = { 
                Column {
                    Text(
                        "La sauvegarde a été restaurée avec succès !",
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Card(
                        elevation = 4.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "Bilan de l'import :",
                                style = MaterialTheme.typography.subtitle1,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("${importCounts.animals}", style = MaterialTheme.typography.h6, color = MaterialTheme.colors.primary)
                                    Text("Animaux", style = MaterialTheme.typography.body2)
                                }
                                Column {
                                    Text("${importCounts.foods}", style = MaterialTheme.typography.h6, color = MaterialTheme.colors.primary)
                                    Text("Aliments", style = MaterialTheme.typography.body2)
                                }
                                Column {
                                    Text("${importCounts.equations}", style = MaterialTheme.typography.h6, color = MaterialTheme.colors.primary)
                                    Text("Équations", style = MaterialTheme.typography.body2)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("${importCounts.conseils}", style = MaterialTheme.typography.h6, color = MaterialTheme.colors.primary)
                                    Text("Conseils", style = MaterialTheme.typography.body2)
                                }
                                Column {
                                    Text("${importCounts.rations}", style = MaterialTheme.typography.h6, color = MaterialTheme.colors.primary)
                                    Text("Rations", style = MaterialTheme.typography.body2)
                                }
                                Column {
                                    Text("${importCounts.recipes}", style = MaterialTheme.typography.h6, color = MaterialTheme.colors.primary)
                                    Text("Recettes", style = MaterialTheme.typography.body2)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("${importCounts.references}", style = MaterialTheme.typography.h6, color = MaterialTheme.colors.primary)
                                    Text("Références", style = MaterialTheme.typography.body2)
                                }
                                Column {
                                    Text("${importCounts.biblios}", style = MaterialTheme.typography.h6, color = MaterialTheme.colors.primary)
                                    Text("Biblios", style = MaterialTheme.typography.body2)
                                }
                                Column { } // Colonne vide pour l'alignement
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.dismissRestoreResultDialog() }
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun BackupItem(
    backup: BackupMetadata,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    viewModel: BackupRestoreViewModel
) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // En-tête avec date et actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(viewModel.formatDate(backup.createdAt), style = MaterialTheme.typography.subtitle1, fontWeight = FontWeight.Bold)
                    Text(
                        text = backup.fileName,
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                Row {
                    IconButton(onClick = onRestore) {
                        Icon(Icons.Default.Restore, contentDescription = "Restaurer", tint = MaterialTheme.colors.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = MaterialTheme.colors.error)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Statistiques
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("Animaux", backup.animalCount.toString())
                StatItem("Aliments", backup.foodCount.toString())
                StatItem("Équations", backup.equationCount.toString())
                StatItem("Conseils", backup.conseilCount.toString())
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Taille du fichier
            Text("Taille: ${viewModel.formatFileSize(backup.fileSize)}", style = MaterialTheme.typography.body2, color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, style = MaterialTheme.typography.subtitle1, fontWeight = FontWeight.Bold, color = MaterialTheme.colors.primary)
        Text(label, style = MaterialTheme.typography.body2, color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
    }
}
