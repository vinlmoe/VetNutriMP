package fr.vetbrain.vetnutri_mp.View

import fr.vetbrain.vetnutri_mp.Data.ajouterOuCumulerAliment
import fr.vetbrain.vetnutri_mp.Utils.UserErrors
import fr.vetbrain.vetnutri_mp.Utils.orUserError
import fr.vetbrain.vetnutri_mp.Utils.rethrowIfCancellation
import fr.vetbrain.vetnutri_mp.Utils.AppLog
import fr.vetbrain.vetnutri_mp.Localization.translate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.vetbrain.vetnutri_mp.Components.IconButtonWithTooltip
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import io.github.koalaplot.core.*
import io.github.koalaplot.core.xygraph.*
import fr.vetbrain.vetnutri_mp.Utils.isIosPlatform

/** Sélecteur de nutriment avec dropdown */
@Composable
private fun NutrimentSelector(
        label: String,
        selectedNutriment: String?,
        onNutrimentSelected: (String) -> Unit,
        modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
                text = label,
                style = MaterialTheme.typography.caption,
                fontWeight = FontWeight.Bold,
                color = VetNutriColors.Primary
        )

        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))

        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            val selectedOption = NUTRIMENT_OPTIONS.find { it.key == selectedNutriment }
            Text(
                    text = selectedOption?.displayName ?: "Sélectionner...",
                    style = MaterialTheme.typography.body2
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = translate("general.expand"))
        }

        DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    if (!isIosPlatform) {
                        expanded = false
                    }
                }
        ) {
            NUTRIMENT_OPTIONS.forEach { option ->
                DropdownMenuItem(
                        onClick = {
                            onNutrimentSelected(option.key)
                            expanded = false
                        }
                ) {
                    Text(
                            text = "${option.displayName} (${option.unit})",
                            style = MaterialTheme.typography.body2
                    )
                }
            }
        }
    }
}

