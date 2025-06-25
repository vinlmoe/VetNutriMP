package fr.vetbrain.vetnutri_mp.View.AnalNut

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.TextUtils

/** Composant pour afficher l'analyse nutritionnelle d'une ration */
@Composable
fun AnalyseNutritionnelleCard(
        ration: Ration,
        poidsMetabolique: Double?,
        referenceUtilisee: ReferenceEv?,
        besoinEnergetiqueEntretien: Double?,
        poidsAnimal: Double?,
        modifier: Modifier = Modifier,
        nutrimentsSelectionnes: List<String>? = null,
        onNutrimentClick: (String, ValeurNutritionnelle) -> Unit
) {
    // Etat pour basculer entre affichage filtré et complet
    var afficherTousLesNutriments by remember { mutableStateOf(false) }

    val valeursNutritionnelles =
            if (afficherTousLesNutriments ||
                            nutrimentsSelectionnes == null ||
                            nutrimentsSelectionnes.isEmpty()
            ) {
                analyserValeursNutritionnellesRation(ration)
            } else {
                analyserValeursNutritionnellesRationSelective(ration, nutrimentsSelectionnes)
            }

    val nutrimentsAffiches = valeursNutritionnelles

    Card(modifier = modifier, elevation = AppSizes.elevationSmall) {
        Column(
                modifier = Modifier.fillMaxSize().padding(AppSizes.paddingSmall),
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingXSmall)
        ) {
            // En-tête avec titre et bouton toggle
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        text = "Analyse nutritionnelle",
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Bold,
                        color = VetNutriColors.Primary,
                        modifier = Modifier.weight(1f)
                )

                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingXSmall)
                ) {
                    Text(
                            text = if (afficherTousLesNutriments) "Tous" else "Sélectionnés",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )

                    IconButton(
                            onClick = { afficherTousLesNutriments = !afficherTousLesNutriments },
                            modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                                imageVector =
                                        if (afficherTousLesNutriments) Icons.Filled.ToggleOn
                                        else Icons.Filled.ToggleOff,
                                contentDescription =
                                        if (afficherTousLesNutriments)
                                                "Afficher seulement les nutriments sélectionnés"
                                        else "Afficher tous les nutriments",
                                tint =
                                        if (afficherTousLesNutriments) VetNutriColors.Primary
                                        else MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Grid adaptatif qui ajuste automatiquement le nombre de colonnes (1 à 4)
            // Taille minimale de 400dp par colonne
            LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 125.dp),
                    verticalArrangement = Arrangement.spacedBy(AppSizes.paddingXSmall),
                    horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingXSmall),
                    modifier = Modifier.fillMaxWidth()
            ) {
                items(nutrimentsAffiches.toList()) { (nom, valeurNutritionnelle) ->
                    NutrimentCard(
                            nom = nom,
                            valeurNutritionnelle = valeurNutritionnelle,
                            poidsMetabolique = poidsMetabolique,
                            referenceUtilisee = referenceUtilisee,
                            besoinEnergetiqueEntretien = besoinEnergetiqueEntretien,
                            poidsAnimal = poidsAnimal,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onNutrimentClick(nom, valeurNutritionnelle) }
                    )
                }
            }
        }
    }
}

