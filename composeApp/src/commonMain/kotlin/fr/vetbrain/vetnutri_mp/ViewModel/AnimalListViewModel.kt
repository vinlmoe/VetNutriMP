package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.AnimalEvJson
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Repository.AnimalRepository
import fr.vetbrain.vetnutri_mp.Repository.FoodImportResult
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalUuidApi::class)
class AnimalListViewModel(private val animalRepository: AnimalRepository) : ViewModel() {
    private val _allAnimals = MutableStateFlow<List<AnimalEv>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedEspece = MutableStateFlow<Espece?>(null)
    val selectedEspece: StateFlow<Espece?> = _selectedEspece

    // Liste de toutes les espèces disponibles, avec une option "Toutes" (null)
    val availableEspeces: List<Espece?> = listOf(null) + Espece.entries.toList()

    /**
     * Flux d'animaux filtrés selon le terme de recherche et l'espèce sélectionnée. La recherche
     * s'effectue sur:
     * - Le nom de l'animal
     * - Le nom du propriétaire
     * - La race de l'animal Les résultats sont triés par ordre alphabétique du nom de l'animal.
     */
    val animals: StateFlow<List<AnimalEv>> =
            _searchQuery
                    .map { query ->
                        if (query.isBlank() && _selectedEspece.value == null) {
                            _allAnimals.value.sortedBy { it.nom }
                        } else {
                            _allAnimals.value
                                    .filter { animal ->
                                        val matchesQuery =
                                                query.isBlank() ||
                                                        animal.nom.contains(
                                                                query,
                                                                ignoreCase = true
                                                        ) ||
                                                        animal.ownerName.contains(
                                                                query,
                                                                ignoreCase = true
                                                        ) ||
                                                        animal.race.contains(
                                                                query,
                                                                ignoreCase = true
                                                        )

                                        val matchesEspece =
                                                _selectedEspece.value == null ||
                                                        animal.getEspece() == _selectedEspece.value

                                        matchesQuery && matchesEspece
                                    }
                                    .sortedBy { it.nom }
                        }
                    }
                    .let { MutableStateFlow(emptyList()) }

    private val _importResult = MutableStateFlow<ImportResult?>(null)
    val importResult: StateFlow<ImportResult?> = _importResult

