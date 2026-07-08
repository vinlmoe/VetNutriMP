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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import fr.vetbrain.vetnutri_mp.Components.ConfirmDialog
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Data.Equation
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.EquationViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.ReferenceEvViewModel
import fr.vetbrain.vetnutri_mp.Utils.isIosPlatform
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate

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

        val currentReferenceEv by
                viewModel.currentReferenceEv.collectAsState(initial = ReferenceEv())
        val equations by equationViewModel.equations.collectAsState(initial = emptyList())
        val availableEquations by viewModel.availableEquations.collectAsState(initial = emptyList())
        val isLoading by viewModel.loading.collectAsState(initial = false)
        val equationLoading by equationViewModel.isLoading.collectAsState(initial = false)
        val scope = rememberCoroutineScope()


        // Combinons les deux sources d'équations pour avoir plus de chances d'en avoir
        val combinedEquations =
                remember(equations, availableEquations) {
                        (equations + availableEquations).distinctBy { it.uuid }
                }


        // État pour la confirmation de suppression
        val (showDeleteConfirm, setShowDeleteConfirm) = remember { mutableStateOf(false) }
        // Équation à supprimer
        val (equationToDelete, setEquationToDelete) = remember { mutableStateOf<Equation?>(null) }

        // Charger les équations associées à la référence au chargement
        LaunchedEffect(referenceId) {
                viewModel.loadReferenceEvById(referenceId)
                equationViewModel.loadEquations()
                viewModel.loadEquations()
        }

        // Logs de débogage pour l'état de chargement et les données
        LaunchedEffect(isLoading, equationLoading, currentReferenceEv, equations) {
        }

        // Nous utiliserons une approche plus directe lors de l'affichage
        currentReferenceEv.equationsNut.forEach { equation ->
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
                                // Indicateur de chargement au centre
                                CircularProgressIndicator(
                                        modifier = Modifier.align(Alignment.Center),
                                        color = VetNutriColors.Primary
                                )
                        } else {
                                // Afficher l'interface que des équations soient disponibles ou non

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
                                                                        translate(LocalizationKeys.Reference.EQ_NO_EQUATION_AVAILABLE),
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
                                                                                        translate(LocalizationKeys.General.ADD)
                                                                        )
                                                                        Spacer(
                                                                                modifier =
                                                                                        Modifier.width(
                                                                                                AppSizes.paddingSmall
                                                                                        )
                                                                        )
                                                                        Text(translate(LocalizationKeys.Reference.EQ_CREATE_EQUATION_BUTTON))
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
                                                                                translate(LocalizationKeys.Reference.EQ_MAIN_EQUATIONS_TITLE),
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
                                                                                translate(LocalizationKeys.Reference.EQ_LABEL_BW),
                                                                        selectedEquation =
                                                                                currentReferenceEv
                                                                                        .equationBW,
                                                                        availableEquations =
                                                                                combinedEquations,
                                                                        onEquationSelected = {
                                                                                equation ->
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
                                                                                translate(LocalizationKeys.Reference.EQ_LABEL_BEE),
                                                                        selectedEquation =
                                                                                currentReferenceEv
                                                                                        .equationBEE,
                                                                        availableEquations =
                                                                                combinedEquations,
                                                                        onEquationSelected = {
                                                                                equation ->
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
                                                                                translate(LocalizationKeys.Reference.EQ_LABEL_DECOM),
                                                                        selectedEquation =
                                                                                currentReferenceEv
                                                                                        .equationDEcom,
                                                                        availableEquations =
                                                                                combinedEquations,
                                                                        onEquationSelected = {
                                                                                equation ->
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
                                                                                translate(LocalizationKeys.Reference.EQ_LABEL_DERAW),
                                                                        selectedEquation =
                                                                                currentReferenceEv
                                                                                        .equationDEraw,
                                                                        availableEquations =
                                                                                combinedEquations,
                                                                        onEquationSelected = {
                                                                                equation ->
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
                                                                                translate(LocalizationKeys.Reference.EQ_ADDITIONAL_EQUATIONS_TITLE),
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
                                                                                        translate(LocalizationKeys.General.ADD)
                                                                        )
                                                                        Spacer(
                                                                                modifier =
                                                                                        Modifier.width(
                                                                                                AppSizes.paddingSmall
                                                                                        )
                                                                        )
                                                                        Text(translate(LocalizationKeys.Reference.EQ_NEW_EQUATION_BUTTON))
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
                                                                Row(
                                                                        modifier = Modifier.fillMaxWidth(),
                                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                                        verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                        Text(
                                                                                text =
                                                                                        translate(LocalizationKeys.Reference.EQ_ASSOCIATED_EQUATIONS_TITLE),
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .subtitle1,
                                                                                fontWeight = FontWeight.Bold
                                                                        )
                                                                        if (currentReferenceEv.maladie) {
                                                                                Text(
                                                                                        text = translate(LocalizationKeys.Reference.EQ_ENERCOMP_HINT),
                                                                                        style = MaterialTheme.typography.caption,
                                                                                        color = VetNutriColors.Primary
                                                                                )
                                                                        }
                                                                }

                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.height(
                                                                                        AppSizes.paddingMedium
                                                                                )
                                                                )

                                                                currentReferenceEv.equationsNut
                                                                        .forEach { equation ->
                                                                        }

                                                                if (currentReferenceEv.equationsNut
                                                                                .isEmpty()
                                                                ) {
                                                                        Text(
                                                                                translate(LocalizationKeys.Reference.EQ_NO_ASSOCIATED_EQUATION),
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .body2
                                                                        )
                                                                } else {

                                                                        // Afficher directement les
                                                                        // équations associées
                                                                        currentReferenceEv
                                                                                .equationsNut
                                                                                .forEach { equation ->
                                                                                        val isEnercomp =
                                                                                                equation.kind ==
                                                                                                        fr.vetbrain.vetnutri_mp.Enumer
                                                                                                                .EquationKind
                                                                                                                .ENERCOMP
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
                                                                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                                                                        Text(
                                                                                                                text = equation.name,
                                                                                                                style = MaterialTheme.typography.body1
                                                                                                        )
                                                                                                        if (isEnercomp) {
                                                                                                                Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                                                                                                                Text(
                                                                                                                        text = translate("auto.view.referenceevequationview.enercomp"),
                                                                                                                        style = MaterialTheme.typography.caption,
                                                                                                                        color = VetNutriColors.Secondary
                                                                                                                )
                                                                                                        }
                                                                                                }
                                                                                                IconButton(
                                                                                                        onClick = {
                                                                                                                viewModel.toggleNutritionEquation(equation)
                                                                                                        }
                                                                                                ) {
                                                                                                        Icon(
                                                                                                                Icons.Default.Delete,
                                                                                                                contentDescription = translate(LocalizationKeys.Reference.EQ_REMOVE_ACTION)
                                                                                                        )
                                                                                                }
                                                                                        }
                                                                                        Divider(
                                                                                                modifier =
                                                                                                        Modifier.padding(
                                                                                                                vertical = AppSizes.paddingSmall
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
                                                                                translate(LocalizationKeys.Reference.EQ_ABOUT_TITLE),
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
                                                                        translate(LocalizationKeys.Reference.EQ_ABOUT_INTRO),
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
                                                                                translate(LocalizationKeys.Reference.EQ_BULLET_ENERGY_CALC),
                                                                                translate(LocalizationKeys.Reference.EQ_BULLET_CONVERSION),
                                                                                translate(LocalizationKeys.Reference.EQ_BULLET_CORRECTION_FACTORS),
                                                                                translate(LocalizationKeys.Reference.EQ_BULLET_PATHOLOGY_NEEDS)
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
                                                                text = translate(LocalizationKeys.Reference.EQ_AVAILABLE_EQUATIONS_TITLE),
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
                                                        val isEnercomp =
                                                                equation.kind ==
                                                                        fr.vetbrain.vetnutri_mp.Enumer
                                                                                .EquationKind
                                                                                .ENERCOMP
                                                        EquationItem(
                                                                equation = equation,
                                                                onEdit = {
                                                                        onEditEquation(
                                                                                equation.uuid
                                                                        )
                                                                },
                                                                onDelete = {
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
                                                                        val isAssociated =
                                                                                currentReferenceEv
                                                                                        .equationsNut
                                                                                        .any {
                                                                                                it.uuid ==
                                                                                                        equation.uuid
                                                                                        }
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
                                                        if (isEnercomp && !currentReferenceEv.maladie) {
                                                                Text(
                                                                        text = translate(LocalizationKeys.Reference.EQ_ENERCOMP_DISEASE_ONLY_HINT),
                                                                        style = MaterialTheme.typography.caption,
                                                                        color = Color.Gray
                                                                )
                                                        }
                                                }
                                        }
                                }
                        }

                        // Dialogue de confirmation de suppression
                        if (showDeleteConfirm) {
                                ConfirmDialog(
                                        title = translate(LocalizationKeys.Reference.EQ_DELETE_CONFIRM_TITLE),
                                        message =
                                                translate(LocalizationKeys.Reference.EQ_DELETE_CONFIRM_MESSAGE_FORMAT, equationToDelete?.name ?: ""),
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
                                TopBarSimple(
                                        title = translate(LocalizationKeys.Reference.EQ_VIEW_TITLE_FORMAT, currentReferenceEv.nom),
                                        onNavigateBack = onNavigateBack,
                                        actions = {
                                                IconButton(onClick = onCreateEquation) {
                                                        Icon(
                                                                imageVector = Icons.Default.Add,
                                                                contentDescription =
                                                                        translate(LocalizationKeys.Reference.EQ_ADD_EQUATION_CONTENT_DESC),
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

        // Logging individual equations for debugging
        availableEquations.forEach { equation ->
        }

        var expanded by remember { mutableStateOf(false) }

        Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = label, style = MaterialTheme.typography.subtitle1)
                Spacer(modifier = Modifier.height(AppSizes.paddingTiny))

                OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(
                                text =
                                        selectedEquation?.name?.ifBlank {
                                                translate(LocalizationKeys.NewReference.SELECT_EQUATION)
                                        }
                                                ?: translate(LocalizationKeys.NewReference.SELECT_EQUATION),
                                style = MaterialTheme.typography.body1,
                                modifier = Modifier.weight(1f)
                        )
                }

                DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                            if (!isIosPlatform) {
                                expanded = false 
                            }
                        },
                        modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                        // Option pour ne sélectionner aucune équation
                        DropdownMenuItem(
                                onClick = {
                                        onEquationSelected(null)
                                        expanded = false
                                }
                        ) { Text(translate(LocalizationKeys.General.NONE)) }

                        // Liste des équations disponibles
                        availableEquations.forEach { equation ->
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
                                        Icon(Icons.Default.Edit, contentDescription = translate(LocalizationKeys.General.EDIT))
                                }
                        }

                        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))

                        Text(
                                text = translate(LocalizationKeys.Reference.EQ_TYPE_FORMAT, equation.kind.toString()),
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
                                ) { Text(if (isAssociated) translate(LocalizationKeys.Reference.EQ_REMOVE_ACTION) else translate(LocalizationKeys.Reference.EQ_ASSOCIATE_ACTION)) }

                                Spacer(modifier = Modifier.width(AppSizes.paddingSmall))

                                // Bouton pour supprimer l'équation
                                OutlinedButton(onClick = onDelete) { Text(translate(LocalizationKeys.General.DELETE)) }
                        }
                }
        }
}
