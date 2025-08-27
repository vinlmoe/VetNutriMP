package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.StadePhysio
import fr.vetbrain.vetnutri_mp.Enumer.UnitEnum
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.NewReferenceEvViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Vue pour l'édition des références nutritionnelles avec système d'onglets.
 *
 * @param viewModel ViewModel pour les opérations sur les références
 * @param referenceId Identifiant de la référence à éditer (null pour une nouvelle référence)
 * @param onNavigateBack Callback pour revenir à l'écran précédent
 * @param modifier Modifier à appliquer à la vue
 */
@Composable
fun NewReferenceEvEditView(
        viewModel: NewReferenceEvViewModel,
        referenceId: String?,
        onNavigateBack: () -> Unit,
        modifier: Modifier = Modifier
) {
        // État local pour suivre l'onglet sélectionné
        var selectedTabIndex by remember { mutableStateOf(0) }

        // Observer l'état de la référence en cours
        val currentReference by viewModel.currentReference.collectAsState()
        val isEditMode by viewModel.isEditMode.collectAsState()
        val errorMessage by viewModel.errorMessage.collectAsState()
        val operationSuccess by viewModel.operationSuccess.collectAsState()

        // Scope pour les coroutines
        val coroutineScope = rememberCoroutineScope()

        // Effet pour initialiser le ViewModel
        LaunchedEffect(referenceId) {
                if (referenceId.isNullOrEmpty()) {
                        viewModel.initForNew()
                } else {
                        viewModel.initForEdit(referenceId)
                }
        }

        // Effet pour traiter le succès de l'opération
        LaunchedEffect(operationSuccess) {
                if (operationSuccess) {
                        // Revenir à l'écran précédent après une sauvegarde réussie
                        onNavigateBack()
                        viewModel.resetOperationSuccess()
                }
        }

        // Définir les onglets
        val tabs =
                listOf("Informations", "Nutriments", "Équations", "Complémentaires", "Coefficients")

        Scaffold(
                topBar = {
                        TopBarSimple(
                                title =
                                        if (isEditMode) "Modifier la référence"
                                        else "Nouvelle référence",
                                onNavigateBack = {
                                        // Sauvegarde automatique avant navigation
                                        if (viewModel.hasUnsavedChanges()) {
                                                viewModel.saveReferenceSilently()
                                                // Attendre un court délai pour la sauvegarde puis
                                                // naviguer
                                                coroutineScope.launch {
                                                        delay(500) // Délai un peu plus long pour la
                                                        // sauvegarde
                                                        onNavigateBack()
                                                }
                                        } else {
                                                // Si pas de modifications, naviguer directement
                                                onNavigateBack()
                                        }
                                }
                        )
                }
        ) { paddingValues ->
                Column(modifier = modifier.fillMaxSize().padding(paddingValues)) {
                        // Onglets
                        TabRow(
                                selectedTabIndex = selectedTabIndex,
                                backgroundColor = MaterialTheme.colors.surface,
                                contentColor = VetNutriColors.Primary
                        ) {
                                tabs.forEachIndexed { index, title ->
                                        Tab(
                                                text = { Text(title) },
                                                selected = selectedTabIndex == index,
                                                onClick = { selectedTabIndex = index }
                                        )
                                }
                        }

                        // Afficher les messages d'erreur s'il y en a
                        errorMessage?.let { message ->
                                Text(
                                        text = message,
                                        color = MaterialTheme.colors.error,
                                        style = MaterialTheme.typography.caption,
                                        modifier =
                                                Modifier.padding(
                                                        horizontal = 16.dp,
                                                        vertical = 8.dp
                                                )
                                )
                        }

                        // Contenu de l'onglet sélectionné
                        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                when (selectedTabIndex) {
                                        0 -> ReferenceEvInfoTab(viewModel, currentReference)
                                        1 -> ReferenceEvNutrientsTab(viewModel, currentReference)
                                        2 -> ReferenceEvEquationsTab(viewModel)
                                        3 -> ReferenceEvComplementaryTab(viewModel)
                                        4 -> ReferenceEvCoefficientsTab(viewModel, currentReference)
                                }
                        }
                }
        }
}

/**
 * Onglet pour les informations générales de la référence.
 *
 * @param viewModel ViewModel pour les opérations sur les références
 * @param currentReference La référence actuelle
 */
