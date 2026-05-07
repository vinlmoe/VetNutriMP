package fr.vetbrain.vetnutri_mp.Utils

import kotlin.math.pow

/** Utilitaires pour le formatage de texte */
object TextUtils {

    private val superscriptMap =
            mapOf(
                    '0' to '⁰',
                    '1' to '¹',
                    '2' to '²',
                    '3' to '³',
                    '4' to '⁴',
                    '5' to '⁵',
                    '6' to '⁶',
                    '7' to '⁷',
                    '8' to '⁸',
                    '9' to '⁹',
                    '.' to '·',
                    ',' to '·',
                    '-' to '⁻',
                    '+' to '⁺'
            )

    /**
     * Convertit une chaîne de caractères en exposant Unicode
     * @param text Le texte à convertir en exposant
     * @return Le texte formaté avec les caractères d'exposant Unicode
     */
    fun toSuperscript(text: String): String {
        return text.map { char -> superscriptMap[char] ?: char }.joinToString("")
    }

    /**
     * Formate un nombre décimal avec un nombre de décimales fixe, de manière multiplateforme.
     * N'ajoute aucune séparation de milliers et utilise un point comme séparateur décimal.
     *
     * @param value Valeur à formater
     * @param decimales Nombre de décimales à afficher (>= 0)
     * @return Chaîne formatée avec exactement [decimales] décimales
     */
    fun formatDecimal(value: Double, decimales: Int = 2): String {
        if (decimales <= 0) {
            val entier: Long = kotlin.math.round(value).toLong()
            return entier.toString()
        }
        if (value.isNaN() || value.isInfinite()) return value.toString()
        val d: Int = if (decimales < 0) 0 else decimales
        val facteur: Long = d10(d)
        val echelle: Double = value * facteur.toDouble()
        val arrondiEchelle: Long = kotlin.math.round(echelle).toLong()
        val arrondiAbsolu: Long = kotlin.math.abs(arrondiEchelle)
        val partieEntiere: Long = arrondiAbsolu / facteur
        val partieDecimale: Long = arrondiAbsolu % facteur
        val signe: String = if (arrondiEchelle < 0) "-" else ""
        val decimaleStr: String = partieDecimale.toString().padStart(d, '0')
        return "$signe$partieEntiere.$decimaleStr"
    }

    // Pré-calcul des puissances de 10 pour éviter les boucles
    private val powerOf10 = (0..10).associateWith { calculatePowerOf10(it) }

    private fun d10(exp: Int): Long {
        return powerOf10[exp] ?: calculatePowerOf10(exp)
    }

    private fun calculatePowerOf10(exp: Int): Long {
        // Calcul simple des puissances de 10
        var res: Long = 1
        repeat(exp) { res *= 10 }
        return res
    }

    /**
     * Formate kg^0.75 en kg⁰·⁷⁵
     * @param value La valeur numérique
     * @param decimales Le nombre de décimales à afficher
     * @return Le texte formaté avec exposant Unicode
     */
    fun formatKgPuissance075(value: Double, decimales: Int = 2): String {
        return "${formatDecimal(value, decimales)} kg${toSuperscript("0.75")}"
    }

    /**
     * Extrait la puissance de l'équation du poids métabolique (BW)
     * @param equationScript Le script de l'équation (ex: "BW ^ 0.75" ou "BW^0.67")
     * @return La puissance extraite sous forme de String, ou "0.75" par défaut
     */
    fun extrairePuissanceEquationBW(equationScript: String?): String {
        if (equationScript.isNullOrBlank()) {
            return "0.75"
        }
        // Patterns possibles: "BW ^ 0.75", "BW^0.75", "BW ^0.75", "BW^ 0.75", etc.
        val pattern = Regex("""BW\s*\^\s*([0-9]+\.?[0-9]*)""", RegexOption.IGNORE_CASE)
        val match = pattern.find(equationScript)
        return match?.groupValues?.get(1) ?: "0.75"
    }

    /**
     * Formate kg avec une puissance dynamique extraite de l'équation BW
     * @param value La valeur numérique
     * @param equationScript Le script de l'équation BW (ex: "BW ^ 0.75")
     * @param decimales Le nombre de décimales à afficher
     * @return Le texte formaté avec exposant Unicode
     */
    fun formatKgAvecPuissanceDynamique(
            value: Double,
            equationScript: String?,
            decimales: Int = 2
    ): String {
        val puissance = extrairePuissanceEquationBW(equationScript)
        return "${formatDecimal(value, decimales)} kg${toSuperscript(puissance)}"
    }

    /**
     * Formate une unité avec exposant
     * @param value La valeur numérique
     * @param unite L'unité de base
     * @param exposant L'exposant à appliquer
     * @param decimales Le nombre de décimales
     * @return Le texte formaté avec exposant Unicode
     */
    fun formatAvecExposant(
            value: Double,
            unite: String,
            exposant: String,
            decimales: Int = 2
    ): String {
        return "${formatDecimal(value, decimales)} $unite${toSuperscript(exposant)}"
    }
}
