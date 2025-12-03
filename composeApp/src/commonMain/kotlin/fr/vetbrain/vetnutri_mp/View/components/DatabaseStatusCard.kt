package fr.vetbrain.vetnutri_mp.View.Components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.DatabaseStatus

/**
 * Composant optimisé pour afficher le statut de la base de données
 * Utilise remember pour éviter les recalculs inutiles
 */
@Composable
fun DatabaseStatusCard(
    databaseStatus: DatabaseStatus?,
    isCheckingDatabase: Boolean,
    isUpdatingDatabase: Boolean,
    modifier: Modifier = Modifier
) {
    // Optimisation : utiliser remember pour éviter les recalculs
    val statusInfo = remember(databaseStatus) {
        databaseStatus?.let { status ->
            when {
                status.needsUpdate -> StatusInfo(
                    icon = Icons.Default.Warning,
                    title = "Base de données incomplète",
                    description = "Mise à jour requise",
                    color = androidx.compose.ui.graphics.Color.Red,
                    isError = true
                )
                status.foodCount > 0 && status.referenceCount > 0 -> StatusInfo(
                    icon = Icons.Default.CheckCircle,
                    title = "Base de données complète",
                    description = "${status.foodCount} aliments, ${status.referenceCount} références",
                    color = VetNutriColors.Primary,
                    isError = false
                )
                else -> StatusInfo(
                    icon = Icons.Default.Info,
                    title = "Vérification en cours...",
                    description = "Chargement des données",
                    color = VetNutriColors.Secondary,
                    isError = false
                )
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Indicateur de statut
            when {
                isCheckingDatabase -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = VetNutriColors.Primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Vérification de la base de données...",
                        style = MaterialTheme.typography.body2,
                        color = VetNutriColors.Secondary
                    )
                }
                isUpdatingDatabase -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = VetNutriColors.Primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Mise à jour en cours...",
                        style = MaterialTheme.typography.body2,
                        color = VetNutriColors.Secondary
                    )
                }
                statusInfo != null -> {
                    Icon(
                        imageVector = statusInfo.icon,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = statusInfo.color
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = statusInfo.title,
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold,
                        color = statusInfo.color,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = statusInfo.description,
                        style = MaterialTheme.typography.body2,
                        color = VetNutriColors.Secondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Données optimisées pour l'affichage du statut
 */
@Stable
private data class StatusInfo(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val description: String,
    val color: androidx.compose.ui.graphics.Color,
    val isError: Boolean
)
