package fr.vetbrain.vetnutri_mp.Navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
        title = { Text(translate("dialog.resultImportAnimals.title")) },
        text = {
            when (importResult) {
                is AnimalListViewModel.ImportResult.Success ->
                    Text(translate("dialog.resultImportAnimals.successFormat", importResult.count.toString()))
                is AnimalListViewModel.ImportResult.Error ->
                    Text(translate("dialog.resultImportAnimals.errorFormat", importResult.message))
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text(translate("general.ok")) }
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
        title = { Text(translate("auto.navigation.appdialogs.resultat_de_l_importation_des_aliments")) },
        text = {
            when (importResult) {
                is SettingsViewModel.ImportResult.Success -> {
                    Column {
                        Text(
                            translate("auto.navigation.appdialogs.food_import_success_count", importResult.count.toString()),
                            style = MaterialTheme.typography.subtitle1
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(translate("auto.navigation.appdialogs.details_de_l_importation"), style = MaterialTheme.typography.subtitle2)
                        Text(translate("auto.navigation.appdialogs.arg_nouveaux_aliments", (importResult.importedCount).toString()))
                        if (importResult.updatedCount > 0)
                            Text(translate("auto.navigation.appdialogs.arg_aliments_mis_a_jour", (importResult.updatedCount).toString()))
                        if (importResult.deletedCount > 0)
                            Text(translate("auto.navigation.appdialogs.arg_aliments_supprimes", (importResult.deletedCount).toString()))
                        if (importResult.errorCount > 0)
                            Text(
                                translate("auto.navigation.appdialogs.food_import_errors_count", importResult.errorCount.toString()),
                                color = MaterialTheme.colors.error
                            )
                        if (importResult.nonResolvedNutrients > 0)
                            Text(
                                translate("auto.navigation.appdialogs.food_import_unresolved_nutrients", importResult.nonResolvedNutrients.toString()),
                                color = MaterialTheme.colors.error.copy(alpha = 0.7f)
                            )
                    }
                }
                is SettingsViewModel.ImportResult.Error ->
                    Text(translate("auto.navigation.appdialogs.erreur_lors_de_l_importation_arg", (importResult.message).toString()))
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text(translate("general.ok")) }
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
