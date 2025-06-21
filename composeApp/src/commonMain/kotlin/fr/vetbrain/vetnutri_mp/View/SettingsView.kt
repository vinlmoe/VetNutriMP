package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.Section
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.ImportViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/**
 * Dialogue simple pour les paramètres d'affichage
 * @param viewModel Le ViewModel des paramètres
 * @param onDismiss Callback appelé lorsque l'utilisateur ferme le dialogue
 */
@Composable
fun SettingsDialog(viewModel: SettingsViewModel, onDismiss: () -> Unit) {
        val uiScale by viewModel.uiScale.collectAsState()

        AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Paramètres d'affichage", style = MaterialTheme.typography.h6) },
                text = {
                        Column(
                                modifier = Modifier.padding(AppSizes.paddingMedium),
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                        ) {
                                Text(
                                        "Taille de l'interface",
                                        style = MaterialTheme.typography.subtitle1
                                )

                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Button(
                                                onClick = { viewModel.decrementUiScale() },
                                                enabled = uiScale > 0.5f,
                                                modifier = Modifier.size(AppSizes.buttonHeight)
                                        ) { Text("-") }

                                        Text(
                                                "${(uiScale * 100).roundToInt()}%",
                                                style = MaterialTheme.typography.body1
                                        )

                                        Button(
                                                onClick = { viewModel.incrementUiScale() },
                                                enabled = uiScale < 2f,
                                                modifier = Modifier.size(AppSizes.buttonHeight)
                                        ) { Text("+") }
                                }
                        }
                },
                confirmButton = {
                        Button(
                                onClick = onDismiss,
                                colors =
                                        ButtonDefaults.buttonColors(
                                                backgroundColor = VetNutriColors.Primary,
                                                contentColor = Color.White
                                        )
                        ) { Text("Fermer") }
                },
                backgroundColor = MaterialTheme.colors.surface
        )
}

/**
 * Menu latéral pour la navigation dans les paramètres
 * @param currentSection Section actuellement sélectionnée
 * @param onSectionSelected Callback appelé lors de la sélection d'une section
 */
@Composable
fun SettingsDrawer(
        currentSection: SettingsSection,
        onSectionSelected: (SettingsSection) -> Unit,
        onClose: () -> Unit
) {
        Column(
                modifier =
                        Modifier.fillMaxHeight()
                                .width(300.dp)
                                .background(MaterialTheme.colors.surface)
                                .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
                Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        Text(
                                "Paramètres",
                                style = MaterialTheme.typography.h6,
                                color = VetNutriColors.Primary
                        )
                        IconButton(onClick = onClose) {
                                Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Fermer",
                                        tint = Color.Gray
                                )
                        }
                }

                Divider(color = Color.LightGray, thickness = 1.dp)

                // Sections de paramètres
                SettingsSectionItem(
                        section = SettingsSection.INTERFACE,
                        isSelected = currentSection == SettingsSection.INTERFACE,
                        onSelected = onSectionSelected,
                        icon = Icons.Default.Settings
                )

                SettingsSectionItem(
                        section = SettingsSection.NUTRIMENTS,
                        isSelected = currentSection == SettingsSection.NUTRIMENTS,
                        onSelected = onSectionSelected,
                        icon = Icons.Default.List
                )

                SettingsSectionItem(
                        section = SettingsSection.PREFERENCES,
                        isSelected = currentSection == SettingsSection.PREFERENCES,
                        onSelected = onSectionSelected,
                        icon = Icons.Default.Settings
                )

                SettingsSectionItem(
                        section = SettingsSection.IMPORTATION,
                        isSelected = currentSection == SettingsSection.IMPORTATION,
                        onSelected = onSectionSelected,
                        icon = Icons.Default.Build
                )

                SettingsSectionItem(
                        section = SettingsSection.ADMINISTRATION,
                        isSelected = currentSection == SettingsSection.ADMINISTRATION,
                        onSelected = onSectionSelected,
                        icon = Icons.Default.Settings
                )

                Spacer(modifier = Modifier.weight(1f))

                Divider(color = Color.LightGray, thickness = 1.dp)

                Text(
                        "VetNutri MP",
                        style = MaterialTheme.typography.caption,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
                Text("Version 1.0", style = MaterialTheme.typography.caption, color = Color.Gray)
        }
}

