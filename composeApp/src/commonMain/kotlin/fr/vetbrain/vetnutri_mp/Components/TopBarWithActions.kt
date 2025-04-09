package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import fr.vetbrain.vetnutri_mp.Theme.AppSizes

/**
 * Barre supérieure avec titre, bouton de retour et espace pour des actions supplémentaires à
 * droite.
 *
 * @param title Titre à afficher
 * @param onNavigateBack Fonction pour naviguer en arrière
 * @param actions Contenu pour les actions supplémentaires à afficher à droite
 * @param modifier Modifier à appliquer à la barre supérieure
 */
@Composable
fun TopBarWithActions(
        title: String,
        onNavigateBack: () -> Unit,
        modifier: Modifier = Modifier,
        actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
            modifier = modifier.fillMaxWidth().padding(AppSizes.paddingMedium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall),
                verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack, modifier = Modifier.size(AppSizes.iconSizeLarge)) {
                Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Retour",
                        modifier = Modifier.size(AppSizes.iconSizeMedium)
                )
            }
            Text(
                    text = title,
                    style = MaterialTheme.typography.h5.copy(fontSize = AppSizes.fontSizeH5)
            )
        }

        Row(
                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall),
                verticalAlignment = Alignment.CenterVertically
        ) { actions() }
    }
}
