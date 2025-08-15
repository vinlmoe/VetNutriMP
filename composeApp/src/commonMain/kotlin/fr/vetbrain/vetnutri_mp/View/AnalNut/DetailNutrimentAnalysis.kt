package fr.vetbrain.vetnutri_mp.View.AnalNut

import androidx.compose.foundation.layout.*
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
import fr.vetbrain.vetnutri_mp.Enumer.*
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
                                        Pair(
                                                TextUtils.formatDecimal(valeurParKg, 2),
                                                "$uniteOriginale/kg"
                                        )
                                } else {
                                        Pair(
                                                TextUtils.formatDecimal(valeurAbsolue, 2),
                                                uniteOriginale
                                        )
                                }
                        }
                                ?: Pair(TextUtils.formatDecimal(valeurAbsolue, 2), uniteOriginale)
                }
                TypeExpressionBesoin.PAR_KG_METABOLIQUE -> {
                        // Par kg de poids métabolique (kg^0.75)
                        poidsMetabolique?.let { poidsMetab ->
                                if (poidsMetab > 0) {
                                        val valeurParKgMetab = valeurAbsolue / poidsMetab
                                        Pair(
                                                TextUtils.formatDecimal(valeurParKgMetab, 2),
                                                "$uniteOriginale/kg${TextUtils.toSuperscript("0.75")}"
                                        )
                                } else {
                                        Pair(
                                                TextUtils.formatDecimal(valeurAbsolue, 2),
                                                uniteOriginale
                                        )
                                }
                        }
                                ?: Pair(TextUtils.formatDecimal(valeurAbsolue, 2), uniteOriginale)
                }
                TypeExpressionBesoin.PAR_KCAL -> {
                        // Par 1000 kcal de BEE (Besoin Énergétique d'Entretien)
                        besoinEnergetiqueEntretien?.let { bee ->
                                if (bee > 0) {
                                        val valeurPar1000Kcal = (valeurAbsolue / bee) * 1000
                                        Pair(
                                                TextUtils.formatDecimal(valeurPar1000Kcal, 2),
                                                "$uniteOriginale/1000 kcal"
                                        )
                                } else {
                                        Pair(
                                                TextUtils.formatDecimal(valeurAbsolue, 2),
                                                uniteOriginale
                                        )
                                }
                        }
                                ?: Pair(TextUtils.formatDecimal(valeurAbsolue, 2), uniteOriginale)
                }
                TypeExpressionBesoin.PAR_KJ -> {
                        // Par 1000 kJ de BEE (conversion : 1 kcal = 4.184 kJ)
                        besoinEnergetiqueEntretien?.let { bee ->
                                if (bee > 0) {
                                        val beeEnKj = bee * 4.184 // Conversion kcal vers kJ
                                        val valeurPar1000Kj = (valeurAbsolue / beeEnKj) * 1000
                                        Pair(
                                                TextUtils.formatDecimal(valeurPar1000Kj, 2),
                                                "$uniteOriginale/1000 kJ"
                                        )
                                } else {
                                        Pair(
                                                TextUtils.formatDecimal(valeurAbsolue, 2),
                                                uniteOriginale
                                        )
                                }
                        }
                                ?: Pair(TextUtils.formatDecimal(valeurAbsolue, 2), uniteOriginale)
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
        equationRepository: EquationRepository? = null,
        referencesMaladies: List<ReferenceEv> = emptyList(),
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
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f),
                onDismissRequest = onDismiss,
                title = { DialogTitre(titre = "Détails : $nom", onDismiss = onDismiss) },
                text = {
                        Column(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {

                                // Titre et apport non scrollables
                                RecapitulatifCard(
                                        valeurNutritionnelle = valeurNutritionnelle,
                                        typeExpressionBesoin = typeExpressionBesoin,
                                        poidsMetabolique = poidsMetabolique,
                                        poidsAnimal = poidsAnimal,
                                        besoinEnergetiqueEntretien = besoinEnergetiqueEntretien
                                )
                                Text(
                                        text = "Références nutritionnelles",
                                        style = MaterialTheme.typography.subtitle1,
                                        fontWeight = FontWeight.Bold,
                                        color = VetNutriColors.Primary
                                )
                                Divider()

                                // Contenu scrollable
                                LazyColumn(
                                        modifier = Modifier.fillMaxWidth().weight(1f),
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
                                                                }

                                                if (hasReferenceValues) {
                                                        item {
                                                                ReferenceCard(
                                                                        titre =
                                                                                "Références nutritionnelles - ${ref.nom}",
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
                                                                        referencesMaladies =
                                                                                referencesMaladies
                                                                )
                                                        }
                                                }
                                        }

                                        // Section des références maladies (après les références
                                        // générales)
                                        if (referencesMaladies.isNotEmpty()) {
                                                println(
                                                        "DEBUG Detail: Références maladies affichées pour $nom -> ${referencesMaladies.joinToString { it.nom }}"
                                                )
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
                                                                                        "Références maladies - ${refMaladie.nom}",
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
                                                                                        besoinEnergetiqueEntretien
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
        valeurApport: Float,
        reference: ReferenceEv,
        nutriment: Nutrient,
        typeExpressionBesoin: TypeExpressionBesoin,
        poidsAnimal: Double?,
        poidsMetabolique: Double?,
        besoinEnergetiqueEntretien: Double?,
        referencesMaladies: List<ReferenceEv> = emptyList()
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
        val isAnalysisNoUnit =
                nutriment is fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis &&
                        nutriment.unite.isBlank()
        val minRefConverti =
                if (minRef > 0f) {
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
                } else null

        val optiminRefConverti =
                if (optiminRef > 0f) {
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
                if (optimaxRef > 0f) {
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
                if (maxRef > 0f) {
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
                                                                TextUtils.formatDecimal(
                                                                        tick.toDouble(),
                                                                        1
                                                                )
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

                                // Lignes de référence (générales)
                                minRefConverti?.let { min -> comparativeMeasure(min) }
                                optiminRefConverti?.let { optimin -> comparativeMeasure(optimin) }
                                optimaxRefConverti?.let { optimax -> comparativeMeasure(optimax) }
                                maxRefConverti?.let { max -> comparativeMeasure(max) }

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
                                                if (minM > 0f)
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
                                                if (optiMinM > 0f)
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
                                                if (optiMaxM > 0f)
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
                                                if (maxM > 0f)
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
                                                        comparativeMeasure(v)
                                                }
                                }
                        }
                }

                // Légende retirée sous les bullet graphs
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
                                if (poids > 0.0) (valeurAbsolue / poids).toFloat() else null
                        }
                }

                // Vers PERMS (par kg de poids métabolique)
                UnitReqEnum.PERMS -> {
                        poidsMetabolique?.let { poidsMetab ->
                                if (poidsMetab > 0.0) (valeurAbsolue / poidsMetab).toFloat()
                                else null
                        }
                }

                // Vers PERKCAL (par 1000 kcal)
                UnitReqEnum.PERKCAL -> {
                        besoinEnergetiqueEntretien?.let { bee ->
                                if (bee > 0.0) ((valeurAbsolue * 1000.0) / bee).toFloat() else null
                        }
                }

                // Vers PERKJ (par 1000 kJ)
                UnitReqEnum.PERKJ -> {
                        besoinEnergetiqueEntretien?.let { bee ->
                                if (bee > 0.0) {
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
                text = "Contribution par ingrédient (par ordre décroissant)",
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Bold,
                color = VetNutriColors.Primary
        )
        val selectedEquationUuids: List<String> =
                referenceUtilisee?.equationsNut?.map { it.uuid } ?: emptyList()
        val allEquations =
                try {
                        kotlinx.coroutines.runBlocking {
                                equationRepository?.getAllEquations() ?: emptyList()
                        }
                } catch (_: Exception) {
                        emptyList()
                }
        val applicableEquations =
                allEquations.filter { eq ->
                        val kindOk: Boolean = eq.kind == EquationKind.COMPLEMENTARY_NUTRIENT
                        val specieOk: Boolean = eq.specie == espece || eq.specie == Espece.CH
                        val nutrientOk: Boolean =
                                (eq.nutrient == valeurNutritionnelle.nutriment) ||
                                        (eq.nutrient?.label == valeurNutritionnelle.nutriment.label)
                        (eq.uuid in selectedEquationUuids) && kindOk && specieOk && nutrientOk
                }
        println(
                "EQDBG-ING applicableEquations=${applicableEquations.map { it.uuid }} for nutrient=${valeurNutritionnelle.nutriment.label} specie=${espece.name}"
        )
        val isRatioNutrient: Boolean =
                valeurNutritionnelle.unite == fr.vetbrain.vetnutri_mp.Enumer.UnitEnum.NO ||
                        valeurNutritionnelle.unite.label == "RATIO"
        val contributionsTriees =
                ration.alimentMutableList
                        .map { alimentRation ->
                                val quantite: Float = alimentRation.quantite
                                val nutrient: Nutrient = valeurNutritionnelle.nutriment
                                val valeurPour100g: Double? =
                                        try {
                                                println(
                                                        "EQDBG-ING detail: try getNutrientWithComplementary for food='${alimentRation.aliment?.nom}' nutr=${nutrient.label}"
                                                )
                                                kotlinx.coroutines
                                                        .runBlocking {
                                                                alimentRation
                                                                        .getNutrientWithComplementary(
                                                                                nutrient = nutrient,
                                                                                preferences = null,
                                                                                equationRepository =
                                                                                        equationRepository,
                                                                                referenceEv =
                                                                                        referenceUtilisee
                                                                        )
                                                        }
                                                        ?.toDouble()
                                        } catch (e: Exception) {
                                                println(
                                                        "EQDBG-ING detail: exception getNutrientWithComplementary ${e.message}"
                                                )
                                                null
                                        }
                                val contributionCalculee: Double =
                                        if (valeurPour100g != null) {
                                                if (isRatioNutrient) valeurPour100g
                                                else (valeurPour100g * quantite.toDouble()) / 100.0
                                        } else 0.0
                                println(
                                        "EQDBG-ING detail: food='${alimentRation.aliment?.nom}' v100g=${valeurPour100g} contrib=${contributionCalculee}"
                                )
                                val contributionPourcentage: Double =
                                        if (!isRatioNutrient && valeurNutritionnelle.valeur > 0) {
                                                (contributionCalculee /
                                                        valeurNutritionnelle.valeur.toDouble()) *
                                                        100.0
                                        } else 0.0
                                Triple(alimentRation, contributionCalculee, contributionPourcentage)
                        }
                        .sortedByDescending { it.second }
        Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
        ) {
                contributionsTriees.forEach {
                        (alimentRation, contributionAbsolue, contributionPourcentage) ->
                        ContributionItem(
                                alimentRation = alimentRation,
                                valeurNutritionnelle = valeurNutritionnelle,
                                espece = espece,
                                preferencesRepo = preferencesRepo,
                                equationRepository = equationRepository,
                                referenceUtilisee = referenceUtilisee,
                                contributionAbsolue = contributionAbsolue,
                                contributionPourcentage = contributionPourcentage
                        )
                }
        }
}

