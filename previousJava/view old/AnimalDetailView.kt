package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.*
import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Localization.translateNonComposable
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.ViewModel.*
import kotlinx.coroutines.launch

enum class MainSection(val title: String) {
        ANIMAL("general.animal".translateNonComposable()),
        CONSULTATIONS("general.consultations".translateNonComposable()),
        RATIONS("general.rations".translateNonComposable())
}

enum class AnimalTab(val title: String) {
        IDENTITY("general.identity".translateNonComposable()),
        WEIGHT("general.weight".translateNonComposable())
}

@Composable
fun AnimalDetailView(
        viewModel: AnimalDetailViewModel,
        settingsViewModel: SettingsViewModel,
        onNavigateBack: () -> Unit,
        onNavigateToRations: () -> Unit,
        modifier: Modifier = Modifier,
        initialSection: MainSection = MainSection.ANIMAL
) {
        val animal by viewModel.animal.collectAsState()
        val selectedConsultation by viewModel.selectedConsultation.collectAsState()
        var showConsultationDetail by remember { mutableStateOf(false) }
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        var selectedSection by remember { mutableStateOf(initialSection) }
        var selectedTab by remember { mutableStateOf(0) }

        val showSettings by settingsViewModel.showSettings.collectAsState()

        if (showSettings) {
                SettingsDialog(
                        viewModel = settingsViewModel,
                        onDismiss = { settingsViewModel.hideSettings() }
                )
        }

        BoxWithConstraints(modifier = modifier.fillMaxSize()) {
                val isWideScreen = maxWidth > AppSizes.breakpointWideScreen

                if (isWideScreen) {
                        Row(modifier = Modifier.fillMaxSize()) {
                                // Menu latéral permanent
                                Surface(
                                        modifier = Modifier.width(AppSizes.drawerWidth),
                                        elevation = AppSizes.elevationSmall
                                ) {
                                        DrawerContent(
                                                selectedSection = selectedSection,
                                                onSectionSelected = { section ->
                                                        selectedSection = section
                                                },
                                                onNavigateBack = onNavigateBack,
                                                onSettingsClick = {
                                                        settingsViewModel.showSettings()
                                                },
                                                selectedConsultation = selectedConsultation
                                        )
                                }

                                // Contenu principal
                                Surface(modifier = Modifier.weight(1f)) {
                                        MainContent(
                                                selectedSection = selectedSection,
                                                selectedTab = selectedTab,
                                                onTabSelected = { selectedTab = it },
                                                viewModel = viewModel,
                                                settingsViewModel = settingsViewModel
                                        )
                                }
                        }
                } else {
                        ModalDrawer(
                                drawerState = drawerState,
                                drawerContent = {
                                        DrawerContent(
                                                selectedSection = selectedSection,
                                                onSectionSelected = { section ->
                                                        selectedSection = section
                                                        scope.launch { drawerState.close() }
                                                },
                                                onNavigateBack = onNavigateBack,
                                                onSettingsClick = {
                                                        settingsViewModel.showSettings()
                                                        scope.launch { drawerState.close() }
                                                },
                                                selectedConsultation = selectedConsultation
                                        )
                                }
                        ) {
                                MainContent(
                                        selectedSection = selectedSection,
                                        selectedTab = selectedTab,
                                        onTabSelected = { selectedTab = it },
                                        viewModel = viewModel,
                                        settingsViewModel = settingsViewModel,
                                        onMenuClick = { scope.launch { drawerState.open() } }
                                )
                        }
                }
        }
}

