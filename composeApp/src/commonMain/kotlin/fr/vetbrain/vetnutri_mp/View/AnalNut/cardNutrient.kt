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
import kotlinx.coroutines.runBlocking

/**
 * Obtient le nom traduit d'un nutriment selon son type en utilisant les traductions JSON
 */
private fun obtenirNomTraduitNutriment(nom: String, nutriment: Any): String {
    return when (nutriment) {
        is NutrientLipid -> nutriment.translateEnum()
        is NutrientMacro -> nutriment.translateEnum()
        is NutrientMain -> nutriment.translateEnum()
        is NutrientMin -> nutriment.translateEnum()
        is NutrientOther -> nutriment.translateEnum()
        is NutrientVitam -> nutriment.translateEnum()
        is AAEnum -> nutriment.translateEnum()
        is NutrientAnalysis -> nutriment.translateEnum()
        else -> nom // Fallback sur le nom original si le type n'est pas reconnu
    }
}

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
        // Paramètres pour les préférences (maintenant optionnels)
        animal: AnimalEv? = null,
        preferencesRepository: PreferencesRepository? = null,
        equationRepository: EquationRepository? = null,
        // Nouveau paramètre pour les préférences pré-chargées
        typeExpressionBesoin: TypeExpressionBesoin? = null,
        // Paramètre pour adapter la hauteur selon la vue (large ou compacte)
        isLargeView: Boolean = false,
        // Références de maladies pour le contrôle et les graphes
        referencesMaladies: List<ReferenceEv> = emptyList()
) {
    // Etat pour basculer entre affichage filtré et complet
    var afficherTousLesNutriments by remember { mutableStateOf(false) }

    // Utiliser les préférences pré-chargées ou les charger localement si nécessaire
    var localTypeExpressionBesoin by remember { mutableStateOf<TypeExpressionBesoin?>(null) }
    var preferencesLoaded by remember { mutableStateOf(false) }

    // Priorité aux préférences pré-chargées
    val finalTypeExpressionBesoin = typeExpressionBesoin ?: localTypeExpressionBesoin

    LaunchedEffect(animal, preferencesRepository, typeExpressionBesoin) {
        // Si les préférences sont déjà fournies, les utiliser
        if (typeExpressionBesoin != null) {
            localTypeExpressionBesoin = typeExpressionBesoin
            preferencesLoaded = true
            return@LaunchedEffect
        }

        // Sinon, charger les préférences localement (fallback)
        animal?.let { animalData ->
            preferencesRepository?.let { repo ->
                try {
                    val preferences = repo.getPreferencesForSpecies(animalData.getEspece())
                    localTypeExpressionBesoin = preferences.getTypeExpressionBesoinEnum()
                    preferencesLoaded = true
                } catch (e: Exception) {
                    localTypeExpressionBesoin = TypeExpressionBesoin.DEFAULT
                    preferencesLoaded = false
                }
            }
                    ?: run {
                        localTypeExpressionBesoin = TypeExpressionBesoin.DEFAULT
                        preferencesLoaded = false
                    }
        }
                ?: run {
                    localTypeExpressionBesoin = TypeExpressionBesoin.DEFAULT
                    preferencesLoaded = false
                }
    }

    val valeursNutritionnellesBase =
            if (afficherTousLesNutriments ||
                            nutrimentsSelectionnes == null ||
                            nutrimentsSelectionnes.isEmpty()
            ) {
                // Utiliser les équations complémentaires depuis la ReferenceEv si disponible
                if (referenceUtilisee != null && equationRepository != null) {
                    // Créer les préférences de l'espèce à partir de l'animal
                    val preferencesEspece = animal?.let { 
                        runBlocking { preferencesRepository?.getPreferencesForSpecies(it.getEspece()) }
                    } ?: PreferencesEspece()
                    
                    
                    val resultat = runBlocking {
                        fr.vetbrain.vetnutri_mp.Data.analyserValeursNutritionnellesRationAvecEquations(
                                ration = ration,
                                preferencesEspece = preferencesEspece,
                                equationRepository = equationRepository,
                                referenceEv = referenceUtilisee
                        )
                    }
                    resultat
                } else {
                    analyserValeursNutritionnellesRation(ration)
                }
            } else {
                // Mode filtré: intégrer aussi les équations si disponibles via la ReferenceEv
                if (referenceUtilisee != null && equationRepository != null) {
                    // Créer les préférences de l'espèce à partir de l'animal
                    val preferencesEspece = animal?.let { 
                        runBlocking { preferencesRepository?.getPreferencesForSpecies(it.getEspece()) }
                    } ?: PreferencesEspece()
                    
                    
                    val resultat = runBlocking {
                        fr.vetbrain.vetnutri_mp.Data.analyserValeursNutritionnellesRationSelective(
                                ration = ration,
                                nutrimentsSelectionnes = nutrimentsSelectionnes,
                                preferencesEspece = preferencesEspece,
                                equationRepository = equationRepository,
                                referenceEv = referenceUtilisee
                        )
                    }
                    resultat
                } else {
                    runBlocking {
                        analyserValeursNutritionnellesRationSelective(ration, nutrimentsSelectionnes)
                    }
                }
            }

    // Les équations complémentaires sont déjà appliquées dans analyserValeursNutritionnellesRationAvecEquations
    // via getNutrientWithComplementary, donc on utilise directement les valeurs de base
    val valeursNutritionnelles = valeursNutritionnellesBase

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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                            text = "Analyse nutritionnelle",
                            style = MaterialTheme.typography.subtitle1,
                            fontWeight = FontWeight.Bold,
                            color = VetNutriColors.Primary
                    )

                    // Indicateur des préférences d'affichage
                    finalTypeExpressionBesoin?.let { typeExpr ->
                        Text(
                                text = "Affichage: ${typeExpr.displayName}",
                                style = MaterialTheme.typography.caption,
                                color =
                                        if (preferencesLoaded) VetNutriColors.Primary
                                        else VetNutriColors.Error,
                                modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

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
                                                                    finalTypeExpressionBesoin,
                                                            preferencesLoaded = preferencesLoaded
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
                                    val apport = valeur.valeur
                                    val typeExpr =
                                            finalTypeExpressionBesoin
                                                    ?: TypeExpressionBesoin.DEFAULT
                                    
                                    // Convertir la valeur selon l'unité des préférences pour le graphique bullet
                                    val apportConverti = when (typeExpr) {
                                        TypeExpressionBesoin.PAR_KG -> {
                                            poidsAnimal?.let { poids ->
                                                if (poids > 0) apport / poids else apport
                                            } ?: apport
                                        }
                                        TypeExpressionBesoin.PAR_KG_METABOLIQUE -> {
                                            poidsMetabolique?.let { poidsMetab ->
                                                if (poidsMetab > 0) apport / poidsMetab else apport
                                            } ?: apport
                                        }
                                        TypeExpressionBesoin.PAR_KCAL -> {
                                            besoinEnergetiqueEntretien?.let { bee ->
                                                if (bee > 0) (apport / bee) * 1000 else apport
                                            } ?: apport
                                        }
                                        TypeExpressionBesoin.PAR_KJ -> {
                                            besoinEnergetiqueEntretien?.let { bee ->
                                                if (bee > 0) {
                                                    val beeEnKj = bee * 4.184
                                                    (apport / beeEnKj) * 1000
                                                } else apport
                                            } ?: apport
                                        }
                                    }
                                    
                                    if (referenceUtilisee != null) {
                                        Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // Calculer l'affichage selon les préférences
                                            val (valeurAffichee, uniteAffichee) = calculerAffichageNutriment(
                                                    valeurNutritionnelle = valeur,
                                                    typeExpressionBesoin = typeExpr,
                                                    poidsMetabolique = poidsMetabolique,
                                                    poidsAnimal = poidsAnimal,
                                                    besoinEnergetiqueEntretien = besoinEnergetiqueEntretien
                                            )
                                            
                                            Column(
                                                    modifier = Modifier.width(200.dp)
                                            ) {
                                                Text(
                                                        text = obtenirNomTraduitNutriment(nom, valeur.nutriment),
                                                        style = MaterialTheme.typography.caption,
                                                        color =
                                                                MaterialTheme.colors.onSurface.copy(
                                                                        alpha = 0.8f
                                                                ),
                                                        fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                        text = "$valeurAffichee $uniteAffichee",
                                                        style = MaterialTheme.typography.overline,
                                                        color =
                                                                MaterialTheme.colors.onSurface.copy(
                                                                        alpha = 0.6f
                                                                )
                                                )
                                            }
                                            Box(modifier = Modifier.weight(1f)) {
                                                ReferenceBulletGraph(
                                                        valeurApport = apportConverti,
                                                        reference = referenceUtilisee,
                                                        nutriment = valeur.nutriment,
                                                        typeExpressionBesoin = typeExpr,
                                                        poidsAnimal = poidsAnimal,
                                                        poidsMetabolique = poidsMetabolique,
                                                        besoinEnergetiqueEntretien =
                                                                besoinEnergetiqueEntretien,
                                                        referencesMaladies = referencesMaladies,
                                                        onClick = { onNutrimentClick(nom, valeur) }
                                                )
                                            }
                                        }
                                    } else {
                                        // Calculer l'affichage selon les préférences même sans référence
                                        val (valeurAffichee, uniteAffichee) = calculerAffichageNutriment(
                                                valeurNutritionnelle = valeur,
                                                typeExpressionBesoin = typeExpr,
                                                poidsMetabolique = poidsMetabolique,
                                                poidsAnimal = poidsAnimal,
                                                besoinEnergetiqueEntretien = besoinEnergetiqueEntretien
                                        )
                                        
                                        Column {
                                                Text(
                                                        text = obtenirNomTraduitNutriment(nom, valeur.nutriment),
                                                        style = MaterialTheme.typography.caption,
                                                        color = MaterialTheme.colors.onSurface,
                                                        fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                        text = "$valeurAffichee $uniteAffichee",
                                                        style = MaterialTheme.typography.overline,
                                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                                )
                                        }
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
        preferencesLoaded: Boolean = false,
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
            if (preferencesLoaded) {
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
            } else {
                // Afficher un indicateur de chargement des préférences
                Text(
                        text = "Chargement des préférences...",
                        style = MaterialTheme.typography.overline,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                        maxLines = 2
                )
            }
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
        
        // Vérifier si c'est un nutriment de ratio (calculé par équation)
        val isNutrimentRatio = when (nutrient) {
            is fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis -> {
                // Les nutriments d'analyse avec une unité vide sont des ratios
                nutrient.unite.isEmpty()
            }
            else -> false
        }

        // Vérifier les minimums (MIN et OPTIMIN)
        listOf(Reflevel.MIN, Reflevel.OPTIMIN).forEach { level ->
            if (reference.contientNutriment(nutrient, level)) {
                hasReferences = true
                val valeurRef = reference.obtenirNutriment(nutrient, level)
                val uniteRef = UnitReqEnum.getById(reference.obtenirUniteNutriment(nutrient, level))

                val besoinAbsolu = if (isNutrimentRatio) {
                    // Pour les ratios, utiliser directement la valeur de référence
                    // car ils ne dépendent pas du poids ou de l'énergie
                    valeurRef
                } else {
                    calculerBesoinAbsoluLocal(
                            valeurRef,
                            uniteRef,
                            besoinEnergetiqueEntretien,
                            poidsAnimal,
                            poidsMetabolique
                    )
                }

                besoinAbsolu?.let { besoin ->
                    if (apportAbsolu < besoin) {
                        return IconeConformite(
                                icone = Icons.Filled.ArrowDownward, // Icône "↓" pour carence
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

                val besoinAbsolu = if (isNutrimentRatio) {
                    // Pour les ratios, utiliser directement la valeur de référence
                    // car ils ne dépendent pas du poids ou de l'énergie
                    valeurRef
                } else {
                    calculerBesoinAbsoluLocal(
                            valeurRef,
                            uniteRef,
                            besoinEnergetiqueEntretien,
                            poidsAnimal,
                            poidsMetabolique
                    )
                }

                besoinAbsolu?.let { besoin ->
                    if (apportAbsolu > besoin) {
                        return IconeConformite(
                                icone = Icons.Filled.ArrowUpward, // Icône "↑" pour excès
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
        
        // Vérifier si c'est un nutriment de ratio pour les références de maladies aussi
        val isNutrimentRatio = when (nutrient) {
            is fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis -> {
                // Les nutriments d'analyse avec une unité vide sont des ratios
                nutrient.unite.isEmpty()
            }
            else -> false
        }
        
        // Contrôle MIN/MAX maladie
        listOf(Reflevel.MIN, Reflevel.MAX).forEach { level ->
            if (refMaladie.contientNutriment(nutrient, level)) {
                val valeurRef = refMaladie.obtenirNutriment(nutrient, level)
                val uniteRef =
                        UnitReqEnum.getById(refMaladie.obtenirUniteNutriment(nutrient, level))
                val besoinAbsolu = if (isNutrimentRatio) {
                    // Pour les ratios, utiliser directement la valeur de référence
                    // car ils ne dépendent pas du poids ou de l'énergie
                    valeurRef
                } else {
                    calculerBesoinAbsoluLocal(
                            valeurRef,
                            uniteRef,
                            besoinEnergetiqueEntretien,
                            poidsAnimal,
                            poidsMetabolique
                    )
                }
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
        valeurRef: Double,
        uniteRef: UnitReqEnum,
        besoinEnergetiqueEntretien: Double?,
        poidsAnimal: Double?,
        poidsMetabolique: Double?
): Double? {
    return when (uniteRef) {
        UnitReqEnum.PERKCAL ->
                besoinEnergetiqueEntretien?.let { bee: Double -> (valeurRef * bee) / 1000.0 }
        UnitReqEnum.PERKJ ->
                besoinEnergetiqueEntretien?.let { bee: Double ->
                    val beeEnKj: Double = bee * 4.184
                    (valeurRef * beeEnKj) / 1000.0
                }
        UnitReqEnum.PERKG -> poidsAnimal?.let { poids: Double -> valeurRef * poids }
        UnitReqEnum.PERMS -> poidsMetabolique?.let { poidsMetab: Double -> valeurRef * poidsMetab }
        UnitReqEnum.ABSOLUTE -> valeurRef
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
        return Pair(TextUtils.formatDecimal(valeurAbsolue, 2), "")
    }

    // Si pas de type d'expression défini, affichage par défaut
    val typeExpression = typeExpressionBesoin ?: TypeExpressionBesoin.DEFAULT

    return when (typeExpression) {
        TypeExpressionBesoin.PAR_KG -> {
            // Par kg de poids vif
            poidsAnimal?.let { poids ->
                if (poids > 0) {
                    val valeurParKg = valeurAbsolue / poids
                    Pair(TextUtils.formatDecimal(valeurParKg, 2), "$uniteOriginale/kg")
                } else {
                    // Si pas de poids disponible, garder l'unité originale mais indiquer le type
                    // d'expression
                    Pair(
                            TextUtils.formatDecimal(valeurAbsolue, 2),
                            "$uniteOriginale (par kg si poids disponible)"
                    )
                }
            }
                    ?: Pair(
                            TextUtils.formatDecimal(valeurAbsolue, 2),
                            "$uniteOriginale (par kg si poids disponible)"
                    )
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
                    // Si pas de poids métabolique disponible, garder l'unité originale mais
                    // indiquer le type d'expression
                    Pair(
                            TextUtils.formatDecimal(valeurAbsolue, 2),
                            "$uniteOriginale (par kg^0.75 si poids métabolique disponible)"
                    )
                }
            }
                    ?: Pair(
                            TextUtils.formatDecimal(valeurAbsolue, 2),
                            "$uniteOriginale (par kg^0.75 si poids métabolique disponible)"
                    )
        }
        TypeExpressionBesoin.PAR_KCAL -> {
            // Par 1000 kcal de BEE (Besoin Énergétique d'Entretien)
            besoinEnergetiqueEntretien?.let { bee ->
                if (bee > 0) {
                    val valeurPar1000Kcal = (valeurAbsolue / bee) * 1000
                    Pair(TextUtils.formatDecimal(valeurPar1000Kcal, 2), "$uniteOriginale/1000 kcal")
                } else {
                    // Si pas de BEE disponible, garder l'unité originale mais indiquer le type
                    // d'expression
                    Pair(
                            TextUtils.formatDecimal(valeurAbsolue, 2),
                            "$uniteOriginale (par 1000 kcal si BEE disponible)"
                    )
                }
            }
                    ?: Pair(
                            TextUtils.formatDecimal(valeurAbsolue, 2),
                            "$uniteOriginale (par 1000 kcal si BEE disponible)"
                    )
        }
        TypeExpressionBesoin.PAR_KJ -> {
            // Par 1000 kJ de BEE (conversion : 1 kcal = 4.184 kJ)
            besoinEnergetiqueEntretien?.let { bee ->
                if (bee > 0) {
                    val beeEnKj = bee * 4.184 // Conversion kcal vers kJ
                    val valeurPar1000Kj = (valeurAbsolue / beeEnKj) * 1000
                    Pair(TextUtils.formatDecimal(valeurPar1000Kj, 2), "$uniteOriginale/1000 kJ")
                } else {
                    // Si pas de BEE disponible, garder l'unité originale mais indiquer le type
                    // d'expression
                    Pair(
                            TextUtils.formatDecimal(valeurAbsolue, 2),
                            "$uniteOriginale (par 1000 kJ si BEE disponible)"
                    )
                }
            }
                    ?: Pair(
                            TextUtils.formatDecimal(valeurAbsolue, 2),
                            "$uniteOriginale (par 1000 kJ si BEE disponible)"
                    )
        }
    }
}
