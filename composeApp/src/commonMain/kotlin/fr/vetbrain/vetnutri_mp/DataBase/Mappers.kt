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
        fun AlimentEv.toFoodEntity(): FoodEntity {
                return FoodEntity(
                        uuid = this.uuid,
                        groupAlim = this.group?.ordinal ?: 0,
                        typeAlim = this.typeAliment?.ordinal ?: 0,
                        ingredients = this.ingredients ?: "",
                        price = this.price ?: 0.0,
                        categPrice = this.categPrice ?: "",
                        brand = this.brand ?: "",
                        gamme = this.gamme ?: "",
                        cont = this.cont?.name ?: "NO",
                        unitPres = 0, // À adapter selon vos besoins
                        quantityPres = this.quantInt ?: 0f,
                        version = 1, // À adapter selon vos besoins
                        date = "", // À adapter selon vos besoins
                        nameDef = this.nom ?: "",
                        consistent = if (this.consistent) 1 else 0,
                        deprecated = if (this.deprecated) 1 else 0,
                        DataB = this.dataB ?: "",
                        RefRation = this.rationUUID,
                        name = this.nom,
                        quantite = this.quantInt,
                        especesJson =
                                if (this.especes.isNotEmpty()) this.especes.joinToString(",")
                                else null,
                        indicationsJson =
                                if (this.indicat.isNotEmpty())
                                        this.indicat.joinToString(",") { it.name }
                                else null
                )
        }

        // Fonction pour convertir les entités en AlimentEv
        fun FoodEntity.toAlimentEv(
                especes: List<EspeceAlimentEntity> = emptyList(),
                indications: List<IndicationAlimentEntity> = emptyList(),
                nutrientValues: List<NutrientValueEntity> = emptyList()
        ): AlimentEv {
                // Récupérer les espèces à partir du JSON si disponible
                val especesList = mutableListOf<String>()
                if (!this.especesJson.isNullOrEmpty()) {
                        especesList.addAll(this.especesJson.split(","))
                } else {
                        especesList.addAll(especes.map { it.espece })
                }

                // Récupérer les indications à partir du JSON si disponible
                val indicatList = mutableListOf<AlimIndic>()
                if (!this.indicationsJson.isNullOrEmpty()) {
                        this.indicationsJson.split(",").forEach { indic ->
                                try {
                                        AlimIndic.valueOf(indic)?.let { indicatList.add(it) }
                                } catch (e: Exception) {
                                        // Ignorer les indications invalides
                                }
                        }
                } else {
                        indicatList.addAll(
                                indications.mapNotNull { entity ->
                                        try {
                                                AlimIndic.entries.getOrNull(entity.indication)
                                        } catch (e: Exception) {
                                                null
                                        }
                                }
                        )
                }

                return AlimentEv(
                        uuid = this.uuid,
                        nom = this.nameDef,
                        typeAliment =
                                try {
                                        this.typeAlim.let { FoodKind.entries.getOrNull(it) }
                                } catch (e: Exception) {
                                        null
                                },
                        group =
                                try {
                                        this.groupAlim.let { GroupAlim.entries.getOrNull(it) }
                                } catch (e: Exception) {
                                        null
                                },
                        consistent = this.consistent == 1,
                        deprecated = this.deprecated == 1,
                        price = this.price,
                        categPrice = this.categPrice,
                        ingredients = this.ingredients,
                        brand = this.brand,
                        gamme = this.gamme,
                        quantInt = this.quantityPres,
                        dataB = this.DataB,
                        especes = especesList.toMutableList(),
                        indicat = indicatList.toMutableList(),
                        valMap = nutrientValues.toNutrientValueMap(),
                        cont = fr.vetbrain.vetnutri_mp.Enumer.ContEnum.getByName(this.cont),
                        rationUUID = this.RefRation
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
        fun Map<Nutrient, fr.vetbrain.vetnutri_mp.Data.NutrientQuantity>.toNutrientValueEntities(
                alimentUuid: String
        ): List<NutrientValueEntity> {
                return map { (nutrient, nutrientQuantity) ->
                        NutrientValueEntity(
                                refAliment = alimentUuid,
                                nutrientLabel = nutrient.label,
                                value = nutrientQuantity.value
                        )
                }
        }

        /**
         * Convertit une liste d'entités de valeurs de nutriments en map de nutriments et valeurs.
         */
        fun List<NutrientValueEntity>.toNutrientValueMap():
                MutableMap<Nutrient, fr.vetbrain.vetnutri_mp.Data.NutrientQuantity> {
                val result = mutableMapOf<Nutrient, fr.vetbrain.vetnutri_mp.Data.NutrientQuantity>()
                forEach { entity ->
                        val nutrient = NutrientResolver.AllNutrientResolver(entity.nutrientLabel)
                        if (nutrient != null) {
                                result[nutrient] =
                                        fr.vetbrain.vetnutri_mp.Data.NutrientQuantity(
                                                entity.value,
                                                entity.nutrientLabel
                                        )
                        }
                }
                return result
        }
}
