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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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

    // Flow réactif pour notifier les changements
    private val _foodsFlow = MutableStateFlow<List<AlimentEv>>(emptyList())
    private val coroutineScope = CoroutineScope(AppDispatchers.Main)

    init {
        // Initialiser le flow au démarrage
        coroutineScope.launch { refreshFoodsFlow() }
    }

    /** Met à jour le Flow avec la liste actuelle des aliments */
    private suspend fun refreshFoodsFlow() {
        try {
            val foods = getAllFoods()
            _foodsFlow.value = foods
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Insère un aliment dans la base de données.
     * @param food L'aliment à insérer
     */
    override suspend fun insert(food: AlimentEv) {
        withContext(AppDispatchers.IO) { foodDao.insert(food.toFoodEntity()) }
        refreshFoodsFlow()
    }

    /**
     * Met à jour un aliment existant dans la base de données.
     * @param food L'aliment à mettre à jour
     */
    override suspend fun update(food: AlimentEv) {
        withContext(AppDispatchers.IO) { foodDao.update(food.toFoodEntity()) }
        refreshFoodsFlow()
    }

    /**
     * Supprime un aliment de la base de données.
     * @param food L'aliment à supprimer
     */
    override suspend fun delete(food: AlimentEv) {
        withContext(AppDispatchers.IO) { foodDao.delete(food.toFoodEntity()) }
        refreshFoodsFlow()
    }

    /**
     * Récupère tous les aliments stockés dans la base de données.
     * @return Une liste de tous les aliments
     */
    override suspend fun getAllFoods(): List<AlimentEv> {
        return withContext(AppDispatchers.IO) {

            // Récupérer tous les aliments de la base de données
            val foodEntities = foodDao.getAllFoods()

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

            // Récupérer tous les aliments de la base de données
            val foodEntities = foodDao.getAllFoods()

            // Transformer chaque entité en modèle de domaine léger sans les valeurs nutritionnelles
            val result = foodEntities.map { foodEntity -> foodEntity.toAlimentEvLight() }

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
        return _foodsFlow.asStateFlow()
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


            // Récupérer la liste des UUIDs des aliments existants pour vérification rapide
            val existingFoodUUIDs = foodDao.getAllFoods().map { it.uuid }.toSet()

            // Traiter chaque aliment
            foods.forEachIndexed { index, food ->
                try {

                    if (food.UUID.isNullOrBlank()) {
                        val errorMsg = "Erreur: UUID vide pour l'aliment ${food.nom}"
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


                                            // Essayer plusieurs stratégies pour reconnaître
                                            // l'espèce
                                            val espece = Espece.getFromString(cleanedEspece)
                                            if (espece != null) {
                                                espece.name
                                            } else {
                                                cleanedEspece
                                            }
                                        } catch (e: Exception) {
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


                                            // Essayer de reconnaître l'indication
                                            val indication = AlimIndic.getFromString(cleanedIndic)
                                            if (indication != null) {
                                                indication.name
                                            } else {
                                                cleanedIndic
                                            }
                                        } catch (e: Exception) {
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
                                                        0
                                                    },
                                            typeAlim =
                                                    try {
                                                        FoodKind.valueOf(food.foodKind ?: "")
                                                                .ordinal
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
                                            name = food.nom ?: "",
                                            quantite = 0f,
                                            especesJson = especesJson,
                                            indicationsJson = indicationsJson
                                    )

                            // Insérer l'aliment
                            foodDao.insert(foodEntity)

                            // Traiter les valeurs nutritionnelles
                            processNutrientValues(food, nonResolvedNutrients)

                            importCount++
                        } catch (e: Exception) {
                            errorCount++
                            e.printStackTrace()

                            importErrors.add(
                                    "Erreur lors de la création de l'aliment ${food.nom} (${food.UUID}): ${e.message}"
                            )

                            // Essayer de créer un aliment minimal en cas d'erreur
                            try {
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
                            } catch (e2: Exception) {
                                e2.printStackTrace()
                            }
                        }
                    } else {
                        // L'aliment existe déjà, le mettre à jour
                        try {
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


                                            // Essayer plusieurs stratégies pour reconnaître
                                            // l'espèce
                                            val espece = Espece.getFromString(cleanedEspece)
                                            if (espece != null) {
                                                espece.name
                                            } else {
                                                cleanedEspece
                                            }
                                        } catch (e: Exception) {
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


                                            // Essayer de reconnaître l'indication
                                            val indication = AlimIndic.getFromString(cleanedIndic)
                                            if (indication != null) {
                                                indication.name
                                            } else {
                                                cleanedIndic
                                            }
                                        } catch (e: Exception) {
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

                                // Traiter les valeurs nutritionnelles
                                processNutrientValues(food, nonResolvedNutrients)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                importErrors.add(
                                        "Erreur lors de la mise à jour de l'aliment ${food.nom} (${food.UUID}): ${e.message}"
                                )
                                errorCount++
                            }
                        } catch (e: Exception) {
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
                    importErrors.add(errorMsg)
                    e.printStackTrace()
                }
            }


            // Afficher en détail les erreurs si nécessaire
            if (errorCount > 0) {
                importErrors.forEachIndexed { index, error ->
                }
            }

            // Afficher les nutriments non résolus
            if (nonResolvedNutrients.isNotEmpty()) {
                nonResolvedNutrients.forEach { (nutrient, count) ->
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
                nutrientValueDao.deleteAllNutrientValuesForAliment(food.UUID)
            } else {
                return
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Continuer malgré l'erreur pour tenter l'insertion
        }

        println("\n===== TRAITEMENT DES NUTRIMENTS POUR ${food.nom} (${food.UUID}) =====")

        // Vérifier si la carte des valeurs nutritionnelles est vide
        if (food.valMap.isEmpty()) {
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
                    try {
                        nutrientValues.add(
                                NutrientValueEntity(
                                        refAliment = food.UUID,
                                        nutrientLabel = nutrient.label,
                                        value = value
                                )
                        )
                    } catch (e: Exception) {
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
                        try {
                            nutrientValues.add(
                                    NutrientValueEntity(
                                            refAliment = food.UUID,
                                            nutrientLabel = nutrient.label,
                                            value = value
                                    )
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        nonResolvedCount++
                        nonResolvedNutrients[nutrientKey] =
                                (nonResolvedNutrients[nutrientKey] ?: 0) + 1
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        // Insérer toutes les valeurs nutritionnelles
        if (nutrientValues.isNotEmpty()) {
            try {
                if (nutrientValueDao != null) {
                    nutrientValueDao.insertNutrientValues(nutrientValues)
                } else {
                }
            } catch (e: Exception) {
                e.printStackTrace()

                // Tenter d'insérer les valeurs une par une en cas d'erreur sur l'insertion en bloc
                if (nutrientValueDao != null) {
                    var successCount = 0
                    nutrientValues.forEach { nutrientValue ->
                        try {
                            // Utiliser insertNutrientValues avec une liste contenant un seul
                            // élément
                            // au lieu de insertNutrientValue qui n'existe pas
                            nutrientValueDao.insertNutrientValues(listOf(nutrientValue))
                            successCount++
                        } catch (innerE: Exception) {
                        }
                    }
                }
            }
        } else {
            println("Aucune valeur nutritionnelle à insérer pour ${food.nom} (${food.UUID})")
        }
    }
    // FIN ZONE PROTÉGÉE

    /**
     * Insère un aliment avec toutes ses propriétés associées.
     * @param food L'aliment à insérer
     */
    override suspend fun insertFood(food: AlimentEv) {
        withContext(AppDispatchers.IO) {
            try {
                // Convertir en FoodEntity et insérer
                val foodEntity = food.toFoodEntity().copy(RefRation = null)
                foodDao.insert(foodEntity)

                // Ajout d'un espèce par défaut "AUTRE" si la liste est vide pour éviter l'erreur de
                // clé étrangère
                if (food.especes.isEmpty()) {
                    // Pas d'insertion d'espèce, on évite l'erreur de clé étrangère
                } else {
                    // Insérer les espèces associées si disponibles
                    val especeEntities =
                            food.especes.map { espece ->
                                fr.vetbrain.vetnutri_mp.DataBase.EspeceAlimentEntity(
                                        refAliment = food.uuid,
                                        espece = espece
                                )
                            }
                    if (especeEntities.isNotEmpty()) {
                        foodDao.insertEspeces(especeEntities)
                    }
                }

                // Traiter les valeurs nutritionnelles
                val nutrientValues = food.valMap.toNutrientValueEntities(food.uuid)
                if (nutrientValueDao != null && nutrientValues.isNotEmpty()) {
                    nutrientValueDao.insertNutrientValues(nutrientValues)
                }


                // Mettre à jour le Flow pour notifier les observateurs
                refreshFoodsFlow()
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
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

            // Récupérer les espèces
            val especeEntities =
                    try {
                        foodDao.getEspecesForAliment(uuid)
                    } catch (e: Exception) {
                        emptyList()
                    }

            // Récupérer les valeurs nutritionnelles
            val nutrientValues =
                    if (nutrientValueDao != null) {
                        nutrientValueDao.getNutrientValues(uuid)
                    } else {
                        emptyList()
                    }

            return@withContext foodEntity.toAlimentEv(
                    especes = especeEntities,
                    nutrientValues = nutrientValues
            )
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
                    throw Exception("Aliment non trouvé dans la base de données: ${food.uuid}")
                }


                // Au lieu de modifier toute l'entité, on garde la référence à la ration de
                // l'existant pour éviter les problèmes de clé étrangère
                val foodEntity = food.toFoodEntity().copy(RefRation = existingFood.RefRation)

                try {
                    // Mettre à jour l'entité principale
                    foodDao.update(foodEntity)
                } catch (e: Exception) {

                    // Plan B: si la mise à jour échoue, on essaie de mettre à jour sans la
                    // référence à la ration
                    val updatedEntity = foodEntity.copy(RefRation = null)
                    foodDao.update(updatedEntity)
                }

                // Supprimer les anciennes espèces
                try {
                    foodDao.deleteEspecesForAliment(food.uuid)

                    // Ajouter les nouvelles espèces si disponibles
                    if (food.especes.isNotEmpty()) {
                        val especeEntities =
                                food.especes.map { espece ->
                                    fr.vetbrain.vetnutri_mp.DataBase.EspeceAlimentEntity(
                                            refAliment = food.uuid,
                                            espece = espece
                                    )
                                }
                        foodDao.insertEspeces(especeEntities)
                    } else {
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Supprimer toutes les anciennes valeurs nutritionnelles quelle que soit la
                // situation
                if (nutrientValueDao != null) {
                    nutrientValueDao.deleteAllNutrientValuesForAliment(food.uuid)
                } else {
                }

                // Seulement si des valeurs nutritionnelles existent et que le DAO existe, les
                // ajouter
                val nutrientValues = food.valMap.toNutrientValueEntities(food.uuid)

                // Insérer les nouvelles valeurs nutritionnelles
                if (nutrientValueDao != null && nutrientValues.isNotEmpty()) {
                    try {
                        nutrientValueDao.insertNutrientValues(nutrientValues)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        throw e
                    }
                } else {
                }


                // Mettre à jour le Flow pour notifier les observateurs
                refreshFoodsFlow()
                return@withContext
            } catch (e: Exception) {
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
