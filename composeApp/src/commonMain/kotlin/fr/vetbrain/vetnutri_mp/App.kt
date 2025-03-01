package fr.vetbrain.vetnutri_mp

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import fr.vetbrain.vetnutri_mp.Components.TopBar
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.DataBase.AppDatabase
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Localization.LocalizationManager
import fr.vetbrain.vetnutri_mp.Repository.DatabaseAnimalRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseConsultationRepository
import fr.vetbrain.vetnutri_mp.Theme.VetNutriTheme
import fr.vetbrain.vetnutri_mp.View.*
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalListViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.CreateAnimalViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel
import kotlinx.coroutines.runBlocking

expect fun importAnimalsFromFile(viewModel: AnimalListViewModel)

@Composable
fun App(appDatabase: AppDatabase) {
    // Initialisation de la localisation
    LocalizationManager.initialize()

    // Création des repositories avec la base de données
    val animalRepository = remember {
        DatabaseAnimalRepository(appDatabase.animalDao()).apply {
            // Ajout de quelques animaux de test uniquement si la base est vide
            runBlocking {
                if (getAllAnimals().isEmpty()) {
                    saveAnimal(AnimalEv.createTestAnimal())
                    saveAnimal(
                            AnimalEv.createTestAnimal()
                                    .copy(
                                            nom = "Felix",
                                            specieId = Espece.CHAT.name,
                                            race = "Siamois"
                                    )
                    )
                }
            }
        }
    }

    val consultationRepository = remember {
        DatabaseConsultationRepository(appDatabase.consultationDao())
    }

    // Initialisation des ViewModels
    val animalListViewModel = remember { AnimalListViewModel(animalRepository) }
    val createAnimalViewModel = remember { CreateAnimalViewModel(animalRepository) }
    val animalDetailViewModel = remember {
        AnimalDetailViewModel(consultationRepository, animalRepository)
    }
    val settingsViewModel = remember { SettingsViewModel() }

    var currentScreen by remember { mutableStateOf<Screen>(Screen.List) }
    var selectedAnimal by remember { mutableStateOf<AnimalEv?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    // Observer le résultat de l'importation
    val importResult = animalListViewModel.importResult.collectAsState().value
    var showImportResult by remember { mutableStateOf(false) }

    LaunchedEffect(importResult) {
        if (importResult != null) {
            showImportResult = true
        }
    }

    // Effet pour recharger la liste des animaux lorsque l'utilisateur revient à l'écran de liste
    LaunchedEffect(currentScreen) {
        if (currentScreen == Screen.List) {
            animalListViewModel.loadAnimals()
        }
    }

    // Fonction locale pour importer les animaux
    val handleImportAnimals: () -> Unit = {
        // Appel à la fonction expect/actual
        importAnimalsFromFile(animalListViewModel)
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
                                    onSelectAnimal = { animal ->
                                        selectedAnimal = animal
                                        animalDetailViewModel.setAnimal(animal)
                                        currentScreen = Screen.Detail
                                    },
                                    onEditAnimal = { animal ->
                                        selectedAnimal = animal
                                        createAnimalViewModel.updateAnimal(animal)
                                        isEditing = true
                                        currentScreen = Screen.Create
                                    },
                                    onImportAnimals = handleImportAnimals,
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
                }
            }

            if (showSettings) {
                SettingsDialog(viewModel = settingsViewModel, onDismiss = { showSettings = false })
            }

            // Afficher le résultat de l'importation
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
        }
    }
}

private sealed class Screen {
    object List : Screen()
    object Create : Screen()
    object Detail : Screen()
}
