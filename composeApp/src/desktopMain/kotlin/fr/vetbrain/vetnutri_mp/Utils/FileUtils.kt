package fr.vetbrain.vetnutri_mp.Utils

import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/** Classe utilitaire pour la gestion des fichiers sur la plateforme desktop */
object FileUtils {
    /**
     * Ouvre une boîte de dialogue pour sélectionner un fichier JSON
     *
     * @return Le contenu du fichier sélectionné ou null si aucun fichier n'est sélectionné
     */
    fun openJsonFileDialog(): String? {
        val fileChooser =
                JFileChooser().apply {
                    dialogTitle = "Importer des animaux"
                    fileFilter = FileNameExtensionFilter("Fichiers JSON (*.json)", "json")
                }

        return if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                File(fileChooser.selectedFile.absolutePath).readText()
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    /**
     * Lit le contenu d'un fichier JSON à partir d'un chemin spécifié
     *
     * @param path Le chemin du fichier à lire
     * @return Le contenu du fichier ou null en cas d'erreur
     */
    fun readJsonFile(path: String): String? {
        return try {
            File(path).readText()
        } catch (e: Exception) {
            null
        }
    }
}
