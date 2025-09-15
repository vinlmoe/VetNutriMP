package fr.vetbrain.vetnutri_mp.Service

import fr.vetbrain.vetnutri_mp.Repository.ExportImportRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Service de démarrage de l'application
 * Gère l'initialisation des services et la sauvegarde automatique
 */
class StartupService(
    private val exportImportRepository: ExportImportRepository,
    private val fileService: FileService
) {
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var backupService: BackupService? = null
    
    /**
     * Initialiser les services au démarrage de l'application
     */
    suspend fun initialize() {
        try {
            
            // Créer le répertoire de sauvegarde
            val backupDirectory = fileService.getBackupDirectory()
            fileService.createDirectoryIfNotExists(backupDirectory)
            
            // Initialiser le service de sauvegarde
            backupService = BackupService(exportImportRepository, backupDirectory)
            
            // Démarrer la sauvegarde automatique
            backupService?.startAutomaticBackup()
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Obtenir le service de sauvegarde
     */
    fun getBackupService(): BackupService? {
        return backupService
    }
    
    /**
     * Arrêter les services
     */
    fun shutdown() {
        backupService?.cleanup()
        scope.cancel()
    }
    
    /**
     * Créer une sauvegarde manuelle
     */
    suspend fun createManualBackup(): Result<BackupService.BackupMetadata> {
        return backupService?.createBackup() ?: Result.failure(Exception("Service de sauvegarde non initialisé"))
    }
    
    /**
     * Obtenir la liste des sauvegardes disponibles
     */
    fun getAvailableBackups(): List<BackupService.BackupMetadata> {
        return backupService?.getAvailableBackups() ?: emptyList()
    }
}
