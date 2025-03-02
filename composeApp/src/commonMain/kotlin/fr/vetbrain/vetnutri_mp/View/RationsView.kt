package fr.vetbrain.vetnutri_mp.View

// Importation des composants nécessaires
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import kotlinx.datetime.LocalDate

/**
 * Vue pour afficher les rations d'un animal
 *
 * @param viewModel ViewModel contenant les données de l'animal
 * @param modifier Modificateur optionnel pour personnaliser l'apparence
 */
@Composable
fun RationsView(viewModel: AnimalDetailViewModel, modifier: Modifier = Modifier) {
    val animal by viewModel.animal.collectAsState()
    val selectedConsultation by viewModel.selectedConsultation.collectAsState()
    val selectedRation by viewModel.selectedRation.collectAsState()

    // DEBUG: Afficher des informations sur les données disponibles
    LaunchedEffect(Unit) {
        println(
                "DEBUG Rations - Animal: ${animal?.nom}, Consultations: ${animal?.consultations?.size}"
        )
        println(
                "DEBUG Rations - Consultation sélectionnée: ${selectedConsultation?.date}, Rations: ${selectedConsultation?.rations?.size}"
        )
        println(
                "DEBUG Rations - Ration sélectionnée: ${selectedRation?.name}, Aliments: ${selectedRation?.alimentMutableList?.size}"
        )

        // Détails supplémentaires sur les aliments
        selectedRation?.alimentMutableList?.forEachIndexed { index, alimentRation ->
            println(
                    "DEBUG Aliment[$index]: UUID=${alimentRation.uuid}, refAlimUnif=${alimentRation.refAlimUnif}, aliment=${alimentRation.aliment?.nom ?: "null"}"
            )
        }
    }

    // Surveiller les changements de ration sélectionnée pour plus de détails
    LaunchedEffect(selectedRation) {
        println(
                "DEBUG Ration changée: ${selectedRation?.name}, Aliments: ${selectedRation?.alimentMutableList?.size}"
        )
        selectedRation?.alimentMutableList?.forEachIndexed { index, alimentRation ->
            println(
                    "DEBUG Aliment[$index]: UUID=${alimentRation.uuid}, refAlimUnif=${alimentRation.refAlimUnif}, aliment=${alimentRation.aliment?.nom ?: "null"}"
            )
        }
    }

    // Sélectionner automatiquement la consultation la plus récente si aucune n'est sélectionnée
    LaunchedEffect(animal) {
        if (selectedConsultation == null && animal?.consultations?.isNotEmpty() == true) {
            // Trouver la consultation la plus récente
            val defaultDate = LocalDate(2000, 1, 1)
            val mostRecentConsultation =
                    animal?.consultations?.maxByOrNull { it.date ?: defaultDate }
            println("DEBUG Rations - Sélection auto consultation: ${mostRecentConsultation?.date}")
            mostRecentConsultation?.let { viewModel.selectConsultation(it) }
        }
    }

    // Sélectionner automatiquement la première ration si aucune n'est sélectionnée
    LaunchedEffect(selectedConsultation) {
        if (selectedRation == null && selectedConsultation?.rations?.isNotEmpty() == true) {
            val firstRation = selectedConsultation?.rations?.firstOrNull()
            println("DEBUG Rations - Sélection auto ration: ${firstRation?.name}")
            firstRation?.let { viewModel.selectRation(it) }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // En-tête compact avec les informations essentielles
            Card(
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    elevation = 4.dp,
                    backgroundColor = MaterialTheme.colors.surface
            ) {
                Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                                text = "Consultation: ${selectedConsultation?.date ?: "Aucune"}",
                                style = MaterialTheme.typography.subtitle1,
                                color = VetNutriColors.Primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                                text =
                                        "Ration: ${selectedRation?.name ?: "Aucune"} ${if (selectedRation?.actual == true) "(Actuelle)" else "(Proposée)"}",
                                style = MaterialTheme.typography.body2,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Contenu principal - grille 2x2 de cartes
            Box(modifier = Modifier.weight(1f)) {
                Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Colonne gauche
                    Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Segment 2: Liste des rations de la consultation
                        Card(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                elevation = 4.dp,
                                backgroundColor = MaterialTheme.colors.surface
                        ) {
                            Column(
                                    modifier = Modifier.fillMaxSize().padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                        text = "Rations de la consultation",
                                        style = MaterialTheme.typography.h6,
                                        color = VetNutriColors.Primary
                                )
                                Divider()
                                if (selectedConsultation?.rations.isNullOrEmpty()) {
                                    CenteredMessage(
                                            message = "Aucune ration disponible",
                                            modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    LazyColumn(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(selectedConsultation?.rations ?: emptyList()) { ration
                                            ->
                                            RationItem(
                                                    ration = ration,
                                                    isSelected =
                                                            selectedRation?.uuid == ration.uuid,
                                                    onClick = { viewModel.selectRation(ration) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Segment 3: Liste des aliments de la ration
                        Card(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                elevation = 4.dp,
                                backgroundColor = MaterialTheme.colors.surface
                        ) {
                            Column(
                                    modifier = Modifier.fillMaxSize().padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                        text = "Aliments de la ration",
                                        style = MaterialTheme.typography.h6,
                                        color = VetNutriColors.Primary
                                )
                                Divider()
                                if (selectedRation?.alimentMutableList.isNullOrEmpty()) {
                                    CenteredMessage(
                                            message = "Aucun aliment dans cette ration",
                                            modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    LazyColumn(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(selectedRation?.alimentMutableList ?: emptyList()) {
                                                aliment ->
                                            AlimentItem(aliment = aliment)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Colonne droite
                    Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Segment 4: Détails de la ration
                        Card(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                elevation = 4.dp,
                                backgroundColor = MaterialTheme.colors.surface
                        ) {
                            Column(
                                    modifier = Modifier.fillMaxSize().padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                        text = "Détails de la ration",
                                        style = MaterialTheme.typography.h6,
                                        color = VetNutriColors.Primary
                                )
                                Divider()
                                if (selectedRation == null) {
                                    CenteredMessage(
                                            message = "Aucune ration sélectionnée",
                                            modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        InfoRow(label = "Nom", value = selectedRation?.name ?: "")
                                        InfoRow(
                                                label = "Type",
                                                value =
                                                        if (selectedRation?.actual == true)
                                                                "Actuelle"
                                                        else "Proposée"
                                        )
                                        InfoRow(
                                                label = "Nombre d'aliments",
                                                value =
                                                        "${selectedRation?.alimentMutableList?.size ?: 0}"
                                        )
                                    }
                                }
                            }
                        }

                        // Segment 5: Informations nutritionnelles
                        Card(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                elevation = 4.dp,
                                backgroundColor = MaterialTheme.colors.surface
                        ) {
                            Column(
                                    modifier = Modifier.fillMaxSize().padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                        text = "Informations nutritionnelles",
                                        style = MaterialTheme.typography.h6,
                                        color = VetNutriColors.Primary
                                )
                                Divider()
                                if (selectedRation == null) {
                                    CenteredMessage(
                                            message = "Aucune ration sélectionnée",
                                            modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Box(
                                            modifier = Modifier.weight(1f),
                                            contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                                text =
                                                        "Les informations nutritionnelles seront calculées à partir des aliments de la ration.",
                                                style = MaterialTheme.typography.body2,
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
    }
}
