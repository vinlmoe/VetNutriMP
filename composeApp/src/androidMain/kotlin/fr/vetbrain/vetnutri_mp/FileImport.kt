package fr.vetbrain.vetnutri_mp

import android.net.Uri
import fr.vetbrain.vetnutri_mp.Localization.AndroidContext
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalListViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel
import kotlinx.coroutines.withContext

actual fun importAnimalsFromFile(viewModel: AnimalListViewModel, clearFoodsBeforeImport: Boolean) =
    importAnimalJsonFile(viewModel, clearFoodsBeforeImport)

actual fun importFoodsFromFile(viewModel: SettingsViewModel) = importFoodJsonFile(viewModel)
actual fun importApiFromFile(viewModel: SettingsViewModel) = importApiJsonFile(viewModel)

suspend fun readFileContent(uri: Uri): String = withContext(AppDispatchers.IO) {
    val stream = AndroidContext.appContext.contentResolver.openInputStream(uri)
        ?: error("Impossible de lire le fichier sélectionné")
    stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
}
