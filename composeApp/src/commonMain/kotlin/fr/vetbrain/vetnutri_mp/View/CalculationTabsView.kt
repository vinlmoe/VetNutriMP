package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Repository.BiblioRefRepository
import fr.vetbrain.vetnutri_mp.Repository.ConseilRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseReferenceEvRepository
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.PlatformDispatcher
import fr.vetbrain.vetnutri_mp.ViewModel.BiblioRefViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.EquationViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.NewReferenceEvViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.ReferenceEvViewModel

/**
 * Vue à onglets pour naviguer entre les équations, les références bibliographiques et les besoins
 * nutritionnels
 *
 * @param equationViewModel ViewModel pour les équations
 * @param biblioRefViewModel ViewModel pour les références bibliographiques
 * @param referenceEvViewModel ViewModel pour les références évaluées
 * @param onNavigateBack Callback pour revenir à l'écran précédent
 * @param onEditReferenceEv Callback pour éditer une référence évaluée
 * @param onCreateReferenceEv Callback pour créer une nouvelle référence évaluée
 * @param selectedTab Index de l'onglet sélectionné (par défaut 0)
 * @param onTabChanged Callback appelé quand l'onglet change
 * @param modifier Modifier à appliquer à la vue
 * @param biblioRefRepository Repository pour les références bibliographiques
 * @param equationRepository Repository pour les équations
 * @param referenceEvRepository Repository pour les références évaluées
 * @param platformDispatcher Dispatcher pour les opérations asynchrones
 */
