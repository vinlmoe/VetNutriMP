package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
        
        // Légende des couleurs
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
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
            // Jaune (0.5 <= ratio < 1.0)
            Box(
                    modifier = Modifier
                            .size(16.dp)
                            .background(Color(0xFFFFEB3B))
            )
            Text(
                    text = " 0.5-1.0",
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(start = 4.dp)
            )
            Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
            // Rouge (ratio < 0.5)
            Box(
                    modifier = Modifier
                            .size(16.dp)
                            .background(Color(0xFFF44336))
            )
            Text(
                    text = " <0.5",
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(start = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
        
        // Tableau HeatMap avec scroll horizontal et vertical
        Box(
                modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colors.surface)
        ) {
            LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // En-tête fixe avec les noms des aliments
                item {
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        // Cellule vide pour le coin supérieur gauche
                        Box(
                                modifier = Modifier
                                        .width(150.dp)
                                        .height(48.dp)
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
                        
                        // En-têtes des colonnes (aliments)
                        heatMapData.aliments.forEach { alimentData ->
                            Box(
                                    modifier = Modifier
                                            .width(120.dp)
                                            .height(48.dp)
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
                }
                
                // Lignes de données (nutriments)
                items(heatMapData.nutriments) { nutrimentData ->
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        // Nom du nutriment (en-tête de ligne)
                        Box(
                                modifier = Modifier
                                        .width(150.dp)
                                        .height(40.dp)
                                        .background(MaterialTheme.colors.surface)
                                        .padding(AppSizes.paddingXSmall),
                                contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                    text = nutrimentData.nom,
                                    style = MaterialTheme.typography.caption,
                                    maxLines = 1
                            )
                        }
                        
                        // Valeurs pour chaque aliment
                        heatMapData.aliments.forEach { alimentData ->
                            val valeur = heatMapData.valeurs[nutrimentData.nutriment]?.get(alimentData.aliment.uuid) ?: 0.0
                            val couleur = calculerCouleurHeatMap(valeur)
                            
                            Box(
                                    modifier = Modifier
                                            .width(120.dp)
                                            .height(40.dp)
                                            .background(couleur)
                                            .padding(AppSizes.paddingXSmall),
                                    contentAlignment = Alignment.Center
                            ) {
                                Text(
                                        text = if (valeur > 0) {
                                            GraphFormattingUtils.formatDecimal(valeur, 2)
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
        val niveau: Reflevel
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
    
    // Parcourir tous les types de nutriments possibles
    val tousLesNutriments = mutableListOf<Pair<Any, String>>()
    
    // Nutriments principaux
    NutrientMain.values().forEach { nutriment ->
        val nom = nutriment.translateEnum()
        if (referenceEv.contientNutriment(nutriment, Reflevel.MIN) ||
                referenceEv.contientNutriment(nutriment, Reflevel.OPTIMIN)) {
            val niveau = if (referenceEv.contientNutriment(nutriment, Reflevel.MIN)) {
                Reflevel.MIN
            } else {
                Reflevel.OPTIMIN
            }
            nutrimentsAvecReference.add(NutrimentHeatMap(nutriment, nom, niveau))
        }
    }
    
    // Macronutriments
    NutrientMacro.values().forEach { nutriment ->
        val nom = nutriment.translateEnum()
        if (referenceEv.contientNutriment(nutriment, Reflevel.MIN) ||
                referenceEv.contientNutriment(nutriment, Reflevel.OPTIMIN)) {
            val niveau = if (referenceEv.contientNutriment(nutriment, Reflevel.MIN)) {
                Reflevel.MIN
            } else {
                Reflevel.OPTIMIN
            }
            nutrimentsAvecReference.add(NutrimentHeatMap(nutriment, nom, niveau))
        }
    }
    
    // Oligo-éléments
    NutrientMin.values().forEach { nutriment ->
        val nom = nutriment.translateEnum()
        if (referenceEv.contientNutriment(nutriment, Reflevel.MIN) ||
                referenceEv.contientNutriment(nutriment, Reflevel.OPTIMIN)) {
            val niveau = if (referenceEv.contientNutriment(nutriment, Reflevel.MIN)) {
                Reflevel.MIN
            } else {
                Reflevel.OPTIMIN
            }
            nutrimentsAvecReference.add(NutrimentHeatMap(nutriment, nom, niveau))
        }
    }
    
    // Vitamines
    NutrientVitam.values().forEach { nutriment ->
        val nom = nutriment.translateEnum()
        if (referenceEv.contientNutriment(nutriment, Reflevel.MIN) ||
                referenceEv.contientNutriment(nutriment, Reflevel.OPTIMIN)) {
            val niveau = if (referenceEv.contientNutriment(nutriment, Reflevel.MIN)) {
                Reflevel.MIN
            } else {
                Reflevel.OPTIMIN
            }
            nutrimentsAvecReference.add(NutrimentHeatMap(nutriment, nom, niveau))
        }
    }
    
    // Acides gras
    NutrientLipid.values().forEach { nutriment ->
        val nom = nutriment.translateEnum()
        if (referenceEv.contientNutriment(nutriment, Reflevel.MIN) ||
                referenceEv.contientNutriment(nutriment, Reflevel.OPTIMIN)) {
            val niveau = if (referenceEv.contientNutriment(nutriment, Reflevel.MIN)) {
                Reflevel.MIN
            } else {
                Reflevel.OPTIMIN
            }
            nutrimentsAvecReference.add(NutrimentHeatMap(nutriment, nom, niveau))
        }
    }
    
    // Acides aminés
    AAEnum.values().forEach { nutriment ->
        val nom = nutriment.translateEnum()
        if (referenceEv.contientNutriment(nutriment, Reflevel.MIN) ||
                referenceEv.contientNutriment(nutriment, Reflevel.OPTIMIN)) {
            val niveau = if (referenceEv.contientNutriment(nutriment, Reflevel.MIN)) {
                Reflevel.MIN
            } else {
                Reflevel.OPTIMIN
            }
            nutrimentsAvecReference.add(NutrimentHeatMap(nutriment, nom, niveau))
        }
    }
    
    // Autres nutriments
    NutrientOther.values().forEach { nutriment ->
        val nom = nutriment.translateEnum()
        if (referenceEv.contientNutriment(nutriment, Reflevel.MIN) ||
                referenceEv.contientNutriment(nutriment, Reflevel.OPTIMIN)) {
            val niveau = if (referenceEv.contientNutriment(nutriment, Reflevel.MIN)) {
                Reflevel.MIN
            } else {
                Reflevel.OPTIMIN
            }
            nutrimentsAvecReference.add(NutrimentHeatMap(nutriment, nom, niveau))
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
    
    return HeatMapData(
            nutriments = nutrimentsAvecReference,
            aliments = alimentsHeatMap,
            valeurs = valeursMap
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
    
    val concentrationNutriment = when (nutriment) {
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
        is AAEnum -> alimentRation.getNutrientWithComplementary(
                nutriment, preferencesEspece, equationRepository, referenceEv
        ) ?: 0.0
        is NutrientOther -> alimentRation.getNutrientWithComplementary(
                nutriment, preferencesEspece, equationRepository, referenceEv
        ) ?: 0.0
        else -> 0.0
    }
    
    // Convertir en g/1000kcal
    val concentrationPer1000Kcal = if (densiteEnergetique > 0) {
        (concentrationNutriment * 1000.0) / densiteEnergetique
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
        ratio < 0.5 -> Color(0xFFF44336) // Rouge - carence importante
        ratio < 1.0 -> Color(0xFFFFEB3B) // Jaune - carence modérée
        else -> Color(0xFF4CAF50) // Vert - besoin satisfait
    }
}

