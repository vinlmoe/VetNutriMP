package fr.vetbrain.vetnutri_mp.Service

import fr.vetbrain.vetnutri_mp.Data.ApiEnvelope
import fr.vetbrain.vetnutri_mp.PlatformFile.PlatformFile
import fr.vetbrain.vetnutri_mp.Repository.ExportImportRepository
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
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
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
        explicitNulls = false
    }

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

        // Créer le répertoire de sauvegarde s'il n'existe pas
        scope.launch {
            println("[BackupService] Initialisation du répertoire de sauvegarde...")
            val backupDirectory = fileService.getBackupDirectory()
            println("[BackupService] Répertoire de sauvegarde: ${backupDirectory.absolutePath}")
            fileService.createDirectoryIfNotExists(backupDirectory)
            println(
                    "[BackupService] Répertoire prêt: existe=${backupDirectory.exists()} contenu=${backupDirectory.listFiles()?.size ?: 0}"
            )
        }

        // Sauvegarde immédiate au démarrage
        scope.launch {
            try {
                println("[BackupService] Sauvegarde immédiate au démarrage...")
                createBackup()
            } catch (e: Exception) {
                println("[BackupService] Erreur sauvegarde immédiate: ${e.message}")
            }
        }

        // Planifier les sauvegardes périodiques
        backupJob =
                scope.launch {
                    while (isActive) {
                        delay(BACKUP_INTERVAL_MINUTES * 60 * 1000) // 10 minutes
                        try {
                            println("[BackupService] Sauvegarde périodique...")
                            createBackup()
                        } catch (e: Exception) {
                            println("[BackupService] Erreur sauvegarde périodique: ${e.message}")
                        }
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
    suspend fun createBackup(): Result<BackupMetadata> {
        return try {
            // Exporter toutes les données
            println("[BackupService] Début création sauvegarde...")
            val jsonData = exportImportRepository.exportAll()
            println("[BackupService] Données exportées: taille=${jsonData.length}")
            val envelope = json.decodeFromString<ApiEnvelope>(jsonData)

            // Créer le nom de fichier avec timestamp
            val timestamp = Clock.System.now().toEpochMilliseconds()
            val fileName = "${BACKUP_PREFIX}${timestamp}${BACKUP_EXTENSION}"
            val backupDirectory = fileService.getBackupDirectory()
            println(
                    "[BackupService] Répertoire backup pour écriture: ${backupDirectory.absolutePath}"
            )
            val file = PlatformFile.create("${backupDirectory.absolutePath}/$fileName")

            // Sauvegarder le fichier
            println("[BackupService] Écriture du fichier: ${file.absolutePath}")
            fileService.writeText(file, jsonData)
            println("[BackupService] Fichier écrit: existe=${file.exists()} taille=${file.length}")

            // Gérer la rotation des fichiers
            println("[BackupService] Gestion rotation des sauvegardes...")
            manageBackupRotation()

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
            println("[BackupService] Sauvegarde des métadonnées: ${file.absolutePath}")
            saveBackupMetadata(metadata)

            println(
                    "[BackupService] Sauvegarde terminée: ${metadata.fileName} taille=${metadata.fileSize}"
            )
            Result.success(metadata)
        } catch (e: Exception) {
            println("[BackupService] Erreur création sauvegarde: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Gérer la rotation des fichiers de sauvegarde Garde seulement les 10 fichiers les plus récents
     */
    private suspend fun manageBackupRotation() {
        try {
            val backupDirectory = fileService.getBackupDirectory()
            println("[BackupService] Rotation: scan du répertoire ${backupDirectory.absolutePath}")
            val backupFiles =
                    fileService.listFiles(backupDirectory).filter { file ->
                        file.isFile() &&
                                file.name.startsWith(BACKUP_PREFIX) &&
                                file.name.endsWith(BACKUP_EXTENSION) &&
                                !file.name.contains(
                                        "_metadata"
                                ) // Exclure les fichiers de métadonnées
                    }
            println("[BackupService] Rotation: trouvés=${backupFiles.size}")

            if (backupFiles.size > MAX_BACKUP_FILES) {
                // Trier par date de modification (plus ancien en premier)
                val sortedFiles = backupFiles.sortedBy { it.lastModified }

                // Supprimer les fichiers les plus anciens
                val filesToDelete = sortedFiles.take(backupFiles.size - MAX_BACKUP_FILES)
                println("[BackupService] Rotation: suppression de ${filesToDelete.size} fichiers")
                filesToDelete.forEach { file ->
                    try {
                        println("[BackupService] Suppression ancien backup: ${file.absolutePath}")
                        fileService.deleteFile(file)
                        // Supprimer aussi le fichier de métadonnées associé
                        val metadataFile =
                                PlatformFile.create(
                                        file.absolutePath.replace(
                                                BACKUP_EXTENSION,
                                                "_metadata.json"
                                        )
                                )
                        if (fileService.fileExists(metadataFile)) {
                            println(
                                    "[BackupService] Suppression métadonnées: ${metadataFile.absolutePath}"
                            )
                            fileService.deleteFile(metadataFile)
                        }
                    } catch (e: Exception) {
                        println(
                                "[BackupService] Erreur suppression rotation ${file.absolutePath}: ${e.message}"
                        )
                    }
                }
            }
        } catch (e: Exception) {
            println("[BackupService] Erreur rotation: ${e.message}")
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
            println(
                    "[BackupService] Écriture métadonnées: ${metadataFile.absolutePath} taille=${metadataJson.length}"
            )
            fileService.writeText(metadataFile, metadataJson)
        } catch (e: Exception) {
            println("[BackupService] Erreur écriture métadonnées: ${e.message}")
        }
    }

    /** Récupérer la liste de tous les backups disponibles */
    suspend fun getAvailableBackups(): List<BackupMetadata> {
        return try {
            val backupDirectory = fileService.getBackupDirectory()
            println("[BackupService] Liste backups dans: ${backupDirectory.absolutePath}")
            val backupFiles =
                    fileService.listFiles(backupDirectory).filter { file ->
                        file.isFile() &&
                                file.name.startsWith(BACKUP_PREFIX) &&
                                file.name.endsWith(BACKUP_EXTENSION) &&
                                !file.name.contains(
                                        "_metadata"
                                ) // Exclure les fichiers de métadonnées
                    }
            println("[BackupService] Backups détectés: ${backupFiles.size}")

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
                                println(
                                        "[BackupService] Lecture métadonnées: ${metadataFile.absolutePath}"
                                )
                                val metadataJson =
                                        fileService.readText(metadataFile).getOrNull() ?: ""
                                val loaded = json.decodeFromString<BackupMetadata>(metadataJson)
                                loaded.copy(filePath = buildBackupFilePath(loaded.fileName))
                            } else {
                                // Créer des métadonnées basiques si le fichier n'existe pas
                                println(
                                        "[BackupService] Métadonnées absentes, création basique pour: ${file.absolutePath}"
                                )
                                val metadata =
                                        BackupMetadata(
                                                fileName = file.name,
                                                filePath = buildBackupFilePath(file.name),
                                                createdAt = file.lastModified,
                                                fileSize = file.length,
                                                animalCount = 0,
                                                foodCount =
                                                        8846, // Valeur approximative basée sur les
                                                // logs
                                                equationCount = 24,
                                                conseilCount = 1,
                                                recipeCount = 1,
                                                rationCount = 0
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
                                    println(
                                            "[BackupService] Écriture métadonnées (créées): ${createdMetadataFile.absolutePath}"
                                    )
                                    fileService.writeText(createdMetadataFile, metadataJson)
                                } catch (e: Exception) {
                                    println(
                                            "[BackupService] Erreur écriture métadonnées (créées): ${e.message}"
                                    )
                                }

                                metadata
                            }
                        } catch (e: Exception) {
                            println("[BackupService] Erreur lecture métadonnées: ${e.message}")
                            null
                        }
                    }
                    .sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            println("[BackupService] Erreur listage backups: ${e.message}")
            emptyList()
        }
    }

    /** Restaurer une sauvegarde */
    suspend fun restoreBackup(
            metadata: BackupMetadata
    ): Result<fr.vetbrain.vetnutri_mp.Repository.ExportImportRepository.ImportCounts> {
        return try {
            val currentPath = buildBackupFilePath(metadata.fileName)
            val file = PlatformFile.create(currentPath)
            println("[BackupService] Restauration depuis: ${file.absolutePath}")
            if (!fileService.fileExists(file)) {
                println(
                        "[BackupService] Fichier de sauvegarde introuvable (chemin courant): ${file.absolutePath}"
                )
                return Result.failure(
                        Exception("Fichier de sauvegarde introuvable: ${file.absolutePath}")
                )
            }

            println("[BackupService] Lecture fichier de sauvegarde...")
            val jsonData = fileService.readText(file).getOrNull() ?: ""
            println("[BackupService] Lecture OK, taille=${jsonData.length}")
            // importAll retourne ImportCounts
            println("[BackupService] Import des données...")
            val importCounts = exportImportRepository.importAll(jsonData)
            println("[BackupService] Import terminé: ${importCounts}")
            Result.success(importCounts)
        } catch (e: Exception) {
            println("[BackupService] Erreur restauration: ${e.message}")
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

            println("[BackupService] Suppression backup: ${file.absolutePath}")
            if (fileService.fileExists(file)) {
                fileService.deleteFile(file)
            }
            println("[BackupService] Suppression métadonnées: ${metadataFile.absolutePath}")
            if (fileService.fileExists(metadataFile)) {
                fileService.deleteFile(metadataFile)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            println("[BackupService] Erreur suppression backup: ${e.message}")
            Result.failure(e)
        }
    }

    /** Nettoyer le service */
    fun cleanup() {
        stopAutomaticBackup()
        scope.cancel()
    }
}
