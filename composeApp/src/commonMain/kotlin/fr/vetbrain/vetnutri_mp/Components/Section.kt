package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.vetbrain.vetnutri_mp.Theme.AppSizes

/**
 * Composant Section réutilisable pour regrouper des éléments de l'interface
 *
 * @param title Titre de la section
 * @param modifier Modifier optionnel pour la section
 * @param content Contenu de la section
 */
@Composable
fun Section(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(
            modifier = modifier.fillMaxWidth().padding(vertical = AppSizes.paddingSmall),
            verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
    ) {
        // Titre de la section
        Text(text = title, style = MaterialTheme.typography.h6)

        Divider()

        // Contenu de la section
        Column(
                modifier = Modifier.fillMaxWidth().padding(top = AppSizes.paddingSmall),
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
        ) { content() }
    }
}
