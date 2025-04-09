package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import fr.vetbrain.vetnutri_mp.Data.NutrientRef
import fr.vetbrain.vetnutri_mp.Enumer.MainNutrientEnum
import fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.NutrientRefViewModel

/**
 * Vue pour l'édition des besoins nutritionnels d'une référence par catégorie
 *
 * @param viewModel ViewModel pour la gestion des besoins nutritionnels
 * @param nutrientType Type de nutriment à éditer (BASE, MACRO, MIN, etc.)
 * @param onNavigateBack Callback pour revenir à l'écran précédent
 * @param modifier Modifier à appliquer à la vue
 */
@Composable
fun NutrientRefEditView(
        viewModel: NutrientRefViewModel,
        nutrientType: MainNutrientEnum,
        onNavigateBack: () -> Unit,
        modifier: Modifier = Modifier
) {
    val nutrientRefs by viewModel.nutrientRefs.collectAsState()
    val availableBiblioRefs by viewModel.availableBiblioRefs.collectAsState()

    LaunchedEffect(nutrientType) {
        viewModel.loadNutrientsForType(nutrientType)
        viewModel.loadAvailableBiblioRefs()
    }

    Scaffold(
            topBar = {
                TopBarSimple(
                        title = "Édition des besoins - ${nutrientType.name}",
                        onNavigateBack = onNavigateBack
                )
            }
    ) { paddingValues ->
        Column(modifier = modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            // En-tête du tableau
            Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        "Nutriment",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(2f).padding(horizontal = 8.dp)
                )
                Text(
                        "Valeur",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                )
                Text(
                        "Unité",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                )
                Text(
                        "Référence",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(2f).padding(horizontal = 8.dp)
                )
            }

            Divider()

            // Liste des nutriments
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                items(nutrientRefs.filter { it.nutrientType == nutrientType }) { nutrientRef ->
                    NutrientRefItem(
                            nutrientRef = nutrientRef,
                            onValueChange = { newValue ->
                                viewModel.updateNutrientValue(nutrientRef.id, newValue)
                            },
                            onUnitChange = { newUnit ->
                                viewModel.updateNutrientUnit(nutrientRef.id, newUnit)
                            },
                            onBiblioRefChange = { newBiblioRef ->
                                viewModel.updateNutrientBiblioRef(nutrientRef.id, newBiblioRef)
                            },
                            availableBiblioRefs = availableBiblioRefs
                    )
                    Divider()
                }
            }

            // Boutons d'action en bas
            Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                        onClick = { viewModel.resetNutrientsForType(nutrientType) },
                        modifier = Modifier.padding(end = 8.dp)
                ) { Text("Réinitialiser") }

                Button(
                        onClick = {
                            viewModel.saveNutrientsForType(nutrientType)
                            onNavigateBack()
                        }
                ) { Text("Enregistrer") }
            }
        }
    }
}

@Composable
private fun NutrientRefItem(
        nutrientRef: NutrientRef,
        onValueChange: (String) -> Unit,
        onUnitChange: (UnitReqEnum) -> Unit,
        onBiblioRefChange: (BiblioRef?) -> Unit,
        availableBiblioRefs: List<BiblioRef>,
        modifier: Modifier = Modifier
) {
    var showUnitDropdown by remember { mutableStateOf(false) }
    var showBiblioDropdown by remember { mutableStateOf(false) }

    Row(
            modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        // Nom du nutriment
        Text(text = nutrientRef.name, modifier = Modifier.weight(2f).padding(horizontal = 8.dp))

        // Valeur du nutriment
        OutlinedTextField(
                value = nutrientRef.value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                singleLine = true,
                colors =
                        TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = VetNutriColors.Primary,
                                unfocusedBorderColor = Color.Gray
                        )
        )

        // Sélection de l'unité
        Box(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
            OutlinedButton(
                    onClick = { showUnitDropdown = true },
                    modifier = Modifier.fillMaxWidth()
            ) { Text(nutrientRef.unitReq.toString()) }

            DropdownMenu(
                    expanded = showUnitDropdown,
                    onDismissRequest = { showUnitDropdown = false }
            ) {
                UnitReqEnum.values().forEach { unit ->
                    DropdownMenuItem(
                            onClick = {
                                onUnitChange(unit)
                                showUnitDropdown = false
                            }
                    ) { Text(unit.toString()) }
                }
            }
        }

        // Sélection de la référence bibliographique
        Box(modifier = Modifier.weight(2f).padding(horizontal = 4.dp)) {
            OutlinedButton(
                    onClick = { showBiblioDropdown = true },
                    modifier = Modifier.fillMaxWidth()
            ) { Text(nutrientRef.biblioRef?.firstAuthor ?: "Aucune") }

            DropdownMenu(
                    expanded = showBiblioDropdown,
                    onDismissRequest = { showBiblioDropdown = false }
            ) {
                // Option pour aucune référence
                DropdownMenuItem(
                        onClick = {
                            onBiblioRefChange(null)
                            showBiblioDropdown = false
                        }
                ) { Text("Aucune") }

                // Liste des références disponibles
                availableBiblioRefs.forEach { biblioRef ->
                    DropdownMenuItem(
                            onClick = {
                                onBiblioRefChange(biblioRef)
                                showBiblioDropdown = false
                            }
                    ) { Text("${biblioRef.firstAuthor} (${biblioRef.year})") }
                }
            }
        }
    }
}
