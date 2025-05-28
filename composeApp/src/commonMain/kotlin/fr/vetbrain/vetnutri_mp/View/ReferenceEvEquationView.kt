package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import fr.vetbrain.vetnutri_mp.Components.ConfirmDialog
import fr.vetbrain.vetnutri_mp.Components.TopBarWithActions
import fr.vetbrain.vetnutri_mp.Data.Equation
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.EquationViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.ReferenceEvViewModel

/**
 * Écran de gestion des équations pour une référence nutritionnelle
 *
 * @param viewModel ViewModel pour les références nutritionnelles
 * @param equationViewModel ViewModel pour les équations
 * @param referenceId ID de la référence affichée
 * @param onNavigateBack Callback pour naviguer en arrière
 * @param onEditEquation Callback pour éditer une équation
 * @param onCreateEquation Callback pour créer une nouvelle équation
 * @param isInTabView Indique si la vue est affichée dans un TabView
 */
@Composable
fun ReferenceEvEquationView(
        viewModel: ReferenceEvViewModel,
        equationViewModel: EquationViewModel,
        referenceId: String,
        onNavigateBack: () -> Unit,
        onEditEquation: (String) -> Unit = {},
        onCreateEquation: () -> Unit = {},
        modifier: Modifier = Modifier,
        isInTabView: Boolean = false
) {
        println("DEBUG: Displaying ReferenceEvEquationView for reference ID: $referenceId")

        val currentReferenceEv by
                viewModel.currentReferenceEv.collectAsState(initial = ReferenceEv())
        val equations by equationViewModel.equations.collectAsState(initial = emptyList())
        val availableEquations by viewModel.availableEquations.collectAsState(initial = emptyList())
        val isLoading by viewModel.loading.collectAsState(initial = false)
        val equationLoading by equationViewModel.isLoading.collectAsState(initial = false)
        val scope = rememberCoroutineScope()

        println(
                "DEBUG: ReferenceEvEquationView - loadingReferenceEv: $isLoading, loadingEquations: $equationLoading"
        )
        println(
                "DEBUG: ReferenceEvEquationView - availableEquations from equationViewModel: ${equations.size}, from referenceEvViewModel: ${availableEquations.size}"
        )
        println(
                "DEBUG: ReferenceEvEquationView - associatedEquations: ${currentReferenceEv.equationsNut.size}"
        )
        println(
                "DEBUG: Current reference: ${currentReferenceEv.uuid}, name: ${currentReferenceEv.nom}"
        )

        // Combinons les deux sources d'équations pour avoir plus de chances d'en avoir
        val combinedEquations =
                remember(equations, availableEquations) {
                        println(
                                "DEBUG: Combining equations from both sources. EquationViewModel: ${equations.size}, ReferenceViewModel: ${availableEquations.size}"
                        )
                        (equations + availableEquations).distinctBy { it.uuid }
                }

        println("DEBUG: Combined equations count: ${combinedEquations.size}")

        // État pour la confirmation de suppression
        val (showDeleteConfirm, setShowDeleteConfirm) = remember { mutableStateOf(false) }
        // Équation à supprimer
        val (equationToDelete, setEquationToDelete) = remember { mutableStateOf<Equation?>(null) }

        // Charger les équations associées à la référence au chargement
        LaunchedEffect(referenceId) {
                println(
                        "DEBUG: LaunchedEffect triggered in ReferenceEvEquationView with referenceId: $referenceId"
                )
                println("DEBUG: Loading reference and equations...")
                viewModel.loadReferenceEvById(referenceId)
                equationViewModel.loadEquations()
                viewModel.loadEquations()
        }

        // Logs de débogage pour l'état de chargement et les données
        LaunchedEffect(isLoading, equationLoading, currentReferenceEv, equations) {
                println(
                        "DEBUG: Loading states - Reference: $isLoading, Equations: $equationLoading"
                )
                println(
                        "DEBUG: Current reference: ${currentReferenceEv.nom}, ID: ${currentReferenceEv.uuid}"
                )
                println("DEBUG: Available equations count: ${equations.size}")
                println(
                        "DEBUG: Associated nutritional equations count: ${currentReferenceEv.equationsNut.size}"
                )
                println(
                        "DEBUG: Current equations - BW: ${currentReferenceEv.equationBW?.uuid}, BEE: ${currentReferenceEv.equationBEE?.uuid}"
                )
                println(
                        "DEBUG: Current equations - DEcom: ${currentReferenceEv.equationDEcom?.uuid}, DEraw: ${currentReferenceEv.equationDEraw?.uuid}"
                )
        }

        // Nous utiliserons une approche plus directe lors de l'affichage
        println("DEBUG: Associated equation IDs count: ${currentReferenceEv.equationsNut.size}")
        currentReferenceEv.equationsNut.forEach { equation ->
                println("DEBUG: Associated equation: ${equation.name}, UUID: ${equation.uuid}")
        }

        // Définir le contenu principal
        val content: @Composable (PaddingValues) -> Unit = { paddingValues ->
                Box(
                        modifier =
                                modifier.fillMaxSize()
                                        .padding(paddingValues)
                                        .padding(horizontal = AppSizes.paddingMedium)
                ) {
                        if (isLoading || equationLoading) {
                                println("DEBUG: Showing loading indicator")
                                // Indicateur de chargement au centre
                                CircularProgressIndicator(
                                        modifier = Modifier.align(Alignment.Center),
                                        color = VetNutriColors.Primary
                                )
                        } else {
                                // Afficher l'interface que des équations soient disponibles ou non
                                println(
                                        "DEBUG: Rendering UI with ${combinedEquations.size} combined equations"
                                )
                                println(
                                        "DEBUG: Reference equations: BW=${currentReferenceEv.equationBW?.name}, BEE=${currentReferenceEv.equationBEE?.name}"
                                )
                                println(
                                        "DEBUG: Reference equations: DEcom=${currentReferenceEv.equationDEcom?.name}, DEraw=${currentReferenceEv.equationDEraw?.name}"
                                )
                                println(
                                        "DEBUG: Associated nutrition equations: ${currentReferenceEv.equationsNut.size}"
                                )

                                // Section des équations principales et liste des équations
                                // additionnelles
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                        // Message d'information si aucune équation n'est disponible
                                        if (combinedEquations.isEmpty()) {
                                                item {
                                                        Column(
                                                                modifier =
                                                                        Modifier.fillMaxWidth()
                                                                                .padding(
                                                                                        vertical =
                                                                                                AppSizes.paddingMedium
                                                                                ),
                                                                horizontalAlignment =
                                                                        Alignment.CenterHorizontally
                                                        ) {
                                                                Text(
                                                                        "Aucune équation disponible. Veuillez en créer avant de pouvoir les associer.",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .subtitle1,
                                                                        color =
                                                                                MaterialTheme.colors
                                                                                        .error
                                                                )
                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.height(
                                                                                        AppSizes.paddingSmall
                                                                                )
                                                                )
                                                                Button(onClick = onCreateEquation) {
                                                                        Icon(
                                                                                Icons.Default.Add,
                                                                                contentDescription =
                                                                                        "Ajouter"
                                                                        )
                                                                        Spacer(
                                                                                modifier =
                                                                                        Modifier.width(
                                                                                                AppSizes.paddingSmall
                                                                                        )
                                                                        )
                                                                        Text("Créer une équation")
                                                                }
                                                        }
                                                }
                                        }

                                        item {
                                                // Section des équations principales
                                                Card(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .padding(
                                                                                vertical =
                                                                                        AppSizes.paddingSmall
                                                                        ),
                                                        elevation = AppSizes.cardElevationNormal
                                                ) {
                                                        Column(
                                                                modifier =
                                                                        Modifier.fillMaxWidth()
                                                                                .padding(
                                                                                        AppSizes.paddingMedium
                                                                                )
                                                        ) {
                                                                Text(
                                                                        text =
                                                                                "Équations principales",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .h6,
                                                                        fontWeight = FontWeight.Bold
                                                                )

                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.height(
                                                                                        AppSizes.paddingMedium
                                                                                )
                                                                )

                                                                // Équation de poids corporel (BW)
                                                                EquationDropdown(
                                                                        label =
                                                                                "Équation BW (poids corporel)",
                                                                        selectedEquation =
                                                                                currentReferenceEv
                                                                                        .equationBW,
                                                                        availableEquations =
                                                                                combinedEquations,
                                                                        onEquationSelected = {
                                                                                equation ->
                                                                                println(
                                                                                        "DEBUG: Updating BW equation to: ${equation?.name}, ID: ${equation?.uuid}"
                                                                                )
                                                                                viewModel
                                                                                        .updateReferenceEquation(
                                                                                                "BW",
                                                                                                equation?.uuid
                                                                                        )
                                                                        },
                                                                        isLoading = isLoading
                                                                )

                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.height(
                                                                                        AppSizes.paddingSmall
                                                                                )
                                                                )

                                                                // Équation de besoin énergétique
                                                                // (BEE)
                                                                EquationDropdown(
                                                                        label =
                                                                                "Équation BEE (besoin énergétique)",
                                                                        selectedEquation =
                                                                                currentReferenceEv
                                                                                        .equationBEE,
                                                                        availableEquations =
                                                                                combinedEquations,
                                                                        onEquationSelected = {
                                                                                equation ->
                                                                                println(
                                                                                        "DEBUG: Updating BEE equation to: ${equation?.name}, ID: ${equation?.uuid}"
                                                                                )
                                                                                viewModel
                                                                                        .updateReferenceEquation(
                                                                                                "BEE",
                                                                                                equation?.uuid
                                                                                        )
                                                                        },
                                                                        isLoading = isLoading
                                                                )

                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.height(
                                                                                        AppSizes.paddingSmall
                                                                                )
                                                                )

                                                                // Équation de densité énergétique
                                                                // pour aliments commerciaux (DEcom)
                                                                EquationDropdown(
                                                                        label =
                                                                                "Équation DEcom (densité énergétique commerciale)",
                                                                        selectedEquation =
                                                                                currentReferenceEv
                                                                                        .equationDEcom,
                                                                        availableEquations =
                                                                                combinedEquations,
                                                                        onEquationSelected = {
                                                                                equation ->
                                                                                println(
                                                                                        "DEBUG: Updating DEcom equation to: ${equation?.name}, ID: ${equation?.uuid}"
                                                                                )
                                                                                viewModel
                                                                                        .updateReferenceEquation(
                                                                                                "DEcom",
                                                                                                equation?.uuid
                                                                                        )
                                                                        },
                                                                        isLoading = isLoading
                                                                )

                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.height(
                                                                                        AppSizes.paddingSmall
                                                                                )
                                                                )

                                                                // Équation de densité énergétique
                                                                // pour matières premières (DEraw)
                                                                EquationDropdown(
                                                                        label =
                                                                                "Équation DEraw (densité énergétique matières premières)",
                                                                        selectedEquation =
                                                                                currentReferenceEv
                                                                                        .equationDEraw,
                                                                        availableEquations =
                                                                                combinedEquations,
                                                                        onEquationSelected = {
                                                                                equation ->
                                                                                println(
                                                                                        "DEBUG: Updating DEraw equation to: ${equation?.name}, ID: ${equation?.uuid}"
                                                                                )
                                                                                viewModel
                                                                                        .updateReferenceEquation(
                                                                                                "DEraw",
                                                                                                equation?.uuid
                                                                                        )
                                                                        },
                                                                        isLoading = isLoading
                                                                )
                                                        }
                                                }
                                        }

                                        // Titre pour les équations nutritionnelles additionnelles
                                        item {
                                                Column(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .padding(
                                                                                vertical =
                                                                                        AppSizes.paddingMedium
                                                                        )
                                                ) {
                                                        Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement =
                                                                        Arrangement.SpaceBetween,
                                                                verticalAlignment =
                                                                        Alignment.CenterVertically
                                                        ) {
                                                                Text(
                                                                        text =
                                                                                "Équations nutritionnelles additionnelles",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .h6,
                                                                        fontWeight = FontWeight.Bold
                                                                )

                                                                // Bouton pour ajouter une nouvelle
                                                                // équation
                                                                OutlinedButton(
                                                                        onClick = onCreateEquation
                                                                ) {
                                                                        Icon(
                                                                                Icons.Default.Add,
                                                                                contentDescription =
                                                                                        "Ajouter"
                                                                        )
                                                                        Spacer(
                                                                                modifier =
                                                                                        Modifier.width(
                                                                                                AppSizes.paddingSmall
                                                                                        )
                                                                        )
                                                                        Text("Nouvelle équation")
                                                                }
                                                        }
                                                }
                                        }

                                        // Section des équations nutritionnelles associées
                                        item {
                                                Card(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .padding(
                                                                                vertical =
                                                                                        AppSizes.paddingSmall
                                                                        ),
                                                        elevation = AppSizes.cardElevationNormal
                                                ) {
                                                        Column(
                                                                modifier =
                                                                        Modifier.fillMaxWidth()
                                                                                .padding(
                                                                                        AppSizes.paddingMedium
                                                                                )
                                                        ) {
                                                                Text(
                                                                        text =
                                                                                "Équations nutritionnelles associées",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .subtitle1,
                                                                        fontWeight = FontWeight.Bold
                                                                )

                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.height(
                                                                                        AppSizes.paddingMedium
                                                                                )
                                                                )

                                                                println(
                                                                        "DEBUG: Rendering associated nutrition equations section"
                                                                )
                                                                println(
                                                                        "DEBUG: Current reference equationsNut size: ${currentReferenceEv.equationsNut.size}"
                                                                )
                                                                currentReferenceEv.equationsNut
                                                                        .forEach { equation ->
                                                                                println(
                                                                                        "DEBUG: Processing associated equation: ${equation.name}, ID: ${equation.uuid}"
                                                                                )
                                                                        }

                                                                if (currentReferenceEv.equationsNut
                                                                                .isEmpty()
                                                                ) {
                                                                        println(
                                                                                "DEBUG: No associated nutrition equations to display"
                                                                        )
                                                                        Text(
                                                                                "Aucune équation nutritionnelle associée",
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .body2
                                                                        )
                                                                } else {
                                                                        println(
                                                                                "DEBUG: Found ${currentReferenceEv.equationsNut.size} associated equations to display"
                                                                        )

                                                                        // Afficher directement les
                                                                        // équations associées
                                                                        currentReferenceEv
                                                                                .equationsNut
                                                                                .forEach { equation
                                                                                        ->
                                                                                        println(
                                                                                                "DEBUG: Displaying associated equation: ${equation.name}, UUID: ${equation.uuid}"
                                                                                        )
                                                                                        Row(
                                                                                                modifier =
                                                                                                        Modifier.fillMaxWidth(),
                                                                                                horizontalArrangement =
                                                                                                        Arrangement
                                                                                                                .SpaceBetween,
                                                                                                verticalAlignment =
                                                                                                        Alignment
                                                                                                                .CenterVertically
                                                                                        ) {
                                                                                                Text(
                                                                                                        text =
                                                                                                                equation.name,
                                                                                                        style =
                                                                                                                MaterialTheme
                                                                                                                        .typography
                                                                                                                        .body1
                                                                                                )
                                                                                                IconButton(
                                                                                                        onClick = {
                                                                                                                println(
                                                                                                                        "DEBUG: Removing associated equation: ${equation.name}, UUID: ${equation.uuid}"
                                                                                                                )
                                                                                                                viewModel
                                                                                                                        .toggleNutritionEquation(
                                                                                                                                equation
                                                                                                                        )
                                                                                                        }
                                                                                                ) {
                                                                                                        Icon(
                                                                                                                Icons.Default
                                                                                                                        .Delete,
                                                                                                                contentDescription =
                                                                                                                        "Retirer"
                                                                                                        )
                                                                                                }
                                                                                        }
                                                                                        Divider(
                                                                                                modifier =
                                                                                                        Modifier.padding(
                                                                                                                vertical =
                                                                                                                        AppSizes.paddingSmall
                                                                                                        )
                                                                                        )
                                                                                }
                                                                }
                                                        }
                                                }
                                        }

                                        // Section d'informations sur les équations nutritionnelles
                                        item {
                                                Card(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .padding(
                                                                                vertical =
                                                                                        AppSizes.paddingSmall
                                                                        ),
                                                        elevation = AppSizes.cardElevationNormal
                                                ) {
                                                        Column(
                                                                modifier =
                                                                        Modifier.fillMaxWidth()
                                                                                .padding(
                                                                                        AppSizes.paddingMedium
                                                                                )
                                                        ) {
                                                                Text(
                                                                        text =
                                                                                "À propos des équations nutritionnelles",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .subtitle1,
                                                                        fontWeight = FontWeight.Bold
                                                                )

                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.height(
                                                                                        AppSizes.paddingMedium
                                                                                )
                                                                )

                                                                Text(
                                                                        "Les équations nutritionnelles vous permettent de définir :",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .body1
                                                                )

                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.height(
                                                                                        AppSizes.paddingSmall
                                                                                )
                                                                )

                                                                val bulletPoints =
                                                                        listOf(
                                                                                "Le calcul des besoins énergétiques",
                                                                                "La conversion entre matière brute et matière sèche",
                                                                                "Les facteurs de correction selon l'espèce et le stade physiologique",
                                                                                "Les besoins spécifiques pour différentes pathologies"
                                                                        )

                                                                bulletPoints.forEach { point ->
                                                                        Row(
                                                                                modifier =
                                                                                        Modifier.padding(
                                                                                                vertical =
                                                                                                        AppSizes.paddingXSmall
                                                                                        ),
                                                                                verticalAlignment =
                                                                                        Alignment
                                                                                                .CenterVertically
                                                                        ) {
                                                                                Text(
                                                                                        "•",
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .body1
                                                                                )
                                                                                Spacer(
                                                                                        modifier =
                                                                                                Modifier.width(
                                                                                                        AppSizes.paddingSmall
                                                                                                )
                                                                                )
                                                                                Text(
                                                                                        point,
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .body2
                                                                                )
                                                                        }
                                                                }
                                                        }
                                                }
                                        }

                                        // Afficher la liste des équations disponibles uniquement
                                        // s'il y en a
                                        if (!combinedEquations.isEmpty()) {
                                                item {
                                                        Text(
                                                                text = "Équations disponibles",
                                                                style = MaterialTheme.typography.h6,
                                                                fontWeight = FontWeight.Bold,
                                                                modifier =
                                                                        Modifier.padding(
                                                                                vertical =
                                                                                        AppSizes.paddingMedium
                                                                        )
                                                        )
                                                }

                                                // Liste des équations disponibles
                                                items(combinedEquations) { equation ->
                                                        EquationItem(
                                                                equation = equation,
                                                                onEdit = {
                                                                        println(
                                                                                "DEBUG: Edit equation requested: ${equation.name}, UUID: ${equation.uuid}"
                                                                        )
                                                                        onEditEquation(
                                                                                equation.uuid
                                                                        )
                                                                },
                                                                onDelete = {
                                                                        println(
                                                                                "DEBUG: Delete equation requested: ${equation.name}, UUID: ${equation.uuid}"
                                                                        )
                                                                        setEquationToDelete(
                                                                                equation
                                                                        )
                                                                        setShowDeleteConfirm(true)
                                                                },
                                                                onAssociate = {
                                                                        // Associer l'équation à la
                                                                        // référence en tant
                                                                        // qu'équation
                                                                        // nutritionnelle
                                                                        println(
                                                                                "DEBUG: Toggle association requested for equation: ${equation.name}, UUID: ${equation.uuid}"
                                                                        )
                                                                        println(
                                                                                "DEBUG: Current equationsNut contains ${currentReferenceEv.equationsNut.size} items"
                                                                        )
                                                                        val isAssociated =
                                                                                currentReferenceEv
                                                                                        .equationsNut
                                                                                        .any {
                                                                                                it.uuid ==
                                                                                                        equation.uuid
                                                                                        }
                                                                        println(
                                                                                "DEBUG: Equation is currently associated: $isAssociated"
                                                                        )
                                                                        viewModel
                                                                                .toggleNutritionEquation(
                                                                                        equation
                                                                                )
                                                                },
                                                                isAssociated =
                                                                        currentReferenceEv
                                                                                .equationsNut.any {
                                                                                it.uuid ==
                                                                                        equation.uuid
                                                                        }
                                                        )
                                                }
                                        }
                                }
                        }

                        // Dialogue de confirmation de suppression
                        if (showDeleteConfirm) {
                                ConfirmDialog(
                                        title = "Supprimer l'équation",
                                        message =
                                                "Êtes-vous sûr de vouloir supprimer l'équation '${equationToDelete?.name}'?",
                                        onConfirm = {
                                                equationToDelete?.let {
                                                        equationViewModel.deleteEquation()
                                                        // Retirer aussi les associations avec la
                                                        // référence
                                                        // viewModel.removeEquationAssociation(referenceEv.uuid, it.uuid)
                                                }
                                                setShowDeleteConfirm(false)
                                        },
                                        onDismiss = { setShowDeleteConfirm(false) }
                                )
                        }
                }
        }

        // Utiliser le Scaffold uniquement si on n'est pas dans un TabView
        if (isInTabView) {
                content(PaddingValues())
        } else {
                Scaffold(
                        topBar = {
                                TopBarWithActions(
                                        title = "Équations pour ${currentReferenceEv.nom}",
                                        onNavigateBack = onNavigateBack,
                                        actions = {
                                                IconButton(onClick = onCreateEquation) {
                                                        Icon(
                                                                imageVector = Icons.Default.Add,
                                                                contentDescription =
                                                                        "Ajouter une équation",
                                                                tint =
                                                                        MaterialTheme.colors
                                                                                .onPrimary
                                                        )
                                                }
                                        }
                                )
                        }
                ) { paddingValues -> content(paddingValues) }
        }
}

