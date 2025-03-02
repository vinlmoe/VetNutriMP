package fr.vetbrain.vetnutri_mp.DataBase

import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.Enumer.*
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
        fun AlimentEv.toEntity(includeRelations: Boolean = true): AlimentEntity {
                val entity =
                        AlimentEntity(
                                uuid = this.uuid,
                                group = this.group?.id,
                                typeAliment = this.typeAliment?.coef,
                                ingredients = this.ingredients,
                                price = this.price,
                                categPrice = this.categPrice,
                                brand = this.brand,
                                gamme = this.gamme,
                                nom = this.nom,
                                consistent = this.consistent,
                                cont = this.cont,
                                quantInt = this.quantInt,
                                deprecated = this.deprecated,
                                dataB = this.dataB
                        )

                if (includeRelations) {
                        // Créer les entités EspeceAliment
                        this.especes.forEach { espece ->
                                EspeceAlimentEntity(refAliment = this.uuid, espece = espece)
                        }
                        // Créer les entités IndicationAliment
                        this.indicat.forEach { indication ->
                                IndicationAlimentEntity(
                                        refAliment = this.uuid,
                                        indication = indication.coef
                                )
                        }
                }

                return entity
        }

        fun AlimentEntity.toData(
                especes: List<EspeceAlimentEntity> = emptyList(),
                indications: List<IndicationAlimentEntity> = emptyList()
        ): AlimentEv {
                return AlimentEv(
                        uuid = this.uuid,
                        group = this.group?.let { GroupAlim.byId(it) },
                        typeAliment = this.typeAliment?.let { FoodKind.byCoef(it) },
                        ingredients = this.ingredients ?: "",
                        price = this.price,
                        categPrice = this.categPrice ?: "",
                        brand = this.brand ?: "",
                        gamme = this.gamme ?: "",
                        nom = this.nom ?: "",
                        consistent = this.consistent,
                        cont = this.cont,
                        quantInt = this.quantInt,
                        deprecated = this.deprecated ?: 0,
                        dataB = this.dataB ?: "",
                        especes = especes.map { it.espece }.toMutableList(),
                        indicat =
                                indications
                                        .mapNotNull { AlimIndic.byCoef(it.indication) }
                                        .toMutableList()
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
}
