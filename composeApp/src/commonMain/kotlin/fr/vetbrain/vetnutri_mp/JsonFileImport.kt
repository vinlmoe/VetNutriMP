package fr.vetbrain.vetnutri_mp

import androidx.lifecycle.viewModelScope
import fr.vetbrain.vetnutri_mp.Repository.ExportImportRepository
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import fr.vetbrain.vetnutri_mp.Utils.ImportUtils
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalListViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Native pickers return null only on cancellation; read errors are surfaced to the screen. */
internal fun launchJsonFileImport(
    scope: CoroutineScope,
    onError: (String) -> Unit,
    onContent: suspend (String) -> Unit
) {
    scope.launch {
        try {
            val content = openJsonFileContent() ?: return@launch
            onContent(content)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            onError("Erreur d'import : ${e.message}")
        }
    }
}

internal fun importAnimalJsonFile(viewModel: AnimalListViewModel, clearFoods: Boolean) {
    launchJsonFileImport(viewModel.viewModelScope, viewModel::setImportError) { content ->
        if (clearFoods) {
            // Validate before honoring the explicitly selected food reset option.
            val parsed = withContext(AppDispatchers.IO) { ImportUtils.importAnimalsFromJson(content) }
            require(parsed.animals.isNotEmpty()) { "Aucun animal dans le fichier" }
            withContext(AppDispatchers.IO) { viewModel.getFoodRepository()?.clearAllFoods() }
        }
        viewModel.importAnimalsFromJson(content)
    }
}

internal fun importFoodJsonFile(viewModel: SettingsViewModel) {
    launchJsonFileImport(viewModel.viewModelScope, {
        viewModel.setImportResult(SettingsViewModel.ImportResult.Error(it))
    }) { content ->
        val foods = withContext(AppDispatchers.IO) { ImportUtils.importFoodsFromJson(content) }
        require(foods.isNotEmpty()) { "Aucun aliment dans le fichier" }
        viewModel.importFoodsFromList(foods)
    }
}

internal fun importApiJsonFile(viewModel: SettingsViewModel) {
    launchJsonFileImport(viewModel.viewModelScope, {
        viewModel.setImportResult(SettingsViewModel.ImportResult.Error(it))
    }) { content ->
        viewModel.startApiImport()
        try {
            val repository = ExportImportRepository(
                animalRepository = viewModel.animalRepository,
                foodRepository = viewModel.foodRepository,
                equationRepository = viewModel.equationRepository,
                referenceRepository = viewModel.referenceEvRepository,
                biblioRepository = viewModel.biblioRefRepository,
                consultationRepository = viewModel.consultationRepository,
                recipeRepository = viewModel.recipeRepository,
                conseilRepository = viewModel.conseilRepository
            )
            val counts = withContext(AppDispatchers.IO) {
                repository.importAll(content, ExportImportRepository.ImportProgressListener(
                    viewModel::updateApiImportProgress, viewModel::appendApiImportLog
                ))
            }
            if (counts.errorCount > 0) {
                viewModel.setImportResult(SettingsViewModel.ImportResult.Error(
                    "Import partiel : ${counts.errorCount} erreur(s). Consultez le journal d'import."
                ))
            } else {
                val total = counts.animals + counts.foods + counts.equations + counts.references +
                    counts.biblios + counts.rations + counts.recipes + counts.conseils
                viewModel.setImportResult(SettingsViewModel.ImportResult.Success(count = total, conseils = counts.conseils))
            }
        } finally {
            viewModel.finishApiImport()
        }
    }
}
