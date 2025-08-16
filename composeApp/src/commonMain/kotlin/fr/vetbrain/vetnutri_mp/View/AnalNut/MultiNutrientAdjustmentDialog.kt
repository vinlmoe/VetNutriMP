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
        var isLocked: Boolean = false,
        var mode: ConstraintMode = ConstraintMode.ADJUSTABLE,
        var weight: Double = 1.0,
        var minQuantity: Double = 0.0,
        var maxQuantity: Double = Double.MAX_VALUE
)

/** Modes de contrainte pour un aliment */
enum class ConstraintMode {
    ADJUSTABLE,
    FIXED,
    EXCLUDED
}

/** Contrainte appliquée lors du calcul */
data class AlimentConstraint(
        val alimentUuid: String,
        val mode: ConstraintMode,
        val weight: Double,
        val minQuantity: Double,
        val maxQuantity: Double
)

/** Paramètres de l'algorithme d'ajustement */
data class AdjustmentParams(
        val toleranceGrams: Double = 0.1,
        val roundTo: Double = 0.1,
        val energyLastRebalance: Boolean = true
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
                    val suggestion = suggestDefaultTargetNutrient(alimentRation)
                    AlimentAdjustmentData(
                            alimentRation = alimentRation,
                            selectedNutrient = suggestion,
                            isLocked = false
                    )
                }
        )
    }

    // Sélection du niveau de référence par nutriment (par défaut OPTIMIN)
    var refLevelByNutrient by remember { mutableStateOf<Map<String, Reflevel>>(emptyMap()) }
    var preview by remember { mutableStateOf<RationAdjustmentResult?>(null) }
    var params by remember { mutableStateOf(AdjustmentParams()) }
    var energyRebalanceNutrients by remember { mutableStateOf<Set<String>>(emptySet()) }

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
                    // Actions rapides
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)) {
                        OutlinedButton(
                                onClick = {
                                    adjustmentData =
                                            adjustmentData.map {
                                                it.copy(
                                                        mode = ConstraintMode.FIXED,
                                                        isLocked = true
                                                )
                                            }
                                }
                        ) { Text("Tout verrouiller") }
                        OutlinedButton(
                                onClick = {
                                    adjustmentData =
                                            adjustmentData.map {
                                                it.copy(
                                                        mode = ConstraintMode.ADJUSTABLE,
                                                        isLocked = false
                                                )
                                            }
                                }
                        ) { Text("Tout déverrouiller") }
                        OutlinedButton(
                                onClick = {
                                    // Auto-sélection pour ceux sans cible
                                    adjustmentData =
                                            adjustmentData.map { a ->
                                                if (a.selectedNutrient == null)
                                                        a.copy(
                                                                selectedNutrient =
                                                                        suggestDefaultTargetNutrient(
                                                                                a.alimentRation
                                                                        )
                                                        )
                                                else a
                                            }
                                }
                        ) { Text("Autoselect") }
                        OutlinedButton(
                                onClick = {
                                    // Réinit
                                    adjustmentData =
                                            ration.alimentMutableList.map { ar ->
                                                val s = suggestDefaultTargetNutrient(ar)
                                                AlimentAdjustmentData(
                                                        ar,
                                                        s,
                                                        false,
                                                        ConstraintMode.ADJUSTABLE,
                                                        1.0,
                                                        0.0,
                                                        Double.MAX_VALUE
                                                )
                                            }
                                    refLevelByNutrient = emptyMap()
                                    preview = null
                                }
                        ) { Text("Réinitialiser") }
                    }

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
                                                            isLocked = !alimentData.isLocked,
                                                            mode =
                                                                    if (alimentData.isLocked)
                                                                            ConstraintMode
                                                                                    .ADJUSTABLE
                                                                    else ConstraintMode.FIXED
                                                    )
                                            adjustmentData = updatedList
                                        }
                                    }
                            )
                        }
                    }

                    // Sélecteur du niveau de référence par nutriment sélectionné
                    val selectedLabels =
                            remember(adjustmentData) {
                                adjustmentData.mapNotNull { it.selectedNutrient }.distinct()
                            }
                    if (selectedLabels.isNotEmpty()) {
                        Text("Niveaux de référence", style = MaterialTheme.typography.subtitle2)
                        selectedLabels.forEach { label ->
                            Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(label)
                                var expanded by remember { mutableStateOf(false) }
                                val current = refLevelByNutrient[label] ?: Reflevel.OPTIMIN
                                Box {
                                    OutlinedButton(onClick = { expanded = true }) {
                                        Text(current.name)
                                    }
                                    DropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false }
                                    ) {
                                        Reflevel.entries.forEach { r ->
                                            DropdownMenuItem(
                                                    onClick = {
                                                        refLevelByNutrient =
                                                                refLevelByNutrient.toMutableMap()
                                                                        .apply { put(label, r) }
                                                        expanded = false
                                                    }
                                            ) { Text(r.name) }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (selectedLabels.isNotEmpty()) {
                        Divider()
                        Text(
                                "Rééquilibrage énergie: nutriments porteurs",
                                style = MaterialTheme.typography.subtitle2
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(AppSizes.paddingXSmall)) {
                            selectedLabels.forEach { label ->
                                val checked = energyRebalanceNutrients.contains(label)
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(label)
                                    Checkbox(
                                            checked = checked,
                                            onCheckedChange = { isChecked ->
                                                energyRebalanceNutrients =
                                                        if (isChecked)
                                                                energyRebalanceNutrients + label
                                                        else energyRebalanceNutrients - label
                                            }
                                    )
                                }
                            }
                        }
                    }

                    // Aperçu des deltas
                    preview?.let { pr ->
                        if (pr.adjustedAliments != null) {
                            Divider()
                            Text(
                                    "Prévisualisation des deltas",
                                    style = MaterialTheme.typography.subtitle2
                            )
                            pr.adjustedAliments.forEach { a ->
                                val orig = ration.alimentMutableList.find { it.uuid == a.uuid }
                                if (orig != null) {
                                    val delta = a.quantite - orig.quantite
                                    if (kotlin.math.abs(delta) > 0.001) {
                                        Text(
                                                "${orig.aliment?.nom ?: "Aliment"}: ${orig.quantite}g → ${a.quantite}g (Δ ${TextUtils.formatDecimal(delta.toDouble(),2)}g)"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                        onClick = {
                            val constraints =
                                    adjustmentData.map {
                                        AlimentConstraint(
                                                alimentUuid = it.alimentRation.uuid,
                                                mode = it.mode,
                                                weight = it.weight.coerceIn(0.0, 1.0),
                                                minQuantity = it.minQuantity.coerceAtLeast(0.0),
                                                maxQuantity =
                                                        if (it.maxQuantity <= 0.0) Double.MAX_VALUE
                                                        else it.maxQuantity
                                        )
                                    }
                            val result =
                                    calculateMultiNutrientAdjustment(
                                            ration = ration,
                                            referenceUtilisee = referenceUtilisee,
                                            besoinEnergetiqueTotal = besoinEnergetiqueTotal,
                                            adjustmentData = adjustmentData,
                                            poidsAnimal = poidsAnimal,
                                            poidsMetabolique = poidsMetabolique,
                                            refLevelByNutrient = refLevelByNutrient,
                                            constraints = constraints,
                                            energyRebalanceNutrients = energyRebalanceNutrients,
                                            params = params
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
                Row(horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)) {
                    OutlinedButton(
                            onClick = {
                                val constraints =
                                        adjustmentData.map {
                                            AlimentConstraint(
                                                    alimentUuid = it.alimentRation.uuid,
                                                    mode = it.mode,
                                                    weight = it.weight.coerceIn(0.0, 1.0),
                                                    minQuantity = it.minQuantity.coerceAtLeast(0.0),
                                                    maxQuantity =
                                                            if (it.maxQuantity <= 0.0)
                                                                    Double.MAX_VALUE
                                                            else it.maxQuantity
                                            )
                                        }
                                preview =
                                        calculateMultiNutrientAdjustment(
                                                ration = ration,
                                                referenceUtilisee = referenceUtilisee,
                                                besoinEnergetiqueTotal = besoinEnergetiqueTotal,
                                                adjustmentData = adjustmentData,
                                                poidsAnimal = poidsAnimal,
                                                poidsMetabolique = poidsMetabolique,
                                                refLevelByNutrient = refLevelByNutrient,
                                                constraints = constraints,
                                                energyRebalanceNutrients = energyRebalanceNutrients,
                                                params = params
                                        )
                            }
                    ) { Text("Prévisualiser") }
                    TextButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Annuler")
                    }
                }
            }
    )
}

/**
 * Suggère automatiquement le nutriment cible à ajuster pour un aliment, en adaptant la logique
 * fournie. Les comparaisons se font sur matière sèche (MS): 100 * valeur / (100 - HUMIDITE).
 */
private fun suggestDefaultTargetNutrient(alimentRation: AlimentRation): String? {
    val aliment = alimentRation.aliment ?: return null
    val kind: FoodKind? = aliment.typeAliment

    // Si aliment complet → par défaut PROTEINE (éviter ÉNERGIE qui est traitée globalement)
    if (kind == FoodKind.COMPLET) return NutrientMain.PROTEINE.label

    fun n(nutrient: Nutrient): Double {
        return aliment.valMap[nutrient]?.value ?: 0.0
    }

    val humidite: Double = n(NutrientMain.HUMIDITE).coerceIn(0.0, 99.9)
    val msDenom: Double = (100.0 - humidite).takeIf { it > 0.001 } ?: 100.0
    fun toMsPercent(value: Double): Double = (100.0 * value) / msDenom

    val cendreMs: Double = toMsPercent(n(NutrientMain.CENDRE))
    val proteineMs: Double = toMsPercent(n(NutrientMain.PROTEINE))
    val lipideMs: Double = toMsPercent(n(NutrientMain.LIPIDE))
    val enaMs: Double = toMsPercent(n(NutrientMain.ENA))
    val celluloseMs: Double = toMsPercent(n(NutrientMain.CELLULOSE))
    val o6Ms: Double = toMsPercent(n(NutrientLipid.O6))
    val epaDha: Double = n(NutrientLipid.EPADHA)

    // Adaptation des cibles: chaînes compatibles avec nos labels de nutriments pour pré-sélection
    // targetAdjust.CALCIUMPHOS → favoriser le Calcium (CAL) en pratique
    // targetAdjust.PROT → PROTEINE
    // targetAdjust.O6 → O6
    // targetAdjust.EPA → EPADHA
    // targetAdjust.LIP → LIPIDE
    // targetAdjust.FIBER → CELLULOSE
    // L'énergie est traitée globalement via besoin énergétique, ne pas la proposer ici.

    return when {
        // (Cendres/MS > 10) et présence de Calcium
        (cendreMs > 10.0 && aliment.valMap.containsKey(NutrientMacro.CAL)) ->
                NutrientMacro.CAL.label
        // Protéines/MS > 30
        (proteineMs > 30.0) -> NutrientMain.PROTEINE.label
        // Lipides/MS > 30 et O6/MS > 15
        (lipideMs > 30.0 && o6Ms > 15.0) -> NutrientLipid.O6.label
        // Lipides/MS > 30 et EPADHA > 0
        (lipideMs > 30.0 && epaDha > 0.0) -> NutrientLipid.EPADHA.label
        // Lipides/MS > 40
        (lipideMs > 40.0) -> NutrientMain.LIPIDE.label
        // Cellulose/MS > 20
        (celluloseMs > 20.0) -> NutrientMain.CELLULOSE.label
        // ENA/MS > 50 → énergie/carbs
        (enaMs > 50.0) -> NutrientMain.PROTEINE.label
        else -> null
    }
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
                val labels = mutableSetOf<String>()
                val valMap = aliment.valMap
                if (valMap != null) {
                    for ((nutr, qty) in valMap.entries) {
                        if (qty.value > 0) labels.add(nutr.label)
                    }
                }
                // Ne pas ajouter ÉNERGIE ici: l'énergie est ajustée globalement en fin de processus
                labels.toList().sorted()
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
        adjustmentData: List<AlimentAdjustmentData>,
        refLevelByNutrient: Map<String, Reflevel> = emptyMap(),
        constraints: List<AlimentConstraint> = emptyList(),
        energyRebalanceNutrients: Set<String> = emptySet(),
        params: AdjustmentParams = AdjustmentParams()
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
                adjustedAliments[i] = adjustedAliments[i].copy(quantite = 0.0)
            }
        }

        println(
                "🔍 DEBUG: ${alimentsVerrouilles.size} aliments verrouillés, ${adjustedAliments.size - alimentsVerrouilles.size} aliments mis à 0"
        )

        // Étape 2: Traiter les nutriments sélectionnés par l'utilisateur, avec ordre dynamique
        val nutrimentsTraites = mutableSetOf<String>()
        val processingOrder = buildProcessingOrderFromSelections(adjustmentData)

        for (nutrientLabel in processingOrder) {
            val nutrient = findNutrientByLabel(nutrientLabel)
            if (nutrient == null) {
                println("⚠️ DEBUG: Nutriment $nutrientLabel non trouvé")
                continue
            }

            // Calculer le besoin absolu en fonction du nutriment
            val besoinAbsoluGrammes: Double =
                    if (nutrient == NutrientMain.ENERGIE) {
                        // Pour l'énergie, la cible est le besoin énergétique total (kcal)
                        besoinEnergetiqueTotal
                    } else {
                        val rl = refLevelByNutrient[nutrientLabel] ?: Reflevel.OPTIMIN
                        val nutrimentRef = referenceUtilisee.obtenirNutrimentRef(nutrient, rl)
                        if (nutrimentRef == null || nutrimentRef.quantite <= 0) {
                            println("⚠️ DEBUG: Pas de référence pour $nutrientLabel")
                            continue
                        }
                        calculerBesoinAbsoluGrammes(
                                nutrimentRef = nutrimentRef,
                                poidsAnimal = poidsAnimal,
                                poidsMetabolique = poidsMetabolique,
                                besoinEnergetiqueTotal = besoinEnergetiqueTotal
                        )
                    }
            if (besoinAbsoluGrammes <= 0) {
                println("⚠️ DEBUG: Besoin nul pour $nutrientLabel: $besoinAbsoluGrammes")
                continue
            }

            // Trouver les aliments ajustables pour ce nutriment
            val constraintByUuid = constraints.associateBy { it.alimentUuid }
            val alimentsAjustables =
                    adjustmentData.filter {
                        it.selectedNutrient == nutrientLabel &&
                                !it.isLocked &&
                                (constraintByUuid[it.alimentRation.uuid]?.mode
                                        ?: it.mode) == ConstraintMode.ADJUSTABLE
                    }

            println(
                    "🔍 DEBUG: $nutrientLabel - Besoin: ${TextUtils.formatDecimal(besoinAbsoluGrammes, 2)}g, Aliments ajustables: ${alimentsAjustables.size}"
            )

            if (alimentsAjustables.isEmpty()) {
                println("⚠️ DEBUG: Aucun aliment ajustable pour $nutrientLabel")
                continue
            }

            // Étape 3: Calculer l'apport actuel du nutriment (avec TOUS les aliments déjà ajustés)
            var apportActuel = 0.0
            if (nutrient == NutrientMain.ENERGIE) {
                // Somme des kcal de chaque aliment
                for (i in adjustedAliments.indices) {
                    val alimentRation = adjustedAliments[i]
                    val densite = alimentRation.densiteEnergetique
                    if (densite > 0.0) {
                        apportActuel += densite * alimentRation.quantite.toDouble()
                    } else {
                        val energiePar100g =
                                alimentRation
                                        .aliment
                                        ?.valMap
                                        ?.get(NutrientMain.ENERGIE)
                                        ?.value
                                        ?.toDouble()
                                        ?: 0.0
                        if (energiePar100g > 0.0) {
                            apportActuel +=
                                    (energiePar100g * alimentRation.quantite.toDouble()) / 100.0
                        }
                    }
                }
            } else {
                for (i in adjustedAliments.indices) {
                    val alimentRation = adjustedAliments[i]
                    val aliment = alimentRation.aliment ?: continue
                    val quantiteNutriment: Double =
                            (aliment.valMap?.get(nutrient)?.value ?: 0.0).toDouble()
                    val quantiteAliment: Double = alimentRation.quantite.toDouble()
                    apportActuel += (quantiteNutriment * quantiteAliment) / 100.0
                }
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
                                alimentsVerrouilles = alimentsVerrouilles,
                                constraints = constraintByUuid
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

        // Rééquilibrage énergie si demandé
        if (params.energyLastRebalance) {
            val currentEnergy =
                    adjustedAliments.sumOf { ar ->
                        val densite = ar.densiteEnergetique
                        if (densite > 0.0) densite * ar.quantite.toDouble()
                        else {
                            val e =
                                    ar.aliment?.valMap?.get(NutrientMain.ENERGIE)?.value?.toDouble()
                                            ?: 0.0
                            (e * ar.quantite.toDouble()) / 100.0
                        }
                    }
            val diff = besoinEnergetiqueTotal - currentEnergy
            if (kotlin.math.abs(diff) > params.toleranceGrams) {
                // Distribuer sur aliments ajustables avec énergie disponible, pondéré par poids
                val energyCandidates =
                        adjustmentData.filter {
                            val c =
                                    constraints.find { cc ->
                                        cc.alimentUuid == it.alimentRation.uuid
                                    }
                            val allowedByNutrient =
                                    energyRebalanceNutrients.isEmpty() ||
                                            (it.selectedNutrient != null &&
                                                    energyRebalanceNutrients.contains(
                                                            it.selectedNutrient!!
                                                    ))
                            (c?.mode
                                    ?: it.mode) == ConstraintMode.ADJUSTABLE &&
                                    allowedByNutrient &&
                                    (it.alimentRation.densiteEnergetique > 0.0 ||
                                            (it.alimentRation.aliment?.valMap?.get(
                                                            NutrientMain.ENERGIE
                                                    )
                                                    ?.value
                                                    ?: 0.0) > 0.0)
                        }
                val totalWeight =
                        energyCandidates.sumOf {
                            (constraints
                                            .find { cc -> cc.alimentUuid == it.alimentRation.uuid }
                                            ?.weight
                                            ?: it.weight)
                                    .toDouble()
                                    .coerceAtLeast(0.0)
                        }
                if (totalWeight > 0.0) {
                    for (cand in energyCandidates) {
                        val idx =
                                adjustedAliments.indexOfFirst { it.uuid == cand.alimentRation.uuid }
                        if (idx < 0) continue
                        val w =
                                (constraints
                                                .find { cc ->
                                                    cc.alimentUuid == cand.alimentRation.uuid
                                                }
                                                ?.weight
                                                ?: cand.weight).coerceIn(0.0, 1.0)
                        val share = (w.toDouble() / totalWeight) * diff
                        val densite =
                                if (adjustedAliments[idx].densiteEnergetique > 0.0)
                                        adjustedAliments[idx].densiteEnergetique
                                else
                                        (adjustedAliments[idx]
                                                .aliment
                                                ?.valMap
                                                ?.get(NutrientMain.ENERGIE)
                                                ?.value
                                                ?.toDouble()
                                                ?: 0.0) / 100.0
                        if (densite > 0.0) {
                            val deltaQ: Double = share / densite
                            val currentQ: Double = adjustedAliments[idx].quantite.toDouble()
                            val c =
                                    constraints.find { cc ->
                                        cc.alimentUuid == cand.alimentRation.uuid
                                    }
                            val minQ: Double = (c?.minQuantity ?: 0.0).coerceAtLeast(0.0)
                            val maxQ: Double =
                                    (c?.maxQuantity ?: Double.MAX_VALUE).coerceAtLeast(0.0)
                            val newQ: Double = (currentQ + deltaQ).coerceIn(minQ, maxQ)
                            adjustedAliments[idx] = adjustedAliments[idx].copy(quantite = newQ)
                        }
                    }
                }
            }
        }

        // Arrondi final pour stabiliser l'UI
        for (i in adjustedAliments.indices) {
            val q: Double = adjustedAliments[i].quantite.toDouble()
            val step: Double = params.roundTo.toDouble()
            val rounded: Double = if (step > 0.0) kotlin.math.round(q / step) * step else q
            adjustedAliments[i] = adjustedAliments[i].copy(quantite = rounded)
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
    val quantiteEnGrammes: Double = quantite * uniteBase.conv.toDouble()

    // Calcul du besoin absolu en fonction de l'unité requise
    return when (uniteRequis) {
        UnitReqEnum.PERKG -> quantiteEnGrammes * (poidsAnimal ?: 0.0)
        UnitReqEnum.PERMS -> quantiteEnGrammes * (poidsMetabolique ?: 0.0)
        UnitReqEnum.PERKCAL -> (quantiteEnGrammes / 1000.0) * besoinEnergetiqueTotal
        else -> quantiteEnGrammes // Si c'est déjà en besoin journalier
    }
}

/** Obtient l'ordre de traitement des nutriments */
private fun buildProcessingOrderFromSelections(
        adjustmentData: List<AlimentAdjustmentData>
): List<String> {
    val selected = adjustmentData.mapNotNull { it.selectedNutrient }.toSet()
    if (selected.isEmpty()) return emptyList()

    val energyLabel = NutrientMain.ENERGIE.label
    val hasEnergy = selected.contains(energyLabel)

    fun isOf(label: String, group: Array<out Nutrient>): Boolean {
        return group.any { it.label == label }
    }

    val nonEnergy = selected.filter { it != energyLabel }

    val order = mutableListOf<String>()

    // Priorité: Macro > Minéraux > Vitamines > Lipides > AA > Autres (arbitré), trié par label
    // interne
    order += nonEnergy.filter { isOf(it, NutrientMacro.values()) }.sorted()
    order += nonEnergy.filter { isOf(it, NutrientMin.values()) }.sorted()
    order += nonEnergy.filter { isOf(it, NutrientVitam.values()) }.sorted()
    order += nonEnergy.filter { isOf(it, NutrientLipid.values()) }.sorted()
    order += nonEnergy.filter { isOf(it, AAEnum.values()) }.sorted()
    order += nonEnergy.filter { isOf(it, NutrientOther.values()) }.sorted()
    order += nonEnergy.filter { isOf(it, NutrientMain.values()) && it != energyLabel }.sorted()

    if (hasEnergy) order += energyLabel // Toujours en dernier

    // Dédupliquer en conservant l'ordre
    return order.distinct()
}

private fun buildProcessingOrderFromSelectionLabels(labels: List<String>): List<String> {
    val selected = labels.toSet()
    if (selected.isEmpty()) return emptyList()

    val energyLabel = NutrientMain.ENERGIE.label
    val hasEnergy = selected.contains(energyLabel)

    fun isOf(label: String, group: Array<out Nutrient>): Boolean {
        return group.any { it.label == label }
    }

    val nonEnergy = selected.filter { it != energyLabel }

    val order = mutableListOf<String>()

    order += nonEnergy.filter { isOf(it, NutrientMacro.values()) }.sorted()
    order += nonEnergy.filter { isOf(it, NutrientMin.values()) }.sorted()
    order += nonEnergy.filter { isOf(it, NutrientVitam.values()) }.sorted()
    order += nonEnergy.filter { isOf(it, NutrientLipid.values()) }.sorted()
    order += nonEnergy.filter { isOf(it, AAEnum.values()) }.sorted()
    order += nonEnergy.filter { isOf(it, NutrientOther.values()) }.sorted()
    order += nonEnergy.filter { isOf(it, NutrientMain.values()) && it != energyLabel }.sorted()

    if (hasEnergy) order += energyLabel
    return order.distinct()
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
            val quantiteNutriment: Double = (aliment.valMap?.get(nutrient)?.value ?: 0.0).toDouble()
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
                val nouvelleQuantite: Double = quantiteActuelle * ratio
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
        alimentsVerrouilles: Set<String>,
        constraints: Map<String, AlimentConstraint> = emptyMap()
): RationAdjustmentResult {
    try {
        // Filtrer les aliments disponibles pour l'ajustement
        val alimentsDisponibles =
                alimentsAjustables.filter { data ->
                    if (alimentsVerrouilles.contains(data.alimentRation.uuid)) return@filter false
                    val aliment = data.alimentRation.aliment ?: return@filter false
                    if (nutriment == NutrientMain.ENERGIE) {
                        val densite = data.alimentRation.densiteEnergetique
                        val energiePar100g = aliment.valMap[nutriment]?.value ?: 0.0
                        densite > 0.0 || energiePar100g > 0.0
                    } else {
                        (aliment.valMap[nutriment]?.value ?: 0.0) > 0.0
                    }
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
            val index = adjustedAliments.indexOfFirst { it.uuid == alimentData.alimentRation.uuid }
            if (index < 0) continue
            if (nutriment == NutrientMain.ENERGIE) {
                val densite = alimentData.alimentRation.densiteEnergetique
                if (densite > 0.0) {
                    val quantiteNecessaire = manque / densite
                    contributions.add(Triple(alimentData, quantiteNecessaire, index))
                } else {
                    val energiePar100g =
                            alimentData.alimentRation.aliment?.valMap?.get(nutriment)?.value ?: 0.0
                    if (energiePar100g > 0.0) {
                        val quantiteNecessaire = (manque * 100.0) / energiePar100g.toDouble()
                        contributions.add(Triple(alimentData, quantiteNecessaire, index))
                    }
                }
            } else {
                val aliment = alimentData.alimentRation.aliment ?: continue
                val quantiteNutriment: Double = (aliment.valMap[nutriment]?.value ?: 0.0).toDouble()
                if (quantiteNutriment > 0.0) {
                    // Quantité (g) nécessaire pour couvrir le manque (g) à partir de valeur/100g
                    val quantiteNecessaire = (manque * 100.0) / quantiteNutriment
                    contributions.add(Triple(alimentData, quantiteNecessaire, index))
                }
            }
        }

        if (contributions.isEmpty()) {
            return RationAdjustmentResult(
                    success = false,
                    message = "Aucune contribution possible pour le nutriment ${nutriment.label}"
            )
        }

        // Appliquer une pondération par poids (préférence d'usage) avant tri
        for (i in contributions.indices) {
            val (data, need, idx) = contributions[i]
            val w = (constraints[data.alimentRation.uuid]?.weight ?: data.weight).coerceIn(0.0, 1.0)
            val adjNeed = if (w > 0.0) need / w else Double.POSITIVE_INFINITY
            contributions[i] = Triple(data, adjNeed, idx)
        }
        // Trier par efficacité pondérée
        contributions.sortBy { it.second }

        // Répartir le manque entre les aliments les plus efficaces
        var manqueRestant = manque
        var indexContribution = 0
        var totalAjoute = 0.0

        while (manqueRestant > 0.01 && indexContribution < contributions.size) { // Tolérance
            val (alimentData, quantiteNecessaire, index) = contributions[indexContribution]
            val quantiteAAjouter =
                    if (nutriment == NutrientMain.ENERGIE) {
                        val densite = adjustedAliments[index].densiteEnergetique
                        if (densite > 0.0) {
                            if (indexContribution == contributions.size - 1) {
                                manqueRestant / densite
                            } else {
                                val alimentsRestants = contributions.size - indexContribution
                                (manqueRestant / densite) / alimentsRestants
                            }
                        } else {
                            val energiePar100g =
                                    adjustedAliments[index]
                                            .aliment
                                            ?.valMap
                                            ?.get(NutrientMain.ENERGIE)
                                            ?.value
                                            ?.toDouble()
                                            ?: 0.0
                            if (energiePar100g <= 0.0) 0.0
                            else if (indexContribution == contributions.size - 1) {
                                (manqueRestant * 100.0) / energiePar100g
                            } else {
                                val alimentsRestants = contributions.size - indexContribution
                                ((manqueRestant * 100.0) / energiePar100g) / alimentsRestants
                            }
                        }
                    } else {
                        val aliment = alimentData.alimentRation.aliment
                        val quantiteNutriment: Double =
                                (aliment?.valMap?.get(nutriment)?.value ?: 0.0).toDouble()
                        if (quantiteNutriment <= 0.0) 0.0
                        else if (indexContribution == contributions.size - 1) {
                            (manqueRestant * 100.0) / quantiteNutriment
                        } else {
                            val alimentsRestants = contributions.size - indexContribution
                            (manqueRestant * 100.0) / (quantiteNutriment * alimentsRestants)
                        }
                    }

            if (quantiteAAjouter > 0) {
                // Ajouter cette quantité à l'aliment en respectant min/max
                val quantiteActuelle: Double = adjustedAliments[index].quantite
                val minQ =
                        alimentsAjustables
                                .firstOrNull {
                                    it.alimentRation.uuid == adjustedAliments[index].uuid
                                }
                                ?.minQuantity
                                ?: 0.0
                val maxQ =
                        alimentsAjustables
                                .firstOrNull {
                                    it.alimentRation.uuid == adjustedAliments[index].uuid
                                }
                                ?.maxQuantity
                                ?: Double.MAX_VALUE
                val nouvelleQuantite: Double =
                        (quantiteActuelle + quantiteAAjouter).coerceIn(minQ, maxQ)
                adjustedAliments[index] = adjustedAliments[index].copy(quantite = nouvelleQuantite)

                // Mettre à jour le manque restant
                val apportAjoute: Double =
                        if (nutriment == NutrientMain.ENERGIE) {
                            val densite = adjustedAliments[index].densiteEnergetique
                            if (densite > 0.0) {
                                densite * quantiteAAjouter
                            } else {
                                val energiePar100g =
                                        adjustedAliments[index]
                                                .aliment
                                                ?.valMap
                                                ?.get(NutrientMain.ENERGIE)
                                                ?.value
                                                ?.toDouble()
                                                ?: 0.0
                                (energiePar100g * quantiteAAjouter) / 100.0
                            }
                        } else {
                            val aliment = alimentData.alimentRation.aliment
                            val quantiteNutriment: Double =
                                    (aliment?.valMap?.get(nutriment)?.value ?: 0.0).toDouble()
                            (quantiteNutriment * quantiteAAjouter) / 100.0
                        }
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
                adjustedAliments[i] = adjustedAliments[i].copy(quantite = 0.0)
            }
        }

        // Étape 2: Traiter les nutriments selon les sélections utilisateur
        val nutrimentsTraites = mutableSetOf<String>()
        val processingOrder =
                buildProcessingOrderFromSelectionLabels(alimentsParNutriment.keys.toList())

        for (nutrientLabel in processingOrder) {
            val besoinAbsolu = besoinsNutriments[nutrientLabel] ?: continue
            val alimentsAjustables = alimentsParNutriment[nutrientLabel] ?: continue
            val nutrient = findNutrientByLabel(nutrientLabel) ?: continue

            // Étape 3: Calculer l'apport actuel du nutriment (avec TOUS les aliments déjà ajustés)
            var apportActuel = 0.0
            for (i in adjustedAliments.indices) {
                val alimentRation = adjustedAliments[i]
                val aliment = alimentRation.aliment ?: continue
                val quantiteNutriment: Double =
                        (aliment.valMap?.get(nutrient)?.value ?: 0.0).toDouble()
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
