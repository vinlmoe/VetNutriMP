package fr.vetbrain.vetnutri_mp.Service

import fr.vetbrain.vetnutri_mp.Repository.ExportImportRepository
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Service de démarrage de l'application Gère l'initialisation des services et la sauvegarde
 * automatique
 */
class StartupService(
        private val exportImportRepository: ExportImportRepository,
        private val fileService: FileService
) {

    private val scope = CoroutineScope(AppDispatchers.IO + SupervisorJob())
    private val backupService: BackupService = BackupService(exportImportRepository, fileService)

    /** Initialiser les services au démarrage de l'application */
    suspend fun initialize() {
        try {

            // Créer le répertoire de sauvegarde
            val backupDirectory = fileService.getBackupDirectory()
            fileService.createDirectoryIfNotExists(backupDirectory)

            // Démarrer la sauvegarde automatique
            backupService.startAutomaticBackup()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** Obtenir le service de sauvegarde */
    fun getBackupService(): BackupService? {
        return backupService
    }

    /** Arrêter les services */
    fun shutdown() {
        backupService.cleanup()
        scope.cancel()
    }

    /** Créer une sauvegarde manuelle */
    suspend fun createManualBackup(): Result<BackupService.BackupMetadata> {
        return backupService.createBackup()
    }

    /** Obtenir la liste des sauvegardes disponibles */
    suspend fun getAvailableBackups(): List<BackupService.BackupMetadata> {
        return backupService.getAvailableBackups()
    }

    /** Sauvegarde JSON à proposer quand la base démarre sans animaux (null si aucune) */
    suspend fun findRecoveryBackup(): BackupService.BackupMetadata? =
            backupService.findLatestBackupWithAnimals()

    /** Régénère la base depuis une sauvegarde JSON */
    suspend fun restoreBackup(
            metadata: BackupService.BackupMetadata
    ): Result<ExportImportRepository.ImportCounts> = backupService.restoreBackup(metadata)
}
