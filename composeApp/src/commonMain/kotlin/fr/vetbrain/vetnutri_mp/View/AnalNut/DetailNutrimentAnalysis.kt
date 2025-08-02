package fr.vetbrain.vetnutri_mp.View.AnalNut

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Data.ValeurNutritionnelle
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.Reflevel
import fr.vetbrain.vetnutri_mp.Enumer.TypeExpressionBesoin
import fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum
import fr.vetbrain.vetnutri_mp.Repository.PreferencesRepository
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.PreferencesStorage
import fr.vetbrain.vetnutri_mp.Utils.TextUtils
import io.github.koalaplot.core.bar.BulletGraphs
import io.github.koalaplot.core.bar.FixedFraction
import io.github.koalaplot.core.bar.HorizontalBarIndicator
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.xygraph.FloatLinearAxisModel

// Fonction locale InfoRow po

/**
 * Détermine la couleur d'affichage selon la conformité aux références
 *
 * @param level Niveau de référence (MIN, MAX, etc.)
 * @param apportAbsolu Apport absolu de la ration
 * @param besoinAbsolu Besoin absolu calculé selon la référence
 * @return Couleur à utiliser (rouge du thème si non conforme, secondaire sinon)
 */
private fun obtenirCouleurConformite(
        level: Reflevel,
        apportAbsolu: Double,
        besoinAbsolu: Double?
): androidx.compose.ui.graphics.Color {
        besoinAbsolu?.let { besoin ->
                return when (level) {
                        // Pour les minimums : rouge si apport < besoin
                        Reflevel.MIN,
                        Reflevel.OPTIMIN -> {
                                if (apportAbsolu < besoin) VetNutriColors.Error
                                else VetNutriColors.Secondary
                        }
                        // Pour les maximums : rouge si apport > besoin
                        Reflevel.MAX,
                        Reflevel.OPTIMAX -> {
                                if (apportAbsolu > besoin) VetNutriColors.Error
                                else VetNutriColors.Secondary
                        }
                        // Autres niveaux : couleur normale
                        else -> VetNutriColors.Secondary
                }
        }
        // Si pas de calcul possible, couleur normale
        return VetNutriColors.Secondary
}

/**
 * Calcule l'affichage d'un nutriment selon le type d'expression des besoins choisi
 * @param valeurNutritionnelle Valeur nutritionnelle du nutriment
 * @param typeExpressionBesoin Type d'expression des besoins (préférences utilisateur)
 * @param poidsMetabolique Poids métabolique de l'animal
 * @param poidsAnimal Poids vif de l'animal
 * @param besoinEnergetiqueEntretien Besoin énergétique d'entretien (BEE)
 * @return Pair<valeur formatée, unité d'affichage>
 */
private fun calculerAffichageNutriment(
        valeurNutritionnelle: ValeurNutritionnelle,
        typeExpressionBesoin: TypeExpressionBesoin?,
        poidsMetabolique: Double?,
        poidsAnimal: Double?,
        besoinEnergetiqueEntretien: Double?
): Pair<String, String> {

        val valeurAbsolue = valeurNutritionnelle.valeur
        val uniteOriginale = valeurNutritionnelle.unite.displayName

        // Si pas de type d'expression défini, affichage par défaut
        val typeExpression = typeExpressionBesoin ?: TypeExpressionBesoin.DEFAULT


        return when (typeExpression) {
                TypeExpressionBesoin.PAR_KG -> {
                        // Par kg de poids vif
                        poidsAnimal?.let { poids ->
                                if (poids > 0) {
                                        val valeurParKg = valeurAbsolue / poids
                                        Pair("%.2f".format(valeurParKg), "$uniteOriginale/kg")
                                } else {
                                        Pair(String.format("%.2f", valeurAbsolue), uniteOriginale)
                                }
                        }
                                ?: Pair(String.format("%.2f", valeurAbsolue), uniteOriginale)
                }
                TypeExpressionBesoin.PAR_KG_METABOLIQUE -> {
                        // Par kg de poids métabolique (kg^0.75)
                        poidsMetabolique?.let { poidsMetab ->
                                if (poidsMetab > 0) {
                                        val valeurParKgMetab = valeurAbsolue / poidsMetab
                                        Pair(
                                                String.format("%.2f", valeurParKgMetab),
                                                "$uniteOriginale/kg${TextUtils.toSuperscript("0.75")}"
                                        )
                                } else {
                                        Pair(String.format("%.2f", valeurAbsolue), uniteOriginale)
                                }
                        }
                                ?: Pair(String.format("%.2f", valeurAbsolue), uniteOriginale)
                }
                TypeExpressionBesoin.PAR_KCAL -> {
                        // Par 1000 kcal de BEE (Besoin Énergétique d'Entretien)
                        besoinEnergetiqueEntretien?.let { bee ->
                                if (bee > 0) {
                                        val valeurPar1000Kcal = (valeurAbsolue / bee) * 1000
                                        Pair(
                                                String.format("%.2f", valeurPar1000Kcal),
                                                "$uniteOriginale/1000 kcal"
                                        )
                                } else {
                                        Pair(String.format("%.2f", valeurAbsolue), uniteOriginale)
                                }
                        }
                                ?: Pair(String.format("%.2f", valeurAbsolue), uniteOriginale)
                }
                TypeExpressionBesoin.PAR_KJ -> {
                        // Par 1000 kJ de BEE (conversion : 1 kcal = 4.184 kJ)
                        besoinEnergetiqueEntretien?.let { bee ->
                                if (bee > 0) {
                                        val beeEnKj = bee * 4.184 // Conversion kcal vers kJ
                                        val valeurPar1000Kj = (valeurAbsolue / beeEnKj) * 1000
                                        Pair(
                                                String.format("%.2f", valeurPar1000Kj),
                                                "$uniteOriginale/1000 kJ"
                                        )
                                } else {
                                        Pair(String.format("%.2f", valeurAbsolue), uniteOriginale)
                                }
                        }
                                ?: Pair(String.format("%.2f", valeurAbsolue), uniteOriginale)
                }
        }
}

