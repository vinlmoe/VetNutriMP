package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.View.components.FoodSearchComponent
import fr.vetbrain.vetnutri_mp.View.components.FoodSearchConfig
import fr.vetbrain.vetnutri_mp.View.components.FoodSearchFilters
import fr.vetbrain.vetnutri_mp.View.components.FoodSearchLayout

/**
 * Vue étendue d'analyse de la sélection d'aliments avec recherche et filtrage.
 * Permet de rechercher et filtrer les aliments avant l'analyse.
 */
@Composable
fun AnalyseSelectionAlimentsView(
    aliments: List<AlimentEv>, 
    onClose: () -> Unit,
    onAlimentSelected: ((AlimentEv) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // État pour les filtres de recherche
    var filters by remember { mutableStateOf(FoodSearchFilters()) }
    
    // Configuration pour FoodSearchComponent (layout compact)
    val searchConfig = remember {
        FoodSearchConfig(
            layout = FoodSearchLayout.COMPACT,
            showFilters = true,
            showSearchBar = true,
            showResultsCount = true,
            onFoodSelected = onAlimentSelected,
            availableActions = listOf("Analyser", "Comparer"),
            onFoodAction = { aliment, action ->
                when (action) {
                    "Analyser" -> onAlimentSelected?.invoke(aliment)
                    "Comparer" -> onAlimentSelected?.invoke(aliment)
                }
            }
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(), 
        elevation = AppSizes.elevationSmall
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(AppSizes.paddingMedium),
            verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
        ) {
            // En-tête avec titre et bouton de fermeture
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Analyse de ${aliments.size} aliment(s)",
                        style = MaterialTheme.typography.h6
                    )
                    Text(
                        text = "Recherchez et filtrez les aliments pour l'analyse",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
                Button(onClick = onClose) { 
                    Text("Fermer") 
                }
            }

            Spacer(modifier = Modifier.height(AppSizes.paddingSmall))

            // Composant de recherche et filtrage partagé
            FoodSearchComponent(
                foods = aliments,
                filters = filters,
                onFiltersChange = { filters = it },
                config = searchConfig,
                modifier = Modifier.fillMaxWidth().weight(1f)
            )

            // Section d'analyse (espace réservé pour les graphiques)
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = AppSizes.elevationSmall
            ) {
                Column(
                    modifier = Modifier.padding(AppSizes.paddingMedium)
                ) {
                    Text(
                        text = "Analyse des nutriments",
                        style = MaterialTheme.typography.subtitle1
                    )
                    Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                    Text(
                        text = "Espace réservé pour les graphiques et comparaisons de nutriments.",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

