package fr.vetbrain.vetnutri_mp.Service

import fr.vetbrain.vetnutri_mp.PlatformFile.PlatformFile
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/**
 * Implémentation iOS du FileService
 */
@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual class FileService {
    actual suspend fun getBackupDirectory(): PlatformFile {
        return withContext(AppDispatchers.IO) {
            val documentsPath = NSFileManager.defaultManager.URLForDirectory(
                NSDocumentDirectory,
                NSUserDomainMask,
                null,
                false,
                null
            )?.path ?: ""
            val backupDir = PlatformFile.create("$documentsPath/VetNutriMP/backups")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }
            backupDir
        }
    }

    actual suspend fun getDataDirectory(): PlatformFile {
        return withContext(AppDispatchers.IO) {
            val documentsPath = NSFileManager.defaultManager.URLForDirectory(
                NSDocumentDirectory,
                NSUserDomainMask,
                null,
                false,
                null
            )?.path ?: ""
            val dataDir = PlatformFile.create("$documentsPath/VetNutriMP/data")
            if (!dataDir.exists()) {
                dataDir.mkdirs()
            }
            dataDir
        }
    }

    actual suspend fun createDirectoryIfNotExists(directory: PlatformFile): Result<Unit> {
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

    actual suspend fun fileExists(file: PlatformFile): Boolean {
        return withContext(AppDispatchers.IO) {
            file.exists()
        }
    }

    actual suspend fun getFileSize(file: PlatformFile): Long {
        return withContext(AppDispatchers.IO) {
            if (file.exists()) file.length else 0L
        }
    }

    actual suspend fun deleteFile(file: PlatformFile): Result<Unit> {
        return withContext(AppDispatchers.IO) {
            try {
                if (file.exists()) file.delete()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    actual suspend fun listFiles(directory: PlatformFile, pattern: String?): List<PlatformFile> {
        return withContext(AppDispatchers.IO) {
            try {
                if (!directory.exists()) emptyList()
                else {
                    val files = directory.listFiles() ?: emptyList()
                    if (pattern != null) files.filter { it.name.matches(pattern.toRegex()) } else files
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    actual suspend fun copyFile(source: PlatformFile, destination: PlatformFile): Result<Unit> {
        return withContext(AppDispatchers.IO) {
            try {
                source.copyTo(destination, overwrite = true)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    actual suspend fun moveFile(source: PlatformFile, destination: PlatformFile): Result<Unit> {
        return withContext(AppDispatchers.IO) {
            try {
                source.renameTo(destination)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    actual suspend fun writeText(file: PlatformFile, text: String): Result<Unit> {
        return withContext(AppDispatchers.IO) {
            try {
                file.writeText(text)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    actual suspend fun readText(file: PlatformFile): Result<String> {
        return withContext(AppDispatchers.IO) {
            try {
                Result.success(file.readText())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}