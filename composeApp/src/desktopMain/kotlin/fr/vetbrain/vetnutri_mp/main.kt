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
import fr.vetbrain.vetnutri_mp.Utils.FileUtils
import fr.vetbrain.vetnutri_mp.Utils.ImportUtils
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalListViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.ImportViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.filechooser.FileFilter
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.encodeToStream
import kotlin.system.exitProcess

// Configuration du gestionnaire d'exceptions pour desktop (accessible globalement)
private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
    throwable.printStackTrace()
}

private var appScope: CoroutineScope? = null

private fun chooseFileOnEdt(dialogTitle: String, fileFilter: FileFilter): File? {
    var selectedFile: File? = null
    val openChooser = {
        val chooser =
                JFileChooser().apply {
                    this.dialogTitle = dialogTitle
                    this.fileFilter = fileFilter
                }
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.selectedFile
        }
    }
    if (javax.swing.SwingUtilities.isEventDispatchThread()) {
        openChooser()
    } else {
        javax.swing.SwingUtilities.invokeAndWait { openChooser() }
    }
    return selectedFile
}

private fun createExtensionFilter(description: String, vararg extensions: String): FileFilter =
        object : FileFilter() {
            override fun accept(f: File): Boolean {
                if (f.isDirectory) return true
                val name = f.name.lowercase()
                return extensions.any { extension -> name.endsWith(".${extension.lowercase()}") }
            }

            override fun getDescription(): String = description
        }

// L'entrée principale de l'application
suspend fun main(args: Array<String> = emptyArray()) {
    
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
        when (args[0]) {
            "import-ani" -> {
                if (args.size <= 1) {
                    exitProcess(1)
                }
                val fileName = args[1]
                val file = File(fileName)
                val absoluteFile =
                        if (file.isAbsolute) file
                        else File(System.getProperty("user.dir"), fileName)

                if (!absoluteFile.exists()) {
                    exitProcess(1)
                }

                val jsonContent = withContext(Dispatchers.IO) { absoluteFile.readText() }
                val importResult = ImportUtils.importAnimalsFromJson(jsonContent)

                if (importResult.animals.isNotEmpty()) {
                    if (importResult.foods.isNotEmpty()) {
                        foodRepository.importFoods(importResult.foods)
                    }
                    animalRepository.importAnimals(importResult.animals)
                }
                exitProcess(0)
            }
            "import-food" -> {
                if (args.size <= 1) {
                    exitProcess(1)
                }
                val fileName = args[1]
                val file = File(fileName)
                val absoluteFile =
                        if (file.isAbsolute) file
                        else File(System.getProperty("user.dir"), fileName)

                if (!absoluteFile.exists()) {
                    exitProcess(1)
                }

                val jsonContent = withContext(Dispatchers.IO) { absoluteFile.readText() }
                val foodsJson = ImportUtils.importFoodsFromJson(jsonContent)

                if (foodsJson.isNotEmpty()) {
                    foodRepository.importFoods(foodsJson)
                }
                exitProcess(0)
            }
            "import-ref" -> {
                if (args.size <= 1) {
                    exitProcess(1)
                }
                val fileName = args[1]
                val file = File(fileName)
                val absoluteFile =
                        if (file.isAbsolute) file
                        else File(System.getProperty("user.dir"), fileName)

                if (!absoluteFile.exists()) {
                    exitProcess(1)
                }

                try {
                    ImportUtils.clearResolutionsProblematiques()
                    val jsonContent = withContext(Dispatchers.IO) { absoluteFile.readText() }
                    ImportUtils.importNutritionalRequirementsFromJson(
                            jsonContent = jsonContent,
                            sauvegarderEnBase = false
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    exitProcess(1)
                }
                exitProcess(0)
            }
            else -> {
                exitProcess(1)
            }
        }
    } else {
        // Lancement normal de l'application avec interface graphique
        appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)

        application {
            // Vérification de l'existence du fichier d'importation par défaut
            val defaultImportFile = File("animaux_import.json")
            if (defaultImportFile.exists()) {
                appScope?.launch {
                    val jsonContent = withContext(Dispatchers.IO) { defaultImportFile.readText() }
                    val importResult = ImportUtils.importAnimalsFromJson(jsonContent)
                    if (importResult.animals.isNotEmpty()) {
                        animalRepository.importAnimals(importResult.animals)
                    }
                }
            }

            Window(
                    onCloseRequest = {
                        appScope?.cancel()
                        exitApplication()
                    },
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
actual fun importAnimalsFromFile(
        viewModel: AnimalListViewModel,
        clearFoodsBeforeImport: Boolean
) {
    val selectedFile =
            chooseFileOnEdt(
                    dialogTitle = "Sélectionner un fichier JSON d'animaux",
                    fileFilter = FileNameExtensionFilter("Fichiers JSON", "json")
            ) ?: return

    (appScope ?: CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)).launch {
        if (clearFoodsBeforeImport) {
            val clearError = runCatching { viewModel.getFoodRepository()?.clearAllFoods() }.exceptionOrNull()
            if (clearError != null) {
                viewModel.setImportError(
                        "Erreur lors de la suppression des aliments: ${clearError.message}"
                )
                return@launch
            }
        }

        runCatching { selectedFile.readText() }
                .onSuccess { jsonContent -> viewModel.importAnimalsFromJson(jsonContent) }
                .onFailure { error ->
                    viewModel.setImportError(
                            "Erreur lors de la lecture du fichier: ${error.message}"
                    )
                }
    }
}

/**
 * Importe les aliments à partir d'un fichier JSON. Cette fonction est spécifique à la plateforme
 * desktop.
 */
actual fun importFoodsFromFile(viewModel: SettingsViewModel) {
    val selectedFile =
            chooseFileOnEdt(
                    dialogTitle = "Sélectionner un fichier JSON",
                    fileFilter = createExtensionFilter("Fichiers JSON (*.json)", "json")
            ) ?: return

    (appScope ?: CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)).launch {
        runCatching { selectedFile.readText() }
                .mapCatching { jsonContent -> ImportUtils.importFoodsFromJson(jsonContent) }
                .onSuccess { foodsJson ->
                    if (foodsJson.isNotEmpty()) {
                        viewModel.importFoodsFromList(foodsJson)
                    }
                }
                .onFailure { error ->
                    viewModel.setImportResult(
                            SettingsViewModel.ImportResult.Error(
                                    "Erreur lors de la lecture du fichier: ${error.message}"
                            )
                    )
                }
    }
}

/**
 * Importe les références nutritionnelles à partir d'un fichier .vbnr.json. Cette fonction est
 * spécifique à la plateforme desktop.
 */
actual fun importNutritionalRequirementsFromFile(viewModel: ImportViewModel) {
    val selectedFile =
            chooseFileOnEdt(
                    dialogTitle = "Sélectionner un fichier de références nutritionnelles (.vbnr.json)",
                    fileFilter = createExtensionFilter(
                            "Fichiers de références nutritionnelles (.vbnr.json, .json)",
                            "vbnr.json",
                            "json"
                    )
            )

    if (selectedFile == null) {
        viewModel.setNutritionalRequirementImportError("Importation annulée par l'utilisateur")
        return
    }

    // Lecture sur IO puis délégation au ViewModel pour centraliser l'état d'import.
    (appScope ?: CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)).launch {
        try {
            val jsonContent = withContext(Dispatchers.IO) { selectedFile.readText() }
            withContext(Dispatchers.Main) {
                viewModel.importNutritionalRequirementsFromJson(jsonContent)
            }
        } catch (e: Exception) {
            viewModel.setNutritionalRequirementImportError(
                    "Erreur lors de la lecture du fichier: ${e.message}"
            )
        }
    }
}

