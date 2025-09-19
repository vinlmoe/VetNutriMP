package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.AlimIndic
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.FoodKindResolver
import fr.vetbrain.vetnutri_mp.Enumer.GroupAlim
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
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
            espece = 0, // À adapter selon votre logique
            Especes = this.especes,
            gamme = this.gamme ?: "",
            presentation = "", // Non présent dans AlimentEv
            quantInt = this.quantInt ?: 0.0,
            cont = if (this.cont != null) this.cont.name else "NO",
            deprecated = this.deprecated,
            DataB = this.dataB ?: "6",
            valMap =
                    this.valMap
                            .map { (nutrient, nutrientQuantity) ->
                                nutrient.label to
                                        NutrientQuantity(nutrientQuantity.value, nutrient.label)
                            }
                            .toMap()
    )
}

// Fonction utilitaire pour convertir une chaîne en AlimIndic
private fun stringToAlimIndic(indicStr: String): AlimIndic? {
    return try {
        // Essayer d'abord par le nom de l'énumération (PED, NEUT, etc.)
        AlimIndic.valueOf(indicStr)
    } catch (e: Exception) {
        try {
            // Si ça échoue, essayer par le label (Pédiatrique, Stérilisé, etc.)
            AlimIndic.byName(indicStr)
        } catch (e: Exception) {
            // Si ça échoue encore, retourner null
            null
        }
    }
}

fun AlimentEvJson.toData(): AlimentEv {
    // Convertir les espèces: transformer les ID numériques en labels
    val especesConverties = convertirEspecesEnLabels(this.Especes, this.espece)

    // Vérifier si le JSON contient des valeurs de nutriments

    // Améliorer la conversion des nutriments
    val nutrientMap = mutableMapOf<Nutrient, NutrientQuantity>()
    if (this.valMap.isNotEmpty()) {
        this.valMap.forEach { (key, nutrientQuantity) ->
            // Récupérer le nom du nutriment et sa valeur
            val nutrientKey = nutrientQuantity.nut
            val value = nutrientQuantity.value

            // Essayer d'abord la résolution directe
            var nutrient = AllNutrientResolver(nutrientKey)
            
            // Si la résolution échoue, essayer de nettoyer la clé
            if (nutrient == null) {
                val cleanedKey = nutrientKey.trim().replace("_", " ")
                nutrient = AllNutrientResolver(cleanedKey)
            }
            
            // Si la résolution réussit, ajouter le nutriment
            if (nutrient != null) {
                nutrientMap[nutrient] = NutrientQuantity(value, nutrient.label)
            } else {
                // Log pour débogage - nutriment non résolu
                println("⚠️ [NUTRIENT] Nutriment non résolu: '$nutrientKey' (valeur: $value)")
            }
        }
    }

    // Créer l'objet AlimentEv
    val alimentEv =
            AlimentEv(
                    uuid = this.UUID,
                    nom = this.nom,
                    group =
                            try {
                                GroupAlim.valueOf(this.group)
                            } catch (e: Exception) {
                                null
                            },
                    typeAliment = FoodKindResolver.resoudreFoodKindBrut(this.foodKind),
                    ingredients = this.ingredients,
                    price = this.prix,
                    categPrice = this.categoriePrix,
                    brand = this.marque,
                    gamme = this.gamme,
                    cont =
                            fr.vetbrain.vetnutri_mp.Enumer.ContEnum.getByName(
                                    if (this.cont == "YES") "CAN" else "NO"
                            ),
                    quantInt = this.quantInt,
                    deprecated = this.deprecated,
                    dataB = this.DataB,
                    especes = especesConverties.toMutableList(),
                    indicat = this.indication.mapNotNull { stringToAlimIndic(it) }.toMutableList(),
                    // Assurez-vous que valMap est mutable
                    valMap = nutrientMap.toMutableMap(),
                    rationUUID = null
            )

    // Vérifier l'objet après conversion

    return alimentEv
}