@Composable
fun CalculationTabsView(
        equationViewModel: EquationViewModel,
        biblioRefViewModel: BiblioRefViewModel,
        referenceEvViewModel: ReferenceEvViewModel,
        conseilRepository: ConseilRepository,
        onNavigateBack: () -> Unit,
        onEditReferenceEv: (String) -> Unit,
        onCreateReferenceEv: () -> Unit,
        onEditConseil: (String) -> Unit,
        onCreateConseil: () -> Unit,
        selectedTab: Int = 0,
        onTabChanged: (Int) -> Unit = {},
        modifier: Modifier = Modifier,
        isExamMode: Boolean = false,
        biblioRefRepository: BiblioRefRepository? = null,
        equationRepository: EquationRepository? = null,
        referenceEvRepository: DatabaseReferenceEvRepository? = null,
        platformDispatcher: PlatformDispatcher? = null
) {
        var selectedEquationId by remember { mutableStateOf<String?>(null) }
        var selectedBiblioRefId by remember { mutableStateOf<String?>(null) }
        var isCreatingBiblioRef by remember { mutableStateOf(false) }
        var isCreatingEquation by remember { mutableStateOf(false) }
        var selectedReferenceEvId by remember { mutableStateOf<String?>(null) }
        var isEditingReferenceEv by remember { mutableStateOf(false) }

        // Gérer l'édition d'une équation existante
        if (selectedEquationId != null) {
                EquationEditView(
                        viewModel = equationViewModel,
                        equationId = selectedEquationId,
                        onNavigateBack = { selectedEquationId = null }
                )
                return
        }

        // Gérer la création d'une nouvelle équation
        if (isCreatingEquation) {
                EquationEditView(
                        viewModel = equationViewModel,
                        equationId = null,
                        onNavigateBack = { isCreatingEquation = false }
                )
                return
        }

        // Gérer l'édition d'une référence bibliographique existante
        if (selectedBiblioRefId != null) {
                BiblioRefEditView(
                        viewModel = biblioRefViewModel,
                        biblioRefId = selectedBiblioRefId,
                        onNavigateBack = { selectedBiblioRefId = null }
                )
                return
        }

        // Gérer la création d'une nouvelle référence bibliographique
        if (isCreatingBiblioRef) {
                BiblioRefEditView(
                        viewModel = biblioRefViewModel,
                        biblioRefId = null,
                        onNavigateBack = { isCreatingBiblioRef = false }
                )
                return
        }

        // Gérer l'édition d'une référence évaluée en interne
        if (isEditingReferenceEv &&
                        selectedReferenceEvId != null &&
                        biblioRefRepository != null &&
                        equationRepository != null &&
                        referenceEvRepository != null &&
                        platformDispatcher != null
        ) {
                NewReferenceEvEditView(
                        viewModel =
                                NewReferenceEvViewModel(
                                        repository = referenceEvRepository,
                                        equationRepository = equationRepository,
                                        biblioRefRepository = biblioRefRepository
                                ),
                        referenceId = selectedReferenceEvId,
                        onNavigateBack = {
                                isEditingReferenceEv = false
                                selectedReferenceEvId = null
                        },
                        modifier = Modifier.fillMaxSize()
                )
                return
        }

        Column(modifier = modifier.fillMaxSize()) {
                TopAppBar(
                        title = { Text("Gestion des données de calcul") },
                        navigationIcon = {
                                IconButton(onClick = onNavigateBack) {
                                        Icon(
                                                imageVector = AppIcons.ArrowBack,
                                                contentDescription = "Retour"
                                        )
                                }
                        },
                        backgroundColor = VetNutriColors.Primary,
                        contentColor = VetNutriColors.OnPrimary
                )

                TabRow(
                        selectedTabIndex = selectedTab,
                        backgroundColor = VetNutriColors.Primary,
                        contentColor = VetNutriColors.OnPrimary
                ) {
                        Tab(
                                selected = selectedTab == 0,
                                onClick = { onTabChanged(0) },
                                text = { Text("Équations") }
                        )
                        Tab(
                                selected = selectedTab == 1,
                                onClick = { onTabChanged(1) },
                                text = { Text("Références bibliographiques") }
                        )
                        Tab(
                                selected = selectedTab == 2,
                                onClick = { onTabChanged(2) },
                                text = { Text("Systèmes de calcul") }
                        )
                        Tab(
                                selected = selectedTab == 3,
                                onClick = { if (!isExamMode) onTabChanged(3) },
                                modifier = Modifier.alpha(if (isExamMode) 0.5f else 1f),
                                text = { Text("Conseils personnalisés") }
                        )
                }

                when (selectedTab) {
                        0 ->
                                EquationListView(
                                        viewModel = equationViewModel,
                                        onEditEquation = { selectedEquationId = it },
                                        onCreateEquation = { isCreatingEquation = true }
                                )
                        1 ->
                                BiblioRefListView(
                                        viewModel = biblioRefViewModel,
                                        onEditBiblioRef = { selectedBiblioRefId = it },
                                        onCreateBiblioRef = { isCreatingBiblioRef = true }
                                )
                        2 ->
                                NutrientRequirementView(
                                        viewModel = referenceEvViewModel,
                                        onEditReference = { referenceEvId ->
                                                selectedReferenceEvId = referenceEvId
                                                isEditingReferenceEv = true
                                        },
                                        onCreateReference = onCreateReferenceEv,
                                        onEditNutrients = { referenceEvId ->
                                                selectedReferenceEvId = referenceEvId
                                                isEditingReferenceEv = true
                                        }
                                )
                        3 ->
                                if (isExamMode) {
                                        Box(
                                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                                contentAlignment = Alignment.Center
                                        ) {
                                                Text("Conseils personnalisés indisponibles en mode examen.")
                                        }
                                } else {
                                        ConseilsPersonnalisesView(
                                                conseilRepository = conseilRepository,
                                                onNavigateBack = onNavigateBack,
                                                onEditConseil = onEditConseil,
                                                onCreateConseil = onCreateConseil
                                        )
                                }
                }
        }
}

/**
 * Vue temporaire pour afficher les besoins nutritionnels À développer ultérieurement avec un
 * ViewModel dédié
 */
@Composable
fun NutrientRequirementViewTemp() {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text("Fonctionnalité en développement", style = MaterialTheme.typography.h5)

                Text(
                        "Cette fonctionnalité permettra de gérer les besoins nutritionnels des animaux. Elle sera disponible dans une prochaine version.",
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)
                )
        }
}

sealed class Screen {
        object List : Screen()
        object Create : Screen()
        object Detail : Screen()
        object FoodList : Screen()
        object FoodEdit : Screen()
        object BiblioRefList : Screen()
        object BiblioRefEdit : Screen()
        object EquationList : Screen()
        object EquationEdit : Screen()
        object CalculationTabs : Screen()
        object ReferenceEvList : Screen()
        object ReferenceEvEdit : Screen()
}
