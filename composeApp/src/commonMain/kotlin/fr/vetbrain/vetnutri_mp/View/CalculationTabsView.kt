package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.BiblioRefViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.EquationViewModel

/**
 * Vue à onglets pour naviguer entre les équations et les références bibliographiques
 *
 * @param equationViewModel ViewModel pour les équations
 * @param biblioRefViewModel ViewModel pour les références bibliographiques
 * @param onNavigateBack Callback pour revenir à l'écran précédent
 * @param modifier Modifier à appliquer à la vue
 */
@Composable
fun CalculationTabsView(
        equationViewModel: EquationViewModel,
        biblioRefViewModel: BiblioRefViewModel,
        onNavigateBack: () -> Unit,
        modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    var selectedEquationId by remember { mutableStateOf<String?>(null) }
    var selectedBiblioRefId by remember { mutableStateOf<String?>(null) }
    var isCreatingBiblioRef by remember { mutableStateOf(false) }
    var isCreatingEquation by remember { mutableStateOf(false) }

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
                        Icon(imageVector = AppIcons.ArrowBack, contentDescription = "Retour")
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
        }

        when (selectedTab) {
            0 ->
                    EquationListView(
                            viewModel = equationViewModel,
                            onNavigateBack = {}, // Ne rien faire car on reste dans cette vue
                            onEditEquation = { selectedEquationId = it },
                            onCreateEquation = { isCreatingEquation = true }
                    )
            1 ->
                    BiblioRefListView(
                            viewModel = biblioRefViewModel,
                            onNavigateBack = {}, // Ne rien faire car on reste dans cette vue
                            onEditBiblioRef = { selectedBiblioRefId = it },
                            onCreateBiblioRef = { isCreatingBiblioRef = true }
                    )
        }
    }
}
