package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import fr.vetbrain.vetnutri_mp.Enumer.Language
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel
import kotlin.math.roundToInt

@Composable
fun SettingsDialog(viewModel: SettingsViewModel, onDismiss: () -> Unit) {
        val uiScale by viewModel.uiScale.collectAsState()
        val currentLanguage by viewModel.currentLanguage.collectAsState()
        var expandedLanguageMenu by remember { mutableStateOf(false) }

        AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("settings.title".translate(), style = MaterialTheme.typography.h6) },
                text = {
                        Column(
                                modifier = Modifier.padding(AppSizes.paddingMedium),
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                        ) {
                                // Section Affichage
                                Text(
                                        "settings.display".translate(),
                                        style = MaterialTheme.typography.subtitle1
                                )

                                // Taille de l'interface
                                Text(
                                        "settings.interface_size".translate(),
                                        style = MaterialTheme.typography.subtitle2
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

                                Divider(
                                        modifier =
                                                Modifier.padding(vertical = AppSizes.paddingMedium)
                                )

                                // Section Langue
                                Text(
                                        "settings.language".translate(),
                                        style = MaterialTheme.typography.subtitle1
                                )

                                Box {
                                        Button(
                                                onClick = { expandedLanguageMenu = true },
                                                modifier = Modifier.fillMaxWidth()
                                        ) {
                                                Text(
                                                        "settings.language.${currentLanguage.code}".translate()
                                                )
                                        }

                                        DropdownMenu(
                                                expanded = expandedLanguageMenu,
                                                onDismissRequest = { expandedLanguageMenu = false },
                                                modifier = Modifier.fillMaxWidth(0.8f)
                                        ) {
                                                Language.values().forEach { language ->
                                                        DropdownMenuItem(
                                                                onClick = {
                                                                        viewModel.setLanguage(
                                                                                language
                                                                        )
                                                                        expandedLanguageMenu = false
                                                                }
                                                        ) {
                                                                Text(
                                                                        "settings.language.${language.code}".translate()
                                                                )
                                                        }
                                                }
                                        }
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
                        ) { Text("save".translate()) }
                },
                backgroundColor = MaterialTheme.colors.surface
        )
}
