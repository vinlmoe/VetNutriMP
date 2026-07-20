package fr.vetbrain.vetnutri_mp.ExcelPlatform

import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * Implémentation Desktop des opérations de fichiers Excel/CSV.
 * Reste synchrone/bloquant en interne (JFileChooser est modal) : `suspend` ici sert uniquement
 * à unifier la signature commune avec Android/iOS, pas à changer le comportement.
 */
actual suspend fun openCsvFileForImport(): String? {
    // Approche synchrone simple pour éviter les problèmes de coroutines
    val fileChooser =
            JFileChooser().apply {
                dialogTitle = "Importer un fichier CSV"
                fileFilter = FileNameExtensionFilter("Fichiers CSV (*.csv)", "csv")
                fileFilter =
                        FileNameExtensionFilter("Fichiers Excel (*.xlsx, *.xls)", "xlsx", "xls")
            }

    return if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        try {
            File(fileChooser.selectedFile.absolutePath).readText()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    } else {
        null
    }
}

actual suspend fun saveCsvFileForExport(csvContent: String, defaultFileName: String): Boolean {
    val fileChooser =
            JFileChooser().apply {
                dialogTitle = "Exporter vers CSV"
                selectedFile = File(defaultFileName)
                fileFilter = FileNameExtensionFilter("Fichiers CSV (*.csv)", "csv")
            }

    return if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
        try {
            val file = fileChooser.selectedFile
            val csvFile = File(file.parent, ensureCsvExtension(file.name))
            csvFile.writeText(csvContent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    } else {
        false
    }
}

actual suspend fun openCsvFileWithPreview(): String? {
    // Pour Desktop, on utilise la même fonction que l'import normal
    return openCsvFileForImport()
}

actual fun isCsvFileOperationsSupported(): Boolean {
    return true
}

/** S'assure que le nom de fichier se termine par ".csv" (ajoute l'extension si absente). */
internal fun ensureCsvExtension(fileName: String): String =
    if (fileName.endsWith(".csv")) fileName else "$fileName.csv"
