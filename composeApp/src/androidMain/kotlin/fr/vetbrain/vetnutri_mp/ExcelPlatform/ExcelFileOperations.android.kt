package fr.vetbrain.vetnutri_mp.ExcelPlatform

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import fr.vetbrain.vetnutri_mp.createFileService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Implémentation Android des opérations de fichiers Excel/CSV
 */

actual fun openCsvFileForImport(): String? {
    // Cette fonction sera appelée depuis un composable avec le contexte approprié
    // L'implémentation réelle se fait via le composable openCsvFileForImportComposable
    return null
}

actual fun saveCsvFileForExport(csvContent: String, defaultFileName: String): Boolean {
    // Cette fonction sera appelée depuis un composable avec le contexte approprié
    // L'implémentation réelle se fait via le composable saveCsvFileForExportComposable
    return false
}

actual fun openCsvFileWithPreview(): String? {
    // Cette fonction sera appelée depuis un composable avec le contexte approprié
    // L'implémentation réelle se fait via le composable openCsvFileWithPreviewComposable
    return null
}

actual fun isCsvFileOperationsSupported(): Boolean {
    return true
}

/**
 * Composable pour ouvrir un fichier CSV sur Android
 */
@Composable
fun openCsvFileForImportComposable(
    onFileSelected: (String?) -> Unit
) {
    val context = LocalContext.current
    var selectedFileContent by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().use { it.readText() }
                }
                selectedFileContent = content
                onFileSelected(content)
            } catch (e: Exception) {
                e.printStackTrace()
                onFileSelected(null)
            }
        } else {
            onFileSelected(null)
        }
    }

    LaunchedEffect(Unit) {
        filePickerLauncher.launch("text/csv")
    }
}

/**
 * Composable pour sauvegarder un fichier CSV sur Android
 */
@Composable
fun saveCsvFileForExportComposable(
    csvContent: String,
    defaultFileName: String,
    onSaveResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var saveResult by remember { mutableStateOf<Boolean?>(null) }

    val saveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(csvContent.toByteArray())
                }
                saveResult = true
                onSaveResult(true)
            } catch (e: Exception) {
                e.printStackTrace()
                saveResult = false
                onSaveResult(false)
            }
        } else {
            saveResult = false
            onSaveResult(false)
        }
    }

    LaunchedEffect(Unit) {
        saveLauncher.launch(defaultFileName)
    }
}

/**
 * Composable pour ouvrir un fichier CSV avec prévisualisation sur Android
 */
@Composable
fun openCsvFileWithPreviewComposable(
    onFileSelected: (String?) -> Unit
) {
    // Utilise la même implémentation que l'import normal
    openCsvFileForImportComposable(onFileSelected)
}
