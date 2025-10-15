package fr.vetbrain.vetnutri_mp.ExcelPlatform

import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/** Implémentation Desktop des opérations de fichiers Excel/CSV */
actual fun openCsvFileForImport(): String? {
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

actual fun saveCsvFileForExport(csvContent: String, defaultFileName: String): Boolean {
    val fileChooser =
            JFileChooser().apply {
                dialogTitle = "Exporter vers CSV"
                selectedFile = File(defaultFileName)
                fileFilter = FileNameExtensionFilter("Fichiers CSV (*.csv)", "csv")
            }

    return if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
        try {
            val file = fileChooser.selectedFile
            // S'assurer que l'extension .csv est présente
            val fileName = if (file.name.endsWith(".csv")) file.name else "${file.name}.csv"
            val csvFile = File(file.parent, fileName)
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

actual fun openCsvFileWithPreview(): String? {
    // Pour Desktop, on utilise la même fonction que l'import normal
    return openCsvFileForImport()
}

actual fun isCsvFileOperationsSupported(): Boolean {
    return true
}
