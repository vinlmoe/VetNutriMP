package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.ConfirmDialog
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Enumer.Sex
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Animal
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Consultation
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.General
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailSection
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.*

data class MenuOption(val section: AnimalDetailSection, val title: String, val icon: ImageVector)

@Composable
fun AnimalDetailView(
        viewModel: AnimalDetailViewModel,
        settingsViewModel: SettingsViewModel,
        onNavigateBack: () -> Unit,
        onOpenSettings: () -> Unit,
        modifier: Modifier = Modifier
) {
    val animal by viewModel.animal.collectAsState()
    val selectedConsultation by viewModel.selectedConsultation.collectAsState()
        val currentSection by viewModel.currentSection.collectAsState()
    var showConsultationDetail by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

        // Options du menu
        val menuOptions =
                listOf(
                        MenuOption(
                                section = AnimalDetailSection.IDENTIFICATION,
                                title = "Identification",
                                icon = Icons.Default.Person
                        ),
                        MenuOption(
                                section = AnimalDetailSection.CONSULTATIONS,
                                title = "Consultations",
                                icon = Icons.Default.Info
                        ),
                        MenuOption(
                                section = AnimalDetailSection.RATIONS,
                                title = "Rations",
                                icon = Icons.AutoMirrored.Filled.List
                        )
                )

        var showDeleteConfirmation by remember { mutableStateOf(false) }
        var isEditing by remember { mutableStateOf(false) }

        animal?.let { animalDetails ->
                // Utiliser BoxWithConstraints pour déterminer si l'écran est large
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth > AppSizes.breakpointWideScreen

        if (isWideScreen) {
                                // Layout pour écrans larges avec sidebar permanente
                                Row(modifier = modifier.fillMaxSize()) {
                                        // Sidebar
                                        Column(
                                                modifier =
                                                        Modifier.width(250.dp)
                                                                .fillMaxHeight()
                                                                .padding(16.dp),
                                                verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                                // En-tête avec nom et espèce de l'animal
                                                Column(modifier = Modifier.fillMaxWidth()) {
                                                        Text(
                                                                text = animalDetails.nom,
                                                                style = MaterialTheme.typography.h5
                                                        )
                                                        Text(
                                                                text =
                                                                        animalDetails.getEspece()
                                                                                .label,
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .subtitle1,
                                                                color = Color.Gray
                                                        )
                                                }

                                                Divider()

                                                // Options du menu
                                                menuOptions.forEach { option ->
                                                        Row(
                                                                modifier =
                                                                        Modifier.fillMaxWidth()
                                                                                .clickable {
                                                                                        viewModel
                                                                                                .navigateTo(
                                                                                                        option.section
                                                                                                )
                                                                                }
                                                                                .padding(
                                                                                        vertical =
                                                                                                8.dp
                                                                                ),
                                                                verticalAlignment =
                                                                        Alignment.CenterVertically
                                                        ) {
                                                                Icon(
                                                                        imageVector = option.icon,
                                                                        contentDescription =
                                                                                option.title,
                                                                        tint =
                                                                                if (currentSection ==
                                                                                                option.section
                                                                                )
                                                                                        VetNutriColors
                                                                                                .Primary
                                                                                else Color.Gray
                                                                )
                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.width(
                                                                                        16.dp
                                                                                )
                                                                )
                                                                Text(
                                                                        text = option.title,
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .body1,
                                                                        color =
                                                                                if (currentSection ==
                                                                                                option.section
                                                                                )
                                                                                        VetNutriColors
                                                                                                .Primary
                                                                                else Color.Gray
                                                                )
                                                        }
                                                }

                                                Spacer(modifier = Modifier.weight(1f))

                                                // Bouton retour
                                                Button(
                                                        onClick = onNavigateBack,
                                                        colors =
                                                                ButtonDefaults.buttonColors(
                                                                        backgroundColor =
                                                                                VetNutriColors
                                                                                        .Secondary,
                                                                        contentColor =
                                                                                VetNutriColors
                                                                                        .OnSecondary
                                                                ),
                                                        modifier = Modifier.fillMaxWidth()
                                                ) {
                                                        Icon(
                                                                imageVector =
                                                                        Icons.AutoMirrored.Filled
                                                                                .ArrowBack,
                                                                contentDescription = "Retour"
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(text = "Retour")
                                                }

                                                // Ajout de l'option Paramètres en bas du menu
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Row(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .clickable {
                                                                                onOpenSettings()
                                                                        }
                                                                        .padding(vertical = 8.dp),
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        Icon(
                                                                imageVector =
                                                                        Icons.Default.Settings,
                                                                contentDescription = "Paramètres",
                                                                tint = Color.Gray
                                                        )
                                                        Spacer(modifier = Modifier.width(16.dp))
                                                        Text(
                                                                text = "Paramètres",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .body1,
                                                                color = Color.Gray
                                                        )
                                                }
                                        }

                                        // Contenu principal
                                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                                when (currentSection) {
                                                        AnimalDetailSection.IDENTIFICATION -> {
                                                                if (isEditing) {
                                                                        AnimalEditView(
                                                                                animal =
                                                                                        animalDetails,
                                                                                onSave = {
                                                                                        updatedAnimal
                                                                                        ->
                                                                                        viewModel
                                                                                                .updateAnimal(
                                                                                                        updatedAnimal
                                                                                                )
                                                                                        isEditing =
                                                                                                false
                                                                                },
                                                                                onCancel = {
                                                                                        isEditing =
                                                                                                false
                                                                                },
                                                                                modifier =
                                                                                        Modifier.fillMaxSize()
                                                                                                .padding(
                                                                                                        16.dp
                                                                                                )
                                                                        )
                                                                } else {
                                                                        AnimalIdentificationView(
                                                                                animal =
                                                                                        animalDetails,
                                                                                onEdit = {
                                                                                        isEditing =
                                                                                                true
                                                                                },
                                                                                onDelete = {
                                                                                        showDeleteConfirmation =
                                                                                                true
                                                                                },
                                                                                modifier =
                                                                                        Modifier.fillMaxSize()
                                                                                                .padding(
                                                                                                        16.dp
                                                                                                )
                                                                        )
                                                                }
                                                        }
                                                        AnimalDetailSection.CONSULTATIONS -> {
                                                                ConsultationsView(
                                                                        viewModel = viewModel,
                                                                        showConsultationDetail =
                                                                                showConsultationDetail,
                                                                        onShowConsultationDetail = {
                                                                                show ->
                                                                                showConsultationDetail =
                                                                                        show
                                                                        },
                                                                        modifier =
                                                                                Modifier.fillMaxSize()
                                                                )
                                                        }
                                                        AnimalDetailSection.RATIONS -> {
                                                                RationsView(
                                                                        viewModel = viewModel,
                                                                        modifier =
                                                                                Modifier.fillMaxSize()
                                                                )
                                                        }
                                                }
                                        }
                                }
                        } else {
                                // Layout pour écrans étroits avec drawer
                                ModalDrawer(
                                        drawerState = drawerState,
                                        drawerContent = {
                    Column(
                                                        modifier =
                                                                Modifier.fillMaxHeight()
                                                                        .width(250.dp)
                                                                        .padding(16.dp),
                                                        verticalArrangement =
                                                                Arrangement.spacedBy(16.dp)
                                                ) {
                                                        // En-tête avec nom et espèce de l'animal
                                                        Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                                                                        text = animalDetails.nom,
                                style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .h5
                                                                )
                                                                Text(
                                                                        text =
                                                                                animalDetails
                                                                                        .getEspece()
                                                                                        .label,
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .subtitle1,
                                                                        color = Color.Gray
                                                                )
                                                        }

                        Divider()

                                                        // Options du menu
                                                        menuOptions.forEach { option ->
                                                                Row(
                                                                        modifier =
                                                                                Modifier.fillMaxWidth()
                                                                                        .clickable {
                                                                                                viewModel
                                                                                                        .navigateTo(
                                                                                                                option.section
                                                                                                        )
                                                                                                scope
                                                                                                        .launch {
                                                                                                                drawerState
                                                                                                                        .close()
                                                                                                        }
                                                                                        }
                                                                                        .padding(
                                                                                                vertical =
                                                                                                        8.dp
                                                                                        ),
                                                                        verticalAlignment =
                                                                                Alignment
                                                                                        .CenterVertically
                                                                ) {
                                                                        Icon(
                                                                                imageVector =
                                                                                        option.icon,
                                                                                contentDescription =
                                                                                        option.title,
                                                                                tint =
                                                                                        if (currentSection ==
                                                                                                        option.section
                                                                                        )
                                                                                                VetNutriColors
                                                                                                        .Primary
                                                                                        else
                                                                                                Color.Gray
                                                                        )
                                                                        Spacer(
                                                                                modifier =
                                                                                        Modifier.width(
                                                                                                16.dp
                                                                                        )
                                                                        )
                            Text(
                                                                                text = option.title,
                                    style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .body1,
                                                                                color =
                                                                                        if (currentSection ==
                                                                                                        option.section
                                                                                        )
                                                                                                VetNutriColors
                                                                                                        .Primary
                                                                                        else
                                                                                                Color.Gray
                                                                        )
                                                                }
                                                        }

                                                        Spacer(modifier = Modifier.weight(1f))

                                                        // Bouton retour
                                                        Button(
                                                                onClick = onNavigateBack,
                                                                colors =
                                                                        ButtonDefaults.buttonColors(
                                                                                backgroundColor =
                                                                                        VetNutriColors
                                                                                                .Secondary,
                                                                                contentColor =
                                                                                        VetNutriColors
                                                                                                .OnSecondary
                                                                        ),
                                                                modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                                Icon(
                                                                        imageVector =
                                                                                Icons.AutoMirrored
                                                                                        .Filled
                                                                                        .ArrowBack,
                                                                        contentDescription =
                                                                                "Retour"
                                                                )
                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.width(8.dp)
                                                                )
                                                                Text(text = "Retour")
                                                        }

                                                        // Ajout de l'option Paramètres en bas du
                                                        // menu
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        Row(
                                                                modifier =
                                                                        Modifier.fillMaxWidth()
                                                                                .clickable {
                                                                                        onOpenSettings()
                                                                                        scope
                                                                                                .launch {
                                                                                                        drawerState
                                                                                                                .close()
                                                                                                }
                                                                                }
                                                                                .padding(
                                                                                        vertical =
                                                                                                8.dp
                                                                                ),
                                                                verticalAlignment =
                                                                        Alignment.CenterVertically
                                                        ) {
                                                                Icon(
                                                                        imageVector =
                                                                                Icons.Default
                                                                                        .Settings,
                                                                        contentDescription =
                                                                                "Paramètres",
                                                                        tint = Color.Gray
                                                                )
                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.width(
                                                                                        16.dp
                                            )
                            )
                            Text(
                                                                        text = "Paramètres",
                                    style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .body1,
                                                                        color = Color.Gray
                                                                )
                                                        }
                                                }
                                        },
                                        content = {
                                                Column(modifier = Modifier.fillMaxSize()) {
                                                        // En-tête avec bouton menu (remplace la
                                                        // TopAppBar)
                                                        Row(
                                                                modifier =
                                                                        Modifier.fillMaxWidth()
                                                                                .padding(16.dp),
                                                                horizontalArrangement =
                                                                        Arrangement.SpaceBetween,
                                                                verticalAlignment =
                                                                        Alignment.CenterVertically
                                                        ) {
                                                                IconButton(
                                                                        onClick = {
                                                                                scope.launch {
                                                                                        drawerState
                                                                                                .open()
                                                                                }
                                                                        }
                                                                ) {
                                                                        Icon(
                                                                                imageVector =
                                                                                        Icons.Default
                                                                                                .Menu,
                                                                                contentDescription =
                                                                                        "Menu",
                                                                                tint =
                                                                                        VetNutriColors
                                                                                                .Primary
                                                                        )
                                                                }

                            Text(
                                                                        text = animalDetails.nom,
                                    style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .h6,
                                                                        color =
                                                                                VetNutriColors
                                                                                        .Primary
                                                                )

                                                                // Espace vide pour équilibrer la
                                                                // mise en page
                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.size(48.dp)
                                                                )
                                                        }

                                                        Divider(
                                                                color =
                                                                        MaterialTheme.colors
                                                                                .onSurface.copy(
                                                                                alpha = 0.12f
                                                                        ),
                                                                thickness = AppSizes.dividerHeight
                                                        )

                                                        // Contenu principal
                                                        Box(
                                                                modifier =
                                                                        Modifier.weight(1f)
                                                                                .fillMaxWidth()
                                                        ) {
                                                                when (currentSection) {
                                                                        AnimalDetailSection
                                                                                .IDENTIFICATION -> {
                                                                                if (isEditing) {
                                                                                        AnimalEditView(
                                                                                                animal =
                                                                                                        animalDetails,
                                                                                                onSave = {
                                                                                                        updatedAnimal
                                                                                                        ->
                                                                                                        viewModel
                                                                                                                .updateAnimal(
                                                                                                                        updatedAnimal
                                                                                                                )
                                                                                                        isEditing =
                                                                                                                false
                                                                                                },
                                                                                                onCancel = {
                                                                                                        isEditing =
                                                                                                                false
                                                                                                },
                                                                                                modifier =
                                                                                                        Modifier.fillMaxSize()
                                                                                                                .padding(
                                                                                                                        16.dp
                                                                                                                )
                                                                                        )
                                                                                } else {
                                                                                        AnimalIdentificationView(
                                                                                                animal =
                                                                                                        animalDetails,
                                                                                                onEdit = {
                                                                                                        isEditing =
                                                                                                                true
                                                                                                },
                                                                                                onDelete = {
                                                                                                        showDeleteConfirmation =
                                                                                                                true
                                                                                                },
                                                                                                modifier =
                                                                                                        Modifier.fillMaxSize()
                                                                                                                .padding(
                                                                                                                        16.dp
                                                                                                                )
                                                                                        )
                                                                                }
                                                                        }
                                                                        AnimalDetailSection
                                                                                .CONSULTATIONS -> {
                                                                                ConsultationsView(
                                                                                        viewModel =
                                                                                                viewModel,
                                                                                        showConsultationDetail =
                                                                                                showConsultationDetail,
                                                                                        onShowConsultationDetail = {
                                                                                                show
                                                                                                ->
                                                                                                showConsultationDetail =
                                                                                                        show
                                                                                        },
                                                                                        modifier =
                                                                                                Modifier.fillMaxSize()
                                                                                )
                                                                        }
                                                                        AnimalDetailSection
                                                                                .RATIONS -> {
                                                                                RationsView(
                                                                                        viewModel =
                                                                                                viewModel,
                                                                                        modifier =
                                                                                                Modifier.fillMaxSize()
                                                                                )
                                                                        }
                                                                }
                                                        }
                                                }
                                        }
                                )
                        }

                        // Boîte de dialogue de confirmation de suppression
                        if (showDeleteConfirmation) {
                                ConfirmDialog(
                                        title = "Confirmation de suppression",
                                        message = "Êtes-vous sûr de vouloir supprimer cet animal ?",
                                        onConfirm = {
                                                // Appeler la fonction de suppression du ViewModel
                                                val success = viewModel.deleteAnimal()
                                                if (success) {
                                                        // Naviguer vers la liste des animaux
                                                        onNavigateBack()
                                                }
                                                showDeleteConfirmation = false
                                        },
                                        onDismiss = { showDeleteConfirmation = false }
                                )
                        }
                }
        }
}

