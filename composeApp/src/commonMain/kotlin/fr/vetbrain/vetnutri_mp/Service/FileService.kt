package fr.vetbrain.vetnutri_mp.Service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Service pour la gestion des fichiers et répertoires
 * Fournit des méthodes communes pour la gestion des fichiers sur toutes les plateformes
 */
expect class FileService {
    suspend fun getBackupDirectory(): File
    suspend fun getDataDirectory(): File
    suspend fun createDirectoryIfNotExists(directory: File): Result<Unit>
    suspend fun fileExists(file: File): Boolean
    suspend fun getFileSize(file: File): Long
    suspend fun deleteFile(file: File): Result<Unit>
    suspend fun listFiles(directory: File, pattern: String? = null): List<File>
    suspend fun copyFile(source: File, destination: File): Result<Unit>
    suspend fun moveFile(source: File, destination: File): Result<Unit>
}

/**
 * Implémentation commune réutilisable (via héritage) pour partager la logique IO
 */
open class BaseFileService {
    open suspend fun createDirectoryIfNotExists(directory: File): Result<Unit> {
        return withContext(Dispatchers.IO) {
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

    open suspend fun fileExists(file: File): Boolean {
        return withContext(Dispatchers.IO) { file.exists() }
    }

    open suspend fun getFileSize(file: File): Long {
        return withContext(Dispatchers.IO) { if (file.exists()) file.length() else 0L }
    }

    open suspend fun deleteFile(file: File): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                if (file.exists()) file.delete()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    open suspend fun listFiles(directory: File, pattern: String?): List<File> {
        return withContext(Dispatchers.IO) {
            try {
                if (!directory.exists()) emptyList()
                else {
                    val files = directory.listFiles()?.toList() ?: emptyList()
                    if (pattern != null) files.filter { it.name.matches(pattern.toRegex()) } else files
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    open suspend fun copyFile(source: File, destination: File): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                source.copyTo(destination, overwrite = true)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    open suspend fun moveFile(source: File, destination: File): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                source.renameTo(destination)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
