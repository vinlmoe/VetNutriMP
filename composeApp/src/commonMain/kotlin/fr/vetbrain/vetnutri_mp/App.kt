package fr.vetbrain.vetnutri_mp

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.TopBar
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.DataBase.AppDatabase
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Localization.LocalizationManager
import fr.vetbrain.vetnutri_mp.Repository.AlimentRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseAnimalRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseConsultationRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseFoodRepository
import fr.vetbrain.vetnutri_mp.Theme.VetNutriTheme
import fr.vetbrain.vetnutri_mp.View.*
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalListViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.CreateAnimalViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.FoodEditViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.FoodListViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel
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

    // Création des ViewModels
    val animalListViewModel = remember { AnimalListViewModel(animalRepository) }
    val animalDetailViewModel = remember {
        AnimalDetailViewModel(consultationRepository, animalRepository)
    }
    val createAnimalViewModel = remember { CreateAnimalViewModel(animalRepository) }
    val settingsViewModel = remember { SettingsViewModel(animalRepository, foodRepository) }
    val foodListViewModel = remember { FoodListViewModel(foodRepository) }
    var selectedFoodUuid by remember { mutableStateOf<String?>(null) }

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
                            )

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
                                    onShowBiblioRefs = { currentScreen = Screen.BiblioRefList },
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
                    Screen.BiblioRefList -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            TopBar(
                                    title = "Gestion des références bibliographiques",
                                    onBackClick = { currentScreen = Screen.List },
                                    onSettingsClick = { showSettings = true }
                            )

                            BiblioRefView()
                        }
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
    object BiblioRefList : Screen()
}