@Composable
private fun RecapitulatifCard(
        valeurNutritionnelle: ValeurNutritionnelle,
        typeExpressionBesoin: TypeExpressionBesoin,
        poidsMetabolique: Double?,
        poidsAnimal: Double?,
        besoinEnergetiqueEntretien: Double?
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
                                if (valeurNutritionnelle.nutriment is
                                                fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis &&
                                                valeurNutritionnelle.unite.displayName.isBlank()
                                ) {
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
                                                besoinEnergetiqueEntretien
                                        )
                                }
                        Text(
                                text = "Apport: $valeurFormatee $uniteAffichage",
                                style = MaterialTheme.typography.body1,
                                fontWeight = FontWeight.Bold,
                                color = VetNutriColors.Primary
                        )
                        if (!(valeurNutritionnelle.nutriment is
                                        fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis &&
                                        valeurNutritionnelle.unite.displayName.isBlank())
                        ) {
                                Text(
                                        text =
                                                "→ ${TextUtils.formatDecimal(valeurNutritionnelle.valeur, 2)} ${valeurNutritionnelle.unite.displayName}/jour",
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
                                                if (isComplete) "Données complètes"
                                                else "Données incomplètes",
                                        tint =
                                                if (isComplete) Color.Green
                                                else VetNutriColors.Error,
                                        modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                        text =
                                                if (isComplete) "Données complètes"
                                                else "Données incomplètes",
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
        contributionPourcentage: Double
) {
        val quantite: Float = alimentRation.quantite
        val nutrient: Nutrient = valeurNutritionnelle.nutriment
        val valeurAliment: Float? = alimentRation.aliment?.getNutrient(nutrient)
        val prefsEspeceItem =
                try {
                        kotlinx.coroutines.runBlocking {
                                preferencesRepo.loadPreferences()
                                preferencesRepo.getPreferencesForSpecies(espece)
                        }
                } catch (_: Exception) {
                        null
                }
        if (prefsEspeceItem == null) {
                println("EQDBG-ING detail(item): prefsEspece null for ${espece.name}")
        } else {
                println(
                        "EQDBG-ING detail(item): prefs uuids=" +
                                prefsEspeceItem.getSelectedEquationUuids()
                )
        }
        val valeurPour100gItem: Float? =
                try {
                        println(
                                "EQDBG-ING detail(item): try getNutrientWithComplementary food='${alimentRation.aliment?.nom}' nutr=${nutrient.label}"
                        )
                        kotlinx.coroutines.runBlocking {
                                alimentRation.getNutrientWithComplementary(
                                        nutrient = nutrient,
                                        preferences = null,
                                        equationRepository = equationRepository,
                                        referenceEv = referenceUtilisee
                                )
                        }
                } catch (e: Exception) {
                        println("EQDBG-ING detail(item): exception ${e.message}")
                        null
                }
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
                                Text(
                                        text = alimentRation.aliment?.nom ?: "Aliment inconnu",
                                        style = MaterialTheme.typography.subtitle2,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                )
                                val hasEqForAliment: Boolean =
                                        (valeurAliment == null || valeurAliment <= 0f) &&
                                                valeurPour100gItem != null
                                if (hasEqForAliment) {
                                        Icon(
                                                imageVector = Icons.Filled.Info,
                                                contentDescription = "Valeur calculée",
                                                tint = Color(0xFFFF9800),
                                                modifier = Modifier.size(16.dp)
                                        )
                                } else if (valeurAliment == null) {
                                        Icon(
                                                imageVector = Icons.Filled.Warning,
                                                contentDescription = "Information manquante",
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
                                                        "Quantité: ${TextUtils.formatDecimal(quantite.toDouble(), 1)}g",
                                                style = MaterialTheme.typography.body2,
                                                fontWeight = FontWeight.Medium,
                                                color = VetNutriColors.Primary
                                        )
                                        val isAnalysisNoUnit: Boolean =
                                                nutrient is
                                                        fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis &&
                                                        nutrient.unite.isBlank()
                                        Text(
                                                text =
                                                        if (isAnalysisNoUnit) {
                                                                if (valeurPour100gItem != null) {
                                                                        "Valeur: ${TextUtils.formatDecimal(valeurPour100gItem.toDouble(), 2)}"
                                                                } else {
                                                                        "Valeur: NA"
                                                                }
                                                        } else {
                                                                if (valeurAliment != null) {
                                                                        "Valeur (100g): ${TextUtils.formatDecimal(valeurAliment.toDouble(), 2)} ${valeurNutritionnelle.unite.displayName}"
                                                                } else if (valeurPour100gItem !=
                                                                                null
                                                                ) {
                                                                        "Valeur (100g): ${TextUtils.formatDecimal(valeurPour100gItem.toDouble(), 2)} ${valeurNutritionnelle.unite.displayName}"
                                                                } else {
                                                                        "Valeur (100g): NA"
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
                                                        "Contribution: ${TextUtils.formatDecimal(contributionAbsolue, 2)} ${valeurNutritionnelle.unite.displayName}",
                                                style = MaterialTheme.typography.body2,
                                                fontWeight = FontWeight.Medium,
                                                color = VetNutriColors.Secondary
                                        )
                                        Text(
                                                text =
                                                        "Part: ${TextUtils.formatDecimal(contributionPourcentage, 1)}%",
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
                IconButton(onClick = onDismiss) {
                        Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Fermer",
                                tint = VetNutriColors.Primary
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
        referencesMaladies: List<ReferenceEv> = emptyList()
) {
        val nutrient: Nutrient = valeurNutritionnelle.nutriment
        val isAnalysisNoUnit: Boolean =
                (nutrient is fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis &&
                        nutrient.unite.isBlank())
        val apportConverti: Float =
                if (isAnalysisNoUnit) {
                        valeurNutritionnelle.valeur.toFloat()
                } else {
                        convertirVersUnitePreferences(
                                valeurNutritionnelle.valeur.toFloat(),
                                UnitReqEnum.ABSOLUTE,
                                typeExpressionBesoin.unitReqEnum,
                                besoinEnergetiqueEntretien,
                                poidsAnimal,
                                poidsMetabolique
                        )
                                ?: valeurNutritionnelle.valeur.toFloat()
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
                                valeurApport = apportConverti,
                                reference = reference,
                                nutriment = nutrient,
                                typeExpressionBesoin = typeExpressionBesoin,
                                poidsAnimal = poidsAnimal,
                                poidsMetabolique = poidsMetabolique,
                                besoinEnergetiqueEntretien = besoinEnergetiqueEntretien,
                                referencesMaladies = referencesMaladies
                        )
                        ReferenceLevelsList(
                                reference = reference,
                                nutrient = nutrient,
                                valeurNutritionnelle = valeurNutritionnelle,
                                typeExpressionBesoin = typeExpressionBesoin,
                                poidsAnimal = poidsAnimal,
                                poidsMetabolique = poidsMetabolique,
                                besoinEnergetiqueEntretien = besoinEnergetiqueEntretien,
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
        isAnalysisNoUnit: Boolean
) {
        val refLevels: List<Pair<Reflevel, String>> =
                listOf(
                        Reflevel.MIN to "Minimum",
                        Reflevel.OPTIMIN to "Optimal minimum",
                        Reflevel.OPTIMAX to "Optimal maximum",
                        Reflevel.MAX to "Maximum"
                )
        refLevels.forEach { (level: Reflevel, levelName: String) ->
                if (reference.contientNutriment(nutrient, level)) {
                        val valeurRef: Float = reference.obtenirNutriment(nutrient, level)
                        val uniteRef: UnitReqEnum =
                                UnitReqEnum.getById(
                                        reference.obtenirUniteNutriment(nutrient, level)
                                )
                        val biblioRef = reference.obtenirBiblioNutriment(nutrient, level)
                        val besoinAbsolu: Double? =
                                if (isAnalysisNoUnit) null
                                else
                                        calculerBesoinAbsolu(
                                                valeurRef,
                                                uniteRef,
                                                besoinEnergetiqueEntretien,
                                                poidsAnimal,
                                                poidsMetabolique
                                        )
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
                                                text = "$levelName:",
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
                                                                else
                                                                        "${TextUtils.formatDecimal(valeurRef.toDouble(), 2)} ${uniteRef.label}",
                                                        style = MaterialTheme.typography.body2,
                                                        color = couleurConformite
                                                )
                                                if (!isAnalysisNoUnit &&
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
                                                                        besoinEnergetiqueEntretien
                                                                )
                                                        Text(
                                                                text =
                                                                        "→ $valeurPreferee $unitePreferee",
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
                                                                        "→ ${TextUtils.formatDecimal(valeurAbsolue, 2)} ${valeurNutritionnelle.unite.displayName}/jour",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .caption,
                                                                fontWeight = FontWeight.Bold,
                                                                color = couleurConformite
                                                        )
                                                }
                                                if (biblioRef.firstAuthor.isNotEmpty() ||
                                                                biblioRef.completeRef.isNotEmpty()
                                                ) {
                                                        Text(
                                                                text =
                                                                        ("Réf: ${biblioRef.firstAuthor} ${biblioRef.completeRef}")
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
        val valeurRefD: Double = valeurRef.toDouble()
        return when (uniteRef) {
                // Basé sur l'énergie (par 1000 kcal)
                UnitReqEnum.PERKCAL -> {
                        besoinEnergetiqueEntretien?.let { bee -> (valeurRefD * bee) / 1000.0 }
                }

                // Basé sur l'énergie (par 1000 kJ) - conversion en kcal puis calcul
                UnitReqEnum.PERKJ -> {
                        besoinEnergetiqueEntretien?.let { bee ->
                                // Convertir kJ en kcal : 1 kcal = 4.184 kJ
                                val beeEnKj: Double = bee * 4.184
                                (valeurRefD * beeEnKj) / 1000.0
                        }
                }

                // Basé sur le poids corporel (par kg de poids vif)
                UnitReqEnum.PERKG -> {
                        poidsAnimal?.let { poids -> valeurRefD * poids }
                }

                // Basé sur le poids métabolique (par kg^0.75)
                UnitReqEnum.PERMS -> {
                        poidsMetabolique?.let { poidsMetab -> valeurRefD * poidsMetab }
                }

                // Valeur absolue (déjà en unité finale)
                UnitReqEnum.ABSOLUTE -> {
                        valeurRefD
                }

                // Ratio - pas de calcul absolu possible
                UnitReqEnum.RATIO -> null

                // Autres unités non supportées pour le calcul absolu
                else -> null
        }
}