/** Composant card pour afficher un nutriment individuel */
@Composable
private fun NutrimentCard(
        nom: String,
        valeurNutritionnelle: ValeurNutritionnelle,
        poidsMetabolique: Double?,
        referenceUtilisee: ReferenceEv?,
        besoinEnergetiqueEntretien: Double?,
        poidsAnimal: Double?,
        modifier: Modifier = Modifier,
        onClick: () -> Unit
) {
    // Vérifier la conformité aux références
    val iconeConformite =
            obtenirIconeConformite(
                    valeurNutritionnelle,
                    referenceUtilisee,
                    besoinEnergetiqueEntretien,
                    poidsAnimal,
                    poidsMetabolique
            )

    Card(
            modifier = modifier.fillMaxWidth().clickable { onClick() },
            elevation = AppSizes.elevationSmall,
            backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(
                modifier = Modifier.padding(AppSizes.paddingXSmall),
                verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Nom du nutriment avec icônes de statut
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        text = nom,
                        style = MaterialTheme.typography.caption,
                        fontWeight = FontWeight.Bold,
                        color = VetNutriColors.Primary,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                )

                Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icône de conformité aux références (+ ou -)
                    iconeConformite?.let { conformite ->
                        if (conformite.isCritical) {
                            // Double icône pour les références
                            // critiques non respectées
                            Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                                Icon(
                                        imageVector = conformite.icone,
                                        contentDescription = conformite.description,
                                        tint = conformite.couleur,
                                        modifier = Modifier.size(10.dp)
                                )
                                Icon(
                                        imageVector = conformite.icone,
                                        contentDescription = conformite.description,
                                        tint = conformite.couleur,
                                        modifier = Modifier.size(10.dp)
                                )
                            }
                        } else {
                            // Icône simple pour les références
                            // optimales
                            Icon(
                                    imageVector = conformite.icone,
                                    contentDescription = conformite.description,
                                    tint = conformite.couleur,
                                    modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    // Icône de statut (seulement si incomplet)
                    if (!valeurNutritionnelle.complete) {
                        Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = "Informations incomplètes",
                                tint = VetNutriColors.Error,
                                modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // Valeur et unité (affichage par défaut pour l'instant)
            val valeurAffichee =
                    if (poidsMetabolique != null && poidsMetabolique > 0) {
                        val valeurParKgMetabolique = valeurNutritionnelle.valeur / poidsMetabolique
                        "${String.format("%.2f", valeurParKgMetabolique)} ${valeurNutritionnelle.unite.displayName}/kg${TextUtils.toSuperscript("0.75")}"
                    } else {
                        "${String.format("%.2f", valeurNutritionnelle.valeur)} ${valeurNutritionnelle.unite.displayName}"
                    }

            Text(
                    text = valeurAffichee,
                    style = MaterialTheme.typography.overline,
                    color = MaterialTheme.colors.onSurface,
                    maxLines = 2
            )
        }
    }
}

/** Données pour l'icône de conformité */
private data class IconeConformite(
        val icone: ImageVector,
        val couleur: Color,
        val description: String,
        val isCritical:
                Boolean // true pour MIN/MAX (double icône), false pour OPTIMIN/OPTIMAX (simple
// icône)
)

/**
 * Détermine l'icône de conformité aux références nutritionnelles
 *
 * @param valeurNutritionnelle Valeur nutritionnelle de la ration
 * @param referenceUtilisee Référence nutritionnelle utilisée
 * @param besoinEnergetiqueEntretien Besoin énergétique d'entretien
 * @param poidsAnimal Poids de l'animal
 * @param poidsMetabolique Poids métabolique
 * @return IconeConformite si non conforme, null sinon
 */
private fun obtenirIconeConformite(
        valeurNutritionnelle: ValeurNutritionnelle,
        referenceUtilisee: ReferenceEv?,
        besoinEnergetiqueEntretien: Double?,
        poidsAnimal: Double?,
        poidsMetabolique: Double?
): IconeConformite? {

    referenceUtilisee?.let { reference ->
        val nutrient = valeurNutritionnelle.nutriment
        val apportAbsolu = valeurNutritionnelle.valeur
        var hasReferences = false

        // Vérifier les minimums (MIN et OPTIMIN)
        listOf(Reflevel.MIN, Reflevel.OPTIMIN).forEach { level ->
            if (reference.contientNutriment(nutrient, level)) {
                hasReferences = true
                val valeurRef = reference.obtenirNutriment(nutrient, level)
                val uniteRef = UnitReqEnum.getById(reference.obtenirUniteNutriment(nutrient, level))

                val besoinAbsolu =
                        calculerBesoinAbsoluLocal(
                                valeurRef,
                                uniteRef,
                                besoinEnergetiqueEntretien,
                                poidsAnimal,
                                poidsMetabolique
                        )

                besoinAbsolu?.let { besoin ->
                    if (apportAbsolu < besoin) {
                        return IconeConformite(
                                icone = Icons.Filled.KeyboardArrowDown, // Icône
                                // "↓"
                                // pour
                                // carence
                                couleur = VetNutriColors.Error,
                                description =
                                        "Carence : apport inférieur au ${if (level == Reflevel.MIN) "minimum" else "optimal minimum"}",
                                isCritical = level == Reflevel.MIN
                        )
                    }
                }
            }
        }

        // Vérifier les maximums (MAX et OPTIMAX)
        listOf(Reflevel.MAX, Reflevel.OPTIMAX).forEach { level ->
            if (reference.contientNutriment(nutrient, level)) {
                hasReferences = true
                val valeurRef = reference.obtenirNutriment(nutrient, level)
                val uniteRef = UnitReqEnum.getById(reference.obtenirUniteNutriment(nutrient, level))

                val besoinAbsolu =
                        calculerBesoinAbsoluLocal(
                                valeurRef,
                                uniteRef,
                                besoinEnergetiqueEntretien,
                                poidsAnimal,
                                poidsMetabolique
                        )

                besoinAbsolu?.let { besoin ->
                    if (apportAbsolu > besoin) {
                        return IconeConformite(
                                icone = Icons.Filled.KeyboardArrowUp, // Icône
                                // "↑"
                                // pour
                                // excès
                                couleur = VetNutriColors.Error,
                                description =
                                        "Excès : apport supérieur au ${if (level == Reflevel.MAX) "maximum" else "optimal maximum"}",
                                isCritical = level == Reflevel.MAX
                        )
                    }
                }
            }
        }

        // Si il y a des références et qu'aucune n'est violée, tout est conforme
        if (hasReferences) {
            return IconeConformite(
                    icone = Icons.Filled.Check, // Icône "✓" pour conformité
                    couleur = Color.Green,
                    description = "Conforme : toutes les références sont respectées",
                    isCritical = false
            )
        }
    }

    return null // Pas de référence
}

/**
 * Version locale de la fonction de calcul des besoins absolus (Copie de celle dans
 * DetailNutrimentAnalysis.kt pour éviter les dépendances)
 */
private fun calculerBesoinAbsoluLocal(
        valeurRef: Float,
        uniteRef: UnitReqEnum,
        besoinEnergetiqueEntretien: Double?,
        poidsAnimal: Double?,
        poidsMetabolique: Double?
): Double? {
    return when (uniteRef) {
        UnitReqEnum.PERKCAL -> {
            besoinEnergetiqueEntretien?.let { bee -> (valeurRef * bee) / 1000.0 }
        }
        UnitReqEnum.PERKJ -> {
            besoinEnergetiqueEntretien?.let { bee ->
                val beeEnKj = bee * 4.184
                (valeurRef * beeEnKj) / 1000.0
            }
        }
        UnitReqEnum.PERKG -> {
            poidsAnimal?.let { poids -> valeurRef * poids }
        }
        UnitReqEnum.PERMS -> {
            poidsMetabolique?.let { poidsMetab -> valeurRef * poidsMetab }
        }
        UnitReqEnum.ABSOLUTE -> {
            valeurRef.toDouble()
        }
        UnitReqEnum.RATIO -> null
        else -> null
    }
}
