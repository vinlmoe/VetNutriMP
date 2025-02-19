package fr.vetbrain.vetnutri_mp

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import fr.vetbrain.vetnutri_mp.DataBase.DatabaseBuilder
import fr.vetbrain.vetnutri_mp.Localization.LocalizationManager
import fr.vetbrain.vetnutri_mp.Theme.VetNutriTheme
import fr.vetbrain.vetnutri_mp.View.*
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalListViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.CreateAnimalViewModel

@Composable
fun App() {
    // Initialisation de la localisation
    LocalizationManager.initialize()

    // Initialisation de la base de données
    DatabaseBuilder.initialize()

    // Initialisation des dépendances
    val animalRepository = DatabaseBuilder.getAnimalRepository()
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
