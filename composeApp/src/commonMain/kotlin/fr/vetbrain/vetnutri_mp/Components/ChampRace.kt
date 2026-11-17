package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import fr.vetbrain.vetnutri_mp.Data.CatalogueRaces
import fr.vetbrain.vetnutri_mp.Data.SuggestionRace
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Localization.LocalizationManager

/**
 * Champ race avec suggestions : races du catalogue de l'espèce ([CatalogueRaces], hors ligne)
 * dans la langue de l'interface, puis [racesConnues] (races déjà saisies). La recherche trouve une
 * race par n'importe lequel de ses noms (« German shepherd » propose « Berger allemand » en
 * français) ; la saisie reste libre.
 */
@Composable
fun ChampRace(
        value: String,
        onValueChange: (String) -> Unit,
        espece: Espece?,
        label: String,
        modifier: Modifier = Modifier,
        racesConnues: List<String> = emptyList(),
        maxSuggestions: Int = 8
) {
    val langue = LocalizationManager.currentLocale
    val suggestions =
            remember(espece, langue, racesConnues) {
                CatalogueRaces.suggestions(espece, langue, racesConnues)
            }
    AutocompleteTextField(
            value = value,
            onValueChange = onValueChange,
            suggestions = suggestions,
            // Le clic sur une suggestion transmet déjà son libellé via onValueChange
            onSuggestionSelected = { _: SuggestionRace -> },
            suggestionToString = { it.libelle },
            label = label,
            modifier = modifier,
            maxSuggestions = maxSuggestions,
            filterSuggestions = { requete, elements, _ -> CatalogueRaces.filtrer(requete, elements, maxSuggestions) }
    )
}
