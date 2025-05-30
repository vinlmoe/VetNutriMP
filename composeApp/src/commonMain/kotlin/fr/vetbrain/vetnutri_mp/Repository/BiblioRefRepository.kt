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
import kotlinx.coroutines.launch
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
        println("DEBUG InMemoryRepo: Recherche de référence par ID: $uuid")
        return _biblioRefs.value.find { it.uuid == uuid }
    }

    override fun getAllBiblioRefs(): Flow<List<BiblioRef>> {
        println(
                "DEBUG InMemoryRepo: getAllBiblioRefs() appelé - ${_biblioRefs.value.size} références disponibles"
        )
        return _biblioRefs.asStateFlow()
    }

    override suspend fun insertBiblioRef(biblioRef: BiblioRef) {
        println("DEBUG InMemoryRepo: Insertion de référence: ${biblioRef.firstAuthor}")

        // Créer une nouvelle liste avec la référence ajoutée ou mise à jour
        val newList = _biblioRefs.value.toMutableList()
        val existingIndex = newList.indexOfFirst { it.uuid == biblioRef.uuid }

        if (existingIndex >= 0) {
            newList[existingIndex] = biblioRef
        } else {
            newList.add(biblioRef)
        }

        // Mettre à jour le StateFlow avec la nouvelle liste
        _biblioRefs.value = newList

        println(
                "DEBUG InMemoryRepo: Référence ajoutée/mise à jour: ${biblioRef.firstAuthor}. Total: ${_biblioRefs.value.size} références"
        )
    }

    override suspend fun updateBiblioRef(biblioRef: BiblioRef) {
        println("DEBUG InMemoryRepo: Mise à jour de référence: ${biblioRef.firstAuthor}")
        insertBiblioRef(biblioRef)
    }

    override suspend fun deleteBiblioRef(biblioRef: BiblioRef) {
        println("DEBUG InMemoryRepo: Suppression de référence: ${biblioRef.firstAuthor}")
        _biblioRefs.value = _biblioRefs.value.filter { it.uuid != biblioRef.uuid }
        println(
                "DEBUG InMemoryRepo: Référence supprimée. Total: ${_biblioRefs.value.size} références"
        )
    }

    override suspend fun clearAllBiblioRefs(): Int {
        val count = _biblioRefs.value.size
        _biblioRefs.value = emptyList()
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
        println("DEBUG TestRepo: Recherche de référence par ID: $uuid")
        return _biblioRefs.value.find { it.uuid == uuid }
    }

    override fun getAllBiblioRefs(): Flow<List<BiblioRef>> {
        println(
                "DEBUG TestRepo: getAllBiblioRefs() appelé - ${_biblioRefs.value.size} références disponibles"
        )
        return _biblioRefs.asStateFlow()
    }

    override suspend fun insertBiblioRef(biblioRef: BiblioRef) {
        println("DEBUG TestRepo: Insertion de référence: ${biblioRef.firstAuthor}")

        // Créer une nouvelle liste avec la référence ajoutée ou mise à jour
        val newList = _biblioRefs.value.toMutableList()
        val existingIndex = newList.indexOfFirst { it.uuid == biblioRef.uuid }

        if (existingIndex >= 0) {
            newList[existingIndex] = biblioRef
        } else {
            newList.add(biblioRef)
        }

        // Mettre à jour le StateFlow avec la nouvelle liste
        _biblioRefs.value = newList

        println(
                "DEBUG TestRepo: Référence ajoutée/mise à jour: ${biblioRef.firstAuthor}. Total: ${_biblioRefs.value.size} références"
        )
    }

    override suspend fun updateBiblioRef(biblioRef: BiblioRef) {
        println("DEBUG TestRepo: Mise à jour de référence: ${biblioRef.firstAuthor}")
        insertBiblioRef(biblioRef)
    }

    override suspend fun deleteBiblioRef(biblioRef: BiblioRef) {
        println("DEBUG TestRepo: Suppression de référence: ${biblioRef.firstAuthor}")
        _biblioRefs.value = _biblioRefs.value.filter { it.uuid != biblioRef.uuid }
        println("DEBUG TestRepo: Référence supprimée. Total: ${_biblioRefs.value.size} références")
    }

    override suspend fun clearAllBiblioRefs(): Int {
        val count = _biblioRefs.value.size
        _biblioRefs.value = emptyList()
        return count
    }
}

