package fr.vetbrain.vetnutri_mp.Service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import java.io.File

/**
 * Implémentation iOS du FileService
 */
actual class FileService {
    actual suspend fun getBackupDirectory(): File {
        return withContext(Dispatchers.IO) {
            val documentsPath = NSFileManager.defaultManager.URLForDirectory(
                NSDocumentDirectory,
                NSUserDomainMask,
                null,
                null,
                null
            )?.path ?: ""
            val backupDir = File(documentsPath, "VetNutriMP/backups")
            backupDir.mkdirs()
            backupDir
        }
    }

    actual suspend fun getDataDirectory(): File {
        return withContext(Dispatchers.IO) {
            val documentsPath = NSFileManager.defaultManager.URLForDirectory(
                NSDocumentDirectory,
                NSUserDomainMask,
                null,
                null,
                null
            )?.path ?: ""
            val dataDir = File(documentsPath, "VetNutriMP/data")
            dataDir.mkdirs()
            dataDir
        }
    }

    actual suspend fun createDirectoryIfNotExists(directory: File): Result<Unit> {
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

    actual suspend fun fileExists(file: File): Boolean {
        return withContext(Dispatchers.IO) {
            file.exists()
        }
    }

    actual suspend fun getFileSize(file: File): Long {
        return withContext(Dispatchers.IO) {
            if (file.exists()) file.length() else 0L
        }
    }

    actual suspend fun deleteFile(file: File): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                if (file.exists()) file.delete()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    actual suspend fun listFiles(directory: File, pattern: String?): List<File> {
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

    actual suspend fun copyFile(source: File, destination: File): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                source.copyTo(destination, overwrite = true)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    actual suspend fun moveFile(source: File, destination: File): Result<Unit> {
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
