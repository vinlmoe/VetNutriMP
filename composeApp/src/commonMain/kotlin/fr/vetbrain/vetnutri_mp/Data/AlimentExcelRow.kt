package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Utils.genUUID

/**
 * Représentation d'un aliment pour l'import/export Excel
 * Structure optimisée pour une feuille Excel avec toutes les colonnes nécessaires
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

    // Nutriments - colonnes dynamiques pour chaque nutriment
    // Le format sera : Map<nutrientLabel, Pair<valeur, unite>>
    val nutriments: Map<String, Pair<Double?, String?>> = emptyMap()
) {

    companion object {
        /**
         * Liste de tous les nutriments possibles pour créer les colonnes Excel
         */
        val ALL_NUTRIENTS = listOf(
            // Nutriments principaux (NutrientMain)
            "HUMIDITE", "PROTEINE", "LIPIDE", "GLUCIDE", "ENA", "FIBRE", "CELLULOSE",
            "CENDRE", "ENERGIE", "SUCRE", "AMIDON", "FIBRESOL", "FIBRETOT", "NDF", "ADF",

            // Vitamines (NutrientVitam)
            "VITA", "VITC", "VITD", "VITE", "VITK", "VITB1", "VITB2", "VITB3",
            "VITB5", "VITB6", "VITB8", "VITB9", "VITB12", "CHOLINE", "RETINOL", "BETACAR",

            // Minéraux (NutrientMin)
            "FE", "CU", "ZN", "MN", "I", "SE",

            // Macroéléments (NutrientMacro)
            "CAL", "PHOS", "MG", "NA", "K", "CHL",

            // Lipides (NutrientLipid)
            "AGSATURE", "AGMONO", "AGPOLY", "AG40", "AG60", "AG80", "AG100", "AG120",
            "AG140", "AG160", "AG180", "AG181", "AG182", "AG183", "AG204", "AG205",
            "AG226", "CHOLES", "O3", "O6", "EPADHA",

            // Autres (NutrientOther)
            "TAURINE", "CARNITINE", "FOS", "MOS", "SACC", "FRUCT", "LACTO", "MALT",
            "AcOx", "GAL", "GLUCOSE", "DEXTROSE"
        )

        /**
         * Convertit un AlimentEv en AlimentExcelRow
         */
        fun fromAlimentEv(alimentEv: AlimentEv): AlimentExcelRow {
            val nutrimentsMap = mutableMapOf<String, Pair<Double?, String?>>()

            // Ajouter tous les nutriments
            alimentEv.valMap.forEach { (nutrient, quantity) ->
                nutrimentsMap[nutrient.label] = Pair(quantity.value, quantity.unit)
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
                especes = alimentEv.especes.joinToString(", "),
                indications = alimentEv.indicat.joinToString(", ") { it.label },
                rationUUID = alimentEv.rationUUID,
                nutriments = nutrimentsMap
            )
        }

        /**
         * Convertit un AlimentExcelRow en AlimentEv
         */
        fun toAlimentEv(row: AlimentExcelRow): AlimentEv {
            return AlimentEv(
                uuid = row.uuid,
                nom = row.nom,
                brand = row.brand,
                gamme = row.gamme,
                ingredients = row.ingredients,
                group = row.groupAlim?.let { GroupAlim.byName(it) },
                typeAliment = row.typeAliment?.let { FoodKind.values().find { fk -> fk.label == it } },
                cont = row.contEnum?.let { ContEnum.getByName(it) },
                price = row.price,
                categPrice = row.categPrice,
                quantInt = row.quantInt,
                consistent = row.consistent,
                deprecated = row.deprecated,
                dataB = row.dataB,
                especes = row.especes?.split(",")?.map { it.trim() }?.toMutableList() ?: mutableListOf(),
                indicat = row.indications?.split(",")?.mapNotNull {
                    AlimIndic.getFromString(it.trim())
                }?.toMutableList() ?: mutableListOf(),
                rationUUID = row.rationUUID
            ).apply {
                // Ajouter les nutriments
                row.nutriments.forEach { (nutrientLabel, pair) ->
                    val (valeur, unite) = pair
                    if (valeur != null) {
                        // Trouver le nutriment correspondant
                        getNutrientFromLabel(nutrientLabel)?.let { nutrient ->
                            setNutrient(nutrient, valeur)
                            // Note: L'unité pourrait être utilisée pour validation si nécessaire
                        }
                    }
                }
            }
        }

        /**
         * Trouve un nutriment par son label dans tous les enums
         */
        private fun getNutrientFromLabel(label: String): Nutrient? {
            // Chercher dans NutrientMain
            NutrientMain.getByLabel(label)?.let { return it }

            // Chercher dans NutrientVitam
            NutrientVitam.getByLabel(label)?.let { return it }

            // Chercher dans NutrientMin
            NutrientMin.getByLabel(label)?.let { return it }

            // Chercher dans NutrientMacro
            NutrientMacro.getByLabel(label)?.let { return it }

            // Chercher dans NutrientLipid
            NutrientLipid.getByLabel(label)?.let { return it }

            // Chercher dans NutrientOther
            NutrientOther.getByLabel(label)?.let { return it }

            return null
        }
    }
}
