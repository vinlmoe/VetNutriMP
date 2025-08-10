package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.*
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Repository pour export/import JSON des objets ACTUELS (nouveau format pour future REST API). Ne
 * modifie pas les structures historiques déjà utilisées pour réimporter d’anciens JSON.
 */
class ExportImportRepository(
        private val animalRepository: AnimalRepository,
        private val foodRepository: FoodRepository? = null,
        private val equationRepository: EquationRepository? = null,
        private val referenceRepository: DatabaseReferenceEvRepository? = null,
        private val biblioRepository: BiblioRefRepository? = null,
        private val consultationRepository: ConsultationRepository? = null
) {
        class ImportProgressListener(val onProgress: (Float) -> Unit, val onLog: (String) -> Unit)

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
                val includeEquations: Boolean = true,
                val animalIds: Set<String> = emptySet(),
                val foodIds: Set<String> = emptySet()
        )

        /** Exporte l’ensemble des données (animaux, aliments, rations, équations) au format API. */
        suspend fun exportAll(): String {
                val domainAnimals = animalRepository.getAllAnimals()
                // Charger les consultations/rations pour chaque animal pour un export complet
                val animalsWithConsultations: List<fr.vetbrain.vetnutri_mp.Data.AnimalEv> =
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
                val rations: List<RationApi> =
                        animalsWithConsultations
                                .asSequence()
                                .flatMap { animal: fr.vetbrain.vetnutri_mp.Data.AnimalEv ->
                                        animal.consultations.asSequence().flatMap {
                                                consult: fr.vetbrain.vetnutri_mp.Data.ConsultationEv
                                                ->
                                                consult.rations.asSequence().map {
                                                        ration: fr.vetbrain.vetnutri_mp.Data.Ration
                                                        ->
                                                        ration.toApi()
                                                }
                                        }
                                }
                                .toList()
                val equations =
                        equationRepository?.getAllEquations()?.map { it.toApi() } ?: emptyList()
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
                                rations = rations,
                                equations = equations,
                                biblioRefs = biblioRefs,
                                references = references
                        )
                return jsonPretty.encodeToString(envelope)
        }

        /** Export avec filtres et sélections par type/identifiants. */
        suspend fun exportWithSelection(options: ExportSelectionOptions): String {
                val allDomainAnimals =
                        if (options.includeAnimals) animalRepository.getAllAnimals()
                        else emptyList()
                // Charger les consultations/rations sur les animaux retenus
                val allDomainAnimalsWithConsultations: List<fr.vetbrain.vetnutri_mp.Data.AnimalEv> =
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

                val rations: List<RationApi> =
                        if (options.includeRations) {
                                filteredDomainAnimals
                                        .asSequence()
                                        .flatMap { animal: fr.vetbrain.vetnutri_mp.Data.AnimalEv ->
                                                animal.consultations.asSequence().flatMap {
                                                        consult:
                                                                fr.vetbrain.vetnutri_mp.Data.ConsultationEv
                                                        ->
                                                        consult.rations.asSequence().map {
                                                                ration:
                                                                        fr.vetbrain.vetnutri_mp.Data.Ration
                                                                ->
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
                                rations = rations,
                                equations = equations,
                                biblioRefs = biblioRefs,
                                references = references
                        )
                return jsonPretty.encodeToString(envelope)
        }

        /**
         * Importe les données (pour l’instant animaux) au format API et les sauvegarde via les
         * repositories.
         */
        suspend fun importAll(
                apiJson: String,
                listener: ImportProgressListener? = null
        ): ImportCounts {

                listener?.onProgress(0.02f)
                listener?.onLog("Lecture/Parsing du JSON…")
                val envelope = jsonPretty.decodeFromString<ApiEnvelope>(apiJson)

                listener?.onLog(
                        "Contenu: animals=${envelope.animals.size}, foods=${envelope.foods.size}, equations=${envelope.equations.size}, biblioRefs=${envelope.biblioRefs.size}, references=${envelope.references.size}"
                )
                var animalsImported: Int = 0
                var foodsImported: Int = 0
                var equationsImported: Int = 0
                var referencesImported: Int = 0
                var biblioImported: Int = 0
                var rationsImported: Int = 0
                val totalUnits: Int =
                        (envelope.foods.size +
                                        envelope.equations.size +
                                        envelope.biblioRefs.size +
                                        envelope.animals.size +
                                        envelope.references.size)
                                .coerceAtLeast(1)
                var processedUnits = 0
                fun advance(units: Int = 1) {
                        processedUnits += units
                        val p = 0.1f + 0.9f * (processedUnits.toFloat() / totalUnits.toFloat())
                        listener?.onProgress(p.coerceIn(0f, 1f))
                }
                listener?.onProgress(0.1f)

                // 1) Aliments
                if (envelope.foods.isNotEmpty() && foodRepository != null) {

                        listener?.onLog("Import des aliments (${envelope.foods.size})…")
                        if (foodRepository is DatabaseFoodRepository) {
                                try {
                                        val aliments = envelope.foods.map { it.toDomain() }
                                        val res = foodRepository.importFoodsDomain(aliments)
                                        foodsImported += res.importedCount + res.updatedCount
                                        // Avancer d'un coup pour tous les aliments
                                        advance(envelope.foods.size)
                                        listener?.onLog(
                                                "Aliments importés=${res.importedCount}, mis à jour=${res.updatedCount}, erreurs=${res.errorCount}"
                                        )
                                } catch (e: Exception) {
                                        listener?.onLog("Erreur import bulk aliments: ${e.message}")
                                }
                        } else {
                                // Fallback: insertion/MAJ unitaire si repo non-DB
                                // Activer le mode batch si disponible (noop sinon)
                                for (api in envelope.foods) {
                                        try {
                                                val aliment = api.toDomain()
                                                // Sans accès rapide aux IDs, on tente update puis
                                                // insert
                                                try {
                                                        foodRepository.updateFood(aliment)
                                                } catch (_: Exception) {
                                                        foodRepository.insertFood(aliment)
                                                }
                                                foodsImported++
                                        } catch (e: Exception) {
                                                listener?.onLog(
                                                        "Erreur aliment ${api.uuid}: ${e.message}"
                                                )
                                        } finally {
                                                advance()
                                        }
                                }
                        }
                }

                // 2) Équations
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

                // 3) Références biblio
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

                // 4) Animaux + consultations/rations
                if (envelope.animals.isNotEmpty())
                        listener?.onLog("Import des animaux (${envelope.animals.size})…")
                // Préparer un set d'UUID d'aliments pour limiter les accès DB répétés lors des
                // rations
                var existingFoodIdsForRations: MutableSet<String> = mutableSetOf()
                if (foodRepository != null) {
                        existingFoodIdsForRations =
                                try {
                                        if (foodRepository is DatabaseFoodRepository) {
                                                foodRepository.getAllFoodIds().toMutableSet()
                                        } else {
                                                foodRepository
                                                        .getAllFoodsLight()
                                                        .map { it.uuid }
                                                        .toMutableSet()
                                        }
                                } catch (_: Exception) {
                                        mutableSetOf()
                                }
                }

                for (animalApi in envelope.animals) {
                        val animal = animalApi.toDomain()
                        animalRepository.saveAnimal(animal)

                        // Sauvegarder les consultations avec rations si possible
                        if (consultationRepository != null) {
                                for (consult in animal.consultations) {
                                        // Créer les aliments manquants référencés par les rations
                                        if (foodRepository != null) {
                                                consult.rations.forEach { ration ->
                                                        ration.alimentMutableList.forEach { ar ->
                                                                val foodId: String? = ar.refAlimUnif
                                                                if (foodId != null &&
                                                                                foodRepository !=
                                                                                        null
                                                                ) {
                                                                        // Éviter l'appel DB si
                                                                        // l'UUID est déjà connu
                                                                        val existsLocally: Boolean =
                                                                                existingFoodIdsForRations
                                                                                        .contains(
                                                                                                foodId
                                                                                        )
                                                                        if (!existsLocally) {
                                                                                // Insérer un
                                                                                // placeholder
                                                                                // rapide
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
                                                                                                        foodId,
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
                                                                                                .insertFood(
                                                                                                        placeholder
                                                                                                )
                                                                                        existingFoodIdsForRations
                                                                                                .add(
                                                                                                        foodId
                                                                                                )
                                                                                        foodsImported++
                                                                                } catch (
                                                                                        _:
                                                                                                Exception) {}
                                                                        }
                                                                }
                                                        }
                                                }
                                        }
                                        consultationRepository.saveConsultation(consult)
                                        listener?.onLog(
                                                "Consultation ${consult.uuid}: rations=${consult.rations.size}"
                                        )
                                }
                                // Compter les rations importées
                                rationsImported += animal.consultations.sumOf { it.rations.size }
                        }
                        animalsImported++
                        advance()
                }

                listener?.onLog("Animaux importés=$animalsImported, rations liées=$rationsImported")

                // 5) Références nutritionnelles (avec liens vers équations et biblio)
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
                                                        espece =
                                                                fr.vetbrain.vetnutri_mp.Enumer
                                                                        .Espece.valueOf(
                                                                        refApi.espece
                                                                ),
                                                        stadePhysio =
                                                                fr.vetbrain.vetnutri_mp.Enumer
                                                                        .StadePhysio.valueOf(
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
                                        // Nutriments
                                        for (n in refApi.nutrients) {
                                                val nutrient =
                                                        fr.vetbrain.vetnutri_mp.Enumer
                                                                .NutrientResolver
                                                                .findNutrientByLabel(
                                                                        n.nutrientLabel
                                                                )
                                                if (nutrient != null) {
                                                        val bib: BiblioRef =
                                                                if (n.biblioRefId != null) {
                                                                        biblioCache[n.biblioRefId]
                                                                                ?: BiblioRef()
                                                                } else BiblioRef()
                                                        val level =
                                                                when (n.reflevel) {
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
                                                        ref.definirNutriment(
                                                                n.quantity,
                                                                nutrient,
                                                                level,
                                                                fr.vetbrain.vetnutri_mp.Enumer
                                                                        .UnitReqEnum.getById(
                                                                        n.uniteReqId
                                                                ),
                                                                bib
                                                        )
                                                }
                                        }
                                        referenceRepository.saveReferenceEv(ref)
                                        println("IMPORT API: referenceEv ${ref.uuid} sauvegardée")
                                        listener?.onLog("ReferenceEv ${ref.uuid} sauvegardée")
                                        referencesImported++
                                        advance()
                                } catch (e: Exception) {
                                        println(
                                                "IMPORT API: erreur referenceEv=${refApi.uuid} → ${e.message}"
                                        )
                                        listener?.onLog(
                                                "Erreur referenceEv ${refApi.uuid}: ${e.message}"
                                        )
                                        advance()
                                }
                        }
                        println("IMPORT API: referencesEv importées=$referencesImported")
                        listener?.onLog("Références nutritionnelles importées=$referencesImported")
                }

                return ImportCounts(
                        animals = animalsImported,
                        foods = foodsImported,
                        equations = equationsImported,
                        references = referencesImported,
                        biblios = biblioImported,
                        rations = rationsImported
                )
        }

        data class ImportCounts(
                val animals: Int,
                val foods: Int,
                val equations: Int,
                val references: Int,
                val biblios: Int,
                val rations: Int
        )
}