/** Vue d'analyse graphique des aliments sélectionnés */
@Composable
fun AnalyseGraphiqueAlimentsView(
        aliments: List<AlimentEv>,
        referenceEv: ReferenceEv?,
        equationRepository: EquationRepository?,
        viewModel: fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel? = null,
        onClose: () -> Unit,
        modifier: Modifier = Modifier
) {
    // États pour les données d'analyse
    var alimentsAnalyses by remember { mutableStateOf<List<AlimentAnalyseData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // États pour observer les consultations et rations sélectionnées
    val selectedConsultation by viewModel?.selectedConsultation?.collectAsState() ?: remember { mutableStateOf(null) }
    val selectedRation by viewModel?.selectedRation?.collectAsState() ?: remember { mutableStateOf(null) }
    var rationGraphData by remember { mutableStateOf<List<AlimentAnalyseData>>(emptyList()) }
    
    // États pour les valeurs métaboliques nécessaires à la HeatMap
    val poidsMetabolique by viewModel?.poidsMetabolique?.collectAsState() ?: remember { mutableStateOf(null) }
    val besoinEnergetiqueStandard by viewModel?.besoinEnergetiqueStandard?.collectAsState() ?: remember { mutableStateOf(null) }
    val besoinEnergetiqueTotal by viewModel?.besoinEnergetiqueTotal?.collectAsState() ?: remember { mutableStateOf(null) }
    
    // Déclencher les calculs métaboliques si nécessaire
    LaunchedEffect(selectedConsultation) {
        selectedConsultation?.let { viewModel?.calculerValeursMetaboliques(it) }
    }

    // CoroutineScope pour les opérations asynchrones
    val coroutineScope = rememberCoroutineScope()

    // État pour l'aliment sélectionné (UUID de l'aliment)
    var alimentSelectionne by remember { mutableStateOf<String?>(null) }
    
    // État pour les aliments masqués (Set d'UUIDs)
    var alimentsMasques by remember { mutableStateOf<Set<String>>(emptySet()) }

    // État pour l'onglet actif
    var ongletActif by remember { mutableStateOf("densite_energetique") }

    // États pour les sélections de nutriments personnalisés
    var nutrimentX by remember { mutableStateOf<String?>("proteine") }
    var nutrimentY by remember { mutableStateOf<String?>("") } // Par défaut : aucun

    // États pour les toggles d'unités
    var useDryMatterPer100g by remember { mutableStateOf(false) } // Toggle pour /1000 kcal vs /100g MS

    LaunchedEffect(selectedConsultation, referenceEv, equationRepository, useDryMatterPer100g) {
        rationGraphData = emptyList()
        val consultation = selectedConsultation ?: return@LaunchedEffect
        val prepared = fr.vetbrain.vetnutri_mp.View.AnalyseGraphique.prepareGraphConsultations(
            emptyList(), consultation, true, true
        ).single()
        val result = mutableListOf<AlimentAnalyseData>()
        var number = 0
        for (ration in prepared.rations) {
            val isSum = fr.vetbrain.vetnutri_mp.View.AnalyseGraphique.graphRationLabel(ration.uuid, 0) == "Σ"
            if (!isSum) number++
            rationFoodGraphData(ration, number, isSum, referenceEv, equationRepository, useDryMatterPer100g)
                ?.let { result.add(it) }
        }
        rationGraphData = result
    }

    // Utiliser directement la liste des aliments sans filtrage
    val alimentsFiltres = aliments

    // Calculer les données d'analyse pour chaque aliment filtré de manière asynchrone
    LaunchedEffect(alimentsFiltres, referenceEv, equationRepository, useDryMatterPer100g) {
        isLoading = true
        val resultat = mutableListOf<AlimentAnalyseData>()

        for (aliment in alimentsFiltres) {
            try {
                // 🔍 LOG DIAGNOSTIC : Vérifier les valeurs de base de l'aliment

                // ✅ CRÉER UN ALIMENTRATION TRANSITOIRE POUR UTILISER getNutrientWithComplementary
                val alimentRation =
                        AlimentRation(
                                aliment = aliment,
                                quantite = 100.0, // 100g pour les calculs
                                weight = 1.0
                        )

                // Utiliser getNutrientWithComplementary de manière asynchrone
                val _proteines =
                        alimentRation.getNutrientWithComplementary(
                                nutrient = NutrientMain.PROTEINE,
                                equationRepository = equationRepository,
                                referenceEv = referenceEv
                        )
                                ?: 0.0

                val _lipides =
                        alimentRation.getNutrientWithComplementary(
                                nutrient = NutrientMain.LIPIDE,
                                equationRepository = equationRepository,
                                referenceEv = referenceEv
                        )
                                ?: 0.0

                val _glucides =
                        alimentRation.getNutrientWithComplementary(
                                nutrient = NutrientMain.GLUCIDE,
                                equationRepository = equationRepository,
                                referenceEv = referenceEv
                        )
                                ?: 0.0

                // Utiliser getEnergie() qui utilise EquationEvaluator.calculerEnergiePour100g()
                // quand tous les paramètres sont disponibles, garantissant l'utilisation
                // de l'équation énergétique du référentiel
                val _energie =
                        alimentRation.getEnergie(
                                referenceEv = referenceEv,
                                equationRepository = equationRepository
                        )

                // Récupération du phosphore pour le second graphique
                val phosphore =
                        alimentRation.getNutrientWithComplementary(
                                nutrient = NutrientMacro.PHOS,
                                equationRepository = equationRepository,
                                referenceEv = referenceEv
                        )
                                ?: 0.0

                // Récupération du calcium pour le troisième graphique
                val calcium =
                        alimentRation.getNutrientWithComplementary(
                                nutrient = NutrientMacro.CAL,
                                equationRepository = equationRepository,
                                referenceEv = referenceEv
                        )
                                ?: 0.0

                // Récupération des autres nutriments pour le graphique personnalisé
                // Note: Seuls CAL, PHOS, MG, NA, K, CHL sont disponibles dans NutrientMacro
                val magnesium =
                        alimentRation.getNutrientWithComplementary(
                                nutrient = NutrientMacro.MG,
                                equationRepository = equationRepository,
                                referenceEv = referenceEv
                        )
                                ?: 0.0

                val sodium =
                        alimentRation.getNutrientWithComplementary(
                                nutrient = NutrientMacro.NA,
                                equationRepository = equationRepository,
                                referenceEv = referenceEv
                        )
                                ?: 0.0

                val potassium =
                        alimentRation.getNutrientWithComplementary(
                                nutrient = NutrientMacro.K,
                                equationRepository = equationRepository,
                                referenceEv = referenceEv
                        )
                                ?: 0.0

                // Obtenir l'humidité pour la conversion en matière sèche
                val humidite =
                        alimentRation.getNutrientWithComplementary(
                                nutrient = NutrientMain.HUMIDITE,
                                equationRepository = equationRepository,
                                referenceEv = referenceEv
                        )
                                ?: 0.0
                val matiereSeche = 100.0 - humidite

                val densiteEnergetiqueBase =
                        calculerDensiteEnergetiqueAsync(
                                aliment,
                                referenceEv,
                                equationRepository,
                                false
                        )
                val densiteEnergetique = if (useDryMatterPer100g && matiereSeche > 0)
                        densiteEnergetiqueBase * 100.0 / matiereSeche else densiteEnergetiqueBase
                val (pourcentageProteines, pourcentageLipides) =
                        calculerPourcentagesEnergieAlimentAsync(
                                aliment,
                                equationRepository,
                                referenceEv
                        )

                // Fonction helper pour convertir en matière sèche si nécessaire
                fun convertirEnMatiereSeche(valeurAsFed: Double): Double {
                        return if (useDryMatterPer100g) {
                                // /100g MS : convertir de g/100g as fed vers g/100g MS
                                if (matiereSeche > 0) {
                                        (valeurAsFed * 100.0) / matiereSeche
                        } else {
                                        valeurAsFed // Fallback si pas de matière sèche
                                }
                        } else {
                                // /1000 kcal : calculer par 1000 kcal
                        if (densiteEnergetique > 0) {
                                        (valeurAsFed * 1000.0) / densiteEnergetique
                        } else {
                            0.0
                                }
                        }
                }

                // Calculs pour le graphique Phosphore/Protéines (par 1000 kcal ou /100g MS)
                val proteinePer1000Kcal = convertirEnMatiereSeche(_proteines)
                val phosphorePer1000Kcal = convertirEnMatiereSeche(phosphore)
                val calciumPer1000Kcal = convertirEnMatiereSeche(calcium)

                // Calculs pour tous les nutriments en g/1000kcal ou /100g MS
                val energiePer1000Kcal = convertirEnMatiereSeche(_energie)
                val lipidePer1000Kcal = convertirEnMatiereSeche(_lipides)
                val glucidePer1000Kcal = convertirEnMatiereSeche(_glucides)
                val magnesiumPer1000Kcal = convertirEnMatiereSeche(magnesium)
                val sodiumPer1000Kcal = convertirEnMatiereSeche(sodium)
                val potassiumPer1000Kcal = convertirEnMatiereSeche(potassium)

                // 🔍 LOG DIAGNOSTIC : Vérifier les valeurs calculées

                resultat.add(
                        AlimentAnalyseData(
                                aliment = aliment,
                                numero = 0, // Numéro temporaire, sera réassigné après le tri
                                densiteEnergetique = densiteEnergetique,
                                energyAsFed = densiteEnergetiqueBase,
                                pourcentageProteines = pourcentageProteines,
                                pourcentageLipides = pourcentageLipides,
                                phosphorePer1000Kcal = phosphorePer1000Kcal,
                                proteinePer1000Kcal = proteinePer1000Kcal,
                                calciumPer1000Kcal = calciumPer1000Kcal,
                                energiePer1000Kcal = energiePer1000Kcal,
                                lipidePer1000Kcal = lipidePer1000Kcal,
                                glucidePer1000Kcal = glucidePer1000Kcal,
                                fibrePer1000Kcal =
                                        magnesiumPer1000Kcal, // Utilisation de magnesium à la place
                                // de fibre
                                cendrePer1000Kcal =
                                        sodiumPer1000Kcal, // Utilisation de sodium à la place de
                                // cendre
                                eauPer1000Kcal =
                                        potassiumPer1000Kcal // Utilisation de potassium à la place
                                // d'eau
                                )
                )
            } catch (e: Exception) {
                e.rethrowIfCancellation()

                AppLog.e("AnalyseGraphiqueAlimentsView", e.message ?: "Erreur", e)
            }
        }

        // Mettre à jour l'état avec les résultats triés par densité énergétique décroissante
        // et réassigner les numéros dans l'ordre du tri
        alimentsAnalyses =
                resultat.sortedByDescending { it.densiteEnergetique }.mapIndexed { index, data ->
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
                IconButtonWithTooltip(
                        onClick = onClose,
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = translate("auto.view.analysegraphiquealimentsview.retour_a_la_selection_d_aliments"),
                        tooltip = translate("graph.back"),
                        tint = VetNutriColors.Primary
                )
                Column {
                    Text(
                            text = "Analyse graphique de ${aliments.size} aliment(s)",
                            style = MaterialTheme.typography.h5,
                            fontWeight = FontWeight.Bold,
                            color = VetNutriColors.Primary
                    )
                    Text(
                            text = translate("auto.view.analysegraphiquealimentsview.visualisation_des_caracteristiques_nutritionnell"),
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Onglets pour choisir le type de graphique
        Row(
                modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ✨ Onglet Histogramme Densité Énergétique (PREMIER)
            Button(
                    onClick = { ongletActif = "densite_energetique" },
                    colors =
                            ButtonDefaults.buttonColors(
                                    backgroundColor =
                                            if (ongletActif == "densite_energetique")
                                                    VetNutriColors.Primary
                                            else Color.Gray.copy(alpha = 0.3f),
                                    contentColor =
                                            if (ongletActif == "densite_energetique") Color.White
                                            else Color.Black
                            )
            ) { Text(translate("chart.energyDensityLabel")) }

            // Onglet Protéines/Lipides
            Button(
                    onClick = { ongletActif = "protein_lipid" },
                    colors =
                            ButtonDefaults.buttonColors(
                                    backgroundColor =
                                            if (ongletActif == "protein_lipid")
                                                    VetNutriColors.Primary
                                            else Color.Gray.copy(alpha = 0.3f),
                                    contentColor =
                                            if (ongletActif == "protein_lipid") Color.White
                                            else Color.Black
                            )
            ) { Text(translate("auto.view.analysegraphiquealimentsview.proteines_lipides")) }

            // Onglet Phosphore/Protéines
            Button(
                    onClick = { ongletActif = "phosphore_protein" },
                    colors =
                            ButtonDefaults.buttonColors(
                                    backgroundColor =
                                            if (ongletActif == "phosphore_protein")
                                                    VetNutriColors.Primary
                                            else Color.Gray.copy(alpha = 0.3f),
                                    contentColor =
                                            if (ongletActif == "phosphore_protein") Color.White
                                            else Color.Black
                            )
            ) { Text(translate("auto.view.analysegraphiquealimentsview.phosphore_proteines")) }

            // Onglet Calcium/Phosphore
            Button(
                    onClick = { ongletActif = "calcium_phosphore" },
                    colors =
                            ButtonDefaults.buttonColors(
                                    backgroundColor =
                                            if (ongletActif == "calcium_phosphore")
                                                    VetNutriColors.Primary
                                            else Color.Gray.copy(alpha = 0.3f),
                                    contentColor =
                                            if (ongletActif == "calcium_phosphore") Color.White
                                            else Color.Black
                            )
            ) { Text(translate("auto.view.analysegraphiquealimentsview.calcium_phosphore")) }

            // ✨ Nouvel onglet Nutriments personnalisés
            Button(
                    onClick = { ongletActif = "nutriments_perso" },
                    colors =
                            ButtonDefaults.buttonColors(
                                    backgroundColor =
                                            if (ongletActif == "nutriments_perso")
                                                    VetNutriColors.Primary
                                            else Color.Gray.copy(alpha = 0.3f),
                                    contentColor =
                                            if (ongletActif == "nutriments_perso") Color.White
                                            else Color.Black
                            )
            ) { Text(translate("auto.view.analysegraphiquealimentsview.nutriments_personnalisas")) }

            // ✨ Nouvel onglet Analyse détaillée
            Button(
                    onClick = { ongletActif = "analyse_detaillee" },
                    colors =
                            ButtonDefaults.buttonColors(
                                    backgroundColor =
                                            if (ongletActif == "analyse_detaillee")
                                                    VetNutriColors.Primary
                                            else Color.Gray.copy(alpha = 0.3f),
                                    contentColor =
                                            if (ongletActif == "analyse_detaillee") Color.White
                                            else Color.Black
                            )
            ) { Text(translate("auto.view.analysegraphiquealimentsview.analyse_dataillae")) }

            // ✨ Nouvel onglet HeatMap
            Button(
                    onClick = { ongletActif = "heatmap" },
                    colors =
                            ButtonDefaults.buttonColors(
                                    backgroundColor =
                                            if (ongletActif == "heatmap")
                                                    VetNutriColors.Primary
                                            else Color.Gray.copy(alpha = 0.3f),
                                    contentColor =
                                            if (ongletActif == "heatmap") Color.White
                                            else Color.Black
                            )
            ) { Text(translate("auto.view.analysegraphiquealimentsview.heatmap")) }
        }

        // Toggle pour /1000 kcal vs /100g MS (pas pour les ratios énergétiques, l'analyse détaillée et la heatmap)
        if (ongletActif != "protein_lipid" && ongletActif != "analyse_detaillee" && ongletActif != "heatmap") {
        Row(
                modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
        ) {
                Text(
                                text = translate("chart.togglePer1000Kcal"),
                        style = MaterialTheme.typography.caption,
                                color = if (!useDryMatterPer100g) VetNutriColors.Primary else MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
                Switch(
                                checked = useDryMatterPer100g,
                                onCheckedChange = { useDryMatterPer100g = it }
                )
                Text(
                                text = translate("chart.togglePer100gDm"),
                        style = MaterialTheme.typography.caption,
                                color = if (useDryMatterPer100g) VetNutriColors.Primary else MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }
        }
        // Vérifier si une référence est disponible
        val hasReference = referenceEv != null
        val hasEquationRepository = equationRepository != null

        // Contenu principal - responsive selon la largeur
        if (!hasReference) {
            // Aucune référence disponible
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                ) {
                    Text(
                            text = translate("consultation.edit.ref.general_none"),
                            style = MaterialTheme.typography.body1,
                            fontWeight = FontWeight.Bold,
                            color = VetNutriColors.Error
                    )
                    Text(
                            text = translate("graph.selectReferenceHint"),
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        } else if (!hasEquationRepository) {
            // Aucun repository d'équations disponible
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                ) {
                    Text(
                            text = translate("auto.view.analysegraphiquealimentsview.repository_d_equations_non_disponible"),
                            style = MaterialTheme.typography.body1,
                            fontWeight = FontWeight.Bold,
                            color = VetNutriColors.Error
                    )
                    Text(
                            text = translate("auto.view.analysegraphiquealimentsview.le_repository_d_equations_est_requis_pour_calcul"),
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        } else if (isLoading) {
            // Indicateur de chargement
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                ) {
                    CircularProgressIndicator(color = VetNutriColors.Primary)
                    Text(
                            text = translate("auto.view.analysegraphiquealimentsview.calcul_des_valeurs_nutritionnelles"),
                            style = MaterialTheme.typography.body1,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        } else {

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val isCompact = maxWidth < 800.dp

                if (isCompact) {
                    // Vue compacte : graphiques puis liste avec bouton retour plus visible ET
                    // SCROLLABLE (sauf pour analyse_detaillee et heatmap qui gèrent leur propre scroll)
                    if (ongletActif == "analyse_detaillee") {
                        // ✨ Nouvelle vue détaillée - pas de scroll parent, le LazyColumn gère le scroll
                        AnalyseDetailleeAlimentsView(
                                aliments = aliments,
                                alimentsAnalyses = alimentsAnalyses.filter { it.aliment.uuid !in alimentsMasques },
                                referenceEv = referenceEv,
                                equationRepository = equationRepository,
                                viewModel = viewModel,
                                useDryMatterPer100g = useDryMatterPer100g,
                                alimentSelectionne = alimentSelectionne,
                                onAlimentSelected = { uuid -> alimentSelectionne = uuid },
                                onUseDryMatterPer100gChange = { newValue -> useDryMatterPer100g = newValue },
                                modifier = Modifier.fillMaxSize()
                        )
                    } else if (ongletActif == "heatmap") {
                        // ✨ Vue HeatMap - pas de scroll parent, le LazyColumn gère le scroll
                        HeatMapAlimentsView(
                                aliments = aliments,
                                alimentsAnalyses = alimentsAnalyses.filter { it.aliment.uuid !in alimentsMasques },
                                referenceEv = referenceEv,
                                equationRepository = equationRepository,
                                besoinEnergetiqueEntretien = besoinEnergetiqueStandard,
                                poidsAnimal = selectedConsultation?.weight,
                                poidsMetabolique = poidsMetabolique,
                                modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .verticalScroll(
                                                        rememberScrollState()
                                                ), // ✨ Rendre la vue principale scrollable
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                        ) {
                            // 🔧 Bouton retour plus visible en mode compact

                            // Graphique principal
                            // Graphique principal
                            GraphiqueNuagePoints(
                                    alimentsAnalyses = (alimentsAnalyses + rationGraphData).filter { it.aliment.uuid !in alimentsMasques },
                                    ongletActif = ongletActif,
                                    alimentSelectionne = alimentSelectionne,
                                    nutrimentX = nutrimentX,
                                    nutrimentY = nutrimentY,
                                    useDryMatterPer100g = useDryMatterPer100g,
                                    referenceEv = referenceEv,
                                    equationRepository = equationRepository,
                                    modifier = Modifier.fillMaxWidth()
                            )

                            // ✨ Sélecteurs de nutriments pour l'onglet personnalisé (sous le graphique)
                            if (ongletActif == "nutriments_perso") {
                                Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement =
                                                Arrangement.spacedBy(AppSizes.paddingMedium)
                                ) {
                                    NutrimentSelector(
                                            label = translate("graph.axisXLabel"),
                                            selectedNutriment = nutrimentX,
                                            onNutrimentSelected = { nutrimentX = it },
                                            modifier = Modifier.weight(1f)
                                    )

                                    NutrimentSelector(
                                            label = translate("graph.axisYLabel"),
                                            selectedNutriment = nutrimentY,
                                            onNutrimentSelected = { nutrimentY = it },
                                            modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            // Liste des aliments (sans LazyColumn en mode compact)
                            ListeAlimentsAnalyse(
                                    alimentsAnalyses = alimentsAnalyses + rationGraphData,
                                    alimentSelectionne = alimentSelectionne,
                                    alimentsMasques = alimentsMasques,
                                    onAlimentSelected = { uuid -> alimentSelectionne = uuid },
                                    onToggleHidden = { uuid, isHidden ->
                                        alimentsMasques = if (isHidden) {
                                            alimentsMasques + uuid
                                        } else {
                                            alimentsMasques - uuid
                                        }
                                    },
                                    isCompactMode = true, // ✨ Mode compact = pas de LazyColumn
                                    modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    // Vue large : côte à côte avec scroll uniquement sur la partie droite
                    Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                    ) {
                        // Colonne gauche : liste des aliments (1/4 de la largeur) - masquée pour l'analyse détaillée et la heatmap
                        if (ongletActif != "analyse_detaillee" && ongletActif != "heatmap") {
                            Column(
                                    modifier = Modifier
                                        .weight(0.25f)
                                        .fillMaxHeight(),
                                    verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                            ) {
                                ListeAlimentsAnalyse(
                                        alimentsAnalyses = alimentsAnalyses + rationGraphData,
                                        alimentSelectionne = alimentSelectionne,
                                        alimentsMasques = alimentsMasques,
                                        onAlimentSelected = { uuid -> alimentSelectionne = uuid },
                                        onToggleHidden = { uuid, isHidden ->
                                            alimentsMasques = if (isHidden) {
                                                alimentsMasques + uuid
                                            } else {
                                                alimentsMasques - uuid
                                            }
                                        },
                                        isCompactMode = false, // ✨ Mode large = avec LazyColumn
                                        modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Colonne droite : graphiques (3/4 de la largeur) ou analyse détaillée/heatmap (plein écran)
                        if (ongletActif == "analyse_detaillee") {
                            // ✨ Nouvelle vue détaillée - pas de scroll parent, le LazyColumn gère le scroll
                            AnalyseDetailleeAlimentsView(
                                    aliments = aliments,
                                    alimentsAnalyses = alimentsAnalyses.filter { it.aliment.uuid !in alimentsMasques },
                                    referenceEv = referenceEv,
                                    equationRepository = equationRepository,
                                    viewModel = viewModel,
                                    useDryMatterPer100g = useDryMatterPer100g,
                                    alimentSelectionne = alimentSelectionne,
                                    onAlimentSelected = { uuid -> alimentSelectionne = uuid },
                                    onUseDryMatterPer100gChange = { newValue -> useDryMatterPer100g = newValue },
                                    modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                            )
                        } else if (ongletActif == "heatmap") {
                            // ✨ Vue HeatMap - pas de scroll parent, le LazyColumn gère le scroll
                            HeatMapAlimentsView(
                                    aliments = aliments,
                                    alimentsAnalyses = alimentsAnalyses.filter { it.aliment.uuid !in alimentsMasques },
                                    referenceEv = referenceEv,
                                    equationRepository = equationRepository,
                                    besoinEnergetiqueEntretien = besoinEnergetiqueStandard,
                                    poidsAnimal = selectedConsultation?.weight,
                                    poidsMetabolique = poidsMetabolique,
                                    modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                            )
                        } else {
                            // Graphiques avec scroll
                            Column(
                                    modifier = Modifier
                                        .weight(0.75f)
                                        .fillMaxHeight()
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                            ) {
                                GraphiqueNuagePoints(
                                        alimentsAnalyses = (alimentsAnalyses + rationGraphData).filter { it.aliment.uuid !in alimentsMasques },
                                        ongletActif = ongletActif,
                                        alimentSelectionne = alimentSelectionne,
                                        nutrimentX = nutrimentX,
                                        nutrimentY = nutrimentY,
                                        useDryMatterPer100g = useDryMatterPer100g,
                                        referenceEv = referenceEv,
                                        equationRepository = equationRepository,
                                        modifier = Modifier.fillMaxWidth()
                                )

                                // ✨ Sélecteurs de nutriments pour l'onglet personnalisé (sous le
                                // graphique)
                                if (ongletActif == "nutriments_perso") {
                                    Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                                    Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement =
                                                    Arrangement.spacedBy(AppSizes.paddingMedium)
                                    ) {
                                        NutrimentSelector(
                                                label = translate("graph.axisXLabel"),
                                                selectedNutriment = nutrimentX,
                                                onNutrimentSelected = { nutrimentX = it },
                                                modifier = Modifier.weight(1f)
                                        )

                                        NutrimentSelector(
                                                label = translate("graph.axisYLabel"),
                                                selectedNutriment = nutrimentY,
                                                onNutrimentSelected = { nutrimentY = it },
                                                modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Boutons flottants pour ajouter des aliments aux rations
                if (viewModel != null) {
                    // Bouton + (ajouter à la ration existante)
                    FloatingActionButton(
                        onClick = {
                            val aliment = aliments.find { it.uuid == alimentSelectionne }
                                .orUserError("aucun aliment n'est sélectionné")
                            if (aliment != null) {
                                // Même logique que RationsView.kt pour une mise à jour immédiate
                                coroutineScope.launch {
                                    val alimentComplet = viewModel.getAlimentComplet(aliment.uuid)
                                        .orUserError("l'aliment « ${aliment.nom ?: aliment.uuid} » est introuvable en base")
                                        ?: return@launch
                                    // Sélections relues après le chargement : elles ont pu changer
                                    val ration = selectedRation
                                        .orUserError("aucune ration n'est sélectionnée") ?: return@launch
                                    val consultation = selectedConsultation
                                        .orUserError("aucune consultation n'est sélectionnée") ?: return@launch
                                    val newAlimentRation = AlimentRation(
                                        refAlimUnif = alimentComplet.uuid,
                                        quantite = 100.0,
                                        aliment = alimentComplet,
                                        refRation = ration.uuid
                                    )
                                    // Aliment déjà présent : quantité cumulée (une ligne par aliment en base)
                                    val updatedAliments =
                                        ajouterOuCumulerAliment(ration.alimentMutableList, newAlimentRation).toMutableList()
                                    val updatedRation = ration.copy(alimentMutableList = updatedAliments)

                                    val updatedRations = consultation.rations.toMutableList()
                                    val rationIndex = updatedRations.indexOfFirst { it.uuid == ration.uuid }
                                    if (rationIndex < 0) {
                                        UserErrors.report(
                                            "Action impossible : la ration « ${ration.name} » n'appartient plus à la consultation sélectionnée."
                                        )
                                        return@launch
                                    }
                                    updatedRations[rationIndex] = updatedRation
                                    viewModel.updateConsultation(consultation.copy(rations = updatedRations))
                                    // Sélectionner la ration mise à jour pour une mise à jour immédiate
                                    viewModel.selectRation(updatedRation)
                                }
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .offset(y = (-80).dp),
                        backgroundColor = VetNutriColors.Primary,
                        contentColor = Color.White
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = translate("auto.view.analysegraphiquealimentsview.ajouter_a_la_ration")
                        )
                    }
                    
                    // Bouton ++ (créer nouvelle ration)
                    FloatingActionButton(
                        onClick = {
                            val aliment = aliments.find { it.uuid == alimentSelectionne }
                                .orUserError("aucun aliment n'est sélectionné")
                            if (aliment != null) {
                                // Même logique que RationsView.kt pour une mise à jour immédiate
                                coroutineScope.launch {
                                    val alimentComplet = viewModel.getAlimentComplet(aliment.uuid)
                                        .orUserError("l'aliment « ${aliment.nom ?: aliment.uuid} » est introuvable en base")
                                        ?: return@launch
                                    val consultation = selectedConsultation
                                        .orUserError("aucune consultation n'est sélectionnée") ?: return@launch
                                    // Nouvelle ration nommée d'après la marque de l'aliment
                                    val nomRation = alimentComplet.brand ?: alimentComplet.nom ?: "Nouvelle ration"
                                    val nouvelleRation = fr.vetbrain.vetnutri_mp.Data.Ration(
                                        name = nomRation,
                                        actual = false,
                                        alimentMutableList = mutableListOf()
                                    )
                                    val newAlimentRation = AlimentRation(
                                        refAlimUnif = alimentComplet.uuid,
                                        quantite = 100.0,
                                        aliment = alimentComplet,
                                        refRation = nouvelleRation.uuid
                                    )
                                    val rationAvecAliment = nouvelleRation.copy(
                                        alimentMutableList = mutableListOf(newAlimentRation)
                                    )
                                    val updatedRations = consultation.rations.toMutableList()
                                    updatedRations.add(rationAvecAliment)
                                    viewModel.updateConsultation(consultation.copy(rations = updatedRations))
                                    // Sélectionner automatiquement la nouvelle ration créée
                                    viewModel.selectRation(rationAvecAliment)
                                }
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        backgroundColor = VetNutriColors.Secondary,
                        contentColor = Color.White
                    ) {
                        Text(
                            text = "+R",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/** Ligne d'aliment, de ration ou de somme dans la légende du graphique. */
@Composable
private fun AlimentRow(
        data: AlimentAnalyseData,
        isSelected: Boolean,
        isHidden: Boolean,
        onAlimentSelected: (String?) -> Unit,
        onToggleHidden: (String, Boolean) -> Unit
) {
    val rowColor = data.rationColor ?: VetNutriColors.Primary
    val itemType = when {
        data.isRationSum -> "la somme"
        data.rationActual != null -> "la ration"
        else -> "l'aliment"
    }
    val visibilityLabel = if (isHidden) "Afficher $itemType" else "Masquer $itemType"
    Row(
            modifier =
                    Modifier.fillMaxWidth()
                            .clickable {
                                onAlimentSelected(if (isSelected) null else data.aliment.uuid)
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
                text = data.graphLabel,
                modifier = Modifier.width(28.dp),
                style = MaterialTheme.typography.caption,
                fontWeight = FontWeight.Bold,
                color = rowColor
        )
        Text(
                text = if (data.isRationSum) data.aliment.nom.orEmpty().removePrefix("Σ").trim().ifEmpty { "Somme" }
                       else data.aliment.nom ?: "Sans nom",
                modifier = Modifier.weight(0.6f),
                style = MaterialTheme.typography.caption,
                fontWeight = FontWeight.Medium,
                color = data.rationColor ?: MaterialTheme.colors.onSurface
        )
        Text(
                text = when (data.rationActual) {
                    true -> "Actuelle"
                    false -> "Proposée"
                    null -> data.aliment.brand ?: "-"
                },
                modifier = Modifier.weight(0.3f),
                style = MaterialTheme.typography.caption
        )
        // Visibilité individuelle des aliments, rations et sommes.
        IconButtonWithTooltip(
                onClick = { onToggleHidden(data.aliment.uuid, !isHidden) },
                modifier = Modifier.size(32.dp),
                imageVector = if (isHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = visibilityLabel,
                tooltip = visibilityLabel,
                iconModifier = Modifier.size(20.dp),
                tint = if (isHidden) MaterialTheme.colors.onSurface.copy(alpha = 0.5f) else rowColor
        )
    }
}

/** Légende commune aux aliments, rations et sommes représentés dans les graphiques. */
@Composable
private fun ListeAlimentsAnalyse(
        alimentsAnalyses: List<AlimentAnalyseData>,
        alimentSelectionne: String? = null,
        alimentsMasques: Set<String> = emptySet(),
        onAlimentSelected: (String?) -> Unit = {},
        onToggleHidden: (String, Boolean) -> Unit = { _, _ -> },
        isCompactMode: Boolean = false, // ✨ Mode compact pour éviter les conflits de scroll
        modifier: Modifier = Modifier
) {
    val hasRations = alimentsAnalyses.any { it.rationActual != null }
    Card(modifier = modifier, elevation = AppSizes.elevationMedium) {
        Column(modifier = Modifier.padding(AppSizes.paddingMedium)) {
            Text(
                    text = if (hasRations) "Aliments et rations" else translate("animalList.foodList"),
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
                            text = translate("auto.view.analysegraphiquealimentsview.n"),
                            modifier = Modifier.width(28.dp),
                            style = MaterialTheme.typography.caption,
                            fontWeight = FontWeight.Bold
                    )
                    Text(
                            text = translate("ration.name"),
                            modifier = Modifier.weight(0.6f),
                            style = MaterialTheme.typography.caption,
                            fontWeight = FontWeight.Bold
                    )
                    Text(
                            text = if (hasRations) "Marque / type" else translate("food_edit.field.brand"),
                            modifier = Modifier.weight(0.3f),
                            style = MaterialTheme.typography.caption,
                            fontWeight = FontWeight.Bold
                    )
                    // Espace pour l'icône de visibilité (pas de texte)
                    Spacer(modifier = Modifier.width(32.dp))
                }

                Divider(modifier = Modifier.padding(vertical = AppSizes.paddingSmall))

                // Liste des aliments - LazyColumn en mode large, Column normale en mode compact
                if (isCompactMode) {
                    // ✨ Mode compact : Column normale (pas de scroll interne)
                    Column(verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall / 2)) {
                        alimentsAnalyses.forEach { data ->
                            AlimentRow(
                                    data = data,
                                    isSelected = data.aliment.uuid == alimentSelectionne,
                                    isHidden = data.aliment.uuid in alimentsMasques,
                                    onAlimentSelected = onAlimentSelected,
                                    onToggleHidden = onToggleHidden
                            )
                        }
                    }
                } else {
                    // ✨ Mode large : LazyColumn pour performance avec scroll
                    LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall / 2),
                            modifier = Modifier.fillMaxHeight()
                    ) {
                        items(alimentsAnalyses) { data ->
                            AlimentRow(
                                    data = data,
                                    isSelected = data.aliment.uuid == alimentSelectionne,
                                    isHidden = data.aliment.uuid in alimentsMasques,
                                    onAlimentSelected = onAlimentSelected,
                                    onToggleHidden = onToggleHidden
                            )
                        }
                    }
                }
            } else {
                Text(
                        text = translate("chart.noFoodToAnalyze"),
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}
