package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Data.Equation
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Repository.BiblioRefRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseReferenceEvRepository
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.PlatformDispatcher
import fr.vetbrain.vetnutri_mp.ViewModel.EquationViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.NewReferenceEvViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.ReferenceEvViewModel

/**
 * Vue à onglets pour gérer les différents aspects d'une référence nutritionnelle.
 *
 * @param referenceEvViewModel ViewModel pour la référence
 * @param equationViewModel ViewModel pour les équations
 * @param biblioRefRepository Repository pour les références bibliographiques
 * @param equationRepository Repository pour les équations
 * @param referenceEvRepository Repository pour les références nutritionnelles
 * @param platformDispatcher Dispatcher pour la concurrence
 * @param referenceEvId Identifiant de la référence à éditer
 * @param onNavigateBack Callback pour revenir à la liste des références
 * @param onEditEquation Callback pour éditer une équation spécifique
 * @param onCreateEquation Callback pour créer une nouvelle équation
 * @param modifier Modifier à appliquer à la vue
 * @param isIntegratedView Indique si la vue est intégrée directement dans l'écran d'édition
 * @param newReferenceEvViewModel ViewModel pour la nouvelle référence
 */
@Composable
fun ReferenceEvTabsView(
        referenceEvViewModel: ReferenceEvViewModel,
        equationViewModel: EquationViewModel,
        biblioRefRepository: BiblioRefRepository,
        equationRepository: EquationRepository,
        referenceEvRepository: DatabaseReferenceEvRepository,
        platformDispatcher: PlatformDispatcher,
        referenceEvId: String,
        onNavigateBack: () -> Unit,
        onEditEquation: (String) -> Unit = {},
        onCreateEquation: () -> Unit = {},
        modifier: Modifier = Modifier,
        isIntegratedView: Boolean = false,
        newReferenceEvViewModel: NewReferenceEvViewModel =
                NewReferenceEvViewModel(referenceEvRepository, equationRepository)
) {
    val currentReferenceEv by
            referenceEvViewModel.currentReferenceEv.collectAsState(initial = ReferenceEv())

    // État pour suivre l'onglet sélectionné
    var selectedTabIndex by remember { mutableStateOf(0) }

    // Charger la référence et les équations lorsque l'onglet change
    LaunchedEffect(selectedTabIndex, referenceEvId) {

        // Toujours charger la référence
        referenceEvViewModel.loadReferenceEvById(referenceEvId)

        // Définir l'ID de référence courante dans le EquationViewModel
        equationViewModel.setCurrentReferenceId(referenceEvId)

        // Charger les équations si l'onglet Équations est sélectionné
        if (selectedTabIndex == 1) {
            equationViewModel.loadEquations()
            referenceEvViewModel.loadEquations()
        }
    }

    // Définir les onglets disponibles
    val tabs = listOf("Informations", "Équations", "Besoins nutritionnels")

    // Si la vue est intégrée, ne pas afficher de Scaffold ni de TopBar
    val content: @Composable (PaddingValues) -> Unit = { paddingValues ->
        Column(modifier = modifier.fillMaxSize().padding(paddingValues)) {
            // Barre d'onglets
            TabRow(
                    selectedTabIndex = selectedTabIndex,
                    backgroundColor = MaterialTheme.colors.surface,
                    contentColor = VetNutriColors.Primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                    )
                }
            }

            // Contenu de l'onglet sélectionné
            when (selectedTabIndex) {
                0 -> {
                    // Onglet Informations
                    NewReferenceEvEditView(
                            viewModel = newReferenceEvViewModel,
                            referenceId = referenceEvId,
                            onNavigateBack = onNavigateBack,
                            modifier = Modifier.fillMaxSize()
                    )
                }
                1 -> {
                    // Onglet Équations

                    // Nouveau composant pour les équations qui utilise EquationViewModel
                    PersonnalizedEquationView(
                            equationViewModel = equationViewModel,
                            modifier = Modifier.fillMaxSize()
                    )
                }
                2 -> {
                    // Onglet Besoins nutritionnels
                    ReferenceEvNutrientView(
                            referenceEvViewModel = referenceEvViewModel,
                            biblioRefRepository = biblioRefRepository,
                            referenceEvRepository = referenceEvRepository,
                            platformDispatcher = platformDispatcher,
                            referenceEvId = referenceEvId,
                            onNavigateBack = onNavigateBack,
                            modifier = Modifier.fillMaxSize(),
                            isInTabView = true // Indique que la vue est dans un système d'onglets
                    )
                }
            }
        }
    }

    // Si c'est une vue intégrée, retourner directement le contenu sans le Scaffold
    if (isIntegratedView) {
        content(PaddingValues())
    } else {
        // Sinon utiliser le Scaffold standard
        Scaffold(
                topBar = {
                    TopBarSimple(
                            title = "Référence : ${currentReferenceEv.nom}",
                            onNavigateBack = onNavigateBack
                    )
                }
        ) { paddingValues -> content(paddingValues) }
    }
}

