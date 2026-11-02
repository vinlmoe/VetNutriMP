package fr.vetbrain.vetnutri_mp.Utils

import kotlinx.datetime.LocalDate

/** Date affichée à l'utilisateur, partout la même : 25/10/2026 (la forme ISO reste pour le stockage). */
fun LocalDate.enTexte(): String =
        "${dayOfMonth.toString().padStart(2, '0')}/${monthNumber.toString().padStart(2, '0')}/$year"

/**
 * Date saisie par l'utilisateur : 25/10/2026 (aussi 25-10-2026, 25.10.2026, 5/3/2026) ou la forme
 * ISO 2026-10-25 ; null si le texte n'est pas une date valide.
 */
fun lireDate(texte: String): LocalDate? {
        val t = texte.trim()
        Regex("""(\d{4})-(\d{1,2})-(\d{1,2})""").matchEntire(t)?.let { m ->
                val (a, mo, j) = m.destructured
                return dateOuNull(a.toInt(), mo.toInt(), j.toInt())
        }
        Regex("""(\d{1,2})[/.\-](\d{1,2})[/.\-](\d{4})""").matchEntire(t)?.let { m ->
                val (j, mo, a) = m.destructured
                return dateOuNull(a.toInt(), mo.toInt(), j.toInt())
        }
        return null
}

private fun dateOuNull(annee: Int, mois: Int, jour: Int): LocalDate? =
        try {
                LocalDate(annee, mois, jour)
        } catch (e: IllegalArgumentException) {
                null
        }