/**
 * Dialog détaillé pour afficher les informations complètes d'un nutriment Affiche l'apport total et
 * la contribution de chaque ingrédient
 */
@Composable
fun NutrientDetailDialog(
        nom: String,
        valeurNutritionnelle: ValeurNutritionnelle,
        ration: Ration,
        poidsMetabolique: Double?,
        referenceUtilisee: ReferenceEv?,
        besoinEnergetiqueEntretien: Double?,
        poidsAnimal: Double?,
        espece: Espece,
        preferencesStorage: PreferencesStorage,
        onDismiss: () -> Unit
) {
        // Récupération des préférences de l'espèce
        val preferencesRepo = remember { PreferencesRepository(preferencesStorage) }
        var typeExpressionBesoin by remember { mutableStateOf(TypeExpressionBesoin.DEFAULT) }

        LaunchedEffect(espece) {
                try {
                        // IMPORTANT: Charger les préférences depuis le stockage avant de les
                        // utiliser
                        preferencesRepo.loadPreferences()
                        val preferences = preferencesRepo.getPreferencesForSpecies(espece)
                        typeExpressionBesoin = preferences.getTypeExpressionBesoinEnum()
                } catch (e: Exception) {
                        typeExpressionBesoin = TypeExpressionBesoin.DEFAULT
                }
        }

        AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Text(
                                        text = "Détails : $nom",
                                        style = MaterialTheme.typography.h6,
                                        fontWeight = FontWeight.Bold,
                                        color = VetNutriColors.Primary,
                                        modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = onDismiss) {
                                        Icon(
                                                imageVector = Icons.Filled.Close,
                                                contentDescription = "Fermer",
                                                tint = VetNutriColors.Primary
                                        )
                                }
                        }
                },
                text = {
                        Column(
                                modifier = Modifier.fillMaxWidth().height(600.dp),
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                        ) {
                                // Section récapitulatif
                                Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        elevation = AppSizes.elevationSmall,
                                        backgroundColor = VetNutriColors.Primary.copy(alpha = 0.1f)
                                ) {
                                        Column(
                                                modifier = Modifier.padding(AppSizes.paddingMedium),
                                                verticalArrangement =
                                                        Arrangement.spacedBy(AppSizes.paddingSmall)
                                        ) {

                                                // Apport selon le type d'expression choisi
                                                // (priorité - en gras)
                                                val (valeurFormatee, uniteAffichage) =
                                                        calculerAffichageNutriment(
                                                                valeurNutritionnelle,
                                                                typeExpressionBesoin,
                                                                poidsMetabolique,
                                                                poidsAnimal,
                                                                besoinEnergetiqueEntretien
                                                        )

                                                Text(
                                                        text =
                                                                "Apport: $valeurFormatee $uniteAffichage",
                                                        style = MaterialTheme.typography.body1,
                                                        fontWeight = FontWeight.Bold,
                                                        color = VetNutriColors.Primary
                                                )

                                                // Apport absolu total avec flèche (comme les
                                                // références)
                                                Text(
                                                        text =
                                                                "→ ${String.format("%.2f", valeurNutritionnelle.valeur)} ${valeurNutritionnelle.unite.displayName}/jour",
                                                        style = MaterialTheme.typography.body2,
                                                        fontWeight = FontWeight.Medium,
                                                        color =
                                                                VetNutriColors.Primary.copy(
                                                                        alpha = 0.8f
                                                                )
                                                )

                                                // Statut de complétude
                                                val isComplete = valeurNutritionnelle.complete
                                                Row(
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        Icon(
                                                                imageVector =
                                                                        if (isComplete)
                                                                                Icons.Filled.Check
                                                                        else Icons.Filled.Warning,
                                                                contentDescription =
                                                                        if (isComplete)
                                                                                "Données complètes"
                                                                        else "Données incomplètes",
                                                                tint =
                                                                        if (isComplete) Color.Green
                                                                        else VetNutriColors.Error,
                                                                modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                                text =
                                                                        if (isComplete)
                                                                                "Données complètes"
                                                                        else "Données incomplètes",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .body2,
                                                                color =
                                                                        if (isComplete) Color.Green
                                                                        else VetNutriColors.Error
                                                        )
                                                }
                                        }
                                }

                                // Section des références nutritionnelles
                                referenceUtilisee?.let { reference ->
                                        val nutrient: Nutrient = valeurNutritionnelle.nutriment
                                        val hasReferenceValues =
                                                listOf(
                                                                Reflevel.MIN,
                                                                Reflevel.MAX,
                                                                Reflevel.OPTIMIN,
                                                                Reflevel.OPTIMAX
                                                        )
                                                        .any { level: Reflevel ->
                                                                reference.contientNutriment(
                                                                        nutrient,
                                                                        level
                                                                )
                                                        }

                                        if (hasReferenceValues) {
                                                Card(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        elevation = AppSizes.elevationSmall,
                                                        backgroundColor =
                                                                VetNutriColors.Secondary.copy(
                                                                        alpha = 0.1f
                                                                )
                                                ) {
                                                        Column(
                                                                modifier =
                                                                        Modifier.padding(
                                                                                AppSizes.paddingMedium
                                                                        ),
                                                                verticalArrangement =
                                                                        Arrangement.spacedBy(
                                                                                AppSizes.paddingSmall
                                                                        )
                                                        ) {
                                                                Text(
                                                                        text =
                                                                                "Références nutritionnelles - ${reference.nom}",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .subtitle1,
                                                                        fontWeight =
                                                                                FontWeight.Bold,
                                                                        color =
                                                                                VetNutriColors
                                                                                        .Secondary
                                                                )

                                                                // Graphique bullet pour visualiser
                                                                // l'apport et les références
                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.height(
                                                                                        AppSizes.paddingSmall
                                                                                )
                                                                )
                                                                // Convertir l'apport vers l'unité
                                                                // des préférences
                                                                val apportConverti =
                                                                        convertirVersUnitePreferences(
                                                                                valeurNutritionnelle
                                                                                        .valeur
                                                                                        .toFloat(),
                                                                                UnitReqEnum
                                                                                        .ABSOLUTE, // L'apport est en valeur absolue (g/jour)
                                                                                typeExpressionBesoin
                                                                                        .unitReqEnum,
                                                                                besoinEnergetiqueEntretien,
                                                                                poidsAnimal,
                                                                                poidsMetabolique
                                                                        )
                                                                                ?: valeurNutritionnelle
                                                                                        .valeur
                                                                                        .toFloat()

                                                                ReferenceBulletGraph(
                                                                        valeurApport =
                                                                                apportConverti,
                                                                        reference = reference,
                                                                        nutriment = nutrient,
                                                                        typeExpressionBesoin =
                                                                                typeExpressionBesoin,
                                                                        poidsAnimal = poidsAnimal,
                                                                        poidsMetabolique =
                                                                                poidsMetabolique,
                                                                        besoinEnergetiqueEntretien =
                                                                                besoinEnergetiqueEntretien
                                                                )

                                                                // Afficher les valeurs de référence
                                                                // disponibles
                                                                val refLevels =
                                                                        listOf(
                                                                                Reflevel.MIN to
                                                                                        "Minimum",
                                                                                Reflevel.OPTIMIN to
                                                                                        "Optimal minimum",
                                                                                Reflevel.OPTIMAX to
                                                                                        "Optimal maximum",
                                                                                Reflevel.MAX to
                                                                                        "Maximum"
                                                                        )

                                                                refLevels.forEach {
                                                                        (level, levelName) ->
                                                                        if (reference
                                                                                        .contientNutriment(
                                                                                                nutrient,
                                                                                                level
                                                                                        )
                                                                        ) {
                                                                                val valeurRef =
                                                                                        reference
                                                                                                .obtenirNutriment(
                                                                                                        nutrient,
                                                                                                        level
                                                                                                )
                                                                                val uniteRef =
                                                                                        UnitReqEnum
                                                                                                .getById(
                                                                                                        reference
                                                                                                                .obtenirUniteNutriment(
                                                                                                                        nutrient,
                                                                                                                        level
                                                                                                                )
                                                                                                )
                                                                                val biblioRef =
                                                                                        reference
                                                                                                .obtenirBiblioNutriment(
                                                                                                        nutrient,
                                                                                                        level
                                                                                                )

                                                                                // Calculer le
                                                                                // besoin absolu
                                                                                // selon l'unité de
                                                                                // référence
                                                                                val besoinAbsolu =
                                                                                        calculerBesoinAbsolu(
                                                                                                valeurRef,
                                                                                                uniteRef,
                                                                                                besoinEnergetiqueEntretien,
                                                                                                poidsAnimal,
                                                                                                poidsMetabolique
                                                                                        )

                                                                                // Déterminer la
                                                                                // couleur selon la
                                                                                // conformité
                                                                                val couleurConformite =
                                                                                        obtenirCouleurConformite(
                                                                                                level,
                                                                                                valeurNutritionnelle
                                                                                                        .valeur,
                                                                                                besoinAbsolu
                                                                                        )

                                                                                Column(
                                                                                        modifier =
                                                                                                Modifier.fillMaxWidth()
                                                                                ) {
                                                                                        Row(
                                                                                                modifier =
                                                                                                        Modifier.fillMaxWidth(),
                                                                                                horizontalArrangement =
                                                                                                        Arrangement
                                                                                                                .SpaceBetween
                                                                                        ) {
                                                                                                Text(
                                                                                                        text =
                                                                                                                "$levelName:",
                                                                                                        style =
                                                                                                                MaterialTheme
                                                                                                                        .typography
                                                                                                                        .body2,
                                                                                                        fontWeight =
                                                                                                                FontWeight
                                                                                                                        .Medium
                                                                                                )
                                                                                                Column(
                                                                                                        horizontalAlignment =
                                                                                                                Alignment
                                                                                                                        .End
                                                                                                ) {
                                                                                                        // Valeur de référence avec son unité
                                                                                                        Text(
                                                                                                                text =
                                                                                                                        "${String.format("%.2f", valeurRef)} ${uniteRef.label}",
                                                                                                                style =
                                                                                                                        MaterialTheme
                                                                                                                                .typography
                                                                                                                                .body2,
                                                                                                                color =
                                                                                                                        couleurConformite
                                                                                                        )

                                                                                                        // Expression selon les préférences (si différente de la référence)
                                                                                                        if (typeExpressionBesoin
                                                                                                                        .unitReqEnum !=
                                                                                                                        uniteRef
                                                                                                        ) {
                                                                                                                // Créer une ValeurNutritionnelle temporaire pour la conversion
                                                                                                                val valeurTemp =
                                                                                                                        ValeurNutritionnelle(
                                                                                                                                valeurNutritionnelle
                                                                                                                                        .nutriment,
                                                                                                                                valeurNutritionnelle
                                                                                                                                        .unite,
                                                                                                                                valeurRef
                                                                                                                                        .toDouble(),
                                                                                                                                "Référence convertie",
                                                                                                                                true
                                                                                                                        )
                                                                                                                val (
                                                                                                                        valeurPreferee,
                                                                                                                        unitePreferee) =
                                                                                                                        calculerAffichageNutriment(
                                                                                                                                valeurTemp,
                                                                                                                                typeExpressionBesoin,
                                                                                                                                poidsMetabolique,
                                                                                                                                poidsAnimal,
                                                                                                                                besoinEnergetiqueEntretien
                                                                                                                        )

                                                                                                                Text(
                                                                                                                        text =
                                                                                                                                "→ $valeurPreferee $unitePreferee",
                                                                                                                        style =
                                                                                                                                MaterialTheme
                                                                                                                                        .typography
                                                                                                                                        .caption,
                                                                                                                        fontWeight =
                                                                                                                                FontWeight
                                                                                                                                        .Bold,
                                                                                                                        color =
                                                                                                                                couleurConformite
                                                                                                                )
                                                                                                        }

                                                                                                        // Besoin absolu calculé
                                                                                                        besoinAbsolu
                                                                                                                ?.let {
                                                                                                                        valeurAbsolue
                                                                                                                        ->
                                                                                                                        Text(
                                                                                                                                text =
                                                                                                                                        "→ ${String.format("%.2f", valeurAbsolue)} ${valeurNutritionnelle.unite.displayName}/jour",
                                                                                                                                style =
                                                                                                                                        MaterialTheme
                                                                                                                                                .typography
                                                                                                                                                .caption,
                                                                                                                                fontWeight =
                                                                                                                                        FontWeight
                                                                                                                                                .Bold,
                                                                                                                                color =
                                                                                                                                        couleurConformite
                                                                                                                        )
                                                                                                                }

                                                                                                        // Référence bibliographique
                                                                                                        if (biblioRef
                                                                                                                        .firstAuthor
                                                                                                                        .isNotEmpty() ||
                                                                                                                        biblioRef
                                                                                                                                .completeRef
                                                                                                                                .isNotEmpty()
                                                                                                        ) {
                                                                                                                Text(
                                                                                                                        text =
                                                                                                                                "Réf: ${biblioRef.firstAuthor} ${biblioRef.completeRef}".take(
                                                                                                                                        30
                                                                                                                                ) +
                                                                                                                                        if (biblioRef
                                                                                                                                                        .firstAuthor
                                                                                                                                                        .length +
                                                                                                                                                        biblioRef
                                                                                                                                                                .completeRef
                                                                                                                                                                .length >
                                                                                                                                                        30
                                                                                                                                        )
                                                                                                                                                "..."
                                                                                                                                        else
                                                                                                                                                "",
                                                                                                                        style =
                                                                                                                                MaterialTheme
                                                                                                                                        .typography
                                                                                                                                        .caption,
                                                                                                                        color =
                                                                                                                                couleurConformite
                                                                                                                                        .copy(
                                                                                                                                                alpha =
                                                                                                                                                        0.7f
                                                                                                                                        )
                                                                                                                )
                                                                                                        }
                                                                                                }
                                                                                        }
                                                                                        Spacer(
                                                                                                modifier =
                                                                                                        Modifier.height(
                                                                                                                4.dp
                                                                                                        )
                                                                                        )
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                }
                                        }
                                }

                                // Section contribution des ingrédients
                                Text(
                                        text =
                                                "Contribution par ingrédient (par ordre décroissant)",
                                        style = MaterialTheme.typography.subtitle1,
                                        fontWeight = FontWeight.Bold,
                                        color = VetNutriColors.Primary
                                )

                                // Calculer et trier les contributions
                                val contributionsTriees =
                                        ration.alimentMutableList
                                                .map { alimentRation ->
                                                        val quantite = alimentRation.quantite
                                                        val nutrient: Nutrient =
                                                                valeurNutritionnelle.nutriment
                                                        val valeurAliment =
                                                                alimentRation
                                                                        .aliment
                                                                        ?.getNutrient(nutrient)
                                                                        ?.toDouble()
                                                        val contributionAbsolue =
                                                                if (valeurAliment != null) {
                                                                        (valeurAliment *
                                                                                quantite.toDouble()) /
                                                                                100.0
                                                                } else {
                                                                        0.0
                                                                }
                                                        val contributionPourcentage =
                                                                if (valeurNutritionnelle.valeur > 0
                                                                ) {
                                                                        (contributionAbsolue /
                                                                                valeurNutritionnelle
                                                                                        .valeur
                                                                                        .toDouble()) *
                                                                                100.0
                                                                } else {
                                                                        0.0
                                                                }
                                                        Triple(
                                                                alimentRation,
                                                                contributionAbsolue,
                                                                contributionPourcentage
                                                        )
                                                }
                                                .sortedByDescending { it.second }

                                // Liste scrollable des contributions
                                LazyColumn(
                                        modifier = Modifier.fillMaxWidth().weight(1f),
                                        verticalArrangement =
                                                Arrangement.spacedBy(AppSizes.paddingSmall)
                                ) {
                                        items(contributionsTriees) {
                                                (
                                                        alimentRation,
                                                        contributionAbsolue,
                                                        contributionPourcentage) ->
                                                val quantite = alimentRation.quantite
                                                val nutrient: Nutrient =
                                                        valeurNutritionnelle.nutriment
                                                val valeurAliment =
                                                        alimentRation.aliment?.getNutrient(nutrient)

                                                Card(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        elevation = AppSizes.elevationSmall,
                                                        backgroundColor =
                                                                MaterialTheme.colors.surface
                                                ) {
                                                        Column(
                                                                modifier =
                                                                        Modifier.padding(
                                                                                AppSizes.paddingMedium
                                                                        )
                                                        ) {
                                                                // En-tête avec nom et icône d'état
                                                                Row(
                                                                        modifier =
                                                                                Modifier.fillMaxWidth(),
                                                                        horizontalArrangement =
                                                                                Arrangement
                                                                                        .SpaceBetween,
                                                                        verticalAlignment =
                                                                                Alignment
                                                                                        .CenterVertically
                                                                ) {
                                                                        Text(
                                                                                text =
                                                                                        alimentRation
                                                                                                .aliment
                                                                                                ?.nom
                                                                                                ?: "Aliment inconnu",
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .subtitle2,
                                                                                fontWeight =
                                                                                        FontWeight
                                                                                                .Bold,
                                                                                modifier =
                                                                                        Modifier.weight(
                                                                                                1f
                                                                                        )
                                                                        )

                                                                        // Icône d'état basée sur la
                                                                        // disponibilité des données
                                                                        if (valeurAliment == null) {
                                                                                Icon(
                                                                                        imageVector =
                                                                                                Icons.Filled
                                                                                                        .Warning,
                                                                                        contentDescription =
                                                                                                "Information manquante",
                                                                                        tint =
                                                                                                VetNutriColors
                                                                                                        .Error,
                                                                                        modifier =
                                                                                                Modifier.size(
                                                                                                        16.dp
                                                                                                )
                                                                                )
                                                                        }
                                                                }

                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.height(
                                                                                        AppSizes.paddingSmall
                                                                                )
                                                                )

                                                                // Informations sur 2 colonnes
                                                                Row(
                                                                        modifier =
                                                                                Modifier.fillMaxWidth(),
                                                                        horizontalArrangement =
                                                                                Arrangement
                                                                                        .spacedBy(
                                                                                                AppSizes.paddingMedium
                                                                                        )
                                                                ) {
                                                                        // Colonne gauche
                                                                        Column(
                                                                                modifier =
                                                                                        Modifier.weight(
                                                                                                1f
                                                                                        )
                                                                        ) {
                                                                                // Quantité utilisée
                                                                                Text(
                                                                                        text =
                                                                                                "Quantité: ${String.format("%.1f", quantite)}g",
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .body2,
                                                                                        fontWeight =
                                                                                                FontWeight
                                                                                                        .Medium,
                                                                                        color =
                                                                                                VetNutriColors
                                                                                                        .Primary
                                                                                )

                                                                                // Valeur
                                                                                // nutritionnelle
                                                                                // pour 100g
                                                                                Text(
                                                                                        text =
                                                                                                if (valeurAliment !=
                                                                                                                null
                                                                                                ) {
                                                                                                        "Valeur (100g): ${String.format("%.2f", valeurAliment)} ${valeurNutritionnelle.unite.displayName}"
                                                                                                } else {
                                                                                                        "Valeur (100g): NA"
                                                                                                },
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .body2,
                                                                                        fontWeight =
                                                                                                FontWeight
                                                                                                        .Medium,
                                                                                        color =
                                                                                                if (valeurAliment !=
                                                                                                                null
                                                                                                )
                                                                                                        VetNutriColors
                                                                                                                .Primary
                                                                                                else
                                                                                                        VetNutriColors
                                                                                                                .Error
                                                                                )
                                                                        }

                                                                        // Colonne droite
                                                                        Column(
                                                                                modifier =
                                                                                        Modifier.weight(
                                                                                                1f
                                                                                        )
                                                                        ) {
                                                                                // Contribution
                                                                                // absolue
                                                                                Text(
                                                                                        text =
                                                                                                if (valeurAliment !=
                                                                                                                null
                                                                                                ) {
                                                                                                        "Contribution: ${String.format("%.2f", contributionAbsolue)} ${valeurNutritionnelle.unite.displayName}"
                                                                                                } else {
                                                                                                        "Contribution: NA"
                                                                                                },
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .body2,
                                                                                        fontWeight =
                                                                                                FontWeight
                                                                                                        .Medium,
                                                                                        color =
                                                                                                if (valeurAliment !=
                                                                                                                null
                                                                                                )
                                                                                                        VetNutriColors
                                                                                                                .Secondary
                                                                                                else
                                                                                                        VetNutriColors
                                                                                                                .Error
                                                                                )

                                                                                // Pourcentage de
                                                                                // contribution
                                                                                Text(
                                                                                        text =
                                                                                                if (valeurAliment !=
                                                                                                                null
                                                                                                ) {
                                                                                                        "Part: ${String.format("%.1f", contributionPourcentage)}%"
                                                                                                } else {
                                                                                                        "Part: NA"
                                                                                                },
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .body2,
                                                                                        color =
                                                                                                if (valeurAliment !=
                                                                                                                null
                                                                                                )
                                                                                                        VetNutriColors
                                                                                                                .Secondary
                                                                                                else
                                                                                                        VetNutriColors
                                                                                                                .Error
                                                                                )
                                                                        }
                                                                }
                                                        }
                                                }
                                        }
                                }
                        }
                },
                confirmButton = {},
                dismissButton = {}
        )
}

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
private fun ReferenceBulletGraph(
        valeurApport: Float,
        reference: ReferenceEv,
        nutriment: Nutrient,
        typeExpressionBesoin: TypeExpressionBesoin,
        poidsAnimal: Double?,
        poidsMetabolique: Double?,
        besoinEnergetiqueEntretien: Double?
) {
        // Récupération des valeurs de référence avec leurs unités
        val minRef = reference.obtenirNutriment(nutriment, Reflevel.MIN)
        val optiminRef = reference.obtenirNutriment(nutriment, Reflevel.OPTIMIN)
        val optimaxRef = reference.obtenirNutriment(nutriment, Reflevel.OPTIMAX)
        val maxRef = reference.obtenirNutriment(nutriment, Reflevel.MAX)

        // Récupération des unités de référence
        val minUnit = reference.obtenirUniteNutriment(nutriment, Reflevel.MIN)
        val optiminUnit = reference.obtenirUniteNutriment(nutriment, Reflevel.OPTIMIN)
        val optimaxUnit = reference.obtenirUniteNutriment(nutriment, Reflevel.OPTIMAX)
        val maxUnit = reference.obtenirUniteNutriment(nutriment, Reflevel.MAX)

        // Conversion des valeurs de référence dans l'unité des préférences
        val minRefConverti =
                if (minRef > 0f) {
                        convertirVersUnitePreferences(
                                minRef,
                                UnitReqEnum.getById(minUnit),
                                typeExpressionBesoin.unitReqEnum,
                                besoinEnergetiqueEntretien,
                                poidsAnimal,
                                poidsMetabolique
                        )
                                ?: minRef
                } else null

        val optiminRefConverti =
                if (optiminRef > 0f) {
                        convertirVersUnitePreferences(
                                optiminRef,
                                UnitReqEnum.getById(optiminUnit),
                                typeExpressionBesoin.unitReqEnum,
                                besoinEnergetiqueEntretien,
                                poidsAnimal,
                                poidsMetabolique
                        )
                                ?: optiminRef
                } else null

        val optimaxRefConverti =
                if (optimaxRef > 0f) {
                        convertirVersUnitePreferences(
                                optimaxRef,
                                UnitReqEnum.getById(optimaxUnit),
                                typeExpressionBesoin.unitReqEnum,
                                besoinEnergetiqueEntretien,
                                poidsAnimal,
                                poidsMetabolique
                        )
                                ?: optimaxRef
                } else null

        val maxRefConverti =
                if (maxRef > 0f) {
                        convertirVersUnitePreferences(
                                maxRef,
                                UnitReqEnum.getById(maxUnit),
                                typeExpressionBesoin.unitReqEnum,
                                besoinEnergetiqueEntretien,
                                poidsAnimal,
                                poidsMetabolique
                        )
                                ?: maxRef
                } else null

        val valeurs =
                listOfNotNull(
                        valeurApport,
                        minRefConverti,
                        optiminRefConverti,
                        optimaxRefConverti,
                        maxRefConverti
                )
        if (valeurs.isEmpty()) return // Rien à tracer

        val maxAxis = (valeurs.maxOrNull() ?: 0f) * 1.1f

        Column(modifier = Modifier.fillMaxWidth()) {
                // Graphique bullet plus fin
                BulletGraphs(
                        modifier = Modifier.fillMaxWidth().height(60.dp) // Hauteur fixe plus petite
                ) {
                        // Pas d'étiquette à gauche
                        labelWidth = FixedFraction(0f)

                        bullet(FloatLinearAxisModel(0f..maxAxis)) {
                                // Axe X avec labels
                                axis {
                                        labels {
                                                val tick = it
                                                val label =
                                                        if (tick % 1f == 0f) {
                                                                tick.toInt().toString()
                                                        } else {
                                                                String.format("%.1f", tick)
                                                        }
                                                AxisText(label)
                                        }
                                }

                                // Barre représentant l'apport
                                featuredMeasureBar(valeurApport) {
                                        HorizontalBarIndicator(
                                                SolidColor(Color.Gray),
                                                fraction = 0.33f
                                        )
                                }

                                // Construction dynamique des intervalles colorés
                                val bornes =
                                        buildList {
                                                        add(0f)
                                                        minRefConverti?.let { add(it) }
                                                        optiminRefConverti?.let { add(it) }
                                                        optimaxRefConverti?.let { add(it) }
                                                        maxRefConverti?.let { add(it) }
                                                        add(maxAxis)
                                                }
                                                .distinct()
                                                .sorted()

                                ranges(0f) {
                                        for (i in 0 until bornes.size - 1) {
                                                val start = bornes[i]
                                                val end = bornes[i + 1]
                                                if (end <= start) continue

                                                // Détection du type d'intervalle
                                                val color =
                                                        when {
                                                                // Rouge : 0 à MIN
                                                                minRefConverti != null &&
                                                                        start == 0f &&
                                                                        end == minRefConverti ->
                                                                        VetNutriColors.Error
                                                                // Rouge : MAX à maxAxis
                                                                maxRefConverti != null &&
                                                                        start == maxRefConverti &&
                                                                        end == maxAxis ->
                                                                        VetNutriColors.Error
                                                                // Bleu : MIN à OPTIMIN
                                                                minRefConverti != null &&
                                                                        optiminRefConverti !=
                                                                                null &&
                                                                        start == minRefConverti &&
                                                                        end == optiminRefConverti ->
                                                                        Color(0xFF2196F3)
                                                                // Bleu : OPTIMAX à MAX
                                                                optimaxRefConverti != null &&
                                                                        maxRefConverti != null &&
                                                                        start ==
                                                                                optimaxRefConverti &&
                                                                        end == maxRefConverti ->
                                                                        Color(0xFF2196F3)
                                                                // Bleu : OPTIMAX à maxAxis (si pas
                                                                // de MAX)
                                                                optimaxRefConverti != null &&
                                                                        maxRefConverti == null &&
                                                                        start ==
                                                                                optimaxRefConverti &&
                                                                        end == maxAxis ->
                                                                        Color(0xFF2196F3)
                                                                // Bleu : MIN à OPTIMIN (si pas de
                                                                // MIN)
                                                                minRefConverti == null &&
                                                                        optiminRefConverti !=
                                                                                null &&
                                                                        start == 0f &&
                                                                        end == optiminRefConverti ->
                                                                        Color(0xFF2196F3)
                                                                // Vert : tout le reste
                                                                else -> Color(0xFF4CAF50)
                                                        }
                                                range(end) {
                                                        HorizontalBarIndicator(SolidColor(color))
                                                }
                                        }
                                }

                                // Lignes de référence
                                minRefConverti?.let { min -> comparativeMeasure(min) }
                                optiminRefConverti?.let { optimin -> comparativeMeasure(optimin) }
                                optimaxRefConverti?.let { optimax -> comparativeMeasure(optimax) }
                                maxRefConverti?.let { max -> comparativeMeasure(max) }
                        }
                }

                // Légende explicative
                Spacer(modifier = Modifier.height(8.dp))
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                        // Légende pour les éléments du graphique
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                                // Barre d'apport
                                Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                        Box(
                                                modifier =
                                                        Modifier.size(12.dp, 4.dp)
                                                                .background(
                                                                        color = Color.Gray,
                                                                        shape =
                                                                                RoundedCornerShape(
                                                                                        2.dp
                                                                                )
                                                                )
                                        )
                                        Text(
                                                text =
                                                        "Apport: ${String.format("%.1f", valeurApport)}",
                                                style = MaterialTheme.typography.caption,
                                                color = Color.Gray
                                        )
                                }

                                // Zones de référence
                                Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                        // Zone rouge (dangereuse)
                                        Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                                Box(
                                                        modifier =
                                                                Modifier.size(12.dp, 8.dp)
                                                                        .background(
                                                                                color =
                                                                                        VetNutriColors
                                                                                                .Error,
                                                                                shape =
                                                                                        RoundedCornerShape(
                                                                                                2.dp
                                                                                        )
                                                                        )
                                                )
                                                Text(
                                                        text = "Dangereux",
                                                        style = MaterialTheme.typography.caption,
                                                        color = VetNutriColors.Error
                                                )
                                        }

                                        // Zone bleue (acceptable)
                                        Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                                Box(
                                                        modifier =
                                                                Modifier.size(12.dp, 8.dp)
                                                                        .background(
                                                                                color =
                                                                                        Color(
                                                                                                0xFF2196F3
                                                                                        ),
                                                                                shape =
                                                                                        RoundedCornerShape(
                                                                                                2.dp
                                                                                        )
                                                                        )
                                                )
                                                Text(
                                                        text = "Acceptable",
                                                        style = MaterialTheme.typography.caption,
                                                        color = Color(0xFF2196F3)
                                                )
                                        }

                                        // Zone verte (optimal)
                                        Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                                Box(
                                                        modifier =
                                                                Modifier.size(12.dp, 8.dp)
                                                                        .background(
                                                                                color =
                                                                                        Color(
                                                                                                0xFF4CAF50
                                                                                        ),
                                                                                shape =
                                                                                        RoundedCornerShape(
                                                                                                2.dp
                                                                                        )
                                                                        )
                                                )
                                                Text(
                                                        text = "Optimal",
                                                        style = MaterialTheme.typography.caption,
                                                        color = Color(0xFF4CAF50)
                                                )
                                        }
                                }
                        }
                }
        }
}

