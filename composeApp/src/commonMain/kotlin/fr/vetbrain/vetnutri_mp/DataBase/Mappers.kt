package fr.vetbrain.vetnutri_mp.DataBase

import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Enumer.AlimIndic
import fr.vetbrain.vetnutri_mp.Enumer.FoodKind
import fr.vetbrain.vetnutri_mp.Enumer.GroupAlim
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver
import kotlinx.datetime.LocalDate

object Mappers {
        // Animal Mappers avec relations
        fun AnimalEv.toEntity(includeRelations: Boolean = true): AnimalEntity {
                val entity =
                        AnimalEntity(
                                uuid = this.uuid,
                                nom = this.nom,
                                dead = this.dead,
                                id = this.id ?: "",
                                sexId = this.sexId,
                                specieId = this.specieId,
                                ownerName = this.ownerName,
                                birthdate = this.birthdate?.toString() ?: "",
                                race = this.race,
                                summary = this.summary
                        )

                if (includeRelations) {
                        // Convertir les consultations
                        this.consultations.forEach { consultation ->
                                consultation.idAnim = this.uuid
                        }
                        // Convertir l'historique des poids
                        this.weightHistory.forEach { weight -> weight.refAnimal = this.uuid }
                }

                return entity
        }

        fun AnimalEntity.toData(
                consultations: List<ConsultationEntity> = emptyList(),
                weights: List<WeightEntity> = emptyList()
        ): AnimalEv {
                return AnimalEv(
                        uuid = this.uuid,
                        nom = this.nom ?: "",
                        dead = this.dead ?: false,
                        id = this.id?.takeIf { it.isNotEmpty() },
                        sexId = this.sexId ?: 0,
                        specieId = this.specieId ?: "",
                        ownerName = this.ownerName ?: "",
                        birthdate =
                                this.birthdate?.takeIf { it.isNotEmpty() }?.let {
                                        LocalDate.parse(it)
                                },
                        race = this.race ?: "",
                        summary = this.summary ?: "",
                        consultations = consultations.map { it.toData() }.toMutableList(),
                        weightHistory = weights.map { it.toData() }.toMutableList()
                )
        }

        // Consultation Mappers avec relations
        fun ConsultationEv.toEntity(includeRelations: Boolean = true): ConsultationEntity {
                val entity =
                        ConsultationEntity(
                                uuid = this.uuid,
                                idAnim = this.idAnim,
                                date = this.date?.toString() ?: "",
                                objectConsult = this.objectConsult,
                                observation = this.observation,
                                cRendu = this.cRendu,
                                weight = this.weight,
                                idealWeight = this.idealWeight,
                                water = this.water,
                                bodyFat = this.bodyFat,
                                methodAnalysis = this.methodAnalysis,
                                BCS = this.BCS,
                                k1Id = this.k1Id,
                                k1Value = this.k1Value,
                                k2Id = this.k2Id,
                                k2Value = this.k2Value,
                                k3Id = this.k3Id,
                                k3Value = this.k3Value,
                                k4Id = this.k4Id,
                                k4Value = this.k4Value,
                                k5Id = this.k5Id,
                                k5Value = this.k5Value,
                                nLittle = this.nLittle,
                                pAdult = this.pAdult,
                                coefGes = this.coefGes,
                                coefLact = this.coefLact,
                                MCS = this.MCS
                        )

                if (includeRelations) {
                        // Mettre à jour les références des rations
                        this.rations.forEach { ration -> ration.idConsult = this.uuid }
                        // Mettre à jour les références des variables supplémentaires
                        this.suppVarp.forEach { suppVar ->
                                suppVar.variable?.let { variable ->
                                        SupplementalVariableEntity(
                                                idConsult = this.uuid,
                                                variableKind = variable.uuid,
                                                value = suppVar.varue
                                        )
                                }
                        }
                }

                return entity
        }

        fun ConsultationEntity.toData(
                rations: List<RationEntity> = emptyList(),
                suppVars: List<SupplementalVariableEntity> = emptyList()
        ): ConsultationEv {
                return ConsultationEv(
                        uuid = this.uuid,
                        idAnim = this.idAnim ?: "",
                        date = this.date?.takeIf { it.isNotEmpty() }?.let { LocalDate.parse(it) },
                        objectConsult = this.objectConsult ?: "",
                        observation = this.observation ?: "",
                        cRendu = this.cRendu ?: "",
                        weight = this.weight,
                        idealWeight = this.idealWeight,
                        water = this.water,
                        bodyFat = this.bodyFat,
                        methodAnalysis = this.methodAnalysis ?: "",
                        BCS = this.BCS,
                        k1Id = this.k1Id,
                        k1Value = this.k1Value,
                        k2Id = this.k2Id,
                        k2Value = this.k2Value,
                        k3Id = this.k3Id,
                        k3Value = this.k3Value,
                        k4Id = this.k4Id,
                        k4Value = this.k4Value,
                        k5Id = this.k5Id,
                        k5Value = this.k5Value,
                        nLittle = this.nLittle,
                        pAdult = this.pAdult,
                        coefGes = this.coefGes,
                        coefLact = this.coefLact,
                        MCS = this.MCS,
                        suppVarp =
                                suppVars
                                        .map { entity ->
                                                SupplementalvariableP(
                                                        variable =
                                                                VariableKind.getById(
                                                                        entity.variableKind
                                                                ),
                                                        varue = entity.value
                                                )
                                        }
                                        .toMutableList(),
                        rations = rations.map { it.toData() }.toMutableList()
                )
        }

