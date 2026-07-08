package fr.vetbrain.vetnutri_mp.View

import fr.vetbrain.vetnutri_mp.Localization.translate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import fr.vetbrain.vetnutri_mp.Service.BackupService.BackupMetadata
import fr.vetbrain.vetnutri_mp.ViewModel.BackupRestoreViewModel

/**
 * Dialog de démarrage pour proposer la restauration des sauvegardes
 */
@Composable
fun StartupBackupDialog(
    viewModel: BackupRestoreViewModel,
    onDismiss: () -> Unit,
    onRestore: (BackupMetadata) -> Unit
) {
    val backups = viewModel.backups.collectAsState().value
    val isLoading = viewModel.isLoading.collectAsState().value
    val error = viewModel.error.collectAsState().value
    val isRestoring = viewModel.isRestoring.collectAsState().value
    val restoreProgress = viewModel.restoreProgress.collectAsState().value
    val showRestoreResultDialog = viewModel.showRestoreResultDialog.collectAsState().value
    
    // Fermer le dialog après une restauration réussie
    LaunchedEffect(showRestoreResultDialog) {
        if (showRestoreResultDialog != null) {
            // Attendre un peu pour que l'utilisateur voie le dialog de bilan
            kotlinx.coroutines.delay(2000)
            onDismiss()
        }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp),
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // En-tête
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(translate("auto.view.startupbackupdialog.sauvegardes_disponibles"), style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)
                        Text(
                            text = translate("auto.view.startupbackupdialog.selectionnez_une_sauvegarde_a_restaurer_ou_conti"),
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = translate("settings.close"))
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
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Contenu principal
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (isRestoring) {
                    // Indicateur de restauration
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = translate("auto.view.startupbackupdialog.restauration_en_cours"),
                                style = MaterialTheme.typography.h6
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = restoreProgress.toFloat(),
                                modifier = Modifier.fillMaxWidth(0.6f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${(restoreProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.body2,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                            )
                        }
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
                            Text(translate("auto.view.startupbackupdialog.continuez_sans_restaurer_de_sauvegarde"), style = MaterialTheme.typography.body2, color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f), textAlign = TextAlign.Center)
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(backups) { backup ->
                            StartupBackupItem(
                                backup = backup,
                                onRestore = { onRestore(backup) },
                                viewModel = viewModel
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Boutons d'action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isRestoring
                    ) {
                        Text(translate("auto.view.startupbackupdialog.continuer_sans_restaurer"))
                    }
                }
            }
        }
        
        // Dialog de bilan de restauration
        showRestoreResultDialog?.let { importCounts ->
            AlertDialog(
                onDismissRequest = { viewModel.dismissRestoreResultDialog() },
                title = {
                    Text(
                        text = translate("auto.view.backuprestoreview.restauration_terminee"),
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = translate("auto.view.startupbackupdialog.donnees_restaurees_avec_succes"),
                                style = MaterialTheme.typography.subtitle1,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = importCounts.animals.toString(),
                                        style = MaterialTheme.typography.h6,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colors.primary
                                    )
                                    Text(
                                        text = translate("auto.view.backuprestoreview.animaux"),
                                        style = MaterialTheme.typography.caption
                                    )
                                }
                                
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = importCounts.foods.toString(),
                                        style = MaterialTheme.typography.h6,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colors.primary
                                    )
                                    Text(
                                        text = translate("database.stat.foods"),
                                        style = MaterialTheme.typography.caption
                                    )
                                }
                                
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = importCounts.equations.toString(),
                                        style = MaterialTheme.typography.h6,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colors.primary
                                    )
                                    Text(
                                        text = translate("new_reference.tab.equations"),
                                        style = MaterialTheme.typography.caption
                                    )
                                }
                                
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = importCounts.conseils.toString(),
                                        style = MaterialTheme.typography.h6,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colors.primary
                                    )
                                    Text(
                                        text = translate("database.stat.conseils"),
                                        style = MaterialTheme.typography.caption
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.dismissRestoreResultDialog() },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.primary
                        )
                    ) {
                        Text(translate("general.ok"))
                    }
                }
            )
        }
    }
}

@Composable
private fun StartupBackupItem(
    backup: BackupMetadata,
    onRestore: () -> Unit,
    viewModel: BackupRestoreViewModel
) {
    val isRestoring = viewModel.isRestoring.collectAsState().value
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isRestoring) { onRestore() },
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // En-tête avec date
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
                
                IconButton(
                    onClick = onRestore,
                    enabled = !isRestoring
                ) {
                    Icon(
                        Icons.Default.Restore, 
                        contentDescription = translate("auto.view.backuprestoreview.restaurer"), 
                        tint = if (isRestoring) 
                            MaterialTheme.colors.onSurface.copy(alpha = 0.3f) 
                        else 
                            MaterialTheme.colors.primary
                    )
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
