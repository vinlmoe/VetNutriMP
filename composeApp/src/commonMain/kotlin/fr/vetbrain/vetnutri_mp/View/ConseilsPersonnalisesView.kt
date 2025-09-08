package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.vetbrain.vetnutri_mp.Export.*
import fr.vetbrain.vetnutri_mp.Repository.ConseilRepository
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ConseilsPersonnalisesView(
        conseilRepository: ConseilRepository, 
        onNavigateBack: () -> Unit,
        onEditConseil: (String) -> Unit = {},
        onCreateConseil: () -> Unit = {}
) {
    var conseils by remember { mutableStateOf<List<HtmlSection>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<SectionCategory?>(null) }
    var showTemplates by remember { mutableStateOf(false) }
    var conseilToDelete by remember { mutableStateOf<HtmlSection?>(null) }

    val scope = rememberCoroutineScope()

    // Charger les conseils au démarrage
    LaunchedEffect(Unit) {
        try {
            val result = conseilRepository.getConseilsActifs()
            if (result.isSuccess) {
                conseils = result.getOrThrow()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    // Filtrer les conseils selon la recherche et la catégorie
    val filteredConseils =
            remember(conseils, searchQuery, selectedCategory) {
                conseils.filter { conseil ->
                    val matchesSearch =
                            searchQuery.isEmpty() ||
                                    conseil.title.contains(searchQuery, ignoreCase = true) ||
                                    conseil.tags.any { it.contains(searchQuery, ignoreCase = true) }

                    val matchesCategory =
                            selectedCategory == null || conseil.category == selectedCategory

                    matchesSearch && matchesCategory
                }
            }

    Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                        onClick = { onCreateConseil() },
                        backgroundColor = VetNutriColors.Primary
                ) {
                    Icon(
                            imageVector = AppIcons.Add,
                            contentDescription = "Ajouter un conseil",
                            tint = VetNutriColors.OnPrimary
                    )
                }
            }
    ) { paddingValues ->
        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .padding(paddingValues)
                                .padding(AppSizes.paddingMedium)
        ) {
            // Barre de recherche
            OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Rechercher un conseil") },
                    leadingIcon = {
                        Icon(imageVector = AppIcons.Search, contentDescription = "Rechercher")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = AppIcons.Close, contentDescription = "Effacer")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                            TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = VetNutriColors.Primary,
                                    unfocusedBorderColor = Color.Gray
                            )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Filtres par catégorie
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Chip(onClick = { selectedCategory = null }, modifier = Modifier.height(32.dp)) {
                    Text("Toutes")
                }

                SectionCategory.values()
                        .filter { it.name.contains("CONSEIL") }
                        .take(4) // Limiter à 4 catégories pour l'affichage
                        .forEach { category ->
                            Chip(
                                    onClick = { selectedCategory = category },
                                    modifier = Modifier.height(32.dp)
                            ) { Text(category.name.replace("CONSEIL_", "")) }
                        }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bouton pour voir les templates
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(
                        onClick = { showTemplates = true },
                        colors =
                                ButtonDefaults.outlinedButtonColors(
                                        contentColor = VetNutriColors.Primary
                                )
                ) {
                    Icon(
                            imageVector = AppIcons.Library,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Templates")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Affichage des résultats
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VetNutriColors.Primary)
                }
            } else if (filteredConseils.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                            text =
                                    if (searchQuery.isNotEmpty() || selectedCategory != null) {
                                        "Aucun conseil ne correspond à votre recherche"
                                    } else {
                                        "Aucun conseil personnalisé disponible"
                                    },
                            style = MaterialTheme.typography.h6
                    )
                }
            } else {
                Text(
                        text = "Liste des conseils (${filteredConseils.size})",
                        style = MaterialTheme.typography.h6
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredConseils) { conseil ->
                        ConseilCard(
                                conseil = conseil,
                                onClick = {
                                    onEditConseil(conseil.id)
                                },
                                onEdit = {
                                    onEditConseil(conseil.id)
                                },
                                onDelete = { conseilToDelete = conseil }
                        )
                    }
                }
            }
        }
    }


    // Dialog pour afficher les templates
    if (showTemplates) {
        TemplatesDialog(
                conseilRepository = conseilRepository,
                onDismiss = { showTemplates = false },
                onSelectTemplate = { template ->
                    // TODO: Créer un conseil à partir du template
                    showTemplates = false
                }
        )
    }

    // Dialog de confirmation de suppression
    if (conseilToDelete != null) {
        AlertDialog(
                onDismissRequest = { conseilToDelete = null },
                title = { Text("Supprimer le conseil") },
                text = {
                    Text(
                            "Êtes-vous sûr de vouloir supprimer le conseil \"${conseilToDelete?.title}\" ?"
                    )
                },
                confirmButton = {
                    TextButton(
                            onClick = {
                                conseilToDelete?.let { conseil ->
                                    scope.launch {
                                        try {
                                            conseilRepository.supprimerConseil(conseil.id)
                                            // Recharger la liste
                                            val result = conseilRepository.getConseilsActifs()
                                            if (result.isSuccess) {
                                                conseils = result.getOrThrow()
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                                conseilToDelete = null
                            },
                            colors =
                                    ButtonDefaults.textButtonColors(
                                            contentColor = VetNutriColors.Primary
                                    )
                    ) { Text("Supprimer") }
                },
                dismissButton = {
                    TextButton(onClick = { conseilToDelete = null }) { Text("Annuler") }
                }
        )
    }
}

@Composable
private fun ConseilCard(
        conseil: HtmlSection,
        onClick: () -> Unit,
        onEdit: () -> Unit,
        onDelete: () -> Unit
) {
    Card(
            modifier = Modifier.fillMaxWidth().clickable { onClick() },
            elevation = 4.dp,
            shape = RoundedCornerShape(8.dp),
            backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(modifier = Modifier.padding(AppSizes.paddingMedium)) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                            text = conseil.title,
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colors.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                            text = conseil.category.name.replace("CONSEIL_", ""),
                            style = MaterialTheme.typography.caption,
                            color = VetNutriColors.Primary
                    )
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                                imageVector = AppIcons.Edit,
                                contentDescription = "Modifier",
                                tint = VetNutriColors.Primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                                imageVector = AppIcons.Delete,
                                contentDescription = "Supprimer",
                                tint = Color.Red
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tags
            if (conseil.tags.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    conseil.tags.take(3).forEach { tag ->
                        Surface(
                                color = VetNutriColors.Primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                    text = tag,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.caption,
                                    fontSize = 10.sp,
                                    color = VetNutriColors.Primary
                            )
                        }
                    }
                    if (conseil.tags.size > 3) {
                        Text(
                                text = "+${conseil.tags.size - 3}",
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Informations supplémentaires
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                        text = "Utilisé ${conseil.usageCount} fois",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )

                if (conseil.priority > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = Color(0xFFFFD700)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                                text = "Priorité ${conseil.priority}",
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun TemplatesDialog(
        conseilRepository: ConseilRepository,
        onDismiss: () -> Unit,
        onSelectTemplate: (HtmlSection) -> Unit
) {
    var templates by remember { mutableStateOf<List<HtmlSection>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val result = conseilRepository.getTemplatesConseils()
            if (result.isSuccess) {
                templates = result.getOrThrow()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Templates de conseils") },
            text = {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (templates.isEmpty()) {
                    Text("Aucun template disponible")
                } else {
                    LazyColumn(modifier = Modifier.height(300.dp)) {
                        items(templates) { template ->
                            Card(
                                    modifier =
                                            Modifier.fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .clickable { onSelectTemplate(template) },
                                    elevation = 1.dp
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                            text = template.title,
                                            style = MaterialTheme.typography.subtitle1,
                                            fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                            text = template.category.name.replace("CONSEIL_", ""),
                                            style = MaterialTheme.typography.caption,
                                            color = MaterialTheme.colors.primary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                        onClick = onDismiss,
                        colors =
                                ButtonDefaults.textButtonColors(
                                        contentColor = VetNutriColors.Primary
                                )
                ) { Text("Fermer") }
            }
    )
}
