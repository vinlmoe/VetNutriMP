package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.AlimIndic
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.FoodKind
import fr.vetbrain.vetnutri_mp.Enumer.GroupAlim
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver.AllNutrientResolver
import fr.vetbrain.vetnutri_mp.Enumer.TargetAdjust
import fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum
import fr.vetbrain.vetnutri_mp.Enumer.VariableKind
import kotlinx.datetime.LocalDate

/** Fonctions d'extension pour convertir entre les classes de données et les structures JSON */

// AlimentEv <-> AlimentEvJson
fun AlimentEv.toJson(): AlimentEvJson {
    return AlimentEvJson(
            UUID = this.uuid,
            nom = this.nom ?: "",
            group = this.group?.name ?: "",
            foodKind = this.typeAliment?.name ?: "",
            ingredients = this.ingredients ?: "",
            prix = this.price ?: 0.0,
            categoriePrix = this.categPrice ?: "i",
            marque = this.brand ?: "",
            indication = this.indicat.map { it.name },
            espece = 1, // Valeur par défaut, à adapter selon votre logique
            Especes = this.especes,
            gamme = this.gamme ?: "",
            presentation = "", // Non présent dans AlimentEv
            quantInt = this.quantInt ?: 0f,
            cont = this.cont?.toString() ?: "NO",
            deprecated = this.deprecated == 1,
            DataB = this.dataB ?: "6",
            valMap = this.valMap.mapKeys { (key, _) -> key.label } // Ajoute un préfixe à chaque clé
                .mapValues { (_, value) ->
                    value
                }
                .toMutableMap() // À adapter selon votre logique
    )
}

fun AlimentEvJson.toData(): AlimentEv {
    return AlimentEv(
        uuid = this.UUID,
        nom = this.nom,
        group =
        try {
            GroupAlim.valueOf(this.group)
        } catch (e: Exception) {
            null
        },
        typeAliment =
        try {
            FoodKind.valueOf(this.foodKind)
        } catch (e: Exception) {
            null
        },
        ingredients = this.ingredients,
        price = this.prix,
        categPrice = this.categoriePrix,
        brand = this.marque,
        gamme = this.gamme,
        consistent = true,
        cont = if (this.cont == "NO") 0 else 1,
        quantInt = this.quantInt,
        deprecated = if (this.deprecated) 1 else 0,
        dataB = this.DataB,
        especes = this.Especes.toMutableList(),
        indicat =
        this.indication
            .mapNotNull {
                try {
                    AlimIndic.valueOf(it)
                } catch (e: Exception) {
                    null
                }
            }
            .toMutableList(),
        valMap = this.valMap.mapKeys { (key, _) -> AllNutrientResolver(key) ?:NutrientMain.PROTEINE } // Ajoute un préfixe à chaque clé
            .mapValues { (_, value) ->
                value
            }

            .toMutableMap(),
        rationUUID =null
    )
}
fun AlimentEvJson.toData(ratUUID :String): AlimentEv {
    return AlimentEv(
        uuid = this.UUID,
        nom = this.nom,
        group =
        try {
            GroupAlim.valueOf(this.group)
        } catch (e: Exception) {
            null
        },
        typeAliment =
        try {
            FoodKind.valueOf(this.foodKind)
        } catch (e: Exception) {
            null
        },
        ingredients = this.ingredients,
        price = this.prix,
        categPrice = this.categoriePrix,
        brand = this.marque,
        gamme = this.gamme,
        consistent = true,
        cont = if (this.cont == "NO") 0 else 1,
        quantInt = this.quantInt,
        deprecated = if (this.deprecated) 1 else 0,
        dataB = this.DataB,
        especes = this.Especes.toMutableList(),
        indicat =
        this.indication
            .mapNotNull {
                try {
                    AlimIndic.valueOf(it)
                } catch (e: Exception) {
                    null
                }
            }
            .toMutableList(),
        valMap = this.valMap.mapKeys { (key, _) -> AllNutrientResolver(key) ?:NutrientMain.PROTEINE } // Ajoute un préfixe à chaque clé
            .mapValues { (_, value) ->
                value
            }
            .toMutableMap(),
        rationUUID =ratUUID
    )
}

