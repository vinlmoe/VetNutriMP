package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.Equation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Interface définissant les opérations disponibles pour la gestion des équations */
interface EquationRepository {
    /**
     * Récupère toutes les équations
     * @return Une liste de toutes les équations
     */
    suspend fun getAllEquations(): List<Equation>

    /**
     * Récupère toutes les équations sous forme de Flow
     * @return Un Flow de liste d'équations
     */
    fun observeAllEquations(): Flow<List<Equation>>

    /**
     * Récupère une équation par son UUID
     * @param uuid L'UUID de l'équation à récupérer
     * @return L'équation si trouvée, null sinon
     */
    suspend fun getEquationById(uuid: String): Equation?

    /**
     * Sauvegarde une équation
     * @param equation L'équation à sauvegarder
     */
    suspend fun saveEquation(equation: Equation)

    /**
     * Met à jour une équation existante
     * @param equation L'équation à mettre à jour
     */
    suspend fun updateEquation(equation: Equation)

    /**
     * Supprime une équation
     * @param uuid L'UUID de l'équation à supprimer
     */
    suspend fun deleteEquation(uuid: String)
}

/** Implémentation en mémoire du repository des équations */
class InMemoryEquationRepository : EquationRepository {
    private val _equations = MutableStateFlow<List<Equation>>(emptyList())

    override suspend fun getAllEquations(): List<Equation> {
        return _equations.value
    }

    override fun observeAllEquations(): Flow<List<Equation>> {
        return _equations.asStateFlow()
    }

    override suspend fun getEquationById(uuid: String): Equation? {
        return _equations.value.find { it.uuid == uuid }
    }

    override suspend fun saveEquation(equation: Equation) {
        // Création d'une nouvelle liste pour déclencher la réactivité
        val newList = _equations.value.toMutableList()
        newList.add(equation)
        _equations.value = newList
    }

    override suspend fun updateEquation(equation: Equation) {
        _equations.update { currentList ->
            currentList.map { if (it.uuid == equation.uuid) equation else it }
        }
    }

    override suspend fun deleteEquation(uuid: String) {
        _equations.update { currentList -> currentList.filter { it.uuid != uuid } }
    }
}
