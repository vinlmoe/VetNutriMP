package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import fr.vetbrain.vetnutri_mp.DataBase.BiblioRefDao
import fr.vetbrain.vetnutri_mp.DataBase.BiblioRefEntity
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import fr.vetbrain.vetnutri_mp.Utils.genUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Interface définissant les opérations possibles sur les références bibliographiques. */
interface BiblioRefRepository {
    suspend fun getBiblioRefById(uuid: String): BiblioRef?
    fun getAllBiblioRefs(): Flow<List<BiblioRef>>
    suspend fun insertBiblioRef(biblioRef: BiblioRef)
    suspend fun updateBiblioRef(biblioRef: BiblioRef)
    suspend fun deleteBiblioRef(biblioRef: BiblioRef)

    /**
     * Vide entièrement la base de données des références bibliographiques
     * @return Le nombre de références bibliographiques supprimées
     */
    suspend fun clearAllBiblioRefs(): Int
}

/**
 * Implémentation en mémoire du repository des références bibliographiques. À utiliser en attendant
 * l'implémentation d'une persistance en base de données.
 */
class InMemoryBiblioRefRepository : BiblioRefRepository {
    private val _biblioRefs = MutableStateFlow<List<BiblioRef>>(emptyList())

    override suspend fun getBiblioRefById(uuid: String): BiblioRef? {
        return _biblioRefs.value.find { it.uuid == uuid }
    }

    override fun getAllBiblioRefs(): Flow<List<BiblioRef>> {
        return _biblioRefs.asStateFlow()
    }

    override suspend fun insertBiblioRef(biblioRef: BiblioRef) {
        _biblioRefs.update { current ->
            val idx = current.indexOfFirst { it.uuid == biblioRef.uuid }
            if (idx >= 0) current.toMutableList().also { it[idx] = biblioRef }
            else current + biblioRef
        }
    }

    override suspend fun updateBiblioRef(biblioRef: BiblioRef) {
        insertBiblioRef(biblioRef)
    }

    override suspend fun deleteBiblioRef(biblioRef: BiblioRef) {
        _biblioRefs.update { current -> current.filter { it.uuid != biblioRef.uuid } }
    }

    override suspend fun clearAllBiblioRefs(): Int {
        var count = 0
        _biblioRefs.update { current -> count = current.size; emptyList() }
        return count
    }
}

/** Implémentation en mémoire du repository avec des données de test */
class TestBiblioRefRepository : BiblioRefRepository {
    // Initialiser avec quelques références de test
    private val _biblioRefs =
            MutableStateFlow<List<BiblioRef>>(
                    listOf(
                            BiblioRef(
                                    uuid = "test-1",
                                    firstAuthor = "Dupont",
                                    year = 2020,
                                    completeRef = "Dupont et al., Etude sur les nutriments, 2020",
                                    comments = "Étude importante",
                                    consistent = 1
                            ),
                            BiblioRef(
                                    uuid = "test-2",
                                    firstAuthor = "Martin",
                                    year = 2021,
                                    completeRef = "Martin J., Nutrition canine, 2021",
                                    comments = "À vérifier",
                                    consistent = 1
                            )
                    )
            )

    override suspend fun getBiblioRefById(uuid: String): BiblioRef? {
        return _biblioRefs.value.find { it.uuid == uuid }
    }

    override fun getAllBiblioRefs(): Flow<List<BiblioRef>> {
        return _biblioRefs.asStateFlow()
    }

    override suspend fun insertBiblioRef(biblioRef: BiblioRef) {
        _biblioRefs.update { current ->
            val idx = current.indexOfFirst { it.uuid == biblioRef.uuid }
            if (idx >= 0) current.toMutableList().also { it[idx] = biblioRef }
            else current + biblioRef
        }
    }

    override suspend fun updateBiblioRef(biblioRef: BiblioRef) {
        insertBiblioRef(biblioRef)
    }

    override suspend fun deleteBiblioRef(biblioRef: BiblioRef) {
        _biblioRefs.update { current -> current.filter { it.uuid != biblioRef.uuid } }
    }

    override suspend fun clearAllBiblioRefs(): Int {
        var count = 0
        _biblioRefs.update { current -> count = current.size; emptyList() }
        return count
    }
}

/**
 * Implémentation du repository qui utilise la base de données Room pour persister les références
 * bibliographiques
 */
class DatabaseBiblioRefRepository(private val biblioRefDao: BiblioRefDao) : BiblioRefRepository {
    private val _biblioRefs = MutableStateFlow<List<BiblioRef>>(emptyList())
    private val writeMutex = Mutex()

    init {
        // Chargement initial des données depuis la base
        CoroutineScope(AppDispatchers.IO).launch {
            refreshFromDatabase()

            // Ajouter des références de test si la base est vide
            if (_biblioRefs.value.isEmpty()) {
                val testRefs =
                        listOf(
                                BiblioRef(
                                        uuid = "test-1",
                                        firstAuthor = "Dupont",
                                        year = 2020,
                                        completeRef =
                                                "Dupont et al., Etude sur les nutriments, 2020",
                                        comments = "Étude importante",
                                        consistent = 1
                                ),
                                BiblioRef(
                                        uuid = "test-2",
                                        firstAuthor = "Martin",
                                        year = 2021,
                                        completeRef = "Martin J., Nutrition canine, 2021",
                                        comments = "À vérifier",
                                        consistent = 1
                                )
                        )

                for (ref in testRefs) {
                    insertBiblioRef(ref)
                }
            }
        }
    }