fun AlimentEvJson.toData(ratUUID: String): AlimentEv {
    // Convertir les espèces: transformer les ID numériques en labels
    val especesConverties = convertirEspecesEnLabels(this.Especes, this.espece)

    // Améliorer la conversion des nutriments
    val nutrientMap = mutableMapOf<Nutrient, NutrientQuantity>()
    if (this.valMap.isNotEmpty()) {
        this.valMap.forEach { (key, nutrientQuantity) ->
            // Récupérer le nom du nutriment et sa valeur
            val nutrientKey = nutrientQuantity.nut
            val value = nutrientQuantity.value

            // Essayer d'abord la résolution directe
            var nutrient = AllNutrientResolver(nutrientKey)
            
            // Si la résolution échoue, essayer de nettoyer la clé
            if (nutrient == null) {
                val cleanedKey = nutrientKey.trim().replace("_", " ")
                nutrient = AllNutrientResolver(cleanedKey)
            }
            
            // Si la résolution réussit, ajouter le nutriment
            if (nutrient != null) {
                nutrientMap[nutrient] = NutrientQuantity(value, nutrient.label)
            } else {
                // Log pour débogage - nutriment non résolu
                println("⚠️ [NUTRIENT] Nutriment non résolu: '$nutrientKey' (valeur: $value)")
            }
        }
    }

    return AlimentEv(
            uuid = this.UUID,
            nom = this.nom,
            group =
                    try {
                        GroupAlim.valueOf(this.group)
                    } catch (e: Exception) {
                        null
                    },
            typeAliment = FoodKindResolver.resoudreFoodKindBrut(this.foodKind),
            ingredients = this.ingredients,
            price = this.prix,
            categPrice = this.categoriePrix,
            brand = this.marque,
            gamme = this.gamme,
            cont =
                    fr.vetbrain.vetnutri_mp.Enumer.ContEnum.getByName(
                            if (this.cont == "YES") "CAN" else "NO"
                    ),
            quantInt = this.quantInt,
            deprecated = this.deprecated,
            dataB = this.DataB,
            especes = especesConverties.toMutableList(),
            indicat = this.indication.mapNotNull { stringToAlimIndic(it) }.toMutableList(),
            valMap = nutrientMap.toMutableMap(),
            rationUUID = ratUUID
    )
}

/**
 * Convertit une liste d'identifiants d'espèces en leurs labels correspondants. Si la liste est vide
 * mais qu'un id d'espèce est fourni, il sera également converti.
 *
 * @param especes Liste des espèces (qui peuvent être sous forme d'ID ou de labels)
 * @param especeId ID numérique d'une espèce (utilisé si la liste est vide)
 * @return Liste des labels d'espèces convertis
 */
