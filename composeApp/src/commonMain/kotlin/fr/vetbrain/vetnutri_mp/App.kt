package fr.vetbrain.vetnutri_mp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.TopBar
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.DataBase.AppDatabase
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Localization.LocalizationManager
import fr.vetbrain.vetnutri_mp.Localization.ResourceReader
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Repository.*
import fr.vetbrain.vetnutri_mp.Theme.VetNutriTheme
import fr.vetbrain.vetnutri_mp.Utils.PlatformDispatcher
import fr.vetbrain.vetnutri_mp.Utils.createPreferencesStorage
import fr.vetbrain.vetnutri_mp.View.*
import fr.vetbrain.vetnutri_mp.ViewModel.*
import kotlinx.coroutines.runBlocking

// Fonctions d'importation de fichiers - implémentées par plateforme spécifique
expect fun importAnimalsFromFile(viewModel: AnimalListViewModel)

expect fun importFoodsFromFile(viewModel: SettingsViewModel)

expect fun importNutritionalRequirementsFromFile(viewModel: ImportViewModel)

// Import (nouveau format API) depuis un fichier – implémenté par plateforme
expect fun importApiFromFile(viewModel: SettingsViewModel)

// Export/Import génériques pour le nouveau format API
expect fun exportJsonToFile(content: String, defaultFileName: String): Boolean

expect fun openJsonFileContent(): String?

