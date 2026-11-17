package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.ViewModel.NewReferenceEvViewModel
import fr.vetbrain.vetnutri_mp.Utils.isIosPlatform

// Éditeur de référentiel : onglets Équations et Compléments

/**
 * Onglet pour la gestion des équations de la référence.
 *
 * @param viewModel ViewModel pour les opérations sur les références
 */
@Composable
fun ReferenceEvEquationsTab(viewModel: NewReferenceEvViewModel) {
        val availableEquations by viewModel.availableEquations.collectAsState()
        val currentEquations by viewModel.currentEquations.collectAsState()
        val currentReference by viewModel.currentReference.collectAsState()

        // Vérifier si la référence est pour maladie
        val isForMaladie = currentEquations.maladie

        var selectedEquationBW by remember { mutableStateOf(currentEquations.equationBW) }
        var selectedEquationBEE by remember { mutableStateOf(currentEquations.equationBEE) }
        var selectedEquationDEcom by remember { mutableStateOf(currentEquations.equationDEcom) }
        var selectedEquationDEraw by remember { mutableStateOf(currentEquations.equationDEraw) }

        var expandedBW by remember { mutableStateOf(false) }
        var expandedBEE by remember { mutableStateOf(false) }
        var expandedDEcom by remember { mutableStateOf(false) }
        var expandedDEraw by remember { mutableStateOf(false) }

        // Filtrage des équations par type
        val bwEquations =
                availableEquations.filter {
                        it.kind == fr.vetbrain.vetnutri_mp.Enumer.EquationKind.MW
                }
        val beeEquations =
                availableEquations.filter {
                        it.kind == fr.vetbrain.vetnutri_mp.Enumer.EquationKind.ENERGYNEED
                }
        val energyDensityEquations =
                availableEquations.filter {
                        it.kind == fr.vetbrain.vetnutri_mp.Enumer.EquationKind.ENERGYDENSITY
                }

        // Effet pour charger les équations au chargement de l'onglet
        LaunchedEffect(Unit) { viewModel.loadEquations() }

        // Débogage: Affichage des informations sur les équations disponibles
        LaunchedEffect(availableEquations) {
                
                
                availableEquations.forEachIndexed { index, equation -> }
        }

        Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
                // Barre d'actions locale (Rafraîchir)
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        OutlinedButton(onClick = { viewModel.loadEquations() }) {
                                Text(translate(LocalizationKeys.NewReference.REFRESH_EQUATIONS))
                        }
                }

                // Afficher un message d'information si la référence est pour maladie
                if (isForMaladie) {
                        Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = 2.dp,
                                backgroundColor = MaterialTheme.colors.secondary.copy(alpha = 0.1f)
                        ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                         Text(
                                                text = translate(LocalizationKeys.NewReference.MALADIE_WARNING_TITLE),
                                                style = MaterialTheme.typography.h6,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colors.secondary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                                text = translate(LocalizationKeys.NewReference.MALADIE_WARNING_DESC),
                                                style = MaterialTheme.typography.body2,
                                                color =
                                                        MaterialTheme.colors.onSurface.copy(
                                                                alpha = 0.7f
                                                        )
                                        )
                                }
                        }

                        // Combobox unique ENERCOMP
                        val enercompEquations =
                                availableEquations.filter {
                                        it.kind == fr.vetbrain.vetnutri_mp.Enumer.EquationKind.ENERCOMP &&
                                                (it.specie == null || it.specie == currentReference.espece)
                                }

                        Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                         Text(
                                                text = translate(LocalizationKeys.NewReference.EQ_ENERCOMP_TITLE),
                                                style = MaterialTheme.typography.subtitle1,
                                                fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        var expanded by remember { mutableStateOf(false) }
                                        val selectedEnercomp =
                                                currentReference.equationsNut.firstOrNull {
                                                        it.kind ==
                                                                fr.vetbrain.vetnutri_mp.Enumer
                                                                        .EquationKind.ENERCOMP
                                                }

                                        Box(modifier = Modifier.fillMaxWidth()) {
                                                OutlinedButton(
                                                        onClick = { expanded = true },
                                                        modifier = Modifier.fillMaxWidth()
                                                ) {
                                                         Text(
                                                                selectedEnercomp?.name
                                                                        ?: translate(LocalizationKeys.NewReference.SELECT_EQUATION)
                                                        )
                                                }
                                                DropdownMenu(
                                                        expanded = expanded,
                                                        onDismissRequest = {
                                                            if (!isIosPlatform) {
                                                                expanded = false 
                                                            }
                                                        },
                                                        modifier = Modifier.fillMaxWidth(0.9f)
                                                ) {
                                                        // Option Aucune
                                                        DropdownMenuItem(
                                                                onClick = {
                                                                        if (selectedEnercomp != null) {
                                                                                viewModel.toggleComplementaryEquation(
                                                                                        selectedEnercomp
                                                                                )
                                                                        }
                                                                        expanded = false
                                                                }
                                                         ) { Text(translate(LocalizationKeys.NewReference.EQ_NONE)) }

                                                        enercompEquations.forEach { eq ->
                                                                DropdownMenuItem(
                                                                        onClick = {
                                                                                if (selectedEnercomp != null &&
                                                                                                selectedEnercomp.uuid !=
                                                                                                        eq.uuid
                                                                                ) {
                                                                                        viewModel.toggleComplementaryEquation(
                                                                                                selectedEnercomp
                                                                                        )
                                                                                }
                                                                                if (selectedEnercomp?.uuid != eq.uuid) {
                                                                                        viewModel.toggleComplementaryEquation(eq)
                                                                                }
                                                                                expanded = false
                                                                        }
                                                                ) { Text(eq.name) }
                                                        }
                                                }
                                        }
                                }
                        }
                } else {
                        // Afficher les équations seulement si ce n'est pas pour maladie

                        // Équation Poids corporel (BW)
                        Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                         Text(
                                                text = translate(LocalizationKeys.NewReference.EQ_METABOLIC_WEIGHT),
                                                style = MaterialTheme.typography.subtitle1,
                                                fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Box(modifier = Modifier.fillMaxWidth()) {
                                                OutlinedButton(
                                                        onClick = { expandedBW = true },
                                                        modifier = Modifier.fillMaxWidth()
                                                ) {
                                                         Text(
                                                                selectedEquationBW?.name
                                                                        ?: translate(LocalizationKeys.NewReference.SELECT_EQUATION)
                                                        )
                                                }

                                                DropdownMenu(
                                                        expanded = expandedBW,
                                                        onDismissRequest = {
                                                            if (!isIosPlatform) {
                                                                expandedBW = false 
                                                            }
                                                        },
                                                        modifier = Modifier.fillMaxWidth(0.9f)
                                                ) {
                                                        // Option "Aucune équation"
                                                        DropdownMenuItem(
                                                                onClick = {
                                                                        selectedEquationBW = null
                                                                        viewModel
                                                                                .setEquationBWSilently(
                                                                                        null
                                                                                )
                                                                        expandedBW = false
                                                                }
                                                        ) { Text(text = translate(LocalizationKeys.NewReference.EQ_NONE)) }

                                                        // Filtrer les équations de type MW
                                                        bwEquations.forEach { equation ->
                                                                DropdownMenuItem(
                                                                        onClick = {
                                                                                selectedEquationBW =
                                                                                        equation
                                                                                viewModel
                                                                                        .setEquationBWSilently(
                                                                                                equation
                                                                                        )
                                                                                expandedBW = false
                                                                        }
                                                                ) { Text(text = equation.name) }
                                                        }
                                                }
                                        }
                                }
                        }

                        // Équation Besoin Énergétique de Base (BEE)
                        Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                         Text(
                                                text = translate(LocalizationKeys.NewReference.EQ_BEE),
                                                style = MaterialTheme.typography.subtitle1,
                                                fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Box(modifier = Modifier.fillMaxWidth()) {
                                                OutlinedButton(
                                                        onClick = { expandedBEE = true },
                                                        modifier = Modifier.fillMaxWidth()
                                                ) {
                                                         Text(
                                                                selectedEquationBEE?.name
                                                                        ?: translate(LocalizationKeys.NewReference.SELECT_EQUATION)
                                                        )
                                                }

                                                DropdownMenu(
                                                        expanded = expandedBEE,
                                                        onDismissRequest = {
                                                            if (!isIosPlatform) {
                                                                expandedBEE = false 
                                                            }
                                                        },
                                                        modifier = Modifier.fillMaxWidth(0.9f)
                                                ) {
                                                        // Option "Aucune équation"
                                                        DropdownMenuItem(
                                                                onClick = {
                                                                        selectedEquationBEE = null
                                                                        viewModel
                                                                                .setEquationBEESilently(
                                                                                        null
                                                                                )
                                                                        expandedBEE = false
                                                                }
                                                        ) { Text(text = translate(LocalizationKeys.NewReference.EQ_NONE)) }

                                                        // Filtrer les équations de type ENERGYNEED
                                                        beeEquations.forEach { equation ->
                                                                DropdownMenuItem(
                                                                        onClick = {
                                                                                selectedEquationBEE =
                                                                                        equation
                                                                                viewModel
                                                                                        .setEquationBEESilently(
                                                                                                equation
                                                                                        )
                                                                                expandedBEE = false
                                                                        }
                                                                ) { Text(text = equation.name) }
                                                        }
                                                }
                                        }
                                }
                        }

                        // Équation pour l'Énergie Digestible des Aliments Composés (DEcom)
                        Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                         Text(
                                                text = translate(LocalizationKeys.NewReference.EQ_DECOM),
                                                style = MaterialTheme.typography.subtitle1,
                                                fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Box(modifier = Modifier.fillMaxWidth()) {
                                                OutlinedButton(
                                                        onClick = { expandedDEcom = true },
                                                        modifier = Modifier.fillMaxWidth()
                                                ) {
                                                         Text(
                                                                selectedEquationDEcom?.name
                                                                        ?: translate(LocalizationKeys.NewReference.SELECT_EQUATION)
                                                        )
                                                }

                                                DropdownMenu(
                                                        expanded = expandedDEcom,
                                                        onDismissRequest = {
                                                            if (!isIosPlatform) {

                                                                expandedDEcom = false

                                                            }
                                                        },
                                                        modifier = Modifier.fillMaxWidth(0.9f)
                                                ) {
                                                        // Option "Aucune équation"
                                                        DropdownMenuItem(
                                                                onClick = {
                                                                        selectedEquationDEcom = null
                                                                        viewModel
                                                                                .setEquationDEcomSilently(
                                                                                        null
                                                                                )
                                                                        expandedDEcom = false
                                                                }
                                                        ) { Text(text = translate(LocalizationKeys.NewReference.EQ_NONE)) }

                                                        // Filtrer les équations de type
                                                        // ENERGYDENSITY
                                                        energyDensityEquations.forEach { equation ->
                                                                DropdownMenuItem(
                                                                        onClick = {
                                                                                selectedEquationDEcom =
                                                                                        equation
                                                                                viewModel
                                                                                        .setEquationDEcomSilently(
                                                                                                equation
                                                                                        )
                                                                                expandedDEcom =
                                                                                        false
                                                                        }
                                                                ) { Text(text = equation.name) }
                                                        }
                                                }
                                        }
                                }
                        }

                        // Équation pour l'Énergie Digestible des Aliments Bruts (DEraw)
                        Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                         Text(
                                                text = translate(LocalizationKeys.NewReference.EQ_DERAW),
                                                style = MaterialTheme.typography.subtitle1,
                                                fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Box(modifier = Modifier.fillMaxWidth()) {
                                                OutlinedButton(
                                                        onClick = { expandedDEraw = true },
                                                        modifier = Modifier.fillMaxWidth()
                                                ) {
                                                         Text(
                                                                selectedEquationDEraw?.name
                                                                        ?: translate(LocalizationKeys.NewReference.SELECT_EQUATION)
                                                        )
                                                }

                                                DropdownMenu(
                                                        expanded = expandedDEraw,
                                                        onDismissRequest = {
                                                            if (!isIosPlatform) {

                                                                expandedDEraw = false

                                                            }
                                                        },
                                                        modifier = Modifier.fillMaxWidth(0.9f)
                                                ) {
                                                        // Option "Aucune équation"
                                                        DropdownMenuItem(
                                                                onClick = {
                                                                        selectedEquationDEraw = null
                                                                        viewModel
                                                                                .setEquationDErawSilently(
                                                                                        null
                                                                                )
                                                                        expandedDEraw = false
                                                                }
                                                        ) { Text(text = translate(LocalizationKeys.NewReference.EQ_NONE)) }

                                                        // Filtrer les équations de type
                                                        // ENERGYDENSITY
                                                        energyDensityEquations.forEach { equation ->
                                                                DropdownMenuItem(
                                                                        onClick = {
                                                                                selectedEquationDEraw =
                                                                                        equation
                                                                                viewModel
                                                                                        .setEquationDErawSilently(
                                                                                                equation
                                                                                        )
                                                                                expandedDEraw =
                                                                                        false
                                                                        }
                                                                ) { Text(text = equation.name) }
                                                        }
                                                }
                                        }
                                }
                        }
                }

                // Bouton de sauvegarde des équations
        }
}

