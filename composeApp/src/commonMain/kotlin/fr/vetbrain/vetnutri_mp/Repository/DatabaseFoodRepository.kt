package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentEvJson
import fr.vetbrain.vetnutri_mp.DataBase.FoodDao
import fr.vetbrain.vetnutri_mp.DataBase.FoodEntity
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toAlimentEv
import fr.vetbrain.vetnutri_mp.DataBase.NutrientValueDao
import fr.vetbrain.vetnutri_mp.Enumer.AlimIndic
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.FoodKind
import fr.vetbrain.vetnutri_mp.Enumer.GroupAlim
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import fr.vetbrain.vetnutri_mp.Utils.ImportTester
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
            try {
                val foodEntities = foodDao.findAll()

                // Conversion des entités en objets de domaine
                val alimentEvList = foodEntities.map { foodEntity -> foodEntity.toAlimentEv() }

                return@withContext alimentEvList
            } catch (e: Exception) {
                println("Erreur lors de la récupération de tous les aliments: ${e.message}")
                return@withContext emptyList()
            }
        }
    }

    override suspend fun getFoodById(id: String): AlimentEv? {
        return withContext(AppDispatchers.IO) {
            try {
                val foodEntity = foodDao.getFoodById(id)
                if (foodEntity != null) {
                    // Conversion de l'entité en objet de domaine
                    val alimentEv = foodEntity.toAlimentEv()

                    // Nous n'avons plus besoin de récupérer les indications depuis la table
                    // d'association
                    // car elles sont maintenant stockées dans le champ indicationsJson

                    return@withContext alimentEv
                }
                return@withContext null
            } catch (e: Exception) {
                println("Erreur lors de la récupération de l'aliment par ID: ${e.message}")
                return@withContext null
            }
        }
    }

    override fun observeAllFoods(): Flow<List<AlimentEv>> {
        return flow {
            val entities = foodDao.getAllFoods()
            emit(entities.map { entity -> entity.toAlimentEv() })
        }
    }

    /** Importe une liste d'aliments JSON dans la base de données */
    override suspend fun importFoods(foods: List<AlimentEvJson>): Int {
        return withContext(AppDispatchers.IO) {
            var importCount = 0
            var updateCount = 0
            var deleteCount = 0

            // Collecter les UUIDs de tous les aliments présents dans le fichier JSON
            val importedUUIDs = foods.map { it.UUID }.toSet()

            // Récupérer tous les aliments existants dans la base de données
            val existingFoods = foodDao.getAllFoods()

            // Identifier les aliments à supprimer (existants mais pas dans la liste importée)
            val foodsToDelete = existingFoods.filter { !importedUUIDs.contains(it.uuid) }

            // Supprimer les aliments qui ne sont plus présents dans le fichier JSON
            foodsToDelete.forEach { food ->
                // Supprimer d'abord les valeurs nutritionnelles
                nutrientValueDao.deleteAllNutrientValuesForAliment(food.uuid)

                // Supprimer les indications associées
                foodDao.deleteIndicationsForAliment(food.uuid)

                // Supprimer l'aliment lui-même
                foodDao.deleteFood(food.uuid)

                deleteCount++
                println("Suppression de l'aliment: ${food.name ?: "Sans nom"} (${food.uuid})")
            }

            foods.forEach { food ->
                try {
                    val foodId = food.UUID

                    // Vérifier si l'aliment existe déjà dans la table FOOD
                    val existingInFood = foodDao.getFoodById(foodId)

                    // Convertir les espèces: si c'est un ID numérique, le transformer en label
                    val especesConverties = convertirEspecesEnLabels(food.Especes, food.espece)

                    if (existingInFood == null) {
                        // L'aliment n'existe pas dans FOOD, l'ajouter
                        // Conversion des espèces en JSON
                        val especesJson =
                                if (especesConverties.isNotEmpty()) {
                                    Json.encodeToString(especesConverties)
                                } else {
                                    null
                                }

                        // Conversion des indications en JSON
                        val indicationsConverties = convertirIndicationsEnLabels(food.indication)
                        val indicationsJson =
                                if (indicationsConverties.isNotEmpty()) {
                                    Json.encodeToString(indicationsConverties)
                                } else {
                                    null
                                }

                        // Créer le FoodEntity avec le JSON des espèces
                        val foodEntity =
                                FoodEntity(
                                        uuid = food.UUID,
                                        nameDef = food.nom ?: "",
                                        groupAlim =
                                                try {
                                                    GroupAlim.valueOf(food.group).ordinal
                                                } catch (e: Exception) {
                                                    0
                                                },
                                        typeAlim =
                                                try {
                                                    FoodKind.valueOf(food.foodKind).ordinal
                                                } catch (e: Exception) {
                                                    0
                                                },
                                        ingredients = food.ingredients ?: "",
                                        price = food.prix ?: 0.0,
                                        categPrice = food.categoriePrix ?: "",
                                        brand = food.marque ?: "",
                                        gamme = food.gamme ?: "",
                                        unitPres = 0,
                                        quantityPres = food.quantInt ?: 0f,
                                        version = 1,
                                        date = "",
                                        cont = food.cont ?: "NO",
                                        consistent = if (food.cont == "YES") 1 else 0,
                                        deprecated = if (food.deprecated == true) 1 else 0,
                                        DataB = food.DataB ?: "",
                                        RefRation = null,
                                        RefAlimUnif = null,
                                        especesJson = especesJson,
                                        indicationsJson = indicationsJson
                                )

                        println(
                                "Import de l'aliment dans FOOD: ${foodEntity.nameDef} (${foodEntity.uuid})"
                        )
                        foodDao.insert(foodEntity)
                        importCount++
                    } else {
                        // L'aliment existe déjà dans FOOD, le mettre à jour
                        // Déterminer le JSON des espèces à utiliser
                        val especesJson =
                                if (especesConverties.isNotEmpty()) {
                                    Json.encodeToString(especesConverties)
                                } else {
                                    existingInFood.especesJson // Garder les espèces existantes
                                }

                        // Déterminer le JSON des indications à utiliser
                        val indicationsConverties = convertirIndicationsEnLabels(food.indication)
                        val indicationsJson =
                                if (indicationsConverties.isNotEmpty()) {
                                    Json.encodeToString(indicationsConverties)
                                } else {
                                    existingInFood
                                            .indicationsJson // Garder les indications existantes
                                }

                        val foodEntity =
                                FoodEntity(
                                        uuid = food.UUID,
                                        nameDef = food.nom ?: existingInFood.nameDef,
                                        groupAlim =
                                                try {
                                                    GroupAlim.valueOf(food.group).ordinal
                                                } catch (e: Exception) {
                                                    existingInFood.groupAlim
                                                },
                                        typeAlim =
                                                try {
                                                    FoodKind.valueOf(food.foodKind).ordinal
                                                } catch (e: Exception) {
                                                    existingInFood.typeAlim
                                                },
                                        ingredients = food.ingredients
                                                        ?: existingInFood.ingredients,
                                        price = food.prix ?: existingInFood.price,
                                        categPrice = food.categoriePrix
                                                        ?: existingInFood.categPrice,
                                        brand = food.marque ?: existingInFood.brand,
                                        gamme = food.gamme ?: existingInFood.gamme,
                                        unitPres = existingInFood.unitPres,
                                        quantityPres = food.quantInt ?: existingInFood.quantityPres,
                                        version = existingInFood.version,
                                        date = existingInFood.date,
                                        cont = food.cont ?: existingInFood.cont,
                                        consistent = if (food.cont == "YES") 1 else 0,
                                        deprecated = if (food.deprecated == true) 1 else 0,
                                        DataB = food.DataB ?: existingInFood.DataB,
                                        RefRation = existingInFood.RefRation,
                                        RefAlimUnif = existingInFood.RefAlimUnif,
                                        especesJson = especesJson,
                                        indicationsJson = indicationsJson
                                )

                        println(
                                "Mise à jour de l'aliment dans FOOD: ${foodEntity.nameDef} (${foodEntity.uuid})"
                        )
                        foodDao.update(foodEntity)
                        updateCount++
                    }
                } catch (e: Exception) {
                    println("Erreur lors de l'import de l'aliment ${food.nom}: ${e.message}")
                    e.printStackTrace()
                }
            }

            println(
                    "Importation terminée. ${importCount} aliments importés, ${updateCount} aliments mis à jour, ${deleteCount} aliments supprimés."
            )
            println("Base de données vidée : ${deleteCount} aliments supprimés")
            importCount + updateCount
        }
    }

    /**
     * Convertit une liste d'identifiants d'espèces en leurs labels correspondants. Si la liste est
     * vide mais qu'un id d'espèce est fourni, il sera également converti.
     *
     * @param especes Liste des espèces (qui peuvent être sous forme d'ID ou de labels)
     * @param especeId ID numérique d'une espèce (utilisé si la liste est vide)
     * @return Liste des labels d'espèces convertis
     */
    private fun convertirEspecesEnLabels(especes: List<String>, especeId: Int): List<String> {
        // Si la liste d'espèces n'est pas vide, la traiter
        if (especes.isNotEmpty()) {
            return especes.map { especeStr ->
                // Vérifier si l'espèce est un identifiant numérique
                val especeInt = especeStr.toIntOrNull()
                if (especeInt != null) {
                    // C'est un ID numérique, essayer de le convertir en label
                    try {
                        val espece = Espece.getEnumFromInt(especeInt)
                        espece?.label
                                ?: especeStr // Utiliser le label si trouvé, sinon garder la chaîne
                        // d'origine
                    } catch (e: Exception) {
                        especeStr // En cas d'erreur, garder la chaîne d'origine
                    }
                } else {
                    // Ce n'est pas un ID numérique, donc c'est probablement déjà un label
                    especeStr
                }
            }
        }
        // Si la liste est vide mais qu'un ID d'espèce est fourni
        else if (especeId > 0) {
            try {
                val espece = Espece.getEnumFromInt(especeId)
                return listOf(espece?.label ?: especeId.toString())
            } catch (e: Exception) {
                return listOf(especeId.toString())
            }
        }

        // Si aucune espèce n'est spécifiée
        return emptyList()
    }

    /**
     * Convertit une liste d'identifiants d'indications en leurs labels correspondants.
     *
     * @param indications Liste des indications (qui peuvent être sous forme de codes ou de labels)
     * @return Liste des labels d'indications convertis
     */
    private fun convertirIndicationsEnLabels(indications: List<String>): List<String> {
        // Si la liste d'indications n'est pas vide, la traiter
        if (indications.isNotEmpty()) {
            return indications.map { indicStr ->
                // Vérifier si l'indication est un identifiant numérique
                val indicInt = indicStr.toIntOrNull()
                if (indicInt != null) {
                    // C'est un code numérique, essayer de le convertir en label
                    try {
                        val indic = AlimIndic.byCoef(indicInt)
                        indic.label // Utiliser le label correspondant au code
                    } catch (e: Exception) {
                        indicStr // En cas d'erreur, garder la chaîne d'origine
                    }
                } else {
                    // Ce n'est pas un code numérique, donc c'est probablement déjà un label
                    indicStr
                }
            }
        }

        // Si aucune indication n'est spécifiée
        return emptyList()
    }

    /** Insère un aliment avec toutes ses propriétés associées */
    override suspend fun insertFood(food: AlimentEv) {
        withContext(AppDispatchers.IO) {
            try {
                // Conversion de l'objet de domaine en entité
                val foodEntity = food.toFoodEntity()

                // Insertion de l'entité dans la base de données
                foodDao.insert(foodEntity)

                // Nous n'avons plus besoin d'insérer les indications dans la table d'association
                // car elles sont maintenant stockées dans le champ indicationsJson
            } catch (e: Exception) {
                println("Erreur lors de l'insertion de l'aliment: ${e.message}")
            }
        }
    }

    override suspend fun getFood(uuid: String): AlimentEv? {
        return withContext(AppDispatchers.IO) {
            // Essayer d'abord de récupérer l'aliment depuis la table ALIMENTS_BASE
            val alimentEntity = foodDao.getFood(uuid)
            if (alimentEntity != null) {
                println(
                        "DEBUG getFood - Aliment trouvé dans ALIMENTS_BASE: ${alimentEntity.name} (${alimentEntity.uuid})"
                )

                // Charger les indications
                val indications = foodDao.getIndicationsForAliment(uuid)
                val nutrientValues = nutrientValueDao.getNutrientValues(uuid)

                // Convertir en AlimentEv
                return@withContext alimentEntity.toAlimentEv(
                        emptyList(),
                        indications,
                        nutrientValues
                )
            }

            // Sinon, essayer de récupérer l'aliment depuis la table FOOD
            val foodEntity = foodDao.getFoodById(uuid)
            if (foodEntity != null) {
                println(
                        "DEBUG getFood - Aliment trouvé dans FOOD: ${foodEntity.nameDef} (${foodEntity.uuid})"
                )
                return@withContext foodEntity.toAlimentEv()
            }

            println("DEBUG getFood - Aucun aliment trouvé pour UUID: $uuid")
            return@withContext null
        }
    }

    override suspend fun deleteFood(uuid: String) {
        withContext(AppDispatchers.IO) {
            nutrientValueDao.deleteAllNutrientValuesForAliment(uuid)
            foodDao.deleteIndicationsForAliment(uuid)
            foodDao.deleteFood(uuid)
        }
    }

    override suspend fun updateFood(food: AlimentEv) {
        withContext(AppDispatchers.IO) {
            try {
                // Conversion de l'objet de domaine en entité
                val foodEntity = food.toFoodEntity()

                // Mise à jour de l'entité dans la base de données
                foodDao.update(foodEntity)

                // Nous n'avons plus besoin de mettre à jour les indications dans la table
                // d'association
                // car elles sont maintenant stockées dans le champ indicationsJson
            } catch (e: Exception) {
                println("Erreur lors de la mise à jour de l'aliment: ${e.message}")
            }
        }
    }

    // Définition de méthodes d'extension pour la conversion entre AlimentEv et FoodEntity
    private fun AlimentEv.toFoodEntity(): FoodEntity {
        // Conversion des espèces en JSON
        val especesLabels = this.especes.map { it }
        val especesJsonString =
                if (especesLabels.isEmpty()) null else Json.encodeToString(especesLabels)

        // Conversion des indications en JSON
        val indicLabels = this.indicat.map { it.label }
        val indicationsJsonString =
                if (indicLabels.isEmpty()) null else Json.encodeToString(indicLabels)

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
                cont = this.cont?.name ?: "NO",
                consistent = if (this.cont != null && this.cont.ordinal > 0) 1 else 0,
                deprecated = if (this.deprecated) 1 else 0,
                DataB = this.dataB ?: "",
                RefRation = this.rationUUID,
                RefAlimUnif = null,
                especesJson = especesJsonString,
                indicationsJson = indicationsJsonString
        )
    }

    // Extension pour convertir AlimentEvJson en FoodEntity
    private fun AlimentEvJson.toFoodEntity(): FoodEntity {
        // Conversion des espèces en JSON
        val especesConverties = convertirEspecesEnLabels(this.Especes, this.espece)
        val especesJsonString =
                if (especesConverties.isEmpty()) null else Json.encodeToString(especesConverties)

        // Conversion des indications en JSON
        val indicationsConverties = convertirIndicationsEnLabels(this.indication)
        val indicationsJsonString =
                if (indicationsConverties.isEmpty()) null
                else Json.encodeToString(indicationsConverties)

        return FoodEntity(
                uuid = this.UUID,
                groupAlim =
                        try {
                            GroupAlim.valueOf(this.group).ordinal
                        } catch (e: Exception) {
                            0
                        },
                typeAlim =
                        try {
                            FoodKind.valueOf(this.foodKind).ordinal
                        } catch (e: Exception) {
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
                cont = this.cont ?: "NO",
                consistent = if (this.cont == "YES") 1 else 0,
                deprecated = if (this.deprecated == true) 1 else 0,
                DataB = this.DataB,
                RefRation = null,
                RefAlimUnif = null,
                especesJson = especesJsonString,
                indicationsJson = indicationsJsonString
        )
    }

    // Extension pour convertir FoodEntity en AlimentEv
    private fun FoodEntity.toAlimentEv(): AlimentEv {
        // Désérialisation des espèces depuis JSON
        val especesList = mutableListOf<String>()
        if (!this.especesJson.isNullOrEmpty()) {
            try {
                val listeEspeces: List<String> = Json.decodeFromString(this.especesJson)
                especesList.addAll(listeEspeces)
            } catch (e: Exception) {
                println("Erreur lors de la désérialisation des espèces: ${e.message}")
            }
        }

        // Désérialisation des indications depuis JSON
        val indicationsList = mutableListOf<AlimIndic>()
        if (!this.indicationsJson.isNullOrEmpty()) {
            try {
                val listeIndications: List<String> = Json.decodeFromString(this.indicationsJson)
                listeIndications.forEach { indication ->
                    try {
                        // Tenter de convertir le label en AlimIndic
                        val alimIndic = AlimIndic.values().find { it.label == indication }
                        if (alimIndic != null) {
                            indicationsList.add(alimIndic)
                        }
                    } catch (e: Exception) {
                        println(
                                "Erreur lors de la conversion de l'indication '$indication': ${e.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                println("Erreur lors de la désérialisation des indications: ${e.message}")
            }
        }

        return AlimentEv(
                uuid = this.uuid,
                nom = this.nameDef,
                group =
                        try {
                            GroupAlim.values().getOrNull(this.groupAlim)
                        } catch (e: Exception) {
                            null
                        },
                typeAliment =
                        try {
                            FoodKind.values().getOrNull(this.typeAlim)
                        } catch (e: Exception) {
                            null
                        },
                ingredients = this.ingredients,
                price = this.price,
                categPrice = this.categPrice,
                brand = this.brand,
                gamme = this.gamme,
                quantInt = this.quantityPres,
                cont = fr.vetbrain.vetnutri_mp.Enumer.ContEnum.getByName(this.cont),
                deprecated = this.deprecated > 0,
                dataB = this.DataB,
                especes = especesList,
                indicat = indicationsList,
                rationUUID = this.RefRation
        )
    }

    /**
     * Diagnostique l'importation des aliments à partir d'un fichier JSON et retourne un rapport
     * détaillé
     *
     * @param jsonContent Le contenu JSON à analyser
     * @return Un rapport détaillé sur l'importation et les problèmes éventuels
     */
    suspend fun diagnosticFoodImport(jsonContent: String): String {
        return try {
            // Utiliser l'utilitaire de test pour analyser l'importation
            val report = ImportTester.testFoodImport(jsonContent)

            // Compter le nombre d'aliments dans la base de données pour référence
            val currentFoodsCount = getAllFoods().size
            report +
                    "\n\nNombre d'aliments actuellement dans la base de données: $currentFoodsCount"
        } catch (e: Exception) {
            "ERREUR LORS DU DIAGNOSTIC: ${e.message}\n${e.stackTraceToString()}"
        }
    }

    /**
     * Vide entièrement la base de données des aliments. Supprime tous les aliments, leurs espèces
     * associées, leurs indications et leurs valeurs nutritionnelles.
     *
     * @return Le nombre d'aliments supprimés
     */
    suspend fun clearAllFoods(): Int {
        return withContext(AppDispatchers.IO) {
            var totalCount = 0

            // Récupérer tous les aliments de la table ALIMENTS_BASE
            val allFoodsFromAlimentBase = foodDao.getAllFoods()
            val count1 = allFoodsFromAlimentBase.size

            // Pour chaque aliment dans ALIMENTS_BASE, supprimer ses valeurs nutritionnelles et
            // indications
            allFoodsFromAlimentBase.forEach { food ->
                // Supprimer d'abord les valeurs nutritionnelles
                nutrientValueDao.deleteAllNutrientValuesForAliment(food.uuid)

                // Supprimer les indications associées
                foodDao.deleteIndicationsForAliment(food.uuid)

                // Supprimer l'aliment lui-même
                foodDao.deleteFood(food.uuid)
            }

            // Récupérer tous les aliments de la table FOOD
            val allFoodsFromFood = foodDao.findAll()
            val count2 = allFoodsFromFood.size

            // Pour chaque aliment dans FOOD, supprimer ses valeurs nutritionnelles et indications
            allFoodsFromFood.forEach { food ->
                // Supprimer d'abord les valeurs nutritionnelles
                nutrientValueDao.deleteAllNutrientValuesForAliment(food.uuid)

                // Supprimer les indications associées
                foodDao.deleteIndicationsForAliment(food.uuid)

                // Attention: nous utilisons une requête spécifique pour supprimer de la table FOOD
                try {
                    // Nous devons appeler delete car il n'y a pas de méthode dédiée pour supprimer
                    // de FOOD
                    foodDao.delete(food)
                } catch (e: Exception) {
                    println(
                            "Erreur lors de la suppression de l'aliment ${food.nameDef} (${food.uuid}) de FOOD: ${e.message}"
                    )
                }
            }

            totalCount = count1 + count2
            println(
                    "Base de données vidée : $totalCount aliments supprimés (${count1} de ALIMENTS_BASE, ${count2} de FOOD)"
            )
            totalCount
        }
    }
}