/** Composant pour sélectionner une équation dans une liste déroulante */
@Composable
private fun EquationDropdown(
        label: String,
        selectedEquation: Equation?,
        availableEquations: List<Equation>,
        onEquationSelected: (Equation?) -> Unit,
        isLoading: Boolean
) {
        println(
                "DEBUG: EquationDropdown component for $label - Selected equation: ${selectedEquation?.name ?: "none"}, ID: ${selectedEquation?.uuid ?: "none"}"
        )
        println("DEBUG: EquationDropdown has ${availableEquations.size} available equations")

        // Logging individual equations for debugging
        availableEquations.forEach { equation ->
                println(
                        "DEBUG: Available equation in dropdown: ${equation.name}, ID: ${equation.uuid}"
                )
        }

        var expanded by remember { mutableStateOf(false) }

        Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = label, style = MaterialTheme.typography.subtitle1)
                Spacer(modifier = Modifier.height(AppSizes.paddingTiny))

                OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(
                                text =
                                        selectedEquation?.name?.ifBlank {
                                                "Sélectionner une équation"
                                        }
                                                ?: "Sélectionner une équation",
                                style = MaterialTheme.typography.body1,
                                modifier = Modifier.weight(1f)
                        )
                }

                DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                        // Option pour ne sélectionner aucune équation
                        DropdownMenuItem(
                                onClick = {
                                        println("DEBUG: Selected 'None' option for $label")
                                        onEquationSelected(null)
                                        expanded = false
                                }
                        ) { Text("Aucune") }

                        // Liste des équations disponibles
                        availableEquations.forEach { equation ->
                                DropdownMenuItem(
                                        onClick = {
                                                println(
                                                        "DEBUG: Selected equation for $label: ${equation.name}, ID: ${equation.uuid}"
                                                )
                                                onEquationSelected(equation)
                                                expanded = false
                                        }
                                ) { Text(equation.name) }
                        }
                }
        }
}

