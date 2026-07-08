package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.DropdownField
import fr.vetbrain.vetnutri_mp.Components.MultiSelectionCard
import fr.vetbrain.vetnutri_mp.Components.RichTextEditor
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Export.*
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Repository.ConseilRepository
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import kotlinx.datetime.Clock
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import kotlinx.coroutines.launch

/**
 * Vue pour éditer un conseil personnalisé
 *
 * @param conseilRepository Repository pour la gestion des conseils
 * @param conseilId ID du conseil à éditer (null pour création)
 * @param onNavigateBack Callback pour revenir à l'écran précédent
 * @param modifier Modifier à appliquer à la vue
 */
@Composable
fun ConseilEditView(
        conseilRepository: ConseilRepository,
        conseilId: String?,
        onNavigateBack: () -> Unit,
        modifier: Modifier = Modifier
) {
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // États du formulaire
    var conseilTitle by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(SectionCategory.CONSEIL_NUTRITIONNEL) }
    var isActive by remember { mutableStateOf(true) }
    var selectedEspecesState by remember { mutableStateOf<List<Espece>>(emptyList()) }
    var content by remember { mutableStateOf(RichTextContent()) }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Charger le conseil si on est en mode édition
    LaunchedEffect(conseilId) {
        if (conseilId != null) {
            try {
                val result = conseilRepository.getConseilsActifs()
                if (result.isSuccess) {
                    val conseil = result.getOrThrow().find { it.id == conseilId }
                    if (conseil != null) {
                        conseilTitle = conseil.title
                        category = conseil.category
                        isActive = conseil.isActive

                        // Convertir les espèces string en objets Espece
                        val matchedEspeces = mutableListOf<Espece>()
                        conseil.targetSpecies.forEach { especeStr ->
                            val espece = Espece.getFromString(especeStr)
                            if (espece != null) {
                                matchedEspeces.add(espece)
                            }
                        }
                        selectedEspecesState = matchedEspeces

                        content = conseil.content
                    } else {
                        errorMessage = translate("conseil.edit.notFoundError")
                        showErrorDialog = true
                    }
                } else {
                    errorMessage = translate("conseil.edit.loadError")
                    showErrorDialog = true
                }
            } catch (e: Exception) {
                errorMessage = translate("conseil.edit.exceptionErrorFormat", e.message.orEmpty())
                showErrorDialog = true
            }
        }
        isLoading = false
    }

    // Mode d'édition ou de création
    val isEditMode = conseilId != null
    val screenTitle = if (isEditMode) translate("conseil.edit.titleEdit") else translate("conseil.edit.titleCreate")

    Scaffold(topBar = { TopBarSimple(title = screenTitle, onNavigateBack = onNavigateBack) }) {
            paddingValues ->
        if (isLoading) {
            Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = VetNutriColors.Primary) }
        } else {
            Column(
                    modifier =
                            Modifier.fillMaxSize()
                                    .padding(paddingValues)
                                    .padding(AppSizes.paddingMedium)
                                    .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section informations générales
                Card(elevation = 4.dp, shape = MaterialTheme.shapes.medium) {
                    Column(modifier = Modifier.padding(AppSizes.paddingMedium)) {
                        Text(
                                text = translate("conseil.edit.generalInfoTitle"),
                                style = MaterialTheme.typography.h6,
                                fontWeight = FontWeight.Bold,
                                color = VetNutriColors.Primary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Titre
                        OutlinedTextField(
                                value = conseilTitle,
                                onValueChange = { conseilTitle = it },
                                label = { Text(translate("conseil.edit.conseilTitleLabel")) },
                                modifier = Modifier.fillMaxWidth(),
                                colors =
                                        TextFieldDefaults.outlinedTextFieldColors(
                                                focusedBorderColor = VetNutriColors.Primary,
                                                unfocusedBorderColor = Color.Gray
                                        )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Catégorie
                        DropdownField(
                                selectedValue = category,
                                onValueChange = { category = it },
                                label = translate("conseil.edit.categoryLabel"),
                                options =
                                        SectionCategory.values().filter {
                                            it.name.contains("CONSEIL")
                                        },
                                valueToString = { cat -> cat.name.replace("CONSEIL_", "") },
                                modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Statut actif
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                    checked = isActive,
                                    onCheckedChange = { isActive = it },
                                    colors =
                                            CheckboxDefaults.colors(
                                                    checkedColor = VetNutriColors.Primary
                                            )
                            )
                            Text(translate("auto.view.conseileditview.actif"))
                        }
                    }
                }

                // Section espèces cibles - Utilise le composant réutilisable MultiSelectionCard
                MultiSelectionCard(
                        titre = "Espèces cibles",
                        elementsDisponibles = Espece.entries,
                        elementsSelectionnes = selectedEspecesState.toMutableList(),
                        onSelectionChange = { newSelection -> selectedEspecesState = newSelection },
                        getLabel = { espece -> espece.label },
                        getIdentifiant = { espece -> espece.name },
                        couleurArrierePlan = VetNutriColors.Secondary
                )

                // Section contenu
                Card(elevation = 4.dp, shape = MaterialTheme.shapes.medium) {
                    Column(modifier = Modifier.padding(AppSizes.paddingMedium)) {
                        Text(
                                text = translate("auto.view.conseileditview.contenu_du_conseil"),
                                style = MaterialTheme.typography.h6,
                                fontWeight = FontWeight.Bold,
                                color = VetNutriColors.Primary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        RichTextEditor(
                                initialContent = content,
                                onContentChange = { content = it },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 600.dp)
                        )
                    }
                }

                // Boutons d'action
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.weight(1f),
                            colors =
                                    ButtonDefaults.outlinedButtonColors(
                                            contentColor = VetNutriColors.Primary
                                    )
                    ) { Text(translate("general.cancel")) }

                    Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        isSaving = true

                                        val conseil =
                                                HtmlSection(
                                                        id = conseilId
                                                                        ?: "conseil_${Clock.System.now().toEpochMilliseconds()}",
                                                        title = conseilTitle,
                                                        content = content,
                                                        category = category,
                                                        tags = emptyList(), // Plus de tags
                                                        priority = 0, // Priorité par défaut
                                                        isActive = isActive,
                                                        targetSpecies =
                                                                selectedEspecesState.map { espece ->
                                                                    espece.id
                                                                },
                                                        targetAgeGroups =
                                                                emptyList() // Plus de groupes d'âge
                                                )

                                        val result = conseilRepository.sauvegarderConseil(conseil)
                                        if (result.isSuccess) {
                                            showSuccessDialog = true
                                        } else {
                                            errorMessage = "Erreur lors de la sauvegarde"
                                            showErrorDialog = true
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Erreur: ${e.message}"
                                        showErrorDialog = true
                                    } finally {
                                        isSaving = false
                                    }
                                }
                            },
                            enabled = !isSaving && conseilTitle.isNotBlank(),
                            modifier = Modifier.weight(1f),
                            colors =
                                    ButtonDefaults.buttonColors(
                                            backgroundColor = VetNutriColors.Primary
                                    )
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White
                            )
                        } else {
                            Text(if (isEditMode) "Modifier" else "Créer")
                        }
                    }
                }
            }
        }
    }

    // Dialog d'erreur
    if (showErrorDialog) {
        AlertDialog(
                onDismissRequest = { showErrorDialog = false },
                title = { Text(translate("general.error")) },
                text = { Text(errorMessage) },
                confirmButton = {
                    TextButton(
                            onClick = { showErrorDialog = false },
                            colors =
                                    ButtonDefaults.textButtonColors(
                                            contentColor = VetNutriColors.Primary
                                    )
                    ) { Text(translate("general.ok")) }
                }
        )
    }

    // Dialog de succès
    if (showSuccessDialog) {
        AlertDialog(
                onDismissRequest = {
                    showSuccessDialog = false
                    onNavigateBack()
                },
                title = { Text(translate("general.success")) },
                text = {
                    Text(translate(if (isEditMode) "auto.view.conseileditview.success_updated" else "auto.view.conseileditview.success_created"))
                },
                confirmButton = {
                    TextButton(
                            onClick = {
                                showSuccessDialog = false
                                onNavigateBack()
                            },
                            colors =
                                    ButtonDefaults.textButtonColors(
                                            contentColor = VetNutriColors.Primary
                                    )
                    ) { Text(translate("general.ok")) }
                }
        )
    }
}
