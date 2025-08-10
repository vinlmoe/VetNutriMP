package fr.vetbrain.vetnutri_mp.Data

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Modèles JSON actuels pour l’API (nouveau format, distinct des anciens JSON). Tous les modèles
 * sont stables et versionnés pour une future REST API.
 */
@Serializable
data class ApiEnvelope(
        val version: String,
        val generatedAtEpochMs: Long,
        val animals: List<AnimalApi>,
        val foods: List<FoodApi> = emptyList(),
        val rations: List<RationApi> = emptyList(),
        val equations: List<EquationApi> = emptyList()
)

@Serializable
data class AnimalApi(
        val uuid: String,
        val name: String,
        val isDead: Boolean,
        val externalId: String? = null,
        val sexId: Int,
        val specieId: String,
        val ownerName: String,
        @Serializable(with = LocalDateSerializer::class) val birthdate: LocalDate? = null,
        val breed: String,
        val summary: String,
        val weights: List<WeightEntryApi> = emptyList(),
        val consultations: List<ConsultationApi> = emptyList()
)

@Serializable
data class WeightEntryApi(
        val uuid: String,
        @Serializable(with = LocalDateSerializer::class) val date: LocalDate,
        val valueKg: Float
)

@Serializable
data class ConsultationApi(
        val uuid: String,
        @Serializable(with = LocalDateSerializer::class) val date: LocalDate? = null,
        val objective: String = "",
        val observation: String = "",
        val report: String = "",
        val weightKg: Float? = null,
        val idealWeightKg: Float? = null,
        val waterMl: Float? = null,
        val bodyFatPercent: Float? = null,
        val bcs: Int? = null,
        val mcs: Int? = null
)

@Serializable
data class FoodApi(
        val uuid: String,
        val name: String? = null,
        val group: String? = null,
        val kind: String? = null,
        val brand: String? = null,
        val price: Double? = null,
        val species: List<String> = emptyList(),
        val indications: List<String> = emptyList(),
        val nutrients: Map<String, Float> = emptyMap()
)

@Serializable
data class RationApi(
        val uuid: String,
        val consultationId: String,
        val name: String,
        val isCurrent: Boolean,
        val items: List<RationItemApi>
)

@Serializable
data class RationItemApi(val foodId: String, val quantity: Float, val proportion: Float)

@Serializable
data class EquationApi(
        val uuid: String,
        val name: String,
        val specie: String?,
        val kind: String,
        val nutrient: String?,
        val script: String,
        val variables: List<String>,
        val ratio: Boolean
)

/** Mappers domaine -> API */
fun AnimalEv.toApi(): AnimalApi {
        return AnimalApi(
                uuid = uuid,
                name = nom,
                isDead = dead,
                externalId = id,
                sexId = sexId,
                specieId = specieId,
                ownerName = ownerName,
                birthdate = birthdate,
                breed = race,
                summary = summary,
                weights = weightHistory.map { it.toApi() },
                consultations = consultations.map { it.toApi() }
        )
}

fun WeightDate.toApi(): WeightEntryApi {
        return WeightEntryApi(uuid = uuid, date = date, valueKg = value)
}

fun ConsultationEv.toApi(): ConsultationApi {
        return ConsultationApi(
                uuid = uuid,
                date = date,
                objective = objectConsult,
                observation = observation,
                report = cRendu,
                weightKg = weight,
                idealWeightKg = idealWeight,
                waterMl = water,
                bodyFatPercent = bodyFat,
                bcs = BCS,
                mcs = MCS
        )
}

/** Mappers API -> domaine */
fun AnimalApi.toDomain(): AnimalEv {
        return AnimalEv(
                uuid = uuid,
                nom = name,
                dead = isDead,
                id = externalId,
                sexId = sexId,
                specieId = specieId,
                ownerName = ownerName,
                birthdate = birthdate,
                race = breed,
                summary = summary,
                consultations = consultations.map { it.toDomain() }.toMutableList(),
                weightHistory = weights.map { it.toDomain() }.toMutableList()
        )
}

// Mappeurs Foods
fun AlimentEv.toApi(): FoodApi {
        return FoodApi(
                uuid = uuid,
                name = nom,
                group = group?.name,
                kind = typeAliment?.name,
                brand = brand,
                price = price,
                species = especes,
                indications = indicat.map { it.name },
                nutrients = valMap.mapKeys { it.key.label }.mapValues { it.value.value }
        )
}

// Rations (simplifié: on sérialise ce qui est disponible depuis le domaine)
fun Ration.toApi(): RationApi {
        return RationApi(
                uuid = uuid,
                consultationId = idConsult,
                name = name,
                isCurrent = actual,
                items =
                        alimentMutableList.mapNotNull { item ->
                                val foodId: String? = item.aliment?.uuid
                                if (foodId != null)
                                        RationItemApi(
                                                foodId = foodId,
                                                quantity = item.quantite,
                                                proportion = item.proportion
                                        )
                                else null
                        }
        )
}

// Equations
fun Equation.toApi(): EquationApi {
        return EquationApi(
                uuid = uuid,
                name = name,
                specie = specie?.name,
                kind = kind.name,
                nutrient = nutrient?.label,
                script = equationScript,
                variables = variables.map { it.variable },
                ratio = ratio
        )
}

fun WeightEntryApi.toDomain(): WeightDate {
        return WeightDate(uuid = uuid, refAnimal = "", date = date, value = valueKg)
}

fun ConsultationApi.toDomain(): ConsultationEv {
        return ConsultationEv(
                uuid = uuid,
                date = date,
                objectConsult = objective,
                observation = observation,
                cRendu = report,
                weight = weightKg,
                idealWeight = idealWeightKg,
                water = waterMl,
                bodyFat = bodyFatPercent,
                BCS = bcs,
                MCS = mcs
        )
}
