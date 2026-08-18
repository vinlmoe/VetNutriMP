package fr.vetbrain.vetnutri_mp.View.AnalNut

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
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
import fr.vetbrain.vetnutri_mp.Data.calculerAffichageNutriment
import fr.vetbrain.vetnutri_mp.Data.calculerBesoinAbsolu
import fr.vetbrain.vetnutri_mp.Data.convertirVersUnitePreferences
import fr.vetbrain.vetnutri_mp.Data.hasDefaultEnergyReferenceLevels
import fr.vetbrain.vetnutri_mp.Data.defaultEnergyReferenceLevel
import fr.vetbrain.vetnutri_mp.Data.calculerContributionsIngredients
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Utils.GraphFormattingUtils
import fr.vetbrain.vetnutri_mp.Utils.NumberUtils
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.Reflevel
import fr.vetbrain.vetnutri_mp.Enumer.TypeExpressionBesoin
import fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Repository.PreferencesRepository
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.PreferencesStorage
import fr.vetbrain.vetnutri_mp.Utils.TextUtils
import io.github.koalaplot.core.bar.BulletGraphs
import io.github.koalaplot.core.bar.FixedFraction
import io.github.koalaplot.core.bar.HorizontalBarIndicator
import kotlin.math.abs
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.xygraph.FloatLinearAxisModel

/**
 * Formate les labels de l'axe des abscisses des bullet graphs avec formatage intelligent
 */
