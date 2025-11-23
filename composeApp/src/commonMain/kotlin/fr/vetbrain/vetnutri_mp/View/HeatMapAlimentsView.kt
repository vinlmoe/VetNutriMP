package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import fr.vetbrain.vetnutri_mp.Components.BasicNumberTextField
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.GraphFormattingUtils
import kotlinx.coroutines.runBlocking

/**
 * Vue HeatMap pour comparer les aliments aux références nutritionnelles
 * Affiche un tableau avec les aliments en colonnes et les nutriments en lignes
 */
@Composable
fun HeatMapAlimentsView(
        aliments: List<AlimentEv>,
        alimentsAnalyses: List<AlimentAnalyseData>,
        referenceEv: ReferenceEv?,
        equationRepository: EquationRepository?,
        preferencesEspece: fr.vetbrain.vetnutri_mp.Data.PreferencesEspece?,
        besoinEnergetiqueEntretien: Double?,
        poidsAnimal: Double?,
        poidsMetabolique: Double?,
        modifier: Modifier = Modifier
) {
    // Calculer les données de la heatmap
    val heatMapData = remember(
            alimentsAnalyses,
            referenceEv,
            equationRepository,
            preferencesEspece,
            besoinEnergetiqueEntretien,
            poidsAnimal,
            poidsMetabolique
    ) {
        calculerHeatMapData(
                alimentsAnalyses = alimentsAnalyses,
                referenceEv = referenceEv,
                equationRepository = equationRepository,
                preferencesEspece = preferencesEspece,
                besoinEnergetiqueEntretien = besoinEnergetiqueEntretien,
                poidsAnimal = poidsAnimal,
                poidsMetabolique = poidsMetabolique
        )
    }

    Column(
            modifier = modifier
                    .fillMaxSize()
                    .padding(AppSizes.paddingMedium),
            verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
    ) {
        // En-tête
        Text(
                text = "HeatMap - Comparaison aux références",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                color = VetNutriColors.Primary
        )
        
        Text(
                text = "Ratio = (Concentration dans l'aliment /1000 kcal) / (Besoin /1000 kcal BEE)",
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )
        
        if (referenceEv == null) {
            Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
            ) {
                Text(
                        text = "Aucune référence disponible pour la HeatMap",
                        style = MaterialTheme.typography.body1,
                        color = VetNutriColors.Error
                )
            }
            return
        }
        
        if (heatMapData.nutriments.isEmpty() || heatMapData.aliments.isEmpty()) {
            Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
            ) {
                Text(
                        text = "Aucune donnée disponible pour la HeatMap",
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }
            return
        }
        
        // Coefficient multiplicatif et Légende sur la même ligne
        var coefficientText by remember { mutableStateOf("1.0") }
        var coefficient by remember { mutableStateOf(1.0) }
        
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            // Légende des couleurs (à gauche)
            Row(
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        text = "Légende: ",
                        style = MaterialTheme.typography.caption,
                        fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                // Vert (ratio >= 1.0)
                Box(
                        modifier = Modifier
                                .size(16.dp)
                                .background(Color(0xFF4CAF50))
                )
                Text(
                        text = " ≥1.0",
                        style = MaterialTheme.typography.caption,
                        modifier = Modifier.padding(start = 4.dp)
                )
                Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                // Jaune (0.8 <= ratio < 1.0)
                Box(
                        modifier = Modifier
                                .size(16.dp)
                                .background(Color(0xFFFFEB3B))
                )
                Text(
                        text = " 0.8-1.0",
                        style = MaterialTheme.typography.caption,
                        modifier = Modifier.padding(start = 4.dp)
                )
                Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                // Rouge (ratio < 0.8)
                Box(
                        modifier = Modifier
                                .size(16.dp)
                                .background(Color(0xFFF44336))
                )
                Text(
                        text = " <0.8",
                        style = MaterialTheme.typography.caption,
                        modifier = Modifier.padding(start = 4.dp)
                )
            }
            
            // Coefficient multiplicatif (à droite)
            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
            ) {
                Text(
                        text = "Coeff.:",
                        style = MaterialTheme.typography.body2,
                        fontWeight = FontWeight.Medium
                )
                BasicNumberTextField(
                        value = coefficientText,
                        onValueChange = { newValue ->
                            coefficientText = newValue
                            // Convertir en double et mettre à jour le coefficient
                            val normalizedText = newValue.replace(',', '.')
                            val value = normalizedText.toDoubleOrNull()
                            if (value != null && value > 0) {
                                coefficient = value
                            } else if (normalizedText.isEmpty() || normalizedText == ".") {
                                coefficient = 1.0
                            }
                        },
                        placeholder = "1.0",
                        modifier = Modifier.width(80.dp),
                        allowDecimals = true,
                        allowNegative = false
                )
            }
        }
        
        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
        
        // Tableau HeatMap avec scroll horizontal et vertical synchronisé parfaitement
        // Utilisation de ScrollState standards (non Lazy) pour permettre le partage d'état
        val verticalScrollState = rememberScrollState()
        val horizontalScrollState = rememberScrollState()
        
        Box(
                modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colors.surface)
        ) {
            // 1. Grille de données (Zone principale)
            // Elle définit le scroll vertical et horizontal maître
            Box(
                modifier = Modifier
                    .padding(start = 150.dp, top = 48.dp) // Décalage pour les en-têtes
                    .fillMaxSize()
            ) {
                // Conteneur scrollable verticalement
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(verticalScrollState)
                ) {
                    // Conteneur scrollable horizontalement (le contenu complet)
                    Row(
                        modifier = Modifier.horizontalScroll(horizontalScrollState)
                    ) {
                        // La grille de données elle-même
                        Column {
                            heatMapData.nutriments.forEach { nutrimentData ->
                                Row(
                                    modifier = Modifier.height(40.dp)
                                ) {
                                    heatMapData.aliments.forEach { alimentData ->
                                        val valeur = heatMapData.valeurs[nutrimentData.nutriment]?.get(alimentData.aliment.uuid) ?: 0.0
                                        val valeurAvecCoefficient = valeur * coefficient
                                        val couleur = calculerCouleurHeatMap(valeurAvecCoefficient)
                                        
                                        Box(
                                                modifier = Modifier
                                                        .width(120.dp)
                                                        .fillMaxHeight()
                                                        .background(couleur)
                                                        .padding(AppSizes.paddingXSmall),
                                                contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                    text = if (valeur > 0) {
                                                        GraphFormattingUtils.formatDecimal(valeurAvecCoefficient, 2)
                                                    } else {
                                                        "-"
                                                    },
                                                    style = MaterialTheme.typography.caption,
                                                    textAlign = TextAlign.Center,
                                                    color = if (valeur > 0) Color.Black else MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Colonne fixe à gauche (Noms des nutriments)
            // Partage le verticalScrollState pour une synchro parfaite
            Column(
                    modifier = Modifier
                            .width(150.dp)
                            .padding(top = 48.dp) // Décalage pour l'en-tête
                            .fillMaxHeight()
                            .verticalScroll(verticalScrollState) // Même état que les données !
            ) {
                heatMapData.nutriments.forEach { nutrimentData ->
                    Box(
                            modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .background(MaterialTheme.colors.surface)
                                    .padding(AppSizes.paddingXSmall),
                            contentAlignment = Alignment.CenterStart
                    ) {
                        Column(
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                    text = nutrimentData.nom,
                                    style = MaterialTheme.typography.caption,
                                    maxLines = 1
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                        text = if (nutrimentData.niveau == Reflevel.OPTIMIN) "OPTIMIN" else "MIN",
                                        style = MaterialTheme.typography.overline,
                                        color = if (nutrimentData.niveau == Reflevel.OPTIMIN) 
                                            VetNutriColors.Primary 
                                        else 
                                            MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                                        fontSize = 9.sp
                                )
                                if (nutrimentData.valeurReferenceDisplay.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                            text = "(${nutrimentData.valeurReferenceDisplay})",
                                            style = MaterialTheme.typography.caption,
                                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                                            fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // 3. En-tête fixe en haut (Noms des aliments)
            // Partage le horizontalScrollState pour une synchro parfaite
            Row(
                modifier = Modifier
                    .padding(start = 150.dp) // Décalage pour la colonne fixe
                    .height(48.dp)
                    .fillMaxWidth()
                    .horizontalScroll(horizontalScrollState) // Même état que les données !
            ) {
                heatMapData.aliments.forEach { alimentData ->
                    Box(
                            modifier = Modifier
                                    .width(120.dp)
                                    .fillMaxHeight()
                                    .background(VetNutriColors.Primary.copy(alpha = 0.1f))
                                    .padding(AppSizes.paddingXSmall),
                            contentAlignment = Alignment.Center
                    ) {
                        Text(
                                text = alimentData.nom,
                                style = MaterialTheme.typography.caption,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 2
                        )
                    }
                }
            }
            
            // 4. Coin supérieur gauche fixe (Label "Nutriment")
            Box(
                    modifier = Modifier
                            .width(150.dp)
                            .height(48.dp)
                            .background(MaterialTheme.colors.surface) // Fond opaque pour cacher le scroll
            ) {
                Box(
                        modifier = Modifier
                                .fillMaxSize()
                                .background(VetNutriColors.Primary.copy(alpha = 0.1f))
                                .padding(AppSizes.paddingXSmall),
                        contentAlignment = Alignment.Center
                ) {
                    Text(
                            text = "Nutriment",
                            style = MaterialTheme.typography.caption,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Données pour la HeatMap
 */
private data class HeatMapData(
        val nutriments: List<NutrimentHeatMap>,
        val aliments: List<AlimentHeatMap>,
        val valeurs: Map<Nutrient, Map<String, Double>> // nutriment -> (alimentUuid -> ratio)
)

/**
 * Données pour un nutriment dans la HeatMap
 */
private data class NutrimentHeatMap(
        val nutriment: Nutrient,
        val nom: String,
        val niveau: Reflevel,
        val valeurReferenceDisplay: String = "" // Valeur de référence formatée (ex: "2.5 g/1000kcal")
)

/**
 * Données pour un aliment dans la HeatMap
 */
private data class AlimentHeatMap(
        val aliment: AlimentEv,
        val nom: String
)

/**
 * Calcule les données de la HeatMap
 */
private fun calculerHeatMapData(
        alimentsAnalyses: List<AlimentAnalyseData>,
        referenceEv: ReferenceEv?,
        equationRepository: EquationRepository?,
        preferencesEspece: fr.vetbrain.vetnutri_mp.Data.PreferencesEspece?,
        besoinEnergetiqueEntretien: Double?,
        poidsAnimal: Double?,
        poidsMetabolique: Double?
): HeatMapData {
    if (referenceEv == null || besoinEnergetiqueEntretien == null || besoinEnergetiqueEntretien <= 0) {
        return HeatMapData(emptyList(), emptyList(), emptyMap())
    }
    
    // Récupérer tous les nutriments avec MIN ou OPTIMIN dans la référence
    val nutrimentsAvecReference = mutableListOf<NutrimentHeatMap>()
    
    // Fonction locale pour calculer et formater la valeur de référence
    fun calculerEtFormaterReference(nutriment: Nutrient, niveau: Reflevel): String {
        val valeurRef = referenceEv.obtenirNutriment(nutriment, niveau)
        if (valeurRef < 0) return ""
        
        val uniteRefId = referenceEv.obtenirUniteNutriment(nutriment, niveau)
        val uniteRef = UnitReqEnum.getById(uniteRefId)
        
        val besoinAbsolu = calculerBesoinAbsoluHeatMap(
                valeurRef = valeurRef,
                uniteRef = uniteRef,
                besoinEnergetiqueEntretien = besoinEnergetiqueEntretien,
                poidsAnimal = poidsAnimal,
                poidsMetabolique = poidsMetabolique
        ) ?: return ""
        
        val besoinPer1000Kcal = (besoinAbsolu * 1000.0) / besoinEnergetiqueEntretien
        
        // Utiliser l'unité définie dans l'enum du nutriment
        val uniteAffichage = nutriment.unite
        
        return "${GraphFormattingUtils.formatDecimal(besoinPer1000Kcal, 2)} $uniteAffichage"
    }
    
    // Parcourir tous les types de nutriments possibles
    val tousLesNutriments = mutableListOf<Pair<Any, String>>()
    
    // Nutriments principaux
    NutrientMain.values().forEach { nutriment ->
        val nom = nutriment.translateEnum()
        if (referenceEv.contientNutriment(nutriment, Reflevel.MIN) ||
                referenceEv.contientNutriment(nutriment, Reflevel.OPTIMIN)) {
            // Prioriser OPTIMIN si les deux existent, sinon utiliser celui qui existe
            val niveau = if (referenceEv.contientNutriment(nutriment, Reflevel.OPTIMIN)) {
                Reflevel.OPTIMIN
            } else {
                Reflevel.MIN
            }
            val valeurDisplay = calculerEtFormaterReference(nutriment, niveau)
            nutrimentsAvecReference.add(NutrimentHeatMap(nutriment, nom, niveau, valeurDisplay))
        }
    }
    
    // Macronutriments
    NutrientMacro.values().forEach { nutriment ->
        val nom = nutriment.translateEnum()
        if (referenceEv.contientNutriment(nutriment, Reflevel.MIN) ||
                referenceEv.contientNutriment(nutriment, Reflevel.OPTIMIN)) {
            // Prioriser OPTIMIN si les deux existent, sinon utiliser celui qui existe
            val niveau = if (referenceEv.contientNutriment(nutriment, Reflevel.OPTIMIN)) {
                Reflevel.OPTIMIN
            } else {
                Reflevel.MIN
            }
            val valeurDisplay = calculerEtFormaterReference(nutriment, niveau)
            nutrimentsAvecReference.add(NutrimentHeatMap(nutriment, nom, niveau, valeurDisplay))
        }
    }
    
    // Oligo-éléments
    NutrientMin.values().forEach { nutriment ->
        val nom = nutriment.translateEnum()
        if (referenceEv.contientNutriment(nutriment, Reflevel.MIN) ||
                referenceEv.contientNutriment(nutriment, Reflevel.OPTIMIN)) {
            // Prioriser OPTIMIN si les deux existent, sinon utiliser celui qui existe
            val niveau = if (referenceEv.contientNutriment(nutriment, Reflevel.OPTIMIN)) {
                Reflevel.OPTIMIN
            } else {
                Reflevel.MIN
            }
            val valeurDisplay = calculerEtFormaterReference(nutriment, niveau)
            nutrimentsAvecReference.add(NutrimentHeatMap(nutriment, nom, niveau, valeurDisplay))
        }
    }
    
    // Vitamines
    NutrientVitam.values().forEach { nutriment ->
        val nom = nutriment.translateEnum()
        if (referenceEv.contientNutriment(nutriment, Reflevel.MIN) ||
                referenceEv.contientNutriment(nutriment, Reflevel.OPTIMIN)) {
            // Prioriser OPTIMIN si les deux existent, sinon utiliser celui qui existe
            val niveau = if (referenceEv.contientNutriment(nutriment, Reflevel.OPTIMIN)) {
                Reflevel.OPTIMIN
            } else {
                Reflevel.MIN
            }
            val valeurDisplay = calculerEtFormaterReference(nutriment, niveau)
            nutrimentsAvecReference.add(NutrimentHeatMap(nutriment, nom, niveau, valeurDisplay))
        }
    }
    
    // Acides gras
    NutrientLipid.values().forEach { nutriment ->
        val nom = nutriment.translateEnum()
        if (referenceEv.contientNutriment(nutriment, Reflevel.MIN) ||
                referenceEv.contientNutriment(nutriment, Reflevel.OPTIMIN)) {
            // Prioriser OPTIMIN si les deux existent, sinon utiliser celui qui existe
            val niveau = if (referenceEv.contientNutriment(nutriment, Reflevel.OPTIMIN)) {
                Reflevel.OPTIMIN
            } else {
                Reflevel.MIN
            }
            val valeurDisplay = calculerEtFormaterReference(nutriment, niveau)
            nutrimentsAvecReference.add(NutrimentHeatMap(nutriment, nom, niveau, valeurDisplay))
        }
    }
    
    // Acides aminés
    AAEnum.values().forEach { nutriment ->
        val nom = nutriment.translateEnum()
        if (referenceEv.contientNutriment(nutriment, Reflevel.MIN) ||
                referenceEv.contientNutriment(nutriment, Reflevel.OPTIMIN)) {
            // Prioriser OPTIMIN si les deux existent, sinon utiliser celui qui existe
            val niveau = if (referenceEv.contientNutriment(nutriment, Reflevel.OPTIMIN)) {
                Reflevel.OPTIMIN
            } else {
                Reflevel.MIN
            }
            val valeurDisplay = calculerEtFormaterReference(nutriment, niveau)
            nutrimentsAvecReference.add(NutrimentHeatMap(nutriment, nom, niveau, valeurDisplay))
        }
    }
    
    // Autres nutriments
    NutrientOther.values().forEach { nutriment ->
        val nom = nutriment.translateEnum()
        if (referenceEv.contientNutriment(nutriment, Reflevel.MIN) ||
                referenceEv.contientNutriment(nutriment, Reflevel.OPTIMIN)) {
            // Prioriser OPTIMIN si les deux existent, sinon utiliser celui qui existe
            val niveau = if (referenceEv.contientNutriment(nutriment, Reflevel.OPTIMIN)) {
                Reflevel.OPTIMIN
            } else {
                Reflevel.MIN
            }
            val valeurDisplay = calculerEtFormaterReference(nutriment, niveau)
            nutrimentsAvecReference.add(NutrimentHeatMap(nutriment, nom, niveau, valeurDisplay))
        }
    }
    
    // Créer la liste des aliments
    val alimentsHeatMap = alimentsAnalyses.map { data ->
        AlimentHeatMap(
                aliment = data.aliment,
                nom = data.aliment.nom ?: "Aliment ${data.numero}"
        )
    }
    
    // Calculer les valeurs pour chaque combinaison nutriment/aliment
    val valeursMap = mutableMapOf<Nutrient, MutableMap<String, Double>>()
    
    nutrimentsAvecReference.forEach { nutrimentData ->
        val valeursAliment = mutableMapOf<String, Double>()
        
        alimentsAnalyses.forEach { alimentData ->
            val ratio = runBlocking {
                calculerRatioHeatMap(
                        aliment = alimentData.aliment,
                        nutriment = nutrimentData.nutriment,
                        niveau = nutrimentData.niveau,
                        referenceEv = referenceEv,
                        equationRepository = equationRepository,
                        preferencesEspece = preferencesEspece,
                        besoinEnergetiqueEntretien = besoinEnergetiqueEntretien,
                        poidsAnimal = poidsAnimal,
                        poidsMetabolique = poidsMetabolique,
                        densiteEnergetique = alimentData.densiteEnergetique
                )
            }
            valeursAliment[alimentData.aliment.uuid] = ratio
        }
        
        valeursMap[nutrimentData.nutriment] = valeursAliment
    }
    
    // Filtrer les nutriments qui n'ont aucune valeur > 0 dans aucun aliment
    val nutrimentsFiltres = nutrimentsAvecReference.filter { nutrimentData ->
        val valeurs = valeursMap[nutrimentData.nutriment] ?: emptyMap()
        valeurs.values.any { it > 0.0 }
    }
    
    // Filtrer aussi les valeurs pour ne garder que celles des nutriments filtrés
    val valeursMapFiltree = valeursMap.filterKeys { nutriment ->
        nutrimentsFiltres.any { it.nutriment == nutriment }
    }
    
    return HeatMapData(
            nutriments = nutrimentsFiltres,
            aliments = alimentsHeatMap,
            valeurs = valeursMapFiltree
    )
}

/**
 * Calcule le ratio pour un nutriment dans un aliment
 * Ratio = (concentration dans l'aliment en g/1000kcal) / (besoin en g/1000kcal BEE)
 */
private suspend fun calculerRatioHeatMap(
        aliment: AlimentEv,
        nutriment: Nutrient,
        niveau: Reflevel,
        referenceEv: ReferenceEv,
        equationRepository: EquationRepository?,
        preferencesEspece: fr.vetbrain.vetnutri_mp.Data.PreferencesEspece?,
        besoinEnergetiqueEntretien: Double,
        poidsAnimal: Double?,
        poidsMetabolique: Double?,
        densiteEnergetique: Double
): Double {
    // 1. Obtenir la concentration du nutriment dans l'aliment en g/1000kcal
    val alimentRation = AlimentRation(
            aliment = aliment,
            quantite = 100.0,
            weight = 1.0
    )
    
    // Obtenir la valeur brute du nutriment (en % de protéines pour les acides aminés)
    val valeurBruteNutriment = when (nutriment) {
        is NutrientMain -> alimentRation.getNutrientWithComplementary(
                nutriment, preferencesEspece, equationRepository, referenceEv
        ) ?: 0.0
        is NutrientMacro -> alimentRation.getNutrientWithComplementary(
                nutriment, preferencesEspece, equationRepository, referenceEv
        ) ?: 0.0
        is NutrientMin -> alimentRation.getNutrientWithComplementary(
                nutriment, preferencesEspece, equationRepository, referenceEv
        ) ?: 0.0
        is NutrientVitam -> alimentRation.getNutrientWithComplementary(
                nutriment, preferencesEspece, equationRepository, referenceEv
        ) ?: 0.0
        is NutrientLipid -> alimentRation.getNutrientWithComplementary(
                nutriment, preferencesEspece, equationRepository, referenceEv
        ) ?: 0.0
        is AAEnum -> {
            // Pour les acides aminés, la valeur est en % de protéines, il faut convertir en valeur absolue
            // (même logique que dans RationNutrientAnalyzer.kt ligne 313-316)
            val valeurPourcentProteines = alimentRation.getNutrientWithComplementary(
                    nutriment, preferencesEspece, equationRepository, referenceEv
            ) ?: 0.0
            val teneurProteines = alimentRation.aliment?.getNutrient(NutrientMain.PROTEINE) ?: 0.0
            // Convertir de % de protéines vers g/100g d'aliment
            (valeurPourcentProteines * teneurProteines) / 100.0
        }
        is NutrientOther -> alimentRation.getNutrientWithComplementary(
                nutriment, preferencesEspece, equationRepository, referenceEv
        ) ?: 0.0
        else -> 0.0
    }
    
    // Convertir en g/1000kcal
    val concentrationPer1000Kcal = if (densiteEnergetique > 0) {
        (valeurBruteNutriment * 1000.0) / densiteEnergetique
    } else {
        0.0
    }
    
    // 2. Obtenir le besoin de référence et le convertir en g/1000kcal BEE
    val valeurRef = referenceEv.obtenirNutriment(nutriment, niveau)
    if (valeurRef < 0) return 0.0 // -1.0 signifie que le nutriment n'est pas trouvé
    
    val uniteRefId = referenceEv.obtenirUniteNutriment(nutriment, niveau)
    val uniteRef = UnitReqEnum.getById(uniteRefId)
    
    // Convertir le besoin en valeur absolue
    val besoinAbsolu = calculerBesoinAbsoluHeatMap(
            valeurRef = valeurRef,
            uniteRef = uniteRef,
            besoinEnergetiqueEntretien = besoinEnergetiqueEntretien,
            poidsAnimal = poidsAnimal,
            poidsMetabolique = poidsMetabolique
    ) ?: return 0.0
    
    // Convertir le besoin absolu en g/1000kcal BEE
    val besoinPer1000Kcal = (besoinAbsolu * 1000.0) / besoinEnergetiqueEntretien
    
    // 3. Calculer le ratio
    return if (besoinPer1000Kcal > 0) {
        concentrationPer1000Kcal / besoinPer1000Kcal
    } else {
        0.0
    }
}

/**
 * Calcule le besoin absolu à partir d'une valeur de référence et de son unité
 * (identique à la fonction dans cardNutrient.kt)
 */
private fun calculerBesoinAbsoluHeatMap(
        valeurRef: Double,
        uniteRef: UnitReqEnum,
        besoinEnergetiqueEntretien: Double,
        poidsAnimal: Double?,
        poidsMetabolique: Double?
): Double? {
    return when (uniteRef) {
        // Basé sur l'énergie (par 1000 kcal)
        UnitReqEnum.PERKCAL -> {
            (valeurRef * besoinEnergetiqueEntretien) / 1000.0
        }
        // Basé sur l'énergie (par 1000 kJ) - conversion en kcal puis calcul
        UnitReqEnum.PERKJ -> {
            val beeEnKj = besoinEnergetiqueEntretien * 4.184
            (valeurRef * beeEnKj) / 1000.0
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
            valeurRef
        }
        // Ratio - pas de calcul absolu possible
        UnitReqEnum.RATIO -> null
    }
}

/**
 * Calcule la couleur pour une valeur de ratio dans la HeatMap
 */
private fun calculerCouleurHeatMap(ratio: Double): Color {
    return when {
        ratio <= 0.0 -> Color.Transparent // Pas de données
        ratio < 0.8 -> Color(0xFFF44336) // Rouge - carence importante
        ratio < 1.0 -> Color(0xFFFFEB3B) // Jaune - carence modérée
        else -> Color(0xFF4CAF50) // Vert - besoin satisfait
    }
}

