package fr.vetbrain.vetnutri_mp.View.AnalNut

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.DropdownField
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.TextUtils

/** Données d'ajustement pour un aliment spécifique */
data class AlimentAdjustmentData(
        val alimentRation: AlimentRation,
        var selectedNutrient: String? = null,
        var isLocked: Boolean = false
)

/** Résultat d'un ajustement de ration */
data class RationAdjustmentResult(
        val success: Boolean,
        val message: String,
        val adjustedAliments: List<AlimentRation>? = null
)

/** Dialog pour l'ajustement multi-nutriments de la ration */
@Composable
fun MultiNutrientAdjustmentDialog(
        ration: Ration,
        referenceUtilisee: ReferenceEv,
        besoinEnergetiqueTotal: Double,
        poidsAnimal: Double?,
        poidsMetabolique: Double?,
        onConfirm: (RationAdjustmentResult) -> Unit,
        onDismiss: () -> Unit
) {
    var adjustmentData by remember {
        mutableStateOf(
                ration.alimentMutableList.map { alimentRation ->
                    AlimentAdjustmentData(alimentRation = alimentRation)
                }
        )
    }

    AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                        text = "Ajustement Multi-Nutriments",
                        style = MaterialTheme.typography.h6,
                        color = VetNutriColors.Primary
                )
            },
            text = {
                Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                ) {
                    Text(
                            text = "Sélectionnez les nutriments cibles pour chaque aliment :",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface
                    )

                    LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                    ) {
                        items(adjustmentData) { alimentData ->
                            AlimentAdjustmentItem(
                                    alimentData = alimentData,
                                    onNutrientChange = { newNutrient ->
                                        val index = adjustmentData.indexOf(alimentData)
                                        if (index != -1) {
                                            val updatedList = adjustmentData.toMutableList()
                                            updatedList[index] =
                                                    alimentData.copy(selectedNutrient = newNutrient)
                                            adjustmentData = updatedList
                                        }
                                    },
                                    onLockToggle = {
                                        val index = adjustmentData.indexOf(alimentData)
                                        if (index != -1) {
                                            val updatedList = adjustmentData.toMutableList()
                                            updatedList[index] =
                                                    alimentData.copy(
                                                            isLocked = !alimentData.isLocked
                                                    )
                                            adjustmentData = updatedList
                                        }
                                    }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                        onClick = {
                            val result =
                                    calculateMultiNutrientAdjustment(
                                            ration = ration,
                                            referenceUtilisee = referenceUtilisee,
                                            besoinEnergetiqueTotal = besoinEnergetiqueTotal,
                                            adjustmentData = adjustmentData,
                                            poidsAnimal = poidsAnimal,
                                            poidsMetabolique = poidsMetabolique
                                    )
                            onConfirm(result)
                        },
                        enabled = adjustmentData.any { it.selectedNutrient != null && !it.isLocked }
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ajuster")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Annuler")
                }
            }
    )
}

