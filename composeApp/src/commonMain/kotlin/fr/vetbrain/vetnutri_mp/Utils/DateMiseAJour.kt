package fr.vetbrain.vetnutri_mp.Utils

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

private val dateFrancaise = Regex("""(\d{1,2})/(\d{1,2})/(\d{4})""")

/**
 * Date de mise à jour d'un aliment (`lastUpdateDate`) en instant comparable, ou null si elle est
 * vide ou illisible. Formats acceptés : instant ISO (`2026-10-27T10:00:00Z`), date ISO éventuellement
 * suivie d'une heure (`2026-10-27`, `2026-10-27 10:00`) et date française `jj/mm/aaaa`, présente
 * dans les données initiales et les fichiers Excel.
 */
fun parseDateMiseAJourOrNull(rawDate: String?): Instant? {
    val date = rawDate?.trim().orEmpty()
    if (date.isEmpty()) return null
    runCatching { return Instant.parse(date) }
    runCatching { return Instant.parse("${LocalDate.parse(date.take(10))}T00:00:00Z") }
    val francaise = dateFrancaise.matchEntire(date) ?: return null
    val (jour, mois, annee) = francaise.destructured
    return runCatching {
        Instant.parse("${LocalDate(annee.toInt(), mois.toInt(), jour.toInt())}T00:00:00Z")
    }.getOrNull()
}
