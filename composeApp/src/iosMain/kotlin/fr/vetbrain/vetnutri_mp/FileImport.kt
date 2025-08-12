package fr.vetbrain.vetnutri_mp

import fr.vetbrain.vetnutri_mp.ViewModel.AnimalListViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.ImportViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel

actual fun importAnimalsFromFile(viewModel: AnimalListViewModel) {
        viewModel.setImportError("L'importation de fichiers n'est pas encore implémentée sur iOS.")
}

actual fun importFoodsFromFile(viewModel: SettingsViewModel) {
        viewModel.setImportResult(
                SettingsViewModel.ImportResult.Error(
                        "L'importation de fichiers n'est pas encore implémentée sur iOS."
                )
        )
}

actual fun importNutritionalRequirementsFromFile(viewModel: ImportViewModel) {
        viewModel.setNutritionalRequirementImportError(
                "L'import des besoins n'est pas encore implémenté sur iOS."
        )
}

actual fun importApiFromFile(viewModel: SettingsViewModel) {
        viewModel.setImportResult(
                SettingsViewModel.ImportResult.Error(
                        "L'import API n'est pas encore implémenté sur iOS."
                )
        )
}

actual fun exportJsonToFile(content: String, defaultFileName: String): Boolean {
        return false
}

actual fun openJsonFileContent(): String? {
        return null
}
