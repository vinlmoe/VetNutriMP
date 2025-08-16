package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Enumer.MainNutrientEnum
import fr.vetbrain.vetnutri_mp.Repository.BiblioRefRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseReferenceEvRepository
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.PlatformDispatcher
import fr.vetbrain.vetnutri_mp.ViewModel.NutrientRefViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.ReferenceEvViewModel

/**
 * Vue pour l'édition des besoins nutritionnels d'une référence évaluée
 *
 * @param referenceEvViewModel ViewModel pour gérer la référence évaluée
 * @param biblioRefRepository Repository pour accéder aux références bibliographiques
 * @param referenceEvRepository Repository pour accéder aux références évaluées
 * @param platformDispatcher PlatformDispatcher pour gérer les tâches de fond
 * @param referenceEvId Identifiant de la référence à éditer
 * @param onNavigateBack Callback pour revenir à l'écran précédent
 * @param modifier Modifier à appliquer à la vue
 * @param isInTabView Indique si la vue est affichée dans un TabView
 */
@Composable
fun ReferenceEvNutrientView(
        referenceEvViewModel: ReferenceEvViewModel,
        biblioRefRepository: BiblioRefRepository,
        referenceEvRepository: DatabaseReferenceEvRepository,
        platformDispatcher: PlatformDispatcher,
        referenceEvId: String,
        onNavigateBack: () -> Unit,
        modifier: Modifier = Modifier,
        isInTabView: Boolean = false
) {
        // Charger la référence
        val currentReferenceEv by
                referenceEvViewModel.currentReferenceEv.collectAsState(initial = ReferenceEv())

        // Créer le ViewModel spécifique pour les besoins nutritionnels
        val nutrientRefViewModel = remember {
                NutrientRefViewModel(
                        referenceEvRepository = referenceEvRepository,
                        biblioRefRepository = biblioRefRepository,
                        platformDispatcher = platformDispatcher
                )
        }

        // Positionner la référence en cours d'édition
        LaunchedEffect(referenceEvId) { referenceEvViewModel.loadReferenceEvById(referenceEvId) }

        // Quand la référence est chargée, l'envoyer au nutrientRefViewModel
        LaunchedEffect(currentReferenceEv.uuid) {
                if (currentReferenceEv.uuid.isNotBlank()) {
                        nutrientRefViewModel.setReference(currentReferenceEv)
                }
        }

        // Variables d'état pour l'interface
        var selectedNutrientType by remember { mutableStateOf<MainNutrientEnum?>(null) }

        // Afficher un écran de chargement si nécessaire
        val loading by nutrientRefViewModel.loading.collectAsState(initial = false)

        // Définir le contenu principal
        val content: @Composable (PaddingValues) -> Unit = { paddingValues ->
                Column(modifier = modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
                        // Informations sur la référence
                        Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                                text = "Référence: ${currentReferenceEv.nom}",
                                                style = MaterialTheme.typography.h6,
                                                color = VetNutriColors.Primary
                                        )
                                        Text(
                                                text = "Espèce: ${currentReferenceEv.espece}",
                                                style = MaterialTheme.typography.body1
                                        )
                                        Text(
                                                text =
                                                        "Stade physiologique: ${currentReferenceEv.stadePhysio}",
                                                style = MaterialTheme.typography.body1
                                        )
                                        if (currentReferenceEv.maladie) {
                                                Text(
                                                        text =
                                                                "Maladie: ${currentReferenceEv.nomMaladie}",
                                                        style = MaterialTheme.typography.body1,
                                                        color = Color.Red
                                                )
                                        }
                                }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Sélection du type de nutriments à éditer
                        Text(
                                text = "Sélectionnez une catégorie de nutriments :",
                                style = MaterialTheme.typography.h6
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Boutons pour chaque type de nutriments
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                                MainNutrientEnum.values().forEach { nutrientType ->
                                        OutlinedButton(
                                                onClick = { selectedNutrientType = nutrientType },
                                                colors =
                                                        ButtonDefaults.outlinedButtonColors(
                                                                backgroundColor =
                                                                        if (selectedNutrientType ==
                                                                                        nutrientType
                                                                        )
                                                                                VetNutriColors
                                                                                        .Primary
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.1
                                                                                        )
                                                                        else Color.Transparent,
                                                                contentColor =
                                                                        if (selectedNutrientType ==
                                                                                        nutrientType
                                                                        )
                                                                                VetNutriColors
                                                                                        .Primary
                                                                        else Color.Gray
                                                        ),
                                                modifier = Modifier.padding(4.dp)
                                        ) { Text(nutrientType.name) }
                                }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Zone d'édition des nutriments sélectionnés
                        if (selectedNutrientType != null) {
                                Card(
                                        modifier = Modifier.fillMaxWidth().weight(1.0f),
                                        elevation = 4.dp
                                ) {
                                        if (loading) {
                                                Box(
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentAlignment = Alignment.Center
                                                ) {
                                                        CircularProgressIndicator(
                                                                color = VetNutriColors.Primary
                                                        )
                                                }
                                        } else {
                                                // Utiliser la vue d'édition des nutriments par type
                                                NutrientRefEditView(
                                                        viewModel = nutrientRefViewModel,
                                                        nutrientType = selectedNutrientType!!,
                                                        onNavigateBack = {
                                                                selectedNutrientType = null
                                                        }
                                                )
                                        }
                                }
                        } else {
                                // Afficher un message lorsqu'aucun type n'est sélectionné
                                Box(
                                        modifier = Modifier.fillMaxWidth().weight(1.0f),
                                        contentAlignment = Alignment.Center
                                ) {
                                        Text(
                                                text =
                                                        "Sélectionnez une catégorie de nutriments pour éditer les besoins",
                                                style = MaterialTheme.typography.body1,
                                                color = Color.Gray
                                        )
                                }
                        }
                }
        }

        // Utiliser le Scaffold uniquement si on n'est pas dans un TabView
        if (isInTabView) {
                content(PaddingValues(0.dp))
        } else {
                Scaffold(
                        topBar = {
                                TopBarSimple(
                                        title = "Besoins nutritionnels - ${currentReferenceEv.nom}",
                                        onNavigateBack = onNavigateBack
                                )
                        }
                ) { paddingValues -> content(paddingValues) }
        }
}
