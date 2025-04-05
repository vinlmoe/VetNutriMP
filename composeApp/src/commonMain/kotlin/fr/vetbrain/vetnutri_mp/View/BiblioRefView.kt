package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import fr.vetbrain.vetnutri_mp.Repository.InMemoryBiblioRefRepository
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.BiblioRefViewModel
import java.util.*
import kotlinx.coroutines.launch

/** Vue principale pour la gestion des références bibliographiques */
@Composable
fun BiblioRefView() {
    val repository = remember { InMemoryBiblioRefRepository() }
    val viewModel = remember { BiblioRefViewModel(repository) }
    val coroutineScope = rememberCoroutineScope()

    val biblioRefs by viewModel.allBiblioRefs.collectAsState()
    val operationMessage by viewModel.operationMessage.collectAsState()
    val actionInProgress by viewModel.actionInProgress.collectAsState()

    LaunchedEffect(biblioRefs) {
        println("DEBUG BiblioRefView: Liste mise à jour - ${biblioRefs.size} références")
    }

    val (refToDelete, setRefToDelete) = remember { mutableStateOf<BiblioRef?>(null) }

    LaunchedEffect(Unit) {
        println("DEBUG BiblioRefView: Initialisation")

        // Initialiser le ViewModel avec une nouvelle référence
        viewModel.initForEdit()

        // Ajouter des références de test uniquement si la liste est vide
        if (biblioRefs.isEmpty()) {
            coroutineScope.launch {
                println("DEBUG BiblioRefView: Ajout de références de test")

                // Attendre un peu pour assurer que le repository est prêt
                kotlinx.coroutines.delay(100)

                // Générer des UUID uniques pour les références
                val uuid1 = UUID.randomUUID().toString()
                val uuid2 = UUID.randomUUID().toString()

                // Ajouter la première référence
                repository.insertBiblioRef(
                        BiblioRef(
                                uuid = uuid1,
                                firstAuthor = "Smith",
                                year = 2020,
                                completeRef =
                                        "Smith J, et al. (2020) Nutrition canine. Journal of Veterinary Science, 45(2), 102-115.",
                                comments = "Étude sur les besoins nutritionnels des chiens adultes"
                        )
                )

                // Attendre un peu entre les insertions
                kotlinx.coroutines.delay(100)

                // Ajouter la deuxième référence
                repository.insertBiblioRef(
                        BiblioRef(
                                uuid = uuid2,
                                firstAuthor = "Dupont",
                                year = 2018,
                                completeRef =
                                        "Dupont M, et al. (2018) Alimentation féline. Revue Vétérinaire, 30(1), 56-72.",
                                comments = "Recherche sur les allergies alimentaires chez les chats"
                        )
                )

                println(
                        "DEBUG BiblioRefView: Références de test ajoutées avec les UUID: $uuid1, $uuid2"
                )
            }
        }
    }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Gestion des références bibliographiques") },
                        backgroundColor = VetNutriColors.Primary,
                        contentColor = VetNutriColors.OnPrimary
                )
            }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(modifier = Modifier.fillMaxSize().padding(AppSizes.paddingMedium)) {
                // Formulaire d'ajout/édition
                BiblioRefForm(
                        viewModel = viewModel,
                        modifier =
                                Modifier.fillMaxWidth()
                                        .background(
                                                color = VetNutriColors.Surface,
                                                shape = RoundedCornerShape(AppSizes.cornerRadius)
                                        )
                                        .padding(AppSizes.paddingMedium)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Liste des références
                BiblioRefList(
                        biblioRefs = biblioRefs,
                        onEditClick = { biblioRef -> viewModel.initForEdit(biblioRef) },
                        onDeleteClick = { biblioRef -> setRefToDelete(biblioRef) },
                        modifier = Modifier.fillMaxWidth().weight(1f)
                )
            }

            // Message de succès ou d'erreur
            operationMessage?.let { message ->
                AlertDialog(
                        onDismissRequest = { viewModel.clearOperationMessage() },
                        title = { Text("Information") },
                        text = { Text(message) },
                        confirmButton = {
                            Button(onClick = { viewModel.clearOperationMessage() }) { Text("OK") }
                        }
                )
            }

            // Dialogue de confirmation de suppression
            refToDelete?.let { biblioRef ->
                AlertDialog(
                        onDismissRequest = { setRefToDelete(null) },
                        title = { Text("Confirmation de suppression") },
                        text = {
                            Text(
                                    "Êtes-vous sûr de vouloir supprimer la référence '${biblioRef.firstAuthor}, ${biblioRef.year}' ?"
                            )
                        },
                        confirmButton = {
                            Button(
                                    onClick = {
                                        viewModel.deleteBiblioRef(biblioRef)
                                        setRefToDelete(null)
                                    }
                            ) { Text("Supprimer") }
                        },
                        dismissButton = {
                            OutlinedButton(onClick = { setRefToDelete(null) }) { Text("Annuler") }
                        }
                )
            }

            // Indicateur de chargement
            if (actionInProgress) {
                CircularProgressIndicator(modifier = Modifier.size(50.dp).align(Alignment.Center))
            }
        }
    }
}