@Composable
fun ReferenceEvInfoTab(viewModel: NewReferenceEvViewModel, currentReference: ReferenceEv) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // Champ pour le nom
                OutlinedTextField(
                        value = currentReference.nom,
                        onValueChange = { viewModel.updateReferenceProperty("nom", it) },
                        label = { Text("Nom de la référence") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                // Champ pour la description
                OutlinedTextField(
                        value = currentReference.description,
                        onValueChange = { viewModel.updateReferenceProperty("description", it) },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        minLines = 3
                )

                // Sélection de l'espèce
                Text(
                        text = "Espèce",
                        style = MaterialTheme.typography.subtitle1,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )

                val especeOptions = Espece.valuesExcept(Espece.CH)
                var especeExpanded by remember { mutableStateOf(false) }

                Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                                onClick = { especeExpanded = true },
                                modifier = Modifier.fillMaxWidth()
                        ) { Text(currentReference.espece.label) }

                        DropdownMenu(
                                expanded = especeExpanded,
                                onDismissRequest = { especeExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                                especeOptions.forEach { espece ->
                                        DropdownMenuItem(
                                                onClick = {
                                                        viewModel.updateReferenceProperty(
                                                                "espece",
                                                                espece
                                                        )
                                                        especeExpanded = false
                                                }
                                        ) { Text(espece.label) }
                                }
                        }
                }

                // Sélection du stade physiologique
                Text(
                        text = "Stade physiologique",
                        style = MaterialTheme.typography.subtitle1,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )

                val stadePhysioOptions = StadePhysio.values()
                var stadePhysioExpanded by remember { mutableStateOf(false) }

                Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                                onClick = { stadePhysioExpanded = true },
                                modifier = Modifier.fillMaxWidth()
                        ) { Text(currentReference.stadePhysio.label) }

                        DropdownMenu(
                                expanded = stadePhysioExpanded,
                                onDismissRequest = { stadePhysioExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                                stadePhysioOptions.forEach { stade ->
                                        DropdownMenuItem(
                                                onClick = {
                                                        viewModel.updateReferenceProperty(
                                                                "stadePhysio",
                                                                stade
                                                        )
                                                        stadePhysioExpanded = false
                                                }
                                        ) { Text(stade.label) }
                                }
                        }
                }

                // Checkbox pour maladie
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp)
                ) {
                        Checkbox(
                                checked = currentReference.maladie,
                                onCheckedChange = {
                                        viewModel.updateReferenceProperty("maladie", it)
                                }
                        )
                        Text(
                                text = "Est lié à une maladie",
                                modifier = Modifier.padding(start = 8.dp)
                        )
                }

                // Champ pour le nom de la maladie (visible uniquement si maladie est coché)
                if (currentReference.maladie) {
                        OutlinedTextField(
                                value = currentReference.nomMaladie,
                                onValueChange = {
                                        viewModel.updateReferenceProperty("nomMaladie", it)
                                },
                                label = { Text("Nom de la maladie") },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                }

                // Champ pour le nom de l'énergie
                OutlinedTextField(
                        value = currentReference.nomEnergie,
                        onValueChange = { viewModel.updateReferenceProperty("nomEnergie", it) },
                        label = { Text("Nom de l'énergie") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                // Slider pour la consistance
                Text(
                        text = "Consistance: ${currentReference.consistent}",
                        style = MaterialTheme.typography.subtitle1,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )

                Slider(
                        value = currentReference.consistent.toFloat(),
                        onValueChange = {
                                viewModel.updateReferenceProperty("consistent", it.toInt())
                        },
                        valueRange = 1f..10f,
                        steps = 9,
                        modifier = Modifier.fillMaxWidth()
                )

                // Bouton de sauvegarde
                Button(
                        onClick = { viewModel.saveReference() },
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                ) { Text("Enregistrer") }
        }
}

/**
 * Onglet pour la gestion des nutriments de la référence.
 *
 * @param viewModel ViewModel pour les opérations sur les références
 * @param currentReference La référence actuelle
 */
@Composable
fun ReferenceEvNutrientsTab(viewModel: NewReferenceEvViewModel, currentReference: ReferenceEv) {
        var selectedNutrientType by remember { mutableStateOf(0) }
        val biblioRefs by viewModel.availableBiblioRefs.collectAsState()

        // État pour suivre le nutriment sélectionné pour l'édition
        var selectedNutrient by remember { mutableStateOf<Nutrient?>(null) }

        // Effet pour charger les références bibliographiques au chargement
        LaunchedEffect(Unit) { viewModel.loadBiblioRefs() }

        Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
                // En-tête avec sélection du type de nutriment
                Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
                        Column(modifier = Modifier.padding(16.dp)) {
                                Spacer(modifier = Modifier.height(8.dp))

                                // Liste des types de nutriments disponibles
                                val nutrientTypes =
                                        listOf(
                                                "Macronutriments",
                                                "Minéraux principaux",
                                                "Oligo-éléments",
                                                "Vitamines",
                                                "Lipides",
                                                "Acides aminés",
                                                "Autres nutriments",
                                                "Analyses calculées"
                                        )

                                TabRow(
                                        selectedTabIndex = selectedNutrientType,
                                        backgroundColor = MaterialTheme.colors.surface,
                                        contentColor = VetNutriColors.Primary
                                ) {
                                        nutrientTypes.forEachIndexed { index, title ->
                                                Tab(
                                                        text = { Text(title) },
                                                        selected = selectedNutrientType == index,
                                                        onClick = {
                                                                selectedNutrientType = index
                                                                selectedNutrient =
                                                                        null // Réinitialiser le
                                                                // nutriment
                                                                // sélectionné lors du
                                                                // changement de type
                                                        }
                                                )
                                        }
                                }
                        }
                }

                // Contenu selon le type de nutriment sélectionné
                when (selectedNutrientType) {
                        0 ->
                                NutrientListView(
                                        nutrients =
                                                fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.entries
                                                        .toList(),
                                        currentReference = currentReference,
                                        biblioRefs = biblioRefs,
                                        onNutrientSelected = { selectedNutrient = it },
                                        viewModel = viewModel
                                )
                        1 ->
                                NutrientListView(
                                        nutrients =
                                                fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro.entries
                                                        .toList(),
                                        currentReference = currentReference,
                                        biblioRefs = biblioRefs,
                                        onNutrientSelected = { selectedNutrient = it },
                                        viewModel = viewModel
                                )
                        2 ->
                                NutrientListView(
                                        nutrients =
                                                fr.vetbrain.vetnutri_mp.Enumer.NutrientMin.entries
                                                        .toList(),
                                        currentReference = currentReference,
                                        biblioRefs = biblioRefs,
                                        onNutrientSelected = { selectedNutrient = it },
                                        viewModel = viewModel
                                )
                        3 ->
                                NutrientListView(
                                        nutrients =
                                                fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam.entries
                                                        .toList(),
                                        currentReference = currentReference,
                                        biblioRefs = biblioRefs,
                                        onNutrientSelected = { selectedNutrient = it },
                                        viewModel = viewModel
                                )
                        4 ->
                                NutrientListView(
                                        nutrients =
                                                fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid.entries
                                                        .toList(),
                                        currentReference = currentReference,
                                        biblioRefs = biblioRefs,
                                        onNutrientSelected = { selectedNutrient = it },
                                        viewModel = viewModel
                                )
                        5 ->
                                NutrientListView(
                                        nutrients =
                                                fr.vetbrain.vetnutri_mp.Enumer.AAEnum.entries
                                                        .toList(),
                                        currentReference = currentReference,
                                        biblioRefs = biblioRefs,
                                        onNutrientSelected = { selectedNutrient = it },
                                        viewModel = viewModel
                                )
                        6 ->
                                NutrientListView(
                                        nutrients =
                                                fr.vetbrain.vetnutri_mp.Enumer.NutrientOther.entries
                                                        .toList(),
                                        currentReference = currentReference,
                                        biblioRefs = biblioRefs,
                                        onNutrientSelected = { selectedNutrient = it },
                                        viewModel = viewModel
                                )
                        7 ->
                                NutrientListView(
                                        nutrients =
                                                fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis
                                                        .entries
                                                        .toList(),
                                        currentReference = currentReference,
                                        biblioRefs = biblioRefs,
                                        onNutrientSelected = { selectedNutrient = it },
                                        viewModel = viewModel
                                )
                }

                // Affichage de la fenêtre d'édition si un nutriment est sélectionné
                selectedNutrient?.let { nutrient ->
                        NutrientEditDialog(
                                nutrient = nutrient,
                                currentReference = currentReference,
                                biblioRefs = biblioRefs,
                                viewModel = viewModel,
                                onDismiss = { selectedNutrient = null }
                        )
                }
        }
}

/**
 * Onglet pour la gestion des équations de la référence.
 *
 * @param viewModel ViewModel pour les opérations sur les références
 */
@Composable
fun ReferenceEvEquationsTab(viewModel: NewReferenceEvViewModel) {
        val availableEquations by viewModel.availableEquations.collectAsState()
        val currentEquations by viewModel.currentEquations.collectAsState()

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
                println("DEBUG: Équations BW (MW): ${bwEquations.size}")
                println("DEBUG: Équations BEE (ENERGYNEED): ${beeEquations.size}")
                availableEquations.forEachIndexed { index, equation -> }
        }

        Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

                // Afficher un message d'information si la référence est pour maladie
                if (isForMaladie) {
                        Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = 2.dp,
                                backgroundColor = MaterialTheme.colors.secondary.copy(alpha = 0.1f)
                        ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                                text = "Référence pour maladie",
                                                style = MaterialTheme.typography.subtitle1,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colors.secondary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                                text =
                                                        "Les équations d'énergie, de densité énergétique et de poids métabolique ne sont pas applicables pour les références liées à une maladie.",
                                                style = MaterialTheme.typography.body2,
                                                color =
                                                        MaterialTheme.colors.onSurface.copy(
                                                                alpha = 0.7f
                                                        )
                                        )
                                }
                        }
                } else {
                        // Afficher les équations seulement si ce n'est pas pour maladie

                        // Équation Poids corporel (BW)
                        Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                                text = "Équation pour le Poids métabolique",
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
                                                                        ?: "Sélectionner une équation"
                                                        )
                                                }

                                                DropdownMenu(
                                                        expanded = expandedBW,
                                                        onDismissRequest = { expandedBW = false },
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
                                                        ) { Text(text = "Aucune équation") }

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
                                                text =
                                                        "Équation pour le Besoin Énergétique de Base",
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
                                                                        ?: "Sélectionner une équation"
                                                        )
                                                }

                                                DropdownMenu(
                                                        expanded = expandedBEE,
                                                        onDismissRequest = { expandedBEE = false },
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
                                                        ) { Text(text = "Aucune équation") }

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
                                                text =
                                                        "Équation pour l'Énergie Digestible des Aliments Composés",
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
                                                                        ?: "Sélectionner une équation"
                                                        )
                                                }

                                                DropdownMenu(
                                                        expanded = expandedDEcom,
                                                        onDismissRequest = {
                                                                expandedDEcom = false
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
                                                        ) { Text(text = "Aucune équation") }

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
                                                text =
                                                        "Équation pour l'Énergie Digestible des Aliments Bruts",
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
                                                                        ?: "Sélectionner une équation"
                                                        )
                                                }

                                                DropdownMenu(
                                                        expanded = expandedDEraw,
                                                        onDismissRequest = {
                                                                expandedDEraw = false
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
                                                        ) { Text(text = "Aucune équation") }

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
                        text = "Équations de nutriments complémentaires",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                )

                if (complementary.isEmpty()) {
                        Text(
                                text =
                                        "Aucune équation complémentaire disponible pour ${currentReference.espece.label}.",
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
                                                                                ?: "Toutes espèces"
                                                                Text(
                                                                        text =
                                                                                "Nutriment: $nutrientLabel · Espèce: $especeLabel",
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
                                                                        if (isSelected) "Retirer"
                                                                        else "Associer"
                                                                )
                                                        }
                                                }
                                        }
                                }
                        }
                }
        }
}

