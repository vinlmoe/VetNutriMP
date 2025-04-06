package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Interface définissant les opérations possibles sur les références bibliographiques. */
interface BiblioRefRepository {
    suspend fun getBiblioRefById(uuid: String): BiblioRef?
    fun getAllBiblioRefs(): Flow<List<BiblioRef>>
    suspend fun insertBiblioRef(biblioRef: BiblioRef)
    suspend fun updateBiblioRef(biblioRef: BiblioRef)
    suspend fun deleteBiblioRef(biblioRef: BiblioRef)
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
}
