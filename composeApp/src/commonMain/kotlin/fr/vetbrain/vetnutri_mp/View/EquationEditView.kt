package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.DropdownField
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Enumer.EquationKind
import fr.vetbrain.vetnutri_mp.Enumer.EquationType
import fr.vetbrain.vetnutri_mp.Enumer.VariableKind
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Animal
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.ExpressionEvaluator
import fr.vetbrain.vetnutri_mp.ViewModel.EquationViewModel

/**
 * Vue pour éditer une équation avec onglets d'édition et de test
 *
 * @param viewModel ViewModel pour gérer les équations
 * @param equationId Identifiant de l'équation (null pour une nouvelle équation)
 * @param onNavigateBack Callback pour revenir à l'écran précédent
 * @param modifier Modifier à appliquer à la vue
 */
@Composable
fun EquationEditView(
        viewModel: EquationViewModel,
        equationId: String?,
        onNavigateBack: () -> Unit,
        modifier: Modifier = Modifier
) {
    // État du chargement
    val isLoading by viewModel.isLoading.collectAsState(initial = false)

    // Équation en cours
    val currentEquation by viewModel.currentEquation.collectAsState()

    // Message d'opération (succès/erreur)
    val operationMessage by viewModel.operationMessage.collectAsState()

    // État de succès de sauvegarde
    val saveSuccessful by viewModel.saveSuccessful.collectAsState()

    // État pour afficher l'alerte d'erreur
    var showErrorAlert by remember { mutableStateOf(false) }

    // État pour l'onglet sélectionné
    var selectedTabIndex by remember { mutableStateOf(0) }

    // Effet pour charger l'équation à l'initialisation si un ID est fourni
    LaunchedEffect(equationId) {
        viewModel.clearOperationMessage()

        if (equationId?.isEmpty() == true || equationId == null) {
            viewModel.createNewEquation()
        } else {
            viewModel.loadEquation(equationId)
        }
    }

    // Effet pour surveiller les messages d'opération
    LaunchedEffect(operationMessage, saveSuccessful) {
        val message = operationMessage

        if (message != null) {
            if (saveSuccessful) {
                // Naviguer directement sans afficher de dialogue
                onNavigateBack()
            } else if (message.isNotEmpty()) {
                showErrorAlert = true
            }
        }
    }

    // Titre dynamique basé sur l'opération (création ou édition)
    val title = if (equationId == null) "Nouvelle équation" else "Modifier l'équation"

    Scaffold(topBar = { TopBarSimple(title = title, onNavigateBack = onNavigateBack) }) {
            paddingValues ->
        Box(modifier = modifier.fillMaxSize().padding(paddingValues)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Onglets
                TabRow(
                        selectedTabIndex = selectedTabIndex,
                        backgroundColor = VetNutriColors.Surface,
                        contentColor = VetNutriColors.Primary
                ) {
                    Tab(
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 },
                            text = { Text("Édition") },
                            icon = { Icon(Icons.Default.Edit, contentDescription = null) }
                    )
                    Tab(
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 },
                            text = { Text("Test") },
                            icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) }
                    )
                }

                // Contenu des onglets
                when (selectedTabIndex) {
                    0 -> EquationEditTab(viewModel = viewModel, currentEquation = currentEquation)
                    1 -> EquationTestTab(viewModel = viewModel, currentEquation = currentEquation)
                }
            }

            // Indicateur de chargement
            if (isLoading) {
                CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = VetNutriColors.Primary
                )
            }

            // Alerte d'erreur
            if (showErrorAlert) {
                AlertDialog(
                        onDismissRequest = {
                            showErrorAlert = false
                            viewModel.clearOperationMessage()
                        },
                        title = { Text("Erreur") },
                        text = {
                            val message = operationMessage
                            Text(message ?: "")
                        },
                        confirmButton = {
                            Button(
                                    onClick = {
                                        showErrorAlert = false
                                        viewModel.clearOperationMessage()
                                    }
                            ) { Text("OK") }
                        }
                )
            }

            // Afficher l'erreur s'il y en a une
            val message = operationMessage
            if (message != null && message.isNotEmpty() && message.startsWith("Erreur")) {
                Snackbar(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        action = {
                            TextButton(onClick = { viewModel.clearOperationMessage() }) {
                                Text("Fermer")
                            }
                        }
                ) { Text(message) }
            }
        }
    }
}

