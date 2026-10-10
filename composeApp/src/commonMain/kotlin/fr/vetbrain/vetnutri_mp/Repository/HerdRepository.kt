package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.ContenuTroupeau
import fr.vetbrain.vetnutri_mp.Data.Troupeau
import fr.vetbrain.vetnutri_mp.DataBase.HerdDao
import fr.vetbrain.vetnutri_mp.DataBase.HerdEntity
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlinx.coroutines.withContext

/** Troupeaux / groupes d'animaux (types, consultations de groupe) enregistrés en JSON. */
interface HerdRepository {
    suspend fun getAllHerds(): List<Troupeau>
    suspend fun getHerdById(uuid: String): Troupeau?
    suspend fun saveHerd(troupeau: Troupeau)
    suspend fun deleteHerd(uuid: String)
}

class DatabaseHerdRepository(private val dao: HerdDao) : HerdRepository {

    override suspend fun getAllHerds(): List<Troupeau> =
            withContext(AppDispatchers.IO) { dao.getAll().map { it.toDomain() } }

    override suspend fun getHerdById(uuid: String): Troupeau? =
            withContext(AppDispatchers.IO) { dao.getById(uuid)?.toDomain() }

    override suspend fun saveHerd(troupeau: Troupeau) {
        withContext(AppDispatchers.IO) {
            dao.upsert(
                    HerdEntity(
                            uuid = troupeau.uuid,
                            nom = troupeau.nom,
                            espece = troupeau.espece,
                            contenuJson = troupeau.contenu.versJson(),
                            updatedAt = troupeau.updatedAt
                    )
            )
        }
    }

    override suspend fun deleteHerd(uuid: String) {
        withContext(AppDispatchers.IO) { dao.deleteById(uuid) }
    }

    private fun HerdEntity.toDomain() =
            Troupeau(
                    uuid = uuid,
                    nom = nom,
                    espece = espece,
                    contenu = ContenuTroupeau.depuisJson(contenuJson),
                    updatedAt = updatedAt
            )
}

/** Implémentation en mémoire (tests). */
class InMemoryHerdRepository : HerdRepository {
    private val troupeaux = mutableMapOf<String, Troupeau>()

    override suspend fun getAllHerds(): List<Troupeau> = troupeaux.values.sortedByDescending { it.updatedAt }

    override suspend fun getHerdById(uuid: String): Troupeau? = troupeaux[uuid]

    override suspend fun saveHerd(troupeau: Troupeau) {
        troupeaux[troupeau.uuid] = troupeau
    }

    override suspend fun deleteHerd(uuid: String) {
        troupeaux.remove(uuid)
    }
}
