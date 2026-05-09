package fr.vetbrain.vetnutri_mp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import fr.vetbrain.vetnutri_mp.DataBase.getDatabaseBuilder
import fr.vetbrain.vetnutri_mp.DataBase.getRoomDatabase
import fr.vetbrain.vetnutri_mp.Localization.LocalizationManager
import fr.vetbrain.vetnutri_mp.Repository.DatabaseAnimalRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseFoodRepository
import java.io.File
import kotlinx.coroutines.runBlocking

// L'entrée principale de l'application Windows
fun main(args: Array<String> = emptyArray()) {
    // Initialisation de la localisation
    LocalizationManager.initialize()

    // Initialisation de la base de données
    val appDatabase = getRoomDatabase(getDatabaseBuilder())

    // Création du repository des animaux
    val animalRepository = DatabaseAnimalRepository(appDatabase.animalDao(), appDatabase.foodDao())
    val foodRepository =
            DatabaseFoodRepository(
                appDatabase.foodDao(),
                appDatabase.nutrientValueDao(),
                appDatabase.customNutrientDao(),
                appDatabase.alimentBiblioRefDao(),
                appDatabase.biblioRefDao()
            )

    // Vérifier si nous avons des arguments en ligne de commande
    if (args.isNotEmpty()) {
        runBlocking {
            when (args[0]) {
                "import-ani" -> {
                    if (args.size > 1) {
                        val fileName = args[1]

                        // Convertir en chemin absolu si nécessaire
                        val file = File(fileName)
                        val absoluteFile =
                                if (file.isAbsolute) file
                                else File(System.getProperty("user.dir"), fileName)

                        if (!absoluteFile.exists()) {
                            return@runBlocking
                        }

                        val jsonContent = absoluteFile.readText()
                        val importResult = ImportUtils.importAnimalsFromJson(jsonContent)

                        if (importResult.animals.isNotEmpty()) {
                            // Traiter d'abord les aliments des rations si présents
                            if (importResult.foods.isNotEmpty()) {
                                val importedFoodsCount =
                                        foodRepository.importFoods(importResult.foods)
                            }

                            // Importer les animaux
                            val importedCount = animalRepository.importAnimals(importResult.animals)
                        } else {}
                        return@runBlocking
                    } else {
                        return@runBlocking
                    }
                }
                "import-food" -> {
                    if (args.size > 1) {
                        val fileName = args[1]

                        // Convertir en chemin absolu si nécessaire
                        val file = File(fileName)
                        val absoluteFile =
                                if (file.isAbsolute) file
                                else File(System.getProperty("user.dir"), fileName)

                        if (!absoluteFile.exists()) {
                            return@runBlocking
                        }

                        val jsonContent = absoluteFile.readText()
                        val foodsJson = ImportUtils.importFoodsFromJson(jsonContent)

                        if (foodsJson.isNotEmpty()) {
                            val importedCount = foodRepository.importFoods(foodsJson)
                        } else {}
                        return@runBlocking
                    } else {
                        return@runBlocking
                    }
                }
                "import-ref" -> {
                    if (args.size > 1) {
                        val fileName = args[1]

                        // Convertir en chemin absolu si nécessaire
                        val file = File(fileName)
                        val absoluteFile =
                                if (file.isAbsolute) file
                                else File(System.getProperty("user.dir"), fileName)

                        if (!absoluteFile.exists()) {
                            return@runBlocking
                        }

                        try {
                            // Vider les résolutions problématiques précédentes
                            ImportUtils.clearResolutionsProblematiques()

                            val jsonContent = absoluteFile.readText()
                            val references =
                                    ImportUtils.importNutritionalRequirementsFromJson(
                                            jsonContent = jsonContent,
                                            sauvegarderEnBase =
                                                    false // Pour l'instant, pas de sauvegarde
                                            // automatique depuis CLI
                                            )

                            references.forEachIndexed { index, ref -> }

                            // Afficher le rapport des résolutions problématiques

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {}
                    return@runBlocking
                }
                else -> {
                    return@runBlocking
                }
            }
        }
    } else {
        // Lancement normal de l'application avec interface graphique
        application {
            // Vérification de l'existence du fichier d'importation par défaut
            val defaultImportFile = File("animaux_import.json")
            if (defaultImportFile.exists()) {
                val jsonContent = defaultImportFile.readText()
                val importResult = ImportUtils.importAnimalsFromJson(jsonContent)

                if (importResult.animals.isNotEmpty()) {
                    runBlocking {
                        val importedCount = animalRepository.importAnimals(importResult.animals)
                    }
                }
            }

            Window(
                    onCloseRequest = ::exitApplication,
                    title = "VetNutri",
                    state = rememberWindowState(width = 1200.dp, height = 800.dp)
            ) { App(appDatabase) }
        }
    }
}