/** Formulaire d'ajout/édition d'une référence bibliographique */
@Composable
fun BiblioRefForm(viewModel: BiblioRefViewModel, modifier: Modifier = Modifier) {
    val currentBiblioRef by viewModel.currentBiblioRef.collectAsState()
    val firstAuthor by viewModel.firstAuthor
    val year by viewModel.year
    val completeRef by viewModel.completeRef
    val comments by viewModel.comments
    val isValid by viewModel.isValid

    Column(modifier = modifier) {
        Text(
                text =
                        if (currentBiblioRef == BiblioRef.EMPTY) "Ajouter une référence"
                        else "Modifier la référence",
                style = MaterialTheme.typography.h6
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Champ pour le premier auteur
        OutlinedTextField(
                value = firstAuthor,
                onValueChange = { viewModel.updateFirstAuthor(it) },
                label = { Text("Premier auteur*") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Champ pour l'année
        OutlinedTextField(
                value = year,
                onValueChange = { viewModel.updateYear(it) },
                label = { Text("Année*") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Champ pour la référence complète
        OutlinedTextField(
                value = completeRef,
                onValueChange = { viewModel.updateCompleteRef(it) },
                label = { Text("Référence complète*") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Champ pour les commentaires
        OutlinedTextField(
                value = comments,
                onValueChange = { viewModel.updateComments(it) },
                label = { Text("Commentaires") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Boutons d'action
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            OutlinedButton(
                    onClick = { viewModel.initForEdit() },
                    modifier = Modifier.padding(end = 8.dp)
            ) { Text("Réinitialiser") }

            Button(onClick = { viewModel.saveBiblioRef() }, enabled = isValid) {
                Text(if (currentBiblioRef == BiblioRef.EMPTY) "Ajouter" else "Mettre à jour")
            }
        }
    }
}

/** Liste des références bibliographiques */
@Composable
fun BiblioRefList(
        biblioRefs: List<BiblioRef>,
        onEditClick: (BiblioRef) -> Unit,
        onDeleteClick: (BiblioRef) -> Unit,
        modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
                text = "Liste des références (${biblioRefs.size})",
                style = MaterialTheme.typography.h6
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(biblioRefs) { biblioRef ->
                BiblioRefItem(
                        biblioRef = biblioRef,
                        onEditClick = { onEditClick(biblioRef) },
                        onDeleteClick = { onDeleteClick(biblioRef) }
                )

                Divider()
            }
        }
    }
}

/** Élément représentant une référence bibliographique dans la liste */
@Composable
fun BiblioRefItem(biblioRef: BiblioRef, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), elevation = 2.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                            text = "${biblioRef.firstAuthor}, ${biblioRef.year}",
                            style = MaterialTheme.typography.subtitle1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(text = biblioRef.completeRef, style = MaterialTheme.typography.body2)

                    if (biblioRef.comments.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                                text = "Commentaire: ${biblioRef.comments}",
                                style = MaterialTheme.typography.caption
                        )
                    }
                }

                // Boutons d'action
                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(
                                imageVector = AppIcons.Edit,
                                contentDescription = "Modifier",
                                tint = VetNutriColors.Primary
                        )
                    }

                    IconButton(onClick = onDeleteClick) {
                        Icon(
                                imageVector = AppIcons.Delete,
                                contentDescription = "Supprimer",
                                tint = VetNutriColors.Error
                        )
                    }
                }
            }
        }
    }
}
