package fr.vetbrain.vetnutri_mp.Utils

import kotlinx.datetime.LocalDate

/** Date affichée à l'utilisateur, partout la même : 25/10/2026 (la forme ISO reste pour le stockage). */
fun LocalDate.enTexte(): String =
        "${dayOfMonth.toString().padStart(2, '0')}/${monthNumber.toString().padStart(2, '0')}/$year"
