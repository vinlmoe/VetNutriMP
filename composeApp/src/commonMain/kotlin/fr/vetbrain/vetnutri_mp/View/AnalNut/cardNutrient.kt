package fr.vetbrain.vetnutri_mp.View.AnalNut

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Repository.PreferencesRepository
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.TextUtils

/** Données pour un item de la grille (titre ou nutriment) */
private sealed class GridItem {
    data class TitreSection(val categorie: String, val titre: String) : GridItem()
    data class NutrimentItem(val nom: String, val valeur: ValeurNutritionnelle) : GridItem()
}

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
        onNutrimentClick: (String, ValeurNutritionnelle) -> Unit,
        // Nouveaux paramètres pour les préférences
        animal: AnimalEv? = null,
        preferencesRepository: PreferencesRepository? = null,
        equationRepository: EquationRepository? = null,
        // Paramètre pour adapter la hauteur selon la vue (large ou compacte)
        isLargeView: Boolean = false,
        // Références de maladies pour le contrôle et les graphes
        referencesMaladies: List<ReferenceEv> = emptyList()
) {
    // Etat pour basculer entre affichage filtré et complet
    var afficherTousLesNutriments by remember { mutableStateOf(false) }

    // Charger les préférences d'expression pour l'espèce de l'animal
    var typeExpressionBesoin by remember { mutableStateOf<TypeExpressionBesoin?>(null) }

    LaunchedEffect(animal, preferencesRepository) {
        animal?.let { animalData ->
            preferencesRepository?.let { repo ->
                try {
                    val preferences = repo.getPreferencesForSpecies(animalData.getEspece())
                    typeExpressionBesoin = preferences.getTypeExpressionBesoinEnum()
                } catch (e: Exception) {
                    typeExpressionBesoin = TypeExpressionBesoin.DEFAULT
                }
            }
        }
                ?: run { typeExpressionBesoin = TypeExpressionBesoin.DEFAULT }
    }

    val valeursNutritionnellesBase =
            if (afficherTousLesNutriments ||
                            nutrimentsSelectionnes == null ||
                            nutrimentsSelectionnes.isEmpty()
            ) {
                // Utiliser les équations complémentaires depuis la ReferenceEv si disponible
                if (referenceUtilisee != null && equationRepository != null) {
                    fr.vetbrain.vetnutri_mp.Data.analyserValeursNutritionnellesRationAvecEquations(
                            ration = ration,
                            preferencesEspece = PreferencesEspece(),
                            equationRepository = equationRepository,
                            referenceEv = referenceUtilisee
                    )
                } else {
                    analyserValeursNutritionnellesRation(ration)
                }
            } else {
                // Mode filtré: intégrer aussi les équations si disponibles via la ReferenceEv
                if (referenceUtilisee != null && equationRepository != null) {
                    fr.vetbrain.vetnutri_mp.Data.analyserValeursNutritionnellesRationSelective(
                            ration = ration,
                            nutrimentsSelectionnes = nutrimentsSelectionnes,
                            preferencesEspece = null,
                            equationRepository = equationRepository,
                            referenceEv = referenceUtilisee
                    )
                } else {
                    analyserValeursNutritionnellesRationSelective(ration, nutrimentsSelectionnes)
                }
            }

    // Appliquer les équations complémentaires sélectionnées (si présentes)
    val selectedEquationUuidsKey =
            remember(referenceUtilisee) {
                referenceUtilisee?.equationsNut?.joinToString("|") { it.uuid } ?: ""
            }
    val valeursNutritionnelles =
            remember(valeursNutritionnellesBase, referenceUtilisee, selectedEquationUuidsKey) {
                val baseMap = valeursNutritionnellesBase.toMutableMap()
                try {
                    if (referenceUtilisee != null) {
                        val selectedEquationUuids = referenceUtilisee.equationsNut.map { it.uuid }
                        println(
                                "EQDBG selectedEquationUuids (ReferenceEv): " +
                                        selectedEquationUuids
                        )
                        if (selectedEquationUuids.isNotEmpty()) {
                            val equationRepo = equationRepository

                            // Préparer les variables globales pour l’évaluation (ration + animal)
                            // Récupérées dans EquationEvaluator.evaluerBesoinNutritionnel côté
                            // repository

                            // Récupérer toutes les équations une fois et indexer par UUID pour
                            // éviter
                            // les incohérences de lookup
                            val eqMap: Map<String, fr.vetbrain.vetnutri_mp.Data.Equation> =
                                    try {
                                        val all =
                                                kotlinx.coroutines.runBlocking {
                                                    equationRepo?.getAllEquations() ?: emptyList()
                                                }
                                        println(
                                                "EQDBG repository loaded equations count=" +
                                                        all.size
                                        )
                                        all.associateBy { it.uuid }
                                    } catch (e: Exception) {
                                        println(
                                                "EQDBG failed to load all equations from repo: " + e
                                        )
                                        emptyMap()
                                    }

                            // Pour chaque équation sélectionnée, calculer sa contribution et
                            // l’ajouter au nutriment correspondant
                            selectedEquationUuids.forEach { eqId ->
                                println("EQDBG fetching equation: " + eqId)
                                val eq = eqMap[eqId]
                                if (eq == null) {
                                    println("EQDBG equation not found: " + eqId)
                                }
                                if (eq != null &&
                                                eq.kind ==
                                                        fr.vetbrain.vetnutri_mp.Enumer.EquationKind
                                                                .COMPLEMENTARY_NUTRIENT &&
                                                eq.nutrient != null
                                ) {
                                    println(
                                            "EQDBG evaluating: name='" +
                                                    eq.name +
                                                    "' nutrient=" +
                                                    eq.nutrient!!.label +
                                                    " ratio=" +
                                                    eq.ratio +
                                                    " expr='" +
                                                    eq.equationScript +
                                                    "'"
                                    )
                                    val poids = (animal?.consultations?.lastOrNull()?.weight ?: 0f)
                                    val bee = (animal?.getBEE()?.toFloat() ?: 0f)
                                    val mw =
                                            if (poids > 0)
                                                    fr.vetbrain.vetnutri_mp.Utils.EquationEvaluator
                                                            .calculerPoidsMetabolique(poids)
                                                            .toFloat()
                                            else 0f
                                    println(
                                            "EQDBG inputs: BW=" +
                                                    poids +
                                                    " BEE=" +
                                                    bee +
                                                    " MW=" +
                                                    mw
                                    )
                                    val valeur =
                                            if (eq.ratio) {
                                                fr.vetbrain.vetnutri_mp.Utils.EquationEvaluator
                                                        .evaluerBesoinNutritionnelAvecComplementairesBlocking(
                                                                expression = eq.equationScript,
                                                                poidsCorps = poids,
                                                                besoinEnergetique = bee,
                                                                poidsMetabolique = mw,
                                                                variablesSupp =
                                                                        (animal?.consultations
                                                                                ?.lastOrNull()
                                                                                ?.suppVarp
                                                                                ?: mutableListOf()),
                                                                ration = ration,
                                                                preferences =
                                                                        (kotlinx.coroutines
                                                                                .runBlocking {
                                                                                    preferencesRepository
                                                                                            ?.getPreferencesForSpecies(
                                                                                                    ration.getEspece()
                                                                                            )
                                                                                }
                                                                                ?: fr.vetbrain
                                                                                        .vetnutri_mp
                                                                                        .Data
                                                                                        .PreferencesEspece
                                                                                        .createDefault(
                                                                                                ration.getEspece()
                                                                                        )),
                                                                equationRepository =
                                                                        equationRepository!!,
                                                                referenceEv = referenceUtilisee
                                                        )
                                                        ?: 0.0
                                            } else 0.0
                                    println("EQDBG result: value=" + valeur)
                                    val nutrient = eq.nutrient!!
                                    val label = nutrient.translateEnum()
                                    val existante = baseMap[label]

                                    if (existante != null) {
                                        if (eq.ratio) {
                                            println(
                                                    "EQDBG apply ratio: replace previous=" +
                                                            existante.valeur +
                                                            " by=" +
                                                            valeur
                                            )
                                            baseMap[label] = existante.copy(valeur = valeur)
                                        } else {
                                            // Ne plus additionner au total ici (calcul déjà par
                                            // aliment)
                                            println(
                                                    "EQDBG skip add at ration level (handled per-ingredient)"
                                            )
                                        }
                                    } else {
                                        println(
                                                "EQDBG apply create: label=" +
                                                        label +
                                                        " value=" +
                                                        valeur
                                        )
                                        if (eq.ratio) {
                                            baseMap[label] =
                                                    ValeurNutritionnelle(
                                                            nutriment = nutrient,
                                                            unite = nutrient.ue,
                                                            valeur = valeur,
                                                            description = "Calculé par équation",
                                                            complete = true
                                                    )
                                        } else {
                                            // Pas de création au niveau ration pour non-ratio
                                            println(
                                                    "EQDBG skip create at ration level for non-ratio"
                                            )
                                        }
                                    }
                                }
                                if (eq != null && eq.nutrient == null) {
                                    println("EQDBG skipped equation without nutrient: " + eq.uuid)
                                }
                                if (eq != null &&
                                                eq.kind !=
                                                        fr.vetbrain.vetnutri_mp.Enumer.EquationKind
                                                                .COMPLEMENTARY_NUTRIENT
                                ) {
                                    println(
                                            "EQDBG skipped equation kind " +
                                                    eq.kind.name +
                                                    " for uuid=" +
                                                    eq.uuid
                                    )
                                }
                            }
                        } else {
                            val specieName = referenceUtilisee?.espece?.name ?: "?"
                            println("EQDBG no selected equations for " + specieName)
                        }
                    }
                } catch (e: Exception) {
                    println("EQDBG exception during complementary equations apply: " + e)
                }
                baseMap
            }

    // Grouper les nutriments par catégorie
    val nutrimentsGroupes =
            remember(valeursNutritionnelles) {
                grouperNutrimentsParCategorie(valeursNutritionnelles)
            }

    Card(modifier = modifier, elevation = AppSizes.elevationSmall) {
        Column(
                modifier = Modifier.fillMaxSize().padding(AppSizes.paddingSmall),
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingXSmall)
        ) {
            var afficherBullet by remember { mutableStateOf(false) }
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

                    // Toggle d'affichage BulletGraph
                    Text(
                            text = if (afficherBullet) "Bullet" else "Cartes",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                    IconButton(
                            onClick = { afficherBullet = !afficherBullet },
                            modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                                imageVector =
                                        if (afficherBullet) Icons.Filled.ToggleOn
                                        else Icons.Filled.ToggleOff,
                                contentDescription =
                                        if (afficherBullet) "Afficher en cartes"
                                        else "Afficher en bullet graphs",
                                tint =
                                        if (afficherBullet) VetNutriColors.Primary
                                        else MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Liste avec titres de section et grilles de nutriments
            LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall),
                    modifier =
                            if (isLargeView) {
                                // En vue large, utiliser une hauteur adaptative basée sur l'espace
                                // disponible
                                Modifier.fillMaxWidth().fillMaxHeight()
                            } else {
                                // En vue compacte, utiliser une hauteur fixe pour éviter les
                                // conflits de scroll
                                Modifier.fillMaxWidth().height(400.dp)
                            }
            ) {
                // Ordre d'affichage des catégories
                val ordreCategories =
                        listOf("BASE", "MACRO", "MIN", "VITAM", "LIPID", "AMA", "ANA", "OTHER")

                ordreCategories.forEach { categorie ->
                    nutrimentsGroupes[categorie]?.let { nutriments ->
                        if (nutriments.isNotEmpty()) {
                            // Titre de section
                            item {
                                TitreSectionCard(
                                        titre = obtenirTitreCategorie(categorie),
                                        modifier = Modifier.fillMaxWidth()
                                )
                            }

                            if (!afficherBullet) {
                                // Grille des nutriments (cartes)
                                item {
                                    Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement =
                                                    Arrangement.spacedBy(AppSizes.paddingXSmall)
                                    ) {
                                        nutriments.chunked(3).forEach { rangeeNutriments ->
                                            Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement =
                                                            Arrangement.spacedBy(
                                                                    AppSizes.paddingXSmall
                                                            )
                                            ) {
                                                rangeeNutriments.forEach { (nom, valeur) ->
                                                    NutrimentCard(
                                                            nom = nom,
                                                            valeurNutritionnelle = valeur,
                                                            poidsMetabolique = poidsMetabolique,
                                                            referenceUtilisee = referenceUtilisee,
                                                            besoinEnergetiqueEntretien =
                                                                    besoinEnergetiqueEntretien,
                                                            poidsAnimal = poidsAnimal,
                                                            modifier = Modifier.weight(1f),
                                                            onClick = {
                                                                onNutrimentClick(nom, valeur)
                                                            },
                                                            typeExpressionBesoin =
                                                                    typeExpressionBesoin
                                                    )
                                                }
                                                repeat(3 - rangeeNutriments.size) {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Liste de bullet graphs, un par nutriment
                                items(items = nutriments.filter { it.second.valeur > 0.0 }) { pair
                                    ->
                                    val nom = pair.first
                                    val valeur = pair.second
                                    val apport = valeur.valeur.toFloat()
                                    val typeExpr =
                                            typeExpressionBesoin ?: TypeExpressionBesoin.DEFAULT
                                    if (referenceUtilisee != null) {
                                        Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                    text = nom,
                                                    style = MaterialTheme.typography.caption,
                                                    color =
                                                            MaterialTheme.colors.onSurface.copy(
                                                                    alpha = 0.8f
                                                            ),
                                                    modifier = Modifier.width(140.dp)
                                            )
                                            Box(modifier = Modifier.weight(1f)) {
                                                ReferenceBulletGraph(
                                                        valeurApport = apport,
                                                        reference = referenceUtilisee,
                                                        nutriment = valeur.nutriment,
                                                        typeExpressionBesoin = typeExpr,
                                                        poidsAnimal = poidsAnimal,
                                                        poidsMetabolique = poidsMetabolique,
                                                        besoinEnergetiqueEntretien =
                                                                besoinEnergetiqueEntretien,
                                                        referencesMaladies = referencesMaladies
                                                )
                                            }
                                        }
                                    } else {
                                        Text(
                                                text =
                                                        "$nom: ${String.format("%.2f", apport)} ${valeur.unite.displayName}",
                                                style = MaterialTheme.typography.caption,
                                                color = MaterialTheme.colors.onSurface
                                        )
                                    }
                                    Divider(
                                            modifier = Modifier.fillMaxWidth(),
                                            color =
                                                    MaterialTheme.colors.onSurface.copy(
                                                            alpha = 0.05f
                                                    )
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

/** Composant pour afficher un titre de section */
@Composable
private fun TitreSectionCard(titre: String, modifier: Modifier = Modifier) {
    Card(
            modifier = modifier,
            elevation = 2.dp,
            backgroundColor = VetNutriColors.Primary.copy(alpha = 0.1f)
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(AppSizes.paddingSmall),
                verticalAlignment = Alignment.CenterVertically
        ) {
            Divider(
                    modifier = Modifier.weight(1f).height(1.dp),
                    color = VetNutriColors.Primary.copy(alpha = 0.3f)
            )
            Text(
                    text = titre,
                    style = MaterialTheme.typography.subtitle2,
                    fontWeight = FontWeight.Bold,
                    color = VetNutriColors.Primary,
                    modifier = Modifier.padding(horizontal = AppSizes.paddingSmall)
            )
            Divider(
                    modifier = Modifier.weight(1f).height(1.dp),
                    color = VetNutriColors.Primary.copy(alpha = 0.3f)
            )
        }
    }
}

/** Groupe les nutriments par catégorie selon leur type */
private fun grouperNutrimentsParCategorie(
        valeursNutritionnelles: Map<String, ValeurNutritionnelle>
): Map<String, List<Pair<String, ValeurNutritionnelle>>> {
    val groupes = mutableMapOf<String, MutableList<Pair<String, ValeurNutritionnelle>>>()

    valeursNutritionnelles.forEach { (nom, valeur) ->
        val categorie = determinerCategorieNutriment(nom, valeur.nutriment)
        groupes.getOrPut(categorie) { mutableListOf() }.add(nom to valeur)
    }

    // Trier les nutriments dans chaque catégorie selon l'ordre de l'enum
    return groupes.mapValues { (categorie, nutriments) ->
        trierNutrimentsParOrdreEnum(categorie, nutriments)
    }
}

/** Trie une liste de nutriments selon l'ordre défini dans l'énumération correspondante */
private fun trierNutrimentsParOrdreEnum(
        categorie: String,
        nutriments: List<Pair<String, ValeurNutritionnelle>>
): List<Pair<String, ValeurNutritionnelle>> {
    return when (categorie) {
        "BASE" -> {
            nutriments.sortedBy { (nom, _) -> obtenirOrdreNutrimentBase(nom) }
        }
        "MACRO" -> {
            nutriments.sortedBy { (nom, _) -> obtenirOrdreNutrimentMacro(nom) }
        }
        "MIN" -> {
            nutriments.sortedBy { (nom, _) -> obtenirOrdreNutrimentMin(nom) }
        }
        "VITAM" -> {
            nutriments.sortedBy { (nom, _) -> obtenirOrdreNutrimentVitam(nom) }
        }
        "LIPID" -> {
            nutriments.sortedBy { (nom, _) -> obtenirOrdreNutrimentLipid(nom) }
        }
        "AMA" -> {
            nutriments.sortedBy { (nom, _) -> obtenirOrdreNutrimentAA(nom) }
        }
        "ANA" -> {
            nutriments.sortedBy { (nom, _) -> obtenirOrdreNutrimentAnalysis(nom) }
        }
        else -> {
            // Pour les autres catégories, garder l'ordre alphabétique
            nutriments.sortedBy { it.first }
        }
    }
}

/** Obtient l'ordre d'un nutriment de base selon l'enum NutrientMain */
private fun obtenirOrdreNutrimentBase(nom: String): Int {
    return when (nom) {
        "HUMIDITE" -> 0
        "PROTEINE" -> 1
        "LIPIDE" -> 2
        "GLUCIDE" -> 3
        "ENA" -> 4
        "FIBRE" -> 5
        "CELLULOSE" -> 6
        "CENDRE" -> 7
        "ENERGIE" -> 8
        "SUCRE" -> 9
        "AMIDON" -> 10
        "FIBRESOL" -> 11
        "FIBRETOT" -> 12
        "NDF" -> 13
        "ADF" -> 14
        else -> 999 // Pour les nutriments non définis dans l'enum
    }
}

/** Obtient l'ordre d'un macronutriment selon l'enum NutrientMacro */
private fun obtenirOrdreNutrimentMacro(nom: String): Int {
    return when (nom) {
        "CAL" -> 0 // Calcium
        "PHOS" -> 1 // Phosphore
        "MG" -> 2 // Magnésium
        "NA" -> 3 // Sodium
        "K" -> 4 // Potassium
        "CHL" -> 5 // Chlore
        else -> 999
    }
}

/** Obtient l'ordre d'un minéral selon l'enum NutrientMin */
private fun obtenirOrdreNutrimentMin(nom: String): Int {
    return when (nom) {
        "FE" -> 0 // Fer
        "CU" -> 1 // Cuivre
        "ZN" -> 2 // Zinc
        "MN" -> 3 // Manganèse
        "I" -> 4 // Iode
        "SE" -> 5 // Sélénium
        else -> 999
    }
}

/** Obtient l'ordre d'une vitamine selon l'enum NutrientVitam */
private fun obtenirOrdreNutrimentVitam(nom: String): Int {
    return when (nom) {
        "VITA" -> 0
        "VITC" -> 1
        "VITD" -> 2
        "VITE" -> 3
        "VITK" -> 4
        "VITB1" -> 5
        "VITB2" -> 6
        "VITB3" -> 7
        "VITB5" -> 8
        "VITB6" -> 9
        "VITB8" -> 10
        "VITB9" -> 11
        "VITB12" -> 12
        "CHOLINE" -> 13
        "RETINOL" -> 14
        "BETACAR" -> 15
        else -> 999
    }
}

/** Obtient l'ordre d'un lipide selon l'enum NutrientLipid */
private fun obtenirOrdreNutrimentLipid(nom: String): Int {
    return when (nom) {
        "AGSATURE" -> 0
        "AGMONO" -> 1
        "AGPOLY" -> 2
        "AG40" -> 3
        "AG60" -> 4
        "AG80" -> 5
        "AG100" -> 6
        "AG120" -> 7
        "AG140" -> 8
        "AG160" -> 9
        "AG180" -> 10
        "AG181" -> 11
        "AG182" -> 12
        "AG183" -> 13
        "AG204" -> 14
        "AG205" -> 15
        "AG226" -> 16
        "CHOLES" -> 17
        "O3" -> 18
        "O6" -> 19
        "EPADHA" -> 20
        else -> 999
    }
}

/** Obtient l'ordre d'un acide aminé selon l'enum AAEnum */
private fun obtenirOrdreNutrimentAA(nom: String): Int {
    return when (nom) {
        "ALANINE" -> 0
        "ARGININE" -> 1
        "ASPARAGINE" -> 2
        "ASPARATE" -> 3
        "CYSTEINE" -> 4
        "GLUTAMATE" -> 5
        "GLUTAMINE" -> 6
        "GLYCINE" -> 7
        "HISTIDINE" -> 8
        "ISOLEUCINE" -> 9
        "LEUCINE" -> 10
        "LYSINE" -> 11
        "METHIONINE" -> 12
        "PHENYLALANINE" -> 13
        "PROLINE" -> 14
        "PYRROLYSINE" -> 15
        "SELENOCYSTEINE" -> 16
        "SERINE" -> 17
        "THREONINE" -> 18
        "TRYPTOPHANE" -> 19
        "TYROSINE" -> 20
        "VALINE" -> 21
        else -> 999
    }
}

/** Obtient l'ordre d'un nutriment d'analyse selon l'enum NutrientAnalysis */
private fun obtenirOrdreNutrimentAnalysis(nom: String): Int {
    return when (nom) {
        "KNA" -> 0 // Rapport K/NA
        "CAP" -> 1 // Rapport phosphocalcique
        "O6O3" -> 2 // Rapport omega 6/omega3
        "ZNCU" -> 3 // Rapport Zn/Cu
        "PROTP" -> 4 // Rapport Protéines/Phosphore
        "METHCYS" -> 5 // Methionine+cystéine
        "PHENTYR" -> 6 // Phénylalanine+tyrosine
        "nonOsPhos" -> 7 // Phosphore non osseux
        "nonOsProt" -> 8 // Proteine non osseuse
        "nonOsPP" -> 9 // Ratio Prot/phos non osseux
        else -> 999
    }
}

/** Détermine la catégorie d'un nutriment selon son nom et son type */
private fun determinerCategorieNutriment(nom: String, nutriment: Any): String {
    return when {
        // Nutriments de base
        nom in
                listOf(
                        "HUMIDITE",
                        "PROTEINE",
                        "LIPIDE",
                        "GLUCIDE",
                        "ENA",
                        "FIBRE",
                        "CELLULOSE",
                        "CENDRE",
                        "ENERGIE",
                        "SUCRE",
                        "AMIDON",
                        "FIBRESOL",
                        "FIBRETOT",
                        "NDF",
                        "ADF"
                ) -> "BASE"

        // Macronutriments
        nom in listOf("CAL", "PHOS", "MG", "NA", "K", "CHL") -> "MACRO"

        // Minéraux
        nom in listOf("FE", "CU", "ZN", "MN", "I", "SE") -> "MIN"

        // Vitamines
        nom.startsWith("VIT") || nom in listOf("CHOLINE", "RETINOL", "BETACAR") -> "VITAM"

        // Lipides
        nom in listOf("O3", "O6", "EPADHA", "AGSATURE", "AGMONO", "AGPOLY") ||
                nom.startsWith("AG") -> "LIPID"

        // Acides aminés
        nom in listOf("LYSINE", "METHIONINE", "TRYPTOPHANE", "METHCYS", "PHENTYR") -> "AMA"

        // Analyses/Ratios
        nom in listOf("KNA", "CAP", "O6O3", "ZNCU", "nonOsPhos", "nonOsProt", "nonOsPP", "PROTP") ->
                "ANA"

        // Autres
        else -> "OTHER"
    }
}

/** Traduit les codes de catégorie en titres lisibles */
private fun obtenirTitreCategorie(categorie: String): String {
    return when (categorie) {
        "BASE" -> "Nutriments de base"
        "MACRO" -> "Macroéléments"
        "MIN" -> "Oligoéléments"
        "VITAM" -> "Vitamines"
        "LIPID" -> "Acides gras"
        "AMA" -> "Acides aminés"
        "ANA" -> "Analyses/Ratios"
        "OTHER" -> "Autres"
        else -> "Divers"
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
        onClick: () -> Unit,
        typeExpressionBesoin: TypeExpressionBesoin?,
        referencesMaladies: List<ReferenceEv> = emptyList()
) {
    // Vérifier la conformité aux références
    val iconeConformite =
            obtenirIconeConformite(
                    valeurNutritionnelle,
                    referenceUtilisee,
                    besoinEnergetiqueEntretien,
                    poidsAnimal,
                    poidsMetabolique,
                    referencesMaladies
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

            // Valeur et unité selon les préférences d'expression
            val (valeurAffichee, uniteAffichee) =
                    calculerAffichageNutriment(
                            valeurNutritionnelle = valeurNutritionnelle,
                            typeExpressionBesoin = typeExpressionBesoin,
                            poidsMetabolique = poidsMetabolique,
                            poidsAnimal = poidsAnimal,
                            besoinEnergetiqueEntretien = besoinEnergetiqueEntretien
                    )

            Text(
                    text = "$valeurAffichee $uniteAffichee",
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
        poidsMetabolique: Double?,
        referencesMaladies: List<ReferenceEv> = emptyList()
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

    // Vérification des références de maladies (icône violette en cas de non-respect)
    referencesMaladies.forEach { refMaladie ->
        val nutrient = valeurNutritionnelle.nutriment
        val apportAbsolu = valeurNutritionnelle.valeur
        // Contrôle MIN/MAX maladie
        listOf(Reflevel.MIN, Reflevel.MAX).forEach { level ->
            if (refMaladie.contientNutriment(nutrient, level)) {
                val valeurRef = refMaladie.obtenirNutriment(nutrient, level)
                val uniteRef =
                        UnitReqEnum.getById(refMaladie.obtenirUniteNutriment(nutrient, level))
                val besoinAbsolu =
                        calculerBesoinAbsoluLocal(
                                valeurRef,
                                uniteRef,
                                besoinEnergetiqueEntretien,
                                poidsAnimal,
                                poidsMetabolique
                        )
                besoinAbsolu?.let { besoin ->
                    val violation =
                            (level == Reflevel.MIN && apportAbsolu < besoin) ||
                                    (level == Reflevel.MAX && apportAbsolu > besoin)
                    if (violation) {
                        return IconeConformite(
                                icone = Icons.Filled.Warning,
                                couleur = Color(0xFF9C27B0), // Violet
                                description = "Non conforme (réf. maladie)",
                                isCritical = true
                        )
                    }
                }
            }
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

    // Cas spécial: nutriments d'analyse/ratio sans unité (ex: CAP, KNA, O6O3...)
    // - Ne pas afficher d'unité
    // - Ne pas appliquer de transformation UnitReqEnum
    val isUnitEmpty = uniteOriginale.isBlank()
    val isAnalysis =
            valeurNutritionnelle.nutriment is fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis
    if (isAnalysis && isUnitEmpty) {
        return Pair(String.format("%.2f", valeurAbsolue), "")
    }

    // Si pas de type d'expression défini, affichage par défaut
    val typeExpression = typeExpressionBesoin ?: TypeExpressionBesoin.DEFAULT

    return when (typeExpression) {
        TypeExpressionBesoin.PAR_KG -> {
            // Par kg de poids vif
            poidsAnimal?.let { poids ->
                if (poids > 0) {
                    val valeurParKg = valeurAbsolue / poids
                    Pair(String.format("%.2f", valeurParKg), "$uniteOriginale/kg")
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
                    Pair(String.format("%.2f", valeurPar1000Kcal), "$uniteOriginale/1000 kcal")
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
                    Pair(String.format("%.2f", valeurPar1000Kj), "$uniteOriginale/1000 kJ")
                } else {
                    Pair(String.format("%.2f", valeurAbsolue), uniteOriginale)
                }
            }
                    ?: Pair(String.format("%.2f", valeurAbsolue), uniteOriginale)
        }
    }
}
