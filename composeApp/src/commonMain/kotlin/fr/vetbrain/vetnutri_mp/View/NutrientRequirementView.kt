package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors

/**
 * Vue des besoins nutritionnels Cette vue affiche une liste des besoins nutritionnels de base pour
 * différentes espèces
 *
 * @param onNavigateBack Callback pour revenir à la vue précédente
 * @param modifier Modifier à appliquer à la vue
 */
@Composable
fun NutrientRequirementView(onNavigateBack: () -> Unit, modifier: Modifier = Modifier) {
    var selectedSpecies by remember { mutableStateOf("Chien") }

    // Besoins nutritionnels de base pour un chien
    val nutritionRequirements = remember {
        mapOf(
                "Chien" to
                        listOf(
                                NutrientRequirement(
                                        "Énergie",
                                        "Besoins énergétiques",
                                        "95-130 kcal/kg^0.75"
                                ),
                                NutrientRequirement(
                                        "Protéines",
                                        "Matière azotée totale",
                                        "18-25% MS"
                                ),
                                NutrientRequirement("Lipides", "Matière grasse", "5-15% MS"),
                                NutrientRequirement("Calcium", "Minéral essentiel", "0.5-1.8% MS"),
                                NutrientRequirement("Phosphore", "Minéral essentiel", "0.4-1.6% MS")
                        ),
                "Chat" to
                        listOf(
                                NutrientRequirement(
                                        "Énergie",
                                        "Besoins énergétiques",
                                        "100-140 kcal/kg^0.67"
                                ),
                                NutrientRequirement(
                                        "Protéines",
                                        "Matière azotée totale",
                                        "25-35% MS"
                                ),
                                NutrientRequirement("Lipides", "Matière grasse", "10-30% MS"),
                                NutrientRequirement("Calcium", "Minéral essentiel", "0.6-1.5% MS"),
                                NutrientRequirement(
                                        "Taurine",
                                        "Acide aminé essentiel",
                                        "0.1-0.2% MS"
                                )
                        )
        )
    }

    Scaffold(
            topBar = {
                TopBarSimple(title = "Besoins Nutritionnels", onNavigateBack = onNavigateBack)
            }
    ) { paddingValues ->
        Column(modifier = modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            // Sélection de l'espèce
            Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                            "Espèce",
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                                onClick = { selectedSpecies = "Chien" },
                                colors =
                                        ButtonDefaults.buttonColors(
                                                backgroundColor =
                                                        if (selectedSpecies == "Chien")
                                                                VetNutriColors.Primary
                                                        else VetNutriColors.Surface,
                                                contentColor =
                                                        if (selectedSpecies == "Chien")
                                                                VetNutriColors.OnPrimary
                                                        else VetNutriColors.OnSurface
                                        ),
                                modifier = Modifier.weight(1f)
                        ) { Text("Chien") }

                        Button(
                                onClick = { selectedSpecies = "Chat" },
                                colors =
                                        ButtonDefaults.buttonColors(
                                                backgroundColor =
                                                        if (selectedSpecies == "Chat")
                                                                VetNutriColors.Primary
                                                        else VetNutriColors.Surface,
                                                contentColor =
                                                        if (selectedSpecies == "Chat")
                                                                VetNutriColors.OnPrimary
                                                        else VetNutriColors.OnSurface
                                        ),
                                modifier = Modifier.weight(1f)
                        ) { Text("Chat") }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Liste des besoins nutritionnels
            Text(
                    "Besoins Nutritionnels - $selectedSpecies",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(nutritionRequirements[selectedSpecies] ?: emptyList()) { requirement ->
                    NutrientRequirementCard(requirement = requirement)
                }
            }
        }
    }
}

/** Modèle de données pour un besoin nutritionnel */
data class NutrientRequirement(val name: String, val description: String, val value: String)

/** Carte affichant un besoin nutritionnel */
@Composable
fun NutrientRequirementCard(requirement: NutrientRequirement, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth(), elevation = 4.dp) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(2f)) {
                    Text(
                            text = requirement.name,
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Bold
                    )

                    Text(text = requirement.description, style = MaterialTheme.typography.body2)
                }

                Text(
                        text = requirement.value,
                        style = MaterialTheme.typography.body1,
                        fontWeight = FontWeight.Medium,
                        color = VetNutriColors.Primary,
                        modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