/**
 * Composant pour afficher et sélectionner des équations personnalisées pour une référence
 *
 * @param equationViewModel ViewModel pour les équations
 * @param modifier Modifier à appliquer au composant
 */
@Composable
fun PersonnalizedEquationView(equationViewModel: EquationViewModel, modifier: Modifier = Modifier) {
    val equations = equationViewModel.equations.collectAsState(initial = emptyList()).value

    // Débogage pour vérifier si les équations sont chargées
    LaunchedEffect(Unit) {
        if (equations.isEmpty()) {
            equationViewModel.loadEquations()
        }
    }

    Column(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        // Poids corporel
        EquationDropdown(
                label = "Équation de poids corporel",
                equations = equations,
                selectedEquation =
                        equationViewModel.equationBW.collectAsState(initial = null).value,
                onEquationSelected = { equation ->
                    equationViewModel.setEquationBW(equation)
                }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Dépense énergétique basale
        EquationDropdown(
                label = "Équation de dépense énergétique basale",
                equations = equations,
                selectedEquation =
                        equationViewModel.equationBEE.collectAsState(initial = null).value,
                onEquationSelected = { equation ->
                    equationViewModel.setEquationBEE(equation)
                }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Énergie métabolisable
        EquationDropdown(
                label = "Équation d'énergie métabolisable",
                equations = equations,
                selectedEquation =
                        equationViewModel.equationME.collectAsState(initial = null).value,
                onEquationSelected = { equation ->
                    equationViewModel.setEquationME(equation)
                }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Énergie digestible commerciale
        EquationDropdown(
                label = "Équation d'énergie digestible commerciale",
                equations = equations,
                selectedEquation =
                        equationViewModel.equationDEcom.collectAsState(initial = null).value,
                onEquationSelected = { equation ->
                    equationViewModel.setEquationDEcom(equation)
                }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Énergie digestible brute
        EquationDropdown(
                label = "Équation d'énergie digestible brute",
                equations = equations,
                selectedEquation =
                        equationViewModel.equationDEraw.collectAsState(initial = null).value,
                onEquationSelected = { equation ->
                    equationViewModel.setEquationDEraw(equation)
                }
        )
    }
}

@Composable
fun EquationDropdown(
        label: String,
        equations: List<Equation>,
        selectedEquation: Equation?,
        onEquationSelected: (Equation?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val displayText = selectedEquation?.name ?: "Sélectionner une équation"
    
    // Debug temporaire pour DM sélectionné
    LaunchedEffect(selectedEquation) {
        if (selectedEquation != null && (selectedEquation.name.contains("DM") || selectedEquation.equationScript.contains("DM"))) {
            if (selectedEquation.nutrient != null) {
                val nutrient = selectedEquation.nutrient!!
            }
        }
    }

    // Débogage pour vérifier les équations disponibles
    LaunchedEffect(equations) {
        equations.forEachIndexed { index, equation ->
            // Debug temporaire pour DM
            if (equation.name.contains("DM") || equation.equationScript.contains("DM")) {
                if (equation.nutrient != null) {
                    val nutrient = equation.nutrient!!
                }
            }
        }
    }

    Column {
        Text(text = label, style = MaterialTheme.typography.body1)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(
                    text = displayText,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
            )
        }

        DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            // Option pour aucune équation
            DropdownMenuItem(
                    onClick = {
                        onEquationSelected(null)
                        expanded = false
                    }
            ) { Text("Aucune équation") }

            // Liste des équations disponibles
            equations.forEach { equation ->
                DropdownMenuItem(
                        onClick = {
                            onEquationSelected(equation)
                            expanded = false
                        }
                ) { Text(equation.name) }
            }
        }
    }
}
