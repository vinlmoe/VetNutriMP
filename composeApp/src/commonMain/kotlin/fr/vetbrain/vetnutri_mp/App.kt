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
    val animalDetailViewModel = remember { AnimalDetailViewModel(consultationRepository) }
    val settingsViewModel = remember { SettingsViewModel() }

    var currentScreen by remember { mutableStateOf<Screen>(Screen.List) }
    var selectedAnimal by remember { mutableStateOf<AnimalEv?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

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
                            TopBar(
                                    title = selectedAnimal?.nom ?: "",
                                    onSettingsClick = { showSettings = true }
                            )
                            AnimalDetailView(
                                    viewModel = animalDetailViewModel,
                                    settingsViewModel = settingsViewModel,
                                    onNavigateBack = { currentScreen = Screen.List },
                                    modifier = Modifier.fillMaxWidth().weight(1f)
                            )
                        }
                    }
                }
            }

            if (showSettings) {
                SettingsDialog(viewModel = settingsViewModel, onDismiss = { showSettings = false })
            }
        }
    }
}

private sealed class Screen {
    object List : Screen()
    object Create : Screen()
    object Detail : Screen()
}
