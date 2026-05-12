package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentEvJson
import fr.vetbrain.vetnutri_mp.Data.AlimentEvLight
import fr.vetbrain.vetnutri_mp.DataBase.AlimentBiblioRefDao
import fr.vetbrain.vetnutri_mp.DataBase.AlimentBiblioRefEntity
import fr.vetbrain.vetnutri_mp.DataBase.BiblioRefDao
import fr.vetbrain.vetnutri_mp.DataBase.CustomNutrientDao
import fr.vetbrain.vetnutri_mp.DataBase.CustomNutrientEntity
import fr.vetbrain.vetnutri_mp.DataBase.FoodDao
import fr.vetbrain.vetnutri_mp.DataBase.FoodEntity
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toDomain
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toAlimentEv
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toAlimentEvLight
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toFoodEntity
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toNutrientValueEntities
import fr.vetbrain.vetnutri_mp.DataBase.NutrientValueDao
import fr.vetbrain.vetnutri_mp.DataBase.NutrientValueEntity
import fr.vetbrain.vetnutri_mp.Enumer.AlimIndic
import fr.vetbrain.vetnutri_mp.Enumer.CustomNutrient
import fr.vetbrain.vetnutri_mp.Enumer.CustomNutrientRegistry
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.FoodKindResolver
import fr.vetbrain.vetnutri_mp.Enumer.GroupAlim
import fr.vetbrain.vetnutri_mp.Enumer.MainNutrientEnum
import fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam
import fr.vetbrain.vetnutri_mp.Enumer.UnitEnum
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import fr.vetbrain.vetnutri_mp.Utils.DatabaseChangeNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Implémentation de FoodRepository utilisant une base de données SQLite. Cette classe gère les
 * opérations CRUD pour les aliments.
 */