/** Importe des données au nouveau format API (enveloppe) depuis un fichier – Desktop. */
actual fun importApiFromFile(viewModel: SettingsViewModel) {
    val selectedFile =
            chooseFileOnEdt(
                    dialogTitle = "Sélectionner un fichier d'export API (.json)",
                    fileFilter = createExtensionFilter("Fichiers JSON (.json)", "json")
            )

    if (selectedFile == null) {
        viewModel.setImportResult(SettingsViewModel.ImportResult.Error("❌ Import API annulé"))
        return
    }

    viewModel.startApiImport()
    (appScope ?: CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)).launch {
        try {
            val content = withContext(Dispatchers.IO) { selectedFile.readText() }
            val exportRepo =
                    fr.vetbrain.vetnutri_mp.Repository.ExportImportRepository(
                            animalRepository = viewModel.animalRepository,
                            foodRepository = viewModel.foodRepository,
                            equationRepository = viewModel.equationRepository,
                            referenceRepository = viewModel.referenceEvRepository,
                            biblioRepository = viewModel.biblioRefRepository,
                            consultationRepository = viewModel.consultationRepository,
                            recipeRepository = viewModel.recipeRepository,
                            conseilRepository = viewModel.conseilRepository
                    )
            // Découper l'import en étapes et mettre à jour la progression/logs via callbacks
            // simples
            viewModel.appendApiImportLog("Lecture du fichier terminée")
            val counts = withContext(Dispatchers.IO) {
                exportRepo.importAll(
                        apiJson = content,
                        listener =
                                fr.vetbrain.vetnutri_mp.Repository.ExportImportRepository
                                        .ImportProgressListener(
                                                onProgress = { p ->
                                                    viewModel.updateApiImportProgress(p)
                                                },
                                                onLog = { msg ->
                                                    viewModel.appendApiImportLog(msg)
                                                }
                                        )
                )
            }
            viewModel.updateApiImportProgress(1.0)
            val total = counts.animals + counts.foods + counts.equations + counts.references + counts.conseils
            viewModel.appendApiImportLog(
                    "Import terminé → animals=${counts.animals}, foods=${counts.foods}, equations=${counts.equations}, refs=${counts.references}, conseils=${counts.conseils}"
            )
            viewModel.setImportResult(
                    SettingsViewModel.ImportResult.Success(
                            count = total,
                            importedCount = counts.animals + counts.foods,
                            updatedCount = 0,
                            deletedCount = 0,
                            errorCount = 0,
                            nonResolvedNutrients = 0,
                            conseils = counts.conseils
                    )
            )
        } catch (e: Exception) {
            viewModel.appendApiImportLog("Erreur: ${e.message}")
            viewModel.setImportResult(
                    SettingsViewModel.ImportResult.Error("Erreur import API: ${e.message}")
            )
        } finally {
            viewModel.finishApiImport()
        }
    }
}

