package fr.vetbrain.vetnutri_mp.View

import fr.vetbrain.vetnutri_mp.Components.AdaptiveTabRow
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
import androidx.compose.material.*
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.StadePhysio
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.NewReferenceEvViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import fr.vetbrain.vetnutri_mp.Utils.isIosPlatform

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
                        val tabs = listOf(
                                translate(LocalizationKeys.NewReference.TAB_INFO),
                                translate(LocalizationKeys.NewReference.TAB_NUTRIENTS),
                                translate(LocalizationKeys.NewReference.TAB_EQUATIONS),
                                translate(LocalizationKeys.NewReference.TAB_COMPLEMENTARY),
                                translate(LocalizationKeys.NewReference.TAB_COEFFICIENTS)
                        )

        Scaffold(
                topBar = {
                        TopBarSimple(
                                title = if (isEditMode) translate(LocalizationKeys.NewReference.TITLE_EDIT) else translate(LocalizationKeys.NewReference.TITLE_NEW),
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
                        AdaptiveTabRow(
                                selectedTabIndex = selectedTabIndex,
                                tabCount = tabs.size,
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
                        label = { Text(translate(LocalizationKeys.NewReference.FIELD_NAME)) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                // Champ pour la description
                OutlinedTextField(
                        value = currentReference.description,
                        onValueChange = { viewModel.updateReferenceProperty("description", it) },
                        label = { Text(translate(LocalizationKeys.NewReference.FIELD_DESCRIPTION)) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        minLines = 3
                )

                // Sélection de l'espèce
                Text(
                        text = translate(LocalizationKeys.NewReference.FIELD_SPECIES),
                        style = MaterialTheme.typography.subtitle1,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )

                val especeOptions = Espece.valuesExcept(Espece.CH)
                var especeExpanded by remember { mutableStateOf(false) }

                Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                                onClick = { especeExpanded = true },
                                modifier = Modifier.fillMaxWidth()
                        ) { Text(currentReference.espece.translateEnum()) }

                        DropdownMenu(
                                expanded = especeExpanded,
                                onDismissRequest = {
                                    if (!isIosPlatform) {
                                        especeExpanded = false 
                                    }
                                },
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
                                        ) { Text(espece.translateEnum()) }
                                }
                        }
                }

                // Sélection du stade physiologique
                Text(
                        text = translate(LocalizationKeys.NewReference.FIELD_PHYSIO_STAGE),
                        style = MaterialTheme.typography.subtitle1,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )

                val stadePhysioOptions = StadePhysio.values()
                var stadePhysioExpanded by remember { mutableStateOf(false) }

                Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                                onClick = { stadePhysioExpanded = true },
                                modifier = Modifier.fillMaxWidth()
                        ) { Text(currentReference.stadePhysio.translateEnum()) }

                        DropdownMenu(
                                expanded = stadePhysioExpanded,
                                onDismissRequest = {
                                    if (!isIosPlatform) {
                                        stadePhysioExpanded = false 
                                    }
                                },
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
                                        ) { Text(stade.translateEnum()) }
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
                                text = translate(LocalizationKeys.NewReference.FIELD_IS_COMPLEMENTARY),
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
                                label = { Text(translate(LocalizationKeys.NewReference.FIELD_COMPLEMENTARY_NEED)) },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                }

                // Champ pour le nom de l'énergie
                OutlinedTextField(
                        value = currentReference.nomEnergie,
                        onValueChange = { viewModel.updateReferenceProperty("nomEnergie", it) },
                        label = { Text(translate(LocalizationKeys.NewReference.FIELD_ENERGY_NAME)) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                // Slider pour la consistance
                Text(
                        text = translate(LocalizationKeys.NewReference.FIELD_CONSISTENCY, currentReference.consistent.toString()),
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
                ) { Text(translate(LocalizationKeys.General.SAVE)) }
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
                                                translate(LocalizationKeys.NewReference.NUTRIENT_CATEGORY_MACRO),
                                                translate(LocalizationKeys.NewReference.NUTRIENT_CATEGORY_MINERALS),
                                                translate(LocalizationKeys.NewReference.NUTRIENT_CATEGORY_OLIGO),
                                                translate(LocalizationKeys.NewReference.NUTRIENT_CATEGORY_VITAMINS),
                                                translate(LocalizationKeys.NewReference.NUTRIENT_CATEGORY_LIPIDS),
                                                translate(LocalizationKeys.NewReference.NUTRIENT_CATEGORY_AA),
                                                translate(LocalizationKeys.NewReference.NUTRIENT_CATEGORY_OTHERS),
                                                translate(LocalizationKeys.NewReference.NUTRIENT_CATEGORY_ANALYSIS)
                                        )

                                AdaptiveTabRow(
                                        selectedTabIndex = selectedNutrientType,
                                        tabCount = nutrientTypes.size,
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
        val groupNames =
                (1..5).map { index ->
                        translate(LocalizationKeys.NewReference.COEFF_GROUP_PREFIX, "K$index")
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
                                                text =
                                                        translate(
                                                                LocalizationKeys
                                                                        .NewReference
                                                                        .MALADIE_WARNING_TITLE
                                                        ),
                                                style = MaterialTheme.typography.subtitle1,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colors.secondary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                                text =
                                                        translate(
                                                                LocalizationKeys
                                                                        .NewReference
                                                                        .MALADIE_COEFF_WARNING
                                                        ),
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

                                        AdaptiveTabRow(
                                                selectedTabIndex = selectedGroupIndex,
                                                tabCount = groupNames.size,
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
                                                                ?: translate(LocalizationKeys.NewReference.COEFF_NORMAL),
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