/** Onglet d'édition de l'équation */
@Composable
private fun EquationEditTab(
        viewModel: EquationViewModel,
        currentEquation: fr.vetbrain.vetnutri_mp.Data.Equation
) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        // Nom de l'équation
        OutlinedTextField(
                value = currentEquation.name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text("Nom de l'équation") },
                modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        OutlinedTextField(
                value = currentEquation.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Type d'équation (dropdown)
        DropdownField(
                label = "Type d'équation",
                selectedValue =
                        currentEquation.kind.let { kind ->
                            when (kind) {
                                fr.vetbrain.vetnutri_mp.Enumer.EquationKind.ENERGYNEED ->
                                        EquationType.ENERGYNEED
                                fr.vetbrain.vetnutri_mp.Enumer.EquationKind.ENERGYDENSITY ->
                                        EquationType.ENERGYDENSITY
                                fr.vetbrain.vetnutri_mp.Enumer.EquationKind.MW -> EquationType.MW
                                fr.vetbrain.vetnutri_mp.Enumer.EquationKind.INDICATOR ->
                                        EquationType.INDICATOR
                                fr.vetbrain.vetnutri_mp.Enumer.EquationKind.NEED ->
                                        EquationType.NEED
                                fr.vetbrain.vetnutri_mp.Enumer.EquationKind
                                        .COMPLEMENTARY_NUTRIENT ->
                                        EquationType.COMPLEMENTARY_NUTRIENT
                            }
                        },
                options = EquationType.values().toList(),
                onValueChange = { viewModel.updateKind(it.toEquationKind()) },
                valueToString = { it.toString() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Espèce d'application (dropdown)
        DropdownField(
                label = Animal.SPECIES.translate(),
                selectedValue = currentEquation.specie ?: fr.vetbrain.vetnutri_mp.Enumer.Espece.CH,
                options = fr.vetbrain.vetnutri_mp.Enumer.Espece.entries,
                onValueChange = { viewModel.updateSpecie(it) },
                valueToString = { espece ->
                    when (espece) {
                        fr.vetbrain.vetnutri_mp.Enumer.Espece.CH -> "enum.Espece.ALL".translate()
                        else -> "${espece.translateEnum()} (${espece.name})"
                    }
                }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Sélection du nutriment (pour les types NEED et COMPLEMENTARY_NUTRIENT)
        if (viewModel.isNutrientRequired()) {
            val allNutrients = viewModel.getAllNutrients()
            val currentNutrient = currentEquation.nutrient

            // Trouver le nutriment correspondant dans la liste par label
            val selectedNutrient =
                    if (currentNutrient != null) {
                        allNutrients.find { it.label == currentNutrient.label }
                    } else null

            DropdownField(
                    label = "Nutriment associé",
                    selectedValue = selectedNutrient,
                    options = listOf(null) + allNutrients,
                    onValueChange = { viewModel.updateNutrient(it) },
                    valueToString = { nutrient ->
                        nutrient?.translateEnum() ?: "Aucun nutriment sélectionné"
                    }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Script de l'équation
        OutlinedTextField(
                value = currentEquation.equationScript,
                onValueChange = { newValue -> viewModel.updateEquationScript(newValue) },
                label = { Text("Script de l'équation") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Code, contentDescription = null) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Case à cocher: Equation de type ratio (utilise les nutriments de la ration)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                    checked = currentEquation.ratio,
                    onCheckedChange = { checked -> viewModel.updateRatio(checked) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Équation de type ratio (utiliser les nutriments de la ration)")
        }

        // Légende des codes couleur
        Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Variables reconnues depuis le ViewModel
            val recognizedVars by
                    viewModel.recognizedVariables.collectAsState(initial = emptyList())

            Surface(
                    shape = MaterialTheme.shapes.small,
                    color = VetNutriColors.Primary,
                    contentColor = VetNutriColors.OnPrimary,
                    modifier = Modifier.padding(4.dp)
            ) {
                Text(
                        "Variables reconnues : ${recognizedVars.joinToString(", ")}",
                        modifier = Modifier.padding(4.dp),
                        style = MaterialTheme.typography.caption
                )
            }
        }

        // Variables non reconnues
        val unrecognizedVars by
                viewModel.unrecognizedVariables.collectAsState(initial = emptyList())
        if (unrecognizedVars.isNotEmpty()) {
            Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                        shape = MaterialTheme.shapes.small,
                        color = Color.Red,
                        contentColor = Color.White,
                        modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                            "Variables non reconnues : ${unrecognizedVars.joinToString(", ")}",
                            modifier = Modifier.padding(4.dp),
                            style = MaterialTheme.typography.caption
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sélecteur de variables supplémentaires
        var expandedVariables by remember { mutableStateOf(false) }

        // Obtenir toutes les variables disponibles
        val allAvailableVariables = remember {
            val variableKindList =
                    VariableKind.entries.map {
                        "${it.variable} - ${it.translateEnum()}" to it.variable
                    }
            val nutrientsMain =
                    fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.entries.map {
                        "${it.translateEnum()} - ${it.nameToString()}" to it.label
                    }
            val nutrientsLipides =
                    fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid.entries.map {
                        "${it.translateEnum()} - ${it.label}" to it.label
                    }
            val nutrientsVitamines =
                    fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam.entries.map {
                        "${it.translateEnum()} - ${it.label}" to it.label
                    }
            val nutrientsMacro =
                    fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro.entries.map {
                        "${it.translateEnum()} - ${it.label}" to it.label
                    }
            val nutrientsMin =
                    fr.vetbrain.vetnutri_mp.Enumer.NutrientMin.entries.map {
                        "${it.translateEnum()} - ${it.label}" to it.label
                    }
            val nutrientsOther =
                    fr.vetbrain.vetnutri_mp.Enumer.NutrientOther.entries.map {
                        "${it.translateEnum()} - ${it.label}" to it.label
                    }
            val nutrientsAnalysis =
                    fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis.entries.map {
                        "${it.translateEnum()} - ${it.label}" to it.label
                    }
            val acideAmines =
                    fr.vetbrain.vetnutri_mp.Enumer.AAEnum.entries.map {
                        "${it.translateEnum()} - ${it.label}" to it.label
                    }
            val customNutrients =
                    fr.vetbrain.vetnutri_mp.Enumer.CustomNutrientRegistry.all().map {
                        "${it.nameToString()} - ${it.label}" to it.label
                    }

            // Variables système
            val systemVariables =
                    listOf(
                            "BW - Poids corporel" to "BW",
                            "BEE - Besoin énergétique de base" to "BEE",
                            "MW - Poids métabolique" to "MW",
                            "iBW - Poids corporel idéal" to "iBW",
                            "AW - Poids adulte" to "AW",
                            "L - Longueur" to "L",
                            "wG - Gain de poids" to "wG",
                            "wL - Perte de poids" to "wL"
                    )

            // Combiner toutes les variables et les trier par nom d'affichage
            (variableKindList +
                            nutrientsMain +
                            nutrientsLipides +
                            nutrientsVitamines +
                            nutrientsMacro +
                            nutrientsMin +
                            nutrientsOther +
                            acideAmines +
                            nutrientsAnalysis +
                            customNutrients +
                            systemVariables)
                    .sortedBy { it.first }
        }

        Box {
            OutlinedButton(
                    onClick = { expandedVariables = true },
                    modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = null)
                    Text("Ajouter une variable au script")
                }
            }
            DropdownMenu(
                    expanded = expandedVariables,
                    onDismissRequest = { expandedVariables = false }
            ) {
                allAvailableVariables.forEach { (displayName, variableCode) ->
                    DropdownMenuItem(
                            onClick = {
                                // Ajouter la variable directement dans le script
                                val currentScript = currentEquation.equationScript
                                val newScript =
                                        if (currentScript.isBlank()) {
                                            variableCode
                                        } else {
                                            "$currentScript + $variableCode"
                                        }
                                viewModel.updateEquationScript(newScript)
                                expandedVariables = false
                            }
                    ) { Text(displayName) }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Note bibliographique
        OutlinedTextField(
                value = currentEquation.bib.comments,
                onValueChange = { viewModel.updateBibNote(it) },
                label = { Text("Note bibliographique") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Référence bibliographique (sélecteur au lieu de champ libre)
        val biblioRefs by viewModel.biblioRefs.collectAsState()
        var expandedBiblioRefs by remember { mutableStateOf(false) }

        Box {
            OutlinedTextField(
                    value = currentEquation.bib.completeRef,
                    onValueChange = { /* Lecture seule, modification via le sélecteur uniquement */
                    },
                    label = { Text("Référence bibliographique") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { expandedBiblioRefs = true }) {
                            Icon(
                                    imageVector = Icons.Default.AddCircle,
                                    contentDescription = "Sélectionner une référence"
                            )
                        }
                    }
            )

            DropdownMenu(
                    expanded = expandedBiblioRefs,
                    onDismissRequest = { expandedBiblioRefs = false }
            ) {
                if (biblioRefs.isEmpty()) {
                    DropdownMenuItem(onClick = { expandedBiblioRefs = false }) {
                        Text("Aucune référence disponible")
                    }
                } else {
                    biblioRefs.forEach { biblioRef ->
                        DropdownMenuItem(
                                onClick = {
                                    viewModel.selectBiblioRef(biblioRef)
                                    expandedBiblioRefs = false
                                }
                        ) {
                            Text(
                                    "${biblioRef.firstAuthor} (${biblioRef.year}) - ${biblioRef.completeRef.take(30)}${if (biblioRef.completeRef.length > 30) "..." else ""}"
                            )
                        }
                    }
                }
            }
        }

        // Section multi-références (uniquement pour COMPLEMENTARY_NUTRIENT)
        if (currentEquation.kind == EquationKind.COMPLEMENTARY_NUTRIENT) {
            val allReferences by viewModel.allReferences.collectAsState()
            val equations by viewModel.equations.collectAsState()
            val isSaved = equations.any { it.uuid == currentEquation.uuid }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                    "Assigner aux références :",
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (!isSaved) {
                Text(
                        "Enregistrez d'abord l'équation pour l'assigner aux références.",
                        style = MaterialTheme.typography.caption,
                        color = Color.Gray
                )
            } else if (allReferences.isEmpty()) {
                Text(
                        "Aucune référence disponible.",
                        style = MaterialTheme.typography.caption,
                        color = Color.Gray
                )
            } else {
                allReferences.forEach { reference ->
                    val isAssociated = reference.equationsNut.any { it.uuid == currentEquation.uuid }
                    Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                    ) {
                        Checkbox(
                                checked = isAssociated,
                                onCheckedChange = {
                                    viewModel.toggleEquationForReference(currentEquation, reference)
                                }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(reference.nom)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Bouton d'enregistrement
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(
                    onClick = {
                        viewModel.saveCurrentEquation()
                        // Ne pas naviguer ici - la navigation se fera via LaunchedEffect si
                        // succès
                    },
                    modifier = Modifier.padding(8.dp)
            ) { Text("Enregistrer") }
        }
    }
}

/** Onglet de test de l'équation */
@Composable
private fun EquationTestTab(
        viewModel: EquationViewModel,
        currentEquation: fr.vetbrain.vetnutri_mp.Data.Equation
) {
    // État pour les valeurs des variables de test
    var testVariables by remember { mutableStateOf(mutableMapOf<String, String>()) }

    // État pour le résultat du test
    var testResult by remember { mutableStateOf<Double?>(null) }
    var testError by remember { mutableStateOf<String?>(null) }

    // Extraire les variables de l'expression courante en utilisant la même logique que le ViewModel
    val variablesInExpression =
            remember(currentEquation.equationScript) {
                if (currentEquation.equationScript.isNotBlank()) {
                    ExpressionEvaluator.extraireVariables(currentEquation.equationScript)
                } else {
                    emptyList()
                }
            }

    // Identifier les variables non reconnues en utilisant la même logique que le ViewModel
    val unrecognizedVariables =
            remember(variablesInExpression) {
                // Fonction helper pour vérifier si une variable est un nutriment (même logique que
                // le ViewModel)
                fun isNutrientVariable(variable: String): Boolean {
                    val nutrientsMain =
                            fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.entries.map { it.label }
                    val nutrientsLipides =
                            fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid.entries.map { it.label }
                    val nutrientsVitamines =
                            fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam.entries.map { it.label }
                    val nutrientsMacro =
                            fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro.entries.map { it.label }
                    val nutrientsMin =
                            fr.vetbrain.vetnutri_mp.Enumer.NutrientMin.entries.map { it.label }
                    val acideAmines = fr.vetbrain.vetnutri_mp.Enumer.AAEnum.entries.map { it.label }

                    return variable in
                            (nutrientsMain +
                                    nutrientsLipides +
                                    nutrientsVitamines +
                                    nutrientsMacro +
                                    nutrientsMin +
                                    acideAmines)
                }

                variablesInExpression.filter { variable ->
                    // Utiliser exactement la même logique que validerCoherenceEquation dans le
                    // ViewModel
                    // Vérifier si la variable est reconnue dans VariableKind
                    VariableKind.entries.none { it.variable == variable } &&
                            // Vérifier si c'est une variable système connue
                            variable !in setOf("BW", "BEE", "MW", "iBW", "AW", "L", "wG", "wL") &&
                            // Vérifier si c'est une variable de nutriment (utiliser la même logique
                            // que le ViewModel)
                            !isNutrientVariable(variable)
                }
            }

    // Debug
    LaunchedEffect(variablesInExpression, unrecognizedVariables) {}

    // Initialiser les valeurs par défaut pour les variables reconnues
    LaunchedEffect(variablesInExpression, unrecognizedVariables) {
        val newTestVariables = mutableMapOf<String, String>()
        variablesInExpression.forEach { variable ->
            if (variable !in unrecognizedVariables) {
                newTestVariables[variable] =
                        testVariables[variable]
                                ?: when (variable) {
                                    "BW" -> "25.0"
                                    "BEE" -> "400.0"
                                    "MW" -> "15.0"
                                    "iBW" -> "20.0"
                                    "AW" -> "30.0"
                                    "L" -> "6.0"
                                    "wG" -> "8.0"
                                    "wL" -> "4.0"
                                    "PB" -> "25.0" // Protéines brutes
                                    "MG" -> "10.0" // Matières grasses
                                    "ENA" -> "45.0" // Extractif non azoté
                                    else -> "10.0"
                                }
            }
        }
        testVariables = newTestVariables
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
                text = "Test de l'équation",
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(bottom = 16.dp)
        )

        if (currentEquation.equationScript.isBlank()) {
            // Message si aucune expression n'est définie
            Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0xFFFFF3E0), // Couleur ambre claire
                    elevation = 4.dp
            ) {
                Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                            text = "Aucune expression à tester",
                            style = MaterialTheme.typography.body1,
                            color = Color(0xFFE65100) // Couleur ambre foncée
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                            text = "Veuillez d'abord saisir une expression dans l'onglet Édition.",
                            style = MaterialTheme.typography.body2,
                            color = Color(0xFFE65100)
                    )
                }
            }
        } else if (currentEquation.equationScript.isNotBlank() && unrecognizedVariables.isNotEmpty()
        ) {
            // Message si des variables non reconnues sont présentes
            Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0xFFFFE0B2), // Couleur orange claire
                    elevation = 4.dp
            ) {
                Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                            text = "Test impossible",
                            style = MaterialTheme.typography.body1,
                            color = Color(0xFFE65100) // Couleur orange foncée
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                            text =
                                    "L'expression contient des variables non reconnues : ${unrecognizedVariables.joinToString(", ")}",
                            style = MaterialTheme.typography.body2,
                            color = Color(0xFFE65100)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                            text =
                                    "Veuillez corriger l'expression ou définir ces variables dans l'onglet Édition.",
                            style = MaterialTheme.typography.body2,
                            color = Color(0xFFE65100)
                    )
                }
            }
        } else if (variablesInExpression.isNotEmpty()) {
            // Interface de test avec variables reconnues
            Text(
                    text = "Expression : ${currentEquation.equationScript}",
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                    text = "Variables détectées :",
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier.padding(bottom = 8.dp)
            )

            // Tableau des variables
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(variablesInExpression.filter { it !in unrecognizedVariables }) { variable ->
                    Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            elevation = 2.dp
                    ) {
                        Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                    text = variable,
                                    style = MaterialTheme.typography.body1,
                                    modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                    value = testVariables[variable] ?: "",
                                    onValueChange = { newValue ->
                                        testVariables =
                                                testVariables.toMutableMap().apply {
                                                    this[variable] = newValue
                                                }
                                    },
                                    label = { Text("Valeur") },
                                    modifier = Modifier.width(120.dp),
                                    singleLine = true
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bouton de test
            Button(
                    onClick = {
                        try {
                            val variablesMap = mutableMapOf<String, Double>()
                            var hasError = false
                            var errorMessage = ""

                            // Convertir les valeurs string en double
                            testVariables.forEach { (variable, value) ->
                                val doubleValue = value.toDoubleOrNull()
                                if (doubleValue != null) {
                                    variablesMap[variable] = doubleValue
                                } else {
                                    hasError = true
                                    errorMessage = "Valeur invalide pour $variable: '$value'"
                                }
                            }

                            if (!hasError) {
                                val result =
                                        ExpressionEvaluator.evaluer(
                                                currentEquation.equationScript,
                                                variablesMap
                                        )
                                testResult = result
                                testError =
                                        if (result == null) {
                                            "Erreur lors de l'évaluation de l'expression"
                                        } else {
                                            null
                                        }
                            } else {
                                testResult = null
                                testError = errorMessage
                            }
                        } catch (e: Exception) {
                            testResult = null
                            testError = "Erreur: ${e.message}"
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
            ) { Text("Tester l'expression") }

            Spacer(modifier = Modifier.height(16.dp))

            // Affichage du résultat
            testResult?.let { result ->
                Card(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color(0xFFE8F5E8), // Vert clair
                        elevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                                text = "Résultat :",
                                style = MaterialTheme.typography.subtitle1,
                                color = Color(0xFF2E7D32) // Vert foncé
                        )
                        Text(
                                text = result.toString(),
                                style = MaterialTheme.typography.h6,
                                color = Color(0xFF2E7D32)
                        )
                    }
                }
            }

            // Affichage des erreurs
            testError?.let { error ->
                Card(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color(0xFFFFEBEE), // Rouge clair
                        elevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                                text = "Erreur :",
                                style = MaterialTheme.typography.subtitle1,
                                color = Color(0xFFC62828) // Rouge foncé
                        )
                        Text(
                                text = error,
                                style = MaterialTheme.typography.body2,
                                color = Color(0xFFC62828)
                        )
                    }
                }
            }
        } else {
            // Cas où l'expression ne contient aucune variable
            Text(
                    text = "Expression : ${currentEquation.equationScript}",
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                    text = "Cette expression ne contient aucune variable.",
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(bottom = 16.dp)
            )

            // Bouton de test pour expression sans variables
            Button(
                    onClick = {
                        try {
                            val result =
                                    ExpressionEvaluator.evaluer(
                                            currentEquation.equationScript,
                                            emptyMap()
                                    )
                            testResult = result
                            testError =
                                    if (result == null) {
                                        "Erreur lors de l'évaluation de l'expression"
                                    } else {
                                        null
                                    }
                        } catch (e: Exception) {
                            testResult = null
                            testError = "Erreur: ${e.message}"
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
            ) { Text("Tester l'expression") }

            Spacer(modifier = Modifier.height(16.dp))

            // Affichage du résultat
            testResult?.let { result ->
                Card(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color(0xFFE8F5E8), // Vert clair
                        elevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                                text = "Résultat :",
                                style = MaterialTheme.typography.subtitle1,
                                color = Color(0xFF2E7D32) // Vert foncé
                        )
                        Text(
                                text = result.toString(),
                                style = MaterialTheme.typography.h6,
                                color = Color(0xFF2E7D32)
                        )
                    }
                }
            }

            // Affichage des erreurs
            testError?.let { error ->
                Card(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color(0xFFFFEBEE), // Rouge clair
                        elevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                                text = "Erreur :",
                                style = MaterialTheme.typography.subtitle1,
                                color = Color(0xFFC62828) // Rouge foncé
                        )
                        Text(
                                text = error,
                                style = MaterialTheme.typography.body2,
                                color = Color(0xFFC62828)
                        )
                    }
                }
            }
        }
    }
}
