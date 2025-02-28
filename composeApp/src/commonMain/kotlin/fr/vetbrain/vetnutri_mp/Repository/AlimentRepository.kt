package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.Nutriment
import fr.vetbrain.vetnutri_mp.DataBase.*
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toData
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toEntity
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toNutrientValueEntities
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlinx.coroutines.withContext

interface AlimentRationRepository {
    suspend fun saveAlimentRation(alimentRation: AlimentRation)
    suspend fun getAlimentRationsForRation(rationId: String): List<AlimentRation>
    suspend fun getAlimentRationById(id: String): AlimentRation?
    suspend fun deleteAlimentRation(alimentRation: AlimentRation)
}

class DatabaseAlimentRationRepository(private val alimentRationDao: AlimentRationDao) :
        AlimentRationRepository {
    override suspend fun saveAlimentRation(alimentRation: AlimentRation) {
        withContext(AppDispatchers.Default) {
            val entity = alimentRation.toEntity()
            if (getAlimentRationById(alimentRation.uuid) != null) {
                alimentRationDao.update(entity)
            } else {
                alimentRationDao.insert(entity)
            }
        }
    }

    override suspend fun getAlimentRationsForRation(rationId: String): List<AlimentRation> {
        return withContext(AppDispatchers.Default) {
            alimentRationDao.getAlimentRationsForRation(rationId).map { it.toData() }
        }
    }

    override suspend fun getAlimentRationById(id: String): AlimentRation? {
        return withContext(AppDispatchers.Default) {
            alimentRationDao.getAlimentRationById(id)?.toData()
        }
    }

    override suspend fun deleteAlimentRation(alimentRation: AlimentRation) {
        withContext(AppDispatchers.Default) { alimentRationDao.delete(alimentRation.toEntity()) }
    }
}

interface AlimentRepository {
    suspend fun saveAliment(aliment: AlimentEv)
    suspend fun getAllAliments(): List<AlimentEv>
    suspend fun getAlimentById(id: String): AlimentEv?
    suspend fun getAlimentsByType(typeAliment: Int): List<AlimentEv>
    suspend fun getAlimentsByGroup(groupId: Int): List<AlimentEv>
    suspend fun getNonDeprecatedAliments(): List<AlimentEv>
    suspend fun deleteAliment(aliment: AlimentEv)
}

