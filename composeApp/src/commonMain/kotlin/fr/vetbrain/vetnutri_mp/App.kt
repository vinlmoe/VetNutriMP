package fr.vetbrain.vetnutri_mp

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import fr.vetbrain.vetnutri_mp.Data.ApiEnvelope
import fr.vetbrain.vetnutri_mp.Data.ExamSession
import fr.vetbrain.vetnutri_mp.DataBase.AppDatabase
import fr.vetbrain.vetnutri_mp.Localization.LocalizationManager
import fr.vetbrain.vetnutri_mp.Navigation.*
import fr.vetbrain.vetnutri_mp.Repository.*
import fr.vetbrain.vetnutri_mp.Service.*
import fr.vetbrain.vetnutri_mp.Theme.VetNutriTheme
import fr.vetbrain.vetnutri_mp.Utils.PlatformDispatcher
import fr.vetbrain.vetnutri_mp.View.StartupScreen
import fr.vetbrain.vetnutri_mp.ViewModel.*
import fr.vetbrain.vetnutri_mp.Export.DocumentType
import fr.vetbrain.vetnutri_mp.Export.ExportData

// Fonctions d'importation de fichiers - implémentées par plateforme spécifique
expect fun importAnimalsFromFile(viewModel: AnimalListViewModel, clearFoodsBeforeImport: Boolean)
expect fun importFoodsFromFile(viewModel: SettingsViewModel)
expect fun importNutritionalRequirementsFromFile(viewModel: ImportViewModel)
expect fun importApiFromFile(viewModel: SettingsViewModel)
expect fun exportJsonToFile(content: String, defaultFileName: String): Boolean
expect fun exportApiEnvelopeToFile(envelope: ApiEnvelope, defaultFileName: String): Boolean
expect fun openJsonFileContent(): String?
expect fun performDatabaseFactoryReset(): String?
expect suspend fun exportPdfDocument(
    documentType: DocumentType,
    data: ExportData,
    defaultFileName: String
): Boolean
expect fun createFileService(): FileService

private suspend fun ensureDefaultConsultationKeywords(
    consultationRepository: ConsultationRepository
) {
    val defaultLabels = listOf(
        "MRC", "IPE", "Entéropathie chronique", "Urolithiases", "Insuffisance hépatique",
        "Shunt portosystémique", "Cardiomyopathie dilatée", "Maladie valvulaire dégénérative",
        "Cardiomyopathie hypertrophique", "Hypothyroïdie", "Hyperthyroïdie", "Diabète sucré",
        "Diabète insipide", "Obésité", "Cachexie", "Travaux dirigés", "Animal théorique"
    )
    val existingLabels = consultationRepository.getAllKeywords().map { it.label.lowercase() }.toSet()
    defaultLabels
        .filter { label -> !existingLabels.contains(label.lowercase()) }
        .forEach { label ->
            try {
                consultationRepository.saveKeyword(
                    fr.vetbrain.vetnutri_mp.Data.ConsultationKeyword(label = label)
                )
            } catch (_: Exception) {}
        }
}

private suspend fun ensureDefaultBiblioRef(biblioRefRepository: BiblioRefRepository) {
    try {
        val defaultUuid = "default-biblio"
        if (biblioRefRepository.getBiblioRefById(defaultUuid) == null) {
            biblioRefRepository.insertBiblioRef(
                fr.vetbrain.vetnutri_mp.Data.BiblioRef(
                    uuid = defaultUuid,
                    firstAuthor = "Système VetNutri",
                    year = 2024,
                    completeRef = "Référence par défaut générée automatiquement",
                    comments = "Créée automatiquement pour éviter les erreurs de clé étrangère",
                    consistent = 1
                )
            )
        }
    } catch (_: Exception) {}
}