@Composable
fun AnimalIdentificationView(
        animal: AnimalEv,
        onEdit: () -> Unit,
        onDelete: () -> Unit,
        modifier: Modifier = Modifier
) {
        Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
                // En-tête avec boutons d'action
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                            Text(
                                text = "Identification de l'animal",
                                style = MaterialTheme.typography.h6
                        )
                        Row {
                                IconButton(onClick = onEdit) {
                                        Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Modifier",
                                                tint = VetNutriColors.Primary
                                        )
                                }
                                IconButton(onClick = onDelete) {
                                        Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Supprimer",
                                                tint = Color.Red
                                        )
                                }
                        }
                }

                Divider()

                // Informations de l'animal
                Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                        // Nom et race
                        InfoRow(label = Animal.NAME.translate(), value = animal.nom)
                        InfoRow(label = Animal.BREED.translate(), value = animal.race)

                        // Sexe
                        InfoRow(
                                label = Animal.SEX.translate(),
                                value = Sex.getSimpleSex(animal.sexId)
                        )

                        // Date de naissance et âge
                        val birthdate = animal.birthdate
                        if (birthdate != null) {
                                val today =
                                        Clock.System.now()
                                                .toLocalDateTime(TimeZone.currentSystemDefault())
                                                .date
                                val age =
                                        today.year -
                                                birthdate.year -
                                                (if (today.month < birthdate.month ||
                                                                (today.month == birthdate.month &&
                                                                        today.dayOfMonth <
                                                                                birthdate
                                                                                        .dayOfMonth)
                                                )
                                                        1
                                                else 0)

                                InfoRow(
                                        label = Animal.BIRTH_DATE.translate(),
                                        value = birthdate.toString()
                                )
                                InfoRow(label = Animal.AGE.translate(), value = "$age ans")
                        }

                        // Propriétaire
                        if (animal.ownerName.isNotEmpty()) {
                                InfoRow(label = Animal.OWNER.translate(), value = animal.ownerName)
                        }

                        // Statut (vivant/décédé)
                        InfoRow(label = "Statut", value = if (animal.dead) "Décédé" else "Vivant")

                        // Résumé
                        if (animal.summary.isNotEmpty()) {
                                Text(
                                        text = Animal.SUMMARY.translate(),
                                        style = MaterialTheme.typography.subtitle1
                                )
                                Text(text = animal.summary, style = MaterialTheme.typography.body1)
                        }

                        // Historique de poids
                        if (animal.weightHistory.isNotEmpty()) {
                                val lastWeight = animal.weightHistory.maxByOrNull { it.date }
                                lastWeight?.let { weight ->
                                        InfoRow(
                                                label = Animal.WEIGHT.translate(),
                                                value = "${weight.value} kg (${weight.date})"
                                        )
                                }
                        }
                }
        }
}

