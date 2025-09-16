package fr.vetbrain.vetnutri_mp.Service

import fr.vetbrain.vetnutri_mp.PlatformFile.PlatformFile
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlinx.coroutines.withContext

/**
 * Service pour la gestion des fichiers et répertoires Fournit des méthodes communes pour la gestion
 * des fichiers sur toutes les plateformes
 */
expect class FileService {
    suspend fun getBackupDirectory(): PlatformFile
    suspend fun getDataDirectory(): PlatformFile
    suspend fun createDirectoryIfNotExists(directory: PlatformFile): Result<Unit>
    suspend fun fileExists(file: PlatformFile): Boolean
    suspend fun getFileSize(file: PlatformFile): Long
    suspend fun deleteFile(file: PlatformFile): Result<Unit>
    suspend fun listFiles(directory: PlatformFile, pattern: String? = null): List<PlatformFile>
    suspend fun copyFile(source: PlatformFile, destination: PlatformFile): Result<Unit>
    suspend fun moveFile(source: PlatformFile, destination: PlatformFile): Result<Unit>
    suspend fun writeText(file: PlatformFile, text: String): Result<Unit>
    suspend fun readText(file: PlatformFile): Result<String>
}

/** Implémentation commune réutilisable (via héritage) pour partager la logique IO */
open class BaseFileService {
    open suspend fun createDirectoryIfNotExists(directory: PlatformFile): Result<Unit> {
        return withContext(AppDispatchers.IO) {
            try {
                if (!directory.exists()) {
                    directory.mkdirs()
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    open suspend fun fileExists(file: PlatformFile): Boolean {
        return withContext(AppDispatchers.IO) { file.exists() }
    }

    open suspend fun getFileSize(file: PlatformFile): Long {
        return withContext(AppDispatchers.IO) { if (file.exists()) file.length else 0L }
    }

    open suspend fun deleteFile(file: PlatformFile): Result<Unit> {
        return withContext(AppDispatchers.IO) {
            try {
                if (file.exists()) file.delete()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    open suspend fun listFiles(directory: PlatformFile, pattern: String?): List<PlatformFile> {
        return withContext(AppDispatchers.IO) {
            try {
                if (!directory.exists()) emptyList()
                else {
                    val files = directory.listFiles() ?: emptyList()
                    if (pattern != null) files.filter { it.name.matches(pattern.toRegex()) }
                    else files
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    open suspend fun copyFile(source: PlatformFile, destination: PlatformFile): Result<Unit> {
        return withContext(AppDispatchers.IO) {
            try {
                source.copyTo(destination, overwrite = true)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    open suspend fun moveFile(source: PlatformFile, destination: PlatformFile): Result<Unit> {
        return withContext(AppDispatchers.IO) {
            try {
                source.renameTo(destination)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    open suspend fun writeText(file: PlatformFile, text: String): Result<Unit> {
        return withContext(AppDispatchers.IO) {
            try {
                file.writeText(text)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    open suspend fun readText(file: PlatformFile): Result<String> {
        return withContext(AppDispatchers.IO) {
            try {
                Result.success(file.readText())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