/**
 * Implémentation du repository qui utilise la base de données Room pour persister les références
 * bibliographiques
 */
class DatabaseBiblioRefRepository(private val biblioRefDao: BiblioRefDao) : BiblioRefRepository {
    private val _biblioRefs = MutableStateFlow<List<BiblioRef>>(emptyList())

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
            println(
                    "DEBUG DatabaseBiblioRefRepo: SQL exécuté, ${allRefs.size} références trouvées en base"
            )

            // Convertir en objets du domaine
            val dbRefs = allRefs.map { it.toDomain() }
            _biblioRefs.value = dbRefs

            println(
                    "DEBUG DatabaseBiblioRefRepo: ${dbRefs.size} références chargées depuis la base de données"
            )
            if (dbRefs.isNotEmpty()) {
                println(
                        "DEBUG DatabaseBiblioRefRepo: Première référence: ${dbRefs[0].firstAuthor}, ${dbRefs[0].year}"
                )
            }
        } catch (e: Exception) {
            println("DEBUG DatabaseBiblioRefRepo: ERREUR lors du rafraîchissement: ${e.message}")
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
        println(
                "DEBUG DatabaseBiblioRefRepo: Récupération de toutes les références bibliographiques"
        )

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

                println(
                        "DEBUG DatabaseBiblioRefRepo: ${dbRefs.size} références chargées depuis la base de données"
                )

                // Mettre à jour le cache et émettre les nouvelles données
                _biblioRefs.value = dbRefs
                emit(dbRefs)
            } catch (e: Exception) {
                println(
                        "DEBUG DatabaseBiblioRefRepo: Erreur lors du chargement des références: ${e.message}"
                )
                // En cas d'erreur, on n'émet rien de plus (les données du cache ont déjà été
                // émises)
            }
        }
    }

    override suspend fun insertBiblioRef(biblioRef: BiblioRef) {
        println("DEBUG DatabaseBiblioRefRepo: Mise à jour de référence: ${biblioRef.firstAuthor}")
        println("DEBUG DatabaseBiblioRefRepo: UUID: ${biblioRef.uuid}")
        println("DEBUG DatabaseBiblioRefRepo: Année: ${biblioRef.year}")
        println("DEBUG DatabaseBiblioRefRepo: Référence complète: ${biblioRef.completeRef}")
        println("DEBUG DatabaseBiblioRefRepo: Commentaires: ${biblioRef.comments}")
        println("DEBUG DatabaseBiblioRefRepo: BibTeX: ${biblioRef.bibtex}")
        println("DEBUG DatabaseBiblioRefRepo: Cohérence originale: ${biblioRef.consistent}")

        try {
            // S'assurer que le champ consistent a une valeur valide (au moins 1)
            val safeRef =
                    if (biblioRef.consistent <= 0) {
                        println(
                                "DEBUG DatabaseBiblioRefRepo: Correction du champ consistent (0 -> 1)"
                        )
                        biblioRef.copy(consistent = 1)
                    } else {
                        biblioRef
                    }

            // Conversion en entité avec la référence sécurisée
            val entity = safeRef.toEntity()
            println("DEBUG DatabaseBiblioRefRepo: Entité créée avec succès: ${entity.firstAuthor}")
            println("DEBUG DatabaseBiblioRefRepo: Entité consistent: ${entity.consistent}")

            // Vérifier si la référence existe déjà
            val existingRef = biblioRefDao.getBiblioRefById(safeRef.uuid)
            println(
                    "DEBUG DatabaseBiblioRefRepo: Vérification si la référence existe déjà: ${existingRef != null}"
            )

            // Insérer dans la base de données
            if (existingRef != null) {
                // Si elle existe déjà, mettre à jour
                println("DEBUG DatabaseBiblioRefRepo: Référence existante, mise à jour")
                biblioRefDao.updateBiblioRef(entity)
                println("DEBUG DatabaseBiblioRefRepo: Mise à jour réussie dans la base de données")
            } else {
                // Sinon, insérer une nouvelle référence
                println("DEBUG DatabaseBiblioRefRepo: Nouvelle référence, insertion")
                biblioRefDao.insertBiblioRef(entity)
                println("DEBUG DatabaseBiblioRefRepo: Insertion réussie dans la base de données")
            }

            // Vérifier si l'insertion a fonctionné
            val verifyRef = biblioRefDao.getBiblioRefById(safeRef.uuid)
            println(
                    "DEBUG DatabaseBiblioRefRepo: Vérification post-insertion: ${verifyRef != null}"
            )
            if (verifyRef != null) {
                println(
                        "DEBUG DatabaseBiblioRefRepo: Référence vérifiée en base: ${verifyRef.firstAuthor}, ${verifyRef.year}"
                )
            }

            // Lister toutes les références après l'insertion
            val allRefs = biblioRefDao.getAllBiblioRefs()
            println(
                    "DEBUG DatabaseBiblioRefRepo: Après insertion, base contient ${allRefs.size} références:"
            )
            allRefs.forEach {
                println(
                        "DEBUG DatabaseBiblioRefRepo: - Référence: ${it.firstAuthor} (${it.year}), UUID: ${it.uuid}"
                )
            }

            // Rafraîchir la liste en mémoire
            refreshFromDatabase()
        } catch (e: Exception) {
            println("DEBUG DatabaseBiblioRefRepo: ERREUR lors de l'insertion: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun updateBiblioRef(biblioRef: BiblioRef) {
        println("DEBUG DatabaseBiblioRefRepo: Mise à jour de référence: ${biblioRef.firstAuthor}")

        try {
            // Mettre à jour dans la base de données
            biblioRefDao.updateBiblioRef(biblioRef.toEntity())
            println("DEBUG DatabaseBiblioRefRepo: Mise à jour réussie dans la base de données")

            // Rafraîchir la liste en mémoire
            refreshFromDatabase()
        } catch (e: Exception) {
            println("DEBUG DatabaseBiblioRefRepo: ERREUR lors de la mise à jour: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun deleteBiblioRef(biblioRef: BiblioRef) {
        println("DEBUG DatabaseBiblioRefRepo: Suppression de référence: ${biblioRef.firstAuthor}")

        try {
            // Supprimer de la base de données
            biblioRefDao.deleteBiblioRef(biblioRef.toEntity())
            println("DEBUG DatabaseBiblioRefRepo: Suppression réussie dans la base de données")

            // Rafraîchir la liste en mémoire
            refreshFromDatabase()
        } catch (e: Exception) {
            println("DEBUG DatabaseBiblioRefRepo: ERREUR lors de la suppression: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun clearAllBiblioRefs(): Int {
        println("DEBUG DatabaseBiblioRefRepository: clearAllBiblioRefs() démarrée")

        return try {
            // Obtenir le nombre total de références bibliographiques avant suppression directement
            // depuis la base
            println(
                    "DEBUG DatabaseBiblioRefRepository: Récupération de toutes les références biblio..."
            )
            val allBiblioRefEntities = biblioRefDao.getAllBiblioRefs()
            val count = allBiblioRefEntities.size
            println(
                    "DEBUG DatabaseBiblioRefRepository: $count références biblio trouvées dans la base"
            )

            if (count > 0) {
                println(
                        "DEBUG DatabaseBiblioRefRepository: Suppression de toutes les références biblio..."
                )
                // Supprimer toutes les références bibliographiques
                biblioRefDao.deleteAllBiblioRefs()
                println("DEBUG DatabaseBiblioRefRepository: Suppression terminée")

                // Rafraîchir le cache local
                _biblioRefs.value = emptyList()
                println("DEBUG DatabaseBiblioRefRepository: Cache local vidé")
            }

            println(
                    "DEBUG DatabaseBiblioRefRepository: $count références bibliographiques supprimées avec succès"
            )
            count
        } catch (e: Exception) {
            println(
                    "DEBUG DatabaseBiblioRefRepository: ERREUR lors de la suppression: ${e.message}"
            )
            e.printStackTrace()
            throw e
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
        println("DEBUG DatabaseBiblioRefRepo: toEntity - UUID: $uuid")
        println("DEBUG DatabaseBiblioRefRepo: toEntity - Premier auteur: $firstAuthor")
        println("DEBUG DatabaseBiblioRefRepo: toEntity - Année: $year")
        println(
                "DEBUG DatabaseBiblioRefRepo: toEntity - Longueur référence complète: ${completeRef.length}"
        )
        println("DEBUG DatabaseBiblioRefRepo: toEntity - Longueur commentaires: ${comments.length}")
        println("DEBUG DatabaseBiblioRefRepo: toEntity - Longueur bibtex: ${bibtex.length}")
        println("DEBUG DatabaseBiblioRefRepo: toEntity - Valeur consistent: $consistent")

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