@Composable
fun App(appDatabase: AppDatabase) {
    // Initialisation de la localisation
    LocalizationManager.initialize()

    // Création des repositories avec la base de données
    val animalRepository = remember {
        DatabaseAnimalRepository(
                appDatabase.animalDao(),
                appDatabase.foodDao(),
                appDatabase.nutrientValueDao()
        )
    }

    // Création du repository pour les aliments
    val foodRepository = remember {
        DatabaseFoodRepository(appDatabase.foodDao(), appDatabase.nutrientValueDao())
    }

    // Initialiser le repository statique AlimentRepository avec le DatabaseFoodRepository
    AlimentRepository.initializeDatabaseFoodRepository(foodRepository)

    val consultationRepository = remember {
        DatabaseConsultationRepository(appDatabase.consultationDao(), foodRepository)
    }

    // Repository pour les recettes
    val recipeRepository = remember {
        RecipeRepository(appDatabase.recipeDao(), appDatabase.foodDao())
    }

    // Création du repository pour les références bibliographiques - version database directe
    val biblioRefRepository = remember {
        val repo = DatabaseBiblioRefRepository(appDatabase.biblioRefDao())

        // S'assurer que la référence par défaut existe dès la création du repository
        runBlocking {
            try {
                val defaultUuid = "default-biblio"
                val existingRef = repo.getBiblioRefById(defaultUuid)

                if (existingRef == null) {
                    val defaultRef =
                            fr.vetbrain.vetnutri_mp.Data.BiblioRef(
                                    uuid = defaultUuid,
                                    firstAuthor = "Système VetNutri",
                                    year = 2024,
                                    completeRef = "Référence par défaut générée automatiquement",
                                    comments =
                                            "Créée automatiquement pour éviter les erreurs de clé étrangère",
                                    consistent = 1
                            )
                    repo.insertBiblioRef(defaultRef)
                } else {}
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        repo
    }

    // Création du repository pour les équations avec base de données
    val equationRepository = remember {
        DatabaseEquationRepository(appDatabase.equationDao(), appDatabase.biblioRefDao())
    }

    // création des ViewModels (existant)...
    // ViewModel et état pour les équations et références (déclarée avant AnimalDetailViewModel)
    val platformDispatcher = remember { PlatformDispatcher() }
    val databaseReferenceEvRepository = remember {
        DatabaseReferenceEvRepository(
                appDatabase.referenceEvDao(),
                appDatabase.equationDao(),
                appDatabase.biblioRefDao()
        )
    }

    // Repository des préférences (global pour tous les ViewModels)
    val preferencesRepository = remember {
        fr.vetbrain.vetnutri_mp.Repository.PreferencesRepository(createPreferencesStorage())
    }

    // Création des ViewModels
    val animalListViewModel = remember { AnimalListViewModel(animalRepository) }

    val animalDetailViewModel = remember {
        AnimalDetailViewModel(
                consultationRepository,
                animalRepository,
                databaseReferenceEvRepository,
                preferencesRepository
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
                consultationRepository = consultationRepository
        )
    }
    val foodListViewModel = remember { FoodListViewModel(foodRepository) }
    var selectedFoodUuid by remember { mutableStateOf<String?>(null) }

    // ViewModel et état pour les références bibliographiques
    val biblioRefViewModel = remember { BiblioRefViewModel(biblioRefRepository) }
    var selectedBiblioRefId by remember { mutableStateOf<String?>(null) }

    val referenceEvViewModel = remember {
        ReferenceEvViewModel(databaseReferenceEvRepository, platformDispatcher)
    }
    var selectedReferenceEvId by remember { mutableStateOf<String?>(null) }

    // État pour gérer l'onglet sélectionné dans CalculationTabsView
    var selectedCalculationTab by remember { mutableStateOf(0) }

    val equationViewModel = remember {
        EquationViewModel(
                equationRepository = equationRepository,
                biblioRefDao = appDatabase.biblioRefDao(),
                biblioRepository = biblioRefRepository,
                referenceRepository = databaseReferenceEvRepository
        )
    }
    var selectedEquationId by remember { mutableStateOf<String?>(null) }

    // Création des view models en fonction des besoins de la navigation
    val foodEditViewModel by
            remember(selectedFoodUuid) {
                mutableStateOf(
                        FoodEditViewModel(
                                alimentRepository = AlimentRepository.getInstance(foodRepository),
                                alimentUuid = selectedFoodUuid
                        )
                )
            }

    val newReferenceEvViewModel = remember {
        NewReferenceEvViewModel(
                repository = databaseReferenceEvRepository,
                equationRepository = equationRepository,
                biblioRefRepository = biblioRefRepository
        )
    }

    var currentScreen by remember { mutableStateOf<Screen>(Screen.List) }
    var selectedAnimal by remember { mutableStateOf<AnimalEv?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var selectedSpecies by remember { mutableStateOf<fr.vetbrain.vetnutri_mp.Enumer.Espece?>(null) }

    // Observer le résultat de l'importation
    val importResult = animalListViewModel.importResult.collectAsState().value
    var showImportResult by remember { mutableStateOf(false) }

    // Observer le résultat de l'importation des aliments
    val foodImportResult = settingsViewModel.importResult.collectAsState().value
    var showFoodImportResult by remember { mutableStateOf(false) }

    // ViewModel pour l'importation avec tous les repositories nécessaires
    val importViewModel = remember {
        ImportViewModel(
                animalRepository = animalRepository,
                databaseReferenceEvRepository = databaseReferenceEvRepository,
                equationRepository = equationRepository,
                biblioRefRepository = biblioRefRepository
        )
    }

    LaunchedEffect(importResult) {
        if (importResult != null) {
            showImportResult = true
        }
    }

    LaunchedEffect(foodImportResult) {
        if (foodImportResult != null) {
            showFoodImportResult = true
        }
    }

    // Effet pour recharger la liste des animaux lorsque l'utilisateur revient à l'écran de liste
    LaunchedEffect(currentScreen) {
        if (currentScreen == Screen.List) {
            animalListViewModel.loadAnimals()
        } else if (currentScreen == Screen.FoodList) {
            // Toujours recharger explicitement la liste des aliments quand on affiche la liste
            foodListViewModel.loadFoods()
        } else if (currentScreen == Screen.EquationList) {
            // Recharger la liste des équations
            equationViewModel.loadEquations()
        }
    }

    // Ajouter un effet pour recharger explicitement la liste des aliments après modification
    LaunchedEffect(selectedFoodUuid) {
        // Si on revient de l'écran d'édition (selectedFoodUuid devient null après avoir été
        // non-null)
        if (selectedFoodUuid == null && currentScreen == Screen.FoodList) {
            // Recharger explicitement la liste des aliments
            foodListViewModel.loadFoods()
        }
    }

    // Fonction locale pour importer les animaux
    val handleImportAnimals: () -> Unit = {
        // Utiliser la méthode du ViewModel pour éviter l'ambiguïté
        animalListViewModel.importAnimalsFromFileUI()
    }

    // Fonction locale pour importer les aliments
    val handleImportFoods: () -> Unit = {
        // Utiliser la méthode du ViewModel pour éviter l'ambiguïté
        settingsViewModel.importFoodsFromFileUI()
    }

    // Import automatique des aliments ET des références AVANT le thème
    LaunchedEffect(Unit) {
        // --- ALIMENTS ET REFERENCES ---
        val currentFoodCount = foodRepository.getAllFoods().size
        val currentReferenceCount = databaseReferenceEvRepository.getAllReferenceEv().size

        if (currentFoodCount == 0 || currentReferenceCount == 0) {
            try {
                println("IMPORT AUTO: Début de l'import automatique...")
                println("IMPORT AUTO: Lecture du fichier de ressources...")

                // Essayer d'abord le chemin iOS (direct), puis le chemin Android/Desktop (data/)
                val json =
                        try {
                            println(
                                    "IMPORT AUTO: Tentative 1 - Chemin iOS: vetnutri_export_init.json"
                            )
                            val result = ResourceReader().readResource("vetnutri_export_init.json")
                            println("IMPORT AUTO: ✅ Succès avec le chemin iOS!")
                            result
                        } catch (e: Exception) {
                            println("IMPORT AUTO: ❌ Échec chemin iOS: ${e.message}")
                            try {
                                println(
                                        "IMPORT AUTO: Tentative 2 - Chemin Android/Desktop: data/vetnutri_export_init.json"
                                )
                                val result =
                                        ResourceReader()
                                                .readResource("data/vetnutri_export_init.json")
                                println("IMPORT AUTO: ✅ Succès avec le chemin Android/Desktop!")
                                result
                            } catch (e2: Exception) {
                                println(
                                        "IMPORT AUTO: ❌ Échec chemin Android/Desktop: ${e2.message}"
                                )
                                throw IllegalStateException(
                                        "Fichier vetnutri_export_init.json introuvable sur iOS (vetnutri_export_init.json) et Android/Desktop (data/vetnutri_export_init.json). Erreurs: iOS=${e.message}, Android/Desktop=${e2.message}"
                                )
                            }
                        }
                if (json.isNotEmpty()) {
                    println("IMPORT AUTO: Fichier JSON lu avec succès (${json.length} caractères)")

                    val exportImportRepo =
                            ExportImportRepository(
                                    animalRepository = animalRepository,
                                    foodRepository = foodRepository,
                                    equationRepository = equationRepository,
                                    referenceRepository = databaseReferenceEvRepository,
                                    biblioRepository = biblioRefRepository,
                                    consultationRepository = consultationRepository,
                                    recipeRepository = recipeRepository
                            )

                    println("IMPORT AUTO: ExportImportRepository créé, début de l'import...")

                    val importResult =
                            exportImportRepo.importAll(
                                    apiJson = json,
                                    listener =
                                            ExportImportRepository.ImportProgressListener(
                                                    onProgress = { progress ->
                                                        println(
                                                                "IMPORT AUTO: Progression: ${(progress * 100).toInt()}%"
                                                        )
                                                    },
                                                    onLog = { msg -> println("IMPORT AUTO: $msg") }
                                            )
                            )

                    println("IMPORT AUTO: Import terminé avec succès!")
                    println(
                            "IMPORT AUTO: Résultats - Animaux: ${importResult.animals}, Aliments: ${importResult.foods}, Références: ${importResult.references}"
                    )

                    // 🔧 CORRECTION : Notifier les ViewModels que les données ont été mises à jour
                    println("IMPORT AUTO: Notification des ViewModels...")

                    // Invalider le cache du repository des aliments et forcer le rechargement
                    if (foodRepository is DatabaseFoodRepository) {
                        foodRepository.forceRefresh()
                    }

                    // Recharger les données dans les ViewModels
                    animalListViewModel.loadAnimals()
                    foodListViewModel.forceRefresh()
                    equationViewModel.loadEquations()
                    referenceEvViewModel.loadAllReferences()
                    biblioRefViewModel.refreshBiblioRefs()

                    println("IMPORT AUTO: ViewModels notifiés et données rechargées")
                } else {
                    println("IMPORT AUTO: ERREUR - Le fichier JSON est vide!")
                }
            } catch (e: Exception) {
                println("IMPORT AUTO: ERREUR CRITIQUE lors de l'import automatique!")
                println("IMPORT AUTO: Type d'erreur: ${e::class.simpleName}")
                println("IMPORT AUTO: Message: ${e.message}")
                println("IMPORT AUTO: Stack trace: ${e.stackTraceToString()}")

                // Afficher des informations de débogage supplémentaires
                try {
                    println("IMPORT AUTO: Vérification des ressources disponibles...")
                    val resourceReader = ResourceReader()
                    println("IMPORT AUTO: ResourceReader créé avec succès")
                } catch (resourceError: Exception) {
                    println(
                            "IMPORT AUTO: ERREUR lors de la création du ResourceReader: ${resourceError.message}"
                    )
                    println(
                            "IMPORT AUTO: Stack trace ResourceReader: ${resourceError.stackTraceToString()}"
                    )
                }
            }
        } else {
            println(
                    "IMPORT AUTO: Base déjà peuplée (Aliments: $currentFoodCount, Références: $currentReferenceCount)"
            )
        }
    }

    VetNutriTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                when (currentScreen) {
                    Screen.List -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            TopBar(
                                    title = "Liste des animaux",
                                    onSettingsClick = { currentScreen = Screen.Settings }
                            ) {
                                // Boutons supprimés
                            }

                            // Ajout d'un LaunchedEffect pour recharger la liste lorsque l'écran
                            // devient visible
                            LaunchedEffect(currentScreen) {
                                if (currentScreen == Screen.List) {
                                    animalListViewModel.loadAnimals()
                                }
                            }

                            AnimalListView(
                                    viewModel = animalListViewModel,
                                    onAddAnimal = {
                                        isEditing = false
                                        selectedAnimal = null
                                        createAnimalViewModel.resetAnimal()
                                        currentScreen = Screen.Create
                                    },
                                    onSelectAnimal = { animal: AnimalEv ->
                                        selectedAnimal = animal
                                        animalDetailViewModel.setAnimal(animal)
                                        currentScreen = Screen.Detail
                                    },
                                    onEditAnimal = { animal: AnimalEv ->
                                        selectedAnimal = animal
                                        createAnimalViewModel.updateAnimal(animal)
                                        isEditing = true
                                        currentScreen = Screen.Create
                                    },
                                    onShowFoodList = { currentScreen = Screen.FoodList },
                                    onShowCalculationTabs = {
                                        currentScreen = Screen.CalculationTabs
                                    },
                                    modifier = Modifier.fillMaxWidth().weight(1f)
                            )
                        }
                    }
                    Screen.Create -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            TopBar(
                                    title =
                                            if (isEditing) "Modifier un animal"
                                            else "Ajouter un animal",
                                    onBackClick = {
                                        isEditing = false
                                        selectedAnimal = null
                                        currentScreen = Screen.List
                                    },
                                    onSettingsClick = { currentScreen = Screen.Settings }
                            )
                            CreateAnimalView(
                                    viewModel = createAnimalViewModel,
                                    onNavigateBack = {
                                        isEditing = false
                                        selectedAnimal = null
                                        currentScreen = Screen.List
                                    },
                                    isEditing = isEditing,
                                    modifier = Modifier.fillMaxWidth().weight(1f)
                            )
                        }
                    }
                    Screen.Detail -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            AnimalDetailView(
                                    viewModel = animalDetailViewModel,
                                    settingsViewModel = settingsViewModel,
                                    onNavigateBack = { currentScreen = Screen.List },
                                    onOpenSettings = { currentScreen = Screen.Settings },
                                    modifier = Modifier.fillMaxWidth().weight(1f),
                                    equationRepository = equationRepository,
                                    recipeRepository = recipeRepository,
                                    foodRepository = foodRepository
                            )
                        }
                    }
                    Screen.FoodList -> {
                        FoodListView(
                                viewModel = foodListViewModel,
                                onNavigateBack = { currentScreen = Screen.List },
                                onOpenSettings = { currentScreen = Screen.Settings },
                                onEditFood = { foodUuid ->
                                    selectedFoodUuid = foodUuid
                                    currentScreen = Screen.FoodEdit
                                },
                                onCreateFood = {
                                    selectedFoodUuid = null
                                    currentScreen = Screen.FoodEdit
                                },
                                modifier = Modifier.fillMaxSize()
                        )
                    }
                    Screen.FoodEdit -> {
                        FoodEditView(
                                viewModel = foodEditViewModel,
                                onNavigateBack = {
                                    selectedFoodUuid = null
                                    currentScreen = Screen.FoodList
                                },
                                onNavigateToSettings = { currentScreen = Screen.Settings },
                                modifier = Modifier.fillMaxSize()
                        )
                    }
                    Screen.CalculationTabs -> {
                        CalculationTabsView(
                                equationViewModel = equationViewModel,
                                biblioRefViewModel = biblioRefViewModel,
                                referenceEvViewModel = referenceEvViewModel,
                                onNavigateBack = { currentScreen = Screen.List },
                                onEditReferenceEv = { referenceEvId ->
                                    selectedReferenceEvId = referenceEvId
                                    currentScreen = Screen.NewReferenceEvEdit
                                },
                                onCreateReferenceEv = {
                                    selectedReferenceEvId = null
                                    currentScreen = Screen.NewReferenceEvEdit
                                },
                                selectedTab = selectedCalculationTab,
                                onTabChanged = { selectedCalculationTab = it },
                                modifier = Modifier.fillMaxSize(),
                                biblioRefRepository = biblioRefRepository,
                                equationRepository = equationRepository,
                                referenceEvRepository = databaseReferenceEvRepository,
                                platformDispatcher = platformDispatcher
                        )
                    }
                    Screen.BiblioRefList -> {
                        BiblioRefListView(
                                viewModel = biblioRefViewModel,
                                onEditBiblioRef = { biblioRefId ->
                                    selectedBiblioRefId = biblioRefId
                                    currentScreen = Screen.BiblioRefEdit
                                },
                                onCreateBiblioRef = {
                                    selectedBiblioRefId = null
                                    currentScreen = Screen.BiblioRefEdit
                                },
                                modifier = Modifier.fillMaxSize()
                        )
                    }
                    Screen.BiblioRefEdit -> {
                        BiblioRefEditView(
                                viewModel = biblioRefViewModel,
                                biblioRefId = selectedBiblioRefId,
                                onNavigateBack = {
                                    selectedBiblioRefId = null
                                    currentScreen = Screen.BiblioRefList
                                },
                                modifier = Modifier.fillMaxSize()
                        )
                    }
                    Screen.EquationList -> {
                        EquationListView(
                                viewModel = equationViewModel,
                                onEditEquation = { equationId ->
                                    selectedEquationId = equationId
                                    equationViewModel.clearOperationMessage()
                                    currentScreen = Screen.EquationEdit
                                },
                                onCreateEquation = {
                                    selectedEquationId = null
                                    equationViewModel.clearOperationMessage()
                                    currentScreen = Screen.EquationEdit
                                },
                                modifier = Modifier.fillMaxSize()
                        )
                    }
                    Screen.EquationEdit -> {
                        EquationEditView(
                                viewModel = equationViewModel,
                                equationId = selectedEquationId,
                                onNavigateBack = {
                                    selectedEquationId = null
                                    currentScreen = Screen.EquationList
                                },
                                modifier = Modifier.fillMaxSize()
                        )
                    }
                    Screen.ReferenceEvList -> {
                        NutrientRequirementView(
                                viewModel = referenceEvViewModel,
                                onEditReference = { referenceEvId ->
                                    selectedReferenceEvId = referenceEvId
                                    currentScreen = Screen.NewReferenceEvEdit
                                },
                                onCreateReference = {
                                    selectedReferenceEvId = null
                                    currentScreen = Screen.NewReferenceEvEdit
                                },
                                onEditNutrients = { referenceEvId ->
                                    selectedReferenceEvId = referenceEvId
                                    currentScreen = Screen.ReferenceEvNutrient
                                },
                                modifier = Modifier.fillMaxSize()
                        )
                    }
                    Screen.ReferenceEvNutrient -> {
                        ReferenceEvNutrientView(
                                referenceEvViewModel = referenceEvViewModel,
                                biblioRefRepository = biblioRefRepository,
                                referenceEvRepository = databaseReferenceEvRepository,
                                platformDispatcher = platformDispatcher,
                                referenceEvId = selectedReferenceEvId ?: "",
                                onNavigateBack = { currentScreen = Screen.EquationList },
                                modifier = Modifier.fillMaxSize()
                        )
                    }
                    Screen.ReferenceEvTabs -> {
                        NewReferenceEvEditView(
                                viewModel = newReferenceEvViewModel,
                                referenceId = selectedReferenceEvId,
                                onNavigateBack = { currentScreen = Screen.ReferenceEvList },
                                modifier = Modifier.fillMaxSize()
                        )
                    }
                    Screen.TestYellowBox -> {
                        // Interface de test ultra-visible
                        Box(modifier = Modifier.fillMaxSize().background(Color.Yellow)) {
                            Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                        "TEST BOÎTE JAUNE\nCeci est un test de visibilité",
                                        style = MaterialTheme.typography.h4,
                                        color = Color.Red,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(24.dp)
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                        onClick = { currentScreen = Screen.List },
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor = Color.Blue
                                                )
                                ) { Text("Retour à la liste", color = Color.White) }
                            }
                        }
                    }
                    Screen.NewReferenceEvEdit -> {
                        NewReferenceEvEditView(
                                viewModel = newReferenceEvViewModel,
                                referenceId = selectedReferenceEvId,
                                onNavigateBack = {
                                    selectedReferenceEvId = null
                                    selectedCalculationTab = 2 // Sélectionner l'onglet "Besoins"
                                    currentScreen = Screen.CalculationTabs
                                },
                                modifier = Modifier.fillMaxSize()
                        )
                    }
                    Screen.Settings -> {
                        Scaffold(
                                topBar = {
                                    TopBarSimple(
                                            title = "Paramètres",
                                            onNavigateBack = { currentScreen = Screen.List }
                                    )
                                }
                        ) { paddingValues ->
                            SettingsView(
                                    viewModel = settingsViewModel,
                                    importViewModel = importViewModel,
                                    onImportAnimals = {
                                        // Lancer l'importation
                                        importViewModel.importAnimalsFromFileUI()
                                    },
                                    onBack = { currentScreen = Screen.List },
                                    onAnimalListRefresh = {
                                        // Rafraîchir la liste des animaux
                                        animalListViewModel.loadAnimals()
                                    },
                                    onFoodListRefresh = {
                                        // Rafraîchir la liste des aliments
                                        // TODO: implémenter le rafraîchissement de la liste des
                                        // aliments
                                    },
                                    modifier = Modifier.padding(paddingValues),
                                    onSpeciesClick = { species ->
                                        selectedSpecies = species
                                        currentScreen = Screen.SpeciesPreferences
                                    }
                            )
                        }
                    }
                    Screen.SpeciesPreferences -> {
                        selectedSpecies?.let { species ->
                            Scaffold(
                                    topBar = {
                                        TopBarSimple(
                                                title =
                                                        "${"preferences.title".translate()} ${species.translateEnum()}",
                                                onNavigateBack = { currentScreen = Screen.Settings }
                                        )
                                    }
                            ) { paddingValues ->
                                fr.vetbrain.vetnutri_mp.View.SpeciesPreferencesView(
                                        species = species,
                                        preferencesRepository = preferencesRepository,
                                        equationRepository = equationRepository,
                                        modifier = Modifier.padding(paddingValues)
                                )
                            }
                        }
                                ?: run {
                                    // Fallback si aucune espèce n'est sélectionnée
                                    currentScreen = Screen.Settings
                                }
                    }
                }
            }

            // Afficher le résultat de l'importation des animaux
            if (showImportResult && importResult != null) {
                AlertDialog(
                        onDismissRequest = {
                            showImportResult = false
                            animalListViewModel.resetImportResult()
                        },
                        title = { Text("dialog.resultImportAnimals.title".translate()) },
                        text = {
                            when (importResult) {
                                is AnimalListViewModel.ImportResult.Success -> {
                                    Text(
                                            "${importResult.count} " +
                                                    "dialog.resultImportAnimals.success".translate()
                                    )
                                }
                                is AnimalListViewModel.ImportResult.Error -> {
                                    Text(
                                            "dialog.resultImportAnimals.error".translate() +
                                                    " ${importResult.message}"
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                    onClick = {
                                        showImportResult = false
                                        animalListViewModel.resetImportResult()
                                    }
                            ) { Text("OK") }
                        }
                )
            }

            // Afficher le résultat de l'importation des aliments
            if (showFoodImportResult && foodImportResult != null) {
                AlertDialog(
                        onDismissRequest = {
                            showFoodImportResult = false
                            settingsViewModel.resetImportResult()
                        },
                        title = { Text("Résultat de l'importation des aliments") },
                        text = {
                            when (foodImportResult) {
                                is SettingsViewModel.ImportResult.Success -> {
                                    Column {
                                        Text(
                                                "${foodImportResult.count} aliments ont été importés avec succès.",
                                                style = MaterialTheme.typography.subtitle1
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Afficher les statistiques détaillées
                                        Text(
                                                "Détails de l'importation:",
                                                style = MaterialTheme.typography.subtitle2
                                        )
                                        Text(
                                                "• ${foodImportResult.importedCount} nouveaux aliments"
                                        )

                                        if (foodImportResult.updatedCount > 0) {
                                            Text(
                                                    "• ${foodImportResult.updatedCount} aliments mis à jour"
                                            )
                                        }

                                        if (foodImportResult.deletedCount > 0) {
                                            Text(
                                                    "• ${foodImportResult.deletedCount} aliments supprimés"
                                            )
                                        }

                                        if (foodImportResult.errorCount > 0) {
                                            Text(
                                                    "• ${foodImportResult.errorCount} erreurs rencontrées",
                                                    color = MaterialTheme.colors.error
                                            )
                                        }

                                        if (foodImportResult.nonResolvedNutrients > 0) {
                                            Text(
                                                    "• ${foodImportResult.nonResolvedNutrients} nutriments non résolus",
                                                    color =
                                                            MaterialTheme.colors.error.copy(
                                                                    alpha = 0.7f
                                                            )
                                            )
                                        }
                                    }
                                }
                                is SettingsViewModel.ImportResult.Error -> {
                                    Text(
                                            "Erreur lors de l'importation : ${foodImportResult.message}"
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                    onClick = {
                                        showFoodImportResult = false
                                        settingsViewModel.resetImportResult()
                                    }
                            ) { Text("OK") }
                        }
                )
            }
        }
    }
}

private sealed class Screen {
    object List : Screen()
    object Create : Screen()
    object Detail : Screen()
    object FoodList : Screen()
    object FoodEdit : Screen()
    object CalculationTabs : Screen()
    object BiblioRefList : Screen()
    object BiblioRefEdit : Screen()
    object EquationList : Screen()
    object EquationEdit : Screen()
    object ReferenceEvList : Screen()
    object ReferenceEvNutrient : Screen()
    object ReferenceEvTabs : Screen()
    object TestYellowBox : Screen()
    object NewReferenceEvEdit : Screen()
    object Settings : Screen()
    object SpeciesPreferences : Screen()
}
