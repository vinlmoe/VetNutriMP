package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import kotlinx.coroutines.launch

/**
 * Écran d'analyse graphique des aliments. Permet de rechercher un aliment et d'afficher ses détails
 * de manière structurée.
 */
@Composable
fun AnalyseGraphiqueAlimentsView(viewModel: AnimalDetailViewModel, modifier: Modifier = Modifier) {
    val isLoadingFoodsState: Boolean by viewModel.isLoadingFoods.collectAsState()
    val alimentsDisponibles: List<AlimentEv> = viewModel.getAliments()
    var requeteRecherche: String by remember { mutableStateOf(viewModel.alimentSearchQuery) }
    var alimentSelectionne: AlimentEv? by remember { mutableStateOf(null) }
    var alimentChargeComplet: AlimentEv? by remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope()
    val alimentsFiltres: List<AlimentEv> =
            remember(requeteRecherche, alimentsDisponibles) {
                viewModel.getFilteredFoods(requeteRecherche)
            }
    Column(modifier = modifier.fillMaxSize().padding(AppSizes.paddingMedium)) {
        Text(text = "Analyse graphique des aliments", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
        OutlinedTextField(
                value = requeteRecherche,
                onValueChange = { nouvelleValeur: String ->
                    requeteRecherche = nouvelleValeur
                    viewModel.setAlimentSearchQuery(nouvelleValeur)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Rechercher un aliment") },
                singleLine = true
        )
        Spacer(modifier = Modifier.height(AppSizes.paddingMedium))
        Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
        ) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                if (isLoadingFoodsState) {
                    CircularProgressIndicator()
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(alimentsFiltres) { aliment: AlimentEv ->
                            Card(
                                    modifier =
                                            Modifier.fillMaxWidth()
                                                    .padding(bottom = AppSizes.paddingSmall)
                                                    .clickable {
                                                        alimentSelectionne = aliment
                                                        scope.launch {
                                                            val alimentComplet: AlimentEv? =
                                                                    viewModel.getAlimentComplet(
                                                                            aliment.uuid
                                                                    )
                                                            alimentChargeComplet =
                                                                    alimentComplet ?: aliment
                                                        }
                                                    }
                            ) {
                                Column(modifier = Modifier.padding(AppSizes.paddingSmall)) {
                                    Text(
                                            text = aliment.nom ?: "Aliment sans nom",
                                            style = MaterialTheme.typography.subtitle1
                                    )
                                    Text(
                                            text = aliment.brand ?: "",
                                            style = MaterialTheme.typography.body2
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                if (alimentSelectionne == null) {
                    Text(text = "Sélectionnez un aliment pour afficher les détails")
                } else {
                    val details: AlimentEv? = alimentChargeComplet
                    Column(modifier = Modifier.fillMaxSize()) {
                        val nomAffiche: String = details?.nom ?: "Aliment"
                        val marqueAffiche: String = details?.brand ?: "-"
                        val groupeAffiche: String = details?.group?.label ?: "-"
                        val typeAffiche: String = details?.typeAliment?.label ?: "-"

                        Text(text = nomAffiche, style = MaterialTheme.typography.h6)
                        Divider(modifier = Modifier.padding(vertical = AppSizes.paddingSmall))
                        Text(text = "Marque: $marqueAffiche")
                        Text(text = "Groupe: $groupeAffiche")
                        Text(text = "Type: $typeAffiche")
                        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                        Text(
                                text = "Aperçu nutriments (simplifié)",
                                style = MaterialTheme.typography.subtitle2
                        )
                        // Ici, on pourrait tracer des barres proportionnelles si les valeurs
                        // existent
                        // Pour un premier écran, on affiche seulement un message si les données
                        // sont absentes
                        val aDesValeurs: Boolean = details?.valMap?.isNotEmpty() == true
                        if (!aDesValeurs) {
                            Text(text = "Valeurs nutritionnelles non chargées")
                        } else {
                            val nombreValeurs: Int = details?.valMap?.size ?: 0
                            Text(text = "Nombre de nutriments disponibles: $nombreValeurs")
                        }
                    }
                }
            }
        }
    }
}
