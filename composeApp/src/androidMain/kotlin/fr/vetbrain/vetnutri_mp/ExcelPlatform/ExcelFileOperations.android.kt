package fr.vetbrain.vetnutri_mp.ExcelPlatform

import fr.vetbrain.vetnutri_mp.Localization.AndroidContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implémentation Android des opérations de fichiers Excel/CSV, basée sur
 * ExcelFileOperationsBridge (ActivityResultLauncher enregistrés dans MainActivity.onCreate).
 */

actual suspend fun openCsvFileForImport(): String? {
    val uri = ExcelFileOperationsBridge.pickImportUri() ?: return null
    return withContext(Dispatchers.IO) {
        try {
            AndroidContext.appContext.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().use { it.readText() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

actual suspend fun saveCsvFileForExport(csvContent: String, defaultFileName: String): Boolean {
    val uri = ExcelFileOperationsBridge.pickExportUri(defaultFileName) ?: return false
    return withContext(Dispatchers.IO) {
        try {
            AndroidContext.appContext.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(csvContent.toByteArray())
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

actual suspend fun openCsvFileWithPreview(): String? = openCsvFileForImport()

actual fun isCsvFileOperationsSupported(): Boolean = true
