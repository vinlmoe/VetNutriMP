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
import fr.vetbrain.vetnutri_mp.Utils.ImportUtils
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalListViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.ImportViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// L'entrée principale de l'application
fun main(args: Array<String> = emptyArray()) {
    // Initialisation de la localisation
    LocalizationManager.initialize()

    // Initialisation de la base de données
    val appDatabase = getRoomDatabase(getDatabaseBuilder())

    // Création du repository des animaux
    val animalRepository = DatabaseAnimalRepository(appDatabase.animalDao(), appDatabase.foodDao())
    val foodRepository =
            DatabaseFoodRepository(appDatabase.foodDao(), appDatabase.nutrientValueDao())

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
                        } else {
                        }
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
                        } else {
                        }
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

                            println("\n" + "=".repeat(70))
                            references.forEachIndexed { index, ref ->
                            }

                            // Afficher le rapport des résolutions problématiques
                            println("\n" + ImportUtils.genererRapportResolutionsProblematiques())
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                    }
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

            // Lancer l'importation dans un thread séparé
            GlobalScope.launch { viewModel.importAnimalsFromJson(jsonContent) }
        } catch (e: Exception) {
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

        try {
            val jsonContent = selectedFile.readText()

            // Utiliser ImportUtils pour importer les aliments, qui détectera automatiquement
            // le format (standard ou Oza2)
            val foodsJson = ImportUtils.importFoodsFromJson(jsonContent)

            if (foodsJson.isNotEmpty()) {

                // Lancer l'importation dans un thread séparé
                GlobalScope.launch {
                    val importResult = viewModel.importFoodsFromList(foodsJson)
                }
            } else {
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

/**
 * Importe les références nutritionnelles à partir d'un fichier .vbnr.json. Cette fonction est
 * spécifique à la plateforme desktop.
 */
@OptIn(DelicateCoroutinesApi::class)
actual fun importNutritionalRequirementsFromFile(viewModel: ImportViewModel) {
    val fileChooser = JFileChooser()
    fileChooser.dialogTitle = "Sélectionner un fichier de références nutritionnelles (.vbnr.json)"
    fileChooser.fileFilter =
            object : javax.swing.filechooser.FileFilter() {
                override fun accept(f: File): Boolean {
                    return f.isDirectory ||
                            f.name.lowercase().endsWith(".vbnr.json") ||
                            f.name.lowercase().endsWith(".json")
                }

                override fun getDescription(): String {
                    return "Fichiers de références nutritionnelles (.vbnr.json, .json)"
                }
            }

    val result = fileChooser.showOpenDialog(null)
    if (result == JFileChooser.APPROVE_OPTION) {
        val selectedFile = fileChooser.selectedFile

        // Mettre à jour le message de statut immédiatement
        viewModel.updateNutritionalRequirementImportResultMessage(
                "🔄 Lecture du fichier en cours..."
        )

        // Lancer l'importation dans un thread séparé avec sauvegarde automatique
        GlobalScope.launch {
            try {
                // Vider les résolutions problématiques précédentes
                ImportUtils.clearResolutionsProblematiques()

                // Lire le contenu du fichier
                val jsonContent = selectedFile.readText()
                viewModel.updateNutritionalRequirementImportResultMessage(
                        "🔄 Importation en cours avec sauvegarde automatique..."
                )

                // Utiliser la nouvelle fonction avec sauvegarde automatique
                val references =
                        ImportUtils.importNutritionalRequirementsFromJson(
                                jsonContent = jsonContent,
                                databaseReferenceEvRepository =
                                        viewModel.databaseReferenceEvRepository,
                                equationRepository = viewModel.equationRepository,
                                biblioRefRepository = viewModel.biblioRefRepository,
                                sauvegarderEnBase = true // Activer la sauvegarde automatique
                        )

                if (references.isNotEmpty()) {
                    // Générer un rapport de résolutions problématiques
                    val rapportResolutions = ImportUtils.genererRapportResolutionsProblematiques()
                    val messageSucces = StringBuilder()

                    messageSucces.append(
                            "✅ ${references.size} références nutritionnelles importées avec succès\n"
                    )
                    messageSucces.append(
                            "💾 Données sauvegardées automatiquement en base de données\n\n"
                    )

                    // Ajouter le détail des références
                    messageSucces.append("📋 Références importées:\n")
                    references.forEachIndexed { index, ref ->
                        messageSucces.append(
                                "  ${index + 1}. ${ref.nom} (${ref.espece} - ${ref.stadePhysio})"
                        )
                        if (ref.maladie) {
                            messageSucces.append(" - Maladie")
                        }
                        messageSucces.append("\n")
                    }

                    // Ajouter le rapport des résolutions si nécessaire
                    if (rapportResolutions.isNotEmpty()) {
                        messageSucces.append("\n⚠️ Résolutions de nutriments:\n")
                        messageSucces.append(rapportResolutions)
                    }

                    viewModel.updateNutritionalRequirementImportResultMessage(
                            messageSucces.toString()
                    )
                } else {
                    viewModel.updateNutritionalRequirementImportResultMessage(
                            "❌ Aucune référence nutritionnelle trouvée dans le fichier"
                    )
                }
            } catch (e: Exception) {
                val messageErreur = "❌ Erreur lors de l'importation: ${e.message}"
                viewModel.updateNutritionalRequirementImportResultMessage(messageErreur)
                println("Erreur détaillée: ${e.stackTraceToString()}")
            }
        }
    } else {
        viewModel.updateNutritionalRequirementImportResultMessage(
                "❌ Importation annulée par l'utilisateur"
        )
    }
}
