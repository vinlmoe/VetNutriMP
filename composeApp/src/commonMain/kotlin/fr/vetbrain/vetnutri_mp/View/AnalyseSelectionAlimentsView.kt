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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Theme.AppSizes

/**
 * Vue basique d'analyse de la sélection d'aliments. Affiche un résumé du nombre d'aliments et un
 * espace pour des graphiques à venir.
 */
@Composable
fun AnalyseSelectionAlimentsView(aliments: List<AlimentEv>, onClose: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = AppSizes.elevationSmall) {
        Column(modifier = Modifier.fillMaxSize().padding(AppSizes.paddingMedium)) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                        text = "Analyse de ${aliments.size} aliment(s)",
                        style = MaterialTheme.typography.h6
                )
                Button(onClick = onClose) { Text("Fermer") }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Espace réservé pour les graphiques et comparaisons de nutriments.")
        }
    }
}

