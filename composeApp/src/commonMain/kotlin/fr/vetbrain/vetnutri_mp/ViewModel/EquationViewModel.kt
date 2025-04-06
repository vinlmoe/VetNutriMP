package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import fr.vetbrain.vetnutri_mp.Data.Equation
import fr.vetbrain.vetnutri_mp.Enumer.EquationKind
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.VariableKind
import fr.vetbrain.vetnutri_mp.Repository.BiblioRefRepository
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Utils.PlatformDispatcher
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel pour la gestion des équations
 *
 * @param equationRepository Le repository pour accéder aux équations
 * @param biblioRefRepository Le repository pour accéder aux références bibliographiques
 */
class EquationViewModel(
        private val equationRepository: EquationRepository,
        private val biblioRefRepository: BiblioRefRepository
) {
    private val dispatcher = PlatformDispatcher().provideMainDispatcher()
    private val viewModelScope = CoroutineScope(dispatcher)

    // État pour la liste des équations
    private val _equations = MutableStateFlow<List<Equation>>(emptyList())
    val equations: StateFlow<List<Equation>> = _equations.asStateFlow()

    // État pour l'équation en cours d'édition
    private val _currentEquation = MutableStateFlow<Equation?>(null)
    val currentEquation: StateFlow<Equation?> = _currentEquation.asStateFlow()

    // État pour les références bibliographiques disponibles
    private val _biblioRefs = MutableStateFlow<List<BiblioRef>>(emptyList())
    val biblioRefs: StateFlow<List<BiblioRef>> = _biblioRefs.asStateFlow()

    // État pour les messages d'erreur
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // État indiquant si une opération est en cours
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /** Charge la liste des équations */
    fun loadEquations() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                equationRepository
                        .observeAllEquations()
                        .catch { e ->
                            _errorMessage.value =
                                    "Erreur lors du chargement des équations: ${e.message}"
                            println(
                                    "DEBUG EquationViewModel: Erreur lors du chargement des équations: ${e.message}"
                            )
                        }
                        .collect { equations ->
                            _equations.value = equations
                            _isLoading.value = false
                            println("DEBUG EquationViewModel: ${equations.size} équations chargées")
                        }
            } catch (e: Exception) {
                _errorMessage.value = "Erreur lors du chargement des équations: ${e.message}"
                _isLoading.value = false
                println(
                        "DEBUG EquationViewModel: Exception lors du chargement des équations: ${e.message}"
                )
            }
        }
    }

    /** Charge la liste des références bibliographiques */
    fun loadBiblioRefs() {
        viewModelScope.launch {
            try {
                // Utiliser firstOrNull pour éviter de bloquer
                biblioRefRepository
                        .getAllBiblioRefs()
                        .catch { e ->
                            println(
                                    "DEBUG EquationViewModel: Erreur lors du chargement des références: ${e.message}"
                            )
                        }
                        .firstOrNull()
                        ?.let { refs ->
                            _biblioRefs.value = refs
                            println("DEBUG EquationViewModel: ${refs.size} références chargées")
                        }
            } catch (e: Exception) {
                println(
                        "DEBUG EquationViewModel: Exception lors du chargement des références: ${e.message}"
                )
            }
        }
    }

    // Charge une équation spécifique par son UUID
    fun loadEquation(uuid: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (uuid != null) {
                    val equation = equationRepository.getEquationById(uuid)
                    _currentEquation.value = equation
                } else {
                    // Créer une nouvelle équation vide
                    resetCurrentEquation()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erreur lors du chargement de l'équation: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Réinitialise l'équation courante avec une nouvelle équation vide
    @OptIn(ExperimentalUuidApi::class)
    fun resetCurrentEquation() {
        _currentEquation.value =
                Equation(
                        uuid = Uuid.random().toString(),
                        description = "",
                        equationScript = "",
                        bib = BiblioRef(),
                        specie = Espece.CHIEN,
                        name = "",
                        kind = EquationKind.ENERGYNEED,
                        consistent = true,
                        variables = mutableListOf()
                )
    }

    // Met à jour un champ de l'équation courante
    fun updateEquationField(field: String, value: Any) {
        _currentEquation.update { current ->
            current?.let {
                when (field) {
                    "name" -> it.copy(name = value as String)
                    "description" -> it.copy(description = value as String)
                    "equationScript" -> it.copy(equationScript = value as String)
                    "specie" -> it.copy(specie = value as Espece)
                    "kind" -> it.copy(kind = value as EquationKind)
                    "biblioRef" -> it.copy(bib = value as BiblioRef)
                    else -> it
                }
            }
        }
    }

    // Ajoute une variable à l'équation courante
    fun addVariable(variable: VariableKind) {
        _currentEquation.update { current ->
            current?.let {
                val updatedVariables = it.variables.toMutableList()
                if (!updatedVariables.contains(variable)) {
                    updatedVariables.add(variable)
                }
                it.copy(variables = updatedVariables)
            }
        }
    }

    // Supprime une variable de l'équation courante
    fun removeVariable(variable: VariableKind) {
        _currentEquation.update { current ->
            current?.let {
                val updatedVariables = it.variables.toMutableList()
                updatedVariables.remove(variable)
                it.copy(variables = updatedVariables)
            }
        }
    }

    // Sauvegarde l'équation courante
    fun saveEquation() {
        val currentEq = _currentEquation.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val existingEquation =
                        if (currentEq.uuid.isNotBlank())
                                equationRepository.getEquationById(currentEq.uuid)
                        else null

                if (existingEquation != null) {
                    equationRepository.updateEquation(currentEq)
                } else {
                    equationRepository.saveEquation(currentEq)
                }

                // Recharger la liste des équations
                loadEquations()
            } catch (e: Exception) {
                _errorMessage.value = "Erreur lors de la sauvegarde de l'équation: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Supprime l'équation courante
    fun deleteEquation() {
        val currentEq = _currentEquation.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                equationRepository.deleteEquation(currentEq.uuid)

                // Réinitialiser et recharger
                resetCurrentEquation()
                loadEquations()
            } catch (e: Exception) {
                _errorMessage.value = "Erreur lors de la suppression de l'équation: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Efface le message d'erreur
    fun clearError() {
        _errorMessage.value = null
    }

    // Initialisation du ViewModel
    init {
        loadEquations()
        loadBiblioRefs()
    }
}
