package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import fr.vetbrain.vetnutri_mp.Data.Equation
import fr.vetbrain.vetnutri_mp.Enumer.EquationKind
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.VariableKind
import fr.vetbrain.vetnutri_mp.Repository.BiblioRefRepository
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Utils.PlatformDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel pour gérer les équations et les bibliographies associées
 *
 * @param equationRepository Repository pour accéder aux équations
 * @param biblioRefRepository Repository pour accéder aux références bibliographiques
 * @param platformDispatcher Dispatcher pour exécuter les coroutines sur la plateforme appropriée
 */
class EquationViewModel(
        private val equationRepository: EquationRepository,
        private val biblioRefRepository: BiblioRefRepository,
        platformDispatcher: PlatformDispatcher
) {
    private val dispatcher = platformDispatcher.provideMainDispatcher()
    private val viewModelScope = CoroutineScope(dispatcher)

    // État de l'équation en cours d'édition
    private val _currentEquation = MutableStateFlow(Equation())
    val currentEquation: StateFlow<Equation> = _currentEquation.asStateFlow()

    // Liste des équations disponibles
    private val _equations = MutableStateFlow<List<Equation>>(emptyList())
    val equations = _equations.asStateFlow()

    // État de chargement (pour afficher un indicateur de progression)
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Message d'opération (succès/erreur)
    private val _operationMessage = MutableStateFlow("")
    val operationMessage = _operationMessage.asStateFlow()

    /**
     * Charge une équation à partir de son ID
     *
     * @param id L'identifiant de l'équation à charger
     */
    fun loadEquation(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val equation = equationRepository.getEquationById(id)
                if (equation != null) {
                    _currentEquation.value = equation
                } else {
                    _operationMessage.value = "Erreur: Équation non trouvée"
                }
            } catch (e: Exception) {
                _operationMessage.value = "Erreur lors du chargement de l'équation: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Charge une équation à partir de son ID et l'assigne à l'équation courante
     *
     * @param id L'identifiant de l'équation à charger
     */
    fun loadEquationById(id: String) {
        loadEquation(id)
    }

    /** Charge toutes les équations disponibles */
    fun loadEquations() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _equations.value = equationRepository.getAllEquations()
            } catch (e: Exception) {
                _operationMessage.value = "Erreur lors du chargement des équations: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Sauvegarde l'équation en cours d'édition */
    fun saveEquation() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                equationRepository.saveEquation(_currentEquation.value)
                _operationMessage.value = "Équation enregistrée avec succès."
                loadEquations() // Recharger la liste
            } catch (e: Exception) {
                _operationMessage.value = "Erreur lors de l'enregistrement: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Supprime l'équation en cours d'édition */
    fun deleteEquation() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                equationRepository.deleteEquation(_currentEquation.value.uuid)
                _operationMessage.value = "Équation supprimée avec succès."
                _currentEquation.value = Equation() // Réinitialiser
                loadEquations() // Recharger la liste
            } catch (e: Exception) {
                _operationMessage.value = "Erreur lors de la suppression: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Réinitialise l'équation en cours d'édition */
    fun clearCurrentEquation() {
        _currentEquation.value = Equation()
    }

    /** Efface le message d'opération */
    fun clearOperationMessage() {
        _operationMessage.value = ""
    }

    /** Met à jour le nom de l'équation */
    fun updateName(name: String) {
        _currentEquation.value = _currentEquation.value.copy(name = name)
    }

    /** Met à jour la description de l'équation */
    fun updateDescription(description: String) {
        _currentEquation.value = _currentEquation.value.copy(description = description)
    }

    /** Met à jour le type d'équation */
    fun updateKind(kind: EquationKind) {
        _currentEquation.value = _currentEquation.value.copy(kind = kind)
    }

    /** Met à jour l'espèce de l'équation */
    fun updateSpecie(specie: Espece?) {
        _currentEquation.value = _currentEquation.value.copy(specie = specie)
    }

    /** Met à jour le script de l'équation */
    fun updateScript(script: String) {
        _currentEquation.value = _currentEquation.value.copy(equationScript = script)
    }

    /** Met à jour le facteur de correction */
    fun updateCorrectionFactor(factor: Double) {
        _currentEquation.value = _currentEquation.value.copy(correctionFactor = factor)
    }

    /** Met à jour la note bibliographique */
    fun updateBibNote(note: String) {
        _currentEquation.value = _currentEquation.value.copy(bibNote = note)
    }

    /** Met à jour la référence bibliographique */
    fun updateBibRef(ref: String) {
        _currentEquation.value = _currentEquation.value.copy(bibRef = ref)
    }

    /** Ajoute une variable à l'équation */
    fun addVariable(variable: VariableKind) {
        val updatedVariables = _currentEquation.value.variables.toMutableList()
        if (!updatedVariables.contains(variable)) {
            updatedVariables.add(variable)
        }
        _currentEquation.value = _currentEquation.value.copy(variables = updatedVariables)
    }

    /** Supprime une variable de l'équation */
    fun removeVariable(variable: VariableKind) {
        val updatedVariables = _currentEquation.value.variables.toMutableList()
        updatedVariables.remove(variable)
        _currentEquation.value = _currentEquation.value.copy(variables = updatedVariables)
    }

    /** Met à jour les informations bibliographiques */
    fun updateBibRef(biblioRef: BiblioRef) {
        _currentEquation.value = _currentEquation.value.copy(bib = biblioRef)
    }

    /** Fixe si l'équation est cohérente ou non */
    fun setConsistent(consistent: Boolean) {
        _currentEquation.value = _currentEquation.value.copy(consistent = consistent)
    }
}
