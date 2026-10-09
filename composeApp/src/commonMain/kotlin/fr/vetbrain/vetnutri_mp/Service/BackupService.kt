package fr.vetbrain.vetnutri_mp.Service

import fr.vetbrain.vetnutri_mp.Data.ApiEnvelope
import fr.vetbrain.vetnutri_mp.PlatformFile.PlatformFile
import fr.vetbrain.vetnutri_mp.Repository.ExportImportRepository
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import fr.vetbrain.vetnutri_mp.Utils.isDebugBuild
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Service de sauvegarde automatique de la base de données Gère la création, rotation et
 * restauration des sauvegardes
 */
class BackupService(
        private val exportImportRepository: ExportImportRepository,
        private val fileService: FileService
) {

    companion object {
        private const val MAX_BACKUP_FILES = 10
        private const val BACKUP_PREFIX = "vetnutri_backup_"
        private const val BACKUP_EXTENSION = ".json"
        private const val BACKUP_INTERVAL_MINUTES = 10L
    }

    private val json = Json {
        prettyPrint = isDebugBuild()
        encodeDefaults = true
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private val operationMutex = Mutex()
    private var backupJob: Job? = null
    private val scope = CoroutineScope(AppDispatchers.IO + SupervisorJob())

    /** Métadonnées d'un fichier de sauvegarde */
    @Serializable
    data class BackupMetadata(
            val fileName: String,
            val filePath: String,
            val createdAt: Long, // Utiliser Long au lieu d'Instant
            val fileSize: Long,
            val animalCount: Int,
            val foodCount: Int,
            val equationCount: Int,
            val conseilCount: Int,
            val recipeCount: Int,
            val rationCount: Int
    )

    /** Démarrer le service de sauvegarde automatique */
    fun startAutomaticBackup() {
        stopAutomaticBackup() // Arrêter toute sauvegarde existante

        backupJob = scope.launch {
            createAutomaticBackup()
            while (isActive) {
                delay(BACKUP_INTERVAL_MINUTES * 60 * 1000)
                createAutomaticBackup()
            }
        }
    }

    /** Arrêter le service de sauvegarde automatique */
    fun stopAutomaticBackup() {
        backupJob?.cancel()
        backupJob = null
    }

    private suspend fun buildBackupFilePath(fileName: String): String {
        val backupDirectory = fileService.getBackupDirectory()
        return "${backupDirectory.absolutePath}/$fileName"
    }

    /** Créer une sauvegarde manuelle */
    suspend fun createBackup(): Result<BackupMetadata> = operationMutex.withLock {
        createBackupLocked()
    }

    /**
     * Sauvegarde automatique. Une base sans animaux n'est pas sauvegardée tant qu'une sauvegarde
     * plus ancienne en contient : après une perte de base, la rotation effacerait sinon les
     * dernières sauvegardes utiles en moins de deux heures.
     */
    internal suspend fun createAutomaticBackup(): Result<BackupMetadata> = operationMutex.withLock {
        createBackupLocked(skipIfNoAnimals = true)
    }

    /** Sauvegarde la plus récente contenant des animaux, candidate à une restauration. */
    suspend fun findLatestBackupWithAnimals(): BackupMetadata? =
            getAvailableBackups().firstOrNull { it.animalCount > 0 }

    private suspend fun createBackupLocked(
            rotate: Boolean = true,
            skipIfNoAnimals: Boolean = false
    ): Result<BackupMetadata> {
        return try {
            // Exporter toutes les données
            val envelope = exportImportRepository.exportAllEnvelope()
            if (skipIfNoAnimals && envelope.animals.isEmpty() && findLatestBackupWithAnimals() != null) {
                return Result.failure(IllegalStateException("Base sans animaux : sauvegarde automatique ignorée"))
            }

            // Créer le nom de fichier avec timestamp
            var timestamp = Clock.System.now().toEpochMilliseconds()
            val backupDirectory = fileService.getBackupDirectory()
            while (PlatformFile.create("${backupDirectory.absolutePath}/${BACKUP_PREFIX}${timestamp}${BACKUP_EXTENSION}").exists()) timestamp++
            val fileName = "${BACKUP_PREFIX}${timestamp}${BACKUP_EXTENSION}"
            val file = PlatformFile.create("${backupDirectory.absolutePath}/$fileName")

            // Sauvegarder le fichier (streaming si possible)
            val temporary = PlatformFile.create("${file.absolutePath}.tmp")
            try {
                exportImportRepository.writeEnvelopeToFile(envelope, temporary).getOrThrow()
                check(temporary.isFile() && temporary.length > 0) { "Sauvegarde vide ou absente" }
                fileService.moveFile(temporary, file).getOrThrow()
            } finally {
                if (temporary.exists()) fileService.deleteFile(temporary)
            }

            // Créer les métadonnées
            val metadata =
                    BackupMetadata(
                            fileName = fileName,
                            filePath = file.absolutePath,
                            createdAt = Clock.System.now().toEpochMilliseconds(),
                            fileSize = file.length,
                            animalCount = envelope.animals.size,
                            foodCount = envelope.foods.size,
                            equationCount = envelope.equations.size,
                            conseilCount = envelope.conseils.size,
                            recipeCount = envelope.recipes.size,
                            rationCount = envelope.rations.size
                    )

            // Sauvegarder les métadonnées
            saveBackupMetadata(metadata)
            if (rotate) manageBackupRotation()
            Result.success(metadata)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    /**
     * Gérer la rotation des fichiers de sauvegarde Garde seulement les 10 fichiers les plus récents
     */
    private suspend fun manageBackupRotation() {
        try {
            val backupDirectory = fileService.getBackupDirectory()
            val backupFiles =
                    fileService.listFiles(backupDirectory).filter { file ->
                        file.isFile() &&
                                file.name.startsWith(BACKUP_PREFIX) &&
                                file.name.endsWith(BACKUP_EXTENSION) &&
                                !file.name.contains(
                                        "_metadata"
                                ) // Exclure les fichiers de métadonnées
                    }

            if (backupFiles.size > MAX_BACKUP_FILES) {
                // Trier par date de modification (plus ancien en premier)
                val sortedFiles = backupFiles.sortedBy { it.lastModified }

                // Supprimer les fichiers les plus anciens
                val filesToDelete = sortedFiles.take(backupFiles.size - MAX_BACKUP_FILES)
                filesToDelete.forEach { file ->
                    try {
                        fileService.deleteFile(file).getOrThrow()
                        // Supprimer aussi le fichier de métadonnées associé
                        val metadataFile =
                                PlatformFile.create(
                                        file.absolutePath.replace(
                                                BACKUP_EXTENSION,
                                                "_metadata.json"
                                        )
                                )
                        if (fileService.fileExists(metadataFile)) {
                            fileService.deleteFile(metadataFile).getOrThrow()
                        }
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                    }
                }
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
        }
    }

    /** Sauvegarder les métadonnées d'un backup */
    private suspend fun saveBackupMetadata(metadata: BackupMetadata) {
        try {
            val metadataFile =
                    PlatformFile.create(
                            metadata.filePath.replace(BACKUP_EXTENSION, "_metadata.json")
                    )
            val metadataJson = json.encodeToString(metadata)
            fileService.writeText(metadataFile, metadataJson)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
        }
    }

    /** Récupérer la liste de tous les backups disponibles */
    suspend fun getAvailableBackups(): List<BackupMetadata> {
        return try {
            val backupDirectory = fileService.getBackupDirectory()
            val backupFiles =
                    fileService.listFiles(backupDirectory).filter { file ->
                        file.isFile() &&
                                file.name.startsWith(BACKUP_PREFIX) &&
                                file.name.endsWith(BACKUP_EXTENSION) &&
                                !file.name.contains(
                                        "_metadata"
                                ) // Exclure les fichiers de métadonnées
                    }

            backupFiles
                    .mapNotNull { file ->
                        try {
                            val metadataFile =
                                    PlatformFile.create(
                                            file.absolutePath.replace(
                                                    BACKUP_EXTENSION,
                                                    "_metadata.json"
                                            )
                                    )
                            if (fileService.fileExists(metadataFile)) {
                                val metadataJson =
                                        fileService.readText(metadataFile).getOrNull() ?: ""
                                val loaded = json.decodeFromString<BackupMetadata>(metadataJson)
                                loaded.copy(filePath = buildBackupFilePath(loaded.fileName))
                            } else {
                                val envelope = json.decodeFromString<ApiEnvelope>(fileService.readText(file).getOrThrow())
                                val metadata = BackupMetadata(
                                    fileName = file.name,
                                    filePath = file.absolutePath,
                                    createdAt = file.lastModified,
                                    fileSize = file.length,
                                    animalCount = envelope.animals.size,
                                    foodCount = envelope.foods.size,
                                    equationCount = envelope.equations.size,
                                    conseilCount = envelope.conseils.size,
                                    recipeCount = envelope.recipes.size,
                                    rationCount = envelope.rations.size
                                )

                                // Sauvegarder les métadonnées pour éviter de les recréer à chaque
                                // fois
                                try {
                                    val createdMetadataFile =
                                            PlatformFile.create(
                                                    file.absolutePath.replace(
                                                            BACKUP_EXTENSION,
                                                            "_metadata.json"
                                                    )
                                            )
                                    val metadataJson = json.encodeToString(metadata)
                                    fileService.writeText(createdMetadataFile, metadataJson)
                                } catch (e: Exception) {
                                    if (e is CancellationException) throw e
                                }

                                metadata
                            }
                        } catch (e: Exception) {
                            if (e is CancellationException) throw e
                            null
                        }
                    }
                    .sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emptyList()
        }
    }

    /** Restaurer une sauvegarde */
    suspend fun restoreBackup(
            metadata: BackupMetadata
    ): Result<fr.vetbrain.vetnutri_mp.Repository.ExportImportRepository.ImportCounts> = operationMutex.withLock {
        try {
            val currentPath = buildBackupFilePath(metadata.fileName)
            val file = PlatformFile.create(currentPath)
            if (!fileService.fileExists(file)) {
                error("Fichier de sauvegarde introuvable: ${file.absolutePath}")
            }

            // Parse before changing data or creating a recovery backup.
            val envelope = json.decodeFromString<ApiEnvelope>(fileService.readText(file).getOrThrow())
            val recovery = createBackupLocked(rotate = false).getOrThrow()
            try {
                val importCounts = exportImportRepository.importAll(envelope)
                check(importCounts.errorCount == 0) { "${importCounts.errorCount} erreur(s) d'import" }
                Result.success(importCounts)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(IllegalStateException(
                    "Restauration incomplète : ${e.message}. Sauvegarde préalable conservée : ${recovery.fileName}", e
                ))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    /** Supprimer une sauvegarde */
    suspend fun deleteBackup(metadata: BackupMetadata): Result<Unit> {
        return try {
            val currentBackupPath = buildBackupFilePath(metadata.fileName)
            val file = PlatformFile.create(currentBackupPath)
            val metadataFile =
                    PlatformFile.create(
                            currentBackupPath.replace(BACKUP_EXTENSION, "_metadata.json")
                    )

            if (fileService.fileExists(file)) {
                fileService.deleteFile(file).getOrThrow()
            }
            if (fileService.fileExists(metadataFile)) {
                fileService.deleteFile(metadataFile).getOrThrow()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    /** Nettoyer le service */
    fun cleanup() {
        stopAutomaticBackup()
        scope.cancel()
    }
}
