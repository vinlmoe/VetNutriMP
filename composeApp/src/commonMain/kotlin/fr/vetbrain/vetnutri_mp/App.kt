package fr.vetbrain.vetnutri_mp

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Localization.LocalizationManager
<<<<<<< HEAD
import fr.vetbrain.vetnutri_mp.Repository.DatabaseAlimentRationRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseAlimentRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseAnimalRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseConsultationRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseRationRepository
=======
import fr.vetbrain.vetnutri_mp.Repository.InMemoryAnimalRepository
>>>>>>> parent of f5d4378 (Houra cela marche reste des petit truc et es fonctionalité)
import fr.vetbrain.vetnutri_mp.Theme.VetNutriTheme
import fr.vetbrain.vetnutri_mp.View.*
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalListViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.CreateAnimalViewModel
<<<<<<< HEAD
import fr.vetbrain.vetnutri_mp.ViewModel.RationsViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel
=======
>>>>>>> parent of f5d4378 (Houra cela marche reste des petit truc et es fonctionalité)
import kotlinx.coroutines.runBlocking

enum class Screen {
    LIST,
    ANIMAL_DETAIL,
    RATIONS
}

@Composable
fun App() {
    // Initialisation de la localisation
    LocalizationManager.initialize()

    // Création du repository en mémoire avec des données de test
    val animalRepository = remember {
        InMemoryAnimalRepository().apply {
            // Ajout de quelques animaux de test
            runBlocking {
                saveAnimal(AnimalEv.createTestAnimal())
                saveAnimal(
                        AnimalEv.createTestAnimal()
                                .copy(nom = "Felix", specieId = Espece.CHAT.name, race = "Siamois")
                )
            }
        }
    }

<<<<<<< HEAD
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
=======
    // Initialisation des ViewModels
    val animalListViewModel = remember { AnimalListViewModel(animalRepository) }
    val createAnimalViewModel = remember { CreateAnimalViewModel(animalRepository) }
    val animalDetailViewModel = remember { AnimalDetailViewModel() }
>>>>>>> parent of f5d4378 (Houra cela marche reste des petit truc et es fonctionalité)

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

    VetNutriTheme {
<<<<<<< HEAD
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
=======
        when (currentScreen) {
            Screen.List -> {
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
                        modifier = Modifier.fillMaxSize()
                )
>>>>>>> parent of f5d4378 (Houra cela marche reste des petit truc et es fonctionalité)
            }
            Screen.Create -> {
                CreateAnimalView(
                        viewModel = createAnimalViewModel,
                        onNavigateBack = { currentScreen = Screen.List },
                        modifier = Modifier.fillMaxSize()
                )
            }
            Screen.Detail -> {
                AnimalDetailView(
                        viewModel = animalDetailViewModel,
                        onNavigateBack = { currentScreen = Screen.List },
                        modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