@Composable
fun InfoRow(label: String, value: String) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Start
        ) {
                                Text(
                        text = "$label :",
                        style = MaterialTheme.typography.subtitle1,
                        modifier = Modifier.width(150.dp)
                )
                Text(text = value, style = MaterialTheme.typography.body1)
        }
}

/**
 * Composant réutilisable pour afficher une carte de consultation
 *
 * @param consultation La consultation à afficher
 * @param isSelected Indique si la consultation est sélectionnée
 * @param onEdit Action à exécuter lors du clic sur le bouton d'édition
 * @param onDelete Action à exécuter lors du clic sur le bouton de suppression
 * @param isDeleteEnabled Indique si le bouton de suppression est activé
 * @param onClick Action à exécuter lors du clic sur la carte
 * @param modifier Modificateur optionnel pour personnaliser l'apparence
 */
@Composable
fun ConsultationCard(
        consultation: ConsultationEv,
        isSelected: Boolean,
        onEdit: () -> Unit,
        onDelete: () -> Unit,
        isDeleteEnabled: Boolean = true,
        onClick: () -> Unit = {},
        modifier: Modifier = Modifier
) {
        Card(
                modifier =
                        modifier.fillMaxWidth()
                                .padding(
                                        vertical = AppSizes.paddingSmall,
                                        horizontal = AppSizes.paddingXXSmall
                                )
                                .clickable(onClick = onClick),
                elevation =
                        if (isSelected) AppSizes.cardElevationSelected
                        else AppSizes.cardElevationNormal,
                backgroundColor =
                        if (isSelected) VetNutriColors.Primary.copy(alpha = 0.12f)
                        else MaterialTheme.colors.surface,
                shape = MaterialTheme.shapes.medium,
                border =
                        if (isSelected)
                                BorderStroke(1.dp, VetNutriColors.Primary.copy(alpha = 0.5f))
                        else BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.5f))
        ) {
                Column(
                        modifier = Modifier.fillMaxWidth().padding(AppSizes.paddingMedium),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                ) {
                        // Date de la consultation et boutons d'action
                        Row(
                                modifier = Modifier.fillMaxWidth(),
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
                                                contentDescription = null,
                                                tint = VetNutriColors.Primary,
                                                modifier = Modifier.size(AppSizes.iconSizeSmall)
                                        )
                                Text(
                                                text = consultation.date?.toString()
                                                                ?: "Date inconnue",
                                                style = MaterialTheme.typography.subtitle1,
                                                color = VetNutriColors.Primary
                                        )
                                }

                                Row {
                                        // Bouton d'édition
                                        IconButton(
                                                onClick = onEdit,
                                                modifier = Modifier.size(AppSizes.iconSizeMedium)
                                        ) {
                                                Icon(
                                                        imageVector = AppIcons.Edit,
                                                        contentDescription =
                                                                "Modifier la consultation",
                                                        tint = VetNutriColors.Primary
                                                )
                                        }

                                        // Bouton de suppression
                                        IconButton(
                                                onClick = onDelete,
                                                enabled = isDeleteEnabled,
                                                modifier = Modifier.size(AppSizes.iconSizeMedium)
                                        ) {
                                                Icon(
                                                        imageVector = AppIcons.Delete,
                                                        contentDescription =
                                                                "Supprimer la consultation",
                                                        tint =
                                                                if (isDeleteEnabled) Color.Red
                                                                else Color.Gray.copy(alpha = 0.5f)
                                                )
                                        }
                                }
                        }

                        // Afficher le poids si disponible
                        consultation.weight?.let { weight ->
                                Row(
                                        horizontalArrangement =
                                                Arrangement.spacedBy(AppSizes.paddingSmall),
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Icon(
                                                AppIcons.Weight,
                                                contentDescription = null,
                                                tint = Color.Gray,
                                                modifier = Modifier.size(AppSizes.iconSizeXSmall)
                                        )
                                Text(
                                                text = "$weight kg",
                                                style = MaterialTheme.typography.caption,
                                                color = Color.Gray
                                        )
                                }
                        }

                        Divider(
                                modifier = Modifier.padding(vertical = AppSizes.paddingXXSmall),
                                color = Color.LightGray,
                                thickness = 0.5.dp
                        )

                        // Motif de la consultation
                        Row(
                                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall),
                                verticalAlignment = Alignment.Top
                        ) {
                                Icon(
                                        AppIcons.Info,
                                        contentDescription = null,
                                        tint = VetNutriColors.Secondary,
                                        modifier = Modifier.size(AppSizes.iconSizeSmall)
                                )
                                Text(
                                        text = consultation.objectConsult,
                                        style = MaterialTheme.typography.body1,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                )
                        }

                        // Afficher un aperçu des observations si disponibles
                        if (!consultation.observation.isNullOrEmpty()) {
                                Row(
                                        horizontalArrangement =
                                                Arrangement.spacedBy(AppSizes.paddingSmall),
                                        verticalAlignment = Alignment.Top
                                ) {
                                        Icon(
                                                AppIcons.Info,
                                                contentDescription = null,
                                                tint = Color.Gray,
                                                modifier = Modifier.size(AppSizes.iconSizeSmall)
                                        )
                                    Text(
                                                text = consultation.observation,
                                                style = MaterialTheme.typography.body2,
                                                color = Color.Gray,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                        )
                                }
                        }

                        // Afficher le nombre de rations si disponibles
                        if (consultation.rations.isNotEmpty()) {
                                Row(
                                        horizontalArrangement =
                                                Arrangement.spacedBy(AppSizes.paddingSmall),
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Icon(
                                                AppIcons.Ration,
                                                contentDescription = null,
                                                tint = VetNutriColors.Secondary,
                                                modifier = Modifier.size(AppSizes.iconSizeXSmall)
                                        )
                                        Text(
                                                text = "Rations: ${consultation.rations.size}",
                                                style = MaterialTheme.typography.caption,
                                                color = VetNutriColors.Secondary
                                    )
                                }
                            }
                        }
                    }
                }

