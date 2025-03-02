package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel
import kotlin.math.roundToInt

@Composable
fun SettingsDialog(viewModel: SettingsViewModel, onDismiss: () -> Unit) {
    val uiScale by viewModel.uiScale.collectAsState()

    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Paramètres d'affichage", style = MaterialTheme.typography.h6) },
            text = {
                Column(
                        modifier = Modifier.padding(AppSizes.paddingMedium),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                ) {
                    Text("Taille de l'interface", style = MaterialTheme.typography.subtitle1)

                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                                onClick = { viewModel.decrementUiScale() },
                                enabled = uiScale > 0.5f,
                                modifier = Modifier.size(AppSizes.buttonHeight)
                        ) { Text("-") }

                        Text(
                                "${(uiScale * 100).roundToInt()}%",
                                style = MaterialTheme.typography.body1
                        )

                        Button(
                                onClick = { viewModel.incrementUiScale() },
                                enabled = uiScale < 2f,
                                modifier = Modifier.size(AppSizes.buttonHeight)
                        ) { Text("+") }
                    }
                }
            },
            confirmButton = {
                Button(
                        onClick = onDismiss,
                        colors =
                                ButtonDefaults.buttonColors(
                                        backgroundColor = VetNutriColors.Primary,
                                        contentColor = VetNutriColors.OnPrimary
                                )
                ) { Text("Fermer") }
            },
            backgroundColor = MaterialTheme.colors.surface
    )
}
