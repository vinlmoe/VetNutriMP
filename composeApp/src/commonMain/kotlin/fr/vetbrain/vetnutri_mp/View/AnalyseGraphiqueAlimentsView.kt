package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import io.github.koalaplot.core.*
import io.github.koalaplot.core.line.LinePlot
import io.github.koalaplot.core.xygraph.*
import io.github.koalaplot.core.xygraph.FloatLinearAxisModel
import io.github.koalaplot.core.xygraph.Point
import io.github.koalaplot.core.xygraph.XYGraph

import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.bar.DefaultVerticalBar
import io.github.koalaplot.core.bar.VerticalBarPlot

/**
 * Données calculées pour un aliment avec sa densité énergétique et pourcentages
 */
data class AlimentAnalyseData(
    val aliment: AlimentEv,
    val numero: Int,
    val densiteEnergetique: Double,
    val pourcentageProteines: Double,
    val pourcentageLipides: Double,
    val phosphorePer1000Kcal: Double = 0.0,
    val proteinePer1000Kcal: Double = 0.0
)

/**
 * Calcule la densité énergétique d'un aliment de manière asynchrone
 */
private suspend fun calculerDensiteEnergetiqueAsync(
    aliment: AlimentEv,
    referenceEv: ReferenceEv?,
    equationRepository: EquationRepository?,
    preferencesEspece: fr.vetbrain.vetnutri_mp.Data.PreferencesEspece?
): Double {
    // ✅ UTILISER LA MÊME APPROCHE QUE RATIONSVIEW : AlimentRation transitoire
    if (referenceEv != null && equationRepository != null) {
        try {
            val alimentRation = AlimentRation(
                aliment = aliment,
                quantite = 100.0,
                weight = 1.0
            )
            
            val energie = alimentRation.getNutrientWithComplementary(
                nutrient = NutrientMain.ENERGIE,
                preferences = null,
                equationRepository = equationRepository,
                referenceEv = referenceEv
            )
            if (energie != null && energie > 0) {
                return energie
            }
        } catch (e: Exception) {
            // Fallback sur la méthode simple
        }
    }
    
    // Méthode simple : calcul basé sur les macronutriments
    val alimentRation = AlimentRation(
        aliment = aliment,
        quantite = 100.0,
        weight = 1.0
    )
    
    val proteines = alimentRation.getNutrientWithComplementary(
        nutrient = NutrientMain.PROTEINE,
        preferences = preferencesEspece,
        equationRepository = equationRepository,
        referenceEv = referenceEv
    ) ?: 0.0
    
    val lipides = alimentRation.getNutrientWithComplementary(
        nutrient = NutrientMain.LIPIDE,
        preferences = preferencesEspece,
        equationRepository = equationRepository,
        referenceEv = referenceEv
    ) ?: 0.0
    
    val glucides = alimentRation.getNutrientWithComplementary(
        nutrient = NutrientMain.GLUCIDE,
        preferences = preferencesEspece,
        equationRepository = equationRepository,
        referenceEv = referenceEv
    ) ?: 0.0
    
    // Coefficients énergétiques (kcal/g)
    val kcalProteines = proteines * 3.5
    val kcalLipides = lipides * 8.5
    val kcalGlucides = glucides * 3.5
    
    return kcalProteines + kcalLipides + kcalGlucides
}

/**
 * Calcule le pourcentage d'énergie apporté par les protéines de manière asynchrone
 */
private suspend fun calculerPourcentageEnergieProteinesAsync(
    aliment: AlimentEv,
    densiteEnergetique: Double,
    equationRepository: EquationRepository?,
    preferencesEspece: fr.vetbrain.vetnutri_mp.Data.PreferencesEspece?
): Double {
    if (densiteEnergetique <= 0) return 0.0
    
    // ✅ UTILISER LA MÊME APPROCHE QUE RATIONSVIEW : AlimentRation transitoire
    val alimentRation = AlimentRation(
        aliment = aliment,
        quantite = 100.0,
        weight = 1.0
    )
    
    val proteines = alimentRation.getNutrientWithComplementary(
        nutrient = NutrientMain.PROTEINE,
        preferences = preferencesEspece,
        equationRepository = equationRepository,
        referenceEv = null
    ) ?: 0.0
    val energieProteines = proteines * 3.5
    
    return (energieProteines / densiteEnergetique) * 100.0
}

