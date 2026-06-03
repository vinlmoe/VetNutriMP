package fr.vetbrain.vetnutri_mp.ViewModel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LegacyMigrationViewModel(
    private val scope: CoroutineScope
) {
    data class MigrationCounts(
        val animals: Int = 0,
        val consultations: Int = 0,
        val rations: Int = 0,
        val weights: Int = 0,
        val foods: Int = 0
    )

    data class MigrationResult(
        val imported: MigrationCounts,
        val skipped: MigrationCounts,
        val errors: List<String>
    )

    sealed class Step {
        object Idle : Step()
        object Detecting : Step()
        data class Detected(val path: String) : Step()
        object NotDetected : Step()
        object Previewing : Step()
        data class Preview(val path: String, val counts: MigrationCounts) : Step()
        object Migrating : Step()
        data class Done(val result: MigrationResult) : Step()
        data class Error(val message: String) : Step()
    }

    private val _step = MutableStateFlow<Step>(Step.Idle)
    val step: StateFlow<Step> = _step

    private val _log = MutableStateFlow<List<String>>(emptyList())
    val log: StateFlow<List<String>> = _log

    fun detect() {
        _step.value = Step.Detecting
        scope.launch {
            val path = detectLegacyV2DbFolder()
            _step.value = if (path != null) Step.Detected(path) else Step.NotDetected
        }
    }

    fun useCustomPath(path: String) {
        _step.value = Step.Detected(path)
    }

    fun loadPreview(dbFolderPath: String) {
        _step.value = Step.Previewing
        scope.launch {
            try {
                val counts = previewLegacyV2Migration(dbFolderPath)
                _step.value = Step.Preview(dbFolderPath, counts)
            } catch (e: Exception) {
                _step.value = Step.Error("Erreur lors de la lecture : ${e.message}")
            }
        }
    }

    fun startMigration(dbFolderPath: String) {
        _step.value = Step.Migrating
        _log.value = emptyList()
        scope.launch {
            try {
                val result = runLegacyV2Migration(dbFolderPath) { msg ->
                    _log.value = _log.value + msg
                }
                _step.value = Step.Done(result)
            } catch (e: Exception) {
                _step.value = Step.Error("Erreur lors de la migration : ${e.message}")
            }
        }
    }

    fun reset() {
        _step.value = Step.Idle
        _log.value = emptyList()
    }
}
