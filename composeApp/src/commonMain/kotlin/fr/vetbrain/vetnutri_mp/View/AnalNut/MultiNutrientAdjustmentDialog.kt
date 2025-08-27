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
import fr.vetbrain.vetnutri_mp.Data.PreferencesEspece
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.TextUtils
import kotlinx.coroutines.launch

/** Données d'ajustement pour un aliment spécifique */
data class AlimentAdjustmentData(
        val alimentRation: AlimentRation,
        var selectedNutrient: String? = null,
        var isLocked: Boolean = false,
        var mode: ConstraintMode = ConstraintMode.ADJUSTABLE,
        var weight: Double = 1.0,
        var minQuantity: Double = 0.0,
        var maxQuantity: Double = Double.MAX_VALUE,
        var isEnergyAdjustable: Boolean = true
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
        besoinEnergetiqueStandard: Double,
        poidsAnimal: Double?,
        poidsMetabolique: Double?,
        onConfirm: (RationAdjustmentResult) -> Unit,
        onDismiss: () -> Unit
) {
        var adjustmentData by remember {
                mutableStateOf(
                        ration.alimentMutableList.map { alimentRation ->
                                val suggestion =
                                        suggestDefaultTargetNutrient(
                                                alimentRation,
                                                referenceUtilisee
                                        )
                                AlimentAdjustmentData(
                                        alimentRation = alimentRation,
                                        selectedNutrient = suggestion,
                                        isLocked = false,
                                        isEnergyAdjustable = true
                                )
                        }
                )
        }

        // Sélection du niveau de référence par nutriment (par défaut OPTIMIN)
        var refLevelByNutrient by remember { mutableStateOf<Map<String, Reflevel>>(emptyMap()) }
        var preview by remember { mutableStateOf<RationAdjustmentResult?>(null) }
        var params by remember { mutableStateOf(AdjustmentParams()) }

        // Scope pour les coroutines
        val coroutineScope = rememberCoroutineScope()

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
                                Row(
                                        horizontalArrangement =
                                                Arrangement.spacedBy(AppSizes.paddingSmall)
                                ) {
                                        OutlinedButton(
                                                onClick = {
                                                        adjustmentData =
                                                                adjustmentData.map {
                                                                        it.copy(
                                                                                mode =
                                                                                        ConstraintMode
                                                                                                .FIXED,
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
                                                                                mode =
                                                                                        ConstraintMode
                                                                                                .ADJUSTABLE,
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
                                                                        if (a.selectedNutrient ==
                                                                                        null
                                                                        )
                                                                                a.copy(
                                                                                        selectedNutrient =
                                                                                                suggestDefaultTargetNutrient(
                                                                                                        a.alimentRation,
                                                                                                        referenceUtilisee
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
                                                                ration.alimentMutableList.map { ar
                                                                        ->
                                                                        val s =
                                                                                suggestDefaultTargetNutrient(
                                                                                        ar,
                                                                                        referenceUtilisee
                                                                                )
                                                                        AlimentAdjustmentData(
                                                                                ar,
                                                                                s,
                                                                                false,
                                                                                ConstraintMode
                                                                                        .ADJUSTABLE,
                                                                                1.0,
                                                                                0.0,
                                                                                Double.MAX_VALUE,
                                                                                true
                                                                        )
                                                                }
                                                        refLevelByNutrient = emptyMap()
                                                        preview = null
                                                }
                                        ) { Text("Réinitialiser") }
                                }

                                Text(
                                        text =
                                                "Sélectionnez les nutriments cibles pour chaque aliment :",
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.onSurface
                                )

                                LazyColumn(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement =
                                                Arrangement.spacedBy(AppSizes.paddingSmall)
                                ) {
                                        items(adjustmentData) { alimentData ->
                                                AlimentAdjustmentItem(
                                                        alimentData = alimentData,
                                                        referenceUtilisee = referenceUtilisee,
                                                        onNutrientChange = { newNutrient ->
                                                                val index =
                                                                        adjustmentData.indexOf(
                                                                                alimentData
                                                                        )
                                                                if (index != -1) {
                                                                        val updatedList =
                                                                                adjustmentData
                                                                                        .toMutableList()
                                                                        updatedList[index] =
                                                                                alimentData.copy(
                                                                                        selectedNutrient =
                                                                                                newNutrient
                                                                                )
                                                                        adjustmentData = updatedList
                                                                }
                                                        },
                                                        onLockToggle = {
                                                                val index =
                                                                        adjustmentData.indexOf(
                                                                                alimentData
                                                                        )
                                                                if (index != -1) {
                                                                        val updatedList =
                                                                                adjustmentData
                                                                                        .toMutableList()
                                                                        updatedList[index] =
                                                                                alimentData.copy(
                                                                                        isLocked =
                                                                                                !alimentData
                                                                                                        .isLocked,
                                                                                        mode =
                                                                                                if (alimentData
                                                                                                                .isLocked
                                                                                                )
                                                                                                        ConstraintMode
                                                                                                                .ADJUSTABLE
                                                                                                else
                                                                                                        ConstraintMode
                                                                                                                .FIXED
                                                                                )
                                                                        adjustmentData = updatedList
                                                                }
                                                        },
                                                        onEnergyAdjustableToggle = {
                                                                isEnergyAdjustable ->
                                                                val index =
                                                                        adjustmentData.indexOf(
                                                                                alimentData
                                                                        )
                                                                if (index != -1) {
                                                                        val updatedList =
                                                                                adjustmentData
                                                                                        .toMutableList()
                                                                        updatedList[index] =
                                                                                alimentData.copy(
                                                                                        isEnergyAdjustable =
                                                                                                isEnergyAdjustable
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
                                                adjustmentData
                                                        .mapNotNull { it.selectedNutrient }
                                                        .distinct()
                                        }
                                if (selectedLabels.isNotEmpty()) {
                                        Text(
                                                "Niveaux de référence",
                                                style = MaterialTheme.typography.subtitle2
                                        )
                                        selectedLabels.forEach { label ->
                                                Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement =
                                                                Arrangement.SpaceBetween,
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        Text(label)
                                                        var expanded by remember {
                                                                mutableStateOf(false)
                                                        }
                                                        val current =
                                                                refLevelByNutrient[label]
                                                                        ?: Reflevel.OPTIMIN

                                                        // Récupérer les niveaux disponibles pour ce
                                                        // nutriment spécifique
                                                        val availableRefLevels =
                                                                remember(label, referenceUtilisee) {
                                                                        val nutrient =
                                                                                findNutrientByLabel(
                                                                                        label
                                                                                )
                                                                        if (nutrient != null) {
                                                                                getAvailableRefLevelsForNutrient(
                                                                                        nutrient,
                                                                                        referenceUtilisee
                                                                                )
                                                                        } else {
                                                                                emptyList()
                                                                        }
                                                                }

                                                        // Vérifier que le niveau actuel est
                                                        // disponible, sinon utiliser le
                                                        // premier disponible
                                                        val currentRefLevel =
                                                                if (availableRefLevels.contains(
                                                                                current
                                                                        )
                                                                ) {
                                                                        current
                                                                } else if (availableRefLevels
                                                                                .isNotEmpty()
                                                                ) {
                                                                        // Mettre à jour la valeur
                                                                        // par défaut si le niveau
                                                                        // actuel n'est pas
                                                                        // disponible
                                                                        val newDefaultLevel =
                                                                                availableRefLevels
                                                                                        .first()
                                                                        refLevelByNutrient =
                                                                                refLevelByNutrient
                                                                                        .toMutableMap()
                                                                                        .apply {
                                                                                                put(
                                                                                                        label,
                                                                                                        newDefaultLevel
                                                                                                )
                                                                                        }
                                                                        newDefaultLevel
                                                                } else {
                                                                        Reflevel.OPTIMIN
                                                                }

                                                        Box {
                                                                OutlinedButton(
                                                                        onClick = {
                                                                                expanded = true
                                                                        }
                                                                ) { Text(currentRefLevel.name) }
                                                                DropdownMenu(
                                                                        expanded = expanded,
                                                                        onDismissRequest = {
                                                                                expanded = false
                                                                        }
                                                                ) {
                                                                        // Afficher seulement les
                                                                        // niveaux disponibles
                                                                        availableRefLevels
                                                                                .forEach { r ->
                                                                                        DropdownMenuItem(
                                                                                                onClick = {
                                                                                                        refLevelByNutrient =
                                                                                                                refLevelByNutrient
                                                                                                                        .toMutableMap()
                                                                                                                        .apply {
                                                                                                                                put(
                                                                                                                                        label,
                                                                                                                                        r
                                                                                                                                )
                                                                                                                        }
                                                                                                        expanded =
                                                                                                                false
                                                                                                }
                                                                                        ) {
                                                                                                Text(
                                                                                                        r.name
                                                                                                )
                                                                                        }
                                                                                }
                                                                }
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
                                                        val orig =
                                                                ration.alimentMutableList.find {
                                                                        it.uuid == a.uuid
                                                                }
                                                        if (orig != null) {
                                                                val delta =
                                                                        a.quantite - orig.quantite
                                                                if (kotlin.math.abs(delta) > 0.001
                                                                ) {
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
                                                                weight =
                                                                        it.weight.coerceIn(
                                                                                0.0,
                                                                                1.0
                                                                        ),
                                                                minQuantity =
                                                                        it.minQuantity
                                                                                .coerceAtLeast(0.0),
                                                                maxQuantity =
                                                                        if (it.maxQuantity <= 0.0)
                                                                                Double.MAX_VALUE
                                                                        else it.maxQuantity
                                                        )
                                                }
                                        coroutineScope.launch {
                                                val result =
                                                        calculateMultiNutrientAdjustment(
                                                                ration = ration,
                                                                referenceUtilisee =
                                                                        referenceUtilisee,
                                                                besoinEnergetiqueTotal =
                                                                        besoinEnergetiqueTotal,
                                                                besoinEnergetiqueStandard =
                                                                        besoinEnergetiqueStandard,
                                                                adjustmentData = adjustmentData,
                                                                poidsAnimal = poidsAnimal,
                                                                poidsMetabolique = poidsMetabolique,
                                                                refLevelByNutrient =
                                                                        refLevelByNutrient,
                                                                constraints = constraints,
                                                                params = params
                                                        )
                                                onConfirm(result)
                                        }
                                },
                                enabled =
                                        adjustmentData.any {
                                                it.selectedNutrient != null && !it.isLocked
                                        }
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
                                                                        alimentUuid =
                                                                                it.alimentRation
                                                                                        .uuid,
                                                                        mode = it.mode,
                                                                        weight =
                                                                                it.weight.coerceIn(
                                                                                        0.0,
                                                                                        1.0
                                                                                ),
                                                                        minQuantity =
                                                                                it.minQuantity
                                                                                        .coerceAtLeast(
                                                                                                0.0
                                                                                        ),
                                                                        maxQuantity =
                                                                                if (it.maxQuantity <=
                                                                                                0.0
                                                                                )
                                                                                        Double.MAX_VALUE
                                                                                else it.maxQuantity
                                                                )
                                                        }
                                                coroutineScope.launch {
                                                        preview =
                                                                calculateMultiNutrientAdjustment(
                                                                        ration = ration,
                                                                        referenceUtilisee =
                                                                                referenceUtilisee,
                                                                        besoinEnergetiqueTotal =
                                                                                besoinEnergetiqueTotal,
                                                                        besoinEnergetiqueStandard =
                                                                                besoinEnergetiqueStandard,
                                                                        adjustmentData =
                                                                                adjustmentData,
                                                                        poidsAnimal = poidsAnimal,
                                                                        poidsMetabolique =
                                                                                poidsMetabolique,
                                                                        refLevelByNutrient =
                                                                                refLevelByNutrient,
                                                                        constraints = constraints,
                                                                        params = params
                                                                )
                                                }
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
 *
 * @param alimentRation L'aliment ration pour lequel suggérer un nutriment cible
 * @param referenceUtilisee La référence utilisée pour vérifier la disponibilité des nutriments
 * @return Le label du nutriment suggéré ou null si aucun n'est disponible
 */
private fun suggestDefaultTargetNutrient(
        alimentRation: AlimentRation,
        referenceUtilisee: ReferenceEv
): String? {
        val aliment = alimentRation.aliment ?: return null
        val kind: FoodKind? = aliment.typeAliment

        // Si aliment complet → par défaut ÉNERGIE (ajustement énergétique par défaut)
        if (kind == FoodKind.COMPLET) return NutrientMain.ENERGIE.label

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

        // Adaptation des cibles: chaînes compatibles avec nos labels de nutriments pour
        // pré-sélection
        // targetAdjust.CALCIUMPHOS → favoriser le Calcium (CAL) en pratique
        // targetAdjust.PROT → PROTEINE
        // targetAdjust.O6 → O6
        // targetAdjust.EPA → EPADHA
        // targetAdjust.LIP → LIPIDE
        // targetAdjust.FIBER → CELLULOSE
        // L'énergie est maintenant proposée mais traitée en dernier dans l'ordre de traitement

        // Fonction helper pour vérifier si un nutriment a des références disponibles
        fun hasReferenceForNutrient(nutrient: Nutrient): Boolean {
                return Reflevel.entries.any { refLevel ->
                        referenceUtilisee.obtenirNutrimentRef(nutrient, refLevel) != null
                }
        }

        return when {
                // (Cendres/MS > 10) et présence de Calcium
                (cendreMs > 10.0 &&
                        aliment.valMap.containsKey(NutrientMacro.CAL) &&
                        hasReferenceForNutrient(NutrientMacro.CAL)) -> NutrientMacro.CAL.label
                // Protéines/MS > 30
                (proteineMs > 30.0 && hasReferenceForNutrient(NutrientMain.PROTEINE)) ->
                        NutrientMain.PROTEINE.label
                // Lipides/MS > 30 et O6/MS > 15
                (lipideMs > 30.0 && o6Ms > 15.0 && hasReferenceForNutrient(NutrientLipid.O6)) ->
                        NutrientLipid.O6.label
                // Lipides/MS > 30 et EPADHA > 0
                (lipideMs > 30.0 &&
                        epaDha > 0.0 &&
                        hasReferenceForNutrient(NutrientLipid.EPADHA)) -> NutrientLipid.EPADHA.label
                // Lipides/MS > 40
                (lipideMs > 40.0 && hasReferenceForNutrient(NutrientMain.LIPIDE)) ->
                        NutrientMain.LIPIDE.label
                // Cellulose/MS > 20
                (celluloseMs > 20.0 && hasReferenceForNutrient(NutrientMain.CELLULOSE)) ->
                        NutrientMain.CELLULOSE.label
                // ENA/MS > 50 → énergie/carbs
                (enaMs > 50.0 && hasReferenceForNutrient(NutrientMain.PROTEINE)) ->
                        NutrientMain.ENERGIE.label
                // Pour tous les autres cas (aliments inclassables) → ÉNERGIE par défaut
                // L'énergie est toujours disponible, pas besoin de vérifier les références
                else -> NutrientMain.ENERGIE.label
        }
}

/** Composant pour un aliment dans le dialog d'ajustement */
@Composable
private fun AlimentAdjustmentItem(
        alimentData: AlimentAdjustmentData,
        referenceUtilisee: ReferenceEv,
        onNutrientChange: (String?) -> Unit,
        onLockToggle: () -> Unit,
        onEnergyAdjustableToggle: (Boolean) -> Unit
) {
        val aliment = alimentData.alimentRation.aliment
        if (aliment == null) return

        // Filtrer les nutriments disponibles pour cet aliment spécifique
        val availableNutrients =
                remember(aliment, referenceUtilisee) {
                        val labels = mutableSetOf<String>()
                        val valMap = aliment.valMap
                        if (valMap != null) {
                                for ((nutr, qty) in valMap.entries) {
                                        if (qty.value > 0) {
                                                // Vérifier que le nutriment a au moins une
                                                // référence disponible
                                                val hasReference =
                                                        Reflevel.entries.any { refLevel ->
                                                                referenceUtilisee
                                                                        .obtenirNutrimentRef(
                                                                                nutr,
                                                                                refLevel
                                                                        ) != null
                                                        }
                                                if (hasReference) {
                                                        labels.add(nutr.label)
                                                }
                                        }
                                }
                        }
                        // Ajouter ÉNERGIE car elle peut maintenant être sélectionnée comme
                        // nutriment cible
                        // (l'énergie est toujours disponible, pas besoin de vérifier les
                        // références)
                        // L'énergie doit toujours être disponible pour tous les aliments
                        labels.add(NutrientMain.ENERGIE.label)

                        // Appliquer le même ordre de priorité que dans le calcul
                        val orderedLabels = mutableListOf<String>()

                        // 1er : NutrientMain (sans l'énergie)
                        val mainNutrients =
                                labels
                                        .filter { label ->
                                                NutrientMain.entries.any {
                                                        it.label == label &&
                                                                it != NutrientMain.ENERGIE
                                                }
                                        }
                                        .sorted()
                        orderedLabels.addAll(mainNutrients)

                        // 2ème : NutrientMacro
                        val macroNutrients =
                                labels
                                        .filter { label ->
                                                NutrientMacro.entries.any { it.label == label }
                                        }
                                        .sorted()
                        orderedLabels.addAll(macroNutrients)

                        // 3ème : NutrientMin
                        val minNutrients =
                                labels
                                        .filter { label ->
                                                NutrientMin.entries.any { it.label == label }
                                        }
                                        .sorted()
                        orderedLabels.addAll(minNutrients)

                        // 4ème : NutrientVitam
                        val vitamNutrients =
                                labels
                                        .filter { label ->
                                                NutrientVitam.entries.any { it.label == label }
                                        }
                                        .sorted()
                        orderedLabels.addAll(vitamNutrients)

                        // 5ème : NutrientLipid
                        val lipidNutrients =
                                labels
                                        .filter { label ->
                                                NutrientLipid.entries.any { it.label == label }
                                        }
                                        .sorted()
                        orderedLabels.addAll(lipidNutrients)

                        // 6ème : AAEnum
                        val aaNutrients =
                                labels
                                        .filter { label ->
                                                AAEnum.entries.any { it.label == label }
                                        }
                                        .sorted()
                        orderedLabels.addAll(aaNutrients)

                        // 7ème : NutrientOther
                        val otherNutrients =
                                labels
                                        .filter { label ->
                                                NutrientOther.entries.any { it.label == label }
                                        }
                                        .sorted()
                        orderedLabels.addAll(otherNutrients)

                        // Dernier : ENERGIE (toujours en dernier)
                        if (labels.contains(NutrientMain.ENERGIE.label)) {
                                orderedLabels.add(NutrientMain.ENERGIE.label)
                        }

                        orderedLabels
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
                                                        MaterialTheme.colors.onSurface.copy(
                                                                alpha = 0.5f
                                                        )
                                                else MaterialTheme.colors.onSurface
                                )

                                Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        // Case pour l'ajustement énergétique
                                        Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                                Text(
                                                        text = "Énergie",
                                                        style = MaterialTheme.typography.caption,
                                                        color =
                                                                MaterialTheme.colors.onSurface.copy(
                                                                        alpha = 0.7f
                                                                )
                                                )
                                                Checkbox(
                                                        checked = alimentData.isEnergyAdjustable,
                                                        onCheckedChange = { isChecked ->
                                                                onEnergyAdjustableToggle(isChecked)
                                                        },
                                                        enabled = !alimentData.isLocked,
                                                        modifier = Modifier.size(20.dp)
                                                )
                                        }

                                        // Bouton de verrouillage
                                        IconButton(
                                                onClick = onLockToggle,
                                                modifier = Modifier.size(24.dp)
                                        ) {
                                                Icon(
                                                        imageVector =
                                                                if (alimentData.isLocked)
                                                                        Icons.Filled.Lock
                                                                else Icons.Filled.LockOpen,
                                                        contentDescription =
                                                                if (alimentData.isLocked)
                                                                        "Déverrouiller"
                                                                else "Verrouiller",
                                                        tint =
                                                                if (alimentData.isLocked) Color.Gray
                                                                else VetNutriColors.Primary
                                                )
                                        }
                                }
                        }

                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Text(
                                        text =
                                                "Quantité actuelle: ${alimentData.alimentRation.quantite}g",
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
private suspend fun calculateMultiNutrientAdjustment(
        ration: Ration,
        referenceUtilisee: ReferenceEv,
        besoinEnergetiqueTotal: Double,
        besoinEnergetiqueStandard: Double,
        poidsAnimal: Double?,
        poidsMetabolique: Double?,
        adjustmentData: List<AlimentAdjustmentData>,
        refLevelByNutrient: Map<String, Reflevel> = emptyMap(),
        constraints: List<AlimentConstraint> = emptyList(),
        params: AdjustmentParams = AdjustmentParams(),
        preferences: PreferencesEspece? = null,
        equationRepository: fr.vetbrain.vetnutri_mp.Repository.EquationRepository? = null
): RationAdjustmentResult {
        try {
                // Créer une copie des aliments pour les ajustements
                val adjustedAliments = ration.alimentMutableList.map { it.copy() }.toMutableList()

                // Étape 1: Mettre tous les aliments à 0 (sauf ceux verrouillés)
                val alimentsVerrouilles = mutableSetOf<String>()

                for (i in adjustedAliments.indices) {
                        val alimentUuid = adjustedAliments[i].uuid
                        val estVerrouille =
                                adjustmentData.any {
                                        it.alimentRation.uuid == alimentUuid && it.isLocked
                                }

                        if (estVerrouille) {
                                alimentsVerrouilles.add(alimentUuid)
                        } else {
                                adjustedAliments[i] = adjustedAliments[i].copy(quantite = 0.0)
                        }
                }

                println(
                        "🔍 DEBUG: ${alimentsVerrouilles.size} aliments verrouillés, ${adjustedAliments.size - alimentsVerrouilles.size} aliments mis à 0"
                )

                // Étape 2: Traiter les nutriments sélectionnés par l'utilisateur, avec ordre
                // dynamique
                val nutrimentsTraites = mutableSetOf<String>()
                val processingOrder = buildProcessingOrderFromSelections(adjustmentData)

                println("🔍 DEBUG: Ordre de traitement des nutriments: $processingOrder")
                println("🔍 DEBUG: Nutriments sélectionnés dans adjustmentData:")
                adjustmentData.forEach { data ->
                        println(
                                "  - ${data.alimentRation.aliment?.nom}: sélectionné=${data.selectedNutrient}, énergie=${data.isEnergyAdjustable}"
                        )
                }

                // PREMIÈRE ÉTAPE : Ajuster tous les nutriments sauf l'énergie
                val nutrimentsNonEnergetiques = processingOrder.filter { it != "ENERGIE" }
                for (nutrientLabel in nutrimentsNonEnergetiques) {
                        val nutrient = findNutrientByLabel(nutrientLabel)
                        if (nutrient == null) {
                                println("⚠️ DEBUG: Nutriment $nutrientLabel non trouvé")
                                continue
                        }

                        println("🔍 DEBUG: === PREMIÈRE ÉTAPE : $nutrientLabel ===")

                        // Calculer le besoin absolu en fonction du nutriment
                        val rl = refLevelByNutrient[nutrientLabel] ?: Reflevel.OPTIMIN
                        val nutrimentRef = referenceUtilisee.obtenirNutrimentRef(nutrient, rl)
                        if (nutrimentRef == null || nutrimentRef.quantite <= 0) {
                                println("⚠️ DEBUG: Pas de référence pour $nutrientLabel")
                                continue
                        }

                        val besoinAbsoluGrammes =
                                calculerBesoinAbsoluGrammes(
                                        nutrimentRef = nutrimentRef,
                                        poidsAnimal = poidsAnimal,
                                        poidsMetabolique = poidsMetabolique,
                                        besoinEnergetiqueTotal = besoinEnergetiqueStandard
                                )

                        if (besoinAbsoluGrammes <= 0) {
                                println(
                                        "⚠️ DEBUG: Besoin nul pour $nutrientLabel: $besoinAbsoluGrammes"
                                )
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

                        if (alimentsAjustables.isEmpty()) {
                                println("⚠️ DEBUG: Aucun aliment ajustable pour $nutrientLabel")
                                continue
                        }

                        // Calculer l'apport actuel pour ce nutriment
                        var apportActuel = 0.0
                        for (i in adjustedAliments.indices) {
                                val alimentRation = adjustedAliments[i]
                                val aliment = alimentRation.aliment ?: continue
                                val quantiteNutriment: Double =
                                        (aliment.valMap?.get(nutrient)?.value ?: 0.0).toDouble()
                                val quantiteAliment: Double = alimentRation.quantite.toDouble()
                                apportActuel += (quantiteNutriment * quantiteAliment) / 100.0
                        }

                        val manque = besoinAbsoluGrammes - apportActuel
                        println(
                                "🔍 DEBUG: Nutriment $nutrientLabel - Besoin: ${TextUtils.formatDecimal(besoinAbsoluGrammes, 2)}g, Apport actuel: ${TextUtils.formatDecimal(apportActuel, 2)}g, Manque: ${TextUtils.formatDecimal(manque, 2)}g"
                        )

                        if (manque > 0.01) {
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
                                                constraints = constraintByUuid,
                                                referenceUtilisee = referenceUtilisee,
                                                preferences = preferences,
                                                equationRepository = equationRepository
                                        )

                                if (!result.success) {
                                        println(
                                                "❌ DEBUG: Échec ajustement $nutrientLabel - ${result.message}"
                                        )
                                        return result
                                } else {
                                        println(
                                                "✅ DEBUG: Succès ajustement $nutrientLabel - ${result.message}"
                                        )
                                }
                        } else {
                                println(
                                        "✅ DEBUG: Aucun ajustement nécessaire pour $nutrientLabel (manque: ${TextUtils.formatDecimal(manque, 2)}g)"
                                )
                        }

                        nutrimentsTraites.add(nutrientLabel)
                }

                // DEUXIÈME ÉTAPE : Ajuster l'énergie en recalculant l'apport total de la ration
                // finale
                if (processingOrder.contains("ENERGIE")) {
                        println("🔍 DEBUG: === DEUXIÈME ÉTAPE : AJUSTEMENT ÉNERGÉTIQUE ===")

                        // Créer une ration temporaire avec les ajustements effectués
                        val rationTemp = ration.copy()
                        for (i in adjustedAliments.indices) {
                                val alimentRation = adjustedAliments[i]
                                val alimentRationTemp =
                                        rationTemp.alimentMutableList.find {
                                                it.uuid == alimentRation.uuid
                                        }
                                if (alimentRationTemp != null) {
                                        // Créer une nouvelle instance avec la quantité modifiée
                                        val index =
                                                rationTemp.alimentMutableList.indexOf(
                                                        alimentRationTemp
                                                )
                                        if (index >= 0) {
                                                rationTemp.alimentMutableList[index] =
                                                        alimentRationTemp.copy(
                                                                quantite = alimentRation.quantite
                                                        )
                                        }
                                }
                        }

                        // Calculer l'apport énergétique total de la ration finale
                        var apportEnergetiqueTotal = 0.0
                        for (alimentRation in rationTemp.alimentMutableList) {
                                if (alimentRation.quantite > 0.01) {
                                        // Utiliser la même logique que le système principal pour
                                        // assurer la cohérence
                                        val energieAliment =
                                                alimentRation.getEnergie(
                                                        referenceUtilisee,
                                                        preferences,
                                                        equationRepository
                                                )
                                        apportEnergetiqueTotal += energieAliment
                                        println(
                                                "  - ${alimentRation.aliment?.nom}: ${TextUtils.formatDecimal(alimentRation.quantite, 2)}g → ${TextUtils.formatDecimal(energieAliment, 2)} kcal"
                                        )
                                }
                        }

                        println(
                                "🔍 DEBUG: Apport énergétique total de la ration finale: ${TextUtils.formatDecimal(apportEnergetiqueTotal, 2)} kcal"
                        )
                        println(
                                "🔍 DEBUG: Besoin énergétique: ${TextUtils.formatDecimal(besoinEnergetiqueTotal, 2)} kcal"
                        )

                        val manqueEnergie = besoinEnergetiqueTotal - apportEnergetiqueTotal
                        println(
                                "🔍 DEBUG: Manque énergétique: ${TextUtils.formatDecimal(manqueEnergie, 2)} kcal"
                        )

                        if (manqueEnergie > 0.01) {
                                // PRIORITÉ : Utiliser d'abord les aliments qui ont l'énergie comme
                                // nutriment
                                // principal
                                val constraintByUuid = constraints.associateBy { it.alimentUuid }

                                // 1. Aliments avec l'énergie comme nutriment principal (priorité
                                // maximale)
                                val alimentsEnergiePrincipale =
                                        adjustmentData.filter {
                                                it.selectedNutrient == "ENERGIE" &&
                                                        !it.isLocked &&
                                                        (constraintByUuid[it.alimentRation.uuid]
                                                                ?.mode
                                                                ?: it.mode) ==
                                                                ConstraintMode.ADJUSTABLE
                                        }

                                // 2. Aliments avec ajustement énergétique secondaire (priorité
                                // secondaire)
                                val alimentsEnergieSecondaire =
                                        adjustmentData.filter {
                                                it.selectedNutrient !=
                                                        "ENERGIE" && // Pas déjà traité comme
                                                        // nutriment principal
                                                        it.isEnergyAdjustable &&
                                                        !it.isLocked &&
                                                        (constraintByUuid[it.alimentRation.uuid]
                                                                ?.mode
                                                                ?: it.mode) ==
                                                                ConstraintMode.ADJUSTABLE
                                        }

                                // Combiner les deux listes avec priorité aux aliments énergie
                                // principale
                                val alimentsAjustablesEnergie =
                                        alimentsEnergiePrincipale + alimentsEnergieSecondaire

                                println(
                                        "🔍 DEBUG: Ajustement énergétique nécessaire - ${alimentsAjustablesEnergie.size} aliments ajustables"
                                )
                                println(
                                        "  - Aliments énergie principale: ${alimentsEnergiePrincipale.size}"
                                )
                                println(
                                        "  - Aliments énergie secondaire: ${alimentsEnergieSecondaire.size}"
                                )

                                val result =
                                        ajusterAlimentsPourNutriment(
                                                nutriment = NutrientMain.ENERGIE,
                                                manque = manqueEnergie,
                                                alimentsAjustables = alimentsAjustablesEnergie,
                                                adjustedAliments = adjustedAliments,
                                                alimentsVerrouilles = alimentsVerrouilles,
                                                constraints = constraintByUuid,
                                                referenceUtilisee = referenceUtilisee,
                                                preferences = preferences,
                                                equationRepository = equationRepository
                                        )

                                if (result.success) {
                                        println(
                                                "✅ DEBUG: Succès ajustement énergétique - ${result.message}"
                                        )
                                        nutrimentsTraites.add("ENERGIE")
                                } else {
                                        println(
                                                "❌ DEBUG: Échec ajustement énergétique - ${result.message}"
                                        )
                                        return RationAdjustmentResult(
                                                success = false,
                                                message =
                                                        "Échec de l'ajustement énergétique: ${result.message}",
                                                adjustedAliments = null
                                        )
                                }
                        } else {
                                println(
                                        "✅ DEBUG: Aucun ajustement énergétique nécessaire (surplus: ${TextUtils.formatDecimal(-manqueEnergie, 2)} kcal)"
                                )
                                nutrimentsTraites.add("ENERGIE")
                        }
                }

                // Rééquilibrage énergie final désactivé car l'énergie est maintenant traitée comme
                // les
                // autres nutriments
                // L'énergie est ajustée dans la boucle principale des nutriments
                /*
                if (params.energyLastRebalance) {
                    val currentEnergy =
                            adjustedAliments.sumOf { ar ->
                                // Utiliser la nouvelle logique d'énergie via ReferenceEv si disponible
                                ar.getEnergie(referenceUtilisee)
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
                                    (c?.mode
                                            ?: it.mode) == ConstraintMode.ADJUSTABLE &&
                                            it.isEnergyAdjustable &&
                                            (it.alimentRation.getEnergie(
                                                    referenceUtilisee,
                                                    preferences,
                                                    equationRepository
                                            ) > 0.0)
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
                */

                // Arrondi final pour stabiliser l'UI - arrondi au gramme
                for (i in adjustedAliments.indices) {
                        val q: Double = adjustedAliments[i].quantite.toDouble()
                        val rounded: Double = kotlin.math.round(q)
                        adjustedAliments[i] = adjustedAliments[i].copy(quantite = rounded)
                }

                println("✅ DEBUG: Traitement terminé pour ${nutrimentsTraites.size} nutriments")

                return RationAdjustmentResult(
                        success = true,
                        message =
                                "Ajustement séquentiel réussi pour ${nutrimentsTraites.size} nutriments",
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
        // L'énergie doit être traitée si elle est sélectionnée OU si au moins un aliment a
        // l'énergie
        // cochée
        val hasEnergy =
                selected.contains(energyLabel) || adjustmentData.any { it.isEnergyAdjustable }

        fun isOf(label: String, group: Array<out Nutrient>): Boolean {
                return group.any { it.label == label }
        }

        val nonEnergy = selected.filter { it != energyLabel }

        val order = mutableListOf<String>()

        // Priorité: Main (sans l'énergie) > Macro > Minéraux > Vitamines > Lipides > AA > Autres,
        // trié
        // par label
        order +=
                nonEnergy
                        .filter { isOf(it, NutrientMain.values()) && it != energyLabel }
                        .sorted() // 1er - Base de la ration
        order += nonEnergy.filter { isOf(it, NutrientMacro.values()) }.sorted() // 2ème
        order += nonEnergy.filter { isOf(it, NutrientMin.values()) }.sorted() // 3ème
        order += nonEnergy.filter { isOf(it, NutrientVitam.values()) }.sorted() // 4ème
        order += nonEnergy.filter { isOf(it, NutrientLipid.values()) }.sorted() // 5ème
        order += nonEnergy.filter { isOf(it, AAEnum.values()) }.sorted() // 6ème
        order += nonEnergy.filter { isOf(it, NutrientOther.values()) }.sorted() // 7ème

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

        order +=
                nonEnergy
                        .filter { isOf(it, NutrientMain.values()) && it != energyLabel }
                        .sorted() // 1er - Base de la ration
        order += nonEnergy.filter { isOf(it, NutrientMacro.values()) }.sorted() // 2ème
        order += nonEnergy.filter { isOf(it, NutrientMin.values()) }.sorted() // 3ème
        order += nonEnergy.filter { isOf(it, NutrientVitam.values()) }.sorted() // 4ème
        order += nonEnergy.filter { isOf(it, NutrientLipid.values()) }.sorted() // 5ème
        order += nonEnergy.filter { isOf(it, AAEnum.values()) }.sorted() // 6ème
        order += nonEnergy.filter { isOf(it, NutrientOther.values()) }.sorted() // 7ème

        if (hasEnergy) order += energyLabel // Toujours en dernier
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
                AAEnum.entries.any { it.label == label } ->
                        AAEnum.entries.find { it.label == label }
                else -> null
        }
}

/** Vérifie quels niveaux de référence sont disponibles pour un nutriment donné */
private fun getAvailableRefLevelsForNutrient(
        nutrient: Nutrient,
        referenceUtilisee: ReferenceEv
): List<Reflevel> {
        return Reflevel.entries.filter { refLevel ->
                referenceUtilisee.obtenirNutrimentRef(nutrient, refLevel) != null
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
                                (aliment.valMap?.get(nutrient)?.value ?: 0.0).toDouble()
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
                        val index =
                                adjustedAliments.indexOfFirst {
                                        it.uuid == alimentData.alimentRation.uuid
                                }
                        if (index >= 0) {
                                val quantiteActuelle: Double =
                                        alimentData.alimentRation.quantite.toDouble()
                                val nouvelleQuantite: Double = quantiteActuelle * ratio
                                // Arrondir au gramme
                                val nouvelleQuantiteArrondie: Double =
                                        kotlin.math.round(nouvelleQuantite)
                                adjustedAliments[index] =
                                        adjustedAliments[index].copy(
                                                quantite = nouvelleQuantiteArrondie
                                        )
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
        constraints: Map<String, AlimentConstraint> = emptyMap(),
        referenceUtilisee: ReferenceEv? = null,
        preferences: PreferencesEspece? = null,
        equationRepository: fr.vetbrain.vetnutri_mp.Repository.EquationRepository? = null
): RationAdjustmentResult {
        try {
                // Filtrer les aliments disponibles pour l'ajustement
                val alimentsDisponibles =
                        alimentsAjustables.filter { data ->
                                if (alimentsVerrouilles.contains(data.alimentRation.uuid))
                                        return@filter false
                                val aliment = data.alimentRation.aliment ?: return@filter false
                                if (nutriment == NutrientMain.ENERGIE) {
                                        // Utiliser la référence pour déterminer la disponibilité
                                        // énergétique
                                        val energiePar100gRef: Double =
                                                aliment.getNutrient(
                                                        NutrientMain.ENERGIE,
                                                        referenceUtilisee
                                                )
                                                        ?: 0.0
                                        val densite: Double =
                                                if (energiePar100gRef > 0.0)
                                                        energiePar100gRef / 100.0
                                                else data.alimentRation.densiteEnergetique
                                        densite > 0.0
                                } else {
                                        (aliment.valMap[nutriment]?.value ?: 0.0) > 0.0
                                }
                        }

                if (alimentsDisponibles.isEmpty()) {
                        return RationAdjustmentResult(
                                success = false,
                                message =
                                        "Aucun aliment disponible pour le nutriment ${nutriment.label}"
                        )
                }

                // Calculer la contribution potentielle de chaque aliment
                val contributions = mutableListOf<Triple<AlimentAdjustmentData, Double, Int>>()

                for (alimentData in alimentsDisponibles) {
                        val index =
                                adjustedAliments.indexOfFirst {
                                        it.uuid == alimentData.alimentRation.uuid
                                }
                        if (index < 0) continue
                        if (nutriment == NutrientMain.ENERGIE) {
                                val energiePar100gRef: Double =
                                        alimentData.alimentRation.aliment?.getNutrient(
                                                NutrientMain.ENERGIE,
                                                referenceUtilisee
                                        )
                                                ?: 0.0
                                val densite: Double =
                                        if (energiePar100gRef > 0.0) energiePar100gRef / 100.0
                                        else adjustedAliments[index].densiteEnergetique
                                if (densite > 0.0) {
                                        val quantiteNecessaire = manque / densite
                                        contributions.add(
                                                Triple(alimentData, quantiteNecessaire, index)
                                        )
                                }
                        } else {
                                val aliment = alimentData.alimentRation.aliment ?: continue
                                val quantiteNutriment: Double =
                                        (aliment.valMap[nutriment]?.value ?: 0.0).toDouble()
                                if (quantiteNutriment > 0.0) {
                                        // Quantité (g) nécessaire pour couvrir le manque (g) à
                                        // partir de valeur/100g
                                        val quantiteNecessaire =
                                                (manque * 100.0) / quantiteNutriment
                                        contributions.add(
                                                Triple(alimentData, quantiteNecessaire, index)
                                        )
                                }
                        }
                }

                if (contributions.isEmpty()) {
                        return RationAdjustmentResult(
                                success = false,
                                message =
                                        "Aucune contribution possible pour le nutriment ${nutriment.label}"
                        )
                }

                // Appliquer une pondération par poids (préférence d'usage) avant tri
                for (i in contributions.indices) {
                        val (data, need, idx) = contributions[i]
                        val w =
                                (constraints[data.alimentRation.uuid]?.weight ?: data.weight)
                                        .coerceIn(0.0, 1.0)
                        val adjNeed = if (w > 0.0) need / w else Double.POSITIVE_INFINITY
                        contributions[i] = Triple(data, adjNeed, idx)
                }
                // Trier par efficacité pondérée
                contributions.sortBy { it.second }

                // Répartir le manque entre les aliments les plus efficaces
                var manqueRestant = manque
                var indexContribution = 0
                var totalAjoute = 0.0

                while (manqueRestant > 0.01 &&
                        indexContribution < contributions.size) { // Tolérance
                        val (alimentData, quantiteNecessaire, index) =
                                contributions[indexContribution]
                        val quantiteAAjouter =
                                if (nutriment == NutrientMain.ENERGIE) {
                                        val energiePar100gRef: Double =
                                                adjustedAliments[index].aliment?.getNutrient(
                                                        NutrientMain.ENERGIE,
                                                        referenceUtilisee
                                                )
                                                        ?: 0.0
                                        val densite: Double =
                                                if (energiePar100gRef > 0.0)
                                                        energiePar100gRef / 100.0
                                                else adjustedAliments[index].densiteEnergetique
                                        if (densite <= 0.0) 0.0
                                        else if (indexContribution == contributions.size - 1) {
                                                manqueRestant / densite
                                        } else {
                                                val alimentsRestants =
                                                        contributions.size - indexContribution
                                                (manqueRestant / densite) / alimentsRestants
                                        }
                                } else {
                                        val aliment = alimentData.alimentRation.aliment
                                        val quantiteNutriment: Double =
                                                (aliment?.valMap?.get(nutriment)?.value ?: 0.0)
                                                        .toDouble()
                                        if (quantiteNutriment <= 0.0) 0.0
                                        else if (indexContribution == contributions.size - 1) {
                                                (manqueRestant * 100.0) / quantiteNutriment
                                        } else {
                                                val alimentsRestants =
                                                        contributions.size - indexContribution
                                                (manqueRestant * 100.0) /
                                                        (quantiteNutriment * alimentsRestants)
                                        }
                                }

                        if (quantiteAAjouter > 0) {
                                // Ajouter cette quantité à l'aliment en respectant min/max
                                val quantiteActuelle: Double = adjustedAliments[index].quantite
                                val minQ =
                                        alimentsAjustables
                                                .firstOrNull {
                                                        it.alimentRation.uuid ==
                                                                adjustedAliments[index].uuid
                                                }
                                                ?.minQuantity
                                                ?: 0.0
                                val maxQ =
                                        alimentsAjustables
                                                .firstOrNull {
                                                        it.alimentRation.uuid ==
                                                                adjustedAliments[index].uuid
                                                }
                                                ?.maxQuantity
                                                ?: Double.MAX_VALUE
                                val nouvelleQuantite: Double =
                                        (quantiteActuelle + quantiteAAjouter).coerceIn(minQ, maxQ)
                                // Arrondir au gramme
                                val nouvelleQuantiteArrondie: Double =
                                        kotlin.math.round(nouvelleQuantite)
                                adjustedAliments[index] =
                                        adjustedAliments[index].copy(
                                                quantite = nouvelleQuantiteArrondie
                                        )

                                // Mettre à jour le manque restant
                                val apportAjoute: Double =
                                        if (nutriment == NutrientMain.ENERGIE) {
                                                val energiePar100gRef: Double =
                                                        adjustedAliments[index].aliment
                                                                ?.getNutrient(
                                                                        NutrientMain.ENERGIE,
                                                                        referenceUtilisee
                                                                )
                                                                ?: 0.0
                                                val densite: Double =
                                                        if (energiePar100gRef > 0.0)
                                                                energiePar100gRef / 100.0
                                                        else
                                                                adjustedAliments[index]
                                                                        .densiteEnergetique
                                                densite * quantiteAAjouter
                                        } else {
                                                val aliment = alimentData.alimentRation.aliment
                                                val quantiteNutriment: Double =
                                                        (aliment?.valMap?.get(nutriment)?.value
                                                                        ?: 0.0).toDouble()
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
                        message =
                                "Erreur lors de l'ajustement pour ${nutriment.label}: ${e.message}"
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

                        // Étape 3: Calculer l'apport actuel du nutriment (avec TOUS les aliments
                        // déjà ajustés)
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

                        val unite = if (nutrientLabel == NutrientMain.ENERGIE.label) "kcal" else "g"
                        println(
                                "🔍 DEBUG: Nutriment $nutrientLabel - Besoin: ${TextUtils.formatDecimal(besoinAbsolu, 2)}$unite, Apport actuel: ${TextUtils.formatDecimal(apportActuel, 2)}$unite, Manque: ${TextUtils.formatDecimal(manque, 2)}$unite"
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
                                        println(
                                                "❌ DEBUG: Échec ajustement $nutrientLabel - ${result.message}"
                                        )
                                        return result
                                } else {
                                        println(
                                                "✅ DEBUG: Succès ajustement $nutrientLabel - ${result.message}"
                                        )
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
                        message =
                                "Ajustement séquentiel réussi pour ${nutrimentsTraites.size} nutriments"
                )
        } catch (e: Exception) {
                return RationAdjustmentResult(
                        success = false,
                        message = "Erreur lors de l'ajustement séquentiel: ${e.message}"
                )
        }
}
