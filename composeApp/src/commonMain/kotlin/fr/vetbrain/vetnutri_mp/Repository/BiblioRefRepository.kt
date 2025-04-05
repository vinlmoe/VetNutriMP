package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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
        return _biblioRefs.value.find { it.uuid == uuid }
    }

    override fun getAllBiblioRefs(): Flow<List<BiblioRef>> {
        return _biblioRefs.asStateFlow()
    }

    override suspend fun insertBiblioRef(biblioRef: BiblioRef) {
        _biblioRefs.update { currentList ->
            if (currentList.any { it.uuid == biblioRef.uuid }) {
                currentList
            } else {
                currentList + biblioRef
            }
        }
    }

    override suspend fun updateBiblioRef(biblioRef: BiblioRef) {
        _biblioRefs.update { currentList ->
            currentList.map { if (it.uuid == biblioRef.uuid) biblioRef else it }
        }
    }

    override suspend fun deleteBiblioRef(biblioRef: BiblioRef) {
        _biblioRefs.update { currentList -> currentList.filter { it.uuid != biblioRef.uuid } }
    }
}