        // Ration Mappers avec relations
        fun Ration.toEntity(includeRelations: Boolean = true): RationEntity {
                val entity =
                        RationEntity(
                                uuid = this.uuid,
                                idConsult = this.idConsult,
                                name = this.name,
                                coef = this.coef,
                                actual = this.actual,
                                number = this.number,
                                espece = this.espece,
                                recette = this.recette,
                                description = this.description
                        )

                if (includeRelations) {
                        // Mettre à jour les références des aliments
                        this.alimentMutableList.forEach { aliment -> aliment.refRation = this.uuid }
                }

                return entity
        }

        fun RationEntity.toData(aliments: List<AlimentRationEntity> = emptyList()): Ration {
                return Ration(
                        uuid = this.uuid,
                        idConsult = this.idConsult ?: "",
                        name = this.name ?: "",
                        coef = this.coef ?: 1.0f,
                        actual = this.actual ?: false,
                        number = this.number ?: 1,
                        espece = this.espece,
                        recette = this.recette ?: false,
                        description = this.description ?: "",
                        alimentMutableList = aliments.map { it.toData() }.toMutableList()
                )
        }

        // AlimentRation Mappers
        fun AlimentRation.toEntity(): AlimentRationEntity {
                return AlimentRationEntity(
                        uuid = this.uuid,
                        refAlimUnif = this.refAlimUnif,
                        refRation = this.refRation,
                        quantity = this.quantity,
                        refTarget = this.refTarget
                )
        }

        fun AlimentRationEntity.toData(): AlimentRation {
                return AlimentRation(
                        uuid = this.uuid,
                        uuidUnif = this.refAlimUnif ?: "",
                        quantity = this.quantity ?: 0f,
                        proportion = 0f,
                        aliment = null,
                        refAlimUnif = this.refAlimUnif,
                        refRation = this.refRation,
                        refTarget = this.refTarget
                )
        }

        // AlimentEv Mappers avec relations
        fun AlimentEv.toAlimentEntity(): AlimentEntity {
                return AlimentEntity(
                        uuid = this.uuid,
                        name = this.nom ?: "",
                        typeAliment = this.typeAliment?.ordinal ?: 0,
                        groupAliment = this.group?.ordinal ?: 0,
                        consistent = if (this.consistent) 1 else 0,
                        deprecated = this.deprecated ?: 0,
                        price = this.price ?: 0.0,
                        categoriePrix = this.categPrice ?: "",
                        ingredients = this.ingredients ?: "",
                        marque = this.brand ?: "",
                        gamme = this.gamme ?: "",
                        quantite = this.quantInt ?: 0f,
                        dataB = this.dataB ?: "",
                        rationUUID = this.rationUUID
                )
        }

        // Fonction pour convertir les entités en AlimentEv
        fun AlimentEntity.toAlimentEv(
                especes: List<EspeceAlimentEntity> = emptyList(),
                indications: List<IndicationAlimentEntity> = emptyList(),
                nutrientValues: List<NutrientValueEntity> = emptyList()
        ): AlimentEv {
                return AlimentEv(
                        uuid = this.uuid,
                        nom = this.name,
                        typeAliment =
                                try {
                                        this.typeAliment.let { FoodKind.entries.getOrNull(it) }
                                } catch (e: Exception) {
                                        null
                                },
                        group =
                                try {
                                        this.groupAliment.let { GroupAlim.entries.getOrNull(it) }
                                } catch (e: Exception) {
                                        null
                                },
                        consistent = this.consistent == 1,
                        deprecated = this.deprecated,
                        price = this.price,
                        categPrice = this.categoriePrix,
                        ingredients = this.ingredients,
                        brand = this.marque,
                        gamme = this.gamme,
                        quantInt = this.quantite,
                        dataB = this.dataB,
                        especes = especes.map { it.espece }.toMutableList(),
                        indicat =
                                indications
                                        .mapNotNull { entity ->
                                                try {
                                                        AlimIndic.entries.getOrNull(
                                                                entity.indication
                                                        )
                                                } catch (e: Exception) {
                                                        null
                                                }
                                        }
                                        .toMutableList(),
                        valMap = nutrientValues.toNutrientValueMap(),
                        rationUUID = this.rationUUID
                )
        }

        // Weight Mappers
        fun WeightDate.toEntity(): WeightEntity {
                return WeightEntity(
                        uuid = this.uuid,
                        refAnimal = this.refAnimal,
                        date = this.date.toString(),
                        value = this.value
                )
        }

        fun WeightEntity.toData(): WeightDate {
                return WeightDate(
                        uuid = this.uuid,
                        refAnimal = this.refAnimal,
                        date = LocalDate.parse(this.date),
                        value = this.value
                )
        }

        /**
         * Convertit une map de nutriments et valeurs en liste d'entités de valeurs de nutriments.
         */
        fun Map<Nutrient, Float>.toNutrientValueEntities(
                alimentUuid: String
        ): List<NutrientValueEntity> {
                return map { (nutrient, value) ->
                        NutrientValueEntity(
                                refAliment = alimentUuid,
                                nutrientLabel = nutrient.label,
                                value = value
                        )
                }
        }

        /**
         * Convertit une liste d'entités de valeurs de nutriments en map de nutriments et valeurs.
         */
        fun List<NutrientValueEntity>.toNutrientValueMap(): Map<Nutrient, Float> {
                val result = mutableMapOf<Nutrient, Float>()
                forEach { entity ->
                        val nutrient = NutrientResolver.AllNutrientResolver(entity.nutrientLabel)
                        if (nutrient != null) {
                                result[nutrient] = entity.value
                        }
                }
                return result
        }
}
