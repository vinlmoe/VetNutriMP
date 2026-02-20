package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Utils.genUUID

/**
 * Représentation d'un aliment pour l'import/export Excel Structure optimisée pour une feuille Excel
 * avec toutes les colonnes nécessaires
 */
data class AlimentExcelRow(
        // Informations de base
        val uuid: String = genUUID(),
        val nom: String? = null,
        val brand: String? = null,
        val gamme: String? = null,
        val ingredients: String? = null,

        // Classification
        val groupAlim: String? = null, // Label du GroupAlim
        val typeAliment: String? = null, // Label du FoodKind
        val contEnum: String? = null, // Label du ContEnum

        // Prix et quantité
        val price: Double? = null,
        val categPrice: String? = null,
        val quantInt: Double? = null,

        // Statuts
        val consistent: Boolean = false,
        val deprecated: Boolean = false,
        val dataB: String? = null,

        // Espèces (séparées par des virgules)
        val especes: String? = null,

        // Indications (séparées par des virgules)
        val indications: String? = null,

        // Ration associée
        val rationUUID: String? = null,

        // Date de dernière mise à jour (format libre, idéalement ISO 8601)
        val lastUpdateDate: String? = null,

        // Nutriments - colonnes dynamiques pour chaque nutriment
        // Le format sera : Map<nutrientLabel, valeur>
        val nutriments: Map<String, Double?> = emptyMap()
) {

    companion object {
        /** Liste de tous les nutriments possibles pour créer les colonnes Excel */
        val ALL_NUTRIENTS =
                listOf(
                        // Nutriments principaux (NutrientMain)
                        "HUMIDITE",
                        "PROTEINE",
                        "LIPIDE",
                        "GLUCIDE",
                        "ENA",
                        "CELLULOSE",
                        "CENDRE",
                        "ENERGIE",
                        "SUCRE",
                        "AMIDON",
                        "FIBRESOL",
                        "FIBRETOT",
                        "NDF",
                        "ADF",

                        // Vitamines (NutrientVitam)
                        "VITA",
                        "VITC",
                        "VITD",
                        "VITE",
                        "VITK",
                        "VITB1",
                        "VITB2",
                        "VITB3",
                        "VITB5",
                        "VITB6",
                        "VITB8",
                        "VITB9",
                        "VITB12",
                        "CHOLINE",
                        "RETINOL",
                        "BETACAR",

                        // Minéraux (NutrientMin)
                        "FE",
                        "CU",
                        "ZN",
                        "MN",
                        "I",
                        "SE",

                        // Macroéléments (NutrientMacro)
                        "CAL",
                        "PHOS",
                        "MG",
                        "NA",
                        "K",
                        "CHL",

                        // Lipides (NutrientLipid)
                        "AGSATURE",
                        "AGMONO",
                        "AGPOLY",
                        "AG40",
                        "AG60",
                        "AG80",
                        "AG100",
                        "AG120",
                        "AG140",
                        "AG160",
                        "AG180",
                        "AG181",
                        "AG182",
                        "AG183",
                        "AG204",
                        "AG205",
                        "AG226",
                        "CHOLES",
                        "O3",
                        "O6",
                        "EPADHA",

                        // Acides aminés (AAEnum)
                        "ALANINE",
                        "ARGININE",
                        "ASPARAGINE",
                        "ASPARATE",
                        "CYSTEINE",
                        "GLUTAMATE",
                        "GLUTAMINE",
                        "GLYCINE",
                        "HISTIDINE",
                        "ISOLEUCINE",
                        "LEUCINE",
                        "LYSINE",
                        "METHIONINE",
                        "PHENYLALANINE",
                        "PROLINE",
                        "PYRROLYSINE",
                        "SELENOCYSTEINE",
                        "SERINE",
                        "THREONINE",
                        "TRYPTOPHANE",
                        "TYROSINE",
                        "VALINE",

                        // Autres (NutrientOther)
                        "TAURINE",
                        "CARNITINE",
                        "FOS",
                        "MOS",
                        "SACC",
                        "FRUCT",
                        "LACTO",
                        "MALT",
                        "AcOx",
                        "GAL",
                        "GLUCOSE",
                        "DEXTROSE"
                )

        /** Convertit un AlimentEv en AlimentExcelRow */
        fun fromAlimentEv(alimentEv: AlimentEv): AlimentExcelRow {
            val nutrimentsMap = mutableMapOf<String, Double?>()

            // Ajouter tous les nutriments
            alimentEv.valMap.forEach { (nutrient, quantity) ->
                nutrimentsMap[nutrient.label] = quantity.value
            }

            return AlimentExcelRow(
                    uuid = alimentEv.uuid,
                    nom = alimentEv.nom,
                    brand = alimentEv.brand,
                    gamme = alimentEv.gamme,
                    ingredients = alimentEv.ingredients,
                    groupAlim = alimentEv.group?.label,
                    typeAliment = alimentEv.typeAliment?.label,
                    contEnum = alimentEv.cont?.label,
                    price = alimentEv.price,
                    categPrice = alimentEv.categPrice,
                    quantInt = alimentEv.quantInt,
                    consistent = alimentEv.consistent,
                    deprecated = alimentEv.deprecated,
                    dataB = alimentEv.dataB,
                    lastUpdateDate = alimentEv.lastUpdateDate,
                    especes = alimentEv.especes.joinToString(", "),
                    indications = alimentEv.indicat.joinToString(", ") { it.nameToString() },
                    rationUUID = alimentEv.rationUUID,
                    nutriments = nutrimentsMap
            )
        }

        /** Convertit un AlimentExcelRow en AlimentEv avec logs détaillés */
        fun toAlimentEv(row: AlimentExcelRow): AlimentEv {
            
            // Conversion des enums avec logs
            val group = row.groupAlim?.let { 
                val result = GroupAlim.byName(it)
                result
            }
            
            val typeAliment = row.typeAliment?.let {
                val result = FoodKind.values().find { fk -> fk.label == it }
                result
            }
            
            val cont = row.contEnum?.let {
                val result = ContEnum.getByLabel(it)
                result
            }
            
            val especes = row.especes?.split(",")?.map { it.trim() }?.toMutableList() ?: mutableListOf()
            
            val indicat = row.indications?.split(",")?.mapNotNull { 
                val trimmed = it.trim()
                AlimIndic.getFromString(trimmed)
            }?.toMutableList() ?: mutableListOf()
            
            return AlimentEv(
                            uuid = row.uuid,
                            nom = row.nom,
                            brand = row.brand,
                            gamme = row.gamme,
                            ingredients = row.ingredients,
                            group = group,
                            typeAliment = typeAliment,
                            cont = cont,
                            price = row.price,
                            categPrice = row.categPrice,
                            quantInt = row.quantInt,
                            consistent = row.consistent,
                            deprecated = row.deprecated,
                            dataB = row.dataB,
                            lastUpdateDate = row.lastUpdateDate,
                            especes = especes,
                            indicat = indicat,
                            rationUUID = row.rationUUID
                    )
        .apply {
            // Ajouter les nutriments avec logs
            var nutrimentSuccessCount = 0
            var nutrimentErrorCount = 0
            
            row.nutriments.forEach { (nutrientLabel, valeur) ->
                if (valeur != null) {
                    
                    // Trouver le nutriment correspondant
                    val nutrient = getNutrientFromLabel(nutrientLabel)
                    if (nutrient != null) {
                        setNutrient(nutrient, valeur)
                        nutrimentSuccessCount++
                    } else {
                        nutrimentErrorCount++
                    }
                }
            }
            
        }
        }

        /** Trouve un nutriment par son label dans tous les enums */
        private fun getNutrientFromLabel(label: String): Nutrient? {
            // Utiliser le NutrientResolver qui gère tous les cas spéciaux et la normalisation
            return NutrientResolver.AllNutrientResolver(label)
        }
    }
}