@Suppress("UNUSED_PARAMETER")
actual fun exportJsonToFile(content: String, defaultFileName: String): Boolean {
    var resultat: Boolean = false
    if (javax.swing.SwingUtilities.isEventDispatchThread()) {
        resultat =
                fr.vetbrain.vetnutri_mp.Utils.FileUtils.saveJsonFileDialog(
                        content = content,
                        defaultFileName = defaultFileName
                )
    } else {
        javax.swing.SwingUtilities.invokeAndWait {
            resultat =
                    fr.vetbrain.vetnutri_mp.Utils.FileUtils.saveJsonFileDialog(
                            content = content,
                            defaultFileName = defaultFileName
                    )
        }
    }
    return resultat
}

@Suppress("UNUSED_PARAMETER")
@OptIn(ExperimentalSerializationApi::class)
actual fun exportApiEnvelopeToFile(
    envelope: fr.vetbrain.vetnutri_mp.Data.ApiEnvelope,
    defaultFileName: String
): Boolean {
    var resultat: Boolean = false
    if (javax.swing.SwingUtilities.isEventDispatchThread()) {
        resultat =
            fr.vetbrain.vetnutri_mp.Utils.FileUtils.saveJsonFileDialogStream(
                defaultFileName = defaultFileName
            ) { stream ->
                val json = fr.vetbrain.vetnutri_mp.Utils.createExportJson()
                json.encodeToStream(
                    fr.vetbrain.vetnutri_mp.Data.ApiEnvelope.serializer(),
                    envelope,
                    stream
                )
            }
    } else {
        javax.swing.SwingUtilities.invokeAndWait {
            resultat =
                fr.vetbrain.vetnutri_mp.Utils.FileUtils.saveJsonFileDialogStream(
                    defaultFileName = defaultFileName
                ) { stream ->
                    val json = fr.vetbrain.vetnutri_mp.Utils.createExportJson()
                    json.encodeToStream(
                        fr.vetbrain.vetnutri_mp.Data.ApiEnvelope.serializer(),
                        envelope,
                        stream
                    )
                }
        }
    }
    return resultat
}

actual fun openJsonFileContent(): String? {
    var contenu: String? = null
    if (javax.swing.SwingUtilities.isEventDispatchThread()) {
        contenu = fr.vetbrain.vetnutri_mp.Utils.FileUtils.openJsonFileDialog()
    } else {
        javax.swing.SwingUtilities.invokeAndWait {
            contenu = fr.vetbrain.vetnutri_mp.Utils.FileUtils.openJsonFileDialog()
        }
    }
    return contenu
}

actual suspend fun exportPdfDocument(
    documentType: fr.vetbrain.vetnutri_mp.Export.DocumentType,
    data: fr.vetbrain.vetnutri_mp.Export.ExportData,
    defaultFileName: String
): Boolean {
    val html: String = fr.vetbrain.vetnutri_mp.Export.HtmlDocumentBuilder.buildHtml(documentType, data)
    return try {
        val baos = java.io.ByteArrayOutputStream()
        com.openhtmltopdf.pdfboxout.PdfRendererBuilder().withHtmlContent(html, null).toStream(baos).run()
        val bytes = baos.toByteArray()
        
        // Appel direct comme dans exportJsonToFile
        fr.vetbrain.vetnutri_mp.Utils.FileUtils.saveBinaryFileDialog(
            bytes = bytes,
            defaultFileName = defaultFileName.ifBlank { "document.pdf" }
        )
    } catch (t: Throwable) {
        t.printStackTrace()
        false
    }
}
