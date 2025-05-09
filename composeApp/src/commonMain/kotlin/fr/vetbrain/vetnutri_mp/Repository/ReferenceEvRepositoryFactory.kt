package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.DataBase.AppDatabase
import fr.vetbrain.vetnutri_mp.DataBase.BiblioRefDao
import fr.vetbrain.vetnutri_mp.DataBase.ReferenceEvDao

/** Factory pour créer des instances de ReferenceEvRepository */
object ReferenceEvRepositoryFactory {
    /**
     * Crée un ReferenceEvRepository à partir d'une instance de base de données
     * @param database La base de données à utiliser
     * @return Une instance de ReferenceEvRepository
     */
    fun create(database: AppDatabase): ReferenceEvRepository {
        return DatabaseReferenceEvRepository(
                referenceEvDao = database.referenceEvDao(),
                biblioRefDao = database.biblioRefDao()
        )
    }

    /**
     * Crée un ReferenceEvRepository à partir de DAOs spécifiques
     * @param referenceEvDao Le DAO pour les références nutritionnelles
     * @param biblioRefDao Le DAO pour les références bibliographiques
     * @return Une instance de ReferenceEvRepository
     */
    fun create(referenceEvDao: ReferenceEvDao, biblioRefDao: BiblioRefDao): ReferenceEvRepository {
        return DatabaseReferenceEvRepository(
                referenceEvDao = referenceEvDao,
                biblioRefDao = biblioRefDao
        )
    }
}