    fun loadAnimals() {
        viewModelScope.launch {
            _allAnimals.value = animalRepository.getAllAnimals()
            updateFilteredAnimals()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        updateFilteredAnimals()
    }

    fun setSelectedEspece(espece: Espece?) {
        _selectedEspece.value = espece
        updateFilteredAnimals()
    }

    /**
     * Met à jour la liste des animaux filtrés en fonction du terme de recherche et de l'espèce
     * sélectionnée.
     */
    private fun updateFilteredAnimals() {
        val query = _searchQuery.value
        val espece = _selectedEspece.value

        (animals as MutableStateFlow).value =
                if (query.isBlank() && espece == null) {
                    _allAnimals.value.sortedBy { it.nom }
                } else {
                    _allAnimals.value
                            .filter { animal ->
                                val matchesQuery =
                                        query.isBlank() ||
                                                animal.nom.contains(query, ignoreCase = true) ||
                                                animal.ownerName.contains(
                                                        query,
                                                        ignoreCase = true
                                                ) ||
                                                animal.race.contains(query, ignoreCase = true)

                                val matchesEspece = espece == null || animal.getEspece() == espece

                                matchesQuery && matchesEspece
                            }
                            .sortedBy { it.nom }
                }
    }

    fun deleteAnimal(animal: AnimalEv) {
        viewModelScope.launch {
            animalRepository.deleteAnimal(animal)
            loadAnimals() // Refresh the list after deletion
        }
    }

    fun importAnimals(animalsJson: List<AnimalEvJson>) {
        viewModelScope.launch {
            try {
                val importResult = animalRepository.importAnimals(animalsJson)
                _importResult.value =
                        ImportResult.Success(importResult.importedCount + importResult.updatedCount)
                loadAnimals() // Refresh the list after import
            } catch (e: Exception) {
                _importResult.value = ImportResult.Error(e.message ?: "Erreur inconnue")
            }
        }
    }

    /**
     * Définit une erreur d'importation
     *
     * @param message Le message d'erreur à afficher
     */
    fun setImportError(message: String) {
        _importResult.value = ImportResult.Error(message)
    }

    fun resetImportResult() {
        _importResult.value = null
    }

    /**
     * Délègue l'importation des animaux à la fonction de plateforme spécifique Cela permet d'éviter
     * l'ambiguïté d'appel direct dans la vue
     */
    fun importAnimalsFromFileUI() {
        fr.vetbrain.vetnutri_mp.importAnimalsFromFile(this)
    }

    /**
     * Importe des animaux à partir d'une chaîne JSON
     *
     * @param jsonContent Le contenu JSON à désérialiser
     */
    fun importAnimalsFromJson(jsonContent: String) {
        viewModelScope.launch {
            try {
                val importResult =
                        fr.vetbrain.vetnutri_mp.Utils.ImportUtils.importAnimalsFromJson(jsonContent)

                if (importResult.animals.isNotEmpty()) {
                    var foodsImported = false
                    var foodImportResult: FoodImportResult? = null

                    // Importer d'abord les aliments extraits des rations s'il y en a
                    if (importResult.foods.isNotEmpty()) {
                        println(
                                "Importation de ${importResult.foods.size} aliments extraits des rations..."
                        )
                        try {
                            // Obtenir le repository des aliments
                            val foodRepository = animalRepository.getFoodRepository()
                            if (foodRepository != null) {
                                try {
                                    foodImportResult =
                                            foodRepository.importFoods(importResult.foods)
                                    println(
                                            "${foodImportResult.importedCount} aliments importés, ${foodImportResult.updatedCount} mis à jour"
                                    )
                                    foodsImported =
                                            foodImportResult.importedCount +
                                                    foodImportResult.updatedCount > 0
                                } catch (e: Exception) {
                                    println(
                                            "Erreur lors de l'importation des aliments: ${e.message}"
                                    )
                                    e.printStackTrace()
                                    // Continuer l'importation des animaux même si l'importation des
                                    // aliments échoue
                                }
                            } else {
                                println("Impossible d'obtenir le repository des aliments")
                            }
                        } catch (e: Exception) {
                            println("Erreur lors de l'importation des aliments: ${e.message}")
                            e.printStackTrace()
                        }
                    }

                    try {
                        val importResult = animalRepository.importAnimals(importResult.animals)

                        if (importResult.importedCount + importResult.updatedCount > 0) {
                            if (foodsImported) {
                                _importResult.value =
                                        ImportResult.Success(
                                                importResult.importedCount +
                                                        importResult.updatedCount
                                        )
                                println(
                                        "${importResult.importedCount} animaux importés, ${importResult.updatedCount} mis à jour, et leurs aliments importés avec succès"
                                )
                            } else {
                                _importResult.value =
                                        ImportResult.Success(
                                                importResult.importedCount +
                                                        importResult.updatedCount
                                        )
                                println(
                                        "${importResult.importedCount} animaux importés, ${importResult.updatedCount} mis à jour (sans aliments)"
                                )
                            }
                            loadAnimals() // Actualiser la liste après l'importation
                        } else {
                            _importResult.value =
                                    ImportResult.Error("Échec de l'importation des animaux")
                            println("Échec de l'importation des animaux")
                        }
                    } catch (e: Exception) {
                        _importResult.value =
                                ImportResult.Error(
                                        "Erreur lors de l'importation des animaux: ${e.message}"
                                )
                        println("Erreur lors de l'importation des animaux: ${e.message}")
                        e.printStackTrace()
                    }
                } else {
                    _importResult.value =
                            ImportResult.Error("Aucun animal trouvé dans le fichier JSON")
                }
            } catch (e: Exception) {
                _importResult.value = ImportResult.Error(e.message ?: "Erreur inconnue")
                println("Erreur lors du traitement du JSON: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    sealed class ImportResult {
        data class Success(val count: Int) : ImportResult()
        data class Error(val message: String) : ImportResult()
    }
}