@Composable
private fun DrawerContent(
        selectedSection: MainSection,
        onSectionSelected: (MainSection) -> Unit,
        onNavigateBack: () -> Unit,
        onSettingsClick: () -> Unit = {},
        selectedConsultation: ConsultationEv? = null
) {
        Column(
                modifier = Modifier.fillMaxHeight().padding(AppSizes.paddingMedium),
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
        ) {
                // Bouton retour
                Row(
                        modifier = Modifier.clickable(onClick = onNavigateBack),
                        horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        Icon(
                                AppIcons.ArrowBack,
                                contentDescription = "Retour",
                                tint = MaterialTheme.colors.primary
                        )
                        Text(
                                text = "Retour",
                                style =
                                        MaterialTheme.typography.body1.copy(
                                                color = MaterialTheme.colors.primary
                                        )
                        )
                }

                Divider()

                // Section Animal
                Row(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .clickable { onSectionSelected(MainSection.ANIMAL) }
                                        .padding(AppSizes.paddingSmall),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        Row(
                                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Icon(
                                        AppIcons.Pets,
                                        contentDescription = MainSection.ANIMAL.title,
                                        tint =
                                                if (selectedSection == MainSection.ANIMAL)
                                                        MaterialTheme.colors.primary
                                                else MaterialTheme.colors.onSurface
                                )
                                Text(
                                        text = MainSection.ANIMAL.title,
                                        style =
                                                if (selectedSection == MainSection.ANIMAL)
                                                        MaterialTheme.typography.body1.copy(
                                                                color = MaterialTheme.colors.primary
                                                        )
                                                else MaterialTheme.typography.body1
                                )
                        }
                }

                // Section Consultations avec sous-section Rations
                Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                ) {
                        // Consultations
                        Row(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .clickable {
                                                        onSectionSelected(MainSection.CONSULTATIONS)
                                                }
                                                .padding(AppSizes.paddingSmall),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Row(
                                        horizontalArrangement =
                                                Arrangement.spacedBy(AppSizes.paddingSmall),
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Icon(
                                                AppIcons.DateRange,
                                                contentDescription =
                                                        MainSection.CONSULTATIONS.title,
                                                tint =
                                                        if (selectedSection ==
                                                                        MainSection.CONSULTATIONS
                                                        )
                                                                MaterialTheme.colors.primary
                                                        else MaterialTheme.colors.onSurface
                                        )
                                        Text(
                                                text = MainSection.CONSULTATIONS.title,
                                                style =
                                                        if (selectedSection ==
                                                                        MainSection.CONSULTATIONS
                                                        )
                                                                MaterialTheme.typography.body1.copy(
                                                                        color =
                                                                                MaterialTheme.colors
                                                                                        .primary
                                                                )
                                                        else MaterialTheme.typography.body1
                                        )
                                }
                        }

                        // Date de la consultation sélectionnée
                        if (selectedSection == MainSection.CONSULTATIONS &&
                                        selectedConsultation != null
                        ) {
                                Text(
                                        text = selectedConsultation.date?.toString()
                                                        ?: "Date non spécifiée",
                                        style = MaterialTheme.typography.caption,
                                        modifier =
                                                Modifier.padding(
                                                        start =
                                                                AppSizes.paddingLarge +
                                                                        AppSizes.paddingSmall
                                                )
                                )
                        }

                        // Rations (sous-section)
                        Row(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .clickable {
                                                        onSectionSelected(MainSection.RATIONS)
                                                }
                                                .padding(
                                                        start = AppSizes.paddingLarge,
                                                        top = AppSizes.paddingSmall,
                                                        end = AppSizes.paddingSmall,
                                                        bottom = AppSizes.paddingSmall
                                                ),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Row(
                                        horizontalArrangement =
                                                Arrangement.spacedBy(AppSizes.paddingSmall),
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Icon(
                                                AppIcons.List,
                                                contentDescription = MainSection.RATIONS.title,
                                                tint =
                                                        if (selectedSection == MainSection.RATIONS)
                                                                MaterialTheme.colors.primary
                                                        else MaterialTheme.colors.onSurface
                                        )
                                        Text(
                                                text = MainSection.RATIONS.title,
                                                style =
                                                        if (selectedSection == MainSection.RATIONS)
                                                                MaterialTheme.typography.body1.copy(
                                                                        color =
                                                                                MaterialTheme.colors
                                                                                        .primary
                                                                )
                                                        else MaterialTheme.typography.body1
                                        )
                                }
                        }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bouton paramètres
                Row(
                        modifier = Modifier.clickable(onClick = onSettingsClick),
                        horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        Icon(
                                AppIcons.Settings,
                                contentDescription = "settings.title".translate(),
                                tint = MaterialTheme.colors.onSurface
                        )
                        Text(
                                text = "settings.title".translate(),
                                style = MaterialTheme.typography.body1
                        )
                }
        }
}