/** Élément représentant une équation dans la liste */
@Composable
private fun EquationItem(
        equation: Equation,
        onEdit: () -> Unit,
        onDelete: () -> Unit,
        onAssociate: () -> Unit,
        isAssociated: Boolean
) {
        Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = AppSizes.paddingSmall),
                elevation = AppSizes.cardElevationNormal
        ) {
                Column(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .clickable(onClick = onEdit)
                                        .padding(AppSizes.paddingMedium)
                ) {
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Text(
                                        text = equation.name,
                                        style = MaterialTheme.typography.h6,
                                        fontWeight = FontWeight.Bold
                                )
                                IconButton(onClick = onEdit) {
                                        Icon(Icons.Default.Edit, contentDescription = "Éditer")
                                }
                        }

                        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))

                        Text(
                                text = "Type: ${equation.kind}",
                                style = MaterialTheme.typography.body2
                        )

                        if (equation.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                                Text(
                                        text = equation.description,
                                        style = MaterialTheme.typography.body2
                                )
                        }

                        Spacer(modifier = Modifier.height(AppSizes.paddingMedium))

                        // Boutons d'action
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                        ) {
                                // Bouton pour associer/dissocier l'équation comme équation
                                // nutritionnelle
                                Button(
                                        onClick = onAssociate,
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor =
                                                                if (isAssociated)
                                                                        MaterialTheme.colors.primary
                                                                else MaterialTheme.colors.surface
                                                )
                                ) { Text(if (isAssociated) "Retirer" else "Associer") }

                                Spacer(modifier = Modifier.width(AppSizes.paddingSmall))

                                // Bouton pour supprimer l'équation
                                OutlinedButton(onClick = onDelete) { Text("Supprimer") }
                        }
                }
        }
}