private fun convertirEspecesEnLabels(especes: List<String>, especeId: Int): List<String> {
    // Si la liste d'espèces n'est pas vide, la traiter
    if (especes.isNotEmpty()) {
        return especes.map { especeStr ->
            // Vérifier si l'espèce est un identifiant numérique
            val especeInt = especeStr.toIntOrNull()
            if (especeInt != null) {
                // C'est un ID numérique, essayer de le convertir en label
                try {
                    val espece = Espece.getEnumFromInt(especeInt)
                    espece?.label
                            ?: especeStr // Utiliser le label si trouvé, sinon garder la chaîne
                    // d'origine
                } catch (e: Exception) {
                    especeStr // En cas d'erreur, garder la chaîne d'origine
                }
            } else {
                // Ce n'est pas un ID numérique, donc c'est probablement déjà un label
                especeStr
            }
        }
    }
    // Si la liste est vide mais qu'un ID d'espèce est fourni
    else if (especeId > 0) {
        try {
            val espece = Espece.getEnumFromInt(especeId)
            return listOf(espece?.label ?: especeId.toString())
        } catch (e: Exception) {
            return listOf(especeId.toString())
        }
    }

    // Si aucune espèce n'est spécifiée
    return emptyList()
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
            try {
                when {
                    // Format 1: consultations directement dans l'objet animal
                    this.consultations != null -> this.consultations.map { it.toData() }

                    // Format 2: consultations dans list.consultations
                    this.list != null -> this.list.consultations.map { it.toData() }

                    // Aucune consultation
                    else -> emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }

    // Récupération de l'espèce avec gestion des erreurs
    val especeStr = this.espece.toString()
    val specieId =
            try {
                val especeId = especeStr.toIntOrNull()
                if (especeId != null) {
                    try {
                        // Si c'est un ID numérique, récupérer le label
                        val especeEnum = Espece.getEnumFromInt(especeId)
                        especeEnum.label
                    } catch (e: Exception) {
                        // Fallback au label CHIEN
                        Espece.CHIEN.label
                    }
                } else {
                    // Si c'est déjà un label, vérifier qu'il est valide
                    try {
                        val especeEnum = Espece.valueOf(especeStr)
                        especeEnum.label
                    } catch (e: Exception) {
                        // Essayer de trouver par label
                        val especeByLabel = Espece.getByLabel(especeStr)
                        if (especeByLabel != null) {
                            especeByLabel.label
                        } else {
                            Espece.CHIEN.label
                        }
                    }
                }
            } catch (e: Exception) {
                // Valeur par défaut en cas d'erreur
                Espece.CHIEN.label
            }

    return AnimalEv(
            uuid = this.UUID,
            nom = this.nom,
            dead = this.dead,
            id = this.id ?: "",
            sexId = this.sex,
            specieId = specieId,
            ownerName = this.nomProprio,
            birthdate = this.dateNaiss,
            race = this.race,
            summary = this.resume,
            weightHistory =
                    try {
                        this.listWeight.map { it.toData() }.toMutableList()
                    } catch (e: Exception) {
                        mutableListOf()
                    },
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
            Poids = this.weight ?: 0.0,
            PoidsIdeal = this.idealWeight ?: 0.0,
            PoidsIdealex = this.idealWeight != null,
            Boisson = this.water ?: 0.0,
            TauxMG = this.bodyFat ?: 20.0,
            suivi = false, // Non présent dans ConsultationEv
            bcs = "", // Non présent dans ConsultationEv
            MCS = this.MCS ?: 3,
            k1value = this.k1Value ?: 1.0,
            k2value = this.k2Value ?: 1.0,
            k3value = this.k3Value ?: 1.0,
            k4value = this.k4Value ?: 1.0,
            k5value = this.k5Value ?: 1.0,
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
            quantite = this.quantite,
            prop = this.proportion,
            alime = this.aliment?.toJson()
                            ?: AlimentEvJson(UUID = "", group = "", foodKind = "", espece = 0),
            weight = this.weight,
            categ = this.category,
            density = this.densiteEnergetique
    )
}

fun AlimentRationJson.toData(): AlimentRation {
    return AlimentRation(
            uuid = this.UUID,
            uuidUnif = this.UUIDunif,
            quantite = this.quantite,
            proportion = this.prop,
            aliment = this.alime.toData(this.UUID),
            weight = this.weight,
            category = this.categ,
            densiteEnergetique = this.density,
            refAlimUnif = this.UUIDunif,
            refRation = null,
            refTarget = null
    )
}

// SupplementalvariableP <-> SupplementalvariablePJson
fun SupplementalvariableP.toJson(): SupplementalvariablePJson {
    return SupplementalvariablePJson(
            variable = this.variable?.name ?: "",
            value = this.varue ?: 0.0
    )
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
            firstAuthor = this.firstAuthor,
            year = this.year,
            completeRef = this.completeRef,
            comment = this.comments,
            consistent = this.consistent
    )
}

fun BiblioRefJson.toData(): BiblioRef {
    return BiblioRef(
            uuid = this.UUID,
            firstAuthor = this.firstAuthor,
            year = this.year,
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
            value = this.varue ?: 0.0,
            unit = this.ure?.name ?: "",
            percentCompletion = this.percent ?: 0.0,
            pas = this.measure ?: 0.0
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