/** Élément d'une section dans le menu latéral */
@Composable
fun SettingsSectionItem(
        section: SettingsSection,
        isSelected: Boolean,
        onSelected: (SettingsSection) -> Unit,
        icon: androidx.compose.ui.graphics.vector.ImageVector
) {
        val backgroundColor =
                if (isSelected) VetNutriColors.Primary.copy(alpha = 0.1f) else Color.Transparent

        val textColor = if (isSelected) VetNutriColors.Primary else Color.DarkGray

        Row(
                modifier =
                        Modifier.fillMaxWidth()
                                .background(backgroundColor, RoundedCornerShape(4.dp))
                                .clickable { onSelected(section) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
                Icon(icon, contentDescription = section.title, tint = textColor)

                Text(
                        section.title,
                        style = MaterialTheme.typography.body1,
                        color = textColor,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                )
        }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SettingsView(
        viewModel: SettingsViewModel,
        importViewModel: ImportViewModel,
        onImportAnimals: () -> Unit,
        onBack: () -> Unit,
        onAnimalListRefresh: () -> Unit,
        onFoodListRefresh: () -> Unit,
        modifier: Modifier = Modifier,
        onSpeciesClick: (fr.vetbrain.vetnutri_mp.Enumer.Espece) -> Unit = {}
) {
        // État pour le dialogue de confirmation de suppression
        var isDialogVisible by remember { mutableStateOf(false) }
        var isProcessing by remember { mutableStateOf(false) }
        var resultMessage by remember { mutableStateOf("") }
        var isAnimalDeleteDialogVisible by remember { mutableStateOf(false) }

        // États pour les nouveaux dialogues de suppression
        var isReferenceDeleteDialogVisible by remember { mutableStateOf(false) }
        var isEquationDeleteDialogVisible by remember { mutableStateOf(false) }
        var isBiblioDeleteDialogVisible by remember { mutableStateOf(false) }

        // État pour le dialogue d'alerte d'importation des références nutritionnelles
        var showImportDialog by remember { mutableStateOf(false) }
        var importDialogMessage by remember { mutableStateOf("") }

        val coroutineScope = rememberCoroutineScope()
        val uiScale by viewModel.uiScale.collectAsState()

        // État pour la section actuelle et le menu latéral
        var currentSection by remember { mutableStateOf(SettingsSection.INTERFACE) }
        val isDrawerOpen by viewModel.isDrawerOpen.collectAsState()

        // États pour les listes de nutriments
        val selectedMainNutrients by viewModel.selectedMainNutrients.collectAsState()
        val selectedMinerals by viewModel.selectedMinerals.collectAsState()
        val selectedVitamins by viewModel.selectedVitamins.collectAsState()
        val selectedLipids by viewModel.selectedLipids.collectAsState()
        val selectedAminoAcids by viewModel.selectedAminoAcids.collectAsState()
        val selectedOtherNutrients by viewModel.selectedOtherNutrients.collectAsState()

        // Observer le message d'importation des références nutritionnelles
        val nutritionalRequirementMessage by remember {
                derivedStateOf { importViewModel.nutritionalRequirementImportResultMessage }
        }

        // Afficher le dialogue d'alerte quand l'importation est terminée
        LaunchedEffect(nutritionalRequirementMessage) {
                nutritionalRequirementMessage?.let { message ->
                        // Afficher le dialogue si le message n'est pas vide et ne contient pas
                        // l'indicateur de progression
                        if (message.isNotEmpty() &&
                                        !message.contains("🔄") &&
                                        !message.contains("Sélection du fichier") &&
                                        (message.startsWith("✅") || message.startsWith("❌"))
                        ) {
                                importDialogMessage = message
                                showImportDialog = true
                        }
                }
        }

        // État pour la catégorie de nutriments en cours d'édition
        var editingCategory by remember { mutableStateOf(MainNutrientEnum.BASE) }

        // Liste des catégories de nutriments à afficher dans les onglets
        val nutrientCategories = remember {
                listOf(
                        MainNutrientEnum.BASE,
                        MainNutrientEnum.MIN,
                        MainNutrientEnum.VITAM,
                        MainNutrientEnum.LIPID,
                        MainNutrientEnum.AMA,
                        MainNutrientEnum.OTHER
                )
        }

        // Fonction pour obtenir l'index de l'onglet correspondant à la catégorie
        val getTabIndex = { category: MainNutrientEnum ->
                nutrientCategories.indexOf(category).coerceIn(0, nutrientCategories.size - 1)
        }

        // État pour le drawer
        val scaffoldState =
                rememberScaffoldState(
                        drawerState =
                                rememberDrawerState(
                                        initialValue =
                                                if (isDrawerOpen) DrawerValue.Open
                                                else DrawerValue.Closed
                                )
                )

        LaunchedEffect(isDrawerOpen) {
                if (isDrawerOpen) {
                        scaffoldState.drawerState.open()
                } else {
                        scaffoldState.drawerState.close()
                }
        }

        Scaffold(
                scaffoldState = scaffoldState,
                topBar = {
                        Row(
                                modifier = Modifier.fillMaxWidth().padding(AppSizes.paddingMedium),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Row(
                                        horizontalArrangement =
                                                Arrangement.spacedBy(AppSizes.paddingSmall),
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        IconButton(
                                                onClick = { viewModel.openDrawer() },
                                                modifier = Modifier.size(AppSizes.iconSizeLarge)
                                        ) {
                                                Icon(
                                                        Icons.Default.Menu,
                                                        contentDescription = "Menu",
                                                        modifier =
                                                                Modifier.size(
                                                                        AppSizes.iconSizeMedium
                                                                )
                                                )
                                        }
                                        Text(
                                                text = "Paramètres",
                                                style =
                                                        MaterialTheme.typography.h5.copy(
                                                                fontSize = AppSizes.fontSizeH5
                                                        )
                                        )
                                }

                                IconButton(
                                        onClick = onBack,
                                        modifier = Modifier.size(AppSizes.iconSizeLarge)
                                ) {
                                        Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Fermer",
                                                modifier = Modifier.size(AppSizes.iconSizeMedium)
                                        )
                                }
                        }
                },
                drawerContent = {
                        SettingsDrawer(
                                currentSection = currentSection,
                                onSectionSelected = { section ->
                                        currentSection = section
                                        viewModel.closeDrawer()
                                },
                                onClose = { viewModel.closeDrawer() }
                        )
                },
                drawerGesturesEnabled = true
        ) { paddingValues ->
                Column(
                        modifier =
                                Modifier.fillMaxSize()
                                        .padding(paddingValues)
                                        .padding(AppSizes.paddingMedium),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                ) {
                        when (currentSection) {
                                SettingsSection.INTERFACE -> {
                                        // Section pour l'échelle de l'interface
                                        Section(title = "Échelle de l'interface") {
                                                Row(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .padding(vertical = 8.dp),
                                                        horizontalArrangement =
                                                                Arrangement.SpaceBetween,
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        Button(
                                                                onClick = {
                                                                        viewModel.decrementUiScale()
                                                                },
                                                                enabled = uiScale > 0.5f,
                                                                modifier =
                                                                        Modifier.size(
                                                                                AppSizes.buttonHeight
                                                                        )
                                                        ) { Text("-") }

                                                        Text(
                                                                "${(uiScale * 100).roundToInt()}%",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .body1
                                                        )

                                                        Button(
                                                                onClick = {
                                                                        viewModel.incrementUiScale()
                                                                },
                                                                enabled = uiScale < 2f,
                                                                modifier =
                                                                        Modifier.size(
                                                                                AppSizes.buttonHeight
                                                                        )
                                                        ) { Text("+") }
                                                }
                                        }
                                }
                                SettingsSection.NUTRIMENTS -> {
                                        // Section pour les nutriments à afficher et leur ordre
                                        Section(title = "Configuration des nutriments") {
                                                Text(
                                                        "Sélectionnez les nutriments à afficher",
                                                        style = MaterialTheme.typography.body2,
                                                        color = Color.Gray,
                                                        modifier = Modifier.padding(bottom = 16.dp)
                                                )

                                                // Sélection de la catégorie
                                                Text(
                                                        "Catégorie de nutriments",
                                                        style = MaterialTheme.typography.subtitle1,
                                                        fontWeight = FontWeight.Medium
                                                )

                                                // Onglets pour les catégories de nutriments
                                                ScrollableTabRow(
                                                        selectedTabIndex =
                                                                getTabIndex(editingCategory),
                                                        backgroundColor = Color.Transparent,
                                                        contentColor = VetNutriColors.Primary,
                                                        edgePadding = 0.dp,
                                                        modifier = Modifier.padding(vertical = 8.dp)
                                                ) {
                                                        // Créer un onglet pour chaque catégorie de
                                                        // nutriments définie
                                                        nutrientCategories.forEach { category ->
                                                                Tab(
                                                                        selected =
                                                                                editingCategory ==
                                                                                        category,
                                                                        onClick = {
                                                                                editingCategory =
                                                                                        category
                                                                        },
                                                                        text = {
                                                                                Text(
                                                                                        when (category
                                                                                        ) {
                                                                                                MainNutrientEnum
                                                                                                        .BASE ->
                                                                                                        "Principaux"
                                                                                                MainNutrientEnum
                                                                                                        .MIN ->
                                                                                                        "Minéraux"
                                                                                                MainNutrientEnum
                                                                                                        .VITAM ->
                                                                                                        "Vitamines"
                                                                                                MainNutrientEnum
                                                                                                        .LIPID ->
                                                                                                        "Lipides"
                                                                                                MainNutrientEnum
                                                                                                        .AMA ->
                                                                                                        "Acides Aminés"
                                                                                                MainNutrientEnum
                                                                                                        .OTHER ->
                                                                                                        "Autres"
                                                                                                else ->
                                                                                                        category.label
                                                                                        }
                                                                                )
                                                                        }
                                                                )
                                                        }
                                                }

                                                Spacer(modifier = Modifier.height(16.dp))

                                                // Affichage des nutriments de la catégorie
                                                // sélectionnée
                                                Text(
                                                        "Cochez les nutriments à afficher",
                                                        style = MaterialTheme.typography.body2,
                                                        color = Color.Gray,
                                                        modifier = Modifier.padding(bottom = 8.dp)
                                                )

                                                // Liste des nutriments avec checkboxes
                                                when (editingCategory) {
                                                        MainNutrientEnum.BASE -> {
                                                                val nutrients = remember {
                                                                        mutableStateListOf<
                                                                                        NutrientMain>()
                                                                                .apply {
                                                                                        addAll(
                                                                                                selectedMainNutrients
                                                                                        )
                                                                                }
                                                                }
                                                                NutrientCheckboxList(
                                                                        nutrients = nutrients,
                                                                        getAllNutrients = {
                                                                                NutrientMain.entries
                                                                                        .toList()
                                                                        },
                                                                        onNutrientsUpdated = {
                                                                                viewModel
                                                                                        .updateMainNutrients(
                                                                                                it
                                                                                        )
                                                                        }
                                                                )
                                                        }
                                                        MainNutrientEnum.MIN -> {
                                                                val nutrients = remember {
                                                                        mutableStateListOf<
                                                                                        NutrientMin>()
                                                                                .apply {
                                                                                        addAll(
                                                                                                selectedMinerals
                                                                                        )
                                                                                }
                                                                }
                                                                NutrientCheckboxList(
                                                                        nutrients = nutrients,
                                                                        getAllNutrients = {
                                                                                NutrientMin.entries
                                                                                        .toList()
                                                                        },
                                                                        onNutrientsUpdated = {
                                                                                viewModel
                                                                                        .updateMinerals(
                                                                                                it
                                                                                        )
                                                                        }
                                                                )
                                                        }
                                                        MainNutrientEnum.VITAM -> {
                                                                val nutrients = remember {
                                                                        mutableStateListOf<
                                                                                        NutrientVitam>()
                                                                                .apply {
                                                                                        addAll(
                                                                                                selectedVitamins
                                                                                        )
                                                                                }
                                                                }
                                                                NutrientCheckboxList(
                                                                        nutrients = nutrients,
                                                                        getAllNutrients = {
                                                                                NutrientVitam
                                                                                        .entries
                                                                                        .toList()
                                                                        },
                                                                        onNutrientsUpdated = {
                                                                                viewModel
                                                                                        .updateVitamins(
                                                                                                it
                                                                                        )
                                                                        }
                                                                )
                                                        }
                                                        MainNutrientEnum.LIPID -> {
                                                                val nutrients = remember {
                                                                        mutableStateListOf<
                                                                                        NutrientLipid>()
                                                                                .apply {
                                                                                        addAll(
                                                                                                selectedLipids
                                                                                        )
                                                                                }
                                                                }
                                                                NutrientCheckboxList(
                                                                        nutrients = nutrients,
                                                                        getAllNutrients = {
                                                                                NutrientLipid
                                                                                        .entries
                                                                                        .toList()
                                                                        },
                                                                        onNutrientsUpdated = {
                                                                                viewModel
                                                                                        .updateLipids(
                                                                                                it
                                                                                        )
                                                                        }
                                                                )
                                                        }
                                                        MainNutrientEnum.AMA -> {
                                                                val nutrients = remember {
                                                                        mutableStateListOf<AAEnum>()
                                                                                .apply {
                                                                                        addAll(
                                                                                                selectedAminoAcids
                                                                                        )
                                                                                }
                                                                }
                                                                NutrientCheckboxList(
                                                                        nutrients = nutrients,
                                                                        getAllNutrients = {
                                                                                AAEnum.entries
                                                                                        .toList()
                                                                        },
                                                                        onNutrientsUpdated = {
                                                                                viewModel
                                                                                        .updateAminoAcids(
                                                                                                it
                                                                                        )
                                                                        }
                                                                )
                                                        }
                                                        MainNutrientEnum.OTHER -> {
                                                                val nutrients = remember {
                                                                        mutableStateListOf<
                                                                                        NutrientOther>()
                                                                                .apply {
                                                                                        addAll(
                                                                                                selectedOtherNutrients
                                                                                        )
                                                                                }
                                                                }
                                                                NutrientCheckboxList(
                                                                        nutrients = nutrients,
                                                                        getAllNutrients = {
                                                                                NutrientOther
                                                                                        .entries
                                                                                        .toList()
                                                                        },
                                                                        onNutrientsUpdated = {
                                                                                viewModel
                                                                                        .updateOtherNutrients(
                                                                                                it
                                                                                        )
                                                                        }
                                                                )
                                                        }
                                                        else -> {
                                                                Text(
                                                                        "Catégorie de nutriments non prise en charge"
                                                                )
                                                        }
                                                }
                                        }
                                }
                                SettingsSection.PREFERENCES -> {
                                        // Section pour les préférences
                                        Section(title = "Préférences de l'application") {
                                                PreferencesSection(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        onSpeciesClick = onSpeciesClick
                                                )
                                        }
                                }
                                SettingsSection.IMPORTATION -> {
                                        // Section pour l'importation des données
                                        Section(title = "Importation des données") {
                                                Column(
                                                        verticalArrangement =
                                                                Arrangement.spacedBy(8.dp)
                                                ) {
                                                        // Affichage du message de résultat
                                                        // d'importation des références
                                                        // nutritionnelles
                                                        nutritionalRequirementMessage?.let { message
                                                                ->
                                                                Card(
                                                                        modifier =
                                                                                Modifier.fillMaxWidth()
                                                                                        .padding(
                                                                                                bottom =
                                                                                                        8.dp
                                                                                        ),
                                                                        backgroundColor =
                                                                                if (message.startsWith(
                                                                                                "✅"
                                                                                        )
                                                                                )
                                                                                        VetNutriColors
                                                                                                .Primary
                                                                                                .copy(
                                                                                                        alpha =
                                                                                                                0.1f
                                                                                                )
                                                                                else if (message.startsWith(
                                                                                                "❌"
                                                                                        )
                                                                                )
                                                                                        VetNutriColors
                                                                                                .Error
                                                                                                .copy(
                                                                                                        alpha =
                                                                                                                0.1f
                                                                                                )
                                                                                else
                                                                                        VetNutriColors
                                                                                                .Secondary
                                                                                                .copy(
                                                                                                        alpha =
                                                                                                                0.1f
                                                                                                )
                                                                ) {
                                                                        Row(
                                                                                modifier =
                                                                                        Modifier.fillMaxWidth()
                                                                                                .padding(
                                                                                                        12.dp
                                                                                                ),
                                                                                horizontalArrangement =
                                                                                        Arrangement
                                                                                                .SpaceBetween,
                                                                                verticalAlignment =
                                                                                        Alignment
                                                                                                .CenterVertically
                                                                        ) {
                                                                                Text(
                                                                                        message,
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .body2,
                                                                                        color =
                                                                                                if (message.startsWith(
                                                                                                                "✅"
                                                                                                        )
                                                                                                )
                                                                                                        VetNutriColors
                                                                                                                .Primary
                                                                                                else if (message.startsWith(
                                                                                                                "❌"
                                                                                                        )
                                                                                                )
                                                                                                        VetNutriColors
                                                                                                                .Error
                                                                                                else
                                                                                                        Color.DarkGray,
                                                                                        modifier =
                                                                                                Modifier.weight(
                                                                                                        1f
                                                                                                )
                                                                                )
                                                                                IconButton(
                                                                                        onClick = {
                                                                                                importViewModel
                                                                                                        .resetImportResult()
                                                                                        }
                                                                                ) {
                                                                                        Icon(
                                                                                                Icons.Default
                                                                                                        .Close,
                                                                                                contentDescription =
                                                                                                        "Fermer",
                                                                                                tint =
                                                                                                        Color.Gray
                                                                                        )
                                                                                }
                                                                        }
                                                                }
                                                        }

                                                        Button(
                                                                onClick = onImportAnimals,
                                                                colors =
                                                                        ButtonDefaults.buttonColors(
                                                                                backgroundColor =
                                                                                        VetNutriColors
                                                                                                .Primary
                                                                        ),
                                                                modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                                Text(
                                                                        "Importer des animaux",
                                                                        color = Color.White
                                                                )
                                                        }

                                                        // Bouton pour importer des aliments
                                                        Button(
                                                                onClick = {
                                                                        try {
                                                                                // Utilisons la
                                                                                // méthode du
                                                                                // ViewModel qui
                                                                                // encapsule l'appel
                                                                                // à
                                                                                // importFoodsFromFile
                                                                                viewModel
                                                                                        .importFoodsFromFileUI()
                                                                        } catch (e: Exception) {
                                                                                resultMessage =
                                                                                        "Erreur lors de l'importation : ${e.message}"
                                                                        }
                                                                },
                                                                colors =
                                                                        ButtonDefaults.buttonColors(
                                                                                backgroundColor =
                                                                                        VetNutriColors
                                                                                                .Primary
                                                                        ),
                                                                modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                                Text(
                                                                        "Importer des aliments",
                                                                        color = Color.White
                                                                )
                                                        }

                                                        // Bouton pour importer des références
                                                        // nutritionnelles
                                                        Button(
                                                                onClick = {
                                                                        try {
                                                                                // Utilisons la
                                                                                // méthode du
                                                                                // ImportViewModel
                                                                                // pour
                                                                                // importer les
                                                                                // références
                                                                                // nutritionnelles
                                                                                importViewModel
                                                                                        .importNutritionalRequirementsFromFileUI()
                                                                        } catch (e: Exception) {
                                                                                resultMessage =
                                                                                        "Erreur lors de l'importation des références : ${e.message}"
                                                                        }
                                                                },
                                                                colors =
                                                                        ButtonDefaults.buttonColors(
                                                                                backgroundColor =
                                                                                        VetNutriColors
                                                                                                .Secondary
                                                                        ),
                                                                modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                                Text(
                                                                        "Importer des références nutritionnelles (.vbnr.json)",
                                                                        color = Color.White
                                                                )
                                                        }
                                                }
                                        }
                                }
                                SettingsSection.ADMINISTRATION -> {
                                        // Section pour l'administration de la base de données
                                        Section(title = "Administration de la base de données") {
                                                Column(
                                                        verticalArrangement =
                                                                Arrangement.spacedBy(16.dp)
                                                ) {
                                                        Button(
                                                                onClick = {
                                                                        isDialogVisible = true
                                                                },
                                                                colors =
                                                                        ButtonDefaults.buttonColors(
                                                                                backgroundColor =
                                                                                        VetNutriColors
                                                                                                .Error,
                                                                                contentColor =
                                                                                        Color.White
                                                                        ),
                                                                modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                                Text(
                                                                        "Vider la base de données des aliments"
                                                                )
                                                        }

                                                        // Nouveau bouton pour vider la base de
                                                        // données des animaux
                                                        Button(
                                                                onClick = {
                                                                        isAnimalDeleteDialogVisible =
                                                                                true
                                                                },
                                                                colors =
                                                                        ButtonDefaults.buttonColors(
                                                                                backgroundColor =
                                                                                        VetNutriColors
                                                                                                .Error,
                                                                                contentColor =
                                                                                        Color.White
                                                                        ),
                                                                modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                                Text(
                                                                        "Vider la base de données des animaux"
                                                                )
                                                        }

                                                        // Boutons pour supprimer les références
                                                        // nutritionnelles, équations et
                                                        // bibliographies
                                                        Button(
                                                                onClick = {
                                                                        isReferenceDeleteDialogVisible =
                                                                                true
                                                                },
                                                                colors =
                                                                        ButtonDefaults.buttonColors(
                                                                                backgroundColor =
                                                                                        VetNutriColors
                                                                                                .Error,
                                                                                contentColor =
                                                                                        Color.White
                                                                        ),
                                                                modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                                Text(
                                                                        "Vider la base de données des références nutritionnelles"
                                                                )
                                                        }

                                                        Button(
                                                                onClick = {
                                                                        isEquationDeleteDialogVisible =
                                                                                true
                                                                },
                                                                colors =
                                                                        ButtonDefaults.buttonColors(
                                                                                backgroundColor =
                                                                                        VetNutriColors
                                                                                                .Error,
                                                                                contentColor =
                                                                                        Color.White
                                                                        ),
                                                                modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                                Text(
                                                                        "Vider la base de données des équations"
                                                                )
                                                        }

                                                        Button(
                                                                onClick = {
                                                                        isBiblioDeleteDialogVisible =
                                                                                true
                                                                },
                                                                colors =
                                                                        ButtonDefaults.buttonColors(
                                                                                backgroundColor =
                                                                                        VetNutriColors
                                                                                                .Error,
                                                                                contentColor =
                                                                                        Color.White
                                                                        ),
                                                                modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                                Text(
                                                                        "Vider la base de données des bibliographies"
                                                                )
                                                        }
                                                }
                                        }
                                }
                        }
                }
        }

        // Dialogue de confirmation pour vider la base de données
        if (isDialogVisible) {
                AlertDialog(
                        onDismissRequest = { isDialogVisible = false },
                        title = { Text("Confirmation") },
                        text = {
                                Text(
                                        "Êtes-vous sûr de vouloir supprimer TOUS les aliments de la base de données ? Cette action est irréversible."
                                )
                        },
                        confirmButton = {
                                Button(
                                        onClick = {
                                                isDialogVisible = false
                                                isProcessing = true
                                                coroutineScope.launch {
                                                        try {
                                                                val count =
                                                                        viewModel.clearAllFoods()
                                                                resultMessage =
                                                                        "$count aliments ont été supprimés avec succès."
                                                                // Rafraîchir la liste des aliments
                                                                onFoodListRefresh()
                                                        } catch (e: Exception) {
                                                                resultMessage =
                                                                        "Erreur lors de la suppression : ${e.message}"
                                                        } finally {
                                                                isProcessing = false
                                                        }
                                                }
                                        },
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor = VetNutriColors.Error,
                                                        contentColor = Color.White
                                                )
                                ) { Text("Oui, vider la base") }
                        },
                        dismissButton = {
                                Button(onClick = { isDialogVisible = false }) { Text("Annuler") }
                        }
                )
        }

        // Dialogue de confirmation pour vider la base des animaux
        if (isAnimalDeleteDialogVisible) {
                AlertDialog(
                        onDismissRequest = { isAnimalDeleteDialogVisible = false },
                        title = { Text("Confirmation") },
                        text = {
                                Text(
                                        "Êtes-vous sûr de vouloir supprimer TOUS les animaux de la base de données ? Cette action est irréversible."
                                )
                        },
                        confirmButton = {
                                Button(
                                        onClick = {
                                                isAnimalDeleteDialogVisible = false
                                                isProcessing = true
                                                coroutineScope.launch {
                                                        try {
                                                                val count =
                                                                        viewModel.clearAllAnimals()
                                                                resultMessage =
                                                                        "$count animaux ont été supprimés avec succès."
                                                                // Rafraîchir la liste des animaux
                                                                onAnimalListRefresh()
                                                        } catch (e: Exception) {
                                                                resultMessage =
                                                                        "Erreur lors de la suppression : ${e.message}"
                                                        } finally {
                                                                isProcessing = false
                                                        }
                                                }
                                        },
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor = VetNutriColors.Error,
                                                        contentColor = Color.White
                                                )
                                ) { Text("Oui, vider la base") }
                        },
                        dismissButton = {
                                Button(onClick = { isAnimalDeleteDialogVisible = false }) {
                                        Text("Annuler")
                                }
                        }
                )
        }

        // Dialogue de confirmation pour vider la base des références nutritionnelles
        if (isReferenceDeleteDialogVisible) {
                AlertDialog(
                        onDismissRequest = { isReferenceDeleteDialogVisible = false },
                        title = { Text("Confirmation") },
                        text = {
                                Text(
                                        "Êtes-vous sûr de vouloir supprimer TOUTES les références nutritionnelles de la base de données ? Cette action est irréversible."
                                )
                        },
                        confirmButton = {
                                Button(
                                        onClick = {
                                                println(
                                                        "DEBUG SettingsView: Bouton 'Vider les références' cliqué"
                                                )
                                                isReferenceDeleteDialogVisible = false
                                                isProcessing = true
                                                coroutineScope.launch {
                                                        try {
                                                                println(
                                                                        "DEBUG SettingsView: Appel de viewModel.clearAllReferences()"
                                                                )
                                                                val count =
                                                                        viewModel
                                                                                .clearAllReferences()
                                                                println(
                                                                        "DEBUG SettingsView: clearAllReferences() a retourné: $count"
                                                                )
                                                                resultMessage =
                                                                        "$count références nutritionnelles ont été supprimées avec succès."
                                                        } catch (e: Exception) {
                                                                println(
                                                                        "DEBUG SettingsView: ERREUR dans clearAllReferences(): ${e.message}"
                                                                )
                                                                e.printStackTrace()
                                                                resultMessage =
                                                                        "Erreur lors de la suppression : ${e.message}"
                                                        } finally {
                                                                isProcessing = false
                                                        }
                                                }
                                        },
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor = VetNutriColors.Error,
                                                        contentColor = Color.White
                                                )
                                ) { Text("Oui, vider la base") }
                        },
                        dismissButton = {
                                Button(onClick = { isReferenceDeleteDialogVisible = false }) {
                                        Text("Annuler")
                                }
                        }
                )
        }

        // Dialogue de confirmation pour vider la base des équations
        if (isEquationDeleteDialogVisible) {
                AlertDialog(
                        onDismissRequest = { isEquationDeleteDialogVisible = false },
                        title = { Text("Confirmation") },
                        text = {
                                Text(
                                        "Êtes-vous sûr de vouloir supprimer TOUTES les équations de la base de données ? Cette action est irréversible."
                                )
                        },
                        confirmButton = {
                                Button(
                                        onClick = {
                                                println(
                                                        "DEBUG SettingsView: Bouton 'Vider les équations' cliqué"
                                                )
                                                isEquationDeleteDialogVisible = false
                                                isProcessing = true
                                                coroutineScope.launch {
                                                        try {
                                                                println(
                                                                        "DEBUG SettingsView: Appel de viewModel.clearAllEquations()"
                                                                )
                                                                val count =
                                                                        viewModel
                                                                                .clearAllEquations()
                                                                println(
                                                                        "DEBUG SettingsView: clearAllEquations() a retourné: $count"
                                                                )
                                                                resultMessage =
                                                                        "$count équations ont été supprimées avec succès."
                                                        } catch (e: Exception) {
                                                                println(
                                                                        "DEBUG SettingsView: ERREUR dans clearAllEquations(): ${e.message}"
                                                                )
                                                                e.printStackTrace()
                                                                resultMessage =
                                                                        "Erreur lors de la suppression : ${e.message}"
                                                        } finally {
                                                                isProcessing = false
                                                        }
                                                }
                                        },
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor = VetNutriColors.Error,
                                                        contentColor = Color.White
                                                )
                                ) { Text("Oui, vider la base") }
                        },
                        dismissButton = {
                                Button(onClick = { isEquationDeleteDialogVisible = false }) {
                                        Text("Annuler")
                                }
                        }
                )
        }

        // Dialogue de confirmation pour vider la base des bibliographies
        if (isBiblioDeleteDialogVisible) {
                AlertDialog(
                        onDismissRequest = { isBiblioDeleteDialogVisible = false },
                        title = { Text("Confirmation") },
                        text = {
                                Text(
                                        "Êtes-vous sûr de vouloir supprimer TOUTES les références bibliographiques de la base de données ? Cette action est irréversible."
                                )
                        },
                        confirmButton = {
                                Button(
                                        onClick = {
                                                println(
                                                        "DEBUG SettingsView: Bouton 'Vider les bibliographies' cliqué"
                                                )
                                                isBiblioDeleteDialogVisible = false
                                                isProcessing = true
                                                coroutineScope.launch {
                                                        try {
                                                                println(
                                                                        "DEBUG SettingsView: Appel de viewModel.clearAllBiblioRefs()"
                                                                )
                                                                val count =
                                                                        viewModel
                                                                                .clearAllBiblioRefs()
                                                                println(
                                                                        "DEBUG SettingsView: clearAllBiblioRefs() a retourné: $count"
                                                                )
                                                                resultMessage =
                                                                        "$count références bibliographiques ont été supprimées avec succès."
                                                        } catch (e: Exception) {
                                                                println(
                                                                        "DEBUG SettingsView: ERREUR dans clearAllBiblioRefs(): ${e.message}"
                                                                )
                                                                e.printStackTrace()
                                                                resultMessage =
                                                                        "Erreur lors de la suppression : ${e.message}"
                                                        } finally {
                                                                isProcessing = false
                                                        }
                                                }
                                        },
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor = VetNutriColors.Error,
                                                        contentColor = Color.White
                                                )
                                ) { Text("Oui, vider la base") }
                        },
                        dismissButton = {
                                Button(onClick = { isBiblioDeleteDialogVisible = false }) {
                                        Text("Annuler")
                                }
                        }
                )
        }

        // Affichage du résultat
        if (resultMessage.isNotEmpty()) {
                Snackbar(
                        modifier = Modifier.padding(16.dp),
                        action = { TextButton(onClick = { resultMessage = "" }) { Text("OK") } }
                ) { Text(resultMessage) }
        }

        // Indicateur de progression pendant le traitement
        if (isProcessing) {
                Box(
                        modifier =
                                Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = VetNutriColors.Primary) }
        }

        // Dialogue d'alerte pour l'importation des références nutritionnelles
        if (showImportDialog) {
                AlertDialog(
                        onDismissRequest = {
                                showImportDialog = false
                                importViewModel.resetImportResult()
                        },
                        title = {
                                Text(
                                        "Résultat de l'importation",
                                        style = MaterialTheme.typography.h6,
                                        color =
                                                if (importDialogMessage.startsWith("✅"))
                                                        VetNutriColors.Primary
                                                else VetNutriColors.Error
                                )
                        },
                        text = {
                                Text(importDialogMessage, style = MaterialTheme.typography.body2)
                        },
                        confirmButton = {
                                Button(
                                        onClick = {
                                                showImportDialog = false
                                                importViewModel.resetImportResult()
                                        },
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor = VetNutriColors.Primary,
                                                        contentColor = Color.White
                                                )
                                ) { Text("OK") }
                        },
                        backgroundColor = MaterialTheme.colors.surface
                )
        }
}