/**
 * Onglet pour la gestion des coefficients de la référence.
 *
 * @param viewModel ViewModel pour les opérations sur les références
 * @param currentReference La référence actuelle
 */
@Composable
fun ReferenceEvCoefficientsTab(viewModel: NewReferenceEvViewModel, currentReference: ReferenceEv) {
        // Vérifier si la référence est pour maladie
        val isForMaladie = currentReference.maladie

        // État pour suivre le groupe de coefficients sélectionné
        var selectedGroupIndex by remember { mutableStateOf(0) }

        // États pour les boîtes de dialogue
        var showAddCoefficientDialog by remember { mutableStateOf(false) }
        var showEditCoefficientDialog by remember { mutableStateOf(false) }
        var editingCoefficientIndex by remember { mutableStateOf(-1) }

        // Définir les groupes de coefficients
        val groupNames = listOf("Groupe K1", "Groupe K2", "Groupe K3", "Groupe K4", "Groupe K5")

        Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

                // Afficher un message d'information si la référence est pour maladie
                if (isForMaladie) {
                        Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = 2.dp,
                                backgroundColor = MaterialTheme.colors.secondary.copy(alpha = 0.1f)
                        ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                                text = "Référence pour maladie",
                                                style = MaterialTheme.typography.subtitle1,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colors.secondary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                                text =
                                                        "Les coefficients ne sont pas applicables pour les références liées à une maladie.",
                                                style = MaterialTheme.typography.body2,
                                                color =
                                                        MaterialTheme.colors.onSurface.copy(
                                                                alpha = 0.7f
                                                        )
                                        )
                                }
                        }
                } else {
                        // Afficher les coefficients seulement si ce n'est pas pour maladie

                        // Sélection du groupe de coefficients
                        Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                        Spacer(modifier = Modifier.height(8.dp))

                                        TabRow(
                                                selectedTabIndex = selectedGroupIndex,
                                                backgroundColor = MaterialTheme.colors.surface,
                                                contentColor = VetNutriColors.Primary
                                        ) {
                                                groupNames.forEachIndexed { index, title ->
                                                        Tab(
                                                                text = { Text(title) },
                                                                selected =
                                                                        selectedGroupIndex == index,
                                                                onClick = {
                                                                        selectedGroupIndex = index
                                                                }
                                                        )
                                                }
                                        }
                                }
                        }

                        // Gestion du groupe sélectionné
                        CoefficientGroupView(
                                viewModel = viewModel,
                                groupIndex = selectedGroupIndex,
                                onAddCoefficient = { showAddCoefficientDialog = true },
                                onEditCoefficient = { index ->
                                        editingCoefficientIndex = index
                                        showEditCoefficientDialog = true
                                }
                        )

                        // Boîte de dialogue pour ajouter un coefficient
                        if (showAddCoefficientDialog) {
                                AddCoefficientDialog(
                                        onDismiss = { showAddCoefficientDialog = false },
                                        onConfirm = { description, coef ->
                                                viewModel.addCoefficient(
                                                        selectedGroupIndex,
                                                        description,
                                                        coef
                                                )
                                                showAddCoefficientDialog = false
                                        }
                                )
                        }

                        // Boîte de dialogue pour éditer un coefficient
                        if (showEditCoefficientDialog && editingCoefficientIndex >= 0) {
                                val coefficients = viewModel.getCoefficientGroup(selectedGroupIndex)
                                if (editingCoefficientIndex < coefficients.size) {
                                        val coefficient = coefficients[editingCoefficientIndex]
                                        EditCoefficientDialog(
                                                initialDescription = coefficient.description
                                                                ?: "Normal",
                                                initialCoef = coefficient.coef ?: 1.0,
                                                onDismiss = {
                                                        showEditCoefficientDialog = false
                                                        editingCoefficientIndex = -1
                                                },
                                                onConfirm = { description, coef ->
                                                        viewModel.updateCoefficient(
                                                                selectedGroupIndex,
                                                                editingCoefficientIndex,
                                                                description,
                                                                coef
                                                        )
                                                        showEditCoefficientDialog = false
                                                        editingCoefficientIndex = -1
                                                },
                                                onDelete = {
                                                        viewModel.removeCoefficient(
                                                                selectedGroupIndex,
                                                                editingCoefficientIndex
                                                        )
                                                        showEditCoefficientDialog = false
                                                        editingCoefficientIndex = -1
                                                }
                                        )
                                }
                        }
                }
        }
}

/** Vue pour afficher une liste de nutriments avec leurs valeurs actuelles */
@Composable
fun <T : Nutrient> NutrientListView(
        nutrients: List<T>,
        currentReference: ReferenceEv,
        biblioRefs: List<BiblioRef>,
        onNutrientSelected: (Nutrient) -> Unit,
        viewModel: NewReferenceEvViewModel
) {
        LazyVerticalGrid(
                columns =
                        GridCells.Adaptive(
                                minSize = 280.dp
                        ), // Adaptation automatique avec largeur minimale de 280dp
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(4.dp)
        ) {
                items(nutrients.size) { index ->
                        val nutrient = nutrients[index]
                        NutrientCard(
                                nutrient = nutrient,
                                currentReference = currentReference,
                                onNutrientSelected = onNutrientSelected,
                                viewModel = viewModel
                        )
                }
        }
}

/**
 * Carte affichant les informations d'un nutriment et ses valeurs
 *
 * @param nutrient Le nutriment à afficher
 * @param currentReference La référence actuelle contenant les valeurs du nutriment
 * @param onNutrientSelected Callback appelé lorsque le nutriment est sélectionné pour édition
 * @param viewModel ViewModel pour écouter les mises à jour forcées
 */
