package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.vetbrain.vetnutri_mp.Components.SectionTitle
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel

@Composable
fun WeightHistoryView(viewModel: AnimalDetailViewModel, modifier: Modifier = Modifier) {
    Column(
            modifier = modifier.fillMaxSize().padding(AppSizes.paddingMedium),
            verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
    ) {
        SectionTitle(text = "Historique du poids")
        Text("Fonctionnalité à venir")
    }
}