class DatabaseFoodRepository(
        private val foodDao: FoodDao,
        private val nutrientValueDao: NutrientValueDao?,
        private val customNutrientDao: CustomNutrientDao? = null,
        private val alimentBiblioRefDao: AlimentBiblioRefDao? = null,
        private val biblioRefDao: BiblioRefDao? = null
) : FoodRepository {
    private val json = Json { ignoreUnknownKeys = true }

    // Flow réactif pour notifier les changements
    private val _foodsFlow = MutableStateFlow<List<AlimentEv>>(emptyList())
    private val coroutineScope = CoroutineScope(AppDispatchers.Main)

    // Cache en mémoire pour les aliments avec métadonnées améliorées
    private var cachedFoods: List<AlimentEv>? = null
    private var lastCacheTime: Long = 0
    private val cacheValidityDuration = 5 * 60 * 1000L // 5 minutes

    // Mode batch pour désactiver les refresh coûteux à chaque insert/update
    // KMP: éviter @Volatile en commonMain
    private var isBatchMode: Boolean = false

    init {
        coroutineScope.launch { loadCustomNutrientsFromDb() }
    }


    // Cache LRU pour les recherches (max 50 entrées, éviction par accès)
    private val maxSearchCacheEntries = 50
    private val searchCache: LinkedHashMap<String, List<AlimentEv>> =
        object : LinkedHashMap<String, List<AlimentEv>>(maxSearchCacheEntries + 1, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, List<AlimentEv>>) =
                size > maxSearchCacheEntries
        }
    private val searchCacheTime = mutableMapOf<String, Long>()
    fun beginBatch() {
        isBatchMode = true
    }
    fun endBatch() {
        isBatchMode = false
        coroutineScope.launch { refreshFoodsFlow() }
    }

    /** Vide le cache en mémoire pour forcer un rechargement des données */
    fun clearCache() {
        cachedFoods = null
        lastCacheTime = 0
        searchCache.clear()
        searchCacheTime.clear()
    }

    private suspend fun loadCustomNutrientsFromDb() {
        try {
            customNutrientDao?.getAll()?.forEach { entity ->
                val ue = try { UnitEnum.valueOf(entity.ueCode) } catch (_: Exception) { UnitEnum.BUg }
                val category = try { MainNutrientEnum.valueOf(entity.categoryCode) } catch (_: Exception) { MainNutrientEnum.OTHER }
                val nutrient = CustomNutrient(
                    displayName = entity.displayName,
                    label = entity.label,
                    unite = entity.unite,
                    ue = ue,
                    category = category
                )
                CustomNutrientRegistry.register(nutrient)
            }
        } catch (_: Exception) { }
    }

    private suspend fun saveCustomNutrientsFromFood(food: fr.vetbrain.vetnutri_mp.Data.AlimentEv) {
        val dao = customNutrientDao ?: return
        val entities = food.valMap.keys
            .filterIsInstance<CustomNutrient>()
            .map { n ->
                CustomNutrientEntity(
                    label = n.label,
                    displayName = n.nameToString(),
                    unite = n.unite,
                    ueCode = n.ue.name,
                    categoryCode = n.getMNE().name
                )
            }
        if (entities.isNotEmpty()) dao.insertOrReplaceAll(entities)
    }


    private fun pruneSearchCacheIfNeeded() {
        // L'éviction LRU est gérée automatiquement par LinkedHashMap.removeEldestEntry
        // On nettoie seulement les entrées expirées dans searchCacheTime
        val now = Clock.System.now().toEpochMilliseconds()
        val expired = searchCacheTime.entries
            .filter { now - it.value > cacheValidityDuration }
            .map { it.key }
        expired.forEach { key ->
            searchCache.remove(key)
            searchCacheTime.remove(key)
        }
    }

    private fun parseDateToInstantOrNull(rawDate: String?): Instant? {
        val date = rawDate?.trim().orEmpty()
        if (date.isEmpty()) return null
        return try {
            Instant.parse(date)
        } catch (_: Exception) {
            val datePart = if (date.length >= 10) date.substring(0, 10) else date
            try {
                Instant.parse("${LocalDate.parse(datePart)}T00:00:00Z")
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun shouldUpdateFood(
            existing: FoodEntity,
            incomingDate: String?,
            importOnlyIfNewer: Boolean
    ): Boolean {
        if (!importOnlyIfNewer) return true
        val incomingInstant = parseDateToInstantOrNull(incomingDate)
        val existingInstant = parseDateToInstantOrNull(existing.lastUpdateDate)
        if (incomingInstant == null || existingInstant == null) return true
        return incomingInstant > existingInstant
    }

    /**
     * Recherche d'aliments avec cache intelligent
     */
    suspend fun searchFoodsOptimized(
        searchQuery: String = "",
        espece: Espece? = null,
        groupAlim: GroupAlim? = null,
        forceRefresh: Boolean = false
    ): List<AlimentEv> {
        val cacheKey = "${searchQuery}:${espece}:${groupAlim}"

        // Vérifier le cache si pas de refresh forcé
        if (!forceRefresh) {
            val cachedTime = searchCacheTime[cacheKey]
            if (cachedTime != null) {
                val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
                if (now - cachedTime < cacheValidityDuration) {
                    return searchCache[cacheKey] ?: emptyList()
                }
                searchCache.remove(cacheKey)
                searchCacheTime.remove(cacheKey)
            }
        }

        val normalizedQuery = searchQuery.trim()
        val foodEntities =
                if (normalizedQuery.isBlank() && groupAlim == null && espece == null) {
                        foodDao.findAll()
                } else {
                        foodDao.searchFoodsFiltered(
                                query = normalizedQuery,
                                groupAlim = groupAlim?.ordinal,
                                especeText = espece?.name
                        )
                }

        val filteredFoods =
                foodEntities
                        .map { it.toAlimentEv() }
                        .filter { aliment ->
                                // Garde-fou pour conserver le comportement métier
                                val especeMatches =
                                        espece == null || aliment.especes.contains(espece.name)
                                val groupMatches = groupAlim == null || aliment.group == groupAlim
                                especeMatches && groupMatches
                        }

        // Mettre en cache le résultat
        searchCache[cacheKey] = filteredFoods
        searchCacheTime[cacheKey] = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        pruneSearchCacheIfNeeded()

        return filteredFoods
    }

    /**
     * Importe/mettre à jour en LOT une liste d'aliments du domaine. Optimisé: résolution existence
     * par Set d'UUID, insert/update par lots, suppression groupée des nutriments puis insertion
     * groupée.
     */
    suspend fun importFoodsDomain(
            aliments: List<AlimentEv>,
            mergeNutrients: Boolean = false,
            importOnlyIfNewer: Boolean = false
    ): FoodImportResult {
        return withContext(AppDispatchers.IO) {
            var importCount: Int = 0
            var updateCount: Int = 0
            var errorCount: Int = 0
            val nonResolvedNutrients: MutableMap<String, Int> = mutableMapOf()
            try {
                beginBatch()
                
                val existingFoodUUIDs: Set<String> =
                        try {
                            foodDao.getAllFoodIds().toSet()
                        } catch (_: Exception) {
                            emptySet()
                        }
                val batchSize = 200
                aliments.chunked(batchSize).forEach { batch ->
                    val newEntities: MutableList<FoodEntity> = mutableListOf()
                    val updateEntities: MutableList<FoodEntity> = mutableListOf()
                    val updateIds: MutableList<String> = mutableListOf()
                    val batchNutrientValues: MutableList<NutrientValueEntity> = mutableListOf()

                    // Pré-traiter insert vs update (batch)
                    batch.forEach { aliment ->
                        try {
                            if (aliment.uuid.isBlank()) {
                                errorCount++
                                return@forEach
                            }
                            val belongs: Boolean = existingFoodUUIDs.contains(aliment.uuid)
                            if (!belongs) {
                                val entity: FoodEntity =
                                        aliment.toFoodEntity().copy(RefRation = null)
                                newEntities.add(entity)
                                val nutrientValues =
                                        aliment.valMap.toNutrientValueEntities(aliment.uuid)
                                batchNutrientValues.addAll(nutrientValues)
                            } else {
                                updateIds.add(aliment.uuid)
                            }
                        } catch (_: Exception) {
                            errorCount++
                        }
                    }

                    // Préparer les updates en se basant sur l'état existant (batch)
                    if (updateIds.isNotEmpty()) {
                        val existants: Map<String, FoodEntity> =
                                try {
                                    foodDao.getFoodsByIds(updateIds).associateBy { it.uuid }
                                } catch (_: Exception) {
                                    emptyMap()
                                }
                        batch.forEach { aliment ->
                            val id: String = aliment.uuid
                            if (existants.containsKey(id)) {
                                try {
                                    val existing: FoodEntity = existants[id]!!
                                    if (!shouldUpdateFood(
                                                    existing,
                                                    aliment.lastUpdateDate,
                                                    importOnlyIfNewer
                                            )) {
                                        return@forEach
                                    }
                                    val updated: FoodEntity =
                                            aliment.toFoodEntity()
                                                    .copy(RefRation = existing.RefRation)
                                    updateEntities.add(updated)
                                    batchNutrientValues.addAll(
                                            aliment.valMap.toNutrientValueEntities(aliment.uuid)
                                    )
                                } catch (_: Exception) {
                                    errorCount++
                                }
                            }
                        }
                    }

                    // Inserts/updates en lots (batch)
                    if (newEntities.isNotEmpty()) {
                        newEntities.chunked(500).forEach { part ->
                            try {
                                foodDao.insertFoods(part)
                                importCount += part.size
                            } catch (_: Exception) {
                                part.forEach { entity ->
                                    try {
                                        foodDao.insertFoods(listOf(entity))
                                        importCount++
                                    } catch (_: Exception) {
                                        errorCount++
                                    }
                                }
                            }
                        }
                    }
                    if (updateEntities.isNotEmpty()) {
                        updateEntities.chunked(500).forEach { part ->
                            try {
                                foodDao.updateFoods(part)
                                updateCount += part.size
                            } catch (_: Exception) {
                                part.forEach { entity ->
                                    try {
                                        foodDao.updateFoods(listOf(entity))
                                        updateCount++
                                    } catch (_: Exception) {
                                        errorCount++
                                    }
                                }
                            }
                        }
                    }

                    // Nettoyer et réinsérer les nutriments (batch)
                    if (batchNutrientValues.isNotEmpty() && nutrientValueDao != null) {
                        val allIds: List<String> = buildList {
                            addAll(newEntities.map { it.uuid })
                            addAll(updateEntities.map { it.uuid })
                        }

                        if (!mergeNutrients && allIds.isNotEmpty()) {
                            try {
                                allIds.chunked(500).forEach { part ->
                                    nutrientValueDao.deleteAllForAliments(part)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        try {
                            batchNutrientValues.chunked(1000).forEach { chunk ->
                                nutrientValueDao.insertNutrientValues(chunk)
                            }
                        } catch (_: Exception) {
                            batchNutrientValues.forEach { nv ->
                                try {
                                    nutrientValueDao.insertNutrientValues(listOf(nv))
                                } catch (_: Exception) {}
                            }
                        }
                    }
                }
            } finally {
                endBatch()

                // Invalider le cache après l'import
                clearCache()

                // Notifier les changements de base de données
                val totalImported = importCount + updateCount
                if (totalImported > 0) {
                    DatabaseChangeNotifier.notifyFoodImport(
                            count = totalImported,
                            source = "importFoodsDomain"
                    )
                }
            }
            val result = FoodImportResult(
                    importedCount = importCount,
                    updatedCount = updateCount,
                    errorCount = errorCount,
                    deletedCount = 0,
                    totalCount =
                            try {
                                foodDao.getAllFoods().size
                            } catch (_: Exception) {
                                0
                            },
                    nonResolvedNutrientsCount = nonResolvedNutrients.size,
                    nonResolvedNutrients = nonResolvedNutrients.keys.toList()
            )
            return@withContext result
        }
    }

    init {
        // Initialiser le flow au démarrage
        coroutineScope.launch {
            try {
                val initialFoods = getAllFoodsLightAsEv()
                _foodsFlow.value = initialFoods
            } catch (e: Exception) {
                _foodsFlow.value = emptyList()
            }
        }
    }

    /** Met à jour le Flow avec la liste actuelle des aliments */
    private suspend fun refreshFoodsFlow() {
        try {
            val foods = getAllFoodsLightAsEv()
            _foodsFlow.value = foods
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Récupère tous les aliments en forçant un rechargement frais depuis la base de données
     * (sans utiliser le cache)
     */
    private suspend fun getAllFoodsFresh(): List<AlimentEv> {
        return withContext(AppDispatchers.IO) {
            val foodEntities = foodDao.getAllFoods()
            if (foodEntities.isEmpty()) return@withContext emptyList()

            // Requête batch unique au lieu de N requêtes individuelles
            val nutrientsByFood: Map<String, List<NutrientValueEntity>> =
                if (nutrientValueDao != null) {
                    try {
                        val uuids = foodEntities.map { it.uuid }
                        // Découper en lots de 500 pour éviter les limites SQL IN (...)
                        uuids.chunked(500)
                            .flatMap { chunk -> nutrientValueDao.getNutrientValuesForAliments(chunk) }
                            .groupBy { it.refAliment }
                    } catch (_: Exception) { emptyMap() }
                } else emptyMap()

            val result = foodEntities.map { foodEntity ->
                foodEntity.toAlimentEv(nutrientValues = nutrientsByFood[foodEntity.uuid] ?: emptyList())
            }

            val currentTime = Clock.System.now().toEpochMilliseconds()
            cachedFoods = result
            lastCacheTime = currentTime
            return@withContext result
        }
    }

    /**
     * Récupère tous les aliments en version légère (sans valeurs nutritionnelles),
     * pour éviter les pics mémoire lors du chargement initial.
     */
    private suspend fun getAllFoodsLightAsEv(): List<AlimentEv> {
        return withContext(AppDispatchers.IO) {
            val foodEntities = foodDao.getAllFoods()
            val result = foodEntities.map { foodEntity ->
                val light = foodEntity.toAlimentEvLight()
                AlimentEv(
                    uuid = light.uuid,
                    nom = light.nom,
                    brand = light.brand,
                    group = light.group,
                    typeAliment = light.typeAliment,
                    gamme = light.gamme,
                    deprecated = light.deprecated,
                    dataB = light.dataB,
                    especes = light.especes.toMutableList(),
                    indicat = light.indicat.toMutableList(),
                    valMap = mutableMapOf()
                )
            }
            return@withContext result
        }
    }

    /**
     * Insère un aliment dans la base de données.
     * @param food L'aliment à insérer
     */
    override suspend fun insert(food: AlimentEv) {
        withContext(AppDispatchers.IO) { foodDao.insertFood(food.toFoodEntity()) }
        // Invalider le cache après insertion
        clearCache()
        if (!isBatchMode) {
            coroutineScope.launch { refreshFoodsFlow() }
        }
    }

    /**
     * Met à jour un aliment existant dans la base de données.
     * @param food L'aliment à mettre à jour
     */
    override suspend fun update(food: AlimentEv) {
        withContext(AppDispatchers.IO) { foodDao.update(food.toFoodEntity()) }
        // Invalider le cache après mise à jour
        clearCache()
        if (!isBatchMode) {
            coroutineScope.launch { refreshFoodsFlow() }
        }
    }

    /**
     * Supprime un aliment de la base de données.
     * @param food L'aliment à supprimer
     */
    override suspend fun delete(food: AlimentEv) {
        withContext(AppDispatchers.IO) { foodDao.delete(food.toFoodEntity()) }
        // Invalider le cache après suppression
        clearCache()
        if (!isBatchMode) {
            coroutineScope.launch { refreshFoodsFlow() }
        }
    }

    /**
     * Récupère tous les aliments stockés dans la base de données.
     * @return Une liste de tous les aliments
     */
    override suspend fun getAllFoods(): List<AlimentEv> {
        return withContext(AppDispatchers.IO) {
            val currentTime = Clock.System.now().toEpochMilliseconds()
            if (cachedFoods != null && (currentTime - lastCacheTime) < cacheValidityDuration) {
                return@withContext cachedFoods!!
            }
            // Déléguer à getAllFoodsFresh() qui utilise déjà la requête batch
            getAllFoodsFresh()
        }
    }

    /** Récupère uniquement les UUID de tous les aliments (optimisé pour des jeux volumineux). */
    suspend fun getAllFoodIds(): Set<String> {
        return withContext(AppDispatchers.IO) { foodDao.getAllFoodIds().toSet() }
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
    override suspend fun importFoods(
            foods: List<AlimentEvJson>,
            mergeNutrients: Boolean,
            importOnlyIfNewer: Boolean
    ): FoodImportResult {
        return withContext(AppDispatchers.IO) {
            var importCount: Int = 0
            var updateCount: Int = 0
            var errorCount: Int = 0
            val nonResolvedNutrients: MutableMap<String, Int> = mutableMapOf()
            val importErrors: MutableList<String> = mutableListOf()
            try {
                beginBatch()
                val existingFoodUUIDs: Set<String> =
                        try {
                            foodDao.getAllFoodIds().toSet()
                        } catch (_: Exception) {
                            emptySet()
                        }
                val newEntities: MutableList<FoodEntity> = mutableListOf()
                val updateEntities: MutableList<FoodEntity> = mutableListOf()
                val updateCandidateIds: MutableList<String> = mutableListOf()
                val updateIds: MutableList<String> = mutableListOf()
                val allNutrientValues: MutableList<NutrientValueEntity> = mutableListOf()
                fun nettoyerChaineBruteBrackets(valeur: String): String {
                    return valeur.replace("[", "").replace("]", "").replace("\"", "").trim()
                }
                fun convertirEspecesList(list: List<String>): List<String> {
                    return list.map { especeStr ->
                        try {
                            val cleanedEspece: String = nettoyerChaineBruteBrackets(especeStr)
                            val espece: Espece? = Espece.getFromString(cleanedEspece)
                            if (espece != null) espece.name else cleanedEspece
                        } catch (_: Exception) {
                            especeStr
                        }
                    }
                }
                fun convertirIndicationsList(list: List<String>): List<String> {
                    return list.map { indicStr ->
                        try {
                            val cleanedIndic: String = nettoyerChaineBruteBrackets(indicStr)
                            val indication: AlimIndic? = AlimIndic.getFromString(cleanedIndic)
                            indication?.name ?: cleanedIndic
                        } catch (_: Exception) {
                            indicStr
                        }
                    }
                }
                fun resoudreCont(rawInput: String?): String {
                    return try {
                        val raw: String = (rawInput ?: "NO").trim()
                        when {
                            raw.equals("YES", ignoreCase = true) ->
                                    fr.vetbrain.vetnutri_mp.Enumer.ContEnum.CAN.name
                            fr.vetbrain.vetnutri_mp.Enumer.ContEnum.entries.any {
                                it.name == raw.uppercase()
                            } -> raw.uppercase()
                            else -> fr.vetbrain.vetnutri_mp.Enumer.ContEnum.NO.name
                        }
                    } catch (_: Exception) {
                        fr.vetbrain.vetnutri_mp.Enumer.ContEnum.NO.name
                    }
                }
                fun genererNutrientValues(food: AlimentEvJson): List<NutrientValueEntity> {
                    if (food.valMap.isEmpty()) return emptyList()
                    val result: MutableList<NutrientValueEntity> = mutableListOf()
                    food.valMap.forEach { (key, nutrientQuantity) ->
                        try {
                            val nutrientKey: String? = nutrientQuantity.nut
                            val value: Double = nutrientQuantity.value
                            if (nutrientKey.isNullOrBlank()) return@forEach
                            var nutrient =
                                    fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver
                                            .AllNutrientResolver(nutrientKey)
                            if (nutrient == null) {
                                val special =
                                        when {
                                            nutrientKey.contains("HUMID", true) ||
                                                    nutrientKey.contains("WATER", true) ||
                                                    nutrientKey.contains("EAU", true) ->
                                                    NutrientMain.entries.find { n ->
                                                        n.label.equals("humidité", true)
                                                    }
                                            nutrientKey.contains("PROT", true) ||
                                                    nutrientKey.contains("PROTEIN", true) ||
                                                    nutrientKey.contains("MAT", true) ->
                                                    NutrientMain.entries.find { n ->
                                                        n.label.equals("protéine", true)
                                                    }
                                            nutrientKey.contains("LIP", true) ||
                                                    nutrientKey.contains("FAT", true) ||
                                                    nutrientKey.contains("MG", true) ->
                                                    NutrientMain.entries.find { n ->
                                                        n.label.equals("lipide", true)
                                                    }
                                            nutrientKey.contains("GLUC", true) ||
                                                    nutrientKey.contains("CARB", true) ||
                                                    nutrientKey.contains("CHO", true) ->
                                                    NutrientMain.entries.find { n ->
                                                        n.label.equals("glucide", true)
                                                    }
                                            nutrientKey.contains("CEND", true) ||
                                                    nutrientKey.contains("ASH", true) ||
                                                    nutrientKey.contains("MM", true) ->
                                                    NutrientMain.entries.find { n ->
                                                        n.label.equals("cendre", true)
                                                    }
                                            nutrientKey.contains("VIT", true) &&
                                                    nutrientKey.contains("A", true) ->
                                                    NutrientVitam.entries.find { n ->
                                                        n.label.equals("VITA", true)
                                                    }
                                            nutrientKey.contains("VIT", true) &&
                                                    nutrientKey.contains("C", true) ->
                                                    NutrientVitam.entries.find { n ->
                                                        n.label.equals("VITC", true)
                                                    }
                                            nutrientKey.contains("VIT", true) &&
                                                    nutrientKey.contains("E", true) ->
                                                    NutrientVitam.entries.find { n ->
                                                        n.label.equals("VITE", true)
                                                    }
                                            nutrientKey.contains("VIT", true) &&
                                                    nutrientKey.contains("D", true) ->
                                                    NutrientVitam.entries.find { n ->
                                                        n.label.equals("VITD", true)
                                                    }
                                            nutrientKey.contains("VIT", true) &&
                                                    nutrientKey.contains("B1", true) ->
                                                    NutrientVitam.entries.find { n ->
                                                        n.label.equals("VITB1", true)
                                                    }
                                            nutrientKey.contains("CHOL", true) ||
                                                    nutrientKey.contains("CHOLEST", true) ->
                                                    NutrientLipid.entries.find { n ->
                                                        n.label.equals("CHOLES", true)
                                                    }
                                            nutrientKey.contains("OMEG", true) &&
                                                    (nutrientKey.contains("3", true) ||
                                                            nutrientKey.contains("TROIS", true)) ->
                                                    NutrientLipid.entries.find { n ->
                                                        n.label.equals("O3", true)
                                                    }
                                            nutrientKey.contains("OMEG", true) &&
                                                    (nutrientKey.contains("6", true) ||
                                                            nutrientKey.contains("SIX", true)) ->
                                                    NutrientLipid.entries.find { n ->
                                                        n.label.equals("O6", true)
                                                    }
                                            else -> null
                                        }
                                if (special != null) nutrient = special
                                else {
                                    nonResolvedNutrients[nutrientKey] =
                                            (nonResolvedNutrients[nutrientKey] ?: 0) + 1
                                }
                            }
                            if (nutrient != null) {
                                result.add(
                                        NutrientValueEntity(
                                                refAliment = food.UUID,
                                                nutrientLabel = nutrient.label,
                                                value = value
                                        )
                                )
                            }
                        } catch (_: Exception) {}
                    }
                    return result
                }
                // Prétraiter les entités
                foods.forEach { food ->
                    try {
                        if (food.UUID.isNullOrBlank()) {
                            importErrors.add("Erreur: UUID vide pour l'aliment ${food.nom}")
                            errorCount++
                            return@forEach
                        }
                        val especes: List<String> = convertirEspecesList(food.Especes)
                        val indications: List<String> = convertirIndicationsList(food.indication)
                        val especesJson: String = json.encodeToString(especes)
                        val indicationsJson: String = json.encodeToString(indications)
                        val belongs: Boolean = existingFoodUUIDs.contains(food.UUID)
                        val contName: String = resoudreCont(if (belongs) null else food.cont)
                        val quantityPres: Double = food.quantInt ?: 0.0
                        if (!belongs) {
                            val entity: FoodEntity =
                                    FoodEntity(
                                            uuid = food.UUID,
                                            nameDef = food.nom ?: "",
                                            groupAlim =
                                                    try {
                                                        GroupAlim.valueOf(food.group ?: "").ordinal
                                                    } catch (_: Exception) {
                                                        0
                                                    },
                                            typeAlim =
                                                    FoodKindResolver.resoudreFoodKindBrut(
                                                                    food.foodKind
                                                            )
                                                            ?.ordinal
                                                            ?: 0,
                                            ingredients = food.ingredients ?: "",
                                            price = food.prix ?: 0.0,
                                            categPrice = food.categoriePrix ?: "",
                                            brand = food.marque ?: "",
                                            gamme = food.gamme ?: "",
                                            unitPres = 0,
                                            quantityPres = quantityPres,
                                            version = 1,
                                            date = "",
                                            cont = contName,
                                            consistent =
                                                    if (contName !=
                                                                    fr.vetbrain.vetnutri_mp.Enumer
                                                                            .ContEnum.NO
                                                                            .name
                                                    )
                                                            1
                                                    else 0,
                                            deprecated = if (food.deprecated) 1 else 0,
                                            DataB = food.DataB ?: "",
                                            RefRation = null,
                                            RefAlimUnif = null,
                                            name = food.nom ?: "",
                                            quantite = 0.0,
                                            lastUpdateDate = food.dateMaj,
                                            imageRef = food.imageRef,
                                            especesJson = especesJson,
                                            indicationsJson = indicationsJson
                                    )
                            newEntities.add(entity)
                            allNutrientValues.addAll(genererNutrientValues(food))
                            importCount++
                        } else {
                            updateCandidateIds.add(food.UUID)
                            // On résoudra RefRation et les champs conservés après avoir fetch tous
                            // les existants
                        }
                    } catch (e: Exception) {
                        errorCount++
                        importErrors.add(
                                "Erreur générale lors du prétraitement de l'aliment ${food.nom} (${food.UUID}): ${e.message}"
                        )
                    }
                }
                if (updateCandidateIds.isNotEmpty()) {
                    val existants: Map<String, FoodEntity> =
                            try {
                                foodDao.getFoodsByIds(updateCandidateIds).associateBy { it.uuid }
                            } catch (_: Exception) {
                                emptyMap()
                            }
                    foods.forEach { food ->
                        if (existants.containsKey(food.UUID)) {
                            try {
                                val existing: FoodEntity = existants[food.UUID]!!
                                if (!shouldUpdateFood(existing, food.dateMaj, importOnlyIfNewer)) {
                                    return@forEach
                                }
                                val especes: List<String> = convertirEspecesList(food.Especes)
                                val indications: List<String> =
                                        convertirIndicationsList(food.indication)
                                val especesJson: String = json.encodeToString(especes)
                                val indicationsJson: String = json.encodeToString(indications)
                                val contName: String =
                                        try {
                                            val raw: String =
                                                    (food.cont ?: existing.cont ?: "NO").trim()
                                            when {
                                                raw.equals("YES", true) ->
                                                        fr.vetbrain.vetnutri_mp.Enumer.ContEnum.CAN
                                                                .name
                                                fr.vetbrain.vetnutri_mp.Enumer.ContEnum.entries
                                                        .any { it.name == raw.uppercase() } ->
                                                        raw.uppercase()
                                                else ->
                                                        fr.vetbrain.vetnutri_mp.Enumer.ContEnum.NO
                                                                .name
                                            }
                                        } catch (_: Exception) {
                                            existing.cont
                                                    ?: fr.vetbrain.vetnutri_mp.Enumer.ContEnum.NO
                                                            .name
                                        }
                                val quantityPres: Double =
                                        food.quantInt ?: existing.quantityPres ?: 0.0
                                val updated: FoodEntity =
                                        existing.copy(
                                                nameDef = food.nom ?: existing.nameDef,
                                                ingredients = food.ingredients
                                                                ?: existing.ingredients,
                                                price = food.prix ?: existing.price,
                                                brand = food.marque ?: existing.brand,
                                                gamme = food.gamme ?: existing.gamme,
                                                categPrice = food.categoriePrix
                                                                ?: existing.categPrice,
                                                deprecated =
                                                        if (food.deprecated == true) 1
                                                        else existing.deprecated,
                                                DataB = food.DataB ?: existing.DataB,
                                                cont = contName,
                                                quantityPres = quantityPres,
                                                especesJson = especesJson,
                                                indicationsJson = indicationsJson,
                                                RefRation = existing.RefRation
                                        )
                                updateEntities.add(updated)
                                allNutrientValues.addAll(genererNutrientValues(food))
                                updateCount++
                                updateIds.add(food.UUID)
                            } catch (e: Exception) {
                                errorCount++
                                importErrors.add(
                                        "Erreur lors de la préparation update ${food.nom} (${food.UUID}): ${e.message}"
                                )
                            }
                        }
                    }
                }
                if (newEntities.isNotEmpty()) {
                    try {
                        newEntities.chunked(500).forEach { batch -> foodDao.insertFoods(batch) }
                    } catch (e: Exception) {
                        errorCount += newEntities.size
                    }
                }
                if (updateEntities.isNotEmpty()) {
                    try {
                        updateEntities.chunked(500).forEach { batch -> foodDao.updateFoods(batch) }
                    } catch (e: Exception) {
                        errorCount += updateEntities.size
                    }
                }
                // Nettoyer et réinsérer les nutriments SEULEMENT si on a des données de nutriments à importer
                if (allNutrientValues.isNotEmpty() && nutrientValueDao != null) {
                    // Supprimer les nutriments existants SEULEMENT si on va les remplacer
                    if (!mergeNutrients && updateIds.isNotEmpty()) {
                        try {
                            updateIds.chunked(500).forEach { part ->
                                nutrientValueDao.deleteAllForAliments(part)
                            }
                        } catch (_: Exception) {}
                    }
                    // Insérer les nouveaux nutriments
                    try {
                        allNutrientValues.chunked(1000).forEach { chunk ->
                            nutrientValueDao.insertNutrientValues(chunk)
                        }
                    } catch (e: Exception) {
                        // Fallback insertion une par une
                        var ok: Int = 0
                        allNutrientValues.forEach { nv ->
                            try {
                                nutrientValueDao.insertNutrientValues(listOf(nv))
                                ok++
                            } catch (_: Exception) {}
                        }
                    }
                }
            } finally {
                endBatch()
            }
            return@withContext FoodImportResult(
                    importedCount = importCount,
                    updatedCount = updateCount,
                    errorCount = errorCount,
                    deletedCount = 0,
                    totalCount =
                            try {
                                foodDao.getAllFoods().size
                            } catch (_: Exception) {
                                0
                            },
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
                                            n.label.equals("VITA", true)
                                        }
                                nutrientKey.contains("VIT", ignoreCase = true) &&
                                        nutrientKey.contains("C", ignoreCase = true) ->
                                        NutrientVitam.entries.find { n ->
                                            n.label.equals("VITC", true)
                                        }
                                nutrientKey.contains("VIT", ignoreCase = true) &&
                                        nutrientKey.contains("E", ignoreCase = true) ->
                                        NutrientVitam.entries.find { n ->
                                            n.label.equals("VITE", true)
                                        }
                                nutrientKey.contains("VIT", ignoreCase = true) &&
                                        nutrientKey.contains("D", ignoreCase = true) ->
                                        NutrientVitam.entries.find { n ->
                                            n.label.equals("VITD", true)
                                        }
                                nutrientKey.contains("VIT", ignoreCase = true) &&
                                        nutrientKey.contains("B1", ignoreCase = true) ->
                                        NutrientVitam.entries.find { n ->
                                            n.label.equals("VITB1", true)
                                        }
                                nutrientKey.contains("CHOL", ignoreCase = true) ||
                                        nutrientKey.contains("CHOLEST", ignoreCase = true) ->
                                        NutrientLipid.entries.find { n ->
                                            n.label.equals("CHOLES", true)
                                        }
                                nutrientKey.contains("OMEG", ignoreCase = true) &&
                                        (nutrientKey.contains("3", ignoreCase = true) ||
                                                nutrientKey.contains("TROIS", ignoreCase = true)) ->
                                        NutrientLipid.entries.find { n ->
                                            n.label.equals("O3", true)
                                        }
                                nutrientKey.contains("OMEG", ignoreCase = true) &&
                                        (nutrientKey.contains("6", ignoreCase = true) ||
                                                nutrientKey.contains("SIX", true)) ->
                                        NutrientLipid.entries.find { n ->
                                            n.label.equals("O6", true)
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
                } else {}
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
                        } catch (innerE: Exception) {}
                    }
                }
            }
        } else {}
    }
    // FIN ZONE PROTÉGÉE

    /**
     * Insère un aliment avec toutes ses propriétés associées.
     * @param food L'aliment à insérer
     */
    override suspend fun insertFood(food: AlimentEv) {
        withContext(AppDispatchers.IO) {
            try {
                println(
                    "[FoodSaveDebug][Repo] insertFood start uuid=${food.uuid} " +
                        "valMapCount=${food.valMap.size}"
                )
                // Convertir en FoodEntity et insérer
                val foodEntity = food.toFoodEntity().copy(RefRation = null)
                foodDao.insertFood(foodEntity)

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
                println(
                    "[FoodSaveDebug][Repo] insertFood nutrientEntities uuid=${food.uuid} " +
                        "count=${nutrientValues.size} " +
                        "sample=${nutrientValues.take(8).joinToString { "${it.nutrientLabel}=${it.value}" }}"
                )
                if (nutrientValueDao != null && nutrientValues.isNotEmpty()) {
                    nutrientValueDao.insertNutrientValues(nutrientValues)
                    val reloaded = nutrientValueDao.getNutrientValues(food.uuid)
                    println(
                        "[FoodSaveDebug][Repo] insertFood DB check uuid=${food.uuid} " +
                            "dbCount=${reloaded.size} " +
                            "sample=${reloaded.take(8).joinToString { "${it.nutrientLabel}=${it.value}" }}"
                    )
                }
                saveCustomNutrientsFromFood(food)

                // Insérer les références bibliographiques associées
                if (alimentBiblioRefDao != null && food.biblioRefs.isNotEmpty()) {
                    val junctions = food.biblioRefs.map { ref ->
                        AlimentBiblioRefEntity(alimentUuid = food.uuid, biblioRefUuid = ref.uuid)
                    }
                    alimentBiblioRefDao.insertAll(junctions)
                }

                clearCache()
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        }

        // Mettre à jour le Flow pour notifier les observateurs (dans le contexte Main)
        if (!isBatchMode) {
            coroutineScope.launch {
                try {
                    refreshFoodsFlow()
                } catch (e: Exception) {
                    // Fallback: essayer de mettre à jour le Flow directement
                    try {
                        val foods = getAllFoodsFresh()
                        _foodsFlow.value = foods
                    } catch (e2: Exception) {
                        // Ignore
                    }
                }
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

            // Récupérer les références bibliographiques associées
            val biblioRefs = if (alimentBiblioRefDao != null && biblioRefDao != null) {
                try {
                    alimentBiblioRefDao.getBiblioRefUuids(uuid)
                        .mapNotNull { refUuid -> biblioRefDao.getBiblioRefById(refUuid)?.toDomain() }
                } catch (_: Exception) { emptyList() }
            } else emptyList()

            return@withContext foodEntity.toAlimentEv(
                    especes = especeEntities,
                    nutrientValues = nutrientValues,
                    biblioRefs = biblioRefs
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
        // Invalider le cache et rafraîchir le Flow pour notifier les observateurs
        clearCache()
        if (!isBatchMode) {
            coroutineScope.launch { refreshFoodsFlow() }
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
        withContext(AppDispatchers.IO) {
            try {
                println(
                    "[FoodSaveDebug][Repo] updateFood start uuid=${food.uuid} " +
                        "valMapCount=${food.valMap.size}"
                )
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
                    } else {}
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Remplacer atomiquement les valeurs nutritionnelles (delete + insert en 1 transaction)
                val replacing = food.valMap.toNutrientValueEntities(food.uuid)
                println(
                    "[FoodSaveDebug][Repo] updateFood replace values uuid=${food.uuid} " +
                        "replaceCount=${replacing.size} " +
                        "sample=${replacing.take(8).joinToString { "${it.nutrientLabel}=${it.value}" }}"
                )
                nutrientValueDao?.replaceNutrientValues(
                        food.uuid,
                        replacing
                )
                if (nutrientValueDao != null) {
                    val reloaded = nutrientValueDao.getNutrientValues(food.uuid)
                    println(
                        "[FoodSaveDebug][Repo] updateFood DB check uuid=${food.uuid} " +
                            "dbCount=${reloaded.size} " +
                            "sample=${reloaded.take(8).joinToString { "${it.nutrientLabel}=${it.value}" }}"
                    )
                }
                saveCustomNutrientsFromFood(food)

                // Remplacer les références bibliographiques associées
                if (alimentBiblioRefDao != null) {
                    alimentBiblioRefDao.deleteForAliment(food.uuid)
                    if (food.biblioRefs.isNotEmpty()) {
                        val junctions = food.biblioRefs.map { ref ->
                            AlimentBiblioRefEntity(alimentUuid = food.uuid, biblioRefUuid = ref.uuid)
                        }
                        alimentBiblioRefDao.insertAll(junctions)
                    }
                }

                clearCache()
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        }

        // Mettre à jour le Flow pour notifier les observateurs (dans le contexte Main)
        if (!isBatchMode) {
            coroutineScope.launch { refreshFoodsFlow() }
        }
    }
    // FIN ZONE PROTÉGÉE

    /**
     * Supprime tous les aliments de la base de données, ainsi que leurs valeurs nutritionnelles
     * associées.
     *
     * ⚠️ MÉTHODE DANGEREUSE - Nécessite une confirmation explicite de l'utilisateur Cette méthode
     * ne doit JAMAIS être appelée automatiquement ou sans action de l'utilisateur.
     *
     * @return Le nombre d'aliments supprimés
     * @throws SecurityException Si la méthode est appelée sans autorisation explicite
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

            // Invalider le cache après la suppression
            clearCache()

            // Notifier les changements de base de données
            DatabaseChangeNotifier.notifyChange(
                    DatabaseChangeNotifier.ChangeType.FOOD_DELETED,
                    "Tous les aliments ont été supprimés",
                    count
            )

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

                // Invalider le cache après la mise à jour
                clearCache()

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

    /**
     * Force le rechargement des données et notifie tous les observateurs Cette méthode est utile
     * après des opérations d'import en lot
     */
    suspend fun forceRefresh() {
        clearCache()
        refreshFoodsFlow()
    }

    /**
     * Obtient le Flow réactif des aliments Les ViewModels peuvent observer ce Flow pour être
     * notifiés des changements
     */
    fun getFoodsFlow(): Flow<List<AlimentEv>> = _foodsFlow

    /**
     * Charge les nutriments spécifiés pour une liste d'aliments depuis la base de données.
     * Cette méthode est optimisée pour charger uniquement les nutriments nécessaires.
     * 
     * @param foodUuids Liste des UUID des aliments
     * @param nutrients Liste des nutriments à charger
     * @return Map avec l'UUID de l'aliment comme clé et une Map de nutriments avec leurs valeurs
     */
    suspend fun loadNutrientsForFoods(
            foodUuids: List<String>,
            nutrients: List<fr.vetbrain.vetnutri_mp.Enumer.Nutrient>
    ): Map<String, Map<fr.vetbrain.vetnutri_mp.Enumer.Nutrient, Double>> {
            return withContext(AppDispatchers.IO) {
                    if (nutrientValueDao == null || foodUuids.isEmpty() || nutrients.isEmpty()) {
                            return@withContext emptyMap()
                    }
                    
                    val nutrientLabels = nutrients.map { it.label }
                    val allNutrientValues = nutrientValueDao.getNutrientValuesForAliments(foodUuids)
                    
                    // Grouper par aliment et filtrer par nutriments demandés
                    val result = mutableMapOf<String, MutableMap<fr.vetbrain.vetnutri_mp.Enumer.Nutrient, Double>>()
                    
                    allNutrientValues.forEach { nutrientValue ->
                            if (nutrientLabels.contains(nutrientValue.nutrientLabel)) {
                                    val nutrient = fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver
                                            .AllNutrientResolver(nutrientValue.nutrientLabel)
                                    if (nutrient != null) {
                                            val alimentMap = result.getOrPut(nutrientValue.refAliment) { mutableMapOf() }
                                            alimentMap[nutrient] = nutrientValue.value
                                    }
                            }
                    }
                    
                    return@withContext result
            }
    }
}
