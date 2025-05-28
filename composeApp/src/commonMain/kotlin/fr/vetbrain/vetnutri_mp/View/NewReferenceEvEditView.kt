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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.StadePhysio
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
        val tabs = listOf("Informations", "Nutriments", "Équations", "Coefficients")

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
                                        3 -> ReferenceEvCoefficientsTab(viewModel, currentReference)
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
                                Text(
                                        "Gestion des besoins nutritionnels",
                                        style = MaterialTheme.typography.h6,
                                        fontWeight = FontWeight.Bold
                                )

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
                                onDismiss = { selectedNutrient = null },
                                onSave = {
                                        min,
                                        max,
                                        optMin,
                                        optMax,
                                        unitMin,
                                        unitMax,
                                        unitOptMin,
                                        unitOptMax,
                                        biblioMin,
                                        biblioMax,
                                        biblioOptMin,
                                        biblioOptMax ->

                                        // Enregistrer les valeurs pour chaque niveau
                                        if (min != -1f) {
                                                viewModel.updateNutrientValue(
                                                        nutrient = nutrient,
                                                        value = min,
                                                        level =
                                                                fr.vetbrain.vetnutri_mp.Enumer
                                                                        .Reflevel.MIN,
                                                        unit = unitMin,
                                                        biblioRef = biblioMin
                                                )
                                        }

                                        if (max != -1f) {
                                                viewModel.updateNutrientValue(
                                                        nutrient = nutrient,
                                                        value = max,
                                                        level =
                                                                fr.vetbrain.vetnutri_mp.Enumer
                                                                        .Reflevel.MAX,
                                                        unit = unitMax,
                                                        biblioRef = biblioMax
                                                )
                                        }

                                        if (optMin != -1f) {
                                                viewModel.updateNutrientValue(
                                                        nutrient = nutrient,
                                                        value = optMin,
                                                        level =
                                                                fr.vetbrain.vetnutri_mp.Enumer
                                                                        .Reflevel.OPTIMIN,
                                                        unit = unitOptMin,
                                                        biblioRef = biblioOptMin
                                                )
                                        }

                                        if (optMax != -1f) {
                                                viewModel.updateNutrientValue(
                                                        nutrient = nutrient,
                                                        value = optMax,
                                                        level =
                                                                fr.vetbrain.vetnutri_mp.Enumer
                                                                        .Reflevel.OPTIMAX,
                                                        unit = unitOptMax,
                                                        biblioRef = biblioOptMax
                                                )
                                        }

                                        selectedNutrient = null
                                }
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
        LaunchedEffect(Unit) {
                println("DEBUG: ReferenceEvEquationsTab - Chargement des équations")
                viewModel.loadEquations()
        }

        // Débogage: Affichage des informations sur les équations disponibles
        LaunchedEffect(availableEquations) {
                println("DEBUG: Nombre d'équations disponibles: ${availableEquations.size}")
                println("DEBUG: Équations BW (MW): ${bwEquations.size}")
                println("DEBUG: Équations BEE (ENERGYNEED): ${beeEquations.size}")
                println(
                        "DEBUG: Équations Energy Density (ENERGYDENSITY): ${energyDensityEquations.size}"
                )
                availableEquations.forEachIndexed { index, equation ->
                        println(
                                "DEBUG: Équation $index - Nom: ${equation.name}, UUID: ${equation.uuid}, Kind: ${equation.kind}, Script: ${equation.equationScript}"
                        )
                }
        }

        Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
                Text(
                        text = "Sélection des équations",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                )

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
                                                                viewModel.setEquationBWSilently(
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
                                        text = "Équation pour le Besoin Énergétique de Base",
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
                                                                viewModel.setEquationBEESilently(
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
                                                onDismissRequest = { expandedDEcom = false },
                                                modifier = Modifier.fillMaxWidth(0.9f)
                                        ) {
                                                // Option "Aucune équation"
                                                DropdownMenuItem(
                                                        onClick = {
                                                                selectedEquationDEcom = null
                                                                viewModel.setEquationDEcomSilently(
                                                                        null
                                                                )
                                                                expandedDEcom = false
                                                        }
                                                ) { Text(text = "Aucune équation") }

                                                // Filtrer les équations de type ENERGYDENSITY
                                                energyDensityEquations.forEach { equation ->
                                                        DropdownMenuItem(
                                                                onClick = {
                                                                        selectedEquationDEcom =
                                                                                equation
                                                                        viewModel
                                                                                .setEquationDEcomSilently(
                                                                                        equation
                                                                                )
                                                                        expandedDEcom = false
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
                                                onDismissRequest = { expandedDEraw = false },
                                                modifier = Modifier.fillMaxWidth(0.9f)
                                        ) {
                                                // Option "Aucune équation"
                                                DropdownMenuItem(
                                                        onClick = {
                                                                selectedEquationDEraw = null
                                                                viewModel.setEquationDErawSilently(
                                                                        null
                                                                )
                                                                expandedDEraw = false
                                                        }
                                                ) { Text(text = "Aucune équation") }

                                                // Filtrer les équations de type ENERGYDENSITY
                                                energyDensityEquations.forEach { equation ->
                                                        DropdownMenuItem(
                                                                onClick = {
                                                                        selectedEquationDEraw =
                                                                                equation
                                                                        viewModel
                                                                                .setEquationDErawSilently(
                                                                                        equation
                                                                                )
                                                                        expandedDEraw = false
                                                                }
                                                        ) { Text(text = equation.name) }
                                                }
                                        }
                                }
                        }
                }

                // Bouton de sauvegarde des équations
                Button(
                        onClick = {
                                // Vérifier si les équations sélectionnées ne sont pas nulles avant
                                // de les
                                // sauvegarder
                                selectedEquationBW?.let { viewModel.setEquationBWSilently(it) }
                                selectedEquationBEE?.let { viewModel.setEquationBEESilently(it) }
                                selectedEquationDEcom?.let {
                                        viewModel.setEquationDEcomSilently(it)
                                }
                                selectedEquationDEraw?.let {
                                        viewModel.setEquationDErawSilently(it)
                                }
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors =
                                ButtonDefaults.buttonColors(
                                        backgroundColor = VetNutriColors.Primary
                                )
                ) {
                        Text(
                                "Enregistrer les équations",
                                color = Color.White,
                                modifier = Modifier.padding(8.dp)
                        )
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
        // Cette partie sera implémentée ultérieurement
        Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
                Text(text = "Gestion des coefficients", style = MaterialTheme.typography.h6)
                Text(
                        text = "Cette fonctionnalité sera implémentée prochainement",
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier.padding(top = 8.dp)
                )
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
        LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
                items(nutrients) { nutrient ->
                        NutrientCard(
                                nutrient = nutrient,
                                currentReference = currentReference,
                                onNutrientSelected = onNutrientSelected
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
 */
@Composable
fun <T : Nutrient> NutrientCard(
        nutrient: T,
        currentReference: ReferenceEv,
        onNutrientSelected: (Nutrient) -> Unit
) {
        // Vérifier si des valeurs sont définies pour ce nutriment
        val hasMin =
                currentReference.contientNutriment(
                        nutrient,
                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MIN
                )
        val hasMax =
                currentReference.contientNutriment(
                        nutrient,
                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MAX
                )
        val hasOptMin =
                currentReference.contientNutriment(
                        nutrient,
                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMIN
                )
        val hasOptMax =
                currentReference.contientNutriment(
                        nutrient,
                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMAX
                )

        // Récupérer les valeurs si elles existent
        val minValue =
                if (hasMin)
                        currentReference.obtenirNutriment(
                                nutrient,
                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MIN
                        )
                else null
        val maxValue =
                if (hasMax)
                        currentReference.obtenirNutriment(
                                nutrient,
                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MAX
                        )
                else null
        val optMinValue =
                if (hasOptMin)
                        currentReference.obtenirNutriment(
                                nutrient,
                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMIN
                        )
                else null
        val optMaxValue =
                if (hasOptMax)
                        currentReference.obtenirNutriment(
                                nutrient,
                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMAX
                        )
                else null

        // Récupérer les unités si elles existent
        val minUnit =
                if (hasMin)
                        fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.getById(
                                currentReference.obtenirUniteNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MIN
                                )
                        )
                else null
        val maxUnit =
                if (hasMax)
                        fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.getById(
                                currentReference.obtenirUniteNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MAX
                                )
                        )
                else null
        val optMinUnit =
                if (hasOptMin)
                        fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.getById(
                                currentReference.obtenirUniteNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMIN
                                )
                        )
                else null
        val optMaxUnit =
                if (hasOptMax)
                        fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum.getById(
                                currentReference.obtenirUniteNutriment(
                                        nutrient,
                                        fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMAX
                                )
                        )
                else null

        // Récupérer les bibliographies si elles existent
        val minBiblio =
                if (hasMin)
                        currentReference.obtenirBiblioNutriment(
                                nutrient,
                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MIN
                        )
                else null
        val maxBiblio =
                if (hasMax)
                        currentReference.obtenirBiblioNutriment(
                                nutrient,
                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.MAX
                        )
                else null
        val optMinBiblio =
                if (hasOptMin)
                        currentReference.obtenirBiblioNutriment(
                                nutrient,
                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMIN
                        )
                else null
        val optMaxBiblio =
                if (hasOptMax)
                        currentReference.obtenirBiblioNutriment(
                                nutrient,
                                fr.vetbrain.vetnutri_mp.Enumer.Reflevel.OPTIMAX
                        )
                else null

        Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
                Column(modifier = Modifier.padding(12.dp)) {
                        // En-tête avec le nom du nutriment et bouton d'édition
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Text(
                                        text = nutrient.label,
                                        style = MaterialTheme.typography.subtitle1,
                                        fontWeight = FontWeight.Bold
                                )

                                IconButton(onClick = { onNutrientSelected(nutrient) }) {
                                        Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Éditer",
                                                tint = VetNutriColors.Primary
                                        )
                                }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Afficher les valeurs avec leurs unités
                        if (hasMin || hasMax || hasOptMin || hasOptMax) {
                                // Si au moins une valeur est définie
                                Column(modifier = Modifier.fillMaxWidth()) {
                                        if (hasMin) {
                                                NutrientValueRow(
                                                        label = "Minimum",
                                                        value = minValue!!,
                                                        unit = minUnit?.label ?: "",
                                                        biblio = minBiblio?.toString() ?: "",
                                                        color = MaterialTheme.colors.primary
                                                )
                                        }
                                        if (hasOptMin) {
                                                NutrientValueRow(
                                                        label = "Optimum Min",
                                                        value = optMinValue!!,
                                                        unit = optMinUnit?.label ?: "",
                                                        biblio = optMinBiblio?.toString() ?: "",
                                                        color = MaterialTheme.colors.primary
                                                )
                                        }
                                        if (hasOptMax) {
                                                NutrientValueRow(
                                                        label = "Optimum Max",
                                                        value = optMaxValue!!,
                                                        unit = optMaxUnit?.label ?: "",
                                                        biblio = optMaxBiblio?.toString() ?: "",
                                                        color = MaterialTheme.colors.secondary
                                                )
                                        }
                                        if (hasMax) {
                                                NutrientValueRow(
                                                        label = "Maximum",
                                                        value = maxValue!!,
                                                        unit = maxUnit?.label ?: "",
                                                        biblio = maxBiblio?.toString() ?: "",
                                                        color = MaterialTheme.colors.secondary
                                                )
                                        }
                                }
                        } else {
                                // Si aucune valeur n'est définie
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Text(
                                                "Aucune valeur définie",
                                                style = MaterialTheme.typography.caption,
                                                color =
                                                        MaterialTheme.colors.onSurface.copy(
                                                                alpha = 0.6f
                                                        )
                                        )
                                }
                        }
                }
        }
}