private fun formaterLabelAxeBullet(valeur: Double, valeurMaximale: Double): String {
    return GraphFormattingUtils.formatSmartDecimal(valeur)
}

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
                }
        }
        // Si pas de calcul possible, couleur normale
        return VetNutriColors.Secondary
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
        besoinEnergetiqueCible: Double? = besoinEnergetiqueEntretien,
        poidsAnimal: Double?,
        espece: Espece,
        preferencesStorage: PreferencesStorage,
        equationRepository: EquationRepository? = null,
        referencesMaladies: List<ReferenceEv> = emptyList(),
        onDismiss: () -> Unit
) {
        // Récupération des préférences de l'espèce (mode d'affichage uniquement)
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
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f),
                onDismissRequest = onDismiss,
                title = { DialogTitre(titre = translate(LocalizationKeys.AnalNut.DETAILS_TITLE, nom), onDismiss = onDismiss) },
                text = {
                        Column(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {

                                // Titre et apport non scrollables
                                RecapitulatifCard(
                                        valeurNutritionnelle = valeurNutritionnelle,
                                        typeExpressionBesoin = typeExpressionBesoin,
                                        poidsMetabolique = poidsMetabolique,
                                        poidsAnimal = poidsAnimal,
                                        besoinEnergetiqueEntretien = besoinEnergetiqueEntretien,
                                        referenceUtilisee = referenceUtilisee
                                )
                                Text(
                                        text = translate(LocalizationKeys.AnalNut.NUTRITIONAL_REFERENCES),
                                        style = MaterialTheme.typography.subtitle1,
                                        fontWeight = FontWeight.Bold,
                                        color = VetNutriColors.Primary
                                )
                                Divider()

                                // Contenu scrollable
                                LazyColumn(
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .weight(1f)
                                                        .heightIn(max = 500.dp),
                                        verticalArrangement =
                                                Arrangement.spacedBy(AppSizes.paddingSmall)
                                ) {
                                        // Section des références nutritionnelles

                                        referenceUtilisee?.let { ref ->
                                                val nutrient: Nutrient =
                                                        valeurNutritionnelle.nutriment
                                                val hasReferenceValues =
                                                        listOf(
                                                                        Reflevel.MIN,
                                                                        Reflevel.MAX,
                                                                        Reflevel.OPTIMIN,
                                                                        Reflevel.OPTIMAX
                                                                )
                                                                .any { level: Reflevel ->
                                                                        ref.contientNutriment(
                                                                                nutrient,
                                                                                level
                                                                        )
                                                                } ||
                                                                hasDefaultEnergyReferenceLevels(
                                                                        nutrient,
                                                                        besoinEnergetiqueEntretien
                                                                )

                                                if (hasReferenceValues) {
                                                        item {
                                                                ReferenceCard(
                                                                        titre =
                                                                                "${translate(LocalizationKeys.AnalNut.NUTRITIONAL_REFERENCES)} - ${ref.nom}",
                                                                        reference = ref,
                                                                        valeurNutritionnelle =
                                                                                valeurNutritionnelle,
                                                                        typeExpressionBesoin =
                                                                                typeExpressionBesoin,
                                                                        poidsAnimal = poidsAnimal,
                                                                        poidsMetabolique =
                                                                                poidsMetabolique,
                                                                        besoinEnergetiqueEntretien =
                                                                                besoinEnergetiqueEntretien,
                                                                        besoinEnergetiqueCible =
                                                                                besoinEnergetiqueCible,
                                                                        referencesMaladies =
                                                                                referencesMaladies,
                                                                        ration = ration,
                                                                        equationRepository =
                                                                                equationRepository
                                                                )
                                                        }
                                                }
                                        }

                                        // Section des références maladies (après les références
                                        // générales)
                                        if (referencesMaladies.isNotEmpty()) {
                                                referencesMaladies.forEach { refMaladie ->
                                                        val nutrient: Nutrient =
                                                                valeurNutritionnelle.nutriment
                                                        val hasReferenceValuesMaladie =
                                                                listOf(
                                                                                Reflevel.MIN,
                                                                                Reflevel.MAX,
                                                                                Reflevel.OPTIMIN,
                                                                                Reflevel.OPTIMAX
                                                                        )
                                                                        .any { level: Reflevel ->
                                                                                refMaladie
                                                                                        .contientNutriment(
                                                                                                nutrient,
                                                                                                level
                                                                                        )
                                                                        }
                                                        if (hasReferenceValuesMaladie) {
                                                                item {
                                                                        ReferenceCard(
                                                                                titre =
                                                                                        "${translate(LocalizationKeys.AnalNut.DISEASE_REFERENCES)} - ${refMaladie.nom}",
                                                                                reference =
                                                                                        refMaladie,
                                                                                valeurNutritionnelle =
                                                                                        valeurNutritionnelle,
                                                                                typeExpressionBesoin =
                                                                                        typeExpressionBesoin,
                                                                                poidsAnimal =
                                                                                        poidsAnimal,
                                                                                poidsMetabolique =
                                                                                        poidsMetabolique,
                                                                                besoinEnergetiqueEntretien =
                                                                                        besoinEnergetiqueEntretien,
                                                                                besoinEnergetiqueCible =
                                                                                        besoinEnergetiqueCible,
                                                                                ration = ration,
                                                                                equationRepository =
                                                                                        equationRepository
                                                                        )
                                                                }
                                                        }
                                                }
                                        }

                                        // Section contribution des ingrédients
                                        item {
                                                ContributionsList(
                                                        ration = ration,
                                                        valeurNutritionnelle = valeurNutritionnelle,
                                                        referenceUtilisee = referenceUtilisee,
                                                        espece = espece,
                                                        preferencesRepo = preferencesRepo,
                                                        equationRepository = equationRepository
                                                )
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
fun ReferenceBulletGraph(
        valeurApport: Double,
        reference: ReferenceEv,
        nutriment: Nutrient,
        typeExpressionBesoin: TypeExpressionBesoin,
        poidsAnimal: Double?,
        poidsMetabolique: Double?,
        besoinEnergetiqueEntretien: Double?,
        besoinEnergetiqueCible: Double? = besoinEnergetiqueEntretien,
        referencesMaladies: List<ReferenceEv> = emptyList(),
        onClick: (() -> Unit)? = null,
        ration: Ration? = null,
        equationRepository: EquationRepository? = null
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
        val isAnalysisNoUnit = estNutrimentAnalysisRatio(nutriment)
        val minRefConverti =
                if (minRef > 0.0) {
                        if (isAnalysisNoUnit) minRef
                        else
                                convertirVersUnitePreferences(
                                        minRef,
                                        UnitReqEnum.getById(minUnit),
                                        typeExpressionBesoin.unitReqEnum,
                                        besoinEnergetiqueEntretien,
                                        poidsAnimal,
                                        poidsMetabolique
                                )
                                        ?: minRef
                } else
                        defaultEnergyReferenceLevel(
                                nutriment = nutriment,
                                level = Reflevel.MIN,
                                typeExpressionBesoin = typeExpressionBesoin,
                                besoinEnergetiqueEntretien = besoinEnergetiqueCible,
                                poidsAnimal = poidsAnimal,
                                poidsMetabolique = poidsMetabolique
                        )

        val optiminRefConverti =
                if (optiminRef > 0.0) {
                        if (isAnalysisNoUnit) optiminRef
                        else
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
                if (optimaxRef > 0.0) {
                        if (isAnalysisNoUnit) optimaxRef
                        else
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
                if (maxRef > 0.0) {
                        if (isAnalysisNoUnit) maxRef
                        else
                                convertirVersUnitePreferences(
                                        maxRef,
                                        UnitReqEnum.getById(maxUnit),
                                        typeExpressionBesoin.unitReqEnum,
                                        besoinEnergetiqueEntretien,
                                        poidsAnimal,
                                        poidsMetabolique
                                )
                                        ?: maxRef
                } else
                        defaultEnergyReferenceLevel(
                                nutriment = nutriment,
                                level = Reflevel.MAX,
                                typeExpressionBesoin = typeExpressionBesoin,
                                besoinEnergetiqueEntretien = besoinEnergetiqueCible,
                                poidsAnimal = poidsAnimal,
                                poidsMetabolique = poidsMetabolique
                        )

        val valeurs =
                listOfNotNull(
                        valeurApport,
                        minRefConverti,
                        optiminRefConverti,
                        optimaxRefConverti,
                        maxRefConverti
                )
        if (valeurs.isEmpty()) return // Rien à tracer

        val maxAxis = (valeurs.maxOrNull() ?: 0.0) * 1.1
        // Vérifier que maxAxis est strictement supérieur à 0 pour éviter une plage invalide (0.0..0.0)
        if (maxAxis <= 0.0) return // Ne pas afficher le graphique si toutes les valeurs sont 0

        var contributionSegments by remember(ration, nutriment, reference, equationRepository, valeurApport) {
                mutableStateOf<List<ContributionSegment>>(emptyList())
        }

        LaunchedEffect(ration, nutriment, reference, equationRepository, valeurApport) {
                contributionSegments =
                        if (ration == null || valeurApport <= 0.0) {
                                emptyList()
                        } else {
                                calculerContributionsIngredients(
                                        ration, nutriment, reference, equationRepository
                                ).map { (index, contribution) ->
                                        ContributionSegment(
                                                index = index,
                                                contribution = contribution,
                                                color =
                                                        fr.vetbrain.vetnutri_mp.Theme
                                                                .VetNutriColors
                                                                .getFeedColor(index)
                                        )
                                }
                        }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
                // Indicateur visuel si le bullet graph est cliquable
                if (onClick != null) {
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Icon(
                                        imageVector = Icons.Filled.Info,
                                        contentDescription = translate(LocalizationKeys.AnalNut.CLICK_DETAILS),
                                        tint = VetNutriColors.Primary.copy(alpha = 0.7f),
                                        modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                        text = translate(LocalizationKeys.AnalNut.CLICK_DETAILS),
                                        style = MaterialTheme.typography.caption,
                                        color = VetNutriColors.Primary.copy(alpha = 0.7f)
                                )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                }

                // Graphique bullet plus fin
                BulletGraphs(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .height(60.dp)
                                        .then(
                                                if (onClick != null) {
                                                        Modifier.clickable { onClick() }
                                                } else {
                                                        Modifier
                                                }
                                        ) // Hauteur fixe plus petite
                ) {
                        // Pas d'étiquette à gauche
                        labelWidth = FixedFraction(0f)

                        bullet(FloatLinearAxisModel(0f..maxAxis.toFloat())) {
                                // Axe X avec labels
                                axis {
                                        labels {
                                                val tick = it
                                                val label = formaterLabelAxeBullet(tick.toDouble(), maxAxis)
                                                AxisText(label)
                                        }
                                }

                                // Barre représentant l'apport (segmentée par aliment)
                                featuredMeasureBar(valeurApport.toFloat()) {
                                        val totalContrib = contributionSegments.sumOf { it.contribution }

                                        if (contributionSegments.isEmpty() || totalContrib <= 0.0) {
                                                HorizontalBarIndicator(
                                                        SolidColor(Color.Gray),
                                                        fraction = 0.33f
                                                )
                                        } else {
                                                Row(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .fillMaxHeight(0.33f),
                                                        verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                        contributionSegments.forEach { segment ->
                                                                Box(
                                                                        modifier =
                                                                                Modifier.weight(
                                                                                                segment.contribution
                                                                                                        .toFloat()
                                                                                        )
                                                                                        .fillMaxHeight()
                                                                                        .background(segment.color)
                                                                )
                                                        }
                                                }
                                        }
                                }

                                // Construction dynamique des intervalles colorés
                                val bornes =
                                        buildList {
                                                        add(0.0)
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
                                                                        start == 0.0 &&
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
                                                                        start == 0.0 &&
                                                                        end == optiminRefConverti ->
                                                                        Color(0xFF2196F3)
                                                                // Vert : tout le reste
                                                                else -> Color(0xFF4CAF50)
                                                        }
                                                range(end.toFloat()) {
                                                        HorizontalBarIndicator(SolidColor(color))
                                                }
                                        }
                                }

                                // Lignes de référence (générales)
                                minRefConverti?.let { min -> comparativeMeasure(min.toFloat()) }
                                optiminRefConverti?.let { optimin ->
                                        comparativeMeasure(optimin.toFloat())
                                }
                                optimaxRefConverti?.let { optimax ->
                                        comparativeMeasure(optimax.toFloat())
                                }
                                maxRefConverti?.let { max -> comparativeMeasure(max.toFloat()) }

                                // Lignes verticales pour les valeurs des références maladies
                                referencesMaladies.forEach { refMaladie ->
                                        val minM =
                                                refMaladie.obtenirNutriment(nutriment, Reflevel.MIN)
                                        val minMU =
                                                refMaladie.obtenirUniteNutriment(
                                                        nutriment,
                                                        Reflevel.MIN
                                                )
                                        val minVal =
                                                if (minM > 0.0)
                                                        convertirVersUnitePreferences(
                                                                minM,
                                                                UnitReqEnum.getById(minMU),
                                                                typeExpressionBesoin.unitReqEnum,
                                                                besoinEnergetiqueEntretien,
                                                                poidsAnimal,
                                                                poidsMetabolique
                                                        )
                                                                ?: minM
                                                else null
                                        val optiMinM =
                                                refMaladie.obtenirNutriment(
                                                        nutriment,
                                                        Reflevel.OPTIMIN
                                                )
                                        val optiMinMU =
                                                refMaladie.obtenirUniteNutriment(
                                                        nutriment,
                                                        Reflevel.OPTIMIN
                                                )
                                        val optiMinVal =
                                                if (optiMinM > 0.0)
                                                        convertirVersUnitePreferences(
                                                                optiMinM,
                                                                UnitReqEnum.getById(optiMinMU),
                                                                typeExpressionBesoin.unitReqEnum,
                                                                besoinEnergetiqueEntretien,
                                                                poidsAnimal,
                                                                poidsMetabolique
                                                        )
                                                                ?: optiMinM
                                                else null
                                        val optiMaxM =
                                                refMaladie.obtenirNutriment(
                                                        nutriment,
                                                        Reflevel.OPTIMAX
                                                )
                                        val optiMaxMU =
                                                refMaladie.obtenirUniteNutriment(
                                                        nutriment,
                                                        Reflevel.OPTIMAX
                                                )
                                        val optiMaxVal =
                                                if (optiMaxM > 0.0)
                                                        convertirVersUnitePreferences(
                                                                optiMaxM,
                                                                UnitReqEnum.getById(optiMaxMU),
                                                                typeExpressionBesoin.unitReqEnum,
                                                                besoinEnergetiqueEntretien,
                                                                poidsAnimal,
                                                                poidsMetabolique
                                                        )
                                                                ?: optiMaxM
                                                else null
                                        val maxM =
                                                refMaladie.obtenirNutriment(nutriment, Reflevel.MAX)
                                        val maxMU =
                                                refMaladie.obtenirUniteNutriment(
                                                        nutriment,
                                                        Reflevel.MAX
                                                )
                                        val maxVal =
                                                if (maxM > 0.0)
                                                        convertirVersUnitePreferences(
                                                                maxM,
                                                                UnitReqEnum.getById(maxMU),
                                                                typeExpressionBesoin.unitReqEnum,
                                                                besoinEnergetiqueEntretien,
                                                                poidsAnimal,
                                                                poidsMetabolique
                                                        )
                                                                ?: maxM
                                                else null

                                        listOfNotNull(minVal, optiMinVal, optiMaxVal, maxVal)
                                                .forEach { v ->
                                                        // Utiliser des marqueurs comparatifs
                                                        // (lignes verticales)
                                                        // pour indiquer les valeurs de maladies
                                                        // (couleur par défaut)
                                                        comparativeMeasure(v.toFloat())
                                                }
                                }
                        }
                }

                // Légende retirée sous les bullet graphs
        }
}

/**
 * Convertit une valeur de référence d'une unité vers une autre
 * @param valeurRef Valeur de référence à convertir
 * @param uniteRef Unité de la valeur de référence
 * @param unitePreferences Unité des préférences utilisateur
 * @param besoinEnergetiqueEntretien Besoin énergétique d'entretien en kcal/jour
 * @param poidsAnimal Poids de l'animal en kg
 * @param poidsMetabolique Poids métabolique en kg^0.75
 * @return Valeur convertie dans l'unité des préférences ou null si impossible à calculer
 */
// convertirVersUnitePreferences/hasDefaultEnergyReferenceLevels/defaultEnergyReferenceLevel
// vivent maintenant dans Data/NutrientDisplayCalculations.kt (partagés avec l'export PDF).

@Composable
private fun AxisText(text: String) {
        Text(text, style = MaterialTheme.typography.caption, textAlign = TextAlign.Center)
}

private data class ContributionSegment(
        val index: Int,
        val contribution: Double,
        val color: Color
)

private data class ContributionData(
        val alimentRation: fr.vetbrain.vetnutri_mp.Data.AlimentRation,
        val contributionAbsolue: Double,
        val contributionPourcentage: Double,
        val valeurPour100gItem: Double?,
        val alimentIndex: Int
)

// calculateContributionForGraph vit maintenant dans Data/NutrientDisplayCalculations.kt
// (calculerContributionIngredient/calculerContributionsIngredients), partagé avec l'export PDF.

@Composable
private fun ContributionsList(
        ration: Ration,
        valeurNutritionnelle: ValeurNutritionnelle,
        referenceUtilisee: ReferenceEv?,
        espece: Espece,
        preferencesRepo: PreferencesRepository,
        equationRepository: EquationRepository?
) {
        Text(
                text = translate("analnut.contribution.title"),
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Bold,
                color = VetNutriColors.Primary
        )

        var contributionsTriees by remember(ration, valeurNutritionnelle, referenceUtilisee, equationRepository) {
                mutableStateOf<List<ContributionData>>(emptyList())
        }

        var isLoading by remember(ration, valeurNutritionnelle, referenceUtilisee, equationRepository) {
                mutableStateOf(true)
        }

        val isRatioNutrient: Boolean = estNutrimentAnalysisRatio(valeurNutritionnelle.nutriment)

        LaunchedEffect(ration, valeurNutritionnelle, referenceUtilisee, equationRepository) {
                isLoading = true
                try {
                        val list =
                                ration.alimentMutableList.mapIndexed { index, alimentRation ->
                                        val quantite: Double = alimentRation.quantite
                                        val nutrient: Nutrient = valeurNutritionnelle.nutriment
                                        val isAA: Boolean =
                                                nutrient is fr.vetbrain.vetnutri_mp.Enumer.AAEnum

                                        val valeurPour100gBrute: Double? =
                                                if (nutrient ==
                                                                fr.vetbrain.vetnutri_mp.Enumer
                                                                        .NutrientMain
                                                                        .ENERGIE
                                                ) {
                                                        val energieTotale =
                                                                alimentRation.getEnergie(
                                                                        referenceUtilisee,
                                                                        equationRepository
                                                                )
                                                        if (quantite > 0.0) {
                                                                (energieTotale / quantite) * 100.0
                                                        } else {
                                                                null
                                                        }
                                                } else {
                                                        alimentRation.getNutrientWithComplementary(
                                                                nutrient = nutrient,
                                                                equationRepository = equationRepository,
                                                                referenceEv = referenceUtilisee
                                                        )
                                                }

                                        val valeurPour100gConvertie: Double? =
                                                if (isAA) {
                                                        val proteines =
                                                                alimentRation.aliment?.getNutrient(
                                                                        fr.vetbrain.vetnutri_mp
                                                                                .Enumer
                                                                                .NutrientMain
                                                                                .PROTEINE
                                                                ) ?: 0.0
                                                        valeurPour100gBrute?.let {
                                                                (it * proteines) / 100.0
                                                        }
                                                } else {
                                                        valeurPour100gBrute
                                                }

                                        val contributionCalculee: Double =
                                                valeurPour100gConvertie?.let { valeur ->
                                                        if (isRatioNutrient) valeur
                                                        else (valeur * quantite) / 100.0
                                                } ?: 0.0

                                        val contributionPourcentage: Double =
                                                if (!isRatioNutrient &&
                                                                valeurNutritionnelle.valeur > 0
                                                ) {
                                                        (contributionCalculee /
                                                                valeurNutritionnelle.valeur
                                                                        .toDouble()) * 100.0
                                                } else {
                                                        0.0
                                                }

                                        ContributionData(
                                                alimentRation = alimentRation,
                                                contributionAbsolue = contributionCalculee,
                                                contributionPourcentage = contributionPourcentage,
                                                valeurPour100gItem = valeurPour100gBrute,
                                                alimentIndex = index
                                        )
                                }
                        contributionsTriees = list.sortedByDescending { it.contributionAbsolue }
                } catch (_: Exception) {
                        contributionsTriees = emptyList()
                } finally {
                        isLoading = false
                }
        }

        if (isLoading) {
                Box(
                        modifier = Modifier.fillMaxWidth().padding(AppSizes.paddingSmall),
                        contentAlignment = Alignment.Center
                ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
        } else {
                Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                ) {
                        contributionsTriees.forEach { data ->
                                ContributionItem(
                                        alimentRation = data.alimentRation,
                                        valeurNutritionnelle = valeurNutritionnelle,
                                        espece = espece,
                                        preferencesRepo = preferencesRepo,
                                        equationRepository = equationRepository,
                                        referenceUtilisee = referenceUtilisee,
                                        contributionAbsolue = data.contributionAbsolue,
                                        contributionPourcentage = data.contributionPourcentage,
                                        valeurPour100gItem = data.valeurPour100gItem,
                                        alimentIndex = data.alimentIndex
                                )
                        }
                }
        }
}

@Composable
private fun RecapitulatifCard(
        valeurNutritionnelle: ValeurNutritionnelle,
        typeExpressionBesoin: TypeExpressionBesoin,
        poidsMetabolique: Double?,
        poidsAnimal: Double?,
        besoinEnergetiqueEntretien: Double?,
        referenceUtilisee: ReferenceEv? = null
) {
        Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = AppSizes.elevationSmall,
                backgroundColor = VetNutriColors.Primary.copy(alpha = 0.1f)
        ) {
                Column(
                        modifier = Modifier.padding(AppSizes.paddingMedium),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                ) {
                        val (valeurFormatee: String, uniteAffichage: String) =
                                if (estNutrimentAnalysisRatio(valeurNutritionnelle.nutriment)) {
                                        Pair(
                                                TextUtils.formatDecimal(
                                                        valeurNutritionnelle.valeur,
                                                        2
                                                ),
                                                ""
                                        )
                                } else {
                                        calculerAffichageNutriment(
                                                valeurNutritionnelle,
                                                typeExpressionBesoin,
                                                poidsMetabolique,
                                                poidsAnimal,
                                                besoinEnergetiqueEntretien,
                                                referenceUtilisee
                                        )
                                }
                        Text(
                                text = translate("analnut.recap.intake", valeurFormatee, uniteAffichage),
                                style = MaterialTheme.typography.body1,
                                fontWeight = FontWeight.Bold,
                                color = VetNutriColors.Primary
                        )
                        if (!estNutrimentAnalysisRatio(valeurNutritionnelle.nutriment)) {
                                Text(
                                        text =
                                                translate(
                                                        "analnut.recap.per_day",
                                                        TextUtils.formatDecimal(valeurNutritionnelle.valeur, 2),
                                                        valeurNutritionnelle.unite.displayName
                                                ),
                                        style = MaterialTheme.typography.body2,
                                        fontWeight = FontWeight.Medium,
                                        color = VetNutriColors.Primary.copy(alpha = 0.8f)
                                )
                        }
                        val isComplete: Boolean = valeurNutritionnelle.complete
                        Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                        imageVector =
                                                if (isComplete) Icons.Filled.Check
                                                else Icons.Filled.Warning,
                                        contentDescription =
                                                if (isComplete) translate("analnut.status.complete_data")
                                                else translate("analnut.status.incomplete_data"),
                                        tint =
                                                if (isComplete) Color.Green
                                                else VetNutriColors.Error,
                                        modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                        text =
                                                if (isComplete) translate("analnut.status.complete_data")
                                                else translate("analnut.status.incomplete_data"),
                                        style = MaterialTheme.typography.body2,
                                        color =
                                                if (isComplete) Color.Green
                                                else VetNutriColors.Error
                                )
                        }
                }
        }
}

@Composable
private fun ContributionItem(
        alimentRation: fr.vetbrain.vetnutri_mp.Data.AlimentRation,
        valeurNutritionnelle: ValeurNutritionnelle,
        espece: Espece,
        preferencesRepo: PreferencesRepository,
        equationRepository: EquationRepository?,
        referenceUtilisee: ReferenceEv?,
        contributionAbsolue: Double,
        contributionPourcentage: Double,
        valeurPour100gItem: Double?,
        alimentIndex: Int = -1
) {
        val quantite: Double = alimentRation.quantite
        val nutrient: Nutrient = valeurNutritionnelle.nutriment
        val valeurAlimentBrute: Double? = alimentRation.aliment?.getNutrient(nutrient)
        val isAAItem: Boolean = nutrient is fr.vetbrain.vetnutri_mp.Enumer.AAEnum
        val valeurAlimentAffichee: Double? = if (isAAItem && valeurAlimentBrute != null) {
                val proteines = alimentRation.aliment?.getNutrient(
                        fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.PROTEINE
                ) ?: 0.0
                (valeurAlimentBrute * proteines) / 100.0
        } else valeurAlimentBrute
        Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = AppSizes.elevationSmall,
                backgroundColor = MaterialTheme.colors.surface
        ) {
                Column(modifier = Modifier.padding(AppSizes.paddingMedium)) {
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Row(
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        if (alimentIndex >= 0) {
                                                Box(
                                                        modifier = Modifier
                                                                .size(12.dp)
                                                                .background(
                                                                        fr.vetbrain.vetnutri_mp.Theme.VetNutriColors.getFeedColor(alimentIndex),
                                                                        MaterialTheme.shapes.small
                                                                )
                                                )
                                        }
                                        Text(
                                                text = alimentRation.aliment?.nom ?: translate("analnut.contribution.unknown_food"),
                                                style = MaterialTheme.typography.subtitle2,
                                                fontWeight = FontWeight.Bold
                                        )
                                }
                                val hasEqForAliment: Boolean =
                                        (valeurAlimentAffichee == null || valeurAlimentAffichee <= 0.0) &&
                                                valeurPour100gItem != null
                                if (hasEqForAliment) {
                                        Icon(
                                                imageVector = Icons.Filled.Info,
                                                contentDescription = translate("analnut.contribution.calculated_value"),
                                                tint = Color(0xFFFF9800),
                                                modifier = Modifier.size(16.dp)
                                        )
                                } else if (valeurAlimentAffichee == null) {
                                        Icon(
                                                imageVector = Icons.Filled.Warning,
                                                contentDescription = translate("analnut.contribution.missing_info"),
                                                tint = VetNutriColors.Error,
                                                modifier = Modifier.size(16.dp)
                                        )
                                }
                        }
                        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                        ) {
                                Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                                text =
                                                        translate(
                                                                "analnut.contribution.quantity",
                                                                TextUtils.formatDecimal(quantite.toDouble(), 1)
                                                        ),
                                                style = MaterialTheme.typography.body2,
                                                fontWeight = FontWeight.Medium,
                                                color = VetNutriColors.Primary
                                        )
                                        val isAnalysisNoUnit: Boolean = estNutrimentAnalysisRatio(nutrient)
                                        Text(
                                                text =
                                                        if (isAnalysisNoUnit) {
                                                                valeurPour100gItem?.let { valeur ->
                                                                        translate(
                                                                                "analnut.contribution.value_ratio",
                                                                                TextUtils.formatDecimal(valeur, 2)
                                                                        )
                                                                }
                                                                        ?: translate("analnut.contribution.value_na")
                                                        } else {
                                                                if (valeurAlimentAffichee != null) {
                                                                        translate(
                                                                                "analnut.contribution.value_100g",
                                                                                TextUtils.formatDecimal(valeurAlimentAffichee.toDouble(), 2),
                                                                                valeurNutritionnelle.unite.displayName
                                                                        )
                                                                } else {
                                                                        valeurPour100gItem?.let {
                                                                                valeur ->
                                                                                translate(
                                                                                        "analnut.contribution.value_100g",
                                                                                        TextUtils.formatDecimal(valeur, 2),
                                                                                        valeurNutritionnelle.unite.displayName
                                                                                )
                                                                        }
                                                                                ?: translate("analnut.contribution.value_100g_na")
                                                                }
                                                        },
                                                style = MaterialTheme.typography.body2,
                                                fontWeight = FontWeight.Medium,
                                                color = VetNutriColors.Primary
                                        )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                                text =
                                                        translate(
                                                                "analnut.contribution.amount",
                                                                TextUtils.formatDecimal(contributionAbsolue, 2),
                                                                valeurNutritionnelle.unite.displayName
                                                        ),
                                                style = MaterialTheme.typography.body2,
                                                fontWeight = FontWeight.Medium,
                                                color = VetNutriColors.Secondary
                                        )
                                        Text(
                                                text =
                                                        translate(
                                                                "analnut.contribution.share",
                                                                TextUtils.formatDecimal(contributionPourcentage, 1)
                                                        ),
                                                style = MaterialTheme.typography.body2,
                                                color = VetNutriColors.Secondary
                                        )
                                }
                        }
                }
        }
}

