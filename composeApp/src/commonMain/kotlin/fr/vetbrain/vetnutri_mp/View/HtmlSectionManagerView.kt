package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.vetbrain.vetnutri_mp.Components.RichTextEditor
import fr.vetbrain.vetnutri_mp.Export.*
import fr.vetbrain.vetnutri_mp.ViewModel.HtmlSectionViewModel

/** Écran principal pour gérer les sections HTML réutilisables */
@Composable
fun HtmlSectionManagerView(viewModel: HtmlSectionViewModel, onNavigateBack: () -> Unit) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<SectionCategory?>(null) }
    var showTemplatesOnly by remember { mutableStateOf(false) }

    val sections by viewModel.sections.collectAsState()
    val selectedSection by viewModel.selectedSection.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Mettre à jour les filtres dans le ViewModel
    LaunchedEffect(searchQuery) { viewModel.searchSections(searchQuery) }

    LaunchedEffect(selectedCategory) { viewModel.filterByCategory(selectedCategory) }

    LaunchedEffect(showTemplatesOnly) {
        if (showTemplatesOnly) {
            viewModel.toggleTemplatesOnly()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Barre d'outils supérieure
        TopAppBar(
                title = { Text("Gestionnaire de sections HTML") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Retour") }
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, "Créer une section")
                    }
                }
        )

        // Barre de recherche et filtres
        Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            // Champ de recherche
            OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Rechercher...") },
                    leadingIcon = { Icon(Icons.Default.Search, "Rechercher") }
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Filtre par catégorie
            var expanded by remember { mutableStateOf(false) }
            Box {
                OutlinedButton(onClick = { expanded = true }, modifier = Modifier.width(150.dp)) {
                    Text(selectedCategory?.name ?: "Toutes les catégories")
                    Icon(
                            if (expanded) Icons.Default.ArrowDropUp
                            else Icons.Default.ArrowDropDown,
                            "Sélectionner une catégorie"
                    )
                }

                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                            onClick = {
                                selectedCategory = null
                                expanded = false
                            }
                    ) { Text("Toutes les catégories") }
                    SectionCategory.entries.forEach { category ->
                        DropdownMenuItem(
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                        ) { Text(category.name) }
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Bascule modèles uniquement
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = showTemplatesOnly, onCheckedChange = { showTemplatesOnly = it })
                Text("Modèles uniquement", fontSize = 14.sp)
            }
        }

        // Contenu principal
        Row(modifier = Modifier.weight(1f)) {
            // Liste des sections
            Box(modifier = Modifier.weight(0.4f)) {
                SectionList(
                        sections = viewModel.getFilteredSections(),
                        selectedSection = selectedSection,
                        onSectionSelect = { viewModel.selectSection(it) },
                        onSectionDelete = { viewModel.deleteSection(it.id) },
                        onSectionDuplicate = {
                            viewModel.duplicateSection(it.id, "${it.title} (copie)")
                        },
                        isLoading = isLoading
                )
            }

            // Séparateur vertical
            Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(Color.LightGray))

            // Éditeur de section
            Box(modifier = Modifier.weight(0.6f)) {
                selectedSection?.let { section ->
                    SectionEditor(
                            section = section,
                            onSectionUpdate = { viewModel.updateSection(it) }
                    )
                }
                        ?: run {
                            Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                            ) {
                                Text(
                                        "Sélectionnez une section pour l'éditer",
                                        color = Color.Gray,
                                        fontSize = 16.sp
                                )
                            }
                        }
            }
        }
    }

    // Dialogues
    if (showCreateDialog) {
        CreateSectionDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { title, category ->
                    viewModel.createSection(title, category)
                    showCreateDialog = false
                }
        )
    }

    if (showEditDialog && selectedSection != null) {
        EditSectionDialog(
                section = selectedSection!!,
                onDismiss = { showEditDialog = false },
                onSave = { updatedSection ->
                    viewModel.updateSection(updatedSection)
                    showEditDialog = false
                }
        )
    }
}

/** Liste des sections avec filtrage */
@Composable
private fun SectionList(
        sections: List<HtmlSection>,
        selectedSection: HtmlSection?,
        onSectionSelect: (HtmlSection) -> Unit,
        onSectionDelete: (HtmlSection) -> Unit,
        onSectionDuplicate: (HtmlSection) -> Unit,
        isLoading: Boolean
) {
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (sections.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Aucune section trouvée", color = Color.Gray, fontSize = 16.sp)
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Grouper par catégorie
        val sectionsByCategory = sections.groupBy { it.category }

        sectionsByCategory.forEach { (category, categorySections) ->
            item {
                Text(
                        text = category.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(8.dp)
                )
            }

            items(categorySections) { section ->
                SectionListItem(
                        section = section,
                        isSelected = section.id == selectedSection?.id,
                        onSelect = { onSectionSelect(section) },
                        onDelete = { onSectionDelete(section) },
                        onDuplicate = { onSectionDuplicate(section) }
                )
            }
        }
    }
}

