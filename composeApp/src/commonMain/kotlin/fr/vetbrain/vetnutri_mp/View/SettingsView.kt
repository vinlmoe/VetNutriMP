package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel
import fr.vetbrain.vetnutri_mp.importFoodsFromFile
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
                                Text(
                                        "Taille de l'interface",
                                        style = MaterialTheme.typography.subtitle1
                                )

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

@Composable
fun TopBar(title: String, onBackClick: () -> Unit) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
                IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                }
                Text(
                        text = title,
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(start = 8.dp)
                )
        }
}

@Composable
fun SettingsView(viewModel: SettingsViewModel, onImportAnimals: () -> Unit, onBack: () -> Unit) {
        Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
                TopBar(title = "Paramètres", onBackClick = onBack)

                // Échelle de l'interface
                Text(text = "Échelle de l'interface", style = MaterialTheme.typography.h6)

                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                        IconButton(onClick = { viewModel.decrementUiScale() }) {
                                Text("-", style = MaterialTheme.typography.h6)
                        }

                        Text(
                                text =
                                        "${(viewModel.uiScale.collectAsState().value * 100).toInt()}%",
                                style = MaterialTheme.typography.body1
                        )

                        IconButton(onClick = { viewModel.incrementUiScale() }) {
                                Icon(Icons.Default.Add, contentDescription = "Augmenter")
                        }
                }

                Divider()

                // Importation des données
                Text(text = "Importation des données", style = MaterialTheme.typography.h6)

                Button(onClick = onImportAnimals, modifier = Modifier.fillMaxWidth()) {
                        Text("+", style = MaterialTheme.typography.button)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Importer des animaux")
                }

                Button(
                        onClick = {
                                // Ouvrir un sélecteur de fichier pour importer les aliments
                                importFoodsFromFile(viewModel)
                        },
                        modifier = Modifier.fillMaxWidth()
                ) {
                        Text("+", style = MaterialTheme.typography.button)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Importer des aliments")
                }
        }
}
