package fr.vetbrain.vetnutri_mp

import fr.vetbrain.vetnutri_mp.ViewModel.LegacyMigrationViewModel

expect fun detectLegacyV2DbFolder(): String?
expect suspend fun browseLegacyV2DbFolder(): String?
expect suspend fun previewLegacyV2Migration(
    dbFolderPath: String
): LegacyMigrationViewModel.MigrationCounts
expect suspend fun runLegacyV2Migration(
    dbFolderPath: String,
    onLog: suspend (String) -> Unit
): LegacyMigrationViewModel.MigrationResult
