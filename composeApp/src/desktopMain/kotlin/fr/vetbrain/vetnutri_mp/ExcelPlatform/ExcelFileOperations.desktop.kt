package fr.vetbrain.vetnutri_mp.ExcelPlatform

import java.io.File
import java.util.concurrent.FutureTask
import javax.swing.JFileChooser
import javax.swing.SwingUtilities
import javax.swing.filechooser.FileNameExtensionFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implémentation Desktop des opérations de fichiers Excel/CSV.
 *
 * Le dialogue Swing est modal. Il ne doit pas être ouvert directement depuis une coroutine
 * Compose : sa boucle AWT imbriquée peut alors reprendre la continuation Compose de façon
 * réentrante et corrompre son état. On quitte d'abord le dispatcher Compose, puis on demande à
 * l'EDT d'afficher le dialogue.
 */
actual suspend fun openCsvFileForImport(): String? {
    val selectedPath =
            runSwingDialog {
                val fileChooser =
                        JFileChooser().apply {
                            dialogTitle = "Importer un fichier CSV"
                            addChoosableFileFilter(
                                    FileNameExtensionFilter("Fichiers CSV (*.csv)", "csv")
                            )
                            addChoosableFileFilter(
                                    FileNameExtensionFilter(
                                            "Fichiers Excel (*.xlsx, *.xls)",
                                            "xlsx",
                                            "xls"
                                    )
                            )
                        }

                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    fileChooser.selectedFile.absolutePath
                } else {
                    null
                }
            }

    return if (selectedPath != null) {
        try {
            withContext(Dispatchers.IO) { File(selectedPath).readText() }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    } else {
        null
    }
}

actual suspend fun saveCsvFileForExport(csvContent: String, defaultFileName: String): Boolean {
    val selectedPath =
            runSwingDialog {
                val fileChooser =
                        JFileChooser().apply {
                            dialogTitle = "Exporter vers CSV"
                            selectedFile = File(defaultFileName)
                            fileFilter = FileNameExtensionFilter("Fichiers CSV (*.csv)", "csv")
                        }

                if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                    fileChooser.selectedFile.absolutePath
                } else {
                    null
                }
            }

    return if (selectedPath != null) {
        try {
            withContext(Dispatchers.IO) {
                val file = File(selectedPath)
                val csvFile = File(file.parent, ensureCsvExtension(file.name))
                csvFile.writeText(csvContent)
            }
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

/**
 * Exécute une opération Swing sur l'EDT sans conserver la continuation appelante dans la boucle
 * modale AWT.
 */
internal suspend fun <T> runSwingDialog(block: () -> T): T =
        withContext(Dispatchers.IO) {
            val task = FutureTask(block)
            SwingUtilities.invokeAndWait(task)
            task.get()
        }
