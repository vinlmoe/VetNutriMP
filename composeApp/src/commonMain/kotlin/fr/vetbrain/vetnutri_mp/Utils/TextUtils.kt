package fr.vetbrain.vetnutri_mp.Utils

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
     * Formate kg^0.75 en kg⁰·⁷⁵
     * @param value La valeur numérique
     * @param decimales Le nombre de décimales à afficher
     * @return Le texte formaté avec exposant Unicode
     */
    fun formatKgPuissance075(value: Double, decimales: Int = 2): String {
        return "${String.format("%.${decimales}f", value)} kg${toSuperscript("0.75")}"
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
        return "${String.format("%.${decimales}f", value)} $unite${toSuperscript(exposant)}"
    }
}
