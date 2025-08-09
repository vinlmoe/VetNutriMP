package fr.vetbrain.vetnutri_mp.Localization

import fr.vetbrain.vetnutri_mp.Data.Labelable

/**
 * Utilitaires de localisation pour les enums implémentant [Labelable]. Les clés utilisées suivent
 * l'ordre de priorité suivant: 1) "enum.{NomClasse}.{labelNormalisé}" 2)
 * "enum.{NomClasse}.{NOM_ENUM}" lorsque l'instance est un Enum 3) label en minuscules (pour
 * compatibilité avec d'anciennes clés simples) 4) retour du label original si aucune clé n'est
 * trouvée
 */
fun Labelable.translateEnum(): String {
    val className: String = this::class.simpleName ?: "Enum"
    val keyFromLabel: String = buildEnumKey(className, this.label)
    val translatedFromLabel: String = LocalizationManager.translate(keyFromLabel)
    if (translatedFromLabel != keyFromLabel) return translatedFromLabel

    // Heuristiques spécifiques pour Nutriments vers des clés existantes
    val nutrientHeuristicKey: String? = computeNutrientHeuristicKey(className, this.label)
    if (nutrientHeuristicKey != null) {
        val translatedHeuristic: String = LocalizationManager.translate(nutrientHeuristicKey)
        if (translatedHeuristic != nutrientHeuristicKey) return translatedHeuristic
    }

    val enumConstName: String? = (this as? Enum<*>)?.name
    if (enumConstName != null) {
        val keyFromName: String = "enum.$className.$enumConstName"
        val translatedFromName: String = LocalizationManager.translate(keyFromName)
        if (translatedFromName != keyFromName) return translatedFromName
    }

    val legacyKey: String = this.label.lowercase()
    val legacyTranslated: String = LocalizationManager.translate(legacyKey)
    if (legacyTranslated != legacyKey) return legacyTranslated

    return this.label
}

internal fun buildEnumKey(className: String, label: String): String {
    val normalizedClass: String = className.replace(Regex("[^A-Za-z0-9]"), "")
    val normalizedLabel: String = label.replace(Regex("[^A-Za-z0-9]"), "")
    return "enum.$normalizedClass.$normalizedLabel"
}

/**
 * Génère une clé heuristique pour les nutriments afin de réutiliser des traductions existantes. Ne
 * dépend que du nom de classe et du label pour éviter les dépendances cycliques.
 */
private fun computeNutrientHeuristicKey(className: String, rawLabel: String): String? {
    val label: String = rawLabel.uppercase()
    return when (className) {
        // Nutriments principaux
        "NutrientMain" ->
                when (label) {
                    "HUMIDITE" -> "nutrition.moisture"
                    "PROTEINE" -> "nutrition.proteins"
                    "LIPIDE" -> "nutrition.lipids"
                    "GLUCIDE" -> "nutrition.carbohydrates"
                    "CENDRE" -> "nutrition.ash"
                    "ENERGIE" -> "nutrition.energy"
                    else -> null
                }
        // Minéraux macros
        "NutrientMacro" ->
                when (label) {
                    "CAL" -> "minerals.calcium"
                    "PHOS" -> "minerals.phosphorus"
                    "MG" -> "minerals.magnesium"
                    "NA" -> "minerals.sodium"
                    "K" -> "minerals.potassium"
                    "CHL" -> "minerals.chlorine"
                    else -> null
                }
        // Minéraux oligo
        "NutrientMin" ->
                when (label) {
                    "FE" -> "minerals.iron"
                    "CU" -> "minerals.copper"
                    "ZN" -> "minerals.zinc"
                    "MN" -> "minerals.manganese"
                    "I" -> "minerals.iodine"
                    "SE" -> "minerals.selenium"
                    else -> null
                }
        // Vitamines (VITA, VITB1...)
        "NutrientVitam" ->
                when {
                    label == "VITA" -> "vitamins.a"
                    label == "VITC" -> "vitamins.c"
                    label == "VITD" -> "vitamins.d"
                    label == "VITE" -> "vitamins.e"
                    label == "VITK" -> "vitamins.k"
                    label.startsWith("VITB") -> {
                        val bPart = label.removePrefix("VITB").lowercase()
                        "vitamins.b$bPart"
                    }
                    else -> null
                }
        // Lipides: AGxxx/AGMONO/AGPOLY/AGSATURE déjà présents en minuscules
        "NutrientLipid" -> label.lowercase()
        // Acides aminés: labels en majuscules mappables à des clés en minuscules existantes
        "AAEnum" ->
                when (label) {
                    "ASPARATE" -> "aspartate" // correctif orthographique
                    else -> label.lowercase()
                }
        // Autres
        "NutrientOther" ->
                when (label) {
                    "SACC" -> "saccharose"
                    else -> label.lowercase()
                }
        else -> null
    }
}
