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
            println("DEBUG: StartupService.initialize() - début")
            
            // Créer le répertoire de sauvegarde
            val backupDirectory = fileService.getBackupDirectory()
            println("DEBUG: Répertoire de sauvegarde: ${backupDirectory.absolutePath}")
            fileService.createDirectoryIfNotExists(backupDirectory)
            
            // Initialiser le service de sauvegarde
            backupService = BackupService(exportImportRepository, backupDirectory)
            println("DEBUG: BackupService créé: ${backupService != null}")
            
            // Démarrer la sauvegarde automatique
            backupService?.startAutomaticBackup()
            println("DEBUG: Sauvegarde automatique démarrée")
            
            println("Services de démarrage initialisés avec succès")
        } catch (e: Exception) {
            println("Erreur lors de l'initialisation des services: ${e.message}")
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
