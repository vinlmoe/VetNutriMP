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