@Composable
fun App(appDatabase: AppDatabase) {
    LaunchedEffect(Unit) { LocalizationManager.loadLocale() }

    val appContainer = rememberAppContainer(appDatabase)
    val animalRepository = appContainer.animalRepository
    val foodRepository = appContainer.foodRepository
    val consultationRepository = appContainer.consultationRepository
    val examGradingRepository = appContainer.examGradingRepository
    val recipeRepository = appContainer.recipeRepository
    val biblioRefRepository = appContainer.biblioRefRepository
    val equationRepository = appContainer.equationRepository
    val conseilRepository = appContainer.conseilRepository
    val databaseReferenceEvRepository = appContainer.referenceRepository
    val exportImportRepository = appContainer.exportImportRepository
    val fileService = appContainer.fileService
    val startupService = appContainer.startupService
    val preferencesRepository = appContainer.preferencesRepository

    val platformDispatcher = remember { PlatformDispatcher() }
    val nav = remember { AppNavController() }

    var examSession by remember { mutableStateOf<ExamSession?>(null) }
    var backupService by remember { mutableStateOf<BackupService?>(null) }
    var showStartupBackupDialog by remember { mutableStateOf(false) }
    var showStartupScreen by remember { mutableStateOf(true) }
    var showAnimalImportResult by remember { mutableStateOf(false) }
    var showFoodImportResult by remember { mutableStateOf(false) }

    // ViewModels
    val animalListViewModel = remember {
        AnimalListViewModel(
            animalRepository = animalRepository,
            foodRepository = foodRepository,
            recipeRepository = recipeRepository,
            referenceEvRepository = databaseReferenceEvRepository,
            equationRepository = equationRepository,
            biblioRefRepository = biblioRefRepository,
            consultationRepository = consultationRepository,
            conseilRepository = conseilRepository
        )
    }
    val examGradingViewModel = remember {
        ExamGradingViewModel(
            repository = examGradingRepository,
            exportImportRepository = exportImportRepository,
            fileService = fileService
        )
    }
    val animalDetailViewModel = remember {
        AnimalDetailViewModel(
            consultationRepository,
            animalRepository,
            databaseReferenceEvRepository,
            preferencesRepository,
            foodRepository,
            equationRepository
        )
    }
    val createAnimalViewModel = remember { CreateAnimalViewModel(animalRepository) }
    val settingsViewModel = remember {
        SettingsViewModel(
            animalRepository = animalRepository,
            foodRepository = foodRepository,
            recipeRepository = recipeRepository,
            referenceEvRepository = databaseReferenceEvRepository,
            equationRepository = equationRepository,
            biblioRefRepository = biblioRefRepository,
            consultationRepository = consultationRepository,
            conseilRepository = conseilRepository
        )
    }
    val foodListViewModel = remember { FoodListViewModel(foodRepository) }
    val biblioRefViewModel = remember { BiblioRefViewModel(biblioRefRepository) }
    val referenceEvViewModel = remember {
        ReferenceEvViewModel(
            repository = databaseReferenceEvRepository,
            equationRepository = equationRepository,
            platformDispatcher = platformDispatcher
        )
    }
    val equationViewModel = remember {
        EquationViewModel(
            equationRepository = equationRepository,
            biblioRefDao = appDatabase.biblioRefDao(),
            biblioRepository = biblioRefRepository,
            referenceRepository = databaseReferenceEvRepository
        )
    }
    val foodEditViewModel = remember(nav.selectedFoodUuid) {
        FoodEditViewModel(
            foodRepository = foodRepository,
            alimentUuid = nav.selectedFoodUuid,
            biblioRefRepository = biblioRefRepository
        )
    }
    DisposableEffect(foodEditViewModel) { onDispose { foodEditViewModel.clear() } }

    val newReferenceEvViewModel = remember {
        NewReferenceEvViewModel(
            repository = databaseReferenceEvRepository,
            equationRepository = equationRepository,
            biblioRefRepository = biblioRefRepository
        )
    }
    val backupRestoreViewModel = remember(backupService) {
        backupService?.let { BackupRestoreViewModel(it, platformDispatcher) }
    }
    val bulkReferenceEditorViewModel = remember {
        BulkReferenceEditorViewModel(
            referenceEvRepository = databaseReferenceEvRepository,
            biblioRefRepository = biblioRefRepository,
            platformDispatcher = platformDispatcher
        )
    }
    val importViewModel = remember {
        ImportViewModel(
            animalListViewModel = animalListViewModel,
            databaseReferenceEvRepository = databaseReferenceEvRepository,
            equationRepository = equationRepository,
            biblioRefRepository = biblioRefRepository
        )
    }
    val crossAnalysisViewModel = remember {
        CrossConsultationAnalysisViewModel(
            animalRepository = animalRepository,
            consultationRepository = consultationRepository,
            referenceEvRepository = databaseReferenceEvRepository,
            equationRepository = equationRepository
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            animalDetailViewModel.clear()
            foodListViewModel.clear()
            biblioRefViewModel.clear()
            referenceEvViewModel.clear()
            newReferenceEvViewModel.clear()
            importViewModel.clear()
        }
    }

    // Résultats d'importation (déclarés après les ViewModels)
    val animalImportResult = animalListViewModel.importResult.collectAsState().value
    val foodImportResult = settingsViewModel.importResult.collectAsState().value

    // Effets d'initialisation
    LaunchedEffect(consultationRepository) {
        try { ensureDefaultConsultationKeywords(consultationRepository) } catch (_: Exception) {}
    }
    LaunchedEffect(biblioRefRepository) {
        try { ensureDefaultBiblioRef(biblioRefRepository) } catch (_: Exception) {}
    }
    LaunchedEffect(Unit) {
        startupService.initialize()
        backupService = startupService.getBackupService()
    }
    LaunchedEffect(examSession) {
        animalListViewModel.setExamSession(examSession)
        if (examSession != null && nav.selectedCalculationTab == 3) nav.selectedCalculationTab = 0
    }
    LaunchedEffect(animalImportResult) { if (animalImportResult != null) showAnimalImportResult = true }
    LaunchedEffect(foodImportResult) { if (foodImportResult != null) showFoodImportResult = true }
    LaunchedEffect(nav.screen) {
        when (nav.screen) {
            Screen.List -> animalListViewModel.loadAnimals()
            Screen.FoodList -> foodListViewModel.loadFoods()
            Screen.EquationList -> equationViewModel.loadEquations()
            else -> {}
        }
    }
    LaunchedEffect(nav.selectedFoodUuid) {
        if (nav.selectedFoodUuid == null && nav.screen == Screen.FoodList) foodListViewModel.loadFoods()
    }

    val models = AppNavModels(
        animalListViewModel = animalListViewModel,
        animalDetailViewModel = animalDetailViewModel,
        createAnimalViewModel = createAnimalViewModel,
        settingsViewModel = settingsViewModel,
        foodListViewModel = foodListViewModel,
        foodEditViewModel = foodEditViewModel,
        biblioRefViewModel = biblioRefViewModel,
        referenceEvViewModel = referenceEvViewModel,
        newReferenceEvViewModel = newReferenceEvViewModel,
        equationViewModel = equationViewModel,
        importViewModel = importViewModel,
        examGradingViewModel = examGradingViewModel,
        crossAnalysisViewModel = crossAnalysisViewModel,
        bulkReferenceEditorViewModel = bulkReferenceEditorViewModel,
        backupRestoreViewModel = backupRestoreViewModel
    )
    val repos = AppNavRepositories(
        equationRepository = equationRepository,
        recipeRepository = recipeRepository,
        conseilRepository = conseilRepository,
        biblioRefRepository = biblioRefRepository,
        databaseReferenceEvRepository = databaseReferenceEvRepository,
        platformDispatcher = platformDispatcher,
        preferencesRepository = preferencesRepository
    )

    VetNutriTheme {
        Box(modifier = Modifier.fillMaxSize().imePadding()) {
            if (showStartupScreen) {
                StartupScreen(
                    referenceRepository = databaseReferenceEvRepository,
                    settingsViewModel = settingsViewModel,
                    onDatabaseReady = { showStartupScreen = false },
                    conseilRepository = settingsViewModel.conseilRepository,
                    onShowBackupDialog = { showStartupBackupDialog = true },
                    onStartExam = { session -> examSession = session }
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    AppNavHost(nav, models, repos, examSession)
                }
            }

            AppOverlayDialogs(
                showAnimalImportResult = showAnimalImportResult,
                animalImportResult = animalImportResult,
                onDismissAnimalImport = {
                    showAnimalImportResult = false
                    animalListViewModel.resetImportResult()
                },
                showFoodImportResult = showFoodImportResult,
                foodImportResult = foodImportResult,
                onDismissFoodImport = {
                    showFoodImportResult = false
                    settingsViewModel.resetImportResult()
                },
                showStartupBackupDialog = showStartupBackupDialog,
                backupRestoreViewModel = backupRestoreViewModel,
                onDismissStartupBackup = { showStartupBackupDialog = false }
            )
        }
    }
}