@Composable
fun ConsultationsView(
        viewModel: AnimalDetailViewModel,
        showConsultationDetail: Boolean,
        onShowConsultationDetail: (Boolean) -> Unit,
        modifier: Modifier = Modifier
) {
        val animal by viewModel.animal.collectAsState()
        val selectedConsultation by viewModel.selectedConsultation.collectAsState()
        val isEditingConsultation by remember { derivedStateOf { viewModel.isEditingConsultation } }
        var showDeleteConfirmation by remember { mutableStateOf(false) }
        var consultationToDelete by remember { mutableStateOf<ConsultationEv?>(null) }

        // Dialogue de confirmation de suppression
        if (showDeleteConfirmation) {
                AlertDialog(
                        onDismissRequest = { showDeleteConfirmation = false },
                        title = { Text(Consultation.DELETE_CONSULTATION.translate()) },
                        text = { Text(Consultation.DELETE_CONSULTATION_CONFIRM.translate()) },
                        confirmButton = {
                                Button(
                                        onClick = {
                                                consultationToDelete?.let { consultation ->
                                                        viewModel.deleteConsultation(consultation)
                                                }
                                                showDeleteConfirmation = false
                                        },
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor = Color.Red,
                                                        contentColor = Color.White
                                                )
                                ) { Text(General.CONFIRM.translate()) }
                        },
                        dismissButton = {
                                Button(
                                        onClick = { showDeleteConfirmation = false },
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor = VetNutriColors.Secondary,
                                                        contentColor = VetNutriColors.OnSecondary
                                                )
                                ) { Text(General.CANCEL.translate()) }
                        }
                )
        }

        Row(modifier = modifier.fillMaxSize()) {
                // Liste des consultations
                        Column(
                                modifier =
                                        Modifier.weight(0.4f)
                                                .fillMaxHeight()
                                                .padding(AppSizes.paddingMedium),
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                        ) {
                            Button(
                                    onClick = {
                                        val currentMoment = Clock.System.now()
                                        val localDateTime =
                                                currentMoment.toLocalDateTime(
                                                        TimeZone.currentSystemDefault()
                                                )
                                        val currentDate = localDateTime.date
                                        viewModel.prepareNewConsultation(currentDate)
                                        viewModel.startEditingConsultation()
                                        onShowConsultationDetail(true)
                                    },
                                    colors =
                                            ButtonDefaults.buttonColors(
                                                    backgroundColor = VetNutriColors.Primary,
                                                    contentColor = VetNutriColors.OnPrimary
                                            ),
                                modifier = Modifier.fillMaxWidth()
                        ) {
                                Row(
                                        horizontalArrangement =
                                                Arrangement.spacedBy(AppSizes.paddingSmall),
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Icon(
                                                AppIcons.Add,
                                                contentDescription = "Ajouter une consultation",
                                                modifier = Modifier.size(AppSizes.iconSizeSmall)
                                        )
                                        Text(text = "Nouvelle consultation")
                                }
                        }

                        Divider(
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
                                thickness = AppSizes.dividerHeight
                        )

                                Text(
                                text = "Consultations",
                                style = MaterialTheme.typography.h6,
                                color = VetNutriColors.Primary
                        )

                        animal?.let { animalDetails ->
                                if (animalDetails.consultations.isEmpty()) {
                                        Box(
                                                modifier = Modifier.fillMaxWidth().weight(1f),
                                                contentAlignment = Alignment.Center
                                        ) {
                                                Text(
                                                        text = "Aucune consultation",
                                                        style = MaterialTheme.typography.body1,
                                                        color = Color.Gray
                                                )
                                        }
                                } else {
                                        // Déterminer si la suppression est autorisée (plus d'une
                                        // consultation)
                                        val canDeleteConsultation =
                                                animalDetails.consultations.size > 1

                            LazyColumn(
                                                modifier = Modifier.fillMaxWidth().weight(1f),
                                                verticalArrangement =
                                                        Arrangement.spacedBy(AppSizes.cardSpacing)
                            ) {
                                                items(animalDetails.consultations) { consultation ->
                                        ConsultationCard(
                                                consultation = consultation,
                                                isSelected =
                                                                        selectedConsultation
                                                                                ?.uuid ==
                                                                                consultation.uuid,
                                                onEdit = {
                                                                        viewModel
                                                                                .selectConsultation(
                                                                                        consultation
                                                                                )
                                                                        viewModel
                                                                                .startEditingConsultation()
                                                                        onShowConsultationDetail(
                                                                                true
                                                                        )
                                                },
                                                onDelete = {
                                                                        consultationToDelete =
                                                                                consultation
                                                                        showDeleteConfirmation =
                                                                                true
                                                                },
                                                                isDeleteEnabled =
                                                                        canDeleteConsultation,
                                                                onClick = {
                                                                        viewModel
                                                                                .selectConsultation(
                                                                                        consultation
                                                                                )
                                                                        onShowConsultationDetail(
                                                                                true
                                                                        )
                                                }
                                        )
                                    }
                                }
                            }
                        }
                }

                // Séparateur vertical
                Divider(
                        modifier = Modifier.fillMaxHeight().width(1.dp),
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                )

                // Détail de la consultation
                if (showConsultationDetail) {
                        Box(modifier = Modifier.weight(0.6f).fillMaxHeight()) {
                                selectedConsultation?.let { consultation ->
                                        AppConsultationDetailView(
                                                consultation = consultation,
                                        onDismiss = {
                                                        if (isEditingConsultation &&
                                                                        consultation.uuid.isEmpty()
                                                        ) {
                                                                // Si on annule l'ajout d'une
                                                                // nouvelle consultation
                                            viewModel.stopEditingConsultation()
                                                        }
                                                        onShowConsultationDetail(false)
                                        },
                                        onSave = { updatedConsultation ->
                                                        if (isEditingConsultation &&
                                                                        consultation.uuid.isEmpty()
                                                        ) {
                                                                // Nouvelle consultation
                                                                viewModel.addConsultation(
                                                                        updatedConsultation
                                                                )
                                                        } else {
                                                                // Mise à jour d'une consultation
                                                                // existante
                                                                viewModel.updateConsultation(
                                                                        updatedConsultation
                                                                )
                                                        }
                                            viewModel.stopEditingConsultation()
                                                        onShowConsultationDetail(false)
                                                }
                                        )
                                }
                        }
                } else {
                        // Message indiquant de sélectionner une consultation
                        Box(
                                modifier = Modifier.weight(0.6f).fillMaxHeight(),
                                contentAlignment = Alignment.Center
                        ) {
                                Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement =
                                                Arrangement.spacedBy(AppSizes.paddingMedium)
                                ) {
                                        Icon(
                                                AppIcons.Info,
                                                contentDescription = null,
                                                tint = Color.Gray,
                                                modifier = Modifier.size(AppSizes.iconSizeLarge)
                                        )
                                        Text(
                                                text =
                                                        "Sélectionnez une consultation pour afficher les détails",
                                                style = MaterialTheme.typography.body1,
                                                color = Color.Gray
                                        )
                                }
                        }
                }
        }
}

