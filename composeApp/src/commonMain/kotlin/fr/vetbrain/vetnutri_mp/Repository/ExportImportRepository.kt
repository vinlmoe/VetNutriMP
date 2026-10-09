package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.AnimalApi
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.ApiEnvelope
import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.ConsultationKeyword
import fr.vetbrain.vetnutri_mp.Data.ConsultationKeywordApi
import fr.vetbrain.vetnutri_mp.Data.Equation
import fr.vetbrain.vetnutri_mp.Data.FoodApi
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Data.RationApi
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Data.toApi
import fr.vetbrain.vetnutri_mp.Data.toApiRef
import fr.vetbrain.vetnutri_mp.Data.toDomain
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.StadePhysio
import fr.vetbrain.vetnutri_mp.PlatformFile.PlatformFile
import fr.vetbrain.vetnutri_mp.Utils.isDebugBuild
import fr.vetbrain.vetnutri_mp.Utils.encodeEnvelopeToFile
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Repository pour export/import JSON des objets ACTUELS (nouveau format pour future REST API). Ne
 * modifie pas les structures historiques déjà utilisées pour réimporter d'anciens JSON.
 */
class ExportImportRepository(
        private val animalRepository: AnimalRepository,
        private val foodRepository: FoodRepository? = null,
        private val equationRepository: EquationRepository? = null,
        private val referenceRepository: DatabaseReferenceEvRepository? = null,
        private val biblioRepository: BiblioRefRepository? = null,
        private val consultationRepository: ConsultationRepository? = null,
        private val recipeRepository: RecipeRepository? = null,
        private val conseilRepository: ConseilRepository? = null
) {
        class ImportProgressListener(val onProgress: (Double) -> Unit, val onLog: (String) -> Unit)

        private val jsonPretty: Json = Json {
                prettyPrint = isDebugBuild()
                encodeDefaults = true
                ignoreUnknownKeys = true
                explicitNulls = false
        }

        data class ExportSelectionOptions(
                val includeAnimals: Boolean = true,
                val includeFoods: Boolean = true,
                val includeRations: Boolean = false,
                val includeRecipes: Boolean = true,
                val includeEquations: Boolean = true,
                val includeConseils: Boolean = true,
                val includeLinkedFromAnimals: Boolean = true,
                val animalIds: Set<String> = emptySet(),
                val foodIds: Set<String> = emptySet(),
                val recipeIds: Set<String> = emptySet(),
                val referenceIds: Set<String> = emptySet(),
                val equationIds: Set<String> = emptySet(),
                val conseilIds: Set<String> = emptySet()
        )

        /**
         * Exporte l'ensemble des données (animaux, aliments, rations, équations, recettes) au
         * format API.
         */
        suspend fun exportAll(): String {
                return jsonPretty.encodeToString(buildEnvelopeAll())
        }

        suspend fun exportAllEnvelope(): ApiEnvelope {
                return buildEnvelopeAll()
        }

        suspend fun writeEnvelopeToFile(envelope: ApiEnvelope, file: PlatformFile): Result<Unit> {
                return jsonPretty.encodeEnvelopeToFile(envelope, file)
        }

        suspend fun writeAllToFile(file: PlatformFile): Result<Unit> {
                val envelope = buildEnvelopeAll()
                return jsonPretty.encodeEnvelopeToFile(envelope, file)
        }

        private suspend fun buildEnvelopeAll(): ApiEnvelope {
                val domainAnimals = animalRepository.getAllAnimals()
                // Do not turn a repository failure into an apparently complete backup.
                val animalsWithConsultations = if (consultationRepository != null) {
                        domainAnimals.map { animal ->
                                animal.copy(consultations = consultationRepository
                                        .getConsultationsForAnimal(animal.uuid).toMutableList())
                        }
                } else domainAnimals
                if (foodRepository is DatabaseFoodRepository) foodRepository.forceRefresh()
                return ApiEnvelope(
                        version = "2.0.0",
                        generatedAtEpochMs = Clock.System.now().toEpochMilliseconds(),
                        animals = animalsWithConsultations.map { it.toApi() },
                        foods = foodRepository?.getAllFoods()?.map { it.toApi() } ?: emptyList(),
                        rations = animalsWithConsultations.flatMap { animal ->
                                animal.consultations.flatMap { consultation ->
                                        consultation.rations.map { it.toApi() }
                                }
                        },
                        recipes = recipeRepository?.getAllRecipesAsRecette()?.map { it.toApi() } ?: emptyList(),
                        equations = equationRepository?.getAllEquations()?.map { it.toApi() } ?: emptyList(),
                        biblioRefs = biblioRepository?.getAllBiblioRefs()?.first()?.map { it.toApi() } ?: emptyList(),
                        references = referenceRepository?.getAllReferenceEv()?.map { it.toApiRef() } ?: emptyList(),
                        conseils = conseilRepository?.getConseilsActifs()?.getOrThrow()?.map { it.toApi() } ?: emptyList(),
                        consultationKeywords = consultationRepository?.getAllKeywords()?.map {
                                ConsultationKeywordApi(uuid = it.uuid, label = it.label)
                        } ?: emptyList()
                )
        }

        /** Export avec filtres et sélections par type/identifiants. */
        suspend fun exportWithSelection(options: ExportSelectionOptions): String {
                return jsonPretty.encodeToString(buildEnvelopeWithSelection(options))
        }

        suspend fun exportWithSelectionEnvelope(options: ExportSelectionOptions): ApiEnvelope {
                return buildEnvelopeWithSelection(options)
        }

        suspend fun writeSelectionToFile(
                options: ExportSelectionOptions,
                file: PlatformFile
        ): Result<Unit> {
                val envelope = buildEnvelopeWithSelection(options)
                return jsonPretty.encodeEnvelopeToFile(envelope, file)
        }

        private suspend fun buildEnvelopeWithSelection(
                options: ExportSelectionOptions
        ): ApiEnvelope {
                val allDomainAnimals =
                        if (options.includeAnimals) animalRepository.getAllAnimals()
                        else emptyList()
                // Charger les consultations/rations sur les animaux retenus
                val allDomainAnimalsWithConsultations: List<AnimalEv> =
                        if (consultationRepository != null) {
                                allDomainAnimals.map { animal ->
                                        val cons =
                                                try {
                                                        consultationRepository
                                                                .getConsultationsForAnimal(
                                                                        animal.uuid
                                                                )
                                                } catch (_: Exception) {
                                                        emptyList()
                                                }
                                        animal.copy(consultations = cons.toMutableList())
                                }
                        } else allDomainAnimals
                val filteredDomainAnimals =
                        allDomainAnimalsWithConsultations
                                .asSequence()
                                .filter { domainAnimal ->
                                        options.animalIds.isEmpty() ||
                                                options.animalIds.contains(domainAnimal.uuid)
                                }
                                .toList()
                val animals: List<AnimalApi> = filteredDomainAnimals.map { it.toApi() }

                // Déduire les éléments liés aux animaux sélectionnés (aliments, références, équations)
                val linkedFoodIds = mutableSetOf<String>()
                val linkedReferenceIds = mutableSetOf<String>()
                if (options.includeAnimals &&
                                options.includeLinkedFromAnimals &&
                                filteredDomainAnimals.isNotEmpty()
                ) {
                        filteredDomainAnimals.forEach { animal ->
                                animal.consultations.forEach { consult ->
                                        consult.referenceGeneraleId
                                                ?.takeIf { it.isNotBlank() }
                                                ?.let { linkedReferenceIds.add(it) }
                                        consult.referencesMaladies
                                                .filter { it.isNotBlank() }
                                                .forEach { linkedReferenceIds.add(it) }
                                        consult.rations.forEach { ration ->
                                                ration.alimentMutableList.forEach { item ->
                                                        val foodId =
                                                                item.aliment?.uuid
                                                                        ?: item.uuidUnif.takeIf {
                                                                                it.isNotBlank()
                                                                        }
                                                                        ?: item.refAlimUnif
                                                                                ?.takeIf {
                                                                                        it.isNotBlank()
                                                                                }
                                                        if (foodId != null) {
                                                                linkedFoodIds.add(foodId)
                                                        }
                                                }
                                        }
                                }
                        }
                }

                val linkedEquationIds = mutableSetOf<String>()
                if ((options.includeEquations || options.includeLinkedFromAnimals) &&
                                referenceRepository != null
                ) {
                        val referenceIdsToScan =
                                if (options.includeLinkedFromAnimals) linkedReferenceIds
                                else emptySet()
                        referenceIdsToScan.forEach { refId ->
                                try {
                                        val ref =
                                                referenceRepository.getReferenceEvById(refId)
                                        if (ref != null) {
                                                listOfNotNull(
                                                                ref.equationBW,
                                                                ref.equationBEE,
                                                                ref.equationDEcom,
                                                                ref.equationDEraw,
                                                                ref.equationME
                                                ).forEach { linkedEquationIds.add(it.uuid) }
                                                ref.equationsNut.forEach {
                                                        linkedEquationIds.add(it.uuid)
                                                }
                                        }
                                } catch (_: Exception) {}
                        }
                }

                val effectiveFoodIds =
                        if (options.includeLinkedFromAnimals)
                                options.foodIds + linkedFoodIds
                        else options.foodIds
                val effectiveReferenceIds =
                        if (options.includeLinkedFromAnimals)
                                options.referenceIds + linkedReferenceIds
                        else options.referenceIds

                val equationIdsFromReferences = mutableSetOf<String>()
                if ((options.includeEquations || options.referenceIds.isNotEmpty()) &&
                                referenceRepository != null
                ) {
                        effectiveReferenceIds.forEach { refId ->
                                try {
                                        val ref =
                                                referenceRepository.getReferenceEvById(refId)
                                        if (ref != null) {
                                                listOfNotNull(
                                                                ref.equationBW,
                                                                ref.equationBEE,
                                                                ref.equationDEcom,
                                                                ref.equationDEraw,
                                                                ref.equationME
                                                ).forEach {
                                                        equationIdsFromReferences.add(it.uuid)
                                                }
                                                ref.equationsNut.forEach {
                                                        equationIdsFromReferences.add(it.uuid)
                                                }
                                        }
                                } catch (_: Exception) {}
                        }
                }

                val effectiveEquationIds =
                        if (options.includeLinkedFromAnimals)
                                options.equationIds + linkedEquationIds + equationIdsFromReferences
                        else options.equationIds + equationIdsFromReferences

                val shouldIncludeFoods =
                        options.includeFoods ||
                                options.foodIds.isNotEmpty() ||
                                (options.includeLinkedFromAnimals &&
                                        linkedFoodIds.isNotEmpty())
                val shouldIncludeEquations =
                        options.includeEquations ||
                                options.equationIds.isNotEmpty() ||
                                equationIdsFromReferences.isNotEmpty() ||
                                (options.includeLinkedFromAnimals &&
                                        linkedEquationIds.isNotEmpty())
                val shouldIncludeReferences =
                        options.includeEquations ||
                                options.referenceIds.isNotEmpty() ||
                                (options.includeLinkedFromAnimals &&
                                        linkedReferenceIds.isNotEmpty())

                // Forcer le chargement frais des aliments (avec nutriments) avant export filtré
                if (shouldIncludeFoods && foodRepository is DatabaseFoodRepository) {
                        try {
                                foodRepository.forceRefresh()
                        } catch (_: Exception) {}
                }
                val allFoods =
                        if (shouldIncludeFoods)
                                (foodRepository?.getAllFoods() ?: emptyList())
                        else emptyList()
                val foods: List<FoodApi> =
                        allFoods.asSequence()
                                .filter {
                                        // Logique de filtrage des aliments :
                                        // - Si foodIds est vide ET animalIds est vide → export général : exporter TOUS les aliments
                                        // - Si foodIds est vide ET animalIds n'est PAS vide → export sélectif : exporter AUCUN aliment (l'animal n'en utilise pas)
                                        // - Si foodIds n'est pas vide → exporter seulement ceux dans la liste
                                        when {
                                                effectiveFoodIds.isNotEmpty() -> effectiveFoodIds.contains(it.uuid)
                                                options.animalIds.isEmpty() -> true // Export général : tous les aliments
                                                else -> false // Export sélectif sans aliments spécifiés : aucun aliment
                                        }
                                }
                                .map { it.toApi() }
                                .toList()

                val rationsList2: List<RationApi> =
                        if (options.includeRations) {
                                filteredDomainAnimals
                                        .asSequence()
                                        .flatMap { animal: AnimalEv ->
                                                animal.consultations.asSequence().flatMap {
                                                        consult: ConsultationEv ->
                                                        consult.rations.asSequence().map {
                                                                ration: Ration ->
                                                                ration.toApi()
                                                        }
                                                }
                                        }
                                        .toList()
                        } else emptyList()
                val allEquations =
                        if (shouldIncludeEquations)
                                (equationRepository?.getAllEquations() ?: emptyList())
                        else emptyList()
                val equations: List<fr.vetbrain.vetnutri_mp.Data.EquationApi> =
                        allEquations.asSequence()
                                .filter {
                                        // Logique de filtrage des équations :
                                        // - Si equationIds est vide ET animalIds est vide → export général : exporter TOUTES les équations
                                        // - Si equationIds est vide ET animalIds n'est PAS vide → export sélectif : exporter AUCUNE équation (l'animal n'en utilise pas)
                                        // - Si equationIds n'est pas vide → exporter seulement celles dans la liste
                                        when {
                                                effectiveEquationIds.isNotEmpty() -> effectiveEquationIds.contains(it.uuid)
                                                options.animalIds.isEmpty() -> true // Export général : toutes les équations
                                                else -> false // Export sélectif sans équations spécifiées : aucune équation
                                        }
                                }
                                .map { it.toApi() }
                                .toList()
                val allReferences =
                        if (shouldIncludeReferences) {
                                referenceRepository?.getAllReferenceEv() ?: emptyList()
                        } else emptyList()
                val references: List<fr.vetbrain.vetnutri_mp.Data.ReferenceEvApi> =
                        allReferences.asSequence()
                                .filter {
                                        // Logique de filtrage des références :
                                        // - Si referenceIds est vide ET animalIds est vide → export général : exporter TOUTES les références
                                        // - Si referenceIds est vide ET animalIds n'est PAS vide → export sélectif : exporter AUCUNE référence (l'animal n'en utilise pas)
                                        // - Si referenceIds n'est pas vide → exporter seulement celles dans la liste
                                        when {
                                                effectiveReferenceIds.isNotEmpty() -> effectiveReferenceIds.contains(it.uuid)
                                                options.animalIds.isEmpty() -> true // Export général : toutes les références
                                                else -> false // Export sélectif sans références spécifiées : aucune référence
                                        }
                                }
                                .map { it.toApiRef() }
                                .toList()

                // Récupérer les recettes selon les options
                val recipes =
                        if (options.includeRecipes) {
                                val allRecipes =
                                        recipeRepository?.getAllRecipesAsRecette() ?: emptyList()
                                allRecipes
                                        .asSequence()
                                        .filter {
                                                when {
                                                        options.recipeIds.isNotEmpty() ->
                                                                options.recipeIds.contains(it.uuid)
                                                        options.animalIds.isEmpty() -> true
                                                        else -> false
                                                }
                                        }
                                        .map { it.toApi() }
                                        .toList()
                        } else emptyList()

                // Récupérer les conseils selon les options
                val conseils =
                        if (options.includeConseils) {
                                try {
                                        val allConseils =
                                                conseilRepository?.getConseilsActifs()?.getOrThrow()
                                                        ?: emptyList()
                                        allConseils
                                                .asSequence()
                                                .filter {
                                                        when {
                                                                options.conseilIds.isNotEmpty() ->
                                                                        options.conseilIds.contains(it.id)
                                                                options.animalIds.isEmpty() -> true
                                                                else -> false
                                                        }
                                                }
                                                .map { it.toApi() }
                                                .toList()
                                } catch (_: Exception) {
                                        emptyList()
                                }
                        } else emptyList()

                // Collecter les UUIDs des biblioRefs utilisées par les références et équations exportées
                val biblioRefIdsToExport = mutableSetOf<String>()

                // Collecter depuis les équations exportées
                equations.forEach { eqApi ->
                        // Les équations ont un champ bibRef qui peut être sérialisé différemment
                        // On doit charger l'équation complète pour obtenir sa biblio
                        equationRepository?.getAllEquations()?.find { it.uuid == eqApi.uuid }?.let { eq ->
                                if (eq.bib.uuid.isNotBlank() && eq.bib.uuid != "default-biblio") {
                                        biblioRefIdsToExport.add(eq.bib.uuid)
                                }
                        }
                }

                // Collecter depuis les références exportées (via leurs nutriments)
                references.forEach { refApi ->
                        refApi.nutrients.forEach { nutrientApi ->
                                nutrientApi.biblioRefId?.takeIf { it.isNotBlank() }?.let {
                                        biblioRefIdsToExport.add(it)
                                }
                        }
                }

                val biblioRefs =
                        try {
                                val allBiblioRefs =
                                        biblioRepository?.getAllBiblioRefs()?.first() ?: emptyList()

                                // Filtrer les biblioRefs selon le contexte d'export
                                val filteredBiblioRefs = when {
                                        // Export sélectif (animalIds non vide) : seulement celles utilisées
                                        options.animalIds.isNotEmpty() -> {
                                                allBiblioRefs.filter { it.uuid in biblioRefIdsToExport }
                                        }
                                        // Export général (animalIds vide) : toutes les biblioRefs
                                        else -> allBiblioRefs
                                }

                                filteredBiblioRefs.map { it.toApi() }
                        } catch (_: Exception) {
                                emptyList()
                        }
                val consultationKeywords: List<ConsultationKeywordApi> =
                        try {
                                val allKeywords =
                                        consultationRepository?.getAllKeywords() ?: emptyList()
                                val filteredKeywords =
                                        when {
                                                options.animalIds.isNotEmpty() -> {
                                                        val usedKeywordIds =
                                                                filteredDomainAnimals
                                                                        .flatMap { animal ->
                                                                                animal.consultations
                                                                                        .flatMap {
                                                                                                consult ->
                                                                                                consult.keywordIds
                                                                                        }
                                                                        }
                                                                        .toSet()
                                                        allKeywords.filter { it.uuid in usedKeywordIds }
                                                }
                                                else -> allKeywords
                                        }
                                filteredKeywords.map {
                                        ConsultationKeywordApi(uuid = it.uuid, label = it.label)
                                }
                        } catch (_: Exception) {
                                emptyList()
                        }

                return ApiEnvelope(
                        version = "2.0.0",
                        generatedAtEpochMs = Clock.System.now().toEpochMilliseconds(),
                        animals = animals,
                        foods = foods,
                        rations = rationsList2,
                        recipes = recipes,
                        equations = equations,
                        biblioRefs = biblioRefs,
                        references = references,
                        conseils = conseils,
                        consultationKeywords = consultationKeywords
                )
        }

        /** Importe les données au format API et les sauvegarde via les repositories. */
        suspend fun importAll(
                apiJson: String,
                listener: ImportProgressListener? = null
        ): ImportCounts {
                listener?.onProgress?.invoke(0.02)
                listener?.onLog?.invoke("Lecture/Parsing du JSON…")
                val envelope = jsonPretty.decodeFromString<ApiEnvelope>(apiJson)
                return importAll(envelope, listener)
        }

        suspend fun importAll(
                envelope: ApiEnvelope,
                listener: ImportProgressListener? = null
        ): ImportCounts {

                listener?.onLog?.invoke(
                        "Contenu: animals=${envelope.animals.size}, foods=${envelope.foods.size}, rations=${envelope.rations.size}, recipes=${envelope.recipes.size}, equations=${envelope.equations.size}, biblioRefs=${envelope.biblioRefs.size}, references=${envelope.references.size}, conseils=${envelope.conseils.size}, consultationKeywords=${envelope.consultationKeywords.size}"
                )
                var errorCount = 0
                var animalsImported: Int = 0
                var foodsImported: Int = 0

                var equationsImported: Int = 0
                var referencesImported: Int = 0
                var biblioImported: Int = 0
                var rationsImported: Int = 0
                var recipesImported: Int = 0
                var conseilsImported: Int = 0
                var keywordsImported: Int = 0
                val totalUnits: Int =
                        (envelope.foods.size +
                                        envelope.equations.size +
                                        envelope.biblioRefs.size +
                                        envelope.animals.size +
                                        envelope.references.size +
                                        envelope.recipes.size +
                                        envelope.conseils.size +
                                        envelope.consultationKeywords.size)
                                .coerceAtLeast(1)
                var processedUnits = 0
                fun advance(units: Int = 1) {
                        processedUnits += units
                        val p = 0.1 + 0.9 * (processedUnits.toDouble() / totalUnits.toDouble())
                        listener?.onProgress?.invoke(p.coerceIn(0.0, 1.0))
                }
                listener?.onProgress?.invoke(0.1)

                // 1) Références biblio (aucune dépendance)
                if (envelope.biblioRefs.isNotEmpty() && biblioRepository != null) {
                        listener?.onLog?.invoke(
                                "Import des bibliographies (${envelope.biblioRefs.size})…"
                        )
                        for (b in envelope.biblioRefs) {
                                try {
                                        biblioRepository.insertBiblioRef(
                                                BiblioRef(
                                                        uuid = b.uuid,
                                                        firstAuthor = b.firstAuthor,
                                                        year = b.year,
                                                        completeRef = b.completeRef,
                                                        comments = b.comments,
                                                        bibtex = b.bibtex,
                                                        consistent = b.consistent
                                                )
                                        )
                                        biblioImported++
                                        advance()
                                } catch (e: Exception) {
                                        if (e is kotlinx.coroutines.CancellationException) throw e
                                        errorCount++
                                        listener?.onLog?.invoke(
                                                "Erreur biblioRef ${b.uuid}: ${e.message}"
                                        )
                                        advance()
                                }
                        }
                        listener?.onLog?.invoke("Bibliographies importées=$biblioImported")
                }

                // 2) Mots-clés de consultation (aucune dépendance)
                if (envelope.consultationKeywords.isNotEmpty() &&
                                consultationRepository != null
                ) {
                        listener?.onLog?.invoke(
                                "Import des mots-clés (${envelope.consultationKeywords.size})…"
                        )
                        for (kw in envelope.consultationKeywords) {
                                try {
                                        consultationRepository.saveKeyword(
                                                ConsultationKeyword(uuid = kw.uuid, label = kw.label)
                                        )
                                        keywordsImported++
                                        advance()
                                } catch (e: Exception) {
                                        if (e is kotlinx.coroutines.CancellationException) throw e
                                        errorCount++
                                        listener?.onLog?.invoke(
                                                "Erreur mot-clé ${kw.uuid}: ${e.message}"
                                        )
                                        advance()
                                }
                        }
                        listener?.onLog?.invoke("Mots-clés importés=$keywordsImported")
                }

                // 3) Équations (aucune dépendance)
                if (envelope.equations.isNotEmpty() && equationRepository != null) {
                        listener?.onLog?.invoke(
                                "Import des équations (${envelope.equations.size})…"
                        )
                        for (eqApi in envelope.equations) {
                                val eq = eqApi.toDomain()
                                equationRepository.saveEquation(eq)
                                equationsImported++
                                advance()
                        }
                        listener?.onLog?.invoke("Équations importées=$equationsImported")
                }

                // 4) Aliments (aucune dépendance)
                if (envelope.foods.isNotEmpty() && foodRepository != null) {
                        listener?.onLog?.invoke("Import des aliments (${envelope.foods.size})…")
                        // Certaines anciennes sauvegardes contiennent exceptionnellement un
                        // aliment sans UUID. On lui attribue un identifiant stable dérivé de sa
                        // position et de son nom : l'aliment est conservé et une restauration ne
                        // doit pas échouer pour cette seule donnée incomplète.
                        val foodsToImport = envelope.foods.mapIndexed { index, food ->
                                if (food.uuid.isBlank()) {
                                        val generatedUuid =
                                                "legacy-food-${index + 1}-${food.name.orEmpty().hashCode().toString(16)}"
                                        food.copy(uuid = generatedUuid)
                                } else food
                        }
                        // Cache des biblioRefs (déjà importées à l'étape 1) pour résoudre les
                        // placeholders uuid-only posés par FoodApi.toDomain()
                        val foodBiblioCache: Map<String, BiblioRef> =
                                if (biblioRepository != null) {
                                        try {
                                                biblioRepository.getAllBiblioRefs().first()
                                                        .associateBy { it.uuid }
                                        } catch (e: Exception) {
                                                if (e is kotlinx.coroutines.CancellationException) throw e
                                                errorCount++
                                                emptyMap()
                                        }
                                } else emptyMap()
                        fun resolveBiblioRefs(api: FoodApi, aliment: AlimentEv): AlimentEv =
                                aliment.copy(
                                        biblioRefs = api.biblioRefIds.mapNotNull { foodBiblioCache[it] }
                                )
                        if (foodRepository is DatabaseFoodRepository) {
                                try {
                                        val aliments = foodsToImport.map { resolveBiblioRefs(it, it.toDomain()) }
                                        val res = foodRepository.importFoodsDomain(aliments)
                                        foodsImported += res.importedCount + res.updatedCount
                                        errorCount += res.errorCount
                                        advance(envelope.foods.size)
                                        listener?.onLog?.invoke(
                                                "Aliments importés=${res.importedCount}, mis à jour=${res.updatedCount}, erreurs=${res.errorCount}"
                                        )
                                } catch (e: Exception) {
                                        if (e is kotlinx.coroutines.CancellationException) throw e
                                        errorCount++
                                        listener?.onLog?.invoke(
                                                "Erreur import bulk aliments: ${e.message}"
                                        )
                                }
                        } else {
                                // Fallback: insertion/MAJ unitaire si repo non-DB
                                for (api in foodsToImport) {
                                        try {
                                                val aliment = resolveBiblioRefs(api, api.toDomain())
                                                foodRepository.insertFood(aliment)
                                                foodsImported++
                                                advance()
                                        } catch (e: Exception) {
                                                if (e is kotlinx.coroutines.CancellationException) throw e
                                                errorCount++
                                                listener?.onLog?.invoke(
                                                        "Erreur aliment ${api.uuid}: ${e.message}"
                                                )
                                                advance()
                                        }
                                }
                        }
                        listener?.onLog?.invoke("Aliments importés=$foodsImported")
                }

                // 5) Références nutritionnelles (avec liens vers équations et biblio)
                if (envelope.references.isNotEmpty() && referenceRepository != null) {
                        listener?.onLog?.invoke(
                                "Import des références nutritionnelles (${envelope.references.size})…"
                        )
                        // Construire un cache d'équations
                        val eqCache: MutableMap<String, Equation> = mutableMapOf()
                        if (equationRepository != null) {
                                try {
                                        equationRepository.getAllEquations().forEach { eq ->
                                                eqCache[eq.uuid] = eq
                                        }
                                } catch (e: Exception) {
                                        if (e is kotlinx.coroutines.CancellationException) throw e
                                        errorCount++
                                }
                        }
                        // Construire un cache de biblio pour éviter des accès répétés
                        val biblioCache: Map<String, BiblioRef> =
                                if (biblioRepository != null) {
                                        try {
                                                (biblioRepository.getAllBiblioRefs().first())
                                                        .associateBy { it.uuid }
                                        } catch (e: Exception) {
                                                if (e is kotlinx.coroutines.CancellationException) throw e
                                                errorCount++
                                                emptyMap()
                                        }
                                } else emptyMap()
                        for (refApi in envelope.references) {
                                try {
                                        val ref =
                                                ReferenceEv(
                                                        uuid = refApi.uuid,
                                                        nom = refApi.nom,
                                                        description = refApi.description,
                                                        maladie = refApi.maladie,
                                                        nomMaladie = refApi.nomMaladie,
                                                        nomEnergie = refApi.nomEnergie,
                                                        consistent = refApi.consistent,
                                                        espece = Espece.valueOf(refApi.espece),
                                                        stadePhysio =
                                                                StadePhysio.valueOf(
                                                                        refApi.stadePhysio
                                                                )
                                                )
                                        // Lier équations
                                        ref.equationBW = refApi.equationBW?.let { eqCache[it] }
                                        ref.equationBEE = refApi.equationBEE?.let { eqCache[it] }
                                        ref.equationDEcom =
                                                refApi.equationDEcom?.let { eqCache[it] }
                                        ref.equationDEraw =
                                                refApi.equationDEraw?.let { eqCache[it] }
                                        ref.equationME = refApi.equationME?.let { eqCache[it] }
                                        ref.equationsNut.addAll(
                                                refApi.equationsNut.mapNotNull { eqCache[it] }
                                        )
                                        
                                        // 🔧 AJOUT : Importer les nutriments
                                        if (refApi.nutrients.isNotEmpty()) {
                                                listener?.onLog?.invoke(
                                                        "Import des nutriments pour ${refApi.nom} (${refApi.nutrients.size} nutriments)"
                                                )
                                            for (nutrientApi in refApi.nutrients) {
                                                try {
                                                                // 🔍 LOG DIAGNOSTIC : Tracer
                                                                // l'import des nutriments

                                                                // Résoudre le nutriment (supporte
                                                                // aussi NutrientAnalysis)
                                                                val nutrient =
                                                                        fr.vetbrain.vetnutri_mp
                                                                                .Enumer
                                                                                .NutrientResolver
                                                                                .AllNutrientResolver(
                                                                                        nutrientApi
                                                                                                .nutrientLabel
                                                                                )
                                                    if (nutrient != null) {
                                                                        // Créer la référence
                                                                        // bibliographique
                                                                        val biblio =
                                                                                if (nutrientApi
                                                                                                .biblioRefId !=
                                                                                                null
                                                                                ) {
                                                                                        biblioCache[
                                                                                                nutrientApi
                                                                                                        .biblioRefId]
                                                                                                ?: BiblioRef()
                                                        } else {
                                                            BiblioRef()
                                                        }
                                                        
                                                                        // Définir le nutriment dans
                                                                        // la référence
                                                                        val reflevel =
                                                                                when (nutrientApi
                                                                                                .reflevel
                                                                                ) {
                                                                                        "MIN" ->
                                                                                                fr.vetbrain
                                                                                                        .vetnutri_mp
                                                                                                        .Enumer
                                                                                                        .Reflevel
                                                                                                        .MIN
                                                                                        "MAX" ->
                                                                                                fr.vetbrain
                                                                                                        .vetnutri_mp
                                                                                                        .Enumer
                                                                                                        .Reflevel
                                                                                                        .MAX
                                                                                        "OPTIMIN" ->
                                                                                                fr.vetbrain
                                                                                                        .vetnutri_mp
                                                                                                        .Enumer
                                                                                                        .Reflevel
                                                                                                        .OPTIMIN
                                                                                        "OPTIMAX" ->
                                                                                                fr.vetbrain
                                                                                                        .vetnutri_mp
                                                                                                        .Enumer
                                                                                                        .Reflevel
                                                                                                        .OPTIMAX
                                                                                        else ->
                                                                                                fr.vetbrain
                                                                                                        .vetnutri_mp
                                                                                                        .Enumer
                                                                                                        .Reflevel
                                                                                                        .MIN
                                                                                }

                                                                        val unitReq =
                                                                                fr.vetbrain
                                                                                        .vetnutri_mp
                                                                                        .Enumer
                                                                                        .UnitReqEnum
                                                                                        .getById(
                                                                                                nutrientApi
                                                                                                        .uniteReqId
                                                                                        )
                                                        
                                                        ref.definirNutriment(
                                                                                valeur =
                                                                                        nutrientApi
                                                                                                .quantity,
                                                            nutrient = nutrient,
                                                                                niveauRef =
                                                                                        reflevel,
                                                            uniteReq = unitReq,
                                                            biblio = biblio
                                                        )
                                                    } else {
                                                        
                                                                        listener?.onLog?.invoke(
                                                                                "⚠️ Nutriment non résolu: ${nutrientApi.nutrientLabel}"
                                                                        )
                                                    }
                                                } catch (e: Exception) {
                                                        if (e is kotlinx.coroutines.CancellationException) throw e
                                                        errorCount++
                                                    
                                                                listener?.onLog?.invoke(
                                                                        "Erreur nutriment ${nutrientApi.nutrientLabel}: ${e.message}"
                                                                )
                                                        }
                                                }
                                        }

                                        // 🔧 AJUSTEMENT : Importer les coefficients en supprimant
                                        // d'abord les valeurs par défaut "Normal"
                                        if (refApi.coefficients.isNotEmpty()) {
                                                // Nettoyer les coefficients par défaut ajoutés au
                                                // constructeur (évite les doublons)
                                                ref.modk1.clear()
                                                ref.modk2.clear()
                                                ref.modk3.clear()
                                                ref.modk4.clear()
                                                ref.modk5.clear()

                                                listener?.onLog?.invoke(
                                                        "Import des coefficients pour ${refApi.nom} (${refApi.coefficients.size} coefficients)"
                                                )
                                            for (coefApi in refApi.coefficients) {
                                                try {
                                                                val coef =
                                                                        fr.vetbrain.vetnutri_mp.Data
                                                                                .CoefP(
                                                                                        uuid =
                                                                                                coefApi.uuid,
                                                                                        description =
                                                                                                coefApi.description,
                                                                                        coef =
                                                                                                coefApi.coef,
                                                                                        groupUUID =
                                                                                                coefApi.groupUUID
                                                    )
                                                    
                                                    when (coefApi.groupType) {
                                                                        "k1" ->
                                                                                ref.modk1
                                                                                        .add(coef)
                                                                        "k2" ->
                                                                                ref.modk2
                                                                                        .add(coef)
                                                                        "k3" ->
                                                                                ref.modk3
                                                                                        .add(coef)
                                                                        "k4" ->
                                                                                ref.modk4
                                                                                        .add(coef)
                                                                        "k5" ->
                                                                                ref.modk5
                                                                                        .add(coef)
                                                    }
                                                } catch (e: Exception) {
                                                        if (e is kotlinx.coroutines.CancellationException) throw e
                                                        errorCount++
                                                                listener?.onLog?.invoke(
                                                                        "Erreur coefficient ${coefApi.uuid}: ${e.message}"
                                                                )
                                                        }
                                            }
                                        }
                                        
                                        referenceRepository.saveReferenceEv(ref)
                                        referencesImported++
                                        advance()
                                } catch (e: Exception) {
                                        if (e is kotlinx.coroutines.CancellationException) throw e
                                        errorCount++
                                        listener?.onLog?.invoke(
                                                "Erreur referenceEv ${refApi.uuid}: ${e.message}"
                                        )
                                        advance()
                                }
                        }
                        listener?.onLog?.invoke(
                                "Références nutritionnelles importées=$referencesImported"
                        )
                }

                // 6) Animaux + consultations/rations (dépendent des aliments et références)
                if (envelope.animals.isNotEmpty()) {
                        listener?.onLog?.invoke("Import des animaux (${envelope.animals.size})…")
                        // `DatabaseAnimalRepository.saveAnimal` réalise un INSERT. Une
                        // restauration se fait souvent au-dessus d'une base contenant déjà les
                        // mêmes UUID : il faut alors utiliser UPDATE, sans quoi Room refuse tous
                        // les animaux sur la contrainte d'unicité.
                        val existingAnimalIds: MutableSet<String> = try {
                                animalRepository.getAllAnimals().map { it.uuid }.toMutableSet()
                        } catch (e: Exception) {
                                if (e is kotlinx.coroutines.CancellationException) throw e
                                errorCount++
                                listener?.onLog?.invoke(
                                        "Erreur lecture des animaux existants: ${e.message}"
                                )
                                mutableSetOf()
                        }
                        var updatedAnimals = 0
                        // rations
                        var existingFoodIdsForRations: MutableSet<String> = mutableSetOf()
                        if (foodRepository != null) {
                                existingFoodIdsForRations =
                                        foodRepository.getAllFoods().map { it.uuid }.toMutableSet()
                        }
                        for (animalApi in envelope.animals) {
                                try {
                                        val animal = animalApi.toDomain()
                                        // La représentation JSON des consultations ne porte pas
                                        // l'UUID de l'animal parent. `saveAnimal` le renseigne
                                        // implicitement pour les nouveaux animaux, mais pas
                                        // `updateAnimal`; sans cette affectation, la sauvegarde
                                        // suivante des consultations viole leur clé étrangère.
                                        animal.consultations.forEach { consultation ->
                                                consultation.idAnim = animal.uuid
                                        }
                                        if (animal.uuid in existingAnimalIds) {
                                                animalRepository.updateAnimal(animal)
                                                updatedAnimals++
                                        } else {
                                                animalRepository.saveAnimal(animal)
                                                existingAnimalIds.add(animal.uuid)
                                        }
                                        // Sauvegarder les consultations avec rations si possible
                                        if (consultationRepository != null) {
                                                // Créer les aliments manquants référencés par les
                                                // rations
                                                animal.consultations.forEach { consult ->
                                                        consult.rations.forEach { ration ->
                                                                ration.alimentMutableList.forEach {
                                                                        ar ->
                                                                        val foodId = ar.refAlimUnif
                                                                        if (!foodId.isNullOrEmpty() &&
                                                                                        !existingFoodIdsForRations
                                                                                                .contains(
                                                                                                        foodId
                                                                                                )
                                                                        ) {
                                                                                val nameGuess:
                                                                                        String =
                                                                                        ar.aliment
                                                                                                ?.nom
                                                                                                ?: ar.aliment
                                                                                                        ?.ingredients
                                                                                                        ?: "Aliment importé ${foodId}"
                                                                                val placeholder =
                                                                                        AlimentEv(
                                                                                                uuid =
                                                                                                        foodId!!,
                                                                                                nom =
                                                                                                        nameGuess,
                                                                                                brand =
                                                                                                        ar.aliment
                                                                                                                ?.brand,
                                                                                                price =
                                                                                                        ar.aliment
                                                                                                                ?.price,
                                                                                                especes =
                                                                                                        mutableListOf(),
                                                                                                indicat =
                                                                                                        mutableListOf()
                                                                                        )
                                                                                try {
                                                                                        foodRepository
                                                                                                ?.insertFood(
                                                                                                        placeholder
                                                                                                )
                                                                                        existingFoodIdsForRations
                                                                                                .add(
                                                                                                        foodId!!
                                                                                                )
                                                                                        foodsImported++
                                                                                } catch (e: Exception) {
                                                                                        if (e is kotlinx.coroutines.CancellationException) throw e
                                                                                        errorCount++
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                        consultationRepository.saveConsultation(
                                                                consult
                                                        )
                                                        listener?.onLog?.invoke(
                                                                "Consultation ${consult.uuid}: rations=${consult.rations.size}"
                                                        )
                                                }
                                        }
                                        // Compter les rations importées
                                        rationsImported +=
                                                animal.consultations.sumOf { it.rations.size }
                                        animalsImported++
                                        advance()
                                } catch (e: Exception) {
                                        if (e is kotlinx.coroutines.CancellationException) throw e
                                        errorCount++
                                        listener?.onLog(
                                                "Erreur animal ${animalApi.uuid}: ${e.message}"
                                        )
                                        advance()
                                }
                        }
                        listener?.onLog?.invoke(
                                "Animaux importés=$animalsImported, mis à jour=$updatedAnimals, rations liées=$rationsImported"
                        )
                }

                // 7) Recettes (dépendent des aliments)
                if (envelope.recipes.isNotEmpty() && recipeRepository != null) {
                        listener?.onLog?.invoke("Import des recettes (${envelope.recipes.size})…")

                        // Construire un cache des aliments existants pour vérifier les références
                        val existingFoodIds: Set<String> =
                                if (foodRepository != null) {
                                        try {
                                                foodRepository.getAllFoods().map { it.uuid }.toSet()
                                        } catch (e: Exception) {
                                                if (e is kotlinx.coroutines.CancellationException) throw e
                                                errorCount++
                                                emptySet()
                                        }
                                } else emptySet()

                        for (recipeApi in envelope.recipes) {
                                try {
                                        
                                        val recipe = recipeApi.toDomain()

                                        // Vérifier et créer les aliments manquants référencés par
                                        // la recette
                                        recipe.aliments.forEach { ingredient ->
                                                val foodId = ingredient.refAlimUnif
                                                if (!foodId.isNullOrEmpty() &&
                                                                !existingFoodIds.contains(foodId)
                                                ) {
                                                        val placeholder =
                                                                AlimentEv(
                                                                        uuid = foodId,
                                                                        nom =
                                                                                "Aliment importé ${foodId}",
                                                                        brand = null,
                                                                        price = null,
                                                                        especes = mutableListOf(),
                                                                        indicat = mutableListOf()
                                                                )
                                                        try {
                                                                foodRepository?.insertFood(
                                                                        placeholder
                                                                )
                                                                existingFoodIds
                                                                        .toMutableSet()
                                                                        .add(foodId)
                                                                foodsImported++
                                                        } catch (e: Exception) {
                                                                if (e is kotlinx.coroutines.CancellationException) throw e
                                                                errorCount++
                                                        }
                                                }
                                        }

                                        recipeRepository.importRecipe(recipe)

                                        recipesImported++
                                        advance()
                                } catch (e: Exception) {
                                        if (e is kotlinx.coroutines.CancellationException) throw e
                                        errorCount++
                                        listener?.onLog?.invoke(
                                                "Erreur recette ${recipeApi.uuid}: ${e.message}"
                                        )
                                        advance()
                                }
                        }
                        listener?.onLog?.invoke("Recettes importées=$recipesImported")
                }

                // 7) Conseils (aucune dépendance)
                if (envelope.conseils.isNotEmpty() && conseilRepository != null) {
                        listener?.onLog?.invoke("Import des conseils (${envelope.conseils.size})…")
                        for (conseilApi in envelope.conseils) {
                                try {
                                        val conseil = conseilApi.toDomain()
                                        // Sauvegarder le conseil (insert ou update)
                                        conseilRepository.saveConseil(conseil).getOrThrow()
                                        conseilsImported++
                                        advance()
                                } catch (e: Exception) {
                                        if (e is kotlinx.coroutines.CancellationException) throw e
                                        errorCount++
                                        listener?.onLog?.invoke(
                                                "Erreur conseil ${conseilApi.id}: ${e.message}"
                                        )
                                        advance()
                                }
                        }
                        listener?.onLog?.invoke("Conseils importés=$conseilsImported")
                }

                return ImportCounts(
                        animals = animalsImported,
                        foods = foodsImported,
                        equations = equationsImported,
                        references = referencesImported,
                        biblios = biblioImported,
                        rations = rationsImported,
                        recipes = recipesImported,
                        conseils = conseilsImported,
                        errorCount = errorCount
                )
        }

        data class ImportCounts(
                val animals: Int,
                val foods: Int,
                val equations: Int,
                val references: Int,
                val biblios: Int,
                val rations: Int,
                val recipes: Int,
                val conseils: Int,
                val errorCount: Int = 0
        ) {
                fun requireComplete(): ImportCounts {
                        check(errorCount == 0) { "Import partiel : $errorCount erreur(s). Certaines données ont pu être importées." }
                        return this
                }
        }
}
