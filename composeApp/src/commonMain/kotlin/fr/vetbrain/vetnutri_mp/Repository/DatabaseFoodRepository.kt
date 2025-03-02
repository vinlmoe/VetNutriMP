package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentEvJson
import fr.vetbrain.vetnutri_mp.DataBase.EspeceAlimentEntity
import fr.vetbrain.vetnutri_mp.DataBase.FoodDao
import fr.vetbrain.vetnutri_mp.DataBase.FoodEntity
import fr.vetbrain.vetnutri_mp.DataBase.IndicationAlimentEntity
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toAlimentEntity
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toAlimentEv
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toNutrientValueEntities
import fr.vetbrain.vetnutri_mp.DataBase.NutrientValueDao
import fr.vetbrain.vetnutri_mp.Enumer.FoodKind
import fr.vetbrain.vetnutri_mp.Enumer.GroupAlim
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

/**
 * Implémentation de FoodRepository utilisant une base de données SQLite. Cette classe gère les
 * opérations CRUD pour les aliments.
 */
class DatabaseFoodRepository(
        private val foodDao: FoodDao,
        private val nutrientValueDao: NutrientValueDao
) : FoodRepository {
    /**
     * Insère un aliment basique (sans propriétés associées) Cette méthode est maintenue pour la
     * compatibilité avec l'interface
     */
    override suspend fun insert(food: AlimentEv) {
        withContext(AppDispatchers.IO) {
            // Déléguer à la méthode complète
            insertFood(food)
        }
    }

    /**
     * Met à jour un aliment basique (sans propriétés associées) Cette méthode est maintenue pour la
     * compatibilité avec l'interface
     */
    override suspend fun update(food: AlimentEv) {
        withContext(AppDispatchers.IO) {
            // Déléguer à la méthode complète
            updateFood(food)
        }
    }

    /**
     * Supprime un aliment basique (sans propriétés associées) Cette méthode est maintenue pour la
     * compatibilité avec l'interface
     */
    override suspend fun delete(food: AlimentEv) {
        withContext(AppDispatchers.IO) {
            // Déléguer à la méthode complète
            deleteFood(food.uuid)
        }
    }

    override suspend fun getAllFoods(): List<AlimentEv> {
        return withContext(AppDispatchers.IO) {
            foodDao.findAll().map { entity -> entity.toDomain() }
        }
    }

    override suspend fun getFoodById(id: String): AlimentEv? {
        return withContext(AppDispatchers.IO) {
            val entity = foodDao.getFoodById(id) ?: return@withContext null
            entity.toDomain()
        }
    }

    override fun observeAllFoods(): Flow<List<AlimentEv>> {
        return flow { emit(getAllFoods()) }
    }

    /**
     * Importe une liste d'aliments et les insère dans la base de données
     * @param foods Liste des aliments à importer
     * @return Nombre d'aliments importés avec succès
     */
    override suspend fun importFoods(foods: List<AlimentEvJson>): Int =
            withContext(AppDispatchers.IO) {
                var importedCount = 0
                var updatedCount = 0
                var errors = 0

                // Récupérer la liste des aliments existants par uuid pour éviter les duplicatas
                val existingFoods = foodDao.findAll().associateBy { it.uuid }

                println(
                        "Début de l'importation de ${foods.size} aliments. ${existingFoods.size} aliments déjà en base"
                )

                for (food in foods) {
                    try {
                        val foodEntity = food.toFoodEntity()

                        // Vérifier si l'aliment existe déjà
                        if (existingFoods.containsKey(foodEntity.uuid)) {
                            // Mettre à jour l'aliment existant
                            foodDao.update(foodEntity)
                            updatedCount++
                            println("Aliment mis à jour: ${food.nom} (uuid: ${food.UUID})")
                        } else {
                            // Insérer le nouvel aliment
                            foodDao.insert(foodEntity)
                            importedCount++
                            println("Nouvel aliment importé: ${food.nom} (uuid: ${food.UUID})")
                        }
                    } catch (e: Exception) {
                        println(
                                "Erreur lors de l'importation de l'aliment ${food.nom}: ${e.message}"
                        )
                        errors++
                    }
                }

                println("Importation terminée:")
                println("- Nombre d'aliments importés: $importedCount")
                println("- Nombre d'aliments mis à jour: $updatedCount")
                println("- Nombre d'erreurs: $errors")

                // Retourner le nombre total d'aliments traités avec succès
                importedCount + updatedCount
            }

    /** Insère un aliment avec toutes ses propriétés associées */
    override suspend fun insertFood(food: AlimentEv) {
        withContext(AppDispatchers.IO) {
            foodDao.insertFood(food.toAlimentEntity())

            // Insertion des espèces
            val especeEntities =
                    food.especes.map { espece ->
                        EspeceAlimentEntity(refAliment = food.uuid, espece = espece)
                    }
            foodDao.insertEspeces(especeEntities)

            // Insertion des indications
            val indicationEntities =
                    food.indicat.map { indic ->
                        IndicationAlimentEntity(refAliment = food.uuid, indication = indic.ordinal)
                    }
            foodDao.insertIndications(indicationEntities)

            // Insertion des valeurs de nutriments
            val nutrientValueEntities = food.valMap.toNutrientValueEntities(food.uuid)
            nutrientValueDao.insertNutrientValues(nutrientValueEntities)
        }
    }

    override suspend fun getFood(uuid: String): AlimentEv? {
        return withContext(AppDispatchers.IO) {
            val alimentEntity = foodDao.getFood(uuid) ?: return@withContext null
            val especes = foodDao.getEspecesForAliment(uuid)
            val indications = foodDao.getIndicationsForAliment(uuid)
            val nutrientValues = nutrientValueDao.getNutrientValues(uuid)

            alimentEntity.toAlimentEv(especes, indications, nutrientValues)
        }
    }

    override suspend fun deleteFood(uuid: String) {
        withContext(AppDispatchers.IO) {
            nutrientValueDao.deleteAllNutrientValuesForAliment(uuid)
            foodDao.deleteFood(uuid)
        }
    }

    override suspend fun updateFood(food: AlimentEv) {
        withContext(AppDispatchers.IO) {
            foodDao.updateFood(food.toAlimentEntity())

            // Mise à jour des espèces (suppression puis insertion)
            foodDao.deleteEspecesForAliment(food.uuid)
            val especeEntities =
                    food.especes.map { espece ->
                        EspeceAlimentEntity(refAliment = food.uuid, espece = espece)
                    }
            foodDao.insertEspeces(especeEntities)

            // Mise à jour des indications (suppression puis insertion)
            foodDao.deleteIndicationsForAliment(food.uuid)
            val indicationEntities =
                    food.indicat.map { indic ->
                        IndicationAlimentEntity(refAliment = food.uuid, indication = indic.ordinal)
                    }
            foodDao.insertIndications(indicationEntities)

            // Mise à jour des valeurs de nutriments (suppression puis insertion)
            nutrientValueDao.deleteAllNutrientValuesForAliment(food.uuid)
            val nutrientValueEntities = food.valMap.toNutrientValueEntities(food.uuid)
            nutrientValueDao.insertNutrientValues(nutrientValueEntities)
        }
    }

    // Extension pour convertir FoodEntity en AlimentEv
    private fun FoodEntity.toDomain(): AlimentEv {
        return AlimentEv(
                uuid = this.uuid,
                nom = this.nameDef,
                group =
                        try {
                            GroupAlim.entries[this.groupAlim]
                        } catch (e: Exception) {
                            null
                        },
                typeAliment =
                        try {
                            FoodKind.entries[this.typeAlim]
                        } catch (e: Exception) {
                            null
                        },
                ingredients = this.ingredients,
                price = this.price,
                categPrice = this.categPrice,
                brand = this.brand,
                gamme = this.gamme,
                consistent = this.consistent != 0,
                cont = this.consistent,
                quantInt = this.quantityPres,
                deprecated = this.deprecated,
                dataB = this.DataB,
                rationUUID = null
        )
    }

    // Extension pour convertir AlimentEvJson en FoodEntity
    private fun AlimentEvJson.toFoodEntity(): FoodEntity {
        return FoodEntity(
                uuid = this.UUID,
                groupAlim =
                        try {
                            GroupAlim.valueOf(this.group).ordinal
                        } catch (e: Exception) {
                            println("Erreur de conversion du groupe '${this.group}': ${e.message}")
                            0
                        },
                typeAlim =
                        try {
                            FoodKind.valueOf(this.foodKind).ordinal
                        } catch (e: Exception) {
                            println("Erreur de conversion du type '${this.foodKind}': ${e.message}")
                            0
                        },
                ingredients = this.ingredients,
                price = this.prix,
                categPrice = this.categoriePrix,
                brand = this.marque,
                gamme = this.gamme,
                unitPres = 0, // Valeur par défaut
                quantityPres = this.quantInt,
                version = 1, // Valeur par défaut
                date = "", // Valeur par défaut
                nameDef = this.nom,
                consistent = if (this.cont == "YES") 1 else 0,
                deprecated = if (this.deprecated) 1 else 0,
                DataB = this.DataB
        )
    }

    // Réintroduit la méthode de conversion AlimentEv -> FoodEntity pour compatibilité
    private fun AlimentEv.toEntity(): FoodEntity {
        return FoodEntity(
                uuid = this.uuid,
                groupAlim = this.group?.ordinal ?: 0,
                typeAlim = this.typeAliment?.ordinal ?: 0,
                ingredients = this.ingredients ?: "",
                price = this.price ?: 0.0,
                categPrice = this.categPrice ?: "",
                brand = this.brand ?: "",
                gamme = this.gamme ?: "",
                unitPres = 0, // Valeur par défaut
                quantityPres = this.quantInt ?: 0f,
                version = 1, // Valeur par défaut
                date = "", // Valeur par défaut
                nameDef = this.nom ?: "",
                consistent = this.cont ?: 0,
                deprecated = this.deprecated ?: 0,
                DataB = this.dataB ?: ""
        )
    }
}
