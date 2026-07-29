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
import fr.vetbrain.vetnutri_mp.Components.IconButtonWithTooltip
import fr.vetbrain.vetnutri_mp.ViewModel.BackupRestoreViewModel
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate

/**
 * Vue sauvegardes/restauration.
 * - Liste les backups, création, suppression, restauration avec dialogues de confirmation.
 * - Affiche progression/log et bilan de restauration.
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
    val restoreLog = viewModel.restoreLog.collectAsState().value
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
                IconButtonWithTooltip(
                    onClick = onBack,
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = translate(LocalizationKeys.Backup.BACK),
                    tooltip = translate(LocalizationKeys.Backup.BACK)
                )
                Text(translate(LocalizationKeys.Backup.TITLE), style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { viewModel.createManualBackup() },
                enabled = !isLoading && !isRestoring
            ) {
                Icon(Icons.Default.Backup, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(translate(LocalizationKeys.Backup.NEW_BACKUP_ACTION))
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
                            contentDescription = translate(LocalizationKeys.General.CLOSE),
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
                    Text(translate(LocalizationKeys.Backup.RESTORE_IN_PROGRESS), style = MaterialTheme.typography.subtitle1, fontWeight = FontWeight.Bold)
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
                    Text(translate("auto.view.backuprestoreview.aucune_sauvegarde_disponible"), style = MaterialTheme.typography.subtitle1, color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(translate("auto.view.backuprestoreview.creez_votre_premiere_sauvegarde_en_cliquant_sur"), style = MaterialTheme.typography.body2, color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f), textAlign = TextAlign.Center)
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
            title = { Text(translate("auto.view.backuprestoreview.supprimer_la_sauvegarde")) },
            text = { 
                Text(translate("auto.view.backuprestoreview.etes_vous_sur_de_vouloir_supprimer_la_sauvegarde_d", (viewModel.formatDate(backup.createdAt)).toString()))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteBackup(backup)
                        showDeleteDialog = null
                    }
                ) {
                    Text(translate("general.delete"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text(translate("general.cancel"))
                }
            }
        )
    }
    
    // Dialog de confirmation de restauration
    showRestoreDialog?.let { backup ->
        AlertDialog(
            onDismissRequest = { showRestoreDialog = null },
            title = { Text(translate("auto.view.backuprestoreview.restaurer_la_sauvegarde")) },
            text = { 
                Column {
                    Text(translate("auto.view.backuprestoreview.etes_vous_sur_de_vouloir_restaurer_la_sauvegarde_d", (viewModel.formatDate(backup.createdAt)).toString()))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(translate("auto.view.backuprestoreview.cette_action_remplacera_toutes_les_donnees_actue"), style = MaterialTheme.typography.body2, color = MaterialTheme.colors.error)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.restoreBackup(backup)
                        showRestoreDialog = null
                    }
                ) {
                    Text(translate("auto.view.backuprestoreview.restaurer"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = null }) {
                    Text(translate("general.cancel"))
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
                    translate("auto.view.backuprestoreview.restauration_terminee"),
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.primary
                )
            },
            text = { 
                Column {
                    Text(
                        translate("auto.view.backuprestoreview.la_sauvegarde_a_ete_restauree_avec_succes"),
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
                                translate("auto.view.backuprestoreview.bilan_de_l_import"),
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
                                    Text(translate("auto.view.backuprestoreview.animaux"), style = MaterialTheme.typography.body2)
                                }
                                Column {
                                    Text("${importCounts.foods}", style = MaterialTheme.typography.h6, color = MaterialTheme.colors.primary)
                                    Text(translate("database.stat.foods"), style = MaterialTheme.typography.body2)
                                }
                                Column {
                                    Text("${importCounts.equations}", style = MaterialTheme.typography.h6, color = MaterialTheme.colors.primary)
                                    Text(translate("new_reference.tab.equations"), style = MaterialTheme.typography.body2)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("${importCounts.conseils}", style = MaterialTheme.typography.h6, color = MaterialTheme.colors.primary)
                                    Text(translate("database.stat.conseils"), style = MaterialTheme.typography.body2)
                                }
                                Column {
                                    Text("${importCounts.rations}", style = MaterialTheme.typography.h6, color = MaterialTheme.colors.primary)
                                    Text(translate("crossConsultation.rationsLabel"), style = MaterialTheme.typography.body2)
                                }
                                Column {
                                    Text("${importCounts.recipes}", style = MaterialTheme.typography.h6, color = MaterialTheme.colors.primary)
                                    Text(translate("settings.tabRecipes"), style = MaterialTheme.typography.body2)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("${importCounts.references}", style = MaterialTheme.typography.h6, color = MaterialTheme.colors.primary)
                                    Text(translate("database.stat.references"), style = MaterialTheme.typography.body2)
                                }
                                Column {
                                    Text("${importCounts.biblios}", style = MaterialTheme.typography.h6, color = MaterialTheme.colors.primary)
                                    Text(translate("auto.view.backuprestoreview.biblios"), style = MaterialTheme.typography.body2)
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
                    Text(translate("general.ok"))
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
                        Icon(Icons.Default.Restore, contentDescription = translate("auto.view.backuprestoreview.restaurer"), tint = MaterialTheme.colors.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = translate("general.delete"), tint = MaterialTheme.colors.error)
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
            Text(translate("auto.view.backuprestoreview.taille_arg", (viewModel.formatFileSize(backup.fileSize)).toString()), style = MaterialTheme.typography.body2, color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
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
