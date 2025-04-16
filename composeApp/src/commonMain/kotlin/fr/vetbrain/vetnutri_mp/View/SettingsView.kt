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
        onImportAnimals: () -> Unit,
        onBack: () -> Unit,
        onAnimalListRefresh: () -> Unit,
        onFoodListRefresh: () -> Unit
) {
        // État pour le dialogue de confirmation de suppression
        var isDialogVisible by remember { mutableStateOf(false) }
        var isProcessing by remember { mutableStateOf(false) }
        var resultMessage by remember { mutableStateOf("") }
        var isAnimalDeleteDialogVisible by remember { mutableStateOf(false) }
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
                                SettingsSection.IMPORTATION -> {
                                        // Section pour l'importation des données
                                        Section(title = "Importation des données") {
                                                Column(
                                                        verticalArrangement =
                                                                Arrangement.spacedBy(8.dp)
                                                ) {
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

                                                        // Bouton pour importer des aliments avec
                                                        // lambda pour éviter
                                                        // l'ambiguïté
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
        IMPORTATION("Importation"),
        ADMINISTRATION("Administration")
}
