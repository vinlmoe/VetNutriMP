package fr.vetbrain.vetnutri_mp.Utils

import java.io.File
import java.io.OutputStream
import kotlinx.coroutines.runBlocking
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/** Classe utilitaire pour la gestion des fichiers sur la plateforme desktop */
object FileUtils {
    private const val LAST_SAVE_DIR_KEY = "last_save_directory"
    private val preferencesStorage: PreferencesStorage = createPreferencesStorage()
    private var lastSaveDirectory: File? = run {
        val saved = runBlocking { preferencesStorage.getString(LAST_SAVE_DIR_KEY, "") }
        if (saved.isNullOrBlank()) null else File(saved)
    }

    private fun applyLastDirectory(fileChooser: JFileChooser) {
        val dir = lastSaveDirectory
        if (dir != null && dir.exists() && dir.isDirectory) {
            fileChooser.currentDirectory = dir
        }
    }

    private fun rememberDirectory(file: File?) {
        val dir = if (file?.isDirectory == true) file else file?.parentFile
        if (dir != null && dir.exists() && dir.isDirectory) {
            lastSaveDirectory = dir
            runBlocking { preferencesStorage.saveString(LAST_SAVE_DIR_KEY, dir.absolutePath) }
        }
    }
    /**
     * Ouvre une boîte de dialogue pour sélectionner un fichier JSON
     *
     * @return Le contenu du fichier sélectionné ou null si aucun fichier n'est sélectionné
     */
    fun openJsonFileDialog(): String? {
        val fileChooser =
                JFileChooser().apply {
                    dialogTitle = "Importer un fichier JSON"
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

    /** Sauvegarde un contenu binaire via une boîte de dialogue. */
    fun saveBinaryFileDialog(bytes: ByteArray, defaultFileName: String = "document.pdf"): Boolean {
        val fileChooser =
                JFileChooser().apply {
                    dialogTitle = "Enregistrer un fichier"
                    applyLastDirectory(this)
                    selectedFile = java.io.File(defaultFileName)
                }
        val userSelection = fileChooser.showSaveDialog(null)
        return if (userSelection == JFileChooser.APPROVE_OPTION) {
            return try {
                val file = fileChooser.selectedFile
                java.nio.file.Files.write(file.toPath(), bytes)
                rememberDirectory(file)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        } else {
            false
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

    /**
     * Ouvre une boîte de dialogue pour enregistrer un contenu JSON dans un fichier
     *
     * @param content Contenu JSON à sauvegarder
     * @param defaultFileName Nom de fichier suggéré
     * @return true si la sauvegarde a réussi, false sinon
     */
    fun saveJsonFileDialog(content: String, defaultFileName: String): Boolean {
        val fileChooser =
                JFileChooser().apply {
                    dialogTitle = "Exporter un fichier JSON"
                    applyLastDirectory(this)
                    selectedFile = File(defaultFileName)
                    fileFilter = FileNameExtensionFilter("Fichiers JSON (*.json)", "json")
                }
        return if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                val file: File =
                        if (fileChooser.selectedFile.path.endsWith(".json", ignoreCase = true)) {
                            fileChooser.selectedFile
                        } else {
                            File(fileChooser.selectedFile.path + ".json")
                        }
                file.writeText(content)
                rememberDirectory(file)
                true
            } catch (e: Exception) {
                false
            }
        } else {
            false
        }
    }

    /**
     * Ouvre une boîte de dialogue pour enregistrer un contenu JSON en streaming.
     *
     * @param defaultFileName Nom de fichier suggéré
     * @param writeTo Fonction qui écrit le contenu dans l'OutputStream fourni
     * @return true si la sauvegarde a réussi, false sinon
     */
    fun saveJsonFileDialogStream(
        defaultFileName: String,
        writeTo: (OutputStream) -> Unit
    ): Boolean {
        val fileChooser =
            JFileChooser().apply {
                dialogTitle = "Exporter un fichier JSON"
                applyLastDirectory(this)
                selectedFile = File(defaultFileName)
                fileFilter = FileNameExtensionFilter("Fichiers JSON (*.json)", "json")
            }
        return if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                val file: File =
                    if (fileChooser.selectedFile.path.endsWith(".json", ignoreCase = true)) {
                        fileChooser.selectedFile
                    } else {
                        File(fileChooser.selectedFile.path + ".json")
                    }
                file.outputStream().use { stream ->
                    writeTo(stream)
                    stream.flush()
                }
                rememberDirectory(file)
                true
            } catch (e: Exception) {
                false
            }
        } else {
            false
        }
    }
}
