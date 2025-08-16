package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors

/**
 * Badge avec bouton de suppression intégré
 *
 * @param text Texte principal du badge
 * @param subText Texte secondaire optionnel (affiché entre parenthèses)
 * @param id Identifiant optionnel à afficher
 * @param backgroundColor Couleur d'arrière-plan du badge
 * @param onDismiss Action à exécuter lors du clic sur le bouton de suppression
 * @param modifier Modificateur Compose optionnel
 */
@Composable
fun DismissableBadge(
        text: String,
        subText: String? = null,
        id: Any? = null,
        backgroundColor: Color = VetNutriColors.Secondary,
        onDismiss: () -> Unit,
        modifier: Modifier = Modifier
) {
    Surface(
            color = backgroundColor.copy(alpha = 0.2f),
            shape = MaterialTheme.shapes.small,
            modifier = modifier.padding(vertical = 2.dp)
    ) {
        Row(
                modifier = Modifier.padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Column {
                Text(text = text, style = MaterialTheme.typography.body2)

                Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    if (id != null) {
                        Text(
                                text = "ID: $id",
                                style = MaterialTheme.typography.caption,
                                color = Color.Gray
                        )
                    }
                    if (subText != null) {
                        Text(
                                text = "($subText)",
                                style = MaterialTheme.typography.caption,
                                color = Color.Gray
                        )
                    }
                }
            }

            IconButton(onClick = onDismiss, modifier = Modifier.size(16.dp)) {
                Icon(
                        Icons.Default.Close,
                        contentDescription = "Supprimer",
                        tint = Color.Gray,
                        modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}