/**
 * Calcule le pourcentage d'énergie apporté par les lipides de manière asynchrone
 */
private suspend fun calculerPourcentageEnergieLipidesAsync(
    aliment: AlimentEv,
    densiteEnergetique: Double,
    equationRepository: EquationRepository?,
    preferencesEspece: fr.vetbrain.vetnutri_mp.Data.PreferencesEspece?
): Double {
    if (densiteEnergetique <= 0) return 0.0
    
    // ✅ UTILISER LA MÊME APPROCHE QUE RATIONSVIEW : AlimentRation transitoire
    val alimentRation = AlimentRation(
        aliment = aliment,
        quantite = 100.0,
        weight = 1.0
    )
    
    val lipides = alimentRation.getNutrientWithComplementary(
        nutrient = NutrientMain.LIPIDE,
        preferences = preferencesEspece,
        equationRepository = equationRepository,
        referenceEv = null
    ) ?: 0.0
    val energieLipides = lipides * 8.5
    
    return (energieLipides / densiteEnergetique) * 100.0
}

/**
 * Vue d'analyse graphique des aliments sélectionnés
 */
@Composable
fun AnalyseGraphiqueAlimentsView(
    aliments: List<AlimentEv>,
    referenceEv: ReferenceEv?,
    equationRepository: EquationRepository?,
    preferencesEspece: fr.vetbrain.vetnutri_mp.Data.PreferencesEspece? = null,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    // États pour les données d'analyse
    var alimentsAnalyses by remember { mutableStateOf<List<AlimentAnalyseData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    // État pour l'aliment sélectionné (UUID de l'aliment)
    var alimentSelectionne by remember { mutableStateOf<String?>(null) }
    
    // État pour l'onglet actif
    var ongletActif by remember { mutableStateOf("protein_lipid") }
    
    // Calculer les données d'analyse pour chaque aliment de manière asynchrone
    LaunchedEffect(aliments, referenceEv, equationRepository) {
        isLoading = true
        val resultat = mutableListOf<AlimentAnalyseData>()
        
        for ((index, aliment) in aliments.withIndex()) {
            try {
                // 🔍 LOG DIAGNOSTIC : Vérifier les valeurs de base de l'aliment
                println("🔍 DIAGNOSTIC GRAPHIQUE: Aliment ${index + 1} - ${aliment.nom}")
                println("  - UUID: ${aliment.uuid}")
                println("  - Nom: ${aliment.nom}")
                println("  - Marque: ${aliment.brand}")
                println("  - Gamme: ${aliment.gamme}")
                println("  - Valeurs stockées dans valMap:")
                println("    * Protéines stockées: ${aliment.valMap[NutrientMain.PROTEINE]?.value ?: "null"}")
                println("    * Lipides stockés: ${aliment.valMap[NutrientMain.LIPIDE]?.value ?: "null"}")
                println("    * Glucides stockés: ${aliment.valMap[NutrientMain.GLUCIDE]?.value ?: "null"}")
                println("    * Énergie stockée: ${aliment.valMap[NutrientMain.ENERGIE]?.value ?: "null"}")
                println("  - Nombre total de nutriments dans valMap: ${aliment.valMap.size}")
                println("  - ReferenceEv: ${referenceEv?.nom ?: "null"}")
                println("  - EquationRepository: ${if (equationRepository != null) "disponible" else "null"}")
                println("  - PreferencesEspece: ${if (preferencesEspece != null) "disponible" else "null"}")
                
                // ✅ CRÉER UN ALIMENTRATION TRANSITOIRE POUR UTILISER getNutrientWithComplementary
                val alimentRation = AlimentRation(
                    aliment = aliment,
                    quantite = 100.0, // 100g pour les calculs
                    weight = 1.0
                )
                
                // Utiliser getNutrientWithComplementary de manière asynchrone
                val proteines = alimentRation.getNutrientWithComplementary(
                    nutrient = NutrientMain.PROTEINE,
                    preferences = preferencesEspece,
                    equationRepository = equationRepository,
                    referenceEv = referenceEv
                ) ?: 0.0
                
                val lipides = alimentRation.getNutrientWithComplementary(
                    nutrient = NutrientMain.LIPIDE,
                    preferences = preferencesEspece,
                    equationRepository = equationRepository,
                    referenceEv = referenceEv
                ) ?: 0.0
                
                val glucides = alimentRation.getNutrientWithComplementary(
                    nutrient = NutrientMain.GLUCIDE,
                    preferences = preferencesEspece,
                    equationRepository = equationRepository,
                    referenceEv = referenceEv
                ) ?: 0.0
                
                val energie = alimentRation.getNutrientWithComplementary(
                    nutrient = NutrientMain.ENERGIE,
                    preferences = preferencesEspece,
                    equationRepository = equationRepository,
                    referenceEv = referenceEv
                ) ?: 0.0
                
                // Récupération du phosphore pour le second graphique
                val phosphore = alimentRation.getNutrientWithComplementary(
                    nutrient = NutrientMacro.PHOS,
                    preferences = preferencesEspece,
                    equationRepository = equationRepository,
                    referenceEv = referenceEv
                ) ?: 0.0
                
                println("  - Protéines: $proteines g")
                println("  - Lipides: $lipides g")
                println("  - Glucides: $glucides g")
                println("  - Énergie: $energie kcal")
                println("  - Phosphore: $phosphore mg")
                
                val densiteEnergetique = calculerDensiteEnergetiqueAsync(aliment, referenceEv, equationRepository, preferencesEspece)
                val pourcentageProteines = calculerPourcentageEnergieProteinesAsync(aliment, densiteEnergetique, equationRepository, preferencesEspece)
                val pourcentageLipides = calculerPourcentageEnergieLipidesAsync(aliment, densiteEnergetique, equationRepository, preferencesEspece)
                
                // Calculs pour le graphique Phosphore/Protéines (par 1000 kcal)
                val proteinePer1000Kcal = if (densiteEnergetique > 0) {
                    (proteines * 1000.0) / densiteEnergetique
                } else {
                    0.0
                }
                
                val phosphorePer1000Kcal = if (densiteEnergetique > 0) {
                    (phosphore * 1000.0) / densiteEnergetique  
                } else {
                    0.0
                }
                
                // 🔍 LOG DIAGNOSTIC : Vérifier les valeurs calculées
                println("  - Densité énergétique: $densiteEnergetique kcal/100g")
                println("  - % Protéines: $pourcentageProteines%")
                println("  - % Lipides: $pourcentageLipides%")
                println("  - Protéines/1000kcal: $proteinePer1000Kcal g")
                println("  - Phosphore/1000kcal: $phosphorePer1000Kcal mg")
                println("  - Point graphique: ($pourcentageProteines, $pourcentageLipides)")
                
                resultat.add(AlimentAnalyseData(
                    aliment = aliment,
                    numero = 0, // Numéro temporaire, sera réassigné après le tri
                    densiteEnergetique = densiteEnergetique,
                    pourcentageProteines = pourcentageProteines,
                    pourcentageLipides = pourcentageLipides,
                    phosphorePer1000Kcal = phosphorePer1000Kcal,
                    proteinePer1000Kcal = proteinePer1000Kcal
                ))
            } catch (e: Exception) {
                println("❌ DIAGNOSTIC GRAPHIQUE: Erreur pour l'aliment ${index + 1}: ${e.message}")
                e.printStackTrace()
            }
        }
        
        // Mettre à jour l'état avec les résultats triés par densité énergétique décroissante
        // et réassigner les numéros dans l'ordre du tri
        alimentsAnalyses = resultat.sortedByDescending { it.densiteEnergetique }
            .mapIndexed { index, data ->
                data.copy(numero = index + 1)
            }
        isLoading = false
    }

    Column(
        modifier = modifier.fillMaxSize().padding(AppSizes.paddingMedium),
        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
    ) {
        // En-tête avec titre et bouton de fermeture
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Retour à la sélection d'aliments",
                        tint = VetNutriColors.Primary
                    )
                }
                Column {
                    Text(
                        text = "Analyse graphique de ${aliments.size} aliment(s)",
                        style = MaterialTheme.typography.h5,
                        fontWeight = FontWeight.Bold,
                        color = VetNutriColors.Primary
                    )
                    Text(
                        text = "Visualisation des caractéristiques nutritionnelles",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Onglets pour choisir le type de graphique
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Onglet Protéines/Lipides
            Button(
                onClick = { ongletActif = "protein_lipid" },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (ongletActif == "protein_lipid") VetNutriColors.Primary else Color.Gray.copy(alpha = 0.3f),
                    contentColor = if (ongletActif == "protein_lipid") Color.White else Color.Black
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Protéines/Lipides (%)")
            }
            
            // Onglet Phosphore/Protéines
            Button(
                onClick = { ongletActif = "phosphore_protein" },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (ongletActif == "phosphore_protein") VetNutriColors.Primary else Color.Gray.copy(alpha = 0.3f),
                    contentColor = if (ongletActif == "phosphore_protein") Color.White else Color.Black
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Phosphore/Protéines\n(par 1000 kcal)")
            }
            
            // ✨ Onglet Histogramme Densité Énergétique
            Button(
                onClick = { ongletActif = "densite_energetique" },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (ongletActif == "densite_energetique") VetNutriColors.Primary else Color.Gray.copy(alpha = 0.3f),
                    contentColor = if (ongletActif == "densite_energetique") Color.White else Color.Black
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Densité énergétique\n(histogramme)")
            }
        }

        // Contenu principal - responsive selon la largeur
        if (isLoading) {
            // Indicateur de chargement
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                ) {
                    CircularProgressIndicator(
                        color = VetNutriColors.Primary
                    )
                    Text(
                        text = "Calcul des valeurs nutritionnelles...",
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val isCompact = maxWidth < 800.dp
            
            if (isCompact) {
                // Vue compacte : graphiques puis liste avec bouton retour plus visible ET SCROLLABLE
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()), // ✨ Rendre la vue principale scrollable
                    verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                ) {
                    // 🔧 Bouton retour plus visible en mode compact
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = VetNutriColors.Primary.copy(alpha = 0.1f),
                        elevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(AppSizes.paddingSmall),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                        ) {
                            IconButton(
                                onClick = onClose,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Retour à la sélection",
                                    tint = VetNutriColors.Primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = "← Retour à la sélection d'aliments",
                                style = MaterialTheme.typography.body2,
                                color = VetNutriColors.Primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    // Graphique principal
                    GraphiqueNuagePoints(
                        alimentsAnalyses = alimentsAnalyses,
                        ongletActif = ongletActif,
                        alimentSelectionne = alimentSelectionne, 
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Liste des aliments (sans LazyColumn en mode compact)
                    ListeAlimentsAnalyse(
                        alimentsAnalyses = alimentsAnalyses, 
                        alimentSelectionne = alimentSelectionne,
                        onAlimentSelected = { uuid -> alimentSelectionne = uuid },
                        isCompactMode = true, // ✨ Mode compact = pas de LazyColumn
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                // Vue large : côte à côte
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                ) {
                    // Colonne gauche : liste des aliments (1/4 de la largeur)
                    Column(
                        modifier = Modifier.weight(0.25f),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                    ) {
                        ListeAlimentsAnalyse(
                            alimentsAnalyses = alimentsAnalyses, 
                            alimentSelectionne = alimentSelectionne,
                            onAlimentSelected = { uuid -> alimentSelectionne = uuid },
                            isCompactMode = false, // ✨ Mode large = avec LazyColumn
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    // Colonne droite : graphiques (3/4 de la largeur)
                    Column(
                        modifier = Modifier.weight(0.75f),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                    ) {
                        GraphiqueNuagePoints(
                            alimentsAnalyses = alimentsAnalyses,
                            ongletActif = ongletActif,
                            alimentSelectionne = alimentSelectionne, 
                            modifier = Modifier.fillMaxWidth()
                        )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Graphique en nuage de points : % énergie protéines vs % énergie lipides
 */
@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
private fun GraphiqueNuagePoints(
    alimentsAnalyses: List<AlimentAnalyseData>,
    ongletActif: String,
    alimentSelectionne: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = AppSizes.elevationMedium
    ) {
        Column(
            modifier = Modifier.padding(AppSizes.paddingMedium)
        ) {
            // Titre dynamique selon l'onglet
            val titre = when (ongletActif) {
                "protein_lipid" -> "Répartition énergétique : Protéines vs Lipides"
                "phosphore_protein" -> "Phosphore vs Protéines (par 1000 kcal)"
                else -> "Analyse nutritionnelle"
            }
            
            Text(
                text = titre,
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                color = VetNutriColors.Primary
            )
            
            Text(
                text = "Chaque point représente un aliment (numéroté de 1 à ${alimentsAnalyses.size})",
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(AppSizes.paddingMedium))
            
            if (alimentsAnalyses.isNotEmpty()) {
                // Affichage selon le type de graphique
                if (ongletActif == "densite_energetique") {
                    // 📊 HISTOGRAMME DENSITÉ ÉNERGÉTIQUE
                    HistogrammeEnergieAliments(
                        alimentsAnalyses = alimentsAnalyses,
                        alimentSelectionne = alimentSelectionne,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                    )
                } else {
                    // 📈 SCATTER PLOTS (PROTÉINES/LIPIDES et PHOSPHORE/PROTÉINES)
                    // Préparer les données pour le graphique selon l'onglet actif
                    val points = when (ongletActif) {
                        "protein_lipid" -> {
                            alimentsAnalyses.map { data ->
                                Point(
                                    x = data.pourcentageProteines.toFloat(),
                                    y = data.pourcentageLipides.toFloat()
                                )
                            }
                        }
                        "phosphore_protein" -> {
                            alimentsAnalyses.map { data ->
                                Point(
                                    x = data.phosphorePer1000Kcal.toFloat(),
                                    y = data.proteinePer1000Kcal.toFloat()
                                )
                            }
                        }
                        else -> {
                            alimentsAnalyses.map { data ->
                    Point(
                        x = data.pourcentageProteines.toFloat(),
                        y = data.pourcentageLipides.toFloat()
                    )
                            }
                        }
                }
                
                // Calculer les plages des axes selon le type de graphique
                val minX = points.minOf { it.x }.coerceAtLeast(0f)
                val maxX = points.maxOf { it.x }
                val minY = points.minOf { it.y }.coerceAtLeast(0f)
                val maxY = points.maxOf { it.y }
                
                // Ajuster les plages selon le type de graphique
                val (xRange, yRange) = when (ongletActif) {
                    "phosphore_protein" -> {
                        // Plages fixes pour une meilleure visibilité du graphique phosphore/protéines
                        val xRangeFixed = (minX - minX*0.05f)..(maxX+ maxX*0.05f)
                        val yRangeFixed = (minY - minY*0.05f)..(maxY + maxY*0.05f)
                        Pair(xRangeFixed, yRangeFixed)
                    }
                    else -> {
                        // Plages automatiques avec padding pour le graphique protéines/lipides
                        val xRangeAuto = (minX - minX*0.05f)..(maxX.coerceAtMost(100f) + maxX*0.05f)
                        val yRangeAuto = (minY - minY*0.05f)..(maxY.coerceAtMost(100f) + maxY*0.05f)
                        Pair(xRangeAuto, yRangeAuto)
                    }
                }
                
                // 🎯 Graphique avec numéros superposés
                BoxWithConstraints(
                    modifier = Modifier.height(400.dp)
                ) {
                    // Graphique principal
                    XYGraph(
                        xAxisModel = FloatLinearAxisModel(range = xRange),
                        yAxisModel = FloatLinearAxisModel(range = yRange),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Afficher chaque point individuellement avec LinePlot et symbol
                        alimentsAnalyses.forEachIndexed { index, data ->
                            val point = points[index] // Utiliser le point calculé selon l'onglet actif
                            LinePlot(
                                data = listOf(point),
                                symbol = {
                                    // Point principal avec couleur selon sélection
                                    val couleurPoint = if (data.aliment.uuid == alimentSelectionne) {
                                        Color(0xFF9C27B0) // Violet
                                    } else {
                                        VetNutriColors.Primary
                                    }
                                    
                                    androidx.compose.foundation.Canvas(
                                        modifier = Modifier.size(12.dp)
                                    ) { 
                                        drawCircle(
                                            color = couleurPoint, 
                                            radius = 6f,
                                            center = center
                                        )
                                    }
                                }
                            )
                        }
                    }
                    
                    // 🎯 Numéros superposés directement avec correction des marges d'axes
                    alimentsAnalyses.forEachIndexed { index, data ->
                        val point = points[index] // Utiliser le point calculé selon l'onglet actif
                        // Calculer la position du numéro avec les vraies dimensions
                        val xPosition = ((point.x - xRange.start) / (xRange.endInclusive - xRange.start))
                        val yPosition = 1f - ((point.y - yRange.start) / (yRange.endInclusive - yRange.start))
                        
                        // Marges typiques des axes KoalaPlot (estimation)
                        // 🔧 Marges AJUSTÉES basées sur l'observation des logs (décalage empirique)
                        val leftAxisMargin = 10.dp  // Marge pour les labels de l'axe Y (augmentée)
                        val bottomAxisMargin = 15.dp  // Marge pour les labels de l'axe X (augmentée)
                        val topMargin = 10.dp  // Marge supérieure
                        val rightMargin = 20.dp  // Marge droite
                        
                        // Zone de graphique effective
                        val effectiveGraphWidth = maxWidth - leftAxisMargin - rightMargin
                        val effectiveGraphHeight = maxHeight  - bottomAxisMargin - topMargin
                        
                        // Couleur selon la sélection (même logique que les points)
                        val numeroColor = if (data.aliment.uuid == alimentSelectionne) {
                            Color(0xFF9C27B0) // Violet pour sélectionné
                        } else {
                            VetNutriColors.Primary // Couleur par défaut pour tous
                        }
                        
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .wrapContentSize(Alignment.TopStart)
                                .offset(
                                    x = leftAxisMargin + (xPosition * effectiveGraphWidth.value).dp - 10.dp, // Position dans la zone effective + centrage
                                    y = topMargin + (yPosition * effectiveGraphHeight.value).dp - 30.dp  // Position dans la zone effective + au-dessus du point
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            // Fond du numéro
                            androidx.compose.foundation.Canvas(
                                modifier = Modifier.size(20.dp)
                            ) {
                                drawCircle(
                                    color = Color.White,
                                    radius = 10.dp.toPx()
                                )
                                drawCircle(
                                    color = numeroColor,
                                    radius = 10.dp.toPx(),
                                    style = Stroke(width = 2.dp.toPx())
                                )
                            }
                            
                            // Numéro
                            Text(
                                text = "${data.numero}",
                                style = MaterialTheme.typography.caption.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                color = numeroColor
                            )
                        }
                    }
                }
                } // Fin du bloc else (scatter plots)
            } else {
                Text(
                    text = "Aucune donnée disponible pour l'analyse",
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Ligne d'aliment dans le tableau
 */
@Composable
private fun AlimentRow(
    data: AlimentAnalyseData,
    isSelected: Boolean,
    onAlimentSelected: (String?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                onAlimentSelected(
                    if (isSelected) null else data.aliment.uuid
                ) 
            }
            .background(
                if (isSelected) Color(0xFF9C27B0).copy(alpha = 0.1f) 
                else Color.Transparent
            )
            .padding(AppSizes.paddingSmall),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${data.numero}",
            modifier = Modifier.weight(0.1f),
            style = MaterialTheme.typography.caption,
            fontWeight = FontWeight.Bold,
            color = VetNutriColors.Primary
        )
        Text(
            text = data.aliment.nom ?: "Sans nom",
            modifier = Modifier.weight(0.4f),
            style = MaterialTheme.typography.caption
        )
        Text(
            text = data.aliment.gamme ?: "-",
            modifier = Modifier.weight(0.25f),
            style = MaterialTheme.typography.caption
        )
        Text(
            text = data.aliment.brand ?: "-",
            modifier = Modifier.weight(0.25f),
            style = MaterialTheme.typography.caption
        )
    }
}

/**
 * Liste des aliments avec leurs données d'analyse
 */
@Composable
private fun ListeAlimentsAnalyse(
    alimentsAnalyses: List<AlimentAnalyseData>,
    alimentSelectionne: String? = null,
    onAlimentSelected: (String?) -> Unit = {},
    isCompactMode: Boolean = false, // ✨ Mode compact pour éviter les conflits de scroll
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = AppSizes.elevationMedium
    ) {
        Column(
            modifier = Modifier.padding(AppSizes.paddingMedium)
        ) {
            Text(
                text = "Liste des aliments (triés par densité énergétique décroissante)",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                color = VetNutriColors.Primary
            )
            
            Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
            
            if (alimentsAnalyses.isNotEmpty()) {
                // En-têtes du tableau
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "N°",
                        modifier = Modifier.weight(0.1f),
                        style = MaterialTheme.typography.caption,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Nom",
                        modifier = Modifier.weight(0.4f),
                        style = MaterialTheme.typography.caption,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Gamme",
                        modifier = Modifier.weight(0.25f),
                        style = MaterialTheme.typography.caption,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Marque",
                        modifier = Modifier.weight(0.25f),
                        style = MaterialTheme.typography.caption,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Divider(modifier = Modifier.padding(vertical = AppSizes.paddingSmall))
                
                // Liste des aliments - LazyColumn en mode large, Column normale en mode compact
                if (isCompactMode) {
                    // ✨ Mode compact : Column normale (pas de scroll interne)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall / 2)
                    ) {
                        alimentsAnalyses.forEach { data ->
                            AlimentRow(
                                data = data,
                                isSelected = data.aliment.uuid == alimentSelectionne,
                                onAlimentSelected = onAlimentSelected
                            )
                        }
                    }
                } else {
                    // ✨ Mode large : LazyColumn pour performance
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall / 2),
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        items(alimentsAnalyses) { data ->
                            AlimentRow(
                                data = data,
                                isSelected = data.aliment.uuid == alimentSelectionne,
                                onAlimentSelected = onAlimentSelected
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "Aucun aliment à analyser",
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Composant graphique pour afficher un histogramme de densité énergétique
 */
@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
private fun HistogrammeEnergieAliments(
    alimentsAnalyses: List<AlimentAnalyseData>,
    alimentSelectionne: String? = null,
    modifier: Modifier = Modifier
) {
    if (alimentsAnalyses.isEmpty()) {
        Text(
            text = "Aucune donnée disponible pour l'histogramme",
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )
        return
    }

    // Préparer les données pour l'histogramme
    // Créer les catégories avec les noms des aliments (tronqués) et leurs numéros
    val categories = alimentsAnalyses.map { data ->
        val nomTronque = if (data.aliment.nom?.length ?: 0 > 10) {
            "${data.aliment.nom?.take(10)}..."
        } else {
            data.aliment.nom ?: "N/A"
        }
        "$nomTronque\n(${data.numero})"
    }
    
    // Données de densité énergétique
    val densiteEnergetique = alimentsAnalyses.map { it.densiteEnergetique.toFloat() }
    
    // Plage pour l'axe Y basée sur les données
    val minDensite = densiteEnergetique.minOfOrNull { it } ?: 0f
    val maxDensite = densiteEnergetique.maxOfOrNull { it } ?: 100f
    val yRange = (minDensite - minDensite * 0.05f).coerceAtLeast(0f)..(maxDensite + maxDensite * 0.1f)

    // Créer le graphique
    XYGraph(
        xAxisModel = remember { CategoryAxisModel(categories) },
        yAxisModel = remember { FloatLinearAxisModel(yRange) },
        yAxisTitle = "Densité énergétique (kcal/100g)",
        modifier = modifier
    ) {
        VerticalBarPlot(
            xData = categories,
            yData = densiteEnergetique,
            bar = { index ->
                // Couleur de la barre selon si l'aliment est sélectionné
                val aliment = alimentsAnalyses[index]
                val couleur = if (aliment.aliment.uuid == alimentSelectionne) {
                    Color(0xFF9C27B0) // Violet pour l'aliment sélectionné
                } else {
                    VetNutriColors.Primary // Couleur par défaut
                }
                DefaultVerticalBar(SolidColor(couleur))
            }
        )
    }
}