/** Composant de liste de nutriments avec checkboxes */
@Composable
fun <T : Nutrient> NutrientCheckboxList(
        nutrients: SnapshotStateList<T>,
        getAllNutrients: () -> List<T>,
        onNutrientsUpdated: (List<T>) -> Unit
) {
        // Créer une map des éléments actifs (cochés)
        val activeItems =
                remember(nutrients) {
                        val map = mutableStateMapOf<T, Boolean>()
                        getAllNutrients().forEach { nutrient ->
                                map[nutrient] = nutrients.contains(nutrient)
                        }
                        map
                }

        // Fonction pour mettre à jour la liste et notifier le ViewModel
        fun updateList(newList: List<T>) {
                // Mettre à jour la liste originale
                nutrients.clear()
                nutrients.addAll(newList)

                // Notifier le ViewModel immédiatement pour sauvegarder les changements
                onNutrientsUpdated(newList)
        }

        // Ajouter un titre pour indiquer clairement la présence de la liste
        Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                        "Liste des nutriments disponibles (${getAllNutrients().size})",
                        style = MaterialTheme.typography.subtitle2,
                        fontWeight = FontWeight.Bold,
                        color = VetNutriColors.Primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                )

                Column(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .height(400.dp)
                                        .border(
                                                2.dp,
                                                VetNutriColors.Primary,
                                                RoundedCornerShape(8.dp)
                                        )
                                        .verticalScroll(rememberScrollState())
                                        .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                        // Afficher tous les nutriments disponibles avec des cases à cocher
                        getAllNutrients().forEachIndexed { index, nutrient ->
                                val isChecked = activeItems[nutrient] ?: false
                                val backgroundColor =
                                        if (index % 2 == 0) Color.White else Color(0xFFF5F5F5)

                                Card(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        elevation = 2.dp,
                                        backgroundColor = backgroundColor
                                ) {
                                        Row(
                                                modifier =
                                                        Modifier.fillMaxWidth()
                                                                .padding(
                                                                        horizontal = 16.dp,
                                                                        vertical = 12.dp
                                                                ),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                                // Checkbox pour activer/désactiver le nutriment
                                                Checkbox(
                                                        checked = isChecked,
                                                        onCheckedChange = { isChecked ->
                                                                activeItems[nutrient] = isChecked

                                                                // Mettre à jour la liste des
                                                                // nutriments actifs
                                                                val activeList =
                                                                        getAllNutrients().filter {
                                                                                activeItems[it] ==
                                                                                        true
                                                                        }

                                                                // Utiliser la fonction de mise à
                                                                // jour commune
                                                                updateList(activeList)
                                                        },
                                                        colors =
                                                                CheckboxDefaults.colors(
                                                                        checkedColor =
                                                                                VetNutriColors
                                                                                        .Primary,
                                                                        uncheckedColor = Color.Gray
                                                                )
                                                )

                                                // Nom du nutriment avec un style plus visible
                                                Text(
                                                        text = getNutrientDisplayName(nutrient),
                                                        style = MaterialTheme.typography.body1,
                                                        fontWeight =
                                                                if (isChecked) FontWeight.Bold
                                                                else FontWeight.Normal,
                                                        color =
                                                                if (isChecked)
                                                                        VetNutriColors.Primary
                                                                else Color.Black,
                                                        modifier = Modifier.weight(1f)
                                                )
                                        }
                                }
                        }
                }
        }
}

