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
        val equations: List<EquationApi> = emptyList(),
        val biblioRefs: List<BiblioRefApi> = emptyList(),
        val references: List<ReferenceEvApi> = emptyList()
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
        val mcs: Int? = null,
        val methodAnalysis: String = "",
        val k1Id: String? = null,
        val k1Value: Float? = null,
        val k2Id: String? = null,
        val k2Value: Float? = null,
        val k3Id: String? = null,
        val k3Value: Float? = null,
        val k4Id: String? = null,
        val k4Value: Float? = null,
        val k5Id: String? = null,
        val k5Value: Float? = null,
        val nLittle: Int? = null,
        val pAdult: Float? = null,
        val coefGes: Int? = null,
        val coefLact: Int? = null,
        val referenceGeneraleId: String? = null,
        val diseaseReferences: List<String> = emptyList(),
        val coefficientAjustement: Double = 1.0,
        val supplementalVariables: List<SupplementalVariableApi> = emptyList(),
        val rations: List<RationApi> = emptyList()
)

@Serializable
data class FoodApi(
        val uuid: String,
        val name: String? = null,
        val group: String? = null,
        val kind: String? = null,
        val brand: String? = null,
        val price: Double? = null,
        val categoryPrice: String? = null,
        val ingredients: String? = null,
        val gamme: String? = null,
        val presentation: String? = null,
        val presentationQuantity: Float? = null,
        val deprecated: Boolean? = null,
        val dataB: String? = null,
        val consistent: Boolean? = null,
        val rationId: String? = null,
        val species: List<String> = emptyList(),
        val indications: List<String> = emptyList(),
        val nutrients: Map<String, Float> = emptyMap()
)

@Serializable
data class RationApi(
        val uuid: String,
        val consultationId: String,
        val name: String,
        val coef: Float,
        val isCurrent: Boolean,
        val number: Int,
        val specie: String?,
        val isRecipe: Boolean,
        val description: String,
        val items: List<RationItemApi>
)

@Serializable
data class RationItemApi(
        val uuid: String,
        val foodId: String,
        val quantity: Float,
        val proportion: Float,
        val weight: Float? = null,
        val category: Int? = null,
        val density: Double? = null
)

@Serializable data class SupplementalVariableApi(val variable: String, val value: Float)

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

// --- Mappers API <-> Domaine (compléments) ---

fun FoodApi.toDomain(): AlimentEv {
        // Normalisation robuste espèces/indications
        fun clean(s: String): String = s.trim().replace("\\\\[|\\\\]|\"".toRegex(), "")
        val normalizedSpecies: MutableList<String> =
                species
                        .map { raw ->
                                val c = clean(raw)
                                val resolved =
                                        try {
                                                fr.vetbrain.vetnutri_mp.Enumer.Espece.getFromString(
                                                        c
                                                )
                                        } catch (e: Exception) {
                                                null
                                        }
                                when {
                                        resolved != null -> resolved.name
                                        c.toIntOrNull() != null -> {
                                                // tenter via catégorie numérique
                                                val byCat =
                                                        try {
                                                                fr.vetbrain.vetnutri_mp.Enumer
                                                                        .Espece.getEnumFromInt(
                                                                        c.toInt()
                                                                )
                                                        } catch (e: Exception) {
                                                                null
                                                        }
                                                byCat?.name ?: c
                                        }
                                        else -> {
                                                val byName =
                                                        try {
                                                                fr.vetbrain.vetnutri_mp.Enumer
                                                                        .Espece.valueOf(
                                                                        c.uppercase()
                                                                )
                                                        } catch (e: Exception) {
                                                                null
                                                        }
                                                byName?.name ?: c
                                        }
                                }
                        }
                        .toMutableList()

        val normalizedIndics: MutableList<fr.vetbrain.vetnutri_mp.Enumer.AlimIndic> =
                indications
                        .mapNotNull { raw ->
                                val c = clean(raw)
                                val fromString =
                                        try {
                                                fr.vetbrain.vetnutri_mp.Enumer.AlimIndic
                                                        .getFromString(c)
                                        } catch (e: Exception) {
                                                null
                                        }
                                fromString
                                        ?: try {
                                                fr.vetbrain.vetnutri_mp.Enumer.AlimIndic.valueOf(
                                                        c.uppercase()
                                                )
                                        } catch (e: Exception) {
                                                null
                                        }
                        }
                        .toMutableList()

        val aliment =
                AlimentEv(
                        uuid = uuid,
                        nom = name,
                        group =
                                group?.let {
                                        runCatching {
                                                        fr.vetbrain.vetnutri_mp.Enumer.GroupAlim
                                                                .valueOf(it)
                                                }
                                                .getOrNull()
                                },
                        typeAliment =
                                kind?.let {
                                        runCatching {
                                                        fr.vetbrain.vetnutri_mp.Enumer.FoodKind
                                                                .valueOf(it)
                                                }
                                                .getOrNull()
                                },
                        ingredients = ingredients,
                        price = price,
                        categPrice = categoryPrice,
                        brand = brand,
                        gamme = gamme,
                        cont =
                                presentation?.let {
                                        fr.vetbrain.vetnutri_mp.Enumer.ContEnum.getByName(
                                                if (it == "YES") "CAN" else it
                                        )
                                },
                        quantInt = presentationQuantity,
                        deprecated = deprecated ?: false,
                        dataB = dataB,
                        especes = normalizedSpecies,
                        indicat = normalizedIndics,
                        rationUUID = rationId
                )
        nutrients.forEach { (label, value) ->
                fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver.AllNutrientResolver(label)?.let {
                        aliment.setNutrient(it, value)
                }
        }
        return aliment
}

