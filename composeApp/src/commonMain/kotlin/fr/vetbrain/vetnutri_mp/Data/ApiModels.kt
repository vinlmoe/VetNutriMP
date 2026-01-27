package fr.vetbrain.vetnutri_mp.Data

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Modèles JSON (nouveau format API, versionné).
 * Distinct des anciens JSON ; utilisés pour import/export et future REST.
 */
@Serializable
data class ApiEnvelope(
        val version: String,
        val generatedAtEpochMs: Long,
        val animals: List<AnimalApi>,
        val foods: List<FoodApi> = emptyList(),
        val rations: List<RationApi> = emptyList(),
        val recipes: List<RecipeApi> = emptyList(),
        val equations: List<EquationApi> = emptyList(),
        val biblioRefs: List<BiblioRefApi> = emptyList(),
        val references: List<ReferenceEvApi> = emptyList(),
        val conseils: List<ConseilApi> = emptyList(),
        val consultationKeywords: List<ConsultationKeywordApi> = emptyList()
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
        val valueKg: Double
)

@Serializable
data class ConsultationApi(
        val uuid: String,
        @Serializable(with = LocalDateSerializer::class) val date: LocalDate? = null,
        val objective: String = "",
        val observation: String = "",
        val report: String = "",
        val weightKg: Double? = null,
        val idealWeightKg: Double? = null,
        val waterMl: Double? = null,
        val bodyFatPercent: Double? = null,
        val bcs: Int? = null,
        val mcs: Int? = null,
        val methodAnalysis: String = "",
        val k1Id: String? = null,
        val k1Value: Double? = null,
        val k2Id: String? = null,
        val k2Value: Double? = null,
        val k3Id: String? = null,
        val k3Value: Double? = null,
        val k4Id: String? = null,
        val k4Value: Double? = null,
        val k5Id: String? = null,
        val k5Value: Double? = null,
        val nLittle: Int? = null,
        val pAdult: Double? = null,
        val coefGes: Int? = null,
        val coefLact: Int? = null,
        val referenceGeneraleId: String? = null,
        val diseaseReferences: List<String> = emptyList(),
        val coefficientAjustement: Double = 1.0,
        val keywords: List<String> = emptyList(),
        val supplementalVariables: List<SupplementalVariableApi> = emptyList(),
        val rations: List<RationApi> = emptyList()
)

@Serializable
data class ConsultationKeywordApi(
        val uuid: String,
        val label: String
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
        val presentationQuantity: Double? = null,
        val deprecated: Boolean? = null,
        val dataB: String? = null,
        val consistent: Boolean? = null,
        val rationId: String? = null,
        val species: List<String> = emptyList(),
        val indications: List<String> = emptyList(),
        val nutrients: Map<String, Double> = emptyMap()
)

@Serializable
data class RationApi(
        val uuid: String,
        val consultationId: String,
        val name: String,
        val coef: Double,
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
        val quantity: Double,
        val proportion: Double,
        val weight: Double? = null,
        val category: Int? = null,
        val density: Double? = null
)

@Serializable data class SupplementalVariableApi(val variable: String, val value: Double)

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
                                        fr.vetbrain.vetnutri_mp.Enumer.FoodKindResolver
                                                .resoudreFoodKindBrut(it)
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
                // Essayer d'abord la résolution directe
                var nutrient = fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver.AllNutrientResolver(label)
                
                // Si la résolution échoue, essayer de nettoyer la clé
                if (nutrient == null) {
                        val cleanedKey = label.trim().replace("_", " ")
                        nutrient = fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver.AllNutrientResolver(cleanedKey)
                }
                
                // Si la résolution réussit, ajouter le nutriment
                if (nutrient != null) {
                        aliment.setNutrient(nutrient, value)
                } else {
                        // Log pour débogage - nutriment non résolu
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
        val quantity: Double,
        val uniteReqId: Int,
        val biblioRefId: String? = null
)