/** Obtient le nom d'affichage d'un nutriment selon son type */
fun getNutrientDisplayName(nutrient: Nutrient): String {
        return nutrient.label
}

/** Sections disponibles dans les paramètres */
enum class SettingsSection(val title: String) {
        INTERFACE("Interface"),
        NUTRIMENTS("Nutriments"),
        PREFERENCES("Préférences"),
        IMPORTATION("Importation"),
        ADMINISTRATION("Administration")
}

/** Composant pour la section des préférences */
@Composable
private fun PreferencesSection(
        modifier: Modifier = Modifier,
        onSpeciesClick: (fr.vetbrain.vetnutri_mp.Enumer.Espece) -> Unit = {}
) {
        // Créer l'instance PreferencesStorage pour Desktop
        val preferencesStorage = remember {
                try {
                        // Utiliser la fonction helper createPreferencesStorage
                        fr.vetbrain.vetnutri_mp.Utils.createPreferencesStorage()
                } catch (e: Exception) {
                        println("Erreur lors de la création de PreferencesStorage: ${e.message}")
                        null
                }
        }

        if (preferencesStorage != null) {
                // Créer le repository des préférences
                val preferencesRepository = remember {
                        fr.vetbrain.vetnutri_mp.Repository.PreferencesRepository(preferencesStorage)
                }

                // Utiliser le vrai système de persistance
                PreferencesContentWithPersistence(
                        preferencesRepository = preferencesRepository,
                        modifier = modifier,
                        onSpeciesClick = onSpeciesClick
                )
        } else {
                // Fallback temporaire si PreferencesStorage n'est pas disponible
                PreferencesContentSimplified(modifier = modifier)
        }
}

