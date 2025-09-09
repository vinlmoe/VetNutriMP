package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.AnimalApi
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.ApiEnvelope
import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
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
                prettyPrint = true
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
                val animalIds: Set<String> = emptySet(),
                val foodIds: Set<String> = emptySet()
        )

        /**
         * Exporte l'ensemble des données (animaux, aliments, rations, équations, recettes) au
         * format API.
         */
        suspend fun exportAll(): String {
                val domainAnimals = animalRepository.getAllAnimals()
                // Charger les consultations/rations pour chaque animal pour un export complet
                val animalsWithConsultations: List<AnimalEv> =
                        if (consultationRepository != null) {
                                domainAnimals.map { animal ->
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
                        } else domainAnimals
                val animals: List<AnimalApi> = animalsWithConsultations.map { it.toApi() }
                val foods = foodRepository?.getAllFoods()?.map { it.toApi() } ?: emptyList()

                // Rassembler toutes les rations depuis les consultations (depuis le domaine)
                val rationsList: List<RationApi> =
                        animalsWithConsultations
                                .asSequence()
                                .flatMap { animal: AnimalEv ->
                                        animal.consultations.asSequence().flatMap {
                                                consult: ConsultationEv ->
                                                consult.rations.asSequence().map { ration: Ration ->
                                                        ration.toApi()
                                                }
                                        }
                                }
                                .toList()

                val equations =
                        equationRepository?.getAllEquations()?.map { it.toApi() } ?: emptyList()

                // Récupérer toutes les recettes
                
                
                val recipes =
                        recipeRepository?.getAllRecipesAsRecette()?.map { it.toApi() }
                                ?: emptyList()
                

                // Récupérer tous les conseils
                val conseils =
                        try {
                                conseilRepository?.getConseilsActifs()?.getOrThrow()?.map { it.toApi() }
                                        ?: emptyList()
                        } catch (e: Exception) {
                                emptyList()
                        }

                // Références et biblios
                val references =
                        referenceRepository?.getAllReferenceEv()?.map { it.toApiRef() }
                                ?: emptyList()
                val biblioRefs =
                        try {
                                val list =
                                        biblioRepository?.getAllBiblioRefs()?.first() ?: emptyList()
                                list.map { it.toApi() }
                        } catch (e: Exception) {
                                emptyList()
                        }
                val envelope =
                        ApiEnvelope(
                                version = "1.0.0",
                                generatedAtEpochMs = Clock.System.now().toEpochMilliseconds(),
                                animals = animals,
                                foods = foods,
                                rations = rationsList,
                                recipes = recipes,
                                equations = equations,
                                biblioRefs = biblioRefs,
                                references = references,
                                conseils = conseils
                        )
                return jsonPretty.encodeToString(envelope)
        }

        /** Export avec filtres et sélections par type/identifiants. */
        suspend fun exportWithSelection(options: ExportSelectionOptions): String {
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

                val allFoods =
                        if (options.includeFoods) (foodRepository?.getAllFoods() ?: emptyList())
                        else emptyList()
                val foods: List<FoodApi> =
                        allFoods.asSequence()
                                .filter {
                                        options.foodIds.isEmpty() ||
                                                options.foodIds.contains(it.uuid)
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
                val equations =
                        if (options.includeEquations)
                                (equationRepository?.getAllEquations()?.map { it.toApi() }
                                        ?: emptyList())
                        else emptyList()
                val references =
                        if (options.includeEquations) {
                                referenceRepository?.getAllReferenceEv()?.map { it.toApiRef() }
                                        ?: emptyList()
                        } else emptyList()

                // Récupérer les recettes selon les options
                
                
                
                val recipes =
                        if (options.includeRecipes) {
                                recipeRepository?.getAllRecipesAsRecette()?.map { it.toApi() }
                                        ?: emptyList()
                        } else emptyList()
                

                // Récupérer les conseils selon les options
                val conseils =
                        if (options.includeConseils) {
                                try {
                                        conseilRepository?.getConseilsActifs()?.getOrThrow()?.map { it.toApi() }
                                                ?: emptyList()
                                } catch (e: Exception) {
                                        emptyList()
                                }
                        } else emptyList()

                val biblioRefs =
                        try {
                                val list =
                                        biblioRepository?.getAllBiblioRefs()?.first() ?: emptyList()
                                list.map { it.toApi() }
                        } catch (e: Exception) {
                                emptyList()
                        }

                val envelope =
                        ApiEnvelope(
                                version = "1.0.0",
                                generatedAtEpochMs = Clock.System.now().toEpochMilliseconds(),
                                animals = animals,
                                foods = foods,
                                rations = rationsList2,
                                recipes = recipes,
                                equations = equations,
                                biblioRefs = biblioRefs,
                                references = references,
                                conseils = conseils
                        )
                return jsonPretty.encodeToString(envelope)
        }

        /** Importe les données au format API et les sauvegarde via les repositories. */
        suspend fun importAll(
                apiJson: String,
                listener: ImportProgressListener? = null
        ): ImportCounts {

                listener?.onProgress(0.02)
                listener?.onLog("Lecture/Parsing du JSON…")
                val envelope = jsonPretty.decodeFromString<ApiEnvelope>(apiJson)

                listener?.onLog(
                        "Contenu: animals=${envelope.animals.size}, foods=${envelope.foods.size}, rations=${envelope.rations.size}, recipes=${envelope.recipes.size}, equations=${envelope.equations.size}, biblioRefs=${envelope.biblioRefs.size}, references=${envelope.references.size}, conseils=${envelope.conseils.size}"
                )
                var animalsImported: Int = 0
                var foodsImported: Int = 0

                var equationsImported: Int = 0
                var referencesImported: Int = 0
                var biblioImported: Int = 0
                var rationsImported: Int = 0
                var recipesImported: Int = 0
                var conseilsImported: Int = 0
                val totalUnits: Int =
                        (envelope.foods.size +
                                        envelope.equations.size +
                                        envelope.biblioRefs.size +
                                        envelope.animals.size +
                                        envelope.references.size +
                                        envelope.recipes.size +
                                        envelope.conseils.size)
                                .coerceAtLeast(1)
                var processedUnits = 0
                fun advance(units: Int = 1) {
                        processedUnits += units
                        val p = 0.1 + 0.9 * (processedUnits.toDouble() / totalUnits.toDouble())
                        listener?.onProgress(p.coerceIn(0.0, 1.0))
                }
                listener?.onProgress(0.1)

                // 1) Références biblio (aucune dépendance)
                if (envelope.biblioRefs.isNotEmpty() && biblioRepository != null) {
                        listener?.onLog("Import des bibliographies (${envelope.biblioRefs.size})…")
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
                                        listener?.onLog("Erreur biblioRef ${b.uuid}: ${e.message}")
                                        advance()
                                }
                        }
                        listener?.onLog("Bibliographies importées=$biblioImported")
                }

                // 2) Équations (aucune dépendance)
                if (envelope.equations.isNotEmpty() && equationRepository != null) {
                        listener?.onLog("Import des équations (${envelope.equations.size})…")
                        for (eqApi in envelope.equations) {
                                val eq = eqApi.toDomain()
                                equationRepository.saveEquation(eq)
                                equationsImported++
                                advance()
                        }
                        listener?.onLog("Équations importées=$equationsImported")
                }

                // 3) Aliments (aucune dépendance)
                if (envelope.foods.isNotEmpty() && foodRepository != null) {
                        listener?.onLog("Import des aliments (${envelope.foods.size})…")
                        if (foodRepository is DatabaseFoodRepository) {
                                try {
                                        val aliments = envelope.foods.map { it.toDomain() }
                                        val res = foodRepository.importFoodsDomain(aliments)
                                        foodsImported += res.importedCount + res.updatedCount
                                        advance(envelope.foods.size)
                                        listener?.onLog(
                                                "Aliments importés=${res.importedCount}, mis à jour=${res.updatedCount}, erreurs=${res.errorCount}"
                                        )
                                } catch (e: Exception) {
                                        listener?.onLog("Erreur import bulk aliments: ${e.message}")
                                }
                        } else {
                                // Fallback: insertion/MAJ unitaire si repo non-DB
                                for (api in envelope.foods) {
                                        try {
                                                val aliment = api.toDomain()
                                                foodRepository.insertFood(aliment)
                                                foodsImported++
                                                advance()
                                        } catch (e: Exception) {
                                                listener?.onLog(
                                                        "Erreur aliment ${api.uuid}: ${e.message}"
                                                )
                                                advance()
                                        }
                                }
                        }
                        listener?.onLog("Aliments importés=$foodsImported")
                }

                // 4) Références nutritionnelles (avec liens vers équations et biblio)
                if (envelope.references.isNotEmpty() && referenceRepository != null) {
                        listener?.onLog(
                                "Import des références nutritionnelles (${envelope.references.size})…"
                        )
                        // Construire un cache d'équations
                        val eqCache: MutableMap<String, Equation> = mutableMapOf()
                        if (equationRepository != null) {
                                try {
                                        equationRepository.getAllEquations().forEach { eq ->
                                                eqCache[eq.uuid] = eq
                                        }
                                } catch (e: Exception) {}
                        }
                        // Construire un cache de biblio pour éviter des accès répétés
                        val biblioCache: Map<String, BiblioRef> =
                                if (biblioRepository != null) {
                                        try {
                                                (biblioRepository.getAllBiblioRefs().first())
                                                        .associateBy { it.uuid }
                                        } catch (_: Exception) {
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
                                            listener?.onLog("Import des nutriments pour ${refApi.nom} (${refApi.nutrients.size} nutriments)")
                                            for (nutrientApi in refApi.nutrients) {
                                                try {
                                                    // 🔍 LOG DIAGNOSTIC : Tracer l'import des nutriments
                                                    
                                                    
                                                    // Résoudre le nutriment
                                                    val nutrient = fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver.AllNutrientResolver(nutrientApi.nutrientLabel)
                                                    if (nutrient != null) {
                                                        // Créer la référence bibliographique
                                                        val biblio = if (nutrientApi.biblioRefId != null) {
                                                            biblioCache[nutrientApi.biblioRefId] ?: BiblioRef()
                                                        } else {
                                                            BiblioRef()
                                                        }
                                                        
                                                        // Définir le nutriment dans la référence
                                                        val reflevel = when (nutrientApi.reflevel) {
                                                            "MIN" -> fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MIN
                                                            "MAX" -> fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MAX
                                                            "OPTIMIN" -> fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMIN
                                                            "OPTIMAX" -> fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMAX
                                                            else -> fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MIN
                                                        }
                                                        
                                                        val unitReq = fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.getById(nutrientApi.uniteReqId)
                                                        
                                                        ref.definirNutriment(
                                                            valeur = nutrientApi.quantity,
                                                            nutrient = nutrient,
                                                            niveauRef = reflevel,
                                                            uniteReq = unitReq,
                                                            biblio = biblio
                                                        )
                                                        
                                                        
                                                    } else {
                                                        
                                                        listener?.onLog("⚠️ Nutriment non résolu: ${nutrientApi.nutrientLabel}")
                                                    }
                                                } catch (e: Exception) {
                                                    
                                                    listener?.onLog("Erreur nutriment ${nutrientApi.nutrientLabel}: ${e.message}")
                                                }
                                            }
                                        }
                                        
                                        // 🔧 AJOUT : Importer les coefficients
                                        if (refApi.coefficients.isNotEmpty()) {
                                            listener?.onLog("Import des coefficients pour ${refApi.nom} (${refApi.coefficients.size} coefficients)")
                                            for (coefApi in refApi.coefficients) {
                                                try {
                                                    val coef = fr.vetbrain.vetnutri_mp.Data.CoefP(
                                                        uuid = coefApi.uuid,
                                                        description = coefApi.description,
                                                        coef = coefApi.coef,
                                                        groupUUID = coefApi.groupUUID
                                                    )
                                                    
                                                    when (coefApi.groupType) {
                                                        "k1" -> ref.getModk1().add(coef)
                                                        "k2" -> ref.getModk2().add(coef)
                                                        "k3" -> ref.getModk3().add(coef)
                                                        "k4" -> ref.getModk4().add(coef)
                                                        "k5" -> ref.getModk5().add(coef)
                                                    }
                                                } catch (e: Exception) {
                                                    listener?.onLog("Erreur coefficient ${coefApi.uuid}: ${e.message}")
                                                }
                                            }
                                        }
                                        
                                        referenceRepository.saveReferenceEv(ref)
                                        referencesImported++
                                        advance()
                                } catch (e: Exception) {
                                        listener?.onLog(
                                                "Erreur referenceEv ${refApi.uuid}: ${e.message}"
                                        )
                                        advance()
                                }
                        }
                        listener?.onLog("Références nutritionnelles importées=$referencesImported")
                }

                // 5) Animaux + consultations/rations (dépendent des aliments et références)
                if (envelope.animals.isNotEmpty()) {
                        listener?.onLog("Import des animaux (${envelope.animals.size})…")
                        // rations
                        var existingFoodIdsForRations: MutableSet<String> = mutableSetOf()
                        if (foodRepository != null) {
                                existingFoodIdsForRations =
                                        foodRepository.getAllFoods().map { it.uuid }.toMutableSet()
                        }
                        for (animalApi in envelope.animals) {
                                try {
                                        val animal = animalApi.toDomain()
                                        animalRepository.saveAnimal(animal)
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
                                                                                } catch (
                                                                                        _:
                                                                                                Exception) {}
                                                                        }
                                                                }
                                                        }
                                                        consultationRepository.saveConsultation(
                                                                consult
                                                        )
                                                        listener?.onLog(
                                                                "Consultation ${consult.uuid}: rations=${consult.rations.size}"
                                                        )
                                                }
                                        }
                                        // Compter les rations importées
                                        rationsImported +=
                                                animal.consultations.sumOf { it.rations.size }
                                } catch (e: Exception) {
                                        listener?.onLog(
                                                "Erreur animal ${animalApi.uuid}: ${e.message}"
                                        )
                                        advance()
                                }
                        }
                        animalsImported++
                        advance()
                        listener?.onLog(
                                "Animaux importés=$animalsImported, rations liées=$rationsImported"
                        )
                }

                // 6) Recettes (dépendent des aliments)
                if (envelope.recipes.isNotEmpty() && recipeRepository != null) {
                        listener?.onLog("Import des recettes (${envelope.recipes.size})…")
                        
                        
                        

                        // Construire un cache des aliments existants pour vérifier les références
                        val existingFoodIds: Set<String> =
                                if (foodRepository != null) {
                                        try {
                                                foodRepository.getAllFoods().map { it.uuid }.toSet()
                                        } catch (_: Exception) {
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
                                                        } catch (_: Exception) {}
                                                }
                                        }

                                        // Créer la recette avec l'UUID original
                                        val createdRecipe =
                                                recipeRepository.createRecipeWithUuid(
                                                        uuid = recipe.uuid,
                                                        name = recipe.name ?: "Recette importée",
                                                        espece = recipe.espece,
                                                        description = recipe.description,
                                                        number = recipe.number ?: 1
                                                )

                                        // Ajouter les ingrédients à la recette
                                        if (recipe.aliments.isNotEmpty()) {
                                                val alimentsRation =
                                                        recipe.aliments.map { ingredient ->
                                                                AlimentRation(
                                                                        uuid = ingredient.uuid,
                                                                        uuidUnif =
                                                                                ingredient
                                                                                        .refAlimUnif,
                                                                        refAlimUnif =
                                                                                ingredient
                                                                                        .refAlimUnif,
                                                                        quantite =
                                                                                ingredient.quantity,
                                                                        refTarget =
                                                                                ingredient
                                                                                        .refTarget,
                                                                        proportion = 0.0,
                                                                        weight = 1.0,
                                                                        category = 0,
                                                                        densiteEnergetique = 0.0,
                                                                        refRation =
                                                                                createdRecipe.uuid
                                                                )
                                                        }
                                                recipeRepository.addAliments(
                                                        createdRecipe.uuid,
                                                        alimentsRation
                                                )
                                        }

                                        recipesImported++
                                        advance()
                                } catch (e: Exception) {
                                        listener?.onLog(
                                                "Erreur recette ${recipeApi.uuid}: ${e.message}"
                                        )
                                        advance()
                                }
                        }
                        listener?.onLog("Recettes importées=$recipesImported")
                }

                // 7) Conseils (aucune dépendance)
                if (envelope.conseils.isNotEmpty() && conseilRepository != null) {
                        listener?.onLog("Import des conseils (${envelope.conseils.size})…")
                        for (conseilApi in envelope.conseils) {
                                try {
                                        val conseil = conseilApi.toDomain()
                                        // Vérifier si le conseil existe déjà
                                        val existingConseil = try {
                                                conseilRepository.getConseilsActifs().getOrThrow()
                                                        .find { it.id == conseil.id }
                                        } catch (e: Exception) {
                                                null
                                        }
                                        
                                        // Sauvegarder le conseil (insert ou update)
                                        conseilRepository.saveConseil(conseil)
                                        conseilsImported++
                                        advance()
                                } catch (e: Exception) {
                                        listener?.onLog("Erreur conseil ${conseilApi.id}: ${e.message}")
                                        advance()
                                }
                        }
                        listener?.onLog("Conseils importés=$conseilsImported")
                }

                return ImportCounts(
                        animals = animalsImported,
                        foods = foodsImported,
                        equations = equationsImported,
                        references = referencesImported,
                        biblios = biblioImported,
                        rations = rationsImported,
                        recipes = recipesImported,
                        conseils = conseilsImported
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
                val conseils: Int
        )
}