@Composable
fun <T : Nutrient> NutrientCard(
        nutrient: T,
        currentReference: ReferenceEv,
        onNutrientSelected: (Nutrient) -> Unit,
        viewModel: NewReferenceEvViewModel
) {
        // Utiliser des variables mutableStateOf pour la réactivité immédiate
        var hasMin by remember { mutableStateOf(false) }
        var hasMax by remember { mutableStateOf(false) }
        var hasOptMin by remember { mutableStateOf(false) }
        var hasOptMax by remember { mutableStateOf(false) }

        var minValue by remember { mutableStateOf(0.0) }
        var maxValue by remember { mutableStateOf(0.0) }
        var optMinValue by remember { mutableStateOf(0.0) }
        var optMaxValue by remember { mutableStateOf(0.0) }

        var minUnit by remember {
                mutableStateOf<fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum?>(null)
        }
        var maxUnit by remember {
                mutableStateOf<fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum?>(null)
        }
        var optMinUnit by remember {
                mutableStateOf<fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum?>(null)
        }
        var optMaxUnit by remember {
                mutableStateOf<fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum?>(null)
        }

        var minUnitEnum by remember { mutableStateOf<UnitEnum?>(null) }
        var maxUnitEnum by remember { mutableStateOf<UnitEnum?>(null) }
        var optMinUnitEnum by remember { mutableStateOf<UnitEnum?>(null) }
        var optMaxUnitEnum by remember { mutableStateOf<UnitEnum?>(null) }

        var minBiblio by remember { mutableStateOf<fr.vetbrain.vetnutri_mp.Data.BiblioRef?>(null) }
        var maxBiblio by remember { mutableStateOf<fr.vetbrain.vetnutri_mp.Data.BiblioRef?>(null) }
        var optMinBiblio by remember {
                mutableStateOf<fr.vetbrain.vetnutri_mp.Data.BiblioRef?>(null)
        }
        var optMaxBiblio by remember {
                mutableStateOf<fr.vetbrain.vetnutri_mp.Data.BiblioRef?>(null)
        }

        // Observer le forceUpdate pour déclencher les mises à jour
        val forceUpdate by viewModel.forceUpdate.collectAsState()

        // Vérifier si le nutriment appartient à la famille 5 (pas d'unité de besoin)
        val shouldShowUnitReq = nutrient.ue.getIDFamily() != 5

        // Effet pour mettre à jour les valeurs quand la référence change OU quand forceUpdate
        // change
        LaunchedEffect(currentReference, forceUpdate) {
                // Vérifier si des valeurs sont définies pour ce nutriment
                hasMin =
                        currentReference.contientNutriment(
                                nutrient,
                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MIN
                        )
                hasMax =
                        currentReference.contientNutriment(
                                nutrient,
                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MAX
                        )
                hasOptMin =
                        currentReference.contientNutriment(
                                nutrient,
                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMIN
                        )
                hasOptMax =
                        currentReference.contientNutriment(
                                nutrient,
                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMAX
                        )

                // Récupérer les valeurs si elles existent
                minValue =
                        if (hasMin)
                                currentReference.obtenirNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MIN
                                )
                        else 0.0

                maxValue =
                        if (hasMax)
                                currentReference.obtenirNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MAX
                                )
                        else 0.0

                optMinValue =
                        if (hasOptMin)
                                currentReference.obtenirNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMIN
                                )
                        else 0.0

                optMaxValue =
                        if (hasOptMax)
                                currentReference.obtenirNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMAX
                                )
                        else 0.0

                // Récupérer les unités si elles existent
                minUnit =
                        if (hasMin)
                                fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.getById(
                                        currentReference.obtenirUniteNutriment(
                                                nutrient,
                                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MIN
                                        )
                                )
                        else null

                maxUnit =
                        if (hasMax)
                                fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.getById(
                                        currentReference.obtenirUniteNutriment(
                                                nutrient,
                                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MAX
                                        )
                                )
                        else null

                optMinUnit =
                        if (hasOptMin)
                                fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.getById(
                                        currentReference.obtenirUniteNutriment(
                                                nutrient,
                                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMIN
                                        )
                                )
                        else null

                optMaxUnit =
                        if (hasOptMax)
                                fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.getById(
                                        currentReference.obtenirUniteNutriment(
                                                nutrient,
                                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMAX
                                        )
                                )
                        else null

                // Récupérer les UnitEnum si elles existent
                minUnitEnum =
                        if (hasMin)
                                currentReference.obtenirUnitEnumNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MIN
                                )
                        else null

                maxUnitEnum =
                        if (hasMax)
                                currentReference.obtenirUnitEnumNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MAX
                                )
                        else null

                optMinUnitEnum =
                        if (hasOptMin)
                                currentReference.obtenirUnitEnumNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMIN
                                )
                        else null

                optMaxUnitEnum =
                        if (hasOptMax)
                                currentReference.obtenirUnitEnumNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMAX
                                )
                        else null

                // Récupérer les bibliographies si elles existent
                minBiblio =
                        if (hasMin)
                                currentReference.obtenirBiblioNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MIN
                                )
                        else null

                maxBiblio =
                        if (hasMax)
                                currentReference.obtenirBiblioNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MAX
                                )
                        else null

                optMinBiblio =
                        if (hasOptMin)
                                currentReference.obtenirBiblioNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMIN
                                )
                        else null

                optMaxBiblio =
                        if (hasOptMax)
                                currentReference.obtenirBiblioNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMAX
                                )
                        else null
        }

        Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp,
                backgroundColor = MaterialTheme.colors.surface,
                shape = RoundedCornerShape(12.dp)
        ) {
                Column(modifier = Modifier.padding(12.dp)) {
                        // En-tête avec le nom du nutriment et bouton d'édition
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Text(
                                        text = nutrient.label,
                                        style = MaterialTheme.typography.subtitle2,
                                        fontWeight = FontWeight.Bold,
                                        color = VetNutriColors.Primary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                )

                                IconButton(
                                        onClick = { onNutrientSelected(nutrient) },
                                        modifier = Modifier.size(32.dp)
                                ) {
                                        Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Éditer",
                                                tint = VetNutriColors.Primary,
                                                modifier = Modifier.size(18.dp)
                                        )
                                }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Afficher les valeurs avec leurs unités
                        if (hasMin || hasMax || hasOptMin || hasOptMax) {
                                // Si au moins une valeur est définie
                                Column(modifier = Modifier.fillMaxWidth()) {
                                        if (hasMin) {
                                                CompactNutrientValueRow(
                                                        label = "Min",
                                                        value = minValue,
                                                        unit = minUnit?.label ?: "",
                                                        unitEnum = minUnitEnum?.displayName ?: "",
                                                        color = MaterialTheme.colors.primary,
                                                        shouldShowUnitReq = shouldShowUnitReq,
                                                        biblio = minBiblio?.toString() ?: ""
                                                )
                                        }
                                        if (hasOptMin) {
                                                CompactNutrientValueRow(
                                                        label = "Opt-",
                                                        value = optMinValue,
                                                        unit = optMinUnit?.label ?: "",
                                                        unitEnum = optMinUnitEnum?.displayName
                                                                        ?: "",
                                                        color =
                                                                Color(
                                                                        0xFF4CAF50
                                                                ), // Vert pour optimum,
                                                        shouldShowUnitReq = shouldShowUnitReq,
                                                        biblio = optMinBiblio?.toString() ?: ""
                                                )
                                        }
                                        if (hasOptMax) {
                                                CompactNutrientValueRow(
                                                        label = "Opt+",
                                                        value = optMaxValue,
                                                        unit = optMaxUnit?.label ?: "",
                                                        unitEnum = optMaxUnitEnum?.displayName
                                                                        ?: "",
                                                        color =
                                                                Color(
                                                                        0xFF4CAF50
                                                                ), // Vert pour optimum,
                                                        shouldShowUnitReq = shouldShowUnitReq,
                                                        biblio = optMaxBiblio?.toString() ?: ""
                                                )
                                        }
                                        if (hasMax) {
                                                CompactNutrientValueRow(
                                                        label = "Max",
                                                        value = maxValue,
                                                        unit = maxUnit?.label ?: "",
                                                        unitEnum = maxUnitEnum?.displayName ?: "",
                                                        color = MaterialTheme.colors.secondary,
                                                        shouldShowUnitReq = shouldShowUnitReq,
                                                        biblio = maxBiblio?.toString() ?: ""
                                                )
                                        }
                                }
                        } else {
                                // Si aucune valeur n'est définie
                                Box(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                ) {
                                        Text(
                                                "Non défini",
                                                style = MaterialTheme.typography.caption,
                                                color =
                                                        MaterialTheme.colors.onSurface.copy(
                                                                alpha = 0.5f
                                                        )
                                        )
                                }
                        }
                }
        }
}