/** Contenu des préférences avec persistance réelle */
@Composable
private fun PreferencesContentWithPersistence(
        preferencesRepository: fr.vetbrain.vetnutri_mp.Repository.PreferencesRepository,
        modifier: Modifier = Modifier,
        onSpeciesClick: (fr.vetbrain.vetnutri_mp.Enumer.Espece) -> Unit = {}
) {
        // État pour les préférences chargées
        var preferencesLoaded by remember { mutableStateOf(false) }
        var currentPreferences by remember {
                mutableStateOf<fr.vetbrain.vetnutri_mp.Data.PreferencesApplication?>(null)
        }
        var isLoading by remember { mutableStateOf(false) }

        // Charger les préférences au démarrage
        LaunchedEffect(Unit) {
                try {
                        isLoading = true
                        preferencesRepository.loadPreferences()
                        currentPreferences = preferencesRepository.preferences
                        preferencesLoaded = true
                        println("DEBUG: Préférences chargées avec succès")
                } catch (e: Exception) {
                        println("Erreur lors du chargement des préférences: ${e.message}")
                        // Utiliser des préférences par défaut
                        currentPreferences = fr.vetbrain.vetnutri_mp.Data.PreferencesApplication()
                        preferencesLoaded = true
                } finally {
                        isLoading = false
                }
        }

        if (isLoading) {
                // Indicateur de chargement
                Box(
                        modifier = modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = VetNutriColors.Primary)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Chargement des préférences...")
                        }
                }
        } else if (preferencesLoaded && currentPreferences != null) {
                Column(
                        modifier = modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                        Text(
                                text = "Expression des besoins par espèce",
                                style = MaterialTheme.typography.h6,
                                color = VetNutriColors.Primary
                        )

                        Text(
                                text =
                                        "Définissez pour chaque espèce comment exprimer les besoins nutritionnels (sauvegarde automatique)",
                                style = MaterialTheme.typography.body2,
                                color = Color.Gray
                        )

                        // Afficher les préférences pour chaque espèce (sauf CH qui est "ALL")
                        fr.vetbrain.vetnutri_mp.Enumer.Espece.valuesExcept(
                                        fr.vetbrain.vetnutri_mp.Enumer.Espece.CH
                                )
                                .forEach { espece ->
                                        SpeciesPreferenceCardWithPersistence(
                                                species = espece,
                                                preferencesRepository = preferencesRepository,
                                                currentPreferences = currentPreferences!!,
                                                onPreferencesChanged = { newPreferences ->
                                                        currentPreferences = newPreferences
                                                },
                                                onSpeciesClick = onSpeciesClick
                                        )
                                }

                        // Informations sur la persistance
                        Card(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = VetNutriColors.Primary.copy(alpha = 0.1f),
                                elevation = 1.dp
                        ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = "Persistance active",
                                                        tint = VetNutriColors.Primary
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                        text = "Persistance active",
                                                        style = MaterialTheme.typography.subtitle2,
                                                        fontWeight = FontWeight.Bold,
                                                        color = VetNutriColors.Primary
                                                )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                                text =
                                                        "Vos préférences sont automatiquement sauvegardées et seront restaurées au prochain démarrage.",
                                                style = MaterialTheme.typography.body2,
                                                color = Color.Gray
                                        )
                                }
                        }
                }
        }
}