/**
 * Convertit une valeur d'une unité vers l'unité des préférences utilisateur
 *
 * @param valeurRef Valeur de référence du nutriment
 * @param uniteRef Unité de la référence (PERKG, PERKCAL, PERMS, etc.)
 * @param unitePreferences Unité des préférences utilisateur
 * @param besoinEnergetiqueEntretien Besoin énergétique d'entretien en kcal/jour
 * @param poidsAnimal Poids de l'animal en kg
 * @param poidsMetabolique Poids métabolique en kg^0.75
 * @return Valeur convertie dans l'unité des préférences ou null si impossible à calculer
 */
private fun convertirVersUnitePreferences(
        valeurRef: Float,
        uniteRef: UnitReqEnum,
        unitePreferences: UnitReqEnum,
        besoinEnergetiqueEntretien: Double?,
        poidsAnimal: Double?,
        poidsMetabolique: Double?
): Float? {
        // Si les unités sont identiques, pas de conversion nécessaire
        if (uniteRef == unitePreferences) {
                return valeurRef
        }

        // Convertir d'abord vers une valeur absolue (g/jour)
        val valeurAbsolue =
                calculerBesoinAbsolu(
                        valeurRef,
                        uniteRef,
                        besoinEnergetiqueEntretien,
                        poidsAnimal,
                        poidsMetabolique
                )
                        ?: return null

        // Puis convertir de la valeur absolue vers l'unité des préférences
        return when (unitePreferences) {
                // Vers PERKG (par kg de poids vif)
                UnitReqEnum.PERKG -> {
                        poidsAnimal?.let { poids ->
                                if (poids > 0) (valeurAbsolue / poids).toFloat() else null
                        }
                }

                // Vers PERMS (par kg de poids métabolique)
                UnitReqEnum.PERMS -> {
                        poidsMetabolique?.let { poidsMetab ->
                                if (poidsMetab > 0) (valeurAbsolue / poidsMetab).toFloat() else null
                        }
                }

                // Vers PERKCAL (par 1000 kcal)
                UnitReqEnum.PERKCAL -> {
                        besoinEnergetiqueEntretien?.let { bee ->
                                if (bee > 0) ((valeurAbsolue * 1000.0) / bee).toFloat() else null
                        }
                }

                // Vers PERKJ (par 1000 kJ)
                UnitReqEnum.PERKJ -> {
                        besoinEnergetiqueEntretien?.let { bee ->
                                if (bee > 0) {
                                        // Convertir kcal en kJ : 1 kcal = 4.184 kJ
                                        val beeEnKj = bee * 4.184
                                        ((valeurAbsolue * 1000.0) / beeEnKj).toFloat()
                                } else null
                        }
                }

                // Vers ABSOLUTE (valeur absolue)
                UnitReqEnum.ABSOLUTE -> {
                        valeurAbsolue.toFloat()
                }

                // Vers RATIO - pas de conversion possible
                UnitReqEnum.RATIO -> null

                // Autres unités non supportées
                else -> null
        }
}