/** Ligne compacte affichant une valeur nutritionnelle pour les cartes */
@Composable
fun CompactNutrientValueRow(
        label: String,
        value: Double,
        unit: String,
        unitEnum: String,
        biblio: String,
        color: Color,
        shouldShowUnitReq: Boolean = true
) {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                // Première ligne : Label et valeur avec unités
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        // Étiquette du niveau avec couleur
                        Text(
                                text = label,
                                style = MaterialTheme.typography.caption,
                                fontWeight = FontWeight.Bold,
                                color = color,
                                modifier = Modifier.width(35.dp)
                        )

                        // Valeur avec unités
                        val displayText =
                                if (shouldShowUnitReq) "$value $unitEnum $unit"
                                else "$value $unitEnum"
                        Text(
                                text = displayText,
                                style = MaterialTheme.typography.caption,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                        )
                }

                // Deuxième ligne : Référence bibliographique (si disponible)
                if (biblio.isNotEmpty()) {
                        Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 1.dp),
                                horizontalArrangement = Arrangement.End
                        ) {
                                Text(
                                        text = "Réf: $biblio",
                                        style =
                                                MaterialTheme.typography.caption.copy(
                                                        fontSize =
                                                                MaterialTheme.typography
                                                                        .caption
                                                                        .fontSize * 0.85f
                                                ),
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.End,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                )
                        }
                }
        }
}

/** Ligne affichant une valeur nutritionnelle avec son unité et sa référence bibliographique */
@Composable
fun NutrientValueRow(
        label: String,
        value: Double,
        unit: String,
        unitEnum: String,
        biblio: String,
        color: Color,
        shouldShowUnitReq: Boolean = true
) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
                // Étiquette du niveau
                Text(
                        text = label,
                        style = MaterialTheme.typography.body2,
                        fontWeight = FontWeight.Medium,
                        color = color
                )

                // Valeur avec unité physique et optionnellement unité de besoin
                val displayText =
                        if (shouldShowUnitReq) "$value $unitEnum $unit" else "$value $unitEnum"
                Text(
                        text = displayText,
                        style = MaterialTheme.typography.body2,
                        fontWeight = FontWeight.Bold
                )

                // Référence bibliographique (truncate si trop longue)
                val shortBiblio = if (biblio.length > 15) biblio.take(12) + "..." else biblio
                Text(
                        text = shortBiblio,
                        style = MaterialTheme.typography.caption,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(80.dp)
                )
        }
}

/** Retourne le nom d'affichage d'un nutriment. */
private fun getNutrientDisplayNameLocal(nutrient: Nutrient): String {
        return nutrient.label
}

/**
 * Boîte de dialogue pour éditer les valeurs d'un nutriment
 *
 * @param nutrient Le nutriment à éditer
 * @param currentReference La référence en cours d'édition
 * @param biblioRefs Liste des références bibliographiques disponibles
 * @param viewModel ViewModel pour les opérations sur les nutriments
 * @param onDismiss Callback appelé lorsque la boîte de dialogue est fermée
 */