@Composable
private fun MainContent(
        selectedSection: MainSection,
        selectedTab: Int,
        onTabSelected: (Int) -> Unit,
        viewModel: AnimalDetailViewModel,
        settingsViewModel: SettingsViewModel,
        onMenuClick: (() -> Unit)? = null
) {
        var showAddConsultationDialog by remember { mutableStateOf(false) }
        var showAddRationDialog by remember { mutableStateOf(false) }
        val selectedConsultation by viewModel.selectedConsultation.collectAsState()
        var showConsultationDetail by remember { mutableStateOf(false) }

        Column(modifier = Modifier.fillMaxSize()) {
                // En-tête avec bouton menu si nécessaire
                Row(
                        modifier = Modifier.fillMaxWidth().padding(AppSizes.paddingMedium),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        if (onMenuClick != null) {
                                IconButton(onClick = onMenuClick) {
                                        Icon(AppIcons.Menu, contentDescription = "Menu")
                                }
                        }
                        Row(
                                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Text(
                                        text = selectedSection.title,
                                        style = MaterialTheme.typography.h6
                                )
                                if (selectedSection == MainSection.CONSULTATIONS) {
                                        IconButton(onClick = { showAddConsultationDialog = true }) {
                                                Icon(
                                                        AppIcons.Add,
                                                        contentDescription =
                                                                "general.add_consultation".translate()
                                                )
                                        }
                                } else if (selectedSection == MainSection.RATIONS) {
                                        IconButton(onClick = { showAddRationDialog = true }) {
                                                Icon(
                                                        AppIcons.Add,
                                                        contentDescription =
                                                                "general.add_ration".translate()
                                                )
                                        }
                                }
                        }
                        // Espace pour l'alignement
                        if (onMenuClick != null) {
                                Spacer(modifier = Modifier.width(48.dp))
                        }
                }

                when (selectedSection) {
                        MainSection.ANIMAL -> {
                                // Onglets pour la section Animal
                                TabRow(selectedTabIndex = selectedTab) {
                                        AnimalTab.values().forEachIndexed { index, tab ->
                                                Tab(
                                                        selected = selectedTab == index,
                                                        onClick = { onTabSelected(index) },
                                                        text = { Text(tab.title) }
                                                )
                                        }
                                }

                                // Contenu de l'onglet sélectionné
                                when (AnimalTab.values()[selectedTab]) {
                                        AnimalTab.IDENTITY -> {
                                                AnimalIdentityView(viewModel = viewModel)
                                        }
                                        AnimalTab.WEIGHT -> {
                                                WeightHistoryView(viewModel = viewModel)
                                        }
                                }
                        }
                        MainSection.CONSULTATIONS -> {
                                ConsultationsView(
                                        viewModel = viewModel,
                                        selectedConsultation = selectedConsultation,
                                        showConsultationDetail = showConsultationDetail,
                                        onShowConsultationDetail = { show ->
                                                showConsultationDetail = show
                                        }
                                )
                        }
                        MainSection.RATIONS -> {
                                RationsView(
                                        viewModel = viewModel.getRationsViewModel(),
                                        onNavigateBack = {},
                                        showAddDialog = showAddRationDialog,
                                        onAddDialogDismiss = { showAddRationDialog = false }
                                )
                        }
                }
        }

        // Dialogs
        if (showAddConsultationDialog) {
                ConsultationEditDialog(
                        consultation = null,
                        onDismiss = { showAddConsultationDialog = false },
                        onSave = { consultation ->
                                viewModel.addConsultation(consultation)
                                showAddConsultationDialog = false
                        }
                )
        }
}
