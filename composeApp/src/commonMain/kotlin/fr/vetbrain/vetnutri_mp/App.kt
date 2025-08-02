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
                .apply {
                    // Ajout de quelques animaux de test uniquement si la base est vide
                    runBlocking {
                        if (getAllAnimals().isEmpty()) {
                            saveAnimal(AnimalEv.createTestAnimal())
                            saveAnimal(
                                    AnimalEv.createTestAnimal()
                                            .copy(
                                                    nom = "Felix",
                                                    specieId = Espece.CHAT.name,
                                            )
                            )
                        }
                    }
                }
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
                } else {
                }
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

    // Création des ViewModels
    val animalListViewModel = remember { AnimalListViewModel(animalRepository) }

    val animalDetailViewModel = remember {
        AnimalDetailViewModel(
                consultationRepository,
                animalRepository,
                databaseReferenceEvRepository
        )
    }
    val createAnimalViewModel = remember { CreateAnimalViewModel(animalRepository) }
    val settingsViewModel = remember {
        SettingsViewModel(
                animalRepository,
                foodRepository,
                databaseReferenceEvRepository,
                equationRepository,
                biblioRefRepository
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
    runBlocking {
        // --- ALIMENTS ---
        val currentFoodCount = foodRepository.getAllFoods().size
        if (currentFoodCount == 0) {
            try {
                val json = ResourceReader().readResource("data/vetfood.json")
                if (json.isNotEmpty()) {
                    val result = settingsViewModel.importFoodsFromJson(json)
                } else {
                }
            } catch (e: Exception) {
            }
        }

        // --- REFERENCES ---
        val currentReferenceCount = databaseReferenceEvRepository.getAllReferenceEv().size
        if (currentReferenceCount == 0) {
            try {
                val jsonRef = ResourceReader().readResource("data/references.json")
                if (jsonRef.isNotEmpty()) {
                    val result = importViewModel.importNutritionalRequirementsFromJson(jsonRef)
                } else {
                }
            } catch (e: Exception) {
            }
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
                                // Ajouter un bouton de test
                                Button(
                                        onClick = { currentScreen = Screen.TestYellowBox },
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor = Color.Yellow
                                                )
                                ) { Text("Test", color = Color.Red) }
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
                                    onImportAnimals = handleImportAnimals,
                                    onImportFoods = handleImportFoods,
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
                                    modifier = Modifier.fillMaxWidth().weight(1f)
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
                            val preferencesRepository = remember {
                                fr.vetbrain.vetnutri_mp.Repository.PreferencesRepository(
                                        createPreferencesStorage()
                                )
                            }

                            Scaffold(
                                    topBar = {
                                        TopBarSimple(
                                                title = "Préférences ${species.label}",
                                                onNavigateBack = { currentScreen = Screen.Settings }
                                        )
                                    }
                            ) { paddingValues ->
                                fr.vetbrain.vetnutri_mp.View.SpeciesPreferencesView(
                                        species = species,
                                        preferencesRepository = preferencesRepository,
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
                        title = { Text("Résultat de l'importation") },
                        text = {
                            when (importResult) {
                                is AnimalListViewModel.ImportResult.Success -> {
                                    Text(
                                            "${importResult.count} animaux ont été importés avec succès."
                                    )
                                }
                                is AnimalListViewModel.ImportResult.Error -> {
                                    Text("Erreur lors de l'importation : ${importResult.message}")
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
