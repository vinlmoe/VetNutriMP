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
import fr.vetbrain.vetnutri_mp.Data.Equation
import fr.vetbrain.vetnutri_mp.Data.PreferencesEspece
import fr.vetbrain.vetnutri_mp.Enumer.EquationKind
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.MainNutrientEnum
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis
import fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMin
import fr.vetbrain.vetnutri_mp.Enumer.NutrientOther
import fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Repository.PreferencesRepository
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors

/** Vue des préférences de l'application */
@Composable
fun PreferencesView(
        preferencesRepository: PreferencesRepository,
        equationRepository: EquationRepository,
        modifier: Modifier = Modifier
) {
        var selectedSpecies by remember { mutableStateOf<Espece?>(null) }
        var availableSpecies by remember { mutableStateOf<List<Espece>>(emptyList()) }
        var currentPreferences by remember { mutableStateOf<PreferencesEspece?>(null) }
        var complementaryEquations by remember { mutableStateOf<List<Equation>>(emptyList()) }
        var selectedNutrients by remember { mutableStateOf<Set<String>>(emptySet()) }

        // Charger les données au démarrage
        LaunchedEffect(Unit) {
                preferencesRepository.loadPreferences()
                availableSpecies = preferencesRepository.getAvailableSpecies()

                // Charger les équations complémentaires
                complementaryEquations =
                        equationRepository.getAllEquations().filter {
                                it.kind == EquationKind.COMPLEMENTARY_NUTRIENT
                        }

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
                                                onDismissRequest = { expanded = false },
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

                // Section des équations complémentaires
                Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        backgroundColor = VetNutriColors.Surface
                ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                        LocalizationKeys.Preferences.COMPLEMENTARY_EQUATIONS.translate(),
                                        style = MaterialTheme.typography.subtitle1,
                                        fontWeight = FontWeight.Bold
                                )
                                Spacer(
                                        modifier =
                                                Modifier.height(4.dp)
                                )
                                if (complementaryEquations.isEmpty()) {
                                        Text(
                                                LocalizationKeys.Preferences.NO_COMPLEMENTARY_EQUATIONS.translate(),
                                                style =
                                                        MaterialTheme
                                                                .typography
                                                                .body2,
                                                color = Color.Gray
                                        )
                                } else {
                                        // Liste des nutriments avec leurs équations associées
                                        LazyColumn {
                                                items(
                                                        getNutrientsWithComplementaryEquations(
                                                                complementaryEquations
                                                        )
                                                ) { nutrient ->
                                                        val currentEquation =
                                                                currentPreferences
                                                                        ?.getEquationComplementaire(
                                                                                nutrient.label
                                                                        )
                                                        val selectedEquation =
                                                                complementaryEquations.find {
                                                                        it.uuid == currentEquation
                                                                }

                                                        Row(
                                                                modifier =
                                                                        Modifier.fillMaxWidth()
                                                                                .padding(
                                                                                        vertical =
                                                                                                4.dp
                                                                                ),
                                                                verticalAlignment =
                                                                        Alignment.CenterVertically
                                                        ) {
                                                                Text(
                                                                        text = nutrient.translateEnum(),
                                                                        modifier =
                                                                                Modifier.weight(1f),
                                                                        fontWeight =
                                                                                FontWeight.Medium
                                                                )

                                                                // Dropdown pour sélectionner
                                                                // l'équation
                                                                var expanded by remember {
                                                                        mutableStateOf(false)
                                                                }

                                                                Box {
                                                                        OutlinedTextField(
                                                                                value =
                                                                                        selectedEquation
                                                                                                ?.name
                                                                                                ?: LocalizationKeys.Preferences.NO_EQUATION.translate(),
                                                                                onValueChange = {},
                                                                                readOnly = true,
                                                                                label = {
                                                                                        Text(
                                                                                                LocalizationKeys.Preferences.EQUATION.translate()
                                                                                        )
                                                                                },
                                                                                modifier =
                                                                                        Modifier.width(
                                                                                                        200.dp
                                                                                                )
                                                                                                .clickable {
                                                                                                        expanded =
                                                                                                                true
                                                                                                },
                                                                                colors =
                                                                                        TextFieldDefaults
                                                                                                .outlinedTextFieldColors(
                                                                                                        focusedBorderColor =
                                                                                                                VetNutriColors
                                                                                                                        .Primary,
                                                                                                        unfocusedBorderColor =
                                                                                                                Color.Gray
                                                                                                )
                                                                        )

                                                                        DropdownMenu(
                                                                                expanded = expanded,
                                                                                onDismissRequest = {
                                                                                        expanded =
                                                                                                false
                                                                                },
                                                                                modifier =
                                                                                        Modifier.background(
                                                                                                        VetNutriColors
                                                                                                                .Surface
                                                                                                )
                                                                                                .border(
                                                                                                        1.dp,
                                                                                                        Color.Gray,
                                                                                                        RoundedCornerShape(
                                                                                                                4.dp
                                                                                                        )
                                                                                                )
                                                                        ) {
                                                                                // Option "Aucune
                                                                                // équation"
                                                                                DropdownMenuItem(
                                                                                        onClick = {
                                                                                                currentPreferences =
                                                                                                        currentPreferences
                                                                                                                ?.removeEquationComplementaire(
                                                                                                                        nutrient.label
                                                                                                                )
                                                                                                expanded =
                                                                                                        false
                                                                                        }
                                                                                ) {
                                                                                        Text(
                                                                                                LocalizationKeys.Preferences.NO_EQUATION.translate()
                                                                                        )
                                                                                }

                                                                                // Options des
                                                                                // équations
                                                                                // disponibles
                                                                                complementaryEquations
                                                                                        .forEach {
                                                                                                equation
                                                                                                ->
                                                                                                DropdownMenuItem(
                                                                                                        onClick = {
                                                                                                                currentPreferences =
                                                                                                                        currentPreferences
                                                                                                                                ?.setEquationComplementaire(
                                                                                                                                        nutrient.label,
                                                                                                                                        equation.uuid
                                                                                                                                )
                                                                                                                expanded =
                                                                                                                        false
                                                                                                        }
                                                                                                ) {
                                                                                                        Text(
                                                                                                                equation.name
                                                                                                        )
                                                                                                }
                                                                                        }
                                                                        }
                                                                }
                                                        }
                                                }
                                        }
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

/** Retourne tous les nutriments disponibles dans l'application */
private fun getAllNutrients(): List<Nutrient> {
        val allNutrients = mutableListOf<Nutrient>()

        // Ajouter les nutriments principaux
        allNutrients.addAll(NutrientMain.entries)

        // Ajouter les nutriments minéraux
        allNutrients.addAll(NutrientMin.entries)

        // Ajouter les nutriments macro
        allNutrients.addAll(NutrientMacro.entries)

        // Ajouter les vitamines
        allNutrients.addAll(NutrientVitam.entries)

        // Ajouter les acides aminés
        allNutrients.addAll(NutrientAnalysis.entries)

        // Ajouter les acides gras
        allNutrients.addAll(NutrientLipid.entries)

        // Ajouter les autres nutriments
        allNutrients.addAll(NutrientOther.entries)

        return allNutrients
}

/**
 * Retourne les nutriments qui ont des équations complémentaires disponibles
 * @param complementaryEquations Liste des équations complémentaires disponibles
 * @return Liste des nutriments qui peuvent être calculés avec des équations complémentaires
 */
private fun getNutrientsWithComplementaryEquations(
        complementaryEquations: List<Equation>
): List<Nutrient> {
        // Pour l'instant, on retourne tous les nutriments car on ne peut pas déterminer
        // automatiquement quels nutriments ont des équations complémentaires
        // Dans une version future, on pourrait analyser les équations pour déterminer
        // quels nutriments elles calculent

        // Pour l'exemple, on va filtrer pour ne montrer que certains nutriments
        // qui sont typiquement calculés avec des équations complémentaires
        val allNutrients = getAllNutrients()

        // Filtrer pour ne montrer que les nutriments qui sont souvent calculés
        // avec des équations complémentaires (vitamines, minéraux, etc.)
        return allNutrients.filter { nutrient ->
                when (nutrient.getMNE()) {
                        MainNutrientEnum.VITAM -> true // Les vitamines sont souvent calculées
                        MainNutrientEnum.MIN -> true // Les minéraux aussi
                        MainNutrientEnum.ANA -> true // Les acides aminés
                        MainNutrientEnum.LIPID -> true // Les acides gras
                        else -> false // Les autres nutriments sont généralement disponibles
                // directement
                }
        }
}
