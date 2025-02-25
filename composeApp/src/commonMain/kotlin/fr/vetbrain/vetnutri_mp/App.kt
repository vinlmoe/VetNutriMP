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
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalListViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.CreateAnimalViewModel

import kotlinx.coroutines.*

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

    var currentScreen by remember { mutableStateOf<Screen>(Screen.List) }

    VetNutriTheme {
        when (currentScreen) {
            Screen.List -> {
                AnimalListView(
                        viewModel = animalListViewModel,
                        onAddAnimal = { currentScreen = Screen.Create },
                        onSelectAnimal = { /* TODO: Implémenter la vue de détails */},
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
        }
    }
}

private sealed class Screen {
    object List : Screen()
    object Create : Screen()
}