    private suspend fun refreshFromDatabase() {
        try {
            // Récupérer toutes les références de la base de données
            val allRefs = biblioRefDao.getAllBiblioRefs()

            // Convertir en objets du domaine
            val dbRefs = allRefs.map { it.toDomain() }
            _biblioRefs.value = dbRefs

            if (dbRefs.isNotEmpty()) {
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun getBiblioRefById(uuid: String): BiblioRef? {
        // Essayer d'abord en mémoire pour des raisons de performance
        val inMemoryRef = _biblioRefs.value.find { it.uuid == uuid }
        if (inMemoryRef != null) return inMemoryRef

        // Sinon, aller chercher dans la base de données
        val dbRef = biblioRefDao.getBiblioRefById(uuid)
        return dbRef?.toDomain()
    }

    /**
     * Récupère toutes les références bibliographiques.
     * @return Un flux contenant la liste des références.
     */
    override fun getAllBiblioRefs(): Flow<List<BiblioRef>> {

        return flow {
            // Émettre d'abord les données en cache
            emit(_biblioRefs.value)

            try {
                // Charger les données de la base de données
                val dbRefs =
                        withContext(AppDispatchers.IO) {
                            val entities = biblioRefDao.getAllBiblioRefs()
                            entities.map { it.toDomain() }
                        }


                // Mettre à jour le cache et émettre les nouvelles données
                _biblioRefs.value = dbRefs
                emit(dbRefs)
            } catch (e: Exception) {
                // En cas d'erreur, on n'émet rien de plus (les données du cache ont déjà été
                // émises)
            }
        }
    }

    override suspend fun insertBiblioRef(biblioRef: BiblioRef) {
        withContext(AppDispatchers.IO) {
            writeMutex.withLock {
                try {
                    val safeRef = if (biblioRef.consistent <= 0) biblioRef.copy(consistent = 1) else biblioRef
                    val entity = safeRef.toEntity()
                    val existingRef = biblioRefDao.getBiblioRefById(safeRef.uuid)
                    if (existingRef != null) {
                        biblioRefDao.updateBiblioRef(entity)
                    } else {
                        biblioRefDao.insertBiblioRef(entity)
                    }
                    refreshFromDatabase()
                } catch (e: Exception) {
                    e.printStackTrace()
                    throw e
                }
            }
        }
    }

    override suspend fun updateBiblioRef(biblioRef: BiblioRef) {
        withContext(AppDispatchers.IO) {
            writeMutex.withLock {
                try {
                    biblioRefDao.updateBiblioRef(biblioRef.toEntity())
                    refreshFromDatabase()
                } catch (e: Exception) {
                    e.printStackTrace()
                    throw e
                }
            }
        }
    }

    override suspend fun deleteBiblioRef(biblioRef: BiblioRef) {
        withContext(AppDispatchers.IO) {
            writeMutex.withLock {
                try {
                    biblioRefDao.deleteBiblioRef(biblioRef.toEntity())
                    refreshFromDatabase()
                } catch (e: Exception) {
                    e.printStackTrace()
                    throw e
                }
            }
        }
    }

    override suspend fun clearAllBiblioRefs(): Int {
        return withContext(AppDispatchers.IO) {
            writeMutex.withLock {
                try {
                    val count = biblioRefDao.getAllBiblioRefs().size
                    if (count > 0) {
                        biblioRefDao.deleteAllBiblioRefs()
                        refreshFromDatabase()
                    }
                    count
                } catch (e: Exception) {
                    e.printStackTrace()
                    throw e
                }
            }
        }
    }

    /** Conversion d'une entité de base de données en objet du domaine */
    private fun BiblioRefEntity.toDomain(): BiblioRef {
        return BiblioRef(
                uuid = uuid,
                firstAuthor = firstAuthor,
                year = year,
                completeRef = completeRef,
                comments = comments,
                bibtex = bibtex,
                consistent = consistent
        )
    }

    /** Conversion d'un objet du domaine en entité de base de données */
    private fun BiblioRef.toEntity(): BiblioRefEntity {

        val validUuid = if (uuid.isBlank()) genUUID() else uuid
        val validFirstAuthor = if (firstAuthor.isBlank()) "Auteur inconnu" else firstAuthor
        val validYear = if (year <= 0) 2000 else year
        val validCompleteRef =
                if (completeRef.isBlank()) "Référence complète non disponible" else completeRef
        val validComments = comments // Déjà non-nullable
        val validBibtex = bibtex // Déjà non-nullable
        val validConsistent = if (consistent <= 0) 1 else consistent

        return BiblioRefEntity(
                uuid = validUuid,
                firstAuthor = validFirstAuthor,
                year = validYear,
                completeRef = validCompleteRef,
                comments = validComments,
                bibtex = validBibtex,
                consistent = validConsistent
        )
    }
}