@Composable
fun NutrientEditDialog(
        nutrient: Nutrient,
        currentReference: ReferenceEv,
        biblioRefs: List<BiblioRef>,
        viewModel: NewReferenceEvViewModel,
        onDismiss: () -> Unit
) {
        // États pour les valeurs
        var minValue by remember {
                mutableStateOf(
                        if (currentReference.contientNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MIN
                                )
                        )
                                currentReference
                                        .obtenirNutriment(
                                                nutrient,
                                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MIN
                                        )
                                        .toString()
                        else ""
                )
        }
        var maxValue by remember {
                mutableStateOf(
                        if (currentReference.contientNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MAX
                                )
                        )
                                currentReference
                                        .obtenirNutriment(
                                                nutrient,
                                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MAX
                                        )
                                        .toString()
                        else ""
                )
        }
        var optMinValue by remember {
                mutableStateOf(
                        if (currentReference.contientNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMIN
                                )
                        )
                                currentReference
                                        .obtenirNutriment(
                                                nutrient,
                                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMIN
                                        )
                                        .toString()
                        else ""
                )
        }
        var optMaxValue by remember {
                mutableStateOf(
                        if (currentReference.contientNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMAX
                                )
                        )
                                currentReference
                                        .obtenirNutriment(
                                                nutrient,
                                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMAX
                                        )
                                        .toString()
                        else ""
                )
        }

        // États pour les unités UnitReqEnum
        val unitOptions = fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.values()

        var selectedUnitMin by remember {
                mutableStateOf(
                        if (currentReference.contientNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MIN
                                )
                        )
                                fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.getById(
                                        currentReference.obtenirUniteNutriment(
                                                nutrient,
                                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MIN
                                        )
                                )
                        else fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.PERKG
                )
        }

        var selectedUnitMax by remember {
                mutableStateOf(
                        if (currentReference.contientNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MAX
                                )
                        )
                                fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.getById(
                                        currentReference.obtenirUniteNutriment(
                                                nutrient,
                                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MAX
                                        )
                                )
                        else fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.PERKG
                )
        }

        var selectedUnitOptMin by remember {
                mutableStateOf(
                        if (currentReference.contientNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMIN
                                )
                        )
                                fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.getById(
                                        currentReference.obtenirUniteNutriment(
                                                nutrient,
                                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMIN
                                        )
                                )
                        else fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.PERKG
                )
        }

        var selectedUnitOptMax by remember {
                mutableStateOf(
                        if (currentReference.contientNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMAX
                                )
                        )
                                fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.getById(
                                        currentReference.obtenirUniteNutriment(
                                                nutrient,
                                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMAX
                                        )
                                )
                        else fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.PERKG
                )
        }

        // États pour les UnitEnum avec filtrage par idFamily
        val defaultUnitEnum = nutrient.ue
        val availableUnitEnums =
                UnitEnum.values().filter { it.getIDFamily() == defaultUnitEnum.getIDFamily() }

        // Vérifier si le nutriment appartient à la famille 5 (pas d'unité de besoin)
        val shouldShowUnitReq = defaultUnitEnum.getIDFamily() != 5

        var selectedUnitEnumMin by remember { mutableStateOf(defaultUnitEnum) }
        var selectedUnitEnumMax by remember { mutableStateOf(defaultUnitEnum) }
        var selectedUnitEnumOptMin by remember { mutableStateOf(defaultUnitEnum) }
        var selectedUnitEnumOptMax by remember { mutableStateOf(defaultUnitEnum) }

        // États pour les références bibliographiques
        var selectedBiblioMin by remember {
                mutableStateOf(
                        if (currentReference.contientNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MIN
                                )
                        )
                                currentReference.obtenirBiblioNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MIN
                                )
                        else if (biblioRefs.isNotEmpty()) biblioRefs[0] else BiblioRef()
                )
        }

        var selectedBiblioMax by remember {
                mutableStateOf(
                        if (currentReference.contientNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MAX
                                )
                        )
                                currentReference.obtenirBiblioNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MAX
                                )
                        else if (biblioRefs.isNotEmpty()) biblioRefs[0] else BiblioRef()
                )
        }

        var selectedBiblioOptMin by remember {
                mutableStateOf(
                        if (currentReference.contientNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMIN
                                )
                        )
                                currentReference.obtenirBiblioNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMIN
                                )
                        else if (biblioRefs.isNotEmpty()) biblioRefs[0] else BiblioRef()
                )
        }

        var selectedBiblioOptMax by remember {
                mutableStateOf(
                        if (currentReference.contientNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMAX
                                )
                        )
                                currentReference.obtenirBiblioNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMAX
                                )
                        else if (biblioRefs.isNotEmpty()) biblioRefs[0] else BiblioRef()
                )
        }

        AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(text = "Édition de ${getNutrientDisplayNameLocal(nutrient)}") },
                text = {
                        Column(
                                modifier = Modifier.width(800.dp).height(400.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                                // En-tête avec info sur les unités
                                Text(
                                        text =
                                                "Unités physiques disponibles: ${defaultUnitEnum.getIDFamily()}",
                                        style = MaterialTheme.typography.caption,
                                        color = MaterialTheme.colors.primary
                                )

                                // Layout en grille pour les 4 niveaux
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        // Minimum
                                        NutrientLevelRow(
                                                label = "Min",
                                                value = minValue,
                                                onValueChange = { minValue = it },
                                                selectedUnitEnum = selectedUnitEnumMin,
                                                onUnitEnumChange = { selectedUnitEnumMin = it },
                                                selectedUnitReq = selectedUnitMin,
                                                onUnitReqChange = { selectedUnitMin = it },
                                                selectedBiblio = selectedBiblioMin,
                                                onBiblioChange = { selectedBiblioMin = it },
                                                availableUnitEnums = availableUnitEnums,
                                                unitReqOptions = unitOptions,
                                                biblioRefs = biblioRefs,
                                                shouldShowUnitReq = shouldShowUnitReq
                                        )

                                        // Maximum
                                        NutrientLevelRow(
                                                label = "Max",
                                                value = maxValue,
                                                onValueChange = { maxValue = it },
                                                selectedUnitEnum = selectedUnitEnumMax,
                                                onUnitEnumChange = { selectedUnitEnumMax = it },
                                                selectedUnitReq = selectedUnitMax,
                                                onUnitReqChange = { selectedUnitMax = it },
                                                selectedBiblio = selectedBiblioMax,
                                                onBiblioChange = { selectedBiblioMax = it },
                                                availableUnitEnums = availableUnitEnums,
                                                unitReqOptions = unitOptions,
                                                biblioRefs = biblioRefs,
                                                shouldShowUnitReq = shouldShowUnitReq
                                        )

                                        // Optimum Min
                                        NutrientLevelRow(
                                                label = "Opt Min",
                                                value = optMinValue,
                                                onValueChange = { optMinValue = it },
                                                selectedUnitEnum = selectedUnitEnumOptMin,
                                                onUnitEnumChange = { selectedUnitEnumOptMin = it },
                                                selectedUnitReq = selectedUnitOptMin,
                                                onUnitReqChange = { selectedUnitOptMin = it },
                                                selectedBiblio = selectedBiblioOptMin,
                                                onBiblioChange = { selectedBiblioOptMin = it },
                                                availableUnitEnums = availableUnitEnums,
                                                unitReqOptions = unitOptions,
                                                biblioRefs = biblioRefs,
                                                shouldShowUnitReq = shouldShowUnitReq
                                        )

                                        // Optimum Max
                                        NutrientLevelRow(
                                                label = "Opt Max",
                                                value = optMaxValue,
                                                onValueChange = { optMaxValue = it },
                                                selectedUnitEnum = selectedUnitEnumOptMax,
                                                onUnitEnumChange = { selectedUnitEnumOptMax = it },
                                                selectedUnitReq = selectedUnitOptMax,
                                                onUnitReqChange = { selectedUnitOptMax = it },
                                                selectedBiblio = selectedBiblioOptMax,
                                                onBiblioChange = { selectedBiblioOptMax = it },
                                                availableUnitEnums = availableUnitEnums,
                                                unitReqOptions = unitOptions,
                                                biblioRefs = biblioRefs,
                                                shouldShowUnitReq = shouldShowUnitReq
                                        )
                                }
                        }
                },
                confirmButton = {
                        TextButton(
                                onClick = {
                                        // Traiter les valeurs pour chaque niveau
                                        // Si la valeur est vide ou null, supprimer la référence
                                        // Sinon, sauvegarder la nouvelle valeur

                                        // Minimum
                                        if (minValue.isBlank()) {
                                                viewModel.removeNutrientValue(
                                                        nutrient,
                                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MIN
                                                )
                                        } else {
                                                val minDouble = minValue.toDoubleOrNull()
                                                if (minDouble != null && minDouble >= 0) {
                                                        viewModel.updateNutrientValue(
                                                                nutrient = nutrient,
                                                                value = minDouble,
                                                                level =
                                                                        fr.vetbrain.vetnutri_mp
                                                                                .Enumer.Reflevel
                                                                                .MIN,
                                                                unit = selectedUnitMin,
                                                                biblioRef = selectedBiblioMin,
                                                                unitEnum = selectedUnitEnumMin
                                                        )
                                                }
                                        }

                                        // Maximum
                                        if (maxValue.isBlank()) {
                                                viewModel.removeNutrientValue(
                                                        nutrient,
                                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MAX
                                                )
                                        } else {
                                                val maxDouble = maxValue.toDoubleOrNull()
                                                if (maxDouble != null && maxDouble >= 0) {
                                                        viewModel.updateNutrientValue(
                                                                nutrient = nutrient,
                                                                value = maxDouble,
                                                                level =
                                                                        fr.vetbrain.vetnutri_mp
                                                                                .Enumer.Reflevel
                                                                                .MAX,
                                                                unit = selectedUnitMax,
                                                                biblioRef = selectedBiblioMax,
                                                                unitEnum = selectedUnitEnumMax
                                                        )
                                                }
                                        }

                                        // Optimum Min
                                        if (optMinValue.isBlank()) {
                                                viewModel.removeNutrientValue(
                                                        nutrient,
                                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel
                                                                .OPTIMIN
                                                )
                                        } else {
                                                val optMinDouble = optMinValue.toDoubleOrNull()
                                                if (optMinDouble != null && optMinDouble >= 0) {
                                                        viewModel.updateNutrientValue(
                                                                nutrient = nutrient,
                                                                value = optMinDouble,
                                                                level =
                                                                        fr.vetbrain.vetnutri_mp
                                                                                .Enumer.Reflevel
                                                                                .OPTIMIN,
                                                                unit = selectedUnitOptMin,
                                                                biblioRef = selectedBiblioOptMin,
                                                                unitEnum = selectedUnitEnumOptMin
                                                        )
                                                }
                                        }

                                        // Optimum Max
                                        if (optMaxValue.isBlank()) {
                                                viewModel.removeNutrientValue(
                                                        nutrient,
                                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel
                                                                .OPTIMAX
                                                )
                                        } else {
                                                val optMaxDouble = optMaxValue.toDoubleOrNull()
                                                if (optMaxDouble != null && optMaxDouble >= 0) {
                                                        viewModel.updateNutrientValue(
                                                                nutrient = nutrient,
                                                                value = optMaxDouble,
                                                                level =
                                                                        fr.vetbrain.vetnutri_mp
                                                                                .Enumer.Reflevel
                                                                                .OPTIMAX,
                                                                unit = selectedUnitOptMax,
                                                                biblioRef = selectedBiblioOptMax,
                                                                unitEnum = selectedUnitEnumOptMax
                                                        )
                                                }
                                        }

                                        onDismiss()
                                }
                        ) { Text("Sauvegarder") }
                },
                dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
        )
}

