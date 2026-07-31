package fr.vetbrain.vetnutri_mp

import fr.vetbrain.vetnutri_mp.ViewModel.LegacyMigrationViewModel

actual fun detectLegacyV2DbFolder(): String? = null
actual suspend fun browseLegacyV2DbFolder(): String? = null
actual suspend fun previewLegacyV2Migration(
    dbFolderPath: String
): LegacyMigrationViewModel.MigrationCounts = LegacyMigrationViewModel.MigrationCounts()
actual suspend fun runLegacyV2Migration(
    dbFolderPath: String,
    onLog: suspend (String) -> Unit
): LegacyMigrationViewModel.MigrationResult = LegacyMigrationViewModel.MigrationResult(
    imported = LegacyMigrationViewModel.MigrationCounts(),
    skipped = LegacyMigrationViewModel.MigrationCounts(),
    errors = listOf("Non disponible sur cette plateforme")
)
