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
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.DataBase.AppDatabase
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Localization.LocalizationManager
import fr.vetbrain.vetnutri_mp.Repository.*
import fr.vetbrain.vetnutri_mp.Theme.VetNutriTheme
import fr.vetbrain.vetnutri_mp.Utils.PlatformDispatcher
import fr.vetbrain.vetnutri_mp.View.*
import fr.vetbrain.vetnutri_mp.ViewModel.*
import kotlinx.coroutines.runBlocking

// Fonctions d'importation de fichiers - implémentées par plateforme spécifique
expect fun importAnimalsFromFile(viewModel: AnimalListViewModel)

expect fun importFoodsFromFile(viewModel: SettingsViewModel)

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
    val biblioRefRepository = remember { DatabaseBiblioRefRepository(appDatabase.biblioRefDao()) }

    // Création du repository pour les équations avec base de données
    val equationRepository = remember {
        DatabaseEquationRepository(appDatabase.equationDao(), appDatabase.biblioRefDao())
    }

    // Création des ViewModels
    val animalListViewModel = remember { AnimalListViewModel(animalRepository) }
    val animalDetailViewModel = remember {
        AnimalDetailViewModel(consultationRepository, animalRepository)
    }
    val createAnimalViewModel = remember { CreateAnimalViewModel(animalRepository) }
    val settingsViewModel = remember { SettingsViewModel(animalRepository, foodRepository) }
    val foodListViewModel = remember { FoodListViewModel(foodRepository) }
    var selectedFoodUuid by remember { mutableStateOf<String?>(null) }

    // ViewModel et état pour les références bibliographiques
    val biblioRefViewModel = remember { BiblioRefViewModel(biblioRefRepository) }
    var selectedBiblioRefId by remember { mutableStateOf<String?>(null) }

    // ViewModel et état pour les équations
    val platformDispatcher = remember { PlatformDispatcher() }
    val databaseReferenceEvRepository = remember {
        DatabaseReferenceEvRepository(
                appDatabase.referenceEvDao(),
                appDatabase.equationDao(),
                appDatabase.biblioRefDao()
        )
    }
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
                                alimentRepository = AlimentRepository(foodRepository),
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
    var showSettings by remember { mutableStateOf(false) }

    // Observer le résultat de l'importation
    val importResult = animalListViewModel.importResult.collectAsState().value
    var showImportResult by remember { mutableStateOf(false) }

    // Observer le résultat de l'importation des aliments
    val foodImportResult = settingsViewModel.importResult.collectAsState().value
    var showFoodImportResult by remember { mutableStateOf(false) }

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
            println("DEBUG App: Rechargement des aliments lors du changement d'écran vers FoodList")
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
            println(
                    "DEBUG App: Rechargement des aliments après édition (selectedFoodUuid devient null)"
            )
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

    VetNutriTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                when (currentScreen) {
                    Screen.List -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            TopBar(
                                    title = "Liste des animaux",
                                    onSettingsClick = { showSettings = true }
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
                                    onSettingsClick = { showSettings = true }
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
                                    onOpenSettings = { showSettings = true },
                                    modifier = Modifier.fillMaxWidth().weight(1f)
                            )
                        }
                    }
                    Screen.FoodList -> {
                        FoodListView(
                                viewModel = foodListViewModel,
                                onNavigateBack = { currentScreen = Screen.List },
                                onOpenSettings = { showSettings = true },
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
                                onNavigateToSettings = { showSettings = true },
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
                                modifier = Modifier.fillMaxSize()
                        )
                    }
                    Screen.BiblioRefList -> {
                        BiblioRefListView(
                                viewModel = biblioRefViewModel,
                                onNavigateBack = { currentScreen = Screen.List },
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
                                onNavigateBack = { currentScreen = Screen.List },
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
                                onNavigateBack = { currentScreen = Screen.List },
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
                        println(
                                "DEBUG: App - Affichage de la vue ReferenceEvTabs avec menu latéral"
                        )
                        ReferenceEvSideMenuView(
                                referenceEvViewModel = referenceEvViewModel,
                                equationViewModel = equationViewModel,
                                biblioRefRepository = biblioRefRepository,
                                equationRepository = equationRepository,
                                referenceEvRepository = databaseReferenceEvRepository,
                                platformDispatcher = platformDispatcher,
                                referenceEvId = selectedReferenceEvId ?: "",
                                onNavigateBack = { currentScreen = Screen.ReferenceEvList },
                                onEditEquation = { equationId ->
                                    currentScreen = Screen.EquationEdit
                                    selectedEquationId = equationId
                                },
                                onCreateEquation = {
                                    currentScreen = Screen.EquationEdit
                                    selectedEquationId = null
                                },
                                useSidebar = true // Forcer l'utilisation du menu latéral
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
                }
            }

            if (showSettings) {
                // Afficher SettingsView dans une boîte de dialogue modale
                AlertDialog(
                        onDismissRequest = { showSettings = false },
                        title = { Text("Paramètres") },
                        text = {
                            // Nous utilisons une Box pour contenir SettingsView avec une taille
                            // maximale
                            Box(modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp)) {
                                // Utiliser le composant SettingsView à l'intérieur de la boîte de
                                // dialogue
                                SettingsView(
                                        viewModel = settingsViewModel,
                                        onImportAnimals = handleImportAnimals,
                                        onBack = { showSettings = false },
                                        onAnimalListRefresh = { animalListViewModel.loadAnimals() },
                                        onFoodListRefresh = { foodListViewModel.loadFoods() }
                                )
                            }
                        },
                        confirmButton = {
                            Button(onClick = { showSettings = false }) { Text("Fermer") }
                        },
                        // Définir une largeur maximale pour la boîte de dialogue
                        modifier = Modifier.widthIn(max = 800.dp)
                )
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
}
