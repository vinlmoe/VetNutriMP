package fr.vetbrain.vetnutri_mp.View.SettingsSections

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.View.SettingsComponents.SettingsSection
import fr.vetbrain.vetnutri_mp.View.SettingsComponents.InfoSection
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel
import kotlin.math.roundToInt

/**
 * Section des paramètres d'interface
 * @param viewModel ViewModel des paramètres
 * @param modifier Modificateur appliqué au composant
 */
@Composable
fun InterfaceSettings(viewModel: SettingsViewModel, modifier: Modifier = Modifier) {
    val uiScale by viewModel.uiScale.collectAsState()

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // Section pour l'échelle de l'interface
        SettingsSection(
            title = "Échelle de l'interface",
            subtitle = "Ajustez la taille des éléments de l'interface pour améliorer la lisibilité",
            icon = Icons.Default.ZoomIn,
            content = {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Contrôles d'échelle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.decrementUiScale() },
                            enabled = uiScale > 0.5f,
                            modifier = Modifier.size(AppSizes.buttonHeight),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = VetNutriColors.Secondary
                            )
                        ) { 
                            Text("-", style = MaterialTheme.typography.h6) 
                        }

                        // Affichage de l'échelle actuelle
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${(uiScale * 100).roundToInt()}%",
                                style = MaterialTheme.typography.h5,
                                color = VetNutriColors.Primary
                            )
                            Text(
                                text = "Taille actuelle",
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        Button(
                            onClick = { viewModel.incrementUiScale() },
                            enabled = uiScale < 2f,
                            modifier = Modifier.size(AppSizes.buttonHeight),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = VetNutriColors.Secondary
                            )
                        ) { 
                            Text("+", style = MaterialTheme.typography.h6) 
                        }
                    }

                    // Informations sur l'échelle
                    InfoSection(
                        title = "Informations sur l'échelle",
                        message = "• Échelle minimale : 50% (0.5x)\n• Échelle maximale : 200% (2.0x)\n• Échelle par défaut : 100% (1.0x)"
                    )
                }
            }
        )
    }
}
