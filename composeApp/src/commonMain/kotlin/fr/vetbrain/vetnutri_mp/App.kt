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
import kotlinx.coroutines.launch
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
        DatabaseAnimalRepository(appDatabase.animalDao(), appDatabase.foodDao()).apply {
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
            foodListViewModel.loadFoods()
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
                /* Screen.IMPORT_EXPORT n'est pas correctement implémenté - à revoir plus tard
                Screen.IMPORT_EXPORT -> {
                    TopBar(
                            title = "Importation / Exportation",
                            onBackClick = { navigateTo(NavigationItem.HOME) },
                            onSettingsClick = { showSettings = true }
                    )
                    Button(
                            onClick = {
                                // Utiliser la méthode du ViewModel pour éviter l'ambiguïté
                                animalListViewModel.importAnimalsFromFileUI()
                            },
                            modifier = Modifier.padding(16.dp).align(Alignment.Center)
                    ) { Text("Importer des animaux") }
                    Button(
                            onClick = {
                                // Utiliser la méthode du ViewModel pour éviter l'ambiguïté
                                settingsViewModel.importFoodsFromFileUI()
                            },
                            modifier = Modifier.padding(16.dp).align(Alignment.Center)
                    ) { Text("Importer des aliments") }
                }
                */
                }
            }

            if (showSettings) {
                // Simple AlertDialog pour remplacer SettingsDialog
                val uiScale by settingsViewModel.uiScale.collectAsState()
                var showConfirmClearFoods by remember { mutableStateOf(false) }
                var showConfirmClearAnimals by remember { mutableStateOf(false) }
                var isProcessing by remember { mutableStateOf(false) }
                var resultMessage by remember { mutableStateOf<String?>(null) }
                val coroutineScope = rememberCoroutineScope()

                AlertDialog(
                        onDismissRequest = { showSettings = false },
                        title = { Text("Paramètres") },
                        text = {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Section taille d'interface
                                Text(
                                        "Taille de l'interface: ${(uiScale * 100).toInt()}%",
                                        style = MaterialTheme.typography.subtitle1
                                )

                                Row(modifier = Modifier.padding(vertical = 8.dp)) {
                                    Button(
                                            onClick = { settingsViewModel.decrementUiScale() },
                                            enabled = uiScale > 0.5f
                                    ) { Text("-") }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                            onClick = { settingsViewModel.incrementUiScale() },
                                            enabled = uiScale < 2f
                                    ) { Text("+") }
                                }

                                Divider(modifier = Modifier.padding(vertical = 8.dp))

                                // Section importation de données
                                Text(
                                        "Importation de données",
                                        style = MaterialTheme.typography.subtitle1
                                )

                                Row(modifier = Modifier.padding(vertical = 8.dp)) {
                                    Button(
                                            onClick = handleImportAnimals,
                                            modifier = Modifier.padding(end = 8.dp)
                                    ) { Text("Importer animaux") }

                                    Button(onClick = handleImportFoods) {
                                        Text("Importer aliments")
                                    }
                                }

                                Divider(modifier = Modifier.padding(vertical = 8.dp))

                                // Section administration de base de données
                                Text(
                                        "Administration de la base de données",
                                        style = MaterialTheme.typography.subtitle1
                                )

                                Row(modifier = Modifier.padding(vertical = 8.dp)) {
                                    Button(
                                            onClick = { showConfirmClearFoods = true },
                                            colors =
                                                    ButtonDefaults.buttonColors(
                                                            backgroundColor =
                                                                    MaterialTheme.colors.error
                                                    ),
                                            modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Text(
                                                "Vider BD aliments",
                                                color = MaterialTheme.colors.onError
                                        )
                                    }

                                    Button(
                                            onClick = { showConfirmClearAnimals = true },
                                            colors =
                                                    ButtonDefaults.buttonColors(
                                                            backgroundColor =
                                                                    MaterialTheme.colors.error
                                                    )
                                    ) {
                                        Text(
                                                "Vider BD animaux",
                                                color = MaterialTheme.colors.onError
                                        )
                                    }
                                }

                                // Affichage du message de résultat
                                resultMessage?.let {
                                    Text(
                                            text = it,
                                            style = MaterialTheme.typography.caption,
                                            modifier = Modifier.padding(top = 8.dp)
                                    )
                                }

                                // Indicateur de progression
                                if (isProcessing) {
                                    LinearProgressIndicator(
                                            modifier =
                                                    Modifier.fillMaxWidth().padding(vertical = 8.dp)
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            Button(onClick = { showSettings = false }) { Text("Fermer") }
                        }
                )

                // Boîte de dialogue de confirmation pour vider la base de données des aliments
                if (showConfirmClearFoods) {
                    AlertDialog(
                            onDismissRequest = { showConfirmClearFoods = false },
                            title = { Text("Confirmation") },
                            text = {
                                Text(
                                        "Êtes-vous sûr de vouloir vider la base de données des aliments?"
                                )
                            },
                            confirmButton = {
                                Button(
                                        onClick = {
                                            showConfirmClearFoods = false
                                            isProcessing = true
                                            // Vider la base de données des aliments
                                            coroutineScope.launch {
                                                try {
                                                    val count = settingsViewModel.clearAllFoods()
                                                    resultMessage =
                                                            "$count aliments ont été supprimés."
                                                } catch (e: Exception) {
                                                    resultMessage =
                                                            "Erreur lors de la suppression des aliments: ${e.message}"
                                                } finally {
                                                    isProcessing = false
                                                }
                                            }
                                        },
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor = MaterialTheme.colors.error
                                                )
                                ) { Text("Confirmer", color = MaterialTheme.colors.onError) }
                            },
                            dismissButton = {
                                Button(onClick = { showConfirmClearFoods = false }) {
                                    Text("Annuler")
                                }
                            }
                    )
                }

                // Boîte de dialogue de confirmation pour vider la base de données des animaux
                if (showConfirmClearAnimals) {
                    AlertDialog(
                            onDismissRequest = { showConfirmClearAnimals = false },
                            title = { Text("Confirmation") },
                            text = {
                                Text(
                                        "Êtes-vous sûr de vouloir vider la base de données des animaux?"
                                )
                            },
                            confirmButton = {
                                Button(
                                        onClick = {
                                            showConfirmClearAnimals = false
                                            isProcessing = true
                                            // Vider la base de données des animaux
                                            coroutineScope.launch {
                                                try {
                                                    val count = settingsViewModel.clearAllAnimals()
                                                    resultMessage =
                                                            "$count animaux ont été supprimés."
                                                } catch (e: Exception) {
                                                    resultMessage =
                                                            "Erreur lors de la suppression des animaux: ${e.message}"
                                                } finally {
                                                    isProcessing = false
                                                }
                                            }
                                        },
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor = MaterialTheme.colors.error
                                                )
                                ) { Text("Confirmer", color = MaterialTheme.colors.onError) }
                            },
                            dismissButton = {
                                Button(onClick = { showConfirmClearAnimals = false }) {
                                    Text("Annuler")
                                }
                            }
                    )
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
                                    Text(
                                            "${foodImportResult.count} aliments ont été importés avec succès."
                                    )
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
}