/** Card de préférences avec persistance pour une espèce */
@Composable
private fun SpeciesPreferenceCardWithPersistence(
        species: fr.vetbrain.vetnutri_mp.Enumer.Espece,
        preferencesRepository: fr.vetbrain.vetnutri_mp.Repository.PreferencesRepository,
        currentPreferences: fr.vetbrain.vetnutri_mp.Data.PreferencesApplication,
        onPreferencesChanged: (fr.vetbrain.vetnutri_mp.Data.PreferencesApplication) -> Unit,
        onSpeciesClick: (fr.vetbrain.vetnutri_mp.Enumer.Espece) -> Unit
) {
        var expanded by remember { mutableStateOf(false) }
        var isSaving by remember { mutableStateOf(false) }

        val speciesPreferences = currentPreferences.getPreferencesEspece(species)
        val currentExpressionType = speciesPreferences.getTypeExpressionBesoinEnum()

        Card(
                modifier = Modifier.fillMaxWidth().clickable { onSpeciesClick(species) },
                backgroundColor = Color.White,
                elevation = 2.dp
        ) {
                Column(modifier = Modifier.padding(16.dp)) {
                        // En-tête avec nom de l'espèce
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Column {
                                        Text(
                                                text = species.label,
                                                style = MaterialTheme.typography.subtitle1,
                                                fontWeight = FontWeight.Bold
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                        text =
                                                                "Expression: ${currentExpressionType.displayName}",
                                                        style = MaterialTheme.typography.body2,
                                                        color = VetNutriColors.Primary
                                                )
                                                if (isSaving) {
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        CircularProgressIndicator(
                                                                modifier = Modifier.size(16.dp),
                                                                color = VetNutriColors.Primary,
                                                                strokeWidth = 2.dp
                                                        )
                                                }
                                        }
                                }

                                IconButton(onClick = { expanded = !expanded }) {
                                        Icon(
                                                imageVector =
                                                        if (expanded) Icons.Default.ExpandLess
                                                        else Icons.Default.ExpandMore,
                                                contentDescription =
                                                        if (expanded) "Réduire" else "Développer"
                                        )
                                }
                        }

                        // Contenu développable
                        if (expanded) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Divider()
                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                        text = "Type d'expression des besoins:",
                                        style = MaterialTheme.typography.subtitle2,
                                        fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Options de type d'expression
                                fr.vetbrain.vetnutri_mp.Enumer.TypeExpressionBesoin.values()
                                        .forEach { type ->
                                                Row(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .clickable(
                                                                                enabled = !isSaving
                                                                        ) {
                                                                                // Sauvegarder avec
                                                                                // persistance
                                                                                kotlinx.coroutines
                                                                                        .GlobalScope
                                                                                        .launch {
                                                                                                try {
                                                                                                        isSaving =
                                                                                                                true

                                                                                                        // Mettre à jour les préférences
                                                                                                        val updatedSpeciesPrefs =
                                                                                                                speciesPreferences
                                                                                                                        .copy(
                                                                                                                                typeExpressionBesoinId =
                                                                                                                                        type.id
                                                                                                                        )

                                                                                                        val updatedPrefs =
                                                                                                                currentPreferences
                                                                                                                        .updatePreferencesEspece(
                                                                                                                                updatedSpeciesPrefs
                                                                                                                        )

                                                                                                        // Sauvegarder dans PreferencesStorage
                                                                                                        preferencesRepository
                                                                                                                .savePreferences(
                                                                                                                        updatedPrefs
                                                                                                                )

                                                                                                        // Mettre à jour l'UI
                                                                                                        onPreferencesChanged(
                                                                                                                updatedPrefs
                                                                                                        )

                                                                                                        println(
                                                                                                                "DEBUG: Préférence sauvegardée pour ${species.label}: ${type.displayName}"
                                                                                                        )
                                                                                                } catch (
                                                                                                        e:
                                                                                                                Exception) {
                                                                                                        println(
                                                                                                                "Erreur lors de la sauvegarde: ${e.message}"
                                                                                                        )
                                                                                                } finally {
                                                                                                        isSaving =
                                                                                                                false
                                                                                                }
                                                                                        }
                                                                        }
                                                                        .padding(vertical = 4.dp),
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        RadioButton(
                                                                selected =
                                                                        currentExpressionType ==
                                                                                type,
                                                                onClick = null, // Géré par le
                                                                // clickable du Row
                                                                enabled = !isSaving
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Column(modifier = Modifier.weight(1f)) {
                                                                Text(
                                                                        text = type.displayName,
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .body1,
                                                                        color =
                                                                                if (isSaving)
                                                                                        Color.Gray
                                                                                else Color.Black
                                                                )
                                                                Text(
                                                                        text =
                                                                                type.unitReqEnum
                                                                                        .label,
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .body2,
                                                                        color = Color.Gray
                                                                )
                                                        }
                                                }
                                        }
                        }
                }
        }
}

