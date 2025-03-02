package fr.vetbrain.vetnutri_mp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import fr.vetbrain.vetnutri_mp.DataBase.getDatabaseBuilder
import fr.vetbrain.vetnutri_mp.DataBase.getRoomDatabase
import fr.vetbrain.vetnutri_mp.Localization.LocalizationManager
import fr.vetbrain.vetnutri_mp.Repository.DatabaseAnimalRepository
import fr.vetbrain.vetnutri_mp.Utils.ImportUtils
import java.io.File
import kotlinx.coroutines.runBlocking

fun main() = application {
    // Initialisation de la localisation
    LocalizationManager.initialize()

    // Initialisation de la base de données
    val appDatabase = getRoomDatabase(getDatabaseBuilder())

    // Création du repository des animaux
    val animalRepository = DatabaseAnimalRepository(appDatabase.animalDao())

    // Vérification de l'existence du fichier d'importation par défaut
    val defaultImportFile = File("animaux_import.json")
    if (defaultImportFile.exists()) {
        val jsonContent = defaultImportFile.readText()
        val animalsJson = ImportUtils.importAnimalsFromJson(jsonContent)

        if (animalsJson.isNotEmpty()) {
            runBlocking {
                val importedCount = animalRepository.importAnimals(animalsJson)
                println("$importedCount animaux importés avec succès.")
            }
        }
    }

    Window(
            onCloseRequest = ::exitApplication,
            title = "VetNutri MP",
    ) { App(appDatabase) }
}
