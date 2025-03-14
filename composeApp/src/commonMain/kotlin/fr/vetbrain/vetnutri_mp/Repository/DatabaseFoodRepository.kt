package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentEvJson
import fr.vetbrain.vetnutri_mp.DataBase.FoodDao
import fr.vetbrain.vetnutri_mp.DataBase.FoodEntity
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toAlimentEv
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toFoodEntity
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toNutrientValueEntities
import fr.vetbrain.vetnutri_mp.DataBase.NutrientValueDao
import fr.vetbrain.vetnutri_mp.DataBase.NutrientValueEntity
import fr.vetbrain.vetnutri_mp.Enumer.AlimIndic
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.FoodKind
import fr.vetbrain.vetnutri_mp.Enumer.GroupAlim
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Implémentation de FoodRepository utilisant une base de données SQLite. Cette classe gère les
 * opérations CRUD pour les aliments.
 */
class DatabaseFoodRepository(
        private val foodDao: FoodDao,
        private val nutrientValueDao: NutrientValueDao?
) : FoodRepository {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Insère un aliment dans la base de données.
     * @param food L'aliment à insérer
     */
    override suspend fun insert(food: AlimentEv) {
        withContext(AppDispatchers.IO) { foodDao.insert(food.toFoodEntity()) }
    }

    /**
     * Met à jour un aliment existant dans la base de données.
     * @param food L'aliment à mettre à jour
     */
    override suspend fun update(food: AlimentEv) {
        withContext(AppDispatchers.IO) { foodDao.update(food.toFoodEntity()) }
    }

    /**
     * Supprime un aliment de la base de données.
     * @param food L'aliment à supprimer
     */
    override suspend fun delete(food: AlimentEv) {
        withContext(AppDispatchers.IO) { foodDao.delete(food.toFoodEntity()) }
    }

    /**
     * Récupère tous les aliments stockés dans la base de données.
     * @return Une liste de tous les aliments
     */
    override suspend fun getAllFoods(): List<AlimentEv> {
        return withContext(AppDispatchers.IO) {
            println(
                    "DEBUG DatabaseFoodRepository: Récupération de tous les aliments depuis la base de données"
            )

            // Récupérer tous les aliments de la base de données
            val foodEntities = foodDao.getAllFoods()
            println(
                    "DEBUG DatabaseFoodRepository: ${foodEntities.size} entités d'aliments récupérées"
            )

            // Transformer chaque entité en modèle de domaine avec ses valeurs nutritionnelles
            val result =
                    foodEntities.map { foodEntity ->
                        val nutrientValues =
                                if (nutrientValueDao != null) {
                                    nutrientValueDao.getNutrientValues(foodEntity.uuid)
                                } else {
                                    emptyList()
                                }
                        foodEntity.toAlimentEv(nutrientValues = nutrientValues)
                    }

            println("DEBUG DatabaseFoodRepository: ${result.size} aliments transformés avec succès")
            return@withContext result
        }
    }

    /**
     * Récupère un aliment par son identifiant.
     * @param id L'identifiant de l'aliment
     * @return L'aliment correspondant ou null si non trouvé
     */
    override suspend fun getFoodById(id: String): AlimentEv? {
        return withContext(AppDispatchers.IO) {
            val food = foodDao.getFoodById(id) ?: return@withContext null
            val nutrientValues =
                    if (nutrientValueDao != null) {
                        nutrientValueDao.getNutrientValues(food.uuid)
                    } else {
                        emptyList()
                    }
            return@withContext food.toAlimentEv(nutrientValues = nutrientValues)
        }
    }

    /**
     * Observe tous les aliments via un Flow.
     * @return Un Flow émettant la liste des aliments à chaque modification
     */
    override fun observeAllFoods(): Flow<List<AlimentEv>> {
        return flow { emit(getAllFoods()) }
    }

    /**
     * Importe une liste d'aliments dans la base de données.
     * @param foods La liste des aliments à importer
     * @return Le nombre d'aliments importés avec succès
     */
    override suspend fun importFoods(foods: List<AlimentEvJson>): Int {
        return withContext(AppDispatchers.IO) {
            var importCount = 0
            var updateCount = 0
            var deleteCount = 0
            var errorCount = 0

            println("===== DÉBUT IMPORTATION ALIMENTS =====")
            println("Nombre d'aliments à importer: ${foods.size}")

            // Collecter les nutriments non résolus
            val nonResolvedNutrients = mutableSetOf<String>()

            // Collecter les UUIDs de tous les aliments présents dans le fichier JSON
            val importedUUIDs = foods.map { it.UUID }.toSet()

            // Récupérer tous les aliments existants dans la base de données
            val existingFoods = foodDao.getAllFoods()
            val existingUUIDs = existingFoods.map { it.uuid }.toSet()

            println("Nombre d'aliments existants: ${existingFoods.size}")
            println("UUIDs des aliments disponibles initialement: $existingUUIDs")

            // Identifier les aliments à supprimer (existants mais pas dans la liste importée)
            val foodsToDelete = existingFoods.filter { !importedUUIDs.contains(it.uuid) }

            println("Nombre d'aliments à supprimer: ${foodsToDelete.size}")

            // Supprimer les aliments qui ne sont plus présents dans le fichier JSON
            foodsToDelete.forEach { food ->
                try {
                    // Supprimer d'abord les valeurs nutritionnelles
                    nutrientValueDao?.deleteAllNutrientValuesForAliment(food.uuid)

                    // Supprimer les indications associées
                    foodDao.deleteIndicationsForAliment(food.uuid)

                    // Supprimer l'aliment lui-même
                    foodDao.deleteFood(food.uuid)

                    deleteCount++
                    println("Suppression de l'aliment: ${food.name ?: "Sans nom"} (${food.uuid})")
                } catch (e: Exception) {
                    errorCount++
                    println("Erreur lors de la suppression de l'aliment ${food.uuid}: ${e.message}")
                }
            }

            // Traiter chaque aliment de la liste importée
            foods.forEach { food ->
                try {
                    // Vérifier si l'aliment existe déjà
                    val foodId = food.UUID
                    val existingFood = foodDao.getFoodById(foodId)

                    if (existingFood == null) {
                        println("Importation d'un nouvel aliment: ${food.nom} (${food.UUID})")

                        // Convertir les espèces
                        val especes =
                                food.Especes.map { especeStr ->
                                    try {
                                        // Nettoyer la chaîne d'espèce (supprimer les crochets et
                                        // guillemets)
                                        val cleanedEspece =
                                                especeStr
                                                        .replace("[", "")
                                                        .replace("]", "")
                                                        .replace("\"", "")
                                                        .trim()

                                        println(
                                                "Chaîne d'espèce nettoyée: $especeStr -> $cleanedEspece"
                                        )

                                        // Essayer plusieurs stratégies pour reconnaître l'espèce
                                        val espece = Espece.getFromString(cleanedEspece)
                                        if (espece != null) {
                                            // Si l'espèce est reconnue, utiliser le nom de
                                            // l'énumération
                                            println(
                                                    "Espèce reconnue: $cleanedEspece -> ${espece.name}"
                                            )
                                            espece.name
                                        } else {
                                            // Si non reconnue, conserver la chaîne originale
                                            println(
                                                    "Espèce non reconnue mais conservée: $cleanedEspece"
                                            )
                                            cleanedEspece
                                        }
                                    } catch (e: Exception) {
                                        println(
                                                "Erreur lors de la conversion de l'espèce $especeStr: ${e.message}"
                                        )
                                        especeStr // Conserver la valeur originale en cas d'erreur
                                    }
                                }
                        println("Espèces converties: $especes")
                        val especesJson = json.encodeToString(especes)

                        // Convertir les indications pour l'affichage et le stockage
                        val indications = mutableListOf<String>()
                        food.indication.forEach { indication ->
                            try {
                                when (indication) {
                                    is String -> {
                                        // Nettoyer la chaîne d'indication (supprimer les crochets
                                        // et guillemets)
                                        val cleanedIndication =
                                                indication
                                                        .replace("[", "")
                                                        .replace("]", "")
                                                        .replace("\"", "")
                                                        .trim()

                                        println(
                                                "Chaîne d'indication nettoyée: $indication -> $cleanedIndication"
                                        )

                                        // Essayer de convertir en AlimIndic
                                        try {
                                            val indicEnum = AlimIndic.valueOf(cleanedIndication)
                                            println(
                                                    "Indication reconnue comme enum: $cleanedIndication -> ${indicEnum.name}"
                                            )
                                            indications.add(
                                                    indicEnum.name
                                            ) // Stocker le nom de l'énumération
                                        } catch (e: IllegalArgumentException) {
                                            // Essayer par le label
                                            val indicByLabel = AlimIndic.byName(cleanedIndication)
                                            if (indicByLabel != AlimIndic.AUTRE) {
                                                println(
                                                        "Indication reconnue par label: $cleanedIndication -> ${indicByLabel.name}"
                                                )
                                                indications.add(
                                                        indicByLabel.name
                                                ) // Stocker le nom de l'énumération
                                            } else {
                                                // Conserver l'indication originale si non reconnue
                                                println(
                                                        "Indication non reconnue mais conservée: $cleanedIndication"
                                                )
                                                indications.add(cleanedIndication)
                                            }
                                        }
                                    }
                                    is Int -> {
                                        // Essayer de convertir l'entier en AlimIndic
                                        val indicationInt: Int = indication // Typage explicite
                                        try {
                                            val indicEnum = AlimIndic.byCoef(indicationInt)
                                            println(
                                                    "Indication reconnue par code: $indicationInt -> ${indicEnum.name}"
                                            )
                                            indications.add(
                                                    indicEnum.name
                                            ) // Stocker le nom de l'énumération
                                        } catch (e: Exception) {
                                            // Stocker la valeur sous forme de chaîne
                                            println(
                                                    "Code d'indication non reconnu mais conservé: $indicationInt"
                                            )
                                            val indicationStr: String = indicationInt.toString()
                                            indications.add(indicationStr)
                                        }
                                    }
                                    else -> {
                                        // Pour tout autre type, utiliser toString() après nettoyage
                                        val cleanedStr =
                                                indication
                                                        .toString()
                                                        .replace("[", "")
                                                        .replace("]", "")
                                                        .replace("\"", "")
                                                        .trim()
                                        println(
                                                "Type d'indication non standard conservé: ${indication.toString()} -> $cleanedStr"
                                        )
                                        indications.add(cleanedStr)
                                    }
                                }
                            } catch (e: Exception) {
                                println(
                                        "Erreur lors du traitement de l'indication $indication: ${e.message}"
                                )
                                // Nettoyer la chaîne avant de l'ajouter
                                val cleanedStr =
                                        indication
                                                .toString()
                                                .replace("[", "")
                                                .replace("]", "")
                                                .replace("\"", "")
                                                .trim()
                                indications.add(cleanedStr) // Conserver en cas d'erreur
                            }
                        }
                        println("Indications converties: $indications")
                        val indicationsJson = json.encodeToString(indications)

                        // Créer une entité FoodEntity avec RefRation à null pour éviter les erreurs
                        // de clé étrangère
                        val foodEntity =
                                FoodEntity(
                                        uuid = food.UUID,
                                        nameDef = food.nom ?: "",
                                        groupAlim =
                                                try {
                                                    GroupAlim.valueOf(food.group ?: "").ordinal
                                                } catch (e: Exception) {
                                                    0
                                                },
                                        typeAlim =
                                                try {
                                                    FoodKind.valueOf(food.foodKind ?: "").ordinal
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
                                        RefRation =
                                                null, // Important: null pour éviter les contraintes
                                        // de clé étrangère
                                        RefAlimUnif = food.UUID, // Utiliser l'UUID comme référence
                                        especesJson = especesJson,
                                        indicationsJson = indicationsJson,
                                        name = food.nom,
                                        quantite = 0f
                                )

                        try {
                            println(
                                    "Insertion de l'aliment: ${foodEntity.nameDef} (${foodEntity.uuid})"
                            )
                            foodDao.insert(foodEntity)
                            importCount++

                            // Traiter les valeurs nutritionnelles
                            processNutrientValues(food, nonResolvedNutrients)
                            println("Aliment importé avec succès: ${food.nom} (${food.UUID})")

                            // Vérifier que l'aliment est bien présent
                            val insertedFood = foodDao.getFoodById(food.UUID)
                            if (insertedFood != null) {
                                println(
                                        "Vérification réussie - Aliment trouvé dans la base: ${insertedFood.nameDef}"
                                )
                            } else {
                                println(
                                        "ERREUR DE VÉRIFICATION - Aliment non trouvé après insertion: ${food.UUID}"
                                )
                            }
                        } catch (e: Exception) {
                            errorCount++
                            println(
                                    "Erreur lors de la création de l'aliment ${food.nom} (${food.UUID}): ${e.message}"
                            )
                            e.printStackTrace()

                            // Essayer de créer un aliment minimal en cas d'erreur
                            try {
                                println("Tentative d'insertion d'un aliment minimal...")
                                val minimalFood =
                                        FoodEntity(
                                                uuid = food.UUID,
                                                nameDef = food.nom ?: "",
                                                groupAlim = 0,
                                                typeAlim = 0,
                                                ingredients = "",
                                                price = 0.0,
                                                categPrice = "",
                                                brand = "",
                                                gamme = "",
                                                unitPres = 0,
                                                quantityPres = 0f,
                                                version = 1,
                                                date = "",
                                                cont = "",
                                                consistent = 0,
                                                deprecated = 0,
                                                DataB = "",
                                                RefRation = null, // Toujours null
                                                RefAlimUnif =
                                                        null, // Également null pour éviter les
                                                // problèmes
                                                especesJson = "[]",
                                                indicationsJson = "[]",
                                                name = food.nom,
                                                quantite = 0f
                                        )

                                foodDao.insert(minimalFood)
                                importCount++
                                println(
                                        "Aliment minimal importé avec succès: ${food.nom} (${food.UUID})"
                                )

                                // Vérifier l'insertion de l'aliment minimal
                                val insertedMinimalFood = foodDao.getFoodById(food.UUID)
                                if (insertedMinimalFood != null) {
                                    println(
                                            "Vérification réussie - Aliment minimal trouvé dans la base"
                                    )
                                } else {
                                    println(
                                            "ERREUR DE VÉRIFICATION - Aliment minimal non trouvé après insertion"
                                    )
                                }
                            } catch (e2: Exception) {
                                println("Échec de l'insertion de l'aliment minimal: ${e2.message}")
                                e2.printStackTrace()
                            }
                        }
                    } else {
                        // L'aliment existe déjà, le mettre à jour
                        println(
                                "Mise à jour de l'aliment existant: ${existingFood.nameDef} (${existingFood.uuid})"
                        )
                        updateCount++

                        // Préserver la référence à la ration si elle existe
                        val existingRationRef = existingFood.RefRation

                        // Mettre à jour l'aliment avec les nouvelles valeurs tout en préservant la
                        // référence à la ration
                        val updatedFood =
                                existingFood.copy(
                                        nameDef = food.nom ?: existingFood.nameDef,
                                        ingredients = food.ingredients ?: existingFood.ingredients,
                                        price = food.prix ?: existingFood.price,
                                        brand = food.marque ?: existingFood.brand,
                                        gamme = food.gamme ?: existingFood.gamme,
                                        // Conserver la référence à la ration
                                        RefRation = existingRationRef
                                )

                        try {
                            foodDao.update(updatedFood)
                            println("Aliment mis à jour avec succès: ${updatedFood.nameDef}")

                            // Traiter les valeurs nutritionnelles
                            processNutrientValues(food, nonResolvedNutrients)
                        } catch (e: Exception) {
                            println("Erreur lors de la mise à jour de l'aliment: ${e.message}")
                            e.printStackTrace()
                        }
                    }
                } catch (e: Exception) {
                    errorCount++
                    println(
                            "Erreur globale lors du traitement de l'aliment ${food.nom} (${food.UUID}): ${e.message}"
                    )
                    e.printStackTrace()
                }
            }

            // Vérifier le résultat de l'importation
            val updatedFoods = foodDao.getAllFoods()
            println("\n===== RÉSULTATS DE L'IMPORTATION =====")
            println("Nombre d'aliments dans la base après importation: ${updatedFoods.size}")
            println("$importCount aliments importés")
            println("$updateCount aliments mis à jour")
            println("$deleteCount aliments supprimés")
            println("$errorCount erreurs rencontrées")

            if (nonResolvedNutrients.isNotEmpty()) {
                println("${nonResolvedNutrients.size} nutriments non résolus")
            }

            println("===== FIN IMPORTATION ALIMENTS =====")
            return@withContext importCount
        }
    }

    /**
     * Traite les valeurs nutritionnelles pour un aliment.
     * @param food L'aliment pour lequel traiter les valeurs nutritionnelles
     * @param nonResolvedNutrients Ensemble mutable pour collecter les nutriments non résolus
     */
    private suspend fun processNutrientValues(
            food: AlimentEvJson,
            nonResolvedNutrients: MutableSet<String>
    ) {
        val nutrientValues = mutableListOf<NutrientValueEntity>()
        val resolvedCount = AtomicInteger(0)
        val nonResolvedCount = AtomicInteger(0)

        // Supprimer d'abord toutes les valeurs nutritionnelles existantes
        if (nutrientValueDao != null) {
            nutrientValueDao.deleteAllNutrientValuesForAliment(food.UUID)
        } else {
            println(
                    "ATTENTION: nutrientValueDao est null, impossible de supprimer ou insérer les valeurs nutritionnelles"
            )
        }

        println("\n===== TRAITEMENT DES NUTRIMENTS POUR ${food.nom} (${food.UUID}) =====")
        println("Nombre de nutriments à traiter: ${food.valMap.size}")

        // Traiter chaque valeur nutritionnelle
        food.valMap.forEach { (key, nutrientQuantity) ->
            val nutrientKey = nutrientQuantity.nut
            val value = nutrientQuantity.value

            val nutrient =
                    fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver.AllNutrientResolver(nutrientKey)
            if (nutrient != null) {
                resolvedCount.incrementAndGet()
                println("✓ Nutriment résolu: $nutrientKey -> ${nutrient.label} = $value")
                nutrientValues.add(
                        NutrientValueEntity(
                                refAliment = food.UUID,
                                nutrientLabel = nutrient.label,
                                value = value
                        )
                )
            } else {
                nonResolvedCount.incrementAndGet()
                nonResolvedNutrients.add(nutrientKey)
                println("✗ Nutriment NON résolu: $nutrientKey = $value")
            }
        }

        println("Résultats du traitement des nutriments:")
        println("- ${resolvedCount.get()} nutriments résolus")
        println("- ${nonResolvedCount.get()} nutriments non résolus")

        // Insérer toutes les valeurs nutritionnelles
        if (nutrientValues.isNotEmpty()) {
            try {
                if (nutrientValueDao != null) {
                    nutrientValueDao.insertNutrientValues(nutrientValues)
                    println(
                            "${nutrientValues.size} valeurs nutritionnelles insérées pour l'aliment ${food.nom} (${food.UUID})"
                    )
                } else {
                    println(
                            "ATTENTION: nutrientValueDao est null, ${nutrientValues.size} valeurs nutritionnelles prêtes mais non insérées"
                    )
                }
            } catch (e: Exception) {
                println("Erreur lors de l'insertion des valeurs nutritionnelles: ${e.message}")
                e.printStackTrace()
            }
        }
        println("===== FIN DU TRAITEMENT DES NUTRIMENTS =====\n")
    }

    /**
     * Insère un aliment avec toutes ses propriétés associées.
     * @param food L'aliment à insérer
     */
    override suspend fun insertFood(food: AlimentEv) {
        withContext(AppDispatchers.IO) {
            // Convertir en FoodEntity et insérer
            foodDao.insert(food.toFoodEntity())

            // Traiter les valeurs nutritionnelles
            val nutrientValues = food.valMap.toNutrientValueEntities(food.uuid)
            if (nutrientValueDao != null && nutrientValues.isNotEmpty()) {
                nutrientValueDao.insertNutrientValues(nutrientValues)
            }
        }
    }

    /**
     * Récupère un aliment avec toutes ses propriétés associées.
     * @param uuid UUID de l'aliment à récupérer
     * @return L'aliment complet ou null si non trouvé
     */
    override suspend fun getFood(uuid: String): AlimentEv? {
        return withContext(AppDispatchers.IO) {
            val foodEntity = foodDao.getFoodById(uuid) ?: return@withContext null
            val nutrientValues =
                    if (nutrientValueDao != null) {
                        nutrientValueDao.getNutrientValues(uuid)
                    } else {
                        emptyList()
                    }
            return@withContext foodEntity.toAlimentEv(nutrientValues = nutrientValues)
        }
    }

    /**
     * Supprime un aliment et toutes ses propriétés associées.
     * @param uuid UUID de l'aliment à supprimer
     */
    override suspend fun deleteFood(uuid: String) {
        withContext(AppDispatchers.IO) {
            // Supprimer d'abord les valeurs nutritionnelles
            nutrientValueDao?.deleteAllNutrientValuesForAliment(uuid)

            // Supprimer les indications associées
            foodDao.deleteIndicationsForAliment(uuid)

            // Supprimer l'aliment lui-même
            foodDao.deleteFood(uuid)
        }
    }

    /**
     * Met à jour un aliment et toutes ses propriétés associées.
     * @param food Aliment à mettre à jour
     */
    override suspend fun updateFood(food: AlimentEv) {
        return withContext(AppDispatchers.IO) {
            try {
                // Vérifier que l'aliment existe
                val existingFood = foodDao.getFoodById(food.uuid)
                if (existingFood == null) {
                    println(
                            "DEBUG DatabaseFoodRepository: ERREUR - Aliment non trouvé dans la base de données: ${food.uuid}"
                    )
                    throw Exception("Aliment non trouvé dans la base de données: ${food.uuid}")
                }

                println("DEBUG DatabaseFoodRepository: Aliment trouvé, conversion en FoodEntity")

                // Au lieu de modifier toute l'entité, on garde la référence à la ration de
                // l'existant
                // pour éviter les problèmes de clé étrangère
                val foodEntity = food.toFoodEntity().copy(RefRation = existingFood.RefRation)
                println(
                        "DEBUG DatabaseFoodRepository: Conversion réussie, mise à jour de l'entité principale"
                )

                try {
                    // Mettre à jour l'entité principale
                    foodDao.update(foodEntity)
                    println(
                            "DEBUG DatabaseFoodRepository: Entité principale mise à jour avec succès"
                    )
                } catch (e: Exception) {
                    println(
                            "DEBUG DatabaseFoodRepository: Erreur lors de la mise à jour de l'entité principale: ${e.message}"
                    )

                    // Plan B: si la mise à jour échoue, on essaie de mettre à jour sans la
                    // référence à la ration
                    println(
                            "DEBUG DatabaseFoodRepository: Tentative de mise à jour sans référence à la ration"
                    )
                    val updatedEntity = foodEntity.copy(RefRation = null)
                    foodDao.update(updatedEntity)
                    println(
                            "DEBUG DatabaseFoodRepository: Entité principale mise à jour sans référence à la ration"
                    )
                }

                // Supprimer toutes les anciennes valeurs nutritionnelles quelle que soit la
                // situation
                println(
                        "DEBUG DatabaseFoodRepository: Suppression des anciennes valeurs nutritionnelles"
                )
                if (nutrientValueDao != null) {
                    nutrientValueDao.deleteAllNutrientValuesForAliment(food.uuid)
                    println(
                            "DEBUG DatabaseFoodRepository: Anciennes valeurs nutritionnelles supprimées avec succès"
                    )
                } else {
                    println(
                            "DEBUG DatabaseFoodRepository: AVERTISSEMENT - nutrientValueDao est null, impossible de supprimer les valeurs nutritionnelles"
                    )
                }

                // Seulement si des valeurs nutritionnelles existent et que le DAO existe, les
                // ajouter
                println(
                        "DEBUG DatabaseFoodRepository: Conversion des valeurs nutritionnelles en entités"
                )
                val nutrientValues = food.valMap.toNutrientValueEntities(food.uuid)
                println(
                        "DEBUG DatabaseFoodRepository: Nouvelles valeurs nutritionnelles: ${nutrientValues.size}"
                )

                if (nutrientValues.isNotEmpty() && nutrientValueDao != null) {
                    println(
                            "DEBUG DatabaseFoodRepository: Insertion des nouvelles valeurs nutritionnelles"
                    )
                    nutrientValues.forEach { entity ->
                        println(
                                "DEBUG DatabaseFoodRepository: Nutriment ${entity.nutrientLabel} = ${entity.value}"
                        )
                    }
                    nutrientValueDao.insertNutrientValues(nutrientValues)
                    println(
                            "DEBUG DatabaseFoodRepository: Nouvelles valeurs nutritionnelles insérées avec succès"
                    )
                } else {
                    println("DEBUG DatabaseFoodRepository: Aucune valeur nutritionnelle à insérer")
                }

                println(
                        "DEBUG DatabaseFoodRepository: Mise à jour de l'aliment terminée avec succès"
                )
            } catch (e: Exception) {
                println("DEBUG DatabaseFoodRepository: ERREUR lors de la mise à jour: ${e.message}")
                e.printStackTrace()
                throw e // Relancer l'exception pour que la chaîne de traitement puisse la gérer
            }
        }
    }

    /**
     * Supprime tous les aliments de la base de données, ainsi que leurs valeurs nutritionnelles
     * associées.
     * @return Le nombre d'aliments supprimés
     */
    override suspend fun clearAllFoods(): Int {
        return withContext(AppDispatchers.IO) {
            // Récupérer tous les aliments pour connaître leur nombre
            val allFoods = foodDao.getAllFoods()
            val count = allFoods.size

            // Supprimer toutes les valeurs nutritionnelles pour tous les aliments
            allFoods.forEach { food ->
                nutrientValueDao?.deleteAllNutrientValuesForAliment(food.uuid)
                foodDao.deleteIndicationsForAliment(food.uuid)
            }

            // Supprimer tous les aliments
            foodDao.deleteAllFoods()

            println("$count aliments ont été supprimés de la base de données")

            return@withContext count
        }
    }

    /**
     * Associe un aliment à une ration.
     * @param foodId Identifiant de l'aliment à associer
     * @param rationId Identifiant de la ration
     * @return true si l'association a réussi, false sinon
     */
    suspend fun associateFoodWithRation(foodId: String, rationId: String): Boolean {
        return withContext(AppDispatchers.IO) {
            try {
                // Vérifier que l'aliment existe
                val food = foodDao.getFoodById(foodId) ?: return@withContext false

                // Créer une entité d'aliment pour la ration (AlimentRationEntity)
                // Cette partie dépend de la structure exacte de votre base de données
                // Si vous avez une table spécifique pour associer les aliments aux rations

                // Mettre à jour la référence de ration dans l'aliment
                val updatedFood = food.copy(RefRation = rationId)
                foodDao.update(updatedFood)

                println("Aliment ${food.name} (${food.uuid}) associé à la ration $rationId")
                return@withContext true
            } catch (e: Exception) {
                println(
                        "Erreur lors de l'association de l'aliment $foodId à la ration $rationId: ${e.message}"
                )
                return@withContext false
            }
        }
    }

    /**
     * Charge les aliments associés à une ration spécifique.
     * @param rationId Identifiant de la ration
     * @return Liste des aliments associés à la ration
     */
    suspend fun getFoodsForRation(rationId: String): List<AlimentEv> {
        return withContext(AppDispatchers.IO) {
            val foods = foodDao.getAllFoods().filter { it.RefRation == rationId }
            return@withContext foods.map { foodEntity ->
                val nutrientValues =
                        if (nutrientValueDao != null) {
                            nutrientValueDao.getNutrientValues(foodEntity.uuid)
                        } else {
                            emptyList()
                        }
                foodEntity.toAlimentEv(nutrientValues = nutrientValues)
            }
        }
    }
}