/** Élément de la liste des sections */
@Composable
private fun SectionListItem(
        section: HtmlSection,
        isSelected: Boolean,
        onSelect: () -> Unit,
        onDelete: () -> Unit,
        onDuplicate: () -> Unit
) {
    val backgroundColor = if (isSelected) Color.Blue.copy(alpha = 0.1f) else Color.Transparent

    Row(
            modifier =
                    Modifier.fillMaxWidth()
                            .background(backgroundColor)
                            .clickable(onClick = onSelect)
                            .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = section.title, fontWeight = FontWeight.Medium, maxLines = 1)
            if (section.tags.isNotEmpty()) {
                Text(
                        text = section.tags.joinToString(", ") { "#$it" },
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1
                )
            }
            if (section.isTemplate) {
                Text(
                        text = "MODÈLE",
                        fontSize = 10.sp,
                        color = Color.Blue,
                        fontWeight = FontWeight.Bold
                )
            }
        }

        Row {
            IconButton(onClick = onDuplicate) {
                Icon(Icons.Default.ContentCopy, "Dupliquer", tint = Color.Gray)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Supprimer", tint = Color.Red)
            }
        }
    }
}

/** Éditeur de section */
@Composable
private fun SectionEditor(section: HtmlSection, onSectionUpdate: (HtmlSection) -> Unit) {
    var currentSection by remember { mutableStateOf(section) }
    var currentContent by remember { mutableStateOf(section.content) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Barre d'outils de l'éditeur
        Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                    text = "Éditeur: ${currentSection.title}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
            )

            Row {
                Button(
                        onClick = { onSectionUpdate(currentSection.copy(content = currentContent)) }
                ) {
                    Icon(Icons.Default.Save, "Sauvegarder")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sauvegarder")
                }
            }
        }

        // Éditeur de texte enrichi
        RichTextEditor(
                initialContent = currentContent,
                onContentChange = { newContent -> currentContent = newContent },
                modifier = Modifier.fillMaxWidth().weight(1f).padding(8.dp)
        )
    }
}

/** Dialogue de création de section */
@Composable
private fun CreateSectionDialog(
        onDismiss: () -> Unit,
        onCreate: (String, SectionCategory) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(SectionCategory.GENERAL) }

    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Créer une nouvelle section") },
            text = {
                Column {
                    OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Titre de la section") },
                            modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Catégorie:", fontWeight = FontWeight.Medium)
                    SectionCategory.entries.forEach { category ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category }
                            )
                            Text(category.name)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onCreate(title, selectedCategory)
                            }
                        },
                        enabled = title.isNotBlank()
                ) { Text("Créer") }
            },
            dismissButton = { Button(onClick = onDismiss) { Text("Annuler") } }
    )
}

/** Dialogue d'édition des propriétés de la section */
@Composable
private fun EditSectionDialog(
        section: HtmlSection,
        onDismiss: () -> Unit,
        onSave: (HtmlSection) -> Unit
) {
    var title by remember { mutableStateOf(section.title) }
    var selectedCategory by remember { mutableStateOf(section.category) }
    var tags by remember { mutableStateOf(section.tags.joinToString(", ")) }
    var isTemplate by remember { mutableStateOf(section.isTemplate) }

    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Propriétés de la section") },
            text = {
                Column {
                    OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Titre") },
                            modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                            value = tags,
                            onValueChange = { tags = it },
                            label = { Text("Tags (séparés par des virgules)") },
                            modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Catégorie:", fontWeight = FontWeight.Medium)
                    SectionCategory.entries.forEach { category ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category }
                            )
                            Text(category.name)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isTemplate, onCheckedChange = { isTemplate = it })
                        Text("Marquer comme modèle")
                    }
                }
            },
            confirmButton = {
                Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                val updatedSection =
                                        section.copy(
                                                title = title,
                                                category = selectedCategory,
                                                tags =
                                                        tags.split(",").map { it.trim() }.filter {
                                                            it.isNotBlank()
                                                        },
                                                isTemplate = isTemplate
                                        )
                                onSave(updatedSection)
                            }
                        },
                        enabled = title.isNotBlank()
                ) { Text("Sauvegarder") }
            },
            dismissButton = { Button(onClick = onDismiss) { Text("Annuler") } }
    )
}