class DatabaseAlimentRepository(
        private val alimentBaseDao: AlimentBaseDao,
        private val nutrientValueDao: NutrientValueDao
) : AlimentRepository {
    override suspend fun saveAliment(aliment: AlimentEv) {
        withContext(AppDispatchers.Default) {
            // Convertir l'aliment en entité
            val entity = aliment.toEntity()

            // Vérifier si l'aliment existe déjà
            val existingAliment = getAlimentById(aliment.uuid)

            if (existingAliment != null) {
                // Mettre à jour l'aliment
                alimentBaseDao.update(entity)

                // Supprimer les anciennes valeurs nutritionnelles
                nutrientValueDao.deleteNutrientValuesForAliment(aliment.uuid)

                // Supprimer les anciennes espèces
                alimentBaseDao.deleteEspecesForAliment(aliment.uuid)

                // Supprimer les anciennes indications
                alimentBaseDao.deleteIndicationsForAliment(aliment.uuid)
            } else {
                // Insérer le nouvel aliment
                alimentBaseDao.insert(entity)
            }

            // Insérer les nouvelles valeurs nutritionnelles
            val nutrientValues = aliment.toNutrientValueEntities()
            nutrientValueDao.insertAll(nutrientValues)

            // Insérer les espèces
            aliment.especes.forEach { espece ->
                alimentBaseDao.insertEspeceAliment(
                        EspeceAlimentEntity(refAliment = aliment.uuid, espece = espece)
                )
            }

            // Insérer les indications
            aliment.indication.forEach { indication ->
                try {
                    val indicValue = indication.toInt()
                    alimentBaseDao.insertIndicationAliment(
                            IndicationAlimentEntity(
                                    refAliment = aliment.uuid,
                                    indication = indicValue
                            )
                    )
                } catch (e: NumberFormatException) {
                    // Ignorer les indications qui ne sont pas des nombres
                }
            }
        }
    }

    override suspend fun getAllAliments(): List<AlimentEv> {
        return withContext(AppDispatchers.Default) {
            val aliments = alimentBaseDao.getAllAliments()
            aliments.map { entity ->
                val especeEntities = alimentBaseDao.getEspecesForAliment(entity.uuid)
                val indicationEntities = alimentBaseDao.getIndicationsForAliment(entity.uuid)
                val nutrientValues = nutrientValueDao.getNutrientValuesForAliment(entity.uuid)
                entity.toData(especeEntities, indicationEntities, nutrientValues)
            }
        }
    }

    override suspend fun getAlimentById(id: String): AlimentEv? {
        return withContext(AppDispatchers.Default) {
            val entity = alimentBaseDao.getAlimentById(id) ?: return@withContext null
            val especeEntities = alimentBaseDao.getEspecesForAliment(entity.uuid)
            val indicationEntities = alimentBaseDao.getIndicationsForAliment(entity.uuid)
            val nutrientValues = nutrientValueDao.getNutrientValuesForAliment(entity.uuid)
            entity.toData(especeEntities, indicationEntities, nutrientValues)
        }
    }

    override suspend fun getAlimentsByType(typeAliment: Int): List<AlimentEv> {
        return withContext(AppDispatchers.Default) {
            val aliments = alimentBaseDao.getAlimentsByType(typeAliment)
            aliments.map { entity ->
                val especeEntities = alimentBaseDao.getEspecesForAliment(entity.uuid)
                val indicationEntities = alimentBaseDao.getIndicationsForAliment(entity.uuid)
                val nutrientValues = nutrientValueDao.getNutrientValuesForAliment(entity.uuid)
                entity.toData(especeEntities, indicationEntities, nutrientValues)
            }
        }
    }

    override suspend fun getAlimentsByGroup(groupId: Int): List<AlimentEv> {
        return withContext(AppDispatchers.Default) {
            val aliments = alimentBaseDao.getAlimentsByGroup(groupId)
            aliments.map { entity ->
                val especeEntities = alimentBaseDao.getEspecesForAliment(entity.uuid)
                val indicationEntities = alimentBaseDao.getIndicationsForAliment(entity.uuid)
                val nutrientValues = nutrientValueDao.getNutrientValuesForAliment(entity.uuid)
                entity.toData(especeEntities, indicationEntities, nutrientValues)
            }
        }
    }

    override suspend fun getNonDeprecatedAliments(): List<AlimentEv> {
        return withContext(AppDispatchers.Default) {
            val aliments = alimentBaseDao.getNonDeprecatedAliments()
            aliments.map { entity ->
                val especeEntities = alimentBaseDao.getEspecesForAliment(entity.uuid)
                val indicationEntities = alimentBaseDao.getIndicationsForAliment(entity.uuid)
                val nutrientValues = nutrientValueDao.getNutrientValuesForAliment(entity.uuid)
                entity.toData(especeEntities, indicationEntities, nutrientValues)
            }
        }
    }

    override suspend fun deleteAliment(aliment: AlimentEv) {
        withContext(AppDispatchers.Default) {
            // Supprimer l'aliment (les relations seront supprimées en cascade)
            alimentBaseDao.delete(aliment.toEntity())
        }
    }
}

interface NutrimentRepository {
    suspend fun saveNutriment(nutriment: Nutriment)
    suspend fun getAllNutriments(): List<Nutriment>
    suspend fun getNutrimentById(id: String): Nutriment?
    suspend fun deleteNutriment(nutriment: Nutriment)
    suspend fun getNutrimentsForAliment(alimentId: String): List<Nutriment>
}

class DatabaseNutrimentRepository(
        private val nutrimentDao: NutrimentDao,
        private val alimentNutrimentDao: AlimentNutrimentDao
) : NutrimentRepository {
    override suspend fun saveNutriment(nutriment: Nutriment) {
        withContext(AppDispatchers.Default) {
            val entity = nutriment.toEntity()
            if (getNutrimentById(nutriment.uuid) != null) {
                nutrimentDao.update(entity)
            } else {
                nutrimentDao.insert(entity)
            }
        }
    }

    override suspend fun getAllNutriments(): List<Nutriment> {
        return withContext(AppDispatchers.Default) {
            nutrimentDao.getAllNutriments().map { it.toData() }
        }
    }

    override suspend fun getNutrimentById(id: String): Nutriment? {
        return withContext(AppDispatchers.Default) { nutrimentDao.getNutrimentById(id)?.toData() }
    }

    override suspend fun deleteNutriment(nutriment: Nutriment) {
        withContext(AppDispatchers.Default) { nutrimentDao.delete(nutriment.toEntity()) }
    }

    override suspend fun getNutrimentsForAliment(alimentId: String): List<Nutriment> {
        return withContext(AppDispatchers.Default) {
            val alimentNutriments = alimentNutrimentDao.getNutrimentsForAliment(alimentId)
            alimentNutriments.mapNotNull { an ->
                nutrimentDao.getNutrimentById(an.refNutriment)?.toData()
            }
        }
    }
}
