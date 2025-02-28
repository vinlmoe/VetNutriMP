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
import fr.vetbrain.vetnutri_mp.Repository.DatabaseAlimentRationRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseAlimentRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseAnimalRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseConsultationRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseRationRepository
import fr.vetbrain.vetnutri_mp.Theme.VetNutriTheme
import fr.vetbrain.vetnutri_mp.View.*
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalListViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.CreateAnimalViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.RationsViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel
import kotlinx.coroutines.runBlocking

enum class Screen {
    LIST,
    ANIMAL_DETAIL,
    RATIONS
}

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

    val rationRepository = remember { DatabaseRationRepository(appDatabase.rationDao()) }
    val alimentRationRepository = remember {
        DatabaseAlimentRationRepository(appDatabase.alimentRationDao())
    }
    val alimentRepository = remember {
        DatabaseAlimentRepository(
                alimentBaseDao = appDatabase.alimentBaseDao(),
                nutrientValueDao = appDatabase.nutrientValueDao()
        )
    }

    // Initialisation des ViewModels
    val animalListViewModel = remember { AnimalListViewModel(animalRepository) }
    val createAnimalViewModel = remember { CreateAnimalViewModel(animalRepository) }
    val animalDetailViewModel =
            AnimalDetailViewModel(
                    consultationRepository = consultationRepository,
                    repository = animalRepository,
                    rationRepository = rationRepository,
                    alimentRationRepository = alimentRationRepository,
                    alimentRepository = alimentRepository
            )

    val rationsViewModel =
            RationsViewModel(
                    rationRepository = rationRepository,
                    alimentRationRepository = alimentRationRepository,
                    alimentRepository = alimentRepository
            )

    val settingsViewModel = SettingsViewModel()

    var currentScreen by remember { mutableStateOf(Screen.LIST) }
    var selectedAnimal by remember { mutableStateOf<AnimalEv?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    VetNutriTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                when (currentScreen) {
                    Screen.LIST -> {
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
                                        currentScreen = Screen.ANIMAL_DETAIL
                                    },
                                    onSelectAnimal = { animal ->
                                        selectedAnimal = animal
                                        animalDetailViewModel.setAnimal(animal)
                                        currentScreen = Screen.ANIMAL_DETAIL
                                    },
                                    onEditAnimal = { animal ->
                                        selectedAnimal = animal
                                        createAnimalViewModel.updateAnimal(animal)
                                        isEditing = true
                                        currentScreen = Screen.ANIMAL_DETAIL
                                    },
                                    modifier = Modifier.fillMaxWidth().weight(1f)
                            )
                        }
                    }
                    Screen.ANIMAL_DETAIL -> {
                        AnimalDetailView(
                                viewModel = animalDetailViewModel,
                                settingsViewModel = settingsViewModel,
                                onNavigateBack = {
                                    selectedAnimal = null
                                    currentScreen = Screen.LIST
                                },
                                onNavigateToRations = { currentScreen = Screen.RATIONS },
                                modifier = Modifier.fillMaxSize()
                        )
                    }
                    Screen.RATIONS -> {
                        AnimalDetailView(
                                viewModel = animalDetailViewModel,
                                settingsViewModel = settingsViewModel,
                                onNavigateBack = {
                                    selectedAnimal = null
                                    currentScreen = Screen.LIST
                                },
                                onNavigateToRations = { currentScreen = Screen.RATIONS },
                                initialSection = MainSection.RATIONS,
                                modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            if (showSettings) {
                SettingsDialog(viewModel = settingsViewModel, onDismiss = { showSettings = false })
            }
        }
    }
}
