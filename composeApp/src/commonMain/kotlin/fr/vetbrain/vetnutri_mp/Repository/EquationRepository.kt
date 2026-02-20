package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.Equation
import fr.vetbrain.vetnutri_mp.DataBase.BiblioRefDao
import fr.vetbrain.vetnutri_mp.DataBase.EquationDao
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toDomain
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toEntity
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

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

    /**
     * Supprime toutes les équations
     * @return Le nombre d'équations supprimées
     */
    suspend fun clearAllEquations(): Int
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

    override suspend fun clearAllEquations(): Int {
        val count = _equations.value.size
        _equations.value = emptyList()
        return count
    }
}

/** Implémentation qui utilise la base de données pour le repository des équations */
class DatabaseEquationRepository(
        private val equationDao: EquationDao,
        private val biblioRefDao: BiblioRefDao
) : EquationRepository {

    private val equationsFlow = MutableStateFlow<List<Equation>>(emptyList())
    private val scope = CoroutineScope(AppDispatchers.IO + SupervisorJob())

    init {
        // Retiré: insertion automatique d'équations de démonstration.
        scope.launch { loadEquations() }
    }

    private suspend fun loadEquations() {
        val equations =
                equationDao.getAllEquations().mapNotNull { entity ->
                    val biblioRef =
                            entity.bibRef?.let { biblioRefDao.getBiblioRefById(it)?.toDomain() }
                    entity.toDomain(biblioRef)
                }
        equationsFlow.value = equations
    }

    override suspend fun getAllEquations(): List<Equation> {
        loadEquations()
        return equationsFlow.value
    }

    override fun observeAllEquations(): Flow<List<Equation>> {
        return equationsFlow.asStateFlow()
    }

    override suspend fun getEquationById(uuid: String): Equation? {
        val entity = equationDao.getEquationById(uuid) ?: return null
        val biblioRef = entity.bibRef?.let { biblioRefDao.getBiblioRefById(it)?.toDomain() }
        return entity.toDomain(biblioRef)
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun saveEquation(equation: Equation) {

        try {
            // Si l'UUID est vide, créer une nouvelle équation avec un UUID généré
            val equationToSave =
                    if (equation.uuid.isEmpty()) {
                        equation.copy(uuid = kotlin.uuid.Uuid.random().toString())
                    } else {
                        equation
                    }

            val entity = equationToSave.toEntity()

            // Vérifier si l'équation existe déjà
            val existingEquation = equationDao.getEquationById(equationToSave.uuid)
            if (existingEquation != null) {
                // Équation existante : utiliser updateEquation
                equationDao.updateEquation(entity)
            } else {
                // Nouvelle équation : utiliser insertEquation
                equationDao.insertEquation(entity)
            }

            loadEquations()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun updateEquation(equation: Equation) {
        equationDao.updateEquation(equation.toEntity())
        loadEquations()
    }

    override suspend fun deleteEquation(uuid: String) {
        equationDao.deleteEquation(uuid)
    }

    override suspend fun clearAllEquations(): Int {
        

        return try {
            // Obtenir le nombre total d'équations avant suppression
            val allEquations = getAllEquations()
            val count = allEquations.size

            if (count > 0) {
                // Supprimer toutes les équations
                equationDao.deleteAllEquations()
            }

            count
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