@Composable
private fun DialogTitre(titre: String, onDismiss: () -> Unit) {
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
                Text(
                        text = titre,
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold,
                        color = VetNutriColors.Primary,
                        modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                        Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = translate(LocalizationKeys.AnalNut.CLOSE),
                                tint = VetNutriColors.Primary,
                                modifier = Modifier.size(24.dp)
                        )
                }
        }
}

@Composable
private fun ReferenceCard(
        titre: String,
        reference: ReferenceEv,
        valeurNutritionnelle: ValeurNutritionnelle,
        typeExpressionBesoin: TypeExpressionBesoin,
        poidsAnimal: Double?,
        poidsMetabolique: Double?,
        besoinEnergetiqueEntretien: Double?,
        besoinEnergetiqueCible: Double? = besoinEnergetiqueEntretien,
        referencesMaladies: List<ReferenceEv> = emptyList(),
        ration: Ration? = null,
        equationRepository: EquationRepository? = null
) {
        val nutrient: Nutrient = valeurNutritionnelle.nutriment
        val isAnalysisNoUnit: Boolean = estNutrimentAnalysisRatio(nutrient)
        val apportConverti: Double =
                if (isAnalysisNoUnit) {
                        valeurNutritionnelle.valeur
                } else {
                        convertirVersUnitePreferences(
                                valeurNutritionnelle.valeur,
                                UnitReqEnum.ABSOLUTE,
                                typeExpressionBesoin.unitReqEnum,
                                besoinEnergetiqueEntretien,
                                poidsAnimal,
                                poidsMetabolique
                        )
                                ?: valeurNutritionnelle.valeur
                }
        val apportBulletGraph: Double =
                if (nutrient == fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.ENERGIE &&
                                besoinEnergetiqueCible != null &&
                                besoinEnergetiqueCible > 0.0
                ) {
                        (valeurNutritionnelle.valeur / besoinEnergetiqueCible) * 100.0
                } else {
                        apportConverti
                }
        Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = AppSizes.elevationSmall,
                backgroundColor = VetNutriColors.Secondary.copy(alpha = 0.1f)
        ) {
                Column(
                        modifier = Modifier.padding(AppSizes.paddingMedium),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                ) {
                        Text(
                                text = titre,
                                style = MaterialTheme.typography.subtitle1,
                                fontWeight = FontWeight.Bold,
                                color = VetNutriColors.Secondary
                        )
                        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                        ReferenceBulletGraph(
                                valeurApport = apportBulletGraph,
                                reference = reference,
                                nutriment = nutrient,
                                typeExpressionBesoin = typeExpressionBesoin,
                                poidsAnimal = poidsAnimal,
                                poidsMetabolique = poidsMetabolique,
                                besoinEnergetiqueEntretien = besoinEnergetiqueEntretien,
                                besoinEnergetiqueCible = besoinEnergetiqueCible,
                                referencesMaladies = referencesMaladies,
                                onClick = null,
                                ration = ration,
                                equationRepository = equationRepository
                        )
                        ReferenceLevelsList(
                                reference = reference,
                                nutrient = nutrient,
                                valeurNutritionnelle = valeurNutritionnelle,
                                typeExpressionBesoin = typeExpressionBesoin,
                                poidsAnimal = poidsAnimal,
                                poidsMetabolique = poidsMetabolique,
                                besoinEnergetiqueEntretien = besoinEnergetiqueEntretien,
                                besoinEnergetiqueCible = besoinEnergetiqueCible,
                                isAnalysisNoUnit = isAnalysisNoUnit
                        )
                }
        }
}