@Serializable
data class ReferenceCoefficientApi(
        val uuid: String,
        val groupType: String, // "k1", "k2", "k3", "k4", "k5"
        val description: String,
        val coef: Double,
        val groupUUID: Int
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
        val nutrients: List<ReferenceNutrientApi> = emptyList(),
        val coefficients: List<ReferenceCoefficientApi> = emptyList()
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
                keywords = keywordIds.toList(),
                supplementalVariables =
                        suppVarp.mapNotNull { sv ->
                                sv.variable?.name?.let { vn ->
                                        SupplementalVariableApi(vn, sv.varue ?: 0.0)
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
        // Résolution robuste du kind (tolérant à la casse et aux alias)
        val resolvedKind = run {
                val raw = kind.trim()
                // essayer directement
                runCatching {
                        fr.vetbrain.vetnutri_mp.Enumer.EquationKind.valueOf(raw)
                }.getOrElse {
                        // essayer en uppercase (pour des valeurs comme "ENErcomp")
                        runCatching {
                                fr.vetbrain.vetnutri_mp.Enumer.EquationKind.valueOf(raw.uppercase())
                        }.getOrElse {
                                // alias connus
                                when (raw.lowercase()) {
                                        "enercomp", "energycomp", "energycomposition", "energycompdesc" ->
                                                fr.vetbrain.vetnutri_mp.Enumer.EquationKind.ENERCOMP
                                        "complnut", "complementary", "complementarynutrient" ->
                                                fr.vetbrain.vetnutri_mp.Enumer.EquationKind.COMPLEMENTARY_NUTRIENT
                                        "energyneed" -> fr.vetbrain.vetnutri_mp.Enumer.EquationKind.ENERGYNEED
                                        "energydensity" -> fr.vetbrain.vetnutri_mp.Enumer.EquationKind.ENERGYDENSITY
                                        "mw", "metabolicweight" -> fr.vetbrain.vetnutri_mp.Enumer.EquationKind.MW
                                        "indicator" -> fr.vetbrain.vetnutri_mp.Enumer.EquationKind.INDICATOR
                                        "need", "needeq" -> fr.vetbrain.vetnutri_mp.Enumer.EquationKind.NEED
                                        else -> fr.vetbrain.vetnutri_mp.Enumer.EquationKind.ENERGYNEED
                                }
                        }
                }
        }
        return Equation(
                uuid = uuid,
                name = name,
                specie = specie?.let { fr.vetbrain.vetnutri_mp.Enumer.Espece.valueOf(it) },
                kind = resolvedKind,
                nutrient =
                        nutrient?.let {
                                // Utiliser le resolver "global" qui couvre aussi NutrientAnalysis
                                fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver.AllNutrientResolver(
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

        // 🆕 AJOUTER LES COEFFICIENTS
        val coefficients = mutableListOf<ReferenceCoefficientApi>()

        // Ajouter les coefficients k1
        modk1.forEach { coef ->
                coefficients.add(
                        ReferenceCoefficientApi(
                                uuid = coef.uuid,
                                groupType = "k1",
                                description = coef.description ?: "Normal",
                                coef = coef.coef ?: 1.0,
                                groupUUID = coef.groupUUID ?: 0
                        )
                )
        }

        // Ajouter les coefficients k2
        modk2.forEach { coef ->
                coefficients.add(
                        ReferenceCoefficientApi(
                                uuid = coef.uuid,
                                groupType = "k2",
                                description = coef.description ?: "Normal",
                                coef = coef.coef ?: 1.0,
                                groupUUID = coef.groupUUID ?: 1
                        )
                )
        }

        // Ajouter les coefficients k3
        modk3.forEach { coef ->
                coefficients.add(
                        ReferenceCoefficientApi(
                                uuid = coef.uuid,
                                groupType = "k3",
                                description = coef.description ?: "Normal",
                                coef = coef.coef ?: 1.0,
                                groupUUID = coef.groupUUID ?: 2
                        )
                )
        }

        // Ajouter les coefficients k4
        modk4.forEach { coef ->
                coefficients.add(
                        ReferenceCoefficientApi(
                                uuid = coef.uuid,
                                groupType = "k4",
                                description = coef.description ?: "Normal",
                                coef = coef.coef ?: 1.0,
                                groupUUID = coef.groupUUID ?: 3
                        )
                )
        }

        // Ajouter les coefficients k5
        modk5.forEach { coef ->
                coefficients.add(
                        ReferenceCoefficientApi(
                                uuid = coef.uuid,
                                groupType = "k5",
                                description = coef.description ?: "Normal",
                                coef = coef.coef ?: 1.0,
                                groupUUID = coef.groupUUID ?: 4
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
                nutrients = nutrients,
                coefficients = coefficients
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
                                                                                                ?: 1.0,
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
                keywordIds = keywords.toMutableList(),
                coefficientAjustement = coefficientAjustement
        )
}

// ============================================================================
// MODÈLES API POUR LES RECETTES
// ============================================================================

@Serializable
data class RecipeApi(
        val uuid: String,
        val name: String,
        val number: Int = 0,
        val specie: String? = null,
        val description: String? = null,
        val aliments: List<RecipeIngredientApi> = emptyList()
)

@Serializable
data class RecipeIngredientApi(
        val uuid: String,
        val foodId: String,
        val quantity: Double,
        val targetRef: Int = 0
)

// ============================================================================
// FONCTIONS DE CONVERSION POUR LES RECETTES
// ============================================================================

fun fr.vetbrain.vetnutri_mp.Data.Recette.toApi(): RecipeApi {
        return RecipeApi(
                uuid = uuid,
                name = name ?: "",
                number = number,
                specie = espece,
                description = description,
                aliments = aliments.map { it.toApi() }
        )
}

fun fr.vetbrain.vetnutri_mp.Data.AlimentRecette.toApi(): RecipeIngredientApi {
        return RecipeIngredientApi(
                uuid = uuid,
                foodId = refAlimUnif,
                quantity = quantity,
                targetRef = refTarget
        )
}

fun RecipeApi.toDomain(): fr.vetbrain.vetnutri_mp.Data.Recette {
        return fr.vetbrain.vetnutri_mp.Data.Recette(
                uuid = uuid,
                name = name,
                number = number,
                espece = specie,
                description = description,
                aliments = aliments.map { it.toDomain() }.toMutableList()
        )
}

fun RecipeIngredientApi.toDomain(): fr.vetbrain.vetnutri_mp.Data.AlimentRecette {
        return fr.vetbrain.vetnutri_mp.Data.AlimentRecette(
                uuid = uuid,
                refAlimUnif = foodId,
                refRecipe = "", // Sera défini lors de l'import
                quantity = quantity,
                refTarget = targetRef
        )
}

// ============================================================================
// MODÈLES API POUR LES CONSEILS
// ============================================================================

@Serializable
data class ConseilApi(
        val id: String,
        val title: String,
        val content: RichTextContentApi,
        val category: String,
        val tags: List<String> = emptyList(),
        val priority: Int = 0,
        val isActive: Boolean = true,
        val targetSpecies: List<String> = emptyList(),
        val targetAgeGroups: List<String> = emptyList(),
        val usageCount: Int = 0,
        val lastUsed: Long? = null, // Timestamp en millisecondes
        val isTemplate: Boolean = false,
        val createdAt: Long, // Timestamp en millisecondes
        val updatedAt: Long // Timestamp en millisecondes
)

@Serializable
data class RichTextContentApi(
        val blocks: List<TextBlockApi> = emptyList()
)

@Serializable
sealed class TextBlockApi {
        abstract val id: String

        @Serializable
        data class Paragraph(
                override val id: String,
                val text: String,
                val formatting: TextFormattingApi = TextFormattingApi()
        ) : TextBlockApi()

        @Serializable
        data class Heading(
                override val id: String,
                val level: Int, // 1-6 pour h1-h6
                val text: String
        ) : TextBlockApi()

        @Serializable
        data class ListBlock(
                override val id: String,
                val items: List<String>,
                val isOrdered: Boolean = false
        ) : TextBlockApi()

        @Serializable
        data class TableBlock(
                override val id: String,
                val headers: List<String>,
                val rows: List<List<String>>
        ) : TextBlockApi()

        @Serializable
        data class RawHtml(
                override val id: String,
                val html: String
        ) : TextBlockApi()
}

@Serializable
data class TextFormattingApi(
        val isBold: Boolean = false,
        val isItalic: Boolean = false,
        val isUnderline: Boolean = false,
        val isStrikethrough: Boolean = false,
        val fontSize: Int? = null,
        val color: String? = null,
        val alignment: String = "LEFT" // LEFT, CENTER, RIGHT, JUSTIFY
)

// ============================================================================
// FONCTIONS DE CONVERSION POUR LES CONSEILS
// ============================================================================

fun fr.vetbrain.vetnutri_mp.Export.HtmlSection.toApi(): ConseilApi {
        return ConseilApi(
                id = id,
                title = title,
                content = content.toApi(),
                category = category.name,
                tags = tags,
                priority = priority,
                isActive = isActive,
                targetSpecies = targetSpecies,
                targetAgeGroups = targetAgeGroups,
                usageCount = usageCount,
                lastUsed = lastUsed?.toEpochMilliseconds(),
                isTemplate = isTemplate,
                createdAt = createdAt.toEpochMilliseconds(),
                updatedAt = updatedAt.toEpochMilliseconds()
        )
}

fun fr.vetbrain.vetnutri_mp.Export.RichTextContent.toApi(): RichTextContentApi {
        return RichTextContentApi(
                blocks = blocks.map { it.toApi() }
        )
}

fun fr.vetbrain.vetnutri_mp.Export.TextBlock.toApi(): TextBlockApi {
        return when (this) {
                is fr.vetbrain.vetnutri_mp.Export.TextBlock.Paragraph -> 
                        TextBlockApi.Paragraph(
                                id = id,
                                text = text,
                                formatting = formatting.toApi()
                        )
                is fr.vetbrain.vetnutri_mp.Export.TextBlock.Heading -> 
                        TextBlockApi.Heading(
                                id = id,
                                level = level,
                                text = text
                        )
                is fr.vetbrain.vetnutri_mp.Export.TextBlock.ListBlock -> 
                        TextBlockApi.ListBlock(
                                id = id,
                                items = items,
                                isOrdered = isOrdered
                        )
                is fr.vetbrain.vetnutri_mp.Export.TextBlock.TableBlock -> 
                        TextBlockApi.TableBlock(
                                id = id,
                                headers = headers,
                                rows = rows
                        )
                is fr.vetbrain.vetnutri_mp.Export.TextBlock.RawHtml ->
                        TextBlockApi.RawHtml(
                                id = id,
                                html = html
                        )
        }
}

fun fr.vetbrain.vetnutri_mp.Export.TextFormatting.toApi(): TextFormattingApi {
        return TextFormattingApi(
                isBold = isBold,
                isItalic = isItalic,
                isUnderline = isUnderline,
                isStrikethrough = isStrikethrough,
                fontSize = fontSize,
                color = color,
                alignment = alignment.name
        )
}

fun ConseilApi.toDomain(): fr.vetbrain.vetnutri_mp.Export.HtmlSection {
        return fr.vetbrain.vetnutri_mp.Export.HtmlSection(
                id = id,
                title = title,
                content = content.toDomain(),
                category = fr.vetbrain.vetnutri_mp.Export.SectionCategory.valueOf(category),
                tags = tags,
                priority = priority,
                isActive = isActive,
                targetSpecies = targetSpecies,
                targetAgeGroups = targetAgeGroups,
                usageCount = usageCount,
                lastUsed = lastUsed?.let { Instant.fromEpochMilliseconds(it) },
                isTemplate = isTemplate,
                createdAt = Instant.fromEpochMilliseconds(createdAt),
                updatedAt = Instant.fromEpochMilliseconds(updatedAt)
        )
}

fun RichTextContentApi.toDomain(): fr.vetbrain.vetnutri_mp.Export.RichTextContent {
        return fr.vetbrain.vetnutri_mp.Export.RichTextContent(
                blocks = blocks.map { it.toDomain() }
        )
}

fun TextBlockApi.toDomain(): fr.vetbrain.vetnutri_mp.Export.TextBlock {
        return when (this) {
                is TextBlockApi.Paragraph -> 
                        fr.vetbrain.vetnutri_mp.Export.TextBlock.Paragraph(
                                id = id,
                                text = text,
                                formatting = formatting.toDomain()
                        )
                is TextBlockApi.Heading -> 
                        fr.vetbrain.vetnutri_mp.Export.TextBlock.Heading(
                                id = id,
                                level = level,
                                text = text
                        )
                is TextBlockApi.ListBlock -> 
                        fr.vetbrain.vetnutri_mp.Export.TextBlock.ListBlock(
                                id = id,
                                items = items,
                                isOrdered = isOrdered
                        )
                is TextBlockApi.TableBlock -> 
                        fr.vetbrain.vetnutri_mp.Export.TextBlock.TableBlock(
                                id = id,
                                headers = headers,
                                rows = rows
                        )
                is TextBlockApi.RawHtml ->
                        fr.vetbrain.vetnutri_mp.Export.TextBlock.RawHtml(
                                id = id,
                                html = html
                        )
        }
}

fun TextFormattingApi.toDomain(): fr.vetbrain.vetnutri_mp.Export.TextFormatting {
        return fr.vetbrain.vetnutri_mp.Export.TextFormatting(
                isBold = isBold,
                isItalic = isItalic,
                isUnderline = isUnderline,
                isStrikethrough = isStrikethrough,
                fontSize = fontSize,
                color = color,
                alignment = fr.vetbrain.vetnutri_mp.Export.TextAlignment.valueOf(alignment)
        )
}
