package fr.vetbrain.vetnutri_mp

import fr.vetbrain.vetnutri_mp.ViewModel.AnimalListViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel

/**
 * Implémentation iOS de la fonction d'importation d'animaux depuis un fichier. Cette fonction ouvre
 * un sélecteur de fichier pour choisir un fichier JSON.
 */
actual fun importAnimalsFromFile(viewModel: AnimalListViewModel) {
    // Pour l'instant, nous affichons simplement un message d'erreur
    viewModel.setImportError("L'importation de fichiers n'est pas encore implémentée sur iOS.")
}

/**
 * Implémentation iOS de la fonction d'importation d'aliments depuis un fichier. Cette fonction
 * ouvre un sélecteur de fichier pour choisir un fichier JSON.
 */
actual fun importFoodsFromFile(viewModel: SettingsViewModel) {
    // Pour l'instant, nous affichons simplement un message d'erreur
    viewModel.setImportResult(
            SettingsViewModel.ImportResult.Error(
                    "L'importation de fichiers n'est pas encore implémentée sur iOS."
            )
    )
}

/** Import API (nouveau format) – iOS stub */
actual fun importApiFromFile(viewModel: SettingsViewModel) {
    viewModel.setImportResult(
            SettingsViewModel.ImportResult.Error(
                    "L'import API n'est pas encore implémenté sur iOS."
            )
    )
}