@Composable
fun RationsView(viewModel: AnimalDetailViewModel, modifier: Modifier = Modifier) {
        val animal by viewModel.animal.collectAsState()
        val selectedConsultation by viewModel.selectedConsultation.collectAsState()
        val selectedRation by viewModel.selectedRation.collectAsState()

        // Sélectionner automatiquement la consultation la plus récente si aucune n'est sélectionnée
        LaunchedEffect(animal) {
                if (selectedConsultation == null && animal?.consultations?.isNotEmpty() == true) {
                        // Trouver la consultation la plus récente
                        val mostRecentConsultation =
                                animal?.consultations?.maxByOrNull {
                                        it.date ?: LocalDate(2000, 1, 1)
                                }
                        mostRecentConsultation?.let { viewModel.selectConsultation(it) }
                }
        }

        // Sélectionner automatiquement la première ration si aucune n'est sélectionnée
        LaunchedEffect(selectedConsultation) {
                if (selectedRation == null && selectedConsultation?.rations?.isNotEmpty() == true) {
                        viewModel.selectRation(
                                selectedConsultation?.rations?.first() ?: return@LaunchedEffect
                        )
                }
        }

                        Column(
                modifier = modifier.padding(AppSizes.paddingMedium),
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
        ) {
                // Segment 1: En-tête avec 5 labels en ligne
                Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = AppSizes.cardElevationNormal,
                        backgroundColor = MaterialTheme.colors.surface
                ) {
                        Row(
                                modifier = Modifier.fillMaxWidth().padding(AppSizes.paddingMedium),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                        text = "Consultation:",
                                        style = MaterialTheme.typography.subtitle1,
                                        color = VetNutriColors.Primary
                                )
                                Text(
                                        text = selectedConsultation?.date?.toString() ?: "Aucune",
                                        style = MaterialTheme.typography.body1
                                )
                                Text(
                                        text = "Ration:",
                                        style = MaterialTheme.typography.subtitle1,
                                        color = VetNutriColors.Primary
                                )
                                Text(
                                        text = selectedRation?.name ?: "Aucune",
                                        style = MaterialTheme.typography.body1
                                )
                                Text(
                                        text =
                                                if (selectedRation?.actual == true) "Actuelle"
                                                else "Proposée",
                                        style = MaterialTheme.typography.caption,
                                        color =
                                                if (selectedRation?.actual == true)
                                                        VetNutriColors.Primary
                                                else Color.Gray
                                )
                        }
                }

                // Segments 2-5: Répartis en 2 lignes et 2 colonnes
                Row(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                ) {
                        // Colonne gauche
                        Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                        ) {
                                // Segment 2: Liste des rations de la consultation sélectionnée
                                Card(
                                        modifier = Modifier.fillMaxWidth().weight(1f),
                                        elevation = AppSizes.cardElevationNormal,
                                        backgroundColor = MaterialTheme.colors.surface
                                ) {
                                        Column(
                                                modifier =
                                                        Modifier.fillMaxSize()
                                                                .padding(AppSizes.paddingMedium),
                                                verticalArrangement =
                                                        Arrangement.spacedBy(AppSizes.paddingSmall)
                                        ) {
                                Text(
                                                        text = "Rations de la consultation",
                                                        style = MaterialTheme.typography.h6,
                                                        color = VetNutriColors.Primary
                                                )

                                                Divider(
                                                        color =
                                                                MaterialTheme.colors.onSurface.copy(
                                                                        alpha = 0.12f
                                                                )
                                                )

                                                if (selectedConsultation?.rations?.isEmpty() == true
                                                ) {
                                                        Box(
                                                                modifier =
                                                                        Modifier.fillMaxWidth()
                                                                                .weight(1f),
                                                                contentAlignment = Alignment.Center
                                                        ) {
                                    Text(
                                                                        text =
                                                                                "Aucune ration disponible",
                                            style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .body1,
                                                                        color = Color.Gray
                                                                )
                                                        }
                                                } else {
                                                        LazyColumn(
                                                                modifier =
                                                                        Modifier.fillMaxWidth()
                                                                                .weight(1f),
                                                                verticalArrangement =
                                                                        Arrangement.spacedBy(
                                                                                AppSizes.paddingSmall
                                                                        )
                                                        ) {
                                                                items(
                                                                        selectedConsultation
                                                                                ?.rations
                                                                                ?: emptyList()
                                                                ) { ration ->
                                                                        RationItem(
                                                                                ration = ration,
                                                                                isSelected =
                                                                                        selectedRation
                                                                                                ?.uuid ==
                                                                                                ration.uuid,
                                                                                onClick = {
                                                                                        viewModel
                                                                                                .selectRation(
                                                                                                        ration
                                                                                                )
                                                                                }
                                                                        )
                                                                }
                                                        }
                                                }
                                        }
                                }

                                // Segment 4: Liste des aliments de la ration sélectionnée
                                Card(
                                        modifier = Modifier.fillMaxWidth().weight(1f),
                                        elevation = AppSizes.cardElevationNormal,
                                        backgroundColor = MaterialTheme.colors.surface
                                ) {
                                        Column(
                                                modifier =
                                                        Modifier.fillMaxSize()
                                                                .padding(AppSizes.paddingMedium),
                                                verticalArrangement =
                                                        Arrangement.spacedBy(AppSizes.paddingSmall)
                                        ) {
                                    Text(
                                                        text = "Aliments de la ration",
                                                        style = MaterialTheme.typography.h6,
                                                        color = VetNutriColors.Primary
                                                )

                                                Divider(
                                                        color =
                                                                MaterialTheme.colors.onSurface.copy(
                                                                        alpha = 0.12f
                                                                )
                                                )

                                                if (selectedRation?.alimentMutableList?.isEmpty() ==
                                                                true
                                                ) {
                                                        Box(
                                                                modifier =
                                                                        Modifier.fillMaxWidth()
                                                                                .weight(1f),
                                                                contentAlignment = Alignment.Center
                                                        ) {
                                    Text(
                                                                        text =
                                                                                "Aucun aliment dans cette ration",
                                            style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .body1,
                                                                        color = Color.Gray
                                                                )
                                                        }
                                                } else {
                                                        LazyColumn(
                                                                modifier =
                                                                        Modifier.fillMaxWidth()
                                                                                .weight(1f),
                                                                verticalArrangement =
                                                                        Arrangement.spacedBy(
                                                                                AppSizes.paddingSmall
                                                                        )
                                                        ) {
                                                                items(
                                                                        selectedRation
                                                                                ?.alimentMutableList
                                                                                ?: emptyList()
                                                                ) { aliment ->
                                                                        AlimentItem(
                                                                                aliment = aliment
                                        )
                                    }
                                }
                            }
                        }
                                }
                        }

                        // Colonne droite
                        Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                        ) {
                                // Segment 3: Détails de la ration sélectionnée
                                Card(
                                        modifier = Modifier.fillMaxWidth().weight(1f),
                                        elevation = AppSizes.cardElevationNormal,
                                        backgroundColor = MaterialTheme.colors.surface
                                ) {
                                Column(
                                        modifier =
                                                Modifier.fillMaxSize()
                                                        .padding(AppSizes.paddingMedium),
                                        verticalArrangement =
                                                        Arrangement.spacedBy(AppSizes.paddingSmall)
                                        ) {
                                                Text(
                                                        text = "Détails de la ration",
                                                        style = MaterialTheme.typography.h6,
                                                        color = VetNutriColors.Primary
                                                )

                                                Divider(
                                                        color =
                                                                MaterialTheme.colors.onSurface.copy(
                                                                        alpha = 0.12f
                                                                )
                                                )

                                                if (selectedRation == null) {
                                                        Box(
                                            modifier =
                                                    Modifier.fillMaxWidth()
                                                                                .weight(1f),
                                                                contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                                                        text =
                                                                                "Aucune ration sélectionnée",
                                                style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .body1,
                                                                        color = Color.Gray
                                                                )
                                                        }
                                                } else {
                                                        Column(
                                                                modifier =
                                                                        Modifier.fillMaxWidth()
                                                                                .weight(1f),
                                            verticalArrangement =
                                                                        Arrangement.spacedBy(
                                                                                AppSizes.paddingSmall
                                                                        )
                                                        ) {
                                                                InfoRow(
                                                                        label = "Nom",
                                                                        value = selectedRation?.name
                                                                                        ?: ""
                                                                )
                                                                InfoRow(
                                                                        label = "Type",
                                                                        value =
                                                                                if (selectedRation
                                                                                                ?.actual ==
                                                                                                true
                                                                                )
                                                                                        "Actuelle"
                                                                                else "Proposée"
                                                                )
                                                                InfoRow(
                                                                        label = "Nombre d'aliments",
                                                                        value =
                                                                                "${selectedRation?.alimentMutableList?.size ?: 0}"
                                                                )

                                                                // Ici, on pourrait ajouter d'autres
                                                                // informations sur la ration
                                                        }
                                                }
                                        }
                                }

                                // Segment 5: Informations nutritionnelles
                                Card(
                                        modifier = Modifier.fillMaxWidth().weight(1f),
                                        elevation = AppSizes.cardElevationNormal,
                                        backgroundColor = MaterialTheme.colors.surface
                                ) {
                                        Column(
                                                modifier =
                                                        Modifier.fillMaxSize()
                                                                .padding(AppSizes.paddingMedium),
                                                verticalArrangement =
                                                        Arrangement.spacedBy(AppSizes.paddingSmall)
                                        ) {
                                                Text(
                                                        text = "Informations nutritionnelles",
                                                        style = MaterialTheme.typography.h6,
                                                        color = VetNutriColors.Primary
                                                )

                                                Divider(
                                                        color =
                                                                MaterialTheme.colors.onSurface.copy(
                                                                        alpha = 0.12f
                                                                )
                                                )

                                                if (selectedRation == null) {
                                                        Box(
                                            modifier =
                                                    Modifier.fillMaxWidth()
                                                                                .weight(1f),
                                                                contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                                                        text =
                                                                                "Aucune ration sélectionnée",
                                                style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .body1,
                                                                        color = Color.Gray
                                                                )
                                                        }
                                                } else {
                                                        Column(
                                                                modifier =
                                                                        Modifier.fillMaxWidth()
                                                                                .weight(1f),
                                                                verticalArrangement =
                                                                        Arrangement.spacedBy(
                                                                                AppSizes.paddingSmall
                                                                        )
                                                        ) {
                                                                // Ici, on afficherait les
                                                                // informations nutritionnelles
                                                                // calculées
                                                                // à partir des aliments de la
                                                                // ration
                                                                Text(
                                                                        text =
                                                                                "Les informations nutritionnelles seront calculées à partir des aliments de la ration.",
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
}

@Composable
fun RationItem(
        ration: Ration,
        isSelected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
        ) {
            Row(
                modifier =
                        modifier.fillMaxWidth()
                                .clickable(onClick = onClick)
                                .background(
                                        if (isSelected) VetNutriColors.Primary.copy(alpha = 0.12f)
                                        else Color.Transparent
                                )
                                .border(
                                        width = if (isSelected) 1.dp else 0.dp,
                                        color =
                                                if (isSelected)
                                                        VetNutriColors.Primary.copy(alpha = 0.5f)
                                                else Color.Transparent,
                                        shape = MaterialTheme.shapes.small
                                )
                                .padding(AppSizes.paddingSmall),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                Text(
                                text = ration.name,
                                style = MaterialTheme.typography.subtitle1,
                                color =
                                        if (isSelected) VetNutriColors.Primary
                                        else MaterialTheme.colors.onSurface
                        )
                        Text(
                                text = if (ration.actual) "Actuelle" else "Proposée",
                                style = MaterialTheme.typography.caption,
                                color = if (ration.actual) VetNutriColors.Primary else Color.Gray
                        )
                }

                Text(
                        text = "${ration.alimentMutableList.size} aliments",
                        style = MaterialTheme.typography.caption,
                        color = Color.Gray
                )
        }
}

@Composable
fun AlimentItem(aliment: AlimentRation, modifier: Modifier = Modifier) {
        Row(
                modifier = modifier.fillMaxWidth().padding(AppSizes.paddingSmall),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
                Column(modifier = Modifier.weight(1f)) {
            Text(
                                text = aliment.aliment?.nom
                                                ?: aliment.uuidUnif, // Afficher le nom de l'aliment
                                // s'il est disponible
                                style = MaterialTheme.typography.subtitle1
                        )
                Text(
                                text = "Catégorie: ${aliment.category}",
                                style = MaterialTheme.typography.caption,
                                color = Color.Gray
                        )
                }

                Column(horizontalAlignment = Alignment.End) {
                        Text(text = "${aliment.quantity} g", style = MaterialTheme.typography.body2)
                        Text(
                                text = "Proportion: ${aliment.proportion}%",
                                style = MaterialTheme.typography.caption,
                                color = Color.Gray
                        )
                }
    }
}