/** Ligne affichant une valeur nutritionnelle avec son unité et sa référence bibliographique */
@Composable
fun NutrientValueRow(label: String, value: Float, unit: String, biblio: String, color: Color) {
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

                // Valeur avec unité
                Text(
                        text = "$value $unit",
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
 * @param onDismiss Callback appelé lorsque la boîte de dialogue est fermée
 * @param onSave Callback appelé lorsque les modifications sont enregistrées
 */
@Composable
fun NutrientEditDialog(
        nutrient: Nutrient,
        currentReference: ReferenceEv,
        biblioRefs: List<BiblioRef>,
        onDismiss: () -> Unit,
        onSave:
                (
                        min: Float,
                        max: Float,
                        optMin: Float,
                        optMax: Float,
                        unitMin: fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum,
                        unitMax: fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum,
                        unitOptMin: fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum,
                        unitOptMax: fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum,
                        biblioMin: BiblioRef,
                        biblioMax: BiblioRef,
                        biblioOptMin: BiblioRef,
                        biblioOptMax: BiblioRef) -> Unit
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

        // États pour les unités
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

        // États d'expansion des menus déroulants pour les unités
        var unitMinExpanded by remember { mutableStateOf(false) }
        var unitMaxExpanded by remember { mutableStateOf(false) }
        var unitOptMinExpanded by remember { mutableStateOf(false) }
        var unitOptMaxExpanded by remember { mutableStateOf(false) }

        // États d'expansion des menus déroulants pour les références bibliographiques
        var biblioMinExpanded by remember { mutableStateOf(false) }
        var biblioMaxExpanded by remember { mutableStateOf(false) }
        var biblioOptMinExpanded by remember { mutableStateOf(false) }
        var biblioOptMaxExpanded by remember { mutableStateOf(false) }

        AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(text = "Édition de ${getNutrientDisplayNameLocal(nutrient)}") },
                text = {
                        Column(
                                modifier = Modifier.verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                                // Minimum
                                Card(elevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                                Text(
                                                        "Minimum",
                                                        style = MaterialTheme.typography.subtitle1,
                                                        fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))

                                                OutlinedTextField(
                                                        value = minValue,
                                                        onValueChange = { minValue = it },
                                                        label = { Text("Valeur") },
                                                        keyboardOptions =
                                                                KeyboardOptions(
                                                                        keyboardType =
                                                                                KeyboardType.Decimal
                                                                ),
                                                        modifier = Modifier.fillMaxWidth()
                                                )

                                                Spacer(modifier = Modifier.height(8.dp))

                                                Text(
                                                        "Unité",
                                                        style = MaterialTheme.typography.caption
                                                )
                                                Box(modifier = Modifier.fillMaxWidth()) {
                                                        OutlinedButton(
                                                                onClick = {
                                                                        unitMinExpanded = true
                                                                },
                                                                modifier = Modifier.fillMaxWidth()
                                                        ) { Text(selectedUnitMin.label) }

                                                        DropdownMenu(
                                                                expanded = unitMinExpanded,
                                                                onDismissRequest = {
                                                                        unitMinExpanded = false
                                                                },
                                                                modifier =
                                                                        Modifier.fillMaxWidth(0.9f)
                                                        ) {
                                                                unitOptions.forEach { unit ->
                                                                        DropdownMenuItem(
                                                                                onClick = {
                                                                                        selectedUnitMin =
                                                                                                unit
                                                                                        unitMinExpanded =
                                                                                                false
                                                                                }
                                                                        ) { Text(unit.label) }
                                                                }
                                                        }
                                                }

                                                Spacer(modifier = Modifier.height(8.dp))

                                                Text(
                                                        "Référence bibliographique",
                                                        style = MaterialTheme.typography.caption
                                                )
                                                Box(modifier = Modifier.fillMaxWidth()) {
                                                        OutlinedButton(
                                                                onClick = {
                                                                        biblioMinExpanded = true
                                                                },
                                                                modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                                Text(
                                                                        if (selectedBiblioMin.uuid
                                                                                        .isNotEmpty()
                                                                        )
                                                                                "${selectedBiblioMin.firstAuthor} (${selectedBiblioMin.year})"
                                                                        else
                                                                                "Sélectionner une référence"
                                                                )
                                                        }

                                                        DropdownMenu(
                                                                expanded = biblioMinExpanded,
                                                                onDismissRequest = {
                                                                        biblioMinExpanded = false
                                                                },
                                                                modifier =
                                                                        Modifier.fillMaxWidth(0.9f)
                                                        ) {
                                                                biblioRefs.forEach { biblio ->
                                                                        DropdownMenuItem(
                                                                                onClick = {
                                                                                        selectedBiblioMin =
                                                                                                biblio
                                                                                        biblioMinExpanded =
                                                                                                false
                                                                                }
                                                                        ) {
                                                                                Text(
                                                                                        "${biblio.firstAuthor} (${biblio.year})"
                                                                                )
                                                                        }
                                                                }
                                                        }
                                                }
                                        }
                                }

                                // Optimum Minimum
                                Card(elevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                                Text(
                                                        "Optimum Minimum",
                                                        style = MaterialTheme.typography.subtitle1,
                                                        fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))

                                                OutlinedTextField(
                                                        value = optMinValue,
                                                        onValueChange = { optMinValue = it },
                                                        label = { Text("Valeur") },
                                                        keyboardOptions =
                                                                KeyboardOptions(
                                                                        keyboardType =
                                                                                KeyboardType.Decimal
                                                                ),
                                                        modifier = Modifier.fillMaxWidth()
                                                )

                                                Spacer(modifier = Modifier.height(8.dp))

                                                Text(
                                                        "Unité",
                                                        style = MaterialTheme.typography.caption
                                                )
                                                Box(modifier = Modifier.fillMaxWidth()) {
                                                        OutlinedButton(
                                                                onClick = {
                                                                        unitOptMinExpanded = true
                                                                },
                                                                modifier = Modifier.fillMaxWidth()
                                                        ) { Text(selectedUnitOptMin.label) }

                                                        DropdownMenu(
                                                                expanded = unitOptMinExpanded,
                                                                onDismissRequest = {
                                                                        unitOptMinExpanded = false
                                                                },
                                                                modifier =
                                                                        Modifier.fillMaxWidth(0.9f)
                                                        ) {
                                                                unitOptions.forEach { unit ->
                                                                        DropdownMenuItem(
                                                                                onClick = {
                                                                                        selectedUnitOptMin =
                                                                                                unit
                                                                                        unitOptMinExpanded =
                                                                                                false
                                                                                }
                                                                        ) { Text(unit.label) }
                                                                }
                                                        }
                                                }

                                                Spacer(modifier = Modifier.height(8.dp))

                                                Text(
                                                        "Référence bibliographique",
                                                        style = MaterialTheme.typography.caption
                                                )
                                                Box(modifier = Modifier.fillMaxWidth()) {
                                                        OutlinedButton(
                                                                onClick = {
                                                                        biblioOptMinExpanded = true
                                                                },
                                                                modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                                Text(
                                                                        if (selectedBiblioOptMin
                                                                                        .uuid
                                                                                        .isNotEmpty()
                                                                        )
                                                                                "${selectedBiblioOptMin.firstAuthor} (${selectedBiblioOptMin.year})"
                                                                        else
                                                                                "Sélectionner une référence"
                                                                )
                                                        }

                                                        DropdownMenu(
                                                                expanded = biblioOptMinExpanded,
                                                                onDismissRequest = {
                                                                        biblioOptMinExpanded = false
                                                                },
                                                                modifier =
                                                                        Modifier.fillMaxWidth(0.9f)
                                                        ) {
                                                                biblioRefs.forEach { biblio ->
                                                                        DropdownMenuItem(
                                                                                onClick = {
                                                                                        selectedBiblioOptMin =
                                                                                                biblio
                                                                                        biblioOptMinExpanded =
                                                                                                false
                                                                                }
                                                                        ) {
                                                                                Text(
                                                                                        "${biblio.firstAuthor} (${biblio.year})"
                                                                                )
                                                                        }
                                                                }
                                                        }
                                                }
                                        }
                                }

                                // Optimum Maximum
                                Card(elevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                                Text(
                                                        "Optimum Maximum",
                                                        style = MaterialTheme.typography.subtitle1,
                                                        fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))

                                                OutlinedTextField(
                                                        value = optMaxValue,
                                                        onValueChange = { optMaxValue = it },
                                                        label = { Text("Valeur") },
                                                        keyboardOptions =
                                                                KeyboardOptions(
                                                                        keyboardType =
                                                                                KeyboardType.Decimal
                                                                ),
                                                        modifier = Modifier.fillMaxWidth()
                                                )

                                                Spacer(modifier = Modifier.height(8.dp))

                                                Text(
                                                        "Unité",
                                                        style = MaterialTheme.typography.caption
                                                )
                                                Box(modifier = Modifier.fillMaxWidth()) {
                                                        OutlinedButton(
                                                                onClick = {
                                                                        unitOptMaxExpanded = true
                                                                },
                                                                modifier = Modifier.fillMaxWidth()
                                                        ) { Text(selectedUnitOptMax.label) }

                                                        DropdownMenu(
                                                                expanded = unitOptMaxExpanded,
                                                                onDismissRequest = {
                                                                        unitOptMaxExpanded = false
                                                                },
                                                                modifier =
                                                                        Modifier.fillMaxWidth(0.9f)
                                                        ) {
                                                                unitOptions.forEach { unit ->
                                                                        DropdownMenuItem(
                                                                                onClick = {
                                                                                        selectedUnitOptMax =
                                                                                                unit
                                                                                        unitOptMaxExpanded =
                                                                                                false
                                                                                }
                                                                        ) { Text(unit.label) }
                                                                }
                                                        }
                                                }

                                                Spacer(modifier = Modifier.height(8.dp))

                                                Text(
                                                        "Référence bibliographique",
                                                        style = MaterialTheme.typography.caption
                                                )
                                                Box(modifier = Modifier.fillMaxWidth()) {
                                                        OutlinedButton(
                                                                onClick = {
                                                                        biblioOptMaxExpanded = true
                                                                },
                                                                modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                                Text(
                                                                        if (selectedBiblioOptMax
                                                                                        .uuid
                                                                                        .isNotEmpty()
                                                                        )
                                                                                "${selectedBiblioOptMax.firstAuthor} (${selectedBiblioOptMax.year})"
                                                                        else
                                                                                "Sélectionner une référence"
                                                                )
                                                        }

                                                        DropdownMenu(
                                                                expanded = biblioOptMaxExpanded,
                                                                onDismissRequest = {
                                                                        biblioOptMaxExpanded = false
                                                                },
                                                                modifier =
                                                                        Modifier.fillMaxWidth(0.9f)
                                                        ) {
                                                                biblioRefs.forEach { biblio ->
                                                                        DropdownMenuItem(
                                                                                onClick = {
                                                                                        selectedBiblioOptMax =
                                                                                                biblio
                                                                                        biblioOptMaxExpanded =
                                                                                                false
                                                                                }
                                                                        ) {
                                                                                Text(
                                                                                        "${biblio.firstAuthor} (${biblio.year})"
                                                                                )
                                                                        }
                                                                }
                                                        }
                                                }
                                        }
                                }

                                // Maximum
                                Card(elevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                                Text(
                                                        "Maximum",
                                                        style = MaterialTheme.typography.subtitle1,
                                                        fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))

                                                OutlinedTextField(
                                                        value = maxValue,
                                                        onValueChange = { maxValue = it },
                                                        label = { Text("Valeur") },
                                                        keyboardOptions =
                                                                KeyboardOptions(
                                                                        keyboardType =
                                                                                KeyboardType.Decimal
                                                                ),
                                                        modifier = Modifier.fillMaxWidth()
                                                )

                                                Spacer(modifier = Modifier.height(8.dp))

                                                Text(
                                                        "Unité",
                                                        style = MaterialTheme.typography.caption
                                                )
                                                Box(modifier = Modifier.fillMaxWidth()) {
                                                        OutlinedButton(
                                                                onClick = {
                                                                        unitMaxExpanded = true
                                                                },
                                                                modifier = Modifier.fillMaxWidth()
                                                        ) { Text(selectedUnitMax.label) }

                                                        DropdownMenu(
                                                                expanded = unitMaxExpanded,
                                                                onDismissRequest = {
                                                                        unitMaxExpanded = false
                                                                },
                                                                modifier =
                                                                        Modifier.fillMaxWidth(0.9f)
                                                        ) {
                                                                unitOptions.forEach { unit ->
                                                                        DropdownMenuItem(
                                                                                onClick = {
                                                                                        selectedUnitMax =
                                                                                                unit
                                                                                        unitMaxExpanded =
                                                                                                false
                                                                                }
                                                                        ) { Text(unit.label) }
                                                                }
                                                        }
                                                }

                                                Spacer(modifier = Modifier.height(8.dp))

                                                Text(
                                                        "Référence bibliographique",
                                                        style = MaterialTheme.typography.caption
                                                )
                                                Box(modifier = Modifier.fillMaxWidth()) {
                                                        OutlinedButton(
                                                                onClick = {
                                                                        biblioMaxExpanded = true
                                                                },
                                                                modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                                Text(
                                                                        if (selectedBiblioMax.uuid
                                                                                        .isNotEmpty()
                                                                        )
                                                                                "${selectedBiblioMax.firstAuthor} (${selectedBiblioMax.year})"
                                                                        else
                                                                                "Sélectionner une référence"
                                                                )
                                                        }

                                                        DropdownMenu(
                                                                expanded = biblioMaxExpanded,
                                                                onDismissRequest = {
                                                                        biblioMaxExpanded = false
                                                                },
                                                                modifier =
                                                                        Modifier.fillMaxWidth(0.9f)
                                                        ) {
                                                                biblioRefs.forEach { biblio ->
                                                                        DropdownMenuItem(
                                                                                onClick = {
                                                                                        selectedBiblioMax =
                                                                                                biblio
                                                                                        biblioMaxExpanded =
                                                                                                false
                                                                                }
                                                                        ) {
                                                                                Text(
                                                                                        "${biblio.firstAuthor} (${biblio.year})"
                                                                                )
                                                                        }
                                                                }
                                                        }
                                                }
                                        }
                                }
                        }
                },
                confirmButton = {
                        Button(
                                onClick = {
                                        // Conversion des valeurs textuelles en float
                                        val min = minValue.toFloatOrNull() ?: -1f
                                        val max = maxValue.toFloatOrNull() ?: -1f
                                        val optMin = optMinValue.toFloatOrNull() ?: -1f
                                        val optMax = optMaxValue.toFloatOrNull() ?: -1f

                                        onSave(
                                                min,
                                                max,
                                                optMin,
                                                optMax,
                                                selectedUnitMin,
                                                selectedUnitMax,
                                                selectedUnitOptMin,
                                                selectedUnitOptMax,
                                                selectedBiblioMin,
                                                selectedBiblioMax,
                                                selectedBiblioOptMin,
                                                selectedBiblioOptMax
                                        )
                                }
                        ) { Text("Enregistrer") }
                },
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