// AnimalEv <-> AnimalEvJson
fun AnimalEv.toJson(): AnimalEvJson {
    return AnimalEvJson(
            UUID = this.uuid,
            nom = this.nom,
            dead = this.dead,
            id = this.id,
            sex = this.sexId,
            espece = this.specieId,
            nomProprio = this.ownerName,
            dateNaiss = this.birthdate ?: LocalDate(2023, 1, 1),
            race = this.race,
            resume = this.summary,
            listWeight = this.weightHistory.map { it.toJson() },
            list = ListConsultEvJson(consultations = this.consultations.map { it.toJson() })
    )
}

fun AnimalEvJson.toData(): AnimalEv {
    val consultationsList =
            when {
                // Format 1: consultations directement dans l'objet animal
                this.consultations != null -> this.consultations.map { it.toData() }

                // Format 2: consultations dans list.consultations
                this.list != null -> this.list.consultations.map { it.toData() }

                // Aucune consultation
                else -> emptyList()
            }

    return AnimalEv(
            uuid = this.UUID,
            nom = this.nom,
            dead = this.dead,
            id = this.id,
            sexId = this.sex,
            specieId = this.espece,
            ownerName = this.nomProprio,
            birthdate = this.dateNaiss,
            race = this.race,
            summary = this.resume,
            weightHistory = this.listWeight.map { it.toData() }.toMutableList(),
            consultations = consultationsList.toMutableList()
    )
}

// WeightDate <-> WeightDateJson
fun WeightDate.toJson(): WeightDateJson {
    return WeightDateJson(UUID = this.uuid, date = this.date, value = this.value)
}

fun WeightDateJson.toData(): WeightDate {
    return WeightDate(
            uuid = this.UUID,
            refAnimal = "", // Non présent dans WeightDateJson
            date = this.date,
            value = this.value
    )
}

// ConsultationEv <-> ConsultationEvJson
fun ConsultationEv.toJson(): ConsultationEvJson {
    return ConsultationEvJson(
            UUID = this.uuid,
            date = this.date ?: LocalDate(2023, 1, 1),
            pdate = this.date ?: LocalDate(2023, 1, 1), // Utilisation de date comme pdate
            objet = this.objectConsult,
            observation = this.observation,
            CRendu = this.cRendu,
            Poids = this.weight ?: 0f,
            PoidsIdeal = this.idealWeight ?: 0f,
            PoidsIdealex = this.idealWeight != null,
            Boisson = this.water ?: 0f,
            TauxMG = this.bodyFat ?: 20f,
            suivi = false, // Non présent dans ConsultationEv
            bcs = "", // Non présent dans ConsultationEv
            MCS = this.MCS ?: 3,
            k1value = this.k1Value ?: 1f,
            k2value = this.k2Value ?: 1f,
            k3value = this.k3Value ?: 1f,
            k4value = this.k4Value ?: 1f,
            k5value = this.k5Value ?: 1f,
            rationList = this.rations.associateBy({ it.uuid }, { it.toJson() }),
            diseaseRef = listOf(), // Non présent dans ConsultationEv
            svp = this.suppVarp.map { it.toJson() }
    )
}

fun ConsultationEvJson.toData(): ConsultationEv {
    return ConsultationEv(
            uuid = this.UUID,
            date = this.date,
            objectConsult = this.objet ?: "",
            observation = this.observation ?: "",
            cRendu = this.CRendu,
            weight = this.Poids,
            idealWeight = this.PoidsIdeal,
            water = this.Boisson,
            bodyFat = this.TauxMG,
            methodAnalysis = "", // Non présent dans ConsultationEvJson
            BCS = null, // À adapter selon votre logique
            MCS = this.MCS,
            k1Value = this.k1value,
            k2Value = this.k2value,
            k3Value = this.k3value,
            k4Value = this.k4value,
            k5Value = this.k5value,
            suppVarp = this.svp.map { it.toData() }.toMutableList(),
            rations = this.rationList.values.map { it.toData() }.toMutableList()
    )
}

// Ration <-> RationJson
fun Ration.toJson(): RationJson {
    return RationJson(
            UUID = this.uuid,
            Nom = this.name,
            alimentList = this.alimentMutableList.map { it.toJson() },
            actual = this.actual
    )
}

fun RationJson.toData(): Ration {
    return Ration(
            uuid = this.UUID,
            name = this.Nom,
            actual = this.actual,
            alimentMutableList = this.alimentList.map { it.toData() }.toMutableList()
    )
}

