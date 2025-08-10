package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.*
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
        private val equationRepository: EquationRepository? = null
) {

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
        val animals = animalRepository.getAllAnimals().map { it.toApi() }
        val foods = foodRepository?.getAllFoods()?.map { it.toApi() } ?: emptyList()
        val rations = emptyList<RationApi>()
        val equations = equationRepository?.getAllEquations()?.map { it.toApi() } ?: emptyList()
        val envelope =
                ApiEnvelope(
                        version = "1.0.0",
                        generatedAtEpochMs = Clock.System.now().toEpochMilliseconds(),
                        animals = animals,
                        foods = foods,
                        rations = rations,
                        equations = equations
                )
        return jsonPretty.encodeToString(envelope)
    }

    /** Export avec filtres et sélections par type/identifiants. */
    suspend fun exportWithSelection(options: ExportSelectionOptions): String {
        val allAnimals =
                if (options.includeAnimals) animalRepository.getAllAnimals() else emptyList()
        val animals: List<AnimalApi> =
                allAnimals
                        .asSequence()
                        .filter {
                            options.animalIds.isEmpty() || options.animalIds.contains(it.uuid)
                        }
                        .map { it.toApi() }
                        .toList()

        val allFoods =
                if (options.includeFoods) (foodRepository?.getAllFoods() ?: emptyList())
                else emptyList()
        val foods: List<FoodApi> =
                allFoods.asSequence()
                        .filter { options.foodIds.isEmpty() || options.foodIds.contains(it.uuid) }
                        .map { it.toApi() }
                        .toList()

        val rations = if (options.includeRations) emptyList<RationApi>() else emptyList()
        val equations =
                if (options.includeEquations)
                        (equationRepository?.getAllEquations()?.map { it.toApi() } ?: emptyList())
                else emptyList()

        val envelope =
                ApiEnvelope(
                        version = "1.0.0",
                        generatedAtEpochMs = Clock.System.now().toEpochMilliseconds(),
                        animals = animals,
                        foods = foods,
                        rations = rations,
                        equations = equations
                )
        return jsonPretty.encodeToString(envelope)
    }

    /**
     * Importe les données (pour l’instant animaux) au format API et les sauvegarde via les
     * repositories.
     */
    suspend fun importAll(apiJson: String): Int {
        val envelope = jsonPretty.decodeFromString<ApiEnvelope>(apiJson)
        var animalsImported = 0
        envelope.animals.forEach { api ->
            animalRepository.saveAnimal(api.toDomain())
            animalsImported++
        }
        // TODO: foods/rations/equations → à importer quand interfaces de repository seront prêtes
        return animalsImported
    }
}
