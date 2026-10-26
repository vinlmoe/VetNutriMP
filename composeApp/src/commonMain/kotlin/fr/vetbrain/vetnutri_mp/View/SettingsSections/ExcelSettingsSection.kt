package fr.vetbrain.vetnutri_mp.View.SettingsSections

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import fr.vetbrain.vetnutri_mp.View.ExcelImportExportSection
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel

/** Contenu de l'onglet « Import/Export Excel » des paramètres (extrait de SettingsView). */
@Composable
internal fun ExcelSettingsSection(viewModel: SettingsViewModel) {
        val excelFoodService = remember { viewModel.createExcelFoodService() }
        ExcelImportExportSection(
                modifier = Modifier.fillMaxWidth(),
                excelFoodService = excelFoodService,
                loadAllFoods = viewModel::getAllFoods
        )
}
