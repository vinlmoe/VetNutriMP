package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentEvJson
import fr.vetbrain.vetnutri_mp.Data.AlimentEvLight
import fr.vetbrain.vetnutri_mp.DataBase.FoodDao
import fr.vetbrain.vetnutri_mp.DataBase.FoodEntity
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toAlimentEv
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toAlimentEvLight
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toFoodEntity
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toNutrientValueEntities
import fr.vetbrain.vetnutri_mp.DataBase.NutrientValueDao
import fr.vetbrain.vetnutri_mp.DataBase.NutrientValueEntity
import fr.vetbrain.vetnutri_mp.Enumer.AlimIndic
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.FoodKind
import fr.vetbrain.vetnutri_mp.Enumer.GroupAlim
import fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
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
     * Récupère une liste légère de tous les aliments sans les valeurs nutritionnelles. Cette
     * méthode est optimisée pour les performances lorsque seules les informations de base des
     * aliments sont nécessaires.
     *
     * @return Une liste d'objets AlimentEvLight contenant les informations de base des aliments
     */
    override suspend fun getAllFoodsLight(): List<AlimentEvLight> {
        return withContext(AppDispatchers.IO) {
            println(
                    "DEBUG DatabaseFoodRepository: Récupération de tous les aliments en version légère"
            )

            // Récupérer tous les aliments de la base de données
            val foodEntities = foodDao.getAllFoods()
            println(
                    "DEBUG DatabaseFoodRepository: ${foodEntities.size} entités d'aliments récupérées pour la version légère"
            )

            // Transformer chaque entité en modèle de domaine léger sans les valeurs nutritionnelles
            val result = foodEntities.map { foodEntity -> foodEntity.toAlimentEvLight() }

            println(
                    "DEBUG DatabaseFoodRepository: ${result.size} aliments légers transformés avec succès"
            )
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
     * Importe une liste d'aliments depuis un format JSON.
     * @param foods Liste des aliments à importer
     * @return Résultat de l'importation
     */
    // DÉBUT ZONE PROTÉGÉE - NE PAS MODIFIER SANS AUTORISATION EXPRESSE
    // Description: Méthode critique pour l'importation des aliments depuis un format JSON.
    // Cette zone de code gère l'analyse et l'insertion des aliments dans la base de données,
    // incluant la vérification des aliments existants et la conversion des données.
    override suspend fun importFoods(foods: List<AlimentEvJson>): FoodImportResult {
        return withContext(AppDispatchers.IO) {
            var importCount = 0
            var updateCount = 0
            var errorCount = 0
            val nonResolvedNutrients = mutableMapOf<String, Int>()
            val importErrors = mutableListOf<String>()

            println("Début de l'importation de ${foods.size} aliments")

            // Récupérer la liste des UUIDs des aliments existants pour vérification rapide
            val existingFoodUUIDs = foodDao.getAllFoods().map { it.uuid }.toSet()
            println("${existingFoodUUIDs.size} aliments existants en base de données")

            // Traiter chaque aliment
            foods.forEachIndexed { index, food ->
                try {
                    println("\n===== TRAITEMENT DE L'ALIMENT ${index + 1}/${foods.size} =====")
                    println("UUID: ${food.UUID}, Nom: ${food.nom}")

                    if (food.UUID.isNullOrBlank()) {
                        val errorMsg = "Erreur: UUID vide pour l'aliment ${food.nom}"
                        println(errorMsg)
                        importErrors.add(errorMsg)
                        errorCount++
                        return@forEachIndexed
                    }

                    // Vérifier si l'aliment existe déjà
                    val existingFood =
                            if (existingFoodUUIDs.contains(food.UUID)) {
                                foodDao.getFoodById(food.UUID)
                            } else {
                                null
                            }

                    if (existingFood == null) {
                        // L'aliment n'existe pas, l'insérer
                        println("Création d'un nouvel aliment: ${food.nom} (${food.UUID})")

                        try {
                            // Convertir les espèces
                            val especes =
                                    food.Especes.map { especeStr ->
                                        try {
                                            // Nettoyer la chaîne d'espèce
                                            val cleanedEspece =
                                                    especeStr
                                                            .replace("[", "")
                                                            .replace("]", "")
                                                            .replace("\"", "")
                                                            .trim()

                                            println(
                                                    "Chaîne d'espèce nettoyée: $especeStr -> $cleanedEspece"
                                            )

                                            // Essayer plusieurs stratégies pour reconnaître
                                            // l'espèce
                                            val espece = Espece.getFromString(cleanedEspece)
                                            if (espece != null) {
                                                println(
                                                        "Espèce reconnue: $cleanedEspece -> ${espece.name}"
                                                )
                                                espece.name
                                            } else {
                                                println(
                                                        "Espèce non reconnue, conservation du texte original: $cleanedEspece"
                                                )
                                                cleanedEspece
                                            }
                                        } catch (e: Exception) {
                                            println(
                                                    "Erreur lors de la conversion de l'espèce $especeStr: ${e.message}"
                                            )
                                            e.printStackTrace()
                                            especeStr // En cas d'erreur, conserver la chaîne
                                            // originale
                                        }
                                    }

                            // Convertir les indications
                            val indications =
                                    food.indication.map { indicStr ->
                                        try {
                                            // Nettoyer la chaîne d'indication
                                            val cleanedIndic =
                                                    indicStr.toString()
                                                            .replace("[", "")
                                                            .replace("]", "")
                                                            .replace("\"", "")
                                                            .trim()

                                            println(
                                                    "Chaîne d'indication nettoyée: $indicStr -> $cleanedIndic"
                                            )

                                            // Essayer de reconnaître l'indication
                                            val indication = AlimIndic.getFromString(cleanedIndic)
                                            if (indication != null) {
                                                println(
                                                        "Indication reconnue: $cleanedIndic -> ${indication.name}"
                                                )
                                                indication.name
                                            } else {
                                                println(
                                                        "Indication non reconnue, conservation du texte original: $cleanedIndic"
                                                )
                                                cleanedIndic
                                            }
                                        } catch (e: Exception) {
                                            println(
                                                    "Erreur lors de la conversion de l'indication $indicStr: ${e.message}"
                                            )
                                            e.printStackTrace()
                                            indicStr.toString() // En cas d'erreur, conserver la
                                            // chaîne originale
                                        }
                                    }

                            // Convertir en JSON
                            val especesJson = json.encodeToString(especes)
                            val indicationsJson = json.encodeToString(indications)

                            // Créer l'entité
                            val foodEntity =
                                    FoodEntity(
                                            uuid = food.UUID,
                                            nameDef = food.nom ?: "",
                                            groupAlim =
                                                    try {
                                                        GroupAlim.valueOf(food.group ?: "").ordinal
                                                    } catch (e: Exception) {
                                                        println(
                                                                "Erreur lors de la conversion du groupe: ${e.message}"
                                                        )
                                                        0
                                                    },
                                            typeAlim =
                                                    try {
                                                        FoodKind.valueOf(food.foodKind ?: "")
                                                                .ordinal
                                                    } catch (e: Exception) {
                                                        println(
                                                                "Erreur lors de la conversion du type: ${e.message}"
                                                        )
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
                                            name = food.nom ?: "",
                                            quantite = 0f,
                                            especesJson = especesJson,
                                            indicationsJson = indicationsJson
                                    )

                            // Insérer l'aliment
                            foodDao.insert(foodEntity)
                            println("Aliment inséré avec succès: ${foodEntity.nameDef}")

                            // Traiter les valeurs nutritionnelles
                            processNutrientValues(food, nonResolvedNutrients)

                            importCount++
                        } catch (e: Exception) {
                            errorCount++
                            println(
                                    "Erreur lors de la création de l'aliment ${food.nom} (${food.UUID}): ${e.message}"
                            )
                            e.printStackTrace()

                            importErrors.add(
                                    "Erreur lors de la création de l'aliment ${food.nom} (${food.UUID}): ${e.message}"
                            )

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
                                                RefRation = null,
                                                RefAlimUnif = null,
                                                especesJson = "[]",
                                                indicationsJson = "[]",
                                                name = food.nom,
                                                quantite = 0f
                                        )

                                foodDao.insert(minimalFood)
                                importCount++
                                println("Aliment minimal inséré avec succès pour ${food.nom}")
                            } catch (e2: Exception) {
                                println("Échec de l'insertion de l'aliment minimal: ${e2.message}")
                                e2.printStackTrace()
                            }
                        }
                    } else {
                        // L'aliment existe déjà, le mettre à jour
                        try {
                            println(
                                    "Mise à jour de l'aliment existant: ${existingFood.nameDef} (${existingFood.uuid})"
                            )
                            updateCount++

                            // Préserver la référence à la ration si elle existe
                            val existingRationRef = existingFood.RefRation

                            // Convertir les espèces
                            val especes =
                                    food.Especes.map { especeStr ->
                                        try {
                                            // Nettoyer la chaîne d'espèce
                                            val cleanedEspece =
                                                    especeStr
                                                            .replace("[", "")
                                                            .replace("]", "")
                                                            .replace("\"", "")
                                                            .trim()

                                            println(
                                                    "Chaîne d'espèce nettoyée (mise à jour): $especeStr -> $cleanedEspece"
                                            )

                                            // Essayer plusieurs stratégies pour reconnaître
                                            // l'espèce
                                            val espece = Espece.getFromString(cleanedEspece)
                                            if (espece != null) {
                                                println(
                                                        "Espèce reconnue (mise à jour): $cleanedEspece -> ${espece.name}"
                                                )
                                                espece.name
                                            } else {
                                                println(
                                                        "Espèce non reconnue (mise à jour), conservation du texte original: $cleanedEspece"
                                                )
                                                cleanedEspece
                                            }
                                        } catch (e: Exception) {
                                            println(
                                                    "Erreur lors de la conversion de l'espèce $especeStr (mise à jour): ${e.message}"
                                            )
                                            e.printStackTrace()
                                            especeStr // En cas d'erreur, conserver la chaîne
                                            // originale
                                        }
                                    }

                            // Convertir les indications
                            val indications =
                                    food.indication.map { indicStr ->
                                        try {
                                            // Nettoyer la chaîne d'indication
                                            val cleanedIndic =
                                                    indicStr.toString()
                                                            .replace("[", "")
                                                            .replace("]", "")
                                                            .replace("\"", "")
                                                            .trim()

                                            println(
                                                    "Chaîne d'indication nettoyée (mise à jour): $indicStr -> $cleanedIndic"
                                            )

                                            // Essayer de reconnaître l'indication
                                            val indication = AlimIndic.getFromString(cleanedIndic)
                                            if (indication != null) {
                                                println(
                                                        "Indication reconnue (mise à jour): $cleanedIndic -> ${indication.name}"
                                                )
                                                indication.name
                                            } else {
                                                println(
                                                        "Indication non reconnue (mise à jour), conservation du texte original: $cleanedIndic"
                                                )
                                                cleanedIndic
                                            }
                                        } catch (e: Exception) {
                                            println(
                                                    "Erreur lors de la conversion de l'indication $indicStr (mise à jour): ${e.message}"
                                            )
                                            e.printStackTrace()
                                            indicStr.toString() // En cas d'erreur, conserver la
                                            // chaîne originale
                                        }
                                    }

                            // Convertir en JSON
                            val especesJson = json.encodeToString(especes)
                            val indicationsJson = json.encodeToString(indications)

                            // Mettre à jour l'aliment
                            val updatedFood =
                                    existingFood.copy(
                                            nameDef = food.nom ?: existingFood.nameDef,
                                            ingredients = food.ingredients
                                                            ?: existingFood.ingredients,
                                            price = food.prix ?: existingFood.price,
                                            brand = food.marque ?: existingFood.brand,
                                            gamme = food.gamme ?: existingFood.gamme,
                                            especesJson = especesJson,
                                            indicationsJson = indicationsJson,
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
                                importErrors.add(
                                        "Erreur lors de la mise à jour de l'aliment ${food.nom} (${food.UUID}): ${e.message}"
                                )
                                errorCount++
                            }
                        } catch (e: Exception) {
                            println(
                                    "Erreur lors du traitement de mise à jour de l'aliment ${food.nom} (${food.UUID}): ${e.message}"
                            )
                            e.printStackTrace()
                            importErrors.add(
                                    "Erreur lors du traitement de mise à jour de l'aliment ${food.nom} (${food.UUID}): ${e.message}"
                            )
                            errorCount++
                        }
                    }
                } catch (e: Exception) {
                    // Gérer les erreurs générale par aliment
                    errorCount++
                    val errorMsg =
                            "Erreur générale lors du traitement de l'aliment ${food.nom} (${food.UUID}): ${e.message}"
                    println(errorMsg)
                    importErrors.add(errorMsg)
                    e.printStackTrace()
                }
            }

            println("\n===== RÉSULTATS DE L'IMPORTATION =====")
            println("Aliments importés: $importCount")
            println("Aliments mis à jour: $updateCount")
            println("Erreurs: $errorCount")

            // Afficher en détail les erreurs si nécessaire
            if (errorCount > 0) {
                println("\n===== DÉTAIL DES ERREURS =====")
                importErrors.forEachIndexed { index, error ->
                    println("Erreur ${index + 1}: $error")
                }
            }

            // Afficher les nutriments non résolus
            if (nonResolvedNutrients.isNotEmpty()) {
                println("\n===== NUTRIMENTS NON RÉSOLUS =====")
                nonResolvedNutrients.forEach { (nutrient, count) ->
                    println("$nutrient: $count occurrences")
                }
            }

            return@withContext FoodImportResult(
                    importedCount = importCount,
                    updatedCount = updateCount,
                    errorCount = errorCount,
                    deletedCount = 0,
                    totalCount = foodDao.getAllFoods().size,
                    nonResolvedNutrientsCount = nonResolvedNutrients.size,
                    nonResolvedNutrients = nonResolvedNutrients.keys.toList()
            )
        }
    }
    // FIN ZONE PROTÉGÉE

    /**
     * Traite les valeurs nutritionnelles pour un aliment.
     * @param food L'aliment pour lequel traiter les valeurs nutritionnelles
     * @param nonResolvedNutrients Ensemble mutable pour collecter les nutriments non résolus
     */
    // DÉBUT ZONE PROTÉGÉE - NE PAS MODIFIER SANS AUTORISATION EXPRESSE
    // Description: Méthode critique pour le traitement des valeurs nutritionnelles d'un aliment.
    // Cette zone de code gère la suppression des anciennes valeurs, la résolution des nutriments
    // et l'insertion des nouvelles valeurs dans la base de données.
    private suspend fun processNutrientValues(
            food: AlimentEvJson,
            nonResolvedNutrients: MutableMap<String, Int>
    ) {
        val nutrientValues = mutableListOf<NutrientValueEntity>()
        var resolvedCount = 0
        var nonResolvedCount = 0

        // Supprimer d'abord toutes les valeurs nutritionnelles existantes
        try {
            if (nutrientValueDao != null) {
                println(
                        "Suppression des valeurs nutritionnelles existantes pour ${food.nom} (${food.UUID})"
                )
                nutrientValueDao.deleteAllNutrientValuesForAliment(food.UUID)
                println("Valeurs nutritionnelles existantes supprimées avec succès")
            } else {
                println(
                        "ATTENTION: nutrientValueDao est null, impossible de supprimer ou insérer les valeurs nutritionnelles"
                )
                return
            }
        } catch (e: Exception) {
            println(
                    "ERREUR lors de la suppression des valeurs nutritionnelles pour ${food.nom} (${food.UUID}): ${e.message}"
            )
            e.printStackTrace()
            // Continuer malgré l'erreur pour tenter l'insertion
        }

        println("\n===== TRAITEMENT DES NUTRIMENTS POUR ${food.nom} (${food.UUID}) =====")
        println("Nombre de nutriments à traiter: ${food.valMap.size}")

        // Vérifier si la carte des valeurs nutritionnelles est vide
        if (food.valMap.isEmpty()) {
            println(
                    "ATTENTION: Aucune valeur nutritionnelle à traiter pour ${food.nom} (${food.UUID})"
            )
            return
        }

        // Traiter chaque valeur nutritionnelle
        food.valMap.forEach { (key, nutrientQuantity) ->
            try {
                val nutrientKey = nutrientQuantity.nut
                val value = nutrientQuantity.value

                if (nutrientKey.isNullOrBlank()) {
                    println("ATTENTION: Clé de nutriment vide pour ${food.nom} (${food.UUID})")
                    return@forEach
                }

                // Premier essai - résolution directe
                var nutrient =
                        fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver.AllNutrientResolver(
                                nutrientKey
                        )

                if (nutrient != null) {
                    resolvedCount++
                    println("✓ Nutriment résolu: $nutrientKey -> ${nutrient.label} = $value")
                    try {
                        nutrientValues.add(
                                NutrientValueEntity(
                                        refAliment = food.UUID,
                                        nutrientLabel = nutrient.label,
                                        value = value
                                )
                        )
                    } catch (e: Exception) {
                        println(
                                "ERREUR lors de la création de l'entité NutrientValueEntity: ${e.message}"
                        )
                        e.printStackTrace()
                    }
                } else {
                    // Deuxième essai - traitements spéciaux pour certains nutriments connus
                    // mais qui pourraient être écrits différemment
                    val specialCaseResolved =
                            when {
                                nutrientKey.contains("HUMID", ignoreCase = true) ||
                                        nutrientKey.contains("WATER", ignoreCase = true) ||
                                        nutrientKey.contains("EAU", ignoreCase = true) ->
                                        NutrientMain.entries.find { n ->
                                            n.label.equals("humidité", ignoreCase = true)
                                        }
                                nutrientKey.contains("PROT", ignoreCase = true) ||
                                        nutrientKey.contains("PROTEIN", ignoreCase = true) ||
                                        nutrientKey.contains("MAT", ignoreCase = true) ->
                                        NutrientMain.entries.find { n ->
                                            n.label.equals("protéine", ignoreCase = true)
                                        }
                                nutrientKey.contains("LIP", ignoreCase = true) ||
                                        nutrientKey.contains("FAT", ignoreCase = true) ||
                                        nutrientKey.contains("MG", ignoreCase = true) ->
                                        NutrientMain.entries.find { n ->
                                            n.label.equals("lipide", ignoreCase = true)
                                        }
                                nutrientKey.contains("GLUC", ignoreCase = true) ||
                                        nutrientKey.contains("CARB", ignoreCase = true) ||
                                        nutrientKey.contains("CHO", ignoreCase = true) ->
                                        NutrientMain.entries.find { n ->
                                            n.label.equals("glucide", ignoreCase = true)
                                        }
                                nutrientKey.contains("CEND", ignoreCase = true) ||
                                        nutrientKey.contains("ASH", ignoreCase = true) ||
                                        nutrientKey.contains("MM", ignoreCase = true) ->
                                        NutrientMain.entries.find { n ->
                                            n.label.equals("cendre", ignoreCase = true)
                                        }
                                nutrientKey.contains("VIT", ignoreCase = true) &&
                                        nutrientKey.contains("A", ignoreCase = true) ->
                                        NutrientVitam.entries.find { n ->
                                            n.label.equals("VITA", ignoreCase = true)
                                        }
                                nutrientKey.contains("VIT", ignoreCase = true) &&
                                        nutrientKey.contains("C", ignoreCase = true) ->
                                        NutrientVitam.entries.find { n ->
                                            n.label.equals("VITC", ignoreCase = true)
                                        }
                                nutrientKey.contains("VIT", ignoreCase = true) &&
                                        nutrientKey.contains("E", ignoreCase = true) ->
                                        NutrientVitam.entries.find { n ->
                                            n.label.equals("VITE", ignoreCase = true)
                                        }
                                nutrientKey.contains("VIT", ignoreCase = true) &&
                                        nutrientKey.contains("D", ignoreCase = true) ->
                                        NutrientVitam.entries.find { n ->
                                            n.label.equals("VITD", ignoreCase = true)
                                        }
                                nutrientKey.contains("VIT", ignoreCase = true) &&
                                        nutrientKey.contains("B1", ignoreCase = true) ->
                                        NutrientVitam.entries.find { n ->
                                            n.label.equals("VITB1", ignoreCase = true)
                                        }
                                nutrientKey.contains("CHOL", ignoreCase = true) ||
                                        nutrientKey.contains("CHOLEST", ignoreCase = true) ->
                                        NutrientLipid.entries.find { n ->
                                            n.label.equals("CHOLES", ignoreCase = true)
                                        }
                                nutrientKey.contains("OMEG", ignoreCase = true) &&
                                        (nutrientKey.contains("3", ignoreCase = true) ||
                                                nutrientKey.contains("TROIS", ignoreCase = true)) ->
                                        NutrientLipid.entries.find { n ->
                                            n.label.equals("O3", ignoreCase = true)
                                        }
                                nutrientKey.contains("OMEG", ignoreCase = true) &&
                                        (nutrientKey.contains("6", ignoreCase = true) ||
                                                nutrientKey.contains("SIX", ignoreCase = true)) ->
                                        NutrientLipid.entries.find { n ->
                                            n.label.equals("O6", ignoreCase = true)
                                        }
                                else -> null
                            }

                    if (specialCaseResolved != null) {
                        nutrient = specialCaseResolved
                        resolvedCount++
                        println(
                                "✓ Nutriment résolu par cas spécial: $nutrientKey -> ${nutrient.label} = $value"
                        )
                        try {
                            nutrientValues.add(
                                    NutrientValueEntity(
                                            refAliment = food.UUID,
                                            nutrientLabel = nutrient.label,
                                            value = value
                                    )
                            )
                        } catch (e: Exception) {
                            println(
                                    "ERREUR lors de la création de l'entité NutrientValueEntity pour cas spécial: ${e.message}"
                            )
                            e.printStackTrace()
                        }
                    } else {
                        nonResolvedCount++
                        nonResolvedNutrients[nutrientKey] =
                                (nonResolvedNutrients[nutrientKey] ?: 0) + 1
                        println("✗ Nutriment NON résolu: $nutrientKey = $value")
                    }
                }
            } catch (e: Exception) {
                println("ERREUR lors du traitement de la valeur nutritionnelle $key: ${e.message}")
                e.printStackTrace()
            }
        }

        println("Résultats du traitement des nutriments:")
        println("- ${resolvedCount} nutriments résolus")
        println("- ${nonResolvedCount} nutriments non résolus")

        // Insérer toutes les valeurs nutritionnelles
        if (nutrientValues.isNotEmpty()) {
            try {
                if (nutrientValueDao != null) {
                    println(
                            "Tentative d'insertion de ${nutrientValues.size} valeurs nutritionnelles pour ${food.nom} (${food.UUID})"
                    )
                    nutrientValueDao.insertNutrientValues(nutrientValues)
                    println(
                            "${nutrientValues.size} valeurs nutritionnelles insérées avec succès pour ${food.nom} (${food.UUID})"
                    )
                } else {
                    println(
                            "ATTENTION: nutrientValueDao est null, ${nutrientValues.size} valeurs nutritionnelles prêtes mais non insérées"
                    )
                }
            } catch (e: Exception) {
                println(
                        "ERREUR lors de l'insertion des valeurs nutritionnelles pour ${food.nom} (${food.UUID}): ${e.message}"
                )
                e.printStackTrace()

                // Tenter d'insérer les valeurs une par une en cas d'erreur sur l'insertion en bloc
                if (nutrientValueDao != null) {
                    println("Tentative d'insertion des valeurs nutritionnelles une par une...")
                    var successCount = 0
                    nutrientValues.forEach { nutrientValue ->
                        try {
                            // Utiliser insertNutrientValues avec une liste contenant un seul
                            // élément
                            // au lieu de insertNutrientValue qui n'existe pas
                            nutrientValueDao.insertNutrientValues(listOf(nutrientValue))
                            successCount++
                        } catch (innerE: Exception) {
                            println(
                                    "ERREUR lors de l'insertion de la valeur nutritionnelle ${nutrientValue.nutrientLabel}: ${innerE.message}"
                            )
                        }
                    }
                    println(
                            "$successCount/${nutrientValues.size} valeurs nutritionnelles insérées individuellement avec succès"
                    )
                }
            }
        } else {
            println("Aucune valeur nutritionnelle à insérer pour ${food.nom} (${food.UUID})")
        }
        println("===== FIN DU TRAITEMENT DES NUTRIMENTS =====\n")
    }
    // FIN ZONE PROTÉGÉE

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
    // DÉBUT ZONE PROTÉGÉE - NE PAS MODIFIER SANS AUTORISATION EXPRESSE
    // Description: Méthode critique pour la mise à jour d'un aliment existant.
    // Cette zone de code gère la mise à jour des propriétés de l'aliment et de ses valeurs
    // nutritionnelles.
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
                // l'existant pour éviter les problèmes de clé étrangère
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

                // Insérer les nouvelles valeurs nutritionnelles
                if (nutrientValueDao != null && nutrientValues.isNotEmpty()) {
                    println(
                            "DEBUG DatabaseFoodRepository: Insertion de ${nutrientValues.size} valeurs nutritionnelles"
                    )
                    try {
                        nutrientValueDao.insertNutrientValues(nutrientValues)
                        println(
                                "DEBUG DatabaseFoodRepository: Valeurs nutritionnelles insérées avec succès"
                        )
                    } catch (e: Exception) {
                        println(
                                "DEBUG DatabaseFoodRepository: ERREUR lors de l'insertion des valeurs nutritionnelles: ${e.message}"
                        )
                        e.printStackTrace()
                        throw e
                    }
                } else {
                    println(
                            "DEBUG DatabaseFoodRepository: Aucune valeur nutritionnelle à insérer ou nutrientValueDao est null"
                    )
                }

                println(
                        "DEBUG DatabaseFoodRepository: Mise à jour de l'aliment terminée avec succès"
                )
                return@withContext
            } catch (e: Exception) {
                println(
                        "DEBUG DatabaseFoodRepository: ERREUR globale lors de la mise à jour de l'aliment: ${e.message}"
                )
                e.printStackTrace()
                throw e
            }
        }
    }
    // FIN ZONE PROTÉGÉE

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