/** Composant pour une ligne compacte d'édition d'un niveau de nutriment */
@Composable
private fun NutrientLevelRow(
        label: String,
        value: String,
        onValueChange: (String) -> Unit,
        selectedUnitEnum: UnitEnum,
        onUnitEnumChange: (UnitEnum) -> Unit,
        selectedUnitReq: fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum,
        onUnitReqChange: (fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum) -> Unit,
        selectedBiblio: BiblioRef,
        onBiblioChange: (BiblioRef) -> Unit,
        availableUnitEnums: List<UnitEnum>,
        unitReqOptions: Array<fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum>,
        biblioRefs: List<BiblioRef>,
        shouldShowUnitReq: Boolean
) {
        var unitEnumExpanded by remember { mutableStateOf(false) }
        var unitReqExpanded by remember { mutableStateOf(false) }
        var biblioExpanded by remember { mutableStateOf(false) }

        Card(elevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        // Label
                        Text(
                                text = label,
                                style = MaterialTheme.typography.body2,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(60.dp)
                        )

                        // Champ valeur
                        OutlinedTextField(
                                value = value,
                                onValueChange = onValueChange,
                                keyboardOptions =
                                        KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.width(80.dp),
                                singleLine = true
                        )

                        // UnitEnum
                        Box(modifier = Modifier.width(80.dp)) {
                                OutlinedButton(
                                        onClick = { unitEnumExpanded = true },
                                        modifier = Modifier.fillMaxWidth()
                                ) {
                                        Text(
                                                selectedUnitEnum.displayName,
                                                style = MaterialTheme.typography.caption,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                        )
                                }

                                DropdownMenu(
                                        expanded = unitEnumExpanded,
                                        onDismissRequest = { unitEnumExpanded = false }
                                ) {
                                        availableUnitEnums.forEach { unitEnum ->
                                                DropdownMenuItem(
                                                        onClick = {
                                                                onUnitEnumChange(unitEnum)
                                                                unitEnumExpanded = false
                                                        }
                                                ) { Text(unitEnum.displayName) }
                                        }
                                }
                        }

                        // UnitReq (affiché seulement si shouldShowUnitReq est true)
                        if (shouldShowUnitReq) {
                                Box(modifier = Modifier.width(80.dp)) {
                                        OutlinedButton(
                                                onClick = { unitReqExpanded = true },
                                                modifier = Modifier.fillMaxWidth()
                                        ) {
                                                Text(
                                                        selectedUnitReq.label,
                                                        style = MaterialTheme.typography.caption,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                )
                                        }

                                        DropdownMenu(
                                                expanded = unitReqExpanded,
                                                onDismissRequest = { unitReqExpanded = false }
                                        ) {
                                                unitReqOptions.forEach { unit ->
                                                        DropdownMenuItem(
                                                                onClick = {
                                                                        onUnitReqChange(unit)
                                                                        unitReqExpanded = false
                                                                }
                                                        ) { Text(unit.label) }
                                                }
                                        }
                                }
                        }

                        // Biblio
                        Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                        onClick = { biblioExpanded = true },
                                        modifier = Modifier.fillMaxWidth()
                                ) {
                                        Text(
                                                if (selectedBiblio.uuid.isNotEmpty())
                                                        "${selectedBiblio.firstAuthor} (${selectedBiblio.year})"
                                                else "Biblio",
                                                style = MaterialTheme.typography.caption,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                        )
                                }

                                DropdownMenu(
                                        expanded = biblioExpanded,
                                        onDismissRequest = { biblioExpanded = false }
                                ) {
                                        biblioRefs.forEach { biblio ->
                                                DropdownMenuItem(
                                                        onClick = {
                                                                onBiblioChange(biblio)
                                                                biblioExpanded = false
                                                        }
                                                ) { Text("${biblio.firstAuthor} (${biblio.year})") }
                                        }
                                }
                        }
                }
        }
}

/**
 * Vue pour afficher et gérer un groupe de coefficients spécifique
 *
 * @param viewModel ViewModel pour les opérations sur les références
 * @param groupIndex Index du groupe de coefficients (0-4)
 * @param onAddCoefficient Callback pour ajouter un coefficient
 * @param onEditCoefficient Callback pour éditer un coefficient
 */
@Composable
fun CoefficientGroupView(
        viewModel: NewReferenceEvViewModel,
        groupIndex: Int,
        onAddCoefficient: () -> Unit,
        onEditCoefficient: (Int) -> Unit
) {
        // Observer directement les StateFlow des coefficients pour une réactivité immédiate
        val coefficients by viewModel.getCoefficientGroupStateFlow(groupIndex).collectAsState()
        val groupNames by viewModel.groupNames.collectAsState()

        // État local pour l'édition du nom du groupe
        var isEditingGroupName by remember { mutableStateOf(false) }
        var editingGroupName by remember { mutableStateOf("") }

        Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
                // En-tête avec nom du groupe éditable
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        if (isEditingGroupName) {
                                OutlinedTextField(
                                        value = editingGroupName,
                                        onValueChange = { editingGroupName = it },
                                        label = { Text("Nom du groupe") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions =
                                                KeyboardOptions(imeAction = ImeAction.Done),
                                        keyboardActions =
                                                KeyboardActions(
                                                        onDone = {
                                                                viewModel
                                                                        .updateCoefficientGroupName(
                                                                                groupIndex,
                                                                                editingGroupName
                                                                        )
                                                                isEditingGroupName = false
                                                        }
                                                )
                                )
                                TextButton(
                                        onClick = {
                                                viewModel.updateCoefficientGroupName(
                                                        groupIndex,
                                                        editingGroupName
                                                )
                                                isEditingGroupName = false
                                        }
                                ) { Text("Valider") }
                        } else {
                                val groupName =
                                        groupNames.getOrElse(groupIndex) {
                                                "Groupe ${groupIndex + 1}"
                                        }
                                Text(
                                        text = groupName.ifEmpty { "Groupe ${groupIndex + 1}" },
                                        style = MaterialTheme.typography.h6,
                                        modifier =
                                                Modifier.weight(1f).clickable {
                                                        editingGroupName = groupName
                                                        isEditingGroupName = true
                                                }
                                )
                                IconButton(
                                        onClick = {
                                                editingGroupName = groupName
                                                isEditingGroupName = true
                                        }
                                ) { Icon(Icons.Default.Edit, contentDescription = "Éditer le nom") }
                        }
                }

                // Bouton ajouter coefficient
                OutlinedButton(onClick = onAddCoefficient, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ajouter un coefficient")
                }

                // Liste des coefficients
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        itemsIndexed(coefficients) { index, coefficient ->
                                CoefficientCard(
                                        coefficient = coefficient,
                                        onEdit = { onEditCoefficient(index) },
                                        onDelete = {
                                                viewModel.removeCoefficient(groupIndex, index)
                                        }
                                )
                        }
                }
        }
}

