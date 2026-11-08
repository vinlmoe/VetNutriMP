package fr.vetbrain.vetnutri_mp.Localization

import fr.vetbrain.vetnutri_mp.Data.CatalogueRaces
import fr.vetbrain.vetnutri_mp.Enumer.Espece

/**
 * Race telle qu'elle s'affiche dans la langue de l'interface : traduite si elle figure au catalogue
 * ([CatalogueRaces.traduire]), sinon telle qu'elle a été saisie. Lue dans une composition, elle suit
 * un changement de langue.
 */
fun traduireRace(race: String, espece: Espece?): String =
        CatalogueRaces.traduire(race, espece, LocalizationManager.currentLocale)
