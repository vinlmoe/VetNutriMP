package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Utils.genUUID
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

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
        val nutriments: Map<String, Double?> = emptyMap(),

        // Énergie par espèce - encodée dans une seule colonne au format "CHIEN:340;CHAT:330"
        val energieParEspece: Map<String, Double> = emptyMap(),

        // Bibliographie liée à l'aliment, encodée en JSON dans une seule cellule
        // (auto-suffisant : évite un format à délimiteurs qui casserait sur des champs
        // texte libre comme firstAuthor/comments/bibtex)
        val biblioRefsJson: String? = null
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
                    nutriments = nutrimentsMap,
                    energieParEspece = alimentEv.energieParEspece,
                    biblioRefsJson = encodeBiblioRefs(alimentEv.biblioRefs)
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
                            rationUUID = row.rationUUID,
                            energieParEspece = row.energieParEspece,
                            biblioRefs = decodeBiblioRefs(row.biblioRefsJson)
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

        /**
         * Encode une map d'énergie par espèce dans une seule cellule CSV, au format
         * "CHIEN:340;CHAT:330" (clé = nom de l'enum Espece, séparateur ";" entre espèces,
         * ":" entre l'espèce et sa valeur).
         */
        fun encodeEnergieParEspece(energieParEspece: Map<String, Double>): String {
            return energieParEspece.entries.joinToString(";") { (espece, valeur) -> "$espece:$valeur" }
        }

        /**
         * Décode une cellule CSV au format "CHIEN:340;CHAT:330" vers une map d'énergie par espèce.
         * Les entrées mal formées (sans ":" ou avec une valeur non numérique) sont ignorées.
         */
        fun decodeEnergieParEspece(value: String?): Map<String, Double> {
            if (value.isNullOrBlank()) return emptyMap()
            return value.split(";")
                    .mapNotNull { entree ->
                        val parts = entree.split(":")
                        if (parts.size != 2) return@mapNotNull null
                        val espece = parts[0].trim()
                        val valeur = parts[1].trim().replace(",", ".").toDoubleOrNull()
                        if (espece.isEmpty() || valeur == null) null else espece to valeur
                    }
                    .toMap()
        }

        /** Encode une liste de BiblioRef en JSON pour une seule cellule CSV, ou null si vide. */
        fun encodeBiblioRefs(biblioRefs: List<BiblioRef>): String? {
            if (biblioRefs.isEmpty()) return null
            return Json.encodeToString(ListSerializer(BiblioRef.serializer()), biblioRefs)
        }

        /**
         * Décode une cellule CSV JSON vers une liste de BiblioRef.
         * Accepte également l'ancien format lisible :
         * "Titre de la référence (2026); Autre référence (2024)".
         */
        fun decodeBiblioRefs(value: String?): List<BiblioRef> {
            if (value.isNullOrBlank()) return emptyList()
            return try {
                Json.decodeFromString(ListSerializer(BiblioRef.serializer()), value)
            } catch (_: Exception) {
                value.split(';')
                    .mapNotNull(::decodeLegacyBiblioRef)
                    .distinctBy { it.uuid }
            }
        }

        private fun decodeLegacyBiblioRef(rawValue: String): BiblioRef? {
            val completeRef = rawValue.trim().takeIf { it.isNotEmpty() } ?: return null
            // L'ancien format est explicitement « titre (AAAA) ». Exiger l'année évite
            // d'accepter comme bibliographie n'importe quel JSON corrompu ou texte arbitraire.
            val match = Regex("^(.*?)\\s*\\((\\d{4})\\)$").matchEntire(completeRef)
                ?: return null
            val title = match.groupValues[1].trim().takeIf { it.isNotEmpty() } ?: return null
            val year = match.groupValues[2].toIntOrNull() ?: return null
            val normalizedIdentity = "$title|$year"
                .lowercase()
                .replace(Regex("\\s+"), " ")

            return BiblioRef(
                uuid = "csv-biblio-${stableHashHex(normalizedIdentity)}",
                firstAuthor = title,
                year = year,
                completeRef = completeRef,
                consistent = 1
            )
        }

        /** Hash FNV-1a déterministe et multiplateforme utilisé pour dédupliquer les imports texte. */
        private fun stableHashHex(value: String): String {
            var hash = 0x811c9dc5u
            value.encodeToByteArray().forEach { byte ->
                hash = (hash xor byte.toUByte().toUInt()) * 0x01000193u
            }
            return hash.toString(16).padStart(8, '0')
        }
    }
}
