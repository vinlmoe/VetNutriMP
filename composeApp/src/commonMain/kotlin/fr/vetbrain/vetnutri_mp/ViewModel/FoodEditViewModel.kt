package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.compose.runtime.mutableStateListOf
import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Repository.AlimentRepository
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalUuidApi::class)
class FoodEditViewModel(
        private val alimentRepository: AlimentRepository,
        private val alimentUuid: String? = null
) {
    private val coroutineScope = CoroutineScope(AppDispatchers.Main)

    // Créer un aliment par défaut avec un UUID aléatoire
    private val defaultAliment = AlimentEv(uuid = Uuid.random().toString(), rationUUID = null)

    private val _alimentState: MutableStateFlow<AlimentEv> = MutableStateFlow(defaultAliment)
    val alimentState: StateFlow<AlimentEv> = _alimentState.asStateFlow()

    // Liste des nutriments disponibles
    private val _allNutrients = mutableStateListOf<Nutrient>()

    init {
        // Charger tous les types de nutriments
        loadNutrients()

        // Si un UUID est fourni, charger l'aliment correspondant
        if (!alimentUuid.isNullOrBlank()) {
            loadAliment(alimentUuid)
        }
    }

    private fun loadNutrients() {
        // Charger tous les types de nutriments

        // Nutriments principaux
        NutrientMain.entries.forEach { nutrient ->
            if (!_allNutrients.contains(nutrient)) {
                _allNutrients.add(nutrient)
            }
        }

        // Lipides
        NutrientLipid.entries.forEach { nutrient ->
            if (!_allNutrients.contains(nutrient)) {
                _allNutrients.add(nutrient)
            }
        }

        // Macronutriments
        NutrientMacro.entries.forEach { nutrient ->
            if (!_allNutrients.contains(nutrient)) {
                _allNutrients.add(nutrient)
            }
        }

        // Minéraux
        NutrientMin.entries.forEach { nutrient ->
            if (!_allNutrients.contains(nutrient)) {
                _allNutrients.add(nutrient)
            }
        }

        // Vitamines
        NutrientVitam.entries.forEach { nutrient ->
            if (!_allNutrients.contains(nutrient)) {
                _allNutrients.add(nutrient)
            }
        }

        // Acides aminés
        AAEnum.entries.forEach { nutrient ->
            if (!_allNutrients.contains(nutrient)) {
                _allNutrients.add(nutrient)
            }
        }

        // Autres nutriments
        NutrientOther.entries.forEach { nutrient ->
            if (!_allNutrients.contains(nutrient)) {
                _allNutrients.add(nutrient)
            }
        }

        println("DEBUG FoodEditViewModel: ${_allNutrients.size} nutriments chargés")
    }

    private fun loadAliment(uuid: String) {
        coroutineScope.launch {
            try {
                println("DEBUG FoodEditViewModel: Chargement de l'aliment avec UUID: $uuid")
                val aliment = alimentRepository.getAlimentByUUID(uuid)
                if (aliment != null) {
                    println("DEBUG FoodEditViewModel: Aliment trouvé: ${aliment.nom}")
                    _alimentState.value = aliment

                    // S'assurer que la liste des nutriments est chargée
                    if (_allNutrients.isEmpty()) {
                        loadNutrients()
                    }

                    // Collecter les labels existants pour éviter les doublons
                    val existingLabels = _allNutrients.map { it.label }.toSet()

                    // Ajouter uniquement les nutriments qui ne sont pas déjà dans la liste
                    aliment.valMap.keys.forEach { nutrient ->
                        if (nutrient.label !in existingLabels) {
                            _allNutrients.add(nutrient)
                        }
                    }
                } else {
                    println("DEBUG FoodEditViewModel: Aucun aliment trouvé pour UUID: $uuid")
                }
            } catch (e: Exception) {
                println("Erreur lors du chargement de l'aliment: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun getAllNutrients(): List<Nutrient> {
        // Si la liste est vide, retourner les clés de l'aliment actuel
        if (_allNutrients.isEmpty()) {
            return _alimentState.value.valMap.keys.toList()
        }
        return _allNutrients
    }

    suspend fun saveAliment(aliment: AlimentEv) {
        try {
            println("DEBUG FoodEditViewModel: Début saveAliment - UUID: ${aliment.uuid}")
            println("DEBUG FoodEditViewModel: Enregistrement de l'aliment: ${aliment.nom}")
            println("DEBUG FoodEditViewModel: Nombre de nutriments: ${aliment.valMap.size}")

            // Vérifier si c'est un nouvel aliment ou une mise à jour
            val existingAliment =
                    try {
                        alimentRepository.getAlimentByUUID(aliment.uuid)
                    } catch (e: Exception) {
                        println(
                                "DEBUG FoodEditViewModel: Erreur lors de la recherche de l'aliment existant: ${e.message}"
                        )
                        null
                    }

            if (existingAliment != null) {
                println("DEBUG FoodEditViewModel: Aliment existant trouvé, mise à jour")
            } else {
                println("DEBUG FoodEditViewModel: Nouvel aliment, création")
            }

            aliment.valMap.forEach { (nutrient, quantity) ->
                println("DEBUG FoodEditViewModel: Nutriment ${nutrient.label} = ${quantity.value}")
            }

            // Sauvegarder l'aliment
            println("DEBUG FoodEditViewModel: Appel à alimentRepository.saveAliment")
            alimentRepository.saveAliment(aliment)
            println("DEBUG FoodEditViewModel: saveAliment terminé avec succès")

            // Mettre à jour l'état local
            _alimentState.value = aliment
            println("DEBUG FoodEditViewModel: État local mis à jour")
        } catch (e: Exception) {
            println(
                    "DEBUG FoodEditViewModel: ERREUR lors de l'enregistrement de l'aliment: ${e.message}"
            )
            e.printStackTrace()
            throw e // Relancer l'exception pour que la vue puisse la gérer
        }
    }
}