@Composable
private fun ReferenceLevelsList(
        reference: ReferenceEv,
        nutrient: Nutrient,
        valeurNutritionnelle: ValeurNutritionnelle,
        typeExpressionBesoin: TypeExpressionBesoin,
        poidsAnimal: Double?,
        poidsMetabolique: Double?,
        besoinEnergetiqueEntretien: Double?,
        besoinEnergetiqueCible: Double? = besoinEnergetiqueEntretien,
        isAnalysisNoUnit: Boolean
) {
        val refLevels: List<Pair<Reflevel, String>> =
                listOf(
                        Reflevel.MIN to translate("analnut.level.minimum"),
                        Reflevel.OPTIMIN to translate("analnut.level.optimal_minimum"),
                        Reflevel.OPTIMAX to translate("analnut.level.optimal_maximum"),
                        Reflevel.MAX to translate("analnut.level.maximum")
                )
        refLevels.forEach { (level: Reflevel, levelName: String) ->
                val hasExplicitReference = reference.contientNutriment(nutrient, level)
                val defaultEnergyFactor =
                        if (!hasExplicitReference &&
                                        hasDefaultEnergyReferenceLevels(
                                                nutrient,
                                                besoinEnergetiqueCible
                                        )
                        ) {
                                when (level) {
                                        Reflevel.MIN -> 0.9
                                        Reflevel.MAX -> 1.1
                                        else -> null
                                }
                        } else {
                                null
                        }
                if (hasExplicitReference || defaultEnergyFactor != null) {
                        val valeurRef: Double =
                                if (hasExplicitReference) reference.obtenirNutriment(nutrient, level)
                                else defaultEnergyFactor ?: 0.0
                        val uniteRef: UnitReqEnum =
                                if (hasExplicitReference) {
                                        UnitReqEnum.getById(
                                                reference.obtenirUniteNutriment(nutrient, level)
                                        )
                                } else {
                                        UnitReqEnum.RATIO
                                }
                        val biblioRef = reference.obtenirBiblioNutriment(nutrient, level)
                        val besoinAbsolu: Double? =
                                if (defaultEnergyFactor != null) {
                                        besoinEnergetiqueCible?.let { it * defaultEnergyFactor }
                                } else if (isAnalysisNoUnit) null
                                else {
                                        calculerBesoinAbsolu(
                                                valeurRef,
                                                uniteRef,
                                                besoinEnergetiqueEntretien,
                                                poidsAnimal,
                                                poidsMetabolique
                                        )
                                }
                        val couleurConformite: Color =
                                obtenirCouleurConformite(
                                        level,
                                        valeurNutritionnelle.valeur,
                                        besoinAbsolu
                                )
                        Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                        Text(
                                                text = translate("analnut.reference.level_label", levelName),
                                                style = MaterialTheme.typography.body2,
                                                fontWeight = FontWeight.Medium
                                        )
                                        Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                        text =
                                                                if (isAnalysisNoUnit)
                                                                        TextUtils.formatDecimal(
                                                                                valeurRef
                                                                                        .toDouble(),
                                                                                2
                                                                        )
                                                                else if (defaultEnergyFactor != null)
                                                                        "${TextUtils.formatDecimal(valeurRef * 100.0, 0)}%"
                                                                else
                                                                        "${TextUtils.formatDecimal(valeurRef.toDouble(), 2)} ${uniteRef.label}",
                                                        style = MaterialTheme.typography.body2,
                                                        color = couleurConformite
                                                )
                                                if (defaultEnergyFactor == null &&
                                                                !isAnalysisNoUnit &&
                                                                typeExpressionBesoin.unitReqEnum !=
                                                                        uniteRef
                                                ) {
                                                        val valeurTemp =
                                                                ValeurNutritionnelle(
                                                                        valeurNutritionnelle
                                                                                .nutriment,
                                                                        valeurNutritionnelle.unite,
                                                                        valeurRef.toDouble(),
                                                                        "Référence convertie",
                                                                        true
                                                                )
                                                        val (
                                                                valeurPreferee: String,
                                                                unitePreferee: String) =
                                                                calculerAffichageNutriment(
                                                                        valeurTemp,
                                                                        typeExpressionBesoin,
                                                                        poidsMetabolique,
                                                                        poidsAnimal,
                                                                        besoinEnergetiqueEntretien,
                                                                        reference
                                                                )
                                                        Text(
                                                                text =
                                                                        translate(
                                                                                "analnut.recap.converted_ref",
                                                                                valeurPreferee,
                                                                                unitePreferee
                                                                        ),
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .caption,
                                                                fontWeight = FontWeight.Bold,
                                                                color = couleurConformite
                                                        )
                                                }
                                                besoinAbsolu?.let { valeurAbsolue: Double ->
                                                        Text(
                                                                text =
                                                                        translate(
                                                                                "analnut.recap.per_day",
                                                                                TextUtils.formatDecimal(valeurAbsolue, 2),
                                                                                valeurNutritionnelle.unite.displayName
                                                                        ),
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .caption,
                                                                fontWeight = FontWeight.Bold,
                                                                color = couleurConformite
                                                        )
                                                }
                                                if (hasExplicitReference &&
                                                                (biblioRef.firstAuthor.isNotEmpty() ||
                                                                biblioRef.completeRef.isNotEmpty()
                                                                )
                                                ) {
                                                        Text(
                                                                text =
                                                                        (translate(
                                                                                "analnut.reference.biblio_ref",
                                                                                biblioRef.firstAuthor,
                                                                                biblioRef.completeRef
                                                                        ))
                                                                                .take(30) +
                                                                                if (biblioRef
                                                                                                .firstAuthor
                                                                                                .length +
                                                                                                biblioRef
                                                                                                        .completeRef
                                                                                                        .length >
                                                                                                30
                                                                                )
                                                                                        "..."
                                                                                else "",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .caption,
                                                                color =
                                                                        couleurConformite.copy(
                                                                                alpha = 0.7f
                                                                        )
                                                        )
                                                }
                                        }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                        }
                }
        }
}

// calculerAffichageNutriment/calculerBesoinAbsolu vivent maintenant dans
// Data/NutrientDisplayCalculations.kt (partagés avec l'écran principal et l'export PDF).