/**
 * Onglet pour la sélection des équations de nutriments complémentaires. Filtre par espèce de la
 * référence et inclut les équations de l'espèce générique CH.
 */
@Composable
fun ReferenceEvComplementaryTab(viewModel: NewReferenceEvViewModel) {
        val currentReference by viewModel.currentReference.collectAsState()
        val forceUpdate by viewModel.forceUpdate.collectAsState()
        val complementary =
                remember(currentReference, forceUpdate) {
                        viewModel.getComplementaryEquationsForCurrent()
                }
        val selectedUuids =
                remember(currentReference, forceUpdate) {
                        currentReference.equationsNut.map { it.uuid }.toSet()
                }

        // État local optimiste pour un retour visuel immédiat au clic
        var selectedUuidsState by
                remember(currentReference, forceUpdate) { mutableStateOf(selectedUuids) }

        Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
                Text(
                        text = translate(LocalizationKeys.NewReference.COMPLEMENTARY_EQ_TITLE),
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                )

                if (complementary.isEmpty()) {
                        Text(
                                text = translate(LocalizationKeys.NewReference.COMPLEMENTARY_EQ_NONE, currentReference.espece.translateEnum()),
                                style = MaterialTheme.typography.body2,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        )
                } else {
                        LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                        ) {
                                items(complementary) { eq ->
                                        Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
                                                Row(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .padding(12.dp),
                                                        horizontalArrangement =
                                                                Arrangement.SpaceBetween,
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                                Text(
                                                                        eq.name,
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .subtitle1,
                                                                        fontWeight =
                                                                                FontWeight.Medium
                                                                )
                                                                val nutrientLabel =
                                                                        eq.nutrient?.label ?: "?"
                                                                val especeLabel =
                                                                        eq.specie?.label
                                                                                ?: translate(LocalizationKeys.NewReference.ALL_SPECIES)
                                                                Text(
                                                                        text =
                                                                                translate(LocalizationKeys.NewReference.NUTRIENT_SPECIES_FOOTER, nutrientLabel, especeLabel),
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .caption,
                                                                        color =
                                                                                MaterialTheme.colors
                                                                                        .onSurface
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.7f
                                                                                        )
                                                                )
                                                        }
                                                        val isSelected =
                                                                selectedUuidsState.contains(eq.uuid)
                                                        Button(
                                                                onClick = {
                                                                        // Mise à jour optimiste
                                                                        selectedUuidsState =
                                                                                if (isSelected)
                                                                                        selectedUuidsState -
                                                                                                eq.uuid
                                                                                else
                                                                                        selectedUuidsState +
                                                                                                eq.uuid
                                                                        viewModel
                                                                                .toggleComplementaryEquation(
                                                                                        eq
                                                                                )
                                                                },
                                                                colors =
                                                                        ButtonDefaults.buttonColors(
                                                                                backgroundColor =
                                                                                        if (isSelected
                                                                                        )
                                                                                                MaterialTheme
                                                                                                        .colors
                                                                                                        .primary
                                                                                        else
                                                                                                MaterialTheme
                                                                                                        .colors
                                                                                                        .surface
                                                                        )
                                                        ) {
                                                                Text(
                                                                        if (isSelected)
                                                                                translate(
                                                                                        LocalizationKeys
                                                                                                .NewReference
                                                                                                .REMOVE_ASSOCIATION
                                                                                )
                                                                        else
                                                                                translate(
                                                                                        LocalizationKeys
                                                                                                .NewReference
                                                                                                .ADD_ASSOCIATION
                                                                                )
                                                                )
                                                        }
                                                }
                                        }
                                }
                        }
                }
        }
}
