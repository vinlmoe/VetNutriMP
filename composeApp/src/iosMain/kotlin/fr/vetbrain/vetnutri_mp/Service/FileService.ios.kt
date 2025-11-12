package fr.vetbrain.vetnutri_mp.Service

import fr.vetbrain.vetnutri_mp.PlatformFile.PlatformFile
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSHomeDirectory
import platform.Foundation.NSUserDomainMask

/** Implémentation iOS du FileService */
@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual class FileService {
    private suspend fun resolveDocumentsPath(): String {
        return withContext(AppDispatchers.IO) {
            try {
                val url =
                        NSFileManager.defaultManager.URLForDirectory(
                                NSDocumentDirectory,
                                NSUserDomainMask,
                                null,
                                true,
                                null
                        )
                val path = url?.path
                if (path != null && path.isNotEmpty()) path else NSHomeDirectory() + "/Documents"
            } catch (_: Exception) {
                println("[FileService][iOS] Échec résolution URLForDirectory, utilisation de NSHomeDirectory/Documents")
                NSHomeDirectory() + "/Documents"
            }
        }
    }
    actual suspend fun getBackupDirectory(): PlatformFile {
        val documentsPath = resolveDocumentsPath()
        return withContext(AppDispatchers.IO) {
            val backupDir = PlatformFile.create("$documentsPath/VetNutriMP/backups")
            if (!backupDir.exists()) backupDir.mkdirs()
            println("[FileService][iOS] Répertoire backup existe=${backupDir.exists()} contenuCount=${backupDir.listFiles()?.size ?: 0}")
            backupDir
        }
    }

    actual suspend fun getDataDirectory(): PlatformFile {
        val documentsPath = resolveDocumentsPath()
        return withContext(AppDispatchers.IO) {
            val dataDir = PlatformFile.create("$documentsPath/VetNutriMP/data")
            if (!dataDir.exists()) dataDir.mkdirs()
            println("[FileService][iOS] Répertoire data existe=${dataDir.exists()} contenuCount=${dataDir.listFiles()?.size ?: 0}")
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
            val exists = file.exists()
            exists
        }
    }

    actual suspend fun getFileSize(file: PlatformFile): Long {
        return withContext(AppDispatchers.IO) { if (file.exists()) file.length else 0L }
    }

    actual suspend fun deleteFile(file: PlatformFile): Result<Unit> {
        return withContext(AppDispatchers.IO) {
            try {
                if (file.exists()) {
                    file.delete()
                } else {
                }
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
                    val filtered =
                            if (pattern != null) files.filter { it.name.matches(pattern.toRegex()) }
                            else files
                    println("[FileService][iOS] Listing: dir=${directory.absolutePath} total=${files.size} filtrés=${filtered.size} pattern=${pattern}")
                    filtered
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    actual suspend fun copyFile(source: PlatformFile, destination: PlatformFile): Result<Unit> {
        return withContext(AppDispatchers.IO) {
            try {
                println("[FileService][iOS] Copie: ${source.absolutePath} => ${destination.absolutePath}")
                source.copyTo(destination, overwrite = true)
                Result.success(Unit)
            } catch (e: Exception) {
                println("[FileService][iOS] Erreur copie ${source.absolutePath} => ${destination.absolutePath}: ${e.message}")
                Result.failure(e)
            }
        }
    }

    actual suspend fun moveFile(source: PlatformFile, destination: PlatformFile): Result<Unit> {
        return withContext(AppDispatchers.IO) {
            try {
                println("[FileService][iOS] Déplacement: ${source.absolutePath} => ${destination.absolutePath}")
                source.renameTo(destination)
                Result.success(Unit)
            } catch (e: Exception) {
                println("[FileService][iOS] Erreur déplacement ${source.absolutePath} => ${destination.absolutePath}: ${e.message}")
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
                val content = file.readText()
                println("[FileService][iOS] Lecture OK: ${file.absolutePath} taille=${content.length}")
                Result.success(content)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
