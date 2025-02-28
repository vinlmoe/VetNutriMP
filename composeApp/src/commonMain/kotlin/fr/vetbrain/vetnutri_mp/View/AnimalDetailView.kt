package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.ConfirmDialog
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Enumer.Sex
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Animal
import fr.vetbrain.vetnutri_mp.Localization.translate
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
                            modifier = Modifier.width(250.dp).fillMaxHeight().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // En-tête avec nom et espèce de l'animal
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(text = animalDetails.nom, style = MaterialTheme.typography.h5)
                            Text(
                                    text = animalDetails.getEspece().label,
                                    style = MaterialTheme.typography.subtitle1,
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
                                                        viewModel.navigateTo(option.section)
                                                    }
                                                    .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                        imageVector = option.icon,
                                        contentDescription = option.title,
                                        tint =
                                                if (currentSection == option.section)
                                                        VetNutriColors.Primary
                                                else Color.Gray
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                        text = option.title,
                                        style = MaterialTheme.typography.body1,
                                        color =
                                                if (currentSection == option.section)
                                                        VetNutriColors.Primary
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
                                                backgroundColor = VetNutriColors.Secondary,
                                                contentColor = VetNutriColors.OnSecondary
                                        ),
                                modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Retour"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Retour")
                        }
                    }

                    // Contenu principal
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        when (currentSection) {
                            AnimalDetailSection.IDENTIFICATION -> {
                                if (isEditing) {
                                    AnimalEditView(
                                            animal = animalDetails,
                                            onSave = { updatedAnimal ->
                                                viewModel.updateAnimal(updatedAnimal)
                                                isEditing = false
                                            },
                                            onCancel = { isEditing = false },
                                            modifier = Modifier.fillMaxSize().padding(16.dp)
                                    )
                                } else {
                                    AnimalIdentificationView(
                                            animal = animalDetails,
                                            onEdit = { isEditing = true },
                                            onDelete = { showDeleteConfirmation = true },
                                            modifier = Modifier.fillMaxSize().padding(16.dp)
                                    )
                                }
                            }
                            AnimalDetailSection.CONSULTATIONS -> {
                                ConsultationsView(
                                        viewModel = viewModel,
                                        showConsultationDetail = showConsultationDetail,
                                        onShowConsultationDetail = { show ->
                                            showConsultationDetail = show
                                        },
                                        modifier = Modifier.fillMaxSize()
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
                                            Modifier.fillMaxHeight().width(250.dp).padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // En-tête avec nom et espèce de l'animal
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                            text = animalDetails.nom,
                                            style = MaterialTheme.typography.h5
                                    )
                                    Text(
                                            text = animalDetails.getEspece().label,
                                            style = MaterialTheme.typography.subtitle1,
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
                                                                viewModel.navigateTo(option.section)
                                                                scope.launch { drawerState.close() }
                                                            }
                                                            .padding(vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                                imageVector = option.icon,
                                                contentDescription = option.title,
                                                tint =
                                                        if (currentSection == option.section)
                                                                VetNutriColors.Primary
                                                        else Color.Gray
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Text(
                                                text = option.title,
                                                style = MaterialTheme.typography.body1,
                                                color =
                                                        if (currentSection == option.section)
                                                                VetNutriColors.Primary
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
                                                        backgroundColor = VetNutriColors.Secondary,
                                                        contentColor = VetNutriColors.OnSecondary
                                                ),
                                        modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Retour"
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "Retour")
                                }
                            }
                        },
                        content = {
                            Column(modifier = Modifier.fillMaxSize()) {
                                // Barre supérieure avec bouton menu
                                TopAppBar(
                                        title = { Text(text = animalDetails.nom) },
                                        navigationIcon = {
                                            IconButton(
                                                    onClick = {
                                                        scope.launch { drawerState.open() }
                                                    }
                                            ) {
                                                Icon(
                                                        imageVector = Icons.Default.Menu,
                                                        contentDescription = "Menu"
                                                )
                                            }
                                        },
                                        backgroundColor = VetNutriColors.Primary,
                                        contentColor = VetNutriColors.OnPrimary
                                )

                                // Contenu principal
                                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                    when (currentSection) {
                                        AnimalDetailSection.IDENTIFICATION -> {
                                            if (isEditing) {
                                                AnimalEditView(
                                                        animal = animalDetails,
                                                        onSave = { updatedAnimal ->
                                                            viewModel.updateAnimal(updatedAnimal)
                                                            isEditing = false
                                                        },
                                                        onCancel = { isEditing = false },
                                                        modifier =
                                                                Modifier.fillMaxSize()
                                                                        .padding(16.dp)
                                                )
                                            } else {
                                                AnimalIdentificationView(
                                                        animal = animalDetails,
                                                        onEdit = { isEditing = true },
                                                        onDelete = {
                                                            showDeleteConfirmation = true
                                                        },
                                                        modifier =
                                                                Modifier.fillMaxSize()
                                                                        .padding(16.dp)
                                                )
                                            }
                                        }
                                        AnimalDetailSection.CONSULTATIONS -> {
                                            ConsultationsView(
                                                    viewModel = viewModel,
                                                    showConsultationDetail = showConsultationDetail,
                                                    onShowConsultationDetail = { show ->
                                                        showConsultationDetail = show
                                                    },
                                                    modifier = Modifier.fillMaxSize()
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
                            // TODO: Implémenter la suppression de l'animal
                            onNavigateBack()
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
    Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // En-tête avec boutons d'action
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Identification de l'animal", style = MaterialTheme.typography.h6)
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
            InfoRow(label = Animal.SEX.translate(), value = Sex.getSimpleSex(animal.sexId))

            // Date de naissance et âge
            val birthdate = animal.birthdate
            if (birthdate != null) {
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                val age =
                        today.year -
                                birthdate.year -
                                (if (today.month < birthdate.month ||
                                                (today.month == birthdate.month &&
                                                        today.dayOfMonth < birthdate.dayOfMonth)
                                )
                                        1
                                else 0)

                InfoRow(label = Animal.BIRTH_DATE.translate(), value = birthdate.toString())
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
                Text(text = Animal.SUMMARY.translate(), style = MaterialTheme.typography.subtitle1)
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
 * @param modifier Modificateur optionnel pour personnaliser l'apparence
 */
@Composable
fun ConsultationCard(
        consultation: ConsultationEv,
        isSelected: Boolean,
        onEdit: () -> Unit,
        onDelete: () -> Unit,
        modifier: Modifier = Modifier
) {
    Card(
            modifier =
                    modifier.fillMaxWidth()
                            .padding(
                                    vertical = AppSizes.paddingSmall,
                                    horizontal = AppSizes.paddingXXSmall
                            ),
            elevation = if (isSelected) AppSizes.elevationMedium else AppSizes.elevationSmall,
            backgroundColor =
                    if (isSelected) VetNutriColors.Primary.copy(alpha = 0.08f)
                    else MaterialTheme.colors.surface,
            shape = MaterialTheme.shapes.medium,
            border =
                    if (isSelected) BorderStroke(1.dp, VetNutriColors.Primary.copy(alpha = 0.5f))
                    else null
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
                Text(
                        text = consultation.date?.toString() ?: "Date inconnue",
                        style = MaterialTheme.typography.subtitle1,
                        color = VetNutriColors.Primary
                )

                Row {
                    // Bouton d'édition
                    IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(AppSizes.iconSizeMedium)
                    ) {
                        Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Modifier la consultation",
                                tint = VetNutriColors.Primary
                        )
                    }

                    // Bouton de suppression
                    IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(AppSizes.iconSizeMedium)
                    ) {
                        Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Supprimer la consultation",
                                tint = Color.Red
                        )
                    }
                }
            }

            // Afficher le poids si disponible
            consultation.weight?.let { weight ->
                Text(
                        text = "$weight kg",
                        style = MaterialTheme.typography.caption,
                        color = Color.Gray
                )
            }

            Divider(
                    modifier = Modifier.padding(vertical = AppSizes.paddingXXSmall),
                    color = Color.LightGray,
                    thickness = 0.5.dp
            )

            // Motif de la consultation
            Text(
                    text = consultation.objectConsult,
                    style = MaterialTheme.typography.body1,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
            )

            // Afficher un aperçu des observations si disponibles
            if (!consultation.observation.isNullOrEmpty()) {
                Text(
                        text = consultation.observation,
                        style = MaterialTheme.typography.body2,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                )
            }

            // Afficher le nombre de rations si disponibles
            if (consultation.rations.isNotEmpty()) {
                Text(
                        text = "Rations: ${consultation.rations.size}",
                        style = MaterialTheme.typography.caption,
                        color = VetNutriColors.Secondary
                )
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

    Row(modifier = modifier.fillMaxSize()) {
        // Liste des consultations
        Column(
                modifier = Modifier.weight(0.4f).fillMaxHeight().padding(AppSizes.paddingMedium),
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
        ) {
            Button(
                    onClick = {
                        val currentMoment = Clock.System.now()
                        val localDateTime =
                                currentMoment.toLocalDateTime(TimeZone.currentSystemDefault())
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
                Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Ajouter une consultation"
                )
                Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                Text(text = "Nouvelle consultation")
            }

            Spacer(modifier = Modifier.height(AppSizes.paddingMedium))

            Text(text = "Consultations", style = MaterialTheme.typography.h6)

            animal?.let { animalDetails ->
                if (animalDetails.consultations.isEmpty()) {
                    Text(
                            text = "Aucune consultation",
                            style = MaterialTheme.typography.body1,
                            color = Color.Gray
                    )
                } else {
                    LazyColumn(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                    ) {
                        items(animalDetails.consultations) { consultation ->
                            ConsultationCard(
                                    consultation = consultation,
                                    isSelected = selectedConsultation?.uuid == consultation.uuid,
                                    onEdit = {
                                        viewModel.selectConsultation(consultation)
                                        viewModel.startEditingConsultation()
                                        onShowConsultationDetail(true)
                                    },
                                    onDelete = {
                                        consultationToDelete = consultation
                                        showDeleteConfirmation = true
                                    }
                            )
                        }
                    }
                }
            }
        }

        // Détail de la consultation
        if (showConsultationDetail) {
            Box(modifier = Modifier.weight(0.6f).fillMaxHeight()) {
                selectedConsultation?.let { consultation ->
                    ConsultationDetailView(
                            consultation = consultation,
                            onDismiss = {
                                if (isEditingConsultation && consultation.uuid.isEmpty()) {
                                    // Si on annule l'ajout d'une nouvelle consultation
                                    viewModel.stopEditingConsultation()
                                }
                                onShowConsultationDetail(false)
                            },
                            onSave = { updatedConsultation ->
                                if (isEditingConsultation && consultation.uuid.isEmpty()) {
                                    // Nouvelle consultation
                                    viewModel.addConsultation(updatedConsultation)
                                } else {
                                    // Mise à jour d'une consultation existante
                                    viewModel.updateConsultation(updatedConsultation)
                                }
                                viewModel.stopEditingConsultation()
                                onShowConsultationDetail(false)
                            }
                    )
                }
            }
        }
    }

    // Boîte de dialogue de confirmation de suppression
    if (showDeleteConfirmation && consultationToDelete != null) {
        ConfirmDialog(
                title = "Confirmation de suppression",
                message = "Êtes-vous sûr de vouloir supprimer cette consultation ?",
                onConfirm = {
                    consultationToDelete?.let { consultation ->
                        viewModel.deleteConsultation(consultation)
                    }
                    showDeleteConfirmation = false
                    consultationToDelete = null
                },
                onDismiss = {
                    showDeleteConfirmation = false
                    consultationToDelete = null
                }
        )
    }
}
