package fr.vetbrain.vetnutri_mp

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Localization.LocalizationManager
import fr.vetbrain.vetnutri_mp.Repository.InMemoryAnimalRepository
import fr.vetbrain.vetnutri_mp.Theme.VetNutriTheme
import fr.vetbrain.vetnutri_mp.View.*
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalListViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.CreateAnimalViewModel
import kotlinx.coroutines.runBlocking

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

    // Initialisation des ViewModels
    val animalListViewModel = remember { AnimalListViewModel(animalRepository) }
    val createAnimalViewModel = remember { CreateAnimalViewModel(animalRepository) }
    val animalDetailViewModel = remember { AnimalDetailViewModel() }

    var currentScreen by remember { mutableStateOf<Screen>(Screen.List) }
    var selectedAnimal by remember { mutableStateOf<AnimalEv?>(null) }
    var isEditing by remember { mutableStateOf(false) }

    VetNutriTheme {
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

private sealed class Screen {
    object List : Screen()
    object Create : Screen()
    object Detail : Screen()
}