// AlimentRation <-> AlimentRationJson
fun AlimentRation.toJson(): AlimentRationJson {
    return AlimentRationJson(
            UUID = this.uuid,
            UUIDunif = this.uuidUnif,
            quantite = this.quantity,
            prop = this.proportion,
            alime = this.aliment?.toJson()
                            ?: AlimentEvJson(UUID = "", group = "", foodKind = "", espece = 0),
            weight = this.weight,
            categ = this.category,
            density = this.density
    )
}

fun AlimentRationJson.toData(): AlimentRation {
    return AlimentRation(
            uuid = this.UUID,
            uuidUnif = this.UUIDunif,
            quantity = this.quantite,
            proportion = this.prop,
            aliment = this.alime.toData(this.UUID),
            weight = this.weight,
            category = this.categ,
            density = this.density
    )
}

// SupplementalvariableP <-> SupplementalvariablePJson
fun SupplementalvariableP.toJson(): SupplementalvariablePJson {
    return SupplementalvariablePJson(variable = this.variable?.name ?: "", value = this.varue ?: 0f)
}

fun SupplementalvariablePJson.toData(): SupplementalvariableP {
    return SupplementalvariableP(
            variable =
                    try {
                        VariableKind.valueOf(this.variable)
                    } catch (e: Exception) {
                        null
                    },
            varue = this.value
    )
}

// BiblioRef <-> BiblioRefJson
fun BiblioRef.toJson(): BiblioRefJson {
    return BiblioRefJson(
            UUID = this.uuid,
            firstAuthor = this.firstAuthor ?: "",
            year = this.year?.toIntOrNull() ?: 1800,
            completeRef = this.completeRef ?: "",
            comment = this.comments ?: "",
            consistent = this.consistent ?: 0
    )
}

fun BiblioRefJson.toData(): BiblioRef {
    return BiblioRef(
            uuid = this.UUID,
            firstAuthor = this.firstAuthor,
            year = this.year.toString(),
            completeRef = this.completeRef,
            comments = this.comment,
            consistent = this.consistent
    )
}

// AdjustSaveEv <-> AdjustSaveEvJson
fun AdjustSaveEv.toJson(): AdjustSaveEvJson {
    return AdjustSaveEvJson(
            UUID = this.uuid,
            Name = this.name ?: "",
            description = this.description ?: "",
            esp = this.esp?.name ?: "CH",
            list = this.MutableList.map { it.toJson() }
    )
}

fun AdjustSaveEvJson.toData(): AdjustSaveEv {
    return AdjustSaveEv(
            uuid = this.UUID,
            name = this.Name,
            description = this.description,
            species = this.esp,
            esp =
                    try {
                        Espece.valueOf(this.esp)
                    } catch (e: Exception) {
                        null
                    },
            MutableList = this.list.map { it.toData() }.toMutableList()
    )
}

// TargetDefinitionEv <-> TargetDefinitionEvJson
fun TargetDefinitionEv.toJson(): TargetDefinitionEvJson {
    return TargetDefinitionEvJson(
            target = this.targ?.name ?: "",
            value = this.varue ?: 0f,
            unit = this.ure?.name ?: "",
            percentCompletion = this.percent ?: 0f,
            pas = this.measure ?: 0f
    )
}

fun TargetDefinitionEvJson.toData(): TargetDefinitionEv {
    return TargetDefinitionEv(
            targ =
                    try {
                        TargetAdjust.valueOf(this.target)
                    } catch (e: Exception) {
                        null
                    },
            varue = this.value,
            ure =
                    try {
                        UnitReqEnum.valueOf(this.unit)
                    } catch (e: Exception) {
                        null
                    },
            percent = this.percentCompletion,
            measure = this.pas,
            refMethod = null,
            ord = null,
            kind = null,
            unit = null
    )
}

// AlimDBList <-> AlimDBListJson
fun AlimDBList.toJson(): AlimDBListJson {
    return AlimDBListJson(
            dbList =
                    this.db.mapValues { (_, value) ->
                        AlimDBJson(
                                UUID = value.uuid,
                                sNom = value.sNom ?: "",
                                description = value.compNom ?: ""
                        )
                    }
    )
}

fun AlimDBListJson.toData(): AlimDBList {
    return AlimDBList(
            db =
                    this.dbList
                            .mapValues { (_, value) ->
                                AlimDB(
                                        uuid = value.UUID,
                                        sNom = value.sNom,
                                        compNom = value.description
                                )
                            }
                            .toMutableMap()
    )
}


