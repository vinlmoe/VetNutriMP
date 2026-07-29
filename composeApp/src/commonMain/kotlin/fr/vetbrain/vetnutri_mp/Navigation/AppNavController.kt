package fr.vetbrain.vetnutri_mp.Navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Enumer.Espece

internal class AppNavController {
    var screen by mutableStateOf<Screen>(Screen.List)
    var selectedAnimal by mutableStateOf<AnimalEv?>(null)
    var isEditing by mutableStateOf(false)
    var selectedFoodUuid by mutableStateOf<String?>(null)
    var selectedBiblioRefId by mutableStateOf<String?>(null)
    var selectedReferenceEvId by mutableStateOf<String?>(null)
    var selectedEquationId by mutableStateOf<String?>(null)
    var selectedConseilId by mutableStateOf<String?>(null)
    var selectedCalculationTab by mutableStateOf(0)
    var selectedReferenceIdsForBulk by mutableStateOf<List<String>>(emptyList())
    var selectedSpecies by mutableStateOf<Espece?>(null)

    fun navigate(target: Screen) { screen = target }
}
