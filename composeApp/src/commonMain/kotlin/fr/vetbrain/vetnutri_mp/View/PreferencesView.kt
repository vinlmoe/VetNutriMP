package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.vetbrain.vetnutri_mp.Data.PreferencesEspece
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Repository.PreferencesRepository
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.isIosPlatform

/** Vue des préférences de l'application */
@Composable
fun PreferencesView(
        preferencesRepository: PreferencesRepository,
        modifier: Modifier = Modifier
) {
        var selectedSpecies by remember { mutableStateOf<Espece?>(null) }
        var availableSpecies by remember { mutableStateOf<List<Espece>>(emptyList()) }
        var currentPreferences by remember { mutableStateOf<PreferencesEspece?>(null) }

        // Charger les données au démarrage
        LaunchedEffect(Unit) {
                preferencesRepository.loadPreferences()
                availableSpecies = preferencesRepository.getAvailableSpecies()

                if (availableSpecies.isNotEmpty()) {
                        selectedSpecies = availableSpecies.first()
                        currentPreferences =
                                preferencesRepository.getPreferencesForSpecies(selectedSpecies!!)
                }
        }

        Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
                // En-tête
                Text(
                        text = LocalizationKeys.Preferences.TITLE.translate(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                )

                // Sélecteur d'espèce
                Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        backgroundColor = VetNutriColors.Surface
                ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                        text = LocalizationKeys.Preferences.SELECTED_SPECIES.translate(),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                )

                                // Dropdown pour sélectionner l'espèce
                                var expanded by remember { mutableStateOf(false) }

                                Box {
                                        OutlinedTextField(
                                                value = selectedSpecies?.label ?: "",
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text(LocalizationKeys.Preferences.CHOOSE_SPECIES.translate()) },
                                                modifier =
                                                        Modifier.fillMaxWidth().clickable {
                                                                expanded = true
                                                        },
                                                colors =
                                                        TextFieldDefaults.outlinedTextFieldColors(
                                                                focusedBorderColor =
                                                                        VetNutriColors.Primary,
                                                                unfocusedBorderColor = Color.Gray
                                                        )
                                        )

                                        DropdownMenu(
                                                expanded = expanded,
                                                onDismissRequest = {
                                                    if (!isIosPlatform) {
                                                        expanded = false 
                                                    }
                                                },
                                                modifier =
                                                        Modifier.background(VetNutriColors.Surface)
                                                                .border(
                                                                        1.dp,
                                                                        Color.Gray,
                                                                        RoundedCornerShape(4.dp)
                                                                )
                                        ) {
                                                availableSpecies.forEach { species ->
                                                        DropdownMenuItem(
                                                                onClick = {
                                                                        selectedSpecies = species
                                                                        expanded = false
                                                                        // Charger les préférences
                                                                        // pour cette espèce
                                                                        // currentPreferences =
                                                                        // preferencesRepository.getPreferencesForSpecies(species)
                                                                }
                                                        ) { Text(species.translateEnum()) }
                                                }
                                        }
                                }
                        }
                }

                // Liste des espèces disponibles
                Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        backgroundColor = VetNutriColors.Surface
                ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                        text = LocalizationKeys.Preferences.AVAILABLE_SPECIES.translate(),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                )

                                LazyColumn(modifier = Modifier.height(120.dp)) {
                                        items(availableSpecies) { species ->
                                                Row(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .padding(vertical = 4.dp)
                                                                        .clickable {
                                                                                selectedSpecies =
                                                                                        species
                                                                                // currentPreferences =
                                                                                // preferencesRepository.getPreferencesForSpecies(species)
                                                                        },
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        Text(
                                                                text = species.translateEnum(),
                                                                modifier = Modifier.weight(1f)
                                                        )

                                                        if (selectedSpecies == species) {
                                                                Text(
                                                                        text = "✓",
                                                                        color =
                                                                                VetNutriColors
                                                                                        .Primary,
                                                                        fontWeight = FontWeight.Bold
                                                                )
                                                        }
                                                }
                                        }
                                }
                        }
                }

                // Préférences actuelles
                currentPreferences?.let { preferences ->
                        Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                backgroundColor = VetNutriColors.Surface
                        ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                                LocalizationKeys.Preferences.PREFERENCES_FOR.translate(
                                                        preferences.getEspeceEnum().translateEnum()
                                                ),
                                                style = MaterialTheme.typography.h5,
                                                fontWeight = FontWeight.Bold,
                                                color = VetNutriColors.Primary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                                LocalizationKeys.Preferences.ENERGY_EXPRESSION.translate(
                                                        preferences
                                                                .getTypeExpressionBesoinEnum()
                                                                .translateEnum()
                                                ),
                                                style = MaterialTheme.typography.subtitle1
                                        )
                                        val selectedNutrientsCount =
                                                preferences.nutrimentsSelectionnes.values.sumOf {
                                                        it.size
                                                }
                                        Text(
                                                LocalizationKeys.Preferences.SELECTED_NUTRIENTS_COUNT.translate(
                                                        selectedNutrientsCount.toString()
                                                ),
                                                style = MaterialTheme.typography.body2,
                                                color = Color.Gray
                                        )
                                }
                        }
                }

                // Boutons d'action
                Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                        Button(
                                onClick = {
                                        // Sauvegarder les préférences
                                },
                                colors =
                                        ButtonDefaults.buttonColors(
                                                backgroundColor = VetNutriColors.Primary
                                        )
                        ) { Text(LocalizationKeys.General.SAVE.translate()) }

                        OutlinedButton(
                                onClick = {
                                        // Réinitialiser les préférences
                                }
                        ) { Text(LocalizationKeys.General.RESET.translate()) }
                }
        }
}