/** Composant pour un aliment dans le dialog d'ajustement */
@Composable
private fun AlimentAdjustmentItem(
        alimentData: AlimentAdjustmentData,
        onNutrientChange: (String?) -> Unit,
        onLockToggle: () -> Unit
) {
    val aliment = alimentData.alimentRation.aliment
    if (aliment == null) return

    // Filtrer les nutriments disponibles pour cet aliment spécifique
    val availableNutrients =
            remember(aliment) {
                val valMap = aliment.valMap
                if (valMap != null) {
                    val nutrientLabels = mutableListOf<String>()
                    for ((nutrientLabel, nutrientQuantity) in valMap.entries) {
                        if (nutrientQuantity.value > 0) {
                            nutrientLabels.add(nutrientLabel.label)
                        }
                    }
                    nutrientLabels.sorted()
                } else {
                    emptyList<String>()
                }
            }

    Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = AppSizes.elevationSmall,
            backgroundColor =
                    if (alimentData.isLocked) MaterialTheme.colors.surface.copy(alpha = 0.5f)
                    else MaterialTheme.colors.surface
    ) {
        Column(
                modifier = Modifier.padding(AppSizes.paddingSmall),
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingXSmall)
        ) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        text = aliment.nom ?: "Aliment inconnu",
                        style = MaterialTheme.typography.subtitle2,
                        fontWeight = FontWeight.Bold,
                        color =
                                if (alimentData.isLocked)
                                        MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                                else MaterialTheme.colors.onSurface
                )

                IconButton(onClick = onLockToggle, modifier = Modifier.size(24.dp)) {
                    Icon(
                            imageVector =
                                    if (alimentData.isLocked) Icons.Filled.Lock
                                    else Icons.Filled.LockOpen,
                            contentDescription =
                                    if (alimentData.isLocked) "Déverrouiller" else "Verrouiller",
                            tint = if (alimentData.isLocked) Color.Gray else VetNutriColors.Primary
                    )
                }
            }

            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        text = "Quantité actuelle: ${alimentData.alimentRation.quantite}g",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }

            if (!alimentData.isLocked) {
                DropdownField(
                        label = "Nutriment cible",
                        selectedValue = alimentData.selectedNutrient,
                        onValueChange = { value -> onNutrientChange(value) },
                        options = availableNutrients,
                        valueToString = { it },
                        enabled = !alimentData.isLocked,
                        modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                        text = "Verrouillé - Aucun ajustement",
                        style = MaterialTheme.typography.caption,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

/** Calcule l'ajustement multi-nutriments de la ration */
private fun calculateMultiNutrientAdjustment(
        ration: Ration,
        referenceUtilisee: ReferenceEv,
        besoinEnergetiqueTotal: Double,
        poidsAnimal: Double?,
        poidsMetabolique: Double?,
        adjustmentData: List<AlimentAdjustmentData>
): RationAdjustmentResult {
    try {
        // Créer une copie des aliments pour les ajustements
        val adjustedAliments = ration.alimentMutableList.map { it.copy() }.toMutableList()

        // Étape 1: Mettre tous les aliments à 0 (sauf ceux verrouillés)
        val alimentsVerrouilles = mutableSetOf<String>()

        for (i in adjustedAliments.indices) {
            val alimentUuid = adjustedAliments[i].uuid
            val estVerrouille =
                    adjustmentData.any { it.alimentRation.uuid == alimentUuid && it.isLocked }

            if (estVerrouille) {
                alimentsVerrouilles.add(alimentUuid)
            } else {
                adjustedAliments[i] = adjustedAliments[i].copy(quantite = 0.0f)
            }
        }

        println(
                "🔍 DEBUG: ${alimentsVerrouilles.size} aliments verrouillés, ${adjustedAliments.size - alimentsVerrouilles.size} aliments mis à 0"
        )

        // Étape 2: Traiter les nutriments dans l'ordre de priorité
        val nutrimentsTraites = mutableSetOf<String>()

        for (nutrientLabel in getNutrientProcessingOrder()) {
            val nutrient = findNutrientByLabel(nutrientLabel)
            if (nutrient == null) {
                println("⚠️ DEBUG: Nutriment $nutrientLabel non trouvé")
                continue
            }

            // Obtenir la référence complète du nutriment
            val refMin = referenceUtilisee.obtenirNutrimentRef(nutrient, Reflevel.MIN)
            val refOptiMin = referenceUtilisee.obtenirNutrimentRef(nutrient, Reflevel.OPTIMIN)

            val nutrimentRef = refOptiMin ?: refMin
            if (nutrimentRef == null || nutrimentRef.quantite <= 0) {
                println("⚠️ DEBUG: Pas de référence pour $nutrientLabel")
                continue
            }

            // Calculer le besoin absolu en grammes
            val besoinAbsoluGrammes =
                    calculerBesoinAbsoluGrammes(
                            nutrimentRef = nutrimentRef,
                            poidsAnimal = poidsAnimal,
                            poidsMetabolique = poidsMetabolique,
                            besoinEnergetiqueTotal = besoinEnergetiqueTotal
                    )
            if (besoinAbsoluGrammes <= 0) {
                println("⚠️ DEBUG: Besoin nul pour $nutrientLabel: $besoinAbsoluGrammes")
                continue
            }

            // Trouver les aliments ajustables pour ce nutriment
            val alimentsAjustables =
                    adjustmentData.filter { it.selectedNutrient == nutrientLabel && !it.isLocked }

            println(
                    "🔍 DEBUG: $nutrientLabel - Besoin: ${TextUtils.formatDecimal(besoinAbsoluGrammes, 2)}g, Aliments ajustables: ${alimentsAjustables.size}"
            )

            if (alimentsAjustables.isEmpty()) {
                println("⚠️ DEBUG: Aucun aliment ajustable pour $nutrientLabel")
                continue
            }

            // Étape 3: Calculer l'apport actuel du nutriment (avec TOUS les aliments déjà ajustés)
            var apportActuel = 0.0
            for (i in adjustedAliments.indices) {
                val alimentRation = adjustedAliments[i]
                val aliment = alimentRation.aliment ?: continue
                val quantiteNutriment: Double =
                        (aliment.valMap?.get(nutrient)?.value ?: 0.0f).toDouble()
                val quantiteAliment: Double = alimentRation.quantite.toDouble()
                apportActuel += (quantiteNutriment * quantiteAliment) / 100.0
            }

            // Étape 4: Calculer ce qui manque
            val manque = besoinAbsoluGrammes - apportActuel

            println(
                    "🔍 DEBUG: Nutriment $nutrientLabel - Besoin: ${TextUtils.formatDecimal(besoinAbsoluGrammes, 2)}g, Apport actuel: ${TextUtils.formatDecimal(apportActuel, 2)}g, Manque: ${TextUtils.formatDecimal(manque, 2)}g"
            )

            if (manque > 0.01) { // Tolérance de 0.01g
                // Étape 5: Ajuster les aliments pour couvrir le manque
                println(
                        "🔍 DEBUG: Ajustement nécessaire pour $nutrientLabel - ${alimentsAjustables.size} aliments ajustables"
                )
                val result =
                        ajusterAlimentsPourNutriment(
                                nutriment = nutrient,
                                manque = manque,
                                alimentsAjustables = alimentsAjustables,
                                adjustedAliments = adjustedAliments,
                                alimentsVerrouilles = alimentsVerrouilles
                        )

                if (!result.success) {
                    println("❌ DEBUG: Échec ajustement $nutrientLabel - ${result.message}")
                    return result
                } else {
                    println("✅ DEBUG: Succès ajustement $nutrientLabel - ${result.message}")
                }
            } else {
                println(
                        "✅ DEBUG: Aucun ajustement nécessaire pour $nutrientLabel (manque: ${TextUtils.formatDecimal(manque, 2)}g)"
                )
            }

            nutrimentsTraites.add(nutrientLabel)
        }

        println("✅ DEBUG: Traitement terminé pour ${nutrimentsTraites.size} nutriments")

        return RationAdjustmentResult(
                success = true,
                message = "Ajustement séquentiel réussi pour ${nutrimentsTraites.size} nutriments",
                adjustedAliments = adjustedAliments
        )
    } catch (e: Exception) {
        return RationAdjustmentResult(
                success = false,
                message = "Erreur lors de l'ajustement séquentiel: ${e.message}"
        )
    }
}

/** Calcule le besoin absolu en grammes pour un nutriment donné */
private fun calculerBesoinAbsoluGrammes(
        nutrimentRef: ReferenceEv.Nut4Ref,
        poidsAnimal: Double?,
        poidsMetabolique: Double?,
        besoinEnergetiqueTotal: Double
): Double {
    val quantite = nutrimentRef.quantite.toDouble()
    val uniteBase = nutrimentRef.unite
    val uniteRequis = nutrimentRef.uniteReq

    // Conversion initiale en grammes
    val quantiteEnGrammes = quantite * uniteBase.conv

    // Calcul du besoin absolu en fonction de l'unité requise
    return when (uniteRequis) {
        UnitReqEnum.PERKG -> quantiteEnGrammes * (poidsAnimal ?: 0.0)
        UnitReqEnum.PERMS -> quantiteEnGrammes * (poidsMetabolique ?: 0.0)
        UnitReqEnum.PERKCAL -> (quantiteEnGrammes / 1000.0) * besoinEnergetiqueTotal
        else -> quantiteEnGrammes // Si c'est déjà en besoin journalier
    }
}

/** Obtient l'ordre de traitement des nutriments */
private fun getNutrientProcessingOrder(): List<String> {
    val mainNutrients =
            NutrientMain.values()
                    .filter {
                        it != NutrientMain.ENERGIE
                    } // Exclure l'énergie pour la traiter en dernier
                    .map { it.label }
                    .sorted()

    return mainNutrients + NutrientMain.ENERGIE.label
}

/** Trouve un nutriment par son label */
private fun findNutrientByLabel(label: String): Nutrient? {
    return when {
        NutrientMain.entries.any { it.label == label } ->
                NutrientMain.entries.find { it.label == label }
        NutrientLipid.entries.any { it.label == label } ->
                NutrientLipid.entries.find { it.label == label }
        NutrientVitam.entries.any { it.label == label } ->
                NutrientVitam.entries.find { it.label == label }
        NutrientMacro.entries.any { it.label == label } ->
                NutrientMacro.entries.find { it.label == label }
        NutrientMin.entries.any { it.label == label } ->
                NutrientMin.entries.find { it.label == label }
        NutrientOther.entries.any { it.label == label } ->
                NutrientOther.entries.find { it.label == label }
        AAEnum.entries.any { it.label == label } -> AAEnum.entries.find { it.label == label }
        else -> null
    }
}

/** Ajuste la ration pour un nutriment spécifique */
private fun adjustRationForNutrient(
        alimentsAjustables: List<AlimentAdjustmentData>,
        adjustedAliments: MutableList<AlimentRation>,
        nutrient: Nutrient,
        besoinAbsoluGrammes: Double,
        referenceUtilisee: ReferenceEv
): RationAdjustmentResult {
    try {
        // Calculer l'apport actuel du nutriment
        var apportActuel = 0.0
        for (alimentData in alimentsAjustables) {
            val aliment = alimentData.alimentRation.aliment ?: continue
            val quantiteNutriment: Double =
                    (aliment.valMap?.get(nutrient)?.value ?: 0.0f).toDouble()
            val quantiteAliment: Double = alimentData.alimentRation.quantite.toDouble()
            apportActuel += (quantiteNutriment * quantiteAliment) / 100.0
        }

        if (apportActuel <= 0) {
            return RationAdjustmentResult(
                    success = false,
                    message = "Aucun apport en ${nutrient.label} détecté"
            )
        }

        // Calculer le ratio d'ajustement
        val ratio = besoinAbsoluGrammes / apportActuel

        // Appliquer l'ajustement aux aliments concernés
        for (alimentData in alimentsAjustables) {
            val index = adjustedAliments.indexOfFirst { it.uuid == alimentData.alimentRation.uuid }
            if (index >= 0) {
                val quantiteActuelle: Double = alimentData.alimentRation.quantite.toDouble()
                val nouvelleQuantite: Float = (quantiteActuelle * ratio).toFloat()
                adjustedAliments[index] = adjustedAliments[index].copy(quantite = nouvelleQuantite)
            }
        }

        return RationAdjustmentResult(
                success = true,
                message = "Ajustement pour ${nutrient.label} réussi"
        )
    } catch (e: Exception) {
        return RationAdjustmentResult(
                success = false,
                message = "Erreur lors de l'ajustement: ${e.message}"
        )
    }
}

/** Ajuste les aliments pour couvrir le manque d'un nutriment spécifique */
private fun ajusterAlimentsPourNutriment(
        nutriment: Nutrient,
        manque: Double,
        alimentsAjustables: List<AlimentAdjustmentData>,
        adjustedAliments: MutableList<AlimentRation>,
        alimentsVerrouilles: Set<String>
): RationAdjustmentResult {
    try {
        // Filtrer les aliments qui contiennent ce nutriment et ne sont pas verrouillés
        val alimentsDisponibles =
                alimentsAjustables.filter {
                    !alimentsVerrouilles.contains(it.alimentRation.uuid) &&
                            it.alimentRation.aliment?.valMap?.get(nutriment)?.value ?: 0.0f > 0.0f
                }

        if (alimentsDisponibles.isEmpty()) {
            return RationAdjustmentResult(
                    success = false,
                    message = "Aucun aliment disponible pour le nutriment ${nutriment.label}"
            )
        }

        // Calculer la contribution potentielle de chaque aliment
        val contributions = mutableListOf<Triple<AlimentAdjustmentData, Double, Int>>()

        for (alimentData in alimentsDisponibles) {
            val aliment = alimentData.alimentRation.aliment ?: continue
            val quantiteNutriment: Double =
                    (aliment.valMap?.get(nutriment)?.value ?: 0.0f).toDouble()
            val index = adjustedAliments.indexOfFirst { it.uuid == alimentData.alimentRation.uuid }

            if (index >= 0 && quantiteNutriment > 0) {
                // Calculer combien de grammes d'aliment il faut pour couvrir le manque
                val quantiteNecessaire = (manque * 100.0) / quantiteNutriment
                contributions.add(Triple(alimentData, quantiteNecessaire, index))
            }
        }

        if (contributions.isEmpty()) {
            return RationAdjustmentResult(
                    success = false,
                    message = "Aucune contribution possible pour le nutriment ${nutriment.label}"
            )
        }

        // Trier par efficacité (moins de grammes nécessaires = plus efficace)
        contributions.sortBy { it.second }

        // Répartir le manque entre les aliments les plus efficaces
        var manqueRestant = manque
        var indexContribution = 0
        var totalAjoute = 0.0

        while (manqueRestant > 0.01 &&
                indexContribution < contributions.size) { // Tolérance de 0.01g
            val (alimentData, quantiteNecessaire, index) = contributions[indexContribution]
            val aliment = alimentData.alimentRation.aliment ?: continue
            val quantiteNutriment: Double =
                    (aliment.valMap?.get(nutriment)?.value ?: 0.0f).toDouble()

            // Calculer la quantité à ajouter pour ce nutriment
            val quantiteAAjouter =
                    if (indexContribution == contributions.size - 1) {
                        // Dernier aliment : prendre tout le manque restant
                        (manqueRestant * 100.0) / quantiteNutriment
                    } else {
                        // Répartir équitablement entre les aliments restants
                        val alimentsRestants = contributions.size - indexContribution
                        (manqueRestant * 100.0) / (quantiteNutriment * alimentsRestants)
                    }

            if (quantiteAAjouter > 0) {
                // Ajouter cette quantité à l'aliment
                val quantiteActuelle: Double = adjustedAliments[index].quantite.toDouble()
                val nouvelleQuantite: Float = (quantiteActuelle + quantiteAAjouter).toFloat()
                adjustedAliments[index] = adjustedAliments[index].copy(quantite = nouvelleQuantite)

                // Mettre à jour le manque restant
                val apportAjoute = (quantiteNutriment * quantiteAAjouter) / 100.0
                manqueRestant -= apportAjoute
                totalAjoute += apportAjoute
            }

            indexContribution++
        }

        if (manqueRestant > 0.01) {
            return RationAdjustmentResult(
                    success = false,
                    message =
                            "Impossible de couvrir complètement le besoin en ${nutriment.label}. Manque: ${TextUtils.formatDecimal(manqueRestant, 2)}g, Ajouté: ${TextUtils.formatDecimal(totalAjoute, 2)}g"
            )
        }

        return RationAdjustmentResult(
                success = true,
                message =
                        "Ajustement réussi pour ${nutriment.label}: ajouté ${TextUtils.formatDecimal(totalAjoute, 2)}g"
        )
    } catch (e: Exception) {
        return RationAdjustmentResult(
                success = false,
                message = "Erreur lors de l'ajustement pour ${nutriment.label}: ${e.message}"
        )
    }
}

/** Ajuste la ration pour plusieurs nutriments de manière séquentielle */
private fun adjustRationForMultipleNutrients(
        besoinsNutriments: Map<String, Double>,
        alimentsParNutriment: Map<String, List<AlimentAdjustmentData>>,
        adjustedAliments: MutableList<AlimentRation>,
        referenceUtilisee: ReferenceEv
): RationAdjustmentResult {
    try {
        // Étape 1: Mettre tous les aliments à 0 (sauf ceux verrouillés)
        val alimentsVerrouilles = mutableSetOf<String>()

        for (i in adjustedAliments.indices) {
            val alimentUuid = adjustedAliments[i].uuid
            val estVerrouille =
                    alimentsParNutriment.values.flatten().any {
                        it.alimentRation.uuid == alimentUuid && it.isLocked
                    }

            if (estVerrouille) {
                alimentsVerrouilles.add(alimentUuid)
            } else {
                adjustedAliments[i] = adjustedAliments[i].copy(quantite = 0.0f)
            }
        }

        // Étape 2: Traiter les nutriments dans l'ordre de priorité
        val nutrimentsTraites = mutableSetOf<String>()

        for (nutrientLabel in getNutrientProcessingOrder()) {
            if (!besoinsNutriments.containsKey(nutrientLabel)) continue

            val nutrient = findNutrientByLabel(nutrientLabel)
            if (nutrient == null) continue

            val besoinAbsolu = besoinsNutriments[nutrientLabel] ?: continue
            val alimentsAjustables = alimentsParNutriment[nutrientLabel] ?: continue

            // Étape 3: Calculer l'apport actuel du nutriment (avec TOUS les aliments déjà ajustés)
            var apportActuel = 0.0
            for (i in adjustedAliments.indices) {
                val alimentRation = adjustedAliments[i]
                val aliment = alimentRation.aliment ?: continue
                val quantiteNutriment: Double =
                        (aliment.valMap?.get(nutrient)?.value ?: 0.0f).toDouble()
                val quantiteAliment: Double = alimentRation.quantite.toDouble()
                apportActuel += (quantiteNutriment * quantiteAliment) / 100.0
            }

            // Étape 4: Calculer ce qui manque
            val manque = besoinAbsolu - apportActuel

            println(
                    "🔍 DEBUG: Nutriment $nutrientLabel - Besoin: ${TextUtils.formatDecimal(besoinAbsolu, 2)}g, Apport actuel: ${TextUtils.formatDecimal(apportActuel, 2)}g, Manque: ${TextUtils.formatDecimal(manque, 2)}g"
            )

            if (manque > 0.01) { // Tolérance de 0.01g
                // Étape 5: Ajuster les aliments pour couvrir le manque
                println(
                        "🔍 DEBUG: Ajustement nécessaire pour $nutrientLabel - ${alimentsAjustables.size} aliments ajustables"
                )
                val result =
                        ajusterAlimentsPourNutriment(
                                nutriment = nutrient,
                                manque = manque,
                                alimentsAjustables = alimentsAjustables,
                                adjustedAliments = adjustedAliments,
                                alimentsVerrouilles = alimentsVerrouilles
                        )

                if (!result.success) {
                    println("❌ DEBUG: Échec ajustement $nutrientLabel - ${result.message}")
                    return result
                } else {
                    println("✅ DEBUG: Succès ajustement $nutrientLabel - ${result.message}")
                }
            } else {
                println(
                        "✅ DEBUG: Aucun ajustement nécessaire pour $nutrientLabel (manque: ${TextUtils.formatDecimal(manque, 2)}g)"
                )
            }

            nutrimentsTraites.add(nutrientLabel)
        }

        return RationAdjustmentResult(
                success = true,
                message = "Ajustement séquentiel réussi pour ${nutrimentsTraites.size} nutriments"
        )
    } catch (e: Exception) {
        return RationAdjustmentResult(
                success = false,
                message = "Erreur lors de l'ajustement séquentiel: ${e.message}"
        )
    }
}
