package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.Service.BackupService
import fr.vetbrain.vetnutri_mp.Service.BackupService.BackupMetadata
import fr.vetbrain.vetnutri_mp.Utils.PlatformDispatcher
import fr.vetbrain.vetnutri_mp.Repository.ExportImportRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.toLocalDateTime

/**
 * ViewModel pour la gestion des sauvegardes et de la restauration
 */
class BackupRestoreViewModel(
    private val backupService: BackupService,
    private val platformDispatcher: PlatformDispatcher
) {
    private val coroutineScope: CoroutineScope = kotlinx.coroutines.CoroutineScope(platformDispatcher.provideIODispatcher())
    
    private val _backups = MutableStateFlow<List<BackupMetadata>>(emptyList())
    val backups: StateFlow<List<BackupMetadata>> = _backups.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _isRestoring = MutableStateFlow(false)
    val isRestoring: StateFlow<Boolean> = _isRestoring.asStateFlow()
    
    private val _restoreProgress = MutableStateFlow(0.0)
    val restoreProgress: StateFlow<Double> = _restoreProgress.asStateFlow()
    
    private val _restoreLog = MutableStateFlow<String>("")
    
    private val _showRestoreResultDialog = MutableStateFlow<ExportImportRepository.ImportCounts?>(null)
    val showRestoreResultDialog: StateFlow<ExportImportRepository.ImportCounts?> = _showRestoreResultDialog.asStateFlow()
    
    init {
        loadBackups()
    }
    
    /**
     * Charger la liste des sauvegardes disponibles
     */
    fun loadBackups() {
        coroutineScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val availableBackups = backupService.getAvailableBackups()
                _backups.value = availableBackups
            } catch (e: Exception) {
                _error.value = "Erreur lors du chargement des sauvegardes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Créer une sauvegarde manuelle
     */
    fun createManualBackup() {
        coroutineScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val result = backupService.createBackup()
                if (result.isSuccess) {
                    loadBackups() // Recharger la liste
                } else {
                    _error.value = "Erreur lors de la création de la sauvegarde: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _error.value = "Erreur lors de la création de la sauvegarde: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Restaurer une sauvegarde
     */
    fun restoreBackup(metadata: BackupMetadata) {
        coroutineScope.launch {
            _isRestoring.value = true
            _error.value = null
            _restoreProgress.value = 0.0
            
            try {
                // Simuler le progrès
                _restoreProgress.value = 0.1
                
                _restoreProgress.value = 0.3
                
                val result = backupService.restoreBackup(metadata)
                
                _restoreProgress.value = 0.7
                
                if (result.isSuccess) {
                    _restoreProgress.value = 1.0
                    // Afficher le dialog de bilan
                    _showRestoreResultDialog.value = result.getOrNull()
                } else {
                    _error.value = "Erreur lors de la restauration: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _error.value = "Erreur lors de la restauration: ${e.message}"
            } finally {
                _isRestoring.value = false
            }
        }
    }
    
    /**
     * Fermer le dialog de bilan de restauration
     */
    fun dismissRestoreResultDialog() {
        _showRestoreResultDialog.value = null
    }
    
    /**
     * Supprimer une sauvegarde
     */
    fun deleteBackup(metadata: BackupMetadata) {
        coroutineScope.launch {
            try {
                val result = backupService.deleteBackup(metadata)
                if (result.isSuccess) {
                    loadBackups() // Recharger la liste
                } else {
                    _error.value = "Erreur lors de la suppression: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _error.value = "Erreur lors de la suppression: ${e.message}"
            }
        }
    }
    
    /**
     * Effacer les messages d'erreur
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Effacer le log de restauration
     */
    fun clearRestoreLog() {
    }
    
    /**
     * Formater la taille de fichier en format lisible
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} MB"
        }
    }
    
    /**
     * Formater la date en format lisible
     */
    fun formatDate(timestamp: Long): String {
        return try {
            val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(timestamp)
            val tz = kotlinx.datetime.TimeZone.currentSystemDefault()
            val localDateTime = instant.toLocalDateTime(tz)
            "${localDateTime.dayOfMonth}/${localDateTime.monthNumber}/${localDateTime.year} ${localDateTime.hour}:${localDateTime.minute.toString().padStart(2, '0')}"
        } catch (e: Exception) {
            "Date inconnue"
        }
    }
}