@Serializable
data class BiblioRefApi(
        val uuid: String,
        val firstAuthor: String,
        val year: Int,
        val completeRef: String,
        val comments: String = "",
        val bibtex: String = "",
        val consistent: Int = 1
)

@Serializable
data class ReferenceNutrientApi(
        val nutrientLabel: String,
        val reflevel: String,
        val quantity: Float,
        val uniteReqId: Int,
        val biblioRefId: String? = null
)

@Serializable
data class ReferenceEvApi(
        val uuid: String,
        val nom: String,
        val description: String = "",
        val maladie: Boolean = false,
        val nomMaladie: String = "",
        val nomEnergie: String = "",
        val consistent: Int = 1,
        val espece: String,
        val stadePhysio: String,
        val equationBW: String? = null,
        val equationBEE: String? = null,
        val equationDEcom: String? = null,
        val equationDEraw: String? = null,
        val equationME: String? = null,
        val equationsNut: List<String> = emptyList(),
        val nutrients: List<ReferenceNutrientApi> = emptyList()
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
                mcs = MCS,
                methodAnalysis = methodAnalysis,
                k1Id = k1Id,
                k1Value = k1Value,
                k2Id = k2Id,
                k2Value = k2Value,
                k3Id = k3Id,
                k3Value = k3Value,
                k4Id = k4Id,
                k4Value = k4Value,
                k5Id = k5Id,
                k5Value = k5Value,
                nLittle = nLittle,
                pAdult = pAdult,
                coefGes = coefGes,
                coefLact = coefLact,
                referenceGeneraleId = referenceGeneraleId,
                diseaseReferences = referencesMaladies,
                coefficientAjustement = coefficientAjustement,
                supplementalVariables =
                        suppVarp.mapNotNull { sv ->
                                sv.variable?.name?.let { vn ->
                                        SupplementalVariableApi(vn, sv.varue ?: 0f)
                                }
                        },
                rations = rations.map { it.toApi() }
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
                categoryPrice = categPrice,
                ingredients = ingredients,
                gamme = gamme,
                presentation = cont?.name,
                presentationQuantity = quantInt,
                deprecated = deprecated,
                dataB = dataB,
                consistent = consistent,
                rationId = rationUUID,
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
                coef = coef,
                isCurrent = actual,
                number = number,
                specie = espece,
                isRecipe = recette,
                description = description,
                items =
                        alimentMutableList.mapNotNull { item ->
                                val foodId: String? = item.aliment?.uuid
                                if (foodId != null)
                                        RationItemApi(
                                                uuid = item.uuid,
                                                foodId = foodId,
                                                quantity = item.quantite,
                                                proportion = item.proportion,
                                                weight = item.weight,
                                                category = item.category,
                                                density = item.densiteEnergetique
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

// Mappers API -> domaine pour les équations
fun EquationApi.toDomain(): Equation {
        return Equation(
                uuid = uuid,
                name = name,
                specie = specie?.let { fr.vetbrain.vetnutri_mp.Enumer.Espece.valueOf(it) },
                kind = fr.vetbrain.vetnutri_mp.Enumer.EquationKind.valueOf(kind),
                nutrient =
                        nutrient?.let {
                                fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver.findNutrientByLabel(
                                        it
                                )
                        },
                equationScript = script,
                variables =
                        variables
                                .mapNotNull {
                                        fr.vetbrain.vetnutri_mp.Enumer.VariableKind.entries.find {
                                                vk ->
                                                vk.variable == it || vk.label == it
                                        }
                                }
                                .toMutableList(),
                ratio = ratio,
                bib = BiblioRef(uuid = "default-biblio")
        )
}

fun BiblioRef.toApi(): BiblioRefApi {
        return BiblioRefApi(
                uuid = uuid,
                firstAuthor = firstAuthor,
                year = year,
                completeRef = completeRef,
                comments = comments,
                bibtex = bibtex,
                consistent = consistent
        )
}

fun ReferenceEv.toApiRef(): ReferenceEvApi {
        val nutrients = mutableListOf<ReferenceNutrientApi>()
        getRefMapMin().forEach { (nutrient, ref) ->
                nutrients.add(
                        ReferenceNutrientApi(
                                nutrientLabel = nutrient.label,
                                reflevel = "MIN",
                                quantity = ref.quantite,
                                uniteReqId = ref.uniteReq.getID(),
                                biblioRefId = ref.biblio.uuid
                        )
                )
        }
        getRefMapMax().forEach { (nutrient, ref) ->
                nutrients.add(
                        ReferenceNutrientApi(
                                nutrientLabel = nutrient.label,
                                reflevel = "MAX",
                                quantity = ref.quantite,
                                uniteReqId = ref.uniteReq.getID(),
                                biblioRefId = ref.biblio.uuid
                        )
                )
        }
        getRefMapOMin().forEach { (nutrient, ref) ->
                nutrients.add(
                        ReferenceNutrientApi(
                                nutrientLabel = nutrient.label,
                                reflevel = "OPTIMIN",
                                quantity = ref.quantite,
                                uniteReqId = ref.uniteReq.getID(),
                                biblioRefId = ref.biblio.uuid
                        )
                )
        }
        getRefMapOMax().forEach { (nutrient, ref) ->
                nutrients.add(
                        ReferenceNutrientApi(
                                nutrientLabel = nutrient.label,
                                reflevel = "OPTIMAX",
                                quantity = ref.quantite,
                                uniteReqId = ref.uniteReq.getID(),
                                biblioRefId = ref.biblio.uuid
                        )
                )
        }

        return ReferenceEvApi(
                uuid = uuid,
                nom = nom,
                description = description,
                maladie = maladie,
                nomMaladie = nomMaladie,
                nomEnergie = nomEnergie,
                consistent = consistent,
                espece = espece.name,
                stadePhysio = stadePhysio.name,
                equationBW = equationBW?.uuid,
                equationBEE = equationBEE?.uuid,
                equationDEcom = equationDEcom?.uuid,
                equationDEraw = equationDEraw?.uuid,
                equationME = equationME?.uuid,
                equationsNut = equationsNut.map { it.uuid },
                nutrients = nutrients
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
                MCS = mcs,
                methodAnalysis = methodAnalysis,
                k1Id = k1Id,
                k1Value = k1Value,
                k2Id = k2Id,
                k2Value = k2Value,
                k3Id = k3Id,
                k3Value = k3Value,
                k4Id = k4Id,
                k4Value = k4Value,
                k5Id = k5Id,
                k5Value = k5Value,
                nLittle = nLittle,
                pAdult = pAdult,
                coefGes = coefGes,
                coefLact = coefLact,
                suppVarp =
                        supplementalVariables
                                .mapNotNull { api ->
                                        runCatching {
                                                        SupplementalvariableP(
                                                                variable =
                                                                        fr.vetbrain.vetnutri_mp
                                                                                .Enumer.VariableKind
                                                                                .valueOf(
                                                                                        api.variable
                                                                                ),
                                                                varue = api.value
                                                        )
                                                }
                                                .getOrNull()
                                }
                                .toMutableList(),
                rations =
                        rations
                                .map { rApi ->
                                        Ration(
                                                uuid = rApi.uuid,
                                                idConsult = rApi.consultationId,
                                                name = rApi.name,
                                                coef = rApi.coef,
                                                actual = rApi.isCurrent,
                                                number = rApi.number,
                                                espece = rApi.specie,
                                                recette = rApi.isRecipe,
                                                description = rApi.description,
                                                alimentMutableList =
                                                        rApi.items
                                                                .map { itApi ->
                                                                        AlimentRation(
                                                                                uuid = itApi.uuid,
                                                                                uuidUnif =
                                                                                        itApi.foodId,
                                                                                quantite =
                                                                                        itApi.quantity,
                                                                                proportion =
                                                                                        itApi.proportion,
                                                                                aliment = null,
                                                                                weight =
                                                                                        itApi.weight
                                                                                                ?: 1f,
                                                                                category =
                                                                                        itApi.category
                                                                                                ?: 0,
                                                                                densiteEnergetique =
                                                                                        itApi.density
                                                                                                ?: 0.0,
                                                                                refAlimUnif =
                                                                                        itApi.foodId,
                                                                                refRation =
                                                                                        rApi.uuid
                                                                        )
                                                                }
                                                                .toMutableList()
                                        )
                                }
                                .toMutableList(),
                referenceGeneraleId = referenceGeneraleId,
                referencesMaladies = diseaseReferences.toMutableList(),
                coefficientAjustement = coefficientAjustement
        )
}