/** Contenu simplifié des préférences qui fonctionne sans PreferencesStorage */
@Composable
private fun PreferencesContentSimplified(modifier: Modifier = Modifier) {
        // État local pour simuler les préférences (sera remplacé par le vrai système)
        val speciesExpressionTypes = remember {
                mutableStateMapOf<
                        fr.vetbrain.vetnutri_mp.Enumer.Espece,
                        fr.vetbrain.vetnutri_mp.Enumer.TypeExpressionBesoin>()
        }

        Column(
                modifier = modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
                Text(
                        text = "Expression des besoins par espèce",
                        style = MaterialTheme.typography.h6,
                        color = VetNutriColors.Primary
                )

                Text(
                        text =
                                "Définissez pour chaque espèce comment exprimer les besoins nutritionnels",
                        style = MaterialTheme.typography.body2,
                        color = Color.Gray
                )

                // Afficher les préférences pour chaque espèce (sauf CH qui est "ALL")
                fr.vetbrain.vetnutri_mp.Enumer.Espece.valuesExcept(
                                fr.vetbrain.vetnutri_mp.Enumer.Espece.CH
                        )
                        .forEach { espece ->
                                SpeciesPreferenceCardSimplified(
                                        species = espece,
                                        currentExpressionType = speciesExpressionTypes[espece]
                                                        ?: fr.vetbrain.vetnutri_mp.Enumer
                                                                .TypeExpressionBesoin.DEFAULT,
                                        onExpressionTypeChanged = { newType ->
                                                speciesExpressionTypes[espece] = newType
                                                // TODO: Sauvegarder dans PreferencesStorage
                                                println(
                                                        "Préférence mise à jour pour ${espece.label}: ${newType.displayName}"
                                                )
                                        }
                                )
                        }

                // Note d'information
                Card(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = VetNutriColors.Primary.copy(alpha = 0.1f),
                        elevation = 1.dp
                ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = "Information",
                                                tint = VetNutriColors.Primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                                text = "Persistance des données",
                                                style = MaterialTheme.typography.subtitle2,
                                                fontWeight = FontWeight.Bold,
                                                color = VetNutriColors.Primary
                                        )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                        text =
                                                "Les préférences sont actuellement stockées temporairement. Le système de persistance multiplateforme sera intégré dans une prochaine version.",
                                        style = MaterialTheme.typography.body2,
                                        color = Color.Gray
                                )
                        }
                }
        }
}

