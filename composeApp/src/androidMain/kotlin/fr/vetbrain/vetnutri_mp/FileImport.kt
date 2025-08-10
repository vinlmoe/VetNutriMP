package fr.vetbrain.vetnutri_mp

import android.net.Uri
import fr.vetbrain.vetnutri_mp.Localization.AndroidContext
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalListViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel
import kotlinx.coroutines.withContext

/**
 * Implémentation Android de la fonction d'importation d'animaux depuis un fichier. Cette fonction
 * ouvre un sélecteur de fichier pour choisir un fichier JSON.
 */
actual fun importAnimalsFromFile(viewModel: AnimalListViewModel) {
    // Cette fonction sera appelée depuis un composable, mais nous ne pouvons pas
    // utiliser directement les fonctions de composition ici.
    // L'implémentation réelle est dans MainActivity.

    // Pour l'instant, nous affichons simplement un message d'erreur
    viewModel.setImportError("L'importation de fichiers n'est pas encore implémentée sur Android.")
}

/**
 * Implémentation Android de la fonction d'importation d'aliments depuis un fichier. Cette fonction
 * ouvre un sélecteur de fichier pour choisir un fichier JSON.
 */
actual fun importFoodsFromFile(viewModel: SettingsViewModel) {
    // Cette fonction sera appelée depuis un composable, mais nous ne pouvons pas
    // utiliser directement les fonctions de composition ici.
    // L'implémentation réelle est dans MainActivity.

    // Pour l'instant, nous affichons simplement un message d'erreur
    viewModel.setImportResult(
            SettingsViewModel.ImportResult.Error(
                    "L'importation de fichiers n'est pas encore implémentée sur Android."
            )
    )
}

/** Import API (nouveau format) – Android stub */
actual fun importApiFromFile(viewModel: SettingsViewModel) {
    viewModel.setImportResult(
            SettingsViewModel.ImportResult.Error(
                    "L'import API n'est pas encore implémenté sur Android."
            )
    )
}

/** Fonction utilitaire pour lire le contenu d'un fichier à partir d'un URI. */
suspend fun readFileContent(uri: Uri): String =
        withContext(AppDispatchers.IO) {
            val inputStream = AndroidContext.appContext.contentResolver.openInputStream(uri)
            inputStream?.bufferedReader()?.use { it.readText() } ?: ""
        }