@Composable
private fun AxisText(text: String) {
        Text(text, style = MaterialTheme.typography.caption, textAlign = TextAlign.Center)
}

/**
 * Calcule le besoin absolu d'un nutriment selon son unité de référence
 *
 * @param valeurRef Valeur de référence du nutriment
 * @param uniteRef Unité de la référence (PERKG, PERKCAL, PERMS, etc.)
 * @param besoinEnergetiqueEntretien Besoin énergétique d'entretien en kcal/jour
 * @param poidsAnimal Poids de l'animal en kg
 * @param poidsMetabolique Poids métabolique en kg^0.75
 * @return Besoin absolu calculé ou null si impossible à calculer
 */
private fun calculerBesoinAbsolu(
        valeurRef: Float,
        uniteRef: UnitReqEnum,
        besoinEnergetiqueEntretien: Double?,
        poidsAnimal: Double?,
        poidsMetabolique: Double?
): Double? {
        return when (uniteRef) {
                // Basé sur l'énergie (par 1000 kcal)
                UnitReqEnum.PERKCAL -> {
                        besoinEnergetiqueEntretien?.let { bee -> (valeurRef * bee) / 1000.0 }
                }

                // Basé sur l'énergie (par 1000 kJ) - conversion en kcal puis calcul
                UnitReqEnum.PERKJ -> {
                        besoinEnergetiqueEntretien?.let { bee ->
                                // Convertir kJ en kcal : 1 kcal = 4.184 kJ
                                val beeEnKj = bee * 4.184
                                (valeurRef * beeEnKj) / 1000.0
                        }
                }

                // Basé sur le poids corporel (par kg de poids vif)
                UnitReqEnum.PERKG -> {
                        poidsAnimal?.let { poids -> valeurRef * poids }
                }

                // Basé sur le poids métabolique (par kg^0.75)
                UnitReqEnum.PERMS -> {
                        poidsMetabolique?.let { poidsMetab -> valeurRef * poidsMetab }
                }

                // Valeur absolue (déjà en unité finale)
                UnitReqEnum.ABSOLUTE -> {
                        valeurRef.toDouble()
                }

                // Ratio - pas de calcul absolu possible
                UnitReqEnum.RATIO -> null

                // Autres unités non supportées pour le calcul absolu
                else -> null
        }
}