/** Card de préférences simplifiée pour une espèce */
@Composable
private fun SpeciesPreferenceCardSimplified(
        species: fr.vetbrain.vetnutri_mp.Enumer.Espece,
        currentExpressionType: fr.vetbrain.vetnutri_mp.Enumer.TypeExpressionBesoin,
        onExpressionTypeChanged: (fr.vetbrain.vetnutri_mp.Enumer.TypeExpressionBesoin) -> Unit
) {
        var expanded by remember { mutableStateOf(false) }

        Card(
                modifier =
                        Modifier.fillMaxWidth().clickable {
                                onExpressionTypeChanged(currentExpressionType)
                        },
                backgroundColor = Color.White,
                elevation = 2.dp
        ) {
                Column(modifier = Modifier.padding(16.dp)) {
                        // En-tête avec nom de l'espèce
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Column {
                                        Text(
                                                text = species.label,
                                                style = MaterialTheme.typography.subtitle1,
                                                fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                                text =
                                                        "Expression: ${currentExpressionType.displayName}",
                                                style = MaterialTheme.typography.body2,
                                                color = VetNutriColors.Primary
                                        )
                                }

                                IconButton(onClick = { expanded = !expanded }) {
                                        Icon(
                                                imageVector =
                                                        if (expanded) Icons.Default.ExpandLess
                                                        else Icons.Default.ExpandMore,
                                                contentDescription =
                                                        if (expanded) "Réduire" else "Développer"
                                        )
                                }
                        }

                        // Contenu développable
                        if (expanded) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Divider()
                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                        text = "Type d'expression des besoins:",
                                        style = MaterialTheme.typography.subtitle2,
                                        fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Options de type d'expression
                                fr.vetbrain.vetnutri_mp.Enumer.TypeExpressionBesoin.values()
                                        .forEach { type ->
                                                Row(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .clickable {
                                                                                onExpressionTypeChanged(
                                                                                        type
                                                                                )
                                                                        }
                                                                        .padding(vertical = 4.dp),
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        RadioButton(
                                                                selected =
                                                                        currentExpressionType ==
                                                                                type,
                                                                onClick = {
                                                                        onExpressionTypeChanged(
                                                                                type
                                                                        )
                                                                }
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Column {
                                                                Text(
                                                                        text = type.displayName,
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .body1
                                                                )
                                                                Text(
                                                                        text =
                                                                                type.unitReqEnum
                                                                                        .label,
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .body2,
                                                                        color = Color.Gray
                                                                )
                                                        }
                                                }
                                        }
                        }
                }
        }
}
