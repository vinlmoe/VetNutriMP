package fr.vetbrain.vetnutri_mp.Utils

/** Garde uniquement une saisie décimale valide (chiffres + 1 séparateur . ou ,). */
fun normalizeDecimalInput(input: String): String {
    if (input.isEmpty()) return ""

    val output = StringBuilder()
    var hasDecimalSeparator = false

    input.forEach { char ->
        when {
            char.isDigit() -> output.append(char)
            (char == ',' || char == '.') && !hasDecimalSeparator -> {
                if (output.isEmpty()) output.append('0')
                output.append(',')
                hasDecimalSeparator = true
            }
        }
    }

    return output.toString()
}

/** Parse une valeur décimale positive en acceptant ',' ou '.'. */
fun parsePositiveDecimal(input: String): Double? {
    return input.replace(',', '.').toDoubleOrNull()?.takeIf { it > 0.0 }
}
