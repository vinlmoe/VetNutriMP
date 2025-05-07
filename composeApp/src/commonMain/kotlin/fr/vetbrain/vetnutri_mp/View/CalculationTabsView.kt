package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.BiblioRefViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.EquationViewModel
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
 * @param onViewReferenceEvTabs Callback pour voir une référence en mode onglets
 * @param modifier Modifier à appliquer à la vue
 */
@Composable
fun CalculationTabsView(
        equationViewModel: EquationViewModel,
        biblioRefViewModel: BiblioRefViewModel,
        referenceEvViewModel: ReferenceEvViewModel,
        onNavigateBack: () -> Unit,
        onEditReferenceEv: (String) -> Unit,
        onCreateReferenceEv: () -> Unit,
        onViewReferenceEvTabs: (String) -> Unit =
                onEditReferenceEv, // Par défaut, utilise onEditReferenceEv
        modifier: Modifier = Modifier
) {
        var selectedTab by remember { mutableStateOf(0) }
        var selectedEquationId by remember { mutableStateOf<String?>(null) }
        var selectedBiblioRefId by remember { mutableStateOf<String?>(null) }
        var isCreatingBiblioRef by remember { mutableStateOf(false) }
        var isCreatingEquation by remember { mutableStateOf(false) }
        var selectedReferenceEvId by remember { mutableStateOf<String?>(null) }

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

        Column(modifier = modifier.fillMaxSize()) {
                TopAppBar(
                        title = { Text("Données de calcul") },
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
                                onClick = { selectedTab = 0 },
                                text = { Text("Équations") }
                        )
                        Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = { Text("Références") }
                        )
                        Tab(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                text = { Text("Besoins") }
                        )
                }

                when (selectedTab) {
                        0 ->
                                EquationListView(
                                        viewModel = equationViewModel,
                                        onNavigateBack = {
                                        }, // Ne rien faire car on reste dans cette vue
                                        onEditEquation = { selectedEquationId = it },
                                        onCreateEquation = { isCreatingEquation = true }
                                )
                        1 ->
                                BiblioRefListView(
                                        viewModel = biblioRefViewModel,
                                        onNavigateBack = {
                                        }, // Ne rien faire car on reste dans cette vue
                                        onEditBiblioRef = { selectedBiblioRefId = it },
                                        onCreateBiblioRef = { isCreatingBiblioRef = true }
                                )
                        2 ->
                                NutrientRequirementView(
                                        viewModel = referenceEvViewModel,
                                        onNavigateBack = {
                                        }, // Ne rien faire car on reste dans cette vue
                                        onEditReference = onEditReferenceEv,
                                        onCreateReference = onCreateReferenceEv,
                                        onEditNutrients =
                                                onEditReferenceEv, // Pour éditer les besoins
                                        // nutritionnels
                                        onViewTabs =
                                                onViewReferenceEvTabs // Pour voir la référence par
                                        // onglets
                                        )
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
