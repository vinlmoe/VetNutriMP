package fr.vetbrain.vetnutri_mp

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import fr.vetbrain.vetnutri_mp.DataBase.getDatabaseBuilder
import fr.vetbrain.vetnutri_mp.DataBase.getRoomDatabase
import fr.vetbrain.vetnutri_mp.Localization.LocalizationManager
import fr.vetbrain.vetnutri_mp.Repository.DatabaseAnimalRepository
import fr.vetbrain.vetnutri_mp.Utils.ImportUtils
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalListViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() = application {
    // Initialisation de la localisation
    LocalizationManager.initialize()

    // Initialisation de la base de données
    val appDatabase = getRoomDatabase(getDatabaseBuilder())

    // Création du repository des animaux
    val animalRepository = DatabaseAnimalRepository(appDatabase.animalDao(), appDatabase.foodDao())

    // Vérification de l'existence du fichier d'importation par défaut
    val defaultImportFile = File("animaux_import.json")
    if (defaultImportFile.exists()) {
        val jsonContent = defaultImportFile.readText()
        val importResult = ImportUtils.importAnimalsFromJson(jsonContent)

        if (importResult.animals.isNotEmpty()) {
            runBlocking {
                val importedCount = animalRepository.importAnimals(importResult.animals)
                println("$importedCount animaux importés avec succès.")
            }
        }
    }

    Window(
            onCloseRequest = ::exitApplication,
            title = "VetNutri",
            state = rememberWindowState(width = 1200.dp, height = 800.dp)
    ) { App(appDatabase) }
}

/**
 * Importe les animaux à partir d'un fichier JSON. Cette fonction est spécifique à la plateforme
 * desktop.
 */
actual fun importAnimalsFromFile(viewModel: AnimalListViewModel) {
    val fileChooser = JFileChooser()
    fileChooser.dialogTitle = "Sélectionner un fichier JSON d'animaux"
    fileChooser.fileFilter = FileNameExtensionFilter("Fichiers JSON", "json")

    val result = fileChooser.showOpenDialog(null)
    if (result == JFileChooser.APPROVE_OPTION) {
        val selectedFile = fileChooser.selectedFile
        try {
            val jsonContent = selectedFile.readText()
            println("Fichier JSON sélectionné. Taille du contenu: ${jsonContent.length} caractères")

            // Lancer l'importation dans un thread séparé
            GlobalScope.launch { viewModel.importAnimalsFromJson(jsonContent) }
        } catch (e: Exception) {
            println("Erreur lors de la lecture du fichier JSON: ${e.message}")
            e.printStackTrace()
        }
    }
}

/**
 * Importe les aliments à partir d'un fichier JSON. Cette fonction est spécifique à la plateforme
 * desktop.
 */
actual fun importFoodsFromFile(viewModel: SettingsViewModel) {
    val fileChooser =
            JFileChooser().apply {
                dialogTitle = "Sélectionner un fichier JSON"
                fileFilter =
                        object : javax.swing.filechooser.FileFilter() {
                            override fun accept(f: File): Boolean {
                                return f.isDirectory || f.name.lowercase().endsWith(".json")
                            }

                            override fun getDescription(): String {
                                return "Fichiers JSON (*.json)"
                            }
                        }
            }

    val result = fileChooser.showOpenDialog(null)
    if (result == JFileChooser.APPROVE_OPTION) {
        val selectedFile = fileChooser.selectedFile
        println("Fichier sélectionné: ${selectedFile.absolutePath}")

        try {
            val jsonContent = selectedFile.readText()
            println("Fichier JSON sélectionné. Taille du contenu: ${jsonContent.length} caractères")

            // Utiliser ImportUtils pour importer les aliments, qui détectera automatiquement
            // le format (standard ou Oza2)
            val foodsJson = ImportUtils.importFoodsFromJson(jsonContent)

            if (foodsJson.isNotEmpty()) {
                println("${foodsJson.size} aliments trouvés dans le fichier JSON")

                // Lancer l'importation dans un thread séparé
                GlobalScope.launch {
                    println("Début de l'importation dans la base de données...")
                    val importedCount = viewModel.importFoodsFromList(foodsJson)
                    println("Importation terminée. ${importedCount} aliments importés.")
                }
            } else {
                println("Aucun aliment trouvé dans le fichier JSON")
            }
        } catch (e: Exception) {
            println("Erreur lors de la lecture du fichier JSON: ${e.message}")
            e.printStackTrace()
        }
    }
}
