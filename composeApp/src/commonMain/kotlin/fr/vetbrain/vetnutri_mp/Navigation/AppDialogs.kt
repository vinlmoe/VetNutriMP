package fr.vetbrain.vetnutri_mp.Navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.View.StartupBackupDialog
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalListViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.BackupRestoreViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel

@Composable
internal fun AnimalImportResultDialog(
    importResult: AnimalListViewModel.ImportResult,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("dialog.resultImportAnimals.title".translate()) },
        text = {
            when (importResult) {
                is AnimalListViewModel.ImportResult.Success ->
                    Text("${importResult.count} ${"dialog.resultImportAnimals.success".translate()}")
                is AnimalListViewModel.ImportResult.Error ->
                    Text("${"dialog.resultImportAnimals.error".translate()} ${importResult.message}")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("OK") }
        }
    )
}

@Composable
internal fun FoodImportResultDialog(
    importResult: SettingsViewModel.ImportResult,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Résultat de l'importation des aliments") },
        text = {
            when (importResult) {
                is SettingsViewModel.ImportResult.Success -> {
                    Column {
                        Text(
                            "${importResult.count} aliments ont été importés avec succès.",
                            style = MaterialTheme.typography.subtitle1
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Détails de l'importation:", style = MaterialTheme.typography.subtitle2)
                        Text("• ${importResult.importedCount} nouveaux aliments")
                        if (importResult.updatedCount > 0)
                            Text("• ${importResult.updatedCount} aliments mis à jour")
                        if (importResult.deletedCount > 0)
                            Text("• ${importResult.deletedCount} aliments supprimés")
                        if (importResult.errorCount > 0)
                            Text(
                                "• ${importResult.errorCount} erreurs rencontrées",
                                color = MaterialTheme.colors.error
                            )
                        if (importResult.nonResolvedNutrients > 0)
                            Text(
                                "• ${importResult.nonResolvedNutrients} nutriments non résolus",
                                color = MaterialTheme.colors.error.copy(alpha = 0.7f)
                            )
                    }
                }
                is SettingsViewModel.ImportResult.Error ->
                    Text("Erreur lors de l'importation : ${importResult.message}")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("OK") }
        }
    )
}

@Composable
internal fun AppOverlayDialogs(
    showAnimalImportResult: Boolean,
    animalImportResult: AnimalListViewModel.ImportResult?,
    onDismissAnimalImport: () -> Unit,
    showFoodImportResult: Boolean,
    foodImportResult: SettingsViewModel.ImportResult?,
    onDismissFoodImport: () -> Unit,
    showStartupBackupDialog: Boolean,
    backupRestoreViewModel: BackupRestoreViewModel?,
    onDismissStartupBackup: () -> Unit
) {
    if (showAnimalImportResult && animalImportResult != null) {
        AnimalImportResultDialog(animalImportResult, onDismissAnimalImport)
    }
    if (showFoodImportResult && foodImportResult != null) {
        FoodImportResultDialog(foodImportResult, onDismissFoodImport)
    }
    if (showStartupBackupDialog && backupRestoreViewModel != null) {
        StartupBackupDialog(
            viewModel = backupRestoreViewModel,
            onDismiss = onDismissStartupBackup,
            onRestore = { backup -> backupRestoreViewModel.restoreBackup(backup) }
        )
    }
}
