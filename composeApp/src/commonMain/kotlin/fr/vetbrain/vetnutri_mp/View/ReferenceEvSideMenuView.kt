package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Repository.BiblioRefRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseReferenceEvRepository
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.PlatformDispatcher
import fr.vetbrain.vetnutri_mp.ViewModel.EquationViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.NewReferenceEvViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.ReferenceEvViewModel

/** Élément de menu pour la navigation */
data class MenuItem(
        val title: String,
        val icon: String, // Texte représentant l'icône
        val id: Int
)

/**
 * Vue avec navigation pour gérer les différents aspects d'une référence nutritionnelle.
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
 * @param useSidebar Paramètre pour utiliser le menu latéral permanent
 * @param newReferenceEvViewModel ViewModel pour la nouvelle référence
 */
@Composable
fun ReferenceEvSideMenuView(
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
        useSidebar: Boolean = true,
        newReferenceEvViewModel: NewReferenceEvViewModel =
                NewReferenceEvViewModel(
                        repository = referenceEvRepository,
                        equationRepository = equationRepository
                )
) {
    val currentReferenceEv by
            referenceEvViewModel.currentReferenceEv.collectAsState(initial = ReferenceEv())

    // État pour suivre l'élément de menu sélectionné
    var selectedMenuItemId by remember { mutableStateOf(0) }

    // Définir les éléments du menu de navigation
    val menuItems =
            listOf(
                    MenuItem("Informations", "⓵", 0),
                    MenuItem("Équations", "⓶", 1),
                    MenuItem("Besoins", "⓷", 2)
            )

    // Charger la référence et les équations lorsque l'élément de menu change
    LaunchedEffect(selectedMenuItemId, referenceEvId) {
        println("DEBUG: Navigation - Élément de menu sélectionné: $selectedMenuItemId")

        // Toujours charger la référence
        referenceEvViewModel.loadReferenceEvById(referenceEvId)

        // Charger les équations si l'élément Équations est sélectionné
        if (selectedMenuItemId == 1) {
            println("DEBUG: Navigation - Chargement des équations pour l'élément Équations")
            equationViewModel.loadEquations()
            referenceEvViewModel.loadEquations()

            val vmEquations = equationViewModel.equations.value
            val availableEquations =
                    vmEquations.map { equation -> Pair(equation.uuid, equation.name) }
            println("DEBUG: Navigation - ${availableEquations.size} équations disponibles")
        }
    }

    @Composable
    fun SidebarMenu() {
        Column(
                modifier =
                        Modifier.width(300.dp)
                                .fillMaxHeight()
                                .background(Color.Yellow)
                                .padding(vertical = 16.dp)
        ) {
            Text(
                    "MENU DE NAVIGATION",
                    style = MaterialTheme.typography.h6,
                    color = Color.Red,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Divider()

            menuItems.forEach { item ->
                Row(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .clickable { selectedMenuItemId = item.id }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                            text = item.icon,
                            color = if (selectedMenuItemId == item.id) Color.Red else Color.Black,
                            style = MaterialTheme.typography.h5
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                            text = item.title,
                            style = MaterialTheme.typography.h6,
                            color = if (selectedMenuItemId == item.id) Color.Red else Color.Black
                    )
                }
            }
        }
    }

    @Composable
    fun ContentArea(innerPadding: PaddingValues) {
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (selectedMenuItemId) {
                0 -> {
                    // Section Informations
                    NewReferenceEvEditView(
                            viewModel = newReferenceEvViewModel,
                            referenceId = referenceEvId,
                            onNavigateBack = onNavigateBack,
                            modifier = Modifier.fillMaxSize()
                    )
                }
                1 -> {
                    // Section Équations
                    println("DEBUG: Navigation - Affichage de la section Équations")

                    // Afficher les équations disponibles et permettre leur sélection
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        Text(
                                "Gestion des équations pour la référence",
                                style = MaterialTheme.typography.h6,
                                modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Text(
                                "ID de la référence: $referenceEvId",
                                style = MaterialTheme.typography.body1,
                                modifier = Modifier.padding(bottom = 24.dp)
                        )

                        // Afficher les équations disponibles
                        LaunchedEffect(equationViewModel) {
                            equationViewModel.loadEquations()
                            referenceEvViewModel.loadEquations()
                            println("DEBUG: Navigation - Équations chargées")
                        }

                        val vmEquations = equationViewModel.equations.value
                        val availableEquations =
                                vmEquations.map { equation -> Pair(equation.uuid, equation.name) }

                        if (availableEquations.isNotEmpty()) {
                            Text(
                                    "${availableEquations.size} équations disponibles :",
                                    style = MaterialTheme.typography.subtitle1,
                                    modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // Liste des équations
                            Column {
                                availableEquations.forEach { pair ->
                                    val id = pair.first
                                    val nom = pair.second
                                    Row(
                                            modifier =
                                                    Modifier.fillMaxWidth()
                                                            .clickable { onEditEquation(id) }
                                                            .padding(vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                    ) { Text(nom, style = MaterialTheme.typography.body1) }
                                    Divider()
                                }
                            }
                        } else {
                            Text(
                                    "Aucune équation disponible actuellement",
                                    style = MaterialTheme.typography.body1,
                                    modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }

                        // Bouton pour créer une nouvelle équation
                        Button(
                                onClick = onCreateEquation,
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                        ) { Text("Créer une nouvelle équation") }
                    }
                }
                2 -> {
                    // Section Besoins nutritionnels
                    ReferenceEvNutrientView(
                            referenceEvViewModel = referenceEvViewModel,
                            biblioRefRepository = biblioRefRepository,
                            referenceEvRepository = referenceEvRepository,
                            platformDispatcher = platformDispatcher,
                            referenceEvId = referenceEvId,
                            onNavigateBack = onNavigateBack,
                            modifier = Modifier.fillMaxSize(),
                            isInTabView = true
                    )
                }
            }
        }
    }

    if (useSidebar) {
        // Pour les grands écrans - Interface avec menu latéral permanent
        Scaffold(
                topBar = {
                    if (!isIntegratedView) {
                        TopBarSimple(
                                title = "Référence : ${currentReferenceEv.nom}",
                                onNavigateBack = onNavigateBack
                        )
                    }
                }
        ) { scaffoldPadding ->
            Row(modifier = Modifier.fillMaxSize().padding(scaffoldPadding)) {
                // Menu latéral
                SidebarMenu()

                // Zone de contenu
                ContentArea(PaddingValues(0.dp))
            }
        }
    } else {
        // Pour les petits écrans - BottomNavigation
        Scaffold(
                topBar = {
                    if (!isIntegratedView) {
                        TopBarSimple(
                                title = "Référence : ${currentReferenceEv.nom}",
                                onNavigateBack = onNavigateBack
                        )
                    }
                },
                bottomBar = {
                    BottomNavigation(
                            backgroundColor = MaterialTheme.colors.surface,
                            contentColor = VetNutriColors.Primary
                    ) {
                        menuItems.forEach { item ->
                            BottomNavigationItem(
                                    icon = { Text(item.icon, style = MaterialTheme.typography.h6) },
                                    label = { Text(item.title) },
                                    selected = selectedMenuItemId == item.id,
                                    onClick = {
                                        println(
                                                "DEBUG: Navigation - Clic sur le menu ${item.title}"
                                        )
                                        selectedMenuItemId = item.id
                                    },
                                    selectedContentColor = VetNutriColors.Primary,
                                    unselectedContentColor = Color.Gray
                            )
                        }
                    }
                }
        ) { paddingValues -> ContentArea(paddingValues) }
    }
}
