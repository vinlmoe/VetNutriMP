package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import fr.vetbrain.vetnutri_mp.Data.Equation
import fr.vetbrain.vetnutri_mp.DataBase.BiblioRefDao
import fr.vetbrain.vetnutri_mp.DataBase.EquationDao
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toDomain
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toEntity
import fr.vetbrain.vetnutri_mp.Enumer.EquationKind
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.VariableKind
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel pour gérer les équations
 *
 * @param equationDao DAO pour accéder aux équations
 * @param biblioRefDao DAO pour accéder aux références bibliographiques
 */
class EquationViewModel(
        private val equationDao: EquationDao,
        private val biblioRefDao: BiblioRefDao
) {
    private val coroutineScope = CoroutineScope(AppDispatchers.Main)

    private val _currentEquation = MutableStateFlow(Equation())
    val currentEquation: StateFlow<Equation> = _currentEquation.asStateFlow()

    private val _equations = MutableStateFlow<List<Equation>>(emptyList())
    val equations: StateFlow<List<Equation>> = _equations.asStateFlow()

    private val _biblioRefs = MutableStateFlow<List<BiblioRef>>(emptyList())
    val biblioRefs: StateFlow<List<BiblioRef>> = _biblioRefs.asStateFlow()

    // État de chargement (pour afficher un indicateur de progression)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Message d'opération (succès/erreur)
    private val _operationMessage = MutableStateFlow("")
    val operationMessage: StateFlow<String> = _operationMessage.asStateFlow()

    init {
        loadEquations()
        loadBiblioRefs()
    }

    /** Charge toutes les équations disponibles */
    fun loadEquations() {
        coroutineScope.launch {
            _isLoading.value = true
            try {
                val equations = equationDao.getAllEquations().map { it.toDomain() }
                _equations.value = equations
            } catch (e: Exception) {
                _operationMessage.value = "Erreur lors du chargement des équations: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Charge une équation à partir de son ID
     *
     * @param uuid L'identifiant de l'équation à charger
     */
    fun loadEquationById(uuid: String) {
        coroutineScope.launch {
            _isLoading.value = true
            try {
                val equation = equationDao.getEquationById(uuid)?.toDomain() ?: Equation()
                _currentEquation.value = equation
            } catch (e: Exception) {
                _operationMessage.value = "Erreur lors du chargement de l'équation: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Charge toutes les références bibliographiques disponibles */
    fun loadBiblioRefs() {
        coroutineScope.launch {
            _isLoading.value = true
            try {
                val refs = biblioRefDao.getAllBiblioRefs().map { it.toDomain() }
                _biblioRefs.value = refs
            } catch (e: Exception) {
                _operationMessage.value = "Erreur lors du chargement des références: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Crée une nouvelle équation */
    fun createNewEquation() {
        _currentEquation.value = Equation()
    }

    /** Sauvegarde l'équation en cours d'édition */
    fun saveCurrentEquation() {
        val equation = _currentEquation.value
        coroutineScope.launch {
            _isLoading.value = true
            try {
                val entity = equation.toEntity()
                if (equation.uuid.isEmpty()) {
                    equationDao.insertEquation(entity)
                } else {
                    equationDao.updateEquation(entity)
                }
                _operationMessage.value = "Équation enregistrée avec succès"
                loadEquations()
            } catch (e: Exception) {
                _operationMessage.value = "Erreur lors de l'enregistrement: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Supprime l'équation en cours d'édition */
    fun deleteEquation() {
        val equation = _currentEquation.value
        coroutineScope.launch {
            _isLoading.value = true
            try {
                equationDao.deleteEquation(equation.toEntity())
                _operationMessage.value = "Équation supprimée avec succès"
                _currentEquation.value = Equation()
                loadEquations()
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
    fun updateEquationScript(script: String) {
        _currentEquation.value = _currentEquation.value.copy(equationScript = script)
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

    /** Fixe si l'équation est cohérente ou non */
    fun setConsistent(consistent: Boolean) {
        _currentEquation.value = _currentEquation.value.copy(consistent = consistent)
    }

    /** Met à jour la note bibliographique */
    fun updateBibNote(note: String) {
        _currentEquation.value =
                _currentEquation.value.copy(bib = _currentEquation.value.bib.copy(comments = note))
    }

    /** Met à jour la référence bibliographique */
    fun updateBibRef(ref: String) {
        _currentEquation.value =
                _currentEquation.value.copy(
                        bib = _currentEquation.value.bib.copy(completeRef = ref)
                )
    }

    /** Sélectionne une référence bibliographique existante */
    fun selectBiblioRef(biblioRef: BiblioRef) {
        _currentEquation.value = _currentEquation.value.copy(bib = biblioRef)
    }
}