/**
 * Carte affichant un coefficient individuel
 *
 * @param coefficient Le coefficient à afficher
 * @param onEdit Callback pour éditer le coefficient
 */
@Composable
fun CoefficientCard(
        coefficient: fr.vetbrain.vetnutri_mp.Data.CoefP,
        onEdit: () -> Unit,
        onDelete: () -> Unit
) {
        Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 2.dp,
                backgroundColor = MaterialTheme.colors.surface
        ) {
                Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        // Informations du coefficient
                        Column(modifier = Modifier.weight(1f)) {
                                Text(
                                        text = coefficient.description ?: "Sans nom",
                                        style = MaterialTheme.typography.subtitle2,
                                        fontWeight = FontWeight.Bold
                                )
                                Text(
                                        text = "Coefficient: ${coefficient.coef ?: 1.0}",
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                        }

                        // Bouton d'édition
                        IconButton(onClick = onEdit) {
                                Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Éditer",
                                        tint = VetNutriColors.Primary
                                )
                        }

                        // Bouton de suppression
                        IconButton(onClick = onDelete) {
                                Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Supprimer",
                                        tint = MaterialTheme.colors.error
                                )
                        }
                }
        }
}

/**
 * Boîte de dialogue pour ajouter un nouveau coefficient
 *
 * @param onDismiss Callback pour fermer la boîte de dialogue
 * @param onConfirm Callback pour confirmer l'ajout avec description et coefficient
 */
@Composable
fun AddCoefficientDialog(onDismiss: () -> Unit, onConfirm: (String, Double) -> Unit) {
        var description by remember { mutableStateOf("") }
        var coefText by remember { mutableStateOf("1.0") }
        var showError by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf("") }

        // Fonction de validation
        fun validateAndConfirm() {
                if (description.isBlank()) {
                        showError = true
                        errorMessage = "La description ne peut pas être vide"
                        return
                }

                val coef = coefText.toDoubleOrNull()
                if (coef == null) {
                        showError = true
                        errorMessage = "Le coefficient doit être un nombre valide"
                        return
                }

                showError = false
                onConfirm(description.trim(), coef)
        }

        AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(text = "Ajouter un coefficient") },
                text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                        value = description,
                                        onValueChange = {
                                                description = it
                                                showError = false
                                        },
                                        label = { Text("Description") },
                                        modifier = Modifier.fillMaxWidth(),
                                        isError = showError && description.isBlank()
                                )

                                OutlinedTextField(
                                        value = coefText,
                                        onValueChange = {
                                                coefText = it
                                                showError = false
                                        },
                                        label = { Text("Coefficient") },
                                        keyboardOptions =
                                                KeyboardOptions(
                                                        keyboardType = KeyboardType.Decimal
                                                ),
                                        modifier = Modifier.fillMaxWidth(),
                                        isError = showError && coefText.toDoubleOrNull() == null
                                )

                                if (showError) {
                                        Text(
                                                text = errorMessage,
                                                color = MaterialTheme.colors.error,
                                                style = MaterialTheme.typography.caption
                                        )
                                }
                        }
                },
                confirmButton = { Button(onClick = { validateAndConfirm() }) { Text("Ajouter") } },
                dismissButton = {
                        Button(
                                onClick = onDismiss,
                                colors =
                                        ButtonDefaults.buttonColors(
                                                backgroundColor = MaterialTheme.colors.surface
                                        )
                        ) { Text("Annuler") }
                }
        )
}

/**
 * Boîte de dialogue pour éditer ou supprimer un coefficient existant
 *
 * @param initialDescription Description initiale du coefficient
 * @param initialCoef Valeur initiale du coefficient
 * @param onDismiss Callback pour fermer la boîte de dialogue
 * @param onConfirm Callback pour confirmer les modifications
 * @param onDelete Callback pour supprimer le coefficient
 */
@Composable
fun EditCoefficientDialog(
        initialDescription: String,
        initialCoef: Double,
        onDismiss: () -> Unit,
        onConfirm: (String, Double) -> Unit,
        onDelete: () -> Unit
) {
        var description by remember { mutableStateOf(initialDescription) }
        var coefText by remember { mutableStateOf(initialCoef.toString()) }
        var showError by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf("") }
        var showDeleteConfirmation by remember { mutableStateOf(false) }

        // Fonction de validation
        fun validateAndConfirm() {
                if (description.isBlank()) {
                        showError = true
                        errorMessage = "La description ne peut pas être vide"
                        return
                }

                val coef = coefText.toDoubleOrNull()
                if (coef == null) {
                        showError = true
                        errorMessage = "Le coefficient doit être un nombre valide"
                        return
                }

                showError = false
                onConfirm(description.trim(), coef)
        }

        if (showDeleteConfirmation) {
                AlertDialog(
                        onDismissRequest = { showDeleteConfirmation = false },
                        title = { Text("Confirmer la suppression") },
                        text = { Text("Êtes-vous sûr de vouloir supprimer ce coefficient ?") },
                        confirmButton = {
                                Button(
                                        onClick = {
                                                showDeleteConfirmation = false
                                                onDelete()
                                        },
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor = MaterialTheme.colors.error
                                                )
                                ) { Text("Supprimer ce coefficient", color = Color.White) }
                        },
                        dismissButton = {
                                Button(onClick = { showDeleteConfirmation = false }) {
                                        Text("Annuler")
                                }
                        }
                )
        } else {
                AlertDialog(
                        onDismissRequest = onDismiss,
                        title = { Text(text = "Éditer le coefficient") },
                        text = {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                                value = description,
                                                onValueChange = {
                                                        description = it
                                                        showError = false
                                                },
                                                label = { Text("Description") },
                                                modifier = Modifier.fillMaxWidth(),
                                                isError = showError && description.isBlank()
                                        )

                                        OutlinedTextField(
                                                value = coefText,
                                                onValueChange = {
                                                        coefText = it
                                                        showError = false
                                                },
                                                label = { Text("Coefficient") },
                                                keyboardOptions =
                                                        KeyboardOptions(
                                                                keyboardType = KeyboardType.Decimal
                                                        ),
                                                modifier = Modifier.fillMaxWidth(),
                                                isError =
                                                        showError &&
                                                                coefText.toDoubleOrNull() == null
                                        )

                                        if (showError) {
                                                Text(
                                                        text = errorMessage,
                                                        color = MaterialTheme.colors.error,
                                                        style = MaterialTheme.typography.caption
                                                )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Bouton de suppression
                                        Button(
                                                onClick = { showDeleteConfirmation = true },
                                                colors =
                                                        ButtonDefaults.buttonColors(
                                                                backgroundColor =
                                                                        MaterialTheme.colors.error
                                                        ),
                                                modifier = Modifier.fillMaxWidth()
                                        ) { Text("Supprimer ce coefficient", color = Color.White) }
                                }
                        },
                        confirmButton = {
                                Button(onClick = { validateAndConfirm() }) { Text("Enregistrer") }
                        },
                        dismissButton = {
                                Button(
                                        onClick = onDismiss,
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor =
                                                                MaterialTheme.colors.surface
                                                )
                                ) { Text("Annuler") }
                        }
                )
        }
}
